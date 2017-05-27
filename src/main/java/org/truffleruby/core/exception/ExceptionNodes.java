/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;

@CoreClass("Exception")
public abstract class ExceptionNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocateNameError(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, nil(), null);
        }

    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject initialize(DynamicObject exception, NotProvided message) {
            Layouts.EXCEPTION.setMessage(exception, nil());
            return exception;
        }

        @Specialization(guards = "wasProvided(message)")
        public DynamicObject initialize(DynamicObject exception, Object message) {
            Layouts.EXCEPTION.setMessage(exception, message);
            return exception;
        }

    }

    @CoreMethod(names = "backtrace")
    public abstract static class BacktraceNode extends CoreMethodArrayArgumentsNode {

        @Child private ReadObjectFieldNode readCustomBacktraceNode;

        @Specialization
        public Object backtrace(
                DynamicObject exception,
                @Cached("createBinaryProfile()") ConditionProfile hasCustomBacktraceProfile,
                @Cached("createBinaryProfile()") ConditionProfile hasBacktraceProfile) {
            final Object customBacktrace = getReadCustomBacktraceNode().execute(exception);

            if (hasCustomBacktraceProfile.profile(customBacktrace != null)) {
                return customBacktrace;
            } else if (hasBacktraceProfile.profile(Layouts.EXCEPTION.getBacktrace(exception) != null)) {
                final Backtrace backtrace = Layouts.EXCEPTION.getBacktrace(exception);
                if (backtrace.getBacktraceStringArray() == null) {
                    backtrace.setBacktraceStringArray(ExceptionOperations.backtraceAsRubyStringArray(
                        getContext(),
                        exception,
                        Layouts.EXCEPTION.getBacktrace(exception)));
                }
                return backtrace.getBacktraceStringArray();
            } else {
                return nil();
            }
        }

        private ReadObjectFieldNode getReadCustomBacktraceNode() {
            if (readCustomBacktraceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readCustomBacktraceNode = insert(ReadObjectFieldNodeGen.create("@custom_backtrace", null));
            }

            return readCustomBacktraceNode;
        }

    }

    @NonStandard
    @CoreMethod(names = "capture_backtrace!", optional = 1, lowerFixnum = 1)
    public abstract static class CaptureBacktraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject captureBacktrace(DynamicObject exception, NotProvided offset) {
            return captureBacktrace(exception, 1);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject captureBacktrace(DynamicObject exception, int offset) {
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(
                    this,
                    offset,
                    exception);
            Layouts.EXCEPTION.setBacktrace(exception, backtrace);
            return nil();
        }

    }

    @Primitive(name = "exception_message")
    public abstract static class MessagePrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object message(DynamicObject exception) {
            return Layouts.EXCEPTION.getMessage(exception);
        }

    }

    @Primitive(name = "exception_set_message")
    public abstract static class MessageSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object setMessage(DynamicObject error, Object message) {
            Layouts.EXCEPTION.setMessage(error, message);
            return error;
        }

    }

    @Primitive(name = "exception_errno_error", needsSelf = false)
    public static abstract class ExceptionErrnoErrorPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject exceptionErrnoError(
                DynamicObject message,
                int errno,
                DynamicObject location) {
            final String errorMessage;
            if (message != nil()) {
                if (RubyGuards.isRubyString(location)) {
                    errorMessage = " @ " + location.toString() + " - " + RopeOperations.decodeRope(StringOperations.rope(message));
                } else {
                    errorMessage = " - " + RopeOperations.decodeRope(StringOperations.rope(message));
                }
            } else {
                errorMessage = "";
            }
            return coreExceptions().errnoError(errno, errorMessage, this);
        }

    }

}
