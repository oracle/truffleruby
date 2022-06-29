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
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.TStringWithEncoding;

public final class ParsingParameters {

    /** For exceptions during parsing */
    private final Node currentNode;
    private final TStringWithEncoding tstringWithEnc;
    private final Source source;

    public ParsingParameters(Node currentNode, TStringWithEncoding tstringWithEnc, Source source) {
        this.currentNode = currentNode;
        this.tstringWithEnc = tstringWithEnc;
        this.source = source;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public String getPath() {
        return RubyLanguage.getPath(source);
    }

    public TStringWithEncoding getTStringWithEnc() {
        return tstringWithEnc;
    }

    public Source getSource() {
        return source;
    }
}
