package org.truffleruby.language.threadlocal;

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

public abstract class FindThreadAndFrameLocalStorageNode extends RubyBaseNode {

    protected final String variableName;

    public FindThreadAndFrameLocalStorageNode(String variableName) {
        this.variableName = variableName;
    }

    public abstract ThreadAndFrameLocalStorage execute(MaterializedFrame frame);

    protected int getLimit() {
        return getContext().getOptions().FRAME_VARIABLE_ACCESS_LIMIT;
    }

    @Specialization(guards = "strategy.matches(frame)", limit = "getLimit()")
    public ThreadAndFrameLocalStorage getStorageCached(MaterializedFrame frame,
            @Cached("of(getContext(), frame, variableName)") StorageInFrameFinder strategy) {
        return strategy.getStorage(getContext(), frame);
    }

    @Specialization(replaces = "getStorageCached")
    public ThreadAndFrameLocalStorage getStorage(MaterializedFrame frame) {
        return getStorageSearchingDeclarations(getContext(), frame, variableName);
    }


    public static class StorageInFrameFinder {
        protected final FrameDescriptor descriptor;
        protected final FrameSlot slot;
        protected final Object defaultValue;
        private final int depth;

        protected StorageInFrameFinder(FrameDescriptor fd, FrameSlot fs, Object defaultValue, int depth) {
            this.descriptor = fd;
            this.slot = fs;
            this.defaultValue = defaultValue;
            this.depth = depth;
        }

        public boolean matches(Frame callerFrame) {
            return callerFrame != null && callerFrame.getFrameDescriptor() == descriptor;
        }

        public ThreadAndFrameLocalStorage getStorage(RubyContext context, MaterializedFrame callerFrame) {
            final MaterializedFrame frame = RubyArguments.getDeclarationFrame(callerFrame, depth);
            return getStorageFromFrame(context, frame, slot, defaultValue, true);
        }

        public static StorageInFrameFinder of(RubyContext context, Frame aFrame, String variableName) {
            MaterializedFrame callerFrame = aFrame.materialize();
            FrameDescriptor fd = callerFrame.getFrameDescriptor();

            int depth = getVariableDeclarationFrameDepth(callerFrame, variableName);
            MaterializedFrame mf = RubyArguments.getDeclarationFrame(callerFrame, depth);
            FrameSlot fs = getVariableFrameSlotWrite(mf, variableName);
            Object defaultValue = mf.getFrameDescriptor().getDefaultValue();
            return new StorageInFrameFinder(fd, fs, defaultValue, depth);
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
    private static ThreadAndFrameLocalStorage getStorageSearchingDeclarations(RubyContext context, Frame topFrame, String variableName) {
        final Frame frame = getVariableDeclarationFrame(topFrame, variableName);
        if (frame == null) {
            return null;
        }
        FrameSlot slot = getVariableFrameSlotWrite(frame.materialize(), variableName);
        return getStorageFromFrame(context, frame, slot, frame.getFrameDescriptor().getDefaultValue(), true);
    }

    private static ThreadAndFrameLocalStorage getStorageFromFrame(RubyContext context, Frame frame, FrameSlot slot, Object defaultValue, boolean add) {
        final Object previousMatchData = frame.getValue(slot);

        if (previousMatchData == defaultValue) { // Never written to
            if (add) {
                ThreadAndFrameLocalStorage storageObject = new ThreadAndFrameLocalStorage(context);
                frame.setObject(slot, storageObject);
                return storageObject;
            } else {
                return null;
            }
        }

        return (ThreadAndFrameLocalStorage) previousMatchData;
    }
}
