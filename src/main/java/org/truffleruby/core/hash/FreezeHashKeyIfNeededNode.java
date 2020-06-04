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

import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyLibrary;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class FreezeHashKeyIfNeededNode extends RubyContextNode {

    @Child private CallDispatchHeadNode dupNode;

    public abstract Object executeFreezeIfNeeded(Object key, boolean compareByIdentity);

    @Specialization(guards = { "isRubyString(string)", "rubyLibrary.isFrozen(string)" }, limit = "3")
    protected Object alreadyFrozen(DynamicObject string, boolean compareByIdentity,
            @CachedLibrary("string") RubyLibrary rubyLibrary) {
        return string;
    }

    @Specialization(
            guards = { "isRubyString(string)", "!rubyLibrary.isFrozen(string)", "!compareByIdentity" },
            limit = "3")
    protected Object dupAndFreeze(DynamicObject string, boolean compareByIdentity,
            @CachedLibrary("string") RubyLibrary rubyLibrary,
            @CachedLibrary(limit = "3") RubyLibrary rubyLibraryObject) {
        final Object object = dup(string);
        rubyLibraryObject.freeze(object);
        return object;
    }

    @Specialization(
            guards = { "isRubyString(string)", "!rubyLibrary.isFrozen(string)", "compareByIdentity" },
            limit = "3")
    protected Object compareByIdentity(DynamicObject string, boolean compareByIdentity,
            @CachedLibrary("string") RubyLibrary rubyLibrary) {
        return string;
    }

    @Specialization(guards = "!isRubyString(value)")
    protected Object passThrough(Object value, boolean compareByIdentity) {
        return value;
    }


    private Object dup(Object value) {
        if (dupNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dupNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return dupNode.call(value, "dup");
    }


}
