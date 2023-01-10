/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.string.RubyString;
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
    @Child private TruffleString.FromJavaStringNode fromJavaStringNode;

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

        if (fromJavaStringNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            fromJavaStringNode = insert(TruffleString.FromJavaStringNode.create());
        }
        if (callWarnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callWarnNode = insert(DispatchNode.create());
        }

        callWarn(getContext(), sourceSection, message, this, fromJavaStringNode, callWarnNode);
    }

    static void callWarn(RubyContext context, SourceSection sourceSection, String message, RubyBaseNode node,
            TruffleString.FromJavaStringNode fromJavaStringNode, DispatchNode callWarnNode) {
        final String warningMessage = buildWarningMessage(context, sourceSection, message);
        final RubyString warningString = node.createString(fromJavaStringNode, warningMessage, Encodings.UTF_8);

        callWarnNode.call(context.getCoreLibrary().kernelModule, "warn", warningString);
    }

    @TruffleBoundary
    private static String buildWarningMessage(RubyContext context, SourceSection sourceSection, String message) {
        final String sourceLocation = sourceSection != null ? context.fileLine(sourceSection) + ": " : "";
        return sourceLocation + "warning: " + message;
    }

    abstract static class AbstractUncachedWarnNode extends RubyBaseNode {

        public abstract boolean shouldWarn();

        public void warningMessage(SourceSection sourceSection, String message) {
            assert shouldWarn();
            WarnNode.callWarn(
                    getContext(), sourceSection, message, this,
                    TruffleString.FromJavaStringNode.getUncached(),
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
