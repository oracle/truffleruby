/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class LookupConstantWithLexicalScopeNode extends LookupConstantBaseNode
        implements LookupConstantInterface {

    private final LexicalScope lexicalScope;
    private final String name;

    public LookupConstantWithLexicalScopeNode(LexicalScope lexicalScope, String name) {
        this.lexicalScope = lexicalScope;
        this.name = name;
    }

    public DynamicObject getModule() {
        return lexicalScope.getLiveModule();
    }

    public abstract RubyConstant executeLookupConstant();

    @SuppressFBWarnings("ES")
    @Override
    public RubyConstant lookupConstant(LexicalScope lexicalScope, DynamicObject module, String name) {
        assert name == this.name;
        return executeLookupConstant();
    }

    @Specialization(assumptions = "constant.getAssumptions()")
    protected RubyConstant lookupConstant(
            @Cached("doLookup()") ConstantLookupResult constant,
            @Cached("isVisible(constant)") boolean isVisible) {
        if (!isVisible) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateConstant(getModule(), name, this));
        }
        if (constant.isDeprecated()) {
            warnDeprecatedConstant(getModule(), constant.getConstant(), name);
        }
        return constant.getConstant();
    }

    @Specialization
    protected RubyConstant lookupConstantUncached(
            @Cached("createBinaryProfile()") ConditionProfile isVisibleProfile,
            @Cached("createBinaryProfile()") ConditionProfile isDeprecatedProfile) {
        final ConstantLookupResult constant = doLookup();
        if (isVisibleProfile.profile(!isVisible(constant))) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateConstant(getModule(), name, this));
        }
        if (isDeprecatedProfile.profile(constant.isDeprecated())) {
            warnDeprecatedConstant(getModule(), constant.getConstant(), name);
        }
        return constant.getConstant();
    }

    @TruffleBoundary
    protected ConstantLookupResult doLookup() {
        return ModuleOperations.lookupConstantWithLexicalScope(getContext(), lexicalScope, name);
    }

    protected boolean isVisible(ConstantLookupResult constant) {
        return constant.isVisibleTo(getContext(), lexicalScope, getModule());
    }

}
