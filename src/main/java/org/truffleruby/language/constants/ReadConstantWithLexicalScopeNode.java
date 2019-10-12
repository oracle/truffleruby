/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.Layouts;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

/** Read a constant using the current lexical scope: CONST */
public class ReadConstantWithLexicalScopeNode extends RubyNode {

    private final LexicalScope lexicalScope;
    private final String name;

    @Child private LookupConstantWithLexicalScopeNode lookupConstantNode;
    @Child private GetConstantNode getConstantNode = GetConstantNode.create();

    public ReadConstantWithLexicalScopeNode(LexicalScope lexicalScope, String name) {
        this.lexicalScope = lexicalScope;
        this.name = name;
        this.lookupConstantNode = LookupConstantWithLexicalScopeNodeGen.create(lexicalScope, name);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final DynamicObject module = lexicalScope.getLiveModule();

        return getConstantNode.lookupAndResolveConstant(lexicalScope, module, name, lookupConstantNode);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        final RubyConstant constant;
        try {
            constant = lookupConstantNode.executeLookupConstant();
        } catch (RaiseException e) {
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().getNameErrorClass()) {
                // private constant
                return nil();
            }
            throw e;
        }

        if (ModuleOperations.isConstantDefined(constant)) {
            return coreStrings().CONSTANT.createInstance();
        } else {
            return nil();
        }
    }

}
