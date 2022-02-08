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

import com.oracle.truffle.api.library.LibraryFactory;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;
import com.oracle.truffle.api.library.Library;

@GenerateLibrary
@DefaultExport(BooleanRubyLibrary.class)
@DefaultExport(IntegerRubyLibrary.class)
@DefaultExport(LongRubyLibrary.class)
@DefaultExport(DoubleRubyLibrary.class)
public abstract class RubyLibrary extends Library {

    public static LibraryFactory<RubyLibrary> getFactory() {
        return FACTORY;
    }

    public static RubyLibrary getUncached() {
        return FACTORY.getUncached();
    }

    private static final LibraryFactory<RubyLibrary> FACTORY = LibraryFactory.resolve(RubyLibrary.class);

    public abstract void freeze(Object object);

    /** The result is not always a PE constant, specifically for RubyString and RubyRange. For RubyDynamicObject it's
     * only constant if not frozen. */
    public abstract boolean isFrozen(Object object);

}

