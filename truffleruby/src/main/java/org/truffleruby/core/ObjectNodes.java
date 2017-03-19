/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.language.objects.ObjectIDOperations;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarGetNodeGen;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;

/**
 * Rubinius primitives associated with the Ruby {@code Object} class.
 */
public abstract class ObjectNodes {

    @Primitive(name = "object_id")
    public abstract static class ObjectIDPrimitiveNode extends PrimitiveArrayArgumentsNode {

        public abstract Object executeObjectID(Object value);

        @Specialization(guards = "isNil(nil)")
        public long objectIDNil(Object nil) {
            return ObjectIDOperations.NIL;
        }

        @Specialization(guards = "value")
        public long objectIDTrue(boolean value) {
            return ObjectIDOperations.TRUE;
        }

        @Specialization(guards = "!value")
        public long objectIDFalse(boolean value) {
            return ObjectIDOperations.FALSE;
        }

        @Specialization
        public long objectID(int value) {
            return ObjectIDOperations.smallFixnumToID(value);
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long objectIDSmallFixnumOverflow(long value) throws ArithmeticException {
            return ObjectIDOperations.smallFixnumToIDOverflow(value);
        }

        @Specialization
        public Object objectID(long value,
                               @Cached("createCountingProfile()") ConditionProfile smallProfile) {
            if (smallProfile.profile(ObjectIDOperations.isSmallFixnum(value))) {
                return ObjectIDOperations.smallFixnumToID(value);
            } else {
                return ObjectIDOperations.largeFixnumToID(getContext(), value);
            }
        }

        @Specialization
        public Object objectID(double value) {
            return ObjectIDOperations.floatToID(getContext(), value);
        }

        @Specialization(guards = "!isNil(object)")
        public long objectID(DynamicObject object,
                @Cached("createReadObjectIDNode()") ReadObjectFieldNode readObjectIdNode,
                @Cached("createWriteObjectIDNode()") WriteObjectFieldNode writeObjectIdNode) {
            final long id = (long) readObjectIdNode.execute(object);

            if (id == 0) {
                final long newId = getContext().getObjectSpaceManager().getNextObjectID();
                writeObjectIdNode.execute(object, newId);
                return newId;
            }

            return id;
        }

        @Fallback
        public long objectID(Object object) {
            return Integer.toUnsignedLong(object.hashCode());
        }

        protected ReadObjectFieldNode createReadObjectIDNode() {
            return ReadObjectFieldNodeGen.create(Layouts.OBJECT_ID_IDENTIFIER, 0L);
        }

        protected WriteObjectFieldNode createWriteObjectIDNode() {
            return WriteObjectFieldNodeGen.create(Layouts.OBJECT_ID_IDENTIFIER);
        }

    }

    @Primitive(name = "object_ivar_get")
    public abstract static class ObjectIVarGetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object ivarGet(DynamicObject object, DynamicObject name,
                @Cached("createObjectIVarGetNode()") ObjectIVarGetNode iVarGetNode) {
            return iVarGetNode.executeIVarGet(object, Layouts.SYMBOL.getString(name));
        }

        protected ObjectIVarGetNode createObjectIVarGetNode() {
            return ObjectIVarGetNodeGen.create(false, null, null);
        }

    }

    @Primitive(name = "object_ivar_set")
    public abstract static class ObjectIVarSetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object ivarSet(DynamicObject object, DynamicObject name, Object value,
                @Cached("createObjectIVarSetNode()") ObjectIVarSetNode iVarSetNode) {
            return iVarSetNode.executeIVarSet(object, Layouts.SYMBOL.getString(name), value);
        }

        protected ObjectIVarSetNode createObjectIVarSetNode() {
            return ObjectIVarSetNodeGen.create(false, null, null, null);
        }

    }

}
