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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantBaseNode;
import org.truffleruby.language.constants.LookupConstantInterface;
import org.truffleruby.language.control.RaiseException;

public class LookupForExistingModuleNode extends LookupConstantBaseNode implements LookupConstantInterface {

    @Child GetConstantNode getConstantNode = GetConstantNode.create(false);

    public Object lookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent) {
        assert RubyGuards.isRubyModule(lexicalParent);

        final LexicalScope lexicalScope = RubyArguments.getMethod(frame).getSharedMethodInfo().getLexicalScope();
        return getConstantNode.lookupAndResolveConstant(lexicalScope, lexicalParent, name, this);
    }

    @Override
    public RubyConstant lookupConstant(LexicalScope lexicalScope, Object module, String name) {
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

    @Override
    public Object execute(VirtualFrame frame) {
        throw new UnsupportedOperationException();
    }

}
