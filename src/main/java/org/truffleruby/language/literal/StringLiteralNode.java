/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.literal;

import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.objects.AllocationTracing;

public class StringLiteralNode extends RubyContextSourceNode {

    private final Rope rope;

    public StringLiteralNode(Rope rope) {
        this.rope = rope;
    }

    @Override
    public RubyString execute(VirtualFrame frame) {
        final RubyString string = new RubyString(
                coreLibrary().stringClass,
                getLanguage().stringShape,
                false,
                rope,
                getContext().getEncodingManager().getRubyEncoding(rope.encoding));
        AllocationTracing.trace(string, this);
        return string;
    }

}
