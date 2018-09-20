/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.backtrace;

import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.methods.InternalMethod;

public class Activation {

    /** might be null */
    private final Node callNode;
    /** non-null iff it's a Ruby frame */
    private final InternalMethod method;

    public Activation(Node callNode, InternalMethod method) {
        this.callNode = callNode;
        this.method = method;
    }

    public Node getCallNode() {
        return callNode;
    }

    public InternalMethod getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "Activation @ " + callNode + " " + method;
    }

}
