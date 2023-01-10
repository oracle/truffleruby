/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.annotations.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocationTracing;

import com.oracle.truffle.api.dsl.Specialization;

@CoreModule(value = "FrozenError", isClass = true)
public abstract class FrozenErrorNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyFrozenError allocateFrozenError(RubyClass rubyClass) {
            final Shape shape = getLanguage().frozenErrorShape;
            final RubyFrozenError instance = new RubyFrozenError(rubyClass, shape, nil, null, nil, null);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "receiver")
    public abstract static class ReceiverNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object receiver(RubyFrozenError self,
                @Cached BranchProfile errorProfile) {
            final Object receiver = self.receiver;

            if (receiver == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorNoReceiver(this));
            }
            return receiver;
        }

    }

    @Primitive(name = "frozen_error_set_receiver")
    public abstract static class ReceiverSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setReceiver(RubyFrozenError error, Object receiver) {
            error.receiver = receiver;
            return receiver;
        }

    }


}
