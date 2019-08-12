package org.truffleruby.core;

import java.lang.ref.ReferenceQueue;

import com.oracle.truffle.api.object.DynamicObject;

class MarkerReference extends ReferenceProcessingService.WeakProcessingReference<MarkerReference, DynamicObject> {

    final MarkingService.MarkerAction action;

    MarkerReference(
            DynamicObject object,
            ReferenceQueue<? super Object> queue,
            MarkingService.MarkerAction action,
            MarkingService service) {
        super(object, queue, service);
        this.action = action;
    }
}
