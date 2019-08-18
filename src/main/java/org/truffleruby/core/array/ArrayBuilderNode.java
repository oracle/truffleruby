/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendArrayNodeGen;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendOneNodeGen;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCapacityNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCopyStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayCopyToNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayNewStoreNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArraySetNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyBaseWithoutContextNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/*
 * TODO(CS): how does this work when when multithreaded? Could a node get replaced by someone else and
 * then suddenly you're passing it a store type it doesn't expect?
 */

/**
 * Builds a new Array and learns its storage strategy and its expected length. The storage strategy
 * is generalized as needed and the expected length is increased until all elements fit.
 * <p>
 * Append nodes handle only one strategy, but must still return a valid storage when:
 * <ul>
 * <li>The element(s) added do not match the strategy.</li>
 * <li>The being-built storage no longer matches the strategy, due to the node having been replaced
 * by another thread or by another usage (e.g. recursive) of this ArrayBuilderNode.</li>
 * </ul>
 */
public abstract class ArrayBuilderNode extends RubyBaseNode {

    public static ArrayBuilderNode create() {
        return new ArrayBuilderProxyNode();
    }

    public abstract Object start();

    public abstract Object start(int length);

    public abstract Object appendArray(Object store, int index, DynamicObject array);

    public abstract Object appendValue(Object store, int index, Object value);

    public abstract Object finish(Object store, int length);

    private static class ArrayBuilderProxyNode extends ArrayBuilderNode {

