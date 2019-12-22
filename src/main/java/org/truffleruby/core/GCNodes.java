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

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule("GC")
public abstract class GCNodes {

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
