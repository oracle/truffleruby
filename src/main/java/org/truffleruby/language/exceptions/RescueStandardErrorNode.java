/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class RescueStandardErrorNode extends RescueNode {

    public RescueStandardErrorNode(RubyNode rescueBody) {
        super(rescueBody);
    }

    @Override
    public boolean canHandle(VirtualFrame frame, Object exceptionObject) {
        return matches(exceptionObject, coreLibrary().standardErrorClass);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return RubyNode.defaultIsDefined(this);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new RescueStandardErrorNode(getRescueBody().cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
