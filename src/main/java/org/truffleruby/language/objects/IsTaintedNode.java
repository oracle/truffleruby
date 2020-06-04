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

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyLibrary;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;

@GenerateUncached
public abstract class IsTaintedNode extends RubyBaseNode {

    public static IsTaintedNode create() {
        return IsTaintedNodeGen.create();
    }

    public abstract boolean executeIsTainted(Object object);

    @Specialization(limit = "getRubyLibraryCacheLimit()", guards = "!isForeignObject(object)")
    protected boolean freeze(Object object,
            @CachedLibrary("object") RubyLibrary rubyLibrary) {
        return rubyLibrary.isTainted(object);
    }

    @Specialization(guards = "isForeignObject(object)")
    protected boolean isTainted(Object object) {
        return false;
    }

    protected int getRubyLibraryCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().RUBY_LIBRARY_CACHE;
    }

}
