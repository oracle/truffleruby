/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.numeric.FixnumLowerNodeGen;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.language.LazyRubyNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.NotProvidedNode;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadBlockFromCurrentFrameArgumentsNode;
import org.truffleruby.language.arguments.ReadKeywordArgumentNode;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadRemainingArgumentsNode;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.ExceptionTranslatingNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.parser.Translator;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.object.DynamicObject;

public class CoreMethodNodeManager {

    private final RubyContext context;
    private final SingletonClassNode singletonClassNode;
    private final PrimitiveManager primitiveManager;

    public CoreMethodNodeManager(
            RubyContext context,
            SingletonClassNode singletonClassNode,
            PrimitiveManager primitiveManager) {
        this.context = context;
        this.singletonClassNode = singletonClassNode;
        this.primitiveManager = primitiveManager;
    }

    public void loadCoreMethodNodes() {
        if (!TruffleOptions.AOT && context.getOptions().LAZY_BUILTINS) {
            BuiltinsClasses.setupBuiltinsLazy(this, primitiveManager);
        } else {
            for (List<? extends NodeFactory<? extends RubyNode>> factory : BuiltinsClasses.getCoreNodeFactories()) {
                addCoreMethodNodes(factory);
            }
        }
    }

    public void addCoreMethodNodes(List<? extends NodeFactory<? extends RubyNode>> nodeFactories) {
        String moduleName = null;
        DynamicObject module = null;

        for (NodeFactory<? extends RubyNode> nodeFactory : nodeFactories) {
            final Class<?> nodeClass = nodeFactory.getNodeClass();
            final CoreMethod methodAnnotation = nodeClass.getAnnotation(CoreMethod.class);
            Primitive primitiveAnnotation;

            if (methodAnnotation != null) {
                if (module == null) {
                    CoreModule coreModule = nodeClass.getEnclosingClass().getAnnotation(CoreModule.class);
                    if (coreModule == null) {
                        throw new Error(nodeClass.getEnclosingClass() + " needs a @CoreModule annotation");
                    }
                    moduleName = coreModule.value();
                    module = getModule(moduleName, coreModule.isClass());
                }
                addCoreMethod(module, new MethodDetails(moduleName, methodAnnotation, nodeFactory));
            } else if ((primitiveAnnotation = nodeClass.getAnnotation(Primitive.class)) != null) {
                primitiveManager.addPrimitive(nodeFactory, primitiveAnnotation);
            }
        }
    }

    private DynamicObject getModule(String fullName, boolean isClass) {
        DynamicObject module;

        if (fullName.equals("main")) {
            module = getSingletonClass(context.getCoreLibrary().mainObject);
        } else {
            module = context.getCoreLibrary().objectClass;

            for (String moduleName : fullName.split("::")) {
                final ConstantLookupResult constant = ModuleOperations.lookupConstant(context, module, moduleName);

                if (!constant.isFound()) {
                    throw new RuntimeException(
                            StringUtils.format("Module %s not found when adding core library", moduleName));
                }

                module = (DynamicObject) constant.getConstant().getValue();
            }
        }

        assert isClass
                ? RubyGuards.isRubyClass(module)
                : RubyGuards.isRubyModule(module) && !RubyGuards.isRubyClass(module) : fullName;
        return module;
    }

    private DynamicObject getSingletonClass(Object object) {
        return singletonClassNode.executeSingletonClass(object);
    }

