/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public final class LocalReturnNode extends RubyContextSourceNode {

    @Child private RubyNode value;

    public LocalReturnNode(RubyNode value) {
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        throw new LocalReturnException(value.execute(frame));
    }

    @Override
    public boolean isContinuable() {
        return false;
    }

    @Override
    public RubyNode simplifyAsTailExpression() {
        return value;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new LocalReturnNode(value.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
