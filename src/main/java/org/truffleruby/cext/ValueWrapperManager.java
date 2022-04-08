/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import java.lang.ref.WeakReference;
import java.lang.ref.Cleaner.Cleanable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.cext.ValueWrapperManagerFactory.AllocateHandleNodeGen;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@SuppressFBWarnings("VO")
public class ValueWrapperManager {

    static final long UNSET_HANDLE = -2L;

    /* These constants are taken from ruby.h, and are based on us not tagging doubles. */

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

    private volatile HandleBlockWeakReference[] blockMap = new HandleBlockWeakReference[0];

    public static HandleBlockHolder getBlockHolder(RubyContext context, RubyLanguage language) {
        return language.getCurrentThread().getCurrentFiber().handleData;
    }

    /* We keep a map of long wrappers that have been generated because various C extensions assume that any given fixnum
     * will translate to a given VALUE. */
    public ValueWrapper longWrapper(long value) {
        return new ValueWrapper(value, UNSET_HANDLE, null);
    }

    public ValueWrapper doubleWrapper(double value) {
        return new ValueWrapper(value, UNSET_HANDLE, null);
    }

    @TruffleBoundary
    public synchronized HandleBlock addToBlockMap(RubyContext context, RubyLanguage language) {
        HandleBlock block = new HandleBlock(context, language, this);
        int blockIndex = block.getIndex();
        HandleBlockWeakReference[] map = growMapIfRequired(blockMap, blockIndex);
        blockMap = map;
        map[blockIndex] = new HandleBlockWeakReference(block);

        return block;
    }

    @TruffleBoundary
    public HandleBlock addToSharedBlockMap(RubyContext context, RubyLanguage language) {
        synchronized (language) {
            HandleBlock block = new HandleBlock(context, language, this);
            int blockIndex = block.getIndex();
            HandleBlockWeakReference[] map = growMapIfRequired(language.handleBlockSharedMap, blockIndex);
            language.handleBlockSharedMap = map;
            map[blockIndex] = new HandleBlockWeakReference(block);
            return block;
        }
    }

    private static HandleBlockWeakReference[] growMapIfRequired(HandleBlockWeakReference[] map, int blockIndex) {
        if (blockIndex + 1 > map.length) {
            final HandleBlockWeakReference[] copy = new HandleBlockWeakReference[blockIndex + 1];
            System.arraycopy(map, 0, copy, 0, map.length);
            map = copy;
        }
        return map;
    }

    public ValueWrapper getWrapperFromHandleMap(long handle, RubyLanguage language) {
        final int index = HandleBlock.getHandleIndex(handle);
        final HandleBlock block = getBlockFromMap(index, language);
        if (block == null) {
            return null;
        }
        return block.getWrapper(handle);
    }

    private HandleBlock getBlockFromMap(int index, RubyLanguage language) {
        final HandleBlockWeakReference[] blockMap = this.blockMap;
        final HandleBlockWeakReference[] sharedMap = language.handleBlockSharedMap;
        HandleBlockWeakReference ref = null;
        // First try getting the block from the context's map
        if (index >= 0 && index < blockMap.length) {
            ref = blockMap[index];
        }
        // If no block was found in the context's map then look in the
        // shared map. If there is a block in a context's map then the
        // same block will not be in the shared map and vice versa.
        if (ref == null && index >= 0 && index < sharedMap.length) {
            ref = sharedMap[index];
        }
        if (ref == null) {
            return null;
        }
        return ref.get();
    }

    public void freeAllBlocksInMap(RubyLanguage language) {
        HandleBlockWeakReference[] map = blockMap;
        HandleBlockAllocator allocator = language.handleBlockAllocator;

        for (HandleBlockWeakReference ref : map) {
            if (ref == null) {
                continue;
            }
            HandleBlock block = ref.get();
            if (block != null) {
                allocator.addFreeBlock(block.base);
            }
        }
    }

    public void cleanup(RubyContext context, HandleBlockHolder holder) {
        context.getMarkingService().queueForMarking(holder.handleBlock);
        holder.handleBlock = null;
    }

    protected static class FreeHandleBlock {
        public final long start;
        public final FreeHandleBlock next;

