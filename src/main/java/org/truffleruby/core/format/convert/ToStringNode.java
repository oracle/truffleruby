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
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsTaintedNode;

import java.nio.charset.StandardCharsets;

@NodeChildren({
        @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class ToStringNode extends FormatNode {

    protected final boolean convertNumbersToStrings;
    private final String conversionMethod;
    private final boolean inspectOnConversionFailure;
    private final Object valueOnNil;

    @Child private CallDispatchHeadNode toStrNode;
    @Child private CallDispatchHeadNode toSNode;
    @Child private KernelNodes.ToSNode inspectNode;
    @Child private IsTaintedNode isTaintedNode;

    private final ConditionProfile taintedProfile = ConditionProfile.createBinaryProfile();

    public ToStringNode(boolean convertNumbersToStrings,
                        String conversionMethod, boolean inspectOnConversionFailure,
                        Object valueOnNil) {
        this.convertNumbersToStrings = convertNumbersToStrings;
        this.conversionMethod = conversionMethod;
        this.inspectOnConversionFailure = inspectOnConversionFailure;
        this.valueOnNil = valueOnNil;
        this.isTaintedNode = IsTaintedNode.create();
    }

    public abstract Object executeToString(VirtualFrame frame, Object object);

    @Specialization(guards = "isNil(nil)")
    public Object toStringNil(Object nil) {
        return valueOnNil;
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    public byte[] toString(int value) {
        return Integer.toString(value).getBytes(StandardCharsets.US_ASCII);
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    public byte[] toString(long value) {
        return Long.toString(value).getBytes(StandardCharsets.US_ASCII);
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    public byte[] toString(double value) {
        return Double.toString(value).getBytes(StandardCharsets.US_ASCII);
    }

    @Specialization(guards = "isRubyString(string)")
    public byte[] toStringString(VirtualFrame frame, DynamicObject string,
            @Cached("create()") RopeNodes.BytesNode bytesNode) {
        if (taintedProfile.profile(isTaintedNode.executeIsTainted(string))) {
            setTainted(frame);
        }
        if ("inspect".equals(conversionMethod)) {
            final Object value = getToStrNode().call(string, conversionMethod);

            if (RubyGuards.isRubyString(value)) {
                if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                    setTainted(frame);
                }

                return bytesNode.execute(Layouts.STRING.getRope((DynamicObject) value));
            } else {
                throw new NoImplicitConversionException(string, "String");
            }
        }
        return bytesNode.execute(Layouts.STRING.getRope(string));
    }

    @Specialization(guards = "isRubyArray(array)")
    public byte[] toString(VirtualFrame frame, DynamicObject array,
            @Cached("create()") RopeNodes.BytesNode bytesNode) {
        if (toSNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toSNode = insert(CallDispatchHeadNode.createReturnMissing());
        }

        final Object value = toSNode.call(array, "to_s");

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                setTainted(frame);
            }

            return bytesNode.execute(Layouts.STRING.getRope((DynamicObject) value));
        } else {
            throw new NoImplicitConversionException(array, "String");
        }
    }

    @Specialization(guards = {"!isRubyString(object)", "!isRubyArray(object)", "!isForeignObject(object)"})
    public byte[] toString(VirtualFrame frame, Object object,
            @Cached("create()") RopeNodes.BytesNode bytesNode) {
        final Object value = getToStrNode().call(object, conversionMethod);

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                setTainted(frame);
            }

            return bytesNode.execute(Layouts.STRING.getRope((DynamicObject) value));
        }

        if (inspectOnConversionFailure) {
            if (inspectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                inspectNode = insert(KernelNodes.ToSNode.create());
            }

            return bytesNode.execute(Layouts.STRING.getRope(inspectNode.toS(object)));
        } else {
            throw new NoImplicitConversionException(object, "String");
        }
    }

    @TruffleBoundary
    @Specialization(guards = "!isRubyBasicObject(object)")
    public byte[] toString(TruffleObject object) {
        return object.toString().getBytes(StandardCharsets.UTF_8);
    }

    private CallDispatchHeadNode getToStrNode() {
        if (toStrNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStrNode = insert(CallDispatchHeadNode.createReturnMissing());
        }
        return toStrNode;
    }

}
