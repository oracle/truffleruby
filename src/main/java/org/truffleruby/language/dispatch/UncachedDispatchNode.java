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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.cast.ToSymbolNodeGen;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;

public class UncachedDispatchNode extends DispatchNode {

    private final boolean ignoreVisibility;
    private final MissingBehavior missingBehavior;

    @Child private IndirectCallNode indirectCallNode;
    @Child private ToSymbolNode toSymbolNode;
    @Child private NameToJavaStringNode toJavaStringNode;

    private final BranchProfile methodMissingProfile = BranchProfile.create();
    private final BranchProfile methodMissingNotFoundProfile = BranchProfile.create();

    public UncachedDispatchNode(boolean ignoreVisibility, DispatchAction dispatchAction, MissingBehavior missingBehavior) {
        super(dispatchAction);
        this.ignoreVisibility = ignoreVisibility;
        this.missingBehavior = missingBehavior;
        this.indirectCallNode = Truffle.getRuntime().createIndirectCallNode();
        this.toSymbolNode = ToSymbolNodeGen.create(null);
        this.toJavaStringNode = NameToJavaStringNode.create();
    }

    @Override
    protected boolean guard(Object methodName, Object receiver) {
        return true;
    }

    @Override
    public Object executeDispatch(
            VirtualFrame frame,
            Object receiverObject,
            Object name,
            DynamicObject blockObject,
            Object[] argumentsObjects) {
        final DispatchAction dispatchAction = getDispatchAction();

        final MethodLookupResult method = lookup(frame, receiverObject, toJavaStringNode.executeToJavaString(frame, name), ignoreVisibility);

        if (method.isDefined()) {
            if (dispatchAction == DispatchAction.CALL_METHOD) {
                return call(method.getMethod(), receiverObject, blockObject, argumentsObjects);
            } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return true;
            } else {
                throw new UnsupportedOperationException();
            }
        }

        if (dispatchAction == DispatchAction.CALL_METHOD && missingBehavior == MissingBehavior.RETURN_MISSING) {
            return DispatchNode.MISSING;
        }

        methodMissingProfile.enter();

        final MethodLookupResult methodMissing = lookup(frame, receiverObject, "method_missing", true);

        if (!methodMissing.isDefined()) {
            if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return false;
            } else {
                methodMissingNotFoundProfile.enter();
                throw new RaiseException(methodMissingNotFound(receiverObject));
            }
        }

        if (dispatchAction == DispatchAction.CALL_METHOD) {
            final DynamicObject nameSymbol = toSymbolNode.executeRubySymbol(frame, name);
            final Object[] modifiedArgumentsObjects = ArrayUtils.unshift(argumentsObjects, nameSymbol);

            return call(methodMissing.getMethod(), receiverObject, blockObject, modifiedArgumentsObjects);
        } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
            return false;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private Object call(InternalMethod method, Object receiverObject, DynamicObject blockObject, Object[] argumentsObjects) {
        return indirectCallNode.call(
                method.getCallTarget(),
                RubyArguments.pack(null, null, method, DeclarationContext.METHOD, null, receiverObject, blockObject, argumentsObjects));
    }

    @TruffleBoundary
    private DynamicObject methodMissingNotFound(Object receiverObject) {
        return coreExceptions().runtimeError(receiverObject.toString() + " didn't have a #method_missing", this);
    }

}