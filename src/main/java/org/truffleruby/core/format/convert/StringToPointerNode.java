/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import java.util.ArrayList;
import java.util.List;

import org.truffleruby.cext.CExtNodes;
import org.truffleruby.core.format.FormatFrameDescriptor;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.objects.TaintNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild("value")
public abstract class StringToPointerNode extends FormatNode {

    @Specialization(guards = "isNil(nil)")
    public long toPointer(DynamicObject nil) {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Specialization(guards = "isRubyString(string)")
    public long toPointer(VirtualFrame frame, DynamicObject string,
            @Cached CExtNodes.StringToNativeNode stringToNativeNode,
            @Cached TaintNode taintNode) {
        taintNode.executeTaint(string);

        final Pointer pointer = stringToNativeNode.executeToNative(string).getNativePointer();

        List<Pointer> associated;

        try {
            associated = (List<Pointer>) frame.getObject(FormatFrameDescriptor.ASSOCIATED_SLOT);
        } catch (FrameSlotTypeException e) {
            throw new JavaException(e);
        }

        if (associated == null) {
            associated = new ArrayList<>();
            frame.setObject(FormatFrameDescriptor.ASSOCIATED_SLOT, associated);
        }

        associated.add(pointer);

        return pointer.getAddress();
    }

}
