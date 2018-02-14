/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendArrayNodeGen;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendOneNodeGen;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

/*
 * TODO(CS): how does this work when when multithreaded? Could a node get replaced by someone else and
 * then suddenly you're passing it a store type it doesn't expect?
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

        public void updateStrategy(ArrayStrategy strategy, int size) {
            final ArrayStrategy oldStrategy = startNode.strategy;
            final ArrayStrategy newStrategy = oldStrategy.generalize(strategy);

            startNode.replacement(strategy, size);

            if (oldStrategy != newStrategy) {
                if (appendArrayNode != null) {
                    appendArrayNode.replace(AppendArrayNode.create(getContext(), newStrategy));
                }
                if (appendOneNode != null) {
                    appendOneNode.replace(AppendOneNode.create(getContext(), newStrategy));
                }
            }
        }
    }

    public abstract static class ArrayBuilderBaseNode extends Node {

        protected void replaceNodes(ArrayStrategy strategy, int size) {
            final ArrayBuilderProxyNode parent = (ArrayBuilderProxyNode) getParent();
            parent.updateStrategy(strategy, size);
        }
    }

    public static class StartNode extends ArrayBuilderBaseNode {

        final ArrayStrategy strategy;
        final int expectedLength;

        public StartNode(ArrayStrategy strategy, int expectedLength) {
            this.strategy = strategy;
            this.expectedLength = expectedLength;
        }

        public Object start() {
            return strategy.newArray(expectedLength).getArray();
        }

        public Object start(int length) {
            if (length > expectedLength) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(strategy, length);
            }
            return strategy.newArray(length).getArray();
        }

        public void replacement(ArrayStrategy strategy, int newLength) {
            final int newExpectedLength = Math.max(expectedLength, newLength);
            if (expectedLength != newExpectedLength) {
                final StartNode newNode = new StartNode(strategy, newExpectedLength);
                this.replace(newNode);
            }
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
        public Object appendCompatibleType(Object array, int index, Object value) {
            ArrayMirror mirror = arrayStrategy.newMirrorFromStore(array);
            if (index >= mirror.getLength()) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                final int capacity = ArrayUtils.capacityForOneMore(context, mirror.getLength());
                replaceNodes(arrayStrategy, capacity);
                mirror = mirror.copyArrayAndMirror(capacity);
            }
            mirror.set(index, value);
            return mirror.getArray();
        }

        @Fallback
        public Object appendNewStrategy(Object array, int index, Object value) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            final ArrayStrategy currentStrategy = ArrayStrategy.ofStore(array);
            final ArrayStrategy valueStrategy = ArrayStrategy.forValue(value);
            final ArrayStrategy generalized = currentStrategy.generalize(valueStrategy);

            final ArrayMirror mirror = currentStrategy.newMirrorFromStore(array);
            final int neededCapacity = ArrayUtils.capacityForOneMore(context, mirror.getLength());

            replaceNodes(generalized, neededCapacity);

            final ArrayMirror newMirror = generalized.newArray(neededCapacity);
            mirror.copyTo(newMirror, 0, 0, index);
            newMirror.set(index, value);
            return newMirror.getArray();
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
                                   "arrayStrategy == generalized" },
                        limit = "STORAGE_STRATEGIES")
        public Object appendSameStrategy(Object array, int index, DynamicObject other,
                @Cached("of(other)") ArrayStrategy otherStrategy,
                @Cached("arrayStrategy.generalize(otherStrategy)") ArrayStrategy generalized) {
            ArrayMirror mirror = arrayStrategy.newMirrorFromStore(array);
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;

            if (neededSize > mirror.getLength()) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(arrayStrategy, neededSize);
                final int capacity = ArrayUtils.capacity(context, mirror.getLength(), neededSize);
                mirror = mirror.copyArrayAndMirror(capacity);
            }

            final ArrayMirror otherMirror = otherStrategy.newMirror(other);
            otherMirror.copyTo(mirror, 0, index, otherSize);
            return mirror.getArray();
        }

        @Specialization(guards = { "arrayStrategy.matchesStore(array)", "otherStrategy.matches(other)",
                                   "arrayStrategy != generalized" },
                        limit = "STORAGE_STRATEGIES")
        public Object appendNewStrategy(Object array, int index, DynamicObject other,
                @Cached("of(other)") ArrayStrategy otherStrategy,
                @Cached("arrayStrategy.generalize(otherStrategy)") ArrayStrategy generalized) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            return fallback(array, index, other, arrayStrategy, otherStrategy, generalized);
        }

        /*
         * This is a corner case which can occur with recursion. If a recursive call changes the
         * storage strategy then we handle that here.
         */
        @Specialization(guards = "!arrayStrategy.matchesStore(array)")
        public Object appendNewStrategy(Object array, int index, DynamicObject other) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            final ArrayStrategy currentStrategy = ArrayStrategy.ofStore(array);
            final ArrayStrategy otherStrategy = ArrayStrategy.of(other);
            final ArrayStrategy generalized = currentStrategy.generalize(otherStrategy);

            return fallback(array, index, other, currentStrategy, otherStrategy, generalized);
        }

        private Object fallback(Object array, int index, DynamicObject other, ArrayStrategy currentStrategy, ArrayStrategy otherStrategy, ArrayStrategy generalized) {
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;

            final ArrayMirror mirror = currentStrategy.newMirrorFromStore(array);
            final ArrayMirror newMirror;

            if (neededSize > mirror.getLength()) {
                replaceNodes(generalized, neededSize);
                final int capacity = ArrayUtils.capacity(context, mirror.getLength(), neededSize);
                newMirror = generalized.newArray(capacity);
            } else {
                replaceNodes(generalized, mirror.getLength());
                newMirror = generalized.newArray(mirror.getLength());
            }

            mirror.copyTo(newMirror, 0, 0, index);

            final ArrayMirror otherMirror = otherStrategy.newMirror(other);
            otherMirror.copyTo(newMirror, 0, index, otherSize);

            return newMirror.getArray();
        }

    }

}
