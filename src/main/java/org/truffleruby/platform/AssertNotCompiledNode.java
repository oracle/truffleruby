/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.platform;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class AssertNotCompiledNode extends RubyNode {

    @Specialization
    protected DynamicObject assertNotCompiled() {
        if (CompilerDirectives.inCompiledCode()) {
            compiledBoundary();
        }

        return nil();
    }

    @TruffleBoundary
    private void compiledBoundary() {
        throw new RaiseException(getContext(), coreExceptions().graalErrorAssertNotCompiledCompiled(this), true);
    }

}
