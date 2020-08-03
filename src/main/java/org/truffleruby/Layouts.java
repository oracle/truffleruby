/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.object.HiddenKey;
import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.core.basicobject.BasicObjectLayoutImpl;
import org.truffleruby.core.fiber.FiberLayout;
import org.truffleruby.core.fiber.FiberLayoutImpl;
import org.truffleruby.core.hash.HashLayout;
import org.truffleruby.core.hash.HashLayoutImpl;
import org.truffleruby.core.klass.ClassLayout;
import org.truffleruby.core.klass.ClassLayoutImpl;
import org.truffleruby.core.method.UnboundMethodLayout;
import org.truffleruby.core.method.UnboundMethodLayoutImpl;
import org.truffleruby.core.module.ModuleLayout;
import org.truffleruby.core.module.ModuleLayoutImpl;
import org.truffleruby.core.proc.ProcLayout;
import org.truffleruby.core.proc.ProcLayoutImpl;
import org.truffleruby.core.queue.SizedQueueLayout;
import org.truffleruby.core.queue.SizedQueueLayoutImpl;
import org.truffleruby.core.range.IntRangeLayout;
import org.truffleruby.core.range.IntRangeLayoutImpl;
import org.truffleruby.core.range.LongRangeLayout;
import org.truffleruby.core.range.LongRangeLayoutImpl;
import org.truffleruby.core.range.ObjectRangeLayout;
import org.truffleruby.core.range.ObjectRangeLayoutImpl;
import org.truffleruby.core.string.StringLayout;
import org.truffleruby.core.string.StringLayoutImpl;
import org.truffleruby.core.thread.ThreadBacktraceLocationLayout;
import org.truffleruby.core.thread.ThreadBacktraceLocationLayoutImpl;
import org.truffleruby.core.thread.ThreadLayout;
import org.truffleruby.core.thread.ThreadLayoutImpl;
import org.truffleruby.core.time.TimeLayout;
import org.truffleruby.core.time.TimeLayoutImpl;
import org.truffleruby.core.tracepoint.TracePointLayout;
import org.truffleruby.core.tracepoint.TracePointLayoutImpl;
import org.truffleruby.extra.AtomicReferenceLayout;
import org.truffleruby.extra.AtomicReferenceLayoutImpl;
import org.truffleruby.stdlib.digest.DigestLayout;
import org.truffleruby.stdlib.digest.DigestLayoutImpl;

public abstract class Layouts {

    // Standard identifiers
    // These must appear before the generated layout list so the identifiers have been initialized by the time
    // the layout singletons are created.

    public static final HiddenKey OBJECT_ID_IDENTIFIER = new HiddenKey("object_id"); // long
    public static final HiddenKey TAINTED_IDENTIFIER = new HiddenKey("tainted?"); // boolean
    public static final HiddenKey FROZEN_IDENTIFIER = new HiddenKey("frozen?"); // boolean
    public static final HiddenKey ASSOCIATED_IDENTIFIER = new HiddenKey("associated"); // Pointer[]
    public static final HiddenKey FINALIZER_REF_IDENTIFIER = new HiddenKey("finalizerRef"); // FinalizerReference
    public static final HiddenKey MARKED_OBJECTS_IDENTIFIER = new HiddenKey("marked_objects"); // Object[]
    public static final HiddenKey VALUE_WRAPPER_IDENTIFIER = new HiddenKey("value_wrapper"); // ValueWrapper

    // Generated layouts

    public static final BasicObjectLayout BASIC_OBJECT = BasicObjectLayoutImpl.INSTANCE;
    public static final ClassLayout CLASS = ClassLayoutImpl.INSTANCE;
    public static final FiberLayout FIBER = FiberLayoutImpl.INSTANCE;
    public static final HashLayout HASH = HashLayoutImpl.INSTANCE;
    public static final IntRangeLayout INT_RANGE = IntRangeLayoutImpl.INSTANCE;
    public static final LongRangeLayout LONG_RANGE = LongRangeLayoutImpl.INSTANCE;
    public static final ModuleLayout MODULE = ModuleLayoutImpl.INSTANCE;
    public static final ObjectRangeLayout OBJECT_RANGE = ObjectRangeLayoutImpl.INSTANCE;
    public static final ProcLayout PROC = ProcLayoutImpl.INSTANCE;
    public static final SizedQueueLayout SIZED_QUEUE = SizedQueueLayoutImpl.INSTANCE;
    public static final StringLayout STRING = StringLayoutImpl.INSTANCE;
    public static final ThreadLayout THREAD = ThreadLayoutImpl.INSTANCE;
    public static final ThreadBacktraceLocationLayout THREAD_BACKTRACE_LOCATION = ThreadBacktraceLocationLayoutImpl.INSTANCE;
    public static final TimeLayout TIME = TimeLayoutImpl.INSTANCE;
    public static final UnboundMethodLayout UNBOUND_METHOD = UnboundMethodLayoutImpl.INSTANCE;
    public static final AtomicReferenceLayout ATOMIC_REFERENCE = AtomicReferenceLayoutImpl.INSTANCE;
    public static final TracePointLayout TRACE_POINT = TracePointLayoutImpl.INSTANCE;
    public static final DigestLayout DIGEST = DigestLayoutImpl.INSTANCE;

}
