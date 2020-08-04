/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.objects.AllocateHelperNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Exception", isClass = true)
public abstract class ExceptionNodes {

    protected final static String CUSTOM_BACKTRACE_FIELD = "@custom_backtrace";

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

        @Specialization
        protected RubyException allocateException(DynamicObject rubyClass) {
            final Shape shape = allocateNode.getCachedShape(rubyClass);
            final RubyException instance = new RubyException(shape, nil, null, nil);
            allocateNode.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyException initialize(RubyException exception, NotProvided message) {
            exception.message = nil;
            return exception;
        }

        @Specialization(guards = "wasProvided(message)")
        protected RubyException initialize(RubyException exception, Object message) {
            exception.message = message;
            return exception;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "self == from")
        protected RubyException initializeCopySelfIsSameAsFrom(RubyException self, RubyException from) {
            return self;
        }

        @Specialization(
                guards = { "self != from", "!isNameError(from)", "!isSystemCallError(from)" })
        protected RubyException initializeCopy(RubyException self, RubyException from) {
            initializeExceptionCopy(self, from);
            return self;
        }

        @Specialization(guards = "self != from")
        protected RubyException initializeSystemCallErrorCopy(RubySystemCallError self, RubySystemCallError from) {
            initializeExceptionCopy(self, from);
            self.errno = from.errno;
            return self;
        }

        @Specialization(guards = "self != from")
        protected RubyException initializeCopyNoMethodError(RubyNoMethodError self, RubyNoMethodError from) {
            initializeExceptionCopy(self, from);
            initializeNameErrorCopy(self, from);
            self.args = from.args;
            return self;
        }

        @Specialization(
                guards = { "self != from", "!isNoMethodError(from)" })
        protected RubyException initializeCopyNameError(RubyNameError self, RubyNameError from) {
            initializeExceptionCopy(self, from);
            initializeNameErrorCopy(self, from);
            return self;
        }

        protected boolean isNameError(RubyException object) {
            return object instanceof RubyNameError;
        }

        protected boolean isNoMethodError(RubyException object) {
            return object instanceof RubyNoMethodError;
        }

        protected boolean isSystemCallError(RubyException object) {
            return object instanceof RubySystemCallError;
        }

        private void initializeNameErrorCopy(RubyNameError self, RubyNameError from) {
            self.name = from.name;
            self.receiver = from.receiver;
        }

        private void initializeExceptionCopy(RubyException self, RubyException from) {
            Backtrace backtrace = from.backtrace;
            if (backtrace != null) {
                self.backtrace = backtrace.copy(getContext(), self);
            } else {
                self.backtrace = backtrace;
            }
            self.formatter = from.formatter;
            self.message = from.message;
            self.cause = from.cause;
            self.backtraceStringArray = from.backtraceStringArray;
            self.backtraceLocations = from.backtraceLocations;
        }

    }

    @CoreMethod(names = "backtrace")
    public abstract static class BacktraceNode extends CoreMethodArrayArgumentsNode {

        @Child private ReadObjectFieldNode readCustomBacktraceNode;

        @Specialization
        protected Object backtrace(RubyException exception,
                @Cached ConditionProfile hasCustomBacktraceProfile,
                @Cached ConditionProfile hasBacktraceProfile) {
            final Object customBacktrace = getReadCustomBacktraceNode()
                    .execute(exception, CUSTOM_BACKTRACE_FIELD, null);

            if (hasCustomBacktraceProfile.profile(customBacktrace != null)) {
                return customBacktrace;
            } else if (hasBacktraceProfile.profile(exception.backtrace != null)) {
                DynamicObject backtraceStringArray = exception.backtraceStringArray;
                if (backtraceStringArray == null) {
                    backtraceStringArray = getContext().getUserBacktraceFormatter().formatBacktraceAsRubyStringArray(
                            exception,
                            exception.backtrace);
                    exception.backtraceStringArray = backtraceStringArray;
                }
                return backtraceStringArray;
            } else {
                return nil;
            }
        }

        private ReadObjectFieldNode getReadCustomBacktraceNode() {
            if (readCustomBacktraceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readCustomBacktraceNode = insert(ReadObjectFieldNode.create());
            }

            return readCustomBacktraceNode;
        }

    }

    @CoreMethod(names = "backtrace_locations")
    public abstract static class BacktraceLocationsNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

