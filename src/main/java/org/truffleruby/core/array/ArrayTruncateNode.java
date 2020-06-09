package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;

import static org.truffleruby.Layouts.ARRAY;

/**
 * Truncates an array by setting its size and clearing the remainder of the store with default values.
 */
@ImportStatic(ArrayGuards.class)
public abstract class ArrayTruncateNode extends RubyBaseNode {

    public static ArrayTruncateNode create() {
        return ArrayTruncateNodeGen.create();
    }

    public abstract void execute(DynamicObject array, int size);

    @Specialization(
            guards = { "getSize(array) > size", "arrays.isMutable(getStore(array))" },
            limit = "storageStrategyLimit()")
    void truncate(DynamicObject array, int size,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {

        ARRAY.setSize(array, size);
        stores.clear(ARRAY.getStore(array), size, ARRAY.getSize(array) - size);
    }

    @Specialization(
            guards = { "getSize(array) > size", "!stores.isMutable(array)" },
            limit = "storageStrategyLimit()")
    void truncateCopy(DynamicObject array, int size,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {

        final Object store = ARRAY.getStore(array);
        final Object newStore = stores.allocator(store).allocate(size);
        stores.copyContents(store, 0, newStore, 0, size);
        ARRAY.setStore(array, newStore);
        ARRAY.setSize(array, size);
    }

    @Specialization(guards = "getSize(array) <= size")
    void truncate(DynamicObject array, int size) {
    }
}
