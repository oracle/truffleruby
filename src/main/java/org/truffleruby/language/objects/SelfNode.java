/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.object.HiddenKey;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.locals.ReadFrameSlotNode;
import org.truffleruby.language.locals.ReadFrameSlotNodeGen;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

public class SelfNode extends RubyContextSourceNode {

    public static final int SELF_INDEX = 0;
    public static final HiddenKey SELF_IDENTIFIER = new HiddenKey("(self)");

    @Child private ReadFrameSlotNode readSelfSlotNode;

    public SelfNode() {
    }

    @Override
    public Object execute(VirtualFrame frame) {
        assert frame.getFrameDescriptor().getSlotName(SELF_INDEX) == SELF_IDENTIFIER;

        if (readSelfSlotNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readSelfSlotNode = insert(ReadFrameSlotNodeGen.create(SELF_INDEX));
        }

        return readSelfSlotNode.executeRead(frame);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return FrozenStrings.SELF;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new SelfNode();
        return copy.copyFlags(this);
    }

}
