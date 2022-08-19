/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.ByteIndexOfStringNode;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.ReRaiseInlinedExceptionNode;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BooleanCastWithDefaultNode;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToPathNodeGen;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStringOrSymbolNodeGen;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.constant.WarnAlreadyInitializedNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.MethodFilter;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.method.RubyUnboundMethod;
import org.truffleruby.core.module.ModuleNodesFactory.ConstSetNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.ConstSetUncheckedNodeGen;
import org.truffleruby.core.module.ModuleNodesFactory.GeneratedReaderNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.GeneratedWriterNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.IsSubclassOfOrEqualToNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.SetMethodVisibilityNodeGen;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.string.TStringConstants;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyLambdaRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.WarningNode.UncachedWarningNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.constants.ConstantEntry;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.loader.EvalLoader;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.CanBindMethodToModuleNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.DeclarationContext.FixedDefaultDefinee;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.methods.UsingNode;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.classvariables.CheckClassVariableNameNode;
import org.truffleruby.language.objects.classvariables.ClassVariableStorage;
import org.truffleruby.language.objects.classvariables.LookupClassVariableNode;
import org.truffleruby.language.objects.classvariables.SetClassVariableNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.Identifiers;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

import static org.truffleruby.core.module.ModuleNodes.GenerateAccessorNode.Accessor.BOTH;
import static org.truffleruby.core.module.ModuleNodes.GenerateAccessorNode.Accessor.READER;
import static org.truffleruby.core.module.ModuleNodes.GenerateAccessorNode.Accessor.WRITER;

@CoreModule(value = "Module", isClass = true)
public abstract class ModuleNodes {

    @TruffleBoundary
    public static RubyModule createModule(RubyContext context, SourceSection sourceSection, RubyClass selfClass,
            RubyModule lexicalParent, String name, Node currentNode) {
        final RubyModule module = new RubyModule(
                selfClass,
                context.getLanguageSlow().moduleShape,
                context.getLanguageSlow(),
                sourceSection,
                lexicalParent,
                name);
        if (lexicalParent != null) {
            module.fields.getAdoptedByLexicalParent(context, lexicalParent, name, currentNode);
        } else if (name != null) { // bootstrap module
            module.fields.setFullName(name);
        }
        return module;
    }

    @CoreMethod(names = "===", required = 1)
    public abstract static class ContainsInstanceNode extends CoreMethodArrayArgumentsNode {

        @Child private IsANode isANode = IsANode.create();

        @Specialization
        protected boolean containsInstance(RubyModule module, Object instance) {
            return isANode.executeIsA(instance, module);
        }

    }

    @CoreMethod(names = "<", required = 1)
    public abstract static class IsSubclassOfNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSubclassOf(VirtualFrame frame, RubyModule self, Object other);

