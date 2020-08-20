/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.FrameSendingNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.CallForeignMethodNode;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.CallInternalMethodNodeGen;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.MetaClassNodeGen;
import org.truffleruby.options.Options;

public class DispatchNode extends FrameSendingNode {

    private static final class Missing implements TruffleObject {
    }

    public static final Missing MISSING = new Missing();

    // NOTE(norswap): We need these static fields to be able to specify these values as `Cached#parameters` string
    //   values. We also want to use `parameters` rather than factory methods because Truffle uses it to automatically
    //   generate uncached instances where required.

    public static final DispatchConfiguration PRIVATE = DispatchConfiguration.PRIVATE;
    public static final DispatchConfiguration PUBLIC = DispatchConfiguration.PUBLIC;
    public static final DispatchConfiguration PRIVATE_RETURN_MISSING = DispatchConfiguration.PRIVATE_RETURN_MISSING;
    public static final DispatchConfiguration PUBLIC_RETURN_MISSING = DispatchConfiguration.PUBLIC_RETURN_MISSING;
    public static final DispatchConfiguration PRIVATE_DOES_RESPOND = DispatchConfiguration.PRIVATE_DOES_RESPOND;
    public static final DispatchConfiguration PUBLIC_DOES_RESPOND = DispatchConfiguration.PUBLIC_DOES_RESPOND;

    public static DispatchNode create(DispatchConfiguration config) {
        return new DispatchNode(config);
    }

    public static DispatchNode create() {
        return new DispatchNode(DispatchConfiguration.PRIVATE);
    }

    public static DispatchNode getUncached(DispatchConfiguration config) {
        return Uncached.uncachedNodes[config.ordinal()];
    }

    public static DispatchNode getUncached() {
        return getUncached(DispatchConfiguration.PRIVATE);
    }

    public final DispatchConfiguration config;

    @Child protected MetaClassNode metaclassNode;
    @Child protected LookupMethodNode methodLookup;
    @Child protected CallInternalMethodNode callNode;
    @Child protected NameToJavaStringNode nameToString;
    @Child protected CallForeignMethodNode callForeign;
    @Child protected DispatchNode callMethodMissing;

    protected final ConditionProfile nameIsString;
    protected final ConditionProfile methodMissing;
    protected final ConditionProfile isForeignCall;
    protected final BranchProfile methodMissingMissing;

    protected DispatchNode(
            DispatchConfiguration config,
            MetaClassNode metaclassNode,
            LookupMethodNode methodLookup,
            CallInternalMethodNode callNode,
            ConditionProfile nameIsString,
            ConditionProfile methodMissing,
            ConditionProfile isForeignCall,
            BranchProfile methodMissingMissing) {
        this.config = config;
        this.metaclassNode = metaclassNode;
        this.methodLookup = methodLookup;
        this.callNode = callNode;
        this.nameIsString = nameIsString;
        this.methodMissing = methodMissing;
        this.isForeignCall = isForeignCall;
        this.methodMissingMissing = methodMissingMissing;
    }

    protected DispatchNode(DispatchConfiguration config) {
        this(
                config,
                MetaClassNode.create(),
                LookupMethodNode.create(),
                CallInternalMethodNode.create(),
                ConditionProfile.create(),
                ConditionProfile.create(),
                ConditionProfile.create(),
                BranchProfile.create());
    }

    public Object call(Object receiver, String method, Object... arguments) {
        return execute(null, receiver, method, null, arguments);
    }

    public Object callWithBlock(Object receiver, String method, RubyProc block, Object... arguments) {
        return execute(null, receiver, method, block, arguments);
    }

    public Object dispatch(VirtualFrame frame, Object receiver, Object methodName, RubyProc block, Object[] arguments) {
        return execute(frame, receiver, methodName, block, arguments);
    }

    public boolean doesRespondTo(VirtualFrame frame, Object methodName, Object receiver) {
        assert config == PRIVATE_DOES_RESPOND || config == PUBLIC_DOES_RESPOND;
        return (boolean) execute(frame, receiver, methodName, null, EMPTY_ARGUMENTS);
    }

    public Object execute(VirtualFrame frame, Object receiver, Object methodName, RubyProc block, Object[] arguments) {

        final String stringName = nameIsString.profile(methodName instanceof String)
                ? (String) methodName
                : nameToString(methodName);

        final RubyClass metaclass = metaclassNode.execute(receiver);

        if (isForeignCall.profile(metaclass == getContext().getCoreLibrary().truffleInteropForeignClass)) {
            assert config.dispatchAction == DispatchAction.CALL_METHOD;
            return callForeign(receiver, stringName, block, arguments);
        }

        final InternalMethod method = methodLookup.execute(frame, metaclass, stringName, config);

        if (methodMissing.profile(method == null || method.isUndefined())) {
            switch (config.dispatchAction) {
                case RESPOND_TO_METHOD:
                    return false;
                case CALL_METHOD:
                    switch (config.missingBehavior) {
                        case RETURN_MISSING:
                            return MISSING;
                        case CALL_METHOD_MISSING:
                            return callMethodMissing(frame, receiver, methodName, stringName, block, arguments);
                    }
            }
        }

        if (config.dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
            // NOTE: The whole point of an "unimplemented method" is that is is callable, but `respond_to` returns false.
            return method.isImplemented();
        }

        final MaterializedFrame callerFrame = getFrameIfRequiredNew(frame);
        final Object[] frameArguments = RubyArguments.pack(null, callerFrame, method, null, receiver, block, arguments);

        return callNode.execute(method, frameArguments);
    }