        @Child StartNode startNode = new StartNode(ArrayStrategy.forValue(0), 0);
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
                appendArrayNode = insert(AppendArrayNode.create(getContext(), startNode.strategy));
            }
            return appendArrayNode;
        }

        private AppendOneNode getAppendOneNode() {
            if (appendOneNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                appendOneNode = insert(AppendOneNode.create(getContext(), startNode.strategy));
            }
            return appendOneNode;
        }

        public void updateStrategy(ArrayStrategy strategy, int newLength) {
            final ArrayStrategy oldStrategy = startNode.strategy;
            final ArrayStrategy newStrategy = oldStrategy.generalize(strategy);

            final int oldLength = startNode.expectedLength;
            final int newExpectedLength = Math.max(oldLength, newLength);
            if (newStrategy != oldStrategy || newExpectedLength > oldLength) {
                startNode.replace(new StartNode(strategy, newExpectedLength));
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

    public abstract static class ArrayBuilderBaseNode extends RubyBaseWithoutContextNode {

        protected void replaceNodes(ArrayStrategy strategy, int size) {
            final ArrayBuilderProxyNode parent = (ArrayBuilderProxyNode) getParent();
            parent.updateStrategy(strategy, size);
        }
    }

    public static class StartNode extends ArrayBuilderBaseNode {

        private final ArrayStrategy strategy;
        private final ArrayOperationNodes.ArrayNewStoreNode newStoreNode;
        private final int expectedLength;

        public StartNode(ArrayStrategy strategy, int expectedLength) {
            this.strategy = strategy;
            newStoreNode = strategy.newStoreNode();
            this.expectedLength = expectedLength;
        }

        public Object start() {
            return newStoreNode.execute(expectedLength);
        }

        public Object start(int length) {
            if (length > expectedLength) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(strategy, length);
            }
            return newStoreNode.execute(length);
        }

    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class AppendOneNode extends ArrayBuilderBaseNode {

        public static AppendOneNode create(RubyContext context, ArrayStrategy arrayStrategy) {
            return AppendOneNodeGen.create(context, arrayStrategy);
        }

        private final RubyContext context;
        protected final ArrayStrategy arrayStrategy;

        public AppendOneNode(RubyContext context, ArrayStrategy arrayStrategy) {
            this.context = context;
            this.arrayStrategy = arrayStrategy;
        }

        public abstract Object executeAppend(Object array, int index, Object value);

        @Specialization(guards = { "arrayStrategy.matchesStore(array)", "arrayStrategy.accepts(value)" })
        protected Object appendCompatibleType(Object array, int index, Object value,
                @Cached("arrayStrategy.capacityNode()") ArrayOperationNodes.ArrayCapacityNode capacityNode,
                @Cached("arrayStrategy.copyStoreNode()") ArrayCopyStoreNode copyStoreNode,
                @Cached("arrayStrategy.setNode()") ArraySetNode setNode) {
            final int length = capacityNode.execute(array);
            if (index >= length) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                final int capacity = ArrayUtils.capacityForOneMore(context, length);
                replaceNodes(arrayStrategy, capacity);
                array = copyStoreNode.execute(array, capacity);
            }
            setNode.execute(array, index, value);
            return array;
        }

        @Fallback
        public Object appendNewStrategy(Object store, int index, Object value) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            final ArrayStrategy currentStrategy = ArrayStrategy.ofStore(store);
            final ArrayStrategy valueStrategy = ArrayStrategy.forValue(value);
            final ArrayStrategy generalized = currentStrategy.generalize(valueStrategy);

            int currentCapacity = currentStrategy.capacityNode().execute(store);
            final int neededCapacity;
            if (index >= currentCapacity) {
                neededCapacity = ArrayUtils.capacityForOneMore(context, currentCapacity);
            } else {
                neededCapacity = currentCapacity;
            }

            replaceNodes(generalized, neededCapacity);

            final Object newStore = generalized.newStoreNode().execute(neededCapacity);
            currentStrategy.copyToNode().execute(store, newStore, 0, 0, index);
            generalized.setNode().execute(newStore, index, value);
            return newStore;
        }

    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class AppendArrayNode extends ArrayBuilderBaseNode {

        public static AppendArrayNode create(RubyContext context, ArrayStrategy arrayStrategy) {
            return AppendArrayNodeGen.create(context, arrayStrategy);
        }

        private final RubyContext context;
        protected final ArrayStrategy arrayStrategy;

        public AppendArrayNode(RubyContext context, ArrayStrategy arrayStrategy) {
            this.context = context;
            this.arrayStrategy = arrayStrategy;
        }

        public abstract Object executeAppend(Object array, int index, DynamicObject value);

        @Specialization(guards = { "arrayStrategy.matchesStore(array)", "otherStrategy.matches(other)",
                "arrayStrategy == generalized" }, limit = "STORAGE_STRATEGIES")
        protected Object appendSameStrategy(Object array, int index, DynamicObject other,
                @Cached("of(other)") ArrayStrategy otherStrategy,
                @Cached("arrayStrategy.generalize(otherStrategy)") ArrayStrategy generalized,
                @Cached("arrayStrategy.capacityNode()") ArrayOperationNodes.ArrayCapacityNode capacityNode,
                @Cached("arrayStrategy.copyStoreNode()") ArrayCopyStoreNode copyStoreNode,
                @Cached("otherStrategy.copyToNode()") ArrayCopyToNode copyToNode) {
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;

            int length = capacityNode.execute(array);
            if (neededSize > length) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(arrayStrategy, neededSize);
                final int capacity = ArrayUtils.capacity(context, length, neededSize);
                array = copyStoreNode.execute(array, capacity);
            }

            final Object otherStore = Layouts.ARRAY.getStore(other);
            copyToNode.execute(otherStore, array, 0, index, otherSize);
            return array;
        }

        @Specialization(guards = { "arrayStrategy.matchesStore(array)", "otherStrategy.matches(other)",
                "arrayStrategy != generalized" }, limit = "STORAGE_STRATEGIES")
        protected Object appendNewStrategy(Object array, int index, DynamicObject other,
                @Cached("of(other)") ArrayStrategy otherStrategy,
                @Cached("arrayStrategy.generalize(otherStrategy)") ArrayStrategy generalized,
                @Cached("arrayStrategy.capacityNode()") ArrayCapacityNode capacityNode,
                @Cached("arrayStrategy.copyToNode()") ArrayCopyToNode copyToNode,
                @Cached("otherStrategy.copyToNode()") ArrayCopyToNode otherCopyToNode,
                @Cached("generalized.newStoreNode()") ArrayNewStoreNode newStoreNode) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return fallback(array, index, other, capacityNode, copyToNode, otherCopyToNode, newStoreNode, generalized);
        }

        @Specialization(guards = "!arrayStrategy.matchesStore(array)")
        protected Object appendNewStrategy(Object array, int index, DynamicObject other) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            final ArrayStrategy currentStrategy = ArrayStrategy.ofStore(array);
            final ArrayStrategy otherStrategy = ArrayStrategy.of(other);
            final ArrayStrategy generalized = currentStrategy.generalize(otherStrategy);

            final ArrayCapacityNode capacityNode = currentStrategy.capacityNode();
            final ArrayCopyToNode copyToNode = currentStrategy.copyToNode();
            final ArrayCopyToNode otherCopyToNode = otherStrategy.copyToNode();
            final ArrayNewStoreNode newStoreNode = generalized.newStoreNode();

            return fallback(array, index, other, capacityNode, copyToNode, otherCopyToNode, newStoreNode, generalized);
        }

        private Object fallback(Object store, int index, DynamicObject other, ArrayCapacityNode capacityNode, ArrayCopyToNode copyToNode, ArrayCopyToNode otherCopyToNode,
                ArrayNewStoreNode newStoreNode,
                ArrayStrategy generalized) {
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;

            final Object newStore;

            final int length = capacityNode.execute(store);
            if (neededSize > length) {
                replaceNodes(generalized, neededSize);
                final int capacity = ArrayUtils.capacity(context, length, neededSize);
                newStore = newStoreNode.execute(capacity);
            } else {
                replaceNodes(generalized, length);
                newStore = newStoreNode.execute(length);
            }

            copyToNode.execute(store, newStore, 0, 0, index);

            final Object otherStore = Layouts.ARRAY.getStore(other);
            otherCopyToNode.execute(otherStore, newStore, 0, index, otherSize);

            return newStore;
        }

    }

}
