/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

@ImportStatic({ StringCachingGuards.class, StringOperations.class })
@NodeChild(value = "value", type = RubyNode.class)
public abstract class NameToJavaStringNode extends RubyNode {

    public static NameToJavaStringNode create() {
        return NameToJavaStringNodeGen.create(null);
    }

    public abstract String executeToJavaString(VirtualFrame frame, Object name);

    @Specialization(guards = "isRubyString(value)")
    public String stringNameToJavaString(VirtualFrame frame, DynamicObject value,
                                         @Cached("create()") ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(frame, value);
    }

    @Specialization(guards = "isRubySymbol(value)")
    public String symbolNameToJavaString(VirtualFrame frame, DynamicObject value,
                                         @Cached("create()") ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(frame, value);
    }

    @Specialization
    public String nameToJavaString(String value) {
        return value;
    }

    @Specialization(guards = { "!isString(object)", "!isRubySymbol(object)", "!isRubyString(object)" })
    public String nameToJavaString(VirtualFrame frame, Object object,
                                    @Cached("create()") BranchProfile errorProfile,
                                    @Cached("create()") CallDispatchHeadNode toStr) {
        final Object coerced;

        try {
            coerced = toStr.call(frame, object, "to_str");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().getNoMethodErrorClass()) {
                throw new RaiseException(coreExceptions().typeErrorNoImplicitConversion(object, "String", this));
            } else {
                throw e;
            }
        }

        if (RubyGuards.isRubyString(coerced)) {
            return StringOperations.getString((DynamicObject) coerced);
        } else {
            errorProfile.enter();
            throw new RaiseException(coreExceptions().typeErrorBadCoercion(object, "String", "to_str", coerced, this));
        }
    }

}
