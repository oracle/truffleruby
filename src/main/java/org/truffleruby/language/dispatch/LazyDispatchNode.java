/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.RubyBaseNode;

@GenerateCached(false)
@GenerateInline(inlineByDefault = true)
public abstract class LazyDispatchNode extends RubyBaseNode {


    public final DispatchNode get(Node node) {
        return execute(node);
    }

    protected abstract DispatchNode execute(Node node);

    @Specialization
    protected static DispatchNode doLazy(
            @Cached(inline = false) DispatchNode dispatchNode) {
        return dispatchNode;
    }
}
