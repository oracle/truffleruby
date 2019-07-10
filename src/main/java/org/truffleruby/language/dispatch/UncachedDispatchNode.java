/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.cast.ToSymbolNodeGen;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.interop.OutgoingForeignCallNode;
import org.truffleruby.interop.OutgoingForeignCallNodeGen;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;
import org.truffleruby.language.objects.MetaClassNode;

public class UncachedDispatchNode extends DispatchNode {

    private final MissingBehavior missingBehavior;

    @Child private LookupMethodNode lookupMethodNode;
    @Child private LookupMethodNode lookupMethodMissingNode;
    @Child private IndirectCallNode indirectCallNode;
    @Child private ToSymbolNode toSymbolNode;
    @Child private NameToJavaStringNode nameToJavaStringNode;
    @Child private MetaClassNode metaClassNode;
    @Child private NotOptimizedWarningNode notOptimizedWarningNode = new NotOptimizedWarningNode();

    private final BranchProfile methodNotFoundProfile = BranchProfile.create();
    private final BranchProfile methodMissingProfile = BranchProfile.create();
    private final BranchProfile methodMissingNotFoundProfile = BranchProfile.create();
    private final BranchProfile foreignProfile = BranchProfile.create();

    public UncachedDispatchNode(boolean ignoreVisibility, boolean onlyCallPublic, DispatchAction dispatchAction, MissingBehavior missingBehavior) {
        super(dispatchAction);
        this.missingBehavior = missingBehavior;
        this.lookupMethodNode = LookupMethodNodeGen.create(ignoreVisibility, onlyCallPublic);
        this.lookupMethodMissingNode = LookupMethodNode.createIgnoreVisibility();
        this.indirectCallNode = Truffle.getRuntime().createIndirectCallNode();
        this.toSymbolNode = ToSymbolNodeGen.create();
        this.nameToJavaStringNode = NameToJavaStringNode.create();
        this.metaClassNode = dispatchAction == DispatchAction.CALL_METHOD ? MetaClassNode.create() : null;
    }

    @Override
    protected boolean guard(Object methodName, Object receiver) {
        return true;
    }

    @Override
    public Object executeDispatch(
            VirtualFrame frame,
            Object receiver,
            Object name,
            DynamicObject block,
            Object[] arguments) {

        final String methodName = nameToJavaStringNode.executeToJavaString(name);

        final DispatchAction dispatchAction = getDispatchAction();
        if (dispatchAction == DispatchAction.CALL_METHOD) {
            if (metaClassNode.executeMetaClass(receiver) == coreLibrary().getTruffleInteropForeignClass()) {
                foreignProfile.enter();
                notOptimizedWarningNode.warn("megamorphic dispatch on foreign object");
                return megamorphicForeignCall(receiver, arguments, methodName);
            }
        } else {
            assert !RubyGuards.isForeignObject(receiver) : "RESPOND_TO_METHOD not supported on foreign objects";
        }

        final InternalMethod method = lookupMethodNode.executeLookupMethod(frame, receiver, methodName);

        if (method != null) {
            if (dispatchAction == DispatchAction.CALL_METHOD) {
                return call(method, receiver, block, arguments);
            } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return !method.isUnimplemented();
            } else {
                throw new UnsupportedOperationException();
            }
        }

        methodNotFoundProfile.enter();

        if (dispatchAction == DispatchAction.CALL_METHOD && missingBehavior == MissingBehavior.RETURN_MISSING) {
            return DispatchNode.MISSING;
        }

        methodMissingProfile.enter();

        final InternalMethod methodMissing = lookupMethodMissingNode.executeLookupMethod(frame, receiver, "method_missing");

        if (methodMissing == null) {
            if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return false;
            } else {
                methodMissingNotFoundProfile.enter();
                final DynamicObject formatter = ExceptionOperations.getFormatter(ExceptionOperations.NO_METHOD_ERROR, getContext());
                throw new RaiseException(getContext(), coreExceptions().noMethodErrorFromMethodMissing(
                        formatter, receiver, methodName, arguments, this));
            }
        }

        if (dispatchAction == DispatchAction.CALL_METHOD) {
            final DynamicObject nameSymbol = toSymbolNode.executeToSymbol(name);
            final Object[] modifiedArgumentsObjects = ArrayUtils.unshift(arguments, nameSymbol);

            return call(methodMissing, receiver, block, modifiedArgumentsObjects);
        } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
            return false;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @TruffleBoundary
    private Object megamorphicForeignCall(Object receiver, Object[] arguments, String methodName) {
        return createOutgoingForeignCallNode(methodName).executeCall((TruffleObject) receiver, arguments);
    }

    @TruffleBoundary
    private OutgoingForeignCallNode createOutgoingForeignCallNode(String methodName) {
        return OutgoingForeignCallNodeGen.create(methodName);
    }

    private Object call(InternalMethod method, Object receiverObject, DynamicObject blockObject, Object[] argumentsObjects) {
        return indirectCallNode.call(
                method.getCallTarget(),
                RubyArguments.pack(null, null, method, null, receiverObject, blockObject, argumentsObjects));
    }

}
