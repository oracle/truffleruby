/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.write.bytes;

import org.truffleruby.core.format.FormatNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

/**
 * Simply write bytes.
 */
@NodeChild("width")
@NodeChild("value")
public abstract class WritePaddedBytesNode extends FormatNode {

    private final ConditionProfile leftJustifiedProfile = ConditionProfile.createBinaryProfile();
    private final boolean leftJustified;

    public WritePaddedBytesNode(boolean leftJustified) {
        this.leftJustified = leftJustified;
    }

    @Specialization
    public Object write(VirtualFrame frame, int padding, byte[] bytes) {
        if (leftJustifiedProfile.profile(leftJustified)) {
            return writeLeftJustified(frame, padding, bytes);
        } else {
            return writeRightJustified(frame, padding, bytes);
        }
    }

    private Object writeLeftJustified(VirtualFrame frame, int padding, byte[] bytes) {
        writeBytes(frame, bytes);

        for (int n = 0; n < padding - bytes.length; n++) {
            writeByte(frame, (byte) ' ');
        }

        return null;
    }

    private Object writeRightJustified(VirtualFrame frame, int padding, byte[] bytes) {
        for (int n = 0; n < padding - bytes.length; n++) {
            writeByte(frame, (byte) ' ');
        }

        writeBytes(frame, bytes);
        return null;
    }

}
