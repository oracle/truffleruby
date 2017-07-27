/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

public class WarnNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode warnMethod = CallDispatchHeadNode.createOnSelf();
    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    public Object warn(String... arguments) {
        final String warningMessage = concatArgumentsToString(arguments);
        final DynamicObject warningString = makeStringNode.executeMake(warningMessage.getBytes(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        return warnMethod.call(null, getContext().getCoreLibrary().getKernelModule(), "warn", warningString);
    }

    @TruffleBoundary
    private String concatArgumentsToString(String... arguments) {
        return String.join("", arguments);
    }
}
