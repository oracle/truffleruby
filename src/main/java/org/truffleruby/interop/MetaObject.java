/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.KeyInfo;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = MetaObject.class)
public final class MetaObject implements TruffleObject {
    final Map<String, ? extends Object> properties;

    public MetaObject(Map<String, ? extends Object> properties) {
        this.properties = properties;
    }

    static boolean isInstance(TruffleObject object) {
        return object instanceof MetaObject;
    }

    @Resolve(message = "READ")
    abstract static class ReadNode extends Node {
        @TruffleBoundary
        Object access(MetaObject obj, String name) {
            Object value = obj.properties.get(name);
            if (value == null) {
                throw UnknownIdentifierException.raise(name);
            }
            return value;
        }
    }

    @Resolve(message = "KEYS")
    abstract static class KeysNode extends Node {
        @TruffleBoundary
        TruffleObject access(MetaObject obj) {
            return JavaInterop.asTruffleObject(obj.properties.keySet().toArray(new String[0]));
        }
    }

    @Resolve(message = "KEY_INFO")
    abstract static class KeyInfoNode extends Node {
        @TruffleBoundary
        int access(MetaObject obj, String name) {
            if (!obj.properties.containsKey(name)) {
                return 0;
            }
            return KeyInfo.newBuilder().setReadable(true).setWritable(false).build();
        }
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return MetaObjectForeign.ACCESS;
    }
}
