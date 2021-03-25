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
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.ReturnID;

import java.util.Optional;

/** Translator environment, unique per parse/translation. */
public class ParseEnvironment {

    private LexicalScope lexicalScope = null;
    private boolean dynamicConstantLookup = false;
    public boolean allowTruffleRubyPrimitives = false;
    private final String corePath;
    private final boolean coverageEnabled;
    public final Optional<RubyContext> contextIfSingleContext;

    public ParseEnvironment(RubyLanguage language) {
        corePath = language.corePath;
        coverageEnabled = language.contextIfSingleContext.map(c -> c.getCoverageManager().isEnabled()).orElse(false);
        contextIfSingleContext = language.contextIfSingleContext;
    }

    public String getCorePath() {
        return corePath;
    }

    public void resetLexicalScope(LexicalScope lexicalScope) {
        this.lexicalScope = lexicalScope;
    }

    public LexicalScope getLexicalScope() {
        // TODO (eregon, 4 Dec. 2016): assert !dynamicConstantLookup;
        return lexicalScope;
    }

    /** Returns false if the AST is shared */
    public boolean isCoverageEnabled() {
        return coverageEnabled;
    }

    public LexicalScope pushLexicalScope() {
        return lexicalScope = new LexicalScope(getLexicalScope());
    }

    public void popLexicalScope() {
        lexicalScope = getLexicalScope().getParent();
    }

    public boolean isDynamicConstantLookup() {
        return dynamicConstantLookup;
    }

    public void setDynamicConstantLookup(boolean dynamicConstantLookup) {
        this.dynamicConstantLookup = dynamicConstantLookup;
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
