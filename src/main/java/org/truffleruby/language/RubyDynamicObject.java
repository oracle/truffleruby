/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.DynamicDispatchLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.Shape;

/** All Ruby DynamicObjects will eventually extend this.
 *
 * {@link org.truffleruby.Layouts} still use DynamicObjectImpl until migrated. */
@ExportLibrary(DynamicDispatchLibrary.class)
public class RubyDynamicObject extends DynamicObject {

    public RubyDynamicObject(Shape shape) {
        super(shape);
    }

    // Same dispatch as in DynamicObjectImpl, until all Layouts are migrated

    @ExportMessage
    static class Accepts {
        @Specialization(limit = "1", guards = "cachedShape == receiver.getShape()")
        protected static boolean doCachedShape(RubyDynamicObject receiver,
                @Shared("cachedShape") @Cached("receiver.getShape()") Shape cachedShape,
                @Shared("cachedTypeClass") @Cached(
                        value = "getObjectTypeClass(receiver)",
                        allowUncached = true) Class<? extends ObjectType> typeClass) {
            return true;
        }

        @Specialization(replaces = "doCachedShape")
        protected static boolean doCachedTypeClass(RubyDynamicObject receiver,
                @Shared("cachedTypeClass") @Cached(
                        value = "getObjectTypeClass(receiver)",
                        allowUncached = true) Class<? extends ObjectType> typeClass) {
            return typeClass == getObjectTypeClass(receiver);
        }
    }

    @ExportMessage
    static class Dispatch {
        @Specialization(limit = "1", guards = "cachedShape == receiver.getShape()")
        protected static Class<?> doCachedShape(RubyDynamicObject receiver,
                @Shared("cachedShape") @Cached("receiver.getShape()") Shape cachedShape,
                @Shared("cachedTypeClass") @Cached(
                        value = "getObjectTypeClass(receiver)",
                        allowUncached = true) Class<? extends ObjectType> typeClass) {
            return cachedShape.getObjectType().dispatch();
        }

        @Specialization(replaces = "doCachedShape")
        protected static Class<?> doCachedTypeClass(RubyDynamicObject receiver,
                @Shared("cachedTypeClass") @Cached(
                        value = "getObjectTypeClass(receiver)",
                        allowUncached = true) Class<? extends ObjectType> typeClass) {
            ObjectType objectType = CompilerDirectives.castExact(receiver.getShape().getObjectType(), typeClass);
            return objectType.dispatch();
        }
    }

    protected static Class<? extends ObjectType> getObjectTypeClass(DynamicObject object) {
        return object.getShape().getObjectType().getClass();
    }

}
