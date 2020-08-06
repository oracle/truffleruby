/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.utilities.TruffleWeakReference;

@CoreModule("Truffle::WeakRefOperations")
public abstract class WeakRefNodes {

    private static final TruffleWeakReference<Object> EMPTY_WEAK_REF = new TruffleWeakReference<>(null);
    private static final HiddenKey fieldName = new HiddenKey("weak_ref");

    @Primitive(name = "weakref_set_object")
    public static abstract class WeakRefSetObjectPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child WriteObjectFieldNode fieldNode = WriteObjectFieldNode.create();

        @Specialization
        protected Object weakRefSetObject(DynamicObject weakRef, Object object) {
            fieldNode.write(weakRef, fieldName, newTruffleWeakReference(object));
            return object;
        }

        @TruffleBoundary // GR-25356
        private static TruffleWeakReference<Object> newTruffleWeakReference(Object object) {
            return new TruffleWeakReference<>(object);
        }

    }

    @Primitive(name = "weakref_object")
    public static abstract class WeakRefObjectPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child ReadObjectFieldNode fieldNode = ReadObjectFieldNode.create();

        @Specialization
        protected Object weakRefObject(DynamicObject weakRef) {
            final TruffleWeakReference<?> ref = (TruffleWeakReference<?>) fieldNode
                    .execute(weakRef, fieldName, EMPTY_WEAK_REF);
            final Object object = ref.get();
            return object == null ? nil : object;
        }
    }
}
