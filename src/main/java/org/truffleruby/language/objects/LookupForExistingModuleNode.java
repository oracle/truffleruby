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
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.module.LoadAutoloadedConstantNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.LookupConstantBaseNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.control.RaiseException;

@NodeChildren({ @NodeChild("name"), @NodeChild("lexicalParent") })
public abstract class LookupForExistingModuleNode extends LookupConstantBaseNode implements LookupConstantInterface {

    @Child private LoadAutoloadedConstantNode loadAutoloadedConstantNode;

    public abstract RubyConstant executeLookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent);

    @Specialization(guards = "isRubyModule(lexicalParent)")
    public RubyConstant lookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent,
            @Cached("createBinaryProfile()") ConditionProfile autoloadProfile) {
        final RubyConstant constant = lookupConstant(frame, lexicalParent, name);

        // If a constant already exists with this class/module name and it's an autoload constant,
        // we have to trigger the autoload behavior before proceeding.
        if (autoloadProfile.profile(constant != null && constant.isAutoload())) {
            loadAutoloadedConstant(name, constant);
            final RubyConstant autoConstant = lookupConstant(frame, lexicalParent, name);

            if (autoConstant == null || autoConstant.isAutoload()) {
                // If it's still an autoload, the autoload failed so let
                // DefineClassNode/DefineModuleNode override the constant
                return null;
            }

            return autoConstant;
        }

        return constant;
    }

    @Override
    public RubyConstant lookupConstant(VirtualFrame frame, Object module, String name) {
        final LexicalScope lexicalScope = RubyArguments.getMethod(frame).getSharedMethodInfo().getLexicalScope();
        final DynamicObject lexicalParent = (DynamicObject) module;
        return deepConstantSearch(name, lexicalScope, lexicalParent);
    }

    @TruffleBoundary(transferToInterpreterOnException = false)
    private RubyConstant deepConstantSearch(String name, LexicalScope lexicalScope, DynamicObject lexicalParent) {
        RubyConstant constant = Layouts.MODULE.getFields(lexicalParent).getConstant(name);

        final DynamicObject objectClass = getContext().getCoreLibrary().getObjectClass();

        if (constant == null && lexicalParent == objectClass) {
            for (DynamicObject included : Layouts.MODULE.getFields(objectClass).prependedAndIncludedModules()) {
                constant = Layouts.MODULE.getFields(included).getConstant(name);

                if (constant != null) {
                    break;
                }
            }
        }

        if (constant != null && !(constant.isVisibleTo(getContext(), lexicalScope, lexicalScope.getLiveModule()) ||
                constant.isVisibleTo(getContext(), LexicalScope.NONE, lexicalParent))) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateConstant(lexicalParent, name, this));
        }

        if (constant != null && constant.isDeprecated()) {
            warnDeprecatedConstant(lexicalParent, constant, name);
        }

        return constant;
    }

    private void loadAutoloadedConstant(String name, RubyConstant constant) {
        if (loadAutoloadedConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            loadAutoloadedConstantNode = insert(new LoadAutoloadedConstantNode());
        }

        loadAutoloadedConstantNode.loadAutoloadedConstant(name, constant);
    }

}
