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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.debug.RubyScope;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;
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
import com.oracle.truffle.api.source.SourceSection;

import static org.truffleruby.debug.RubyScope.RECEIVER_MEMBER;

/** RubyNode has source, execute, and is instrument-able. However, it does not have any fields which would prevent
 * using @GenerateUncached. It should never be subclassed directly, either use {@link RubyContextSourceNode} or
 * {@link RubySourceNode}. SourceRubyNode is not defined since there was no use for it for now. Nodes having context are
 * described by {@link WithContext}. There is also {@link RubyContextNode} if context is needed but source is not. */
@GenerateWrapper
@ExportLibrary(NodeLibrary.class)
public abstract class RubyNode extends RubyBaseNodeWithExecute implements InstrumentableNode {

    public static final RubyNode[] EMPTY_ARRAY = new RubyNode[0];

    private static final byte FLAG_NEWLINE = 0;
    private static final byte FLAG_COVERAGE_LINE = 1;
    private static final byte FLAG_CALL = 2;
    private static final byte FLAG_ROOT = 3;

    protected static final int NO_SOURCE = -1;

    /** This method does not start with "execute" on purpose, so the Truffle DSL does not generate useless copies of
     * this method which would increase the number of runtime compilable methods. */
    public void doExecuteVoid(VirtualFrame frame) {
        execute(frame);
    }

