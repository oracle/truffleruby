/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class CheckArityNode extends RubyContextSourceNode {

    private final Arity arity;
    @Child private RubyNode body;

    private final BranchProfile checkFailedProfile = BranchProfile.create();

    public CheckArityNode(Arity arity, RubyNode body) {
        this.arity = arity;
        this.body = body;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        checkArity(arity, RubyArguments.getArgumentsCount(frame), checkFailedProfile, getContextReference(), this);
        body.doExecuteVoid(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        checkArity(arity, RubyArguments.getArgumentsCount(frame), checkFailedProfile, getContextReference(), this);
        return body.execute(frame);
    }

    public static void checkArity(Arity arity, int given,
            BranchProfile checkFailedProfile,
            ContextReference<RubyContext> contextRef,
            Node currentNode) {
        CompilerAsserts.partialEvaluationConstant(arity);
        if (!arity.check(given)) {
            checkFailedProfile.enter();
            checkArityError(arity, given, contextRef, currentNode);
        }
    }

    private static void checkArityError(Arity arity, int given, ContextReference<RubyContext> contextRef,
            Node currentNode) {
        final RubyContext context = contextRef.get();
        if (arity.hasRest()) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentErrorPlus(given, arity.getRequired(), currentNode));
        } else if (arity.getOptional() > 0) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(
                            given,
                            arity.getRequired(),
                            arity.getOptional(),
                            currentNode));
        } else {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(given, arity.getRequired(), currentNode));
        }
    }

    @Override
    public RubyNode simplifyAsTailExpression() {
        return new CheckArityNode(arity, body.simplifyAsTailExpression()).copySourceSection(this);
    }
}
