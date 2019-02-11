/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import java.lang.ref.WeakReference;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.ValueWrapperManagerFactory.AllocateHandleNodeGen;
import org.truffleruby.cext.ValueWrapperManagerFactory.GetHandleBlockHolderNodeGen;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseWithoutContextNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.profiles.BranchProfile;

public class ValueWrapperManager {

    static final long UNSET_HANDLE = -2L;
    static final HandleBlockAllocator allocator = new HandleBlockAllocator();

    /*
     * These constants are taken from ruby.h, and are based on us not tagging doubles.
     */

    public static final int FALSE_HANDLE = 0b000;
    public static final int TRUE_HANDLE = 0b010;
    public static final int NIL_HANDLE = 0b100;
    public static final int UNDEF_HANDLE = 0b110;

    public static final long LONG_TAG = 1;
    public static final long OBJECT_TAG = 0;

    public static final long MIN_FIXNUM_VALUE = -(1L << 62);
    public static final long MAX_FIXNUM_VALUE = (1L << 62) - 1;

    public static final long TAG_MASK = 0b111;
    public static final long TAG_BITS = 3;

    public final ValueWrapper trueWrapper = new ValueWrapper(true, TRUE_HANDLE, null);
    public final ValueWrapper falseWrapper = new ValueWrapper(false, FALSE_HANDLE, null);
    public final ValueWrapper undefWrapper = new ValueWrapper(NotProvided.INSTANCE, UNDEF_HANDLE, null);
    public final ValueWrapper nilWrapper;

    private Object[] blockMap = new Object[0];

    private final ThreadLocal<HandleBlockHolder> threadBlocks = ThreadLocal.withInitial(() -> new HandleBlockHolder());

    private final RubyContext context;

    public ValueWrapperManager(RubyContext context) {
        this.context = context;
        nilWrapper = new ValueWrapper(context.getCoreLibrary().getNil(), NIL_HANDLE, null);
    }

    @TruffleBoundary
    public HandleBlockHolder getBlockHolder() {
        return threadBlocks.get();
    }

    /*
     * We keep a map of long wrappers that have been generated because various C extensions assume
     * that any given fixnum will translate to a given VALUE.
     */
    public ValueWrapper longWrapper(long value) {
        return new ValueWrapper(value, UNSET_HANDLE, null);
    }

    public ValueWrapper doubleWrapper(double value) {
        return new ValueWrapper(value, UNSET_HANDLE, null);
    }

    @TruffleBoundary
    public synchronized void addToBlockMap(HandleBlock block) {
        int blockIndex = block.getIndex();
        long blockBase = block.getBase();
        Object[] map = blockMap;
        HandleBlockAllocator allocator = ValueWrapperManager.allocator;
        map = ensureCapacity(map, blockIndex + 1);
        map[blockIndex] = new WeakReference<>(block);
        blockMap = map;
        context.getFinalizationService().addFinalizer(block, null, ValueWrapperManager.class, () -> {
            this.blockMap[blockIndex] = null;
            allocator.addFreeBlock(blockBase);
        }, null);
    }

    private Object[] ensureCapacity(Object[] map, int size) {
        if (size > map.length) {
            Object[] newMap = new Object[size];
            if (map.length > 0) {
                System.arraycopy(map, 0, newMap, 0, size - 1);
            }
            map = newMap;
        }
        return map;
    }

    public synchronized Object getFromHandleMap(long handle) {
        ValueWrapper wrapper = getWrapperFromHandleMap(handle);
        if (wrapper == null) {
            return null;
        }
        return wrapper.getObject();
    }

