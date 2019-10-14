/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import org.truffleruby.core.rope.Rope;

import com.oracle.truffle.api.source.Source;

public class RubySource {

    private final Source source;
    private final Rope sourceRope;

    public RubySource(Source source) {
        this(source, null);
    }

    public RubySource(Source source, Rope sourceRope) {
        this.source = source;
        this.sourceRope = sourceRope;
    }

    public Source getSource() {
        return source;
    }

    public Rope getRope() {
        return sourceRope;
    }

}
