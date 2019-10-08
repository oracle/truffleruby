/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.IsANode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild(value = "a", type = RubyNode.class)
@NodeChild(value = "b", type = RubyNode.class)
public abstract class BigDecimalOpNode extends BigDecimalCoreMethodNode {

    @CreateCast("b")
    protected RubyNode coerceB(RubyNode b) {
        return BigDecimalCoerceNodeGen.create(b);
    }

    @Child private IsANode isANode;

    protected boolean isRubyRational(Object object) {
        if (isANode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isANode = insert(IsANode.create());
        }

        return isANode.executeIsA(object, getContext().getCoreLibrary().getRationalClass());
    }

}
