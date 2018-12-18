/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyNode;

import static org.truffleruby.core.array.ArrayHelpers.setSize;

@NodeChildren({
        @NodeChild("array"),
        @NodeChild("value"),
})
@ImportStatic(ArrayGuards.class)
public abstract class ArrayAppendOneNode extends RubyNode {

    public static ArrayAppendOneNode create() {
        return ArrayAppendOneNodeGen.create(null, null);
    }

    public abstract DynamicObject executeAppendOne(DynamicObject array, Object value);

    // Append of the correct type

    @Specialization(guards = { "strategy.matches(array)", "strategy.accepts(value)" }, limit = "STORAGE_STRATEGIES")
    public DynamicObject appendOneSameType(DynamicObject array, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.lengthNode()") ArrayOperationNodes.ArrayLengthNode lengthNode,
            @Cached("strategy.copyStoreNode()") ArrayOperationNodes.ArrayCopyStoreNode copyStoreNode,
            @Cached("strategy.setNode()") ArrayOperationNodes.ArraySetNode setNode,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = Layouts.ARRAY.getStore(array);
        final int oldSize = Layouts.ARRAY.getSize(array);
        final int newSize = oldSize + 1;
        final int length = lengthNode.execute(store);

        if (extendProfile.profile(newSize > length)) {
            final int capacity = ArrayUtils.capacityForOneMore(getContext(), length);
            final Object newStore = copyStoreNode.execute(store, capacity);
            setNode.execute(newStore, oldSize, value);
            strategy.setStore(array, newStore);
            setSize(array, newSize);
        } else {
            setNode.execute(store, oldSize, value);
            setSize(array, newSize);
        }
        return array;
    }

    // Append forcing a generalization

    @Specialization(guards = {
            "strategy.matches(array)", "valueStrategy.specializesFor(value)",
    }, limit = "ARRAY_STRATEGIES")
    public DynamicObject appendOneGeneralizeNonMutable(DynamicObject array, Object value,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("forValue(value)") ArrayStrategy valueStrategy,
            @Cached("strategy.generalize(valueStrategy)") ArrayStrategy generalizedStrategy,
            @Cached("strategy.lengthNode()") ArrayOperationNodes.ArrayLengthNode lengthNode,
            @Cached("strategy.copyToNode()") ArrayOperationNodes.ArrayCopyToNode copyToNode,
            @Cached("generalizedStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode,
            @Cached("generalizedStrategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode) {
        assert strategy != valueStrategy;
        final int oldSize = strategy.getSize(array);
        final int newSize = oldSize + 1;
        final Object currentStore = Layouts.ARRAY.getStore(array);
        final int oldCapacity = lengthNode.execute(currentStore);
        final int newCapacity = newSize > oldCapacity ? ArrayUtils.capacityForOneMore(getContext(), oldCapacity) : oldCapacity;
        final Object newStore = newStoreNode.execute(newCapacity);
        copyToNode.execute(currentStore, newStore, 0, 0, oldSize);
        setNode.execute(newStore, oldSize, value);
        generalizedStrategy.setStore(array, newStore);
        setSize(array, newSize);
        return array;
    }

}
