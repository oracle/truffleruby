/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.io.IOException;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.source.Source;
import org.graalvm.collections.Pair;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.kernel.TruffleKernelNodesFactory.GetSpecialVariableStorageNodeGen;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.FrameOrVariablesReadingNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.ReadOwnFrameAndVariablesNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.MaybeReadCallerVariablesNode;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.globals.ReadSimpleGlobalVariableNode;
import org.truffleruby.language.globals.WriteSimpleGlobalVariableNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FileLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.FrameUtil;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
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
        @Specialization(guards = "strings.isRubyString(file)")
        protected boolean load(Object file, boolean wrap,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached IndirectCallNode callNode) {
            final String feature = strings.getJavaString(file);
            final Pair<Source, Rope> sourceRopePair;
            try {
                final FileLoader fileLoader = new FileLoader(getContext(), getLanguage());
                sourceRopePair = fileLoader.loadFile(feature);
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().loadErrorCannotLoad(feature, this));
            }

            final RubyBasicObject mainObject = getContext().getCoreLibrary().mainObject;

            final RootCallTarget callTarget;
            final DeclarationContext declarationContext;
            final Object self;
            final LexicalScope lexicalScope;
            if (!wrap) {
                lexicalScope = getContext().getRootLexicalScope();
                callTarget = getContext().getCodeLoader().parseTopLevelWithCache(sourceRopePair, this);

                declarationContext = DeclarationContext.topLevel(getContext());
                self = mainObject;
            } else {
                final RubyModule wrapModule = ModuleNodes
                        .createModule(getContext(), null, coreLibrary().moduleClass, null, null, this);
                lexicalScope = new LexicalScope(getContext().getRootLexicalScope(), wrapModule);
                final RubySource rubySource = new RubySource(
                        sourceRopePair.getLeft(),
                        feature,
                        sourceRopePair.getRight());
                callTarget = getContext()
                        .getCodeLoader()
                        .parse(rubySource, ParserContext.TOP_LEVEL, null, lexicalScope, true, this);

                declarationContext = DeclarationContext.topLevel(wrapModule);
                self = DispatchNode.getUncached().call(mainObject, "clone");
                DispatchNode.getUncached().call(self, "extend", wrapModule);
            }

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    callTarget,
                    ParserContext.TOP_LEVEL,
                    declarationContext,
                    null,
                    self,
                    lexicalScope);

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
                RubySymbol name, RubyProc getter, RubyProc setter, RubyProc isDefined) {
            getContext().getCoreLibrary().globalVariables.define(
                    name.getString(),
                    getter,
                    setter,
                    isDefined);
            return nil;
        }

    }

    public static int declarationDepth(Frame topFrame) {
        MaterializedFrame frame = topFrame.materialize();
        int count = 0;

        while (true) {
            final FrameSlot slot = getVariableSlot(frame);
            if (slot != null) {
                return count;
            }

            final MaterializedFrame nextFrame = RubyArguments.getDeclarationFrame(frame);
            if (nextFrame != null) {
                frame = nextFrame;
                count++;
            } else {
                return count;
            }
        }
    }

    public static FrameDescriptor declarationDescriptor(Frame topFrame, int depth) {
        return RubyArguments.getDeclarationFrame(topFrame, depth).getFrameDescriptor();
    }

    @TruffleBoundary
    public static FrameSlot declarationSlot(FrameDescriptor descriptor) {
        return descriptor.findOrAddFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE, FrameSlotKind.Object);
    }

    private static FrameSlot getVariableSlot(MaterializedFrame frame) {
        return frame.getFrameDescriptor().findFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE);
    }

    @ImportStatic({ Layouts.class, TruffleKernelNodes.class })
    public abstract static class GetSpecialVariableStorage extends RubyBaseNode
            implements FrameOrVariablesReadingNode {

        public abstract SpecialVariableStorage execute(Frame frame);

        @Specialization(
                guards = "frame.getFrameDescriptor() == descriptor",
                assumptions = "frameAssumption",
                limit = "1")
        protected SpecialVariableStorage getFromKnownFrameDescriptor(Frame frame,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor descriptor,
                @Cached("declarationDepth(frame)") int declarationFrameDepth,
                @Cached("declarationDescriptor(frame, declarationFrameDepth)") FrameDescriptor declarationFrameDescriptor,
                @Cached("declarationSlot(declarationFrameDescriptor)") FrameSlot declarationFrameSlot,
                @Cached("declarationFrameDescriptor.getVersion()") Assumption frameAssumption) {
            Object variables;
            if (declarationFrameDepth == 0) {
                variables = FrameUtil.getObjectSafe(frame, declarationFrameSlot);
                if (variables == nil) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    variables = new SpecialVariableStorage();
                    frame.setObject(declarationFrameSlot, variables);
                }
            } else {
                Frame storageFrame = RubyArguments.getDeclarationFrame(frame, declarationFrameDepth);

                if (storageFrame == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    int depth = 0;
                    MaterializedFrame currentFrame = RubyArguments.getDeclarationFrame(frame);
                    while (currentFrame != null) {
                        depth += 1;
                        currentFrame = RubyArguments.getDeclarationFrame(currentFrame);
                    }

                    String message = String.format(
                            "Expected %d declaration frames but only found %d frames.",
                            declarationFrameDepth,
                            depth);
                    throw CompilerDirectives.shouldNotReachHere(message);
                }

                variables = FrameUtil.getObjectSafe(storageFrame, declarationFrameSlot);
                if (variables == nil) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    variables = new SpecialVariableStorage();
                    storageFrame.setObject(declarationFrameSlot, variables);
                }
            }
            return (SpecialVariableStorage) variables;
        }

        @Specialization(replaces = "getFromKnownFrameDescriptor")
        protected SpecialVariableStorage slowPath(Frame frame) {
            return getSlow(frame.materialize());
        }

        @TruffleBoundary
        public static SpecialVariableStorage getSlow(MaterializedFrame aFrame) {
            MaterializedFrame frame = aFrame;

            while (true) {
                final FrameSlot slot = getVariableSlot(frame);
                if (slot != null) {
                    Object variables = FrameUtil.getObjectSafe(frame, slot);
                    if (variables == Nil.INSTANCE) {
                        variables = new SpecialVariableStorage();
                        frame.setObject(slot, variables);
                    }
                    return (SpecialVariableStorage) variables;
                }

                final MaterializedFrame nextFrame = RubyArguments.getDeclarationFrame(frame);
                if (nextFrame != null) {
                    frame = nextFrame;
                } else {
                    FrameSlot newSlot = frame
                            .getFrameDescriptor()
                            .findOrAddFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE);
                    SpecialVariableStorage variables = new SpecialVariableStorage();
                    frame.setObject(newSlot, variables);
                    return variables;
                }
            }
        }

        public static GetSpecialVariableStorage create() {
            return GetSpecialVariableStorageNodeGen.create();
        }

        @Override
        public void startSending(boolean variables, boolean frame) {
            if (frame) {
                replace(new ReadOwnFrameAndVariablesNode(), "Starting to read own frame and variables");
            }
        }

        @Override
        public boolean sendingFrame() {
            return false;
        }
    }

    @Primitive(name = "caller_special_variables")
    public abstract static class GetCallerSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Child ReadCallerVariablesNode callerVariablesNode = new ReadCallerVariablesNode();

        @Specialization
        protected Object storage(VirtualFrame frame) {
            return callerVariablesNode.execute(frame);
        }
    }

    @Primitive(name = "caller_special_variables_if_fast")
    public abstract static class GetCallerSpecialVariableStorageIfFast extends PrimitiveArrayArgumentsNode {

        @Child MaybeReadCallerVariablesNode callerVariablesNode = new MaybeReadCallerVariablesNode();

        @Specialization
        protected Object storage(VirtualFrame frame,
                @Cached ConditionProfile nullProfile) {
            Object variables = callerVariablesNode.execute(frame);
            if (nullProfile.profile(variables == null)) {
                return nil;
            } else {
                return variables;
            }
        }
    }

    @Primitive(name = "proc_special_variables")
    public abstract static class GetProcSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object variables(RubyProc proc) {
            return proc.declarationVariables;
        }
    }

    @Primitive(name = "share_special_variables")
    @ImportStatic(TruffleKernelNodes.class)
    public abstract static class ShareSpecialVariableStoage extends PrimitiveArrayArgumentsNode {

        @Specialization(
                guards = "frame.getFrameDescriptor() == descriptor",
                assumptions = "frameAssumption",
                limit = "1")
        protected Object shareSpecialVariable(VirtualFrame frame, SpecialVariableStorage storage,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor descriptor,
                @Cached("declarationDepth(frame)") int declarationFrameDepth,
                @Cached("declarationDescriptor(frame, declarationFrameDepth)") FrameDescriptor declarationFrameDescriptor,
                @Cached("declarationSlot(declarationFrameDescriptor)") FrameSlot declarationFrameSlot,
                @Cached("declarationFrameDescriptor.getVersion()") Assumption frameAssumption) {
            final Frame storageFrame = RubyArguments.getDeclarationFrame(frame, declarationFrameDepth);
            storageFrame.setObject(declarationFrameSlot, storage);
            return nil;
        }

        @Specialization(replaces = "shareSpecialVariable")
        protected Object slowPath(VirtualFrame frame, SpecialVariableStorage storage) {
            return shareSlow(frame.materialize(), storage);
        }

        @TruffleBoundary
        public Object shareSlow(MaterializedFrame aFrame, SpecialVariableStorage storage) {
            MaterializedFrame frame = aFrame;

            while (true) {
                final FrameSlot slot = getVariableSlot(frame);
                if (slot != null) {
                    frame.setObject(slot, storage);
                    return nil;
                }

                final MaterializedFrame nextFrame = RubyArguments.getDeclarationFrame(frame);
                if (nextFrame != null) {
                    frame = nextFrame;
                } else {
                    FrameSlot newSlot = frame
                            .getFrameDescriptor()
                            .findOrAddFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE);
                    frame.setObject(newSlot, storage);
                    return nil;
                }
            }
        }

        public static GetSpecialVariableStorage create() {
            return GetSpecialVariableStorageNodeGen.create();
        }
    }

    @Primitive(name = "regexp_last_match_set")
    public abstract static class SetRegexpMatch extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage variables, Object lastMatch,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            variables.setLastMatch(lastMatch, getContext(), unsetProfile, sameThreadProfile);
            return lastMatch;
        }
    }

    @Primitive(name = "regexp_last_match_get")
    public abstract static class GetRegexpMatch extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage variables,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            return variables.getLastMatch(unsetProfile, sameThreadProfile);
        }
    }

    @Primitive(name = "io_last_line_set")
    public abstract static class SetLastIO extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage variables, Object lastIO,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            variables.setLastLine(lastIO, getContext(), unsetProfile, sameThreadProfile);
            return lastIO;
        }
    }

    @Primitive(name = "io_last_line_get")
    public abstract static class GetLastIO extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage storage,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            return storage.getLastLine(unsetProfile, sameThreadProfile);
        }
    }
}
