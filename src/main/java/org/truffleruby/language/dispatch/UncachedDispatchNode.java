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

import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.cast.ToSymbolNodeGen;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;

public class UncachedDispatchNode extends DispatchNode {

    protected final boolean onlyCallPublic;
    private final MissingBehavior missingBehavior;

    @Child private LookupMethodNode lookupMethodNode;
    @Child private LookupMethodNode lookupMethodMissingNode;
    @Child private IndirectCallNode indirectCallNode;
    @Child private ToSymbolNode toSymbolNode;
    @Child private NameToJavaStringNode nameToJavaStringNode;

    private final BranchProfile methodNotFoundProfile = BranchProfile.create();
    private final BranchProfile methodMissingProfile = BranchProfile.create();
    private final BranchProfile methodMissingNotFoundProfile = BranchProfile.create();

    public UncachedDispatchNode(boolean ignoreVisibility, boolean onlyCallPublic, DispatchAction dispatchAction, MissingBehavior missingBehavior) {
        super(dispatchAction);
        this.onlyCallPublic = onlyCallPublic;
        this.missingBehavior = missingBehavior;
        this.lookupMethodNode = LookupMethodNodeGen.create(ignoreVisibility, onlyCallPublic, null, null);
        this.lookupMethodMissingNode = LookupMethodNode.createIgnoreVisibility();
        this.indirectCallNode = Truffle.getRuntime().createIndirectCallNode();
        this.toSymbolNode = ToSymbolNodeGen.create(null);
        this.nameToJavaStringNode = NameToJavaStringNode.create();
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
        assert !RubyGuards.isForeignObject(receiver) : "uncached dispatch not supported on foreign objects";

        final DispatchAction dispatchAction = getDispatchAction();

        final String methodName = nameToJavaStringNode.executeToJavaString(name);
        final InternalMethod method = lookupMethodNode.executeLookupMethod(frame, receiver, methodName);

        if (method != null) {
            if (dispatchAction == DispatchAction.CALL_METHOD) {
                return call(method, receiver, block, arguments);
            } else if (dispatchAction == DispatchAction.RESPOND_TO_METHOD) {
                return true;
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
                throw new RaiseException(coreExceptions().noMethodErrorOnReceiver(methodName, receiver, arguments, this));
            }
        }

        if (dispatchAction == DispatchAction.CALL_METHOD) {
            final DynamicObject nameSymbol = toSymbolNode.executeRubySymbol(frame, name);
            final Object[] modifiedArgumentsObjects = ArrayUtils.unshift(arguments, nameSymbol);

            return call(methodMissing, receiver, block, modifiedArgumentsObjects);
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

}
