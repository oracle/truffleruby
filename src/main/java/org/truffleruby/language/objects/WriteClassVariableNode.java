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

import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.WarnNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class WriteClassVariableNode extends RubyNode {

    private final String name;

    @Child private RubyNode lexicalScopeNode;
    @Child private RubyNode rhs;
    @Child private WarnNode warnNode;

    public WriteClassVariableNode(RubyNode lexicalScopeNode, String name, RubyNode rhs) {
        this.lexicalScopeNode = lexicalScopeNode;
        this.name = name;
        this.rhs = rhs;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object rhsValue = rhs.execute(frame);

        final LexicalScope lexicalScope = (LexicalScope) lexicalScopeNode.execute(frame);
        // TODO CS 21-Feb-16 these two operations are uncached and use loops
        final DynamicObject module = LexicalScope.resolveTargetModuleForClassVariables(lexicalScope);

        ModuleOperations.setClassVariable(getContext(), module, name, rhsValue, this);

        if (lexicalScope.getParent() == null) {
            warnTopLevelClassVariableAccess();
        }

        return rhsValue;
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return coreStrings().ASSIGNMENT.createInstance();
    }

    private void warnTopLevelClassVariableAccess() {
        if (warnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnNode = insert(new WarnNode());
        }
        warnNode.warningMessage(getSourceSection(), "class variable access from toplevel");
    }

}
