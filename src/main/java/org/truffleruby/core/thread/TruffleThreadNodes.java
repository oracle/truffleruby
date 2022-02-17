/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.language.arguments.CallerDataReadingNode;
import org.truffleruby.language.FrameAndVariablesSendingNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;

@CoreModule("Truffle::ThreadOperations")
public class TruffleThreadNodes {

    private static class FrameAndCallNode {

        public final Frame frame;
        public final Node callNode;

        public FrameAndCallNode(Frame frame, Node callNode) {
            this.frame = frame;
            this.callNode = callNode;
        }
    }

    @CoreMethod(names = "ruby_caller_special_variables", onSingleton = true, required = 1)
    @ImportStatic(ArrayGuards.class)
    public abstract static class FindRubyCallerSpecialStorage extends CoreMethodArrayArgumentsNode
            implements CallerDataReadingNode {

        @TruffleBoundary
        @Specialization(limit = "storageStrategyLimit()")
        protected Object findRubyCaller(RubyArray modules,
                @Bind("modules.store") Object store,
                @CachedLibrary("store") ArrayStoreLibrary stores,
                @Cached GetSpecialVariableStorage storageNode) {
            final int modulesSize = modules.size;
            Object[] moduleArray = stores.boxedCopyOfRange(store, 0, modulesSize);
            FrameAndCallNode data = getContext()
                    .getCallStack()
                    .iterateFrameNotInModules(
                            moduleArray,
                            f -> new FrameAndCallNode(f.getFrame(FrameAccess.MATERIALIZE), f.getCallNode()));
            if (data == null) {
                return nil;
            } else {
                CallerDataReadingNode.notifyCallerToSendData(getContext(), data.callNode, this);
                Object variables = storageNode.execute(data.frame.materialize());
                getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack.setSpecialVariables(variables);
                return variables;
            }
        }

        public void startSending(FrameAndVariablesSendingNode node) {
            node.startSendingOwnVariables();
        }
    }
}
