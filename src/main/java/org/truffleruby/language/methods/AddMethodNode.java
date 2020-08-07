/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.SingletonClassNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class AddMethodNode extends RubyContextNode {

    public static AddMethodNode create(boolean ignoreNameVisibility) {
        return AddMethodNodeGen.create(ignoreNameVisibility);
    }

    // Some method names such as #initialize imply that the method is private - this flag says to ignore that implication
    private final boolean ignoreNameVisibility;

    @Child private SingletonClassNode singletonClassNode;

    public AddMethodNode(boolean ignoreNameVisibility) {
        this.ignoreNameVisibility = ignoreNameVisibility;
    }

    public abstract void executeAddMethod(RubyModule module, InternalMethod method, Visibility visibility);

    @TruffleBoundary
    @Specialization
    protected void addMethod(RubyModule module, InternalMethod method, Visibility visibility) {
        if (!ignoreNameVisibility && ModuleOperations.isMethodPrivateFromName(method.getName())) {
            visibility = Visibility.PRIVATE;
        }

        doAddMethod(module, method, visibility);
    }

    private void doAddMethod(RubyModule module, InternalMethod method, Visibility visibility) {
        if (visibility == Visibility.MODULE_FUNCTION) {
            addMethodToModule(module, method.withVisibility(Visibility.PRIVATE));
            final RubyClass singletonClass = getSingletonClass(module);
            addMethodToModule(
                    singletonClass,
                    method.withDeclaringModule(singletonClass).withVisibility(Visibility.PUBLIC));
        } else {
            addMethodToModule(module, method.withVisibility(visibility));
        }
    }

    public void addMethodToModule(RubyModule module, InternalMethod method) {
        module.fields.addMethod(getContext(), this, method);
    }

    protected RubyClass getSingletonClass(RubyModule module) {
        if (singletonClassNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            singletonClassNode = insert(SingletonClassNode.create());
        }

        return singletonClassNode.executeSingletonClass(module);
    }

}
