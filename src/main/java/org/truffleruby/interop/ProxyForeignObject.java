/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.Message;
import com.oracle.truffle.api.library.ReflectionLibrary;

@ExportLibrary(ReflectionLibrary.class)
public class ProxyForeignObject implements TruffleObject {

    protected final Object delegate;
    protected final Object logger;

    private static final Message EXECUTABLE = Message.resolve(InteropLibrary.class, "execute");
    private static final Message INVOKE = Message.resolve(InteropLibrary.class, "invokeMember");
    private static final Message INSTANTIATE = Message.resolve(InteropLibrary.class, "instantiate");

    public ProxyForeignObject(Object delegate) {
        this(delegate, null);
    }

    public ProxyForeignObject(Object delegate, Object logger) {
        this.delegate = delegate;
        this.logger = logger;
    }

    @TruffleBoundary
    @ExportMessage
    protected Object send(Message message, Object[] rawArgs,
            @Cached DispatchNode dispatchNode,
            @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode,
            @CachedLibrary("this.delegate") ReflectionLibrary reflections,
            @CachedLibrary("this") ReflectionLibrary node) throws Exception {
        if (logger != null) {
            final Object[] args;
            if (message == EXECUTABLE || message == INSTANTIATE) {
                args = (Object[]) rawArgs[0];
            } else if (message == INVOKE) {
                args = ArrayUtils.unshift((Object[]) rawArgs[1], rawArgs[0]);
            } else {
                args = rawArgs;
            }

            Object[] loggingArgs = ArrayUtils.unshift(args, message.getSimpleName());
            for (int i = 0; i < loggingArgs.length; i++) {
                if (loggingArgs[i] instanceof InteropLibrary) {
                    loggingArgs[i] = RubyLanguage.get(node).getSymbol("InteropLibrary");
                }
            }

            Object[] convertedArgs = foreignToRubyArgumentsNode.executeConvert(loggingArgs);
            dispatchNode.call(logger, "<<", convertedArgs);
        }

        return reflections.send(delegate, message, rawArgs);
    }
}
