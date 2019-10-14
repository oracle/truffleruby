/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.bytes;

import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.core.format.FormatFrameDescriptor;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.MissingValue;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.TaintNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@NodeChild("value")
public abstract class ReadStringPointerNode extends FormatNode {

    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    private final BranchProfile errorProfile = BranchProfile.create();
    private final int limit;

    public ReadStringPointerNode(int limit) {
        this.limit = limit;
    }

    @Specialization(guards = "isNil(nil)")
    protected MissingValue decode(DynamicObject nil) {
        return MissingValue.INSTANCE;
    }

    @Specialization
    protected Object read(VirtualFrame frame, long address,
            @Cached TaintNode taintNode) {
        final Pointer pointer = new Pointer(address);
        checkAssociated(
                (Pointer[]) FrameUtil.getObjectSafe(frame, FormatFrameDescriptor.SOURCE_ASSOCIATED_SLOT),
                pointer);

        final byte[] bytes = pointer.readZeroTerminatedByteArray(getContext(), 0, limit);
        final DynamicObject string = makeStringNode.executeMake(bytes, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        taintNode.executeTaint(string);
        return string;
    }

    private void checkAssociated(Pointer[] associated, Pointer reading) {
        if (associated != null) {
            for (Pointer pointer : associated) {
                if (pointer.equals(reading)) {
                    return;
                }
            }
        }

        errorProfile.enter();
        throw new RaiseException(
                getContext(),
                getContext().getCoreExceptions().argumentError("no associated pointer", this));
    }

}
