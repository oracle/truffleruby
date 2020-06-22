/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.lang.reflect.Executable;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.library.Message;
import com.oracle.truffle.api.library.ReflectionLibrary;

import org.truffleruby.language.dispatch.CallDispatchHeadNode;

@ExportLibrary(ReflectionLibrary.class)
public class ProxyForeignObject implements TruffleObject {

    protected final Object delegate;
    protected final Object logger;

    private final static Message EXECUTABLE = Message.resolve(InteropLibrary.class, "execute");
    private final static Message INVOKE = Message.resolve(InteropLibrary.class, "invokeMember");
    private final static Message INSTANTIATE = Message.resolve(InteropLibrary.class, "instantiate");

    public ProxyForeignObject(Object delegate) {
        this(delegate, null);
    }

    public ProxyForeignObject(Object delegate, Object logger) {
        this.delegate = delegate;
        this.logger = logger;
    }

    @ExportMessage
    protected Object send(Message message, Object[] rawArgs,
            @Cached CallDispatchHeadNode dispatchNode,
            @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode,
            @CachedLibrary("this.delegate") ReflectionLibrary reflections) throws Exception {
        if (logger != null) {
            Object[] args;
            if (message == EXECUTABLE || message == INSTANTIATE) {
                args = (Object[])rawArgs[0];
            } else if (message == INVOKE) {
                Object[] invokeArgs = (Object[]) rawArgs[1];
                args = new Object[invokeArgs.length + 1];
                args[0] = rawArgs[0];
                System.arraycopy(invokeArgs, 0, args, 1, invokeArgs.length);
            } else {
                args = rawArgs;
            }
            Object[] loggingArgs = new Object[args.length + 1];
            loggingArgs[0] = message.getSimpleName();
            System.arraycopy(args, 0, loggingArgs, 1, args.length);
            Object[] convertedArgs = foreignToRubyArgumentsNode.executeConvert(loggingArgs);
            for (int i = 0; i < convertedArgs.length; i++) {
                if (convertedArgs[i] instanceof Object[]) {
                }
            }
            dispatchNode.call(logger, "log", convertedArgs);
        }

        return reflections.send(delegate, message, rawArgs);
    }
}