        @TruffleBoundary
        @Specialization
        protected Object isSubclassOf(RubyModule self, RubyModule other) {
            if (self == other) {
                return false;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return true;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return false;
            }

            return nil;
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object isSubclassOfOther(RubyModule self, Object other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = "<=", required = 1)
    public abstract static class IsSubclassOfOrEqualToNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSubclassOfOrEqualTo(RubyModule self, Object other);

        @TruffleBoundary
        @Specialization
        protected Object isSubclassOfOrEqualTo(RubyModule self, RubyModule other) {
            if (self == other || ModuleOperations.includesModule(self, other)) {
                return true;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return false;
            }

            return nil;
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object isSubclassOfOrEqualToOther(RubyModule self, Object other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = ">", required = 1)
    public abstract static class IsSuperclassOfNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSuperclassOf(VirtualFrame frame, RubyModule self, Object other);

        @TruffleBoundary
        @Specialization
        protected Object isSuperclassOf(RubyModule self, RubyModule other) {
            if (self == other) {
                return false;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return true;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return false;
            }

            return nil;
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object isSuperclassOfOther(RubyModule self, Object other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = ">=", required = 1)
    public abstract static class IsSuperclassOfOrEqualToNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSuperclassOfOrEqualTo(VirtualFrame frame, RubyModule self, Object other);

        @TruffleBoundary
        @Specialization
        protected Object isSuperclassOfOrEqualTo(RubyModule self, RubyModule other) {
            if (self == other || ModuleOperations.includesModule(other, self)) {
                return true;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return false;
            }

            return nil;
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object isSuperclassOfOrEqualToOther(RubyModule self, Object other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Child private IsSubclassOfOrEqualToNode subclassNode;

        private Object isSubclass(RubyModule self, RubyModule other) {
            if (subclassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                subclassNode = insert(IsSubclassOfOrEqualToNodeFactory.create(null));
            }
            return subclassNode.executeIsSubclassOfOrEqualTo(self, other);
        }

        @Specialization
        protected Object compare(RubyModule self, RubyModule other) {
            if (self == other) {
                return 0;
            }

            final Object isSubclass = isSubclass(self, other);

            if (isSubclass == nil) {
                return nil;
            } else {
                return (boolean) isSubclass ? -1 : 1;
            }
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object compareOther(RubyModule self, Object other) {
            return nil;
        }

    }

    @CoreMethod(names = "alias_method", required = 2, raiseIfFrozenSelf = true, split = Split.NEVER)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "newName", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "oldName", type = RubyBaseNodeWithExecute.class)
    public abstract static class AliasMethodNode extends CoreMethodNode {

        @CreateCast("newName")
        protected RubyBaseNodeWithExecute coerceNewNameToSymbol(RubyBaseNodeWithExecute newName) {
            return ToSymbolNode.create(newName);
        }

        @CreateCast("oldName")
        protected RubyBaseNodeWithExecute coerceOldNameToSymbol(RubyBaseNodeWithExecute oldName) {
            return ToSymbolNode.create(oldName);
        }

        @Specialization
        protected RubySymbol aliasMethod(RubyModule module, RubySymbol newName, RubySymbol oldName,
                @Cached BranchProfile errorProfile) {
            final InternalMethod method = module.fields
                    .deepMethodSearch(getContext(), oldName.getString());

            if (method == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().nameErrorUndefinedMethod(
                        oldName.getString(),
                        module,
                        this));
            }

            final InternalMethod aliasMethod = method.withName(newName.getString());
            module.addMethodConsiderNameVisibility(getContext(), aliasMethod, aliasMethod.getVisibility(), this);
            return newName;
        }

    }

    @CoreMethod(names = "ancestors")
    public abstract static class AncestorsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray ancestors(RubyModule self) {
            final List<RubyModule> ancestors = new ArrayList<>();
            for (RubyModule module : self.fields.ancestors()) {
                ancestors.add(module);
            }

            return createArray(ancestors.toArray());
        }
    }

    @CoreMethod(names = "append_features", required = 1, visibility = Visibility.PRIVATE, split = Split.NEVER)
    public abstract static class AppendFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object appendFeatures(RubyModule features, RubyModule target,
                @Cached BranchProfile errorProfile) {
            if (features instanceof RubyClass) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("append_features must be called only on modules", this));
            }
            target.fields.include(getContext(), this, features);
            return nil;
        }
    }

    @GenerateUncached
    public abstract static class GeneratedReaderNode extends AlwaysInlinedMethodNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object reader(Frame callerFrame, RubyDynamicObject self, Object[] rubyArgs, RootCallTarget target,
                @CachedLibrary("self") DynamicObjectLibrary objectLibrary) {
            // Or a subclass of RubyRootNode with an extra field?
            final String ivarName = RubyRootNode.of(target).getSharedMethodInfo().getNotes();
            CompilerAsserts.partialEvaluationConstant(ivarName);

            return objectLibrary.getOrDefault(self, ivarName, nil);
        }

        @Specialization(guards = "!isRubyDynamicObject(self)")
        protected Object notObject(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target) {
            return nil;
        }
    }

    @GenerateUncached
    public abstract static class GeneratedWriterNode extends AlwaysInlinedMethodNode {

        @Specialization(guards = "!rubyLibrary.isFrozen(self)")
        protected Object writer(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibrary,
                @Cached WriteObjectFieldNode writeObjectFieldNode) {
            final String ivarName = RubyRootNode.of(target).getSharedMethodInfo().getNotes();
            CompilerAsserts.partialEvaluationConstant(ivarName);

            final Object value = RubyArguments.getArgument(rubyArgs, 0);
            writeObjectFieldNode.execute((RubyDynamicObject) self, ivarName, value);
            return value;
        }

        @Specialization(guards = "rubyLibrary.isFrozen(self)")
        protected Object frozen(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibrary) {
            throw new RaiseException(getContext(), coreExceptions().frozenError(self, this));
        }
    }

    public abstract static class GenerateAccessorNode extends AlwaysInlinedMethodNode {
        enum Accessor {
            READER,
            WRITER,
            BOTH
        }

        protected Object[] generateAccessors(Frame callerFrame, RubyModule module, Object[] names,
                Accessor accessor, RootCallTarget target) {
            needCallerFrame(callerFrame, target);
            final Visibility visibility = DeclarationContext
                    .findVisibilityCheckSelfAndDefaultDefinee(module, callerFrame);
            return createAccessors(module, names, accessor, visibility);
        }

        @TruffleBoundary
        private Object[] createAccessors(RubyModule module, Object[] names, Accessor accessor,
                Visibility visibility) {
            final Node currentNode = getNode();
            final SourceSection sourceSection;
            if (currentNode != null) {
                sourceSection = currentNode.getEncapsulatingSourceSection();
            } else {
                sourceSection = CoreLibrary.UNAVAILABLE_SOURCE_SECTION;
            }

            Object[] generatedMethods = accessor == BOTH ? new Object[names.length * 2] : new Object[names.length];
            int i = 0;
            for (Object nameObject : names) {
                final String name = NameToJavaStringNode.getUncached().execute(nameObject);
                if (accessor == BOTH) {
                    generatedMethods[i++] = createAccessor(module, name, READER, visibility, sourceSection);
                    generatedMethods[i++] = createAccessor(module, name, WRITER, visibility, sourceSection);
                } else {
                    generatedMethods[i++] = createAccessor(module, name, accessor, visibility, sourceSection);
                }
            }
            return generatedMethods;
        }

        @TruffleBoundary
        private RubySymbol createAccessor(RubyModule module, String name, Accessor accessor, Visibility visibility,
                SourceSection sourceSection) {
            assert accessor != BOTH;
            final Arity arity = accessor == READER ? Arity.NO_ARGUMENTS : Arity.ONE_REQUIRED;
            final String ivar = "@" + name;
            final String accessorName = accessor == READER ? name : name + "=";

            final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                    sourceSection,
                    LexicalScope.IGNORE,
                    arity,
                    accessorName,
                    0,
                    SharedMethodInfo.moduleAndMethodName(module, accessorName),
                    ivar, // notes
                    null);

            final NodeFactory<? extends RubyBaseNode> alwaysInlinedNodeFactory = accessor == READER
                    ? GeneratedReaderNodeFactory.getInstance()
                    : GeneratedWriterNodeFactory.getInstance();

            final RubyRootNode reRaiseRootNode = new RubyRootNode(
                    getLanguage(),
                    sourceSection,
                    null,
                    sharedMethodInfo,
                    new ReRaiseInlinedExceptionNode(alwaysInlinedNodeFactory),
                    Split.NEVER,
                    ReturnID.INVALID);
            final RootCallTarget callTarget = reRaiseRootNode.getCallTarget();

            final InternalMethod method = new InternalMethod(
                    getContext(),
                    sharedMethodInfo,
                    LexicalScope.IGNORE,
                    DeclarationContext.NONE,
                    accessorName,
                    module,
                    visibility,
                    false,
                    alwaysInlinedNodeFactory,
                    null,
                    callTarget,
                    null);

            module.fields.addMethod(getContext(), this, method);
            return getLanguage().getSymbol(method.getName());
        }
    }

    @GenerateUncached
    @CoreMethod(names = "attr", rest = true, alwaysInlined = true)
    public abstract static class AttrNode extends GenerateAccessorNode {
        @Specialization
        protected Object attr(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target) {
            final boolean setter;
            Object[] names = RubyArguments.getPositionalArguments(rubyArgs, false);
            if (names.length == 2 && names[1] instanceof Boolean) {
                warnObsoletedBooleanArgument();
                setter = (boolean) names[1];
                names = new Object[]{ names[0] };
            } else {
                setter = false;
            }

            return createArray(generateAccessors(callerFrame, module, names, setter ? BOTH : READER, target));
        }

        @TruffleBoundary
        private void warnObsoletedBooleanArgument() {
            final UncachedWarningNode warningNode = UncachedWarningNode.INSTANCE;
            if (warningNode.shouldWarn()) {
                final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
                warningNode.warningMessage(sourceSection, "optional boolean argument is obsoleted");
            }
        }
    }

    @GenerateUncached
    @CoreMethod(names = "attr_accessor", rest = true, alwaysInlined = true)
    public abstract static class AttrAccessorNode extends GenerateAccessorNode {
        @Specialization
        protected Object attrAccessor(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target) {
            Object[] names = RubyArguments.getPositionalArguments(rubyArgs, false);
            return createArray(generateAccessors(callerFrame, module, names, BOTH, target));
        }
    }

    @GenerateUncached
    @CoreMethod(names = "attr_reader", rest = true, alwaysInlined = true)
    public abstract static class AttrReaderNode extends GenerateAccessorNode {
        @Specialization
        protected Object attrReader(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target) {
            Object[] names = RubyArguments.getPositionalArguments(rubyArgs, false);
            return createArray(generateAccessors(callerFrame, module, names, READER, target));
        }
    }

    @GenerateUncached
    @CoreMethod(names = "attr_writer", rest = true, alwaysInlined = true)
    public abstract static class AttrWriterNode extends GenerateAccessorNode {
        @Specialization
        protected Object attrWriter(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target) {
            Object[] names = RubyArguments.getPositionalArguments(rubyArgs, false);
            return createArray(generateAccessors(callerFrame, module, names, WRITER, target));
        }
    }

    @CoreMethod(names = "autoload", required = 2)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "filename", type = RubyBaseNodeWithExecute.class)
    public abstract static class AutoloadNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceNameToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("filename")
        protected RubyBaseNodeWithExecute coerceFilenameToPath(RubyBaseNodeWithExecute filename) {
            return ToPathNodeGen.create(filename);
        }

        @TruffleBoundary
        @Specialization(guards = "libFilename.isRubyString(filename)", limit = "1")
        protected Object autoload(RubyModule module, String name, Object filename,
                @Cached RubyStringLibrary libFilename) {
            if (!Identifiers.isValidConstantName(name)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameError(
                                StringUtils.format("autoload must be constant name: %s", name),
                                module,
                                name,
                                this));
            }

            if (libFilename.getTString(filename).isEmpty()) {
                throw new RaiseException(getContext(), coreExceptions().argumentError("empty file name", this));
            }

            final String javaStringFilename = RubyGuards.getJavaString(filename);
            module.fields.setAutoloadConstant(getContext(), this, name, filename, javaStringFilename);
            return nil;
        }
    }

    @CoreMethod(names = "autoload?", required = 1, optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    public abstract static class IsAutoloadNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        @Specialization
        @TruffleBoundary
        protected Object isAutoload(RubyModule module, String name, boolean inherit) {
            final ConstantLookupResult constant = ModuleOperations.lookupConstantWithInherit(
                    getContext(),
                    module,
                    name,
                    inherit,
                    this,
                    false,
                    false);

            if (constant.isAutoload() && !constant.getConstant().getAutoloadConstant().isAutoloadingThread()) {
                return constant.getConstant().getAutoloadConstant().getFeature();
            } else {
                return nil;
            }
        }
    }

    @GenerateUncached
    @CoreMethod(names = { "class_eval", "module_eval" }, optional = 3, needsBlock = true, alwaysInlined = true)
    public abstract static class ClassEvalNode extends AlwaysInlinedMethodNode {

        @Specialization(guards = "isBlockProvided(rubyArgs)")
        protected Object evalWithBlock(Frame callerFrame, RubyModule self, Object[] rubyArgs, RootCallTarget target,
                @Cached BranchProfile errorProfile,
                @Cached ClassExecBlockNode classExecNode) {
            final int count = RubyArguments.getPositionalArgumentsCount(rubyArgs, false);

            if (count > 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(count, 0, this));
            }

            final Object block = RubyArguments.getBlock(rubyArgs);
            return classExecNode.execute(EmptyArgumentsDescriptor.INSTANCE, self, new Object[]{ self },
                    (RubyProc) block);
        }

        @Specialization(guards = "!isBlockProvided(rubyArgs)")
        protected Object evalWithString(Frame callerFrame, RubyModule self, Object[] rubyArgs, RootCallTarget target,
                @Cached BranchProfile errorProfile,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached ToStrNode toStrNode,
                @Cached ToIntNode toIntNode,
                @Cached IndirectCallNode callNode) {
            final Object sourceCode;
            String fileName = coreStrings().EVAL_FILENAME_STRING.toString();
            int line = 1;

            int count = RubyArguments.getPositionalArgumentsCount(rubyArgs, false);

            if (count == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(0, 1, 2, this));
            }

            sourceCode = toStrNode.execute(RubyArguments.getArgument(rubyArgs, 0));

            if (count >= 2) {
                fileName = toJavaStringNode
                        .executeToJavaString(toStrNode.execute(RubyArguments.getArgument(rubyArgs, 1)));
            }

            if (count >= 3) {
                line = toIntNode.execute(RubyArguments.getArgument(rubyArgs, 2));
            }

            needCallerFrame(callerFrame, target);
            return classEvalSource(
                    callerFrame.materialize(),
                    self,
                    sourceCode,
                    fileName,
                    line,
                    callNode);
        }

        @TruffleBoundary
        private Object classEvalSource(MaterializedFrame callerFrame, RubyModule module, Object sourceCode, String file,
                int line,
                IndirectCallNode callNode) {
            final RubySource source = EvalLoader.createEvalSource(
                    getContext(),
                    RubyStringLibrary.getUncached().getTString(sourceCode),
                    RubyStringLibrary.getUncached().getEncoding(sourceCode),
                    "class/module_eval",
                    file,
                    line,
                    this);

            final LexicalScope lexicalScope = new LexicalScope(
                    RubyArguments.getMethod(callerFrame).getLexicalScope(),
                    module);

            final RootCallTarget callTarget = getContext().getCodeLoader().parse(
                    source,
                    ParserContext.MODULE,
                    callerFrame,
                    lexicalScope,
                    this);

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    callTarget,
                    ParserContext.MODULE,
                    new DeclarationContext(
                            Visibility.PUBLIC,
                            new FixedDefaultDefinee(module),
                            DeclarationContext.NO_REFINEMENTS),
                    callerFrame,
                    module,
                    lexicalScope);

            return deferredCall.call(callNode);
        }

    }

    @CoreMethod(names = { "class_exec", "module_exec" }, rest = true, needsBlock = true)
    public abstract static class ClassExecNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object withBlock(VirtualFrame frame, RubyModule self, Object[] args, RubyProc block,
                @Cached ClassExecBlockNode classExecBlockNode) {
            return classExecBlockNode.execute(RubyArguments.getDescriptor(frame), self, args, block);
        }

        @Specialization
        protected Object noBlock(RubyModule self, Object[] args, Nil block) {
            throw new RaiseException(getContext(), coreExceptions().noBlockGiven(this));
        }
    }

    @GenerateUncached
    public abstract static class ClassExecBlockNode extends RubyBaseNode {

        public abstract Object execute(ArgumentsDescriptor descriptor, RubyModule self, Object[] args, RubyProc block);

        @Specialization
        protected Object classExec(ArgumentsDescriptor descriptor, RubyModule self, Object[] args, RubyProc block,
                @Cached CallBlockNode callBlockNode) {
            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new FixedDefaultDefinee(self),
                    block.declarationContext.getRefinements());

            return callBlockNode.executeCallBlock(declarationContext, block, self, nil, descriptor, args, null);
        }
    }

    @CoreMethod(names = "class_variable_defined?", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class ClassVariableDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected boolean isClassVariableDefinedString(RubyModule module, String name,
                @Cached CheckClassVariableNameNode checkClassVariableNameNode,
                @Cached LookupClassVariableNode lookupClassVariableNode) {
            checkClassVariableNameNode.execute(module, name);
            return lookupClassVariableNode.execute(module, name) != null;
        }

    }

    @CoreMethod(names = "class_variable_get", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class ClassVariableGetNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected Object getClassVariable(RubyModule module, String name,
                @Cached CheckClassVariableNameNode checkClassVariableNameNode,
                @Cached LookupClassVariableNode lookupClassVariableNode,
                @Cached ConditionProfile undefinedProfile) {
            checkClassVariableNameNode.execute(module, name);
            final Object value = lookupClassVariableNode.execute(module, name);

            if (undefinedProfile.profile(value == null)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUninitializedClassVariable(module, name, this));
            } else {
                return value;
            }
        }

    }

    @CoreMethod(names = "class_variable_set", required = 2, raiseIfFrozenSelf = true)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class ClassVariableSetNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected Object setClassVariable(RubyModule module, String name, Object value,
                @Cached CheckClassVariableNameNode checkClassVariableNameNode,
                @Cached SetClassVariableNode setClassVariableNode) {
            checkClassVariableNameNode.execute(module, name);
            setClassVariableNode.execute(module, name, value);
            return value;
        }

    }

    @CoreMethod(names = "class_variables", optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    public abstract static class ClassVariablesNode extends CoreMethodNode {

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray getClassVariables(RubyModule module, boolean inherit) {
            final Set<Object> variables = new LinkedHashSet<>();

            ModuleOperations.classVariableLookup(module, inherit, m -> {
                final ClassVariableStorage classVariableStorage = m.fields.getClassVariables();
                for (Object key : classVariableStorage.getShape().getKeys()) {
                    variables.add(getSymbol((String) key));
                }
                return null;
            });

            return createArray(variables.toArray());
        }
    }

    @CoreMethod(names = "constants", optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    public abstract static class ConstantsNode extends CoreMethodNode {

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray constants(RubyModule module, boolean inherit) {
            final List<RubySymbol> constantsArray = new ArrayList<>();

            final Iterable<Entry<String, ConstantEntry>> constants;
            if (inherit) {
                constants = ModuleOperations.getAllConstants(module);
            } else {
                constants = module.fields.getConstants();
            }

            for (Entry<String, ConstantEntry> entry : constants) {
                final RubyConstant constant = entry.getValue().getConstant();
                if (constant != null && !constant.isPrivate() && Identifiers.isValidConstantName(constant.getName())) {
                    constantsArray.add(getSymbol(entry.getKey()));
                }
            }

            return createArray(constantsArray.toArray());
        }

    }

    @Primitive(name = "module_const_defined?")
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "check_name", type = RubyNode.class)
    public abstract static class ConstDefinedNode extends PrimitiveNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected boolean isConstDefined(RubyModule module, String fullName, boolean inherit, boolean checkName) {
            final ConstantLookupResult constant = ModuleOperations
                    .lookupScopedConstant(getContext(), module, fullName, inherit, this, checkName);
            return constant.isFound();
        }

    }

    @Primitive(name = "module_const_get")
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "inherit", type = RubyNode.class)
    @NodeChild(value = "look_in_object", type = RubyNode.class)
    @NodeChild(value = "check_name", type = RubyNode.class)
    public abstract static class ConstGetNode extends PrimitiveNode {

        @Child private LookupConstantNode lookupConstantLookInObjectNode = LookupConstantNode.create(true, true);
        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, false);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();
        @Child private ByteIndexOfStringNode byteIndexOfStringNode;

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToSymbolOrString(RubyBaseNodeWithExecute name) {
            // We want to know if the name is a Symbol, as then scoped lookup is not tried
            return ToStringOrSymbolNodeGen.create(name);
        }

        // Symbol

        @Specialization(guards = "inherit")
        protected Object getConstant(
                RubyModule module, RubySymbol name, boolean inherit, boolean lookInObject, boolean checkName) {
            return getConstant(module, name.getString(), checkName, lookInObject);
        }

        @Specialization(guards = "!inherit")
        protected Object getConstantNoInherit(
                RubyModule module, RubySymbol name, boolean inherit, boolean lookInObject, boolean checkName) {
            return getConstantNoInherit(module, name.getString(), checkName);
        }

        // String

        @Specialization(
                guards = {
                        "stringsName.isRubyString(name)",
                        "inherit",
                        "equalNode.execute(stringsName, name, cachedTString, cachedEncoding)",
                        "!scoped",
                        "checkName == cachedCheckName" },
                limit = "getLimit()")
        protected Object getConstantStringCached(
                RubyModule module, Object name, boolean inherit, boolean lookInObject, boolean checkName,
                @Cached RubyStringLibrary stringsName,
                @Cached("asTruffleStringUncached(name)") TruffleString cachedTString,
                @Cached("stringsName.getEncoding(name)") RubyEncoding cachedEncoding,
                @Cached("getJavaString(name)") String cachedString,
                @Cached("checkName") boolean cachedCheckName,
                @Cached StringHelperNodes.EqualNode equalNode,
                @Cached("isScoped(cachedString)") boolean scoped) {
            return getConstant(module, cachedString, checkName, lookInObject);
        }

        @Specialization(
                guards = { "stringsName.isRubyString(name)", "inherit", "!isScoped(stringsName, name)" },
                replaces = "getConstantStringCached", limit = "1")
        protected Object getConstantString(
                RubyModule module, Object name, boolean inherit, boolean lookInObject, boolean checkName,
                @Cached RubyStringLibrary stringsName,
                @Cached ToJavaStringNode toJavaStringNode) {
            return getConstant(module, toJavaStringNode.executeToJavaString(name), checkName, lookInObject);
        }

        @Specialization(
                guards = { "stringsName.isRubyString(name)", "!inherit", "!isScoped(stringsName, name)" }, limit = "1")
        protected Object getConstantNoInheritString(
                RubyModule module, Object name, boolean inherit, boolean lookInObject, boolean checkName,
                @Cached RubyStringLibrary stringsName,
                @Cached ToJavaStringNode toJavaStringNode) {
            return getConstantNoInherit(module, toJavaStringNode.executeToJavaString(name), checkName);
        }

        // Scoped String
        @Specialization(guards = { "stringsName.isRubyString(name)", "isScoped(stringsName, name)" }, limit = "1")
        protected Object getConstantScoped(
                RubyModule module, Object name, boolean inherit, boolean lookInObject, boolean checkName,
                @Cached RubyStringLibrary stringsName) {
            return FAILURE;
        }

        private Object getConstant(RubyModule module, String name, boolean checkName, boolean lookInObject) {
            CompilerAsserts.partialEvaluationConstant(lookInObject);
            if (lookInObject) {
                return getConstantNode
                        .lookupAndResolveConstant(LexicalScope.IGNORE, module, name, checkName,
                                lookupConstantLookInObjectNode);
            } else {
                return getConstantNode
                        .lookupAndResolveConstant(LexicalScope.IGNORE, module, name, checkName,
                                lookupConstantNode);
            }
        }

        private Object getConstantNoInherit(RubyModule module, String name, boolean checkName) {
            final LookupConstantInterface lookup = this::lookupConstantNoInherit;
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, checkName, lookup);
        }

        @TruffleBoundary
        private RubyConstant lookupConstantNoInherit(LexicalScope lexicalScope, RubyModule module, String name,
                boolean checkName) {
            return ModuleOperations
                    .lookupConstantWithInherit(getContext(), module, name, false, this, checkName)
                    .getConstant();
        }

        boolean isScoped(RubyStringLibrary libString, Object string) {
            if (byteIndexOfStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                byteIndexOfStringNode = insert(ByteIndexOfStringNode.create());
            }

            var tstring = libString.getTString(string);
            var encoding = libString.getTEncoding(string);
            int byteLength = tstring.byteLength(encoding);
            return byteIndexOfStringNode.execute(tstring, TStringConstants.COLON_COLON, 0, byteLength, encoding) >= 0;
        }

        @TruffleBoundary
        boolean isScoped(String name) {
            return name.contains("::");
        }

        protected int getLimit() {
            return getLanguage().options.CONSTANT_CACHE;
        }

    }

    @CoreMethod(names = "const_missing", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class ConstMissingNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected Object constMissing(RubyModule module, String name) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorUninitializedConstant(module, name, this));
        }

    }

    @CoreMethod(names = "const_source_location", required = 1, optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    public abstract static class ConstSourceLocationNode extends CoreMethodNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToStringOrSymbol(RubyBaseNodeWithExecute name) {
            return ToStringOrSymbolNodeGen.create(name);
        }

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        @Specialization(guards = "strings.isRubyString(name)", limit = "1")
        @TruffleBoundary
        protected Object constSourceLocation(RubyModule module, Object name, boolean inherit,
                @Cached RubyStringLibrary strings) {
            final ConstantLookupResult lookupResult = ModuleOperations
                    .lookupScopedConstant(getContext(), module, RubyGuards.getJavaString(name), inherit, this, true);

            return getLocation(lookupResult);
        }

        @Specialization
        @TruffleBoundary
        protected Object constSourceLocation(RubyModule module, RubySymbol name, boolean inherit) {
            final ConstantLookupResult lookupResult = ModuleOperations
                    .lookupConstantWithInherit(getContext(), module, name.getString(), inherit, this, true);

            return getLocation(lookupResult);
        }

        private Object getLocation(ConstantLookupResult lookupResult) {
            if (!lookupResult.isFound()) {
                return nil;
            }

            final SourceSection sourceSection = lookupResult.getConstant().getSourceSection();
            if (!BacktraceFormatter.isAvailable(sourceSection)) {
                return createEmptyArray();
            } else {
                final RubyString file = createString(
                        fromJavaStringNode,
                        getLanguage().getSourcePath(sourceSection.getSource()),
                        Encodings.UTF_8);
                return createArray(new Object[]{ file, sourceSection.getStartLine() });
            }
        }

    }

    @CoreMethod(names = "const_set", required = 2)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class ConstSetNode extends CoreMethodNode {

        public static ConstSetNode create() {
            return ConstSetNodeFactory.create(null, null, null);
        }

        @Child private ConstSetUncheckedNode uncheckedSetNode = ConstSetUncheckedNode.create();

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @TruffleBoundary
        @Specialization
        protected Object setConstant(RubyModule module, String name, Object value) {
            if (!Identifiers.isValidConstantName(name)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions()
                                .nameError(StringUtils.format("wrong constant name %s", name), module, name, this));
            }

            return uncheckedSetNode.execute(module, name, value);
        }
    }

    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class ConstSetUncheckedNode extends RubyBaseNode {

        @Child private WarnAlreadyInitializedNode warnAlreadyInitializedNode;

        public static ConstSetUncheckedNode create() {
            return ConstSetUncheckedNodeGen.create(null, null, null);
        }

        public abstract Object execute(VirtualFrame frame);

        public abstract Object execute(RubyModule module, String name, Object value);

        @TruffleBoundary
        @Specialization
        protected Object setConstantNoCheckName(RubyModule module, String name, Object value) {
            final RubyConstant previous = module.fields.setConstant(getContext(), this, name, value);
            if (previous != null && previous.hasValue()) {
                warnAlreadyInitializedConstant(module, name, previous.getSourceSection());
            }
            return value;
        }

        private void warnAlreadyInitializedConstant(RubyModule module, String name,
                SourceSection previousSourceSection) {
            if (warnAlreadyInitializedNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnAlreadyInitializedNode = insert(new WarnAlreadyInitializedNode());
            }

            if (warnAlreadyInitializedNode.shouldWarn()) {
                final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
                warnAlreadyInitializedNode.warnAlreadyInitialized(module, name, sourceSection, previousSourceSection);
            }
        }

    }

    @GenerateUncached
    @CoreMethod(
            names = "define_method",
            needsBlock = true,
            required = 1,
            optional = 1,
            argumentNames = { "name", "proc_or_method", "block" },
            alwaysInlined = true)
    @ImportStatic(RubyArguments.class)
    public abstract static class DefineMethodNode extends AlwaysInlinedMethodNode {

        //Checkstyle: stop
        @Specialization(guards = { "isMethodParameterProvided(rubyArgs)", "isRubyMethod(getArgument(rubyArgs, 1))" })
        //Checkstyle: resume
        protected RubySymbol defineMethodWithMethod(
                Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached(allowUncached = true) CanBindMethodToModuleNode canBindMethodToModuleNode) {
            final String name = nameToJavaStringNode.execute(RubyArguments.getArgument(rubyArgs, 0));
            final Object method = RubyArguments.getArgument(rubyArgs, 1);

            return addMethod(module, name, (RubyMethod) method, canBindMethodToModuleNode);
        }

        //Checkstyle: stop
        @Specialization(guards = { "isMethodParameterProvided(rubyArgs)", "isRubyProc(getArgument(rubyArgs, 1))" })
        //Checkstyle: resume
        protected RubySymbol defineMethodWithProc(
                Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String name = nameToJavaStringNode.execute(RubyArguments.getArgument(rubyArgs, 0));
            final Object method = RubyArguments.getArgument(rubyArgs, 1);

            needCallerFrame(callerFrame, target);
            return addProc(module, name, (RubyProc) method, callerFrame.materialize());
        }

        //Checkstyle: stop
        @Specialization(
                guards = { "isMethodParameterProvided(rubyArgs)", "isRubyUnboundMethod(getArgument(rubyArgs, 1))" })
        //Checkstyle: resume
        protected RubySymbol defineMethodWithUnboundMethod(
                Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String name = nameToJavaStringNode.execute(RubyArguments.getArgument(rubyArgs, 0));
            final Object method = RubyArguments.getArgument(rubyArgs, 1);

            needCallerFrame(callerFrame, target);
            return addUnboundMethod(module, name, (RubyUnboundMethod) method, callerFrame.materialize());
        }

        @Specialization(guards = {
                "isMethodParameterProvided(rubyArgs)",
                "!isExpectedMethodParameterType(getArgument(rubyArgs, 1))" })
        protected RubySymbol defineMethodWithUnexpectedMethodParameterType(
                Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target) {
            final Object method = RubyArguments.getArgument(rubyArgs, 1);
            throw new RaiseException(getContext(),
                    coreExceptions().typeErrorExpectedProcOrMethodOrUnboundMethod(method, this));
        }

        @Specialization(guards = { "!isMethodParameterProvided(rubyArgs)", "isBlockProvided(rubyArgs)" })
        protected RubySymbol defineMethodWithBlock(
                Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String name = nameToJavaStringNode.execute(RubyArguments.getArgument(rubyArgs, 0));
            final Object block = RubyArguments.getBlock(rubyArgs);
            needCallerFrame(callerFrame, target);
            return addProc(module, name, (RubyProc) block, callerFrame.materialize());
        }

        @Specialization(guards = { "!isMethodParameterProvided(rubyArgs)", "!isBlockProvided(rubyArgs)" })
        protected RubySymbol defineMethodWithoutMethodAndBlock(
                Frame callerFrame, RubyModule nodule, Object[] rubyArgs, RootCallTarget target) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorProcWithoutBlock(this));
        }

        @TruffleBoundary
        private RubySymbol addMethod(RubyModule module, String name, RubyMethod method,
                @Cached CanBindMethodToModuleNode canBindMethodToModuleNode) {
            final InternalMethod internalMethod = method.method;

            if (!canBindMethodToModuleNode.executeCanBindMethodToModule(internalMethod, module)) {
                final RubyModule declaringModule = internalMethod.getDeclaringModule();
                if (RubyGuards.isSingletonClass(declaringModule)) {
                    throw new RaiseException(getContext(), coreExceptions().typeError(
                            "can't bind singleton method to a different class",
                            this));
                } else {
                    throw new RaiseException(getContext(), coreExceptions().typeError(
                            "class must be a subclass of " + declaringModule.fields.getName(),
                            this));
                }
            }

            module.fields.addMethod(getContext(), this, internalMethod.withName(name));
            return getSymbol(name);
        }

        @TruffleBoundary
        private RubySymbol addUnboundMethod(RubyModule module, String name, RubyUnboundMethod method,
                MaterializedFrame callerFrame) {
            final InternalMethod internalMethod = method.method;
            if (!ModuleOperations.canBindMethodTo(internalMethod, module)) {
                final RubyModule declaringModule = internalMethod.getDeclaringModule();
                if (RubyGuards.isSingletonClass(declaringModule)) {
                    throw new RaiseException(getContext(), coreExceptions().typeError(
                            "can't bind singleton method to a different class",
                            this));
                } else {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().typeError(
                                    "bind argument must be a subclass of " +
                                            declaringModule.fields.getName(),
                                    this));
                }
            }

            return addInternalMethod(module, name, internalMethod, callerFrame);
        }

        @TruffleBoundary
        private RubySymbol addProc(RubyModule module, String name, RubyProc proc, MaterializedFrame callerFrame) {
            final RootCallTarget callTargetForLambda = proc.callTargets.getCallTargetForLambda();
            final RubyLambdaRootNode rootNode = RubyLambdaRootNode.of(callTargetForLambda);
            final SharedMethodInfo info = proc.getSharedMethodInfo().forDefineMethod(module, name, proc);
            final RubyNode body = rootNode.copyBody();
            final RubyNode newBody = new CallMethodWithLambdaBody(isSingleContext() ? proc : null,
                    callTargetForLambda, body);

            final RubyLambdaRootNode newRootNode = rootNode.copyRootNode(info, newBody);
            final RootCallTarget newCallTarget = newRootNode.getCallTarget();

            final InternalMethod internalMethod = InternalMethod.fromProc(
                    getContext(),
                    info,
                    proc.declarationContext,
                    name,
                    module,
                    Visibility.PUBLIC,
                    proc,
                    newCallTarget);
            return addInternalMethod(module, name, internalMethod, callerFrame);
        }

        private static class CallMethodWithLambdaBody extends RubyContextSourceNode {

            private final RubyProc proc;
            private final RootCallTarget lambdaCallTarget;
            @Child private RubyNode lambdaBody;

            public CallMethodWithLambdaBody(RubyProc proc, RootCallTarget lambdaCallTarget, RubyNode lambdaBody) {
                this.proc = proc;
                this.lambdaCallTarget = lambdaCallTarget;
                this.lambdaBody = lambdaBody;
            }

            @Override
            public Object execute(VirtualFrame frame) {
                final RubyProc proc;
                if (this.proc == null) {
                    proc = RubyArguments.getMethod(frame).getProc();
                    assert proc.callTargets.getCallTargetForLambda() == lambdaCallTarget;
                } else {
                    assert RubyArguments.getMethod(frame).getProc() == this.proc;
                    proc = this.proc;
                }

                RubyArguments.setDeclarationFrame(frame, proc.declarationFrame);
                return lambdaBody.execute(frame);
            }

        }

        @TruffleBoundary
        private RubySymbol addInternalMethod(RubyModule module, String name, InternalMethod method,
                MaterializedFrame callerFrame) {
            method = method.withName(name);

            final Visibility visibility = DeclarationContext
                    .findVisibilityCheckSelfAndDefaultDefinee(module, callerFrame);
            module.addMethodConsiderNameVisibility(getContext(), method, visibility, this);
            return getSymbol(method.getName());
        }

        protected boolean isMethodParameterProvided(Object[] rubyArgs) {
            final int count = RubyArguments.getPositionalArgumentsCount(rubyArgs, false);
            return count >= 2;
        }

        protected boolean isExpectedMethodParameterType(Object method) {
            return RubyGuards.isRubyMethod(method) || RubyGuards.isRubyUnboundMethod(method) ||
                    RubyGuards.isRubyProc(method);
        }

    }

    @CoreMethod(names = "extend_object", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class ExtendObjectNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();

        @Specialization
        protected RubyModule extendObject(RubyModule module, Object object,
                @Cached BranchProfile errorProfile) {
            if (module instanceof RubyClass) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorWrongArgumentType(module, "Module", this));
            }

            singletonClassNode.executeSingletonClass(object).fields
                    .include(getContext(), this, module);
            return module;
        }

    }

    @CoreMethod(names = "extended", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class ExtendedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object extended(RubyModule module, Object object) {
            return nil;
        }

    }


    @CoreMethod(names = "initialize", needsBlock = true) // Ideally should not split if no block given
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        public abstract RubyModule executeInitialize(RubyModule module, Object block);

        @Specialization
        protected RubyModule initialize(RubyModule module, Nil block) {
            return module;
        }

        @Specialization
        protected RubyModule initialize(RubyModule module, RubyProc block,
                @Cached ClassExecBlockNode classExecBlockNode) {
            classExecBlockNode.execute(EmptyArgumentsDescriptor.INSTANCE, module, new Object[]{ module }, block);
            return module;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode;

        @Specialization(guards = { "!isRubyClass(self)", "!isRubyClass(from)" })
        protected Object initializeCopyModule(RubyModule self, RubyModule from) {
            self.fields.initCopy(from);

            final RubyClass selfMetaClass = getSingletonClass(self);
            final RubyClass fromMetaClass = getSingletonClass(from);
            selfMetaClass.fields.initCopy(fromMetaClass);

            return nil;
        }

        @Specialization
        protected Object initializeCopyClass(RubyClass self, RubyClass from,
                @Cached BranchProfile errorProfile) {
            if (from == coreLibrary().basicObjectClass) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeError("can't copy the root class", this));
            } else if (from.isSingleton) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeError("can't copy singleton class", this));
            }

            self.fields.initCopy(from);

            final RubyClass selfMetaClass = getSingletonClass(self);
            final RubyClass fromMetaClass = from.getMetaClass();

            assert fromMetaClass.isSingleton;
            assert self.getMetaClass().isSingleton;

            selfMetaClass.fields.initCopy(fromMetaClass); // copy class methods

            return nil;
        }

        protected RubyClass getSingletonClass(RubyModule object) {
            if (singletonClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singletonClassNode = insert(SingletonClassNode.create());
            }

            return singletonClassNode.executeSingletonClass(object);
        }

    }

    @CoreMethod(names = "included", needsSelf = false, required = 1, visibility = Visibility.PRIVATE)
    public abstract static class IncludedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object included(Object subclass) {
            return nil;
        }

    }

    @CoreMethod(names = "included_modules")
    public abstract static class IncludedModulesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray includedModules(RubyModule module) {
            final List<RubyModule> modules = new ArrayList<>();

            for (RubyModule included : module.fields.ancestors()) {
                if (!(included instanceof RubyClass) && included != module) {
                    modules.add(included);
                }
            }

            return createArray(modules.toArray());
        }
    }

    @CoreMethod(names = "method_defined?", required = 1, optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    public abstract static class MethodDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected boolean isMethodDefined(RubyModule module, String name, boolean inherit) {
            final InternalMethod method;
            if (inherit) {
                method = ModuleOperations.lookupMethodUncached(module, name, null);
            } else {
                method = module.fields.getMethod(name);
            }

            return method != null && !method.isUndefined() && !(method.getVisibility() == Visibility.PRIVATE);
        }

    }

    @GenerateUncached
    @ImportStatic(RubyArguments.class)
    @CoreMethod(names = "module_function", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class ModuleFunctionNode extends AlwaysInlinedMethodNode {
        @Specialization(guards = "names.length == 0")
        protected RubyModule frame(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names,
                @Cached BranchProfile errorProfile) {
            checkNotClass(module, errorProfile);
            needCallerFrame(callerFrame, "Module#module_function with no arguments");
            DeclarationContext.setCurrentVisibility(callerFrame, Visibility.MODULE_FUNCTION);
            return module;
        }

        @Specialization(guards = "names.length > 0")
        protected RubyModule methods(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names,
                @Cached SetMethodVisibilityNode setMethodVisibilityNode,
                @Cached BranchProfile errorProfile,
                @Cached LoopConditionProfile loopProfile) {
            checkNotClass(module, errorProfile);
            int i = 0;
            try {
                for (; loopProfile.inject(i < names.length); ++i) {
                    setMethodVisibilityNode.execute(module, names[i], Visibility.MODULE_FUNCTION);
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, i);
            }
            return module;
        }

        private void checkNotClass(RubyModule module, BranchProfile errorProfile) {
            if (module instanceof RubyClass) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("module_function must be called for modules", this));
            }
        }
    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object name(RubyModule module) {
            return module.fields.getRubyStringName();
        }
    }

    @Primitive(name = "caller_nesting")
    public abstract static class CallerNestingNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyArray nesting(
                @Cached NestingNode nestingNode) {
            return nestingNode.execute();
        }
    }

    @CoreMethod(names = "nesting", onSingleton = true)
    public abstract static class NestingNode extends CoreMethodNode {

        public abstract RubyArray execute();

        @TruffleBoundary
        @Specialization
        protected RubyArray nesting() {
            final List<RubyModule> modules = new ArrayList<>();

            InternalMethod method = getContext().getCallStack().getCallingMethod();
            LexicalScope lexicalScope = method == null ? null : method.getLexicalScope();
            RubyClass objectClass = coreLibrary().objectClass;

            while (lexicalScope != null) {
                final RubyModule enclosing = lexicalScope.getLiveModule();
                if (enclosing == objectClass) {
                    break;
                }
                modules.add(enclosing);
                lexicalScope = lexicalScope.getParent();
            }

            return createArray(modules.toArray());
        }
    }

    @GenerateUncached
    @ImportStatic(RubyArguments.class)
    @CoreMethod(names = "public", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class PublicNode extends AlwaysInlinedMethodNode {
        @Specialization(guards = "names.length == 0")
        protected RubyModule frame(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names) {
            needCallerFrame(callerFrame, "Module#public with no arguments");
            DeclarationContext.setCurrentVisibility(callerFrame, Visibility.PUBLIC);
            return module;
        }

        @Specialization(guards = "names.length > 0")
        protected RubyModule methods(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names,
                @Cached SetMethodVisibilityNode setMethodVisibilityNode) {
            for (Object name : names) {
                setMethodVisibilityNode.execute(module, name, Visibility.PUBLIC);
            }
            return module;
        }
    }

    @CoreMethod(names = "public_class_method", rest = true)
    public abstract static class PublicClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNode.create();

        @Specialization
        protected RubyModule publicClassMethod(RubyModule module, Object[] names) {
            final RubyClass singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.execute(singletonClass, name, Visibility.PUBLIC);
            }

            return module;
        }
    }

    @GenerateUncached
    @ImportStatic(RubyArguments.class)
    @CoreMethod(names = "private", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class PrivateNode extends AlwaysInlinedMethodNode {
        @Specialization(guards = "names.length == 0")
        protected RubyModule frame(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names) {
            needCallerFrame(callerFrame, "Module#private with no arguments");
            DeclarationContext.setCurrentVisibility(callerFrame, Visibility.PRIVATE);
            return module;
        }

        @Specialization(guards = "names.length > 0")
        protected RubyModule methods(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names,
                @Cached SetMethodVisibilityNode setMethodVisibilityNode) {
            for (Object name : names) {
                setMethodVisibilityNode.execute(module, name, Visibility.PRIVATE);
            }
            return module;
        }
    }

    @CoreMethod(names = "prepend_features", required = 1, visibility = Visibility.PRIVATE, split = Split.NEVER)
    public abstract static class PrependFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object prependFeatures(RubyModule features, RubyModule target,
                @Cached BranchProfile errorProfile) {
            if (features instanceof RubyClass) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("prepend_features must be called only on modules", this));
            }
            target.fields.prepend(getContext(), this, features);
            return nil;
        }
    }

    @CoreMethod(names = "private_class_method", rest = true)
    public abstract static class PrivateClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNode.create();

        @Specialization
        protected RubyModule privateClassMethod(VirtualFrame frame, RubyModule module, Object[] names) {
            final RubyClass singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.execute(singletonClass, name, Visibility.PRIVATE);
            }

            return module;
        }
    }

    @CoreMethod(names = "public_instance_method", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class PublicInstanceMethodNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected RubyUnboundMethod publicInstanceMethod(RubyModule module, String name,
                @Cached BranchProfile errorProfile) {
            // TODO(CS, 11-Jan-15) cache this lookup
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, null);

            if (method == null || method.isUndefined()) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorUndefinedMethod(name, module, this));
            } else if (method.getVisibility() != Visibility.PUBLIC) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateMethod(name, module, this));
            }

            final RubyUnboundMethod instance = new RubyUnboundMethod(
                    coreLibrary().unboundMethodClass,
                    getLanguage().unboundMethodShape,
                    module,
                    method);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyBaseNodeWithExecute.class)
    protected abstract static class AbstractInstanceMethodsNode extends CoreMethodNode {

        final Visibility visibility;

        public AbstractInstanceMethodsNode(Visibility visibility) {
            this.visibility = visibility;
        }

        @CreateCast("includeAncestors")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute includeAncestors) {
            return BooleanCastWithDefaultNode.create(true, includeAncestors);
        }

        @Specialization
        @TruffleBoundary
        protected RubyArray getInstanceMethods(RubyModule module, boolean includeAncestors) {
            Object[] objects = module.fields
                    .filterMethods(getLanguage(), includeAncestors, MethodFilter.by(visibility))
                    .toArray();
            return createArray(objects);
        }

    }

    @CoreMethod(names = "public_instance_methods", optional = 1)
    public abstract static class PublicInstanceMethodsNode extends AbstractInstanceMethodsNode {

        public PublicInstanceMethodsNode() {
            super(Visibility.PUBLIC);
        }

    }

    @CoreMethod(names = "protected_instance_methods", optional = 1)
    public abstract static class ProtectedInstanceMethodsNode extends AbstractInstanceMethodsNode {

        public ProtectedInstanceMethodsNode() {
            super(Visibility.PROTECTED);
        }

    }

    @CoreMethod(names = "private_instance_methods", optional = 1)
    public abstract static class PrivateInstanceMethodsNode extends AbstractInstanceMethodsNode {

        public PrivateInstanceMethodsNode() {
            super(Visibility.PRIVATE);
        }

    }


    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "inherit", type = RubyBaseNodeWithExecute.class)
    protected abstract static class AbstractMethodDefinedNode extends CoreMethodNode {

        final Visibility visibility;

        public AbstractMethodDefinedNode(Visibility visibility) {
            this.visibility = visibility;
        }

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("inherit")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute inherit) {
            return BooleanCastWithDefaultNode.create(true, inherit);
        }

        // NOTE(norswap): We considered caching the lookup here, but determined that the resulting complexity
        //   increase in LookupMethodNode wasn't worth it, as it would slow down the more common cases.

        @Specialization(guards = "inherit")
        protected boolean isMethodDefinedInherit(RubyModule module, String name, boolean inherit) {
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, null);
            return method != null && !method.isUndefined() && !method.isUnimplemented() &&
                    method.getVisibility() == visibility;
        }

        @Specialization(guards = "!inherit")
        protected boolean isMethodDefinedDontInherit(RubyModule module, String name, boolean inherit) {
            final InternalMethod method = module.fields.getMethod(name);
            return method != null && !method.isUndefined() && !method.isUnimplemented() &&
                    method.getVisibility() == visibility;
        }
    }

    @CoreMethod(names = "public_method_defined?", required = 1, optional = 1)
    public abstract static class PublicMethodDefinedNode extends AbstractMethodDefinedNode {
        public PublicMethodDefinedNode() {
            super(Visibility.PUBLIC);
        }
    }

    @CoreMethod(names = "protected_method_defined?", required = 1, optional = 1)
    public abstract static class ProtectedMethodDefinedNode extends AbstractMethodDefinedNode {
        public ProtectedMethodDefinedNode() {
            super(Visibility.PROTECTED);
        }
    }

    @CoreMethod(names = "private_method_defined?", required = 1, optional = 1)
    public abstract static class PrivateMethodDefinedNode extends AbstractMethodDefinedNode {
        public PrivateMethodDefinedNode() {
            super(Visibility.PRIVATE);
        }
    }

    @CoreMethod(names = "instance_methods", optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyBaseNodeWithExecute.class)
    public abstract static class InstanceMethodsNode extends CoreMethodNode {

        @CreateCast("includeAncestors")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute includeAncestors) {
            return BooleanCastWithDefaultNode.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray instanceMethods(RubyModule module, boolean includeAncestors) {
            Object[] objects = module.fields
                    .filterMethods(getLanguage(), includeAncestors, MethodFilter.PUBLIC_PROTECTED)
                    .toArray();
            return createArray(objects);
        }
    }

    @GenerateUncached
    @CoreMethod(names = "instance_method", required = 1, alwaysInlined = true)
    public abstract static class InstanceMethodNode extends AlwaysInlinedMethodNode {

        @Specialization
        protected RubyUnboundMethod instanceMethod(
                Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached BranchProfile errorProfile) {
            needCallerFrame(callerFrame, target);
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(callerFrame);
            final String name = nameToJavaStringNode.execute(RubyArguments.getArgument(rubyArgs, 0));

            // TODO(CS, 11-Jan-15) cache this lookup
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, declarationContext);

            if (method == null || method.isUndefined()) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorUndefinedMethod(name, module, this));
            }

            final RubyUnboundMethod instance = new RubyUnboundMethod(
                    coreLibrary().unboundMethodClass,
                    getLanguage().unboundMethodShape,
                    module,
                    method);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "private_constant", rest = true)
    public abstract static class PrivateConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        protected RubyModule privateConstant(RubyModule module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.execute(arg);
                module.fields.changeConstantVisibility(getContext(), this, name, true);
            }
            return module;
        }
    }

    @CoreMethod(names = "deprecate_constant", rest = true, raiseIfFrozenSelf = true)
    public abstract static class DeprecateConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        protected RubyModule deprecateConstant(RubyModule module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.execute(arg);
                module.fields.deprecateConstant(getContext(), this, name);
            }
            return module;
        }
    }

    @CoreMethod(names = "public_constant", rest = true)
    public abstract static class PublicConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        protected RubyModule publicConstant(RubyModule module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.execute(arg);
                module.fields.changeConstantVisibility(getContext(), this, name, false);
            }
            return module;
        }
    }

    @GenerateUncached
    @ImportStatic(RubyArguments.class)
    @CoreMethod(names = "protected", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class ProtectedNode extends AlwaysInlinedMethodNode {
        @Specialization(guards = "names.length == 0")
        protected RubyModule frame(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names) {
            needCallerFrame(callerFrame, "Module#protected with no arguments");
            DeclarationContext.setCurrentVisibility(callerFrame, Visibility.PROTECTED);
            return module;
        }

        @Specialization(guards = "names.length > 0")
        protected RubyModule methods(Frame callerFrame, RubyModule module, Object[] rubyArgs, RootCallTarget target,
                @Bind("getPositionalArguments(rubyArgs, false)") Object[] names,
                @Cached SetMethodVisibilityNode setMethodVisibilityNode) {
            for (Object name : names) {
                setMethodVisibilityNode.execute(module, name, Visibility.PROTECTED);
            }
            return module;
        }
    }

    @CoreMethod(names = "remove_class_variable", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class RemoveClassVariableNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected Object removeClassVariableString(RubyModule module, String name,
                @Cached CheckClassVariableNameNode checkClassVariableNameNode) {
            checkClassVariableNameNode.execute(module, name);
            return ModuleOperations.removeClassVariable(module.fields, getContext(), this, name);
        }

    }

    @Primitive(name = "module_remove_const")
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class RemoveConstNode extends PrimitiveNode {

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected Object removeConstant(RubyModule module, String name) {
            final RubyConstant oldConstant = module.fields.removeConstant(getContext(), this, name);
            if (oldConstant == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorConstantNotDefined(module, name, this));
            } else {
                if (oldConstant.isAutoload() || oldConstant.isUndefined()) {
                    return nil;
                } else {
                    return oldConstant.getValue();
                }
            }
        }

    }

    @CoreMethod(names = "remove_method", rest = true)
    public abstract static class RemoveMethodNode extends CoreMethodArrayArgumentsNode {

        private final BranchProfile errorProfile = BranchProfile.create();

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private TypeNodes.CheckFrozenNode raiseIfFrozenNode = TypeNodes.CheckFrozenNode.create();
        @Child private DispatchNode methodRemovedNode = DispatchNode.create();

        @Specialization
        protected RubyModule removeMethods(RubyModule module, Object[] names) {
            for (Object name : names) {
                removeMethod(module, nameToJavaStringNode.execute(name));
            }
            return module;
        }

        private void removeMethod(RubyModule module, String name) {
            raiseIfFrozenNode.execute(module);

            if (module.fields.removeMethod(name)) {
                if (RubyGuards.isSingletonClass(module)) {
                    final RubyDynamicObject receiver = ((RubyClass) module).attached;
                    methodRemovedNode.call(receiver, "singleton_method_removed", getSymbol(name));
                } else {
                    methodRemovedNode.call(module, "method_removed", getSymbol(name));
                }
            } else {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorMethodNotDefinedIn(module, name, this));
            }
        }

    }

    @CoreMethod(names = { "to_s", "inspect" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyString toS(RubyModule module,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            final String moduleName;
            if (module.fields.isRefinement()) {
                moduleName = module.fields.getRefinementName();
            } else {
                moduleName = module.fields.getName();
            }
            return createString(fromJavaStringNode, moduleName, Encodings.UTF_8);
        }
    }

    @CoreMethod(names = "undef_method", rest = true, split = Split.NEVER, argumentNames = "names")
    public abstract static class UndefMethodNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyModule undefMethods(RubyModule module, Object[] names,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            for (Object name : names) {
                module.fields.undefMethod(getLanguage(), getContext(), this, nameToJavaStringNode.execute(name));
            }
            return module;
        }

        /** Used only by undef keyword {@link org.truffleruby.parser.BodyTranslator#visitUndefNode} */
        @TruffleBoundary
        @Specialization
        protected RubyModule undefKeyword(RubyModule module, RubySymbol name) {
            module.fields.undefMethod(getLanguage(), getContext(), this, name.getString());
            return module;
        }

    }

    @CoreMethod(names = "used_modules", onSingleton = true)
    public abstract static class UsedModulesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray usedModules() {
            final Frame frame = getContext().getCallStack().getCallerFrame(FrameAccess.READ_ONLY);
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
            final Set<RubyModule> refinementNamespaces = new HashSet<>();
            for (RubyModule[] refinementModules : declarationContext.getRefinements().values()) {
                for (RubyModule refinementModule : refinementModules) {
                    refinementNamespaces.add(refinementModule.fields.getRefinementNamespace());
                }
            }
            return createArray(refinementNamespaces.toArray());
        }

    }

    @NonStandard
    @CoreMethod(names = "used_refinements", onSingleton = true)
    public abstract static class UsedRefinementsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray usedRefinements() {
            final Frame frame = getContext().getCallStack().getCallerFrame(FrameAccess.READ_ONLY);
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
            final Set<RubyModule> refinements = new HashSet<>();
            for (RubyModule[] refinementModules : declarationContext.getRefinements().values()) {
                for (RubyModule refinementModule : refinementModules) {
                    refinements.add(refinementModule);
                }
            }
            return createArray(refinements.toArray());
        }

    }

    @GenerateUncached
    @ImportStatic(ArrayGuards.class)
    public abstract static class SetMethodVisibilityNode extends RubyBaseNode {

        public static SetMethodVisibilityNode create() {
            return SetMethodVisibilityNodeGen.create();
        }

        public abstract void execute(RubyModule module, Object name, Visibility visibility);

        @TruffleBoundary
        @Specialization(guards = "!isRubyArray(name)")
        protected void setMethodVisibility(RubyModule module, Object name, Visibility visibility,
                @Cached @Shared("nameToJavaStringNode") NameToJavaStringNode nameToJavaStringNode) {
            final String methodName = nameToJavaStringNode.execute(name);

            final InternalMethod method = module.fields.deepMethodSearch(getContext(), methodName);

            if (method == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUndefinedMethod(methodName, module, this));
            }

            // Do nothing if the method already exists with the same visibility, like MRI
            if (method.getVisibility() == visibility) {
                return;
            }

            /* If the method was already defined in this class, that's fine {@link addMethod} will overwrite it,
             * otherwise we do actually want to add a copy of the method with a different visibility to this module. */
            module.addMethodIgnoreNameVisibility(getContext(), method, visibility, this);
        }

        @TruffleBoundary
        @Specialization
        protected void setMethodVisibilityArray(RubyModule module, RubyArray array, Visibility visibility,
                @Cached @Shared("nameToJavaStringNode") NameToJavaStringNode nameToJavaStringNode) {
            for (Object name : ArrayOperations.toIterable(array)) {
                setMethodVisibility(
                        module,
                        name,
                        visibility,
                        nameToJavaStringNode);
                TruffleSafepoint.poll(this);
            }
        }

    }

    @CoreMethod(names = "refine", needsBlock = true, required = 1, visibility = Visibility.PRIVATE, split = Split.NEVER)
    public abstract static class RefineNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyModule refine(RubyModule self, Object moduleToRefine, Nil block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("no block given", this));
        }

        @Specialization(guards = "!isRubyModule(moduleToRefine)")
        protected RubyModule refineNotModule(RubyModule self, Object moduleToRefine, RubyProc block) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorWrongArgumentType(moduleToRefine, "Class", this));
        }

        @TruffleBoundary
        @Specialization
        protected RubyModule refine(RubyModule namespace, RubyModule moduleToRefine, RubyProc block) {
            final ConcurrentMap<RubyModule, RubyModule> refinements = namespace.fields
                    .getRefinements();
            final RubyModule refinement = ConcurrentOperations
                    .getOrCompute(refinements, moduleToRefine, klass -> newRefinementModule(namespace, moduleToRefine));

            // Apply the existing refinements in this namespace and the new refinement inside the refine block
            final Map<RubyModule, RubyModule[]> refinementsInDeclarationContext = new HashMap<>();
            for (Entry<RubyModule, RubyModule> existingRefinement : refinements.entrySet()) {
                refinementsInDeclarationContext
                        .put(existingRefinement.getKey(), new RubyModule[]{ existingRefinement.getValue() });
            }
            refinementsInDeclarationContext.put(moduleToRefine, new RubyModule[]{ refinement });
            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new FixedDefaultDefinee(refinement),
                    refinementsInDeclarationContext);

            // Update methods in existing refinements in this namespace to also see this new refine block's refinements
            for (RubyModule existingRefinement : refinements.values()) {
                final ModuleFields fields = existingRefinement.fields;
                for (InternalMethod refinedMethodInExistingRefinement : fields.getMethods()) {
                    fields.addMethod(
                            getContext(),
                            this,
                            refinedMethodInExistingRefinement.withDeclarationContext(declarationContext));
                }
            }

            CallBlockNode.getUncached().executeCallBlock(
                    declarationContext,
                    block,
                    refinement,
                    nil,
                    EmptyArgumentsDescriptor.INSTANCE,
                    EMPTY_ARGUMENTS,
                    null);
            return refinement;
        }

        private RubyModule newRefinementModule(RubyModule namespace, RubyModule moduleToRefine) {
            final RubyModule refinement = createModule(
                    getContext(),
                    getEncapsulatingSourceSection(),
                    coreLibrary().moduleClass,
                    null,
                    null,
                    this);
            final ModuleFields refinementFields = refinement.fields;
            refinementFields.setupRefinementModule(moduleToRefine, namespace);
            return refinement;
        }

    }

    @GenerateUncached
    @CoreMethod(names = "using", required = 1, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class ModuleUsingNode extends UsingNode {
        @Specialization
        protected Object moduleUsing(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached BranchProfile errorProfile) {
            needCallerFrame(callerFrame, target);
            final Object refinementModule = RubyArguments.getArgument(rubyArgs, 0);
            if (self != RubyArguments.getSelf(callerFrame)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Module#using is not called on self", this));
            }
            final InternalMethod callerMethod = RubyArguments.getMethod(callerFrame);
            if (!callerMethod.getSharedMethodInfo().isModuleBody()) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Module#using is not permitted in methods", this));
            }
            using(callerFrame, refinementModule, errorProfile);
            return self;
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyModule allocate(RubyClass rubyClass) {
            return createModule(getContext(), getEncapsulatingSourceSection(), rubyClass, null, null, this);
        }

    }

    @CoreMethod(names = "singleton_class?")
    public abstract static class IsSingletonClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isRubyClass(rubyModule)")
        protected Object doModule(RubyModule rubyModule) {
            return false;
        }

        @Specialization
        protected Object doClass(RubyClass rubyClass) {
            return rubyClass.isSingleton;
        }
    }
}
