/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.oracle.truffle.api.dsl.CachedLanguage;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.cast.ToPathNodeGen;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStringOrSymbolNodeGen;
import org.truffleruby.core.constant.WarnAlreadyInitializedNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.MethodFilter;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.method.RubyUnboundMethod;
import org.truffleruby.core.module.ModuleNodesFactory.ClassExecNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.ConstSetNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.GenerateAccessorNodeGen;
import org.truffleruby.core.module.ModuleNodesFactory.IsSubclassOfOrEqualToNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.SetMethodVisibilityNodeGen;
import org.truffleruby.core.module.ModuleNodesFactory.SetVisibilityNodeGen;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.WarningNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.eval.CreateEvalSourceNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.AddMethodNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.CanBindMethodToModuleNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.DeclarationContext.FixedDefaultDefinee;
import org.truffleruby.language.methods.GetCurrentVisibilityNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.methods.UsingNode;
import org.truffleruby.language.methods.UsingNodeGen;
import org.truffleruby.language.objects.AllocateHelperNode;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.ReadInstanceVariableNode;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.WriteInstanceVariableNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.Identifiers;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.Translator;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
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
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.api.source.SourceSection;


@CoreModule(value = "Module", isClass = true)
public abstract class ModuleNodes {

    @TruffleBoundary
    public static RubyModule createModule(RubyContext context, SourceSection sourceSection, RubyClass selfClass,
            RubyModule lexicalParent, String name, Node currentNode) {
        final RubyModule module = new RubyModule(
                selfClass,
                selfClass.instanceShape,
                context,
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
    @NodeChild(value = "newName", type = RubyNode.class)
    @NodeChild(value = "oldName", type = RubyNode.class)
    public abstract static class AliasMethodNode extends CoreMethodNode {

        @CreateCast("newName")
        protected RubyNode coerceNewNameToString(RubyNode newName) {
            return NameToJavaStringNode.create(newName);
        }

        @CreateCast("oldName")
        protected RubyNode coerceOldNameToString(RubyNode oldName) {
            return NameToJavaStringNode.create(oldName);
        }

        @Child AddMethodNode addMethodNode = AddMethodNode.create(false);

        @Specialization
        protected RubyModule aliasMethod(RubyModule module, String newName, String oldName,
                @Cached BranchProfile errorProfile) {
            final InternalMethod method = module.fields
                    .deepMethodSearch(getContext(), oldName);

            if (method == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().nameErrorUndefinedMethod(
                        oldName,
                        module,
                        this));
            }

            InternalMethod aliasMethod = method.withName(newName);

            addMethodNode.executeAddMethod(module, aliasMethod, aliasMethod.getVisibility());
            return module;
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

        @Child private TaintResultNode taintResultNode = new TaintResultNode();

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
            taintResultNode.maybeTaint(features, target);
            return nil;
        }
    }

    public abstract static class GenerateAccessorNode extends RubyContextNode {

        final boolean isGetter;

        public GenerateAccessorNode(boolean isGetter) {
            this.isGetter = isGetter;
        }

        public abstract Object executeGenerateAccessor(VirtualFrame frame, RubyModule module, Object name);

        @Specialization
        protected Object generateAccessor(VirtualFrame frame, RubyModule module, Object nameObject,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached ReadCallerFrameNode readCallerFrame,
                @CachedLanguage RubyLanguage language) {
            final String name = nameToJavaStringNode.execute(nameObject);
            createAccessor(module, name, readCallerFrame.execute(frame), language);
            return nil;
        }

        @TruffleBoundary
        private void createAccessor(RubyModule module, String name, MaterializedFrame callerFrame,
                RubyLanguage language) {
            final SourceSection sourceSection = getContext()
                    .getCallStack()
                    .getCallerNodeIgnoringSend()
                    .getEncapsulatingSourceSection();
            final Visibility visibility = DeclarationContext.findVisibility(callerFrame);
            final Arity arity = isGetter ? Arity.NO_ARGUMENTS : Arity.ONE_REQUIRED;
            final String ivar = "@" + name;
            final String accessorName = isGetter ? name : name + "=";

            final LexicalScope lexicalScope = new LexicalScope(getContext().getRootLexicalScope(), module);
            final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                    sourceSection,
                    lexicalScope,
                    arity,
                    module,
                    accessorName,
                    0,
                    isGetter ? "attr_reader" : "attr_writer",
                    null);

            final RubyNode accessInstanceVariable;
            if (isGetter) {
                accessInstanceVariable = new ReadInstanceVariableNode(ivar, new ReadSelfNode());
            } else {
                RubyNode readArgument = Translator.profileArgument(
                        language,
                        new ReadPreArgumentNode(0, MissingArgumentBehavior.RUNTIME_ERROR));
                accessInstanceVariable = new WriteInstanceVariableNode(ivar, new ReadSelfNode(), readArgument);
            }

            final RubyNode body = Translator
                    .createCheckArityNode(language, arity, accessInstanceVariable);
            final RubyRootNode rootNode = new RubyRootNode(
                    getContext(),
                    sourceSection,
                    null,
                    sharedMethodInfo,
                    body,
                    Split.HEURISTIC);
            final RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
            final InternalMethod method = new InternalMethod(
                    getContext(),
                    sharedMethodInfo,
                    lexicalScope,
                    DeclarationContext.NONE,
                    accessorName,
                    module,
                    visibility,
                    callTarget);

            module.fields.addMethod(getContext(), this, method);
        }
    }

