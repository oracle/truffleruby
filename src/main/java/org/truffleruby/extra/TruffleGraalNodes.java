/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.InternalMethod;

@CoreClass("Truffle::Graal")
public abstract class TruffleGraalNodes {

    @CoreMethod(names = "assert_constant", onSingleton = true, required = 1)
    public abstract static class AssertConstantNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject assertConstant(Object value) {
            throw new RaiseException(coreExceptions().runtimeErrorNotConstant(this));
        }

    }

    @CoreMethod(names = "assert_not_compiled", onSingleton = true)
    public abstract static class AssertNotCompiledNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject assertNotCompiled() {
            throw new RaiseException(coreExceptions().runtimeErrorCompiled(this));
        }

    }

    @CoreMethod(names = "always_split", onSingleton = true, required = 1)
    public abstract static class AlwaysSplitNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyMethod(rubyMethod)")
        public DynamicObject splitMethod(DynamicObject rubyMethod) {
            InternalMethod internalMethod = Layouts.METHOD.getMethod(rubyMethod);
            internalMethod.getSharedMethodInfo().setAlwaysClone(true);
            return rubyMethod;
        }

        @Specialization(guards = "isRubyUnboundMethod(rubyMethod)")
        public DynamicObject splitUnboundMethod(DynamicObject rubyMethod) {
            InternalMethod internalMethod = Layouts.UNBOUND_METHOD.getMethod(rubyMethod);
            internalMethod.getSharedMethodInfo().setAlwaysClone(true);
            return rubyMethod;
        }

    }

}
