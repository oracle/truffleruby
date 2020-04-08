/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2002 Jason Voegele <jason@jvoegele.com>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004-2005 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.core.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.VMPrimitiveNodes.VMRaiseExceptionNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.exception.GetBacktraceException;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.thread.ThreadManager.UnblockingAction;
import org.truffleruby.core.thread.ThreadManager.UnblockingActionHolder;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.SafepointAction;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Thread", isClass = true)
public abstract class ThreadNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @CoreMethod(names = "alive?")
    public abstract static class AliveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean alive(DynamicObject thread) {
            final ThreadStatus status = Layouts.THREAD.getStatus(thread);
            return status != ThreadStatus.DEAD;
        }

    }

    @Primitive(name = "thread_backtrace", lowerFixnum = { 1, 2 })
    public abstract static class BacktraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object backtrace(DynamicObject rubyThread, int omit, NotProvided length) {
            return backtrace(rubyThread, omit, Integer.MAX_VALUE);
        }

        @TruffleBoundary
        @Specialization
        protected Object backtrace(DynamicObject rubyThread, int omit, int length) {
            final Memo<Backtrace> backtraceMemo = new Memo<>(null);

            getContext().getSafepointManager().pauseRubyThreadAndExecute(rubyThread, this, (thread1, currentNode) -> {
                final Backtrace backtrace = getContext().getCallStack().getBacktrace(currentNode, omit);
                backtrace.getStackTrace(); // must be done on the thread
                backtraceMemo.set(backtrace);
            });

            final Backtrace backtrace = backtraceMemo.get();

            // If the thread is dead or aborting the SafepointAction will not run.
            // Must return nil if omitting more entries than available.
            if (backtrace == null || omit > backtrace.getTotalUnderlyingElements()) {
                return nil;
            }

            if (length < 0) {
                length = backtrace.getStackTrace().length + 1 + length;
            }

            return getContext().getUserBacktraceFormatter().formatBacktraceAsRubyStringArray(
                    null,
                    backtrace,
                    length);
        }
    }

    @Primitive(name = "thread_backtrace_locations", lowerFixnum = { 1, 2 })
    public abstract static class BacktraceLocationsNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object backtraceLocations(DynamicObject rubyThread, int first, NotProvided second) {
            return backtraceLocationsInternal(rubyThread, first, GetBacktraceException.UNLIMITED);
        }

        @Specialization
        protected Object backtraceLocations(DynamicObject rubyThread, int first, int second) {
            return backtraceLocationsInternal(rubyThread, first, second);
        }

        @TruffleBoundary
        private Object backtraceLocationsInternal(DynamicObject rubyThread, int omit, int length) {
            final Memo<Object> backtraceLocationsMemo = new Memo<>(null);

            final SafepointAction safepointAction = (thread1, currentNode) -> {
                final Backtrace backtrace = getContext().getCallStack().getBacktrace(this, omit);
                backtraceLocationsMemo.set(backtrace.getBacktraceLocations(length, this));
            };

            getContext()
                    .getSafepointManager()
                    .pauseRubyThreadAndExecute(rubyThread, this, safepointAction);

            // If the thread is dead or aborting the SafepointAction will not run.
            return backtraceLocationsMemo.get() == null
                    ? nil
                    : backtraceLocationsMemo.get();
        }
    }

    @CoreMethod(names = "current", onSingleton = true)
    public abstract static class CurrentNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject current(VirtualFrame frame,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            return getCurrentRubyThreadNode.executeGetRubyThread(frame);
        }

    }

    @CoreMethod(names = "group")
    public abstract static class GroupNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object group(DynamicObject thread) {
            return Layouts.THREAD.getThreadGroup(thread);
        }

    }

    @CoreMethod(names = { "kill", "exit", "terminate" })
    public abstract static class KillNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary(transferToInterpreterOnException = false)
        @Specialization
        protected DynamicObject kill(DynamicObject rubyThread) {
            final ThreadManager threadManager = getContext().getThreadManager();
            final DynamicObject rootThread = threadManager.getRootThread();

            getContext().getSafepointManager().pauseRubyThreadAndExecute(rubyThread, this, (thread, currentNode) -> {
                if (thread == rootThread) {
                    throw new RaiseException(getContext(), coreExceptions().systemExit(0, currentNode));
                } else {
                    Layouts.THREAD.setStatus(thread, ThreadStatus.ABORTING);
                    throw new KillException();
                }
            });

            return rubyThread;
        }

    }

    @CoreMethod(names = "handle_interrupt", required = 2, needsBlock = true, visibility = Visibility.PRIVATE)
    public abstract static class HandleInterruptNode extends YieldingCoreMethodNode {

        @CompilationFinal private DynamicObject immediateSymbol;
        @CompilationFinal private DynamicObject onBlockingSymbol;
        @CompilationFinal private DynamicObject neverSymbol;

        private final BranchProfile errorProfile = BranchProfile.create();

        @Specialization(guards = { "isRubyClass(exceptionClass)", "isRubySymbol(timing)" })
        protected Object handle_interrupt(
                DynamicObject self,
                DynamicObject exceptionClass,
                DynamicObject timing,
                DynamicObject block) {
            // TODO (eregon, 12 July 2015): should we consider exceptionClass?
            final InterruptMode newInterruptMode = symbolToInterruptMode(timing);

            final InterruptMode oldInterruptMode = Layouts.THREAD.getInterruptMode(self);
            Layouts.THREAD.setInterruptMode(self, newInterruptMode);
            try {
                return yield(block);
            } finally {
                Layouts.THREAD.setInterruptMode(self, oldInterruptMode);
            }
        }

        private InterruptMode symbolToInterruptMode(DynamicObject symbol) {
            if (symbol == getImmediateSymbol()) {
                return InterruptMode.IMMEDIATE;
            } else if (symbol == getOnBlockingSymbol()) {
                return InterruptMode.ON_BLOCKING;
            } else if (symbol == getNeverSymbol()) {
                return InterruptMode.NEVER;
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError("invalid timing symbol", this));
            }
        }

        private DynamicObject getImmediateSymbol() {
            if (immediateSymbol == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                immediateSymbol = getSymbol("immediate");
            }

            return immediateSymbol;
        }

        private DynamicObject getOnBlockingSymbol() {
            if (onBlockingSymbol == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                onBlockingSymbol = getSymbol("on_blocking");
            }

            return onBlockingSymbol;
        }

        private DynamicObject getNeverSymbol() {
            if (neverSymbol == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                neverSymbol = getSymbol("never");
            }

            return neverSymbol;
        }

    }

    @Primitive(name = "thread_allocate")
    public abstract static class ThreadAllocateNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass,
                @Cached AllocateObjectNode allocateObjectNode) {
            return getContext().getThreadManager().createThread(rubyClass, allocateObjectNode);
        }

    }

    @Primitive(name = "thread_initialized?")
    public abstract static class ThreadIsInitializedNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean isInitialized(DynamicObject thread) {
            final DynamicObject rootFiber = Layouts.THREAD.getFiberManager(thread).getRootFiber();
            final CountDownLatch initializedLatch = Layouts.FIBER.getInitializedLatch(rootFiber);
            return initializedLatch.getCount() == 0;
        }

    }

    @Primitive(name = "thread_initialize")
    @ImportStatic(ArrayGuards.class)
    public abstract static class ThreadInitializeNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(limit = "STORAGE_STRATEGIES")
        protected Object initialize(DynamicObject thread, DynamicObject arguments, DynamicObject block,
                @CachedLibrary("getStore(arguments)") ArrayStoreLibrary stores) {
            final SourceSection sourceSection = Layouts.PROC.getSharedMethodInfo(block).getSourceSection();
            final String info = getContext().fileLine(sourceSection);
            final int argSize = Layouts.ARRAY.getSize(arguments);
            final Object[] args = stores.boxedCopyOfRange(Layouts.ARRAY.getStore(arguments), 0, argSize);
            final String sharingReason = "creating Ruby Thread " + info;

            if (getContext().getOptions().SHARED_OBJECTS_ENABLED) {
                getContext().getThreadManager().startSharing(thread, sharingReason);
                SharedObjects.shareDeclarationFrame(getContext(), block);
            }

            getContext().getThreadManager().initialize(
                    thread,
                    this,
                    info,
                    sharingReason,
                    () -> ProcOperations.rootCall(block, args));
            return nil;
        }

    }

    @CoreMethod(names = "join", optional = 1, lowerFixnum = 1)
    public abstract static class JoinNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject join(DynamicObject thread, NotProvided timeout) {
            doJoin(getContext(), this, thread);
            return thread;
        }

        @Specialization(guards = "isNil(timeout)")
        protected DynamicObject join(DynamicObject thread, Object timeout) {
            return join(thread, NotProvided.INSTANCE);
        }

        @Specialization
        protected Object join(DynamicObject thread, int timeout) {
            return joinMillis(thread, timeout * 1000);
        }

        @Specialization
        protected Object join(DynamicObject thread, double timeout) {
            return joinMillis(thread, (int) (timeout * 1000.0));
        }

        private Object joinMillis(DynamicObject self, int timeoutInMillis) {
            if (doJoinMillis(self, timeoutInMillis)) {
                return self;
            } else {
                return nil;
            }
        }

        @TruffleBoundary
        static void doJoin(RubyContext context, Node currentNode, DynamicObject thread) {
            context.getThreadManager().runUntilResult(currentNode, () -> {
                Layouts.THREAD.getFinishedLatch(thread).await();
                return ThreadManager.BlockingAction.SUCCESS;
            });

            final DynamicObject exception = Layouts.THREAD.getException(thread);
            if (exception != null) {
                context.getCoreExceptions().showExceptionIfDebug(exception);
                VMRaiseExceptionNode.reRaiseException(context, exception);
            }
        }

        @TruffleBoundary
        private boolean doJoinMillis(DynamicObject thread, int timeoutInMillis) {
            final long start = System.currentTimeMillis();

            final boolean joined = getContext().getThreadManager().runUntilResult(this, () -> {
                long now = System.currentTimeMillis();
                long waited = now - start;
                if (waited >= timeoutInMillis) {
                    // We need to know whether countDown() was called and we do not want to block.
                    return Layouts.THREAD.getFinishedLatch(thread).getCount() == 0;
                }
                return Layouts.THREAD.getFinishedLatch(thread).await(timeoutInMillis - waited, TimeUnit.MILLISECONDS);
            });

            if (joined) {
                final DynamicObject exception = Layouts.THREAD.getException(thread);
                if (exception != null) {
                    getContext().getCoreExceptions().showExceptionIfDebug(exception);
                    VMRaiseExceptionNode.reRaiseException(getContext(), exception);
                }
            }

            return joined;
        }

    }

    @CoreMethod(names = "main", onSingleton = true)
    public abstract static class MainNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject main() {
            return getContext().getThreadManager().getRootThread();
        }

    }

    @CoreMethod(names = "pass", onSingleton = true)
    public abstract static class PassNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object pass() {
            Thread.yield();
            return nil;
        }

    }

    @CoreMethod(names = "status")
    public abstract static class StatusNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected Object status(DynamicObject self) {
            // TODO: slightly hackish
            final ThreadStatus status = Layouts.THREAD.getStatus(self);
            if (status == ThreadStatus.DEAD) {
                if (Layouts.THREAD.getException(self) != null) {
                    return nil;
                } else {
                    return false;
                }
            }
            return makeStringNode
                    .executeMake(StringUtils.toLowerCase(status.name()), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @CoreMethod(names = "stop?")
    public abstract static class StopNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean stop(DynamicObject self) {
            final ThreadStatus status = Layouts.THREAD.getStatus(self);
            return status == ThreadStatus.DEAD || status == ThreadStatus.SLEEP;
        }

    }

    @CoreMethod(names = "value")
    public abstract static class ValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object value(DynamicObject self) {
            JoinNode.doJoin(getContext(), this, self);
            final Object value = Layouts.THREAD.getValue(self);
            assert value != null;
            return value;
        }

    }

    @CoreMethod(names = { "wakeup", "run" })
    public abstract static class WakeupNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject wakeup(DynamicObject rubyThread) {
            final DynamicObject currentFiber = Layouts.THREAD.getFiberManager(rubyThread).getCurrentFiberRacy();
            final Thread thread = Layouts.FIBER.getThread(currentFiber);
            if (!Layouts.FIBER.getAlive(currentFiber) || thread == null) {
                throw new RaiseException(getContext(), coreExceptions().threadErrorKilledThread(this));
            }

            // This only interrupts Kernel#sleep, Mutex#sleep and ConditionVariable#wait by having those check for the
            // wakeup flag. Other operations just retry when interrupted.
            Layouts.THREAD.getWakeUp(rubyThread).set(true);
            getContext().getThreadManager().interrupt(thread);

            return rubyThread;
        }

    }

    @NonStandard
    @CoreMethod(names = "unblock", required = 2)
    public abstract static class UnblockNode extends YieldingCoreMethodNode {

        @Specialization(guards = "isRubyProc(runner)")
        protected Object unblock(DynamicObject thread, Object unblocker, DynamicObject runner,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final ThreadManager threadManager = getContext().getThreadManager();
            final UnblockingAction unblockingAction;
            if (unblocker == nil) {
                unblockingAction = threadManager.getNativeCallUnblockingAction();
            } else {
                unblockingAction = makeUnblockingAction(unblocker);
            }
            final UnblockingActionHolder actionHolder = threadManager.getActionHolder(Thread.currentThread());

            final UnblockingAction oldAction = actionHolder.changeTo(unblockingAction);
            try {
                Object result;
                do {
                    final ThreadStatus status = Layouts.THREAD.getStatus(thread);
                    Layouts.THREAD.setStatus(thread, ThreadStatus.SLEEP);

                    try {
                        result = yield(runner);
                    } finally {
                        Layouts.THREAD.setStatus(thread, status);
                    }
                } while (loopProfile.profile(result == null));

                return result;
            } finally {
                actionHolder.restore(oldAction);
            }
        }

        @TruffleBoundary
        private UnblockingAction makeUnblockingAction(Object unblocker) {
            assert RubyGuards.isRubyProc(unblocker);
            final DynamicObject unblockerProc = (DynamicObject) unblocker;
            return () -> yield(unblockerProc);
        }

    }

    @CoreMethod(names = "abort_on_exception")
    public abstract static class AbortOnExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean abortOnException(DynamicObject self) {
            return Layouts.THREAD.getAbortOnException(self);
        }

    }

    @CoreMethod(names = "abort_on_exception=", required = 1)
    public abstract static class SetAbortOnExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object setAbortOnException(DynamicObject self, boolean abortOnException) {
            Layouts.THREAD.setAbortOnException(self, abortOnException);
            return nil;
        }

    }

    @CoreMethod(names = "list", onSingleton = true)
    public abstract static class ListNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject list() {
            final Object[] threads = getContext().getThreadManager().getThreadList();
            return createArray(threads, threads.length);
        }
    }

    @Primitive(name = "thread_raise")
    public static abstract class ThreadRaisePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyThread(thread)", "isRubyException(exception)" })
        protected Object raise(DynamicObject thread, DynamicObject exception) {
            raiseInThread(getContext(), thread, exception, this);
            return nil;
        }

        @TruffleBoundary
        public static void raiseInThread(RubyContext context, DynamicObject rubyThread, DynamicObject exception,
                Node currentNode) {
            // The exception will be shared with another thread
            SharedObjects.writeBarrier(context, exception);

            context.getSafepointManager().pauseRubyThreadAndExecute(
                    rubyThread,
                    currentNode,
                    (currentThread, currentNode1) -> {
                        if (Layouts.EXCEPTION.getBacktrace(exception) == null) {
                            Backtrace backtrace = context.getCallStack().getBacktrace(currentNode1);
                            Layouts.EXCEPTION.setBacktrace(exception, backtrace);
                        }

                        VMRaiseExceptionNode.reRaiseException(context, exception);
                    });
        }

    }

    @Primitive(name = "thread_source_location")
    public static abstract class ThreadSourceLocationNode extends PrimitiveArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization(guards = "isRubyThread(thread)")
        protected DynamicObject sourceLocation(DynamicObject thread) {
            return makeStringNode
                    .executeMake(Layouts.THREAD.getSourceLocation(thread), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "name")
    public static abstract class ThreadNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyThread(thread)")
        protected Object getName(DynamicObject thread) {
            return Layouts.THREAD.getName(thread);
        }
    }

    @Primitive(name = "thread_set_name")
    public static abstract class ThreadSetNamePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyThread(thread)")
        protected Object setName(DynamicObject thread, Object name) {
            Layouts.THREAD.setName(thread, name);
            return name;
        }
    }

    @Primitive(name = "thread_get_priority")
    public static abstract class ThreadGetPriorityPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyThread(thread)")
        protected int getPriority(DynamicObject thread) {
            final Thread javaThread = Layouts.THREAD.getThread(thread);
            if (javaThread != null) {
                return javaThread.getPriority();
            } else {
                return Layouts.THREAD.getPriority(thread);
            }
        }

    }

    @Primitive(name = "thread_set_priority", lowerFixnum = 1)
    public static abstract class ThreadSetPriorityPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyThread(thread)")
        protected int getPriority(DynamicObject thread, int javaPriority) {
            final Thread javaThread = Layouts.THREAD.getThread(thread);
            if (javaThread != null) {
                javaThread.setPriority(javaPriority);
            }
            Layouts.THREAD.setPriority(thread, javaPriority);
            return javaPriority;
        }

    }

    @Primitive(name = "thread_set_group")
    public static abstract class ThreadSetGroupPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyThread(thread)")
        protected DynamicObject setGroup(DynamicObject thread, DynamicObject threadGroup) {
            Layouts.THREAD.setThreadGroup(thread, threadGroup);
            return threadGroup;
        }
    }

    @Primitive(name = "thread_get_exception")
    public static abstract class ThreadGetExceptionNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getException(VirtualFrame frame,
                @Cached GetCurrentRubyThreadNode getThreadNode) {
            return getLastException(getThreadNode.executeGetRubyThread(frame));
        }

        private static Object getLastException(DynamicObject currentThread) {
            return Layouts.THREAD.getThreadLocalGlobals(currentThread).exception;
        }

        public static Object getLastException(RubyContext context) {
            return getLastException(context.getThreadManager().getCurrentThread());
        }

    }

    @Primitive(name = "thread_get_return_code")
    public static abstract class ThreadGetReturnCodeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getExitCode(VirtualFrame frame,
                @Cached GetCurrentRubyThreadNode getThreadNode) {
            return Layouts.THREAD.getThreadLocalGlobals(getThreadNode.executeGetRubyThread(frame)).processStatus;
        }
    }

    @Primitive(name = "thread_set_exception")
    public static abstract class SetThreadLocalExceptionNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setException(VirtualFrame frame, Object exception,
                @Cached GetCurrentRubyThreadNode getThreadNode) {
            return Layouts.THREAD
                    .getThreadLocalGlobals(getThreadNode.executeGetRubyThread(frame)).exception = exception;
        }
    }

    @Primitive(name = "thread_set_return_code")
    public static abstract class SetThreadLocalReturnCodeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getException(VirtualFrame frame, DynamicObject returnCode,
                @Cached GetCurrentRubyThreadNode getThreadNode) {
            return Layouts.THREAD
                    .getThreadLocalGlobals(getThreadNode.executeGetRubyThread(frame)).processStatus = returnCode;
        }
    }

    @Primitive(name = "thread_get_fiber_locals")
    public static abstract class ThreadGetFiberLocalsNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyThread(thread)")
        protected DynamicObject getFiberLocals(DynamicObject thread) {
            final DynamicObject fiber = Layouts.THREAD.getFiberManager(thread).getCurrentFiberRacy();
            return Layouts.FIBER.getFiberLocals(fiber);
        }
    }

    @Primitive(name = "thread_run_blocking_nfi_system_call")
    public static abstract class ThreadRunBlockingSystemCallNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyProc(block)")
        protected Object runBlockingSystemCall(DynamicObject block,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile,
                @Cached YieldNode yieldNode) {
            final ThreadManager threadManager = getContext().getThreadManager();
            final UnblockingAction unblockingAction = threadManager.getNativeCallUnblockingAction();
            final DynamicObject thread = threadManager.getCurrentThread();
            final UnblockingActionHolder actionHolder = threadManager.getActionHolder(Thread.currentThread());

            final UnblockingAction oldAction = actionHolder.changeTo(unblockingAction);
            try {
                Object result;
                do {
                    final ThreadStatus status = Layouts.THREAD.getStatus(thread);
                    Layouts.THREAD.setStatus(thread, ThreadStatus.SLEEP);

                    try {
                        result = yieldNode.executeDispatch(block);
                    } finally {
                        Layouts.THREAD.setStatus(thread, status);
                    }
                } while (loopProfile.profile(result == NotProvided.INSTANCE));

                return result;
            } finally {
                actionHolder.restore(oldAction);
            }
        }
    }

}
