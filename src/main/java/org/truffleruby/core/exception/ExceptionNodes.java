/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.objects.AllocationTracing;

@CoreModule(value = "Exception", isClass = true)
public abstract class ExceptionNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyException allocateException(RubyClass rubyClass) {
            final Shape shape = getLanguage().exceptionShape;
            final RubyException instance = new RubyException(rubyClass, shape, nil, null, nil);
            AllocationTracing.trace(instance, this);
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
                self.backtrace = backtrace.copy(self);
            } else {
                self.backtrace = null;
            }
            self.formatter = from.formatter;
            self.message = from.message;
            self.cause = from.cause;
            self.backtraceStringArray = from.backtraceStringArray;
            self.backtraceLocations = from.backtraceLocations;
            self.customBacktrace = from.customBacktrace;
        }

    }

    @CoreMethod(names = "backtrace")
    public abstract static class BacktraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object backtrace(RubyException exception,
                @Cached ConditionProfile hasCustomBacktraceProfile,
                @Cached ConditionProfile hasBacktraceProfile) {
            final Object customBacktrace = exception.customBacktrace;

            if (hasCustomBacktraceProfile.profile(customBacktrace != null)) {
                return customBacktrace;
            } else if (hasBacktraceProfile.profile(exception.backtrace != null)) {
                RubyArray backtraceStringArray = exception.backtraceStringArray;
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

    }

    @CoreMethod(names = "backtrace_locations")
    public abstract static class BacktraceLocationsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object backtraceLocations(RubyException exception,
                @Cached ConditionProfile hasBacktraceProfile,
                @Cached ConditionProfile hasLocationsProfile) {
            if (hasBacktraceProfile.profile(exception.backtrace != null)) {
                Object backtraceLocations = exception.backtraceLocations;
                if (hasLocationsProfile.profile(backtraceLocations == null)) {
                    Backtrace backtrace = exception.backtrace;
                    backtraceLocations = backtrace
                            .getBacktraceLocations(getContext(), getLanguage(), GetBacktraceException.UNLIMITED, null);
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

        /* We can cheaply determine if an Exception has a backtrace via object inspection. However, if
         * `Exception#backtrace` is redefined, then `Exception#backtrace?` needs to follow along to be consistent. So,
         * we check if the method has been redefined here and if so, fall back to the Ruby code for the method by
         * returning `FAILURE` in the fallback specialization. */
        @Specialization(
                guards = {
                        "lookupNode.lookupProtected(frame, exception, METHOD) == getContext().getCoreMethods().EXCEPTION_BACKTRACE", },
                limit = "1")
        protected boolean backtraceQuery(VirtualFrame frame, RubyException exception,
                @Cached LookupMethodOnSelfNode lookupNode) {
            return !(exception.customBacktrace == null && exception.backtrace == null);
        }

        @Specialization
        protected Object fallback(RubyException exception) {
            return FAILURE;
        }

        @Specialization(guards = "!isRubyException(exception)", limit = "getInteropCacheLimit()")
        protected boolean foreignException(Object exception,
                @CachedLibrary("exception") InteropLibrary interopLibrary) {
            return interopLibrary.hasExceptionStackTrace(exception);
        }
    }

    @Primitive(name = "exception_capture_backtrace", lowerFixnum = 1)
    public abstract static class CaptureBacktraceNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object captureBacktrace(RubyException exception, int offset) {
            exception.backtrace = getContext().getCallStack().getBacktrace(this, offset);
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
        protected Object setMessage(RubyException exception, Object message) {
            exception.message = message;
            return nil;
        }

    }

    @Primitive(name = "exception_set_custom_backtrace")
    public abstract static class SetCustomBacktrace extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object set(RubyException exception, Object customBacktrace) {
            exception.customBacktrace = customBacktrace;
            return customBacktrace;
        }

    }

    @Primitive(name = "exception_formatter")
    public abstract static class FormatterPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object formatter(RubyException exception) {
            final RubyProc formatter = exception.formatter;
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

        @Specialization(guards = "!isRubyException(exception)")
        protected Object foreignExceptionNoCause(Object exception, Nil cause) {
            return exception;
        }

        @Specialization(guards = { "!isRubyException(exception)", "!isNil(cause)" })
        protected Object foreignExceptionWithCause(Object exception, Object cause) {
            RubyException exc = coreExceptions().runtimeError("Cannot set the cause of a foreign exception", this);
            exc.cause = cause;
            throw new RaiseException(getContext(), exc);
        }
    }

    @Primitive(name = "exception_errno_error", lowerFixnum = 2)
    public abstract static class ExceptionErrnoErrorPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child ErrnoErrorNode errnoErrorNode = ErrnoErrorNode.create();

        @Specialization
        protected RubySystemCallError exceptionErrnoError(RubyClass errorClass, Object message, int errno) {
            return errnoErrorNode.execute(errorClass, errno, message, null);
        }

    }

    @Primitive(name = "java_breakpoint")
    @SuppressWarnings("unused")
    public abstract static class Breakpoint extends PrimitiveNode {

        @SuppressFBWarnings("DLS")
        @TruffleBoundary
        @Specialization
        protected boolean breakpoint() {
            // have a Ruby backtrace at hand
            String printableRubyBacktrace = BacktraceFormatter.printableRubyBacktrace(this);
            return true; // place to put a Java breakpoint
        }


    }

    @Primitive(name = "exception_backtrace_limit")
    public abstract static class BacktraceLimitNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected int limit() {
            return getContext().getOptions().BACKTRACE_LIMIT;
        }

    }

    @Primitive(name = "exception_get_raise_exception")
    public abstract static class GetRaiseExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object getRaiseException(RubyException exception) {
            RaiseException raiseException = exception.backtrace.getRaiseException();
            if (raiseException != null) {
                return raiseException;
            } else {
                return nil;
            }
        }

    }

}
