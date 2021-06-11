/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
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

    public static final ThreadLocalBuffer NULL_BUFFER = new ThreadLocalBuffer(Pointer.NULL, 0, null);

    public final Pointer start;
    long remaining;
    final ThreadLocalBuffer parent;

    private ThreadLocalBuffer(
            Pointer start,
            long remaining,
            ThreadLocalBuffer parent) {
        this.start = start;
        this.remaining = remaining;
        this.parent = parent;
    }

    public void free(RubyThread thread, Pointer ptr, ConditionProfile freeProfile) {
        remaining += ptr.getSize();
        if (freeProfile.profile(parent != null && remaining == start.getSize())) {
            start.freeNoAutorelease();
            thread.ioBuffer = parent;
        }
    }

    public void freeAll(RubyThread thread) {
        ThreadLocalBuffer current = this;
        while (current != null) {
            current.start.freeNoAutorelease();
            current = current.parent;
        }
        thread.ioBuffer = NULL_BUFFER;
    }

    public Pointer allocate(long size, RubyThread thread, ConditionProfile allocationProfile) {
        /* If there is space in the thread's existing buffer then we will return a pointer to that and reduce the
         * remaining space count. Otherwise we will either allocate a new buffer, or (if no space is currently being
         * used in the existing buffer) replace it with a larger one. */
        if (allocationProfile.profile(start.getAddress() != 0 && remaining >= size)) {
            Pointer res = new Pointer(this.start.getAddress() + this.start.getSize() - this.remaining, size);
            remaining -= size;
            return res;
        } else {
            ThreadLocalBuffer newBuffer = allocateNewBlock(size, thread);
            Pointer res = new Pointer(
                    newBuffer.start.getAddress() + newBuffer.start.getSize() - newBuffer.remaining,
                    size);
            newBuffer.remaining -= size;
            return res;
        }
    }

    @TruffleBoundary
    private ThreadLocalBuffer allocateNewBlock(long size, RubyThread thread) {
        // Allocate a new buffer. Chain it if we aren't the default thread buffer, otherwise make a new default buffer.
        final long blockSize = Math.max(size, 1024);
        ThreadLocalBuffer buffer;
        if (this.parent != null && remaining == start.getSize()) {
            buffer = new ThreadLocalBuffer(Pointer.malloc(blockSize), blockSize, this);
        } else {
            // Free the old block
            this.free(thread, start, ConditionProfile.getUncached());
            // Create new bigger block
            buffer = new ThreadLocalBuffer(Pointer.malloc(blockSize), blockSize, null);
        }
        thread.ioBuffer = buffer;
        return buffer;
    }
}
