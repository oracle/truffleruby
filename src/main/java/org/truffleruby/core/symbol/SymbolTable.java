/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.hash.ReHashable;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeCache;
import org.truffleruby.core.rope.RopeKey;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.StringKey;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.Identifiers;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

public class SymbolTable implements ReHashable {

    private final RopeCache ropeCache;
    private final DynamicObjectFactory symbolFactory;
    private final Hashing hashing;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // A cache for j.l.String to Symbols. Entries might get GC'd quickly as nothing references the
    // StringKey. However, this doesn't matter as the cache entries will be re-created when used.
    private final Map<StringKey, SoftReference<DynamicObject>> stringToSymbolCache = new WeakHashMap<>();

    // Weak map of RopeKey to Symbol to keep Symbols unique.
    // As long as the Symbol is referenced, the entry will stay in the symbolMap.
    private final WeakValueCache<RopeKey, DynamicObject> symbolMap = new WeakValueCache<>();

    public SymbolTable(RopeCache ropeCache, DynamicObjectFactory symbolFactory, RubyContext context) {
        this.ropeCache = ropeCache;
        this.symbolFactory = symbolFactory;
        this.hashing = context.getHashing(this);
    }

    @TruffleBoundary
    public DynamicObject getSymbol(String string) {
        final StringKey stringKey = new StringKey(string, hashing);
        DynamicObject symbol;

        final Lock readLock = lock.readLock();

        readLock.lock();
        try {
            symbol = lookupCache(stringToSymbolCache, stringKey);
            if (symbol != null) {
                return symbol;
            }
        } finally {
            readLock.unlock();
        }

        final Rope rope;
        if (StringOperations.isAsciiOnly(string)) {
            rope = RopeOperations.encodeAscii(string, USASCIIEncoding.INSTANCE);
        } else {
            rope = StringOperations.encodeRope(string, UTF8Encoding.INSTANCE);
        }
        symbol = getSymbol(rope);

        // Add it to the direct j.l.String to Symbol cache

        final Lock writeLock = lock.writeLock();

        writeLock.lock();
        try {
            if (lookupCache(stringToSymbolCache, stringKey) == null) {
                stringToSymbolCache.put(stringKey, new SoftReference<>(symbol));
            }
        } finally {
            writeLock.unlock();
        }

        return symbol;
    }

    @TruffleBoundary
    public DynamicObject getSymbol(Rope rope) {
        if (rope instanceof NativeRope) {
            rope = ((NativeRope) rope).toLeafRope();
        }

        if (rope.isAsciiOnly() && rope.getEncoding() != USASCIIEncoding.INSTANCE) {
            rope = rope.withEncoding(USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

        final RopeKey ropeKey = new RopeKey(rope, hashing);

        final DynamicObject symbol = symbolMap.get(ropeKey);
        if (symbol != null) {
            return symbol;
        }

        final Rope cachedRope = ropeCache.getRope(rope);
        final DynamicObject newSymbol = createSymbol(cachedRope);
        // Use a RopeKey with the cached Rope in symbolMap, since the Symbol refers to it and so we
        // do not keep rope alive unnecessarily.
        final RopeKey cachedRopeKey = new RopeKey(cachedRope, hashing);
        return symbolMap.addInCacheIfAbsent(cachedRopeKey, newSymbol);
    }

    private DynamicObject createSymbol(Rope cachedRope) {
        final String string = RopeOperations.decodeOrEscapeBinaryRope(cachedRope);
        return Layouts.SYMBOL.createSymbol(
                symbolFactory,
                string,
                cachedRope,
                computeSymbolHashCode(string));
    }

    private static final int CLASS_SALT = 92021474; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

    private long computeSymbolHashCode(final String string) {
        return hashing.hash(CLASS_SALT, string.hashCode());
    }

    private DynamicObject lookupCache(Map<StringKey, SoftReference<DynamicObject>> cache, StringKey key) {
        final SoftReference<DynamicObject> reference = cache.get(key);
        return reference == null ? null : reference.get();
    }

    @TruffleBoundary
    public Collection<DynamicObject> allSymbols() {
        return symbolMap.values();
    }

    public void rehash() {
        // Just a cache, so we can empty it and it will be repopulated as needed
        stringToSymbolCache.clear();

        symbolMap.rehash();

        // Recompute all Symbols's cached hashCode
        for (DynamicObject symbol : symbolMap.values()) {
            Layouts.SYMBOL.setHashCode(symbol, computeSymbolHashCode(Layouts.SYMBOL.getString(symbol)));
        }
    }

    // TODO (eregon, 10/10/2015): this check could be done when a Symbol is created to be much cheaper
    @TruffleBoundary(transferToInterpreterOnException = false)
    public static String checkInstanceVariableName(
            RubyContext context,
            String name,
            Object receiver,
            Node currentNode) {
        if (!Identifiers.isValidInstanceVariableName(name)) {
            throw new RaiseException(context, context.getCoreExceptions().nameErrorInstanceNameNotAllowable(
                    name,
                    receiver,
                    currentNode));
        }
        return name;
    }

    @TruffleBoundary(transferToInterpreterOnException = false)
    public static String checkClassVariableName(
            RubyContext context,
            String name,
            Object receiver,
            Node currentNode) {
        if (!Identifiers.isValidClassVariableName(name)) {
            throw new RaiseException(context, context.getCoreExceptions().nameErrorInstanceNameNotAllowable(
                    name,
                    receiver,
                    currentNode));
        }
        return name;
    }

}
