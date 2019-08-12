/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.Layouts;
import org.truffleruby.core.constant.WarnAlreadyInitializedNode;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

public class WriteConstantNode extends RubyNode {

    private final String name;

    @Child private RubyNode moduleNode;
    @Child private RubyNode valueNode;
    @Child private WarnAlreadyInitializedNode warnAlreadyInitializedNode;

    private final ConditionProfile moduleProfile = ConditionProfile.createBinaryProfile();

    public WriteConstantNode(String name, RubyNode moduleNode, RubyNode valueNode) {
        this.name = name;
        this.moduleNode = moduleNode;
        this.valueNode = valueNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object value = valueNode.execute(frame);
        final Object moduleObject = moduleNode.execute(frame);

        if (!moduleProfile.profile(RubyGuards.isRubyModule(moduleObject))) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorIsNotAClassModule(moduleObject, this));
        }

        final RubyConstant previous = Layouts.MODULE.getFields((DynamicObject) moduleObject).setConstant(getContext(), this, name, value);
        if (previous != null && previous.hasValue()) {
            warnAlreadyInitializedConstant((DynamicObject) moduleObject, name, previous.getSourceSection());
        }
        return value;
    }

    private void warnAlreadyInitializedConstant(DynamicObject module, String name, SourceSection previousSourceSection) {
        if (warnAlreadyInitializedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnAlreadyInitializedNode = insert(new WarnAlreadyInitializedNode());
        }
        warnAlreadyInitializedNode.warnAlreadyInitialized(module, name, getSourceSection(), previousSourceSection);
    }

}
