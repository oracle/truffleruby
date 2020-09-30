/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;

/** Converts a method name to a Java String. The exception message below assumes this conversion is done for a method
 * name. */
@ImportStatic({ StringCachingGuards.class, StringOperations.class })
@GenerateUncached
@NodeChild(value = "value", type = RubyNode.class)
public abstract class NameToJavaStringNode extends RubySourceNode {

    public static NameToJavaStringNode create() {
        return NameToJavaStringNodeGen.create(null);
    }

    public static NameToJavaStringNode create(RubyNode name) {
        return NameToJavaStringNodeGen.create(name);
    }

    public static NameToJavaStringNode getUncached() {
        return NameToJavaStringNodeGen.getUncached();
    }

    public abstract String execute(Object name);

    @Specialization
    protected String stringNameToJavaString(RubyString value,
            @Cached @Shared("toJavaStringNode") ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(value);
    }

    @Specialization
    protected String symbolNameToJavaString(RubySymbol value,
            @Cached @Shared("toJavaStringNode") ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(value);
    }

    @Specialization
    protected String nameToJavaString(String value) {
        return value;
    }

    @Specialization(guards = { "!isString(object)", "!isRubySymbol(object)", "!isRubyString(object)" })
    protected String nameToJavaString(Object object,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached BranchProfile errorProfile,
            @Cached DispatchNode toStr) {
        final Object coerced;

        try {
            coerced = toStr.call(object, "to_str");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == context.getCoreLibrary().noMethodErrorClass) {
                throw new RaiseException(context, context.getCoreExceptions().typeError(
                        Utils.concat(object, " is not a symbol nor a string"),
                        this));
            } else {
                throw e;
            }
        }

        if (RubyGuards.isRubyString(coerced)) {
            return ((RubyString) coerced).getJavaString();
        } else {
            errorProfile.enter();
            throw new RaiseException(context, context.getCoreExceptions().typeErrorBadCoercion(
                    object,
                    "String",
                    "to_str",
                    coerced,
                    this));
        }
    }

}
