/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.SingletonClassNodeGen;

public abstract class AddMethodNode extends RubyBaseNode {

    public static AddMethodNode create(boolean ignoreNameVisibility) {
        return AddMethodNodeGen.create(ignoreNameVisibility);
    }

    // Some method names such as #initialize imply that the method is private - this flag says to ignore that implication
    private final boolean ignoreNameVisibility;

    @Child private SingletonClassNode singletonClassNode;
    @Child private LookupMethodNode lookupMethodNode;

    public AddMethodNode(boolean ignoreNameVisibility) {
        this.ignoreNameVisibility = ignoreNameVisibility;
    }

    public abstract void executeAddMethod(DynamicObject module, InternalMethod method, Visibility visibility);

    @TruffleBoundary
    @Specialization(guards = "isRubyModule(module)")
    public void addMethod(DynamicObject module, InternalMethod method, Visibility visibility) {
        if (!ignoreNameVisibility && ModuleOperations.isMethodPrivateFromName(method.getName())) {
            visibility = Visibility.PRIVATE;
        }

        if (Layouts.MODULE.getFields(module).isRefinement()) {
            final DynamicObject refinedClass = Layouts.MODULE.getFields(module).getRefinedClass();
            addRefinedMethodEntry(refinedClass, method, visibility);
        }

        doAddMethod(module, method, visibility);
    }

    @TruffleBoundary
    private void addRefinedMethodEntry(DynamicObject module, InternalMethod method, Visibility visibility) {
        final MethodLookupResult result = ModuleOperations.lookupMethodCached(module, method.getName(), null);
        final InternalMethod originalMethod = result.getMethod();
        if (originalMethod == null) {
            doAddMethod(module, method.withRefined(true).withOriginalMethod(null), visibility);
        } else if (originalMethod.isRefined()) {
            // Already marked as refined
        } else {
            doAddMethod(module, originalMethod.withRefined(true).withOriginalMethod(originalMethod), visibility);
        }
    }

    private void doAddMethod(DynamicObject module, InternalMethod method, Visibility visibility) {
        if (visibility == Visibility.MODULE_FUNCTION) {
            addMethodToModule(module, method.withVisibility(Visibility.PRIVATE));
            final DynamicObject singletonClass = getSingletonClass(module);
            addMethodToModule(singletonClass, method.withDeclaringModule(singletonClass).withVisibility(Visibility.PUBLIC));
        } else {
            addMethodToModule(module, method.withVisibility(visibility));
        }
    }

    public void addMethodToModule(DynamicObject module, InternalMethod method) {
        Layouts.MODULE.getFields(module).addMethod(getContext(), this, method);
    }

    protected DynamicObject getSingletonClass(DynamicObject object) {
        if (singletonClassNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            singletonClassNode = insert(SingletonClassNodeGen.create(null));
        }

        return singletonClassNode.executeSingletonClass(object);
    }

}
