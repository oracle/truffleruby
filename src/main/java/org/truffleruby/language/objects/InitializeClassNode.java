/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

public abstract class InitializeClassNode extends RubyBaseNode {

    private final boolean callInherited;

    @Child private ModuleNodes.InitializeNode moduleInitializeNode;
    @Child private CallDispatchHeadNode inheritedNode;

    public InitializeClassNode(boolean callInherited) {
        this.callInherited = callInherited;
    }

    public abstract DynamicObject executeInitialize(DynamicObject rubyClass, Object superclass, Object block);

    @Specialization
    public DynamicObject initialize(DynamicObject rubyClass, NotProvided superclass, NotProvided block) {
        return initializeGeneralWithoutBlock(rubyClass, coreLibrary().getObjectClass(), false);
    }

    @Specialization
    public DynamicObject initialize(DynamicObject rubyClass, NotProvided superclass, DynamicObject block) {
        return initializeGeneralWithBlock(rubyClass, coreLibrary().getObjectClass(), block, false);
    }

    @Specialization(guards = "isRubyClass(superclass)")
    public DynamicObject initialize(DynamicObject rubyClass, DynamicObject superclass, NotProvided block) {
        return initializeGeneralWithoutBlock(rubyClass, superclass, true);
    }

    @Specialization(guards = "isRubyClass(superclass)")
    public DynamicObject initialize(DynamicObject rubyClass, DynamicObject superclass, DynamicObject block) {
        return initializeGeneralWithBlock(rubyClass, superclass, block, true);
    }

    @Specialization(guards = { "!isRubyClass(superclass)", "wasProvided(superclass)" })
    public DynamicObject initializeNotClass(DynamicObject rubyClass, Object superclass, Object maybeBlock) {
        throw new RaiseException(getContext(), coreExceptions().typeErrorSuperclassMustBeClass(this));
    }

    private DynamicObject initializeGeneralWithoutBlock(DynamicObject rubyClass, DynamicObject superclass, boolean superClassProvided) {
        assert RubyGuards.isRubyClass(rubyClass);
        assert RubyGuards.isRubyClass(superclass);

        if (isInitialized(rubyClass)) {
            throw new RaiseException(getContext(), getContext().getCoreExceptions().typeErrorAlreadyInitializedClass(this));
        }

        if (superClassProvided) {
            checkInheritable(superclass);
            if (!isInitialized(superclass)) {
                throw new RaiseException(getContext(), getContext().getCoreExceptions().typeErrorInheritUninitializedClass(this));
            }
        }

        ClassNodes.initialize(getContext(), rubyClass, superclass);

        if (callInherited) {
            triggerInheritedHook(rubyClass, superclass);
        }

        return rubyClass;
    }

    private DynamicObject initializeGeneralWithBlock(DynamicObject rubyClass, DynamicObject superclass, DynamicObject block, boolean superClassProvided) {
        assert RubyGuards.isRubyClass(superclass);

        if (isInitialized(rubyClass)) {
            throw new RaiseException(getContext(), getContext().getCoreExceptions().typeErrorAlreadyInitializedClass(this));
        }
        if (superClassProvided) {
            checkInheritable(superclass);
            if (!isInitialized(superclass)) {
                throw new RaiseException(getContext(), getContext().getCoreExceptions().typeErrorInheritUninitializedClass(this));
            }
        }

        ClassNodes.initialize(getContext(), rubyClass, superclass);
        triggerInheritedHook(rubyClass, superclass);
        moduleInitialize(rubyClass, block);

        return rubyClass;
    }

    private boolean isInitialized(DynamicObject rubyClass) {
        return Layouts.CLASS.getSuperclass(rubyClass) != null;
    }

    // rb_check_inheritable
    private void checkInheritable(DynamicObject superClass) {
        if (!RubyGuards.isRubyClass(superClass)) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorSuperclassMustBeClass(this));
        }
        if (Layouts.CLASS.getIsSingleton(superClass)) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassSingletonClass(this));
        }
        if (superClass == coreLibrary().getClassClass()) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassClass(this));
        }
    }

    private void triggerInheritedHook(DynamicObject subClass, DynamicObject superClass) {
        if (inheritedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            inheritedNode = insert(CallDispatchHeadNode.createPrivate());
        }
        inheritedNode.call(superClass, "inherited", subClass);
    }

    private void moduleInitialize(DynamicObject rubyClass, DynamicObject block) {
        if (moduleInitializeNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            moduleInitializeNode = insert(ModuleNodesFactory.InitializeNodeFactory.create(null));
        }
        moduleInitializeNode.executeInitialize(rubyClass, block);
    }

}
