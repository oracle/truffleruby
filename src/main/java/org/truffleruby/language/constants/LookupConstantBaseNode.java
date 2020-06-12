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

import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.WarnNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public abstract class LookupConstantBaseNode extends RubyContextNode {

    @Child private WarnNode warnNode;

    protected void warnDeprecatedConstant(DynamicObject module, RubyConstant constant, String name) {
        if (warnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnNode = insert(new WarnNode());
        }

        if (warnNode.shouldWarn()) {
            warnNode.warningMessage(getSection(), formatMessage(module, name));
        }
    }

    @TruffleBoundary
    private SourceSection getSection() {
        return getContext().getCallStack().getTopMostUserSourceSection(getEncapsulatingSourceSection());
    }

    @TruffleBoundary
    private String formatMessage(DynamicObject module, String name) {
        return "constant " + ModuleOperations.constantName(getContext(), module, name) + " is deprecated";
    }

    protected int getCacheLimit() {
        return getContext().getOptions().CONSTANT_CACHE;
    }

}
