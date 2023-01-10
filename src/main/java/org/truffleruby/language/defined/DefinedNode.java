/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.defined;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class DefinedNode extends RubyContextSourceNode {

    @Child private RubyNode child;

    public DefinedNode(RubyNode child) {
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return child.isDefined(frame, getLanguage(), getContext());
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new DefinedNode(child.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
