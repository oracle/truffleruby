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

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.cast.ToPathNodeGen;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStringOrSymbolNodeGen;
import org.truffleruby.core.constant.WarnAlreadyInitializedNode;
import org.truffleruby.core.method.MethodFilter;
import org.truffleruby.core.module.ModuleNodesFactory.ClassExecNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.ConstSetNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.GenerateAccessorNodeGen;
import org.truffleruby.core.module.ModuleNodesFactory.IsSubclassOfOrEqualToNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.SetMethodVisibilityNodeGen;
import org.truffleruby.core.module.ModuleNodesFactory.SetVisibilityNodeGen;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
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
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.WarningNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
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
import org.truffleruby.language.methods.UsingNode;
import org.truffleruby.language.methods.UsingNodeGen;
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
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Module", isClass = true)
public abstract class ModuleNodes {

    @TruffleBoundary
    public static DynamicObject createModule(RubyContext context, SourceSection sourceSection, DynamicObject selfClass,
            DynamicObject lexicalParent, String name, Node currentNode) {
        final ModuleFields fields = new ModuleFields(context, sourceSection, lexicalParent, name);
        final DynamicObject module = Layouts.MODULE.createModule(Layouts.CLASS.getInstanceFactory(selfClass), fields);
        fields.rubyModuleObject = module;

        if (lexicalParent != null) {
            fields.getAdoptedByLexicalParent(context, lexicalParent, name, currentNode);
        } else if (fields.givenBaseName != null) { // bootstrap module
            fields.setFullName(fields.givenBaseName);
        }
        return module;
    }

    @CoreMethod(names = "===", required = 1)
    public abstract static class ContainsInstanceNode extends CoreMethodArrayArgumentsNode {

        @Child private IsANode isANode = IsANode.create();

        @Specialization
        protected boolean containsInstance(DynamicObject module, Object instance) {
            return isANode.executeIsA(instance, module);
        }

    }

    @CoreMethod(names = "<", required = 1)
    public abstract static class IsSubclassOfNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSubclassOf(VirtualFrame frame, DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        protected Object isSubclassOf(DynamicObject self, DynamicObject other) {
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
        protected Object isSubclassOfOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = "<=", required = 1)
    public abstract static class IsSubclassOfOrEqualToNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSubclassOfOrEqualTo(DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        protected Object isSubclassOfOrEqualTo(DynamicObject self, DynamicObject other) {
            if (self == other || ModuleOperations.includesModule(self, other)) {
                return true;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return false;
            }

            return nil;
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object isSubclassOfOrEqualToOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = ">", required = 1)
    public abstract static class IsSuperclassOfNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSuperclassOf(VirtualFrame frame, DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        protected Object isSuperclassOf(DynamicObject self, DynamicObject other) {
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
        protected Object isSuperclassOfOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = ">=", required = 1)
    public abstract static class IsSuperclassOfOrEqualToNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSuperclassOfOrEqualTo(VirtualFrame frame, DynamicObject self,
                DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        protected Object isSuperclassOfOrEqualTo(DynamicObject self, DynamicObject other) {
            if (self == other || ModuleOperations.includesModule(other, self)) {
                return true;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return false;
            }

            return nil;
        }

        @Specialization(guards = "!isRubyModule(other)")
        protected Object isSuperclassOfOrEqualToOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Child private IsSubclassOfOrEqualToNode subclassNode;

        private Object isSubclass(DynamicObject self, DynamicObject other) {
            if (subclassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                subclassNode = insert(IsSubclassOfOrEqualToNodeFactory.create(null));
            }
            return subclassNode.executeIsSubclassOfOrEqualTo(self, other);
        }

        @Specialization(guards = "isRubyModule(other)")
        protected Object compare(DynamicObject self, DynamicObject other) {
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
        protected Object compareOther(DynamicObject self, DynamicObject other) {
            return nil;
        }

    }

    @CoreMethod(names = "alias_method", required = 2, raiseIfFrozenSelf = true, neverSplit = true)
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
        protected DynamicObject aliasMethod(DynamicObject module, String newName, String oldName,
                @Cached BranchProfile errorProfile) {
            final InternalMethod method = Layouts.MODULE
                    .getFields(module)
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
        protected DynamicObject ancestors(DynamicObject self) {
            final List<DynamicObject> ancestors = new ArrayList<>();
            for (DynamicObject module : Layouts.MODULE.getFields(self).ancestors()) {
                ancestors.add(module);
            }

            Object[] objects = ancestors.toArray(new Object[ancestors.size()]);
            return createArray(objects, objects.length);
        }
    }

    @CoreMethod(names = "append_features", required = 1, visibility = Visibility.PRIVATE, neverSplit = true)
    public abstract static class AppendFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();

        @Specialization(guards = "isRubyModule(target)")
        protected Object appendFeatures(DynamicObject features, DynamicObject target,
                @Cached BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(features)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("append_features must be called only on modules", this));
            }
            Layouts.MODULE.getFields(target).include(getContext(), this, features);
            taintResultNode.maybeTaint(features, target);
            return nil;
        }
    }

    public abstract static class GenerateAccessorNode extends RubyContextNode {

        final boolean isGetter;

        public GenerateAccessorNode(boolean isGetter) {
            this.isGetter = isGetter;
        }

        public abstract Object executeGenerateAccessor(VirtualFrame frame, DynamicObject module, Object name);

        @Specialization
        protected Object generateAccessor(VirtualFrame frame, DynamicObject module, Object nameObject,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached ReadCallerFrameNode readCallerFrame) {
            final String name = nameToJavaStringNode.executeToJavaString(nameObject);
            createAccessor(module, name, readCallerFrame.execute(frame));
            return nil;
        }

        @TruffleBoundary
        private void createAccessor(DynamicObject module, String name, MaterializedFrame callerFrame) {
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
                    null,
                    false);

            final RubyNode self = ProfileArgumentNodeGen.create(new ReadSelfNode());
            final RubyNode accessInstanceVariable;
            if (isGetter) {
                accessInstanceVariable = new ReadInstanceVariableNode(ivar, self);
            } else {
                RubyNode readArgument = ProfileArgumentNodeGen
                        .create(new ReadPreArgumentNode(0, MissingArgumentBehavior.RUNTIME_ERROR));
                accessInstanceVariable = new WriteInstanceVariableNode(ivar, self, readArgument);
            }

            final RubyNode body = Translator
                    .createCheckArityNode(getContext().getLanguage(), arity, accessInstanceVariable);
            final RubyRootNode rootNode = new RubyRootNode(
                    getContext(),
                    sourceSection,
                    null,
                    sharedMethodInfo,
                    body,
                    true);
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

            Layouts.MODULE.getFields(module).addMethod(getContext(), this, method);
        }
    }

