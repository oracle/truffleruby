/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.collections.BiConsumerNode;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.hash.HashNodesFactory.EachKeyValueNodeGen;
import org.truffleruby.core.hash.HashNodesFactory.HashLookupOrExecuteDefaultNodeGen;
import org.truffleruby.core.hash.HashNodesFactory.InitializeCopyNodeFactory;
import org.truffleruby.core.hash.HashNodesFactory.InternalRehashNodeGen;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;
import org.truffleruby.language.yield.YieldNode;

import java.util.Arrays;

@CoreClass("Hash")
public abstract class HashNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, Layouts.HASH.build(null, 0, null, null, nil(), nil(), false));
        }

    }

    @CoreMethod(names = "[]", constructor = true, rest = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ConstructNode extends CoreMethodArrayArgumentsNode {

        @Child private HashNode hashNode = new HashNode();
        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();
        @Child private CallDispatchHeadNode fallbackNode = CallDispatchHeadNode.createPrivate();

        @ExplodeLoop
        @Specialization(guards = "isSmallArrayOfPairs(args)")
        public Object construct(DynamicObject hashClass, Object[] args) {
            final DynamicObject array = (DynamicObject) args[0];

            final Object[] store = (Object[]) Layouts.ARRAY.getStore(array);

            final int size = Layouts.ARRAY.getSize(array);
            final Object[] newStore = PackedArrayStrategy.createStore(getContext());

            // written very carefully to allow PE
            for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    final Object pair = store[n];

                    if (!RubyGuards.isRubyArray(pair)) {
                        return fallbackNode.call(hashClass, "_constructor_fallback", args);
                    }

                    final DynamicObject pairArray = (DynamicObject) pair;
                    final Object pairStore = Layouts.ARRAY.getStore(pairArray);

                    if (pairStore != null && pairStore.getClass() != Object[].class) {
                        return fallbackNode.call(hashClass, "_constructor_fallback", args);
                    }

                    if (Layouts.ARRAY.getSize(pairArray) != 2) {
                        return fallbackNode.call(hashClass, "_constructor_fallback", args);
                    }

                    final Object[] pairObjectStore = (Object[]) pairStore;

                    final Object key = pairObjectStore[0];
                    final Object value = pairObjectStore[1];

                    final int hashed = hashNode.hash(key, false);

                    PackedArrayStrategy.setHashedKeyValue(newStore, n, hashed, key, value);
                }
            }

            return allocateObjectNode.allocate(hashClass, Layouts.HASH.build(newStore, size, null, null, nil(), nil(), false));
        }

        @Specialization(guards = "!isSmallArrayOfPairs(args)")
        public Object constructFallback(DynamicObject hashClass, Object[] args) {
            return fallbackNode.call(hashClass, "_constructor_fallback", args);
        }

        public boolean isSmallArrayOfPairs(Object[] args) {
            if (args.length != 1) {
                return false;
            }

            final Object arg = args[0];

            if (!RubyGuards.isRubyArray(arg)) {
                return false;
            }

            final DynamicObject array = (DynamicObject) arg;
            final Object store = Layouts.ARRAY.getStore(array);

            if (store == null || store.getClass() != Object[].class) {
                return false;
            }

            final Object[] objectStore = (Object[]) store;

            if (objectStore.length > getContext().getOptions().HASH_PACKED_ARRAY_MAX) {
                return false;
            }

            return true;
        }

    }

    @ImportStatic(HashGuards.class)
    public abstract static class HashLookupOrExecuteDefaultNode extends RubyBaseNode {

        public static HashLookupOrExecuteDefaultNode create() {
            return HashLookupOrExecuteDefaultNodeGen.create();
        }

        public abstract Object executeGet(VirtualFrame frame, DynamicObject hash, Object key, BiFunctionNode defaultValueNode);

        @Specialization(guards = "isNullHash(hash)")
        public Object getNull(VirtualFrame frame, DynamicObject hash, Object key, BiFunctionNode defaultValueNode) {
            return defaultValueNode.accept(frame, hash, key);
        }

        @Specialization(guards = "isPackedHash(hash)")
        public Object getPackedArray(VirtualFrame frame, DynamicObject hash, Object key, BiFunctionNode defaultValueNode,
                @Cached LookupPackedEntryNode lookupPackedEntryNode,
                @Cached("new()") HashNode hashNode,
                @Cached("createBinaryProfile()") ConditionProfile byIdentityProfile) {
            final boolean compareByIdentity = byIdentityProfile.profile(Layouts.HASH.getCompareByIdentity(hash));
            int hashed = hashNode.hash(key, compareByIdentity); // Call key.hash only once
            return lookupPackedEntryNode.executePackedLookup(frame, hash, key, hashed, defaultValueNode);
        }

        @Specialization(guards = "isBucketHash(hash)")
        public Object getBuckets(VirtualFrame frame, DynamicObject hash, Object key, BiFunctionNode defaultValueNode,
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
        @Child private CallDispatchHeadNode callDefaultNode;

        public abstract Object executeGet(VirtualFrame frame, DynamicObject hash, Object key);

        @Specialization
        public Object get(VirtualFrame frame, DynamicObject hash, Object key) {
            return lookupNode.executeGet(frame, hash, key, this);
        }

        @Override
        public Object accept(VirtualFrame frame, Object hash, Object key) {
            if (callDefaultNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                callDefaultNode = insert(CallDispatchHeadNode.createPrivate());
            }
            return callDefaultNode.call(hash, "default", key);
        }

    }

    @CoreMethod(names = "_get_or_undefined", required = 1)
    public abstract static class GetOrUndefinedNode extends CoreMethodArrayArgumentsNode implements BiFunctionNode {

        @Child private HashLookupOrExecuteDefaultNode lookupNode = HashLookupOrExecuteDefaultNode.create();

        @Specialization
        public Object getOrUndefined(VirtualFrame frame, DynamicObject hash, Object key) {
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
        public Object set(DynamicObject hash, Object key, Object value) {
            return setNode.executeSet(hash, key, value, Layouts.HASH.getCompareByIdentity(hash));
        }

    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNullHash(hash)")
        public DynamicObject emptyNull(DynamicObject hash) {
            return hash;
        }

        @Specialization(guards = "!isNullHash(hash)")
        public DynamicObject empty(DynamicObject hash) {
            assert HashOperations.verifyStore(getContext(), hash);
            Layouts.HASH.setStore(hash, null);
            Layouts.HASH.setSize(hash, 0);
            Layouts.HASH.setFirstInSequence(hash, null);
            Layouts.HASH.setLastInSequence(hash, null);
            assert HashOperations.verifyStore(getContext(), hash);
            return hash;
        }

    }

    @CoreMethod(names = "compare_by_identity", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class CompareByIdentityNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isCompareByIdentity(hash)")
        DynamicObject compareByIdentity(DynamicObject hash,
                @Cached InternalRehashNode internalRehashNode) {
            Layouts.HASH.setCompareByIdentity(hash, true);
            return internalRehashNode.executeRehash(hash);
        }

        @Specialization(guards = "isCompareByIdentity(hash)")
        DynamicObject alreadyCompareByIdentity(DynamicObject hash) {
            return hash;
        }

    }

    @CoreMethod(names = "compare_by_identity?")
    public abstract static class IsCompareByIdentityNode extends CoreMethodArrayArgumentsNode {

        private final ConditionProfile profile = ConditionProfile.createBinaryProfile();

        @Specialization
        public boolean compareByIdentity(DynamicObject hash) {
            return profile.profile(Layouts.HASH.getCompareByIdentity(hash));
        }

    }

    @CoreMethod(names = "default_proc")
    public abstract static class DefaultProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object defaultProc(DynamicObject hash) {
            return Layouts.HASH.getDefaultBlock(hash);
        }

    }

    @Primitive(name = "hash_default_value")
    public abstract static class DefaultValueNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object defaultValue(DynamicObject hash) {
            return Layouts.HASH.getDefaultValue(hash);
        }
    }

    @CoreMethod(names = "delete", required = 1, needsBlock = true, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class DeleteNode extends CoreMethodArrayArgumentsNode {

        @Child private CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();
        @Child private HashNode hashNode = new HashNode();
        @Child private LookupEntryNode lookupEntryNode = new LookupEntryNode();
        @Child private YieldNode yieldNode = YieldNode.create();

        @Specialization(guards = "isNullHash(hash)")
        public Object deleteNull(DynamicObject hash, Object key, NotProvided block) {
            assert HashOperations.verifyStore(getContext(), hash);

            return nil();
        }

        @Specialization(guards = "isNullHash(hash)")
        public Object deleteNull(DynamicObject hash, Object key, DynamicObject block) {
            assert HashOperations.verifyStore(getContext(), hash);

            return yieldNode.executeDispatch(block, key);
        }

        @Specialization(guards = "isPackedHash(hash)")
        public Object deletePackedArray(DynamicObject hash, Object key, Object maybeBlock,
                @Cached("createBinaryProfile()") ConditionProfile byIdentityProfile) {
            assert HashOperations.verifyStore(getContext(), hash);
            final boolean compareByIdentity = byIdentityProfile.profile(Layouts.HASH.getCompareByIdentity(hash));
            final int hashed = hashNode.hash(key, compareByIdentity);

            final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
            final int size = Layouts.HASH.getSize(hash);

            for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                    final Object otherKey = PackedArrayStrategy.getKey(store, n);

                    if (equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed)) {
                        final Object value = PackedArrayStrategy.getValue(store, n);
                        PackedArrayStrategy.removeEntry(getContext(), store, n);
                        Layouts.HASH.setSize(hash, size - 1);
                        assert HashOperations.verifyStore(getContext(), hash);
                        return value;
                    }
                }
            }

            assert HashOperations.verifyStore(getContext(), hash);

            if (maybeBlock == NotProvided.INSTANCE) {
                return nil();
            } else {
                return yieldNode.executeDispatch((DynamicObject) maybeBlock, key);
            }
        }

        @Specialization(guards = "isBucketHash(hash)")
        public Object delete(DynamicObject hash, Object key, Object maybeBlock) {
            assert HashOperations.verifyStore(getContext(), hash);

            final HashLookupResult hashLookupResult = lookupEntryNode.lookup(hash, key);

            if (hashLookupResult.getEntry() == null) {
                if (maybeBlock == NotProvided.INSTANCE) {
                    return nil();
                } else {
                    return yieldNode.executeDispatch((DynamicObject) maybeBlock, key);
                }
            }

            final Entry entry = hashLookupResult.getEntry();

            // Remove from the sequence chain

            if (entry.getPreviousInSequence() == null) {
                assert Layouts.HASH.getFirstInSequence(hash) == entry;
                Layouts.HASH.setFirstInSequence(hash, entry.getNextInSequence());
            } else {
                assert Layouts.HASH.getFirstInSequence(hash) != entry;
                entry.getPreviousInSequence().setNextInSequence(entry.getNextInSequence());
            }

            if (entry.getNextInSequence() == null) {
                Layouts.HASH.setLastInSequence(hash, entry.getPreviousInSequence());
            } else {
                entry.getNextInSequence().setPreviousInSequence(entry.getPreviousInSequence());
            }

            // Remove from the lookup chain

            if (hashLookupResult.getPreviousEntry() == null) {
                ((Entry[]) Layouts.HASH.getStore(hash))[hashLookupResult.getIndex()] = entry.getNextInLookup();
            } else {
                hashLookupResult.getPreviousEntry().setNextInLookup(entry.getNextInLookup());
            }

            Layouts.HASH.setSize(hash, Layouts.HASH.getSize(hash) - 1);

            assert HashOperations.verifyStore(getContext(), hash);

            return entry.getValue();
        }

        protected boolean equalKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
            return compareHashKeysNode.equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
        }

    }

    @CoreMethod(names = { "each", "each_pair" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(HashGuards.class)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        private final ConditionProfile arityMoreThanOne = ConditionProfile.createBinaryProfile();

        @Specialization(guards = "isNullHash(hash)")
        public DynamicObject eachNull(DynamicObject hash, DynamicObject block) {
            return hash;
        }

        @ExplodeLoop
        @Specialization(guards = "isPackedHash(hash)")
        public DynamicObject eachPackedArray(DynamicObject hash, DynamicObject block) {
            assert HashOperations.verifyStore(getContext(), hash);
            final Object[] originalStore = (Object[]) Layouts.HASH.getStore(hash);

            // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
            final int size = Layouts.HASH.getSize(hash);
            final Object[] storeCopy = PackedArrayStrategy.copyStore(getContext(), originalStore);

            int count = 0;

            try {
                for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    if (n < size) {
                        yieldPair(block, PackedArrayStrategy.getKey(storeCopy, n), PackedArrayStrategy.getValue(storeCopy, n));
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return hash;
        }

        @Specialization(guards = "isBucketHash(hash)")
        public DynamicObject eachBuckets(DynamicObject hash, DynamicObject block) {
            assert HashOperations.verifyStore(getContext(), hash);

            for (KeyValue keyValue : BucketsStrategy.iterableKeyValues(Layouts.HASH.getFirstInSequence(hash))) {
                yieldPair(block, keyValue.getKey(), keyValue.getValue());
            }

            return hash;
        }

        private Object yieldPair(DynamicObject block, Object key, Object value) {
            // MRI behavior, see rb_hash_each_pair()
            if (arityMoreThanOne.profile(Layouts.PROC.getSharedMethodInfo(block).getArity().getArityNumber() > 1)) {
                return yield(block, key, value);
            } else {
                return yield(block, createArray(new Object[]{ key, value }, 2));
            }
        }

    }

    @CoreMethod(names = "empty?")
    @ImportStatic(HashGuards.class)
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNullHash(hash)")
        public boolean emptyNull(DynamicObject hash) {
            return true;
        }

        @Specialization(guards = "!isNullHash(hash)")
        public boolean emptyPackedArray(DynamicObject hash) {
            return Layouts.HASH.getSize(hash) == 0;
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true, optional = 1, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject initialize(DynamicObject hash, NotProvided defaultValue, NotProvided block) {
            assert HashOperations.verifyStore(getContext(), hash);
            Layouts.HASH.setDefaultValue(hash, nil());
            Layouts.HASH.setDefaultBlock(hash, nil());
            return hash;
        }

        @Specialization
        public DynamicObject initialize(DynamicObject hash, NotProvided defaultValue, DynamicObject block,
                @Cached PropagateSharingNode propagateSharingNode) {
            assert HashOperations.verifyStore(getContext(), hash);
            Layouts.HASH.setDefaultValue(hash, nil());
            propagateSharingNode.propagate(hash, block);
            Layouts.HASH.setDefaultBlock(hash, block);
            return hash;
        }

        @Specialization(guards = "wasProvided(defaultValue)")
        public DynamicObject initialize(DynamicObject hash, Object defaultValue, NotProvided block,
                @Cached PropagateSharingNode propagateSharingNode) {
            assert HashOperations.verifyStore(getContext(), hash);
            propagateSharingNode.propagate(hash, defaultValue);
            Layouts.HASH.setDefaultValue(hash, defaultValue);
            Layouts.HASH.setDefaultBlock(hash, nil());
            return hash;
        }

        @Specialization(guards = "wasProvided(defaultValue)")
        public Object initialize(DynamicObject hash, Object defaultValue, DynamicObject block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("wrong number of arguments (1 for 0)", this));
        }

    }

    @CoreMethod(names = { "initialize_copy", "replace" }, required = 1, raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        public static InitializeCopyNode create() {
            return InitializeCopyNodeFactory.create(null);
        }

        public abstract DynamicObject executeReplace(DynamicObject self, DynamicObject from);

        @Specialization(guards = { "isRubyHash(from)", "isNullHash(from)" })
        public DynamicObject replaceNull(DynamicObject self, DynamicObject from) {
            if (self == from) {
                return self;
            }

            propagateSharingNode.propagate(self, from);

            Layouts.HASH.setStore(self, null);
            Layouts.HASH.setSize(self, 0);
            Layouts.HASH.setFirstInSequence(self, null);
            Layouts.HASH.setLastInSequence(self, null);

            copyOtherFields(self, from);

            assert HashOperations.verifyStore(getContext(), self);
            return self;
        }

        @Specialization(guards = { "isRubyHash(from)", "isPackedHash(from)" })
        public DynamicObject replacePackedArray(DynamicObject self, DynamicObject from) {
            if (self == from) {
                return self;
            }

            propagateSharingNode.propagate(self, from);

            final Object[] store = (Object[]) Layouts.HASH.getStore(from);
            Object storeCopy = PackedArrayStrategy.copyStore(getContext(), store);
            int size = Layouts.HASH.getSize(from);
            Layouts.HASH.setStore(self, storeCopy);
            Layouts.HASH.setSize(self, size);
            Layouts.HASH.setFirstInSequence(self, null);
            Layouts.HASH.setLastInSequence(self, null);

            copyOtherFields(self, from);

            assert HashOperations.verifyStore(getContext(), self);
            return self;
        }

        @TruffleBoundary
        @Specialization(guards = { "isRubyHash(from)", "isBucketHash(from)" })
        public DynamicObject replaceBuckets(DynamicObject self, DynamicObject from) {
            if (self == from) {
                return self;
            }

            propagateSharingNode.propagate(self, from);

            BucketsStrategy.copyInto(getContext(), from, self);
            copyOtherFields(self, from);

            assert HashOperations.verifyStore(getContext(), self);
            return self;
        }

        @Specialization(guards = "!isRubyHash(other)")
        public DynamicObject replaceCoerce(DynamicObject self, Object other,
                @Cached("createPrivate()") CallDispatchHeadNode coerceNode,
                @Cached InitializeCopyNode initializeCopyNode) {
            final Object otherHash = coerceNode.call(coreLibrary().getTruffleTypeModule(), "coerce_to", other,
                    coreLibrary().getHashClass(), coreStrings().TO_HASH.getSymbol());
            return initializeCopyNode.executeReplace(self, (DynamicObject) otherHash);
        }

        private void copyOtherFields(DynamicObject self, DynamicObject from) {
            Layouts.HASH.setDefaultBlock(self, Layouts.HASH.getDefaultBlock(from));
            Layouts.HASH.setDefaultValue(self, Layouts.HASH.getDefaultValue(from));
            Layouts.HASH.setCompareByIdentity(self, Layouts.HASH.getCompareByIdentity(from));
        }

    }

    @CoreMethod(names = { "map", "collect" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(HashGuards.class)
    public abstract static class MapNode extends YieldingCoreMethodNode {

        @Specialization(guards = "isNullHash(hash)")
        public DynamicObject mapNull(DynamicObject hash, DynamicObject block) {
            assert HashOperations.verifyStore(getContext(), hash);

            return createArray(null, 0);
        }

        @ExplodeLoop
        @Specialization(guards = "isPackedHash(hash)")
        public DynamicObject mapPackedArray(DynamicObject hash, DynamicObject block,
                @Cached ArrayBuilderNode arrayBuilderNode) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Object[] store = (Object[]) Layouts.HASH.getStore(hash);

            final int length = Layouts.HASH.getSize(hash);
            Object resultStore = arrayBuilderNode.start(length);

            try {
                for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
                    if (n < length) {
                        final Object key = PackedArrayStrategy.getKey(store, n);
                        final Object value = PackedArrayStrategy.getValue(store, n);
                        resultStore = arrayBuilderNode.appendValue(resultStore, n, yieldPair(block, key, value));
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, length);
                }
            }

            return createArray(arrayBuilderNode.finish(resultStore, length), length);
        }

        @Specialization(guards = "isBucketHash(hash)")
        public DynamicObject mapBuckets(DynamicObject hash, DynamicObject block,
                @Cached ArrayBuilderNode arrayBuilderNode) {
            assert HashOperations.verifyStore(getContext(), hash);

            final int length = Layouts.HASH.getSize(hash);
            Object store = arrayBuilderNode.start(length);

            int index = 0;

            try {
                for (KeyValue keyValue : BucketsStrategy.iterableKeyValues(Layouts.HASH.getFirstInSequence(hash))) {
                    store = arrayBuilderNode.appendValue(store, index, yieldPair(block, keyValue.getKey(), keyValue.getValue()));
                    index++;
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, length);
                }
            }

            return createArray(arrayBuilderNode.finish(store, length), length);
        }

        private Object yieldPair(DynamicObject block, Object key, Object value) {
            return yield(block, createArray(new Object[]{ key, value }, 2));
        }

    }

    @Primitive(name = "hash_set_default_proc")
    public abstract static class SetDefaultProcNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyProc(defaultProc)")
        public DynamicObject setDefaultProc(DynamicObject hash, DynamicObject defaultProc,
                @Cached PropagateSharingNode propagateSharingNode) {
            propagateSharingNode.propagate(hash, defaultProc);

            Layouts.HASH.setDefaultValue(hash, nil());
            Layouts.HASH.setDefaultBlock(hash, defaultProc);
            return defaultProc;
        }

        @Specialization(guards = "isNil(nil)")
        public DynamicObject setDefaultProc(DynamicObject hash, Object nil) {
            Layouts.HASH.setDefaultValue(hash, nil());
            Layouts.HASH.setDefaultBlock(hash, nil());
            return nil();
        }

    }

    @CoreMethod(names = "default=", required = 1, raiseIfFrozenSelf = true)
    public abstract static class SetDefaultValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object setDefault(DynamicObject hash, Object defaultValue,
                @Cached PropagateSharingNode propagateSharingNode) {
            propagateSharingNode.propagate(hash, defaultValue);

            Layouts.HASH.setDefaultValue(hash, defaultValue);
            Layouts.HASH.setDefaultBlock(hash, nil());
            return defaultValue;
        }
    }

    @CoreMethod(names = "shift", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class ShiftNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode callDefaultNode = CallDispatchHeadNode.createPrivate();

        @Specialization(guards = "isEmptyHash(hash)")
        public Object shiftEmpty(DynamicObject hash) {
            return callDefaultNode.call(hash, "default", nil());
        }

        @Specialization(guards = { "!isEmptyHash(hash)", "isPackedHash(hash)" })
        public DynamicObject shiftPackedArray(DynamicObject hash) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Object[] store = (Object[]) Layouts.HASH.getStore(hash);

            final Object key = PackedArrayStrategy.getKey(store, 0);
            final Object value = PackedArrayStrategy.getValue(store, 0);

            PackedArrayStrategy.removeEntry(getContext(), store, 0);

            Layouts.HASH.setSize(hash, Layouts.HASH.getSize(hash) - 1);

            assert HashOperations.verifyStore(getContext(), hash);

            Object[] objects = new Object[]{ key, value };
            return createArray(objects, objects.length);
        }

        @Specialization(guards = { "!isEmptyHash(hash)", "isBucketHash(hash)" })
        public DynamicObject shiftBuckets(DynamicObject hash) {
            assert HashOperations.verifyStore(getContext(), hash);

            final Entry first = Layouts.HASH.getFirstInSequence(hash);
            assert first.getPreviousInSequence() == null;

            final Object key = first.getKey();
            final Object value = first.getValue();

            Layouts.HASH.setFirstInSequence(hash, first.getNextInSequence());

            if (first.getNextInSequence() != null) {
                first.getNextInSequence().setPreviousInSequence(null);
                Layouts.HASH.setFirstInSequence(hash, first.getNextInSequence());
            }

            if (Layouts.HASH.getLastInSequence(hash) == first) {
                Layouts.HASH.setLastInSequence(hash, null);
            }

            final Entry[] store = (Entry[]) Layouts.HASH.getStore(hash);
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

            Layouts.HASH.setSize(hash, Layouts.HASH.getSize(hash) - 1);

            assert HashOperations.verifyStore(getContext(), hash);

            Object[] objects = new Object[]{ key, value };
            return createArray(objects, objects.length);
        }

    }

    @CoreMethod(names = { "size", "length" })
    @ImportStatic(HashGuards.class)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNullHash(hash)")
        public int sizeNull(DynamicObject hash) {
            return 0;
        }

        @Specialization(guards = "!isNullHash(hash)")
        public int sizePackedArray(DynamicObject hash) {
            return Layouts.HASH.getSize(hash);
        }

    }

    @ImportStatic(HashGuards.class)
    public abstract static class InternalRehashNode extends RubyBaseNode {

        @Child private HashNode hashNode = new HashNode();

        public static InternalRehashNode create() {
            return InternalRehashNodeGen.create();
        }

        public abstract DynamicObject executeRehash(DynamicObject hash);

        @Specialization(guards = "isNullHash(hash)")
        DynamicObject rehashNull(DynamicObject hash) {
            return hash;
        }

        @Specialization(guards = "isPackedHash(hash)")
        DynamicObject rehashPackedArray(DynamicObject hash,
                @Cached("createBinaryProfile()") ConditionProfile byIdentityProfile) {
            assert HashOperations.verifyStore(getContext(), hash);

            final boolean compareByIdentity = byIdentityProfile.profile(Layouts.HASH.getCompareByIdentity(hash));
            final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
            final int size = Layouts.HASH.getSize(hash);

            for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    PackedArrayStrategy.setHashed(store, n, hashNode.hash(PackedArrayStrategy.getKey(store, n), compareByIdentity));
                }
            }

            assert HashOperations.verifyStore(getContext(), hash);

            return hash;
        }

        @Specialization(guards = "isBucketHash(hash)")
        DynamicObject rehashBuckets(DynamicObject hash,
                @Cached("createBinaryProfile()") ConditionProfile byIdentityProfile) {
            assert HashOperations.verifyStore(getContext(), hash);

            final boolean compareByIdentity = byIdentityProfile.profile(Layouts.HASH.getCompareByIdentity(hash));
            final Entry[] entries = (Entry[]) Layouts.HASH.getStore(hash);
            Arrays.fill(entries, null);

            Entry entry = Layouts.HASH.getFirstInSequence(hash);

            while (entry != null) {
                final int newHash = hashNode.hash(entry.getKey(), compareByIdentity);
                entry.setHashed(newHash);
                entry.setNextInLookup(null);
                final int index = BucketsStrategy.getBucketIndex(newHash, entries.length);
                Entry bucketEntry = entries[index];

                if (bucketEntry == null) {
                    entries[index] = entry;
                } else {
                    while (bucketEntry.getNextInLookup() != null) {
                        bucketEntry = bucketEntry.getNextInLookup();
                    }

                    bucketEntry.setNextInLookup(entry);
                }

                entry = entry.getNextInSequence();
            }

            assert HashOperations.verifyStore(getContext(), hash);
            return hash;
        }

    }

    @ImportStatic(HashGuards.class)
    public abstract static class EachKeyValueNode extends RubyBaseNode {

        public static EachKeyValueNode create() {
            return EachKeyValueNodeGen.create();
        }

        public abstract Object executeEachKeyValue(VirtualFrame frame, DynamicObject hash, BiConsumerNode callbackNode, Object state);

        @Specialization(guards = "isNullHash(hash)")
        protected Object eachNull(DynamicObject hash, BiConsumerNode callbackNode, Object state) {
            return state;
        }

        @ExplodeLoop
        @Specialization(guards = { "isPackedHash(hash)", "getSize(hash) == cachedSize" }, limit = "getPackedHashLimit()")
        protected Object eachPackedArrayCached(VirtualFrame frame, DynamicObject hash, BiConsumerNode callbackNode, Object state,
                @Cached("getSize(hash)") int cachedSize) {
            assert HashOperations.verifyStore(getContext(), hash);
            final Object[] store = (Object[]) Layouts.HASH.getStore(hash);

            for (int i = 0; i < cachedSize; i++) {
                callbackNode.accept(frame, PackedArrayStrategy.getKey(store, i), PackedArrayStrategy.getValue(store, i), state);
            }

            return state;
        }

        @Specialization(guards = "isBucketHash(hash)")
        protected Object eachBuckets(VirtualFrame frame, DynamicObject hash, BiConsumerNode callbackNode, Object state) {
            assert HashOperations.verifyStore(getContext(), hash);

            for (KeyValue keyValue : BucketsStrategy.iterableKeyValues(Layouts.HASH.getFirstInSequence(hash))) {
                callbackNode.accept(frame, keyValue.getKey(), keyValue.getValue(), state);
            }

            return state;
        }

        protected int getSize(DynamicObject hash) {
            return Layouts.HASH.getSize(hash);
        }

        protected int getPackedHashLimit() {
            // + 1 for packed Hash with size = 0
            return getContext().getOptions().HASH_PACKED_ARRAY_MAX + 1;
        }

    }

    @CoreMethod(names = "rehash", raiseIfFrozenSelf = true)
    @ImportStatic(HashGuards.class)
    public abstract static class RehashNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isCompareByIdentity(hash)")
        public DynamicObject rehashIdentity(DynamicObject hash) {
            // the identity hash of objects never change.
            return hash;
        }

        @Specialization(guards = "!isCompareByIdentity(hash)")
        public DynamicObject rehashNotIdentity(DynamicObject hash,
                @Cached InternalRehashNode internalRehashNode) {
            return internalRehashNode.executeRehash(hash);
        }

    }

}
