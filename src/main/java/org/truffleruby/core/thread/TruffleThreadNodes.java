/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.library.CachedLibrary;

@CoreModule("Truffle::ThreadOperations")
public class TruffleThreadNodes {

    @CoreMethod(names = "ruby_caller_special_variables", onSingleton = true, required = 1)
    @ImportStatic(ArrayGuards.class)
    public abstract static class FindRubyCallerSpecialStorage extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(limit = "storageStrategyLimit()")
        protected Object findRubyCaller(RubyArray modules,
                @CachedLibrary("modules.store") ArrayStoreLibrary stores,
                @Cached GetSpecialVariableStorage storageNode) {
            final int modulesSize = modules.size;
            Object[] moduleArray = stores.boxedCopyOfRange(modules.store, 0, modulesSize);
            Frame rubyCaller = getContext()
                    .getCallStack()
                    .getCallerFrameNotInModules(FrameAccess.MATERIALIZE, moduleArray);
            if (rubyCaller == null) {
                return nil;
            } else {
                return storageNode.execute(rubyCaller.materialize());
            }
        }

    }
}
