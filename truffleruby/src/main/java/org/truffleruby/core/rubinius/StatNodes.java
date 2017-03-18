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
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
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
import org.truffleruby.language.SnippetNode;
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

        @Specialization
        public Object atime(
                VirtualFrame frame,
                DynamicObject rubyStat,
                @Cached("new()") SnippetNode snippetNode) {
            final long time = getStat(rubyStat).atime();
            return snippetNode.execute(frame, "Time.at(time)", "time", time);
        }

    }

    @Primitive(name = "stat_ctime")
    public static abstract class StatCtimePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object ctime(
                VirtualFrame frame,
                DynamicObject rubyStat,
                @Cached("new()") SnippetNode snippetNode) {
            final long time = getStat(rubyStat).ctime();
            return snippetNode.execute(frame, "Time.at(time)", "time", time);
        }

    }

    @Primitive(name = "stat_mtime")
    public static abstract class StatMtimePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object mtime(
                VirtualFrame frame,
                DynamicObject rubyStat,
                @Cached("new()") SnippetNode snippetNode) {
            final long time = getStat(rubyStat).mtime();
            return snippetNode.execute(frame, "Time.at(time)", "time", time);
        }

    }

    @Primitive(name = "stat_nlink")
    public static abstract class NlinkPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int nlink(DynamicObject rubyStat) {
            return getStat(rubyStat).nlink();
        }

    }

    @Primitive(name = "stat_rdev")
    public static abstract class RdevPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long rdev(DynamicObject rubyStat) {
            return getStat(rubyStat).rdev();
        }

    }

    @Primitive(name = "stat_blksize")
    public static abstract class StatBlksizePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long blksize(DynamicObject rubyStat) {
            return getStat(rubyStat).blockSize();
        }

    }

    @Primitive(name = "stat_blocks")
    public static abstract class StatBlocksPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long blocks(DynamicObject rubyStat) {
            return getStat(rubyStat).blocks();
        }

    }

    @Primitive(name = "stat_dev")
    public static abstract class StatDevPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long dev(DynamicObject rubyStat) {
            return getStat(rubyStat).dev();
        }

    }

    @Primitive(name = "stat_ino")
    public static abstract class StatInoPrimitiveNode extends PrimitiveArrayArgumentsNode {

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

    @Primitive(name = "stat_size")
    public static abstract class StatSizePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long size(DynamicObject rubyStat) {
            return getStat(rubyStat).st_size();
        }

    }

    @Primitive(name = "stat_mode")
    public static abstract class StatModePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int mode(DynamicObject rubyStat) {
            return getStat(rubyStat).mode();
        }

    }

    @Primitive(name = "stat_gid")
    public static abstract class StatGIDPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int gid(DynamicObject rubyStat) {
            return getStat(rubyStat).gid();
        }

    }

    @Primitive(name = "stat_uid")
    public static abstract class StatUIDPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int uid(DynamicObject rubyStat) {
            return getStat(rubyStat).uid();
        }

    }

}
