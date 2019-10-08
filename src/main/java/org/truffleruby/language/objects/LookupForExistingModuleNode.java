/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import java.util.ArrayList;

import org.truffleruby.Layouts;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantBaseNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class LookupForExistingModuleNode extends LookupConstantBaseNode implements LookupConstantInterface {

    @Child GetConstantNode getConstantNode = GetConstantNode.create(false);

    public Object lookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent) {
        assert RubyGuards.isRubyModule(lexicalParent);

        final LexicalScope lexicalScope = RubyArguments.getMethod(frame).getSharedMethodInfo().getLexicalScope();
        return getConstantNode.lookupAndResolveConstant(lexicalScope, lexicalParent, name, this);
    }

    @Override
    public RubyConstant lookupConstant(LexicalScope lexicalScope, DynamicObject module, String name) {
        final DynamicObject lexicalParent = module;
        return deepConstantSearch(name, lexicalScope, lexicalParent);
    }

    @TruffleBoundary(transferToInterpreterOnException = false)
    private RubyConstant deepConstantSearch(String name, LexicalScope lexicalScope, DynamicObject lexicalParent) {
        final RubyConstant constant = deepConstantLookup(name, lexicalParent);

        if (constant != null) {
            if (!constant.isVisibleTo(getContext(), lexicalScope, lexicalScope.getLiveModule()) &&
                    !constant.isVisibleTo(getContext(), LexicalScope.NONE, lexicalParent)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorPrivateConstant(lexicalParent, name, this));
            }

            if (constant.isDeprecated()) {
                warnDeprecatedConstant(lexicalParent, constant, name);
            }
        }

        return constant;

    }

    private RubyConstant deepConstantLookup(String name, DynamicObject lexicalParent) {
        final RubyConstant constant = Layouts.MODULE.getFields(lexicalParent).getConstant(name);
        if (ModuleOperations.isConstantDefined(constant)) {
            return constant;
        }

        final DynamicObject objectClass = getContext().getCoreLibrary().getObjectClass();
        if (lexicalParent == objectClass) {
            final ConstantLookupResult result = ModuleOperations
                    .lookupConstantInObject(getContext(), name, new ArrayList<>());
            if (result.isFound()) {
                return result.getConstant();
            }
        }

        return null;
    }

}
