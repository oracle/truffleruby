/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.debug.SingleMemberDescriptor;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags.WriteVariableTag;
import com.oracle.truffle.api.instrumentation.Tag;

public abstract class WriteLocalNode extends RubyContextSourceNode implements AssignableNode {

    protected final FrameSlot frameSlot;

    @Child protected RubyNode valueNode;

    public WriteLocalNode(FrameSlot frameSlot, RubyNode valueNode) {
        this.frameSlot = frameSlot;
        this.valueNode = valueNode;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return FrozenStrings.ASSIGNMENT;
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        return tag == WriteVariableTag.class || super.hasTag(tag);
    }

    @Override
    public Object getNodeObject() {
        return new SingleMemberDescriptor(WriteVariableTag.NAME, frameSlot.getIdentifier().toString());
    }

}
