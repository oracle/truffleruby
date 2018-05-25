/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.shared.BuildInformationImpl;

@CoreClass("TruffleRuby")
public abstract class TruffleRubyNodes {

    @CoreMethod(names = "graal?", onSingleton = true)
    public abstract static class GraalNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public static boolean isGraal() {
            return Truffle.getRuntime().getName().contains("Graal");
        }

    }

    @CoreMethod(names = "native?", onSingleton = true)
    public abstract static class NativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isNative() {
            return TruffleOptions.AOT;
        }

    }

    @CoreMethod(names = "sulong?", onSingleton = true)
    public abstract static class SulongNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isSulong() {
            return isSulongAvailable(getContext());
        }

        @TruffleBoundary
        public static boolean isSulongAvailable(RubyContext context) {
            return context.getEnv().isMimeTypeSupported(RubyLanguage.SULONG_BITCODE_BASE64_MIME_TYPE);
        }

    }

    @CoreMethod(names = "revision", onSingleton = true)
    public abstract static class RevisionNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject revision() {
            return StringOperations.createFrozenString(getContext(),
                    RopeOperations.encodeAscii(BuildInformationImpl.INSTANCE.getRevision(), USASCIIEncoding.INSTANCE));
        }

    }

    @CoreMethod(names = "full_memory_barrier", isModuleFunction = true)
    public abstract static class FullMemoryBarrierPrimitiveNode extends CoreMethodNode {

        @Specialization
        public Object fullMemoryBarrier() {
            Pointer.UNSAFE.fullFence();

            return nil();
        }

    }

}
