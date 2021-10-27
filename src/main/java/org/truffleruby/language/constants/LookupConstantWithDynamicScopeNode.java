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

import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;

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
    public RubyConstant lookupConstant(LexicalScope lexicalScope, RubyModule module, String name, boolean checkName) {
        assert name == this.name;
        return executeLookupConstant(lexicalScope);
    }

    @Specialization(
            guards = { "isSingleContext()", "lexicalScope == cachedLexicalScope" },
            assumptions = "constant.getAssumptions()",
            limit = "getCacheLimit()")
    protected RubyConstant lookupConstant(LexicalScope lexicalScope,
            @Cached("lexicalScope") LexicalScope cachedLexicalScope,
            @Cached("doLookup(cachedLexicalScope)") ConstantLookupResult constant) {
        if (constant.isDeprecated()) {
            warnDeprecatedConstant(constant.getConstant().getDeclaringModule(), name);
        }
        return constant.getConstant();
    }

    @Specialization
    protected RubyConstant lookupConstantUncached(LexicalScope lexicalScope,
            @Cached ConditionProfile isDeprecatedProfile) {
        final ConstantLookupResult constant = doLookup(lexicalScope);
        if (isDeprecatedProfile.profile(constant.isDeprecated())) {
            warnDeprecatedConstant(constant.getConstant().getDeclaringModule(), name);
        }
        return constant.getConstant();
    }

    @TruffleBoundary
    protected ConstantLookupResult doLookup(LexicalScope lexicalScope) {
        return ModuleOperations.lookupConstantWithLexicalScope(getContext(), lexicalScope, name);
    }

}
