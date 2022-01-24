/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.nodes.ControlFlowException;

import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.language.methods.InternalMethod;

public abstract class InlinedMethodNode extends CoreMethodArrayArgumentsNode {

    public abstract Object inlineExecute(Frame callerFrame, Object[] rubyArgs);

    public abstract InternalMethod getMethod();

    public static class RewriteException extends ControlFlowException {

        private static final long serialVersionUID = -4128190563044417424L;
    }
}
