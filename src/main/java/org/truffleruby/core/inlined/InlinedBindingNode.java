/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public abstract class InlinedBindingNode extends UnaryInlinedOperationNode {

    protected static final String METHOD = "binding";

    public InlinedBindingNode(RubyLanguage language, RubyCallNodeParameters callNodeParameters) {
        super(language, callNodeParameters);
    }

    @Specialization(
            guards = { "lookupNode.lookupIgnoringVisibility(frame, self, METHOD) == coreMethods().BINDING", },
            assumptions = "assumptions",
            limit = "1")
    protected RubyBinding binding(VirtualFrame frame, Object self,
            @Cached LookupMethodOnSelfNode lookupNode,
            @Cached("getMyEncapsulatingSourceSection()") SourceSection sourceSection) {
        return BindingNodes.createBinding(getContext(), frame.materialize(), sourceSection);
    }

    @Specialization
    protected Object fallback(VirtualFrame frame, Object self) {
        return rewriteAndCall(frame, self);
    }

    protected SourceSection getMyEncapsulatingSourceSection() {
        // Node#getEncapsulatingSourceSection is filtered out in DSL, calling indirectly
        return this.getEncapsulatingSourceSection();
    }

}
