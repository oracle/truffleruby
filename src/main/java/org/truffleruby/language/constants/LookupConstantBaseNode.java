/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.constants;

import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.WarnNode;

import com.oracle.truffle.api.CompilerDirectives;

public abstract class LookupConstantBaseNode extends RubyNode {

    @Child private WarnNode warnNode;

    protected void warnDeprecatedConstant(RubyConstant constant, String name) {
        if (warnNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warnNode = insert(new WarnNode());
        }

        if (constant.getDeclaringModule() == coreLibrary().getObjectClass()) {
            warnNode.warn("constant ::", name, " is deprecated");
        } else {
            warnNode.warn("constant ", name, " is deprecated");
        }
    }

    protected int getCacheLimit() {
        return getContext().getOptions().CONSTANT_CACHE;
    }

}
