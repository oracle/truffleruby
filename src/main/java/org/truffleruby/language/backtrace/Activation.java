/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.backtrace;

import com.oracle.truffle.api.nodes.Node;

public class Activation {

    /** might be null */
    private final Node callNode;
    private final String methodName;

    public Activation(Node callNode, String methodName) {
        this.callNode = callNode;
        this.methodName = methodName;
    }

    public Node getCallNode() {
        return callNode;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        return "Activation @ " + callNode + " " + methodName;
    }

}
