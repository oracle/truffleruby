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
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.kernel.TruffleKernelNodesFactory.GetSpecialVariableStorageNodeGen;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.ReadCallerStorageNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.globals.ReadSimpleGlobalVariableNode;
import org.truffleruby.language.globals.WriteSimpleGlobalVariableNode;
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
        @Specialization
        protected boolean load(RubyString file, boolean wrap,
                @Cached IndirectCallNode callNode) {
            final String feature = file.getJavaString();
            final RubySource source;
            try {
                final FileLoader fileLoader = new FileLoader(getContext());
                source = fileLoader.loadFile(getContext().getEnv(), feature);
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().loadErrorCannotLoad(feature, this));
            }

            final RubyModule wrapModule;
            if (wrap) {
                wrapModule = ModuleNodes
                        .createModule(getContext(), null, coreLibrary().moduleClass, null, null, this);
            } else {
                wrapModule = null;
            }

            final RubyRootNode rootNode = getContext()
                    .getCodeLoader()
                    .parse(source, ParserContext.TOP_LEVEL, null, wrapModule, true, this);

            final RubyBasicObject mainObject = getContext().getCoreLibrary().mainObject;
            final DeclarationContext declarationContext;
            final Object self;

            if (wrapModule == null) {
                declarationContext = DeclarationContext.topLevel(getContext());
                self = mainObject;
            } else {
                declarationContext = DeclarationContext.topLevel(wrapModule);
                self = DispatchNode.getUncached().call(mainObject, "clone");
                DispatchNode.getUncached().call(self, "extend", wrapModule);
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

    @ImportStatic(Layouts.class)
    public abstract static class GetSpecialVariableStorage extends RubyContextNode {

        public abstract SpecialVariableStorage execute(VirtualFrame frame);

        @Specialization(
                guards = "frame.getFrameDescriptor() == descriptor",
                assumptions = "frameAssumption",
                limit = "1")
        protected SpecialVariableStorage getFromKnownFrameDescriptor(VirtualFrame frame,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor descriptor,
                @Cached("declarationDepth(frame)") int declarationFrameDepth,
                @Cached("declarationDescriptor(frame, declarationFrameDepth)") FrameDescriptor declarationFrameDescriptor,
                @Cached("declarationSlot(declarationFrameDescriptor)") FrameSlot declarationFrameSlot,
                @Cached("declarationFrameDescriptor.getVersion()") Assumption frameAssumption) {
            Object storage;
            if (declarationFrameDepth == 0) {
                storage = FrameUtil.getObjectSafe(frame, declarationFrameSlot);
                if (storage == nil) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    storage = new SpecialVariableStorage();
                    frame.setObject(declarationFrameSlot, storage);
                }
            } else {
                MaterializedFrame storageFrame = RubyArguments.getDeclarationFrame(frame, declarationFrameDepth);

                storage = FrameUtil.getObjectSafe(storageFrame, declarationFrameSlot);
                if (storage == nil) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    storage = new SpecialVariableStorage();
                    storageFrame.setObject(declarationFrameSlot, storage);
                }
            }
            return (SpecialVariableStorage) storage;
        }

        @Specialization(replaces = "getFromKnownFrameDescriptor")
        protected SpecialVariableStorage slowPath(VirtualFrame frame) {
            return getSlow(frame.materialize());
        }

        @TruffleBoundary
        public static SpecialVariableStorage getSlow(MaterializedFrame aFrame) {
            MaterializedFrame frame = aFrame;

            while (true) {
                final FrameSlot slot = getVariableSlot(frame);
                if (slot != null) {
                    Object storage = FrameUtil.getObjectSafe(frame, slot);
                    if (storage == Nil.INSTANCE) {
                        storage = new SpecialVariableStorage();
                        frame.setObject(slot, storage);
                    }
                    return (SpecialVariableStorage) storage;
                }

                final MaterializedFrame nextFrame = RubyArguments.getDeclarationFrame(frame);
                if (nextFrame != null) {
                    frame = nextFrame;
                } else {
                    FrameSlot newSlot = frame
                            .getFrameDescriptor()
                            .findOrAddFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE);
                    SpecialVariableStorage storage = new SpecialVariableStorage();
                    frame.setObject(newSlot, storage);
                    return storage;
                }
            }
        }

        protected int declarationDepth(VirtualFrame topFrame) {
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

        protected FrameDescriptor declarationDescriptor(VirtualFrame topFrame, int depth) {
            if (depth == 0) {
                return topFrame.getFrameDescriptor();
            } else {
                return RubyArguments.getDeclarationFrame(topFrame, depth).getFrameDescriptor();
            }
        }

        @TruffleBoundary
        protected FrameSlot declarationSlot(FrameDescriptor descriptor) {
            return descriptor.findOrAddFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE, FrameSlotKind.Object);
        }

        private static FrameSlot getVariableSlot(MaterializedFrame frame) {
            return frame.getFrameDescriptor().findFrameSlot(Layouts.SPECIAL_VARIABLES_STORAGE);
        }

        public static GetSpecialVariableStorage create() {
            return GetSpecialVariableStorageNodeGen.create();
        }
    }

    @Primitive(name = "caller_special_variables")
    public abstract static class GetCallerSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Child ReadCallerStorageNode callerStorageNode = new ReadCallerStorageNode();

        @Specialization
        protected Object storage(VirtualFrame frame) {
            return callerStorageNode.execute(frame);
        }
    }

    @Primitive(name = "proc_special_variables")
    public abstract static class GetProcSpecialVariableStorage extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object storage(VirtualFrame frame, RubyProc proc) {
            return proc.declarationStorage;
        }
    }

    @Primitive(name = "regexp_last_match_set")
    public abstract static class SetRegexpMatch extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage storage, Object lastMatch,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            storage.setLastMatch(lastMatch, getContext(), unsetProfile, sameThreadProfile);
            return lastMatch;
        }
    }

    @Primitive(name = "regexp_last_match_get")
    public abstract static class GetRegexpMatch extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage storage,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            return storage.getLastMatch(unsetProfile, sameThreadProfile);
        }
    }

    @Primitive(name = "io_last_line_set")
    public abstract static class SetLastIO extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object executeSetRegexpMatch(SpecialVariableStorage storage, Object lastIO,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            storage.setLastLine(lastIO, getContext(), unsetProfile, sameThreadProfile);
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
