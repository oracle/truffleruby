/*
 * Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.core.rope.Rope;

public class ParsingParameters {

    private final Node currentNode;
    private final String path;
    private final Rope rope;
    private final Source source;

    public ParsingParameters(Node currentNode, String path, Rope rope, Source source) {
        this.currentNode = currentNode;
        this.path = path;
        this.rope = rope;
        this.source = source;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public String getPath() {
        return path;
    }

    public Rope getRope() {
        return rope;
    }

    public Source getSource() {
        return source;
    }
}
