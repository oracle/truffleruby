/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.module.ModuleFields;

public class SetTopLevelBindingNode extends RubyNode {

    @Child private RubyNode child;

    public SetTopLevelBindingNode(RubyNode child) {
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyContext context = getContext();
        final ModuleFields fields = Layouts.MODULE.getFields(context.getCoreLibrary().getObjectClass());

        final RubyConstant previousConstant = fields.getConstant("TOPLEVEL_BINDING");
        final Object previousValue = previousConstant == null ? null : previousConstant.getValue();

        final DynamicObject binding = Layouts.BINDING.createBinding(context.getCoreLibrary().getBindingFactory(), frame.materialize());
        fields.setConstant(context, this, "TOPLEVEL_BINDING", binding);

        try {
            return child.execute(frame);
        } finally {
            if (previousValue == null) {
                fields.removeConstant(context, this, "TOPLEVEL_BINDING");
            } else {
                fields.setConstant(context, this, "TOPLEVEL_BINDING", previousValue);
            }
        }
    }

}
