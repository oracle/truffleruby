/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class MarkingServiceNodes {

    public static class KeepAliveNode extends RubyBaseNode {

        public void execute(ValueWrapper object) {
            addToList(getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack, object);
        }

        @TruffleBoundary
        protected void addToList(ExtensionCallStack stack, ValueWrapper object) {
            stack.getKeptObjects().add(object);
        }

        public static KeepAliveNode create() {
            return new KeepAliveNode();
        }

        public static KeepAliveNode getUncached() {
            return new KeepAliveNode();
        }
    }

    public static class QueueForMarkOnExitNode extends RubyBaseNode {

        public void execute(ValueWrapper object) {
            addToList(getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack, object);
        }

        @TruffleBoundary
        protected void addToList(ExtensionCallStack stack, ValueWrapper object) {
            stack.getMarkOnExitObjects().add(object);
        }

        public static QueueForMarkOnExitNode create() {
            return new QueueForMarkOnExitNode();
        }
    }

    public abstract static class RunMarkOnExitNode extends RubyBaseNode {

        public abstract void execute(ExtensionCallStack stack);

        @Specialization(guards = "!stack.hasMarkObjects()")
        protected void nothingToMark(ExtensionCallStack stack) {
            // Do nothing.
        }

        @TruffleBoundary
        @Specialization(guards = "stack.hasMarkObjects()")
        protected void marksToRun(ExtensionCallStack stack,
                @Cached DispatchNode callNode) {
            // Run the markers...
            var valuesForMarking = stack.getMarkOnExitObjects();
            // Push a new stack frame because we should
            // mutate the list while iterating, and we
            // don't know what the mark routine might do.
            stack.push(false, nil, nil);
            try {
                for (var value : valuesForMarking) {
                    callNode.call(getContext().getCoreLibrary().truffleCExtModule, "run_marker", value.getObject());
                }
            } finally {
                stack.pop();
            }
        }


        public static RunMarkOnExitNode create() {
            return MarkingServiceNodesFactory.RunMarkOnExitNodeGen.create();
        }
    }
}
