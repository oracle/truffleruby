/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.method.RubyUnboundMethod;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateInline
@GenerateCached(false)
public abstract class ToCallTargetNode extends RubyBaseNode {

    public abstract RootCallTarget execute(Node node, Object executable);

    @Specialization
    static RootCallTarget boundMethod(RubyMethod method) {
        return method.method.getCallTarget();
    }

    @Specialization
    static RootCallTarget unboundMethod(RubyUnboundMethod method) {
        return method.method.getCallTarget();
    }

    @Specialization
    static RootCallTarget proc(RubyProc proc) {
        return proc.callTarget;
    }

}
