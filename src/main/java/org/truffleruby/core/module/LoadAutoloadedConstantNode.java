/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.object.DynamicObject;

public class LoadAutoloadedConstantNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode callRequireNode = CallDispatchHeadNode.createOnSelf();

    public void loadAutoloadedConstant(String name, RubyConstant constant) {
        assert constant.isAutoload();
        final DynamicObject feature = (DynamicObject) constant.getValue();
        assert RubyGuards.isRubyString(feature);

        final DynamicObject module = constant.getDeclaringModule();
        final ModuleFields fields = Layouts.MODULE.getFields(module);

        // The autoload constant must only be removed if everything succeeds.
        // We remove it first to allow lookup to ignore it and add it back if there was a failure.
        fields.removeConstant(getContext(), this, name);
        try {
            callRequireNode.call(null, coreLibrary().getMainObject(), "require", feature);
        } catch (RaiseException e) {
            fields.setAutoloadConstant(getContext(), this, name, feature);
            throw e;
        }
    }

}
