/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.HostCompilerDirectives.InliningCutoff;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.exception.ExceptionOperations.ExceptionFormatter;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.SpecialVariablesSendingNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptorManager;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.CallForeignMethodNode;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.options.Options;

import static org.truffleruby.language.dispatch.DispatchConfiguration.PRIVATE;

@GenerateUncached
public abstract class DispatchNode extends SpecialVariablesSendingNode {

    private static final class Missing implements TruffleObject {
    }

    public static final Missing MISSING = new Missing();

    @NeverDefault
    public static DispatchNode create() {
        return DispatchNodeGen.create();
    }

    @NeverDefault
    public static DispatchNode getUncached() {
        return DispatchNodeGen.getUncached();
    }


    public abstract Object execute(Frame frame, Object receiver, String methodName, Object[] rubyArgs,
            DispatchConfiguration config,
            LiteralCallNode literalCallNode);

    public Object call(Object receiver, String method, DispatchConfiguration config) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        return execute(null, receiver, method, rubyArgs, config, null);
    }

    public Object call(Object receiver, String method) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object call(Object receiver, String method, DispatchConfiguration config, Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        return execute(null, receiver, method, rubyArgs, config, null);
    }

    public Object call(Object receiver, String method, Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object call(Object receiver, String method, DispatchConfiguration config, Object arg1, Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        return execute(null, receiver, method, rubyArgs, config, null);
    }

    public Object call(Object receiver, String method, Object arg1, Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object call(Object receiver, String method, Object arg1, Object arg2, Object arg3) {
        final Object[] rubyArgs = RubyArguments.allocate(3);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        RubyArguments.setArgument(rubyArgs, 2, arg3);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object call(Object receiver, String method, DispatchConfiguration config, Object[] arguments) {
        final Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArguments(rubyArgs, arguments);
        return execute(null, receiver, method, rubyArgs, config, null);
    }

    public Object call(Object receiver, String method, Object[] arguments) {
        final Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArguments(rubyArgs, arguments);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object callWithKeywords(Object receiver, String method, Object arg1,
            RubyHash keywords) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, KeywordArgumentsDescriptorManager.EMPTY);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, keywords);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object callWithBlock(Object receiver, String method, Object block) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object callWithBlock(Object receiver, String method, Object block, Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        return execute(null, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object callWithBlock(Object receiver, String method, DispatchConfiguration config, Object block, Object arg1,
            Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        return execute(null, receiver, method, rubyArgs, config, null);
    }

    public Object callWithBlock(Object receiver, String method, DispatchConfiguration config, Object block, Object arg1,
            Object arg2, Object arg3) {
        final Object[] rubyArgs = RubyArguments.allocate(3);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        RubyArguments.setArgument(rubyArgs, 2, arg3);
        return execute(null, receiver, method, rubyArgs, config, null);
    }

    public Object callWithDescriptor(Object receiver, String method, Object block,
            ArgumentsDescriptor descriptor, Object[] arguments) {
        return callWithDescriptor(receiver, method, block, descriptor, arguments, null);
    }

    public Object callWithDescriptor(Object receiver, String method, Object block,
            ArgumentsDescriptor descriptor,
            Object[] arguments, LiteralCallNode literalCallNode) {
        final Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setDescriptor(rubyArgs, descriptor);
        RubyArguments.setArguments(rubyArgs, arguments);
        return execute(null, receiver, method, rubyArgs, PRIVATE, literalCallNode);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        return execute(frame, receiver, method, rubyArgs, PRIVATE, null);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, DispatchConfiguration config,
            Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        return execute(frame, receiver, method, rubyArgs, config, null);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, DispatchConfiguration config, Object arg1,
            Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        return execute(frame, receiver, method, rubyArgs, config, null);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, DispatchConfiguration config, Object arg1,
            Object arg2, Object arg3) {
        final Object[] rubyArgs = RubyArguments.allocate(3);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArgument(rubyArgs, 0, arg1);
        RubyArguments.setArgument(rubyArgs, 1, arg2);
        RubyArguments.setArgument(rubyArgs, 2, arg3);
        return execute(frame, receiver, method, rubyArgs, config, null);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, DispatchConfiguration config,
            Object[] arguments) {
        final Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setDescriptor(rubyArgs, EmptyArgumentsDescriptor.INSTANCE);
        RubyArguments.setArguments(rubyArgs, arguments);
        return execute(frame, receiver, method, rubyArgs, config, null);
    }

    public final Object callWithFrameAndBlock(Frame frame, Object receiver, String methodName,
            DispatchConfiguration config, Object block,
            ArgumentsDescriptor descriptor, Object[] arguments) {
        final Object[] rubyArgs = RubyArguments.allocate(arguments.length);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setDescriptor(rubyArgs, descriptor);
        RubyArguments.setArguments(rubyArgs, arguments);
        return execute(frame, receiver, methodName, rubyArgs, config, null);
    }

    @Specialization
    protected Object dispatch(
            Frame frame,
            Object receiver,
            String methodName,
            Object[] rubyArgs,
            DispatchConfiguration config,
            LiteralCallNode literalCallNode,
            @Cached(value = "getSpecialVariableAssumption(frame)",
                    uncached = "getValidAssumption()") Assumption specialVariableAssumption,
            @Cached MetaClassNode metaclassNode,
            @Cached LookupMethodNode methodLookup,
            @Cached ConditionProfile methodMissing,
            @Cached CallInternalMethodNode callNode,
            @Cached CallForeignMethodNode callForeign,
            @Cached DispatchNode callMethodMissing,
            @Cached ToSymbolNode toSymbol,
            @Cached GetSpecialVariableStorage readingNode) {
        return dispatchInternal(frame, receiver, methodName, rubyArgs, config, literalCallNode,
                metaclassNode, methodLookup, methodMissing, callNode, readingNode, callForeign, callMethodMissing,
                toSymbol, specialVariableAssumption);
    }

    protected final Object dispatchInternal(Frame frame, Object receiver, String methodName, Object[] rubyArgs,
            DispatchConfiguration config,
            LiteralCallNode literalCallNode,
            MetaClassNode metaClassNode,
            LookupMethodNode lookupMethodNode,
            ConditionProfile methodMissingProfile,
            CallInternalMethodNode callNode,
            GetSpecialVariableStorage readingNode,
            CallForeignMethodNode callForeign,
            DispatchNode callMethodMissing,
            ToSymbolNode toSymbol, Assumption specialVariableAssumption) {
        assert RubyArguments.getSelf(rubyArgs) == receiver;

        final RubyClass metaclass = metaClassNode.execute(this, receiver);
        final InternalMethod method = lookupMethodNode.execute(frame, metaclass, methodName, config);

        if (methodMissingProfile.profile(method == null || method.isUndefined())) {
            switch (config.missingBehavior) {
                case RETURN_MISSING:
                    return MISSING;
                case CALL_METHOD_MISSING:
                    // Both branches implicitly profile through lazy node creation
                    if (RubyGuards.isForeignObject(receiver)) { // TODO (eregon, 16 Aug 2021) maybe use a final boolean on the class to know if foreign
                        return callForeign(receiver, methodName, rubyArgs, callForeign);
                    } else {
                        return callMethodMissing(frame, receiver, methodName, rubyArgs, literalCallNode,
                                callMethodMissing, toSymbol);
                    }
            }
        }

        RubyArguments.setMethod(rubyArgs, method);

        if (!specialVariableAssumption.isValid()) {
            RubyArguments.setCallerSpecialVariables(rubyArgs, readingNode.execute(frame));
        }
        assert RubyArguments.assertFrameArguments(rubyArgs);

        return callNode.execute(frame, method, receiver, rubyArgs, literalCallNode);
    }


    @InliningCutoff
    private Object callMethodMissing(Frame frame, Object receiver, String methodName, Object[] rubyArgs,
            LiteralCallNode literalCallNode, DispatchNode callMethodMissing, ToSymbolNode toSymbol) {
        // profiles through lazy node creation
        final RubySymbol symbolName = toSymbol.execute(this, methodName);

        final Object[] newArgs = RubyArguments.repack(rubyArgs, receiver, 0, 1);

        RubyArguments.setArgument(newArgs, 0, symbolName);
        final Object result = callMethodMissing.execute(frame, receiver, "method_missing", newArgs,
                DispatchConfiguration.PRIVATE_RETURN_MISSING_IGNORE_REFINEMENTS,
                literalCallNode);

        if (result == MISSING) {
            throw new RaiseException(getContext(), coreExceptions().noMethodErrorFromMethodMissing(
                    ExceptionFormatter.NO_METHOD_ERROR,
                    receiver,
                    methodName,
                    RubyArguments.getPositionalArguments(rubyArgs),
                    this));
        }

        return result;
    }

    @InliningCutoff
    protected Object callForeign(Object receiver, String methodName, Object[] rubyArgs,
            CallForeignMethodNode callForeignMethodNode) {
        // profiles through lazy node creation
        //final CallForeignMethodNode callForeignMethodNode = getCallForeignMethodNode();

        final Object block = RubyArguments.getBlock(rubyArgs);
        final Object[] arguments = RubyArguments.getPositionalArguments(rubyArgs);
        return callForeignMethodNode.execute(receiver, methodName, block, arguments);
    }

    //    protected CallForeignMethodNode getCallForeignMethodNode() {
    //        if (callForeign == null) {
    //            CompilerDirectives.transferToInterpreterAndInvalidate();
    //            callForeign = insert(CallForeignMethodNode.create());
    //        }
    //        return callForeign;
    //    }

    //    protected DispatchNode getMethodMissingNode() {
    //        if (callMethodMissing == null) {
    //            CompilerDirectives.transferToInterpreterAndInvalidate();
    //            // #method_missing ignores refinements on CRuby: https://bugs.ruby-lang.org/issues/13129
    //            callMethodMissing = insert(
    //                    DispatchNode.create());
    //        }
    //        return callMethodMissing;
    //    }

    //    protected void methodMissingMissingProfileEnter() {
    //        if (!methodMissingMissingProfile) {
    //            CompilerDirectives.transferToInterpreterAndInvalidate();
    //            methodMissingMissingProfile = true;
    //        }
    //    }

    //    protected RubySymbol nameToSymbol(String methodName) {
    //        if (toSymbol == null) {
    //            CompilerDirectives.transferToInterpreterAndInvalidate();
    //            toSymbol = insert(ToSymbolNodeGen.create());
    //        }
    //        return toSymbol.execute(methodName);
    //    }

    /** This will be called from the {@link CallInternalMethodNode} child whenever it creates a new
     * {@link DirectCallNode}. */
    public final void applySplittingInliningStrategy(RootCallTarget callTarget, String methodName,
            DirectCallNode callNode) {


        final Options options = getContext().getOptions();

        // The way that #method_missing is used is usually as an indirection to call some other method, and possibly to
        // modify the arguments. In both cases, but especially the latter, it makes a lot of sense to manually clone the
        // call target and to inline it.
        final boolean isMethodMissing = methodName.equals("method_missing");

        if (callNode.isCallTargetCloningAllowed() &&
                (RubyRootNode.of(callTarget).shouldAlwaysClone() ||
                        isMethodMissing && options.METHODMISSING_ALWAYS_CLONE)) {
            callNode.cloneCallTarget();
        }

        if (callNode.isInlinable() && isMethodMissing && options.METHODMISSING_ALWAYS_INLINE) {
            callNode.forceInlining();
        }
    }
}
