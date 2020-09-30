/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.platform;

import org.truffleruby.RubyContext;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.interop.InteropNodes;
import org.truffleruby.interop.TranslateInteropExceptionNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.source.Source;

public class TruffleNFIPlatform {

    final Object defaultLibrary;

    private final String size_t;
    private final NativeFunction strlen;
    private final NativeFunction strnlen;

    public TruffleNFIPlatform(RubyContext context) {
        defaultLibrary = context
                .getEnv()
                .parseInternal(Source.newBuilder("nfi", "default", "native").build())
                .call();

        size_t = resolveType(context.getNativeConfiguration(), "size_t");
        strlen = getFunction("strlen", String.format("(pointer):%s", size_t));
        strnlen = getFunction("strnlen", String.format("(pointer,%s):%s", size_t, size_t));
    }

    public Object getDefaultLibrary() {
        return defaultLibrary;
    }

    public Object lookup(Object library, String name) {
        try {
            return InteropLibrary.getFactory().getUncached(library).readMember(library, name);
        } catch (UnknownIdentifierException | UnsupportedMessageException e) {
            throw TranslateInteropExceptionNode.getUncached().execute(e);
        }
    }

    private static Object invoke(Object receiver, String identifier, Object... args) {
        try {
            return InteropLibrary.getFactory().getUncached(receiver).invokeMember(receiver, identifier, args);
        } catch (UnsupportedMessageException | UnsupportedTypeException | ArityException
                | UnknownIdentifierException e) {
            throw TranslateInteropExceptionNode.getUncached().execute(e);
        }
    }

    private Object bind(Object function, String signature) {
        return invoke(function, "bind", signature);
    }

    public long asPointer(Object object) {
        try {
            return InteropLibrary.getFactory().getUncached(object).asPointer(object);
        } catch (UnsupportedMessageException e) {
            throw TranslateInteropExceptionNode.getUncached().execute(e);
        }
    }

    public Object resolveTypeRaw(NativeConfiguration nativeConfiguration, String type) {
        final Object typedef = nativeConfiguration.get("platform.typedef." + type);
        if (typedef == null) {
            throw CompilerDirectives.shouldNotReachHere("Type " + type + " is not defined in the native configuration");
        }
        return typedef;
    }

    public String resolveType(NativeConfiguration nativeConfiguration, String type) {
        final Object typedef = resolveTypeRaw(nativeConfiguration, type);
        return toNFIType(((RubyString) typedef).getJavaString());
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
        final Object symbol = lookup(defaultLibrary, functionName);
        final Object function = bind(symbol, signature);
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

        private final Object function;
        private final InteropLibrary functionInteropLibrary;

        private NativeFunction(Object function) {
            this.function = function;
            this.functionInteropLibrary = InteropLibrary.getFactory().getUncached(this.function);
        }

        public Object call(Object... arguments) {
            return InteropNodes
                    .execute(function, arguments, functionInteropLibrary, TranslateInteropExceptionNode.getUncached());
        }

    }

}
