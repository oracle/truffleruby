/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.SourceSection;

public abstract class AllocationTracing {

    public static void trace(RubyLanguage language, RubyContext context, RubyDynamicObject instance, Node currentNode) {
        CompilerAsserts.partialEvaluationConstant(language);

        final AllocationReporter allocationReporter = language.getAllocationReporter();
        if (allocationReporter.isActive()) {
            allocationReporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
            allocationReporter.onReturnValue(instance, 0, AllocationReporter.SIZE_UNKNOWN);
        }

        if (context.getObjectSpaceManager().isTracing(language)) {
            traceBoundary(context, instance, currentNode);
        }
    }

    @TruffleBoundary
    private static void traceBoundary(RubyContext context, RubyDynamicObject object, Node currentNode) {
        final ObjectSpaceManager objectSpaceManager = context.getObjectSpaceManager();
        if (!objectSpaceManager.isTracingPaused()) {
            objectSpaceManager.setTracingPaused(true);
            try {
                callTraceAllocation(context, object, currentNode);
            } finally {
                objectSpaceManager.setTracingPaused(false);
            }
        }
    }

    @TruffleBoundary
    private static void callTraceAllocation(RubyContext context, RubyDynamicObject object, Node currentNode) {
        final SourceSection allocatingSourceSection = context
                .getCallStack()
                .getTopMostUserSourceSection(currentNode.getEncapsulatingSourceSection());

        final Frame allocatingFrame = context.getCallStack().getCurrentFrame(FrameAccess.READ_ONLY);

        final Object allocatingSelf = RubyArguments.getSelf(allocatingFrame);
        final String allocatingMethod = RubyArguments.getMethod(allocatingFrame).getName();
        final String className = context.getCoreLibrary().getLogicalClass(allocatingSelf).fields
                .getName();

        context.send(
                context.getCoreLibrary().objectSpaceModule,
                "trace_allocation",
                object,
                string(context, className),
                context.getSymbol(allocatingMethod),
                string(context, context.getSourcePath(allocatingSourceSection.getSource())),
                allocatingSourceSection.getStartLine(),
                ObjectSpaceManager.getCollectionCount());
    }

    private static RubyString string(RubyContext context, String value) {
        // No point to use MakeStringNode (which uses AllocateObjectNode) here, as we should not
        // trace the allocation of Strings used for tracing allocations.
        return StringOperations.createString(context, StringOperations.encodeRope(value, UTF8Encoding.INSTANCE));
    }

}
