/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments.keywords;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.symbol.RubySymbol;

import java.util.Arrays;
import java.util.Objects;

public class NonEmptyKeywordDescriptor extends KeywordDescriptor {

    @CompilationFinal(dimensions = 1) private final String[] keywords;
    private final boolean alsoSplat;
    private final int hashIndex;

    @CompilationFinal(dimensions = 1) private final RubySymbol[] keywordSymbols;

    public NonEmptyKeywordDescriptor(RubyLanguage language, String[] keywords, boolean alsoSplat, int hashIndex) {
        this.keywords = keywords;
        this.alsoSplat = alsoSplat;
        this.hashIndex = hashIndex;
        keywordSymbols = Arrays.stream(keywords).map(language::getSymbol).toArray(n -> new RubySymbol[n]);
    }

    public String[] getKeywords() {
        return keywords;
    }

    public boolean isAlsoSplat() {
        return alsoSplat;
    }

    public int getHashIndex() {
        return hashIndex;
    }

    @Override
    public int getLength() {
        return keywords.length;
    }

    public String getKeyword(int n) {
        return keywords[n];
    }

    public RubySymbol getKeywordSymbol(int n) {
        return keywordSymbols[n];
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        } else if (that instanceof NonEmptyKeywordDescriptor) {
            final NonEmptyKeywordDescriptor nonEmpty = (NonEmptyKeywordDescriptor) that;
            return Arrays.equals(keywords, nonEmpty.getKeywords()) &&
                    alsoSplat == nonEmpty.alsoSplat && hashIndex == nonEmpty.hashIndex;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(keywords), hashIndex);
    }

}
