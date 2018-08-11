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

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

/**
 * Warns if $VERBOSE is true or false, but not nil.
 * Corresponds to Truffle::Warnings.warn, but in Java with a given SourceSection.
 */
public class WarnNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode warnMethod = CallDispatchHeadNode.createOnSelf();
    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    private Object callWarn(String warningMessage) {
        final DynamicObject warningString = makeStringNode.executeMake(warningMessage, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        return warnMethod.call(getContext().getCoreLibrary().getKernelModule(), "warn", warningString);
    }

    public void warningMessage(SourceSection sourceSection, String message) {
        if (coreLibrary().warningsEnabled()) {
            final String sourceLocation = sourceSection != null ? getContext().getSourceLoader().fileLine(sourceSection) + ": " : "";
            callWarn(sourceLocation + "warning: " + message);
        }
    }

}
