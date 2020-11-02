/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.language.methods.InternalMethod;

public abstract class InlinedMethodNode extends CoreMethodArrayArgumentsNode {

    public abstract Object inlineExecute(VirtualFrame frame, Object self, Object[] args, Object proc);

    public abstract InternalMethod getMethod();
}
