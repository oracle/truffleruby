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
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.Layouts;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
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
    @Child private CallDispatchHeadNode dupNode;
    @Child private IsTaintedNode isTaintedNode;
    @Child private TaintNode taintNode;

    private final ConditionProfile taintProfile = ConditionProfile.createCountingProfile();

    public InterpolatedStringNode(ToSNode[] children) {
        this.children = children;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        assert children.length > 0;
        DynamicObject builder = null;
        boolean tainted = false;

        // TODO (nirvdrum 11-Jan-16) Rewrite to avoid massively unbalanced trees.
        for (int n = 0; n < children.length; n++) {
            final Object toInterpolate = children[n].execute(frame);
            assert RubyGuards.isRubyString(toInterpolate);
            if (n == 0) {
                // Start with an empty string, in the case the initial string is of a subclass of String.
                Encoding encoding = Layouts.STRING.getRope((DynamicObject) toInterpolate).getEncoding();
                builder = StringOperations.createString(getContext(), RopeOperations.emptyRope(encoding));
            }
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

    private Object callDup(Object string) {
        if (dupNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dupNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return dupNode.call(string, "dup");
    }

    private boolean executeIsTainted(Object object) {
        if (isTaintedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isTaintedNode = insert(IsTaintedNode.create());
        }
        return isTaintedNode.executeIsTainted(object);
    }
}
