/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.classvariables.ClassVariableStorage;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.parser.Identifiers;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.NeverValidAssumption;

public abstract class ModuleOperations {

    public static final Assumption[] EMPTY_ASSUMPTION_ARRAY = new Assumption[0];

    @TruffleBoundary
    public static boolean includesModule(RubyModule module, RubyModule other) {
        for (RubyModule ancestor : module.fields.ancestors()) {
            if (ancestor == other) {
                return true;
            }
        }

        return false;
    }

    public static boolean assignableTo(RubyModule thisClass, RubyModule otherClass) {
        return includesModule(thisClass, otherClass);
    }

    public static boolean inAncestorsOf(RubyModule module, RubyModule ancestors) {
        return includesModule(ancestors, module);
    }

    @TruffleBoundary
    public static boolean canBindMethodTo(InternalMethod method, RubyModule module) {
        final RubyModule origin = method.getDeclaringModule();

        if (!(origin instanceof RubyClass)) { // Module (not Class) methods can always be bound
            return true;
        } else if (module.fields.isRefinement()) {
            RubyModule refinedModule = module.fields.getRefinedModule();
            return refinedModule instanceof RubyClass && ModuleOperations.assignableTo(refinedModule, origin);
        } else {
            return module instanceof RubyClass && ModuleOperations.assignableTo(module, origin);
        }
    }

    @TruffleBoundary
    public static String constantName(RubyContext context, RubyModule module, String name) {
        if (module == context.getCoreLibrary().objectClass) {
            return "::" + name;
        } else {
            return module.fields.getName() + "::" + name;
        }
    }

    public static String constantNameNoLeadingColon(RubyContext context, RubyModule module, String name) {
        if (module == context.getCoreLibrary().objectClass) {
            return name;
        } else {
            return module.fields.getName() + "::" + name;
        }
    }

    @TruffleBoundary
    public static Iterable<Entry<String, RubyConstant>> getAllConstants(RubyModule module) {
        final Map<String, RubyConstant> constants = new HashMap<>();

        // Look in the current module
        for (Map.Entry<String, RubyConstant> constant : module.fields.getConstants()) {
            constants.put(constant.getKey(), constant.getValue());
        }

        // Look in ancestors
        for (RubyModule ancestor : module.fields.prependedAndIncludedModules()) {
            for (Map.Entry<String, RubyConstant> constant : ancestor.fields.getConstants()) {
                constants.putIfAbsent(constant.getKey(), constant.getValue());
            }
        }

        return constants.entrySet();
    }

    /** NOTE: This method returns false for an undefined RubyConstant */
    public static boolean isConstantDefined(RubyConstant constant) {
        return constant != null && !constant.isUndefined() &&
                !(constant.isAutoload() && constant.getAutoloadConstant().isAutoloadingThread());
    }

