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

import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyLibrary;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

public abstract class FreezeNode extends RubyContextNode {

    public static FreezeNode create() {
        return FreezeNodeGen.create();
    }

    public abstract Object executeFreeze(Object object);

    @Specialization(guards = "isRubyBignum(object)")
    protected Object freezeBignum(Object object) {
        return object;
    }


    @Specialization(guards = "!isRubyBignum(object)", limit = "3")
    protected Object freeze(Object object,
            @CachedLibrary("object") RubyLibrary rubyLibrary) {
        return rubyLibrary.freeze(object);
    }
}
