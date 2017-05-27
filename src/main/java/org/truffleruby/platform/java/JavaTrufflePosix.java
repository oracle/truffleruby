/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.java;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import jnr.constants.platform.Errno;
import jnr.constants.platform.Fcntl;
import jnr.constants.platform.OpenFlags;
import jnr.posix.FileStat;
import jnr.posix.POSIX;
import org.truffleruby.RubyContext;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.platform.posix.JNRTrufflePosix;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JavaTrufflePosix extends JNRTrufflePosix {

    private static class OpenFile {

        private final RandomAccessFile randomAccessFile;
        private final int flags;

        private OpenFile(RandomAccessFile randomAccessFile, int flags) {
            this.randomAccessFile = randomAccessFile;
            this.flags = flags;
        }

        public RandomAccessFile getRandomAccessFile() {
            return randomAccessFile;
        }

        public int getFlags() {
            return flags;
        }
    }

    private final AtomicInteger nextFileHandle = new AtomicInteger(3);
    private final Map<Integer, OpenFile> fileHandles = new ConcurrentHashMap<>();

    public JavaTrufflePosix(RubyContext context, POSIX delegateTo) {
        super(context, delegateTo);
    }

    @TruffleBoundary
    @Override
    public int fcntlInt(int fd, Fcntl fcntlConst, int arg) {
        if (fcntlConst.longValue() == Fcntl.F_GETFL.longValue()) {
            switch (fd) {
                case 0:
                    return OpenFlags.O_RDONLY.intValue();
                case 1:
                case 2:
                    return OpenFlags.O_WRONLY.intValue();
            }

            final OpenFile openFile = fileHandles.get(fd);

            if (openFile != null) {
                return openFile.getFlags();
            }
        }

        return super.fcntlInt(fd, fcntlConst, arg);
    }

    @TruffleBoundary
    @Override
    public int open(CharSequence path, int flags, int perm) {
        if (perm != 0666) {
            return super.open(path, flags, perm);
        }

        final int fileHandle = nextFileHandle.getAndIncrement();

        if (fileHandle < 0) {
            throw new UnsupportedOperationException();
        }

        final int basicMode = flags & 3;
        final String mode;

        if (basicMode == OpenFlags.O_RDONLY.intValue()) {
            mode = "r";
        } else if (basicMode == OpenFlags.O_WRONLY.intValue()) {
            mode = "w";
        } else if (basicMode == OpenFlags.O_RDWR.intValue()) {
            mode = "rw";
        } else {
            return super.open(path, flags, perm);
        }

        final RandomAccessFile randomAccessFile;

        try {
            randomAccessFile = new RandomAccessFile(path.toString(), mode);
        } catch (FileNotFoundException e) {
            return -1;
        }

        fileHandles.put(fileHandle, new OpenFile(randomAccessFile, flags));

        return fileHandle;
    }

    @TruffleBoundary
    @Override
    public int read(int fd, ByteBuffer buf, int n) {
        return pread(fd, buf.array(), buf.arrayOffset(), n);
    }

    @TruffleBoundary
    @Override
    public int read(int fd, byte[] buf, int n) {
        return pread(fd, buf, 0, n);
    }

    @TruffleBoundary
    @Override
    public int write(int fd, ByteBuffer buf, int n) {
        return pwrite(fd, buf.array(), buf.arrayOffset(), n);
    }

    @TruffleBoundary
    @Override
    public int write(int fd, byte[] buf, int n) {
        return pwrite(fd, buf, 0, n);
    }

    @TruffleBoundary
    @Override
    public int close(int fd) {
        final OpenFile openFile = fileHandles.get(fd);

        if (openFile != null) {
            fileHandles.remove(fd);

            try {
                openFile.getRandomAccessFile().close();
            } catch (IOException e) {
                return -1;
            }

            return 0;
        }

        return super.close(fd);
    }

    @Override
    public int getgid() {
        return 0;
    }

    @TruffleBoundary
    @Override
    public FileStat allocateStat() {
        return new TruffleJavaFileStat(getPosix(), null);
    }

    @TruffleBoundary
    @Override
    public String getenv(String envName) {
        final String javaValue = System.getenv(envName);

        if (javaValue != null) {
            return javaValue;
        }

        return super.getenv(envName);
    }

    @TruffleBoundary
    @Override
    public int isatty(int fd) {
        return System.console() != null ? 1 : 0;
    }

    @TruffleBoundary
    private int pwrite(int fd, byte[] buf, int offset, int n) {
        if (fd == 1 || fd == 2) {
            return polyglotWrite(fd, buf, offset, n);
        }

        throw new UnsupportedOperationException();
    }

    @TruffleBoundary
    private int pread(int fd, byte[] buf, int offset, int n) {
        if (fd == 0) {
            return polyglotRead(buf, offset, n);
        }

        final OpenFile openFile = fileHandles.get(fd);

        if (openFile != null) {
            final int read;

            try {
                read = openFile.getRandomAccessFile().read(buf, offset, n);
            } catch (IOException e) {
                return -1;
            }

            if (read == -1) {
                errno(Errno.ETIMEDOUT.intValue());
            }

            return read;
        }

        throw new UnsupportedOperationException();
    }

}
