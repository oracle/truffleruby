/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop.messages;

import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class ProcMessages extends RubyObjectMessages {

    @ExportMessage
    public static boolean isExecutable(DynamicObject receiver) {
        return true;
    }

    @ExportMessage
    public static Object execute(
            DynamicObject proc, Object[] arguments,
            @Cached YieldNode yieldNode,
            @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return yieldNode.executeDispatch(proc, foreignToRubyArgumentsNode.executeConvert(arguments));
    }
}
