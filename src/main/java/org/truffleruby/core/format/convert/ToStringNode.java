/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;

@NodeChild("value")
public abstract class ToStringNode extends FormatNode {

    protected final boolean convertNumbersToStrings;
    private final String conversionMethod;
    private final boolean inspectOnConversionFailure;
    private final Object valueOnNil;

    @Child private DispatchNode toStrNode;
    @Child private DispatchNode toSNode;
    @Child private KernelNodes.ToSNode inspectNode;

    private final ConditionProfile taintedProfile = ConditionProfile.create();

    public ToStringNode(
            boolean convertNumbersToStrings,
            String conversionMethod,
            boolean inspectOnConversionFailure,
            Object valueOnNil) {
        this.convertNumbersToStrings = convertNumbersToStrings;
        this.conversionMethod = conversionMethod;
        this.inspectOnConversionFailure = inspectOnConversionFailure;
        this.valueOnNil = valueOnNil;
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

    @Specialization(limit = "getRubyLibraryCacheLimit()")
    protected byte[] toStringString(VirtualFrame frame, RubyString string,
            @CachedLibrary("string") RubyLibrary rubyLibrary,
            @Cached RopeNodes.BytesNode bytesNode) {
        if (taintedProfile.profile(rubyLibrary.isTainted(string))) {
            setTainted(frame);
        }
        if ("inspect".equals(conversionMethod)) {
            final Object value = getToStrNode().call(string, conversionMethod);

            if (RubyGuards.isRubyString(value)) {
                if (taintedProfile.profile(rubyLibrary.isTainted(value))) {
                    setTainted(frame);
                }

                return bytesNode.execute(((RubyString) value).rope);
            } else {
                throw new NoImplicitConversionException(string, "String");
            }
        }
        return bytesNode.execute(string.rope);
    }

    @Specialization
    protected byte[] toString(VirtualFrame frame, RubyArray array,
            @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibrary,
            @Cached RopeNodes.BytesNode bytesNode) {
        if (toSNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toSNode = insert(DispatchNode.create(PRIVATE_RETURN_MISSING));
        }

        final Object value = toSNode.call(array, "to_s");

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(rubyLibrary.isTainted(value))) {
                setTainted(frame);
            }

            return bytesNode.execute(((RubyString) value).rope);
        } else {
            throw new NoImplicitConversionException(array, "String");
        }
    }

    @Specialization(
            guards = { "!isRubyString(object)", "!isRubyArray(object)", "!isForeignObject(object)" })
    protected byte[] toString(VirtualFrame frame, Object object,
            @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibrary,
            @Cached RopeNodes.BytesNode bytesNode) {
        final Object value = getToStrNode().call(object, conversionMethod);

        if (RubyGuards.isRubyString(value)) {
            if (taintedProfile.profile(rubyLibrary.isTainted(value))) {
                setTainted(frame);
            }

            return bytesNode.execute(((RubyString) value).rope);
        }

        if (inspectOnConversionFailure) {
            if (inspectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                inspectNode = insert(KernelNodes.ToSNode.create());
            }

            return bytesNode.execute((inspectNode.executeToS(object)).rope);
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
