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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;

@MessageResolution(receiverType = ManagedStructObjectType.class)
public class ManagedStructObjectType extends ObjectType {

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


    @Resolve(message = "READ")
    public static abstract class ReadNode extends Node {

        @Child
        ObjectIVarGetNode readObjectFieldNode = ObjectIVarGetNode.create();
        private final BranchProfile notStringProfile = BranchProfile.create();

        protected Object access(DynamicObject object, Object name) {
            if (!(name instanceof String)) {
                notStringProfile.enter();
                throw UnknownIdentifierException.raise(StringUtils.toString(name));
            }

            assert object.containsKey(name) : object + " does not have key " + name;

            return readObjectFieldNode.executeIVarGet(object, name);
        }

    }

    @Resolve(message = "WRITE")
    public static abstract class WriteNode extends Node {

        @Child
        ObjectIVarSetNode writeObjectFieldNode = ObjectIVarSetNodeGen.create(false);
        private final BranchProfile notStringProfile = BranchProfile.create();

        protected Object access(DynamicObject object, Object name, Object value) {
            if (!(name instanceof String)) {
                notStringProfile.enter();
                throw UnknownIdentifierException.raise(StringUtils.toString(name));
            }

            return writeObjectFieldNode.executeIVarSet(object, name, value);
        }

    }

    @SuppressWarnings("unknown-message")
    @Resolve(message = "com.oracle.truffle.llvm.spi.GetDynamicType")
    public abstract static class GetDynamicTypeNode extends Node {

        protected Object access(DynamicObject object) {
            return ManagedStructObjectType.MANAGED_STRUCT.getType(object);
        }
    }

    @Override
    public ForeignAccess getForeignAccessFactory(DynamicObject object) {
        return ManagedStructObjectTypeForeign.ACCESS;
    }

}
