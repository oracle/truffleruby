/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.kernel;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.parser.ParserContext;

import java.io.IOException;

@CoreClass("Truffle::Kernel")
public abstract class TruffleKernelNodes {

    @CoreMethod(names = "at_exit", isModuleFunction = true, needsBlock = true, required = 1)
    public abstract static class AtExitSystemNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object atExit(boolean always, DynamicObject block) {
            getContext().getAtExitManager().add(block, always);
            return nil();
        }
    }

    @NodeChildren({
            @NodeChild(value = "file", type = RubyNode.class),
            @NodeChild(value = "wrap", type = RubyNode.class)
    })
    @CoreMethod(names = "load", isModuleFunction = true, required = 1, optional = 1)
    public abstract static class LoadNode extends CoreMethodNode {

        @CreateCast("wrap")
        public RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(false, inherit);
        }

        @Specialization(guards = "isRubyString(file)")
        public boolean load(VirtualFrame frame, DynamicObject file, boolean wrap,
                @Cached("create()") IndirectCallNode callNode,
                @Cached("create()") BranchProfile errorProfile) {
            if (wrap) {
                throw new UnsupportedOperationException();
            }

            try {
                final RubyRootNode rootNode = getContext().getCodeLoader().parse(
                        getContext().getSourceLoader().load(StringOperations.getString(file)),
                        UTF8Encoding.INSTANCE, ParserContext.TOP_LEVEL,
                        null, true, this);
                final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                        ParserContext.TOP_LEVEL, DeclarationContext.TOP_LEVEL, rootNode, null,
                        getContext().getCoreLibrary().getMainObject());
                deferredCall.call(callNode);
            } catch (IOException e) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().loadErrorCannotLoad(file.toString(), this));
            }

            return true;
        }

    }

}