    @CoreMethod(names = "attr", rest = true)
    public abstract static class AttrNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = GenerateAccessorNodeGen.create(true);
        @Child private GenerateAccessorNode generateSetterNode = GenerateAccessorNodeGen.create(false);
        @Child private WarningNode warnNode;

        @Specialization
        protected Object attr(VirtualFrame frame, RubyModule module, Object[] names) {
            final boolean setter;
            if (names.length == 2 && names[1] instanceof Boolean) {
                warnObsoletedBooleanArgument();
                setter = (boolean) names[1];
                names = new Object[]{ names[0] };
            } else {
                setter = false;
            }

            for (Object name : names) {
                generateGetterNode.executeGenerateAccessor(frame, module, name);
                if (setter) {
                    generateSetterNode.executeGenerateAccessor(frame, module, name);
                }
            }
            return nil;
        }

        private void warnObsoletedBooleanArgument() {
            if (warnNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnNode = insert(new WarningNode());
            }

            if (warnNode.shouldWarn()) {
                final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
                warnNode.warningMessage(sourceSection, "optional boolean argument is obsoleted");
            }
        }

    }

    @CoreMethod(names = "attr_accessor", rest = true)
    public abstract static class AttrAccessorNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = GenerateAccessorNodeGen.create(true);
        @Child private GenerateAccessorNode generateSetterNode = GenerateAccessorNodeGen.create(false);

        @Specialization
        protected Object attrAccessor(VirtualFrame frame, RubyModule module, Object[] names) {
            for (Object name : names) {
                generateGetterNode.executeGenerateAccessor(frame, module, name);
                generateSetterNode.executeGenerateAccessor(frame, module, name);
            }
            return nil;
        }

    }

    @CoreMethod(names = "attr_reader", rest = true)
    public abstract static class AttrReaderNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = GenerateAccessorNodeGen.create(true);

        @Specialization
        protected Object attrReader(VirtualFrame frame, RubyModule module, Object[] names) {
            for (Object name : names) {
                generateGetterNode.executeGenerateAccessor(frame, module, name);
            }
            return nil;
        }

    }

    @CoreMethod(names = "attr_writer", rest = true)
    public abstract static class AttrWriterNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateSetterNode = GenerateAccessorNodeGen.create(false);

        @Specialization
        protected Object attrWriter(VirtualFrame frame, RubyModule module, Object[] names) {
            for (Object name : names) {
                generateSetterNode.executeGenerateAccessor(frame, module, name);
            }
            return nil;
        }

    }

    @CoreMethod(names = "autoload", required = 2)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "filename", type = RubyNode.class)
    public abstract static class AutoloadNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceNameToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("filename")
        protected RubyNode coerceFilenameToPath(RubyNode filename) {
            return ToPathNodeGen.create(filename);
        }

        @TruffleBoundary
        @Specialization
        protected Object autoload(RubyModule module, String name, RubyString filename) {
            if (!Identifiers.isValidConstantName(name)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameError(
                                StringUtils.format("autoload must be constant name: %s", name),
                                module,
                                name,
                                this));
            }

            if (filename.rope.isEmpty()) {
                throw new RaiseException(getContext(), coreExceptions().argumentError("empty file name", this));
            }

            module.fields.setAutoloadConstant(getContext(), this, name, filename);
            return nil;
        }
    }

    @CoreMethod(names = "autoload?", required = 1)
    public abstract static class IsAutoloadNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object isAutoloadSymbol(RubyModule module, RubySymbol name) {
            return isAutoload(module, name.getString());
        }

        @Specialization
        protected Object isAutoloadString(RubyModule module, RubyString name) {
            return isAutoload(module, name.getJavaString());
        }

        private Object isAutoload(RubyModule module, String name) {
            final ConstantLookupResult constant = ModuleOperations.lookupConstant(getContext(), module, name);

            if (constant.isAutoload() && !constant.getConstant().getAutoloadConstant().isAutoloadingThread()) {
                return constant.getConstant().getAutoloadConstant().getFeature();
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = { "class_eval", "module_eval" }, optional = 3, lowerFixnum = 3, needsBlock = true)
    public abstract static class ClassEvalNode extends CoreMethodArrayArgumentsNode {

        @Child private CreateEvalSourceNode createEvalSourceNode = new CreateEvalSourceNode();
        @Child private ToStrNode toStrNode;
        @Child private ReadCallerFrameNode readCallerFrameNode = ReadCallerFrameNode.create();

        protected RubyString toStr(VirtualFrame frame, Object object) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNode.create());
            }
            return toStrNode.executeToStr(object);
        }

        @Specialization
        protected Object classEval(
                VirtualFrame frame,
                RubyModule module,
                RubyString code,
                NotProvided file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, code, "(eval)", callNode);
        }

        @Specialization
        protected Object classEval(
                VirtualFrame frame,
                RubyModule module,
                RubyString code,
                RubyString file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, code, file.getJavaString(), callNode);
        }

        @Specialization
        protected Object classEval(
                VirtualFrame frame,
                RubyModule module,
                RubyString code,
                RubyString file,
                int line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = classEvalSource(
                    frame,
                    module,
                    code,
                    file.getJavaString(),
                    line);
            return deferredCall.call(callNode);
        }

        @Specialization(guards = "wasProvided(code)")
        protected Object classEval(
                VirtualFrame frame,
                RubyModule module,
                Object code,
                NotProvided file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, toStr(frame, code), "(eval)", callNode);
        }

        @Specialization(guards = { "wasProvided(file)" })
        protected Object classEval(
                VirtualFrame frame,
                RubyModule module,
                RubyString code,
                Object file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, code, toStr(frame, file).getJavaString(), callNode);
        }

        private Object classEvalSource(VirtualFrame frame, RubyModule module, RubyString code, String file,
                @Cached IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = classEvalSource(frame, module, code, file, 1);
            return deferredCall.call(callNode);
        }

        private CodeLoader.DeferredCall classEvalSource(VirtualFrame frame, RubyModule module,
                RubyString rubySource, String file, int line) {

            final MaterializedFrame callerFrame = readCallerFrameNode.execute(frame);

            return classEvalSourceInternal(module, rubySource, file, line, callerFrame);
        }

        @TruffleBoundary
        private CodeLoader.DeferredCall classEvalSourceInternal(RubyModule module, RubyString rubySource,
                String file, int line, MaterializedFrame callerFrame) {
            final RubySource source = createEvalSourceNode
                    .createEvalSource(rubySource.rope, "class/module_eval", file, line);

            final RubyRootNode rootNode = getContext().getCodeLoader().parse(
                    source,
                    ParserContext.MODULE,
                    callerFrame,
                    null,
                    true,
                    this);

            return getContext().getCodeLoader().prepareExecute(
                    ParserContext.MODULE,
                    new DeclarationContext(Visibility.PUBLIC, new FixedDefaultDefinee(module)),
                    rootNode,
                    callerFrame,
                    module);
        }

        @Specialization
        protected Object classEval(
                RubyModule self,
                NotProvided code,
                NotProvided file,
                NotProvided line,
                RubyProc block,
                @Cached ClassExecNode classExecNode) {
            return classExecNode.executeClassExec(self, new Object[]{ self }, block);
        }

        @Specialization
        protected Object classEval(
                RubyModule self,
                NotProvided code,
                NotProvided file,
                NotProvided line,
                NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(0, 1, 2, this));
        }

        @Specialization(guards = "wasProvided(code)")
        protected Object classEval(RubyModule self, Object code, NotProvided file, NotProvided line, RubyProc block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(1, 0, this));
        }

    }

    @CoreMethod(names = { "class_exec", "module_exec" }, rest = true, needsBlock = true)
    public abstract static class ClassExecNode extends CoreMethodArrayArgumentsNode {

        public static ClassExecNode create() {
            return ClassExecNodeFactory.create(null);
        }

        @Child private CallBlockNode callBlockNode = CallBlockNode.create();

        abstract Object executeClassExec(RubyModule self, Object[] args, RubyProc block);

        @Specialization
        protected Object classExec(RubyModule self, Object[] args, RubyProc block) {
            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new FixedDefaultDefinee(self),
                    block.declarationContext.getRefinements());

            return callBlockNode.executeCallBlock(declarationContext, block, self, block.block, args);
        }

        @Specialization
        protected Object classExec(RubyModule self, Object[] args, NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().noBlockGiven(this));
        }

    }

    @CoreMethod(names = "class_variable_defined?", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ClassVariableDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @TruffleBoundary
        @Specialization
        protected boolean isClassVariableDefinedString(RubyModule module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            final Object value = ModuleOperations.lookupClassVariable(module, name);

            return value != null;
        }

    }

    @CoreMethod(names = "class_variable_get", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ClassVariableGetNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        @TruffleBoundary
        protected Object getClassVariable(RubyModule module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            final Object value = ModuleOperations.lookupClassVariable(module, name);

            if (value == null) {
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
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class ClassVariableSetNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        @TruffleBoundary
        protected Object setClassVariable(RubyModule module, String name, Object value) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            ModuleOperations.setClassVariable(getContext(), module, name, value, this);

            return value;
        }

    }

    @CoreMethod(names = "class_variables")
    public abstract static class ClassVariablesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray getClassVariables(RubyModule module) {
            final Map<String, Object> allClassVariables = ModuleOperations.getAllClassVariables(module);
            final int size = allClassVariables.size();
            final Object[] store = new Object[size];

            int i = 0;
            for (String variable : allClassVariables.keySet()) {
                store[i++] = getSymbol(variable);
            }
            return createArray(store);
        }
    }

    @CoreMethod(names = "constants", optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "inherit", type = RubyNode.class)
    public abstract static class ConstantsNode extends CoreMethodNode {

        @CreateCast("inherit")
        protected RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray constants(RubyModule module, boolean inherit) {
            final List<RubySymbol> constantsArray = new ArrayList<>();

            final Iterable<Entry<String, RubyConstant>> constants;
            if (inherit) {
                constants = ModuleOperations.getAllConstants(module);
            } else {
                constants = module.fields.getConstants();
            }

            for (Entry<String, RubyConstant> constant : constants) {
                if (!constant.getValue().isPrivate()) {
                    constantsArray.add(getSymbol(constant.getKey()));
                }
            }

            return createArray(constantsArray.toArray());
        }

    }

    @CoreMethod(names = "const_defined?", required = 1, optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "inherit", type = RubyNode.class)
    public abstract static class ConstDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("inherit")
        protected RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        protected boolean isConstDefined(RubyModule module, String fullName, boolean inherit) {
            final ConstantLookupResult constant = ModuleOperations
                    .lookupScopedConstant(getContext(), module, fullName, inherit, this);
            return constant.isFound();
        }

    }

    @Primitive(name = "module_const_get")
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "inherit", type = RubyNode.class)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    public abstract static class ConstGetNode extends PrimitiveNode {

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, true, true);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @CreateCast("name")
        protected RubyNode coerceToSymbolOrString(RubyNode name) {
            // We want to know if the name is a Symbol, as then scoped lookup is not tried
            return ToStringOrSymbolNodeGen.create(name);
        }

        @CreateCast("inherit")
        protected RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        // Symbol

        @Specialization(guards = "inherit")
        protected Object getConstant(RubyModule module, RubySymbol name, boolean inherit) {
            return getConstant(module, name.getString());
        }

        @Specialization(guards = "!inherit")
        protected Object getConstantNoInherit(RubyModule module, RubySymbol name, boolean inherit) {
            return getConstantNoInherit(module, name.getString());
        }

        // String

        @Specialization(
                guards = { "inherit", "equalNode.execute(name.rope, cachedRope)", "!scoped" },
                limit = "getLimit()")
        protected Object getConstantStringCached(RubyModule module, RubyString name, boolean inherit,
                @Cached("privatizeRope(name)") Rope cachedRope,
                @Cached("name.getJavaString()") String cachedString,
                @Cached RopeNodes.EqualNode equalNode,
                @Cached("isScoped(cachedString)") boolean scoped) {
            return getConstant(module, cachedString);
        }

        @Specialization(
                guards = { "inherit", "!isScoped(name)" },
                replaces = "getConstantStringCached")
        protected Object getConstantString(RubyModule module, RubyString name, boolean inherit) {
            return getConstant(module, name.getJavaString());
        }

        @Specialization(guards = { "!inherit", "!isScoped(name)" })
        protected Object getConstantNoInheritString(RubyModule module, RubyString name, boolean inherit) {
            return getConstantNoInherit(module, name.getJavaString());
        }

        // Scoped String
        @Specialization(guards = { "isScoped(name)" })
        protected Object getConstantScoped(RubyModule module, RubyString name, boolean inherit) {
            return FAILURE;
        }

        private Object getConstant(RubyModule module, String name) {
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookupConstantNode);
        }

        private Object getConstantNoInherit(RubyModule module, String name) {
            final LookupConstantInterface lookup = this::lookupConstantNoInherit;
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookup);
        }

        @TruffleBoundary
        private RubyConstant lookupConstantNoInherit(LexicalScope lexicalScope, RubyModule module, String name) {
            return ModuleOperations
                    .lookupConstantWithInherit(getContext(), module, name, false, this)
                    .getConstant();
        }

        @TruffleBoundary
        boolean isScoped(RubyString name) {
            // TODO (eregon, 27 May 2015): Any way to make this efficient?
            return name.getJavaString().contains("::");
        }

        boolean isScoped(String name) {
            return name.contains("::");
        }

        protected int getLimit() {
            return getContext().getOptions().CONSTANT_CACHE;
        }

    }

    @CoreMethod(names = "const_missing", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ConstMissingNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected Object constMissing(RubyModule module, String name) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorUninitializedConstant(module, name, this));
        }

    }

    @CoreMethod(names = "const_set", required = 2)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class ConstSetNode extends CoreMethodNode {

        public static ConstSetNode create() {
            return ConstSetNodeFactory.create(null, null, null);
        }

        @Child private WarnAlreadyInitializedNode warnAlreadyInitializedNode;

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
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

            return setConstantNoCheckName(module, name, value);
        }

        @TruffleBoundary
        public Object setConstantNoCheckName(RubyModule module, String name, Object value) {
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

    @CoreMethod(
            names = "define_method",
            needsBlock = true,
            required = 1,
            optional = 1,
            split = Split.NEVER,
            argumentNames = { "name", "proc_or_method", "block" })
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "proc", type = RubyNode.class)
    @NodeChild(value = "block", type = RubyNode.class)
    public abstract static class DefineMethodNode extends CoreMethodNode {

        @Child private AddMethodNode addMethodNode = AddMethodNode.create(false);
        @Child private ReadCallerFrameNode readCallerFrame = ReadCallerFrameNode.create();

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @TruffleBoundary
        @Specialization
        protected RubySymbol defineMethod(RubyModule module, String name, NotProvided proc, NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("needs either proc or block", this));
        }

        @Specialization
        protected RubySymbol defineMethodBlock(
                VirtualFrame frame,
                RubyModule module,
                String name,
                NotProvided proc,
                RubyProc block) {
            return defineMethodProc(frame, module, name, block, NotProvided.INSTANCE);
        }

        @Specialization
        protected RubySymbol defineMethodProc(
                VirtualFrame frame,
                RubyModule module,
                String name,
                RubyProc proc,
                NotProvided block) {
            return defineMethod(module, name, proc, readCallerFrame.execute(frame));
        }

        @TruffleBoundary
        @Specialization
        protected RubySymbol defineMethodMethod(
                RubyModule module,
                String name,
                RubyMethod methodObject,
                NotProvided block,
                @Cached CanBindMethodToModuleNode canBindMethodToModuleNode) {
            final InternalMethod method = methodObject.method;

            if (!canBindMethodToModuleNode.executeCanBindMethodToModule(method, module)) {
                final RubyModule declaringModule = method.getDeclaringModule();
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

            module.fields.addMethod(getContext(), this, method.withName(name));
            return getSymbol(name);
        }

        @Specialization
        protected RubySymbol defineMethod(
                VirtualFrame frame,
                RubyModule module,
                String name,
                RubyUnboundMethod method,
                NotProvided block) {
            final MaterializedFrame callerFrame = readCallerFrame.execute(frame);
            return defineMethodInternal(module, name, method, callerFrame);
        }

        @TruffleBoundary
        private RubySymbol defineMethodInternal(RubyModule module, String name, RubyUnboundMethod method,
                final MaterializedFrame callerFrame) {
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

            return addMethod(module, name, internalMethod, callerFrame);
        }

        @TruffleBoundary
        private RubySymbol defineMethod(RubyModule module, String name, RubyProc proc,
                MaterializedFrame callerFrame) {
            final RubyRootNode rootNode = (RubyRootNode) proc.callTargetForLambdas.getRootNode();
            final SharedMethodInfo info = proc.sharedMethodInfo.forDefineMethod(module, name);

            final RubyNode body = NodeUtil.cloneNode(rootNode.getBody());
            final RubyNode newBody = new CallMethodWithProcBody(proc.declarationFrame, body);
            final RubyRootNode newRootNode = new RubyRootNode(
                    getContext(),
                    info.getSourceSection(),
                    rootNode.getFrameDescriptor(),
                    info,
                    newBody,
                    Split.HEURISTIC);
            final RootCallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

            final InternalMethod method = InternalMethod.fromProc(
                    getContext(),
                    info,
                    proc.declarationContext,
                    name,
                    module,
                    Visibility.PUBLIC,
                    proc,
                    newCallTarget);
            return addMethod(module, name, method, callerFrame);
        }

        private static class CallMethodWithProcBody extends RubyContextSourceNode {

            private final MaterializedFrame declarationFrame;
            @Child private RubyNode procBody;

            public CallMethodWithProcBody(MaterializedFrame declarationFrame, RubyNode procBody) {
                this.declarationFrame = declarationFrame;
                this.procBody = procBody;
            }

            @Override
            public Object execute(VirtualFrame frame) {
                RubyArguments.setDeclarationFrame(frame, declarationFrame);
                return procBody.execute(frame);
            }

        }

        @TruffleBoundary
        private RubySymbol addMethod(RubyModule module, String name, InternalMethod method,
                MaterializedFrame callerFrame) {
            method = method.withName(name);

            final Visibility visibility = GetCurrentVisibilityNode.getVisibilityFromNameAndFrame(name, callerFrame);
            addMethodNode.executeAddMethod(module, method, visibility);
            return getSymbol(method.getName());
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

    @CoreMethod(names = "initialize", needsBlock = true)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Child private ClassExecNode classExecNode;

        public abstract RubyModule executeInitialize(RubyModule module, Object block);

        void classEval(RubyModule module, RubyProc block) {
            if (classExecNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                classExecNode = insert(ClassExecNode.create());
            }
            classExecNode.executeClassExec(module, new Object[]{ module }, block);
        }

        @Specialization
        protected RubyModule initialize(RubyModule module, NotProvided block) {
            return module;
        }

        @Specialization
        protected RubyModule initialize(RubyModule module, RubyProc block) {
            classEval(module, block);
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
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "inherit", type = RubyNode.class)
    public abstract static class MethodDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @CreateCast("inherit")
        protected RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
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

    @CoreMethod(names = "module_function", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class ModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION);

        @Specialization
        protected RubyModule moduleFunction(VirtualFrame frame, RubyModule module, Object[] names,
                @Cached BranchProfile errorProfile) {
            if (module instanceof RubyClass) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("module_function must be called for modules", this));
            }

            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected Object name(RubyModule module,
                @Cached("createIdentityProfile()") ValueProfile fieldsProfile) {
            final ModuleFields fields = fieldsProfile.profile(module.fields);

            if (!fields.hasPartialName()) {
                return nil;
            }

            return makeStringNode.executeMake(fields.getName(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "nesting", onSingleton = true)
    public abstract static class NestingNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray nesting() {
            final List<RubyModule> modules = new ArrayList<>();

            InternalMethod method = getContext().getCallStack().getCallingMethodIgnoringSend();
            LexicalScope lexicalScope = method == null ? null : method.getSharedMethodInfo().getLexicalScope();
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

    @CoreMethod(names = "public", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class PublicNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.PUBLIC);

        public abstract RubyModule executePublic(VirtualFrame frame, RubyModule module, Object[] args);

        @Specialization
        protected RubyModule doPublic(VirtualFrame frame, RubyModule module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "public_class_method", rest = true)
    public abstract static class PublicClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNodeGen
                .create(Visibility.PUBLIC);

        @Specialization
        protected RubyModule publicClassMethod(VirtualFrame frame, RubyModule module, Object[] names) {
            final RubyClass singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.executeSetMethodVisibility(frame, singletonClass, name);
            }

            return module;
        }
    }

    @CoreMethod(names = "private", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class PrivateNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.PRIVATE);

        public abstract RubyModule executePrivate(VirtualFrame frame, RubyModule module, Object[] args);

        @Specialization
        protected RubyModule doPrivate(VirtualFrame frame, RubyModule module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "prepend_features", required = 1, visibility = Visibility.PRIVATE, split = Split.NEVER)
    public abstract static class PrependFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();

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
            taintResultNode.maybeTaint(features, target);
            return nil;
        }
    }

    @CoreMethod(names = "private_class_method", rest = true)
    public abstract static class PrivateClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNodeGen
                .create(Visibility.PRIVATE);

        @Specialization
        protected RubyModule privateClassMethod(VirtualFrame frame, RubyModule module, Object[] names) {
            final RubyClass singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.executeSetMethodVisibility(frame, singletonClass, name);
            }

            return module;
        }
    }

    @CoreMethod(names = "public_instance_method", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class PublicInstanceMethodNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected RubyUnboundMethod publicInstanceMethod(RubyModule module, String name,
                @Cached AllocateHelperNode allocateHelperNode,
                @Cached BranchProfile errorProfile,
                @CachedLanguage RubyLanguage language) {
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
                    RubyLanguage.unboundMethodShape,
                    module,
                    method);
            allocateHelperNode.trace(instance, this, language);
            return instance;
        }

    }

    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyNode.class)
    protected abstract static class AbstractInstanceMethodsNode extends CoreMethodNode {

        final Visibility visibility;

        public AbstractInstanceMethodsNode(Visibility visibility) {
            this.visibility = visibility;
        }

        @CreateCast("includeAncestors")
        protected RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @Specialization
        @TruffleBoundary
        protected RubyArray getInstanceMethods(RubyModule module, boolean includeAncestors) {
            Object[] objects = module.fields
                    .filterMethods(getContext(), includeAncestors, MethodFilter.by(visibility))
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
    @NodeChild(value = "name", type = RubyNode.class)
    protected abstract static class AbstractMethodDefinedNode extends CoreMethodNode {

        final Visibility visibility;

        public AbstractMethodDefinedNode(Visibility visibility) {
            this.visibility = visibility;
        }

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected boolean isMethodDefined(RubyModule module, String name) {
            // TODO (pitr-ch 30-Mar-2016): cache lookup
            return ModuleOperations.lookupMethod(module, name, visibility) != null;
        }

    }

    @CoreMethod(names = "public_method_defined?", required = 1)
    public abstract static class PublicMethodDefinedNode extends AbstractMethodDefinedNode {

        public PublicMethodDefinedNode() {
            super(Visibility.PUBLIC);
        }

    }

    @CoreMethod(names = "protected_method_defined?", required = 1)
    public abstract static class ProtectedMethodDefinedNode extends AbstractMethodDefinedNode {

        public ProtectedMethodDefinedNode() {
            super(Visibility.PROTECTED);
        }

    }

    @CoreMethod(names = "private_method_defined?", required = 1)
    public abstract static class PrivateMethodDefinedNode extends AbstractMethodDefinedNode {

        public PrivateMethodDefinedNode() {
            super(Visibility.PRIVATE);
        }

    }

    @CoreMethod(names = "instance_methods", optional = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyNode.class)
    public abstract static class InstanceMethodsNode extends CoreMethodNode {

        @CreateCast("includeAncestors")
        protected RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray instanceMethods(RubyModule module, boolean includeAncestors) {
            Object[] objects = module.fields
                    .filterMethods(getContext(), includeAncestors, MethodFilter.PUBLIC_PROTECTED)
                    .toArray();
            return createArray(objects);
        }
    }

    @CoreMethod(names = "instance_method", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class InstanceMethodNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected RubyUnboundMethod instanceMethod(RubyModule module, String name,
                @Cached AllocateHelperNode allocateHelperNode,
                @Cached BranchProfile errorProfile,
                @CachedLanguage RubyLanguage language) {
            // TODO(CS, 11-Jan-15) cache this lookup
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, null);

            if (method == null || method.isUndefined()) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorUndefinedMethod(name, module, this));
            }

            final RubyUnboundMethod instance = new RubyUnboundMethod(
                    coreLibrary().unboundMethodClass,
                    RubyLanguage.unboundMethodShape,
                    module,
                    method);
            allocateHelperNode.trace(instance, this, language);
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

    @CoreMethod(names = "protected", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class ProtectedNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.PROTECTED);

        @Specialization
        protected RubyModule doProtected(VirtualFrame frame, RubyModule module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "remove_class_variable", required = 1)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class RemoveClassVariableNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNode.create(name);
        }

        @TruffleBoundary
        @Specialization
        protected Object removeClassVariableString(RubyModule module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);
            return ModuleOperations.removeClassVariable(module.fields, getContext(), this, name);
        }

    }

    @CoreMethod(names = "remove_const", required = 1, visibility = Visibility.PRIVATE)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class RemoveConstNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
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

        @Child private DispatchNode callRbInspect;
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString toS(RubyModule module) {
            final String moduleName;
            final ModuleFields fields = module.fields;
            if (RubyGuards.isSingletonClass(module)) {
                final RubyDynamicObject attached = ((RubyClass) module).attached;
                final String attachedName;
                if (attached instanceof RubyModule) {
                    attachedName = ((RubyModule) attached).fields.getName();
                } else {
                    if (callRbInspect == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        callRbInspect = insert(DispatchNode.create());
                    }
                    final Object inspectResult = callRbInspect
                            .call(coreLibrary().truffleTypeModule, "rb_inspect", attached);
                    attachedName = ((RubyString) inspectResult).getJavaString();
                }
                moduleName = "#<Class:" + attachedName + ">";
            } else if (fields.isRefinement()) {
                final String refinedModule = fields.getRefinedModule().fields.getName();
                final String refinementNamespace = fields.getRefinementNamespace().fields.getName();
                moduleName = "#<refinement:" + refinedModule + "@" + refinementNamespace + ">";
            } else {
                moduleName = fields.getName();
            }

            return makeStringNode.executeMake(moduleName, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "undef_method", rest = true, split = Split.NEVER, argumentNames = "names")
    public abstract static class UndefMethodNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected RubyModule undefMethods(RubyModule module, Object[] names,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            for (Object name : names) {
                module.fields.undefMethod(getContext(), this, nameToJavaStringNode.execute(name));
            }
            return module;
        }

        /** Used only by undef keyword {@link org.truffleruby.parser.BodyTranslator#visitUndefNode} */
        @TruffleBoundary
        @Specialization
        protected RubyModule undefKeyword(RubyModule module, RubySymbol name) {
            module.fields.undefMethod(getContext(), this, name.getString());
            return module;
        }

    }

    @CoreMethod(names = "used_modules", onSingleton = true)
    public abstract static class UsedModulesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray usedModules() {
            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
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
            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
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

    public abstract static class SetVisibilityNode extends RubyContextNode {

        private final Visibility visibility;

        @Child private SetMethodVisibilityNode setMethodVisibilityNode;

        public SetVisibilityNode(Visibility visibility) {
            this.visibility = visibility;
            setMethodVisibilityNode = SetMethodVisibilityNodeGen.create(visibility);
        }

        public abstract RubyModule executeSetVisibility(VirtualFrame frame, RubyModule module,
                Object[] arguments);

        @Specialization
        protected RubyModule setVisibility(VirtualFrame frame, RubyModule module, Object[] names) {
            if (names.length == 0) {
                DeclarationContext.setCurrentVisibility(getContext(), visibility);
            } else {
                for (Object name : names) {
                    setMethodVisibilityNode.executeSetMethodVisibility(frame, module, name);
                }
            }

            return module;
        }

    }

    public abstract static class SetMethodVisibilityNode extends RubyContextNode {

        private final Visibility visibility;

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private AddMethodNode addMethodNode = AddMethodNode.create(true);

        public SetMethodVisibilityNode(Visibility visibility) {
            this.visibility = visibility;
        }

        public abstract void executeSetMethodVisibility(VirtualFrame frame, RubyModule module, Object name);

        @Specialization
        protected void setMethodVisibility(RubyModule module, Object name,
                @Cached BranchProfile errorProfile) {
            final String methodName = nameToJavaStringNode.execute(name);

            final InternalMethod method = module.fields.deepMethodSearch(getContext(), methodName);

            if (method == null) {
                errorProfile.enter();
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
            addMethodNode.executeAddMethod(module, method, visibility);
        }

    }

    @CoreMethod(names = "refine", needsBlock = true, required = 1, visibility = Visibility.PRIVATE)
    public abstract static class RefineNode extends CoreMethodArrayArgumentsNode {

        @Child private CallBlockNode callBlockNode = CallBlockNode.create();

        @Specialization
        protected RubyModule refine(RubyModule self, Object moduleToRefine, NotProvided block) {
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

            callBlockNode.executeCallBlock(
                    declarationContext,
                    block,
                    refinement,
                    block.block,
                    EMPTY_ARGUMENTS);
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

    @CoreMethod(names = "using", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class ModuleUsingNode extends CoreMethodArrayArgumentsNode {

        @Child private UsingNode usingNode = UsingNodeGen.create();

        @TruffleBoundary
        @Specialization
        protected RubyModule moduleUsing(RubyModule self, RubyModule refinementModule) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
            if (self != RubyArguments.getSelf(callerFrame)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Module#using is not called on self", this));
            }
            if (!isCalledFromClassOrModule(callerFrame)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Module#using is not permitted in methods", this));
            }
            usingNode.executeUsing(refinementModule);
            return self;
        }

        @TruffleBoundary
        private boolean isCalledFromClassOrModule(Frame callerFrame) {
            final String name = RubyArguments.getMethod(callerFrame).getSharedMethodInfo().getName();
            // Handles cases: <main> | <top | <class: | <module: | <singleton
            return name.startsWith("<");
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