    @CoreMethod(names = "attr", rest = true)
    public abstract static class AttrNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = GenerateAccessorNodeGen.create(true);
        @Child private GenerateAccessorNode generateSetterNode = GenerateAccessorNodeGen.create(false);
        @Child private WarningNode warnNode;

        @Specialization
        protected Object attr(VirtualFrame frame, DynamicObject module, Object[] names) {
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
        protected Object attrAccessor(VirtualFrame frame, DynamicObject module, Object[] names) {
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
        protected Object attrReader(VirtualFrame frame, DynamicObject module, Object[] names) {
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
        protected Object attrWriter(VirtualFrame frame, DynamicObject module, Object[] names) {
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
        @Specialization(guards = "isRubyString(filename)")
        protected Object autoload(DynamicObject module, String name, DynamicObject filename) {
            if (!Identifiers.isValidConstantName(name)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameError(
                                StringUtils.format("autoload must be constant name: %s", name),
                                module,
                                name,
                                this));
            }

            if (StringOperations.rope(filename).isEmpty()) {
                throw new RaiseException(getContext(), coreExceptions().argumentError("empty file name", this));
            }

            final RubyConstant constant = Layouts.MODULE.getFields(module).getConstant(name);
            if (constant == null || !constant.hasValue()) {
                Layouts.MODULE.getFields(module).setAutoloadConstant(getContext(), this, name, filename);
            }

            return nil;
        }
    }

    @CoreMethod(names = "autoload?", required = 1)
    public abstract static class IsAutoloadNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object isAutoloadSymbol(DynamicObject module, RubySymbol name) {
            return isAutoload(module, name.getString());
        }

        @Specialization(guards = "isRubyString(name)")
        protected Object isAutoloadString(DynamicObject module, DynamicObject name) {
            return isAutoload(module, StringOperations.getString(name));
        }

        private Object isAutoload(DynamicObject module, String name) {
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

        protected DynamicObject toStr(VirtualFrame frame, Object object) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNode.create());
            }
            return toStrNode.executeToStr(frame, object);
        }