    /** NOTE: This method might return an undefined RubyConstant */
    private static boolean constantExists(RubyConstant constant, ArrayList<Assumption> assumptions) {
        if (constant != null) {
            if (constant.isAutoload() && constant.getAutoloadConstant().isAutoloading()) {
                // Cannot cache the lookup of an autoloading constant as the result depends on the calling thread
                assumptions.add(NeverValidAssumption.INSTANCE);
                return !constant.getAutoloadConstant().isAutoloadingThread();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    @TruffleBoundary
    public static ConstantLookupResult lookupConstant(RubyContext context, RubyModule module, String name) {
        return lookupConstant(context, module, name, new ArrayList<>());
    }

    @TruffleBoundary
    private static ConstantLookupResult lookupConstant(RubyContext context, RubyModule module, String name,
            ArrayList<Assumption> assumptions) {
        // Look in the current module
        ModuleFields fields = module.fields;
        assumptions.add(fields.getConstantsUnmodifiedAssumption());
        RubyConstant constant = fields.getConstant(name);
        if (constantExists(constant, assumptions)) {
            return new ConstantLookupResult(constant, toArray(assumptions));
        }

        // Look in ancestors
        for (RubyModule ancestor : module.fields.ancestors()) {
            if (ancestor == module) {
                continue;
            }
            fields = ancestor.fields;
            assumptions.add(fields.getConstantsUnmodifiedAssumption());
            constant = fields.getConstant(name);
            if (constantExists(constant, assumptions)) {
                return new ConstantLookupResult(constant, toArray(assumptions));
            }
        }

        // Nothing found
        return new ConstantLookupResult(null, toArray(assumptions));
    }

    @TruffleBoundary
    public static ConstantLookupResult lookupConstantInObject(RubyContext context, String name,
            ArrayList<Assumption> assumptions) {
        final RubyClass objectClass = context.getCoreLibrary().objectClass;

        ModuleFields fields = objectClass.fields;
        assumptions.add(fields.getConstantsUnmodifiedAssumption());
        RubyConstant constant = fields.getConstant(name);
        if (constantExists(constant, assumptions)) {
            return new ConstantLookupResult(constant, toArray(assumptions));
        }

        for (RubyModule ancestor : objectClass.fields.prependedAndIncludedModules()) {
            fields = ancestor.fields;
            assumptions.add(fields.getConstantsUnmodifiedAssumption());
            constant = fields.getConstant(name);
            if (constantExists(constant, assumptions)) {
                return new ConstantLookupResult(constant, toArray(assumptions));
            }
        }

        return new ConstantLookupResult(null, toArray(assumptions));
    }

    @TruffleBoundary
    public static ConstantLookupResult lookupConstantAndObject(RubyContext context, RubyModule module, String name,
            ArrayList<Assumption> assumptions) {
        final ConstantLookupResult constant = lookupConstant(context, module, name, assumptions);
        if (constant.isFound()) {
            return constant;
        }

        // Look in Object and its included modules for modules (not for classes)
        if (!(module instanceof RubyClass)) {
            return lookupConstantInObject(context, name, assumptions);
        } else {
            return constant;
        }
    }

    @TruffleBoundary
    public static ConstantLookupResult lookupConstantWithLexicalScope(RubyContext context, LexicalScope lexicalScope,
            String name) {
        final RubyModule module = lexicalScope.getLiveModule();
        final ArrayList<Assumption> assumptions = new ArrayList<>();

        // Look in lexical scope
        while (lexicalScope != context.getRootLexicalScope()) {
            final ModuleFields fields = lexicalScope.getLiveModule().fields;
            assumptions.add(fields.getConstantsUnmodifiedAssumption());
            final RubyConstant constant = fields.getConstant(name);
            if (constantExists(constant, assumptions)) {
                return new ConstantLookupResult(constant, toArray(assumptions));
            }

            lexicalScope = lexicalScope.getParent();
        }

        return lookupConstantAndObject(context, module, name, assumptions);
    }

    @TruffleBoundary
    public static ConstantLookupResult lookupScopedConstant(RubyContext context, RubyModule module, String fullName,
            boolean inherit, Node currentNode, boolean checkName) {
        int start = 0, next;
        if (fullName.startsWith("::")) {
            module = context.getCoreLibrary().objectClass;
            start += 2;
        }

        while ((next = fullName.indexOf("::", start)) != -1) {
            final String segment = fullName.substring(start, next);
            final ConstantLookupResult constant = lookupConstantWithInherit(
                    context,
                    module,
                    segment,
                    inherit,
                    currentNode,
                    checkName);
            if (!constant.isFound()) {
                return constant;
            } else if (constant.getConstant().getValue() instanceof RubyModule) {
                module = (RubyModule) constant.getConstant().getValue();
            } else {
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().typeError(
                                fullName.substring(0, next) + " does not refer to class/module",
                                currentNode));
            }
            start = next + 2;
        }

        final String lastSegment = fullName.substring(start);
        if (checkName && !Identifiers.isValidConstantName(lastSegment)) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().nameError(
                            StringUtils.format("wrong constant name %s", fullName),
                            module,
                            fullName,
                            currentNode));
        }

        return lookupConstantWithInherit(context, module, lastSegment, inherit, currentNode, checkName);
    }

    public static ConstantLookupResult lookupConstantWithInherit(RubyContext context, RubyModule module, String name,
            boolean inherit, Node currentNode) {
        return lookupConstantWithInherit(context, module, name, inherit, currentNode, false);
    }

    public static ConstantLookupResult lookupConstantWithInherit(RubyContext context, RubyModule module, String name,
            boolean inherit, Node currentNode, boolean checkName) {
        return lookupConstantWithInherit(context, module, name, inherit, currentNode, checkName, true);
    }

