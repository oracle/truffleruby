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
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendArrayNodeGen;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.AppendOneNodeGen;
import org.truffleruby.core.array.ArrayBuilderNodeFactory.EnsureCapacityNodeGen;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

/*
 * TODO(CS): how does this work when when multithreaded? Could a node get replaced by someone else and
 * then suddenly you're passing it a store type it doesn't expect?
 */

public abstract class ArrayBuilderNode extends RubyBaseNode {

    private static class ArrayBuilderProxyNode extends ArrayBuilderNode {

        @Child StartNode startNode = new StartNode(ArrayStrategy.forValue(0), 0);
        @Child EnsureCapacityNode ensureNode = EnsureCapacityNode.create();
        @Child AppendArrayNode appendArrayNode = AppendArrayNode.create();
        @Child AppendOneNode appendOneNode = AppendOneNode.create();

        @Override
        public Object start() {
            return startNode.start();
        }

        @Override
        public Object start(int length) {
            return startNode.start(length);
        }

        @Override
        public Object ensure(Object store, int length) {
            return ensureNode.executeEnsure(store, length);
        }

        @Override
        public Object appendArray(Object store, int index, DynamicObject array) {
            return appendArrayNode.executeAppend(store, index, array);
        }

        @Override
        public Object appendValue(Object store, int index, Object value) {
            return appendOneNode.executeAppend(store, index, value);
        }

        @Override
        public Object finish(Object store, int length) {
            return store;
        }

        public void updateStrategy(ArrayStrategy strategy, int size) {
            if (startNode.replacement(strategy, size)) {
                ensureNode.replace(EnsureCapacityNode.create());
                appendArrayNode.replace(AppendArrayNode.create());
                appendOneNode.replace(AppendOneNode.create());
            }
        }
    }

    public abstract static class ArrayBuilderSubNode extends Node {

