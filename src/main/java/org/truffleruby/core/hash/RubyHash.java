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
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.StopIterationException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownKeyException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

@ExportLibrary(InteropLibrary.class)
public class RubyHash extends RubyDynamicObject implements ObjectGraphNode {

    public Object defaultBlock;
    public Object defaultValue;
    public Object store;
    public int size;
    public Entry firstInSequence;
    public Entry lastInSequence;
    public boolean compareByIdentity;
    public boolean ruby2_keywords = false;

    public RubyHash(
            RubyClass rubyClass,
            Shape shape,
            RubyContext context,
            Object store,
            int size,
            Entry firstInSequence,
            Entry lastInSequence,
            Object defaultBlock,
            Object defaultValue,
            boolean compareByIdentity) {
        super(rubyClass, shape);
        this.defaultBlock = defaultBlock;
        this.defaultValue = defaultValue;
        this.store = store;
        this.size = size;
        this.firstInSequence = firstInSequence;
        this.lastInSequence = lastInSequence;
        this.compareByIdentity = compareByIdentity;

        if (context.isPreInitializing()) {
            context.getPreInitializationManager().addPreInitHash(this);
        }
    }

    @TruffleBoundary
    public void getAdjacentObjects(Set<Object> reachable) {
        if (store instanceof Entry[]) {
            BucketsStrategy.getAdjacentObjects(reachable, firstInSequence);
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

    private static final class DefaultProvider implements BiFunctionNode {
        final Object defaultValue;

        private DefaultProvider(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public Object accept(VirtualFrame frame, Object hash, Object key) {
            return defaultValue;
        }
    }

    private static final DefaultProvider NULL_PROVIDER = new DefaultProvider(null);

    @ExportMessage(name = "isHashEntryExisting")
    @ExportMessage(name = "isHashEntryReadable")
    public final boolean isHashEntryExisting(Object key,
            @Cached @Shared("lookup") HashNodes.HashLookupOrExecuteDefaultNode lookup,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby) {
        return lookup.executeGet(null, this, toRuby.executeConvert(key), NULL_PROVIDER) != null;
    }

    @ExportMessage(name = "isHashEntryModifiable")
    @ExportMessage(name = "isHashEntryRemovable")
    public boolean isHashEntryModifiableAndRemovable(Object key,
            @Cached @Shared("lookup") HashNodes.HashLookupOrExecuteDefaultNode lookup,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby) {
        return !rubyLibrary.isFrozen(this) &&
                lookup.executeGet(null, this, toRuby.executeConvert(key), NULL_PROVIDER) != null;
    }

    @ExportMessage
    public boolean isHashEntryInsertable(Object key,
            @Cached @Shared("lookup") HashNodes.HashLookupOrExecuteDefaultNode lookup,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby) {
        return !rubyLibrary.isFrozen(this) &&
                lookup.executeGet(null, this, toRuby.executeConvert(key), NULL_PROVIDER) == null;
    }

    @ExportMessage
    public Object readHashValue(Object key,
            @Cached @Shared("lookup") HashNodes.HashLookupOrExecuteDefaultNode lookup,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby,
            @Cached ConditionProfile unknownKey)
            throws UnknownKeyException {
        final Object value = lookup.executeGet(null, this, toRuby.executeConvert(key), NULL_PROVIDER);
        if (unknownKey.profile(value == null)) {
            throw UnknownKeyException.create(key);
        }
        return value;
    }

    @ExportMessage
    public Object readHashValueOrDefault(Object key, Object defaultValue,
            @Cached @Shared("lookup") HashNodes.HashLookupOrExecuteDefaultNode lookup,
            @Cached @Shared("toRuby") ForeignToRubyNode toRuby) {
        return lookup.executeGet(null, this, toRuby.executeConvert(key), new DefaultProvider(defaultValue));
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
            @Cached @Shared("enumerator") DispatchNode toEnum) {
        return new RubyHashIterator(toEnum.call(this, "to_enum"), RubyHashIterator.Type.ENTRIES);
    }

    @ExportMessage
    public Object getHashKeysIterator(
            @Cached @Shared("enumerator") DispatchNode toEnum) {
        return new RubyHashIterator(toEnum.call(this, "to_enum"), RubyHashIterator.Type.KEYS);
    }

    @ExportMessage
    public Object getHashValuesIterator(
            @Cached @Shared("enumerator") DispatchNode toEnum) {
        return new RubyHashIterator(toEnum.call(this, "to_enum"), RubyHashIterator.Type.VALUES);
    }

    @ImportStatic(ArrayGuards.class)
    @ExportLibrary(InteropLibrary.class)
    public static class RubyHashIterator implements TruffleObject {
        enum Type {
            ENTRIES,
            KEYS,
            VALUES
        }

        private final Object enumerator;
        private final Type type;

        RubyHashIterator(Object enumerator, Type type) {
            this.enumerator = enumerator;
            this.type = type;
        }

        @ExportMessage
        public boolean isIterator() {
            return true;
        }

        @ExportMessage
        public boolean hasIteratorNextElement(
                @Cached @Exclusive DispatchNode peek,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            try {
                peek.call(enumerator, "peek");
                return true;
            } catch (RaiseException e) {
                if (e.getException().getLogicalClass() == context.getCoreLibrary().stopIterationClass) {
                    return false;
                }
                throw e;
            }
        }

        @ExportMessage
        public Object getIteratorNextElement(
                @Cached @Exclusive DispatchNode next,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary arrays,
                @CachedContext(RubyLanguage.class) RubyContext context) throws StopIterationException {
            try {
                final RubyArray array = (RubyArray) next.call(enumerator, "next");
                switch (type) {
                    case ENTRIES:
                        return array;
                    case KEYS:
                        return arrays.read(array.store, 0);
                    case VALUES:
                        return arrays.read(array.store, 1);
                }
                throw new Error("unreachable");
            } catch (RaiseException e) {
                if (e.getException().getLogicalClass() == context.getCoreLibrary().stopIterationClass) {
                    throw StopIterationException.create();
                }
                throw e;
            }
        }
    }

    // endregion
}
