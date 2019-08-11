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

import org.truffleruby.Layouts;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class DefineClassNode extends RubyNode {

    protected final String name;

    @Child private RubyNode superClassNode;
    @Child private RubyNode lexicalParentModule;
    @Child private LookupForExistingModuleNode lookupForExistingModuleNode;
    @Child private CallDispatchHeadNode inheritedNode;

    private final ConditionProfile needToDefineProfile = ConditionProfile.createBinaryProfile();
    private final ConditionProfile noSuperClassSupplied = ConditionProfile.createBinaryProfile();
    private final BranchProfile errorProfile = BranchProfile.create();

    public DefineClassNode(
            String name,
            RubyNode lexicalParent, RubyNode superClass) {
        this.name = name;
        this.lexicalParentModule = lexicalParent;
        this.superClassNode = superClass;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object lexicalParentObject = lexicalParentModule.execute(frame);

        if (!RubyGuards.isRubyModule(lexicalParentObject)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorIsNotA(lexicalParentObject, "module", this));
        }

        final DynamicObject lexicalParentModule = (DynamicObject) lexicalParentObject;
        final DynamicObject suppliedSuperClass = executeSuperClass(frame);
        final Object existing = lookupForExistingModule(frame, name, lexicalParentModule);

        final DynamicObject definedClass;

        if (needToDefineProfile.profile(existing == null)) {
            final DynamicObject superClass;
            if (noSuperClassSupplied.profile(suppliedSuperClass == null)) {
                superClass = getContext().getCoreLibrary().getObjectClass();
            } else {
                superClass = suppliedSuperClass;
            }
            definedClass = ClassNodes.createInitializedRubyClass(getContext(), getEncapsulatingSourceSection(), lexicalParentModule, superClass, name);
            callInherited(frame, superClass, definedClass);
        } else {
            if (!RubyGuards.isRubyClass(existing)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeErrorIsNotA(existing, "class", this));
            }

            definedClass = (DynamicObject) existing;

            final DynamicObject currentSuperClass = ClassNodes.getSuperClass(definedClass);

            if (suppliedSuperClass != null && currentSuperClass != suppliedSuperClass) { // bug-compat with MRI https://bugs.ruby-lang.org/issues/12367
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().superclassMismatch(
                        Layouts.MODULE.getFields(definedClass).getName(), this));
            }
        }

        return definedClass;
    }

    private DynamicObject executeSuperClass(VirtualFrame frame) {
        if (superClassNode == null) {
            return null;
        }
        final Object superClassObject = superClassNode.execute(frame);

        if (!RubyGuards.isRubyClass(superClassObject)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeError("superclass must be a Class", this));
        }

        final DynamicObject superClass = (DynamicObject) superClassObject;

        if (Layouts.CLASS.getIsSingleton(superClass)) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeError("can't make subclass of virtual class", this));
        }

        return superClass;
    }

    private void callInherited(VirtualFrame frame, DynamicObject superClass, DynamicObject childClass) {
        if (inheritedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            inheritedNode = insert(CallDispatchHeadNode.createPrivate());
        }
        inheritedNode.call(superClass, "inherited", childClass);
    }

    private Object lookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent) {
        if (lookupForExistingModuleNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupForExistingModuleNode = insert(new LookupForExistingModuleNode());
        }
        return lookupForExistingModuleNode.lookupForExistingModule(frame, name, lexicalParent);
    }

}
