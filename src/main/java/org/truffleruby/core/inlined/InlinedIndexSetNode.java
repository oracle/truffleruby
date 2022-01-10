/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.dsl.Bind;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayWriteNormalizedNode;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class InlinedIndexSetNode extends TernaryInlinedOperationNode implements AssignableNode {

    protected static final String METHOD = "[]=";

    public InlinedIndexSetNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    protected abstract Object execute(VirtualFrame frame, Object receiver, Object index, Object value);

    @Specialization(
            guards = {
                    "lookupNode.lookupProtected(frame, array, METHOD) == coreMethods().ARRAY_INDEX_SET",
                    "normalizedIndex >= 0" },
            assumptions = "assumptions",
            limit = "1")
    protected Object arrayWrite(VirtualFrame frame, RubyArray array, int index, Object value,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached ConditionProfile denormalized,
            @Bind("normalize(array, index, denormalized)") int normalizedIndex,
            @Cached ArrayWriteNormalizedNode writeNode) {
        return writeNode.executeWrite(array, normalizedIndex, value);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object a, Object b, Object c) {
        return rewriteAndCall(frame, a, b, c);
    }

    protected int normalize(RubyArray array, int index, ConditionProfile denormalized) {
        if (denormalized.profile(index < 0)) {
            index += array.size;
        }
        return index;
    }

    @Override
    public void assign(VirtualFrame frame, Object value) {
        final Object receiver = getReceiver().execute(frame);
        final Object index = getOperand1().execute(frame);
        execute(frame, receiver, index, value);
    }

    @Override
    public AssignableNode toAssignableNode() {
        assert getOperand2() instanceof NilLiteralNode && ((NilLiteralNode) getOperand2()).isImplicit() : getOperand2();
        return this;
    }
}
