/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsTaintedNode;

@NodeChildren({
        @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class ToStringObjectNode extends FormatNode {

    private final String conversionMethod;

    @Child private CallDispatchHeadNode toStrNode;
    @Child private IsTaintedNode isTaintedNode;

    private final ConditionProfile taintedProfile = ConditionProfile.createBinaryProfile();

    public ToStringObjectNode(String conversionMethod) {
        this.conversionMethod = conversionMethod;
        this.isTaintedNode = IsTaintedNode.create();
    }

    @Specialization(guards = "isNil(nil)")
    public DynamicObject toStringString(DynamicObject nil) {
        return nil();
    }

    @Specialization(guards = "isRubyString(string)")
    public Object toStringString(VirtualFrame frame, DynamicObject string) {
        if (taintedProfile.profile(isTaintedNode.executeIsTainted(string))) {
            setTainted(frame);
        }

        return string;
    }

    @Specialization(guards = "!isRubyString(object)")
    public Object toString(VirtualFrame frame, Object object) {
        final Object value = getToStrNode().call(frame, object, conversionMethod);

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                setTainted(frame);
            }

            return value;
        }

        throw new NoImplicitConversionException(object, "String");
    }

    private CallDispatchHeadNode getToStrNode() {
        if (toStrNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStrNode = insert(CallDispatchHeadNode.createReturnMissing());
        }
        return toStrNode;
    }

}
