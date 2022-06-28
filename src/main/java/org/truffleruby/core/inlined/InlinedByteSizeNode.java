/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public abstract class InlinedByteSizeNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "bytesize";

    public InlinedByteSizeNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    @Specialization(
            guards = { "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().STRING_BYTESIZE", },
            assumptions = "assumptions",
            limit = "1")
    protected int byteSize(VirtualFrame frame, RubyString self,
            @Cached LookupMethodOnSelfNode lookupNode) {
        return self.byteLength();
    }

    @Specialization(
            guards = { "lookupNode.lookupProtected(frame, self, METHOD) == coreMethods().STRING_BYTESIZE", },
            assumptions = "assumptions",
            limit = "1")
    protected int byteSizeImmutable(VirtualFrame frame, ImmutableRubyString self,
            @Cached LookupMethodOnSelfNode lookupNode) {
        return self.byteLength();
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

}
