/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.math.BigInteger;

import org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.CoreStrings;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

@TypeSystemReference(RubyTypes.class)
@ImportStatic(RubyGuards.class)
public abstract class RubyBaseNode extends Node {

    @CompilationFinal private RubyContext context;

    // Guards which use the context and so can't be static

    protected boolean isNil(Object value) {
        return value == nil();
    }

    // Helpers methods for terseness

    protected DynamicObject nil() {
        return coreLibrary().getNil();
    }

    protected DynamicObject getSymbol(String name) {
        return getContext().getSymbolTable().getSymbol(name);
    }

    protected DynamicObject getSymbol(Rope name) {
        return getContext().getSymbolTable().getSymbol(name);
    }

    protected Encoding getLocaleEncoding() {
        return getContext().getEncodingManager().getLocaleEncoding();
    }

    protected DynamicObject createArray(Object store, int size) {
        return ArrayHelpers.createArray(getContext(), store, size);
    }

    protected DynamicObject createBignum(BigInteger value) {
        return BignumOperations.createBignum(getContext(), value);
    }

    protected CoreStrings coreStrings() {
        return getContext().getCoreStrings();
    }

    protected CoreLibrary coreLibrary() {
        return getContext().getCoreLibrary();
    }

    protected CoreExceptions coreExceptions() {
        return getContext().getCoreExceptions();
    }

    protected int getDefaultCacheLimit() {
        return getContext().getOptions().DEFAULT_CACHE;
    }

    // Accessors

    public RubyContext getContext() {
        if (context == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            context = RubyLanguage.getCurrentContext();
        }

        return context;
    }

}
