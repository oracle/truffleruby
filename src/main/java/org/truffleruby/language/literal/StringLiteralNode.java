/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
/*
 * Copyright (c) 2013, 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.literal;

import org.truffleruby.Layouts;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class StringLiteralNode extends RubyNode {

    @Child AllocateObjectNode allocateNode = AllocateObjectNode.create();

    private final Rope rope;

    public StringLiteralNode(Rope rope) {
        assert getContext().getRopeCache().contains(rope);
        this.rope = rope;
    }

    @Override
    public DynamicObject execute(VirtualFrame frame) {
        return allocateNode.allocate(coreLibrary().getStringClass(), Layouts.STRING.build(false, false, rope));
    }

}
