/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import java.util.ArrayList;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantBaseNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;

public class LookupForExistingModuleNode extends LookupConstantBaseNode implements LookupConstantInterface {

    @Child GetConstantNode getConstantNode = GetConstantNode.create(false);

    public Object lookupForExistingModule(VirtualFrame frame, String name, RubyModule lexicalParent) {
        final LexicalScope lexicalScope = RubyArguments.getMethod(frame).getLexicalScope();
        return getConstantNode.lookupAndResolveConstant(lexicalScope, lexicalParent, name, this);
    }

    @Override
    public RubyConstant lookupConstant(LexicalScope lexicalScope, RubyModule module, String name, boolean checkName) {
        final RubyModule lexicalParent = module;
        return deepConstantSearch(name, lexicalScope, lexicalParent);
    }

    @TruffleBoundary
    private RubyConstant deepConstantSearch(String name, LexicalScope lexicalScope, RubyModule lexicalParent) {
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

    private RubyConstant deepConstantLookup(String name, RubyModule lexicalParent) {
        final RubyConstant constant = lexicalParent.fields.getConstant(name);
        if (ModuleOperations.isConstantDefined(constant)) {
            return constant;
        }

        final RubyClass objectClass = getContext().getCoreLibrary().objectClass;
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
