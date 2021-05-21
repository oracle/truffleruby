/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;

/** Has context but nothing else. */
public abstract class RubyContextNode extends RubyBaseNode implements RubyNode.WithContext {

    @CompilationFinal private ContextReference<RubyContext> contextReference;
    @CompilationFinal private RubyLanguage language;

    @Override
    public ContextReference<RubyContext> getContextReference() {
        if (contextReference == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            contextReference = lookupContextReference(RubyLanguage.class);
        }

        return contextReference;
    }

    @Override
    public RubyContext getContext() {
        return getContextReference().get();
    }

    @Override
    public int getRubyLibraryCacheLimit() {
        return getLanguage().options.RUBY_LIBRARY_CACHE;
    }

    public RubyLanguage getLanguage() {
        if (language == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            language = getRootNode().getLanguage(RubyLanguage.class);
        }

        return language;
    }

    public RubySymbol getSymbol(String name) {
        return getLanguage().getSymbol(name);
    }

    public RubySymbol getSymbol(Rope name) {
        return getLanguage().getSymbol(name);
    }

    public CoreStrings coreStrings() {
        return getLanguage().coreStrings;
    }

    public CoreSymbols coreSymbols() {
        return getLanguage().coreSymbols;
    }

    public RubyArray createArray(Object store, int size) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store, size);
    }

    public RubyArray createArray(int[] store) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store);
    }

    public RubyArray createArray(long[] store) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store);
    }

    public RubyArray createArray(Object[] store) {
        return ArrayHelpers.createArray(getContext(), getLanguage(), store);
    }

    public RubyArray createEmptyArray() {
        return ArrayHelpers.createEmptyArray(getContext(), getLanguage());
    }
}
