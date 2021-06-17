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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.extra.ffi.Pointer;

public final class ThreadLocalBuffer {

    public static final ThreadLocalBuffer NULL_BUFFER = new ThreadLocalBuffer(new Pointer(0, 0), 0, null);

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

    private long getEndAddress() {
        return start.getAddress() + start.getSize();
    }

    public void free(RubyThread thread, Pointer ptr, ConditionProfile freeProfile) {
        remaining += ptr.getSize();
        if (freeProfile.profile(parent != null && remaining == start.getSize())) {
            thread.ioBuffer = parent;
            start.freeNoAutorelease();
        }
    }

    public void freeAll(RubyThread thread) {
        ThreadLocalBuffer current = this;
        thread.ioBuffer = NULL_BUFFER;
        while (current != null) {
            current.remaining = 0;
            current.start.freeNoAutorelease();
            current = current.parent;
        }
    }

    public Pointer allocate(RubyThread thread, long size, ConditionProfile allocationProfile) {
        /* If there is space in the thread's existing buffer then we will return a pointer to that and reduce the
         * remaining space count. Otherwise we will either allocate a new buffer, or (if no space is currently being
         * used in the existing buffer) replace it with a larger one. */
        final long allocationSize = Math.max(size, 4);
        if (allocationProfile.profile(remaining >= allocationSize)) {
            if (start.isNull()) {
                throw CompilerDirectives.shouldNotReachHere(
                        reportNullAllocation(allocationSize));
            }
            Pointer pointer = new Pointer(this.getEndAddress() - this.remaining, allocationSize);
            remaining -= allocationSize;
            return pointer;
        } else {
            ThreadLocalBuffer newBuffer = allocateNewBlock(thread, allocationSize);
            if (newBuffer.start.isNull()) {
                throw CompilerDirectives.shouldNotReachHere(
                        reportNullAllocation(allocationSize));
            }
            Pointer pointer = new Pointer(
                    newBuffer.start.getAddress(),
                    allocationSize);
            newBuffer.remaining -= allocationSize;
            return pointer;
        }
    }

    @TruffleBoundary
    private String reportNullAllocation(final long allocationSize) {
        return String.format(
                "Allocating %d bytes buffer space (%d remaining) on null pointer.",
                allocationSize,
                remaining);
    }

    @TruffleBoundary
    private ThreadLocalBuffer allocateNewBlock(RubyThread thread, long size) {
        // Allocate a new buffer. Chain it if we aren't the default thread buffer, otherwise make a new default buffer.
        final long blockSize = Math.max(size, 1024);
        ThreadLocalBuffer buffer;
        if (this.parent != null || remaining != start.getSize()) {
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
