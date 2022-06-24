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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.RubyBaseNode;

@GenerateLibrary
public abstract class RubyStringLibrary extends Library {

    private static final LibraryFactory<RubyStringLibrary> FACTORY = LibraryFactory.resolve(RubyStringLibrary.class);

    public static RubyStringLibrary createDispatched() {
        return FACTORY.createDispatched(RubyBaseNode.LIBSTRING_CACHE);
    }

    public static RubyStringLibrary getUncached() {
        return FACTORY.getUncached();
    }

    public boolean isRubyString(Object receiver) {
        return false;
    }

    public abstract Rope getRope(Object object);

    public abstract AbstractTruffleString getTString(Object object);

    /** Use to initialize {@link Cached} values */
    public abstract TruffleString asTruffleStringUncached(Object object);

    public abstract RubyEncoding getEncoding(Object object);

    public abstract TruffleString.Encoding getTEncoding(Object object);

    public abstract int byteLength(Object object);

    /** This is an uncached conversion, for optimized cached conversion to java.lang.String use
     * {@link InteropLibrary#asString(Object)} instead. */
    public abstract String getJavaString(Object receiver);

}
