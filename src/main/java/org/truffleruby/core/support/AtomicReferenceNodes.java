/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import java.util.concurrent.atomic.AtomicReference;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqualNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.IsANode;

@CoreClass("Truffle::AtomicReference")
public abstract class AtomicReferenceNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, new AtomicReference<Object>(nil()));
        }

    }

    @CoreMethod(names = "get")
    public abstract static class GetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object get(DynamicObject self) {
            return Layouts.ATOMIC_REFERENCE.getValue(self);
        }
    }

    @CoreMethod(names = "set", required = 1)
    public abstract static class SetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object set(DynamicObject self, Object value) {
            Layouts.ATOMIC_REFERENCE.setValue(self, value);
            return value;
        }
    }

    @CoreMethod(names = "compare_and_set", required = 2)
    public abstract static class CompareAndSetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean compareAndSetNumber(
                VirtualFrame frame, DynamicObject self, Object expectedValue, Object newValue,
                @Cached("create()") SameOrEqualNode sameOrEqualNode,
                @Cached("create()") IsANode expectedValueIsA,
                @Cached("create()") IsANode currentValueIsA,
                @Cached("createBinaryProfile()") ConditionProfile profile) {

            final boolean expectedValueIsNumeric =
                    expectedValueIsA.executeIsA(expectedValue, coreLibrary().getNumericClass());
            if (profile.profile(expectedValueIsNumeric)) {
                while (true) {
                    final Object currentValue = Layouts.ATOMIC_REFERENCE.getValue(self);
                    if (!currentValueIsA.executeIsA(currentValue, coreLibrary().getNumericClass())) {
                        return false;
                    }
                    if (!sameOrEqualNode.executeSameOrEqual(frame, currentValue, expectedValue)) {
                        return false;
                    }
                    if (cas(self, currentValue, newValue)) {
                        return true;
                    }
                }
            } else {
                return cas(self, expectedValue, newValue);
            }
        }

        private boolean cas(DynamicObject self, Object expectedValue, Object newValue) {
            return Layouts.ATOMIC_REFERENCE.compareAndSetValue(self, expectedValue, newValue);
        }
    }

    @CoreMethod(names = "get_and_set", required = 1)
    public abstract static class GetAndSetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object getAndSet(DynamicObject self, Object value) {
            return Layouts.ATOMIC_REFERENCE.getAndSetValue(self, value);
        }
    }

}
