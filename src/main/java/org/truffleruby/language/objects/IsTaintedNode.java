/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class IsTaintedNode extends RubyBaseNode {

    public static IsTaintedNode create() {
        return IsTaintedNodeGen.create();
    }

    public abstract boolean executeIsTainted(Object object);

    @Specialization
    public boolean isTainted(boolean object) {
        return false;
    }

    @Specialization
    public boolean isTainted(int object) {
        return false;
    }

    @Specialization
    public boolean isTainted(long object) {
        return false;
    }

    @Specialization
    public boolean isTainted(double object) {
        return false;
    }

    @Specialization(guards = "isRubySymbol(object) || isNil(object)")
    protected boolean isTaintedNilOrSymbol(DynamicObject object) {
        return false;
    }

    @Specialization(guards = { "!isRubySymbol(object)", "!isNil(object)" })
    protected boolean isTainted(
            DynamicObject object,
            @Cached ReadObjectFieldNode readTaintedNode) {
        return (boolean) readTaintedNode.execute(object, Layouts.TAINTED_IDENTIFIER, false);
    }

    @Fallback
    public boolean isTainted(Object object) {
        return false;
    }
}
