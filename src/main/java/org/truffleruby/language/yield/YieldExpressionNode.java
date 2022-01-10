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
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;

public class YieldExpressionNode extends RubyContextSourceNode {

    private final boolean unsplat;
    private final boolean warnInModuleBody;

    @Children private final RubyNode[] arguments;
    @Child private CallBlockNode yieldNode;
    @Child private ArrayToObjectArrayNode unsplatNode;
    @Child private RubyNode readBlockNode;
    @Child private WarnNode warnNode;

    private final BranchProfile useCapturedBlock = BranchProfile.create();
    private final BranchProfile noCapturedBlock = BranchProfile.create();

    public YieldExpressionNode(
            boolean unsplat,
            RubyNode[] arguments,
            RubyNode readBlockNode,
            boolean warnInModuleBody) {
        this.unsplat = unsplat;
        this.arguments = arguments;
        this.readBlockNode = readBlockNode;
        this.warnInModuleBody = warnInModuleBody;
    }

    @ExplodeLoop
    @Override
    public final Object execute(VirtualFrame frame) {
        if (warnInModuleBody) {
            warnInModuleBody();
        }

        Object[] argumentsObjects = new Object[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            argumentsObjects[i] = arguments[i].execute(frame);
        }

        Object block = readBlock(frame);

        if (block == nil) {
            noCapturedBlock.enter();
            throw new RaiseException(getContext(), coreExceptions().noBlockToYieldTo(this));
        }

        if (unsplat) {
            argumentsObjects = unsplat(argumentsObjects);
        }

        return getYieldNode().yield((RubyProc) block, argumentsObjects);
    }

    private Object readBlock(VirtualFrame frame) {
        Object block = readBlockNode.execute(frame);

        if (block == nil) {
            useCapturedBlock.enter();
            block = RubyArguments.getMethod(frame).getCapturedBlock();
        }
        return block;
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
        Object block = readBlock(frame);
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

    private void warnInModuleBody() {
        if (warnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnNode = insert(new WarnNode());
        }
        if (warnNode.shouldWarnForDeprecation()) {
            warnNode.warningMessage(
                    getSourceSection(),
                    "`yield' in class syntax will not be supported from Ruby 3.0. [Feature #15575]");
        }
    }
}
