/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.extra.ffi.Pointer;

public final class ThreadLocalBuffer {

    public static final ThreadLocalBuffer NULL_BUFFER = new ThreadLocalBuffer(new Pointer(0, 0), null);

    public final Pointer start;
    long remaining;
    private final ThreadLocalBuffer parent;

    private ThreadLocalBuffer(Pointer start, ThreadLocalBuffer parent) {
        this.start = start;
        this.remaining = start.getSize();
        this.parent = parent;
    }

    private boolean invariants() {
        assert remaining >= 0 && remaining <= start.getSize();
        return true;
    }

    private boolean isEmpty() {
        return remaining == start.getSize();
    }

    private long cursor() {
        return start.getEndAddress() - remaining;
    }

    private void freeMemory() {
        remaining = 0;
        start.freeNoAutorelease();
    }

    public void free(RubyThread thread, Pointer ptr, ConditionProfile freeProfile) {
        assert ptr.getEndAddress() == cursor() : "free(" + Long.toHexString(ptr.getEndAddress()) +
                ") but expected " + Long.toHexString(cursor()) + " to be free'd first";
        remaining += ptr.getSize();
        assert invariants();
        if (freeProfile.profile(parent != null && isEmpty())) {
            thread.ioBuffer = parent;
            freeMemory();
        }
    }

    public void freeAll(RubyThread thread) {
        ThreadLocalBuffer current = this;
        thread.ioBuffer = NULL_BUFFER;
        while (current != null) {
            current.freeMemory();
            current = current.parent;
        }
    }

    public Pointer allocate(RubyThread thread, long size, ConditionProfile allocationProfile) {
        /* If there is space in the thread's existing buffer then we will return a pointer to that and reduce the
         * remaining space count. Otherwise we will either allocate a new buffer, or (if no space is currently being
         * used in the existing buffer) replace it with a larger one. */

        /* We ensure we allocate a non-zero number of bytes so we can track the allocation. This avoids returning null
         * or reallocating a buffer that we technically have a pointer to. */
        final long allocationSize = Math.max(size, 4);
        if (allocationProfile.profile(remaining >= allocationSize)) {
            final Pointer pointer = new Pointer(cursor(), allocationSize);
            remaining -= allocationSize;
            assert invariants();
            return pointer;
        } else {
            final ThreadLocalBuffer newBuffer = allocateNewBlock(thread, allocationSize);
            final Pointer pointer = new Pointer(newBuffer.start.getAddress(), allocationSize);
            newBuffer.remaining -= allocationSize;
            assert newBuffer.invariants();
            return pointer;
        }
    }

    @TruffleBoundary
    private ThreadLocalBuffer allocateNewBlock(RubyThread thread, long size) {
        // Allocate a new buffer. Chain it if we aren't the default thread buffer, otherwise make a new default buffer.
        final long blockSize = Math.max(size, 1024);
        final ThreadLocalBuffer newBuffer;
        if (this.parent == null && this.isEmpty()) {
            // Free the old block
            freeMemory();
            // Create new bigger block
            newBuffer = new ThreadLocalBuffer(Pointer.malloc(blockSize), null);
        } else {
            newBuffer = new ThreadLocalBuffer(Pointer.malloc(blockSize), this);
        }
        thread.ioBuffer = newBuffer;
        return newBuffer;
    }
}
