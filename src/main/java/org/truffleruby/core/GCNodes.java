/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.collections.WeakValueCache;

@CoreModule("GC")
public abstract class GCNodes {

    @Primitive(name = "gc_start")
    public static abstract class GCStartPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object vmGCStart() {
            getContext().getMarkingService().queueMarking();
            System.gc();
            return nil;
        }

    }

    /** Attempts to run the garbage collector. This cannot be guaranteed in general, but calling this method should be
     * much more likely to actually trigger GC than calling {@link System#gc()} or Ruby's {@link GCStartPrimitiveNode}.
     *
     * <p>
     * In particular, this attempts to trigger the GC by waiting until a weak reference has been cleared.
     *
     * <p>
     * Note that even when GC is triggered, there is not guarantee that the all the garbage has been cleared or all the
     * memory reclaimed. */
    @Primitive(name = "gc_force")
    public static abstract class GCForce extends PrimitiveArrayArgumentsNode {

        @SuppressFBWarnings("DLS")
        @TruffleBoundary
        @Specialization
        protected Object force() {
            getContext().getMarkingService().queueMarking();
            Object key = new Object();
            Object value = new Object();

            // NOTE(norswap, 16 Apr 20): We could have used a WeakReference here, but the hope is that the extra
            // indirection will prevent the compiler to optimize this method away (assuming JIT compilation, and
            // the fact that such an optimizaton is permitted and possible, both of which are unlikely).
            WeakValueCache<Object, Object> cache = new WeakValueCache<>();
            cache.put(key, value);
            value = null;

            do {
                System.gc();
                getContext().getSafepointManager().poll(this);
            } while (cache.get(key) != null);

            return nil;
        }
    }


    @CoreMethod(names = "count", onSingleton = true)
    public abstract static class CountNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected int count() {
            return getCollectionCount();
        }

        public static int getCollectionCount() {
            int count = 0;
            for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
                count += bean.getCollectionCount();
            }
            return count;
        }

    }

    @CoreMethod(names = "time", onSingleton = true)
    public abstract static class TimeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected long time() {
            return getCollectionTime();
        }

        public static long getCollectionTime() {
            long time = 0;
            for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
                time += bean.getCollectionTime();
            }
            return time;
        }

    }

}
