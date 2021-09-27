/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.literal;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class FrozenStringLiteralNode extends RubyContextSourceNode {

    private final ImmutableRubyString frozenString;
    private final ImmutableRubyString definition;

    public FrozenStringLiteralNode(ImmutableRubyString frozenString, ImmutableRubyString definition) {
        this.frozenString = frozenString;
        this.definition = definition;
    }

    @Override
    public ImmutableRubyString execute(VirtualFrame frame) {
        return frozenString;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return definition;
    }
}
