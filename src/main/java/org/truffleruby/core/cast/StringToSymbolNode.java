/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Creates a symbol from a string.
 */
@NodeChild("string")
public abstract class StringToSymbolNode extends RubyNode {

    @Specialization(guards = "isRubyString(string)")
    protected DynamicObject doString(DynamicObject string) {
        return getSymbol(Layouts.STRING.getRope(string));
    }

}
