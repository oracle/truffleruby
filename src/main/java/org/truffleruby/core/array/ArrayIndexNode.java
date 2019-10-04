package org.truffleruby.core.array;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.objects.AllocateObjectNode;

import static org.truffleruby.core.array.ArrayHelpers.getSize;

public abstract class ArrayIndexNode extends ArrayCoreMethodNode {

    @Child private ArrayReadDenormalizedNode readNode;
    @Child private ArrayReadSliceDenormalizedNode readSliceNode;
    @Child private ArrayReadSliceNormalizedNode readNormalizedSliceNode;
    @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

    @Specialization
    protected Object index(DynamicObject array, int index, NotProvided length) {
        if (readNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readNode = insert(ArrayReadDenormalizedNodeGen.create(null, null));
        }
        return readNode.executeRead(array, index);
    }

    @Specialization
    protected DynamicObject slice(DynamicObject array, int start, int length) {
        if (length < 0) {
            return nil();
        }

        if (readSliceNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readSliceNode = insert(ArrayReadSliceDenormalizedNodeGen.create());
        }

        return readSliceNode.executeReadSlice(array, start, length);
    }

    @Specialization(guards = "isIntRange(range)")
    protected DynamicObject slice(DynamicObject array, DynamicObject range, NotProvided len,
            @Cached("createBinaryProfile()") ConditionProfile negativeBeginProfile,
            @Cached("createBinaryProfile()") ConditionProfile negativeEndProfile) {
        final int size = getSize(array);
        final int normalizedIndex = ArrayOperations
                .normalizeIndex(size, Layouts.INT_RANGE.getBegin(range), negativeBeginProfile);

        if (normalizedIndex < 0 || normalizedIndex > size) {
            return nil();
        } else {
            final int end = ArrayOperations
                    .normalizeIndex(size, Layouts.INT_RANGE.getEnd(range), negativeEndProfile);
            final int exclusiveEnd = ArrayOperations
                    .clampExclusiveIndex(size, Layouts.INT_RANGE.getExcludedEnd(range) ? end : end + 1);

            if (exclusiveEnd <= normalizedIndex) {
                return allocateObjectNode
                        .allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), ArrayStrategy.NULL_ARRAY_STORE, 0);
            }

            final int length = exclusiveEnd - normalizedIndex;

            if (readNormalizedSliceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readNormalizedSliceNode = insert(ArrayReadSliceNormalizedNodeGen.create());
            }

            return readNormalizedSliceNode.executeReadSlice(array, normalizedIndex, length);
        }
    }

    @Specialization(guards = { "!isInteger(index)", "!isIntRange(index)" })
    protected Object fallbackIndex(DynamicObject array, Object index, Object maybeLength) {
        return fallback(array, index, maybeLength);
    }

    @Specialization(guards = { "wasProvided(length)", "!isInteger(length)" })
    protected Object fallbackLength(DynamicObject array, Object index, Object length) {
        return fallback(array, index, length);
    }

    protected Object fallback(DynamicObject array, Object start, Object length) {
        throw new AbstractMethodError();
    }

}
