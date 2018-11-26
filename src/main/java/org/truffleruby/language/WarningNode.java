/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

/**
 * Warns only if $VERBOSE is true.
 * Corresponds to Truffle::Warnings.warning, but in Java with a given SourceSection.
 */
public class WarningNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode warningMethod = CallDispatchHeadNode.createPrivate();
    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    public void warningMessage(SourceSection sourceSection, String message) {
        if (coreLibrary().isVerbose()) {
            final DynamicObject warningString = makeStringNode.executeMake(buildWarningMessage(sourceSection, message), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            warningMethod.call(getContext().getCoreLibrary().getWarningModule(), "warn", warningString);
        }
    }

    @TruffleBoundary
    private String buildWarningMessage(SourceSection sourceSection, String message) {
        final String sourceLocation = sourceSection != null ? getContext().fileLine(sourceSection) + ": " : "";
        return sourceLocation + "warning: " + message;
    }

}