    @TruffleBoundary
    public static ConstantLookupResult lookupConstantWithInherit(RubyContext context, RubyModule module, String name,
            boolean inherit, Node currentNode, boolean checkName, boolean lookInObject) {
        if (checkName && !Identifiers.isValidConstantName(name)) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().nameError(
                            StringUtils.format("wrong constant name %s", name),
                            module,
                            name,
                            currentNode));
        }

        final ArrayList<Assumption> assumptions = new ArrayList<>();
        if (inherit) {
            if (lookInObject) {
                return ModuleOperations.lookupConstantAndObject(context, module, name, assumptions);
            } else {
                return ModuleOperations.lookupConstant(context, module, name, assumptions);
            }
        } else {
            final ModuleFields fields = module.fields;
            assumptions.add(fields.getConstantsUnmodifiedAssumption());
            final RubyConstant constant = fields.getConstant(name);
            if (constantExists(constant, assumptions)) {
                return new ConstantLookupResult(constant, toArray(assumptions));
            } else {
                return new ConstantLookupResult(null, toArray(assumptions));
            }
        }
    }

    @TruffleBoundary
    public static Map<String, InternalMethod> getAllMethods(RubyModule module) {
        final Map<String, InternalMethod> methods = new HashMap<>();

        for (RubyModule ancestor : module.fields.ancestors()) {
            for (InternalMethod method : ancestor.fields.getMethods()) {
                methods.putIfAbsent(method.getName(), method);
            }
        }

        if (module.fields.isRefinement()) {
            for (RubyModule ancestor : module.fields.getRefinedModule().fields
                    .ancestors()) {
                for (InternalMethod method : ancestor.fields.getMethods()) {
                    methods.putIfAbsent(method.getName(), method);
                }
            }
        }

        return methods;
    }

    @TruffleBoundary
    public static Map<String, InternalMethod> getMethodsBeforeLogicalClass(RubyModule module) {
        final Map<String, InternalMethod> methods = new HashMap<>();

        ModuleChain chain = module.fields.getFirstModuleChain();

        while (true) {
            if (chain instanceof PrependMarker) {
                // We need to stop if we are entering prepended modules of a class which is not a
                // singleton class. So we look for the PrependMarker explicitly here.
                final RubyModule origin = ((PrependMarker) chain).getOrigin().getActualModule();
                // When we find a class which is not a singleton class, we are done
                if (origin instanceof RubyClass && !((RubyClass) origin).isSingleton) {
                    break;
                } else {
                    chain = chain.getParentModule();
                }
            }

            final RubyModule ancestor = chain.getActualModule();

            // When we find a class which is not a singleton class, we are done
            if (ancestor instanceof RubyClass && !((RubyClass) ancestor).isSingleton) {
                break;
            }

            for (InternalMethod method : ancestor.fields.getMethods()) {
                methods.putIfAbsent(method.getName(), method);
            }

            chain = chain.getParentModule();
        }

        return methods;
    }

    @TruffleBoundary
    public static Map<String, InternalMethod> getMethodsUntilLogicalClass(RubyModule module) {
        final Map<String, InternalMethod> methods = new HashMap<>();

        for (RubyModule ancestor : module.fields.ancestors()) {
            for (InternalMethod method : ancestor.fields.getMethods()) {
                methods.putIfAbsent(method.getName(), method);
            }

            // When we find a class which is not a singleton class, we are done
            if (ancestor instanceof RubyClass && !((RubyClass) ancestor).isSingleton) {
                break;
            }
        }

        return methods;
    }

    @TruffleBoundary
    public static Map<String, InternalMethod> withoutUndefinedMethods(Map<String, InternalMethod> methods) {
        Map<String, InternalMethod> definedMethods = new HashMap<>();
        for (Entry<String, InternalMethod> method : methods.entrySet()) {
            if (!method.getValue().isUndefined()) {
                definedMethods.put(method.getKey(), method.getValue());
            }
        }
        return definedMethods;
    }

    public static MethodLookupResult lookupMethodCached(RubyModule module, String name,
            DeclarationContext declarationContext) {
        return lookupMethodCached(module, null, name, declarationContext);
    }

    @TruffleBoundary
    private static MethodLookupResult lookupMethodCached(RubyModule module, RubyModule lookupTo, String name,
            DeclarationContext declarationContext) {
        final ArrayList<Assumption> assumptions = new ArrayList<>();

        // Look in ancestors
        for (RubyModule ancestor : module.fields.ancestors()) {
            if (ancestor == lookupTo) {
                return new MethodLookupResult(null, toArray(assumptions));
            }
            final RubyModule[] refinements = getRefinementsFor(declarationContext, ancestor);

            if (refinements != null) {
                for (RubyModule refinement : refinements) {
                    // If we have more then one active refinement for C (where C is refined module):
                    //     R1.ancestors = [R1, A, C, ...]
                    //     R2.ancestors = [R2, B, C, ...]
                    //     R3.ancestors = [R3, D, C, ...]
                    // we are only looking up to C
                    // R3 -> D -> R2 -> B -> R1 -> A
                    final MethodLookupResult refinedMethod = lookupMethodCached(
                            refinement,
                            ancestor,
                            name,
                            null);
                    for (Assumption assumption : refinedMethod.getAssumptions()) {
                        assumptions.add(assumption);
                    }
                    if (refinedMethod.isDefined()) {
                        InternalMethod method = rememberUsedRefinements(refinedMethod.getMethod(), declarationContext);
                        return new MethodLookupResult(method, toArray(assumptions));
                    }
                }
            }

            final ModuleFields fields = ancestor.fields;
            final InternalMethod method = fields.getMethodAndAssumption(name, assumptions);

            if (method != null) {
                return new MethodLookupResult(method, toArray(assumptions));
            }
        }

        // Nothing found
        return new MethodLookupResult(null, toArray(assumptions));
    }

    public static InternalMethod lookupMethodUncached(RubyModule module, String name,
            DeclarationContext declarationContext) {
        return lookupMethodUncached(module, null, name, declarationContext);
    }

    @TruffleBoundary
    private static InternalMethod lookupMethodUncached(RubyModule module, RubyModule lookupTo, String name,
            DeclarationContext declarationContext) {

        for (RubyModule ancestor : module.fields.ancestors()) {
            if (ancestor == lookupTo) {
                return null;
            }
            final RubyModule[] refinements = getRefinementsFor(declarationContext, ancestor);

            if (refinements != null) {
                for (RubyModule refinement : refinements) {
                    final InternalMethod refinedMethod = lookupMethodUncached(
                            refinement,
                            ancestor,
                            name,
                            null);
                    if (refinedMethod != null) {
                        return rememberUsedRefinements(refinedMethod, declarationContext);
                    }
                }
            }

            final ModuleFields fields = ancestor.fields;
            final InternalMethod method = fields.getMethod(name);

            if (method != null) {
                return method;
            }
        }

        // Nothing found
        return null;
    }

    public static MethodLookupResult lookupSuperMethod(InternalMethod currentMethod, RubyModule objectMetaClass) {
        final String name = currentMethod.getSharedMethodInfo().getMethodNameForNotBlock(); // use the original name

        Memo<Boolean> foundDeclaringModule = new Memo<>(false);
        return lookupSuperMethod(
                currentMethod.getDeclaringModule(),
                null,
                name,
                objectMetaClass,
                foundDeclaringModule,
                currentMethod.getDeclarationContext(),
                currentMethod.getActiveRefinements());
    }


    @TruffleBoundary
    private static MethodLookupResult lookupSuperMethod(RubyModule declaringModule, RubyModule lookupTo,
            String name, RubyModule objectMetaClass, Memo<Boolean> foundDeclaringModule,
            DeclarationContext declarationContext, DeclarationContext callerDeclaringContext) {
        final ArrayList<Assumption> assumptions = new ArrayList<>();
        final boolean isRefinedMethod = declaringModule.fields.isRefinement();

        for (RubyModule ancestor : objectMetaClass.fields.ancestors()) {
            if (ancestor == lookupTo) {
                return new MethodLookupResult(null, toArray(assumptions));
            }

            final RubyModule[] refinements = getRefinementsFor(declarationContext, callerDeclaringContext, ancestor);

            if (refinements != null) {
                for (RubyModule refinement : refinements) {
                    final MethodLookupResult superMethodInRefinement = lookupSuperMethod(
                            declaringModule,
                            ancestor,
                            name,
                            refinement,
                            foundDeclaringModule,
                            null,
                            null);
                    for (Assumption assumption : superMethodInRefinement.getAssumptions()) {
                        assumptions.add(assumption);
                    }
                    if (superMethodInRefinement.isDefined()) {
                        InternalMethod method = superMethodInRefinement.getMethod();
                        return new MethodLookupResult(
                                rememberUsedRefinements(method, declarationContext, refinements, ancestor),
                                toArray(assumptions));
                    }
                    if (foundDeclaringModule.get() && isRefinedMethod) {
                        // if method is defined in refinement module (R)
                        // we should lookup only in this active refinement and skip other
                        break;
                    }
                }
            }

            if (!foundDeclaringModule.get()) {
                if (ancestor == declaringModule) {
                    foundDeclaringModule.set(true);
                }
            } else {
                final ModuleFields fields = ancestor.fields;
                final InternalMethod method = fields.getMethodAndAssumption(name, assumptions);
                if (method != null) {
                    return new MethodLookupResult(method, toArray(assumptions));
                }
            }
        }

        // Nothing found
        return new MethodLookupResult(null, toArray(assumptions));
    }

    private static InternalMethod rememberUsedRefinements(InternalMethod method,
            DeclarationContext declarationContext) {
        return method.withActiveRefinements(declarationContext);
    }

    private static InternalMethod rememberUsedRefinements(InternalMethod method,
            DeclarationContext declarationContext, RubyModule[] refinements, RubyModule ancestor) {
        assert refinements != null;

        final Map<RubyModule, RubyModule[]> currentRefinements = new HashMap<>(
                declarationContext.getRefinements());
        currentRefinements.put(ancestor, refinements);

        return method.withActiveRefinements(declarationContext.withRefinements(currentRefinements));
    }

    private static RubyModule[] getRefinementsFor(DeclarationContext declarationContext,
            DeclarationContext callerDeclaringContext, RubyModule module) {
        final RubyModule[] lexicalRefinements = getRefinementsFor(declarationContext, module);
        final RubyModule[] callerRefinements = getRefinementsFor(callerDeclaringContext, module);

        if (lexicalRefinements == null) {
            return callerRefinements;
        }

        if (callerRefinements == null) {
            return lexicalRefinements;
        }

        final ArrayList<RubyModule> list = new ArrayList<>(Arrays.asList(callerRefinements));
        for (RubyModule refinement : lexicalRefinements) {
            if (!ArrayUtils.contains(callerRefinements, refinement)) {
                list.add(refinement);
            }
        }
        return list.toArray(RubyModule.EMPTY_ARRAY);
    }

    private static RubyModule[] getRefinementsFor(DeclarationContext declarationContext, RubyModule module) {
        if (declarationContext == null) {
            return null;
        }
        return declarationContext.getRefinementsFor(module);
    }

    private static Assumption[] toArray(ArrayList<Assumption> assumptions) {
        return assumptions.toArray(EMPTY_ASSUMPTION_ARRAY);
    }

    @TruffleBoundary
    public static void setClassVariable(RubyLanguage language, RubyContext context, RubyModule module, String name,
            Object value, Node currentNode) {
        ModuleFields moduleFields = module.fields;
        moduleFields.checkFrozen(context, currentNode);
        SharedObjects.propagate(language, module, value);

        // if the cvar is not already defined we need to take lock and ensure there is only one
        // defined in the class tree
        if (!trySetClassVariable(module, name, value)) {
            synchronized (context.getClassVariableDefinitionLock()) {
                if (!trySetClassVariable(module, name, value)) {
                    moduleFields.getClassVariables().put(name, value, DynamicObjectLibrary.getUncached());
                }
            }
        }
    }

    private static boolean trySetClassVariable(RubyModule topModule, String name, Object value) {
        return classVariableLookup(
                topModule,
                module -> module.fields.getClassVariables().putIfPresent(
                        name,
                        value,
                        DynamicObjectLibrary.getUncached()) ? module : null) != null;
    }

    @TruffleBoundary
    public static Object removeClassVariable(ModuleFields fields, RubyContext context, Node currentNode, String name) {
        fields.checkFrozen(context, currentNode);

        final ClassVariableStorage classVariables = fields.getClassVariables();
        final Object found = classVariables.remove(name, DynamicObjectLibrary.getUncached());
        if (found == null) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().nameErrorClassVariableNotDefined(
                            name,
                            fields.rubyModule,
                            currentNode));
        } else {
            return found;
        }
    }

    @TruffleBoundary
    public static <R> R classVariableLookup(RubyModule module, Function<RubyModule, R> action) {
        // Look in the current module
        R result = action.apply(module);
        if (result != null) {
            return result;
        }

        // If singleton class of a module, check the attached module.
        if (module instanceof RubyClass) {
            RubyClass klass = (RubyClass) module;
            if (klass.isSingleton && klass.attached instanceof RubyModule) {
                module = (RubyModule) klass.attached;

                result = action.apply(module);
                if (result != null) {
                    return result;
                }
            }
        }

        // Look in ancestors
        for (RubyModule ancestor : module.fields.ancestors()) {
            if (ancestor == module) {
                continue;
            }
            result = action.apply(ancestor);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @TruffleBoundary
    public static boolean isMethodPrivateFromName(String name) {
        return (name.equals("initialize") || name.equals("initialize_copy") ||
                name.equals("initialize_clone") || name.equals("initialize_dup") ||
                name.equals("respond_to_missing?"));
    }

}
