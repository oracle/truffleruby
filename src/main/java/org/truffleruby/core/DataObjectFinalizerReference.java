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

import org.truffleruby.cext.DataHolder;

public class DataObjectFinalizerReference
        extends
        ReferenceProcessingService.PhantomProcessingReference<DataObjectFinalizerReference, Object> {

    public final Object callable;
    public final DataHolder dataHolder;

    DataObjectFinalizerReference(
            Object object,
            ReferenceQueue<? super Object> queue,
            DataObjectFinalizationService service,
            Object callable,
            DataHolder dataHolder) {
        super(object, queue, service);
        this.callable = callable;
        this.dataHolder = dataHolder;
    }
}
