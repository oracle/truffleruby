/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyAtomicReferenceMessages;
import org.truffleruby.language.RubyDynamicObject;

import java.util.concurrent.atomic.AtomicReference;

public class RubyAtomicReference extends RubyDynamicObject {

    final AtomicReference<Object> value;

    public RubyAtomicReference(Shape shape, AtomicReference<Object> value) {
        super(shape);
        this.value = value;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyAtomicReferenceMessages.class;
    }

}
