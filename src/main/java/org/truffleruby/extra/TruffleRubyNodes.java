/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.dsl.Cached;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.extra.ffi.Pointer;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule("TruffleRuby")
public abstract class TruffleRubyNodes {

    @CoreMethod(names = "graalvm_home", onSingleton = true)
    public abstract static class GraalvmHomeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object graalvmHome(
                @Cached StringNodes.MakeStringNode makeStringNode) {
            String value = getProperty("org.graalvm.home");
            if (value == null) {
                return nil;
            } else {
                return makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
        }

        @TruffleBoundary
        private static String getProperty(String key) {
            return System.getProperty(key);
        }

    }

    @CoreMethod(names = "jit?", onSingleton = true)
    public abstract static class GraalNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected static boolean isGraal() {
            return Truffle.getRuntime().getName().contains("Graal");
        }

    }

    @CoreMethod(names = "native?", onSingleton = true)
    public abstract static class NativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isNative() {
            return TruffleOptions.AOT;
        }

    }

    @CoreMethod(names = "cexts?", onSingleton = true)
    public abstract static class SulongNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isSulong() {
            return isSulongAvailable(getContext());
        }

        @TruffleBoundary
        public static boolean isSulongAvailable(RubyContext context) {
            return context.getEnv().isMimeTypeSupported(RubyLanguage.LLVM_BITCODE_MIME_TYPE);
        }

    }

    @CoreMethod(names = "full_memory_barrier", onSingleton = true)
    public abstract static class FullMemoryBarrierPrimitiveNode extends CoreMethodNode {

        @Specialization
        protected Object fullMemoryBarrier() {
            Pointer.UNSAFE.fullFence();

            return nil;
        }

    }

}
