/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.klass;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.InitializeClassNode;
import org.truffleruby.language.objects.InitializeClassNodeGen;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

import static org.truffleruby.language.RubyBaseNode.nil;

@CoreModule(value = "Class", isClass = true)
public abstract class ClassNodes {

    /** Special constructor for class Class */
    @TruffleBoundary
    public static RubyClass createClassClassAndBootClasses(RubyLanguage language) {
        final RubyClass rubyClass = new RubyClass(language, language.classShape);

        assert rubyClass.getLogicalClass() == rubyClass;
        assert rubyClass.getMetaClass() == rubyClass;

        return rubyClass;
    }

    /** This constructor supports initialization and solves boot-order problems and should not normally be used from
     * outside this class. */
    @TruffleBoundary
    public static RubyClass createBootClass(RubyLanguage language, RubyClass classClass, Object superclass,
            String name) {
        return new RubyClass(classClass, language, null, null, name, false, null, superclass);
    }

    @TruffleBoundary
    public static RubyClass createSingletonClassOfObject(RubyContext context, SourceSection sourceSection,
            RubyClass superclass, RubyDynamicObject attached) {
        // We also need to create the singleton class of a singleton class for proper lookup and consistency.
        // See rb_singleton_class() documentation in MRI.
        // Allocator is null here, we cannot create instances of singleton classes.
        assert attached != null;
        final RubyClass rubyClass = createRubyClass(
                context,
                sourceSection,
                getClassClass(superclass),
                null,
                superclass,
                null,
                true,
                attached);
        return ensureItHasSingletonClassCreated(context, rubyClass);
    }

    @TruffleBoundary
    public static RubyClass createInitializedRubyClass(RubyContext context, SourceSection sourceSection,
            RubyModule lexicalParent, RubyClass superclass, String name) {
        assert superclass != null;
        final RubyClass rubyClass = createRubyClass(
                context,
                sourceSection,
                getClassClass(superclass),
                lexicalParent,
                superclass,
                name,
                false,
                null);
        return ensureItHasSingletonClassCreated(context, rubyClass);
    }

    @TruffleBoundary
    private static RubyClass createRubyClass(RubyContext context,
            SourceSection sourceSection,
            RubyClass classClass,
            RubyModule lexicalParent,
            RubyClass superclass,
            String name,
            boolean isSingleton,
            RubyDynamicObject attached) {
        assert superclass != null;
        final RubyClass rubyClass = new RubyClass(
                classClass,
                context.getLanguageSlow(),
                sourceSection,
                lexicalParent,
                name,
                isSingleton,
                attached,
                superclass);

        if (lexicalParent != null) {
            rubyClass.fields.getAdoptedByLexicalParent(context, lexicalParent, name, null);
        }

        return rubyClass;
    }

    @TruffleBoundary
    public static void initialize(RubyContext context, RubyClass rubyClass) {
        assert !rubyClass.isSingleton : "Singleton classes can only be created internally";

        ensureItHasSingletonClassCreated(context, rubyClass);
    }

    private static RubyClass ensureItHasSingletonClassCreated(RubyContext context, RubyClass rubyClass) {
        getLazyCreatedSingletonClass(context, rubyClass);
        return rubyClass;
    }

    @TruffleBoundary
    public static RubyClass getSingletonClass(RubyContext context, RubyClass rubyClass) {
        // We also need to create the singleton class of a singleton class for proper lookup and consistency.
        // See rb_singleton_class() documentation in MRI.
        return ensureItHasSingletonClassCreated(context, getLazyCreatedSingletonClass(context, rubyClass));
    }

    public static RubyClass getSingletonClassOrNull(RubyContext context, RubyClass rubyClass) {
        RubyClass metaClass = rubyClass.getMetaClass();
        if (metaClass.isSingleton) {
            return ensureItHasSingletonClassCreated(context, metaClass);
        } else {
            return null;
        }
    }

    private static RubyClass getLazyCreatedSingletonClass(RubyContext context, RubyClass rubyClass) {
        synchronized (rubyClass) {
            RubyClass metaClass = rubyClass.getMetaClass();
            if (metaClass.isSingleton) {
                return metaClass;
            }

            return createSingletonClass(context, rubyClass);
        }
    }