    private void addCoreMethod(DynamicObject module, MethodDetails methodDetails) {
        final CoreMethod method = methodDetails.getMethodAnnotation();

        final String[] names = method.names();
        assert names.length >= 1;

        final Visibility visibility = method.visibility();
        verifyUsage(module, methodDetails, method, visibility);

        final String keywordAsOptional = method.keywordAsOptional().isEmpty() ? null : method.keywordAsOptional();
        final Arity arity = createArity(method.required(), method.optional(), method.rest(), keywordAsOptional);

        final NodeFactory<? extends RubyNode> nodeFactory = methodDetails.getNodeFactory();
        final Function<SharedMethodInfo, RubyNode> methodNodeFactory;
        if (!TruffleOptions.AOT && context.getOptions().LAZY_CORE_METHOD_NODES) {
            methodNodeFactory = sharedMethodInfo -> new LazyRubyNode(
                    () -> createCoreMethodNode(nodeFactory, method, sharedMethodInfo));
        } else {
            methodNodeFactory = sharedMethodInfo -> createCoreMethodNode(nodeFactory, method, sharedMethodInfo);
        }

        final boolean onSingleton = method.onSingleton() || method.constructor();
        addMethods(
                module,
                method.isModuleFunction(),
                onSingleton,
                names,
                arity,
                visibility,
                methodNodeFactory,
                method.neverSplit());
    }

    public void addLazyCoreMethod(String nodeFactoryName, String moduleName, boolean isClass, Visibility visibility,
            boolean isModuleFunction, boolean onSingleton,
            boolean neverSplit, int required, int optional, boolean rest, String keywordAsOptional, String... names) {
        final DynamicObject module = getModule(moduleName, isClass);
        final Arity arity = createArity(required, optional, rest, keywordAsOptional);

        final Function<SharedMethodInfo, RubyNode> methodNodeFactory = sharedMethodInfo -> new LazyRubyNode(() -> {
            final NodeFactory<? extends RubyNode> nodeFactory = loadNodeFactory(nodeFactoryName);
            final CoreMethod methodAnnotation = nodeFactory.getNodeClass().getAnnotation(CoreMethod.class);
            return createCoreMethodNode(nodeFactory, methodAnnotation, sharedMethodInfo);
        });

        addMethods(module, isModuleFunction, onSingleton, names, arity, visibility, methodNodeFactory, neverSplit);
    }

    private void addMethods(DynamicObject module, boolean isModuleFunction, boolean onSingleton, String[] names,
            Arity arity, Visibility visibility,
            Function<SharedMethodInfo, RubyNode> methodNodeFactory, boolean neverSplit) {
        if (isModuleFunction) {
            addMethod(context, module, methodNodeFactory, names, Visibility.PRIVATE, arity, neverSplit);
            addMethod(
                    context,
                    getSingletonClass(module),
                    methodNodeFactory,
                    names,
                    Visibility.PUBLIC,
                    arity,
                    neverSplit);
        } else if (onSingleton) {
            addMethod(context, getSingletonClass(module), methodNodeFactory, names, visibility, arity, neverSplit);
        } else {
            addMethod(context, module, methodNodeFactory, names, visibility, arity, neverSplit);
        }
    }

    private static void addMethod(RubyContext context, DynamicObject module,
            Function<SharedMethodInfo, RubyNode> methodNodeFactory, String[] names, Visibility originalVisibility,
            Arity arity,
            boolean neverSplit) {
        final LexicalScope lexicalScope = new LexicalScope(context.getRootLexicalScope(), module);

        for (String name : names) {
            Visibility visibility = originalVisibility;
            if (ModuleOperations.isMethodPrivateFromName(name)) {
                visibility = Visibility.PRIVATE;
            }
            final SharedMethodInfo sharedMethodInfo = makeSharedMethodInfo(context, lexicalScope, module, name, arity);
            final RubyNode methodNode = methodNodeFactory.apply(sharedMethodInfo);
            final RootCallTarget callTarget = createCallTarget(context, sharedMethodInfo, methodNode, neverSplit);
            final InternalMethod method = new InternalMethod(
                    context,
                    sharedMethodInfo,
                    sharedMethodInfo.getLexicalScope(),
                    DeclarationContext.NONE,
                    name,
                    module,
                    visibility,
                    callTarget);

            Layouts.MODULE.getFields(module).addMethod(context, null, method);
        }
    }

    private static SharedMethodInfo makeSharedMethodInfo(RubyContext context, LexicalScope lexicalScope,
            DynamicObject module, String name, Arity arity) {
        return new SharedMethodInfo(
                context.getCoreLibrary().sourceSection,
                lexicalScope,
                arity,
                module,
                name,
                0,
                "builtin",
                null,
                context.getOptions().CORE_ALWAYS_CLONE);
    }

