/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadRejectedKeywordArgumentsNode extends RubyContextNode implements EachEntryCallback {

    @Child private HashStoreLibrary hashes = HashStoreLibrary.getDispatched();

    private final ConditionProfile isSymbolProfile = ConditionProfile.create();

    public RubyHash extractRejectedKwargs(VirtualFrame frame, RubyHash kwargsHash) {
        final RubyHash rejectedKwargs = HashOperations.newEmptyHash(getContext(), getLanguage());
        hashes.eachEntry(kwargsHash.store, frame, kwargsHash, this, rejectedKwargs);
        return rejectedKwargs;
    }

    @Override
    public void accept(VirtualFrame frame, int index, Object key, Object value, Object rejectedKwargs) {
        if (!isSymbolProfile.profile(key instanceof RubySymbol)) {
            final RubyHash hash = (RubyHash) rejectedKwargs;
            hashes.set(hash.store, hash, key, value, false);
        }
    }
}
