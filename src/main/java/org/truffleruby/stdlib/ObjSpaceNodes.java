/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.regexp.MatchDataNodes.ValuesNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.objects.ObjectGraph;

import java.util.Set;

@CoreClass("Truffle::ObjSpace")
public abstract class ObjSpaceNodes {

    @CoreMethod(names = "memsize_of", isModuleFunction = true, required = 1)
    public abstract static class MemsizeOfNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNil(object)")
        public int memsizeOfNil(DynamicObject object) {
            return 0;
        }

        @Specialization(guards = "isRubyArray(object)")
        public int memsizeOfArray(DynamicObject object) {
            return memsizeOfObject(object) + Layouts.ARRAY.getSize(object);
        }

        @Specialization(guards = "isRubyHash(object)")
        public int memsizeOfHash(DynamicObject object) {
            return memsizeOfObject(object) + Layouts.HASH.getSize(object);
        }

        @Specialization(guards = "isRubyString(object)")
        public int memsizeOfString(DynamicObject object) {
            return memsizeOfObject(object) + StringOperations.rope(object).byteLength();
        }

        @Specialization(guards = "isRubyMatchData(object)")
        public int memsizeOfMatchData(DynamicObject object,
                                      @Cached ValuesNode matchDataValues) {
            return memsizeOfObject(object) + matchDataValues.execute(object).length;
        }

        @Specialization(guards = {
                "!isNil(object)",
                "!isRubyArray(object)",
                "!isRubyHash(object)",
                "!isRubyString(object)",
                "!isRubyMatchData(object)"
        })
        public int memsizeOfObject(DynamicObject object) {
            return 1 + object.getShape().getPropertyListInternal(false).size();
        }

        @Specialization(guards = "!isDynamicObject(object)")
        public int memsize(Object object) {
            return 0;
        }
    }

    @CoreMethod(names = "adjacent_objects", isModuleFunction = true, required = 1)
    public abstract static class AdjacentObjectsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject adjacentObjects(DynamicObject object) {
            final Set<DynamicObject> objects = ObjectGraph.getAdjacentObjects(object);
            return createArray(objects.toArray());
        }

        @Fallback
        public DynamicObject adjacentObjectsPrimitive(Object object) {
            return nil();
        }

    }

    @CoreMethod(names = "root_objects", isModuleFunction = true)
    public abstract static class RootObjectsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject rootObjects() {
            final Set<DynamicObject> objects = ObjectGraph.stopAndGetRootObjects(this, getContext());
            return createArray(objects.toArray());
        }

    }

    @CoreMethod(names = "trace_allocations_start", isModuleFunction = true)
    public abstract static class TraceAllocationsStartNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject traceAllocationsStart() {
            getContext().getObjectSpaceManager().traceAllocationsStart();
            return nil();
        }

    }

    @CoreMethod(names = "trace_allocations_stop", isModuleFunction = true)
    public abstract static class TraceAllocationsStopNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject traceAllocationsStop() {
            getContext().getObjectSpaceManager().traceAllocationsStop();
            return nil();
        }

    }

}
