/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib;

import java.util.Set;

import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.regexp.MatchDataNodes.ValuesNode;
import org.truffleruby.core.regexp.RubyMatchData;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.AllocationTracing.AllocationTrace;
import org.truffleruby.language.objects.ObjectGraph;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule("Truffle::ObjSpace")
public abstract class ObjSpaceNodes {

    @CoreMethod(names = "memsize_of", onSingleton = true, required = 1)
    public abstract static class MemsizeOfNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected int memsizeOfArray(RubyArray object) {
            return memsizeOfObject(object) + object.size;
        }

        @Specialization
        protected int memsizeOfHash(RubyHash object) {
            return memsizeOfObject(object) + object.size;
        }

        @Specialization
        protected int memsizeOfString(RubyString object) {
            return memsizeOfObject(object) + object.rope.byteLength();
        }

        @Specialization
        protected int memsizeOfString(ImmutableRubyString object) {
            return 1 + object.rope.byteLength();
        }

        @Specialization
        protected int memsizeOfMatchData(RubyMatchData object,
                @Cached ValuesNode matchDataValues) {
            return memsizeOfObject(object) + matchDataValues.execute(object).length;
        }

        @Specialization(
                guards = {
                        "!isRubyArray(object)",
                        "!isRubyHash(object)",
                        "isNotRubyString(object)",
                        "!isRubyMatchData(object)" })
        protected int memsizeOfObject(RubyDynamicObject object) {
            return 1 + object.getShape().getPropertyListInternal(false).size();
        }

        @Specialization(guards = "!isRubyDynamicObject(object)")
        protected int memsize(Object object) {
            return 0;
        }
    }

    @CoreMethod(names = "adjacent_objects", onSingleton = true, required = 1)
    public abstract static class AdjacentObjectsNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected RubyArray adjacentObjects(RubyDynamicObject object) {
            final Set<Object> objects = ObjectGraph.getAdjacentObjects(object);
            return createArray(objects.toArray());
        }

        @Fallback
        protected Object adjacentObjectsPrimitive(Object object) {
            return nil;
        }
    }

    @CoreMethod(names = "root_objects", onSingleton = true)
    public abstract static class RootObjectsNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected RubyArray rootObjects() {
            final Set<Object> objects = ObjectGraph
                    .stopAndGetRootObjects("ObjectSpace.reachable_objects_from_root", getContext(), this);
            return createArray(objects.toArray());
        }
    }

    @CoreMethod(names = "trace_allocations_start", onSingleton = true)
    public abstract static class TraceAllocationsStartNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object traceAllocationsStart() {
            getContext().getObjectSpaceManager().traceAllocationsStart(getLanguage());
            return nil;
        }
    }

    @CoreMethod(names = "trace_allocations_stop", onSingleton = true)
    public abstract static class TraceAllocationsStopNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object traceAllocationsStop() {
            getContext().getObjectSpaceManager().traceAllocationsStop(getLanguage());
            return nil;
        }
    }

    @CoreMethod(names = "trace_allocations_clear", onSingleton = true)
    public abstract static class TraceAllocationsClearNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object traceAllocationsClear() {
            getContext().getObjectSpaceManager().traceAllocationsClear();
            return nil;
        }
    }

    @Primitive(name = "allocation_class_path")
    public abstract static class AllocationClassPathNode extends PrimitiveArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object allocationInfo(RubyDynamicObject object,
                @Cached MakeStringNode makeStringNode) {
            AllocationTrace trace = getAllocationTrace(getContext(), object);
            if (trace == null) {
                return nil;
            } else {
                return makeStringNode.executeMake(trace.className, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
            }
        }
    }

    @Primitive(name = "allocation_generation")
    public abstract static class AllocationGenerationNode extends PrimitiveArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object allocationInfo(RubyDynamicObject object) {
            AllocationTrace trace = getAllocationTrace(getContext(), object);
            if (trace == null) {
                return nil;
            } else {
                return trace.gcGeneration;
            }
        }
    }

    @Primitive(name = "allocation_method_id")
    public abstract static class AllocationMethodIDNode extends PrimitiveArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object allocationInfo(RubyDynamicObject object) {
            AllocationTrace trace = getAllocationTrace(getContext(), object);
            if (trace == null) {
                return nil;
            } else {
                final String allocatingMethod = trace.allocatingMethod;
                if (allocatingMethod.startsWith("<")) { // <top (required)> or <main> are hidden in MRI
                    return nil;
                } else if (allocatingMethod.equals("__allocate__")) { // The allocator function is hidden in MRI
                    return getLanguage().coreSymbols.NEW;
                } else {
                    return getLanguage().getSymbol(allocatingMethod);
                }
            }
        }
    }

    @Primitive(name = "allocation_sourcefile")
    public abstract static class AllocationSourceFileNode extends PrimitiveArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object allocationInfo(RubyDynamicObject object,
                @Cached MakeStringNode makeStringNode) {
            AllocationTrace trace = getAllocationTrace(getContext(), object);
            if (trace == null) {
                return nil;
            } else {
                final String sourcePath = getLanguage().getSourcePath(trace.allocatingSourceSection.getSource());
                return makeStringNode.executeMake(sourcePath, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
            }
        }
    }

    @Primitive(name = "allocation_sourceline")
    public abstract static class AllocationSourceLineNode extends PrimitiveArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object allocationInfo(RubyDynamicObject object) {
            AllocationTrace trace = getAllocationTrace(getContext(), object);
            if (trace == null) {
                return nil;
            } else {
                return trace.allocatingSourceSection.getStartLine();
            }
        }
    }

    @TruffleBoundary
    private static AllocationTrace getAllocationTrace(RubyContext context, RubyDynamicObject object) {
        final AllocationTrace trace = (AllocationTrace) DynamicObjectLibrary
                .getUncached()
                .getOrDefault(object, Layouts.ALLOCATION_TRACE_IDENTIFIER, null);
        if (trace != null && trace.tracingGeneration == context.getObjectSpaceManager().getTracingGeneration()) {
            return trace;
        } else {
            return null;
        }
    }

}
