/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
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

public class InvalidReturnNode extends RubyContextSourceNode {

    @Child public RubyNode value;

    public InvalidReturnNode(RubyNode value) {
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        value.doExecuteVoid(frame);
        throw new RaiseException(getContext(), coreExceptions().unexpectedReturn(this));
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new InvalidReturnNode(value.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
