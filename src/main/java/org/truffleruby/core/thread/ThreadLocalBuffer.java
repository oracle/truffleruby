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

    public static final ThreadLocalBuffer NULL_BUFFER = new ThreadLocalBuffer(true, Pointer.NULL, 0, 0, null);

    final boolean isBlockStart;
    public final Pointer start;
    final long bufferSize;
    final long tailSize;
    final ThreadLocalBuffer parent;

    private ThreadLocalBuffer(
            boolean isBlockStart,
            Pointer start,
            long bufferSize,
            long tailSize,
            ThreadLocalBuffer parent) {
        this.isBlockStart = isBlockStart;
        this.start = start;
        this.bufferSize = bufferSize;
        this.tailSize = tailSize;
        this.parent = parent;
    }

    public ThreadLocalBuffer free() {
        if (isBlockStart) {
            start.freeNoAutorelease();
        }
        return parent;
    }

    public void freeAll() {
        ThreadLocalBuffer current = this;
        while (current != null) {
            current.free();
            current = current.parent;
        }
    }

    public ThreadLocalBuffer allocate(long size, ConditionProfile allocationPoProfile) {
        if (allocationPoProfile.profile(tailSize >= size)) {
            return new ThreadLocalBuffer(
                    false,
                    new Pointer(this.start.getAddress() + this.bufferSize, size),
                    size,
                    tailSize - size,
                    this);
        } else {
            return allocateNewBlock(size);
        }
    }

    @TruffleBoundary
    private ThreadLocalBuffer allocateNewBlock(long size) {
        // Allocate a new buffer. Chain it if we aren't the default thread buffer, otherwise make a new default buffer.
        if (this.parent != null) {
            final long blockSize = Math.max(size, 1024);
            return new ThreadLocalBuffer(true, Pointer.malloc(blockSize), size, blockSize - size, this);
        } else {
            // Free the old block
            this.free();
            // Create new bigger block
            final long blockSize = Math.max(size, 1024);
            final ThreadLocalBuffer newParent = new ThreadLocalBuffer(
                    true,
                    Pointer.malloc(blockSize),
                    0,
                    blockSize,
                    null);
            return new ThreadLocalBuffer(
                    false,
                    new Pointer(newParent.start.getAddress(), size),
                    size,
                    blockSize - size,
                    newParent);
        }
    }
}
