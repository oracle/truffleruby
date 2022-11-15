/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.library;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;

@GenerateLibrary
@DefaultExport(BooleanRubyLibrary.class)
@DefaultExport(IntegerRubyLibrary.class)
@DefaultExport(LongRubyLibrary.class)
@DefaultExport(DoubleRubyLibrary.class)
public abstract class RubyLibrary extends Library {

    private static final LibraryFactory<RubyLibrary> FACTORY = LibraryFactory.resolve(RubyLibrary.class);

    public static RubyLibrary createDispatched(int limit) {
        return FACTORY.createDispatched(limit);
    }

    public static RubyLibrary getUncached() {
        CompilerAsserts.neverPartOfCompilation("uncached libraries must not be used in PE code");
        return FACTORY.getUncached();
    }

    public abstract void freeze(Object object);

    /** The result is not always a PE constant, specifically for RubyString and RubyRange. For RubyDynamicObject it's
     * only constant if not frozen. */
    public abstract boolean isFrozen(Object object);

}

