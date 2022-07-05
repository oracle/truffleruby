/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.RubyContext;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.dispatch.DispatchNode;

@ExportLibrary(InteropLibrary.class)
public final class RubyIntRange extends ImmutableRubyObject {

    public final int begin;
    public final int end;
    public final boolean excludedEnd;

    public RubyIntRange(boolean excludedEnd, int begin, int end) {
        this.excludedEnd = excludedEnd;
        this.begin = begin;
        this.end = end;
    }

    public RubyIntRange(RubyIntRange other) {
        this.excludedEnd = other.excludedEnd;
        this.begin = other.begin;
        this.end = other.end;
    }

    @ExportMessage
    public boolean hasIterator() {
        return true;
    }

    @ExportMessage
    public Object getIterator(
            @CachedLibrary("this") InteropLibrary node,
            @Exclusive @Cached DispatchNode dispatchNode) {
        final RubyContext context = RubyContext.get(node);
        return dispatchNode.call(context.getCoreLibrary().truffleInteropOperationsModule, "get_iterator", this);
    }

}
