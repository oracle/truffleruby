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
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreModule(value = "NoMethodError", isClass = true)
public abstract class NoMethodErrorNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocateNoMethodError(DynamicObject rubyClass) {
            return allocateObjectNode
                    .allocate(
                            rubyClass,
                            Layouts.NO_METHOD_ERROR.build(nil(), null, null, nil(), null, null, null, nil(), nil()));
        }

    }

    @CoreMethod(names = "args")
    public abstract static class ArgsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object args(DynamicObject self) {
            return Layouts.NO_METHOD_ERROR.getArgs(self);
        }

    }

    @Primitive(name = "no_method_error_set_args")
    public abstract static class ArgsSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setArgs(DynamicObject error, Object args) {
            Layouts.NO_METHOD_ERROR.setArgs(error, args);
            return args;
        }

    }


}
