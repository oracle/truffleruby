/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.nodes.NodeUtil;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.ReRaiseInlinedExceptionNode;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.methods.SharedMethodInfo;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.language.methods.Split;
import org.truffleruby.parser.ParentFrameDescriptor;

public class RubyRootNode extends RubyBaseRootNode {

    public static RubyRootNode of(RootCallTarget callTarget) {
        return (RubyRootNode) callTarget.getRootNode();
    }

    private final SharedMethodInfo sharedMethodInfo;
    private Split split;
    public final ReturnID returnID;

    @Child protected RubyNode body;

    public RubyRootNode(
            RubyLanguage language,
            SourceSection sourceSection,
            FrameDescriptor frameDescriptor,
            SharedMethodInfo sharedMethodInfo,
            RubyNode body,
            Split split,
            ReturnID returnID) {
        super(language, frameDescriptor, sourceSection);
        assert sourceSection != null;
        assert body != null;

        this.sharedMethodInfo = sharedMethodInfo;
        this.body = body;
        this.split = split;
        this.returnID = returnID;

        // Ensure the body node is instrument-able, which requires a non-null SourceSection
        if (!body.hasSource()) {
            body.unsafeSetSourceSection(getSourceSection());
        }

        body.unsafeSetIsCall();
        body.unsafeSetIsRoot();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return body.execute(frame);
    }

    @Override
    public FrameDescriptor getParentFrameDescriptor() {
        var info = getFrameDescriptor().getInfo();
        if (info instanceof ParentFrameDescriptor) {
            return ((ParentFrameDescriptor) info).getDescriptor();
        } else {
            return null;
        }
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
        return sharedMethodInfo.getParseName();
    }

    @Override
    public String toString() {
        return sharedMethodInfo.getParseName();
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public NodeFactory<? extends RubyBaseNode> getAlwaysInlinedNodeFactory() {
        return ((ReRaiseInlinedExceptionNode) body).nodeFactory;
    }

    public RubyNode copyBody() {
        return NodeUtil.cloneNode(body);
    }

}
