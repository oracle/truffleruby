/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;

public abstract class InitializeClassNode extends RubyBaseNode {

    private final boolean callInherited;
    private final BranchProfile errorProfile = BranchProfile.create();

    @Child private ModuleNodes.InitializeNode moduleInitializeNode;
    @Child private DispatchNode inheritedNode;

    public InitializeClassNode(boolean callInherited) {
        this.callInherited = callInherited;
    }

    public abstract RubyClass executeInitialize(RubyClass rubyClass, Object superclass, Object block);

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, NotProvided superclass, Nil block) {
        return initializeGeneralWithoutBlock(rubyClass, coreLibrary().objectClass, false);
    }

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, NotProvided superclass, RubyProc block) {
        return initializeGeneralWithBlock(rubyClass, coreLibrary().objectClass, block, false);
    }

    @Specialization
    protected RubyClass initialize(RubyClass rubyClass, RubyClass superclass, Nil block) {
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
        if (rubyClass.isInitialized()) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().typeErrorAlreadyInitializedClass(this));
        }

        if (superClassProvided) {
            checkInheritable(superclass);
            if (!superclass.isInitialized()) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().typeErrorInheritUninitializedClass(this));
            }
        }

        ClassNodes.initialize(getContext(), rubyClass, superclass);
    }

    // rb_check_inheritable
    private void checkInheritable(RubyClass superClass) {
        if (superClass.isSingleton) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassSingletonClass(this));
        }
        if (superClass == coreLibrary().classClass) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorSubclassClass(this));
        }
    }

    private void triggerInheritedHook(RubyClass subClass, RubyClass superClass) {
        if (inheritedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            inheritedNode = insert(DispatchNode.create());
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
