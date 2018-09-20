/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;

@CoreClass("Exception")
public abstract class ExceptionNodes {

    protected final static String CUSTOM_BACKTRACE_FIELD = "@custom_backtrace";

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocateNameError(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, nil(), null, null);
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

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "self == from")
        public Object initializeCopySelfIsSameAsFrom(DynamicObject self, DynamicObject from) {
            return self;
        }


        @Specialization(guards = { "self != from", "isRubyException(from)" })
        public Object initializeCopy(DynamicObject self, DynamicObject from) {
            Layouts.EXCEPTION.setBacktrace(self, Layouts.EXCEPTION.getBacktrace(from));
            Layouts.EXCEPTION.setFormatter(self, Layouts.EXCEPTION.getFormatter(from));
            Layouts.EXCEPTION.setMessage(self, Layouts.EXCEPTION.getMessage(from));

            return self;
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
                    backtrace.setBacktraceStringArray(
                            getContext().getUserBacktraceFormatter().formatBacktraceAsRubyStringArray(
                                    exception, Layouts.EXCEPTION.getBacktrace(exception)));
                }
                return backtrace.getBacktraceStringArray();
            } else {
                return nil();
            }
        }

        private ReadObjectFieldNode getReadCustomBacktraceNode() {
            if (readCustomBacktraceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readCustomBacktraceNode = insert(ReadObjectFieldNodeGen.create(CUSTOM_BACKTRACE_FIELD, null));
            }

            return readCustomBacktraceNode;
        }

    }

    @Primitive(name = "exception_backtrace?")
    public abstract static class BacktraceQueryPrimitiveNode extends CoreMethodArrayArgumentsNode {

        protected static final String METHOD = "backtrace";

        @Child private ReadObjectFieldNode readCustomBacktraceNode;

        /* We can cheaply determine if an Exception has a backtrace via object inspection. However, if
         * `Exception#backtrace` is redefined, then `Exception#backtrace?` needs to follow along to be consistent.
         * So, we check if the method has been redefined here and if so, fall back to the Ruby code for the method
         * by returning `null` in the fallback specialization.
         */
        @Specialization(guards = {
                "lookupNode.lookup(frame, self, METHOD) == getContext().getCoreMethods().EXCEPTION_BACKTRACE",
        }, limit = "1")
        public boolean backtraceQuery(VirtualFrame frame, DynamicObject self,
                @Cached("create()") LookupMethodNode lookupNode,
                @Cached("createBinaryProfile()") ConditionProfile resultProfile) {
            final Object customBacktrace = readCustomBacktrace(self);

            if (resultProfile.profile(customBacktrace == null && Layouts.EXCEPTION.getBacktrace(self) == null)) {
                return false;
            } else {
                return true;
            }
        }

        @Specialization
        public Object fallback(DynamicObject self) {
            return null;
        }

        private Object readCustomBacktrace(DynamicObject exception) {
            if (readCustomBacktraceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readCustomBacktraceNode = insert(ReadObjectFieldNodeGen.create(CUSTOM_BACKTRACE_FIELD, null));
            }

            return readCustomBacktraceNode.execute(exception);
        }

    }

    @NonStandard
    @CoreMethod(names = "capture_backtrace!", optional = 1, lowerFixnum = 1)
    public abstract static class CaptureBacktraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject captureBacktrace(DynamicObject exception, NotProvided offset) {
            return captureBacktrace(exception, 1);
        }

        @Specialization
        public DynamicObject captureBacktrace(DynamicObject exception, int offset) {
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(this, offset);
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

    @Primitive(name = "exception_formatter")
    public abstract static class FormatterPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject formatter(DynamicObject exception) {
            return Layouts.EXCEPTION.getFormatter(exception);
        }

    }

    @Primitive(name = "exception_errno_error", needsSelf = false, lowerFixnum = 2)
    public static abstract class ExceptionErrnoErrorPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject exceptionErrnoError(DynamicObject message, int errno) {
            return coreExceptions().errnoError(errno, StringOperations.getString(message), this);
        }

    }

}
