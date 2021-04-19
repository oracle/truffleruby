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
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.Nil;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
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
    protected Rope toString(int value) {
        return RopeOperations.encodeAscii(Integer.toString(value), USASCIIEncoding.INSTANCE);
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    protected Rope toString(long value) {
        return RopeOperations.encodeAscii(Long.toString(value), USASCIIEncoding.INSTANCE);
    }

    @TruffleBoundary
    @Specialization(guards = "convertNumbersToStrings")
    protected Rope toString(double value) {
        return RopeOperations.encodeAscii(Double.toString(value), USASCIIEncoding.INSTANCE);
    }

    @TruffleBoundary
    @Specialization(guards = "specialClassBehaviour")
    protected Rope toStringSpecialClass(RubyClass rubyClass,
            @CachedLibrary(limit = "2") RubyStringLibrary libString) {
        if (rubyClass == getContext().getCoreLibrary().trueClass) {
            return RopeOperations.encodeAscii("true", USASCIIEncoding.INSTANCE);
        } else if (rubyClass == getContext().getCoreLibrary().falseClass) {
            return RopeOperations.encodeAscii("false", USASCIIEncoding.INSTANCE);
        } else if (rubyClass == getContext().getCoreLibrary().nilClass) {
            return RopeOperations.encodeAscii("nil", USASCIIEncoding.INSTANCE);
        } else {
            return toString(rubyClass, libString);
        }
    }

    @Specialization(guards = "libString.isRubyString(string)")
    protected Rope toStringString(Object string,
            @CachedLibrary(limit = "2") RubyStringLibrary libValue,
            @CachedLibrary(limit = "2") RubyStringLibrary libString) {
        if ("inspect".equals(conversionMethod)) {
            final Object value = getToStrNode().call(string, conversionMethod);

            if (libValue.isRubyString(value)) {
                return libValue.getRope(value);
            } else {
                throw new NoImplicitConversionException(string, "String");
            }
        }
        return libString.getRope(string);
    }

    @Specialization
    protected Rope toString(RubyArray array,
            @CachedLibrary(limit = "2") RubyStringLibrary libString) {
        if (toSNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toSNode = insert(DispatchNode.create(PRIVATE_RETURN_MISSING));
        }

        final Object value = toSNode.call(array, "to_s");

        if (libString.isRubyString(value)) {
            return libString.getRope(value);
        } else {
            throw new NoImplicitConversionException(array, "String");
        }
    }

    @Specialization(
            guards = { "isNotRubyString(object)", "!isRubyArray(object)", "!isForeignObject(object)" })
    protected Rope toString(Object object,
            @CachedLibrary(limit = "2") RubyStringLibrary libString) {
        final Object value = getToStrNode().call(object, conversionMethod);

        if (libString.isRubyString(value)) {
            return libString.getRope(value);
        }

        if (inspectOnConversionFailure) {
            if (inspectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                inspectNode = insert(KernelNodes.ToSNode.create());
            }

            return inspectNode.executeToS(object).rope;
        } else {
            throw new NoImplicitConversionException(object, "String");
        }
    }

    @TruffleBoundary
    @Specialization(guards = "isForeignObject(object)")
    protected Rope toStringForeign(Object object) {
        return RopeOperations.create(
                object.toString().getBytes(StandardCharsets.UTF_8),
                UTF8Encoding.INSTANCE,
                CodeRange.CR_UNKNOWN);
    }

    private DispatchNode getToStrNode() {
        if (toStrNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStrNode = insert(DispatchNode.create(PRIVATE_RETURN_MISSING));
        }
        return toStrNode;
    }

}
