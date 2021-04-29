/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

@GenerateUncached
public abstract class FreezeHashKeyIfNeededNode extends RubyBaseNode {

    public abstract Object executeFreezeIfNeeded(Object key, boolean compareByIdentity);

    @Specialization
    protected Object immutable(ImmutableRubyString string, boolean compareByIdentity) {
        return string;
    }

    @Specialization(
            guards = "rubyLibrary.isFrozen(string)",
            limit = "baseGetRubyLibraryCacheLimit()")
    protected Object alreadyFrozen(RubyString string, boolean compareByIdentity,
            @CachedLibrary("string") RubyLibrary rubyLibrary) {
        return string;
    }

    @Specialization(
            guards = { "!rubyLibrary.isFrozen(string)", "!compareByIdentity" },
            limit = "baseGetRubyLibraryCacheLimit()")
    protected Object dupAndFreeze(RubyString string, boolean compareByIdentity,
            @CachedLibrary("string") RubyLibrary rubyLibrary,
            @CachedLibrary(limit = "baseGetRubyLibraryCacheLimit()") RubyLibrary rubyLibraryObject,
            @Cached DispatchNode dupNode) {
        final Object object = dupNode.call(string, "dup");
        rubyLibraryObject.freeze(object);
        return object;
    }

    @Specialization(
            guards = { "!rubyLibrary.isFrozen(string)", "compareByIdentity" },
            limit = "baseGetRubyLibraryCacheLimit()")
    protected Object compareByIdentity(RubyString string, boolean compareByIdentity,
            @CachedLibrary("string") RubyLibrary rubyLibrary) {
        return string;
    }

    @Specialization(guards = "isNotRubyString(value)")
    protected Object passThrough(Object value, boolean compareByIdentity) {
        return value;
    }
}
