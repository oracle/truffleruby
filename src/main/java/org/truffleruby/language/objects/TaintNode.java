/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateUncached
public abstract class TaintNode extends RubyBaseNode {

    public static TaintNode create() {
        return TaintNodeGen.create();
    }

    public abstract Object executeTaint(Object object);

    @Specialization
    protected Object taint(boolean object) {
        return object;
    }

    @Specialization
    protected Object taint(int object) {
        return object;
    }

    @Specialization
    protected Object taint(long object) {
        return object;
    }

    @Specialization
    protected Object taint(double object) {
        return object;
    }

    @Specialization
    protected Object taintNil(Nil object) {
        return object;
    }

    @Specialization
    protected Object taintSymbol(RubySymbol object) {
        return object;
    }

    @Specialization
    protected Object taint(DynamicObject object,
            @Cached IsFrozenNode isFrozenNode,
            @Cached IsTaintedNode isTaintedNode,
            @Cached WriteObjectFieldNode writeTaintNode,
            @Cached BranchProfile errorProfile,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        if (!isTaintedNode.executeIsTainted(object) && isFrozenNode.execute(object)) {
            errorProfile.enter();
            throw new RaiseException(context, context.getCoreExceptions().frozenError(object, this));
        }

        writeTaintNode.write(object, Layouts.TAINTED_IDENTIFIER, true);
        return object;
    }
}