    // Declared abstract here so the instrumentation wrapper delegates it
    public abstract Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context);

    protected static Object defaultIsDefined(RubyLanguage language, RubyContext context, Node currentNode) {
        assert !(currentNode instanceof WrapperNode);
        return language.coreStrings.EXPRESSION.createInstance(context);
    }

    // Source

    protected abstract int getSourceCharIndex();

    protected abstract void setSourceCharIndex(int sourceCharIndex);

    protected abstract int getSourceLength();

    protected abstract void setSourceLength(int sourceLength);

    public boolean hasSource() {
        return isAdoptable() && getSourceCharIndex() != NO_SOURCE;
    }

    public void unsafeSetSourceSection(SourceIndexLength sourceIndexLength) {
        assert !hasSource();

        if (sourceIndexLength != null) {
            setSourceCharIndex(sourceIndexLength.getCharIndex());
            setSourceLength(sourceIndexLength.getLength());
        }
    }

    public void unsafeSetSourceSection(SourceSection sourceSection) {
        assert !hasSource();

        if (sourceSection.isAvailable()) {
            setSourceCharIndex(sourceSection.getCharIndex());
            setSourceLength(sourceSection.getCharLength());
        } else {
            setSourceCharIndex(0);
            setSourceLength(SourceIndexLength.UNAVAILABLE);
        }
    }

    public RubyNode copySourceSection(RubyNode from) {
        if (from.hasSource()) {
            setSourceCharIndex(from.getSourceCharIndex());
            setSourceLength(from.getSourceLength());
        }
        return this;
    }

    public SourceIndexLength getSourceIndexLength() {
        if (!hasSource()) {
            return null;
        } else {
            return new SourceIndexLength(getSourceCharIndex(), getSourceLength());
        }
    }

    @Override
    @TruffleBoundary
    public SourceSection getSourceSection() {
        if (!hasSource()) {
            return null;
        } else {
            final com.oracle.truffle.api.source.Source source = getSource();

            if (source == null) {
                return null;
            }

            return getSourceIndexLength().toSourceSection(source);
        }

    }

    private com.oracle.truffle.api.source.Source getSource() {
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

    public SourceIndexLength getEncapsulatingSourceIndexLength() {
        Node node = this;
        while (node != null) {
            if (node instanceof RubyNode && ((RubyNode) node).hasSource()) {
                return ((RubyNode) node).getSourceIndexLength();
            }

            if (node instanceof RootNode) {
                return new SourceIndexLength(node.getSourceSection());
            }

            node = node.getParent();
        }

        return null;
    }

    // Instrumentation

    @Override
    public boolean isInstrumentable() {
        return hasSource();
    }

    protected abstract byte getFlags();

    protected abstract void setFlags(byte flags);

    private void setFlag(byte flag) {
        setFlags((byte) (getFlags() | 1 << flag));
    }

    public void unsafeSetIsNewLine() {
        setFlag(FLAG_NEWLINE);
    }

    public void unsafeSetIsCoverageLine() {
        setFlag(FLAG_COVERAGE_LINE);
    }

    public void unsafeSetIsCall() {
        setFlag(FLAG_CALL);
    }

    public void unsafeSetIsRoot() {
        setFlag(FLAG_ROOT);
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        byte flags = getFlags();
        // NOTE: TraceManager.CallTag for set_trace_func 'call' event is in the callee like in MRI
        if (tag == TraceManager.CallTag.class) {
            return isTag(flags, FLAG_CALL);
        }

        if (tag == TraceManager.LineTag.class || tag == StandardTags.StatementTag.class) {
            return isTag(flags, FLAG_NEWLINE);
        }

        if (tag == CoverageManager.LineTag.class) {
            return isTag(flags, FLAG_COVERAGE_LINE);
        }

        if (tag == StandardTags.RootTag.class) {
            return isTag(flags, FLAG_ROOT);
        }

        return false;
    }

    private static boolean isTag(byte flags, byte flag) {
        return ((flags >> flag) & 1) == 1;
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probe) {
        return new RubyNodeWrapper(this, probe);
    }

    /** Return whether nodes following this one can ever be executed. In most cases this will be true, but some nodes
     * such as those representing a return or other control flow may wish to override this. */
    public boolean isContinuable() {
        return true;
    }

    /** Indicates whether this node can return a new version of itself combined with the next node, which may help with
     * analysis and optimisation. */
    public boolean canSubsumeFollowing() {
        return false;
    }

    /** Combine this node with the next node. Any node which returns true for {@link #canSubsumeFollowing} must override
     * this method. Any new node created in this method should use {@link #copySourceSection(RubyNode)}. */
    public RubyNode subsumeFollowing(RubyNode following) {
        throw new UnsupportedOperationException();
    }

    /** Return a possibly simplified version of this node. This is only called if the node is in tail position. Any new
     * node created in this method should use {@link #copySourceSection(RubyNode)}. */
    public RubyNode simplifyAsTailExpression() {
        return this;
    }


    // NodeLibrary

    @ExportMessage
    boolean accepts(
            @Cached(value = "this", adopt = false) RubyNode cachedNode) {
        return this == cachedNode;
    }

    @ExportMessage
    final boolean hasScope(Frame frame) {
        // hasScope == isAdoptable(), getParent() != null is a fast way to check if adoptable.
        return this.getParent() != null;
    }

    @ExportMessage
    final Object getScope(Frame frame, boolean nodeEnter,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @CachedLanguage RubyLanguage language) throws UnsupportedMessageException {
        if (hasScope(frame)) {
            return new RubyScope(context, language, frame.materialize(), this);
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    final boolean hasReceiverMember(Frame frame) {
        return frame != null;
    }

    @ExportMessage
    final Object getReceiverMember(Frame frame) throws UnsupportedMessageException {
        if (frame == null) {
            throw UnsupportedMessageException.create();
        }
        return RECEIVER_MEMBER;
    }

    @ExportMessage
    boolean hasRootInstance(Frame frame) {
        return frame != null;
    }

    @ExportMessage
    Object getRootInstance(Frame frame,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @CachedLanguage RubyLanguage language) throws UnsupportedMessageException {
        if (frame == null) {
            throw UnsupportedMessageException.create();
        }
        final Object self = RubyArguments.getSelf(frame);
        final InternalMethod method = RubyArguments.getMethod(frame);
        return new RubyMethod(
                context.getCoreLibrary().methodClass,
                language.methodShape,
                self,
                method);
    }

}
