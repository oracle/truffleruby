/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateObjectNode;

@CoreClass("SystemCallError")
public abstract class SystemCallErrorNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocateNameError(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, nil(), null, nil());
        }

    }

    @CoreMethod(names = "errno")
    public abstract static class ErrnoNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object errno(DynamicObject self) {
            return Layouts.SYSTEM_CALL_ERROR.getErrno(self);
        }

    }

    @Primitive(name = "exception_set_errno")
    public abstract static class ErrnoSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object setErrno(DynamicObject error, Object errno) {
            Layouts.SYSTEM_CALL_ERROR.setErrno(error, errno);
            return errno;
        }

    }

}
