/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.extra;

import com.kenai.jffi.Platform;
import com.kenai.jffi.Platform.OS;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.time.GetTimeZoneNode;

@CoreClass("Truffle::POSIX")
public abstract class TrufflePosixNodes {

    @TruffleBoundary
    private static void invalidateENV(String name) {
        if (name.equals("TZ")) {
            GetTimeZoneNode.invalidateTZ();
        }
    }

    @Primitive(name = "posix_invalidate_env", needsSelf = false)
    public abstract static class InvalidateEnvNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(envVar)")
        public DynamicObject invalidate(DynamicObject envVar) {
            invalidateENV(StringOperations.getString(envVar));
            return envVar;
        }

    }

    @CoreMethod(names = "memset", isModuleFunction = true, required = 3, lowerFixnum = 2)
    public abstract static class MemsetNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyPointer(pointer)")
        public DynamicObject memset(DynamicObject pointer, int c, long length) {
            Layouts.POINTER.getPointer(pointer).writeBytes(0, length, (byte) c);
            return pointer;
        }

    }

    @CoreMethod(names = "major", isModuleFunction = true, required = 1)
    public abstract static class MajorNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int major(long dev) {
            if (Platform.getPlatform().getOS() == OS.SOLARIS) {
                return (int) (dev >> 32); // Solaris has major number in the upper 32 bits.
            } else {
                return (int) ((dev >> 24) & 0xff);

            }
        }
    }

    @CoreMethod(names = "minor", isModuleFunction = true, required = 1)
    public abstract static class MinorNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int minor(long dev) {
            if (Platform.getPlatform().getOS() == OS.SOLARIS) {
                return (int) dev; // Solaris has minor number in the lower 32 bits.
            } else {
                return (int) (dev & 0xffffff);
            }
        }

    }

}
