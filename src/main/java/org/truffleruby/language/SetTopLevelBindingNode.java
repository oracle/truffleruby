/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.Layouts;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.module.ModuleFields;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public class SetTopLevelBindingNode extends RubyNode {

    @Override
    public Object execute(VirtualFrame frame) {
        final MaterializedFrame mainScriptFrame = frame.materialize();
        updateTopLevelBindingFrame(mainScriptFrame);
        return nil();
    }

    @TruffleBoundary
    private void updateTopLevelBindingFrame(MaterializedFrame mainScriptFrame) {
        final ModuleFields fields = Layouts.MODULE.getFields(coreLibrary().getObjectClass());
        final DynamicObject toplevelBinding = (DynamicObject) fields.getConstant("TOPLEVEL_BINDING").getValue();
        BindingNodes.insertAncestorFrame(getContext(), toplevelBinding, mainScriptFrame);
    }

}
