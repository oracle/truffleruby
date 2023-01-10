/*
 * Copyright (c) 2018, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.Objects;

import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.source.Source;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.TStringWithEncoding;

public class RubySource {

    private final Source source;
    /** The path that will be used by the parser for __FILE__, warnings and syntax errors. Currently the same as
     * {@link RubyLanguage#getPath(Source)}. Kept separate as we might want to change Source#getName() for non-file
     * Sources in the future (but then we'll need to still use this path in Ruby backtraces). */
    private final String sourcePath;
    private final TruffleString code;
    private final RubyEncoding encoding;
    private final boolean isEval;
    private final int lineOffset;

    public RubySource(Source source, String sourcePath) {
        this(source, sourcePath, null, false);
    }

    public RubySource(Source source, String sourcePath, TStringWithEncoding code) {
        this(source, sourcePath, code, false);
    }

    public RubySource(Source source, String sourcePath, TStringWithEncoding code, boolean isEval) {
        this(source, sourcePath, code, isEval, 0);
    }

    public RubySource(Source source, String sourcePath, TStringWithEncoding code, boolean isEval, int lineOffset) {
        assert RubyLanguage.getPath(source).equals(sourcePath) : RubyLanguage.getPath(source) + " vs " + sourcePath;
        this.source = Objects.requireNonNull(source);
        //intern() to improve footprint
        this.sourcePath = Objects.requireNonNull(sourcePath).intern();
        this.code = code != null ? code.tstring : null;
        this.encoding = code != null ? code.encoding : null;
        this.isEval = isEval;
        this.lineOffset = lineOffset;
    }

    public Source getSource() {
        return source;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public boolean hasTruffleString() {
        return code != null;
    }

    public TruffleString getTruffleString() {
        return code;
    }

    public TStringWithEncoding getTStringWithEncoding() {
        assert hasTruffleString();
        return new TStringWithEncoding(code, encoding);
    }

    public RubyEncoding getEncoding() {
        assert hasTruffleString();
        return encoding;
    }

    public boolean isEval() {
        return isEval;
    }

    public int getLineOffset() {
        return lineOffset;
    }

    public static int getStartLineAdjusted(RubyContext context, SourceSection sourceSection) {
        final Integer lineOffset = context.getSourceLineOffsets().get(sourceSection.getSource());
        if (lineOffset != null) {
            return sourceSection.getStartLine() + lineOffset;
        } else {
            return sourceSection.getStartLine();
        }
    }

}
