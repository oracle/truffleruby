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

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Executes a child node just once, and uses the same value each subsequent time the node is executed. */
public class OnceNode extends RubyContextSourceNode {

    @Child private RubyNode child;

    @CompilationFinal private volatile Object cachedValue;

    public OnceNode(RubyNode child) {
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object value = cachedValue;

        if (value == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            synchronized (this) {
                // Read `cachedValue` again to check if the value was updated by another thread while this thread
                // was waiting on the lock. If it's still null, this thread is the first one to get the lock and
                // must update the cache.
                value = cachedValue;
                if (value == null) {
                    value = cachedValue = child.execute(frame);
                    assert value != null;
                }
            }
        }

        return value;
    }

}
