/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.threadlocal;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

public class GetFromThreadAndFrameLocalStorageNode extends RubyNode {

    @Child private RubyNode value;
    private final ConditionProfile isStorageProfile = ConditionProfile.createBinaryProfile();

    public GetFromThreadAndFrameLocalStorageNode(RubyNode value) {
        this.value = value;
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return value.isDefined(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object storage = value.execute(frame);

        if (isStorageProfile.profile(RubyGuards.isThreadLocal(storage))) {
            return getStoredValue((ThreadAndFrameLocalStorage) storage);
        }

        return storage;
    }

    private Object getStoredValue(ThreadAndFrameLocalStorage threadLocalObject) {
        return threadLocalObject.get();
    }

}
