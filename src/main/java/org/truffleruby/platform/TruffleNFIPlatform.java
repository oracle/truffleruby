/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
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

    private final Node bindNode = Message.INVOKE.createNode();

    private final String size_t;
    private final NativeFunction strlen;
    private final NativeFunction strnlen;

    public TruffleNFIPlatform(RubyContext context) {
        defaultLibrary = (TruffleObject) context.getEnv().parse(Source.newBuilder("nfi", "default", "native").build()).call();

        size_t = resolveType(context.getNativeConfiguration(), "size_t");
        strlen = getFunction("strlen", String.format("(pointer):%s", size_t));
        strnlen = getFunction("strnlen", String.format("(pointer,%s):%s", size_t, size_t));
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

    private static Object execute(Node executeNode, TruffleObject function, Object... args) {
        try {
            return ForeignAccess.sendExecute(executeNode, function, args);
        } catch (UnsupportedMessageException | UnsupportedTypeException | ArityException e) {
            throw new JavaException(e);
        }
    }

    private static Object invoke(Node invokeNode, TruffleObject receiver, String identifier, Object... args) {
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

    public Object resolveTypeRaw(NativeConfiguration nativeConfiguration, String type) {
        final Object typedef = nativeConfiguration.get("platform.typedef." + type);
        if (typedef == null) {
            throw new UnsupportedOperationException("Type " + type + " is not defined in the native configuration");
        }
        return typedef;
    }

    public String resolveType(NativeConfiguration nativeConfiguration, String type) {
        final Object typedef = resolveTypeRaw(nativeConfiguration, type);
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

    public NativeFunction getFunction(String functionName, String signature) {
        final TruffleObject symbol = lookup(defaultLibrary, functionName);
        final TruffleObject function = bind(symbol, signature);
        return new NativeFunction(function);
    }

    public String size_t() {
        return size_t;
    }

    public NativeFunction getStrlen() {
        return strlen;
    }

    public NativeFunction getStrnlen() {
        return strnlen;
    }

    public static class NativeFunction {

        private final TruffleObject function;
        private final Node executeNode;

        private NativeFunction(TruffleObject function) {
            this.function = function;
            this.executeNode = Message.EXECUTE.createNode();
        }

        public Object call(Object... arguments) {
            return execute(executeNode, function, arguments);
        }

    }

}
