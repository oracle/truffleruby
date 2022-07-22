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
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.language.RubyBaseNode;

/** It is important that all messages of this library can be trivially implemented without needing any @Cached state or
 * node. That way, the generated library classes are actually global immutable singletons.
 * <p>
 * Implemented by {@link org.truffleruby.core.string.RubyString} and
 * {@link org.truffleruby.core.string.ImmutableRubyString} */
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

    public abstract AbstractTruffleString getTString(Object object);

    public abstract RubyEncoding getEncoding(Object object);

    public TruffleString.Encoding getTEncoding(Object object) {
        return getEncoding(object).tencoding;
    }

    public int byteLength(Object object) {
        return getTString(object).byteLength(getTEncoding(object));
    }

}
