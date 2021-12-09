/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.RubyCheckArityRootNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.ReadArgumentsNode;
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

import static org.truffleruby.language.dispatch.DispatchNode.PUBLIC;

@NodeChild("descriptor")
public abstract class SymbolProcNode extends RubyContextSourceNode {

    private final String symbol;

    @Child private DispatchNode callNode;

    protected SymbolProcNode(String symbol) {
        this.symbol = symbol;
    }

    public abstract Object execute(VirtualFrame frame);

    @Specialization
    protected Object empty(VirtualFrame frame, EmptyKeywordDescriptor descriptor) {
        return checkArityAndDispatch(frame, descriptor);
    }

    @Specialization
    protected Object nonEmpty(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        return checkArityAndDispatch(frame, descriptor);
    }

    private Object checkArityAndDispatch(VirtualFrame frame, KeywordDescriptor descriptor) {
        final int given = RubyArguments.getArgumentsCount(frame, descriptor);

        final Arity dynamicArity = ((RubyCheckArityRootNode) getRootNode()).arityForCheck;
        if (!dynamicArity.check(given)) {
            ReadArgumentsNode.checkArityError(dynamicArity, given, this);
        }

        assert given >= 1 : "guaranteed from arity check";

        final Object receiver = RubyArguments.getArgument(frame, 0, descriptor);
        final Object[] arguments = ArrayUtils
                .extractRange(RubyArguments.getArguments(frame, descriptor), 1, given);
        final Object block = RubyArguments.getBlock(frame);

        return getCallNode().dispatch(frame, receiver, symbol, block, arguments);
    }

    private DispatchNode getCallNode() {
        if (callNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callNode = insert(DispatchNode.create(PUBLIC));
        }

        return callNode;
    }

}
