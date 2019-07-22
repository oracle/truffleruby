/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.binding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.collections.Memo;

@CoreClass("Truffle::Binding")
public abstract class TruffleBindingNodes {

    @CoreMethod(names = "of_caller", isModuleFunction = true)
    public abstract static class OfCallerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject ofCaller() {
            /*
             * When you use this method you're asking for the binding of the caller at the call site. When we get into
             * this method, that is then the binding of the caller of the caller.
             */

            final Memo<Integer> frameCount = new Memo<>(0);
            final Memo<SourceSection> sourceSection = new Memo<>(null);

            final MaterializedFrame frame = Truffle.getRuntime().iterateFrames(frameInstance -> {
                if (frameCount.get() == 2) {
                    sourceSection.set(frameInstance.getCallNode().getEncapsulatingSourceSection());
                    return frameInstance.getFrame(FrameAccess.MATERIALIZE).materialize();
                } else {
                    frameCount.set(frameCount.get() + 1);
                    return null;
                }
            });

            if (frame == null) {
                return nil();
            }

            return BindingNodes.createBinding(getContext(), frame, sourceSection.get());
        }

    }

}
