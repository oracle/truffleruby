/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.ImmutableRubyString;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.language.library.RubyStringLibrary;

public abstract class FreezeHashKeyIfNeededNode extends RubyContextNode {

    @Child private DispatchNode dupNode;

    public abstract Object executeFreezeIfNeeded(Object key, boolean compareByIdentity);

    @Specialization
    protected Object alreadyFrozen(ImmutableRubyString string, boolean compareByIdentity) {
        return string;
    }

    @Specialization(
            guards = { "strings.isRubyString(string)", "rubyLibrary.isFrozen(string)" },
            limit = "getRubyLibraryCacheLimit()")
    protected Object alreadyFrozen(Object string, boolean compareByIdentity,
            @CachedLibrary(limit = "2") RubyStringLibrary strings,
            @CachedLibrary("string") RubyLibrary rubyLibrary) {
        return string;
    }

    @Specialization(
            guards = { "strings.isRubyString(string)", "!rubyLibrary.isFrozen(string)", "!compareByIdentity" },
            limit = "getRubyLibraryCacheLimit()")
    protected Object dupAndFreeze(Object string, boolean compareByIdentity,
            @CachedLibrary(limit = "2") RubyStringLibrary strings,
            @CachedLibrary("string") RubyLibrary rubyLibrary,
            @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibraryObject) {
        final Object object = dup(string);
        rubyLibraryObject.freeze(object);
        return object;
    }

    @Specialization(
            guards = { "strings.isRubyString(string)", "!rubyLibrary.isFrozen(string)", "compareByIdentity" },
            limit = "getRubyLibraryCacheLimit()")
    protected Object compareByIdentity(RubyString string, boolean compareByIdentity,
            @CachedLibrary(limit = "2") RubyStringLibrary strings,
            @CachedLibrary("string") RubyLibrary rubyLibrary) {
        return string;
    }

    @Specialization(guards = "isNotRubyString(value)")
    protected Object passThrough(Object value, boolean compareByIdentity) {
        return value;
    }


    private Object dup(Object value) {
        if (dupNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dupNode = insert(DispatchNode.create());
        }
        return dupNode.call(value, "dup");
    }


}
