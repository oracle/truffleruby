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
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.library.RubyLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** A list of expressions to build up into a string. */
public final class InterpolatedStringNode extends RubyContextSourceNode {

    @Children private final ToSNode[] children;

    @Child private StringNodes.StringAppendPrimitiveNode appendNode;
    @Child private RubyLibrary rubyLibrary;
    @Child private RubyLibrary rubyLibraryTaint;

    private final Rope emptyRope;

    private final ConditionProfile taintProfile = ConditionProfile.createCountingProfile();

    public InterpolatedStringNode(ToSNode[] children, Encoding encoding) {
        assert children.length > 0;
        this.children = children;
        this.emptyRope = RopeOperations.emptyRope(encoding);
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {

        // Start with an empty string to ensure the result has class String and the proper encoding.
        RubyString builder = StringOperations.createString(getContext(), emptyRope);
        boolean tainted = false;

        // TODO (nirvdrum 11-Jan-16) Rewrite to avoid massively unbalanced trees.
        for (ToSNode child : children) {
            final Object toInterpolate = child.execute(frame);
            builder = executeStringAppend(builder, (RubyString) toInterpolate);
            tainted |= executeIsTainted(toInterpolate);
        }

        if (taintProfile.profile(tainted)) {
            executeTaint(builder);
        }

        return builder;
    }

    private void executeTaint(Object obj) {
        if (rubyLibraryTaint == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            rubyLibraryTaint = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
        }
        rubyLibraryTaint.taint(obj);
    }

    private RubyString executeStringAppend(RubyString builder, RubyString string) {
        if (appendNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            appendNode = insert(StringNodesFactory.StringAppendPrimitiveNodeFactory.create(null));
        }
        return appendNode.executeStringAppend(builder, string);
    }

    private boolean executeIsTainted(Object object) {
        if (rubyLibrary == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            rubyLibrary = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
        }
        return rubyLibrary.isTainted(object);
    }
}
