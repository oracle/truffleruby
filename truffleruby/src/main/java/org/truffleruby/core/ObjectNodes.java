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

import org.truffleruby.Layouts;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarGetNodeGen;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Rubinius primitives associated with the Ruby {@code Object} class.
 */
public abstract class ObjectNodes {

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
