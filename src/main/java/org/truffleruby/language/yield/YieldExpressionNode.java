/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.yield;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.array.ArrayToObjectArrayNodeGen;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptorManager;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.language.dispatch.LiteralCallNode;

public class YieldExpressionNode extends LiteralCallNode {

    @Children private final RubyNode[] arguments;
    @Child private CallBlockNode yieldNode;
    @Child private ArrayToObjectArrayNode unsplatNode;
    @Child private RubyNode readBlockNode;

    public YieldExpressionNode(
            boolean isSplatted,
            ArgumentsDescriptor descriptor,
            RubyNode[] arguments,
            RubyNode readBlockNode) {
        super(isSplatted, descriptor);
        this.arguments = arguments;
        this.readBlockNode = readBlockNode;
    }

    @ExplodeLoop
    @Override
    public final Object execute(VirtualFrame frame) {
        Object[] argumentsObjects = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            argumentsObjects[i] = arguments[i].execute(frame);
        }

        final Object maybeBlock = readBlockNode.execute(frame);
        if (maybeBlock == nil) {
            throw new RaiseException(getContext(), coreExceptions().noBlockToYieldTo(this));
        }

        final RubyProc block = (RubyProc) maybeBlock;

        ArgumentsDescriptor descriptor = this.descriptor;
        boolean ruby2KeywordsHash = false;
        if (isSplatted) {
            argumentsObjects = unsplat(argumentsObjects);
            ruby2KeywordsHash = isRuby2KeywordsHash(argumentsObjects, argumentsObjects.length);
            if (ruby2KeywordsHash) {
                descriptor = KeywordArgumentsDescriptorManager.EMPTY;
            }
        }

        // Remove empty kwargs in the caller, so the callee does not need to care about this special case
        if (descriptor instanceof KeywordArgumentsDescriptor && emptyKeywordArguments(argumentsObjects)) {
            argumentsObjects = removeEmptyKeywordArguments(argumentsObjects);
            descriptor = EmptyArgumentsDescriptor.INSTANCE;
        }

        return getYieldNode().yield(block, descriptor, argumentsObjects, ruby2KeywordsHash ? this : null);
    }

    private Object[] unsplat(Object[] argumentsObjects) {
        if (unsplatNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            unsplatNode = insert(ArrayToObjectArrayNodeGen.create());
        }
        return unsplatNode.unsplat(argumentsObjects);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        Object block = readBlockNode.execute(frame);
        if (block == nil) {
            return nil;
        } else {
            return FrozenStrings.YIELD;
        }
    }

    private CallBlockNode getYieldNode() {
        if (yieldNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            yieldNode = insert(CallBlockNode.create());
        }

        return yieldNode;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new YieldExpressionNode(
                isSplatted,
                descriptor,
                cloneUninitialized(arguments),
                readBlockNode.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
