/*
 * Copyright (c) 2013, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;

/** Dead nodes are removed wherever they are found during translation. They fill in for some missing nodes when we're
 * processing the AST. */
public final class DeadNode extends RubyContextSourceNode {

    private final String reason;

    public DeadNode(String reason) {
        this.reason = reason;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        throw CompilerDirectives.shouldNotReachHere(reason);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new DeadNode(reason);
        return copy.copyFlags(this);
    }

}
