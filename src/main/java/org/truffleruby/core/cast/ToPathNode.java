/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ToPathNode extends RubyBaseNode {

    public abstract Object execute(Object value);

    @Specialization
    protected RubyString coerceRubyString(RubyString path) {
        return path;
    }

    @Specialization
    protected ImmutableRubyString coerceImmutableRubyString(ImmutableRubyString path) {
        return path;
    }

    @Specialization(guards = "isNotRubyString(object)")
    protected RubyString coerceObject(Object object,
            @Cached DispatchNode toPathNode) {
        return (RubyString) toPathNode.call(coreLibrary().truffleTypeModule, "coerce_to_path", object);
    }
}
