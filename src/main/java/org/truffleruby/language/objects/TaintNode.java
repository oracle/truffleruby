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
public abstract class TaintNode extends RubyBaseNode {

    public static TaintNode create() {
        return TaintNodeGen.create();
    }

    public abstract Object executeTaint(Object object);

    @Specialization(limit = "getRubyLibraryCacheLimit()")
    protected Object taint(Object self,
            @CachedLibrary("self") RubyLibrary rubyLibrary) {
        rubyLibrary.taint(self);
        return self;
    }

    protected int getRubyLibraryCacheLimit() {
        return RubyLanguage.getCurrentContext().getOptions().RUBY_LIBRARY_CACHE;
    }

}
