/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.module;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ValueProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.RaiseIfFrozenNode;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.NameToJavaStringNodeGen;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.cast.ToPathNodeGen;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStringOrSymbolNodeGen;
import org.truffleruby.core.constant.WarnAlreadyInitializedNode;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.method.MethodFilter;
import org.truffleruby.core.module.ModuleNodesFactory.ClassExecNodeFactory;
import org.truffleruby.core.module.ModuleNodesFactory.SetMethodVisibilityNodeGen;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.WarningNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.RequireNode;
import org.truffleruby.language.methods.AddMethodNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.CanBindMethodToModuleNode;
import org.truffleruby.language.methods.CanBindMethodToModuleNodeGen;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.GetCurrentVisibilityNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.DeclarationContext.FixedDefaultDefinee;
import org.truffleruby.language.methods.UsingNode;
import org.truffleruby.language.methods.UsingNodeGen;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.IsANodeGen;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsFrozenNodeGen;
import org.truffleruby.language.objects.ReadInstanceVariableNode;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.SingletonClassNodeGen;
import org.truffleruby.language.objects.WriteInstanceVariableNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.Identifiers;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.Translator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

@CoreClass("Module")
public abstract class ModuleNodes {

    @TruffleBoundary
    public static DynamicObject createModule(RubyContext context, SourceSection sourceSection, DynamicObject selfClass, DynamicObject lexicalParent, String name, Node currentNode) {
        final ModuleFields model = new ModuleFields(context, sourceSection, lexicalParent, name);
        final DynamicObject module = Layouts.MODULE.createModule(Layouts.CLASS.getInstanceFactory(selfClass), model);
        model.rubyModuleObject = module;

        if (lexicalParent != null) {
            Layouts.MODULE.getFields(module).getAdoptedByLexicalParent(context, lexicalParent, name, currentNode);
        } else if (Layouts.MODULE.getFields(module).givenBaseName != null) { // bootstrap module
            Layouts.MODULE.getFields(module).setFullName(Layouts.MODULE.getFields(module).givenBaseName);
        }
        return module;
    }

    @CoreMethod(names = "===", required = 1)
    public abstract static class ContainsInstanceNode extends CoreMethodArrayArgumentsNode {

        @Child private IsANode isANode = IsANodeGen.create(null, null);

        @Specialization
        public boolean containsInstance(DynamicObject module, Object instance) {
            return isANode.executeIsA(instance, module);
        }

    }

    @CoreMethod(names = "<", required = 1)
    public abstract static class IsSubclassOfNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSubclassOf(VirtualFrame frame, DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        public Object isSubclassOf(DynamicObject self, DynamicObject other) {
            if (self == other) {
                return false;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return true;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return false;
            }

            return nil();
        }

        @Specialization(guards = "!isRubyModule(other)")
        public Object isSubclassOfOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = "<=", required = 1)
    public abstract static class IsSubclassOfOrEqualToNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSubclassOfOrEqualTo(DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        public Object isSubclassOfOrEqualTo(DynamicObject self, DynamicObject other) {
            if (self == other || ModuleOperations.includesModule(self, other)) {
                return true;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return false;
            }

            return nil();
        }

        @Specialization(guards = "!isRubyModule(other)")
        public Object isSubclassOfOrEqualToOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = ">", required = 1)
    public abstract static class IsSuperclassOfNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSuperclassOf(VirtualFrame frame, DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        public Object isSuperclassOf(DynamicObject self, DynamicObject other) {
            if (self == other) {
                return false;
            }

            if (ModuleOperations.includesModule(other, self)) {
                return true;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return false;
            }

            return nil();
        }

        @Specialization(guards = "!isRubyModule(other)")
        public Object isSuperclassOfOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = ">=", required = 1)
    public abstract static class IsSuperclassOfOrEqualToNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeIsSuperclassOfOrEqualTo(VirtualFrame frame, DynamicObject self, DynamicObject other);

        @TruffleBoundary
        @Specialization(guards = "isRubyModule(other)")
        public Object isSuperclassOfOrEqualTo(DynamicObject self, DynamicObject other) {
            if (self == other || ModuleOperations.includesModule(other, self)) {
                return true;
            }

            if (ModuleOperations.includesModule(self, other)) {
                return false;
            }

            return nil();
        }