        public FreeHandleBlock(long start, FreeHandleBlock next) {
            this.start = start;
            this.next = next;
        }
    }

    private final AtomicLong counter = new AtomicLong();

    protected void recordHandleAllocation() {
        counter.incrementAndGet();
    }

    public long totalHandleAllocations() {
        return counter.get();
    }

    private static final int BLOCK_BITS = 15;
    private static final int BLOCK_SIZE = 1 << (BLOCK_BITS - TAG_BITS);
    private static final int BLOCK_BYTE_SIZE = BLOCK_SIZE << TAG_BITS;
    private static final long BLOCK_MASK = -1L << BLOCK_BITS;
    private static final long OFFSET_MASK = ~BLOCK_MASK;
    public static final long ALLOCATION_BASE = 0x0badL << 48;

    public static class HandleBlockAllocator {

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

    public static class HandleBlock {

        public static final HandleBlock DUMMY_BLOCK = new HandleBlock();

        private static final Set<HandleBlock> keepAlive = ConcurrentHashMap.newKeySet();

        private final long base;
        @SuppressWarnings("rawtypes") private final ValueWrapper[] wrappers;
        private int count;

        @SuppressWarnings("unused") private Cleanable cleanable;

        private HandleBlock() {
            base = 0;
            cleanable = null;
            wrappers = null;
        }

        public HandleBlock(RubyContext context, RubyLanguage language, ValueWrapperManager manager) {
            HandleBlockAllocator allocator = language.handleBlockAllocator;
            long base = allocator.getFreeBlock();
            if (context != null && context.getOptions().CEXTS_KEEP_HANDLES_ALIVE) {
                keepAlive(this);
            }
            this.base = base;
            this.wrappers = new ValueWrapper[BLOCK_SIZE];
            this.count = 0;
            this.cleanable = RubyLanguage.cleaner.register(this, HandleBlock.makeCleaner(manager, base, allocator));
        }

        private static Runnable makeCleaner(ValueWrapperManager manager, long base, HandleBlockAllocator allocator) {
            return () -> {
                manager.blockMap[(int) ((base - ALLOCATION_BASE) >> BLOCK_BITS)] = null;
                allocator.addFreeBlock(base);
            };
        }

        @TruffleBoundary
        private static void keepAlive(HandleBlock block) {
            keepAlive.add(block);
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
            long handle = getBase() + count * Pointer.SIZE;
            wrapper.setHandle(handle, this);
            wrappers[count] = wrapper;
            count++;
            return handle;
        }

        public static int getHandleIndex(long handle) {
            return (int) ((handle - ALLOCATION_BASE) >> BLOCK_BITS);
        }
    }

    public static final class HandleBlockWeakReference extends WeakReference<HandleBlock> {
        HandleBlockWeakReference(HandleBlock referent) {
            super(referent);
        }
    }

    public static class HandleBlockHolder {
        protected HandleBlock handleBlock = null;
        protected HandleBlock sharedHandleBlock = null;
    }

    @GenerateUncached
    public abstract static class AllocateHandleNode extends RubyBaseNode {

        public abstract long execute(ValueWrapper wrapper);

        @Specialization(guards = "!isSharedObject(wrapper)")
        protected long allocateHandleOnKnownThread(ValueWrapper wrapper) {
            return allocateHandle(
                    wrapper,
                    getContext(),
                    getLanguage(),
                    getBlockHolder(getContext(), getLanguage()),
                    false);
        }

        @Specialization(guards = "isSharedObject(wrapper)")
        protected long allocateSharedHandleOnKnownThread(ValueWrapper wrapper) {
            return allocateHandle(
                    wrapper,
                    getContext(),
                    getLanguage(),
                    getBlockHolder(getContext(), getLanguage()),
                    true);
        }