        @Specialization
        protected Object backtraceLocations(RubyException exception,
                @Cached ConditionProfile hasBacktraceProfile,
                @Cached ConditionProfile hasLocationsProfile) {
            if (hasBacktraceProfile.profile(exception.backtrace != null)) {
                Object backtraceLocations = exception.backtraceLocations;
                if (hasLocationsProfile.profile(backtraceLocations == null)) {
                    Backtrace backtrace = exception.backtrace;
                    backtraceLocations = backtrace
                            .getBacktraceLocations(getContext(), allocateNode, GetBacktraceException.UNLIMITED, null);
                    exception.backtraceLocations = backtraceLocations;
                }
                return backtraceLocations;
            } else {
                return nil;
            }
        }
    }

    @Primitive(name = "exception_backtrace?")
    public abstract static class BacktraceQueryPrimitiveNode extends PrimitiveArrayArgumentsNode {

        protected static final String METHOD = "backtrace";

        @Child private ReadObjectFieldNode readCustomBacktraceNode;

        /* We can cheaply determine if an Exception has a backtrace via object inspection. However, if
         * `Exception#backtrace` is redefined, then `Exception#backtrace?` needs to follow along to be consistent. So,
         * we check if the method has been redefined here and if so, fall back to the Ruby code for the method by
         * returning `FAILURE` in the fallback specialization. */
        @Specialization(
                guards = {
                        "lookupNode.lookup(frame, exception, METHOD) == getContext().getCoreMethods().EXCEPTION_BACKTRACE", },
                limit = "1")
        protected boolean backtraceQuery(VirtualFrame frame, RubyException exception,
                @Cached LookupMethodNode lookupNode) {
            final Object customBacktrace = readCustomBacktrace(exception);

            return !(customBacktrace == null && exception.backtrace == null);
        }

        @Specialization
        protected Object fallback(RubyException exception) {
            return FAILURE;
        }

        private Object readCustomBacktrace(RubyException exception) {
            if (readCustomBacktraceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readCustomBacktraceNode = insert(ReadObjectFieldNode.create());
            }

            return readCustomBacktraceNode.execute(exception, CUSTOM_BACKTRACE_FIELD, null);
        }

    }

    @NonStandard
    @CoreMethod(names = "capture_backtrace!", required = 1, lowerFixnum = 1)
    public abstract static class CaptureBacktraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object captureBacktrace(RubyException exception, int offset) {
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(this, offset);
            exception.backtrace = backtrace;
            return nil;
        }

    }

    @Primitive(name = "exception_message")
    public abstract static class MessagePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object message(RubyException exception) {
            final Object message = exception.message;
            if (message == null) {
                return nil;
            } else {
                return message;
            }
        }

    }

    @Primitive(name = "exception_set_message")
    public abstract static class MessageSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setMessage(RubyException error, Object message) {
            error.message = message;
            return error;
        }

    }

    @Primitive(name = "exception_formatter")
    public abstract static class FormatterPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object formatter(RubyException exception) {
            final DynamicObject formatter = exception.formatter;
            if (formatter == null) {
                return nil;
            } else {
                return formatter;
            }
        }

    }

    @CoreMethod(names = "cause")
    public abstract static class CauseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object cause(RubyException exception) {
            return exception.cause;
        }

    }

    @Primitive(name = "exception_set_cause")
    public abstract static class ExceptionSetCauseNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyException setCause(RubyException exception, Object cause) {
            exception.cause = cause;
            return exception;
        }

    }

    @Primitive(name = "exception_errno_error", lowerFixnum = 1)
    public static abstract class ExceptionErrnoErrorPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child ErrnoErrorNode errnoErrorNode = ErrnoErrorNode.create();

        @Specialization
        protected RubySystemCallError exceptionErrnoError(DynamicObject message, int errno) {
            return errnoErrorNode.execute(errno, message, null);
        }

    }

    @Primitive(name = "java_breakpoint")
    @SuppressWarnings("unused")
    public static abstract class Breakpoint extends PrimitiveNode {

        @TruffleBoundary
        @Specialization
        protected boolean breakpoint() {
            // have a Ruby backtrace at hand
            String printableRubyBacktrace = BacktraceFormatter.printableRubyBacktrace(getContext(), this);
            return true; // place to put a Java breakpoint
        }


    }

}
