/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop.messages;

import org.truffleruby.Layouts;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyLanguage;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class SymbolMessages extends RubyObjectMessages {

    @Override
    public Class<?> dispatch() {
        return SymbolMessages.class;
    }

    @ExportMessage
    public static boolean isString(DynamicObject symbol) {
        return true;
    }

    @ExportMessage
    public static class AsString {

        @Specialization(guards = "symbol == cachedSymbol", limit = "getLimit()")
        protected static String asString(DynamicObject symbol,
                @Cached("symbol") DynamicObject cachedSymbol,
                @Cached("uncachedAsString(cachedSymbol)") String cachedString) {
            return cachedString;
        }

        @Specialization(replaces = "asString")
        protected static String uncachedAsString(DynamicObject symbol) {
            return Layouts.SYMBOL.getString(symbol);
        }

        protected static int getLimit() {
            return RubyLanguage.getCurrentContext().getOptions().INTEROP_CONVERT_CACHE;
        }
    }

}
