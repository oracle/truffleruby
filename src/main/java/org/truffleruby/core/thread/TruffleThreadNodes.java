/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.MarkingServiceNodes;
import org.truffleruby.language.FrameAndVariablesSendingNode;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
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
    public abstract static class FindRubyCallerSpecialStorage extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(limit = "storageStrategyLimit()")
        protected Object findRubyCaller(RubyArray modules,
                @CachedLibrary("modules.store") ArrayStoreLibrary stores,
                @Cached GetSpecialVariableStorage storageNode,
                @Cached MarkingServiceNodes.GetMarkerThreadLocalDataNode getDataNode) {
            final int modulesSize = modules.size;
            Object[] moduleArray = stores.boxedCopyOfRange(modules.store, 0, modulesSize);
            FrameAndCallNode data = getContext()
                    .getCallStack()
                    .iterateFrameNotInModules(
                            moduleArray,
                            f -> new FrameAndCallNode(f.getFrame(FrameAccess.MATERIALIZE), f.getCallNode()));
            if (data == null) {
                return nil;
            } else {
                notifyToStartSendingStorage(data.callNode);
                Object variables = storageNode.execute(data.frame.materialize());
                getDataNode.execute().getExtensionCallStack().setVariables(variables);
                return variables;
            }
        }

        private static boolean notifyToStartSendingStorage(Node callerNode) {
            if (callerNode instanceof DirectCallNode || callerNode instanceof IndirectCallNode) {
                Node parent = callerNode.getParent();
                while (parent != null) {
                    if (parent instanceof FrameAndVariablesSendingNode) {
                        ((FrameAndVariablesSendingNode) parent).startSendingOwnVariables();
                        return true;
                    }
                    if (parent instanceof RubyContextNode) {
                        return false;
                    }
                    parent = parent.getParent();
                }
            }

            return false;
        }
    }
}
