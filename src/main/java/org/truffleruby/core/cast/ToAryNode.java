/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.dispatch.DispatchNode;

public abstract class ToAryNode extends RubyBaseNode {


    public abstract RubyArray execute(Object object);


    @Specialization
    protected RubyArray coerceRubyArray(RubyArray array) {
        return array;
    }

    @Specialization(guards = "!isRubyArray(object)")
    protected RubyArray coerceObject(Object object,
            @Cached DispatchNode toAryNode) {
        return (RubyArray) toAryNode.call(
                coreLibrary().truffleTypeModule,
                "rb_convert_type",
                object,
                coreLibrary().arrayClass,
                coreSymbols().TO_ARY);
    }
}
