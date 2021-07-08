/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.inlined.InlinedDispatchNode;
import org.truffleruby.core.inlined.InlinedMethodNode;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyContextSourceNode;
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

    public static void trace(RubyDynamicObject instance, RubyContextNode node) {
        traceObject(node.getLanguage(), node.getContext(), instance, node);
    }

    public static void trace(RubyDynamicObject instance, RubyContextSourceNode node) {
        traceObject(node.getLanguage(), node.getContext(), instance, node);
    }

    public static void trace(ImmutableRubyObject instance, RubyContextSourceNode node) {
        traceObject(node.getLanguage(), node.getContext(), instance, node);
    }

    public static void trace(RubyLanguage language, RubyContext context, RubyDynamicObject instance, Node node) {
        traceObject(language, context, instance, node);
    }

    private static void traceObject(RubyLanguage language, RubyContext context, Object instance, Node currentNode) {
        truffleTracing(language, instance);

        if (context.getObjectSpaceManager().isTracing(language)) {
            traceBoundary(language, context, instance, currentNode);
        }
    }

    public static void traceInlined(RubyDynamicObject instance, String className, String allocatingMethod,
            InlinedMethodNode node) {
        final RubyLanguage language = node.getLanguage();
        final RubyContext context = node.getContext();

        truffleTracing(language, instance);

        if (context.getObjectSpaceManager().isTracing(language)) {
            if (!(node.getParent() instanceof InlinedDispatchNode)) {
                traceBoundary(language, context, instance, node);
            } else {
                traceInlineBoundary(language, context, instance, className, allocatingMethod, node);
            }
        }
    }

    private static void truffleTracing(RubyLanguage language, Object instance) {
        CompilerAsserts.partialEvaluationConstant(language);

        final AllocationReporter allocationReporter = language.getAllocationReporter();
        if (allocationReporter.isActive()) {
            allocationReporter.onEnter(null, 0, AllocationReporter.SIZE_UNKNOWN);
            allocationReporter.onReturnValue(instance, 0, AllocationReporter.SIZE_UNKNOWN);
        }
    }

    @TruffleBoundary
    private static void traceBoundary(RubyLanguage language, RubyContext context, Object object, Node currentNode) {
        final ObjectSpaceManager objectSpaceManager = context.getObjectSpaceManager();
        if (!objectSpaceManager.isTracingPaused()) {
            objectSpaceManager.setTracingPaused(true);
            try {
                callTraceAllocation(language, context, object, currentNode);
            } finally {
                objectSpaceManager.setTracingPaused(false);
            }
        }
    }

    @TruffleBoundary
    private static void traceInlineBoundary(RubyLanguage language, RubyContext context, RubyDynamicObject instance,
            String className, String allocatingMethod, RubyContextSourceNode node) {
        final ObjectSpaceManager objectSpaceManager = context.getObjectSpaceManager();
        if (!objectSpaceManager.isTracingPaused()) {
            objectSpaceManager.setTracingPaused(true);
            try {
                callTraceInlineAllocation(language, context, instance, className, allocatingMethod, node);
            } finally {
                objectSpaceManager.setTracingPaused(false);
            }
        }
    }

    @TruffleBoundary
    private static void callTraceAllocation(RubyLanguage language, RubyContext context, Object object,
            Node currentNode) {
        final SourceSection allocatingSourceSection = context
                .getCallStack()
                .getTopMostUserSourceSection(currentNode.getEncapsulatingSourceSection());

        final Frame allocatingFrame = context.getCallStack().getCurrentFrame(FrameAccess.READ_ONLY);

        final Object allocatingSelf = RubyArguments.getSelf(allocatingFrame);
        final String allocatingMethod = RubyArguments.getMethod(allocatingFrame).getName();
        final String className = LogicalClassNode.getUncached().execute(allocatingSelf).fields.getName();

        callAllocationTrace(language, context, object, allocatingSourceSection, className, allocatingMethod);
    }

    @TruffleBoundary
    private static void callTraceInlineAllocation(RubyLanguage language, RubyContext context,
            RubyDynamicObject instance, String className, String allocatingMethod, RubyContextSourceNode node) {
        final SourceSection allocatingSourceSection = context
                .getCallStack()
                .getTopMostUserSourceSection(node.getEncapsulatingSourceSection());
        callAllocationTrace(language, context, instance, allocatingSourceSection, className, allocatingMethod);
    }

    private static void callAllocationTrace(RubyLanguage language, RubyContext context, Object object,
            SourceSection allocatingSourceSection, String className, String allocatingMethod) {
        RubyContext.send(
                context.getCoreLibrary().objectSpaceModule,
                "trace_allocation",
                object,
                string(context, language, className),
                language.getSymbol(allocatingMethod),
                string(context, language, language.getSourcePath(allocatingSourceSection.getSource())),
                allocatingSourceSection.getStartLine(),
                ObjectSpaceManager.getCollectionCount());
    }

    private static RubyString string(RubyContext context, RubyLanguage language, String value) {
        // No point to use MakeStringNode (which uses AllocateObjectNode) here, as we should not
        // trace the allocation of Strings used for tracing allocations.
        return StringOperations
                .createUTF8String(context, language, StringOperations.encodeRope(value, UTF8Encoding.INSTANCE));
    }

}
