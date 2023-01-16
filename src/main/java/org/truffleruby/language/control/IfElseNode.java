/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.profiles.CountingConditionProfile;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class IfElseNode extends RubyContextSourceNode {

    @Child private BooleanCastNode condition;
    @Child private RubyNode thenBody;
    @Child private RubyNode elseBody;

    private final CountingConditionProfile conditionProfile = CountingConditionProfile.create();

    public IfElseNode(RubyNode condition, RubyNode thenBody, RubyNode elseBody) {
        this.condition = BooleanCastNodeGen.create(condition);
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    IfElseNode(BooleanCastNode condition, RubyNode thenBody, RubyNode elseBody) {
        this.condition = condition;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (conditionProfile.profile(condition.execute(frame))) {
            return thenBody.execute(frame);
        } else {
            return elseBody.execute(frame);
        }
    }

    @Override
    public boolean isContinuable() {
        return thenBody.isContinuable() || elseBody.isContinuable();
    }

    @Override
    public RubyNode simplifyAsTailExpression() {
        final RubyNode newThen = thenBody.simplifyAsTailExpression();
        final RubyNode newElse = elseBody.simplifyAsTailExpression();
        return new IfElseNode(condition, newThen, newElse).copySourceSection(this);
    }

    private RubyNode getConditionBeforeCasting() {
        return condition.getValueNode();
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new IfElseNode(
                getConditionBeforeCasting().cloneUninitialized(),
                thenBody.cloneUninitialized(),
                elseBody.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
