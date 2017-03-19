/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rubinius;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.IsANodeGen;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.LogicalClassNodeGen;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@CoreClass("Rubinius::Type")
public abstract class TypeNodes {

    @CoreMethod(names = "object_kind_of?", onSingleton = true, required = 2)
    public static abstract class ObjectKindOfNode extends CoreMethodArrayArgumentsNode {

        @Child private IsANode isANode = IsANodeGen.create(null, null);

        @Specialization
        public boolean objectKindOf(Object object, DynamicObject rubyClass) {
            return isANode.executeIsA(object, rubyClass);
        }

    }

    @CoreMethod(names = "object_class", onSingleton = true, required = 1)
    public static abstract class VMObjectClassNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNodeGen.create(null);

        @Specialization
        public DynamicObject objectClass(VirtualFrame frame, Object object) {
            return classNode.executeLogicalClass(object);
        }

    }

    @CoreMethod(names = "module_name", onSingleton = true, required = 1)
    public static abstract class VMGetModuleNamePrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject moduleName(DynamicObject module) {
            final String name = Layouts.MODULE.getFields(module).getName();
            return createString(StringOperations.encodeRope(name, UTF8Encoding.INSTANCE));
        }

    }

}
