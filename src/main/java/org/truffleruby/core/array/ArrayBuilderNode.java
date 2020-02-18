/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendArrayNodeGen;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendOneNodeGen;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;

/*
 * TODO(CS): how does this work when when multithreaded? Could a node get replaced by someone else and
 * then suddenly you're passing it a store type it doesn't expect?
 */

/** Builds a new Array and learns its storage strategy and its expected length. The storage strategy is generalized as
 * needed and the expected length is increased until all elements fit.
 * <p>
 * Append nodes handle only one strategy, but must still return a valid storage when:
 *
 * <li>The element(s) added do not match the strategy.
 * <li>The being-built storage no longer matches the strategy, due to the node having been replaced by another thread or
 * by another usage (e.g. recursive) of this ArrayBuilderNode. */
public abstract class ArrayBuilderNode extends RubyContextNode {

    public static ArrayBuilderNode create() {
        return new ArrayBuilderProxyNode();
    }

    public abstract Object start();

    public abstract Object start(int length);

    public abstract Object appendArray(Object store, int index, DynamicObject array);

    public abstract Object appendValue(Object store, int index, Object value);

    public abstract Object finish(Object store, int length);

    private static class ArrayBuilderProxyNode extends ArrayBuilderNode {

        @CompilationFinal private static ArrayAllocator INITIAL_ALLOCATOR = ArrayStoreLibrary.allocatorForValue(0);

        @Child StartNode startNode = new StartNode(INITIAL_ALLOCATOR, 0);
        @Child AppendArrayNode appendArrayNode;
        @Child AppendOneNode appendOneNode;

        @Override
        public Object start() {
            return startNode.start();
        }

        @Override
        public Object start(int length) {
            return startNode.start(length);
        }

        @Override
        public Object appendArray(Object store, int index, DynamicObject array) {
            return getAppendArrayNode().executeAppend(store, index, array);
        }

        @Override
        public Object appendValue(Object store, int index, Object value) {
            return getAppendOneNode().executeAppend(store, index, value);
        }

        @Override
        public Object finish(Object store, int length) {
            return store;
        }

        private AppendArrayNode getAppendArrayNode() {
            if (appendArrayNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                appendArrayNode = insert(AppendArrayNode.create(getContext(), startNode.allocator));
            }
            return appendArrayNode;
        }

        private AppendOneNode getAppendOneNode() {
            if (appendOneNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                appendOneNode = insert(AppendOneNode.create(getContext(), startNode.allocator));
            }
            return appendOneNode;
        }

        public void updateStrategy(ArrayStoreLibrary.ArrayAllocator newStrategy, int newLength) {
            final ArrayStoreLibrary.ArrayAllocator oldStrategy = startNode.allocator;

            final int oldLength = startNode.expectedLength;
            final int newExpectedLength = Math.max(oldLength, newLength);
            if (newStrategy != oldStrategy || newExpectedLength > oldLength) {
                startNode.replace(new StartNode(newStrategy, newExpectedLength));
            }

            if (newStrategy != oldStrategy) {
                if (appendArrayNode != null) {
                    appendArrayNode.replace(AppendArrayNode.create(getContext(), newStrategy));
                }
                if (appendOneNode != null) {
                    appendOneNode.replace(AppendOneNode.create(getContext(), newStrategy));
                }
            }
        }
    }

    public abstract static class ArrayBuilderBaseNode extends RubyContextNode {

        protected void replaceNodes(ArrayStoreLibrary.ArrayAllocator strategy, int size) {
            final ArrayBuilderProxyNode parent = (ArrayBuilderProxyNode) getParent();
            parent.updateStrategy(strategy, size);
        }
    }

    public static class StartNode extends ArrayBuilderBaseNode {

        private final ArrayStoreLibrary.ArrayAllocator allocator;
        private final int expectedLength;

        public StartNode(ArrayStoreLibrary.ArrayAllocator allocator, int expectedLength) {
            this.allocator = allocator;
            this.expectedLength = expectedLength;
        }

        public Object start() {
            return allocator.allocate(expectedLength);
        }

