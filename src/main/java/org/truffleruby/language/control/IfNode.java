/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanExecute;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class IfNode extends RubyContextSourceNode {

    @Child private BooleanExecute condition;
    @Child private RubyNode thenBody;

    private final ConditionProfile conditionProfile = ConditionProfile.createCountingProfile();

    public IfNode(RubyNode condition, RubyNode thenBody) {
        this(BooleanCastNode.createIfNeeded(condition), thenBody);
    }

    private IfNode(BooleanExecute condition, RubyNode thenBody) {
        this.condition = condition;
        this.thenBody = thenBody;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (conditionProfile.profile(condition.executeBoolean(frame))) {
            return thenBody.execute(frame);
        } else {
            return nil;
        }
    }

    @Override
    public boolean canSubsumeFollowing() {
        return !thenBody.isContinuable();
    }

    @Override
    public RubyNode subsumeFollowing(RubyNode following) {
        return new IfElseNode(condition, thenBody, following).copySourceSection(this);
    }

    @Override
    public RubyNode simplifyAsTailExpression() {
        return new IfNode(condition, thenBody.simplifyAsTailExpression()).copySourceSection(this);
    }
}
