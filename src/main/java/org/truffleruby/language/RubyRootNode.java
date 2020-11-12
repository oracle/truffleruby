/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.methods.SharedMethodInfo;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.truffleruby.language.methods.Split;

public final class RubyRootNode extends RubyBaseRootNode {

    private final RubyLanguage language;
    private final SharedMethodInfo sharedMethodInfo;
    private Split split;

    @Child private RubyNode body;

    private CyclicAssumption needsCallerAssumption = new CyclicAssumption("needs caller data");

    public RubyRootNode(
            RubyLanguage language,
            SourceSection sourceSection,
            FrameDescriptor frameDescriptor,
            SharedMethodInfo sharedMethodInfo,
            RubyNode body,
            Split split) {
        super(language, frameDescriptor, sourceSection);
        assert sourceSection != null;
        assert body != null;

        this.language = language;
        this.sharedMethodInfo = sharedMethodInfo;
        this.body = body;
        this.split = split;

        // Ensure the body node is instrument-able, which requires a non-null SourceSection
        if (!body.hasSource()) {
            body.unsafeSetSourceSection(getSourceSection());
        }

        body.unsafeSetIsCall();
        body.unsafeSetIsRoot();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        SafepointManager.poll(language, this);
        return body.execute(frame);
    }

    @Override
    public boolean isCloningAllowed() {
        return split != Split.NEVER;
    }

    public boolean shouldAlwaysClone() {
        return split == Split.ALWAYS;
    }

    public Split getSplit() {
        return split;
    }

    public void setSplit(Split split) {
        this.split = split;
    }

    @Override
    public String getName() {
        return sharedMethodInfo.getModuleAndMethodName();
    }

    @Override
    public String toString() {
        return sharedMethodInfo.getModuleAndMethodName();
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public RubyNode getBody() {
        return body;
    }

    public Assumption getNeedsCallerAssumption() {
        return needsCallerAssumption.getAssumption();
    }

    public void invalidateNeedsCallerAssumption() {
        needsCallerAssumption.invalidate("needs caller frame");
    }

    public synchronized void invalidateNeedsVariablesAssumption() {
        needsCallerAssumption.invalidate("needs caller special variable storage");
    }

    @Override
    public Node copy() {
        RubyRootNode root = (RubyRootNode) super.copy();
        root.needsCallerAssumption = new CyclicAssumption("needs caller data");
        return root;
    }


}
