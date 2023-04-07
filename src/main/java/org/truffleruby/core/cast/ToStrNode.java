/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NeverDefault;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@NodeChild(value = "childNode", type = RubyBaseNodeWithExecute.class)
public abstract class ToStrNode extends RubyBaseNodeWithExecute {

    @NeverDefault
    public static ToStrNode create() {
        return ToStrNodeGen.create(null);
    }

    public static ToStrNode create(RubyBaseNodeWithExecute child) {
        return ToStrNodeGen.create(child);
    }

    public abstract Object execute(Object object);

    public abstract RubyBaseNodeWithExecute getChildNode();

    @Specialization
    protected RubyString coerceRubyString(RubyString string) {
        return string;
    }

    @Specialization
    protected ImmutableRubyString coerceImmutableRubyString(ImmutableRubyString string) {
        return string;
    }

    @Specialization(guards = "isNotRubyString(object)")
    protected Object coerceObject(Object object,
            @Cached DispatchNode toStrNode) {
        return toStrNode.call(
                coreLibrary().truffleTypeModule,
                "rb_convert_type",
                object,
                coreLibrary().stringClass,
                coreSymbols().TO_STR);
    }

    @Override
    public RubyBaseNodeWithExecute cloneUninitialized() {
        return create(getChildNode().cloneUninitialized());
    }
}
