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

import java.util.Objects;

public class RubySource {

    private final Source source;
    /** Same as {@link org.truffleruby.RubyContext#getAbsolutePath(Source)} except for the main file. */
    private final String sourcePath;
    private final Rope sourceRope;

    public RubySource(Source source, String sourcePath) {
        this(source, sourcePath, null);
    }

    public RubySource(Source source, String sourcePath, Rope sourceRope) {
        this.source = Objects.requireNonNull(source);
        //intern() to improve footprint
        this.sourcePath = Objects.requireNonNull(sourcePath).intern();
        this.sourceRope = sourceRope;
    }

    public Source getSource() {
        return source;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public Rope getRope() {
        return sourceRope;
    }

}
