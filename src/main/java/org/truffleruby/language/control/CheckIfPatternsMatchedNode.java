/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;

public final class CheckIfPatternsMatchedNode extends RubyContextSourceNode {

    @Child RubyNode inspected;
    @Child ToJavaStringNode toJavaStringNode;

    public CheckIfPatternsMatchedNode(RubyNode inspected) {
        this.inspected = inspected;
        this.toJavaStringNode = ToJavaStringNode.create();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        String message = toJavaStringNode.executeToJavaString(inspected.execute(frame));
        throw new RaiseException(getContext(), coreExceptions().noMatchingPatternError(message, this));
    }

    @Override
    public RubyNode cloneUninitialized() {
        return new CheckIfPatternsMatchedNode(inspected.cloneUninitialized()).copyFlags(this);
    }
}
