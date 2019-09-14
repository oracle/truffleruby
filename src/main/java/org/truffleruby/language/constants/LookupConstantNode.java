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

import java.util.ArrayList;

import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.Identifiers;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

/**
 * Caches {@link ModuleOperations#lookupConstant}
 * and checks visibility.
 */
public abstract class LookupConstantNode extends LookupConstantBaseNode implements LookupConstantInterface {

    private final boolean ignoreVisibility;
    private final boolean checkName;
    private final boolean lookInObject;

    public static LookupConstantNode create(boolean ignoreVisibility, boolean checkName, boolean lookInObject) {
        return LookupConstantNodeGen.create(ignoreVisibility, checkName, lookInObject);
    }

    public LookupConstantNode(boolean ignoreVisibility, boolean checkName, boolean lookInObject) {
        this.ignoreVisibility = ignoreVisibility;
        this.checkName = checkName;
        this.lookInObject = lookInObject;
    }

    public abstract RubyConstant executeLookupConstant(Object module, String name);

    @Override
    public RubyConstant lookupConstant(LexicalScope lexicalScope, DynamicObject module, String name) {
        return executeLookupConstant(module, name);
    }

    @Specialization(
            guards = { "module == cachedModule", "guardName(name, cachedName, sameNameProfile)" },
            assumptions = "constant.getAssumptions()",
            limit = "getCacheLimit()")
    protected RubyConstant lookupConstant(DynamicObject module, String name,
            @Cached("module") DynamicObject cachedModule,
            @Cached("name") String cachedName,
            @Cached("isValidConstantName(cachedName)") boolean isValidConstantName,
            @Cached("doLookup(cachedModule, cachedName)") ConstantLookupResult constant,
            @Cached("isVisible(cachedModule, constant)") boolean isVisible,
            @Cached("createBinaryProfile()") ConditionProfile sameNameProfile) {
        if (!isValidConstantName) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorWrongConstantName(cachedName, this));
        } else if (!isVisible) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateConstant(module, name, this));
        }
        if (constant.isDeprecated()) {
            warnDeprecatedConstant(module, constant.getConstant(), name);
        }
        return constant.getConstant();
    }

    @Specialization
    protected RubyConstant lookupConstantUncached(DynamicObject module, String name,
            @Cached("createBinaryProfile()") ConditionProfile isValidConstantNameProfile,
            @Cached("createBinaryProfile()") ConditionProfile isVisibleProfile,
            @Cached("createBinaryProfile()") ConditionProfile isDeprecatedProfile) {
        ConstantLookupResult constant = doLookup(module, name);
        boolean isVisible = isVisible(module, constant);

        if (!isValidConstantNameProfile.profile(isValidConstantName(name))) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorWrongConstantName(name, this));
        } else if (isVisibleProfile.profile(!isVisible)) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateConstant(module, name, this));
        }
        if (isDeprecatedProfile.profile(constant.isDeprecated())) {
            warnDeprecatedConstant(module, constant.getConstant(), name);
        }
        return constant.getConstant();
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

    @TruffleBoundary
    protected ConstantLookupResult doLookup(DynamicObject module, String name) {
        assert RubyGuards.isRubyModule(module);

        if (lookInObject) {
            return ModuleOperations.lookupConstantAndObject(getContext(), module, name, new ArrayList<>());
        } else {
            return ModuleOperations.lookupConstant(getContext(), module, name);
        }
    }

    protected boolean isVisible(DynamicObject module, ConstantLookupResult constant) {
        return ignoreVisibility || constant.isVisibleTo(getContext(), LexicalScope.NONE, module);
    }

    protected boolean isValidConstantName(String name) {
        if (checkName) {
            return Identifiers.isValidConstantName(name);
        } else {
            return true;
        }
    }

}
