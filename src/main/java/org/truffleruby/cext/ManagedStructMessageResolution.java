/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;

import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@MessageResolution(receiverType = ManagedStructObjectType.class)
public class ManagedStructMessageResolution {

    @Resolve(message = "READ")
    public static abstract class ForeignReadNode extends Node {

        @Child ObjectIVarGetNode readObjectFieldNode = ObjectIVarGetNode.create();
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
    public static abstract class ForeignWriteNode extends Node {

        @Child ObjectIVarSetNode writeObjectFieldNode = ObjectIVarSetNodeGen.create(false);
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
    public abstract static class GetDynamicType extends Node {

        protected Object access(DynamicObject object) {
            return ManagedStructObjectType.MANAGED_STRUCT.getType(object);
        }
    }
}
