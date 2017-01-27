/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.module.ConstantLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.control.RaiseException;

public abstract class LookupConstantWithLexicalScopeNode extends RubyNode implements LookupConstantInterface {

    private final LexicalScope lexicalScope;
    private final String name;
    @Child private WarnNode warnNode;

    public LookupConstantWithLexicalScopeNode(LexicalScope lexicalScope, String name) {
        this.lexicalScope = lexicalScope;
        this.name = name;
    }

    public DynamicObject getModule() {
        return lexicalScope.getLiveModule();
    }

    public abstract RubyConstant executeLookupConstant(VirtualFrame frame);

    public RubyConstant lookupConstant(VirtualFrame frame, Object module, String name) {
        assert name == this.name;
        return executeLookupConstant(frame);
    }

    @Specialization(assumptions = "constant.getAssumptions()")
    protected RubyConstant lookupConstant(VirtualFrame frame,
            @Cached("doLookup()") ConstantLookupResult constant,
            @Cached("isVisible(constant)") boolean isVisible) {
        if (!isVisible) {
            throw new RaiseException(coreExceptions().nameErrorPrivateConstant(getModule(), name, this));
        }
        if (constant.isDeprecated()) {
            warnDeprecatedConstant(frame, name);
        }
        return constant.getConstant();
    }

    @Specialization
    protected RubyConstant lookupConstantUncached(VirtualFrame frame,
            @Cached("createBinaryProfile()") ConditionProfile isVisibleProfile,
            @Cached("createBinaryProfile()") ConditionProfile isDeprecatedProfile) {
        ConstantLookupResult constant = doLookup();
        if (isVisibleProfile.profile(!isVisible(constant))) {
            throw new RaiseException(coreExceptions().nameErrorPrivateConstant(getModule(), name, this));
        }
        if (isDeprecatedProfile.profile(constant.isDeprecated())) {
            warnDeprecatedConstant(frame, name);
        }
        return constant.getConstant();
    }

    @TruffleBoundary
    protected ConstantLookupResult doLookup() {
        return ModuleOperations.lookupConstantWithLexicalScope(getContext(), lexicalScope, name);
    }

    @TruffleBoundary
    protected boolean isVisible(ConstantLookupResult constant) {
        return constant.isVisibleTo(getContext(), lexicalScope, getModule());
    }

    private void warnDeprecatedConstant(VirtualFrame frame, String name) {
        if (warnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnNode = insert(new WarnNode());
        }
        warnNode.execute(frame, "constant ", name, " is deprecated");
    }

}