    private static Arity createArity(int required, int optional, boolean rest, String keywordAsOptional) {
        if (keywordAsOptional == null) {
            return new Arity(required, optional, rest);
        } else {
            return new Arity(required, optional, rest, 0, new String[]{ keywordAsOptional }, false);
        }
    }

    private static RootCallTarget createCallTarget(RubyContext context, SharedMethodInfo sharedMethodInfo,
            RubyNode methodNode, boolean neverSplit) {
        final RubyRootNode rootNode = new RubyRootNode(
                context,
                sharedMethodInfo.getSourceSection(),
                null,
                sharedMethodInfo,
                methodNode,
                !neverSplit);
        return Truffle.getRuntime().createCallTarget(rootNode);
    }

    public static RubyNode createCoreMethodNode(NodeFactory<? extends RubyNode> nodeFactory, CoreMethod method,
            SharedMethodInfo sharedMethodInfo) {
        final RubyNode[] argumentsNodes = new RubyNode[nodeFactory.getExecutionSignature().size()];
        int i = 0;

        final boolean needsSelf = needsSelf(method);

        if (needsSelf) {
            RubyNode readSelfNode = ProfileArgumentNodeGen.create(new ReadSelfNode());
            argumentsNodes[i++] = transformArgument(method, readSelfNode, 0);
        }

        final int required = method.required();
        final int optional = method.optional();
        final int nArgs = required + optional;

        for (int n = 0; n < nArgs; n++) {
            RubyNode readArgumentNode = ProfileArgumentNodeGen
                    .create(new ReadPreArgumentNode(n, MissingArgumentBehavior.NOT_PROVIDED));
            argumentsNodes[i++] = transformArgument(method, readArgumentNode, n + 1);
        }

        if (method.rest()) {
            argumentsNodes[i++] = new ReadRemainingArgumentsNode(nArgs);
        }

        if (method.needsBlock()) {
            argumentsNodes[i++] = new ReadBlockFromCurrentFrameArgumentsNode(NotProvided.INSTANCE);
        }

        if (!method.keywordAsOptional().isEmpty()) {
            if (optional > 0) {
                throw new UnsupportedOperationException(
                        "core method has been declared with both optional arguments and a keyword-as-optional argument");
            }

            argumentsNodes[i++] = new ReadKeywordArgumentNode(
                    required,
                    method.keywordAsOptional(),
                    new NotProvidedNode());
        }

        RubyNode node = createNodeFromFactory(nodeFactory, argumentsNodes);

        final RubyNode checkArity = Translator.createCheckArityNode(sharedMethodInfo.getArity());

        node = transformResult(method, node);
        node = Translator.sequence(null, Arrays.asList(checkArity, node));

        return new ExceptionTranslatingNode(node, method.unsupportedOperationBehavior());
    }

    public static RubyNode createNodeFromFactory(NodeFactory<? extends RubyNode> nodeFactory,
            RubyNode[] argumentsNodes) {
        final List<List<Class<?>>> signatures = nodeFactory.getNodeSignatures();

        assert signatures.size() == 1;
        final List<Class<?>> signature = signatures.get(0);

        if (signature.size() == 0) {
            return nodeFactory.createNode();
        } else {
            if (signature.size() == 1 && signature.get(0) == RubyNode[].class) {
                Object args = argumentsNodes;
                return nodeFactory.createNode(args);
            } else {
                Object[] args = argumentsNodes;
                return nodeFactory.createNode(args);
            }
        }
    }

    public static boolean needsSelf(CoreMethod method) {
        // Do not use needsSelf=true in module functions, it is either the module/class or the instance.
        // Usage of needsSelf is quite rare for singleton methods (except constructors).
        return method.constructor() || (!method.isModuleFunction() && !method.onSingleton() && method.needsSelf());
    }

