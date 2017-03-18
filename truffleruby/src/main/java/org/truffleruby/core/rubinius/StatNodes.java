/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rubinius;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import jnr.posix.FileStat;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.Visibility;

@CoreClass("File::Stat")
public abstract class StatNodes {

    static FileStat getStat(DynamicObject rubyStat) {
        return Layouts.STAT.getStat(rubyStat);
    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public static abstract class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        public DynamicObject allocate(DynamicObject classToAllocate) {
            return Layouts.STAT.createStat(coreLibrary().getStatFactory(), null);
        }

    }

    @Primitive(name = "stat_atime")
    public static abstract class StatAtimePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long atime(DynamicObject rubyStat) {
            return getStat(rubyStat).atime();
        }

    }

    @Primitive(name = "stat_ctime")
    public static abstract class StatCtimePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long ctime(DynamicObject rubyStat) {
            return getStat(rubyStat).ctime();
        }

    }

    @Primitive(name = "stat_mtime")
    public static abstract class StatMtimePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long mtime(DynamicObject rubyStat) {
            return getStat(rubyStat).mtime();
        }

    }

    @CoreMethod(names = "nlink")
    public static abstract class NLinkNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public int nlink(DynamicObject rubyStat) {
            return getStat(rubyStat).nlink();
        }

    }

    @CoreMethod(names = "rdev")
    public static abstract class RdevNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public long rdev(DynamicObject rubyStat) {
            return getStat(rubyStat).rdev();
        }

    }

    @CoreMethod(names = "blksize")
    public static abstract class StatBlksizeNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public long blksize(DynamicObject rubyStat) {
            return getStat(rubyStat).blockSize();
        }

    }

    @CoreMethod(names = "blocks")
    public static abstract class StatBlocksNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public long blocks(DynamicObject rubyStat) {
            return getStat(rubyStat).blocks();
        }

    }

    @CoreMethod(names = "dev")
    public static abstract class StatDevNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public long dev(DynamicObject rubyStat) {
            return getStat(rubyStat).dev();
        }

    }

    @CoreMethod(names = "ino")
    public static abstract class StatInoNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public long ino(DynamicObject rubyStat) {
            return getStat(rubyStat).ino();
        }

    }

    @Primitive(name = "stat_stat")
    public static abstract class StatStatPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(path)")
        public int stat(DynamicObject rubyStat, DynamicObject path) {
            final FileStat stat = posix().allocateStat();
            final int code = posix().stat(StringOperations.decodeUTF8(path), stat);

            if (code == 0) {
                Layouts.STAT.setStat(rubyStat, stat);
            }
            
            return code;
        }

        @Specialization(guards = "!isRubyString(path)")
        public Object stat(DynamicObject rubyStat, Object path) {
            return null;
        }

    }

    @Primitive(name = "stat_fstat")
    public static abstract class StatFStatPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int fstat(DynamicObject rubyStat, int fd) {
            final FileStat stat = posix().allocateStat();
            final int code = posix().fstat(fd, stat);

            if (code == 0) {
                Layouts.STAT.setStat(rubyStat, stat);
            }

            return code;
        }

    }

    @Primitive(name = "stat_lstat")
    public static abstract class StatLStatPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(path)")
        public int lstat(DynamicObject rubyStat, DynamicObject path) {
            final FileStat stat = posix().allocateStat();
            final int code = posix().lstat(RopeOperations.decodeRope(StringOperations.rope(path)), stat);

            if (code == 0) {
                Layouts.STAT.setStat(rubyStat, stat);
            }

            return code;
        }

        @Specialization(guards = "!isRubyString(path)")
        public Object stat(DynamicObject rubyStat, Object path) {
            return null;
        }

    }

    @CoreMethod(names = "size")
    public static abstract class StatSizeNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public long size(DynamicObject rubyStat) {
            return getStat(rubyStat).st_size();
        }

    }

    @CoreMethod(names = "mode")
    public static abstract class StatModeNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public int mode(DynamicObject rubyStat) {
            return getStat(rubyStat).mode();
        }

    }

    @CoreMethod(names = "gid")
    public static abstract class StatGIDNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public int gid(DynamicObject rubyStat) {
            return getStat(rubyStat).gid();
        }

    }

    @CoreMethod(names = "uid")
    public static abstract class StatUIDNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public int uid(DynamicObject rubyStat) {
            return getStat(rubyStat).uid();
        }

    }

}