    @TruffleBoundary
    private static RubyClass createSingletonClass(RubyContext context, RubyClass rubyClass) {
        final RubyClass singletonSuperclass;
        final Object superclass = rubyClass.superclass;
        if (superclass == nil) {
            singletonSuperclass = rubyClass.getLogicalClass();
        } else {
            singletonSuperclass = getLazyCreatedSingletonClass(context, (RubyClass) superclass);
        }

        RubyClass metaClass = ClassNodes.createRubyClass(
                context,
                rubyClass.fields.getSourceSection(),
                getClassClass(rubyClass),
                null,
                singletonSuperclass,
                null,
                true,
                rubyClass);
        SharedObjects.propagate(context.getLanguageSlow(), rubyClass, metaClass);
        rubyClass.setMetaClass(metaClass);

        return rubyClass.getMetaClass();
    }

    /** The same as {@link CoreLibrary#classClass} but available while executing the CoreLibrary constructor */
    private static RubyClass getClassClass(RubyClass rubyClass) {
        return rubyClass.getLogicalClass();
    }

    @Primitive(name = "class_new")
    public abstract static class NewClassNode extends PrimitiveArrayArgumentsNode {

        @Child private InitializeClassNode initializeClassNode;
        private final BranchProfile errorProfile = BranchProfile.create();

        @Specialization
        protected RubyClass newClass(RubyClass superclass, boolean callInherited, Object maybeBlock) {
            if (superclass.isSingleton) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassSingletonClass(this));
            }
            if (superclass == coreLibrary().classClass) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassClass(this));
            }

            final RubyClass newRubyClass = new RubyClass(
                    coreLibrary().classClass,
                    getLanguage(),
                    getEncapsulatingSourceSection(),
                    null,
                    null,
                    false,
                    null,
                    superclass);

            if (initializeClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                initializeClassNode = insert(InitializeClassNodeGen.create());
            }

            initializeClassNode.executeInitialize(newRubyClass, superclass, callInherited, maybeBlock);

            return newRubyClass;
        }

        @Specialization(guards = "!isRubyClass(superclass)")
        protected RubyClass newClass(Object superclass, boolean callInherited, Object maybeBlock) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorSuperclassMustBeClass(this));
        }
    }

    /** #allocate should only be defined as an instance method of Class (Class#allocate), which is required for
     * compatibility. __allocate__ is our version of the "allocation function" as defined by rb_define_alloc_func() in
     * MRI to define how to create instances of specific classes. */
    @CoreMethod(names = "allocate")
    public abstract static class AllocateInstanceNode extends CoreMethodArrayArgumentsNode {

        @Child private DispatchNode allocateNode = DispatchNode.create();

        @Specialization(guards = "!rubyClass.isSingleton")
        protected Object newInstance(RubyClass rubyClass) {
            return allocateNode.call(rubyClass, "__allocate__");
        }

        @Specialization(guards = "rubyClass.isSingleton")
        protected RubyClass newSingletonInstance(RubyClass rubyClass) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
        }
    }

    // Worth always splitting to have monomorphic #__allocate__ and #initialize,
    // Worth always inlining as the field accesses and initializations are optimized when the allocation is visible,
    // and a non-inlined call to #__allocate__ would allocate the arguments Object[] which is about the same number of
    // nodes as the object allocation. Also avoids many frame and Object[] allocations when creating a new object.
    @GenerateUncached
    @CoreMethod(names = "new", rest = true, alwaysInlined = true)
    public abstract static class NewNode extends AlwaysInlinedMethodNode {
        @Specialization(guards = "!rubyClass.isSingleton")
        protected Object newInstance(Frame callerFrame, RubyClass rubyClass, Object[] rubyArgs, RootCallTarget target,
                @Cached DispatchNode allocateNode,
                @Cached DispatchNode initializeNode) {
            final Object instance = allocateNode.call(rubyClass, "__allocate__");
            initializeNode.dispatch(null, instance, "initialize", RubyArguments.repack(rubyArgs, instance));
            return instance;
        }

        @Specialization(guards = "rubyClass.isSingleton")
        protected RubyClass newSingletonInstance(
                Frame callerFrame, RubyClass rubyClass, Object[] rubyArgs, RootCallTarget target) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().typeErrorCantCreateInstanceOfSingletonClass(this));
        }
    }

    @CoreMethod(names = "initialize", optional = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object initialize(RubyClass rubyClass, Object maybeSuperclass) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().typeErrorAlreadyInitializedClass(this));
        }
    }

    @CoreMethod(names = "inherited", needsSelf = false, required = 1, visibility = Visibility.PRIVATE)
    public abstract static class InheritedNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object inherited(Object subclass) {
            return nil;
        }
    }

    @CoreMethod(names = "superclass")
    public abstract static class SuperClassNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object getSuperClass(RubyClass rubyClass) {
            return rubyClass.superclass;
        }
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }
    }
}
