/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.library.LibraryFactory;
import org.truffleruby.language.library.BooleanRubyLibrary;
import org.truffleruby.language.library.DoubleRubyLibrary;
import org.truffleruby.language.library.IntegerRubyLibrary;
import org.truffleruby.language.library.LongRubyLibrary;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;

@GenerateLibrary
@GenerateLibrary.DefaultExport(BooleanRubyLibrary.class)
@GenerateLibrary.DefaultExport(IntegerRubyLibrary.class)
@GenerateLibrary.DefaultExport(LongRubyLibrary.class)
@GenerateLibrary.DefaultExport(DoubleRubyLibrary.class)
public abstract class RubyLibrary extends Library {

    public static LibraryFactory<RubyLibrary> getFactory() {
        return FACTORY;
    }

    public static RubyLibrary getUncached() {
        return FACTORY.getUncached();
    }

    private static final LibraryFactory<RubyLibrary> FACTORY = LibraryFactory.resolve(RubyLibrary.class);

    public abstract Object freeze(Object object);

    public abstract boolean isFrozen(Object object);

    public abstract boolean isTainted(Object object);

    public abstract Object taint(Object object);

    public abstract Object untaint(Object object);

}

