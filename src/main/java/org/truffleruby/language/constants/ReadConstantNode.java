/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeUtil;
import org.truffleruby.Layouts;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

/** Read a literal constant on a given module: MOD::CONST */
public class ReadConstantNode extends RubyNode {

    private final String name;

    @Child private RubyNode moduleNode;
    @Child private LookupConstantNode lookupConstantNode;
    @Child private GetConstantNode getConstantNode;

    public ReadConstantNode(RubyNode moduleNode, String name) {
        this.name = name;
        this.moduleNode = moduleNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object module = moduleNode.execute(frame);

        final RubyConstant constant = lookupConstant(module);
        return executeGetConstant(module, constant);
    }

    private Object executeGetConstant(Object module, RubyConstant constant) {
        if (getConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getConstantNode = insert(GetConstantNode.create());
        }
        return getConstantNode.executeGetConstant(LexicalScope.IGNORE, module, name, constant, lookupConstantNode);
    }

    private RubyConstant lookupConstant(Object module) {
        if (lookupConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupConstantNode = insert(LookupConstantNode.create(false, true, false));
        }
        return lookupConstantNode.lookupConstant(LexicalScope.IGNORE, module, name);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        // TODO (eregon, 17 May 2016): We execute moduleNode twice here but we both want to make sure the LHS is defined and get the result value.
        // Possible solution: have a isDefinedAndReturnValue()?
        Object isModuleDefined = moduleNode.isDefined(frame);
        if (isModuleDefined == nil()) {
            return nil();
        }

        final Object module;
        try {
            module = moduleNode.execute(frame);
            if (!RubyGuards.isRubyModule(module)) {
                return nil();
            }
        } catch (RaiseException e) {
            // If reading the module raised an exception, it must have been an autoloaded module that failed while
            // loading. MRI dictates that in this case we should swallow the exception and return `nil`.
            return nil();
        }

        final RubyConstant constant;
        try {
            constant = lookupConstant(module);
        } catch (RaiseException e) {
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().getNameErrorClass()) {
                // private constant
                return nil();
            }
            throw e;
        }

        if (constant == null) {
            return nil();
        } else {
            return coreStrings().CONSTANT.createInstance();
        }
    }

    public RubyNode makeWriteNode(RubyNode rhs) {
        return new WriteConstantNode(name, NodeUtil.cloneNode(moduleNode), rhs);
    }

}
