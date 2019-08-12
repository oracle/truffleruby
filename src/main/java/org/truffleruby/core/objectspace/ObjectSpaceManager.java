/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
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
 * Copyright (C) 2002 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
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
package org.truffleruby.core.objectspace;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.truffleruby.cext.ValueWrapperManager;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.utilities.AssumedValue;

/**
 * Supports the Ruby {@code ObjectSpace} module. Object IDs are lazily allocated {@code long}
 * values, mapped to objects with a weak hash map.
 */
public class ObjectSpaceManager {

    private final AssumedValue<Boolean> isTracing = new AssumedValue<>("object-space-tracing", Boolean.FALSE);
    private final AtomicInteger tracingAssumptionActivations = new AtomicInteger(0);
    private final ThreadLocal<Boolean> tracingPaused = ThreadLocal.withInitial(() -> false);

    private final AtomicLong nextObjectID = new AtomicLong(ValueWrapperManager.TAG_MASK + 1);

    public void traceAllocationsStart() {
        if (tracingAssumptionActivations.incrementAndGet() == 1) {
            isTracing.set(Boolean.TRUE);
        }
    }

    public void traceAllocationsStop() {
        if (tracingAssumptionActivations.decrementAndGet() == 0) {
            isTracing.set(Boolean.FALSE);
        }
    }

    public boolean isTracing() {
        return isTracing.get();
    }

    public boolean isTracingPaused() {
        return tracingPaused.get();
    }

    public void setTracingPaused(boolean tracingPaused) {
        this.tracingPaused.set(tracingPaused);
    }

    public long getNextObjectID() {
        final long id = nextObjectID.getAndAdd(ValueWrapperManager.TAG_MASK + 1);

        if (id == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new RuntimeException("Object IDs exhausted");
        }

        return id;
    }

    public static int getCollectionCount() {
        int count = 0;
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            count += bean.getCollectionCount();
        }
        return count;
    }

}