    private static RubyNode transformArgument(CoreMethod method, RubyNode argument, int n) {
        if (ArrayUtils.contains(method.lowerFixnum(), n)) {
            argument = FixnumLowerNodeGen.create(argument);
        }

        if (n == 0 && method.raiseIfFrozenSelf()) {
            argument = TypeNodes.CheckFrozenNode.create(argument);
        }

        return argument;
    }

    private static RubyNode transformResult(CoreMethod method, RubyNode node) {
        if (!method.enumeratorSize().isEmpty()) {
            assert !method
                    .returnsEnumeratorIfNoBlock() : "Only one of enumeratorSize or returnsEnumeratorIfNoBlock can be specified";
            // TODO BF 6-27-2015 Handle multiple method names correctly
            node = new EnumeratorSizeNode(method.enumeratorSize(), method.names()[0], node);
        } else if (method.returnsEnumeratorIfNoBlock()) {
            // TODO BF 3-18-2015 Handle multiple method names correctly
            node = new ReturnEnumeratorIfNoBlockNode(method.names()[0], node);
        }

        if (method.taintFrom() != -1) {
            final boolean taintFromSelf = method.taintFrom() == 0;
            final int taintFromArg = taintFromSelf ? -1 : method.taintFrom() - 1;
            node = new TaintResultNode(taintFromSelf, taintFromArg, node);
        }

        return node;
    }

    private void verifyUsage(DynamicObject module, MethodDetails methodDetails, CoreMethod method,
            Visibility visibility) {
        if (method.isModuleFunction()) {
            if (visibility != Visibility.PUBLIC) {
                RubyLanguage.LOGGER
                        .warning("visibility ignored when isModuleFunction in " + methodDetails.getIndicativeName());
            }
            if (method.onSingleton()) {
                RubyLanguage.LOGGER
                        .warning("either onSingleton or isModuleFunction for " + methodDetails.getIndicativeName());
            }
            if (method.constructor()) {
                RubyLanguage.LOGGER
                        .warning("either constructor or isModuleFunction for " + methodDetails.getIndicativeName());
            }
            if (RubyGuards.isRubyClass(module)) {
                RubyLanguage.LOGGER
                        .warning("using isModuleFunction on a Class for " + methodDetails.getIndicativeName());
            }
        }
        if (method.onSingleton() && method.constructor()) {
            RubyLanguage.LOGGER.warning("either onSingleton or constructor for " + methodDetails.getIndicativeName());
        }

        if (methodDetails.getPrimaryName().equals("allocate") && !methodDetails.getModuleName().equals("Class")) {
            RubyLanguage.LOGGER
                    .warning("do not define #allocate but #__allocate__ for " + methodDetails.getIndicativeName());
        }
        if (methodDetails.getPrimaryName().equals("__allocate__") && method.visibility() != Visibility.PRIVATE) {
            RubyLanguage.LOGGER.warning(methodDetails.getIndicativeName() + " should be private");
        }
    }

    @SuppressWarnings("unchecked")
    public static NodeFactory<? extends RubyNode> loadNodeFactory(String nodeFactoryName) {
        final Object instance;
        try {
            Class<?> nodeFactoryClass = Class.forName(nodeFactoryName);
            instance = nodeFactoryClass.getMethod("getInstance").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new JavaException(e);
        }
        return (NodeFactory<? extends RubyNode>) instance;
    }

    public static class MethodDetails {

        private final String moduleName;
        private final CoreMethod methodAnnotation;
        private final NodeFactory<? extends RubyNode> nodeFactory;

        public MethodDetails(
                String moduleName,
                CoreMethod methodAnnotation,
                NodeFactory<? extends RubyNode> nodeFactory) {
            this.moduleName = moduleName;
            this.methodAnnotation = methodAnnotation;
            this.nodeFactory = nodeFactory;
        }

        public CoreMethod getMethodAnnotation() {
            return methodAnnotation;
        }

        public NodeFactory<? extends RubyNode> getNodeFactory() {
            return nodeFactory;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getPrimaryName() {
            return methodAnnotation.names()[0];
        }

        public String getIndicativeName() {
            return moduleName + "#" + getPrimaryName();
        }
    }

}
