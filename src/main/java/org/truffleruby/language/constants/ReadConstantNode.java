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

import org.truffleruby.RubyContext;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.profiles.BranchProfile;

/** Read a literal constant on a given module: MOD::CONST */
public class ReadConstantNode extends RubyContextSourceNode {

    private final String name;
    private final BranchProfile notModuleProfile = BranchProfile.create();

    @Child private RubyNode moduleNode;
    @Child private LookupConstantNode lookupConstantNode;
    @Child private GetConstantNode getConstantNode;

    public ReadConstantNode(RubyNode moduleNode, String name) {
        this.name = name;
        this.moduleNode = moduleNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object moduleObject = moduleNode.execute(frame);
        return lookupAndGetConstant(checkModule(moduleObject));
    }

    private Object lookupAndGetConstant(RubyModule module) {
        if (getConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getConstantNode = insert(GetConstantNode.create());
        }

        return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, getLookupConstantNode());
    }

    private LookupConstantNode getLookupConstantNode() {
        if (lookupConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupConstantNode = insert(LookupConstantNode.create(false, true, false));
        }
        return lookupConstantNode;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        // TODO (eregon, 17 May 2016): We execute moduleNode twice here but we both want to make sure the LHS is defined and get the result value.
        // Possible solution: have a isDefinedAndReturnValue()?
        final Object isModuleDefined = moduleNode.isDefined(frame, context);
        if (isModuleDefined == nil) {
            return nil;
        }

        final Object moduleObject;
        try {
            moduleObject = moduleNode.execute(frame);
        } catch (RaiseException e) {
            // If reading the module raised an exception, it must have been an autoloaded module that failed while
            // loading. MRI dictates that in this case we should swallow the exception and return `nil`.
            return nil;
        }

        final RubyModule module = checkModule(moduleObject);
        final RubyConstant constant;
        try {
            constant = getLookupConstantNode().lookupConstant(LexicalScope.IGNORE, module, name);
        } catch (RaiseException e) {
            if (e.getException().getLogicalClass() == coreLibrary().nameErrorClass) {
                // private constant
                return nil;
            }
            throw e;
        }

        if (ModuleOperations.isConstantDefined(constant)) {
            return coreStrings().CONSTANT.createInstance(getContext());
        } else {
            return nil;
        }
    }

    public RubyNode makeWriteNode(RubyNode rhs) {
        return new WriteConstantNode(name, NodeUtil.cloneNode(moduleNode), rhs);
    }

    private RubyModule checkModule(Object module) {
        if (module instanceof RubyModule) {
            return ((RubyModule) module);
        } else {
            notModuleProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorIsNotAClassModule(module, this));
        }
    }

}
