/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsTaintedNode;
import org.truffleruby.language.objects.TaintNode;

/**
 * A list of expressions to build up into a string.
 */
public final class InterpolatedStringNode extends RubyNode {

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
        final Object[] strings = new Object[children.length];

        boolean tainted = false;

        for (int n = 0; n < children.length; n++) {
            final Object toInterpolate = children[n].execute(frame);
            strings[n] = toInterpolate;
            tainted |= executeIsTainted(toInterpolate);
        }

        final Object string = concat(frame, strings);

        if (taintProfile.profile(tainted)) {
            executeTaint(string);
        }

        return string;
    }

    private Object concat(VirtualFrame frame, Object[] strings) {
        // TODO(CS): there is a lot of copying going on here - and I think this is sometimes inner loop stuff

        DynamicObject builder = null;

        // TODO (nirvdrum 11-Jan-16) Rewrite to avoid massively unbalanced trees.
        for (Object string : strings) {
            assert RubyGuards.isRubyString(string);

            if (builder == null) {
                builder = (DynamicObject) callDup(frame, string);
            } else {
                builder = executeStringAppend(builder, (DynamicObject) string);
            }
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

    private Object callDup(VirtualFrame frame, Object string) {
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
