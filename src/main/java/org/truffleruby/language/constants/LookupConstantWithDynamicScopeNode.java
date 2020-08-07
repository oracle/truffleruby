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

import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class LookupConstantWithDynamicScopeNode extends LookupConstantBaseNode
        implements LookupConstantInterface {

    private final String name;

    public LookupConstantWithDynamicScopeNode(String name) {
        this.name = name;
    }

    public abstract RubyConstant executeLookupConstant(LexicalScope lexicalScope);

    @SuppressFBWarnings("ES")
    public RubyConstant lookupConstant(LexicalScope lexicalScope, RubyModule module, String name) {
        assert name == this.name;
        return executeLookupConstant(lexicalScope);
    }

    @Specialization(
            guards = "lexicalScope == cachedLexicalScope",
            assumptions = "constant.getAssumptions()",
            limit = "getCacheLimit()")
    protected RubyConstant lookupConstant(LexicalScope lexicalScope,
            @Cached("lexicalScope") LexicalScope cachedLexicalScope,
            @Cached("doLookup(cachedLexicalScope)") ConstantLookupResult constant,
            @Cached("isVisible(cachedLexicalScope, constant)") boolean isVisible) {
        if (!isVisible) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameErrorPrivateConstant(constant.getConstant().getDeclaringModule(), name, this));
        }
        if (constant.isDeprecated()) {
            warnDeprecatedConstant(constant.getConstant().getDeclaringModule(), constant.getConstant(), name);
        }
        return constant.getConstant();
    }

    @Specialization
    protected RubyConstant lookupConstantUncached(LexicalScope lexicalScope,
            @Cached ConditionProfile isVisibleProfile,
            @Cached ConditionProfile isDeprecatedProfile) {
        final ConstantLookupResult constant = doLookup(lexicalScope);
        if (isVisibleProfile.profile(!isVisible(lexicalScope, constant))) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameErrorPrivateConstant(constant.getConstant().getDeclaringModule(), name, this));
        }
        if (isDeprecatedProfile.profile(constant.isDeprecated())) {
            warnDeprecatedConstant(constant.getConstant().getDeclaringModule(), constant.getConstant(), name);
        }
        return constant.getConstant();
    }

    @TruffleBoundary
    protected ConstantLookupResult doLookup(LexicalScope lexicalScope) {
        return ModuleOperations.lookupConstantWithLexicalScope(getContext(), lexicalScope, name);
    }

    protected boolean isVisible(LexicalScope lexicalScope, ConstantLookupResult constant) {
        return constant.isVisibleTo(getContext(), lexicalScope, lexicalScope.getLiveModule());
    }

}
