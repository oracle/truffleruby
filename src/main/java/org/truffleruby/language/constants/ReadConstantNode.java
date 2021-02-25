/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
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

    public Object execute(VirtualFrame frame, RubyModule module, RubyConstant constant) {
        return getConstant(module, constant);
    }

    private Object lookupAndGetConstant(RubyModule module) {
        return getGetConstantNode()
                .lookupAndResolveConstant(LexicalScope.IGNORE, module, name, getLookupConstantNode());
    }

    private Object getConstant(RubyModule module, RubyConstant constant) {
        return getGetConstantNode()
                .executeGetConstant(LexicalScope.IGNORE, module, name, constant, getLookupConstantNode());
    }

    private GetConstantNode getGetConstantNode() {
        if (getConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getConstantNode = insert(GetConstantNode.create());
        }
        return getConstantNode;
    }

    private LookupConstantNode getLookupConstantNode() {
        if (lookupConstantNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupConstantNode = insert(LookupConstantNode.create(false, true, false));
        }
        return lookupConstantNode;
    }

    /** Reads the constant value, wrapping any exception occuring during module autoloading in an
     * {@link AutoloadException}. */
    private Object readConstantWrappingAutoloadExceptions(VirtualFrame frame) {
        final Object moduleObject = moduleNode instanceof ReadConstantNode
                ? ((ReadConstantNode) moduleNode).readConstantWrappingAutoloadExceptions(frame)
                : moduleNode.execute(frame);
        final RubyModule module = checkModule(moduleObject);
        final RubyConstant constant = getLookupConstantNode().lookupConstant(LexicalScope.IGNORE, module, name, true);
        try {
            return getConstant(module, constant);
        } catch (RaiseException e) {
            if (constant.isAutoload()) {
                throw new AutoloadException(e);
            }
            throw e;
        }
    }

    /** Returns the evaluated module part, wrapping any exception occuring during module autoloading in an
     * {@link AutoloadException}. */
    public RubyModule evaluateModuleWrappingAutoloadExceptions(VirtualFrame frame) {
        final Object moduleObject = moduleNode instanceof ReadConstantNode
                ? ((ReadConstantNode) moduleNode).readConstantWrappingAutoloadExceptions(frame)
                : moduleNode.execute(frame);
        return checkModule(moduleObject);
    }

    /** Whether the module part of this constant read is undefined, without attempting to evaluate it. */
    public boolean isModuleTriviallyUndefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return moduleNode.isDefined(frame, language, context) == nil;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        final RubyConstant constant = getConstantIfDefined(frame, language, context);
        return constant == null ? nil : coreStrings().CONSTANT.createInstance(getContext());
    }

    /** Returns the constant, it it is defined. Otherwise returns {@code null}. */
    private RubyConstant getConstantIfDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        if (isModuleTriviallyUndefined(frame, language, context)) {
            return null;
        }

        final RubyModule module;
        try {
            module = evaluateModuleWrappingAutoloadExceptions(frame);
        } catch (AutoloadException e) {
            // If autoloading a module raised an exception,
            // MRI dictates that we should swallow the exception and return `nil`.
            return null;
        }

        return getConstantIfDefined(module);
    }

    /** Given the module, returns the constant, it it is defined. Otherwise returns {@code null}. */
    public RubyConstant getConstantIfDefined(RubyModule module) {
        final RubyConstant constant;
        try {
            constant = getLookupConstantNode().lookupConstant(LexicalScope.IGNORE, module, name, true);
        } catch (RaiseException e) {
            if (e.getException().getLogicalClass() == coreLibrary().nameErrorClass) {
                // private constant
                return null;
            }
            throw e;
        }

        if (ModuleOperations.isConstantDefined(constant)) {
            return constant;
        } else {
            return null;
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
