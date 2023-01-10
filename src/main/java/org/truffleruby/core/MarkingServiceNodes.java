/*
 * Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved. This
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

import java.util.ArrayList;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class MarkingServiceNodes {

    @GenerateUncached
    public abstract static class KeepAliveNode extends RubyBaseNode {

        public abstract void execute(ValueWrapper object);

        @Specialization(guards = "!stack.hasKeptObjects()")
        protected void keepFirstObject(ValueWrapper object,
                @Bind("getStack(object)") ExtensionCallStack stack) {
            stack.current.preservedObject = object;
        }

        @Specialization(guards = "stack.hasSingleKeptObject()")
        protected void keepCreatingList(ValueWrapper object,
                @Bind("getStack(object)") ExtensionCallStack stack,
                @Cached ConditionProfile sameObjectProfile) {
            if (sameObjectProfile.profile(object != stack.current.preservedObject)) {
                createKeptList(object, stack);
            }
        }

        @Specialization(guards = { "stack.hasKeptObjects()", "!stack.hasSingleKeptObject()" })
        @TruffleBoundary
        protected void keepAddingToList(ValueWrapper object,
                @Bind("getStack(object)") ExtensionCallStack stack) {
            stack.current.preservedObjects.add(object);
        }

        @TruffleBoundary
        private void createKeptList(ValueWrapper object, ExtensionCallStack stack) {
            stack.current.preservedObjects = new ArrayList<>();
            stack.current.preservedObjects.add(stack.current.preservedObject);
            stack.current.preservedObjects.add(object);
        }

        // We take a parameter so that the bind isn't considered cacheable.
        protected ExtensionCallStack getStack(ValueWrapper object) {
            return getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack;
        }
    }

    public static class QueueForMarkOnExitNode extends RubyBaseNode {

        public void execute(ValueWrapper object) {
            addToList(getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack, object);
        }

        protected void addToList(ExtensionCallStack stack, ValueWrapper object) {
            stack.markOnExitObject(object);
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

        @Specialization(guards = "stack.hasSingleMarkObject()")
        protected void markSingleObject(ExtensionCallStack stack,
                @Cached DispatchNode callNode) {
            ValueWrapper value = stack.getSingleMarkObject();
            callNode.call(getContext().getCoreLibrary().truffleCExtModule, "run_marker", value.getObject());
        }

        @TruffleBoundary
        @Specialization(guards = { "stack.hasMarkObjects()", "!stack.hasSingleMarkObject()" })
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
