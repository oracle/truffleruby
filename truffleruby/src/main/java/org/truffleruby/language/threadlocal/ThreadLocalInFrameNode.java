package org.truffleruby.language.threadlocal;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class ThreadLocalInFrameNode extends RubyBaseNode {

    protected final String variableName;
    protected final int limit;

    public ThreadLocalInFrameNode(String variableName, int limit) {
        this.variableName = variableName;
        this.limit = limit;
    }

    public abstract ThreadLocalObject execute(MaterializedFrame frame);

    @Specialization(guards = "strategy.matches(frame)", limit = "limit")
    public ThreadLocalObject getThreadLocalCached(MaterializedFrame frame,
            @Cached("of(getContext(), frame, variableName)") SetThreadLocalInFrameStrategy strategy) {
        return strategy.getThreadLocal(getContext(), frame);
    }

    @Specialization(replaces = "getThreadLocalCached")
    public ThreadLocalObject getThreadLocal(MaterializedFrame frame) {
        return getThreadLocalSearchingDeclarations(getContext(), frame, variableName);
    }


    public static abstract class SetThreadLocalInFrameStrategy {
        protected final FrameDescriptor descriptor;
        protected final FrameSlot slot;
        protected final Object defaultValue;

        protected SetThreadLocalInFrameStrategy(FrameDescriptor fd, FrameSlot fs, Object defaultValue) {
            this.descriptor = fd;
            this.slot = fs;
            this.defaultValue = defaultValue;
        }

        public boolean matches(Frame callerFrame) {
            return callerFrame != null && callerFrame.getFrameDescriptor() == descriptor;
        }

        public boolean matchesBlock(DynamicObject block) {
            return matches(Layouts.PROC.getDeclarationFrame(block));
        }

        public abstract ThreadLocalObject getThreadLocal(RubyContext context, MaterializedFrame callerFrame);

        public ThreadLocalObject getThreadLocalBlock(RubyContext context, DynamicObject block) {
            return getThreadLocal(context, Layouts.PROC.getDeclarationFrame(block).materialize());
        }

        public static SetThreadLocalInFrameStrategy ofBlock(RubyContext context, DynamicObject block, String varName) {
            Frame frame = Layouts.PROC.getDeclarationFrame(block);
            if (frame == null) {
                return NullStrategy.INSTANCE;
            }
            return of(context, frame, varName);
        }

        public static SetThreadLocalInFrameStrategy of(RubyContext context, Frame aFrame, String variableName) {
            MaterializedFrame callerFrame = aFrame.materialize();
            FrameDescriptor fd = callerFrame.getFrameDescriptor();

            int depth = getVariableDeclarationFrameDepth(callerFrame, variableName);
            MaterializedFrame mf = RubyArguments.getDeclarationFrame(callerFrame, depth);
            FrameSlot fs = getVariableFrameSlotWrite(mf, variableName);
            Object defaultValue = mf.getFrameDescriptor().getDefaultValue();
            return new NormalStrategy(fd, fs, defaultValue, depth);
        }
    }

    private static class NullStrategy extends SetThreadLocalInFrameStrategy {
        private static NullStrategy INSTANCE = new NullStrategy();

        private NullStrategy() {
            super(null, null, null);
        }

        @Override
        public ThreadLocalObject getThreadLocal(RubyContext context, MaterializedFrame callerFrame) {
            return null;
        }

        @Override
        public ThreadLocalObject getThreadLocalBlock(RubyContext context, DynamicObject block) {
            return null;
        }

        @Override
        public boolean matches(Frame callerFrame) {
            return callerFrame == null;
        }
    }

    private static class NormalStrategy extends SetThreadLocalInFrameStrategy {
        private final int depth;

        public NormalStrategy(FrameDescriptor fd, FrameSlot fs, Object defaultValue, int depth) {
            super(fd, fs, defaultValue);
            this.depth = depth;
        }

        @Override
        public ThreadLocalObject getThreadLocal(RubyContext context, MaterializedFrame callerFrame) {
            final MaterializedFrame frame = RubyArguments.getDeclarationFrame(callerFrame, depth);
            return getThreadLocalObjectFromFrame(context, frame, slot, defaultValue, true);
        }
    }

    private static int getVariableDeclarationFrameDepth(MaterializedFrame topFrame, String variableName) {
        Frame frame = topFrame;
        int count = 0;

        while (true) {
            final FrameSlot slot = getVariableSlot(frame, variableName);
            if (slot != null) {
                return count;
            }

            final Frame nextFrame = RubyArguments.getDeclarationFrame(frame);
            if (nextFrame != null) {
                frame = nextFrame;
                count++;
            } else {
                return count;
            }
        }
    }

    private static Frame getVariableDeclarationFrame(Frame topFrame, String variableName) {
        Frame frame = topFrame;

        while (true) {
            final FrameSlot slot = getVariableSlot(frame, variableName);
            if (slot != null) {
                return frame;
            }

            final Frame nextFrame = RubyArguments.getDeclarationFrame(frame);
            if (nextFrame != null) {
                frame = nextFrame;
            } else {
                return frame;
            }
        }
    }

    private static FrameSlot getVariableSlot(Frame frame, String variableName) {
        return frame.getFrameDescriptor().findFrameSlot(variableName);
    }

    private static FrameSlot getVariableFrameSlotWrite(MaterializedFrame frame, String variableName) {
        FrameDescriptor fd = frame.getFrameDescriptor();
        FrameSlot fs = fd.findFrameSlot(variableName);
        if (fs == null) {
            fs = fd.addFrameSlot(variableName, FrameSlotKind.Object);
        }
        return fs;
    }

    @TruffleBoundary
    private static ThreadLocalObject getThreadLocalSearchingDeclarations(RubyContext context, Frame topFrame, String variableName) {
        final Frame frame = getVariableDeclarationFrame(topFrame, variableName);
        if (frame == null) {
            return null;
        }
        FrameSlot slot = getVariableFrameSlotWrite(frame.materialize(), variableName);
        return getThreadLocalObjectFromFrame(context, frame, slot, frame.getFrameDescriptor().getDefaultValue(), true);
    }

    private static ThreadLocalObject getThreadLocalObjectFromFrame(RubyContext context, Frame frame, FrameSlot slot, Object defaultValue, boolean add) {
        final Object previousMatchData = frame.getValue(slot);

        if (previousMatchData == defaultValue) { // Never written to
            if (add) {
                ThreadLocalObject threadLocalObject = new ThreadLocalObject(context);
                frame.setObject(slot, threadLocalObject);
                return threadLocalObject;
            } else {
                return null;
            }
        }

        return (ThreadLocalObject) previousMatchData;
    }
}
