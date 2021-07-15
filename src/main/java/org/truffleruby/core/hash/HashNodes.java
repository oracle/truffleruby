/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.HashNodesFactory.InitializeCopyNodeFactory;
import org.truffleruby.core.hash.library.EmptyHashStore;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.core.hash.library.PackedHashStoreLibrary;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.shared.PropagateSharingNode;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Hash", isClass = true)
public abstract class HashNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyHash allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().hashShape;
            final EmptyHashStore store = EmptyHashStore.NULL_HASH_STORE;
            final RubyHash hash = new RubyHash(rubyClass, shape, getContext(), store, 0);
            AllocationTracing.trace(hash, this);
            return hash;
        }
    }

    @CoreMethod(names = "[]", constructor = true, rest = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ConstructNode extends CoreMethodArrayArgumentsNode {

        @Child private DispatchNode fallbackNode = DispatchNode.create();

        @ExplodeLoop(kind = LoopExplosionKind.FULL_UNROLL)
        @Specialization(guards = "isSmallArrayOfPairs(args, getLanguage())")
        protected Object construct(RubyClass hashClass, Object[] args,
                @Cached HashingNodes.ToHashByHashCode hashNode) {
            final RubyArray array = (RubyArray) args[0];

            final Object[] store = (Object[]) array.store;

            final int size = array.size;
            final Object[] newStore = PackedHashStoreLibrary.createStore();

            // written very carefully to allow PE
            for (int n = 0; n < PackedHashStoreLibrary.MAX_ENTRIES; n++) {
                if (n < size) {
                    final Object pair = store[n];

                    if (!RubyGuards.isRubyArray(pair)) {
                        return fallbackNode.call(hashClass, "_constructor_fallback", args);
                    }

                    final RubyArray pairArray = (RubyArray) pair;
                    final Object pairStore = pairArray.store;

                    if (pairStore.getClass() != Object[].class || pairArray.size != 2) {
                        return fallbackNode.call(hashClass, "_constructor_fallback", args);
                    }

                    final Object[] pairObjectStore = (Object[]) pairStore;

                    final Object key = pairObjectStore[0];
                    final Object value = pairObjectStore[1];

                    final int hashed = hashNode.execute(key);

                    PackedHashStoreLibrary.setHashedKeyValue(newStore, n, hashed, key, value);
                }
            }

            final Shape shape = getLanguage().hashShape;
            return new RubyHash(hashClass, shape, getContext(), newStore, size);
        }

        @Specialization(guards = "!isSmallArrayOfPairs(args, getLanguage())")
        protected Object constructFallback(RubyClass hashClass, Object[] args) {
            return fallbackNode.call(hashClass, "_constructor_fallback", args);
        }

        public boolean isSmallArrayOfPairs(Object[] args, RubyLanguage language) {
            if (args.length != 1) {
                return false;
            }

            final Object arg = args[0];

            if (!RubyGuards.isRubyArray(arg)) {
                return false;
            }

            final RubyArray array = (RubyArray) arg;
            final Object store = array.store;

            if (store == null || store.getClass() != Object[].class) {
                return false;
            }

            final Object[] objectStore = (Object[]) store;

            return objectStore.length <= PackedHashStoreLibrary.MAX_ENTRIES;
        }
    }

    @CoreMethod(names = "ruby2_keywords_hash?", onSingleton = true, required = 1)
    public abstract static class IsRuby2KeywordsHashNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isRuby2KeywordsHash(RubyHash hash) {
            return hash.ruby2_keywords;
        }
    }

    @Primitive(name = "hash_mark_ruby2_keywords")
    public abstract static class HashMarkRuby2KeywordsNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyHash markRuby2Keywords(RubyHash hash) {
            hash.ruby2_keywords = true;
            return hash;
        }
    }

    @CoreMethod(names = "[]", required = 1)
    @ImportStatic(HashGuards.class)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode implements PEBiFunction {

        @Child private DispatchNode callDefaultNode;

        @Specialization(limit = "hashStrategyLimit()")
        protected Object get(RubyHash hash, Object key,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            return hashes.lookupOrDefault(hash.store, null, hash, key, this);
        }

        @Override
        public Object accept(Frame frame, Object hash, Object key) {
            if (callDefaultNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callDefaultNode = insert(DispatchNode.create());
            }
            return callDefaultNode.call(hash, "default", key);
        }
    }

    @Primitive(name = "hash_get_or_undefined")
    @ImportStatic(HashGuards.class)
    public abstract static class GetOrUndefinedNode extends PrimitiveArrayArgumentsNode implements PEBiFunction {

        @Specialization(limit = "hashStrategyLimit()")
        protected Object getOrUndefined(RubyHash hash, Object key,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            return hashes.lookupOrDefault(hash.store, null, hash, key, this);
        }

        @Override
        public Object accept(Frame frame, Object hash, Object key) {
            return NotProvided.INSTANCE;
        }
    }

    @CoreMethod(names = "[]=", required = 2, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "hashStrategyLimit()")
        protected Object set(RubyHash hash, Object key, Object value,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            hashes.set(hash.store, hash, key, value, hash.compareByIdentity);
            return value;
        }
    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "2")
        protected RubyHash clear(RubyHash hash,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            hashes.clear(hash.store, hash);
            return hash;
        }
    }

    @CoreMethod(names = "compare_by_identity", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class CompareByIdentityNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isCompareByIdentity(hash)", limit = "hashStrategyLimit()")
        protected RubyHash compareByIdentity(RubyHash hash,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            hash.compareByIdentity = true;
            hashes.rehash(hash.store, hash);
            return hash;
        }

        @Specialization(guards = "isCompareByIdentity(hash)")
        protected RubyHash alreadyCompareByIdentity(RubyHash hash) {
            return hash;
        }
    }

    @CoreMethod(names = "compare_by_identity?")
    public abstract static class IsCompareByIdentityNode extends CoreMethodArrayArgumentsNode {

        private final ConditionProfile profile = ConditionProfile.create();

        @Specialization
        protected boolean compareByIdentity(RubyHash hash) {
            return profile.profile(hash.compareByIdentity);
        }
    }

    @CoreMethod(names = "default_proc")
    public abstract static class DefaultProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object defaultProc(RubyHash hash) {
            return hash.defaultBlock;
        }
    }

    @Primitive(name = "hash_default_value")
    public abstract static class DefaultValueNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object defaultValue(RubyHash hash) {
            return hash.defaultValue;
        }
    }

    @CoreMethod(names = "delete", required = 1, needsBlock = true, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class DeleteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "hashStrategyLimit()")
        protected Object delete(RubyHash hash, Object key, Object maybeBlock,
                @CachedLibrary("hash.store") HashStoreLibrary hashes,
                @Cached CallBlockNode yieldNode,
                @Cached ConditionProfile hasValue,
                @Cached ConditionProfile hasBlock) {
            final Object value = hashes.delete(hash.store, hash, key);
            if (hasValue.profile(value != null)) {
                return value;
            } else if (hasBlock.profile(maybeBlock != nil)) {
                return yieldNode.yield((RubyProc) maybeBlock, key);
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = { "each", "each_pair" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(HashGuards.class)
    public abstract static class EachNode extends CoreMethodArrayArgumentsNode implements EachEntryCallback {

        @Child HashStoreLibrary.YieldPairNode yieldPair = HashStoreLibrary.YieldPairNode.create();
        @Child ArrayBuilderNode arrayBuilder = ArrayBuilderNode.create();

        @Specialization(limit = "hashStrategyLimit()")
        protected RubyHash each(RubyHash hash, RubyProc block,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            hashes.eachEntrySafe(hash.store, hash, this, block);
            return hash;
        }

        @Override
        public void accept(int index, Object key, Object value, Object state) {
            yieldPair.execute((RubyProc) state, key, value);
        }
    }

    @CoreMethod(names = "empty?")
    @ImportStatic(HashGuards.class)
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isEmpty(RubyHash hash) {
            return hash.size == 0;
        }
    }

    @CoreMethod(names = "initialize", needsBlock = true, optional = 1, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyHash initialize(RubyHash hash, NotProvided defaultValue, Nil block) {
            assert HashStoreLibrary.verify(hash);
            hash.defaultValue = nil;
            hash.defaultBlock = nil;
            return hash;
        }

        @Specialization
        protected RubyHash initialize(RubyHash hash, NotProvided defaultValue, RubyProc block,
                @Cached PropagateSharingNode propagateSharingNode) {
            assert HashStoreLibrary.verify(hash);
            hash.defaultValue = nil;
            propagateSharingNode.executePropagate(hash, block);
            hash.defaultBlock = block;
            return hash;
        }

        @Specialization(guards = "wasProvided(defaultValue)")
        protected RubyHash initialize(RubyHash hash, Object defaultValue, Nil block,
                @Cached PropagateSharingNode propagateSharingNode) {
            assert HashStoreLibrary.verify(hash);
            propagateSharingNode.executePropagate(hash, defaultValue);
            hash.defaultValue = defaultValue;
            hash.defaultBlock = nil;
            return hash;
        }

        @Specialization(guards = "wasProvided(defaultValue)")
        protected Object initialize(RubyHash hash, Object defaultValue, RubyProc block) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().argumentError("wrong number of arguments (1 for 0)", this));
        }

    }

    @CoreMethod(names = { "initialize_copy", "replace" }, required = 1, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        public static InitializeCopyNode create() {
            return InitializeCopyNodeFactory.create(null);
        }

        public abstract RubyHash execute(RubyHash self, RubyHash from);

        @Specialization(limit = "hashStrategyLimit()")
        protected RubyHash replace(RubyHash self, RubyHash from,
                @CachedLibrary("from.store") HashStoreLibrary hashes) {
            hashes.replace(from.store, from, self);
            return self;
        }

        @Specialization(guards = "!isRubyHash(from)")
        protected RubyHash replaceCoerce(RubyHash self, Object from,
                @Cached DispatchNode coerceNode,
                @Cached InitializeCopyNode initializeCopy) {
            final Object otherHash = coerceNode.call(
                    coreLibrary().truffleTypeModule,
                    "coerce_to",
                    from,
                    coreLibrary().hashClass,
                    coreSymbols().TO_HASH);
            return initializeCopy.execute(self, (RubyHash) otherHash);
        }
    }

    @CoreMethod(names = { "map", "collect" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(HashGuards.class)
    public abstract static class MapNode extends CoreMethodArrayArgumentsNode implements EachEntryCallback {

        @Child HashStoreLibrary.YieldPairNode yieldPair = HashStoreLibrary.YieldPairNode.create();
        @Child ArrayBuilderNode arrayBuilder = ArrayBuilderNode.create();

        private static class MapState {
            final BuilderState builderState;
            final RubyProc block;

            private MapState(BuilderState builderState, RubyProc block) {
                this.builderState = builderState;
                this.block = block;
            }
        }

        @Specialization(limit = "hashStrategyLimit()")
        protected RubyArray map(RubyHash hash, RubyProc block,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            final int size = hash.size;
            final BuilderState state = arrayBuilder.start(size);
            hashes.eachEntrySafe(hash.store, hash, this, new MapState(state, block));
            return ArrayHelpers.createArray(getContext(), getLanguage(), arrayBuilder.finish(state, size), size);
        }

        @Override
        public void accept(int index, Object key, Object value, Object state) {
            final MapState mapState = (MapState) state;
            arrayBuilder.appendValue(mapState.builderState, index, yieldPair.execute(mapState.block, key, value));
        }
    }

    @Primitive(name = "hash_set_default_proc")
    public abstract static class SetDefaultProcNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyProc setDefaultProc(RubyHash hash, RubyProc defaultProc,
                @Cached PropagateSharingNode propagateSharingNode) {
            propagateSharingNode.executePropagate(hash, defaultProc);
            hash.defaultValue = nil;
            hash.defaultBlock = defaultProc;
            return defaultProc;
        }

        @Specialization
        protected Object setDefaultProc(RubyHash hash, Nil defaultProc) {
            hash.defaultValue = nil;
            hash.defaultBlock = nil;
            return nil;
        }
    }

    @CoreMethod(names = "default=", required = 1, raiseIfFrozenSelf = true)
    public abstract static class SetDefaultValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object setDefault(RubyHash hash, Object defaultValue,
                @Cached PropagateSharingNode propagateSharingNode) {
            propagateSharingNode.executePropagate(hash, defaultValue);

            hash.defaultValue = defaultValue;
            hash.defaultBlock = nil;
            return defaultValue;
        }
    }

    @CoreMethod(names = "shift", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ShiftNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isEmptyHash(hash)")
        protected Object shiftEmpty(RubyHash hash,
                @Cached DispatchNode callDefault) {
            return callDefault.call(hash, "default", nil);
        }

        @Specialization(guards = "!isEmptyHash(hash)", limit = "hashStrategyLimit()")
        protected RubyArray shift(RubyHash hash,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            return hashes.shift(hash.store, hash);
        }
    }

    @CoreMethod(names = { "size", "length" })
    @ImportStatic(HashGuards.class)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int size(RubyHash hash) {
            return hash.size;
        }
    }

    @CoreMethod(names = "rehash", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class RehashNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isCompareByIdentity(hash)")
        protected RubyHash rehashIdentity(RubyHash hash) {
            // the identity hash of objects never change.
            return hash;
        }

        @Specialization(guards = "!isCompareByIdentity(hash)", limit = "hashStrategyLimit()")
        protected RubyHash rehashNotIdentity(RubyHash hash,
                @CachedLibrary("hash.store") HashStoreLibrary hashes) {
            hashes.rehash(hash.store, hash);
            return hash;
        }
    }
}
