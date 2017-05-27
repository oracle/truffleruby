/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.array.ArrayToObjectArrayNodeGen;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.cast.ProcOrNullNodeGen;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;

public class RubyCallNode extends RubyNode {

    private final String methodName;

    @Child private RubyNode receiver;
    @Child private ProcOrNullNode block;
    @Children private final RubyNode[] arguments;

    private final boolean isSplatted;
    private final boolean ignoreVisibility;
    private final boolean isVCall;
    private final boolean isSafeNavigation;
    private final boolean isAttrAssign;

    @Child private CallDispatchHeadNode dispatchHead;
    @Child private ArrayToObjectArrayNode toObjectArrayNode;
    @Child private DefinedNode definedNode;

    private final ConditionProfile nilProfile;

    public RubyCallNode(RubyCallNodeParameters parameters) {
        this.methodName = parameters.getMethodName();
        this.receiver = parameters.getReceiver();
        this.arguments = parameters.getArguments();

        if (parameters.getBlock() == null) {
            this.block = null;
        } else {
            this.block = ProcOrNullNodeGen.create(parameters.getBlock());
        }

        this.isSplatted = parameters.isSplatted();
        this.ignoreVisibility = parameters.isIgnoreVisibility();
        this.isVCall = parameters.isVCall();
        this.isSafeNavigation = parameters.isSafeNavigation();
        this.isAttrAssign = parameters.isAttrAssign();

        if (parameters.isSafeNavigation()) {
            nilProfile = ConditionProfile.createCountingProfile();
        } else {
            nilProfile = null;
        }
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object receiverObject = receiver.execute(frame);

        if (isSafeNavigation) {
            if (nilProfile.profile(receiverObject == nil())) {
                return nil();
            }
        }

        final Object[] argumentsObjects = executeArguments(frame);

        return executeWithArgumentsEvaluated(frame, receiverObject, argumentsObjects);
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, Object[] argumentsObjects) {
        final DynamicObject blockObject = executeBlock(frame);

        if (dispatchHead == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dispatchHead = insert(DispatchHeadNodeFactory.createMethodCall(ignoreVisibility));
        }

        final Object returnValue = dispatchHead.dispatch(frame, receiverObject, methodName, blockObject, argumentsObjects);
        if (isAttrAssign) {
            return argumentsObjects[argumentsObjects.length - 1];
        } else {
            return returnValue;
        }
    }

    private DynamicObject executeBlock(VirtualFrame frame) {
        if (block != null) {
            return (DynamicObject) block.execute(frame);
        } else {
            return null;
        }
    }

    @ExplodeLoop
    private Object[] executeArguments(VirtualFrame frame) {
        final Object[] argumentsObjects = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            argumentsObjects[i] = arguments[i].execute(frame);
        }

        if (isSplatted) {
            assert argumentsObjects.length == 1;
            return splat(argumentsObjects);
        } else {
            return argumentsObjects;
        }
    }

    private Object[] splat(Object[] arguments) {
        if (toObjectArrayNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toObjectArrayNode = insert(ArrayToObjectArrayNodeGen.create(null));
        }
        // TODO(CS): what happens if it isn't an Array?
        return toObjectArrayNode.unsplat(arguments);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        if (definedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            definedNode = insert(new DefinedNode());
        }

        return definedNode.isDefined(frame);
    }

    public String getName() {
        return methodName;
    }

    public boolean isVCall() {
        return isVCall;
    }

    public boolean hasLiteralBlock() {
        assert block != null;
        return block.getChild() instanceof BlockDefinitionNode;
    }

    private class DefinedNode extends Node {

        private final DynamicObject methodNameSymbol = getContext().getSymbolTable().getSymbol(methodName);

        @Child private CallDispatchHeadNode respondToMissing = DispatchHeadNodeFactory.createMethodCall(true, MissingBehavior.RETURN_MISSING);
        @Child private BooleanCastNode respondToMissingCast = BooleanCastNodeGen.create(null);

        // TODO CS-10-Apr-17 see below
        // @Child private LookupMethodNode lookupMethodNode = LookupMethodNodeGen.create(ignoreVisibility, false, null, null);

        private final ConditionProfile receiverDefinedProfile = ConditionProfile.createBinaryProfile();
        private final BranchProfile argumentNotDefinedProfile = BranchProfile.create();
        private final BranchProfile allArgumentsDefinedProfile = BranchProfile.create();
        private final BranchProfile receiverExceptionProfile = BranchProfile.create();
        private final ConditionProfile methodNotFoundProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile methodUndefinedProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile methodNotVisibleProfile = ConditionProfile.createBinaryProfile();

        @ExplodeLoop
        public Object isDefined(VirtualFrame frame) {
            if (receiverDefinedProfile.profile(receiver.isDefined(frame) == nil())) {
                return nil();
            }

            for (RubyNode argument : arguments) {
                if (argument.isDefined(frame) == nil()) {
                    argumentNotDefinedProfile.enter();
                    return nil();
                }
            }

            allArgumentsDefinedProfile.enter();

            final Object receiverObject;

            try {
                receiverObject = receiver.execute(frame);
            } catch (Exception e) {
                receiverExceptionProfile.enter();
                return nil();
            }

            final InternalMethod method = doLookup(receiverObject);
            final Object self = RubyArguments.getSelf(frame);

            if (methodNotFoundProfile.profile(method == null)) {
                final Object r = respondToMissing.call(frame, receiverObject, "respond_to_missing?", methodNameSymbol, false);

                if (r != DispatchNode.MISSING && !respondToMissingCast.executeToBoolean(r)) {
                    return nil();
                }
            } else if (methodUndefinedProfile.profile(method.isUndefined())) {
                return nil();
            } else if (methodNotVisibleProfile.profile(!ignoreVisibility && !isVisibleTo(method, self))) {
                return nil();
            }

            return coreStrings().METHOD.createInstance();
        }

        // TODO CS-10-Apr-17 remove this boundary

        @TruffleBoundary
        private InternalMethod doLookup(Object receiverObject) {
            // TODO CS-10-Apr-17 I'd like to use this but it doesn't give the same result
            // lookupMethodNode.executeLookupMethod(frame, coreLibrary().getMetaClass(receiverObject), methodName);

            return ModuleOperations.lookupMethod(coreLibrary().getMetaClass(receiverObject), methodName).getMethod();
        }

        // TODO CS-10-Apr-17 remove this boundary

        @TruffleBoundary
        private boolean isVisibleTo(InternalMethod method, Object self) {
            return method.isVisibleTo(coreLibrary().getMetaClass(self));
        }

    }


}
