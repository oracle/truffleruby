/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import org.truffleruby.RubyContext;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.ReturnID;

import java.util.Optional;

/** Translator environment, unique per parse/translation. This must be immutable to be correct for lazy translation, as
 * then multiple threads might lazy translate methods of the same file in parallel. */
public class ParseEnvironment {

    public final Source source;
    private final boolean inCore;
    private final boolean coverageEnabled;
    public final Optional<RubyContext> contextIfSingleContext;

    // Set once after parsing and before translating
    public Boolean allowTruffleRubyPrimitives = null;

    public ParseEnvironment(RubyLanguage language, RubySource rubySource) {
        this.source = rubySource.getSource();
        this.inCore = RubyLanguage.getPath(source).startsWith(language.corePath);
        coverageEnabled = language.contextIfSingleContext.isPresent() &&
                RubyLanguage.MIME_TYPE_COVERAGE.equals(rubySource.getSource().getMimeType());
        contextIfSingleContext = language.contextIfSingleContext;
    }

    public boolean inCore() {
        return inCore;
    }

    public boolean canUsePrimitives() {
        assert allowTruffleRubyPrimitives != null;
        return inCore() || allowTruffleRubyPrimitives;
    }

    /** Returns false if the AST is shared */
    public boolean isCoverageEnabled() {
        return coverageEnabled;
    }

    @SuppressFBWarnings("ISC_INSTANTIATE_STATIC_CLASS")
    public ReturnID allocateReturnID() {
        return new ReturnID();
    }

    @SuppressFBWarnings("ISC_INSTANTIATE_STATIC_CLASS")
    public BreakID allocateBreakID() {
        return new BreakID();
    }

}
