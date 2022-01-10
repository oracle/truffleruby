/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.array;

import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.read.SourceNode;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild(value = "source", type = SourceNode.class)
public abstract class ReadHashValueNode extends FormatNode {

    private final Object key;

    @Child private DispatchNode fetchNode;

    private final ConditionProfile oneHashProfile = ConditionProfile.create();

    public ReadHashValueNode(Object key) {
        this.key = key;
    }

    @Specialization
    protected Object read(VirtualFrame frame, Object[] source) {
        if (oneHashProfile.profile(source.length != 1 || !RubyGuards.isRubyHash(source[0]))) {
            throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentErrorOneHashRequired(this));
        }

        final RubyHash hash = (RubyHash) source[0];

        if (fetchNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fetchNode = insert(DispatchNode.create());
        }

        return fetchNode.call(hash, "fetch", key);
    }

}
