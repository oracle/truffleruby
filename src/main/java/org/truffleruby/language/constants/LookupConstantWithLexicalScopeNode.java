/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class LookupConstantWithLexicalScopeNode extends LookupConstantBaseNode
        implements LookupConstantInterface {

    private final LexicalScope lexicalScope;
    private final String name;

    public LookupConstantWithLexicalScopeNode(LexicalScope lexicalScope, String name) {
        this.lexicalScope = lexicalScope;
        this.name = name;
    }

    public RubyModule getModule() {
        return lexicalScope.getLiveModule();
    }

    public abstract RubyConstant executeLookupConstant();

    @SuppressFBWarnings("ES")
    @Override
    public RubyConstant lookupConstant(LexicalScope lexicalScope, RubyModule module, String name, boolean checkName) {
        assert name == this.name;
        return executeLookupConstant();
    }

    @Specialization(assumptions = "constant.getAssumptions()")
    protected RubyConstant lookupConstant(
            @Cached("doLookup()") ConstantLookupResult constant) {
        if (constant.isDeprecated()) {
            warnDeprecatedConstant(getModule(), name);
        }
        return constant.getConstant();
    }

    @Specialization
    protected RubyConstant lookupConstantUncached(
            @Cached ConditionProfile isDeprecatedProfile) {
        final ConstantLookupResult constant = doLookup();
        if (isDeprecatedProfile.profile(constant.isDeprecated())) {
            warnDeprecatedConstant(getModule(), name);
        }
        return constant.getConstant();
    }

    @TruffleBoundary
    protected ConstantLookupResult doLookup() {
        return ModuleOperations.lookupConstantWithLexicalScope(getContext(), lexicalScope, name);
    }

}
