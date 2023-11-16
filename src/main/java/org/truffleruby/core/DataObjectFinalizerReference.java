/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.ref.ReferenceQueue;

import org.truffleruby.core.ReferenceProcessingService.PhantomProcessingReference;

public final class DataObjectFinalizerReference
        extends
        PhantomProcessingReference<DataObjectFinalizerReference, Object> {

    public final Object finalizerCFunction;
    public final Object dataStruct;

    DataObjectFinalizerReference(
            Object object,
            ReferenceQueue<? super Object> queue,
            DataObjectFinalizationService service,
            Object finalizerCFunction,
            Object dataStruct) {
        super(object, queue, service);
        this.finalizerCFunction = finalizerCFunction;
        this.dataStruct = dataStruct;
    }
}
