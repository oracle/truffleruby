/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.constant;

import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.WarnNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public class WarnAlreadyInitializedNode extends RubyBaseNode {

    @Child private WarnNode warnNode = new WarnNode();

    @TruffleBoundary
    public void warnAlreadyInitialized(DynamicObject module, String name, SourceSection sourceSection,
            SourceSection previousSourceSection) {
        final String constName = ModuleOperations.constantNameNoLeadingColon(getContext(), module, name);
        warnNode.warningMessage(sourceSection, "already initialized constant " + constName);
        if (previousSourceSection != null) {
            warnNode.warningMessage(previousSourceSection, "previous definition of " + name + " was here");
        }
    }

}
