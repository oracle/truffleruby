/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.io.IOException;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.RaiseException;
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

    @CoreMethod(names = "at_exit", isModuleFunction = true, needsBlock = true, required = 1)
    public abstract static class AtExitSystemNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object atExit(boolean always, DynamicObject block) {
            getContext().getAtExitManager().add(block, always);
            return nil();
        }
    }

    @NodeChild(value = "file", type = RubyNode.class)
    @NodeChild(value = "wrap", type = RubyNode.class)
    @CoreMethod(names = "load", isModuleFunction = true, required = 1, optional = 1)
    public abstract static class LoadNode extends CoreMethodNode {

        @CreateCast("wrap")
        protected RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(false, inherit);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(file)")
        protected boolean load(DynamicObject file, boolean wrap,
                @Cached IndirectCallNode callNode) {
            final String feature = StringOperations.getString(file);
            final RubySource source;
            try {
                final FileLoader fileLoader = new FileLoader(getContext());
                source = fileLoader.loadFile(getContext().getEnv(), feature);
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().loadErrorCannotLoad(feature, this));
            }

            final DynamicObject wrapClass;

            if (wrap) {
                wrapClass = ClassNodes.createInitializedRubyClass(
                        getContext(),
                        null,
                        null,
                        getContext().getCoreLibrary().getObjectClass(),
                        null);
            } else {
                wrapClass = null;
            }

            final RubyRootNode rootNode = getContext()
                    .getCodeLoader()
                    .parse(source, ParserContext.TOP_LEVEL, null, wrapClass, true, this);

            final DeclarationContext declarationContext;
            final DynamicObject mainObject;

            if (wrapClass == null) {
                declarationContext = DeclarationContext.topLevel(getContext());
                mainObject = getContext().getCoreLibrary().getMainObject();
            } else {
                declarationContext = DeclarationContext.topLevel(wrapClass);
                mainObject = Layouts.CLASS.getInstanceFactory(wrapClass).newInstance();
            }

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    ParserContext.TOP_LEVEL,
                    declarationContext,
                    rootNode,
                    null,
                    mainObject);

            deferredCall.call(callNode);

            return true;
        }

    }

    // Only used internally with a constant literal name, does not trigger hooks
    @Primitive(name = "global_variable_set")
    @ImportStatic(Layouts.class)
    public abstract static class WriteGlobalVariableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isRubySymbol(cachedName)", "name == cachedName" }, limit = "1")
        protected Object write(DynamicObject name, Object value,
                @Cached("name") DynamicObject cachedName,
                @Cached("create(SYMBOL.getString(cachedName))") WriteSimpleGlobalVariableNode writeNode) {
            return writeNode.execute(value);
        }
    }

    // Only used internally with a constant literal name, does not trigger hooks
    @Primitive(name = "global_variable_get")
    @ImportStatic(Layouts.class)
    public abstract static class ReadGlobalVariableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isRubySymbol(cachedName)", "name == cachedName" }, limit = "1")
        protected Object read(DynamicObject name,
                @Cached("name") DynamicObject cachedName,
                @Cached("create(SYMBOL.getString(cachedName))") ReadSimpleGlobalVariableNode readNode) {
            return readNode.execute();
        }
    }

    @CoreMethod(names = "define_hooked_variable_with_is_defined", isModuleFunction = true, required = 4)
    public abstract static class DefineHookedVariableInnerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isRubySymbol(name)", "isRubyProc(getter)", "isRubyProc(setter)" })
        protected DynamicObject defineHookedVariableInnerNode(DynamicObject name, DynamicObject getter,
                DynamicObject setter, DynamicObject isDefined) {
            getContext().getCoreLibrary().getGlobalVariables().define(
                    Layouts.SYMBOL.getString(name),
                    getter,
                    setter,
                    isDefined);
            return nil();
        }

    }

    @CoreMethod(names = "frame_local_variable_get", isModuleFunction = true, required = 2)
    public abstract static class GetFrameAndThreadLocalVariable extends CoreMethodArrayArgumentsNode {

        @Child FindThreadAndFrameLocalStorageNode threadLocalNode = FindThreadAndFrameLocalStorageNodeGen.create();

        @Specialization(guards = { "isRubySymbol(name)", "isRubyBinding(binding)" })
        protected Object executeGetValue(DynamicObject name, DynamicObject binding,
                @Cached("createBinaryProfile()") ConditionProfile sameThreadProfile) {
            return threadLocalNode.execute(name, Layouts.BINDING.getFrame(binding)).get(sameThreadProfile);
        }

    }

    @CoreMethod(names = "frame_local_variable_set", isModuleFunction = true, required = 3)
    public abstract static class SetFrameAndThreadLocalVariable extends CoreMethodArrayArgumentsNode {

        @Child FindThreadAndFrameLocalStorageNode threadLocalNode = FindThreadAndFrameLocalStorageNodeGen.create();

        @Specialization(guards = { "isRubySymbol(name)", "isRubyBinding(binding)" })
        protected Object executeGetValue(DynamicObject name, DynamicObject binding, Object value,
                @Cached("createBinaryProfile()") ConditionProfile sameThreadProfile) {
            threadLocalNode.execute(name, Layouts.BINDING.getFrame(binding)).set(value, sameThreadProfile);
            return value;
        }

    }
}
