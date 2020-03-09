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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.methods.CallBoundMethodNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class MethodMessages extends RubyObjectMessages {

    @ExportMessage
    protected static boolean isExecutable(DynamicObject receiver) {
        return true;
    }

    @ExportMessage
    protected static Object execute(
            DynamicObject method, Object[] arguments,
            @Cached CallBoundMethodNode callBoundMethodNode,
            @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        return callBoundMethodNode.executeCallBoundMethod(
                method,
                foreignToRubyArgumentsNode.executeConvert(arguments),
                Nil.INSTANCE);
    }
}
