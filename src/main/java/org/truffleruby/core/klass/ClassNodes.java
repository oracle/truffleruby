/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.klass;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;
import org.truffleruby.language.objects.InitializeClassNode;
import org.truffleruby.language.objects.InitializeClassNodeGen;
import org.truffleruby.language.objects.shared.SharedObjects;

@CoreClass("Class")
public abstract class ClassNodes {

    /**
     * Special constructor for class Class
     */
    @TruffleBoundary
    public static DynamicObject createClassClass(RubyContext context, SourceSection sourceSection) {
        final ModuleFields model = new ModuleFields(context, sourceSection,null, "Class");
        model.setFullName(model.givenBaseName);

        final DynamicObjectFactory tempFactory = Layouts.CLASS.createClassShape(null, null);
        final DynamicObject rubyClass = Layouts.CLASS.createClass(tempFactory, model, false, null, null, null);

        Layouts.BASIC_OBJECT.setLogicalClass(rubyClass, rubyClass);
        Layouts.BASIC_OBJECT.setMetaClass(rubyClass, rubyClass);

        assert RubyGuards.isRubyModule(rubyClass);
        assert RubyGuards.isRubyClass(rubyClass);

        model.rubyModuleObject = rubyClass;

        // Class.new creates instance of Class
        final DynamicObjectFactory instanceFactory = Layouts.CLASS.createClassShape(rubyClass, rubyClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(rubyClass, instanceFactory);

        assert RubyGuards.isRubyModule(rubyClass);
        assert RubyGuards.isRubyClass(rubyClass);

        assert Layouts.MODULE.getFields(rubyClass) == model;
        assert Layouts.BASIC_OBJECT.getLogicalClass(rubyClass) == rubyClass;
        assert Layouts.BASIC_OBJECT.getMetaClass(rubyClass) == rubyClass;

        return rubyClass;
    }

    /**
     * This constructor supports initialization and solves boot-order problems and should not
     * normally be used from outside this class.
     */
    @TruffleBoundary
    public static DynamicObject createBootClass(RubyContext context, SourceSection sourceSection, DynamicObject classClass, DynamicObject superclass, String name) {
        assert RubyGuards.isRubyClass(classClass);
        assert superclass == null || RubyGuards.isRubyClass(superclass);
        final ModuleFields model = new ModuleFields(context, sourceSection, null, name);

        final DynamicObject rubyClass = Layouts.CLASS.createClass(Layouts.CLASS.getInstanceFactory(classClass), model, false, null, null, null);
        assert RubyGuards.isRubyClass(rubyClass) : classClass.getShape().getObjectType().getClass();
        assert RubyGuards.isRubyModule(rubyClass) : classClass.getShape().getObjectType().getClass();

        model.rubyModuleObject = rubyClass;

        final ModuleFields fields = Layouts.MODULE.getFields(rubyClass);
        if (model.lexicalParent == null) { // bootstrap or anonymous module
            fields.setFullName(fields.givenBaseName);
        } else {
            fields.getAdoptedByLexicalParent(context, model.lexicalParent, model.givenBaseName, null);
        }

        if (superclass != null) {
            assert RubyGuards.isRubyClass(superclass);
            assert RubyGuards.isRubyClass(rubyClass);

            fields.parentModule = Layouts.MODULE.getFields(superclass).start;
            Layouts.CLASS.setSuperclass(rubyClass, superclass);

            fields.newHierarchyVersion();
        }

        return rubyClass;
    }

    @TruffleBoundary
    public static DynamicObject createSingletonClassOfObject(RubyContext context, SourceSection sourceSection, DynamicObject superclass, DynamicObject attached, String name) {
        // We also need to create the singleton class of a singleton class for proper lookup and consistency.
        // See rb_singleton_class() documentation in MRI.
        // Allocator is null here, we cannot create instances of singleton classes.
        assert RubyGuards.isRubyClass(superclass);
        assert attached != null;
        return ensureItHasSingletonClassCreated(context, createRubyClass(context, sourceSection, Layouts.BASIC_OBJECT.getLogicalClass(superclass), null, superclass, name, true, attached, true));
    }

    @TruffleBoundary
    public static DynamicObject createInitializedRubyClass(RubyContext context, SourceSection sourceSection, DynamicObject lexicalParent, DynamicObject superclass, String name) {
        final DynamicObject rubyClass = createRubyClass(context, sourceSection, Layouts.BASIC_OBJECT.getLogicalClass(superclass), lexicalParent, superclass, name, false, null, true);
        ensureItHasSingletonClassCreated(context, rubyClass);
        return rubyClass;
    }

    @TruffleBoundary
    public static DynamicObject createRubyClass(RubyContext context,
                                                SourceSection sourceSection,
                                                DynamicObject classClass,
                                                DynamicObject lexicalParent,
                                                DynamicObject superclass,
                                                String name,
                                                boolean isSingleton,
                                                DynamicObject attached,
                                                boolean initialized) {
        assert superclass == null || RubyGuards.isRubyClass(superclass);

        final ModuleFields model = new ModuleFields(context, sourceSection, lexicalParent, name);

        final DynamicObject rubyClass = Layouts.CLASS.createClass(
                Layouts.CLASS.getInstanceFactory(classClass),
                model,
                isSingleton,
                attached,
                null,
                initialized ? superclass : null);
        assert RubyGuards.isRubyClass(rubyClass) : classClass.getShape().getObjectType().getClass();
        assert RubyGuards.isRubyModule(rubyClass) : classClass.getShape().getObjectType().getClass();

        model.rubyModuleObject = rubyClass;

        final ModuleFields fields = Layouts.MODULE.getFields(rubyClass);
        if (model.lexicalParent != null) {
            fields.getAdoptedByLexicalParent(context, model.lexicalParent, model.givenBaseName, null);
        } else if (fields.givenBaseName != null) { // bootstrap module
            fields.setFullName(fields.givenBaseName);
        }

        if (superclass != null) {
            fields.parentModule = Layouts.MODULE.getFields(superclass).start;
            fields.newHierarchyVersion();
        }

        // Singleton classes cannot be instantiated
        if (!isSingleton) {
            setInstanceFactory(rubyClass, superclass);
        }

        return rubyClass;
    }

    @TruffleBoundary
    public static void initialize(RubyContext context, DynamicObject rubyClass, DynamicObject superclass) {
        assert RubyGuards.isRubyClass(superclass);
        assert !Layouts.CLASS.getIsSingleton(rubyClass) : "Singleton classes can only be created internally";

        Layouts.MODULE.getFields(rubyClass).parentModule = Layouts.MODULE.getFields(superclass).start;
        Layouts.MODULE.getFields(rubyClass).newHierarchyVersion();
        ensureItHasSingletonClassCreated(context, rubyClass);

        setInstanceFactory(rubyClass, superclass);

        // superclass is set only here in initialize method to its final value
        Layouts.CLASS.setSuperclass(rubyClass, superclass);
    }

    public static void setInstanceFactory(DynamicObject rubyClass, DynamicObject baseClass) {
        assert !Layouts.CLASS.getIsSingleton(rubyClass) : "Singleton classes cannot be instantiated";
        DynamicObjectFactory factory = Layouts.CLASS.getInstanceFactory(baseClass);
        factory = Layouts.BASIC_OBJECT.setLogicalClass(factory, rubyClass);
        factory = Layouts.BASIC_OBJECT.setMetaClass(factory, rubyClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(rubyClass, factory);
    }

    private static DynamicObject ensureItHasSingletonClassCreated(RubyContext context, DynamicObject rubyClass) {
        getLazyCreatedSingletonClass(context, rubyClass);
        return rubyClass;
    }

    public static DynamicObject getSingletonClass(RubyContext context, DynamicObject rubyClass) {
        // We also need to create the singleton class of a singleton class for proper lookup and consistency.
        // See rb_singleton_class() documentation in MRI.
        return ensureItHasSingletonClassCreated(context, getLazyCreatedSingletonClass(context, rubyClass));
    }

    public static DynamicObject getSingletonClassOrNull(RubyContext context, DynamicObject rubyClass) {
        DynamicObject metaClass = Layouts.BASIC_OBJECT.getMetaClass(rubyClass);
        if (Layouts.CLASS.getIsSingleton(metaClass)) {
            return ensureItHasSingletonClassCreated(context, metaClass);
        } else {
            return null;
        }
    }

    private static DynamicObject getLazyCreatedSingletonClass(RubyContext context, DynamicObject rubyClass) {
        synchronized (rubyClass) {
            DynamicObject metaClass = Layouts.BASIC_OBJECT.getMetaClass(rubyClass);
            if (Layouts.CLASS.getIsSingleton(metaClass)) {
                return metaClass;
            }

            return createSingletonClass(context, rubyClass);
        }
    }

    @TruffleBoundary
    private static DynamicObject createSingletonClass(RubyContext context, DynamicObject rubyClass) {
        final DynamicObject singletonSuperclass;
        if (getSuperClass(rubyClass) == null) {
            singletonSuperclass = Layouts.BASIC_OBJECT.getLogicalClass(rubyClass);
        } else {
            singletonSuperclass = getLazyCreatedSingletonClass(context, getSuperClass(rubyClass));
        }

        String name = StringUtils.format("#<Class:%s>", Layouts.MODULE.getFields(rubyClass).getName());
        DynamicObject metaClass = ClassNodes.createRubyClass(context, Layouts.MODULE.getFields(rubyClass).getSourceSection(), Layouts.BASIC_OBJECT.getLogicalClass(rubyClass), null, singletonSuperclass, name, true, rubyClass, true);
        SharedObjects.propagate(context, rubyClass, metaClass);
        Layouts.BASIC_OBJECT.setMetaClass(rubyClass, metaClass);

        return Layouts.BASIC_OBJECT.getMetaClass(rubyClass);
    }

    public static DynamicObject getSuperClass(DynamicObject rubyClass) {
        CompilerAsserts.neverPartOfCompilation();

        for (DynamicObject ancestor : Layouts.MODULE.getFields(rubyClass).parentAncestors()) {
            if (RubyGuards.isRubyClass(ancestor) && ancestor != rubyClass) {
                return ancestor;
            }
        }

        return null;
    }

    @CoreMethod(names = "allocate")
    public abstract static class AllocateInstanceNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode allocateNode = DispatchHeadNodeFactory.createMethodCallOnSelf();

        @Specialization
        public Object newInstance(VirtualFrame frame, DynamicObject rubyClass) {
            return allocateNode.call(frame, rubyClass, "__allocate__");
        }
    }

    @CoreMethod(names = "new", needsBlock = true, rest = true)
    public abstract static class NewNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode allocateNode = DispatchHeadNodeFactory.createMethodCallOnSelf();
        @Child private CallDispatchHeadNode initialize = DispatchHeadNodeFactory.createMethodCallOnSelf();

        @Specialization
        public Object newInstance(VirtualFrame frame, DynamicObject rubyClass, Object[] args, NotProvided block) {
            return doNewInstance(frame, rubyClass, args, null);
        }

        @Specialization
        public Object newInstance(VirtualFrame frame, DynamicObject rubyClass, Object[] args, DynamicObject block) {
            return doNewInstance(frame, rubyClass, args, block);
        }

        private Object doNewInstance(VirtualFrame frame, DynamicObject rubyClass, Object[] args, DynamicObject block) {
            final Object instance = allocateNode.call(frame, rubyClass, "__allocate__");
            initialize.callWithBlock(frame, instance, "initialize", block, args);
            return instance;
        }
    }

