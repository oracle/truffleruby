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

import java.util.Objects;

import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.Rope;

import com.oracle.truffle.api.source.Source;

public class RubySource {

    private final Source source;
    /** The path that will be used by the parser for __FILE__, warnings and syntax errors. Currently the same as
     * {@link org.truffleruby.RubyContext#getPath(Source)}. Kept separate as we might want to change Source#getName()
     * for non-file Sources in the future (but then we'll need to still use this path in Ruby backtraces). */
    private final String sourcePath;
    private final Rope sourceRope;
    private final boolean isEval;

    public RubySource(Source source, String sourcePath) {
        this(source, sourcePath, null, false);
    }

    public RubySource(Source source, String sourcePath, Rope sourceRope) {
        this(source, sourcePath, sourceRope, false);
    }

    public RubySource(Source source, String sourcePath, Rope sourceRope, boolean isEval) {
        assert RubyContext.getPath(source).equals(sourcePath) : RubyContext.getPath(source) + " vs " + sourcePath;
        this.source = Objects.requireNonNull(source);
        //intern() to improve footprint
        this.sourcePath = Objects.requireNonNull(sourcePath).intern();
        this.sourceRope = sourceRope;
        this.isEval = isEval;
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

    public boolean isEval() {
        return isEval;
    }
}
