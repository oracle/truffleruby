/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.parser.TranslatorEnvironment;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class InlinedBlockGivenNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "block_given?";
    @Child protected RubyNode readNode;

    public InlinedBlockGivenNode(RubyContext context, RubyCallNodeParameters callNodeParameters,  TranslatorEnvironment environment) {
        super(callNodeParameters);
        this.readNode = environment.findLocalVarOrNilNode(TranslatorEnvironment.IMPLICIT_BLOCK_NAME, null);
    }

    @Specialization(guards = {
            "lookupNode.lookup(frame, self, METHOD) == coreMethods().BLOCK_GIVEN",
    }, assumptions = "assumptions", limit = "1")
    boolean blockGiven(VirtualFrame frame, Object self,
            @Cached("createIgnoreVisibility()") LookupMethodNode lookupNode) {
        return readNode.execute(frame) != nil();
    }

    @Specialization
    Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

}
