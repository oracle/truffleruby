/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.cext.StringCharPointerAdapter;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;
import org.truffleruby.language.dispatch.MissingBehavior;
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
    public byte[] toStringString(VirtualFrame frame, DynamicObject string) {
        if (taintedProfile.profile(isTaintedNode.executeIsTainted(string))) {
            setTainted(frame);
        }

        return Layouts.STRING.getRope(string).getBytes();
    }

    @Specialization(guards = "isRubyArray(array)")
    public byte[] toString(VirtualFrame frame, DynamicObject array) {
        if (toSNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toSNode = insert(DispatchHeadNodeFactory.createMethodCall(true,
                    MissingBehavior.RETURN_MISSING));
        }

        final Object value = toSNode.call(frame, array, "to_s");

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                setTainted(frame);
            }

            return Layouts.STRING.getRope((DynamicObject) value).getBytes();
        } else {
            throw new NoImplicitConversionException(array, "String");
        }
    }

    @Specialization(guards = {"!isRubyString(object)", "!isRubyArray(object)", "!isForeignObject(object)"})
    public byte[] toString(VirtualFrame frame, Object object) {
        if (toStrNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStrNode = insert(DispatchHeadNodeFactory.createMethodCall(true,
                    MissingBehavior.RETURN_MISSING));
        }

        final Object value = toStrNode.call(frame, object, conversionMethod);

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(isTaintedNode.executeIsTainted(value))) {
                setTainted(frame);
            }

            return Layouts.STRING.getRope((DynamicObject) value).getBytes();
        }

        if (inspectOnConversionFailure) {
            if (inspectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                inspectNode = insert(KernelNodesFactory.ToSNodeFactory.create(null));
            }

            return Layouts.STRING.getRope(inspectNode.toS(object)).getBytes();
        } else {
            throw new NoImplicitConversionException(object, "String");
        }
    }

    @Specialization
    public byte[] toString(VirtualFrame frame, StringCharPointerAdapter object) {
        return toString(frame, object.getString());
    }

    @TruffleBoundary
    @Specialization(guards = "!isRubyBasicObject(object)")
    public byte[] toString(TruffleObject object) {
        return object.toString().getBytes(StandardCharsets.UTF_8);
    }

    protected static boolean isStringCharPointerAdapter(Object object) {
        return object instanceof StringCharPointerAdapter;
    }

}