    @SuppressWarnings("unchecked")
    public synchronized ValueWrapper getWrapperFromHandleMap(long handle) {
        final int index = HandleBlock.getHandleIndex(handle);
        WeakReference<HandleBlock> ref;
        try {
            ref = (WeakReference<HandleBlock>) blockMap[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
        if (ref == null) {
            return null;
        }
        HandleBlock block;
        if ((block = ref.get()) == null) {
            return null;
        }
        return block.getWrapper(handle);
    }

    protected static class FreeHandleBlock {
        public final long start;
        public final FreeHandleBlock next;

        public FreeHandleBlock(long start, FreeHandleBlock next) {
            this.start = start;
            this.next = next;
        }
    }

    private static final int BLOCK_SIZE = 128;
    private static final int BLOCK_BITS = 10;
    private static final int BLOCK_BYTE_SIZE = BLOCK_SIZE << TAG_BITS;
    private static final long BLOCK_MASK = -1L << BLOCK_BITS;
    private static final long OFFSET_MASK = ~BLOCK_MASK;
    private static final long ALLOCATION_BASE = 0xffffffffL << BLOCK_BITS;

    protected static class HandleBlockAllocator {

        private long nextBlock = ALLOCATION_BASE;
        private FreeHandleBlock firstFreeBlock = null;

        public synchronized long getFreeBlock() {
            if (firstFreeBlock != null) {
                FreeHandleBlock block = firstFreeBlock;
                firstFreeBlock = block.next;
                return block.start;
            } else {
                long block = nextBlock;
                nextBlock = nextBlock + BLOCK_BYTE_SIZE;
                return block;
            }
        }

        public synchronized void addFreeBlock(long blockBase) {
            firstFreeBlock = new FreeHandleBlock(blockBase, firstFreeBlock);
        }
    }

    protected static class HandleBlock {

        private final long base;
        @SuppressWarnings("rawtypes") private final ValueWrapper[] wrappers;
        private int count;

        public HandleBlock() {
            base = allocator.getFreeBlock();
            wrappers = new ValueWrapper[BLOCK_SIZE];
            count = 0;
        }

        public long getBase() {
            return base;
        }

        public int getIndex() {
            return (int) ((base - ALLOCATION_BASE) >> BLOCK_BITS);
        }

        public ValueWrapper getWrapper(long handle) {
            int offset = (int) (handle & OFFSET_MASK) >> TAG_BITS;
            return wrappers[offset];
        }

        public boolean isFull() {
            return count == BLOCK_SIZE;
        }

        public long setHandleOnWrapper(ValueWrapper wrapper) {
            long handle = getBase() + count * 8;
            wrapper.setHandle(handle, this);
            wrappers[count] = wrapper;
            count++;
            return handle;
        }

        public static int getHandleIndex(long handle) {
            return (int) ((handle - ALLOCATION_BASE) >> BLOCK_BITS);
        }
    }

    protected static class HandleBlockHolder {

        private HandleBlock handleBlock;

        public HandleBlockHolder() {
            handleBlock = null;
        }

        public HandleBlock currentBlock() {
            return handleBlock;
        }

        public HandleBlock makeNewBlock() {
            handleBlock = new HandleBlock();
            return handleBlock;
        }

        @TruffleBoundary
        public void debug(HandleBlock block) {
            System.err.printf("Allocated %x.\n", block.getBase());
        }
    }

    @GenerateUncached
    public static abstract class GetHandleBlockHolderNode extends RubyBaseWithoutContextNode {

        public abstract HandleBlockHolder execute(ValueWrapper wrapper);

        @Specialization(guards = "cachedThread == currentJavaThread(wrapper)", limit = "getCacheLimit()")
        protected HandleBlockHolder getHolderOnKnownThread(ValueWrapper wrapper,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached("currentJavaThread(wrapper)") Thread cachedThread,
                @Cached("getBlockHolder(wrapper, context)") HandleBlockHolder blockHolder) {
            return blockHolder;
        }

        @Specialization(replaces = "getHolderOnKnownThread")
        protected HandleBlockHolder getBlockHolder(ValueWrapper wrapper,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return context.getValueWrapperManager().getBlockHolder();
        }

        static protected Thread currentJavaThread(ValueWrapper wrapper) {
            return Thread.currentThread();
        }

        public int getCacheLimit() {
            return 3;
        }

        public static GetHandleBlockHolderNode create() {
            return GetHandleBlockHolderNodeGen.create();
        }
    }

    @GenerateUncached
    public static abstract class AllocateHandleNode extends RubyBaseWithoutContextNode {

        public abstract long execute(ValueWrapper wrapper);

        @Specialization
        protected long allocateHandleOnKnownThread(ValueWrapper wrapper,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached GetHandleBlockHolderNode getBlockHolderNode,
                @Cached BranchProfile newBlockProfile) {
            HandleBlockHolder holder = getBlockHolderNode.execute(wrapper);
            HandleBlock block = holder.handleBlock;
            if (block == null || block.isFull()) {
                newBlockProfile.enter();
                block = holder.makeNewBlock();
                context.getValueWrapperManager().addToBlockMap(block);
            }
            return block.setHandleOnWrapper(wrapper);
        }

        public static AllocateHandleNode create() {
            return AllocateHandleNodeGen.create();
        }
    }

    public static boolean isTaggedLong(long handle) {
        return (handle & LONG_TAG) == LONG_TAG;
    }

    public static boolean isTaggedObject(long handle) {
        return handle != FALSE_HANDLE && (handle & TAG_MASK) == OBJECT_TAG;
    }

    public static boolean isWrapper(TruffleObject value) {
        return value instanceof ValueWrapper;
    }
}
