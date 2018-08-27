/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;

public abstract class ToSymbolNode extends RubyBaseNode {

    public abstract DynamicObject executeToSymbol(VirtualFrame frame, Object object);

    // TODO(CS): cache the conversion to a symbol? Or should the user do that themselves?

    @Specialization(guards = "isRubySymbol(symbol)")
    protected DynamicObject toSymbolSymbol(DynamicObject symbol) {
        return symbol;
    }

    @Specialization(guards = "isRubyString(string)")
    protected DynamicObject toSymbolString(DynamicObject string) {
        return getSymbol(StringOperations.rope(string));
    }

    @Specialization
    protected DynamicObject toSymbol(String string) {
        return getSymbol(string);
    }

}
