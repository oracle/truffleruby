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
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.Layouts;
import org.truffleruby.core.module.LoadAutoloadedConstantNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

@NodeChildren({ @NodeChild("lexicalScope"), @NodeChild("module"), @NodeChild("name"), @NodeChild("constant"), @NodeChild("lookupConstantNode") })
public abstract class GetConstantNode extends RubyNode {

    private final boolean callConstMissing;

    @Child private CallDispatchHeadNode constMissingNode;

    public static GetConstantNode create() {
        return create(true);
    }

    public static GetConstantNode create(boolean callConstMissing) {
        return GetConstantNodeGen.create(callConstMissing, null, null, null, null, null);
    }

    public Object lookupAndResolveConstant(LexicalScope lexicalScope, Object module, String name, LookupConstantInterface lookupConstantNode) {
        final RubyConstant constant = lookupConstantNode.lookupConstant(lexicalScope, module, name);
        return executeGetConstant(lexicalScope, module, name, constant, lookupConstantNode);
    }

    protected abstract Object executeGetConstant(LexicalScope lexicalScope, Object module, String name, RubyConstant constant, LookupConstantInterface lookupConstantNode);

    public GetConstantNode(boolean callConstMissing) {
        this.callConstMissing = callConstMissing;
    }

    @Specialization(guards = { "constant != null", "!constant.isAutoload()" })
    protected Object getConstant(LexicalScope lexicalScope, DynamicObject module, String name, RubyConstant constant, LookupConstantInterface lookupConstantNode) {
        return constant.getValue();
    }

    @Specialization(guards = { "constant != null", "constant.isAutoload()" })
    protected Object autoloadConstant(LexicalScope lexicalScope, DynamicObject module, String name, RubyConstant constant, LookupConstantInterface lookupConstantNode,
            @Cached("new()") LoadAutoloadedConstantNode loadAutoloadedConstantNode) {
        loadAutoloadedConstantNode.loadAutoloadedConstant(name, constant);
        try {
            final RubyConstant resolvedConstant = lookupConstantNode.lookupConstant(lexicalScope, module, name);
            return executeGetConstant(lexicalScope, module, name, resolvedConstant, lookupConstantNode);
        } catch (RaiseException e) {
            Layouts.MODULE.getFields(module).setAutoloadConstant(getContext(), this, name, (DynamicObject) constant.getValue());
            throw e;
        }
    }

    @Specialization(
            guards = { "constant == null", "guardName(name, cachedName, sameNameProfile)" },
            limit = "getCacheLimit()")
    protected Object missingConstantCached(LexicalScope lexicalScope, DynamicObject module, String name, Object constant, LookupConstantInterface lookupConstantNode,
            @Cached("name") String cachedName,
            @Cached("getSymbol(name)") DynamicObject symbolName,
            @Cached("createBinaryProfile()") ConditionProfile sameNameProfile) {
        if (callConstMissing) {
            return doMissingConstant(module, name, symbolName);
        } else {
            return null;
        }
    }

    @Specialization(guards = "constant == null")
    protected Object missingConstantUncached(LexicalScope lexicalScope, DynamicObject module, String name, Object constant, LookupConstantInterface lookupConstantNode) {
        if (callConstMissing) {
            return doMissingConstant(module, name, getSymbol(name));
        } else {
            return null;
        }
    }

    private Object doMissingConstant(DynamicObject module, String name, DynamicObject symbolName) {
        if (constMissingNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            constMissingNode = insert(CallDispatchHeadNode.createOnSelf());
        }

        return constMissingNode.call(null, module, "const_missing", symbolName);
    }

    protected boolean guardName(String name, String cachedName, ConditionProfile sameNameProfile) {
        // This is likely as for literal constant lookup the name does not change and Symbols
        // always return the same String.
        if (sameNameProfile.profile(name == cachedName)) {
            return true;
        } else {
            return name.equals(cachedName);
        }
    }

    protected int getCacheLimit() {
        return getContext().getOptions().CONSTANT_CACHE;
    }

}
