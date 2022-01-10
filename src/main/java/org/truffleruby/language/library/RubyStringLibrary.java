/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.library;

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.Rope;

@GenerateLibrary
public abstract class RubyStringLibrary extends Library {

    private static final LibraryFactory<RubyStringLibrary> FACTORY = LibraryFactory.resolve(RubyStringLibrary.class);

    public static LibraryFactory<RubyStringLibrary> getFactory() {
        return FACTORY;
    }

    public static RubyStringLibrary getUncached() {
        return FACTORY.getUncached();
    }

    public boolean isRubyString(Object receiver) {
        return false;
    }

    public abstract Rope getRope(Object object);

    public abstract RubyEncoding getEncoding(Object object);

    public abstract String getJavaString(Object receiver);

}
