/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.RubyContext;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Can be used as a parent of Ruby nodes which need @GenerateUncached. */
@NodeField(name = "sourceCharIndex", type = int.class)
@NodeField(name = "sourceLength", type = int.class)
@NodeField(name = "flags", type = byte.class)
public abstract class UncacheableSourceRubyNode extends RubyNode {

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        assert !(this instanceof WrapperNode);
        return context.getCoreStrings().EXPRESSION.createInstance();
    }

}
