/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayAppendOneNode;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.inlined.LambdaToProcNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.arguments.SplatToArgsNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

import java.util.Map;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE;
import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE_RETURN_MISSING;
import static org.truffleruby.language.dispatch.DispatchConfiguration.PROTECTED;

public class RubyCallNode extends RubyContextSourceNode implements AssignableNode {

    private final String methodName;

    @Child private RubyNode receiver;
    @Child private RubyNode block;
    private final boolean hasLiteralBlock;
    private final ArgumentsDescriptor descriptor;
    @Children private final RubyNode[] arguments;

    private final boolean isSplatted;
    private final DispatchConfiguration dispatchConfig;
    private final boolean isVCall;
    private final boolean isSafeNavigation;
    private final boolean isAttrAssign;

    @Child private DispatchNode dispatch;
    @Child private DefinedNode definedNode;

    private final ConditionProfile nilProfile;

    @Child private SplatToArgsNode splatToArgs;

    public RubyCallNode(RubyCallNodeParameters parameters) {
        this.methodName = parameters.getMethodName();
        this.receiver = parameters.getReceiver();
        this.arguments = parameters.getArguments();

        final RubyNode block = parameters.getBlock();
        this.block = parameters.getBlock();
        this.hasLiteralBlock = block instanceof BlockDefinitionNode || block instanceof LambdaToProcNode;
        this.descriptor = parameters.getDescriptor();

        this.isSplatted = parameters.isSplatted();
        this.dispatchConfig = parameters.isIgnoreVisibility() ? PRIVATE : PROTECTED;
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
        if (isSafeNavigation && nilProfile.profile(receiverObject == nil)) {
            return nil;
        }
        Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiverObject);
        RubyArguments.setDescriptor(rubyArgs, descriptor);

        executeArguments(frame, rubyArgs);

        RubyArguments.setBlock(rubyArgs, executeBlock(frame));

