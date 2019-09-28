/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import java.util.concurrent.atomic.AtomicReference;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreModule(value = "TruffleRuby::AtomicReference", isClass = true)
public abstract class AtomicReferenceNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, new AtomicReference<Object>(nil()));
        }

    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject initializeNoValue(DynamicObject self, NotProvided notProvided) {
            return self;
        }

        @Specialization(guards = "isNil(nil)")
        protected DynamicObject initializeNil(DynamicObject self, DynamicObject nil) {
            return self;
        }

        @Specialization(guards = { "!isNil(value)", "wasProvided(value)" })
        protected DynamicObject initializeWithValue(DynamicObject self, Object value) {
            Layouts.ATOMIC_REFERENCE.setValue(self, value);
            return self;
        }

    }

    @CoreMethod(names = "get")
    public abstract static class GetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object get(DynamicObject self) {
            return Layouts.ATOMIC_REFERENCE.getValue(self);
        }
    }

    @CoreMethod(names = "set", required = 1)
    public abstract static class SetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object set(DynamicObject self, Object value) {
            Layouts.ATOMIC_REFERENCE.setValue(self, value);
            return value;
        }
    }

    @CoreMethod(names = "get_and_set", required = 1)
    public abstract static class GetAndSetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object getAndSet(DynamicObject self, Object value) {
            return Layouts.ATOMIC_REFERENCE.getAndSetValue(self, value);
        }
    }

    @CoreMethod(names = "compare_and_set_reference", required = 2, visibility = Visibility.PRIVATE)
    public abstract static class CompareAndSetReferenceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isPrimitive(expectedValue)")
        protected boolean compareAndSetPrimitive(DynamicObject self, Object expectedValue, Object newValue) {
            while (true) {
                final Object currentValue = Layouts.ATOMIC_REFERENCE.getValue(self);

                if (RubyGuards.isPrimitive(currentValue) && currentValue.equals(expectedValue)) {
                    if (Layouts.ATOMIC_REFERENCE.compareAndSetValue(self, currentValue, newValue)) {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }

        @Specialization(guards = "!isPrimitive(expectedValue)")
        protected boolean compareAndSetReference(DynamicObject self, Object expectedValue, Object newValue) {
            return Layouts.ATOMIC_REFERENCE.compareAndSetValue(self, expectedValue, newValue);
        }

    }

}