    @CoreMethod(names = "initialize", optional = 1, needsBlock = true)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Child private InitializeClassNode initializeClassNode;

        @Specialization
        public DynamicObject initialize(VirtualFrame frame, DynamicObject rubyClass, Object maybeSuperclass, Object maybeBlock) {
            if (initializeClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                initializeClassNode = insert(InitializeClassNodeGen.create(true, null, null, null));
            }

            return initializeClassNode.executeInitialize(frame, rubyClass, maybeSuperclass, maybeBlock);
        }

    }

    @CoreMethod(names = "inherited", needsSelf = false, required = 1, visibility = Visibility.PRIVATE)
    public abstract static class InheritedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject inherited(Object subclass) {
            return nil();
        }

    }

    @CoreMethod(names = "superclass")
    public abstract static class SuperClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "rubyClass == cachedRubyCLass", "cachedSuperclass != null" }, limit = "getCacheLimit()")
        public Object getSuperClass(DynamicObject rubyClass,
                                    @Cached("rubyClass") DynamicObject cachedRubyCLass,
                                    @Cached("fastLookUp(cachedRubyCLass)") DynamicObject cachedSuperclass) {
            // caches only initialized classes, just allocated will go through slow look up
            return cachedSuperclass;
        }

        @Specialization(replaces = "getSuperClass")
        DynamicObject getSuperClassUncached(DynamicObject rubyClass,
                @Cached("create()") BranchProfile errorProfile) {
            final DynamicObject superclass = fastLookUp(rubyClass);
            if (superclass != null) {
                return superclass;
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext().getCoreExceptions().typeError("uninitialized class", this));
            }
        }

        protected DynamicObject fastLookUp(DynamicObject rubyClass) {
            return Layouts.CLASS.getSuperclass(rubyClass);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().CLASS_CACHE;
        }
    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject allocate(DynamicObject classClass) {
            assert classClass == coreLibrary().getClassClass() : "Subclasses of class Class are forbidden in Ruby";
            return createRubyClass(getContext(), getEncapsulatingSourceSection(), coreLibrary().getClassClass(), null, coreLibrary().getObjectClass(), null, false, null, false);
        }

    }
}
