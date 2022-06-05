/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocationTracing;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

@CoreModule(value = "SyntaxError", isClass = true)
public abstract class SyntaxErrorNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySyntaxError allocateSyntaxError(RubyClass rubyClass) {
            final Shape shape = getLanguage().syntaxErrorShape;
            final RubySyntaxError instance = new RubySyntaxError(rubyClass, shape, nil(), null, nil(), null);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

}