        @Specialization(guards = "isRubyString(code)")
        protected Object classEval(
                VirtualFrame frame,
                DynamicObject module,
                DynamicObject code,
                NotProvided file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, code, "(eval)", callNode);
        }

        @Specialization(guards = { "isRubyString(code)", "isRubyString(file)" })
        protected Object classEval(
                VirtualFrame frame,
                DynamicObject module,
                DynamicObject code,
                DynamicObject file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, code, StringOperations.getString(file), callNode);
        }

        @Specialization(guards = { "isRubyString(code)", "isRubyString(file)" })
        protected Object classEval(
                VirtualFrame frame,
                DynamicObject module,
                DynamicObject code,
                DynamicObject file,
                int line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = classEvalSource(
                    frame,
                    module,
                    code,
                    StringOperations.getString(file),
                    line);
            return deferredCall.call(callNode);
        }

        @Specialization(guards = "wasProvided(code)")
        protected Object classEval(
                VirtualFrame frame,
                DynamicObject module,
                Object code,
                NotProvided file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, toStr(frame, code), "(eval)", callNode);
        }

        @Specialization(guards = { "isRubyString(code)", "wasProvided(file)" })
        protected Object classEval(
                VirtualFrame frame,
                DynamicObject module,
                DynamicObject code,
                Object file,
                NotProvided line,
                NotProvided block,
                @Cached IndirectCallNode callNode) {
            return classEvalSource(frame, module, code, StringOperations.getString(toStr(frame, file)), callNode);
        }

        private Object classEvalSource(VirtualFrame frame, DynamicObject module, DynamicObject code, String file,
                @Cached IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = classEvalSource(frame, module, code, file, 1);
            return deferredCall.call(callNode);
        }

        private CodeLoader.DeferredCall classEvalSource(VirtualFrame frame, DynamicObject module,
                DynamicObject rubySource, String file, int line) {
            assert RubyGuards.isRubyString(rubySource);

            final MaterializedFrame callerFrame = readCallerFrameNode.execute(frame);

            return classEvalSourceInternal(module, rubySource, file, line, callerFrame);
        }

        @TruffleBoundary
        private CodeLoader.DeferredCall classEvalSourceInternal(DynamicObject module, DynamicObject rubySource,
                String file, int line, MaterializedFrame callerFrame) {
            final RubySource source = createEvalSourceNode
                    .createEvalSource(StringOperations.rope(rubySource), "class/module_eval", file, line);

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
                DynamicObject self,
                NotProvided code,
                NotProvided file,
                NotProvided line,
                DynamicObject block,
                @Cached ClassExecNode classExecNode) {
            return classExecNode.executeClassExec(self, new Object[]{ self }, block);
        }

        @Specialization
        protected Object classEval(
                DynamicObject self,
                NotProvided code,
                NotProvided file,
                NotProvided line,
                NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(0, 1, 2, this));
        }

        @Specialization(guards = "wasProvided(code)")
        protected Object classEval(
                DynamicObject self,
                Object code,
                NotProvided file,
                NotProvided line,
                DynamicObject block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(1, 0, this));
        }

    }

    @CoreMethod(names = { "class_exec", "module_exec" }, rest = true, needsBlock = true)
    public abstract static class ClassExecNode extends CoreMethodArrayArgumentsNode {

        public static ClassExecNode create() {
            return ClassExecNodeFactory.create(null);
        }

        @Child private CallBlockNode callBlockNode = CallBlockNode.create();

        public abstract Object executeClassExec(DynamicObject self, Object[] args, Object block);

        @Specialization
        protected Object classExec(DynamicObject self, Object[] args, DynamicObject block) {
            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new FixedDefaultDefinee(self),
                    Layouts.PROC.getDeclarationContext(block).getRefinements());

            return callBlockNode.executeCallBlock(declarationContext, block, self, Layouts.PROC.getBlock(block), args);
        }

        @Specialization
        protected Object classExec(DynamicObject self, Object[] args, NotProvided block) {
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

        @TruffleBoundary(transferToInterpreterOnException = false)
        @Specialization
        protected boolean isClassVariableDefinedString(DynamicObject module, String name) {
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
        @TruffleBoundary(transferToInterpreterOnException = false)
        protected Object getClassVariable(DynamicObject module, String name) {
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
        @TruffleBoundary(transferToInterpreterOnException = false)
        protected Object setClassVariable(DynamicObject module, String name, Object value) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            ModuleOperations.setClassVariable(getContext(), module, name, value, this);

            return value;
        }

    }

    @CoreMethod(names = "class_variables")
    public abstract static class ClassVariablesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject getClassVariables(DynamicObject module) {
            final Map<String, Object> allClassVariables = ModuleOperations.getAllClassVariables(module);
            final int size = allClassVariables.size();
            final Object[] store = new Object[size];

            int i = 0;
            for (String variable : allClassVariables.keySet()) {
                store[i++] = getSymbol(variable);
            }
            return createArray(store, size);
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
        protected DynamicObject constants(DynamicObject module, boolean inherit) {
            final List<RubySymbol> constantsArray = new ArrayList<>();

            final Iterable<Entry<String, RubyConstant>> constants;
            if (inherit) {
                constants = ModuleOperations.getAllConstants(module);
            } else {
                constants = Layouts.MODULE.getFields(module).getConstants();
            }

            for (Entry<String, RubyConstant> constant : constants) {
                if (!constant.getValue().isPrivate()) {
                    constantsArray.add(getSymbol(constant.getKey()));
                }
            }

            Object[] objects = constantsArray.toArray(new Object[constantsArray.size()]);
            return createArray(objects, objects.length);
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
        protected boolean isConstDefined(DynamicObject module, String fullName, boolean inherit) {
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
        protected Object getConstant(DynamicObject module, RubySymbol name, boolean inherit) {
            return getConstant(module, name.getString());
        }

        @Specialization(guards = "!inherit")
        protected Object getConstantNoInherit(DynamicObject module, RubySymbol name, boolean inherit) {
            return getConstantNoInherit(module, name.getString());
        }

        // String

        @Specialization(
                guards = { "inherit", "isRubyString(name)", "equalNode.execute(rope(name), cachedRope)", "!scoped" },
                limit = "getLimit()")
        protected Object getConstantStringCached(DynamicObject module, DynamicObject name, boolean inherit,
                @Cached("privatizeRope(name)") Rope cachedRope,
                @Cached("getString(name)") String cachedString,
                @Cached RopeNodes.EqualNode equalNode,
                @Cached("isScoped(cachedString)") boolean scoped) {
            return getConstant(module, cachedString);
        }

        @Specialization(
                guards = { "inherit", "isRubyString(name)", "!isScoped(name)" },
                replaces = "getConstantStringCached")
        protected Object getConstantString(DynamicObject module, DynamicObject name, boolean inherit) {
            return getConstant(module, StringOperations.getString(name));
        }

        @Specialization(guards = { "!inherit", "isRubyString(name)", "!isScoped(name)" })
        protected Object getConstantNoInheritString(DynamicObject module, DynamicObject name, boolean inherit) {
            return getConstantNoInherit(module, StringOperations.getString(name));
        }

        // Scoped String
        @Specialization(guards = { "isRubyString(name)", "isScoped(name)" })
        protected Object getConstantScoped(DynamicObject module, DynamicObject name, boolean inherit) {
            return FAILURE;
        }

        private Object getConstant(DynamicObject module, String name) {
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookupConstantNode);
        }

        private Object getConstantNoInherit(DynamicObject module, String name) {
            final LookupConstantInterface lookup = this::lookupConstantNoInherit;
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookup);
        }

        private RubyConstant lookupConstantNoInherit(LexicalScope lexicalScope, Object module, String name) {
            return ModuleOperations
                    .lookupConstantWithInherit(getContext(), (DynamicObject) module, name, false, this)
                    .getConstant();
        }

        @TruffleBoundary
        boolean isScoped(DynamicObject name) {
            // TODO (eregon, 27 May 2015): Any way to make this efficient?
            return StringOperations.getString(name).contains("::");
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
        protected Object constMissing(DynamicObject module, String name) {
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
        protected Object setConstant(DynamicObject module, String name, Object value) {
            if (!Identifiers.isValidConstantName(name)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions()
                                .nameError(StringUtils.format("wrong constant name %s", name), module, name, this));
            }

            return setConstantNoCheckName(module, name, value);
        }

        @TruffleBoundary
        public Object setConstantNoCheckName(DynamicObject module, String name, Object value) {
            final RubyConstant previous = Layouts.MODULE.getFields(module).setConstant(getContext(), this, name, value);
            if (previous != null && previous.hasValue()) {
                warnAlreadyInitializedConstant(module, name, previous.getSourceSection());
            }
            return value;
        }

        private void warnAlreadyInitializedConstant(DynamicObject module, String name,
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
            neverSplit = true,
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
        protected DynamicObject defineMethod(DynamicObject module, String name, NotProvided proc, NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("needs either proc or block", this));
        }

        @Specialization
        protected RubySymbol defineMethodBlock(
                VirtualFrame frame,
                DynamicObject module,
                String name,
                NotProvided proc,
                DynamicObject block) {
            return defineMethodProc(frame, module, name, block, NotProvided.INSTANCE);
        }

        @Specialization(guards = "isRubyProc(proc)")
        protected RubySymbol defineMethodProc(
                VirtualFrame frame,
                DynamicObject module,
                String name,
                DynamicObject proc,
                NotProvided block) {
            return defineMethod(module, name, proc, readCallerFrame.execute(frame));
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyMethod(methodObject)")
        protected RubySymbol defineMethodMethod(
                DynamicObject module,
                String name,
                DynamicObject methodObject,
                NotProvided block,
                @Cached CanBindMethodToModuleNode canBindMethodToModuleNode) {
            final InternalMethod method = Layouts.METHOD.getMethod(methodObject);

            if (!canBindMethodToModuleNode.executeCanBindMethodToModule(method, module)) {
                final DynamicObject declaringModule = method.getDeclaringModule();
                if (RubyGuards.isSingletonClass(declaringModule)) {
                    throw new RaiseException(getContext(), coreExceptions().typeError(
                            "can't bind singleton method to a different class",
                            this));
                } else {
                    throw new RaiseException(getContext(), coreExceptions().typeError(
                            "class must be a subclass of " + Layouts.MODULE.getFields(declaringModule).getName(),
                            this));
                }
            }

            Layouts.MODULE.getFields(module).addMethod(getContext(), this, method.withName(name));
            return getSymbol(name);
        }

        @Specialization(guards = "isRubyUnboundMethod(method)")
        protected RubySymbol defineMethod(
                VirtualFrame frame,
                DynamicObject module,
                String name,
                DynamicObject method,
                NotProvided block) {
            final MaterializedFrame callerFrame = readCallerFrame.execute(frame);
            return defineMethodInternal(module, name, method, callerFrame);
        }

        @TruffleBoundary
        private RubySymbol defineMethodInternal(DynamicObject module, String name, DynamicObject method,
                final MaterializedFrame callerFrame) {
            final InternalMethod internalMethod = Layouts.UNBOUND_METHOD.getMethod(method);
            if (!ModuleOperations.canBindMethodTo(internalMethod, module)) {
                final DynamicObject declaringModule = internalMethod.getDeclaringModule();
                if (RubyGuards.isSingletonClass(declaringModule)) {
                    throw new RaiseException(getContext(), coreExceptions().typeError(
                            "can't bind singleton method to a different class",
                            this));
                } else {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().typeError(
                                    "bind argument must be a subclass of " +
                                            Layouts.MODULE.getFields(declaringModule).getName(),
                                    this));
                }
            }

            return addMethod(module, name, internalMethod, callerFrame);
        }

        @TruffleBoundary
        private RubySymbol defineMethod(DynamicObject module, String name, DynamicObject proc,
                MaterializedFrame callerFrame) {
            final RootCallTarget callTarget = Layouts.PROC.getCallTargetForLambdas(proc);
            final RubyRootNode rootNode = (RubyRootNode) callTarget.getRootNode();
            final SharedMethodInfo info = Layouts.PROC.getSharedMethodInfo(proc).forDefineMethod(module, name);
            final MaterializedFrame declarationFrame = Layouts.PROC.getDeclarationFrame(proc);

            final RubyNode body = NodeUtil.cloneNode(rootNode.getBody());
            final RubyNode newBody = new CallMethodWithProcBody(declarationFrame, body);
            final RubyRootNode newRootNode = new RubyRootNode(
                    getContext(),
                    info.getSourceSection(),
                    rootNode.getFrameDescriptor(),
                    info,
                    newBody,
                    true);
            final RootCallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

            final DeclarationContext declarationContext = Layouts.PROC.getDeclarationContext(proc);
            final InternalMethod method = InternalMethod.fromProc(
                    getContext(),
                    info,
                    declarationContext,
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
        private RubySymbol addMethod(DynamicObject module, String name, InternalMethod method,
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
        protected DynamicObject extendObject(DynamicObject module, DynamicObject object,
                @Cached BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(module)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorWrongArgumentType(module, "Module", this));
            }

            Layouts.MODULE
                    .getFields(singletonClassNode.executeSingletonClass(object))
                    .include(getContext(), this, module);
            return module;
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Child private ClassExecNode classExecNode;

        public abstract DynamicObject executeInitialize(DynamicObject module, Object block);

        void classEval(DynamicObject module, DynamicObject block) {
            if (classExecNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                classExecNode = insert(ClassExecNode.create());
            }
            classExecNode.executeClassExec(module, new Object[]{ module }, block);
        }

        @Specialization
        protected DynamicObject initialize(DynamicObject module, NotProvided block) {
            return module;
        }

        @Specialization
        protected DynamicObject initialize(DynamicObject module, DynamicObject block) {
            classEval(module, block);
            return module;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode;

        @Specialization(guards = { "!isRubyClass(self)", "isRubyModule(from)", "!isRubyClass(from)" })
        protected Object initializeCopyModule(DynamicObject self, DynamicObject from) {
            Layouts.MODULE.getFields(self).initCopy(from);

            final DynamicObject selfMetaClass = getSingletonClass(self);
            final DynamicObject fromMetaClass = getSingletonClass(from);
            Layouts.MODULE.getFields(selfMetaClass).initCopy(fromMetaClass);

            return nil;
        }

        @Specialization(guards = { "isRubyClass(self)", "isRubyClass(from)" })
        protected Object initializeCopyClass(DynamicObject self, DynamicObject from,
                @Cached BranchProfile errorProfile) {
            if (from == coreLibrary().basicObjectClass) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeError("can't copy the root class", this));
            } else if (Layouts.CLASS.getIsSingleton(from)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeError("can't copy singleton class", this));
            }

            Layouts.MODULE.getFields(self).initCopy(from);

            final DynamicObject selfMetaClass = getSingletonClass(self);
            final DynamicObject fromMetaClass = Layouts.BASIC_OBJECT.getMetaClass(from);

            assert Layouts.CLASS.getIsSingleton(fromMetaClass);
            assert Layouts.CLASS.getIsSingleton(Layouts.BASIC_OBJECT.getMetaClass(self));

            Layouts.MODULE.getFields(selfMetaClass).initCopy(fromMetaClass); // copy class methods

            return nil;
        }

        protected DynamicObject getSingletonClass(DynamicObject object) {
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
        protected DynamicObject includedModules(DynamicObject module) {
            final List<DynamicObject> modules = new ArrayList<>();

            for (DynamicObject included : Layouts.MODULE.getFields(module).ancestors()) {
                if (!RubyGuards.isRubyClass(included) && included != module) {
                    modules.add(included);
                }
            }

            Object[] objects = modules.toArray(new Object[modules.size()]);
            return createArray(objects, objects.length);
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
        protected boolean isMethodDefined(DynamicObject module, String name, boolean inherit) {
            final InternalMethod method;
            if (inherit) {
                method = ModuleOperations.lookupMethodUncached(module, name, null);
            } else {
                method = Layouts.MODULE.getFields(module).getMethod(name);
            }

            return method != null && !method.isUndefined() && !method.getVisibility().isPrivate();
        }

    }

    @CoreMethod(names = "module_function", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class ModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION);

        @Specialization
        protected DynamicObject moduleFunction(VirtualFrame frame, DynamicObject module, Object[] names,
                @Cached BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(module)) {
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
        protected Object name(DynamicObject module,
                @Cached("createIdentityProfile()") ValueProfile fieldsProfile) {
            final ModuleFields fields = fieldsProfile.profile(Layouts.MODULE.getFields(module));

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
        protected DynamicObject nesting() {
            final List<DynamicObject> modules = new ArrayList<>();

            InternalMethod method = getContext().getCallStack().getCallingMethodIgnoringSend();
            LexicalScope lexicalScope = method == null ? null : method.getSharedMethodInfo().getLexicalScope();
            DynamicObject object = coreLibrary().objectClass;

            while (lexicalScope != null) {
                final DynamicObject enclosing = lexicalScope.getLiveModule();
                if (enclosing == object) {
                    break;
                }
                modules.add(enclosing);
                lexicalScope = lexicalScope.getParent();
            }

            Object[] objects = modules.toArray(new Object[modules.size()]);
            return createArray(objects, objects.length);
        }
    }

    @CoreMethod(names = "public", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class PublicNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.PUBLIC);

        public abstract DynamicObject executePublic(VirtualFrame frame, DynamicObject module, Object[] args);

        @Specialization
        protected DynamicObject doPublic(VirtualFrame frame, DynamicObject module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "public_class_method", rest = true)
    public abstract static class PublicClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNodeGen
                .create(Visibility.PUBLIC);

        @Specialization
        protected DynamicObject publicClassMethod(VirtualFrame frame, DynamicObject module, Object[] names) {
            final DynamicObject singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.executeSetMethodVisibility(frame, singletonClass, name);
            }

            return module;
        }
    }

    @CoreMethod(names = "private", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class PrivateNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.PRIVATE);

        public abstract DynamicObject executePrivate(VirtualFrame frame, DynamicObject module, Object[] args);

        @Specialization
        protected DynamicObject doPrivate(VirtualFrame frame, DynamicObject module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "prepend_features", required = 1, visibility = Visibility.PRIVATE, neverSplit = true)
    public abstract static class PrependFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();

        @Specialization(guards = "isRubyModule(target)")
        protected Object prependFeatures(DynamicObject features, DynamicObject target,
                @Cached BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(features)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("prepend_features must be called only on modules", this));
            }
            Layouts.MODULE.getFields(target).prepend(getContext(), this, features);
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
        protected DynamicObject privateClassMethod(VirtualFrame frame, DynamicObject module, Object[] names) {
            final DynamicObject singletonClass = singletonClassNode.executeSingletonClass(module);

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
        protected DynamicObject publicInstanceMethod(DynamicObject module, String name,
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

            return Layouts.UNBOUND_METHOD.createUnboundMethod(coreLibrary().unboundMethodFactory, module, method);
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
        protected DynamicObject getInstanceMethods(DynamicObject module, boolean includeAncestors) {
            Object[] objects = Layouts.MODULE
                    .getFields(module)
                    .filterMethods(getContext(), includeAncestors, MethodFilter.by(visibility))
                    .toArray();
            return createArray(objects, objects.length);
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
        protected boolean isMethodDefined(DynamicObject module, String name) {
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
        protected DynamicObject instanceMethods(DynamicObject module, boolean includeAncestors) {
            Object[] objects = Layouts.MODULE
                    .getFields(module)
                    .filterMethods(getContext(), includeAncestors, MethodFilter.PUBLIC_PROTECTED)
                    .toArray();
            return createArray(objects, objects.length);
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
        protected DynamicObject instanceMethod(DynamicObject module, String name,
                @Cached BranchProfile errorProfile) {
            // TODO(CS, 11-Jan-15) cache this lookup
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, null);

            if (method == null || method.isUndefined()) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorUndefinedMethod(name, module, this));
            }

            return Layouts.UNBOUND_METHOD.createUnboundMethod(coreLibrary().unboundMethodFactory, module, method);
        }

    }

    @CoreMethod(names = "private_constant", rest = true)
    public abstract static class PrivateConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        protected DynamicObject privateConstant(DynamicObject module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.executeToJavaString(arg);
                Layouts.MODULE.getFields(module).changeConstantVisibility(getContext(), this, name, true);
            }
            return module;
        }
    }

    @CoreMethod(names = "deprecate_constant", rest = true, raiseIfFrozenSelf = true)
    public abstract static class DeprecateConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        protected DynamicObject deprecateConstant(DynamicObject module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.executeToJavaString(arg);
                Layouts.MODULE.getFields(module).deprecateConstant(getContext(), this, name);
            }
            return module;
        }
    }

    @CoreMethod(names = "public_constant", rest = true)
    public abstract static class PublicConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        protected DynamicObject publicConstant(DynamicObject module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.executeToJavaString(arg);
                Layouts.MODULE.getFields(module).changeConstantVisibility(getContext(), this, name, false);
            }
            return module;
        }
    }

    @CoreMethod(names = "protected", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class ProtectedNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.PROTECTED);

        @Specialization
        protected DynamicObject doProtected(VirtualFrame frame, DynamicObject module, Object[] names) {
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

        @TruffleBoundary(transferToInterpreterOnException = false)
        @Specialization
        protected Object removeClassVariableString(DynamicObject module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);
            return ModuleOperations.removeClassVariable(Layouts.MODULE.getFields(module), getContext(), this, name);
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
        protected Object removeConstant(DynamicObject module, String name) {
            final RubyConstant oldConstant = Layouts.MODULE.getFields(module).removeConstant(getContext(), this, name);
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
        @Child private CallDispatchHeadNode methodRemovedNode = CallDispatchHeadNode.createPrivate();

        @Specialization
        protected DynamicObject removeMethods(VirtualFrame frame, DynamicObject module, Object[] names) {
            for (Object name : names) {
                removeMethod(frame, module, nameToJavaStringNode.executeToJavaString(name));
            }
            return module;
        }

        private void removeMethod(VirtualFrame frame, DynamicObject module, String name) {
            raiseIfFrozenNode.execute(module);

            if (Layouts.MODULE.getFields(module).removeMethod(name)) {
                if (RubyGuards.isSingletonClass(module)) {
                    final DynamicObject receiver = Layouts.CLASS.getAttached(module);
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

        @Child private CallDispatchHeadNode callRbInspect;
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject toS(DynamicObject module) {
            final String moduleName;
            final ModuleFields fields = Layouts.MODULE.getFields(module);
            if (RubyGuards.isSingletonClass(module)) {
                final DynamicObject attached = Layouts.CLASS.getAttached(module);
                final String attachedName;
                if (Layouts.MODULE.isModule(attached)) {
                    attachedName = Layouts.MODULE.getFields(attached).getName();
                } else {
                    if (callRbInspect == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        callRbInspect = insert(CallDispatchHeadNode.createPrivate());
                    }
                    final Object inspectResult = callRbInspect
                            .call(coreLibrary().truffleTypeModule, "rb_inspect", attached);
                    attachedName = StringOperations.getString((DynamicObject) inspectResult);
                }
                moduleName = "#<Class:" + attachedName + ">";
            } else if (fields.isRefinement()) {
                final String refinedModule = Layouts.MODULE.getFields(fields.getRefinedModule()).getName();
                final String refinementNamespace = Layouts.MODULE.getFields(fields.getRefinementNamespace()).getName();
                moduleName = "#<refinement:" + refinedModule + "@" + refinementNamespace + ">";
            } else {
                moduleName = fields.getName();
            }

            return makeStringNode.executeMake(moduleName, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "undef_method", rest = true, neverSplit = true, argumentNames = "names")
    public abstract static class UndefMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private TypeNodes.CheckFrozenNode raiseIfFrozenNode = TypeNodes.CheckFrozenNode
                .create(ProfileArgumentNodeGen.create(new ReadSelfNode()));
        @Child private CallDispatchHeadNode methodUndefinedNode = CallDispatchHeadNode.createPrivate();

        @Specialization
        protected DynamicObject undefMethods(VirtualFrame frame, DynamicObject module, Object[] names) {
            for (Object name : names) {
                undefMethod(frame, module, nameToJavaStringNode.executeToJavaString(name));
            }
            return module;
        }

        /** Used only by undef keyword {@link org.truffleruby.parser.BodyTranslator#visitUndefNode} */
        @Specialization
        protected DynamicObject undefKeyword(VirtualFrame frame, DynamicObject module, RubySymbol name) {
            undefMethod(frame, module, name.getString());
            return module;
        }

        private void undefMethod(VirtualFrame frame, DynamicObject module, String name) {
            raiseIfFrozenNode.execute(frame);

            Layouts.MODULE.getFields(module).undefMethod(getContext(), this, name);
            if (RubyGuards.isSingletonClass(module)) {
                final DynamicObject receiver = Layouts.CLASS.getAttached(module);
                methodUndefinedNode.call(receiver, "singleton_method_undefined", getSymbol(name));
            } else {
                methodUndefinedNode.call(module, "method_undefined", getSymbol(name));
            }
        }

    }

    @CoreMethod(names = "used_modules", onSingleton = true)
    public abstract static class UsedModulesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject usedModules() {
            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
            final Set<DynamicObject> refinementNamespaces = new HashSet<>();
            for (DynamicObject refinementModules[] : declarationContext.getRefinements().values()) {
                for (DynamicObject refinementModule : refinementModules) {
                    refinementNamespaces.add(Layouts.MODULE.getFields(refinementModule).getRefinementNamespace());
                }
            }
            final Object[] refinements = refinementNamespaces.toArray(new Object[refinementNamespaces.size()]);
            return createArray(refinements, refinements.length);
        }

    }

    @NonStandard
    @CoreMethod(names = "used_refinements", onSingleton = true)
    public abstract static class UsedRefinementsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject usedRefinements() {
            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
            final Set<DynamicObject> refinements = new HashSet<>();
            for (DynamicObject refinementModules[] : declarationContext.getRefinements().values()) {
                for (DynamicObject refinementModule : refinementModules) {
                    refinements.add(refinementModule);
                }
            }
            final Object[] refinementsArray = refinements.toArray(new Object[refinements.size()]);
            return createArray(refinementsArray, refinementsArray.length);
        }

    }

    public abstract static class SetVisibilityNode extends RubyContextNode {

        private final Visibility visibility;

        @Child private SetMethodVisibilityNode setMethodVisibilityNode;

        public SetVisibilityNode(Visibility visibility) {
            this.visibility = visibility;
            setMethodVisibilityNode = SetMethodVisibilityNodeGen.create(visibility);
        }

        public abstract DynamicObject executeSetVisibility(VirtualFrame frame, DynamicObject module,
                Object[] arguments);

        @Specialization
        protected DynamicObject setVisibility(VirtualFrame frame, DynamicObject module, Object[] names) {
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

        public abstract void executeSetMethodVisibility(VirtualFrame frame, DynamicObject module, Object name);

        @Specialization
        protected void setMethodVisibility(DynamicObject module, Object name,
                @Cached BranchProfile errorProfile) {
            final String methodName = nameToJavaStringNode.executeToJavaString(name);

            final InternalMethod method = Layouts.MODULE.getFields(module).deepMethodSearch(getContext(), methodName);

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
        protected DynamicObject refine(DynamicObject self, DynamicObject moduleToRefine, NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("no block given", this));
        }

        @Specialization(guards = "!isRubyModule(moduleToRefine)")
        protected DynamicObject refineNotModule(DynamicObject self, Object moduleToRefine, DynamicObject block) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorWrongArgumentType(moduleToRefine, "Class", this));
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(moduleToRefine)")
        protected DynamicObject refine(DynamicObject namespace, DynamicObject moduleToRefine, DynamicObject block) {
            final ConcurrentMap<DynamicObject, DynamicObject> refinements = Layouts.MODULE
                    .getFields(namespace)
                    .getRefinements();
            final DynamicObject refinement = ConcurrentOperations
                    .getOrCompute(refinements, moduleToRefine, klass -> newRefinementModule(namespace, moduleToRefine));

            // Apply the existing refinements in this namespace and the new refinement inside the refine block
            final Map<DynamicObject, DynamicObject[]> refinementsInDeclarationContext = new HashMap<>();
            for (Entry<DynamicObject, DynamicObject> existingRefinement : refinements.entrySet()) {
                refinementsInDeclarationContext
                        .put(existingRefinement.getKey(), new DynamicObject[]{ existingRefinement.getValue() });
            }
            refinementsInDeclarationContext.put(moduleToRefine, new DynamicObject[]{ refinement });
            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new FixedDefaultDefinee(refinement),
                    refinementsInDeclarationContext);

            // Update methods in existing refinements in this namespace to also see this new refine block's refinements
            for (DynamicObject existingRefinement : refinements.values()) {
                final ModuleFields fields = Layouts.MODULE.getFields(existingRefinement);
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
                    Layouts.PROC.getBlock(block),
                    EMPTY_ARGUMENTS);
            return refinement;
        }

        private DynamicObject newRefinementModule(DynamicObject namespace, DynamicObject moduleToRefine) {
            final DynamicObject refinement = createModule(
                    getContext(),
                    getEncapsulatingSourceSection(),
                    coreLibrary().moduleClass,
                    null,
                    null,
                    this);
            final ModuleFields refinementFields = Layouts.MODULE.getFields(refinement);
            refinementFields.setupRefinementModule(moduleToRefine, namespace);
            return refinement;
        }

    }

    @CoreMethod(names = "using", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class ModuleUsingNode extends CoreMethodArrayArgumentsNode {

        @Child private UsingNode usingNode = UsingNodeGen.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject moduleUsing(DynamicObject self, DynamicObject refinementModule) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
            if (self != RubyArguments.getSelf(callerFrame)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Module#using is not called on self", this));
            }
            usingNode.executeUsing(refinementModule);
            return self;
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return createModule(getContext(), getEncapsulatingSourceSection(), rubyClass, null, null, this);
        }

    }

    @CoreMethod(names = "singleton_class?")
    public abstract static class IsSingletonClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isRubyClass(rubyModule)")
        protected Object doModule(DynamicObject rubyModule) {
            return false;
        }

        @Specialization(guards = "isRubyClass(rubyClass)")
        protected Object doClass(DynamicObject rubyClass) {
            return Layouts.CLASS.getIsSingleton(rubyClass);
        }
    }
}
