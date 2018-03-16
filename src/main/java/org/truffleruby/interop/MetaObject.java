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

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.KeyInfo;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.java.JavaInterop;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;

@MessageResolution(receiverType = MetaObject.class)
public final class MetaObject implements TruffleObject {

    private static final String[] KEYS = { "type", "className", "description" };

    private final String type;
    private final String className;
    private final String description;

    public MetaObject(String type, String className, String description) {
        this.type = type;
        this.className = className;
        this.description = description;
    }

    @ExplodeLoop
    private static boolean hasKey(String key) {
        for (String known : KEYS) {
            if (key.equals(known)) {
                return true;
            }
        }
        return false;
    }

    static boolean isInstance(TruffleObject object) {
        return object instanceof MetaObject;
    }

    @Resolve(message = "READ")
    abstract static class ReadNode extends Node {
        Object access(MetaObject obj, String name) {
            switch (name) {
                case "type":
                    return obj.type;
                case "className":
                    return obj.className;
                case "description":
                    return obj.description;
                default:
                    throw UnknownIdentifierException.raise(name);
            }
        }
    }

    @Resolve(message = "KEYS")
    abstract static class KeysNode extends Node {
        TruffleObject access(MetaObject obj) {
            return JavaInterop.asTruffleObject(KEYS);
        }
    }

    @Resolve(message = "KEY_INFO")
    abstract static class KeyInfoNode extends Node {

        int access(MetaObject obj, String name) {
            if (hasKey(name)) {
                return KeyInfo.READABLE;
            } else {
                return KeyInfo.NONE;
            }
        }

    }

    @Override
    public ForeignAccess getForeignAccess() {
        return MetaObjectForeign.ACCESS;
    }
}
