/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import java.util.Collection;

import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeCache;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.RopeWithEncoding;
import org.truffleruby.core.rope.TStringCache;
import org.truffleruby.core.string.StringOperations;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class SymbolTable {

    private final TStringCache tstringCache;
    private final RopeCache ropeCache;

    // A cache for j.l.String to Symbols. Entries are kept as long as the Symbol is alive.
    // However, this doesn't matter as the cache entries will be re-created when used.
    private final WeakValueCache<String, RubySymbol> stringToSymbolCache = new WeakValueCache<>();

    // Weak map of RopeWithEncoding to Symbol to keep Symbols unique.
    // As long as the Symbol is referenced, the entry will stay in the symbolMap.
    private final WeakValueCache<RopeWithEncoding, RubySymbol> symbolMap = new WeakValueCache<>();

    public SymbolTable(TStringCache tstringCache, RopeCache ropeCache, CoreSymbols coreSymbols) {
        this.tstringCache = tstringCache;
        this.ropeCache = ropeCache;
        addCoreSymbols(coreSymbols);
    }

    private void addCoreSymbols(CoreSymbols coreSymbols) {
        for (RubySymbol symbol : coreSymbols.CORE_SYMBOLS) {
            final Rope rope = symbol.getRope();
            final RopeWithEncoding ropeWithEncoding = normalizeRopeForLookup(rope, symbol.encoding);
            assert rope == ropeWithEncoding.getRope();
            assert rope == ropeCache.getRope(rope);

            final RubySymbol existing = symbolMap.put(ropeWithEncoding, symbol);
            if (existing != null) {
                throw new AssertionError("Duplicate Symbol in SymbolTable: " + existing);
            }

            final RubySymbol old = stringToSymbolCache.put(symbol.getString(), symbol);
            if (old != null) {
                throw new AssertionError("Duplicate Symbol in SymbolTable: " + old);
            }
        }
    }

    @TruffleBoundary
    public RubySymbol getSymbol(String string) {
        RubySymbol symbol = stringToSymbolCache.get(string);
        if (symbol != null) {
            return symbol;
        }

        final LeafRope rope;
        final RubyEncoding encoding;
        if (StringOperations.isAsciiOnly(string)) {
            rope = RopeOperations.encodeAscii(string, USASCIIEncoding.INSTANCE);
            encoding = Encodings.US_ASCII;
        } else {
            rope = StringOperations.encodeRope(string, UTF8Encoding.INSTANCE);
            encoding = Encodings.UTF_8;
        }
        symbol = getSymbol(rope, encoding);

        // Add it to the direct java.lang.String to Symbol cache
        stringToSymbolCache.addInCacheIfAbsent(string, symbol);

        return symbol;
    }

    @TruffleBoundary
    public RubySymbol getSymbol(Rope rope, RubyEncoding encoding) {
        final RopeWithEncoding ropeEncodingForLookup = normalizeRopeForLookup(rope, encoding);
        final RubySymbol symbol = symbolMap.get(ropeEncodingForLookup);
        if (symbol != null) {
            return symbol;
        }

        final LeafRope cachedRope = ropeCache.getRope(ropeEncodingForLookup.getRope());
        final RubyEncoding symbolEncoding = ropeEncodingForLookup.getEncoding();
        final TruffleString cachedTString = tstringCache.getTString(cachedRope.getBytes(), symbolEncoding);
        final RubySymbol newSymbol = createSymbol(cachedRope, cachedTString, symbolEncoding);
        // Use a RopeWithEncoding with the cached Rope in symbolMap, since the Symbol refers to it and so we
        // do not keep the other Rope alive unnecessarily.
        return symbolMap.addInCacheIfAbsent(new RopeWithEncoding(cachedRope, symbolEncoding), newSymbol);
    }

    @TruffleBoundary
    public RubySymbol getSymbolIfExists(Rope rope, RubyEncoding encoding) {
        final RopeWithEncoding ropeKey = normalizeRopeForLookup(rope, encoding);
        return symbolMap.get(ropeKey);
    }

    private RopeWithEncoding normalizeRopeForLookup(Rope rope, RubyEncoding encoding) {
        if (rope instanceof NativeRope) {
            rope = ((NativeRope) rope).toLeafRope();
        }

        if (rope.isAsciiOnly() && rope.getEncoding() != USASCIIEncoding.INSTANCE) {
            rope = RopeOperations.withEncoding(rope, USASCIIEncoding.INSTANCE);
            encoding = Encodings.US_ASCII;
        }

        return new RopeWithEncoding(rope, encoding);
    }

    private RubySymbol createSymbol(LeafRope cachedRope, TruffleString truffleString, RubyEncoding encoding) {
        final String string = RopeOperations.decodeOrEscapeBinaryRope(cachedRope);
        return new RubySymbol(string, cachedRope, truffleString, encoding);
    }

    @TruffleBoundary
    public Collection<RubySymbol> allSymbols() {
        // allSymbols is a private concrete collection not a view
        return symbolMap.values();
    }

}
