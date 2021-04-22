/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash.library;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.RubyHash;

@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public class NullHashStore {

    public static final NullHashStore NULL_HASH_STORE = new NullHashStore();

    @ExportMessage
    protected Object lookupOrDefault(Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode) {
        return defaultNode.accept((VirtualFrame) frame, hash, key);
    }
}
