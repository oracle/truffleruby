/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.shared.BuildInformationImpl;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@CoreModule("TruffleRuby")
public abstract class TruffleRubyNodes {

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

    @CoreMethod(names = "revision", onSingleton = true)
    public abstract static class RevisionNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject revision() {
            return StringOperations.createFrozenString(
                    getContext(),
                    RopeOperations.encodeAscii(BuildInformationImpl.INSTANCE.getRevision(), USASCIIEncoding.INSTANCE));
        }

    }

    @CoreMethod(names = "full_memory_barrier", isModuleFunction = true)
    public abstract static class FullMemoryBarrierPrimitiveNode extends CoreMethodNode {

        @Specialization
        protected Object fullMemoryBarrier() {
            Pointer.UNSAFE.fullFence();

            return nil();
        }

    }

}
