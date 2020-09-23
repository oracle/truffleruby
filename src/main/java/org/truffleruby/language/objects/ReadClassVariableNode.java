/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import org.truffleruby.RubyContext;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class ReadClassVariableNode extends RubyContextSourceNode {

    private final String name;
    private final BranchProfile missingProfile = BranchProfile.create();

    @Child private RubyNode lexicalScopeNode;
    @Child private WarnNode warnNode;

    public ReadClassVariableNode(RubyNode lexicalScopeNode, String name) {
        this.lexicalScopeNode = lexicalScopeNode;
        this.name = name;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final LexicalScope lexicalScope = (LexicalScope) lexicalScopeNode.execute(frame);
        // TODO CS 21-Feb-16 these two operations are uncached and use loops - same for isDefined below
        final RubyModule module = LexicalScope.resolveTargetModuleForClassVariables(lexicalScope);

        final Object value = ModuleOperations.lookupClassVariable(module, name);

        if (value == null) {
            missingProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameErrorUninitializedClassVariable(module, name, this));
        }

        if (lexicalScope.getParent() == null) {
            warnTopLevelClassVariableAccess();
        }

        return value;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        final LexicalScope lexicalScope = (LexicalScope) lexicalScopeNode.execute(frame);
        final RubyModule module = LexicalScope.resolveTargetModuleForClassVariables(lexicalScope);

        final Object value = ModuleOperations.lookupClassVariable(module, name);

        if (lexicalScope.getParent() == null) {
            warnTopLevelClassVariableAccess();
        }

        if (value == null) {
            return nil;
        } else {
            return coreStrings().CLASS_VARIABLE.createInstance(context);
        }
    }

    private void warnTopLevelClassVariableAccess() {
        if (warnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnNode = insert(new WarnNode());
        }

        if (warnNode.shouldWarn()) {
            warnNode.warningMessage(getSourceSection(), "class variable access from toplevel");
        }
    }

}
