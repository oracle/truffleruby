/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedIndexGetNode extends BinaryInlinedOperationNode {

    protected static final String METHOD = "[]";

    public InlinedIndexGetNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    @Specialization(
            guards = "lookupNode.lookupProtected(frame, array, METHOD) == coreMethods().ARRAY_INDEX_GET",
            assumptions = "assumptions",
            limit = "1")
    protected Object arrayRead(VirtualFrame frame, RubyArray array, int index,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached ArrayIndexNodes.ReadNormalizedNode readNormalizedNode,
            @Cached ConditionProfile denormalized) {
        if (denormalized.profile(index < 0)) {
            index += array.size;
        }
        return readNormalizedNode.executeRead(array, index);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b) {
        return rewriteAndCall(frame, a, b);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = InlinedIndexGetNodeGen.create(
                getLanguage(),
                this.parameters,
                getLeftNode().cloneUninitialized(),
                getRightNode().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
