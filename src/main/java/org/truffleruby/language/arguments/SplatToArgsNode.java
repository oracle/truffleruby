/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node.Child;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;

public abstract class SplatToArgsNode extends RubyBaseNode {

    @Child protected ArrayStoreLibrary stores;

    public SplatToArgsNode() {
        stores = ArrayStoreLibrary.getFactory().createDispatched(ArrayGuards.storageStrategyLimit());
    }

    public abstract Object[] execute(Object receiver, Object[] rubyArgs, RubyArray splatted);

    @Specialization(limit = "2", guards = "splatted.size == size")
    protected Object[] smallSplatted(Object receiver, Object[] rubyArgs, RubyArray splatted,
            @Cached("splatted.size") int size) {
        Object store = splatted.store;
        Object[] newArgs = RubyArguments.repack(rubyArgs, receiver, 0, size, 0);
        stores.copyContents(store, 0, newArgs, RubyArguments.RUNTIME_ARGUMENT_COUNT, size);
        return newArgs;
    }

    @Specialization
    protected Object[] smallSplatted(Object receiver, Object[] rubyArgs, RubyArray splatted) {
        Object store = splatted.store;
        Object[] newArgs = RubyArguments.repack(rubyArgs, receiver, 0, splatted.size, 0);
        stores.copyContents(store, 0, newArgs, RubyArguments.RUNTIME_ARGUMENT_COUNT, splatted.size);
        return newArgs;
    }
}
