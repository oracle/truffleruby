/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.LookupConstantBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

@NodeChildren({ @NodeChild("name"), @NodeChild("lexicalParent") })
public abstract class LookupForExistingModuleNode extends LookupConstantBaseNode {

    @Child private CallDispatchHeadNode callRequireNode;

    public abstract RubyConstant executeLookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent);

    @Specialization(guards = "isRubyModule(lexicalParent)")
    public RubyConstant lookupForExistingModule(VirtualFrame frame, String name, DynamicObject lexicalParent,
            @Cached("createBinaryProfile()") ConditionProfile autoloadProfile,
            @Cached("createBinaryProfile()") ConditionProfile warnProfile) {
        final LexicalScope lexicalScope = RubyArguments.getMethod(frame).getSharedMethodInfo().getLexicalScope();
        final RubyConstant constant = deepConstantSearch(name, lexicalScope, lexicalParent);

        if (warnProfile.profile(constant != null && constant.isDeprecated())) {
            warnDeprecatedConstant(lexicalParent, constant, name);
        }

        // If a constant already exists with this class/module name and it's an autoload module, we have to trigger
        // the autoload behavior before proceeding.

        if (autoloadProfile.profile(constant != null && constant.isAutoload())) {

            // We know that we're redefining this constant as we're defining a class/module with that name.  We remove
            // the constant here rather than just overwrite it in order to prevent autoload loops in either the require
            // call or the recursive execute call.

            Layouts.MODULE.getFields(lexicalParent).removeConstant(getContext(), this, name);
            loadAutoloadedConstant(constant);
            final RubyConstant autoConstant = deepConstantSearch(name, lexicalScope, lexicalParent);

            if (warnProfile.profile(constant.isDeprecated())) {
                warnDeprecatedConstant(lexicalParent, constant, name);
            }

            return autoConstant;
        }

        return constant;
    }

    @TruffleBoundary(transferToInterpreterOnException = false)
    private RubyConstant deepConstantSearch(String name, LexicalScope lexicalScope, DynamicObject lexicalParent) {
        RubyConstant constant = Layouts.MODULE.getFields(lexicalParent).getConstant(name);

        final DynamicObject objectClass = getContext().getCoreLibrary().getObjectClass();

        if (constant == null && lexicalParent == objectClass) {
            for (DynamicObject included : Layouts.MODULE.getFields(objectClass).prependedAndIncludedModules()) {
                constant = Layouts.MODULE.getFields(included).getConstant(name);

                if (constant != null) {
                    break;
                }
            }
        }

        if (constant != null && !(constant.isVisibleTo(getContext(), lexicalScope, lexicalScope.getLiveModule()) ||
                constant.isVisibleTo(getContext(), LexicalScope.NONE, lexicalParent))) {
            throw new RaiseException(getContext(), coreExceptions().nameErrorPrivateConstant(lexicalParent, name, this));
        }

        return constant;
    }

    private void loadAutoloadedConstant(RubyConstant constant) {
        if (callRequireNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callRequireNode = insert(CallDispatchHeadNode.createOnSelf());
        }

        final Object feature = constant.getValue();
        callRequireNode.call(null, coreLibrary().getMainObject(), "require", feature);
    }

}
