/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyContextSourceNode;

/** e.g. for {@code module::FOO ||= 1}
 *
 * <p>
 * We need a separate class for this because we need to check if the constant is defined. Doing so will evaluate the
 * module part, which will be evaluated again when assigning the constant. If evaluating the module part has any
 * side-effect, this is incorrect and differs from MRI semantics. */
public class OrAssignConstantNode extends RubyContextSourceNode {

    @Child protected ReadConstantNode readConstant;
    @Child protected WriteConstantNode writeConstant;
    @Child private BooleanCastNode cast;

    public OrAssignConstantNode(ReadConstantNode readConstant, WriteConstantNode writeConstant) {
        this.readConstant = readConstant;
        this.writeConstant = writeConstant;
    }

    @Override
    public Object execute(VirtualFrame frame) {

        if (readConstant.isModuleTriviallyUndefined(frame, getLanguage(), getContext())) {
            // It might not be defined because of autoloaded constants (maybe other reasons?),
            // simply attempt writing (which will trigger autoloads if required).
            // Since we didn't evaluate the module part yet, no side-effects can occur.
            return writeConstant.execute(frame);
        }

        // Conceptually, we want to rewrite `<x>::Foo ||= <y>` to `(defined?(<x>) && <x>) || (<x>::Foo = <y>)`
        // BUT, we want the side-effects of <x> (the module part) to be only triggered once.
        // We do let any exception raised bubble through. Normally they would be swallowed by `defined?`, but
        // MRI raises them anyway, *before* evaluation the right-hand side (which is a different behaviour from
        // regular constant assignment, which evaluates the right-hand side first).
        final RubyModule module = readConstant.evaluateModule(frame);

        // Next we check if the constant itself is defined, and if it is, we get its value.
        final RubyConstant constant = readConstant.getConstantIfDefined(module);
        final Object value = constant == null
                ? null
                : readConstant.execute(frame, module, constant);

        // Write if the constant is undefined or if its value is falsy.
        if (constant == null || !castToBoolean(value)) {
            return writeConstant.execute(frame, module);
        } else {
            return value;
        }
    }

    private boolean castToBoolean(final Object value) {
        if (cast == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            cast = insert(BooleanCastNodeGen.create(null));
        }
        return cast.executeToBoolean(value);
    }
}
