/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.io.IOException;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.globals.ReadSimpleGlobalVariableNode;
import org.truffleruby.language.globals.WriteSimpleGlobalVariableNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FileLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.threadlocal.FindThreadAndFrameLocalStorageNode;
import org.truffleruby.language.threadlocal.FindThreadAndFrameLocalStorageNodeGen;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule("Truffle::KernelOperations")
public abstract class TruffleKernelNodes {

    @CoreMethod(names = "at_exit", onSingleton = true, needsBlock = true, required = 1)
    public abstract static class AtExitSystemNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object atExit(boolean always, RubyProc block) {
            getContext().getAtExitManager().add(block, always);
            return nil;
        }
    }

    @NodeChild(value = "file", type = RubyNode.class)
    @NodeChild(value = "wrap", type = RubyNode.class)
    @CoreMethod(names = "load", onSingleton = true, required = 1, optional = 1)
    public abstract static class LoadNode extends CoreMethodNode {

        @CreateCast("wrap")
        protected RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(false, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected boolean load(RubyString file, boolean wrap,
                @Cached IndirectCallNode callNode) {
            final String feature = StringOperations.getString(file);
            final RubySource source;
            try {
                final FileLoader fileLoader = new FileLoader(getContext());
                source = fileLoader.loadFile(getContext().getEnv(), feature);
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().loadErrorCannotLoad(feature, this));
            }

            final DynamicObject wrapModule;
            if (wrap) {
                wrapModule = ModuleNodes
                        .createModule(getContext(), null, coreLibrary().moduleClass, null, null, this);
            } else {
                wrapModule = null;
            }

            final RubyRootNode rootNode = getContext()
                    .getCodeLoader()
                    .parse(source, ParserContext.TOP_LEVEL, null, wrapModule, true, this);

            final DynamicObject mainObject = getContext().getCoreLibrary().mainObject;
            final DeclarationContext declarationContext;
            final Object self;

            if (wrapModule == null) {
                declarationContext = DeclarationContext.topLevel(getContext());
                self = mainObject;
            } else {
                declarationContext = DeclarationContext.topLevel(wrapModule);
                self = CallDispatchHeadNode.getUncached().call(mainObject, "clone");
                CallDispatchHeadNode.getUncached().call(self, "extend", wrapModule);
            }

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    ParserContext.TOP_LEVEL,
                    declarationContext,
                    rootNode,
                    null,
                    self);

            deferredCall.call(callNode);

            return true;
        }

    }

    // Only used internally with a constant literal name, does not trigger hooks
    @Primitive(name = "global_variable_set")
    @ImportStatic(Layouts.class)
    public abstract static class WriteGlobalVariableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "name == cachedName", limit = "1")
        protected Object write(RubySymbol name, Object value,
                @Cached("name") RubySymbol cachedName,
                @Cached("create(cachedName.getString())") WriteSimpleGlobalVariableNode writeNode) {
            return writeNode.execute(value);
        }
    }

    // Only used internally with a constant literal name, does not trigger hooks
    @Primitive(name = "global_variable_get")
    @ImportStatic(Layouts.class)
    public abstract static class ReadGlobalVariableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "name == cachedName", limit = "1")
        protected Object read(RubySymbol name,
                @Cached("name") RubySymbol cachedName,
                @Cached("create(cachedName.getString())") ReadSimpleGlobalVariableNode readNode) {
            return readNode.execute();
        }
    }

    @CoreMethod(names = "define_hooked_variable_with_is_defined", onSingleton = true, required = 4)
    public abstract static class DefineHookedVariableInnerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object defineHookedVariableInnerNode(
                RubySymbol name,
                RubyProc getter,
                RubyProc setter,
                RubyProc isDefined) {
            getContext().getCoreLibrary().globalVariables.define(
                    name.getString(),
                    getter,
                    setter,
                    isDefined);
            return nil;
        }

    }

    @CoreMethod(names = "frame_local_variable_get", onSingleton = true, required = 2)
    public abstract static class GetFrameAndThreadLocalVariable extends CoreMethodArrayArgumentsNode {

        @Child FindThreadAndFrameLocalStorageNode threadLocalNode = FindThreadAndFrameLocalStorageNodeGen.create();

        @Specialization
        protected Object executeGetValue(RubySymbol name, RubyBinding binding,
                @Cached ConditionProfile sameThreadProfile) {
            return threadLocalNode.execute(name, binding.getFrame()).get(sameThreadProfile);
        }

    }

    @CoreMethod(names = "frame_local_variable_set", onSingleton = true, required = 3)
    public abstract static class SetFrameAndThreadLocalVariable extends CoreMethodArrayArgumentsNode {

        @Child FindThreadAndFrameLocalStorageNode threadLocalNode = FindThreadAndFrameLocalStorageNodeGen.create();

        @Specialization
        protected Object executeGetValue(RubySymbol name, RubyBinding binding, Object value,
                @Cached ConditionProfile sameThreadProfile) {
            threadLocalNode.execute(name, binding.getFrame()).set(value, sameThreadProfile);
            return value;
        }

    }

}
