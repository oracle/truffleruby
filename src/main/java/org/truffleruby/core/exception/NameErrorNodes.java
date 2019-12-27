/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreModule(value = "NameError", isClass = true)
public abstract class NameErrorNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocateNameError(DynamicObject rubyClass) {
            return allocateObjectNode
                    .allocate(
                            rubyClass,
                            Layouts.NAME_ERROR.build(nil(), null, null, nil(), null, null, null, nil()));
        }

    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object name(DynamicObject self) {
            return Layouts.NAME_ERROR.getName(self);
        }

    }

    @CoreMethod(names = "receiver")
    public abstract static class ReceiverNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object receiver(DynamicObject self) {
            final Object receiver = Layouts.NAME_ERROR.getReceiver(self);

            // TODO BJF July 21, 2016 Implement name error in message field

            if (receiver == null) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorNoReceiver(this));
            }
            return receiver;
        }

    }

    @Primitive(name = "name_error_set_name")
    public abstract static class NameSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setName(DynamicObject error, Object name) {
            Layouts.NAME_ERROR.setName(error, name);
            return name;
        }

    }


}
