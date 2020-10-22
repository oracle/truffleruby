/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is transliterated from C++ code in Rubinius.
 *
 * Copyright (c) 2007-2014, Evan Phoenix and contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Rubinius nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.truffleruby.core;

import java.io.PrintStream;
import java.util.Map.Entry;

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ThrowException;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;
import org.truffleruby.language.objects.AllocateHelperNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.yield.YieldNode;
import org.truffleruby.platform.Signals;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import sun.misc.Signal;
import sun.misc.SignalHandler;

@CoreModule(value = "VMPrimitives", isClass = true)
public abstract class VMPrimitiveNodes {

    @Primitive(name = "vm_catch")
    public abstract static class CatchNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object doCatch(Object tag, RubyProc block,
                @Cached BranchProfile catchProfile,
                @Cached ConditionProfile matchProfile,
                @Cached ReferenceEqualNode referenceEqualNode,
                @Cached YieldNode dispatchNode) {
            try {
                return dispatchNode.executeDispatch(block, tag);
            } catch (ThrowException e) {
                catchProfile.enter();
                if (matchProfile.profile(referenceEqualNode.executeReferenceEqual(e.getTag(), tag))) {
                    return e.getValue();
                } else {
                    throw e;
                }
            }
        }
    }

    // The hard #exit!
    @Primitive(name = "vm_exit", lowerFixnum = 0)
    public static abstract class VMExitNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object vmExit(int status) {
            throw new ExitException(status);
        }

    }

    @Primitive(name = "vm_extended_modules")
    public static abstract class VMExtendedModulesNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object vmExtendedModules(Object object, RubyProc block,
                @Cached MetaClassNode metaClassNode,
                @Cached YieldNode yieldNode,
                @Cached ConditionProfile isSingletonProfile) {
            final RubyClass metaClass = metaClassNode.execute(object);

            if (isSingletonProfile.profile(metaClass.isSingleton)) {
                for (RubyModule included : metaClass.fields.prependedAndIncludedModules()) {
                    yieldNode.executeDispatch(block, included);
                }
            }

            return nil;
        }

    }

    @Primitive(name = "vm_method_is_basic")
    public static abstract class VMMethodIsBasicNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean vmMethodIsBasic(RubyMethod method) {
            return method.method.isBuiltIn();
        }

    }

    @Primitive(name = "vm_method_lookup")
    public static abstract class VMMethodLookupNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

        @Specialization
        protected Object vmMethodLookup(VirtualFrame frame, Object receiver, Object name,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached LookupMethodOnSelfNode lookupMethodNode,
                @CachedLanguage RubyLanguage language) {
            // TODO BJF Sep 14, 2016 Handle private
            final String normalizedName = nameToJavaStringNode.execute(name);
            InternalMethod method = lookupMethodNode.lookupIgnoringVisibility(frame, receiver, normalizedName);
            if (method == null) {
                return nil;
            }
            final RubyMethod instance = new RubyMethod(
                    coreLibrary().methodClass,
                    RubyLanguage.methodShape,
                    receiver,
                    method);
            allocateNode.trace(instance, this, language);
            return instance;
        }

    }

    @Primitive(name = "vm_raise_exception")
    public static abstract class VMRaiseExceptionNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object vmRaiseException(RubyException exception,
                @Cached ConditionProfile reRaiseProfile) {
            final Backtrace backtrace = exception.backtrace;
            if (reRaiseProfile.profile(backtrace != null && backtrace.getRaiseException() != null)) {
                // We need to rethrow the existing RaiseException, otherwise we would lose the
                // TruffleStackTrace stored in it.
                assert backtrace.getRaiseException().getException() == exception;

                if (getContext().getOptions().BACKTRACE_ON_RAISE) {
                    getContext().getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr("raise: ", exception);
                }
                throw backtrace.getRaiseException();
            } else {
                throw new RaiseException(getContext(), exception);
            }
        }

        public static void reRaiseException(RubyContext context, RubyException exception) {
            final Backtrace backtrace = exception.backtrace;
            if (backtrace != null && backtrace.getRaiseException() != null) {
                // We need to rethrow the existing RaiseException, otherwise we would lose the
                // TruffleStackTrace stored in it.
                throw backtrace.getRaiseException();
            } else {
                throw new RaiseException(context, exception);
            }
        }

    }

    @Primitive(name = "vm_throw")
    public abstract static class ThrowNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object doThrow(Object tag, Object value) {
            throw new ThrowException(tag, value);
        }

    }

    @Primitive(name = "vm_watch_signal")
    public static abstract class VMWatchSignalNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean restoreDefault(RubyString signalString, RubyString action) {
            final String actionString = action.getJavaString();
            final String signalName = signalString.getJavaString();

            switch (actionString) {
                case "DEFAULT":
                    return restoreDefaultHandler(signalName);
                case "SYSTEM_DEFAULT":
                    return restoreSystemHandler(signalName);
                case "IGNORE":
                    return registerIgnoreHandler(signalName);
                default:
                    throw new UnsupportedOperationException(actionString);
            }
        }

        @TruffleBoundary
        @Specialization
        protected boolean watchSignalProc(RubyString signalString, RubyProc action) {
            if (getContext().getThreadManager().getCurrentThread() != getContext().getThreadManager().getRootThread()) {
                // The proc will be executed on the main thread
                SharedObjects.writeBarrier(getContext(), action);
            }

            final RubyContext context = getContext();

            String signalName = signalString.getJavaString();
            return registerHandler(signalName, signal -> {
                if (context.getOptions().SINGLE_THREADED) {
                    RubyLanguage.LOGGER.severe(
                            "signal " + signal + " caught but can't create a thread to handle it so ignoring");
                    return;
                }

                final RubyThread rootThread = context.getThreadManager().getRootThread();
                final FiberManager fiberManager = rootThread.fiberManager;
                final ThreadManager threadManager = context.getThreadManager();

                // Workaround: we need to register with Truffle (which means going multithreaded),
                // so that NFI can get its context to call pthread_kill() (GR-7405).
                final TruffleContext truffleContext = context.getEnv().getContext();
                final Object prev;
                try {
                    prev = truffleContext.enter(this);
                } catch (IllegalStateException e) { // Multi threaded access denied from Truffle
                    // Not in a context, so we cannot use TruffleLogger
                    final PrintStream printStream = BacktraceFormatter.printStreamFor(context.getEnv().err());
                    printStream.println(
                            "[ruby] SEVERE: signal " + signal +
                                    " caught but can't attach a thread to handle it so restoring the default handler and re-raising the signal");
                    Signals.restoreDefaultHandler(signalName);
                    try {
                        Signal.raise(signal);
                    } catch (IllegalArgumentException illegalArgumentException) {
                        illegalArgumentException.printStackTrace(printStream);
                    }
                    return;
                }
                try {
                    context.getSafepointManager().pauseAllThreadsAndExecuteFromNonRubyThread(
                            "Handling of signal " + signal,
                            true,
                            (rubyThread, currentNode) -> {
                                if (rubyThread == rootThread &&
                                        threadManager.getRubyFiberFromCurrentJavaThread() == fiberManager
                                                .getCurrentFiber()) {
                                    ProcOperations.rootCall(action);
                                }
                            });
                } finally {
                    truffleContext.leave(this, prev);
                }
            });
        }

        @TruffleBoundary
        private boolean restoreDefaultHandler(String signalName) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning(
                        "restoring default handler for signal " + signalName +
                                " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                return Signals.restoreDefaultHandler(signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        @TruffleBoundary
        private boolean restoreSystemHandler(String signalName) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning(
                        "restoring system handler for signal " + signalName +
                                " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                Signals.restoreSystemHandler(signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

        @TruffleBoundary
        private boolean registerIgnoreHandler(String signalName) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning(
                        "ignoring signal " + signalName +
                                " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                Signals.registerIgnoreHandler(signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

        @TruffleBoundary
        private boolean registerHandler(String signalName, SignalHandler newHandler) {
            if (getContext().getOptions().EMBEDDED) {
                RubyLanguage.LOGGER.warning(
                        "trapping signal " + signalName +
                                " in embedded mode may interfere with other embedded contexts or the host system");
            }

            try {
                Signals.registerHandler(newHandler, signalName);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

    }

    @Primitive(name = "vm_get_config_item")
    public abstract static class VMGetConfigItemNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object get(RubyString key) {
            final Object value = getContext().getNativeConfiguration().get(key.getJavaString());

            if (value == null) {
                return nil;
            } else {
                return value;
            }
        }

    }

    @Primitive(name = "vm_get_config_section")
    public abstract static class VMGetConfigSectionNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object getSection(RubyString section, RubyProc block,
                @Cached MakeStringNode makeStringNode,
                @Cached YieldNode yieldNode) {
            for (Entry<String, Object> entry : getContext()
                    .getNativeConfiguration()
                    .getSection(section.getJavaString())) {
                final RubyString key = makeStringNode
                        .executeMake(entry.getKey(), UTF8Encoding.INSTANCE, CodeRange.CR_7BIT);
                yieldNode.executeDispatch(block, key, entry.getValue());
            }

            return nil;
        }

    }

    @Primitive(name = "vm_set_class")
    public abstract static class VMSetClassNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyDynamicObject setClass(RubyDynamicObject object, RubyClass newClass) {
            SharedObjects.propagate(getContext(), object, newClass);
            synchronized (object) {
                object.setMetaClass(newClass);
            }
            return object;
        }

    }

    @Primitive(name = "vm_dev_urandom_bytes", lowerFixnum = 0)
    public abstract static class VMDevUrandomBytes extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "count >= 0")
        protected RubyString readRandomBytes(int count,
                @Cached MakeStringNode makeStringNode) {
            final byte[] bytes = getContext().getRandomSeedBytes(count);

            return makeStringNode.executeMake(bytes, ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

        @Specialization(guards = "count < 0")
        protected RubyString negativeCount(int count) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().argumentError(
                            getContext().getCoreStrings().NEGATIVE_STRING_SIZE.getRope(),
                            this));
        }

    }

    @Primitive(name = "vm_hash_start")
    public abstract static class VMHashStartNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long startHash(long salt) {
            return getContext().getHashing(this).start(salt);
        }

        @Specialization
        protected long startHashBigNum(RubyBignum salt) {
            return getContext().getHashing(this).start(BigIntegerOps.hashCode(salt));
        }

        @Specialization(guards = "!isRubyNumber(salt)")
        protected Object startHashNotNumber(Object salt,
                @Cached ToRubyIntegerNode toRubyInteger,
                @Cached ConditionProfile isIntegerProfile,
                @Cached ConditionProfile isLongProfile,
                @Cached ConditionProfile isBignumProfile) {
            Object result = toRubyInteger.execute(salt);
            if (isIntegerProfile.profile(result instanceof Integer)) {
                return getContext().getHashing(this).start((int) result);
            } else if (isLongProfile.profile(result instanceof Long)) {
                return getContext().getHashing(this).start((long) result);
            } else if (isBignumProfile.profile(result instanceof RubyBignum)) {
                return getContext().getHashing(this).start(BigIntegerOps.hashCode((RubyBignum) result));
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }

        }
    }

    @Primitive(name = "vm_hash_update")
    public abstract static class VMHashUpdateNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long updateHash(long hash, long value) {
            return Hashing.update(hash, value);
        }

        @Specialization
        protected long updateHash(long hash, RubyBignum value) {
            return Hashing.update(hash, BigIntegerOps.hashCode(value));
        }

        @Specialization(guards = "!isRubyNumber(value)")
        protected Object updateHash(long hash, Object value,
                @Cached ToRubyIntegerNode toRubyInteger,
                @Cached ConditionProfile isIntegerProfile,
                @Cached ConditionProfile isLongProfile,
                @Cached ConditionProfile isBignumProfile) {
            Object result = toRubyInteger.execute(value);
            if (isIntegerProfile.profile(result instanceof Integer)) {
                return Hashing.update(hash, (int) result);
            } else if (isLongProfile.profile(result instanceof Long)) {
                return Hashing.update(hash, (long) result);
            } else if (isBignumProfile.profile(result instanceof RubyBignum)) {
                return Hashing.update(hash, BigIntegerOps.hashCode((RubyBignum) result));
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }

        }
    }

    @Primitive(name = "vm_hash_end")
    public abstract static class VMHashEndNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long endHash(long hash) {
            return Hashing.end(hash);
        }

    }

}