    private Object callMethodMissing(
            VirtualFrame frame, Object receiver, Object methodName, String stringName, RubyProc block,
            Object[] arguments) {

        final RubySymbol symbolName = nameToSymbol(methodName);
        final Object[] newArguments = ArrayUtils.unshift(arguments, symbolName);
        final Object result = callMethodMissingNode(frame, receiver, block, newArguments);

        if (result == MISSING) {
            methodMissingMissing.enter();
            final RubyProc formatter = ExceptionOperations
                    .getFormatter(ExceptionOperations.NO_METHOD_ERROR, getContext());
            throw new RaiseException(getContext(), coreExceptions().noMethodErrorFromMethodMissing(
                    formatter,
                    receiver,
                    stringName,
                    arguments,
                    this));
        }

        return result;
    }

    protected String nameToString(Object methodName) {
        if (nameToString == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            nameToString = insert(NameToJavaStringNode.create());
        }
        return nameToString.execute(methodName);
    }

    protected Object callForeign(Object receiver, String methodName, RubyProc block, Object[] arguments) {
        if (callForeign == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callForeign = insert(CallForeignMethodNode.create());
        }
        return callForeign.execute(receiver, methodName, block, arguments);
    }

    protected Object callMethodMissingNode(
            VirtualFrame frame, Object receiver, RubyProc block, Object[] arguments) {
        if (callMethodMissing == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callMethodMissing = insert(DispatchNode.create(DispatchConfiguration.PRIVATE_RETURN_MISSING));
        }
        // NOTE(norswap, 24 Jul 2020): It's important to not pass a frame here in order to avoid looking up refinements,
        //   which should be ignored in the case of `method_missing`.
        //   cf. https://bugs.ruby-lang.org/issues/13129
        return callMethodMissing.execute(null, receiver, "method_missing", block, arguments);
    }

    private RubySymbol nameToSymbol(Object methodName) {
        if (methodName instanceof RubySymbol) {
            return (RubySymbol) methodName;
        } else if (RubyGuards.isRubyString(methodName)) {
            return getContext().getSymbol(((RubyString) methodName).rope);
        } else if (methodName instanceof String) {
            return getContext().getSymbol((String) methodName);
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }
    }

    /** This will be called from the {@link CallInternalMethodNode} child whenever it creates a new
     * {@link DirectCallNode}. */
    public final void applySplittingInliningStrategy(InternalMethod method, DirectCallNode callNode) {

        final Options options = getContext().getOptions();

        // The way that #method_missing is used is usually as an indirection to call some other method, and possibly to
        // modify the arguments. In both cases, but especially the latter, it makes a lot of sense to manually clone the
        // call target and to inline it.
        final boolean isMethodMissing = method.getName().equals("method_missing");

        if (callNode.isCallTargetCloningAllowed() &&
                (((RubyRootNode) method.getCallTarget().getRootNode()).shouldAlwaysClone() ||
                        isMethodMissing && options.METHODMISSING_ALWAYS_CLONE)) {
            callNode.cloneCallTarget();
        }

        if (callNode.isInlinable() &&
                ((sendingFrames() && options.INLINE_NEEDS_CALLER_FRAME) ||
                        isMethodMissing && options.METHODMISSING_ALWAYS_INLINE)) {
            callNode.forceInlining();
        }
    }

    private static class Uncached extends DispatchNode {

        static final Uncached[] uncachedNodes = new Uncached[DispatchConfiguration.values().length];
        static {
            for (DispatchConfiguration config : DispatchConfiguration.values()) {
                uncachedNodes[config.ordinal()] = new Uncached(config);
            }
        }

        protected Uncached(DispatchConfiguration config) {
            super(
                    config,
                    MetaClassNodeGen.getUncached(),
                    LookupMethodNodeGen.getUncached(),
                    CallInternalMethodNodeGen.getUncached(),
                    ConditionProfile.getUncached(),
                    ConditionProfile.getUncached(),
                    ConditionProfile.getUncached(),
                    BranchProfile.getUncached());
        }

        @Override
        public Object execute(VirtualFrame frame, Object receiver, Object methodName, RubyProc block,
                Object[] arguments) {
            return super.execute(null, receiver, methodName, block, arguments);
        }

        @Override
        protected String nameToString(Object methodName) {
            if (nameToString == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                nameToString = insert(NameToJavaStringNode.getUncached());
            }

            return nameToString.execute(methodName);
        }

        @Override
        protected Object callForeign(Object receiver, String methodName, RubyProc block, Object[] arguments) {
            if (callForeign == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callForeign = insert(CallForeignMethodNode.getUncached());
            }

            return callForeign.execute(receiver, methodName, block, arguments);
        }

        @Override
        protected Object callMethodMissingNode(
                VirtualFrame frame, Object receiver, RubyProc block, Object[] arguments) {
            if (callMethodMissing == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callMethodMissing = insert(
                        DispatchNode.getUncached(DispatchConfiguration.PRIVATE_RETURN_MISSING));
            }

            // null: see note in supermethod
            return callMethodMissing.execute(null, receiver, "method_missing", block, arguments);
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @Override
        public boolean isAdoptable() {
            return false;
        }
    }
}
