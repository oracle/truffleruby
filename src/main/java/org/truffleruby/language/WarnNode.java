/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.NodeCost;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.globals.ReadSimpleGlobalVariableNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.SourceSection;


/** Warns if $VERBOSE is true or false, but not nil. Corresponds to Kernel#warn(message, uplevel: 1), but in Java with a
 * given SourceSection. */
public class WarnNode extends RubyBaseNode {

    @Child protected ReadSimpleGlobalVariableNode readVerboseNode = ReadSimpleGlobalVariableNode.create("$VERBOSE");

    @Child private DispatchNode callWarnNode;
    @Child private MakeStringNode makeStringNode;

    public boolean shouldWarn() {
        final Object verbosity = readVerboseNode.execute();
        return verbosity != nil;
    }

    public final boolean shouldWarnForDeprecation() {
        return shouldWarn() && getContext().getWarningCategoryDeprecated().get();
    }

    /** Must only be called if {@link #shouldWarn()} or {@link #shouldWarnForDeprecation()} is true, in order to avoid
     * computing a SourceSection and message if not needed. */
    public void warningMessage(SourceSection sourceSection, String message) {
        assert shouldWarn();

        if (makeStringNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            makeStringNode = insert(MakeStringNode.create());
        }
        if (callWarnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callWarnNode = insert(DispatchNode.create());
        }

        callWarn(getContext(), sourceSection, message, makeStringNode, callWarnNode);
    }

    static void callWarn(RubyContext context, SourceSection sourceSection, String message,
            MakeStringNode makeStringNode, DispatchNode callWarnNode) {
        final String warningMessage = buildWarningMessage(sourceSection, message);

        final RubyString warningString = makeStringNode
                .executeMake(warningMessage, Encodings.UTF_8, CodeRange.CR_UNKNOWN);

        callWarnNode.call(context.getCoreLibrary().kernelModule, "warn", warningString);
    }

    @TruffleBoundary
    private static String buildWarningMessage(SourceSection sourceSection, String message) {
        final String sourceLocation = sourceSection != null ? RubyLanguage.fileLine(sourceSection) + ": " : "";
        return sourceLocation + "warning: " + message;
    }

    abstract static class AbstractUncachedWarnNode extends RubyBaseNode {

        public abstract boolean shouldWarn();

        public void warningMessage(SourceSection sourceSection, String message) {
            assert shouldWarn();
            WarnNode.callWarn(
                    getContext(),
                    sourceSection,
                    message,
                    MakeStringNode.getUncached(),
                    DispatchNode.getUncached());
        }

        @Override
        public NodeCost getCost() {
            return NodeCost.MEGAMORPHIC;
        }

        @Override
        public boolean isAdoptable() {
            return false;
        }

    }

    @DenyReplace
    public static final class UncachedWarnNode extends AbstractUncachedWarnNode {

        public static final UncachedWarnNode INSTANCE = new UncachedWarnNode();

        UncachedWarnNode() {
        }

        public boolean shouldWarn() {
            return coreLibrary().warningsEnabled();
        }
    }

}
