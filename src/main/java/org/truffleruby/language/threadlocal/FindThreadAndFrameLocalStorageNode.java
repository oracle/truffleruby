/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class FindThreadAndFrameLocalStorageNode extends RubyBaseNode {

    public FindThreadAndFrameLocalStorageNode() {
    }

    public abstract ThreadAndFrameLocalStorage execute(DynamicObject symbol, MaterializedFrame frame);

    protected int getLimit() {
        return getContext().getOptions().FRAME_VARIABLE_ACCESS_CACHE;
    }

    @Specialization(guards = { "symbol == cachedSymbol", "strategy.matches(frame)" }, limit = "getLimit()")
    protected ThreadAndFrameLocalStorage getStorageCached(DynamicObject symbol, MaterializedFrame frame,
            @Cached("symbol") DynamicObject cachedSymbol,
            @Cached("of(getContext(), frame, getSymbolString(symbol))") StorageInFrameFinder strategy) {
        return strategy.getStorage(getContext(), frame);
    }

    @Specialization(guards = "isRubySymbol(symbol)", replaces = "getStorageCached")
    protected ThreadAndFrameLocalStorage getStorage(DynamicObject symbol, MaterializedFrame frame) {
        return getStorageSearchingDeclarations(getContext(), frame, Layouts.SYMBOL.getString(symbol));
    }

    protected String getSymbolString(DynamicObject symbol) {
        return Layouts.SYMBOL.getString(symbol);
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

        public boolean matches(MaterializedFrame callerFrame) {
            return callerFrame != null && callerFrame.getFrameDescriptor() == descriptor;
        }

        public ThreadAndFrameLocalStorage getStorage(RubyContext context, MaterializedFrame callerFrame) {
            final MaterializedFrame frame = RubyArguments.getDeclarationFrame(callerFrame, depth);
            return getStorageFromFrame(context, frame, slot, defaultValue, true);
        }

        public static StorageInFrameFinder of(RubyContext context, MaterializedFrame callerFrame, String variableName) {
            FrameDescriptor descriptor = callerFrame.getFrameDescriptor();

            int depth = getVariableDeclarationFrameDepth(callerFrame, variableName);
            MaterializedFrame methodFrame = RubyArguments.getDeclarationFrame(callerFrame, depth);
            FrameSlot slot = getVariableFrameSlotWrite(methodFrame, variableName);
            Object defaultValue = methodFrame.getFrameDescriptor().getDefaultValue();
            return new StorageInFrameFinder(descriptor, slot, defaultValue, depth);
        }
    }

    private static int getVariableDeclarationFrameDepth(MaterializedFrame topFrame, String variableName) {
        MaterializedFrame frame = topFrame;
        int count = 0;

        while (true) {
            final FrameSlot slot = getVariableSlot(frame, variableName);
            if (slot != null) {
                return count;
            }

            final MaterializedFrame nextFrame = RubyArguments.getDeclarationFrame(frame);
            if (nextFrame != null) {
                frame = nextFrame;
                count++;
            } else {
                return count;
            }
        }
    }

    private static MaterializedFrame getVariableDeclarationFrame(MaterializedFrame topFrame, String variableName) {
        MaterializedFrame frame = topFrame;

        while (true) {
            final FrameSlot slot = getVariableSlot(frame, variableName);
            if (slot != null) {
                return frame;
            }

            final MaterializedFrame nextFrame = RubyArguments.getDeclarationFrame(frame);
            if (nextFrame != null) {
                frame = nextFrame;
            } else {
                return frame;
            }
        }
    }

    private static FrameSlot getVariableSlot(MaterializedFrame frame, String variableName) {
        final FrameDescriptor descriptor = frame.getFrameDescriptor();
        synchronized (descriptor) {
            return descriptor.findFrameSlot(variableName);
        }
    }

    private static FrameSlot getVariableFrameSlotWrite(MaterializedFrame frame, String variableName) {
        final FrameDescriptor descriptor = frame.getFrameDescriptor();
        assert variableName != null && !variableName.isEmpty();
        synchronized (descriptor) {
            return descriptor.findOrAddFrameSlot(variableName, FrameSlotKind.Object);
        }
    }

    @TruffleBoundary
    private static ThreadAndFrameLocalStorage getStorageSearchingDeclarations(RubyContext context,
            MaterializedFrame topFrame, String variableName) {
        final MaterializedFrame frame = getVariableDeclarationFrame(topFrame, variableName);
        if (frame == null) {
            return null;
        }
        FrameSlot slot = getVariableFrameSlotWrite(frame.materialize(), variableName);
        return getStorageFromFrame(context, frame, slot, frame.getFrameDescriptor().getDefaultValue(), true);
    }

    private static ThreadAndFrameLocalStorage getStorageFromFrame(RubyContext context, MaterializedFrame frame,
            FrameSlot slot, Object defaultValue, boolean add) {
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
