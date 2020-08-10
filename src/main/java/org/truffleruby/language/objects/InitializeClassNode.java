/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class InitializeClassNode extends RubyContextNode {

    private final boolean callInherited;

    @Child private ModuleNodes.InitializeNode moduleInitializeNode;
    @Child private CallDispatchHeadNode inheritedNode;

    public InitializeClassNode(boolean callInherited) {
        this.callInherited = callInherited;
    }

    public abstract RubyClass executeInitialize(RubyClass rubyClass, Object superclass, Object block);

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, NotProvided superclass, NotProvided block) {
        return initializeGeneralWithoutBlock(rubyClass, coreLibrary().objectClass, false);
    }

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, NotProvided superclass, RubyProc block) {
        return initializeGeneralWithBlock(rubyClass, coreLibrary().objectClass, block, false);
    }

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, RubyClass superclass, NotProvided block) {
        return initializeGeneralWithoutBlock(rubyClass, superclass, true);
    }

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, RubyClass superclass, RubyProc block) {
        return initializeGeneralWithBlock(rubyClass, superclass, block, true);
    }

    @Specialization(guards = { "!isRubyClass(superclass)", "wasProvided(superclass)" })
    protected RubyClass initializeNotClass(RubyClass rubyClass, Object superclass, Object maybeBlock) {
        throw new RaiseException(getContext(), coreExceptions().typeErrorSuperclassMustBeClass(this));
    }

    private RubyClass initializeGeneralWithoutBlock(RubyClass rubyClass, RubyClass superclass,
            boolean superClassProvided) {
        initializeCommon(rubyClass, superclass, superClassProvided);
        if (callInherited) {
            triggerInheritedHook(rubyClass, superclass);
        }

        return rubyClass;
    }

    private RubyClass initializeGeneralWithBlock(RubyClass rubyClass, RubyClass superclass, RubyProc block,
            boolean superClassProvided) {
        initializeCommon(rubyClass, superclass, superClassProvided);
        triggerInheritedHook(rubyClass, superclass);

        moduleInitialize(rubyClass, block);

        return rubyClass;
    }

    private void initializeCommon(RubyClass rubyClass, RubyClass superclass, boolean superClassProvided) {
        if (isInitialized(rubyClass)) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().typeErrorAlreadyInitializedClass(this));
        }

        if (superClassProvided) {
            checkInheritable(superclass);
            if (!isInitialized(superclass)) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().typeErrorInheritUninitializedClass(this));
            }
        }

        ClassNodes.initialize(getContext(), rubyClass, superclass);
    }

    private boolean isInitialized(RubyClass rubyClass) {
        return rubyClass.superclass != null;
    }

    // rb_check_inheritable
    private void checkInheritable(RubyClass superClass) {
        if (superClass.isSingleton) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassSingletonClass(this));
        }
        if (superClass == coreLibrary().classClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassClass(this));
        }
    }

    private void triggerInheritedHook(RubyClass subClass, RubyClass superClass) {
        if (inheritedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            inheritedNode = insert(CallDispatchHeadNode.createPrivate());
        }
        inheritedNode.call(superClass, "inherited", subClass);
    }

    private void moduleInitialize(RubyClass rubyClass, RubyProc block) {
        if (moduleInitializeNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            moduleInitializeNode = insert(ModuleNodesFactory.InitializeNodeFactory.create(null));
        }
        moduleInitializeNode.executeInitialize(rubyClass, block);
    }

}
