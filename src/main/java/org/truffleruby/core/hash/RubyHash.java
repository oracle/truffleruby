/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnknownKeyException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.hash.library.BucketsHashStore;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

@ExportLibrary(InteropLibrary.class)
@ImportStatic(HashGuards.class)
public class RubyHash extends RubyDynamicObject implements ObjectGraphNode {

    public Object defaultBlock;
    public Object defaultValue;
    public Object store;
    public int size;
    public boolean compareByIdentity;
    public boolean ruby2_keywords = false;

    public RubyHash(
            RubyClass rubyClass,
            Shape shape,
            RubyContext context,
            Object store,
            int size,
            Object defaultBlock,
            Object defaultValue,
            boolean compareByIdentity) {
        super(rubyClass, shape);
        this.defaultBlock = defaultBlock;
        this.defaultValue = defaultValue;
        this.store = store;
        this.size = size;
        this.compareByIdentity = compareByIdentity;

        if (context.isPreInitializing()) {
            context.getPreInitializationManager().addPreInitHash(this);
        }
    }

    @TruffleBoundary
    public void getAdjacentObjects(Set<Object> reachable) {
        if (store instanceof BucketsHashStore) {
            ((BucketsHashStore) store).getAdjacentObjects(reachable);
        } else {
            ObjectGraph.addProperty(reachable, store);
        }

        ObjectGraph.addProperty(reachable, defaultBlock);
        ObjectGraph.addProperty(reachable, defaultValue);
    }

    // region InteropLibrary messages

    // NOTE(norswap, 07 Apr 2021): For now, ignore Ruby default values.

    @ExportMessage
    public boolean hasHashEntries() {
        return true;
    }

    @ExportMessage
    public long getHashSize() {
        return size;
    }

    private static final class DefaultProvider implements PEBiFunction {
        private final Object defaultValue;

        private DefaultProvider(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object accept(Frame frame, Object hash, Object key) {
            return defaultValue;
        }
    }

    private static final DefaultProvider NULL_PROVIDER = new DefaultProvider(null);

    @ExportMessage(name = "isHashEntryExisting", limit = "hashStrategyLimit()")
    @ExportMessage(name = "isHashEntryReadable", limit = "hashStrategyLimit()")
    public final boolean isHashEntryExisting(Object key,
            @CachedLibrary("this.store") HashStoreLibrary hashStores,
            @Cached @Exclusive ForeignToRubyNode toRuby) {
        return hashStores.lookupOrDefault(store, null, this, toRuby.executeConvert(key), NULL_PROVIDER) != null;
    }

    @ExportMessage(name = "isHashEntryModifiable")
    @ExportMessage(name = "isHashEntryRemovable")
    public boolean isHashEntryModifiableAndRemovable(Object key,
            @CachedLibrary("this") InteropLibrary interop,
            @CachedLibrary("this") RubyLibrary rubyLibrary) {
        return !rubyLibrary.isFrozen(this) && interop.isHashEntryExisting(this, key);
    }

    @ExportMessage
    public boolean isHashEntryInsertable(Object key,
            @CachedLibrary("this") InteropLibrary interop,
            @CachedLibrary("this") RubyLibrary rubyLibrary) {
        return !rubyLibrary.isFrozen(this) && !interop.isHashEntryExisting(this, key);
    }

    @ExportMessage(limit = "hashStrategyLimit()")
    public Object readHashValue(Object key,
            @CachedLibrary("this.store") HashStoreLibrary hashStores,
            @Cached @Exclusive ForeignToRubyNode toRuby,
            @Cached ConditionProfile unknownKey)
            throws UnknownKeyException {
        final Object value = hashStores.lookupOrDefault(store, null, this, toRuby.executeConvert(key), NULL_PROVIDER);
        if (unknownKey.profile(value == null)) {
            throw UnknownKeyException.create(key);
        }
        return value;
    }

    @ExportMessage(limit = "hashStrategyLimit()")
    public Object readHashValueOrDefault(Object key, Object defaultValue,
            @CachedLibrary("this.store") HashStoreLibrary hashStores,
            @Cached @Exclusive ForeignToRubyNode toRuby) {
        return hashStores
                .lookupOrDefault(store, null, this, toRuby.executeConvert(key), new DefaultProvider(defaultValue));
    }

    @ExportMessage
    public void writeHashEntry(Object key, Object value,
            @Cached @Exclusive DispatchNode set,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby)
            throws UnsupportedMessageException {
        if (rubyLibrary.isFrozen(this)) {
            throw UnsupportedMessageException.create();
        }
        set.call(this, "[]=", toRuby.executeConvert(key), value);
    }

    @ExportMessage
    public void removeHashEntry(Object key,
            @Cached @Exclusive DispatchNode delete,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @CachedLibrary("this") InteropLibrary interop,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby)
            throws UnsupportedMessageException, UnknownKeyException {
        if (rubyLibrary.isFrozen(this)) {
            throw UnsupportedMessageException.create();
        }
        if (!interop.isHashEntryExisting(this, key)) {
            throw UnknownKeyException.create(key);
        }
        delete.call(this, "delete", toRuby.executeConvert(key));
    }

    @ExportMessage
    public Object getHashEntriesIterator(
            @Cached @Shared("enumerator") DispatchNode eachPair) {
        return eachPair.call(this, "each_pair");
    }

    @ExportMessage
    public Object getHashKeysIterator(
            @Cached @Shared("enumerator") DispatchNode eachKey) {
        return eachKey.call(this, "each_key");
    }

    @ExportMessage
    public Object getHashValuesIterator(
            @Cached @Shared("enumerator") DispatchNode eachValue) {
        return eachValue.call(this, "each_value");
    }

    // endregion
}
