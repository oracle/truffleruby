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

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyUnboundMethodMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.methods.InternalMethod;

public class RubyUnboundMethod extends RubyDynamicObject {


    final DynamicObject origin;
    public final InternalMethod method;

    public RubyUnboundMethod(Shape shape, DynamicObject origin, InternalMethod method) {
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
