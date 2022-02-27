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

public class DataObjectFinalizerReference
        extends
        ReferenceProcessingService.PhantomProcessingReference<DataObjectFinalizerReference, Object> {

    final Object callable;
    final Object dataHolder;

    DataObjectFinalizerReference(Object object, ReferenceQueue<? super Object> queue, DataObjectFinalizationService service, Object callable, Object dataHolder) {
        super(object, queue, service);
        this.callable = callable;
        this.dataHolder = dataHolder;
    }
}
