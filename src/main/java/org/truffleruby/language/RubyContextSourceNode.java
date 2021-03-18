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
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;

/** Has both context and source methods. */
public abstract class RubyContextSourceNode extends RubyNode implements RubyNode.WithContext {

    private int sourceCharIndex = NO_SOURCE;
    private int sourceLength;
    private byte flags;

    @CompilationFinal private ContextReference<RubyContext> contextReference;
    @CompilationFinal private RubyLanguage language;

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return RubyNode.defaultIsDefined(getLanguage(), context, this);
    }

    @Override
    protected byte getFlags() {
        return flags;
    }

    @Override
    protected void setFlags(byte flags) {
        this.flags = flags;
    }

    @Override
    protected int getSourceCharIndex() {
        return sourceCharIndex;
    }

    @Override
    protected void setSourceCharIndex(int sourceCharIndex) {
        this.sourceCharIndex = sourceCharIndex;
    }

    @Override
    protected int getSourceLength() {
        return sourceLength;
    }

    @Override
    protected void setSourceLength(int sourceLength) {
        this.sourceLength = sourceLength;
    }

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

    @Override
    public String toString() {
        return super.toString() + " at " + RubyLanguage.fileLine(getSourceSection());
    }
}
