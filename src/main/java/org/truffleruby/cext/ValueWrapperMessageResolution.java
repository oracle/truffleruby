/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;

@MessageResolution(receiverType = ValueWrapper.class)
public class ValueWrapperMessageResolution {

    @CanResolve
    public abstract static class IsInstance extends Node {

        protected boolean test(TruffleObject receiver) {
            return receiver instanceof ValueWrapper;
        }
    }

    @Resolve(message = "IS_POINTER")
    public static abstract class ForeignIsPointerNode extends Node {

        protected boolean access(VirtualFrame frame, ValueWrapper wrapper) {
            return true;
        }
    }

    @Resolve(message = "TO_NATIVE")
    public static abstract class ForeignToNativeNode extends Node {
        protected Object access(VirtualFrame frame, ValueWrapper receiver) {
            return receiver;
        }
    }

    @Resolve(message = "AS_POINTER")
    public static abstract class ForeignAsPointerNode extends Node {

        @CompilationFinal private RubyContext context;
        private final BranchProfile createHandleProfile = BranchProfile.create();
        private final BranchProfile taggedObjectProfile = BranchProfile.create();

        protected long access(VirtualFrame frame, ValueWrapper wrapper) {
            long handle = wrapper.getHandle();
            if (handle == ValueWrapperManager.UNSET_HANDLE) {
                createHandleProfile.enter();
                handle = getContext().getValueWrapperManager().createNativeHandle(wrapper);
            }
            if (ValueWrapperManager.isTaggedObject(handle)) {
                taggedObjectProfile.enter();
                getContext().getMarkingService().keepObject(wrapper);
            }
            return handle;
        }

        public RubyContext getContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = RubyLanguage.getCurrentContext();
            }

            return context;
        }

    }

}
