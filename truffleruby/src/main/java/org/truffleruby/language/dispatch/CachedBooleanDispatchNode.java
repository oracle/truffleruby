/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.language.methods.InternalMethod;

public class CachedBooleanDispatchNode extends CachedDispatchNode {

    @CompilationFinal private final Assumption[] falseAssumptions;
    private final InternalMethod falseMethod;
    private final BranchProfile falseProfile = BranchProfile.create();

    @Child private DirectCallNode falseCallDirect;

    @CompilationFinal private final Assumption[] trueAssumptions;
    private final InternalMethod trueMethod;
    private final BranchProfile trueProfile = BranchProfile.create();

    @Child private DirectCallNode trueCallDirect;

    public CachedBooleanDispatchNode(
            Object cachedName,
            DispatchNode next,
            MethodLookupResult falseMethodLookup,
            MethodLookupResult trueMethodLookup,
            DispatchAction dispatchAction) {
        super(cachedName, next, dispatchAction);

        this.falseAssumptions = falseMethodLookup.getAssumptions();
        this.falseMethod = falseMethodLookup.getMethod();

        if (falseMethodLookup.isDefined()) {
            this.falseCallDirect = Truffle.getRuntime().createDirectCallNode(falseMethod.getCallTarget());
            applySplittingInliningStrategy(falseCallDirect, falseMethod);
        }

        this.trueAssumptions = trueMethodLookup.getAssumptions();
        this.trueMethod = trueMethodLookup.getMethod();

        if (trueMethodLookup.isDefined()) {
            this.trueCallDirect = Truffle.getRuntime().createDirectCallNode(trueMethod.getCallTarget());
            applySplittingInliningStrategy(trueCallDirect, trueMethod);
        }
    }

    @Override
    protected boolean guard(Object methodName, Object receiver) {
        return guardName(methodName) && (receiver instanceof Boolean);
    }

    @Override
    public Object executeDispatch(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            DynamicObject blockObject,
            Object[] argumentsObjects) {
        try {
            checkAssumptions(trueAssumptions);
            checkAssumptions(falseAssumptions);
        } catch (InvalidAssumptionException e) {
            return resetAndDispatch(
                    frame,
                    receiverObject,
                    methodName,
                    blockObject,
                    argumentsObjects,
                    "class modified");
        }

        if (!guard(methodName, receiverObject)) {
            return next.executeDispatch(
                    frame,
                    receiverObject,
                    methodName,
                    blockObject,
                    argumentsObjects);
        }

        if ((boolean) receiverObject) {
            trueProfile.enter();

            switch (getDispatchAction()) {
                case CALL_METHOD:
                    return call(trueCallDirect, frame, trueMethod, receiverObject, blockObject, argumentsObjects);
                case RESPOND_TO_METHOD:
                    return true;

                default:
                    throw new UnsupportedOperationException();
            }
        } else {
            falseProfile.enter();

            switch (getDispatchAction()) {
                case CALL_METHOD:
                    return call(falseCallDirect, frame, falseMethod, receiverObject, blockObject, argumentsObjects);

                case RESPOND_TO_METHOD:
                    return true;

                default:
                    throw new UnsupportedOperationException();

            }
        }
    }

}
