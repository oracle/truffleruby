/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

/**
 * Converts a method name to a Java String.
 * The exception message below assumes this conversion is done for a method name.
 */
@ImportStatic({ StringCachingGuards.class, StringOperations.class })
@GenerateUncached
public abstract class NameToJavaStringNode extends RubyBaseWithoutContextNode {

    // FIXME (pitr 12-Jun-2019): find a different way
    @NodeChild(value = "value", type = RubyNode.class)
    public static abstract class RubyNodeWrapperNode extends RubyNode {
        @Specialization
        protected Object call(Object value,
                @Cached NameToJavaStringNode toJavaString) {
            return toJavaString.executeToJavaString(value);
        }
    }

    public static NameToJavaStringNode create() {
        return NameToJavaStringNodeGen.create();
    }

    public abstract String executeToJavaString(Object name);

    @Specialization(guards = "isRubyString(value)")
    protected String stringNameToJavaString(DynamicObject value,
            @Cached @Shared("toJavaStringNode") ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(value);
    }

    @Specialization(guards = "isRubySymbol(value)")
    protected String symbolNameToJavaString(DynamicObject value,
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
            @Cached("createPrivate()") CallDispatchHeadNode toStr) {
        final Object coerced;

        try {
            coerced = toStr.call(object, "to_str");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == context
                    .getCoreLibrary()
                    .getNoMethodErrorClass()) {
                throw new RaiseException(context, context.getCoreExceptions().typeError(
                        StringUtils.toString(object) + " is not a symbol nor a string",
                        this));
            } else {
                throw e;
            }
        }

        if (RubyGuards.isRubyString(coerced)) {
            return StringOperations.getString((DynamicObject) coerced);
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
