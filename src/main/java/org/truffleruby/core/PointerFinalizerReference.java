/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.ref.ReferenceQueue;

public class PointerFinalizerReference
        extends
        ReferenceProcessingService.PhantomProcessingReference<PointerFinalizerReference, Object> {

    long address;

    PointerFinalizerReference(
            Object object,
            ReferenceQueue<? super Object> queue,
            PointerFinalizationService service,
            long address) {
        super(object, queue, service);
        this.address = address;
    }

    public void markFreed() {
        address = 0;
    }
}
