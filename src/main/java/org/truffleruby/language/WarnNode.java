/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
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
public class WarnNode extends RubyContextNode {

    @Child protected ReadSimpleGlobalVariableNode readVerboseNode = ReadSimpleGlobalVariableNode.create("$VERBOSE");

    @Child private DispatchNode callWarnNode;
    @Child private MakeStringNode makeStringNode;

    public boolean shouldWarn() {
        final Object verbosity = readVerboseNode.execute();
        return verbosity != nil;
    }

    /** Must only be called if {@link #shouldWarn()} is true, in order to avoid computing a SourceSection and message if
     * not needed. */
    public void warningMessage(SourceSection sourceSection, String message) {
        assert shouldWarn();
        callWarn(sourceSection, message);
    }

    void callWarn(SourceSection sourceSection, String message) {
        final String warningMessage = buildWarningMessage(sourceSection, message);

        if (makeStringNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            makeStringNode = insert(MakeStringNode.create());
        }
        final RubyString warningString = makeStringNode
                .executeMake(warningMessage, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);

        if (callWarnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callWarnNode = insert(DispatchNode.create());
        }
        callWarnNode.call(getContext().getCoreLibrary().kernelModule, "warn", warningString);
    }

    @TruffleBoundary
    private static String buildWarningMessage(SourceSection sourceSection, String message) {
        final String sourceLocation = sourceSection != null ? RubyContext.fileLine(sourceSection) + ": " : "";
        return sourceLocation + "warning: " + message;
    }

}