        // The expansion of the splat is done after executing the block, for m(*args, &args.pop)
        if (isSplatted) {
            rubyArgs = splatArgs(receiverObject, rubyArgs);
        }
        return executeWithArgumentsEvaluated(frame, receiverObject, rubyArgs);
    }

    @Override
    public void assign(VirtualFrame frame, Object value) {
        assert (getLastArgumentNode() instanceof NilLiteralNode &&
                ((NilLiteralNode) getLastArgumentNode()).isImplicit()) : getLastArgumentNode();

        final Object receiverObject = receiver.execute(frame);
        if (isSafeNavigation && nilProfile.profile(receiverObject == nil)) {
            return;
        }
        Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiverObject);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);

        executeArguments(frame, rubyArgs);

        RubyArguments.setBlock(rubyArgs, executeBlock(frame));
        RubyArguments.setDescriptor(rubyArgs, descriptor);

        if (isSplatted) {
            rubyArgs = splatArgs(receiverObject, rubyArgs);
        }

        int argCount = RubyArguments.getArgumentsCount(rubyArgs);
        assert RubyArguments.getArgument(rubyArgs, argCount - 1) == nil;
        RubyArguments.setArgument(rubyArgs, argCount - 1, value);

        executeWithArgumentsEvaluated(frame, receiverObject, rubyArgs);
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, Object[] rubyArgs) {
        if (dispatch == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            dispatch = insert(DispatchNode.create(dispatchConfig));
        }

        final Object returnValue = dispatch.dispatch(frame, receiverObject, methodName, rubyArgs);
        if (isAttrAssign) {
            final Object value = rubyArgs[rubyArgs.length - 1];
            assert RubyGuards.assertIsValidRubyValue(value);
            return value;
        } else {
            assert RubyGuards.assertIsValidRubyValue(returnValue);
            return returnValue;
        }
    }

    public Object executeWithArgumentsEvaluated(VirtualFrame frame, Object receiverObject, Object blockObject,
            Object[] argumentsObjects) {
        Object[] rubyArgs = RubyArguments.allocate(argumentsObjects.length);
        RubyArguments.setSelf(rubyArgs, receiverObject);
        RubyArguments.setBlock(rubyArgs, blockObject);
        RubyArguments.setDescriptor(rubyArgs, descriptor);
        RubyArguments.setArguments(rubyArgs, argumentsObjects);
        return executeWithArgumentsEvaluated(frame, receiverObject, rubyArgs);
    }

    private Object executeBlock(VirtualFrame frame) {
        if (block != null) {
            return block.execute(frame);
        } else {
            return nil;
        }
    }

    @ExplodeLoop
    private void executeArguments(VirtualFrame frame, Object[] rubyArgs) {
        for (int i = 0; i < arguments.length; i++) {
            RubyArguments.setArgument(rubyArgs, i, arguments[i].execute(frame));
        }
    }

    private Object[] splatArgs(Object receiverObject, Object[] rubyArgs) {
        if (splatToArgs == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            splatToArgs = insert(new SplatToArgsNode());
        }

        return splatToArgs.execute(receiverObject, rubyArgs, (RubyArray) RubyArguments.getArgument(rubyArgs, 0));
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        if (definedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            definedNode = insert(new DefinedNode());
        }

        return definedNode.isDefined(frame, context);
    }

    public String getName() {
        return methodName;
    }

    public boolean isVCall() {
        return isVCall;
    }

    public boolean hasLiteralBlock() {
        return hasLiteralBlock;
    }

    private RubyNode getLastArgumentNode() {
        final RubyNode lastArg = arguments[arguments.length - 1];
        if (isSplatted && lastArg instanceof ArrayAppendOneNode) {
            return ((ArrayAppendOneNode) lastArg).getValueNode();
        }
        return lastArg;
    }

    @Override
    public AssignableNode toAssignableNode() {
        return this;
    }

    @Override
    public Map<String, Object> getDebugProperties() {
        final Map<String, Object> map = super.getDebugProperties();
        map.put("methodName", methodName);
        return map;
    }

    private class DefinedNode extends RubyBaseNode {

        private final RubySymbol methodNameSymbol = getSymbol(methodName);

        @Child private DispatchNode respondToMissing = DispatchNode.create(PRIVATE_RETURN_MISSING);
        @Child private BooleanCastNode respondToMissingCast = BooleanCastNodeGen.create(null);


        @Child private LookupMethodOnSelfNode lookupMethodNode = LookupMethodOnSelfNode.create();

        private final ConditionProfile receiverDefinedProfile = ConditionProfile.create();
        private final BranchProfile argumentNotDefinedProfile = BranchProfile.create();
        private final BranchProfile allArgumentsDefinedProfile = BranchProfile.create();
        private final BranchProfile receiverExceptionProfile = BranchProfile.create();
        private final ConditionProfile methodNotFoundProfile = ConditionProfile.create();

        @ExplodeLoop
        public Object isDefined(VirtualFrame frame, RubyContext context) {
            if (receiverDefinedProfile.profile(receiver.isDefined(frame, getLanguage(), context) == nil)) {
                return nil;
            }

            for (RubyNode argument : arguments) {
                if (argument.isDefined(frame, getLanguage(), context) == nil) {
                    argumentNotDefinedProfile.enter();
                    return nil;
                }
            }

            allArgumentsDefinedProfile.enter();

            final Object receiverObject;

            try {
                receiverObject = receiver.execute(frame);
            } catch (Exception e) {
                receiverExceptionProfile.enter();
                return nil;
            }

            final InternalMethod method = lookupMethodNode.execute(frame, receiverObject, methodName, dispatchConfig);

            if (methodNotFoundProfile.profile(method == null)) {
                final Object r = respondToMissing.call(receiverObject, "respond_to_missing?", methodNameSymbol, false);

                if (r != DispatchNode.MISSING && !respondToMissingCast.executeToBoolean(r)) {
                    return nil;
                }
            }

            return FrozenStrings.METHOD;
        }
    }
}
