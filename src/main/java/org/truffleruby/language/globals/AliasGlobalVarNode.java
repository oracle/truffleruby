/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.truffleruby.language.globals;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

public class AliasGlobalVarNode extends RubyNode {

    private final String oldName;
    private final String newName;

    public AliasGlobalVarNode(String oldName, String newName) {
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        checkExisting();
        getContext().getCoreLibrary().getGlobalVariables().alias(oldName, newName);
        return nil();
    }

    @TruffleBoundary
    private void checkExisting() {
        if (getContext().getCoreLibrary().getGlobalVariables().contains(newName)) {
            throw new RaiseException(coreExceptions().internalError(String.format("%s is already a global", newName), this), true);
        }
    }

}
