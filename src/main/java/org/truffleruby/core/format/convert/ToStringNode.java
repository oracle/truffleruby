/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import java.nio.charset.StandardCharsets;

import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.Nil;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.library.RubyStringLibrary;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;

@NodeChild("value")
public abstract class ToStringNode extends FormatNode {

    protected final boolean convertNumbersToStrings;
    private final String conversionMethod;
    private final boolean inspectOnConversionFailure;
    private final Object valueOnNil;
    protected final boolean specialClassBehaviour;

    @Child private DispatchNode toStrNode;
    @Child private DispatchNode toSNode;
    @Child private KernelNodes.ToSNode inspectNode;

    public ToStringNode(
            boolean convertNumbersToStrings,
            String conversionMethod,
            boolean inspectOnConversionFailure,
            Object valueOnNil) {
        this(convertNumbersToStrings, conversionMethod, inspectOnConversionFailure, valueOnNil, false);
    }

    public ToStringNode(
            boolean convertNumbersToStrings,
            String conversionMethod,
            boolean inspectOnConversionFailure,
            Object valueOnNil,
            boolean specialClassBehaviour) {
        this.convertNumbersToStrings = convertNumbersToStrings;
        this.conversionMethod = conversionMethod;
        this.inspectOnConversionFailure = inspectOnConversionFailure;
        this.valueOnNil = valueOnNil;
        this.specialClassBehaviour = specialClassBehaviour;
    }

    public abstract Object executeToString(VirtualFrame frame, Object object);

    @Specialization
    protected Object toStringNil(Nil nil) {
        return valueOnNil;
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    protected byte[] toString(int value) {
        return RopeOperations.encodeAsciiBytes(Integer.toString(value));
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    protected byte[] toString(long value) {
        return RopeOperations.encodeAsciiBytes(Long.toString(value));
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    protected byte[] toString(double value) {
        return RopeOperations.encodeAsciiBytes(Double.toString(value));
    }

    @TruffleBoundary
    @Specialization(guards = "specialClassBehaviour")
    protected byte[] toStringSpecialClass(RubyClass rubyClass,
            @CachedLibrary(limit = "2") RubyStringLibrary libString,
            @Cached RopeNodes.BytesNode bytesNode) {
        if (rubyClass == getContext().getCoreLibrary().trueClass) {
            return RopeOperations.encodeAsciiBytes("true");
        } else if (rubyClass == getContext().getCoreLibrary().falseClass) {
            return RopeOperations.encodeAsciiBytes("false");
        } else if (rubyClass == getContext().getCoreLibrary().nilClass) {
            return RopeOperations.encodeAsciiBytes("nil");
        } else {
            return toString(rubyClass, libString, bytesNode);
        }
    }

    @Specialization(guards = "libString.isRubyString(string)")
    protected byte[] toStringString(Object string,
            @CachedLibrary(limit = "2") RubyStringLibrary libValue,
            @CachedLibrary(limit = "2") RubyStringLibrary libString,
            @Cached RopeNodes.BytesNode bytesNode) {
        if ("inspect".equals(conversionMethod)) {
            final Object value = getToStrNode().call(string, conversionMethod);

            if (libValue.isRubyString(value)) {
                return bytesNode.execute(libValue.getRope(value));
            } else {
                throw new NoImplicitConversionException(string, "String");
            }
        }
        return bytesNode.execute(libString.getRope(string));
    }

    @Specialization
    protected byte[] toString(RubyArray array,
            @CachedLibrary(limit = "2") RubyStringLibrary libString,
            @Cached RopeNodes.BytesNode bytesNode) {
        if (toSNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toSNode = insert(DispatchNode.create(PRIVATE_RETURN_MISSING));
        }

        final Object value = toSNode.call(array, "to_s");

        if (libString.isRubyString(value)) {
            return bytesNode.execute(libString.getRope(value));
        } else {
            throw new NoImplicitConversionException(array, "String");
        }
    }

    @Specialization(
            guards = { "isNotRubyString(object)", "!isRubyArray(object)", "!isForeignObject(object)" })
    protected byte[] toString(Object object,
            @CachedLibrary(limit = "2") RubyStringLibrary libString,
            @Cached RopeNodes.BytesNode bytesNode) {
        final Object value = getToStrNode().call(object, conversionMethod);

        if (libString.isRubyString(value)) {
            return bytesNode.execute(libString.getRope(value));
        }

        if (inspectOnConversionFailure) {
            if (inspectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                inspectNode = insert(KernelNodes.ToSNode.create());
            }

            return bytesNode.execute(inspectNode.executeToS(object).rope);
        } else {
            throw new NoImplicitConversionException(object, "String");
        }
    }

    @TruffleBoundary
    @Specialization(guards = "isForeignObject(object)")
    protected byte[] toStringForeign(Object object) {
        return object.toString().getBytes(StandardCharsets.UTF_8);
    }

    private DispatchNode getToStrNode() {
        if (toStrNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStrNode = insert(DispatchNode.create(PRIVATE_RETURN_MISSING));
        }
        return toStrNode;
    }

}
