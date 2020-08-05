/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.interop.messages.RubyStringMessages;
import org.truffleruby.language.RubyDynamicObject;

public class RubyString extends RubyDynamicObject {

    public boolean frozen;
    public boolean tainted;
    public Rope rope;

    public RubyString(Shape shape, boolean frozen, boolean tainted, Rope rope) {
        super(shape);
        this.frozen = frozen;
        this.tainted = tainted;
        this.rope = rope;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyStringMessages.class;
    }

    @Override
    public String toString() {
        return rope.toString();
    }

}
