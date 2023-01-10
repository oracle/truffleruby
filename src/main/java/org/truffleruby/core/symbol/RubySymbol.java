/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.ImmutableRubyString;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.language.ImmutableRubyObjectNotCopyable;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.parser.IdentifierType;
import org.truffleruby.parser.Identifiers;

@ExportLibrary(InteropLibrary.class)
public final class RubySymbol extends ImmutableRubyObjectNotCopyable implements TruffleObject {

    public static final int UNASSIGNED_ID = -1;

    private static final int CLASS_SALT = 92021474; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

    public final RubyEncoding encoding;
    private final String string;
    public final TruffleString tstring;
    private final int javaStringHashCode;
    private final long id;
    private ImmutableRubyString name;
    private final IdentifierType type;

    private volatile RootCallTarget callTargetNoRefinements = null;

    RubySymbol(String string, TruffleString tstring, RubyEncoding encoding, long id) {
        assert tstring.isManaged();
        assert tstring.isCompatibleTo(encoding.tencoding);
        this.encoding = encoding;
        this.string = string;
        this.tstring = tstring;
        this.javaStringHashCode = string.hashCode();
        this.id = id;
        this.type = Identifiers.stringToType(string);
    }

    RubySymbol(String string, TruffleString tstring, RubyEncoding encoding) {
        this(string, tstring, encoding, UNASSIGNED_ID);
    }

    public long getId() {
        return id;
    }

    public String getString() {
        return string;
    }

    public IdentifierType getType() {
        return this.type;
    }

    @TruffleBoundary
    public RootCallTarget getCallTargetNoRefinements(RubyLanguage language) {
        if (callTargetNoRefinements == null) {
            synchronized (this) {
                if (callTargetNoRefinements == null) {
                    callTargetNoRefinements = SymbolNodes.ToProcNode
                            .createCallTarget(language, this, DeclarationContext.NO_REFINEMENTS);
                }
            }
        }

        return callTargetNoRefinements;
    }

    public long computeHashCode(Hashing hashing) {
        return hashing.hash(CLASS_SALT, javaStringHashCode);
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return ":" + string;
    }

    public ImmutableRubyString getName(RubyLanguage language) {
        if (name == null) {
            name = language.getFrozenStringLiteral(tstring, encoding);
        }
        return name;
    }

    // region InteropLibrary messages
    @Override
    @ExportMessage
    public String toDisplayString(boolean allowSideEffects) {
        return toString();
    }

    @ExportMessage
    public boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    public RubyClass getMetaObject(
            @CachedLibrary("this") InteropLibrary node) {
        return RubyContext.get(node).getCoreLibrary().symbolClass;
    }

    // region String messages
    @ExportMessage
    public boolean isString() {
        return true;
    }

    @ExportMessage
    public static class AsString {
        @Specialization(guards = "symbol == cachedSymbol", limit = "getIdentityCacheLimit()")
        protected static String asStringCached(RubySymbol symbol,
                @Cached("symbol") RubySymbol cachedSymbol,
                @Cached("cachedSymbol.getString()") String cachedString) {
            return cachedString;
        }

        @Specialization(replaces = "asStringCached")
        protected static String asStringUncached(RubySymbol symbol) {
            return symbol.getString();
        }

        protected static int getIdentityCacheLimit() {
            return RubyLanguage.getCurrentLanguage().options.IDENTITY_CACHE;
        }
    }
    // endregion
    // endregion

}
