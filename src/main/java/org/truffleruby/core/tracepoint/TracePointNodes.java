/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.tracepoint;

import com.oracle.truffle.api.profiles.BranchProfile;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreModule(value = "TracePoint", isClass = true)
public abstract class TracePointNodes {

    @TruffleBoundary
    public static boolean isEnabled(DynamicObject tracePoint) {
        return Layouts.TRACE_POINT.getEvents(tracePoint)[0].hasEventBinding();
    }

    @TruffleBoundary
    public static boolean createEventBindings(RubyContext context, DynamicObject tracePoint) {
        final TracePointEvent[] events = Layouts.TRACE_POINT.getEvents(tracePoint);
        for (TracePointEvent event : events) {
            if (!event.setupEventBinding(context, tracePoint)) {
                return false;
            }
        }
        return true;
    }

    @TruffleBoundary
    public static boolean disposeEventBindings(DynamicObject tracePoint) {
        final TracePointEvent[] events = Layouts.TRACE_POINT.getEvents(tracePoint);
        for (TracePointEvent event : events) {
            if (!event.diposeEventBinding()) {
                return false;
            }
        }
        return true;
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, Layouts.TRACE_POINT.build(null, null));
        }

    }

    @Primitive(name = "tracepoint_initialize")
    public abstract static class InitializeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected DynamicObject initialize(DynamicObject tracePoint, DynamicObject eventsArray, DynamicObject block,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode) {
            final Object[] eventSymbols = arrayToObjectArrayNode.executeToObjectArray(eventsArray);

            final TracePointEvent[] events = new TracePointEvent[eventSymbols.length];
            for (int i = 0; i < eventSymbols.length; i++) {
                events[i] = createEvents((RubySymbol) eventSymbols[i]);
            }

            Layouts.TRACE_POINT.setEvents(tracePoint, events);
            Layouts.TRACE_POINT.setProc(tracePoint, block);
            return tracePoint;
        }

        @TruffleBoundary
        private TracePointEvent createEvents(RubySymbol eventSymbol) {
            if (eventSymbol == CoreSymbols.LINE) {
                return new TracePointEvent(TraceManager.LineTag.class, eventSymbol);
            } else if (eventSymbol == CoreSymbols.CLASS) {
                return new TracePointEvent(TraceManager.ClassTag.class, eventSymbol);
            } else if (eventSymbol == CoreSymbols.NEVER) {
                return new TracePointEvent(TraceManager.NeverTag.class, eventSymbol);
            } else {
                throw new UnsupportedOperationException(eventSymbol.getString());
            }
        }

    }

    @CoreMethod(names = "enable", needsBlock = true)
    public abstract static class EnableNode extends YieldingCoreMethodNode {

        @Specialization
        protected boolean enable(DynamicObject tracePoint, NotProvided block) {
            boolean setupDone = createEventBindings(getContext(), tracePoint);
            return !setupDone;
        }

        @Specialization
        protected Object enable(DynamicObject tracePoint, DynamicObject block) {
            final boolean setupDone = createEventBindings(getContext(), tracePoint);
            try {
                return yield(block);
            } finally {
                if (setupDone) {
                    disposeEventBindings(tracePoint);
                }
            }
        }
    }

    @CoreMethod(names = "disable", needsBlock = true)
    public abstract static class DisableNode extends YieldingCoreMethodNode {

        @Specialization
        protected Object disable(DynamicObject tracePoint, NotProvided block) {
            return disposeEventBindings(tracePoint);
        }

        @Specialization
        protected Object disable(DynamicObject tracePoint, DynamicObject block) {
            final boolean wasEnabled = disposeEventBindings(tracePoint);
            try {
                return yield(block);
            } finally {
                if (wasEnabled) {
                    createEventBindings(getContext(), tracePoint);
                }
            }
        }
    }

    @CoreMethod(names = "enabled?")
    public abstract static class EnabledNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean enabled(DynamicObject tracePoint) {
            return isEnabled(tracePoint);
        }
    }

    private abstract static class TracePointCoreNode extends CoreMethodArrayArgumentsNode {

        @Child private GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();
        private final BranchProfile errorProfile = BranchProfile.create();

        protected TracePointState getTracePointState() {
            final TracePointState state = Layouts.THREAD.getTracePointState(getCurrentRubyThreadNode.execute());
            if (!state.insideProc) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().runtimeError("access from outside", this));
            }
            return state;
        }

    }

    @CoreMethod(names = "event")
    public abstract static class EventNode extends TracePointCoreNode {
        @Specialization
        protected RubySymbol event(DynamicObject tracePoint) {
            return getTracePointState().event;
        }
    }

    @CoreMethod(names = "path")
    public abstract static class PathNode extends TracePointCoreNode {
        @Specialization
        protected DynamicObject path(DynamicObject tracePoint) {
            return getTracePointState().path;
        }
    }

    @CoreMethod(names = "lineno")
    public abstract static class LineNode extends TracePointCoreNode {
        @Specialization
        protected int line(DynamicObject tracePoint) {
            return getTracePointState().line;
        }
    }

    @CoreMethod(names = "binding")
    public abstract static class BindingNode extends TracePointCoreNode {
        @Specialization
        protected DynamicObject binding(DynamicObject tracePoint) {
            return getTracePointState().binding;
        }
    }

    @CoreMethod(names = "method_id")
    public abstract static class MethodIDNode extends TracePointCoreNode {
        @Specialization
        protected DynamicObject methodId(DynamicObject tracePoint,
                @Cached MakeStringNode makeStringNode) {
            final RubyBinding binding = getTracePointState().binding;
            final InternalMethod method = RubyArguments.getMethod(binding.frame);
            return makeStringNode.executeMake(method.getName(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "self")
    public abstract static class SelfNode extends TracePointCoreNode {
        @Specialization
        protected Object self(DynamicObject tracePoint) {
            final RubyBinding binding = getTracePointState().binding;
            return RubyArguments.getSelf(binding.frame);
        }
    }

}
