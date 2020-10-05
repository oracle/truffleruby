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

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@CoreModule(value = "SystemCallError", isClass = true)
public abstract class SystemCallErrorNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

        @Specialization
        protected RubySystemCallError allocateNameError(RubyClass rubyClass,
                @CachedLanguage RubyLanguage language) {
            final Shape shape = allocateNode.getCachedShape(rubyClass);
            final RubySystemCallError instance = new RubySystemCallError(rubyClass, shape, nil, null, nil, nil);
            allocateNode.trace(instance, this, language);
            return instance;
        }

    }

    @CoreMethod(names = "errno")
    public abstract static class ErrnoNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object errno(RubySystemCallError self) {
            return self.errno;
        }

    }

    @Primitive(name = "exception_set_errno")
    public abstract static class ErrnoSetNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setErrno(RubySystemCallError error, Object errno) {
            error.errno = errno;
            return errno;
        }

    }

}
