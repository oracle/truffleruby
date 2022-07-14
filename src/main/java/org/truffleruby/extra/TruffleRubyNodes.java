/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.core.proc.RubyProc;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyDynamicObject;

import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.ReentrantLock;

@CoreModule("TruffleRuby")
public abstract class TruffleRubyNodes {

    @CoreMethod(names = "graalvm_home", onSingleton = true)
    public abstract static class GraalvmHomeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object graalvmHome(
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            String value = getProperty("org.graalvm.home");
            if (value == null) {
                return nil;
            } else {
                return createString(fromJavaStringNode, value, Encodings.UTF_8);
            }
        }

        @TruffleBoundary
        private static String getProperty(String key) {
            return System.getProperty(key);
        }

    }

    @CoreMethod(names = "jit?", onSingleton = true)
    public abstract static class GraalNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected static boolean isGraal() {
            return Truffle.getRuntime().getName().contains("Graal");
        }

    }

    @CoreMethod(names = "native?", onSingleton = true)
    public abstract static class NativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isNative() {
            return TruffleOptions.AOT;
        }

    }

    @CoreMethod(names = "cexts?", onSingleton = true)
    public abstract static class SulongNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isSulong() {
            return isSulongAvailable(getContext());
        }

        @TruffleBoundary
        public static boolean isSulongAvailable(RubyContext context) {
            return context.getEnv().isMimeTypeSupported(RubyLanguage.LLVM_BITCODE_MIME_TYPE);
        }

    }

    @CoreMethod(names = "full_memory_barrier", onSingleton = true)
    public abstract static class FullMemoryBarrierPrimitiveNode extends CoreMethodNode {
        @Specialization
        protected Object fullMemoryBarrier() {
            VarHandle.fullFence();
            return nil;
        }
    }

    @CoreMethod(names = "synchronized", onSingleton = true, required = 1, needsBlock = true)
    public abstract static class SynchronizedNode extends YieldingCoreMethodNode {

        /** We must not allow to synchronize on boxed primitives as that would be misleading. We use a ReentrantLock and
         * not simply Java's {@code synchronized} here as we need to be able to interrupt for guest safepoints and it is
         * not possible to interrupt Java's {@code synchronized (object) {}}. */
        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object synchronize(RubyDynamicObject object, RubyProc block,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary,
                @Cached BranchProfile initializeLockProfile) {
            final ReentrantLock lock = getLock(object, objectLibrary, initializeLockProfile);

            MutexOperations.lockInternal(getContext(), lock, this);
            try {
                return callBlock(block);
            } finally {
                MutexOperations.unlockInternal(lock);
            }
        }

        private ReentrantLock getLock(RubyDynamicObject object, DynamicObjectLibrary objectLibrary,
                BranchProfile initializeLockProfile) {
            ReentrantLock lock = (ReentrantLock) objectLibrary.getOrDefault(object, Layouts.OBJECT_LOCK, null);
            if (lock != null) {
                return lock;
            }

            initializeLockProfile.enter();
            synchronized (object) {
                lock = (ReentrantLock) objectLibrary.getOrDefault(object, Layouts.OBJECT_LOCK, null);
                if (lock != null) {
                    return lock;
                } else {
                    lock = new ReentrantLock();
                    objectLibrary.put(object, Layouts.OBJECT_LOCK, lock);
                    return lock;
                }
            }
        }

    }

}
