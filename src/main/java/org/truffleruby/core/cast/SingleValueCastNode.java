/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

@ImportStatic(value = { RubyGuards.class })
public abstract class SingleValueCastNode extends RubyBaseNode {

    public abstract Object executeSingleValue(Object[] args);

    @Specialization(guards = "noArguments(args)")
    protected Object castNil(Object[] args) {
        return nil();
    }

    @Specialization(guards = "singleArgument(args)")
    protected Object castSingle(Object[] args) {
        return args[0];
    }

    @Specialization(guards = { "!noArguments(args)", "!singleArgument(args)" })
    protected RubyArray castMany(Object[] args) {
        return createArray(args);
    }

}
