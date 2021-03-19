/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.yield.CallBlockNode;

public abstract class YieldingCoreMethodNode extends CoreMethodArrayArgumentsNode {

    @Child private CallBlockNode yieldNode = CallBlockNode.create();

    public Object yield(RubyProc block, Object... arguments) {
        return yieldNode.yield(block, arguments);
    }

}
