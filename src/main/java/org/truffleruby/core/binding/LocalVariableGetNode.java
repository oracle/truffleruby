/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.binding;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.locals.FindDeclarationVariableNodes;

@GenerateUncached
@ImportStatic(BindingNodes.class)
public abstract class LocalVariableGetNode extends RubyBaseNode {

    public abstract Object execute(RubyBinding binding, String name);

    @Specialization(guards = "!isHiddenVariable(name)")
    protected Object localVariableGet(RubyBinding binding, String name,
            @Cached FindDeclarationVariableNodes.FindAndReadDeclarationVariableNode readNode) {
        MaterializedFrame frame = binding.getFrame();
        Object result = readNode.execute(frame, name, null);
        if (result == null) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameErrorLocalVariableNotDefined(name, binding, this));
        }
        return result;
    }

    @TruffleBoundary
    @Specialization(guards = "isHiddenVariable(name)")
    protected Object localVariableGetLastLine(RubyBinding binding, String name) {
        throw new RaiseException(
                getContext(),
                coreExceptions().nameError("Bad local variable name", binding, name, this));
    }
}
