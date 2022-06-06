/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.debug.SingleMemberDescriptor;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.ReadVariableTag;
import com.oracle.truffle.api.instrumentation.Tag;

public abstract class ReadLocalNode extends RubyContextSourceNode {

    protected final int frameSlot;
    protected final LocalVariableType type;

    @Child protected ReadFrameSlotNode readFrameSlotNode;

    public ReadLocalNode(int frameSlot, LocalVariableType type) {
        this.frameSlot = frameSlot;
        this.type = type;
    }

    protected abstract Object readFrameSlot(VirtualFrame frame);

    public abstract WriteLocalNode makeWriteNode(RubyNode rhs);

    protected abstract String getVariableName();

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        switch (type) {
            case FRAME_LOCAL:
                return FrozenStrings.LOCAL_VARIABLE;

            case FRAME_LOCAL_GLOBAL:
                if (Nil.isNot(readFrameSlot(frame))) {
                    return FrozenStrings.GLOBAL_VARIABLE;
                } else {
                    return nil();
                }

            default:
                throw Utils.unsupportedOperation("didn't expect local type ", type);
        }
    }


    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        return tag == ReadVariableTag.class || super.hasTag(tag);
    }

    @Override
    public Object getNodeObject() {
        return new SingleMemberDescriptor(ReadVariableTag.NAME, getVariableName());
    }

}
