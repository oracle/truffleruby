/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.RubyBaseNode;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class CanHaveSingletonClassNode extends RubyBaseNode {

    public static CanHaveSingletonClassNode getUncached() {
        return CanHaveSingletonClassNodeGen.getUncached();
    }

    public abstract boolean execute(Object value);

    @Specialization
    protected boolean canHaveSingletonClass(int value) {
        return false;
    }

    @Specialization
    protected boolean canHaveSingletonClass(long value) {
        return false;
    }

    @Specialization
    protected boolean canHaveSingletonClass(double value) {
        return false;
    }

    @Specialization(guards = "!isNil(value)")
    protected boolean canHaveSingletonClass(ImmutableRubyObject value) {
        return false;
    }

    @Fallback
    protected boolean fallback(Object value) {
        return true;
    }

}

