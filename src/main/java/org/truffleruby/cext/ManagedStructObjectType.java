/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarSetNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.llvm.spi.NativeTypeLibrary;

@ExportLibrary(value = NativeTypeLibrary.class, receiverType = DynamicObject.class)
@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class ManagedStructObjectType extends ObjectType {

    @Override
    public Class<?> dispatch() {
        return ManagedStructObjectType.class;
    }

    public static final ManagedStructLayout MANAGED_STRUCT = ManagedStructLayoutImpl.INSTANCE;

    public static DynamicObject createManagedStruct(Object type) {
        return MANAGED_STRUCT.createManagedStruct(type);
    }

    public static boolean isInstance(TruffleObject receiver) {
        return MANAGED_STRUCT.isManagedStruct(receiver);
    }

    @Override
    @TruffleBoundary
    public String toString(DynamicObject object) {
        final StringBuilder builder = new StringBuilder("ManagedStruct{");
        boolean first = true;

        for (Object key : object.getShape().getKeys()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(key).append(": ").append(object.get(key));
        }

        builder.append('}');
        return builder.toString();
    }

    @ExportMessage
    public static boolean hasMembers(DynamicObject receiver) {
        return true;
    }

    @ExportMessage
    public static boolean isMemberReadable(DynamicObject receiver, String member) {
        return true;
    }

    @ExportMessage
    public static boolean isMemberModifiable(DynamicObject receiver, String member) {
        return true;
    }

    @ExportMessage
    public static boolean isMemberInsertable(DynamicObject receiver, String member) {
        return true;
    }

    @ExportMessage
    public static Object getMembers(
            DynamicObject receiver,
            boolean includeInternal,
            @Cached(value = "createPrivate()") CallDispatchHeadNode dispatchNode) {
        return dispatchNode.call(receiver, "instance_variables");
    }

    @ExportMessage
    public static Object readMember(
            DynamicObject receiver,
            String name,
            @Cached ObjectIVarGetNode readObjectFieldNode) throws UnknownIdentifierException {

        if (!receiver.containsKey(name)) {
            throw UnknownIdentifierException.create(name);
        }
        return readObjectFieldNode.executeIVarGet(receiver, name);
    }

    @ExportMessage
    public static void writeMember(
            DynamicObject receiver,
            String name,
            Object value,
            @Cached ObjectIVarSetNode writeObjectFieldNode) {

        writeObjectFieldNode.executeIVarSet(receiver, name, value);
    }

    @ExportMessage
    public static boolean hasNativeType(DynamicObject receiver) {
        return true;
    }

    @ExportMessage
    public static Object getNativeType(DynamicObject receiver) {
        return ManagedStructObjectType.MANAGED_STRUCT.getType(receiver);
    }

}
