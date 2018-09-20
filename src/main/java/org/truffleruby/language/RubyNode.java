/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.stdlib.CoverageManager;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.GenerateWrapper;
import com.oracle.truffle.api.instrumentation.InstrumentableNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

@GenerateWrapper
public abstract class RubyNode extends RubyBaseNode implements InstrumentableNode {

    private static final int NO_SOURCE = -1;

    private static final int FLAG_NEWLINE = 0;
    private static final int FLAG_COVERAGE_LINE = 1;
    private static final int FLAG_CALL = 2;
    private static final int FLAG_ROOT = 3;

    public static final RubyNode[] EMPTY_ARRAY = new RubyNode[]{};
    public static final Object[] EMPTY_ARGUMENTS = new Object[]{};

    private int sourceCharIndex = NO_SOURCE;
    private int sourceLength;

    protected byte flags;

    // Fundamental execute methods

    public abstract Object execute(VirtualFrame frame);

    /**
     * This method does not start with "execute" on purpose, so the Truffle DSL does not generate
     * useless copies of this method which would increase the number of runtime compilable methods.
     */
    public void doExecuteVoid(VirtualFrame frame) {
        execute(frame);
    }

    public Object isDefined(VirtualFrame frame) {
        return coreStrings().EXPRESSION.createInstance();
    }

    // Instrumentation

    public WrapperNode createWrapper(ProbeNode probe) {
        return new RubyNodeWrapper(this, probe);
    }

    public boolean isInstrumentable() {
        return hasSource();
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
            if (node instanceof RubyNode && ((RubyNode) node).sourceCharIndex != NO_SOURCE) {
                return ((RubyNode) node).getSourceIndexLength();
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

    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == TraceManager.CallTag.class || tag == StandardTags.CallTag.class) {
            return isCall();
        }

        if (tag == TraceManager.LineTag.class || tag == StandardTags.StatementTag.class) {
            return isNewLine();
        }

        if (tag == CoverageManager.LineTag.class) {
            return isCoverageLine();
        }

        if (tag == StandardTags.RootTag.class) {
            return isRoot();
        }

        return false;
    }

    // Boundaries

    @TruffleBoundary
    @Override
    public SourceSection getEncapsulatingSourceSection() {
        return super.getEncapsulatingSourceSection();
    }

    // Helpers

    public static RubyNode[] createArray(int size) {
        return size == 0 ? RubyNode.EMPTY_ARRAY : new RubyNode[size];
    }

}
