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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.Rope;

@ExportLibrary(InteropLibrary.class)
public class RubySymbol implements TruffleObject {

    private final String string;
    private final Rope rope;
    private final int hashCode;

    public RubySymbol(String string, Rope rope, int hashCode) {
        this.string = string;
        this.rope = rope;
        this.hashCode = hashCode;
    }

    public String getString() {
        return string;
    }

    public Rope getRope() {
        return rope;
    }

    public int getHashCode() {
        return hashCode;
    }

    @ExportMessage
    protected static boolean isString(RubySymbol symbol) {
        return true;
    }

    @ExportMessage
    public static class AsString {

        @Specialization(guards = "symbol == cachedSymbol", limit = "getLimit()")
        protected static String asString(RubySymbol symbol,
                @Cached("symbol") RubySymbol cachedSymbol,
                @Cached("uncachedAsString(cachedSymbol)") String cachedString) {
            return cachedString;
        }

        @Specialization(replaces = "asString")
        protected static String uncachedAsString(RubySymbol symbol) {
            return symbol.getString();
        }

        protected static int getLimit() {
            return RubyLanguage.getCurrentContext().getOptions().INTEROP_CONVERT_CACHE;
        }
    }


}
