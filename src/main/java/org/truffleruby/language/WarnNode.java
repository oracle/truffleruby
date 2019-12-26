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
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

/**
 * Warns if $VERBOSE is true or false, but not nil.
 * Corresponds to Kernel#warn(message, uplevel: 1), but in Java with a given SourceSection.
 */
public class WarnNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode warnMethod = CallDispatchHeadNode.createPrivate();
    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    public void warningMessage(SourceSection sourceSection, String message) {
        if (coreLibrary().warningsEnabled()) {
            callWarn(sourceSection, message);
        }
    }

    void callWarn(SourceSection sourceSection, String message) {
        final String warningMessage = buildWarningMessage(getContext(), sourceSection, message);
        final DynamicObject warningString = makeStringNode
                .executeMake(warningMessage, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        warnMethod.call(getContext().getCoreLibrary().kernelModule, "warn", warningString);
    }

    @TruffleBoundary
    private static String buildWarningMessage(RubyContext context, SourceSection sourceSection, String message) {
        final String sourceLocation = sourceSection != null ? context.fileLine(sourceSection) + ": " : "";
        return sourceLocation + "warning: " + message;
    }

}