        protected void replaceNodes(ArrayStrategy strategy, int size) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            final ArrayBuilderProxyNode parent = (ArrayBuilderProxyNode) getParent();
            parent.updateStrategy(strategy, size);
        }
    }

    public static class StartNode extends ArrayBuilderSubNode {

        private final ConditionProfile lengthProfile = ConditionProfile.createBinaryProfile();

        private final ArrayStrategy strategy;
        private final int expectedLength;

        public StartNode(ArrayStrategy strategy, int expectedLength) {
            this.strategy = strategy;
            this.expectedLength = expectedLength;
        }

        public Object start() {
            return strategy.newArray(expectedLength).getArray();
        }

        public Object start(int length) {
            if (lengthProfile.profile(length > expectedLength)) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                replaceNodes(strategy, length);
            }
            return strategy.newArray(length).getArray();
        }

        public boolean replacement(ArrayStrategy strategy, int newLength) {
            final StartNode newNode = new StartNode(strategy, Math.max(expectedLength, newLength));
            this.replace(newNode);
            return (strategy != this.strategy);
        }
    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class AppendOneNode extends ArrayBuilderSubNode {

        public static AppendOneNode create() {
            return AppendOneNodeGen.create();
        }

        public abstract Object executeAppend(Object array, int index, Object value);

        @Specialization(guards = { "strategy.matchesStore(array)", "strategy.accepts(value)" },
                        limit = "1")
        public Object appendCompatibleType(Object array, int index, Object value,
                @Cached("ofStore(array)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile expandProfile) {
            ArrayMirror mirror = strategy.newMirrorFromStore(array);
            if (expandProfile.profile(index >= mirror.getLength())) {
                final int capacity = Math.max(mirror.getLength(), index + 1);
                replaceNodes(strategy, capacity);
                mirror = mirror.copyArrayAndMirror(capacity);
            }
            mirror.set(index, value);
            return mirror.getArray();
        }

        @Specialization(guards = { "arrayStrategy.matchesStore(array)", "!arrayStrategy.accepts(value)", "valueStrategy.specializesFor(value)" },
                        limit = "STORAGE_STRATEGIES")
        public Object appendNewStrategy(Object array, int index, Object value,
                @Cached("ofStore(array)") ArrayStrategy arrayStrategy,
                @Cached("forValue(value)") ArrayStrategy valueStrategy,
                @Cached("arrayStrategy.generalize(valueStrategy)") ArrayStrategy generalized) {
            final ArrayMirror mirror = arrayStrategy.newMirrorFromStore(array);
            final int neededCapacity = Math.max(mirror.getLength(), index + 1);
            replaceNodes(generalized, neededCapacity);
            final ArrayMirror newMirror = generalized.newArray(neededCapacity);
            mirror.copyTo(newMirror, 0, 0, index);
            newMirror.set(index, value);
            return newMirror.getArray();
        }

    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class AppendArrayNode extends ArrayBuilderSubNode {

        public static AppendArrayNode create() {
            return AppendArrayNodeGen.create();
        }

        public abstract Object executeAppend(Object array, int index, DynamicObject value);

        @Specialization(guards = { "strategy.matchesStore(array)", "strategy.matches(other)" }, limit = "1")
        public Object appendCompatibleType(Object array, int index, DynamicObject other,
                @Cached("ofStore(array)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile expandProfile) {
            ArrayMirror mirror = strategy.newMirrorFromStore(array);
            final ArrayMirror otherMirror = strategy.newMirror(other);
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;
            if (expandProfile.profile(neededSize > mirror.getLength())) {
                replaceNodes(strategy, index);
                final int capacity = Math.max(mirror.getLength(), neededSize);
                mirror = mirror.copyArrayAndMirror(capacity);
            }
            otherMirror.copyTo(mirror, 0, index, otherSize);
            return mirror.getArray();
        }

        @Specialization(guards = { "arrayStrategy != otherStrategy", "arrayStrategy == generalized",
                                   "arrayStrategy.matchesStore(array)", "otherStrategy.matches(other)" },
                        limit = "STORAGE_STRATEGIES")
        public Object appendNewStrategy(Object array, int index, DynamicObject other,
                @Cached("ofStore(array)") ArrayStrategy arrayStrategy,
                @Cached("of(other)") ArrayStrategy otherStrategy,
                @Cached("arrayStrategy.generalize(otherStrategy)") ArrayStrategy generalized,
                @Cached("createBinaryProfile()") ConditionProfile expandProfile) {
            ArrayMirror mirror = arrayStrategy.newMirrorFromStore(array);
            final ArrayMirror otherMirror = otherStrategy.newMirror(other);
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;
            if (expandProfile.profile(neededSize > mirror.getLength())) {
                replaceNodes(arrayStrategy, neededSize);
                final int capacity = Math.max(mirror.getLength(), neededSize);
                mirror = mirror.copyArrayAndMirror(capacity);
            }
            otherMirror.copyTo(mirror, 0, index, otherSize);
            return mirror.getArray();
        }

        @Specialization(guards = { "arrayStrategy != generalized",
                                   "arrayStrategy.matchesStore(array)", "otherStrategy.matches(other)" },
                        limit = "STORAGE_STRATEGIES")
        public Object appendNewStrategy(Object array, int index, DynamicObject other,
                @Cached("ofStore(array)") ArrayStrategy arrayStrategy,
                @Cached("of(other)") ArrayStrategy otherStrategy,
                @Cached("arrayStrategy.generalize(otherStrategy)") ArrayStrategy generalized) {
            final ArrayMirror mirror = arrayStrategy.newMirrorFromStore(array);
            final ArrayMirror otherMirror = otherStrategy.newMirror(other);
            final int otherSize = Layouts.ARRAY.getSize(other);
            final int neededSize = index + otherSize;
            final ArrayMirror newMirror;
            if (neededSize > mirror.getLength()) {
                replaceNodes(generalized, neededSize);
                final int capacity = Math.max(mirror.getLength(), neededSize);
                newMirror = generalized.newArray(capacity);
            } else {
                replaceNodes(generalized, mirror.getLength());
                newMirror = generalized.newArray(mirror.getLength());
            }
            mirror.copyTo(newMirror, 0, 0, index);
            otherMirror.copyTo(newMirror, 0, index, otherSize);
            return newMirror.getArray();
        }

    }

    @ImportStatic(ArrayGuards.class)
    public abstract static class EnsureCapacityNode extends ArrayBuilderSubNode {
        public static EnsureCapacityNode create() {
            return EnsureCapacityNodeGen.create();
        }

        public abstract Object executeEnsure(Object array, int index);

        @Specialization(guards = { "strategy.matchesStore(array)" })
        public Object ensure(Object array, int size,
                @Cached("ofStore(array)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile expandProfile) {
            final ArrayMirror mirror = strategy.newMirrorFromStore(array);
            if (expandProfile.profile(mirror.getLength() < size)) {
                replaceNodes(strategy, size);
                return mirror.copyArrayAndMirror(size).getArray();
            }
            return array;
        }

    }

    public static ArrayBuilderNode create() {
        return new ArrayBuilderProxyNode();
    }

    public abstract Object start();
    public abstract Object start(int length);
    public abstract Object ensure(Object store, int length);
    public abstract Object appendArray(Object store, int index, DynamicObject array);
    public abstract Object appendValue(Object store, int index, Object value);
    public abstract Object finish(Object store, int length);
}
