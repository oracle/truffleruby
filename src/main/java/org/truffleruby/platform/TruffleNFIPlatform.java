/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform;

import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.control.JavaException;

import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class TruffleNFIPlatform {

    final TruffleObject defaultLibrary;

    private final Node readNode = Message.READ.createNode();
    private final Node asPointerNode = Message.AS_POINTER.createNode();

    private final Node bindNode = Message.createInvoke(1).createNode();

    public TruffleNFIPlatform(RubyContext context) {
        defaultLibrary = (TruffleObject) context.getEnv().parse(Source.newBuilder("default").mimeType("application/x-native").name("native").build()).call();
    }

    public TruffleObject getDefaultLibrary() {
        return defaultLibrary;
    }

    public TruffleObject lookup(TruffleObject library, String name) {
        try {
            return (TruffleObject) ForeignAccess.sendRead(readNode, library, name);
        } catch (UnknownIdentifierException | UnsupportedMessageException e) {
            throw new JavaException(e);
        }
    }

    public Object execute(Node executeNode, TruffleObject function, Object... args) {
        try {
            return ForeignAccess.sendExecute(executeNode, function, args);
        } catch (UnsupportedMessageException | UnsupportedTypeException | ArityException e) {
            throw new JavaException(e);
        }
    }

    public Object invoke(Node invokeNode, TruffleObject receiver, String identifier, Object... args) {
        try {
            return ForeignAccess.sendInvoke(invokeNode, receiver, identifier, args);
        } catch (UnsupportedMessageException | UnsupportedTypeException | ArityException | UnknownIdentifierException e) {
            throw new JavaException(e);
        }
    }

    public TruffleObject bind(TruffleObject function, String signature) {
        return (TruffleObject) invoke(bindNode, function, "bind", signature);
    }

    public long asPointer(TruffleObject function) {
        try {
            return ForeignAccess.sendAsPointer(asPointerNode, function);
        } catch (UnsupportedMessageException e) {
            throw new JavaException(e);
        }
    }

    public String resolveType(RubiniusConfiguration rubiniusConfiguration, String type) {
        final Object typedef = rubiniusConfiguration.get("rbx.platform.typedef." + type);
        if (typedef == null) {
            throw new UnsupportedOperationException("Type " + type + " is not defined in the native configuration");
        }
        return toNFIType(StringOperations.getString((DynamicObject) typedef));
    }

    private String toNFIType(String type) {
        switch (type) {
            case "uint":
                return "uint32";
            case "ulong":
                return "uint64";
            default:
                return type;
        }
    }

    public NativeFunction getFunction(String functionName, int arguments, String signature) {
        final TruffleObject symbol = lookup(defaultLibrary, functionName);
        final TruffleObject function = bind(symbol, signature);
        return new NativeFunction(function, arguments);
    }

    public class NativeFunction {

        private final TruffleObject function;
        private final Node executeNode;

        private NativeFunction(TruffleObject function, int numberOfArguments) {
            this.function = function;
            this.executeNode = Message.createExecute(numberOfArguments).createNode();
        }

        public Object call(Object... arguments) {
            return execute(executeNode, function, arguments);
        }

    }

}
