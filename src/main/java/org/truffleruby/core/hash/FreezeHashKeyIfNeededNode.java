/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsFrozenNode;

public abstract class FreezeHashKeyIfNeededNode extends RubyBaseNode {

    @Child private IsFrozenNode isFrozenNode;
    @Child private CallDispatchHeadNode dupNode;
    @Child private CallDispatchHeadNode freezeNode;

    public abstract Object executeFreezeIfNeeded(Object key, boolean compareByIdentity);

    @Specialization(guards = {"isRubyString(string)", "isFrozen(string)"})
    Object alreadyFrozen(DynamicObject string, boolean compareByIdentity) {
        return string;
    }

    @Specialization(guards = {"isRubyString(string)", "!isFrozen(string)", "!compareByIdentity"})
    Object dupAndFreeze(DynamicObject string, boolean compareByIdentity) {
        return freeze(dup(string));
    }

    @Specialization(guards = {"isRubyString(string)", "!isFrozen(string)", "compareByIdentity"})
    Object compareByIdentity(DynamicObject string, boolean compareByIdentity) {
        return string;
    }

    @Specialization(guards = "!isRubyString(value)")
    Object passThrough(Object value, boolean compareByIdentity) {
        return value;
    }

    protected boolean isFrozen(Object value) {
        if (isFrozenNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isFrozenNode = insert(IsFrozenNode.create());
        }
        return isFrozenNode.executeIsFrozen(value);
    }

    private Object dup(Object value) {
        if (dupNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dupNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return dupNode.call(value, "dup");
    }

    private Object freeze(Object value) {
        if (freezeNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            freezeNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return freezeNode.call(value, "freeze");
    }

}
