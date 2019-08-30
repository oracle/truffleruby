/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.ref.ReferenceQueue;

import com.oracle.truffle.api.object.DynamicObject;

class MarkerReference extends ReferenceProcessingService.WeakProcessingReference<MarkerReference, Object> {

    final MarkingService.MarkerAction action;

    MarkerReference(
            Object object,
            ReferenceQueue<? super Object> queue,
            MarkingService.MarkerAction action,
            MarkingService service) {
        super(object, queue, service);
        this.action = action;
    }
}
