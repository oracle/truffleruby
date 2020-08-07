/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import org.truffleruby.core.module.RubyModule;
import org.truffleruby.interop.messages.RubyUnboundMethodMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

public class RubyUnboundMethod extends RubyDynamicObject {

    final RubyModule origin;
    public final InternalMethod method;

    public RubyUnboundMethod(Shape shape, RubyModule origin, InternalMethod method) {
        super(shape);
        this.origin = origin;
        this.method = method;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyUnboundMethodMessages.class;
    }

}
