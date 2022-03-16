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

import com.oracle.truffle.api.profiles.IntValueProfile;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;

public class SplatToArgsNode extends RubyBaseNode {

    @Child protected ArrayStoreLibrary stores;

    final IntValueProfile splatSizeProfile = IntValueProfile.create();

    public SplatToArgsNode() {
        stores = ArrayStoreLibrary.getFactory().createDispatched(ArrayGuards.storageStrategyLimit());
    }

    public Object[] execute(Object receiver, Object[] rubyArgs, RubyArray splatted) {
        int size = splatSizeProfile.profile(splatted.size);
        Object store = splatted.store;
        final Object[] newArgs = RubyArguments.allocate(size);
        RubyArguments.setSelf(newArgs, receiver);
        stores.copyContents(store, 0, newArgs, RubyArguments.RUNTIME_ARGUMENT_COUNT, size);
        return newArgs;
    }
}
