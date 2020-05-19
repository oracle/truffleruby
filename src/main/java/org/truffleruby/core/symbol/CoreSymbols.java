/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.core.rope.RopeOperations;

import java.util.Arrays;
import java.util.List;

public class CoreSymbols {

    public static final RubySymbol IMMEDIATE_SYMBOL = createRubySymbol("immediate");
    public static final RubySymbol ON_BLOCKING_SYMBOL = createRubySymbol("on_blocking");
    public static final RubySymbol NEVER_SYMBOL = createRubySymbol("never");

    public static List<RubySymbol> CORE_SYMBOLS = Arrays.asList(IMMEDIATE_SYMBOL, ON_BLOCKING_SYMBOL, NEVER_SYMBOL);

    private static RubySymbol createRubySymbol(String string) {
        return new RubySymbol(string, RopeOperations.encodeAscii(string, USASCIIEncoding.INSTANCE));
    }

}
