/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.math.BigInteger;

import org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.CoreStrings;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

@TypeSystemReference(RubyTypes.class)
@ImportStatic(RubyGuards.class)
public abstract class RubyBaseNode extends Node {

    private static final int FLAG_NEWLINE = 0;
    private static final int FLAG_COVERAGE_LINE = 1;
    private static final int FLAG_CALL = 2;
    private static final int FLAG_ROOT = 3;

    private static final int NO_SOURCE = -1;

    @CompilationFinal private RubyContext context;

    private int sourceCharIndex = NO_SOURCE;
    private int sourceLength;

    protected int flags;

    // Guards which use the context and so can't be static

    protected boolean isNil(Object value) {
        return value == nil();
    }

    // Helpers methods for terseness

    protected DynamicObject nil() {
        return coreLibrary().getNil();
    }

    protected DynamicObject getSymbol(String name) {
        return getContext().getSymbolTable().getSymbol(name);
    }

    protected DynamicObject getSymbol(Rope name) {
        return getContext().getSymbolTable().getSymbol(name);
    }

    protected Encoding getLocaleEncoding() {
        return getContext().getEncodingManager().getLocaleEncoding();
    }

    protected DynamicObject createArray(Object store, int size) {
        return ArrayHelpers.createArray(getContext(), store, size);
    }

    protected DynamicObject createBignum(BigInteger value) {
        return BignumOperations.createBignum(getContext(), value);
    }

    protected CoreStrings coreStrings() {
        return getContext().getCoreStrings();
    }

    protected CoreLibrary coreLibrary() {
        return getContext().getCoreLibrary();
    }

    protected CoreExceptions coreExceptions() {
        return getContext().getCoreExceptions();
    }

    // Accessors

    public RubyContext getContext() {
        if (context == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            context = RubyLanguage.getCurrentContext();
        }

        return context;
    }

    // Source section

    public void unsafeSetSourceSection(SourceIndexLength sourceSection) {
        assert sourceCharIndex == NO_SOURCE;
        
        if (sourceSection != null) {
            sourceCharIndex = sourceSection.getCharIndex();
            sourceLength = sourceSection.getLength();
        }
    }

    public void unsafeSetSourceSection(SourceSection sourceSection) {
        assert sourceCharIndex == NO_SOURCE;

        if (sourceSection.isAvailable()) {
            sourceCharIndex = sourceSection.getCharIndex();
            sourceLength = sourceSection.getCharLength();
        } else {
            sourceCharIndex = 0;
            sourceLength = SourceIndexLength.UNAVAILABLE;
        }
    }

    protected boolean hasSource() {
        return sourceCharIndex != NO_SOURCE;
    }

    protected Source getSource() {
        final RootNode rootNode = getRootNode();

        if (rootNode == null) {
            return null;
        }

        final SourceSection sourceSection = rootNode.getSourceSection();

        if (sourceSection == null) {
            return null;
        }

        return sourceSection.getSource();
    }

    public SourceIndexLength getSourceIndexLength() {
        if (sourceCharIndex == NO_SOURCE) {
            return null;
        } else {
            return new SourceIndexLength(sourceCharIndex, sourceLength);
        }
    }

    public SourceIndexLength getEncapsulatingSourceIndexLength() {
        Node node = this;

        while (node != null) {
            if (node instanceof RubyBaseNode && ((RubyBaseNode) node).sourceCharIndex != NO_SOURCE) {
                return ((RubyBaseNode) node).getSourceIndexLength();
            }

            if (node instanceof RootNode) {
                return new SourceIndexLength(node.getSourceSection());
            }

            node = node.getParent();
        }

        return null;
    }

    @TruffleBoundary
    @Override
    public SourceSection getSourceSection() {
        if (sourceCharIndex == NO_SOURCE) {
            return null;
        } else {
            final Source source = getSource();

            if (source == null) {
                return null;
            }

            return getSourceIndexLength().toSourceSection(source);
        }
    }

    // Tags

    public void unsafeSetIsNewLine() {
        flags |= 1 << FLAG_NEWLINE;
    }

    public void unsafeSetIsCoverageLine() {
        flags |= 1 << FLAG_COVERAGE_LINE;
    }

    public void unsafeSetIsCall() {
        flags |= 1 << FLAG_CALL;
    }

    public void unsafeSetIsRoot() {
        flags |= 1 << FLAG_ROOT;
    }

    protected boolean isNewLine() {
        return ((flags >> FLAG_NEWLINE) & 1) == 1;
    }

    protected boolean isCoverageLine() {
        return ((flags >> FLAG_COVERAGE_LINE) & 1) == 1;
    }

    protected boolean isCall() {
        return ((flags >> FLAG_CALL) & 1) == 1;
    }

    protected boolean isRoot() {
        return ((flags >> FLAG_ROOT) & 1) == 1;
    }

    protected void transferFlagsTo(RubyBaseNode to) {
        to.flags = flags;
    }
}
