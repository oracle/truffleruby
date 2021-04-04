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

import java.util.Arrays;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.BiConsumerNode;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.HashNodesFactory.EachKeyValueNodeGen;
import org.truffleruby.core.hash.HashNodesFactory.HashLookupOrExecuteDefaultNodeGen;
import org.truffleruby.core.hash.HashNodesFactory.InitializeCopyNodeFactory;
import org.truffleruby.core.hash.HashNodesFactory.InternalRehashNodeGen;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.shared.PropagateSharingNode;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Hash", isClass = true)
public abstract class HashNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyHash allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().hashShape;
            final RubyHash hash = new RubyHash(rubyClass, shape, getContext(), null, 0, null, null, nil, nil, false);
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
            final Object[] newStore = PackedArrayStrategy.createStore(getLanguage());

            // written very carefully to allow PE
            for (int n = 0; n < getLanguage().options.HASH_PACKED_ARRAY_MAX; n++) {
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

                    PackedArrayStrategy.setHashedKeyValue(newStore, n, hashed, key, value);
                }
            }

            final Shape shape = getLanguage().hashShape;
            return new RubyHash(hashClass, shape, getContext(), newStore, size, null, null, nil, nil, false);
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

            if (objectStore.length > language.options.HASH_PACKED_ARRAY_MAX) {
                return false;
            }

            return true;
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

    @ImportStatic(HashGuards.class)
    public abstract static class HashLookupOrExecuteDefaultNode extends RubyContextNode {

        public static HashLookupOrExecuteDefaultNode create() {
            return HashLookupOrExecuteDefaultNodeGen.create();
        }

        public abstract Object executeGet(VirtualFrame frame, RubyHash hash, Object key,
                BiFunctionNode defaultValueNode);

        @Specialization(guards = "isNullHash(hash)")
        protected Object getNull(VirtualFrame frame, RubyHash hash, Object key, BiFunctionNode defaultValueNode) {
            return defaultValueNode.accept(frame, hash, key);
        }

        @Specialization(guards = "isPackedHash(hash)")
        protected Object getPackedArray(VirtualFrame frame, RubyHash hash, Object key, BiFunctionNode defaultValueNode,
                @Cached LookupPackedEntryNode lookupPackedEntryNode,
                @Cached HashingNodes.ToHash hashNode) {
            int hashed = hashNode.execute(key, hash.compareByIdentity); // Call key.hash only once
            return lookupPackedEntryNode.executePackedLookup(frame, hash, key, hashed, defaultValueNode);
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected Object getBuckets(VirtualFrame frame, RubyHash hash, Object key, BiFunctionNode defaultValueNode,
                @Cached("new()") LookupEntryNode lookupEntryNode,
                @Cached BranchProfile notInHashProfile) {
            final HashLookupResult hashLookupResult = lookupEntryNode.lookup(hash, key);

            if (hashLookupResult.getEntry() != null) {
                return hashLookupResult.getEntry().getValue();
            }

            notInHashProfile.enter();
            return defaultValueNode.accept(frame, hash, key);
        }

    }

    @CoreMethod(names = "[]", required = 1)
    @ImportStatic(HashGuards.class)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode implements BiFunctionNode {

        @Child private HashLookupOrExecuteDefaultNode lookupNode = HashLookupOrExecuteDefaultNode.create();
        @Child private DispatchNode callDefaultNode;

        public abstract Object executeGet(VirtualFrame frame, RubyHash hash, Object key);

        @Specialization
        protected Object get(VirtualFrame frame, RubyHash hash, Object key) {
            return lookupNode.executeGet(frame, hash, key, this);
        }

        @Override
        public Object accept(VirtualFrame frame, Object hash, Object key) {
            if (callDefaultNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callDefaultNode = insert(DispatchNode.create());
            }
            return callDefaultNode.call(hash, "default", key);
        }

    }

    @Primitive(name = "hash_get_or_undefined")
    public abstract static class GetOrUndefinedNode extends PrimitiveArrayArgumentsNode implements BiFunctionNode {

        @Child private HashLookupOrExecuteDefaultNode lookupNode = HashLookupOrExecuteDefaultNode.create();

        @Specialization
        protected Object getOrUndefined(VirtualFrame frame, RubyHash hash, Object key) {
            return lookupNode.executeGet(frame, hash, key, this);
        }

        public Object accept(VirtualFrame frame, Object hash, Object key) {
            return NotProvided.INSTANCE;
        }

    }

    @CoreMethod(names = "[]=", required = 2, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class SetIndexNode extends CoreMethodArrayArgumentsNode {

        @Child private SetNode setNode = SetNode.create();

        @Specialization
        protected Object set(RubyHash hash, Object key, Object value) {
            setNode.executeSet(hash, key, value, hash.compareByIdentity);
            return value;
        }

    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNullHash(hash)")
        protected RubyHash emptyNull(RubyHash hash) {
            return hash;
        }

        @Specialization(guards = "!isNullHash(hash)")
        protected RubyHash empty(RubyHash hash) {
            assert HashOperations.verifyStore(getContext(), hash);
            hash.store = null;
            hash.size = 0;
            hash.firstInSequence = null;
            hash.lastInSequence = null;
            assert HashOperations.verifyStore(getContext(), hash);
            return hash;
        }

    }

    @CoreMethod(names = "compare_by_identity", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class CompareByIdentityNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isCompareByIdentity(hash)")
        protected RubyHash compareByIdentity(RubyHash hash,
                @Cached InternalRehashNode internalRehashNode) {
            hash.compareByIdentity = true;
            return internalRehashNode.executeRehash(hash);
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

        @Child private CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();
        @Child private HashingNodes.ToHash hashNode = HashingNodes.ToHash.create();
        @Child private LookupEntryNode lookupEntryNode = new LookupEntryNode();
        @Child private CallBlockNode yieldNode = CallBlockNode.create();

        @Specialization(guards = "isNullHash(hash)")
        protected Object deleteNull(RubyHash hash, Object key, Nil block) {
            assert HashOperations.verifyStore(getContext(), hash);

            return nil;
        }

        @Specialization(guards = "isNullHash(hash)")
        protected Object deleteNull(RubyHash hash, Object key, RubyProc block) {
            assert HashOperations.verifyStore(getContext(), hash);

            return yieldNode.yield(block, key);
        }

        @Specialization(guards = "isPackedHash(hash)")
        protected Object deletePackedArray(RubyHash hash, Object key, Object maybeBlock,
                @Cached ConditionProfile byIdentityProfile) {
            assert HashOperations.verifyStore(getContext(), hash);
            final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);
            final int hashed = hashNode.execute(key, compareByIdentity);

            final Object[] store = (Object[]) hash.store;
            final int size = hash.size;

            for (int n = 0; n < getLanguage().options.HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                    final Object otherKey = PackedArrayStrategy.getKey(store, n);

                    if (equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed)) {
                        final Object value = PackedArrayStrategy.getValue(store, n);
                        PackedArrayStrategy.removeEntry(getLanguage(), store, n);
                        hash.size -= 1;
                        assert HashOperations.verifyStore(getContext(), hash);
                        return value;
                    }
                }
            }

            assert HashOperations.verifyStore(getContext(), hash);

            if (maybeBlock == nil) {
                return nil;
            } else {
                return yieldNode.yield((RubyProc) maybeBlock, key);
            }
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected Object delete(RubyHash hash, Object key, Object maybeBlock) {
            assert HashOperations.verifyStore(getContext(), hash);

            final HashLookupResult lookupResult = lookupEntryNode.lookup(hash, key);
            final Entry entry = lookupResult.getEntry();

            if (entry == null) {
                if (maybeBlock == nil) {
                    return nil;
                } else {
                    return yieldNode.yield((RubyProc) maybeBlock, key);
                }
            }

            BucketsStrategy.removeFromSequenceChain(hash, entry);

            if (entry.getNextInSequence() == null) {
                hash.lastInSequence = entry.getPreviousInSequence();
            } else {
                entry.getNextInSequence().setPreviousInSequence(entry.getPreviousInSequence());
            }

            BucketsStrategy
                    .removeFromLookupChain(hash, lookupResult.getIndex(), entry, lookupResult.getPreviousEntry());

            hash.size -= 1;

            assert HashOperations.verifyStore(getContext(), hash);
            return entry.getValue();
        }

        protected boolean equalKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey,
                int otherHashed) {
            return compareHashKeysNode.equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
        }

    }

    @CoreMethod(names = { "each", "each_pair" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(HashGuards.class)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        private final ConditionProfile arityMoreThanOne = ConditionProfile.create();

        @Specialization(guards = "isNullHash(hash)")
        protected RubyHash eachNull(RubyHash hash, RubyProc block) {
            return hash;
        }

        @ExplodeLoop
        @Specialization(guards = "isPackedHash(hash)")
        protected RubyHash eachPackedArray(RubyHash hash, RubyProc block) {
            assert HashOperations.verifyStore(getContext(), hash);
            final Object[] originalStore = (Object[]) hash.store;

            // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
            final int size = hash.size;
            final Object[] storeCopy = PackedArrayStrategy.copyStore(getLanguage(), originalStore);

            int n = 0;
            try {
                for (; n < getLanguage().options.HASH_PACKED_ARRAY_MAX; n++) {
                    if (n < size) {
                        yieldPair(
                                block,
                                PackedArrayStrategy.getKey(storeCopy, n),
                                PackedArrayStrategy.getValue(storeCopy, n));
                    }
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
            }

            return hash;
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected RubyHash eachBuckets(RubyHash hash, RubyProc block) {
            assert HashOperations.verifyStore(getContext(), hash);

            Entry entry = hash.firstInSequence;
            while (entry != null) {
                yieldPair(block, entry.getKey(), entry.getValue());
                entry = entry.getNextInSequence();
            }

            return hash;
        }

        private Object yieldPair(RubyProc block, Object key, Object value) {
            // MRI behavior, see rb_hash_each_pair()
            // We use getMethodArityNumber() here since for non-lambda the semantics are the same for both branches
            if (arityMoreThanOne.profile(block.sharedMethodInfo.getArity().getMethodArityNumber() > 1)) {
                return callBlock(block, key, value);
            } else {
                return callBlock(block, createArray(new Object[]{ key, value }));
            }
        }

    }

    @CoreMethod(names = "empty?")
    @ImportStatic(HashGuards.class)
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNullHash(hash)")
        protected boolean emptyNull(RubyHash hash) {
            return true;
        }

        @Specialization(guards = "!isNullHash(hash)")
        protected boolean emptyPackedArray(RubyHash hash) {
            return hash.size == 0;
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true, optional = 1, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyHash initialize(RubyHash hash, NotProvided defaultValue, Nil block) {
            assert HashOperations.verifyStore(getContext(), hash);
            hash.defaultValue = nil;
            hash.defaultBlock = nil;
            return hash;
        }

        @Specialization
        protected RubyHash initialize(RubyHash hash, NotProvided defaultValue, RubyProc block,
                @Cached PropagateSharingNode propagateSharingNode) {
            assert HashOperations.verifyStore(getContext(), hash);
            hash.defaultValue = nil;
            propagateSharingNode.executePropagate(hash, block);
            hash.defaultBlock = block;
            return hash;
        }

        @Specialization(guards = "wasProvided(defaultValue)")
        protected RubyHash initialize(RubyHash hash, Object defaultValue, Nil block,
                @Cached PropagateSharingNode propagateSharingNode) {
            assert HashOperations.verifyStore(getContext(), hash);
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

        @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        public static InitializeCopyNode create() {
            return InitializeCopyNodeFactory.create(null);
        }

        public abstract RubyHash executeReplace(RubyHash self, RubyHash from);

        @Specialization(guards = "isNullHash(from)")
        protected RubyHash replaceNull(RubyHash self, RubyHash from) {
            if (self == from) {
                return self;
            }

            propagateSharingNode.executePropagate(self, from);

            self.store = null;
            self.size = 0;
            self.firstInSequence = null;
            self.lastInSequence = null;

            copyOtherFields(self, from);

            assert HashOperations.verifyStore(getContext(), self);
            return self;
        }

        @Specialization(guards = "isPackedHash(from)")
        protected RubyHash replacePackedArray(RubyHash self, RubyHash from) {
            if (self == from) {
                return self;
            }

            propagateSharingNode.executePropagate(self, from);

            final Object[] store = (Object[]) from.store;
            Object storeCopy = PackedArrayStrategy.copyStore(getLanguage(), store);
            int size = from.size;
            self.store = storeCopy;
            self.size = size;
            self.firstInSequence = null;
            self.lastInSequence = null;

            copyOtherFields(self, from);

            assert HashOperations.verifyStore(getContext(), self);
            return self;
        }

        @TruffleBoundary
        @Specialization(guards = "isBucketHash(from)")
        protected RubyHash replaceBuckets(RubyHash self, RubyHash from) {
            if (self == from) {
                return self;
            }

            propagateSharingNode.executePropagate(self, from);

            BucketsStrategy.copyInto(getContext(), from, self);
            copyOtherFields(self, from);

            assert HashOperations.verifyStore(getContext(), self);
            return self;
        }

        @Specialization(guards = "!isRubyHash(from)")
        protected RubyHash replaceCoerce(RubyHash self, Object from,
                @Cached DispatchNode coerceNode,
                @Cached InitializeCopyNode initializeCopyNode) {
            final Object otherHash = coerceNode.call(
                    coreLibrary().truffleTypeModule,
                    "coerce_to",
                    from,
                    coreLibrary().hashClass,
                    coreSymbols().TO_HASH);
            return initializeCopyNode.executeReplace(self, (RubyHash) otherHash);
        }

        private void copyOtherFields(RubyHash self, RubyHash from) {
            self.defaultBlock = from.defaultBlock;
            self.defaultValue = from.defaultValue;
            self.compareByIdentity = from.compareByIdentity;
        }

    }

    @CoreMethod(names = { "map", "collect" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(HashGuards.class)
    public abstract static class MapNode extends YieldingCoreMethodNode {

        private final ConditionProfile arityMoreThanOne = ConditionProfile.create();

        @Specialization(guards = "isNullHash(hash)")
        protected RubyArray mapNull(RubyHash hash, RubyProc block) {
            assert HashOperations.verifyStore(getContext(), hash);
            return createEmptyArray();
        }

        @ExplodeLoop
        @Specialization(guards = "isPackedHash(hash)")
        protected RubyArray mapPackedArray(RubyHash hash, RubyProc block,
                @Cached ArrayBuilderNode arrayBuilderNode) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Object[] store = (Object[]) hash.store;

            final int length = hash.size;
            BuilderState state = arrayBuilderNode.start(length);

            try {
                for (int n = 0; n < getLanguage().options.HASH_PACKED_ARRAY_MAX; n++) {
                    if (n < length) {
                        final Object key = PackedArrayStrategy.getKey(store, n);
                        final Object value = PackedArrayStrategy.getValue(store, n);
                        arrayBuilderNode.appendValue(state, n, yieldPair(block, key, value));
                    }
                }
            } finally {
                LoopNode.reportLoopCount(this, length);
            }

            return createArray(arrayBuilderNode.finish(state, length), length);
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected RubyArray mapBuckets(RubyHash hash, RubyProc block,
                @Cached ArrayBuilderNode arrayBuilderNode) {
            assert HashOperations.verifyStore(getContext(), hash);

            final int length = hash.size;
            BuilderState state = arrayBuilderNode.start(length);

            int index = 0;

            try {
                Entry entry = hash.firstInSequence;
                while (entry != null) {
                    arrayBuilderNode
                            .appendValue(state, index, yieldPair(block, entry.getKey(), entry.getValue()));
                    index++;
                    entry = entry.getNextInSequence();
                }
            } finally {
                LoopNode.reportLoopCount(this, length);
            }

            return createArray(arrayBuilderNode.finish(state, length), length);
        }

        private Object yieldPair(RubyProc block, Object key, Object value) {
            // MRI behavior, see rb_hash_each_pair()
            // We use getMethodArityNumber() here since for non-lambda the semantics are the same for both branches
            if (arityMoreThanOne.profile(block.sharedMethodInfo.getArity().getMethodArityNumber() > 1)) {
                return callBlock(block, key, value);
            } else {
                return callBlock(block, createArray(new Object[]{ key, value }));
            }
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

        @Child private DispatchNode callDefaultNode = DispatchNode.create();

        @Specialization(guards = "isEmptyHash(hash)")
        protected Object shiftEmpty(RubyHash hash) {
            return callDefaultNode.call(hash, "default", nil);
        }

        @Specialization(guards = { "!isEmptyHash(hash)", "isPackedHash(hash)" })
        protected RubyArray shiftPackedArray(RubyHash hash) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Object[] store = (Object[]) hash.store;

            final Object key = PackedArrayStrategy.getKey(store, 0);
            final Object value = PackedArrayStrategy.getValue(store, 0);

            PackedArrayStrategy.removeEntry(getLanguage(), store, 0);

            hash.size -= 1;

            assert HashOperations.verifyStore(getContext(), hash);
            return createArray(new Object[]{ key, value });
        }

        @Specialization(guards = { "!isEmptyHash(hash)", "isBucketHash(hash)" })
        protected RubyArray shiftBuckets(RubyHash hash) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Entry first = hash.firstInSequence;
            assert first.getPreviousInSequence() == null;

            final Object key = first.getKey();
            final Object value = first.getValue();

            hash.firstInSequence = first.getNextInSequence();

            if (first.getNextInSequence() != null) {
                first.getNextInSequence().setPreviousInSequence(null);
                hash.firstInSequence = first.getNextInSequence();
            }

            if (hash.lastInSequence == first) {
                hash.lastInSequence = null;
            }

            final Entry[] store = (Entry[]) hash.store;
            final int index = BucketsStrategy.getBucketIndex(first.getHashed(), store.length);

            Entry previous = null;
            Entry entry = store[index];
            while (entry != null) {
                if (entry == first) {
                    if (previous == null) {
                        store[index] = first.getNextInLookup();
                    } else {
                        previous.setNextInLookup(first.getNextInLookup());
                    }
                    break;
                }

                previous = entry;
                entry = entry.getNextInLookup();
            }

            hash.size -= 1;

            assert HashOperations.verifyStore(getContext(), hash);
            return createArray(new Object[]{ key, value });
        }

    }

    @CoreMethod(names = { "size", "length" })
    @ImportStatic(HashGuards.class)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNullHash(hash)")
        protected int sizeNull(RubyHash hash) {
            return 0;
        }

        @Specialization(guards = "!isNullHash(hash)")
        protected int sizePackedArray(RubyHash hash) {
            return hash.size;
        }

    }

    @ImportStatic(HashGuards.class)
    public abstract static class InternalRehashNode extends RubyContextNode {

        @Child private HashingNodes.ToHash hashNode = HashingNodes.ToHash.create();
        @Child private CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();

        public static InternalRehashNode create() {
            return InternalRehashNodeGen.create();
        }

        public abstract RubyHash executeRehash(RubyHash hash);

        @Specialization(guards = "isNullHash(hash)")
        protected RubyHash rehashNull(RubyHash hash) {
            return hash;
        }

        @Specialization(guards = "isPackedHash(hash)")
        protected RubyHash rehashPackedArray(RubyHash hash,
                @Cached ConditionProfile byIdentityProfile) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Object[] store = (Object[]) hash.store;
            int size = hash.size;
            final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);

            for (int n = 0; n < size; n++) {
                final Object key = PackedArrayStrategy.getKey(store, n);
                final int newHash = hashNode.execute(PackedArrayStrategy.getKey(store, n), compareByIdentity);
                PackedArrayStrategy.setHashed(store, n, newHash);

                for (int m = n - 1; m >= 0; m--) {
                    if (PackedArrayStrategy.getHashed(store, m) == newHash && compareHashKeysNode.equalKeys(
                            compareByIdentity,
                            key,
                            newHash,
                            PackedArrayStrategy.getKey(store, m),
                            PackedArrayStrategy.getHashed(store, m))) {
                        PackedArrayStrategy.removeEntry(getLanguage(), store, n);
                        size--;
                        n--;
                        break;
                    }
                }
            }

            hash.size = size;
            return hash;
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected RubyHash rehashBuckets(RubyHash hash,
                @Cached ConditionProfile byIdentityProfile) {
            assert HashOperations.verifyStore(getContext(), hash);

            final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);

            final Entry[] entries = (Entry[]) hash.store;
            Arrays.fill(entries, null);

            Entry entry = hash.firstInSequence;
            while (entry != null) {
                final int newHash = hashNode.execute(entry.getKey(), compareByIdentity);
                entry.setHashed(newHash);
                entry.setNextInLookup(null);

                final int index = BucketsStrategy.getBucketIndex(newHash, entries.length);
                Entry bucketEntry = entries[index];

                if (bucketEntry == null) {
                    entries[index] = entry;
                } else {
                    Entry previousEntry = entry;

                    do {
                        if (compareHashKeysNode.equalKeys(
                                compareByIdentity,
                                entry.getKey(),
                                newHash,
                                bucketEntry.getKey(),
                                bucketEntry.getHashed())) {
                            BucketsStrategy.removeFromSequenceChain(hash, entry);
                            if (hash.lastInSequence == entry) {
                                hash.lastInSequence = entry.getPreviousInSequence();
                            }
                            hash.size--;
                            break;
                        }
                        previousEntry = bucketEntry;
                        bucketEntry = bucketEntry.getNextInLookup();
                    } while (bucketEntry != null);

                    previousEntry.setNextInLookup(entry);
                }
                entry = entry.getNextInSequence();
            }

            assert HashOperations.verifyStore(getContext(), hash);
            return hash;
        }

    }

    @ImportStatic(HashGuards.class)
    public abstract static class EachKeyValueNode extends RubyContextNode {

        public static EachKeyValueNode create() {
            return EachKeyValueNodeGen.create();
        }

        public abstract Object executeEachKeyValue(VirtualFrame frame, RubyHash hash, BiConsumerNode callbackNode,
                Object state);

        @Specialization(guards = "isNullHash(hash)")
        protected Object eachNull(RubyHash hash, BiConsumerNode callbackNode, Object state) {
            return state;
        }

        @ExplodeLoop
        @Specialization(
                guards = { "isPackedHash(hash)", "getSize(hash) == cachedSize" },
                limit = "getPackedHashLimit()")
        protected Object eachPackedArrayCached(
                VirtualFrame frame, RubyHash hash, BiConsumerNode callbackNode, Object state,
                @Cached("getSize(hash)") int cachedSize) {
            assert HashOperations.verifyStore(getContext(), hash);
            final Object[] store = (Object[]) hash.store;

            for (int i = 0; i < cachedSize; i++) {
                callbackNode.accept(
                        frame,
                        PackedArrayStrategy.getKey(store, i),
                        PackedArrayStrategy.getValue(store, i),
                        state);
            }

            return state;
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected Object eachBuckets(VirtualFrame frame, RubyHash hash, BiConsumerNode callbackNode, Object state) {
            assert HashOperations.verifyStore(getContext(), hash);

            Entry entry = hash.firstInSequence;
            while (entry != null) {
                callbackNode.accept(frame, entry.getKey(), entry.getValue(), state);
                entry = entry.getNextInSequence();
            }

            return state;
        }

        protected int getSize(RubyHash hash) {
            return hash.size;
        }

        protected int getPackedHashLimit() {
            // + 1 for packed Hash with size = 0
            return RubyLanguage.getCurrentLanguage().options.HASH_PACKED_ARRAY_MAX + 1;
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

        @Specialization(guards = "!isCompareByIdentity(hash)")
        protected RubyHash rehashNotIdentity(RubyHash hash,
                @Cached InternalRehashNode internalRehashNode) {
            return internalRehashNode.executeRehash(hash);
        }

    }

}