        @Specialization(guards = "!isRubyModule(other)")
        public Object isSuperclassOfOrEqualToOther(DynamicObject self, DynamicObject other) {
            throw new RaiseException(coreExceptions().typeError("compared with non class/module", this));
        }

    }

    @CoreMethod(names = "<=>", required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Child private IsSubclassOfOrEqualToNode subclassNode;

        private Object isSubclass(DynamicObject self, DynamicObject other) {
            if (subclassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                subclassNode = insert(ModuleNodesFactory.IsSubclassOfOrEqualToNodeFactory.create(null));
            }
            return subclassNode.executeIsSubclassOfOrEqualTo(self, other);
        }

        @Specialization(guards = "isRubyModule(other)")
        public Object compare(DynamicObject self, DynamicObject other) {
            if (self == other) {
                return 0;
            }

            final Object isSubclass = isSubclass(self, other);

            if (isSubclass == nil()) {
                return nil();
            } else {
                return (boolean) isSubclass ? -1 : 1;
            }
        }

        @Specialization(guards = "!isRubyModule(other)")
        public Object compareOther(DynamicObject self, DynamicObject other) {
            return nil();
        }

    }

    @CoreMethod(names = "alias_method", required = 2, raiseIfFrozenSelf = true, visibility = Visibility.PRIVATE)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "newName"),
            @NodeChild(type = RubyNode.class, value = "oldName")
    })
    public abstract static class AliasMethodNode extends CoreMethodNode {

        @CreateCast("newName")
        public RubyNode coerceNewNameToString(RubyNode newName) {
            return NameToJavaStringNodeGen.create(newName);
        }

        @CreateCast("oldName")
        public RubyNode coerceOldNameToString(RubyNode oldName) {
            return NameToJavaStringNodeGen.create(oldName);
        }

        @Specialization
        public DynamicObject aliasMethod(DynamicObject module, String newName, String oldName) {
            Layouts.MODULE.getFields(module).alias(getContext(), this, newName, oldName);
            return module;
        }

    }

    @CoreMethod(names = "ancestors")
    public abstract static class AncestorsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject ancestors(DynamicObject self) {
            final List<DynamicObject> ancestors = new ArrayList<>();
            for (DynamicObject module : Layouts.MODULE.getFields(self).ancestors()) {
                ancestors.add(module);
            }

            Object[] objects = ancestors.toArray(new Object[ancestors.size()]);
            return createArray(objects, objects.length);
        }
    }

    @CoreMethod(names = "append_features", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class AppendFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();

        @Specialization(guards = "isRubyModule(target)")
        public DynamicObject appendFeatures(DynamicObject features, DynamicObject target,
                @Cached("create()") BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(features)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().typeError("append_features must be called only on modules", this));
            }
            Layouts.MODULE.getFields(target).include(getContext(), this, features);
            taintResultNode.maybeTaint(features, target);
            return nil();
        }
    }

    @NodeChildren({ @NodeChild("module"), @NodeChild("name") })
    public abstract static class GenerateAccessorNode extends RubyNode {

        final boolean isGetter;

        public GenerateAccessorNode(boolean isGetter) {
            this.isGetter = isGetter;
        }

        public abstract DynamicObject executeGenerateAccessor(DynamicObject module, Object name);

        @Specialization
        public DynamicObject generateAccessor(DynamicObject module, Object nameObject,
                @Cached("create()") NameToJavaStringNode nameToJavaStringNode) {
            final String name = nameToJavaStringNode.executeToJavaString(nameObject);
            createAccessor(module, name);
            return nil();
        }

        @TruffleBoundary
        private void createAccessor(DynamicObject module, String name) {
            final FrameInstance callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend();
            final SourceSection sourceSection = callerFrame.getCallNode().getEncapsulatingSourceSection();
            final SourceIndexLength sourceIndexLength = new SourceIndexLength(sourceSection.getCharIndex(), sourceSection.getCharLength());
            final Visibility visibility = DeclarationContext.findVisibility(callerFrame.getFrame(FrameAccess.READ_ONLY));
            final Arity arity = isGetter ? Arity.NO_ARGUMENTS : Arity.ONE_REQUIRED;
            final String ivar = "@" + name;
            final String accessorName = isGetter ? name : name + "=";

            final RubyNode checkArity = Translator.createCheckArityNode(arity);

            final LexicalScope lexicalScope = new LexicalScope(getContext().getRootLexicalScope(), module);
            final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                    sourceSection,
                    lexicalScope,
                    arity,
                    module,
                    accessorName,
                    "attr_" + (isGetter ? "reader" : "writer"),
                    null,
                    false);

            final RubyNode self = ProfileArgumentNodeGen.create(new ReadSelfNode());
            final RubyNode accessInstanceVariable;
            if (isGetter) {
                accessInstanceVariable = new ReadInstanceVariableNode(ivar, self);
            } else {
                RubyNode readArgument = ProfileArgumentNodeGen.create(new ReadPreArgumentNode(0, MissingArgumentBehavior.RUNTIME_ERROR));
                accessInstanceVariable = new WriteInstanceVariableNode(ivar, self, readArgument);
            }
            final RubyNode sequence = Translator.sequence(sourceIndexLength, Arrays.asList(checkArity, accessInstanceVariable));
            final RubyRootNode rootNode = new RubyRootNode(getContext(), sourceSection, null, sharedMethodInfo, sequence);
            final CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);
            final InternalMethod method = new InternalMethod(getContext(), sharedMethodInfo, lexicalScope, DeclarationContext.NONE, accessorName, module, visibility, callTarget);

            Layouts.MODULE.getFields(module).addMethod(getContext(), this, method);
        }
    }

    @CoreMethod(names = "attr", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class AttrNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = ModuleNodesFactory.GenerateAccessorNodeGen.create(true, null, null);
        @Child private GenerateAccessorNode generateSetterNode = ModuleNodesFactory.GenerateAccessorNodeGen.create(false, null, null);
        @Child private WarningNode warnNode;

        @Specialization
        public DynamicObject attr(DynamicObject module, Object[] names) {
            final boolean setter;
            if (names.length == 2 && names[1] instanceof Boolean) {
                warnObsoletedBooleanArgument();
                setter = (boolean) names[1];
                names = new Object[] { names[0] };
            } else {
                setter = false;
            }

            for (Object name : names) {
                generateGetterNode.executeGenerateAccessor(module, name);
                if (setter) {
                    generateSetterNode.executeGenerateAccessor(module, name);
                }
            }
            return nil();
        }

        private void warnObsoletedBooleanArgument() {
            if (warnNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnNode = insert(new WarningNode());
            }
            final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
            warnNode.warningMessage(sourceSection, "optional boolean argument is obsoleted");
        }

    }

    @CoreMethod(names = "attr_accessor", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class AttrAccessorNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = ModuleNodesFactory.GenerateAccessorNodeGen.create(true, null, null);
        @Child private GenerateAccessorNode generateSetterNode = ModuleNodesFactory.GenerateAccessorNodeGen.create(false, null, null);

        @Specialization
        public DynamicObject attrAccessor(DynamicObject module, Object[] names) {
            for (Object name : names) {
                generateGetterNode.executeGenerateAccessor(module, name);
                generateSetterNode.executeGenerateAccessor(module, name);
            }
            return nil();
        }

    }

    @CoreMethod(names = "attr_reader", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class AttrReaderNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateGetterNode = ModuleNodesFactory.GenerateAccessorNodeGen.create(true, null, null);

        @Specialization
        public DynamicObject attrReader(DynamicObject module, Object[] names) {
            for (Object name : names) {
                generateGetterNode.executeGenerateAccessor(module, name);
            }
            return nil();
        }

    }

    @CoreMethod(names = "attr_writer", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class AttrWriterNode extends CoreMethodArrayArgumentsNode {

        @Child private GenerateAccessorNode generateSetterNode = ModuleNodesFactory.GenerateAccessorNodeGen.create(false, null, null);

        @Specialization
        public DynamicObject attrWriter(DynamicObject module, Object[] names) {
            for (Object name : names) {
                generateSetterNode.executeGenerateAccessor(module, name);
            }
            return nil();
        }

    }

    @CoreMethod(names = "autoload", required = 2)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "filename")
    })
    public abstract static class AutoloadNode extends CoreMethodNode {

        @CreateCast("name") public RubyNode coerceNameToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @CreateCast("filename") public RubyNode coerceFilenameToPath(RubyNode filename) {
            return ToPathNodeGen.create(filename);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(filename)")
        public DynamicObject autoload(DynamicObject module, String name, DynamicObject filename) {
            if (!Identifiers.isValidConstantName19(name)) {
                throw new RaiseException(coreExceptions().nameError(StringUtils.format("autoload must be constant name: %s", name), module, name, this));
            }

            if (StringOperations.rope(filename).isEmpty()) {
                throw new RaiseException(coreExceptions().argumentError("empty file name", this));
            }

            if (Layouts.MODULE.getFields(module).getConstant(name) != null) {
                return nil();
            }

            Layouts.MODULE.getFields(module).setAutoloadConstant(getContext(), this, name, filename);

            return nil();
        }
    }

    @CoreMethod(names = "autoload?", required = 1)
    public abstract static class AutoloadQueryNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(name)")
        public Object autoloadQuerySymbol(DynamicObject module, DynamicObject name) {
            return autoloadQuery(module, Layouts.SYMBOL.getString(name));
        }

        @Specialization(guards = "isRubyString(name)")
        public Object autoloadQueryString(DynamicObject module, DynamicObject name) {
            return autoloadQuery(module, StringOperations.getString(name));
        }

        private Object autoloadQuery(DynamicObject module, String name) {
            final ConstantLookupResult constant = ModuleOperations.lookupConstant(getContext(), module, name);

            if (!constant.isFound() || !constant.getConstant().isAutoload()) {
                return nil();
            }

            return constant.getConstant().getValue();
        }
    }

    @CoreMethod(names = { "class_eval", "module_eval" }, optional = 3, lowerFixnum = 3, needsBlock = true)
    public abstract static class ClassEvalNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStrNode;

        protected DynamicObject toStr(VirtualFrame frame, Object object) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNode.create());
            }
            return toStrNode.executeToStr(frame, object);
        }

        @Specialization(guards = "isRubyString(code)")
        public Object classEval(DynamicObject module, DynamicObject code, NotProvided file, NotProvided line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            return classEvalSource(module, code, "(eval)", callNode);
        }

        @Specialization(guards = {"isRubyString(code)", "isRubyString(file)"})
        public Object classEval(DynamicObject module, DynamicObject code, DynamicObject file, NotProvided line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            return classEvalSource(module, code, StringOperations.getString(file), callNode);
        }

        @Specialization(guards = {"isRubyString(code)", "isRubyString(file)"})
        public Object classEval(DynamicObject module, DynamicObject code, DynamicObject file, int line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = classEvalSource(module, code, StringOperations.getString(file), line);
            return deferredCall.call(callNode);
        }

        @Specialization(guards = "wasProvided(code)")
        public Object classEval(VirtualFrame frame, DynamicObject module, Object code, NotProvided file, NotProvided line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            return classEvalSource(module, toStr(frame, code), "(eval)", callNode);
        }

        @Specialization(guards = {"isRubyString(code)", "wasProvided(file)"})
        public Object classEval(VirtualFrame frame, DynamicObject module, DynamicObject code, Object file, NotProvided line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            return classEvalSource(module, code, StringOperations.getString(toStr(frame, file)), callNode);
        }

        private Object classEvalSource(DynamicObject module, DynamicObject code, String file,
                @Cached("create()") IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = classEvalSource(module, code, file, 1);
            return deferredCall.call(callNode);
        }

        @TruffleBoundary
        private CodeLoader.DeferredCall classEvalSource(DynamicObject module, DynamicObject rubySource, String file, int line) {
            assert RubyGuards.isRubyString(rubySource);
            String code = StringOperations.getString(rubySource);

            final MaterializedFrame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend()
                    .getFrame(FrameInstance.FrameAccess.MATERIALIZE).materialize();
            final Encoding encoding = Layouts.STRING.getRope(rubySource).getEncoding();
            code = KernelNodes.EvalNode.offsetSource("class/module_eval", code, file, line);
            Source source = Source.newBuilder(code).name(file.intern()).mimeType(RubyLanguage.MIME_TYPE).build();

            final RubyRootNode rootNode = getContext().getCodeLoader().parse(source, encoding, ParserContext.MODULE, callerFrame, true, this);
            final DeclarationContext declarationContext = new DeclarationContext(Visibility.PUBLIC, new FixedDefaultDefinee(module));
            return getContext().getCodeLoader().prepareExecute(ParserContext.MODULE, declarationContext, rootNode, callerFrame, module);
        }

        @Specialization
        public Object classEval(DynamicObject self, NotProvided code, NotProvided file, NotProvided line, DynamicObject block,
                @Cached("create()") ClassExecNode classExecNode) {
            return classExecNode.executeClassExec(self, new Object[]{ self }, block);
        }

        @Specialization
        public Object classEval(DynamicObject self, NotProvided code, NotProvided file, NotProvided line, NotProvided block) {
            throw new RaiseException(coreExceptions().argumentError(0, 1, 2, this));
        }

        @Specialization(guards = "wasProvided(code)")
        public Object classEval(DynamicObject self, Object code, NotProvided file, NotProvided line, DynamicObject block) {
            throw new RaiseException(coreExceptions().argumentError(1, 0, this));
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
        public Object classExec(DynamicObject self, Object[] args, DynamicObject block) {
            final DeclarationContext declarationContext = new DeclarationContext(Visibility.PUBLIC, new FixedDefaultDefinee(self));
            return callBlockNode.executeCallBlock(declarationContext, block, self, Layouts.PROC.getBlock(block), args);
        }

        @Specialization
        public Object classExec(DynamicObject self, Object[] args, NotProvided block) {
            throw new RaiseException(coreExceptions().noBlockGiven(this));
        }

    }

    @CoreMethod(names = "class_variable_defined?", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class ClassVariableDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @TruffleBoundary(transferToInterpreterOnException = false)
        @Specialization
        public boolean isClassVariableDefinedString(DynamicObject module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            final Object value = ModuleOperations.lookupClassVariable(module, name);

            return value != null;
        }

    }

    @CoreMethod(names = "class_variable_get", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class ClassVariableGetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        @TruffleBoundary(transferToInterpreterOnException = false)
        public Object getClassVariable(DynamicObject module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            final Object value = ModuleOperations.lookupClassVariable(module, name);

            if (value == null) {
                throw new RaiseException(coreExceptions().nameErrorUninitializedClassVariable(module, name, this));
            } else {
                return value;
            }
        }

    }

    @CoreMethod(names = "class_variable_set", required = 2, raiseIfFrozenSelf = true)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "value")
    })
    public abstract static class ClassVariableSetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        @TruffleBoundary(transferToInterpreterOnException = false)
        public Object setClassVariable(DynamicObject module, String name, Object value) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);

            ModuleOperations.setClassVariable(getContext(), module, name, value, this);

            return value;
        }

    }

    @CoreMethod(names = "class_variables")
    public abstract static class ClassVariablesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject getClassVariables(DynamicObject module) {
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
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "inherit")
    })
    public abstract static class ConstantsNode extends CoreMethodNode {

        @CreateCast("inherit")
        public RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject constants(DynamicObject module, boolean inherit) {
            final List<DynamicObject> constantsArray = new ArrayList<>();

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
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "inherit")
    })
    public abstract static class ConstDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @CreateCast("inherit")
        public RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        public boolean isConstDefined(DynamicObject module, String fullName, boolean inherit) {
            return ModuleOperations.lookupScopedConstant(getContext(), module, fullName, inherit, this).isFound();
        }

    }

    @CoreMethod(names = "const_get", required = 1, optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "inherit")
    })
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    public abstract static class ConstGetNode extends CoreMethodNode {

        @Child private RequireNode requireNode;
        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, true);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @CreateCast("name")
        public RubyNode coerceToSymbolOrString(RubyNode name) {
            // We want to know if the name is a Symbol, as then scoped lookup is not tried
            return ToStringOrSymbolNodeGen.create(name);
        }

        @CreateCast("inherit")
        public RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        // Symbol
        @Specialization(guards = { "inherit", "isRubySymbol(name)" })
        public Object getConstant(VirtualFrame frame, DynamicObject module, DynamicObject name, boolean inherit) {
            return getConstant(frame, module, Layouts.SYMBOL.getString(name));
        }

        @Specialization(guards = { "!inherit", "isRubySymbol(name)" })
        public Object getConstantNoInherit(VirtualFrame frame, DynamicObject module, DynamicObject name, boolean inherit) {
            return getConstantNoInherit(frame, module, Layouts.SYMBOL.getString(name), this);
        }

        // String
        @Specialization(guards = { "inherit", "isRubyString(name)", "equalNode.execute(rope(name), cachedRope)", "!scoped" }, limit = "getLimit()")
        public Object getConstantStringCached(VirtualFrame frame, DynamicObject module, DynamicObject name, boolean inherit,
                @Cached("privatizeRope(name)") Rope cachedRope,
                @Cached("getString(name)") String cachedString,
                @Cached("create()") RopeNodes.EqualNode equalNode,
                @Cached("isScoped(cachedString)") boolean scoped) {
            return getConstant(frame, module, cachedString);
        }

        @Specialization(guards = { "inherit", "isRubyString(name)", "!isScoped(name)" }, replaces = "getConstantStringCached")
        public Object getConstantString(VirtualFrame frame, DynamicObject module, DynamicObject name, boolean inherit) {
            return getConstant(frame, module, StringOperations.getString(name));
        }

        @Specialization(guards = { "!inherit", "isRubyString(name)", "!isScoped(name)" })
        public Object getConstantNoInheritString(VirtualFrame frame, DynamicObject module, DynamicObject name, boolean inherit) {
            return getConstantNoInherit(frame, module, StringOperations.getString(name), this);
        }

        // Scoped String
        @Specialization(guards = {"isRubyString(fullName)", "isScoped(fullName)"})
        public Object getConstantScoped(DynamicObject module, DynamicObject fullName, boolean inherit) {
            return getConstantScoped(module, StringOperations.getString(fullName), inherit);
        }

        private Object getConstant(VirtualFrame frame, Object module, String name) {
            final RubyConstant constant = lookupConstantNode.lookupConstant(frame, module, name);
            return getConstantNode.executeGetConstant(frame, module, name, constant, lookupConstantNode);
        }

        private Object getConstantNoInherit(VirtualFrame frame, DynamicObject module, String name, Node currentNode) {
            ConstantLookupResult constant = ModuleOperations.lookupConstantWithInherit(getContext(), module, name, false, currentNode);
            if (!constant.isFound()) {
                // Call const_missing
                return getConstantNode.executeGetConstant(frame, module, name, null, lookupConstantNode);
            } else {
                if (constant.getConstant().isAutoload()) {
                    loadAutoloadedConstant(constant);
                    constant = ModuleOperations.lookupConstantWithInherit(getContext(), module, name, false, currentNode);
                }

                return constant.getConstant().getValue();
            }
        }

        @TruffleBoundary
        private Object getConstantScoped(DynamicObject module, String fullName, boolean inherit) {
            ConstantLookupResult constant = ModuleOperations.lookupScopedConstant(getContext(), module, fullName, inherit, this);
            if (!constant.isFound()) {
                throw new RaiseException(coreExceptions().nameErrorUninitializedConstant(module, fullName, this));
            } else {
                return constant.getConstant().getValue();
            }
        }

        @TruffleBoundary
        boolean isScoped(DynamicObject name) {
            assert RubyGuards.isRubyString(name);
            // TODO (eregon, 27 May 2015): Any way to make this efficient?
            return StringOperations.getString(name).contains("::");
        }

        boolean isScoped(String name) {
            return name.contains("::");
        }

        private void loadAutoloadedConstant(ConstantLookupResult constant) {
            if (requireNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                requireNode = insert(RequireNode.create());
            }

            final String feature = StringOperations.getString((DynamicObject) constant.getConstant().getValue());
            requireNode.executeRequire(feature);
        }

        protected int getLimit() {
            return getContext().getOptions().CONSTANT_CACHE;
        }

    }

    @CoreMethod(names = "const_missing", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class ConstMissingNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public Object constMissing(DynamicObject module, String name) {
            throw new RaiseException(coreExceptions().nameErrorUninitializedConstant(module, name, this));
        }

    }

    @CoreMethod(names = "const_set", required = 2)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "value")
    })
    public abstract static class ConstSetNode extends CoreMethodNode {

        @Child private WarnAlreadyInitializedNode warnAlreadyInitializedNode;

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @TruffleBoundary
        @Specialization
        public Object setConstant(DynamicObject module, String name, Object value) {
            if (!Identifiers.isValidConstantName19(name)) {
                throw new RaiseException(coreExceptions().nameError(StringUtils.format("wrong constant name %s", name), module, name, this));
            }

            final RubyConstant previous = Layouts.MODULE.getFields(module).setConstant(getContext(), this, name, value);
            if (previous != null) {
                warnAlreadyInitializedConstant(module, name, previous.getSourceSection());
            }
            return value;
        }

        private void warnAlreadyInitializedConstant(DynamicObject module, String name, SourceSection previousSourceSection) {
            if (warnAlreadyInitializedNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnAlreadyInitializedNode = insert(new WarnAlreadyInitializedNode());
            }
            final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
            warnAlreadyInitializedNode.warnAlreadyInitialized(module, name, sourceSection, previousSourceSection);
        }

    }

    @CoreMethod(names = "define_method", needsBlock = true, required = 1, optional = 1, visibility = Visibility.PRIVATE)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "proc"),
            @NodeChild(type = RubyNode.class, value = "block")
    })
    public abstract static class DefineMethodNode extends CoreMethodNode {

        @Child private AddMethodNode addMethodNode = AddMethodNode.create(false);

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject defineMethod(DynamicObject module, String name, NotProvided proc, NotProvided block) {
            throw new RaiseException(coreExceptions().argumentError("needs either proc or block", this));
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject defineMethodBlock(DynamicObject module, String name, NotProvided proc, DynamicObject block) {
            return defineMethodProc(module, name, block, NotProvided.INSTANCE);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyProc(proc)")
        public DynamicObject defineMethodProc(DynamicObject module, String name, DynamicObject proc, NotProvided block) {
            return defineMethod(module, name, proc);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyMethod(methodObject)")
        public DynamicObject defineMethodMethod(DynamicObject module, String name, DynamicObject methodObject, NotProvided block,
                @Cached("createCanBindMethodToModuleNode()") CanBindMethodToModuleNode canBindMethodToModuleNode) {
            final InternalMethod method = Layouts.METHOD.getMethod(methodObject);

            if (!canBindMethodToModuleNode.executeCanBindMethodToModule(method, module)) {
                final DynamicObject declaringModule = method.getDeclaringModule();
                if (RubyGuards.isSingletonClass(declaringModule)) {
                    throw new RaiseException(coreExceptions().typeError(
                            "can't bind singleton method to a different class", this));
                } else {
                    throw new RaiseException(coreExceptions().typeError(
                            "class must be a subclass of " + Layouts.MODULE.getFields(declaringModule).getName(), this));
                }
            }

            Layouts.MODULE.getFields(module).addMethod(getContext(), this, method.withName(name));
            return getSymbol(name);
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyUnboundMethod(method)")
        public DynamicObject defineMethod(DynamicObject module, String name, DynamicObject method, NotProvided block) {
            final DynamicObject origin = Layouts.UNBOUND_METHOD.getOrigin(method);
            if (!ModuleOperations.canBindMethodTo(origin, module)) {
                throw new RaiseException(coreExceptions().typeError("bind argument must be a subclass of " + Layouts.MODULE.getFields(origin).getName(), this));
            }

            // TODO CS 5-Apr-15 TypeError if the method came from a singleton

            return addMethod(module, name, Layouts.UNBOUND_METHOD.getMethod(method));
        }

        @TruffleBoundary
        private DynamicObject defineMethod(DynamicObject module, String name, DynamicObject proc) {
            final RootCallTarget callTarget = (RootCallTarget) Layouts.PROC.getCallTargetForLambdas(proc);
            final RubyRootNode rootNode = (RubyRootNode) callTarget.getRootNode();
            final SharedMethodInfo info = Layouts.PROC.getSharedMethodInfo(proc).withName(name);
            final MaterializedFrame declarationFrame = Layouts.PROC.getDeclarationFrame(proc);

            final RubyNode body = NodeUtil.cloneNode(rootNode.getBody());
            final RubyNode newBody = new CallMethodWithProcBody(declarationFrame, body);
            final RubyRootNode newRootNode = new RubyRootNode(getContext(), info.getSourceSection(), rootNode.getFrameDescriptor(), info, newBody);
            final CallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

            final DeclarationContext declarationContext = Layouts.PROC.getDeclarationContext(proc);
            final InternalMethod method = InternalMethod.fromProc(getContext(), info, declarationContext, name, module, Visibility.PUBLIC, proc, newCallTarget);
            return addMethod(module, name, method);
        }

        private static class CallMethodWithProcBody extends RubyNode {

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
        private DynamicObject addMethod(DynamicObject module, String name, InternalMethod method) {
            method = method.withName(name);

            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_ONLY);
            final Visibility visibility = GetCurrentVisibilityNode.getVisibilityFromNameAndFrame(name, frame);
            addMethodNode.executeAddMethod(module, method, visibility);
            return getSymbol(method.getName());
        }

        protected CanBindMethodToModuleNode createCanBindMethodToModuleNode() {
            return CanBindMethodToModuleNodeGen.create(null, null);
        }

    }

    @CoreMethod(names = "extend_object", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class ExtendObjectNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNodeGen.create(null);

        @Specialization
        public DynamicObject extendObject(DynamicObject module, DynamicObject object,
                @Cached("create()") BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(module)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().typeErrorWrongArgumentType(module, "Module", this));
            }

            Layouts.MODULE.getFields(singletonClassNode.executeSingletonClass(object)).include(getContext(), this, module);
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
            classExecNode.executeClassExec(module, new Object[]{module}, block);
        }

        @Specialization
        public DynamicObject initialize(DynamicObject module, NotProvided block) {
            return module;
        }

        @Specialization
        public DynamicObject initialize(DynamicObject module, DynamicObject block) {
            classEval(module, block);
            return module;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode;

        @Specialization(guards = { "!isRubyClass(self)", "isRubyModule(from)", "!isRubyClass(from)" })
        public Object initializeCopyModule(DynamicObject self, DynamicObject from) {
            Layouts.MODULE.getFields(self).initCopy(from);
            
            final DynamicObject selfMetaClass = getSingletonClass(self);
            final DynamicObject fromMetaClass = getSingletonClass(from);
            Layouts.MODULE.getFields(selfMetaClass).initCopy(fromMetaClass);

            return nil();
        }

        @Specialization(guards = {"isRubyClass(self)", "isRubyClass(from)"})
        public Object initializeCopyClass(DynamicObject self, DynamicObject from,
                @Cached("create()") BranchProfile errorProfile) {
            if (from == coreLibrary().getBasicObjectClass()) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().typeError("can't copy the root class", this));
            } else if (Layouts.CLASS.getIsSingleton(from)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().typeError("can't copy singleton class", this));
            }

            Layouts.MODULE.getFields(self).initCopy(from);

            final DynamicObject selfMetaClass = getSingletonClass(self);
            final DynamicObject fromMetaClass = Layouts.BASIC_OBJECT.getMetaClass(from);

            assert Layouts.CLASS.getIsSingleton(fromMetaClass);
            assert Layouts.CLASS.getIsSingleton(Layouts.BASIC_OBJECT.getMetaClass(self));

            Layouts.MODULE.getFields(selfMetaClass).initCopy(fromMetaClass); // copy class methods

            return nil();
        }

        protected DynamicObject getSingletonClass(DynamicObject object) {
            if (singletonClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singletonClassNode = insert(SingletonClassNodeGen.create(null));
            }

            return singletonClassNode.executeSingletonClass(object);
        }

    }

    @CoreMethod(names = "included", needsSelf = false, required = 1, visibility = Visibility.PRIVATE)
    public abstract static class IncludedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject included(Object subclass) {
            return nil();
        }

    }

    @CoreMethod(names = "included_modules")
    public abstract static class IncludedModulesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject includedModules(DynamicObject module) {
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
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "inherit") })
    public abstract static class MethodDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @CreateCast("inherit")
        public RubyNode coerceToBoolean(RubyNode inherit) {
            return BooleanCastWithDefaultNodeGen.create(true, inherit);
        }

        @TruffleBoundary
        @Specialization
        public boolean isMethodDefined(DynamicObject module, String name, boolean inherit) {
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

        @Child private SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION, null, null);

        @Specialization
        public DynamicObject moduleFunction(VirtualFrame frame, DynamicObject module, Object[] names,
                @Cached("create()") BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(module)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().typeError("module_function must be called for modules", this));
            }

            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public Object name(DynamicObject module,
                @Cached("createIdentityProfile()") ValueProfile fieldsProfile) {
            final ModuleFields fields = fieldsProfile.profile(Layouts.MODULE.getFields(module));

            if (!fields.hasPartialName()) {
                return nil();
            }

            return makeStringNode.executeMake(fields.getName(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "nesting", onSingleton = true)
    public abstract static class NestingNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject nesting() {
            final List<DynamicObject> modules = new ArrayList<>();

            InternalMethod method = getContext().getCallStack().getCallingMethodIgnoringSend();
            LexicalScope lexicalScope = method == null ? null : method.getSharedMethodInfo().getLexicalScope();
            DynamicObject object = coreLibrary().getObjectClass();

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

        @Child private SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.PUBLIC, null, null);

        public abstract DynamicObject executePublic(VirtualFrame frame, DynamicObject module, Object[] args);

        @Specialization
        public DynamicObject doPublic(VirtualFrame frame, DynamicObject module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "public_class_method", rest = true)
    public abstract static class PublicClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNodeGen.create(null);
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNodeGen.create(Visibility.PUBLIC);

        @Specialization
        public DynamicObject publicClassMethod(VirtualFrame frame, DynamicObject module, Object[] names) {
            final DynamicObject singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.executeSetMethodVisibility(frame, singletonClass, name);
            }

            return module;
        }
    }

    @CoreMethod(names = "private", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class PrivateNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.PRIVATE, null, null);

        public abstract DynamicObject executePrivate(VirtualFrame frame, DynamicObject module, Object[] args);

        @Specialization
        public DynamicObject doPrivate(VirtualFrame frame, DynamicObject module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "prepend_features", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class PrependFeaturesNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();

        @Specialization(guards = "isRubyModule(target)")
        public DynamicObject prependFeatures(DynamicObject features, DynamicObject target,
                @Cached("create()") BranchProfile errorProfile) {
            if (RubyGuards.isRubyClass(features)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().typeError("prepend_features must be called only on modules", this));
            }
            Layouts.MODULE.getFields(target).prepend(getContext(), this, features);
            taintResultNode.maybeTaint(features, target);
            return nil();
        }
    }

    @CoreMethod(names = "private_class_method", rest = true)
    public abstract static class PrivateClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNodeGen.create(null);
        @Child private SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNodeGen.create(Visibility.PRIVATE);

        @Specialization
        public DynamicObject privateClassMethod(VirtualFrame frame, DynamicObject module, Object[] names) {
            final DynamicObject singletonClass = singletonClassNode.executeSingletonClass(module);

            for (Object name : names) {
                setMethodVisibilityNode.executeSetMethodVisibility(frame, singletonClass, name);
            }

            return module;
        }
    }

    @CoreMethod(names = "public_instance_method", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class PublicInstanceMethodNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public DynamicObject publicInstanceMethod(DynamicObject module, String name,
                @Cached("create()") BranchProfile errorProfile) {
            // TODO(CS, 11-Jan-15) cache this lookup
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, null);

            if (method == null || method.isUndefined()) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUndefinedMethod(name, module, this));
            } else if (method.getVisibility() != Visibility.PUBLIC) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorPrivateMethod(name, module, this));
            }

            return Layouts.UNBOUND_METHOD.createUnboundMethod(coreLibrary().getUnboundMethodFactory(), module, method);
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "includeAncestors")
    })
    protected abstract static class AbstractInstanceMethodsNode extends CoreMethodNode {

        final Visibility visibility;

        public AbstractInstanceMethodsNode(Visibility visibility) {
            this.visibility = visibility;
        }

        @CreateCast("includeAncestors")
        public RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @Specialization
        @TruffleBoundary
        public DynamicObject getInstanceMethods(DynamicObject module, boolean includeAncestors) {
            Object[] objects = Layouts.MODULE.getFields(module).filterMethods(getContext(), includeAncestors, MethodFilter.by(visibility)).toArray();
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


    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    protected abstract static class AbstractMethodDefinedNode extends CoreMethodNode {

        final Visibility visibility;

        public AbstractMethodDefinedNode(Visibility visibility) {
            this.visibility = visibility;
        }

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public boolean isMethodDefined(DynamicObject module, String name) {
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
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "includeAncestors")
    })
    public abstract static class InstanceMethodsNode extends CoreMethodNode {

        @CreateCast("includeAncestors")
        public RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject instanceMethods(DynamicObject module, boolean includeAncestors) {
            Object[] objects = Layouts.MODULE.getFields(module).filterMethods(getContext(), includeAncestors, MethodFilter.PUBLIC_PROTECTED).toArray();
            return createArray(objects, objects.length);
        }
    }

    @CoreMethod(names = "instance_method", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class InstanceMethodNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public DynamicObject instanceMethod(DynamicObject module, String name,
                @Cached("create()") BranchProfile errorProfile) {
            // TODO(CS, 11-Jan-15) cache this lookup
            final InternalMethod method = ModuleOperations.lookupMethodUncached(module, name, null);

            if (method == null || method.isUndefined()) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUndefinedMethod(name, module, this));
            }

            return Layouts.UNBOUND_METHOD.createUnboundMethod(coreLibrary().getUnboundMethodFactory(), module, method);
        }

    }

    @CoreMethod(names = "private_constant", rest = true)
    public abstract static class PrivateConstantNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();

        @Specialization
        public DynamicObject privateConstant(DynamicObject module, Object[] args) {
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
        public DynamicObject deprecateConstant(DynamicObject module, Object[] args) {
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
        public DynamicObject publicConstant(DynamicObject module, Object[] args) {
            for (Object arg : args) {
                String name = nameToJavaStringNode.executeToJavaString(arg);
                Layouts.MODULE.getFields(module).changeConstantVisibility(getContext(), this, name, false);
            }
            return module;
        }
    }

    @CoreMethod(names = "protected", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class ProtectedNode extends CoreMethodArrayArgumentsNode {

        @Child private SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.PROTECTED, null, null);

        @Specialization
        public DynamicObject doProtected(VirtualFrame frame, DynamicObject module, Object[] names) {
            return setVisibilityNode.executeSetVisibility(frame, module, names);
        }

    }

    @CoreMethod(names = "remove_class_variable", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class RemoveClassVariableNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @TruffleBoundary(transferToInterpreterOnException = false)
        @Specialization
        public Object removeClassVariableString(DynamicObject module, String name) {
            SymbolTable.checkClassVariableName(getContext(), name, module, this);
            return ModuleOperations.removeClassVariable(Layouts.MODULE.getFields(module), getContext(), this, name);
        }

    }

    @CoreMethod(names = "remove_const", required = 1, visibility = Visibility.PRIVATE)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class RemoveConstNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        Object removeConstant(DynamicObject module, String name) {
            final RubyConstant oldConstant = Layouts.MODULE.getFields(module).removeConstant(getContext(), this, name);
            if (oldConstant == null) {
                throw new RaiseException(coreExceptions().nameErrorConstantNotDefined(module, name, this));
            } else {
                if (oldConstant.isAutoload()) {
                    return nil();
                } else {
                    return oldConstant.getValue();
                }
            }
        }

    }

    @CoreMethod(names = "remove_method", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class RemoveMethodNode extends CoreMethodArrayArgumentsNode {

        private final BranchProfile errorProfile = BranchProfile.create();

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private IsFrozenNode isFrozenNode = IsFrozenNodeGen.create(null);
        @Child private CallDispatchHeadNode methodRemovedNode = CallDispatchHeadNode.createOnSelf();

        @Specialization
        public DynamicObject removeMethods(VirtualFrame frame, DynamicObject module, Object[] names) {
            for (Object name : names) {
                removeMethod(frame, module, nameToJavaStringNode.executeToJavaString(name));
            }
            return module;
        }

        private void removeMethod(VirtualFrame frame, DynamicObject module, String name) {
            isFrozenNode.raiseIfFrozen(module);

            if (Layouts.MODULE.getFields(module).removeMethod(name)) {
                if (RubyGuards.isSingletonClass(module)) {
                    final DynamicObject receiver = Layouts.CLASS.getAttached(module);
                    methodRemovedNode.call(frame, receiver, "singleton_method_removed", getSymbol(name));
                } else {
                    methodRemovedNode.call(frame, module, "method_removed", getSymbol(name));
                }
            } else {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorMethodNotDefinedIn(module, name, this));
            }
        }

    }

    @CoreMethod(names = { "to_s", "inspect" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode callRbInspect;
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject toS(DynamicObject module) {
            final String moduleName;
            final ModuleFields fields = Layouts.MODULE.getFields(module);
            if (RubyGuards.isSingletonClass(module)) {
                final DynamicObject attached = Layouts.CLASS.getAttached(module);
                final String name;
                if (Layouts.CLASS.isClass(attached) || Layouts.MODULE.isModule(attached)) {
                    if (callRbInspect == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        callRbInspect = insert(CallDispatchHeadNode.createOnSelf());
                    }
                    final Object inspectResult = callRbInspect.call(null, coreLibrary().getTruffleTypeModule(), "rb_inspect", attached);
                    name = StringOperations.getString((DynamicObject) inspectResult);
                } else {
                    name = fields.getName();
                }
                moduleName = "#<Class:" + name + ">";
            } else if (fields.isRefinement()) {
                final String refinedClass = Layouts.MODULE.getFields(fields.getRefinedClass()).getName();
                final String refinementNamespace = Layouts.MODULE.getFields(fields.getRefinementNamespace()).getName();
                moduleName = "#<refinement:" + refinedClass + "@" + refinementNamespace + ">";
            } else {
                moduleName = fields.getName();
            }

            return makeStringNode.executeMake(moduleName, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "undef_method", rest = true, visibility = Visibility.PRIVATE)
    public abstract static class UndefMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private RaiseIfFrozenNode raiseIfFrozenNode = new RaiseIfFrozenNode(ProfileArgumentNodeGen.create(new ReadSelfNode()));
        @Child private CallDispatchHeadNode methodUndefinedNode = CallDispatchHeadNode.createOnSelf();

        @Specialization
        public DynamicObject undefMethods(VirtualFrame frame, DynamicObject module, Object[] names) {
            for (Object name : names) {
                undefMethod(frame, module, nameToJavaStringNode.executeToJavaString(name));
            }
            return module;
        }

        private void undefMethod(VirtualFrame frame, DynamicObject module, String name) {
            raiseIfFrozenNode.execute(frame);

            Layouts.MODULE.getFields(module).undefMethod(getContext(), this, name);
            if (RubyGuards.isSingletonClass(module)) {
                final DynamicObject receiver = Layouts.CLASS.getAttached(module);
                methodUndefinedNode.call(frame, receiver, "singleton_method_undefined", getSymbol(name));
            } else {
                methodUndefinedNode.call(frame, module, "method_undefined", getSymbol(name));
            }
        }

    }

    @CoreMethod(names = "used_modules", onSingleton = true)
    public abstract static class UsedModulesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject usedModules() {
            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_ONLY);
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
        public DynamicObject usedRefinements() {
            final Frame frame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_ONLY);
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

    @NodeChildren({ @NodeChild(value = "module"), @NodeChild(value = "names") })
    public abstract static class SetVisibilityNode extends RubyNode {

        private final Visibility visibility;

        @Child private SetMethodVisibilityNode setMethodVisibilityNode;

        public SetVisibilityNode(Visibility visibility) {
            this.visibility = visibility;
            setMethodVisibilityNode = SetMethodVisibilityNodeGen.create(visibility);
        }

        public abstract DynamicObject executeSetVisibility(VirtualFrame frame, DynamicObject module, Object[] arguments);

        @Specialization
        public DynamicObject setVisibility(VirtualFrame frame, DynamicObject module, Object[] names) {
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

    public abstract static class SetMethodVisibilityNode extends RubyBaseNode {

        private final Visibility visibility;

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private AddMethodNode addMethodNode = AddMethodNode.create(true);

        public SetMethodVisibilityNode(Visibility visibility) {
            this.visibility = visibility;
        }

        public abstract void executeSetMethodVisibility(VirtualFrame frame, DynamicObject module, Object name);

        @Specialization
        public void setMethodVisibility(DynamicObject module, Object name,
                @Cached("create()") BranchProfile errorProfile) {
            final String methodName = nameToJavaStringNode.executeToJavaString(name);

            final InternalMethod method = Layouts.MODULE.getFields(module).deepMethodSearch(getContext(), methodName);

            if (method == null) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUndefinedMethod(methodName, module, this));
            }

            /*
             * If the method was already defined in this class, that's fine
             * {@link addMethod} will overwrite it, otherwise we do actually
             * want to add a copy of the method with a different visibility
             * to this module.
             */
            addMethodNode.executeAddMethod(module, method, visibility);
        }

    }

    @CoreMethod(names = "refine", needsBlock = true, required = 1, visibility = Visibility.PRIVATE)
    public abstract static class RefineNode extends CoreMethodArrayArgumentsNode {

        @Child private CallBlockNode callBlockNode = CallBlockNode.create();

        @Specialization
        protected DynamicObject refine(DynamicObject self, DynamicObject classToRefine, NotProvided block) {
            throw new RaiseException(coreExceptions().argumentError("no block given", this));
        }

        @Specialization(guards = "!isRubyClass(classToRefine)")
        protected DynamicObject refineNotClass(DynamicObject self, Object classToRefine, DynamicObject block) {
            throw new RaiseException(coreExceptions().typeErrorWrongArgumentType(classToRefine, "Class", this));
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyClass(classToRefine)")
        protected DynamicObject refine(DynamicObject namespace, DynamicObject classToRefine, DynamicObject block) {
            final ConcurrentMap<DynamicObject, DynamicObject> refinements = Layouts.MODULE.getFields(namespace).getRefinements();
            final DynamicObject refinement = ConcurrentOperations.getOrCompute(refinements, classToRefine, klass -> newRefinementModule(namespace, classToRefine));

            // Apply the existing refinements in this namespace and the new refinement inside the refine block
            final Map<DynamicObject, DynamicObject[]> refinementsInDeclarationContext = new HashMap<>();
            for (Entry<DynamicObject, DynamicObject> existingRefinement : refinements.entrySet()) {
                refinementsInDeclarationContext.put(existingRefinement.getKey(), new DynamicObject[]{ existingRefinement.getValue() });
            }
            refinementsInDeclarationContext.put(classToRefine, new DynamicObject[]{ refinement });
            final DeclarationContext declarationContext = new DeclarationContext(Visibility.PUBLIC, new FixedDefaultDefinee(refinement), refinementsInDeclarationContext);

            // Update methods in existing refinements in this namespace to also see this new refine block's refinements
            for (DynamicObject existingRefinement : refinements.values()) {
                final ModuleFields fields = Layouts.MODULE.getFields(existingRefinement);
                for (InternalMethod refinedMethodInExistingRefinement : fields.getMethods()) {
                    fields.addMethod(getContext(), this, refinedMethodInExistingRefinement.withDeclarationContext(declarationContext));
                }
            }

            callBlockNode.executeCallBlock(declarationContext, block, refinement, Layouts.PROC.getBlock(block), EMPTY_ARGUMENTS);
            return refinement;
        }

        private DynamicObject newRefinementModule(DynamicObject namespace, DynamicObject classToRefine) {
            final DynamicObject refinement = createModule(getContext(), getEncapsulatingSourceSection(), coreLibrary().getModuleClass(), null, null, this);
            final ModuleFields refinementFields = Layouts.MODULE.getFields(refinement);
            refinementFields.setupRefinementModule(classToRefine, namespace);
            return refinement;
        }

    }

    @CoreMethod(names = "using", required = 1, visibility = Visibility.PRIVATE)
    public abstract static class ModuleUsingNode extends CoreMethodArrayArgumentsNode {

        @Child private UsingNode usingNode = UsingNodeGen.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject moduleUsing(DynamicObject self, DynamicObject refinementModule) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_ONLY);
            if (self != RubyArguments.getSelf(callerFrame)) {
                throw new RaiseException(coreExceptions().runtimeError("Module#using is not called on self", this));
            }
            usingNode.executeUsing(refinementModule);
            return self;
        }

    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return createModule(getContext(), getEncapsulatingSourceSection(), rubyClass, null, null, this);
        }

    }

    @CoreMethod(names = "singleton_class?")
    public abstract static class IsSingletonClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isRubyClass(rubyModule)")
        public Object doModule(DynamicObject rubyModule) {
            return false;
        }

        @Specialization(guards = "isRubyClass(rubyClass)")
        public Object doClass(DynamicObject rubyClass) {
            return Layouts.CLASS.getIsSingleton(rubyClass);
        }
    }
}
