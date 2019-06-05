/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.ShapeCachingGuards;

public final class UnresolvedDispatchNode extends DispatchNode {

    private int depth = 0;

    private final boolean ignoreVisibility;
    private final boolean onlyCallPublic;
    private final MissingBehavior missingBehavior;

    public UnresolvedDispatchNode(
            boolean ignoreVisibility,
            boolean onlyCallPublic,
            MissingBehavior missingBehavior,
            DispatchAction dispatchAction) {
        super(dispatchAction);
        this.ignoreVisibility = ignoreVisibility;
        this.onlyCallPublic = onlyCallPublic;
        this.missingBehavior = missingBehavior;
    }

    @Override
    protected boolean guard(Object methodName, Object receiver) {
        return false;
    }

    @Override
    public Object executeDispatch(
            final VirtualFrame frame,
            final Object receiverObject,
            final Object methodName,
            DynamicObject blockObject,
            final Object[] argumentsObjects) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        // Useful debug aid to catch a running-away NotProvided or undefined
        assert !(receiverObject instanceof NotProvided) : getContext().fileLine(getEncapsulatingSourceSection());

        // Make sure to have an up-to-date Shape.
        if (receiverObject instanceof DynamicObject) {
            ShapeCachingGuards.updateShape((DynamicObject) receiverObject);
        }

        final DispatchNode dispatch = atomic(() -> {
            final DispatchNode first = getHeadNode().getFirstDispatchNode();

            // First try to see if we did not a miss a specialization added by another thread.

            DispatchNode lookupDispatch = first;
            while (lookupDispatch != null) {
                if (lookupDispatch.guard(methodName, receiverObject)) {
                    // This one worked, no need to rewrite anything.
                    return lookupDispatch;
                }
                lookupDispatch = lookupDispatch.getNext();
            }

            // We need a new node to handle this case.

            final DispatchNode newDispatchNode;

            if (depth == getContext().getOptions().DISPATCH_CACHE) {
                newDispatchNode = new UncachedDispatchNode(ignoreVisibility, onlyCallPublic, getDispatchAction(), missingBehavior);
            } else {
                depth++;
                if (depth >= 2) {
                    this.reportPolymorphicSpecialize();
                }
                final String methodNameString = methodNameToString(methodName);
                if (RubyGuards.isForeignObject(receiverObject)) {
                    switch (getDispatchAction()) {
                        case CALL_METHOD:
                            newDispatchNode = new CachedForeignDispatchNode(getContext(), first, methodNameString);
                            break;
                        case RESPOND_TO_METHOD:
                            throw new UnsupportedOperationException();
                        default:
                            throw new UnsupportedOperationException();
                    }
                } else if (RubyGuards.isRubyBasicObject(receiverObject)) {
                    newDispatchNode = doDynamicObject(frame, first, receiverObject, methodName, methodNameString, argumentsObjects);
                } else {
                    newDispatchNode = doUnboxedObject(frame, first, receiverObject, methodName, methodNameString, argumentsObjects);
                }
            }

            first.replace(newDispatchNode);

            return newDispatchNode;
        });

        // Trigger splitting outside the atomic() block as splitting needs the atomic() lock
        // but potentially also other locks/monitors which could trigger a deadlock.
        if (dispatch instanceof CachedDispatchNode) {
            ((CachedDispatchNode) dispatch).reassessSplittingInliningStrategy();
        }

        return dispatch.executeDispatch(frame, receiverObject, methodName, blockObject, argumentsObjects);
    }

    private CachedDispatchNode doUnboxedObject(
            VirtualFrame frame,
            DispatchNode first,
            Object receiverObject,
            Object methodName,
            String methodNameString,
            Object[] argumentsObjects) {

        final MethodLookupResult method = lookup(frame, receiverObject, methodNameString, ignoreVisibility, onlyCallPublic);

        if (!method.isDefined()) {
            return createMethodMissingNode(first, methodName, methodNameString, receiverObject, method, argumentsObjects);
        }

        if (receiverObject instanceof Boolean) {
            final MethodLookupResult falseMethodLookup = lookup(frame, false, methodNameString, ignoreVisibility, onlyCallPublic);
            final MethodLookupResult trueMethodLookup = lookup(frame, true, methodNameString, ignoreVisibility, onlyCallPublic);
            assert falseMethodLookup.isDefined() || trueMethodLookup.isDefined();

            return new CachedBooleanDispatchNode(
                    getContext(), methodName, first,
                    falseMethodLookup, trueMethodLookup,
                    getDispatchAction());
        } else {
            return new CachedUnboxedDispatchNode(
                    getContext(), methodName, first, receiverObject.getClass(),
                    method, getDispatchAction());
        }
    }

    private CachedDispatchNode doDynamicObject(
            VirtualFrame frame,
            DispatchNode first,
            Object receiverObject,
            Object methodName,
            String methodNameString,
            Object[] argumentsObjects) {

        final MethodLookupResult method = lookup(frame, receiverObject, methodNameString, ignoreVisibility, onlyCallPublic);

        if (!method.isDefined()) {
            return createMethodMissingNode(first, methodName, methodNameString, receiverObject, method, argumentsObjects);
        }

        if (RubyGuards.isRubySymbol(receiverObject)) {
            return new CachedBoxedSymbolDispatchNode(getContext(), methodName, first, method, getDispatchAction());
        } else if (Layouts.CLASS.getIsSingleton(coreLibrary().getMetaClass(receiverObject))) {
            return new CachedSingletonDispatchNode(getContext(), methodName, first, ((DynamicObject) receiverObject),
                    method, getDispatchAction());
        } else {
            return new CachedBoxedDispatchNode(getContext(), methodName, first, ((DynamicObject) receiverObject).getShape(),
                    method, getDispatchAction());
        }
    }

    private CachedDispatchNode createMethodMissingNode(
            DispatchNode first,
            Object methodName,
            String methodNameString,
            Object receiverObject,
            MethodLookupResult methodLookup,
            Object[] argumentsObjects) {
        switch (missingBehavior) {
            case RETURN_MISSING: {
                return new CachedReturnMissingDispatchNode(getContext(), methodName, first, methodLookup, coreLibrary().getMetaClass(receiverObject),
                        getDispatchAction());
            }

            case CALL_METHOD_MISSING: {
                final MethodLookupResult methodMissing = lookup(null, receiverObject, "method_missing", true, false);

                if (!methodMissing.isDefined()) {
                    final DynamicObject formatter = ExceptionOperations.getFormatter(ExceptionOperations.NO_METHOD_ERROR, getContext());
                    throw new RaiseException(getContext(), coreExceptions().noMethodErrorFromMethodMissing(
                            formatter, receiverObject, methodNameString, argumentsObjects, this));
                }

                return new CachedMethodMissingDispatchNode(getContext(), methodName, first, coreLibrary().getMetaClass(receiverObject),
                        methodLookup, methodMissing, getDispatchAction());
            }

            default: {
                throw new UnsupportedOperationException(missingBehavior.toString());
            }
        }
    }

}
