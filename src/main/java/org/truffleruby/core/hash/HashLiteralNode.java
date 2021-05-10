/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.library.EntryArrayHashStore;
import org.truffleruby.core.hash.library.NullHashStore;
import org.truffleruby.core.hash.library.PackedHashStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public abstract class HashLiteralNode extends RubyContextSourceNode {

    @Children protected final RubyNode[] keyValues;
    protected final RubyLanguage language;

    protected HashLiteralNode(RubyLanguage language, RubyNode[] keyValues) {
        assert keyValues.length % 2 == 0;
        this.language = language;
        this.keyValues = keyValues;
    }

    public static HashLiteralNode create(RubyLanguage language, RubyNode[] keyValues) {
        if (keyValues.length == 0) {
            return new NullHashStore.EmptyHashLiteralNode(language);
        } else if (keyValues.length <= language.options.HASH_PACKED_ARRAY_MAX * 2) {
            return new PackedHashStoreLibrary.SmallHashLiteralNode(language, keyValues);
        } else {
            return new EntryArrayHashStore.GenericHashLiteralNode(language, keyValues);
        }
    }

    @ExplodeLoop
    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        for (RubyNode child : keyValues) {
            child.doExecuteVoid(frame);
        }
    }
}
