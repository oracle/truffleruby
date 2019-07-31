/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyBaseWithoutContextNode;

public class PropagateSharingNode extends RubyBaseWithoutContextNode {

    @Child private IsSharedNode isSharedNode;
    @Child private WriteBarrierNode writeBarrierNode;

    public static PropagateSharingNode create() {
        return new PropagateSharingNode();
    }

    public PropagateSharingNode() {
        isSharedNode = IsSharedNodeGen.create();
    }

    public void propagate(DynamicObject source, Object value) {
        if (isSharedNode.executeIsShared(source)) {
            writeBarrier(value);
        }
    }

    private void writeBarrier(Object value) {
        if (writeBarrierNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            writeBarrierNode = insert(WriteBarrierNode.create());
        }
        writeBarrierNode.executeWriteBarrier(value);
    }

}
