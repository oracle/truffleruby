/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;

/** The Ruby {@code nil}, the single instance of NilClass. */
@ExportLibrary(InteropLibrary.class)
public final class Nil extends ImmutableRubyObject implements TruffleObject {

    public static final Nil INSTANCE = new Nil();

    private Nil() {
    }

    @Override
    public String toString() {
        return "nil";
    }

    @ExportMessage
    protected boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    protected Class<RubyLanguage> getLanguage() {
        return RubyLanguage.class;
    }

    @ExportMessage
    protected String toDisplayString(boolean allowSideEffects) {
        return "nil";
    }

    @ExportMessage
    protected boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    protected DynamicObject getMetaObject(
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().nilClass;
    }

    @ExportMessage
    protected boolean isNull() {
        return true;
    }

}