        protected static long allocateHandle(ValueWrapper wrapper, RubyContext context, RubyLanguage language,
                HandleBlockHolder holder, boolean shared) {
            HandleBlock block;
            if (shared) {
                block = holder.sharedHandleBlock;
            } else {
                block = holder.handleBlock;
            }

            if (context.getOptions().CEXTS_TO_NATIVE_STATS) {
                context.getValueWrapperManager().recordHandleAllocation();
            }

            if (block == null || block.isFull()) {
                if (block != null) {
                    context.getMarkingService().queueForMarking(block);
                }
                if (shared) {
                    block = context.getValueWrapperManager().addToSharedBlockMap(context, language);
                    holder.sharedHandleBlock = block;
                } else {
                    block = context.getValueWrapperManager().addToBlockMap(context, language);
                    holder.handleBlock = block;
                }

            }
            return block.setHandleOnWrapper(wrapper);
        }

        protected static boolean isSharedObject(ValueWrapper wrapper) {
            return wrapper.getObject() instanceof ImmutableRubyObject;
        }

        public static AllocateHandleNode create() {
            return AllocateHandleNodeGen.create();
        }
    }

    public static HandleBlock allocateNewBlock(RubyContext context, RubyLanguage language) {
        HandleBlockHolder holder = getBlockHolder(context, language);
        HandleBlock block = holder.handleBlock;

        if (block != null) {
            context.getMarkingService().queueForMarking(block);
        }
        block = context.getValueWrapperManager().addToBlockMap(context, language);

        holder.handleBlock = block;
        return block;
    }

    public static boolean isTaggedLong(long handle) {
        return (handle & LONG_TAG) == LONG_TAG;
    }

    public static boolean isTaggedObject(long handle) {
        return handle != FALSE_HANDLE && (handle & TAG_MASK) == OBJECT_TAG;
    }

    public static boolean isMallocAligned(long handle) {
        return handle != FALSE_HANDLE && (handle & 0b111) == 0;
    }

    public static boolean isWrapper(Object value) {
        return value instanceof ValueWrapper;
    }

    public static long untagTaggedLong(long handle) {
        return handle >> 1;
    }

    @ExportLibrary(InteropLibrary.class)
    @GenerateUncached
    public static class UnwrapperFunction implements TruffleObject {

        @ExportMessage
        protected boolean isExecutable() {
            return true;
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                @Cached UnwrapNode unwrapNode) {
            return unwrapNode.execute(arguments[0]);
        }
    }

    @ExportLibrary(InteropLibrary.class)
    @GenerateUncached
    public static class ID2SymbolFunction implements TruffleObject {

        @ExportMessage
        protected boolean isExecutable() {
            return true;
        }

        @ExportMessage
        public RubySymbol execute(Object[] arguments,
                @Cached IDToSymbolNode unwrapIDNode) {
            return unwrapIDNode.execute(arguments[0]);
        }
    }

    @ExportLibrary(InteropLibrary.class)
    @GenerateUncached
    public static class Symbol2IDFunction implements TruffleObject {

        @ExportMessage
        protected boolean isExecutable() {
            return true;
        }

        @ExportMessage
        public Object execute(Object[] arguments,
                @Cached UnwrapNode unwrapNode,
                @Cached SymbolToIDNode symbolTOIDNode) {
            return symbolTOIDNode.execute(unwrapNode.execute(arguments[0]));
        }
    }

    @ExportLibrary(InteropLibrary.class)
    @GenerateUncached
    public static class WrapperFunction implements TruffleObject {

        @ExportMessage
        protected boolean isExecutable() {
            return true;
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                @Cached WrapNode wrapNode) {
            return wrapNode.execute(arguments[0]);
        }
    }

    @ExportLibrary(InteropLibrary.class)
    @GenerateUncached
    public static class IsNativeObjectFunction implements TruffleObject {

        @ExportMessage
        protected boolean isExecutable() {
            return true;
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                @Cached IsNativeObjectNode isNativeObjectNode) {
            return isNativeObjectNode.execute(arguments[0]);
        }
    }

    @ExportLibrary(InteropLibrary.class)
    @GenerateUncached
    public static class ToNativeObjectFunction implements TruffleObject {

        @ExportMessage
        protected boolean isExecutable() {
            return true;
        }

        @ExportMessage
        protected Object execute(Object[] arguments,
                @CachedLibrary(limit = "1") InteropLibrary values) throws UnsupportedMessageException {
            values.toNative(arguments[0]);
            return values.asPointer(arguments[0]);
        }
    }

}
