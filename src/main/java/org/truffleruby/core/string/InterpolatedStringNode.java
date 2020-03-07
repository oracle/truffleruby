/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import org.jcodings.Encoding;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.IsTaintedNode;
import org.truffleruby.language.objects.TaintNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** A list of expressions to build up into a string. */
public final class InterpolatedStringNode extends RubyContextSourceNode {

    @Children private final ToSNode[] children;

    @Child private StringNodes.StringAppendPrimitiveNode appendNode;
    @Child private IsTaintedNode isTaintedNode;
    @Child private TaintNode taintNode;

    private final Encoding encoding;

    private final ConditionProfile taintProfile = ConditionProfile.createCountingProfile();

    public InterpolatedStringNode(ToSNode[] children, Encoding encoding) {
        this.children = children;
        this.encoding = encoding;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        assert children.length > 0;

        // Start with an empty string to ensure the result has class String and the proper encoding.
        DynamicObject builder = StringOperations.createString(getContext(), RopeOperations.emptyRope(encoding));
        boolean tainted = false;

        // TODO (nirvdrum 11-Jan-16) Rewrite to avoid massively unbalanced trees.
        for (ToSNode child : children) {
            final Object toInterpolate = child.execute(frame);
            assert RubyGuards.isRubyString(toInterpolate);
            builder = executeStringAppend(builder, (DynamicObject) toInterpolate);
            tainted |= executeIsTainted(toInterpolate);
        }

        if (taintProfile.profile(tainted)) {
            executeTaint(builder);
        }

        return builder;
    }

    private void executeTaint(Object obj) {
        if (taintNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            taintNode = insert(TaintNode.create());
        }
        taintNode.executeTaint(obj);
    }

    private DynamicObject executeStringAppend(DynamicObject builder, DynamicObject string) {
        if (appendNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            appendNode = insert(StringNodesFactory.StringAppendPrimitiveNodeFactory.create(null));
        }
        return appendNode.executeStringAppend(builder, string);
    }

    private boolean executeIsTainted(Object object) {
        if (isTaintedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isTaintedNode = insert(IsTaintedNode.create());
        }
        return isTaintedNode.executeIsTainted(object);
    }
}