        public Object start(int length) {
            if (length > expectedLength) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(allocator, length);
            }
            return allocator.allocate(length);
        }

    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class AppendOneNode extends ArrayBuilderBaseNode {

        public static AppendOneNode create(RubyContext context, ArrayStoreLibrary.ArrayAllocator allocator) {
            return AppendOneNodeGen.create(context, allocator);
        }

        private final RubyContext context;
        protected final ArrayStoreLibrary.ArrayAllocator allocator;

        public AppendOneNode(RubyContext context, ArrayStoreLibrary.ArrayAllocator allocator) {
            this.context = context;
            this.allocator = allocator;
        }

        public abstract Object executeAppend(Object array, int index, Object value);

        @Specialization(
                guards = "arrays.acceptsValue(array, value)",
                limit = "1")
        protected Object appendCompatibleType(Object array, int index, Object value,
                @CachedLibrary("array") ArrayStoreLibrary arrays) {
            final int length = arrays.capacity(array);
            if (index >= length) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                final int capacity = ArrayUtils.capacityForOneMore(context, length);
                array = arrays.expand(array, capacity);
                replaceNodes(arrays.allocator(array), capacity);
            }
            arrays.write(array, index, value);
            return array;
        }

        @Fallback
        protected Object appendNewStrategy(Object store, int index, Object value) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            final ArrayStoreLibrary stores = ArrayStoreLibrary.getFactory().getUncached();
            final ArrayStoreLibrary.ArrayAllocator newAllocator = stores.generalizeForValue(store, value);

            final int currentCapacity = stores.capacity(store);
            final int neededCapacity;
            if (index >= currentCapacity) {
                neededCapacity = ArrayUtils.capacityForOneMore(context, currentCapacity);
            } else {
                neededCapacity = currentCapacity;
            }

            replaceNodes(newAllocator, neededCapacity);

            final Object newStore = newAllocator.allocate(neededCapacity);
            stores.copyContents(store, 0, newStore, 0, index);
            stores.write(newStore, index, value);
            return newStore;
        }

    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class AppendArrayNode extends ArrayBuilderBaseNode {

        public static AppendArrayNode create(RubyContext context, ArrayStoreLibrary.ArrayAllocator allocator) {
            return AppendArrayNodeGen.create(context, allocator);
        }

        private final RubyContext context;
        protected final ArrayStoreLibrary.ArrayAllocator allocator;

        public AppendArrayNode(RubyContext context, ArrayStoreLibrary.ArrayAllocator allocator) {
            this.context = context;
            this.allocator = allocator;
        }

        public abstract Object executeAppend(Object array, int index, DynamicObject value);

        @Specialization(
                guards = { "arrays.acceptsAllValues(array, getStore(other))" },
                limit = "1")
        protected Object appendCompatibleStrategy(Object array, int index, DynamicObject other,
                @CachedLibrary("array") ArrayStoreLibrary arrays,
                @CachedLibrary("getStore(other)") ArrayStoreLibrary others) {
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;

            int length = arrays.capacity(array);
            if (neededSize > length) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(arrays.allocator(array), neededSize);
                final int capacity = ArrayUtils.capacity(context, length, neededSize);
                array = arrays.expand(array, capacity);
            }

            final Object otherStore = Layouts.ARRAY.getStore(other);
            others.copyContents(otherStore, 0, array, index, otherSize);
            return array;
        }

        @Fallback
        protected Object appendNewStrategy(Object array, int index, DynamicObject other) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            final ArrayStoreLibrary arrays = ArrayStoreLibrary.getFactory().getUncached();
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;

            final Object newStore;

            final int length = arrays.capacity(array);
            ArrayAllocator allocator = arrays.generalizeForStore(array, Layouts.ARRAY.getStore(other));
            if (neededSize > length) {
                replaceNodes(allocator, neededSize);
                final int capacity = ArrayUtils.capacity(context, length, neededSize);
                newStore = allocator.allocate(capacity);
            } else {
                replaceNodes(allocator, length);
                newStore = allocator.allocate(length);
            }

            arrays.copyContents(array, 0, newStore, 0, index);

            final Object otherStore = Layouts.ARRAY.getStore(other);
            arrays.copyContents(otherStore, 0, newStore, index, otherSize);

            return newStore;
        }

        protected static Object getStore(DynamicObject array) {
            return Layouts.ARRAY.getStore(array);
        }
    }

}
