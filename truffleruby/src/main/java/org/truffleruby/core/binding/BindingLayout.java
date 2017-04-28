/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.binding;

import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

import org.truffleruby.core.basicobject.BasicObjectLayout;

/**
 * Bindings capture the frame from where they are called, which is stored in {@code frames}, but
 * also have their own frame(s) in {@code extras} to store variables added in the Binding, which
 * must not be added in the captured frame (MRI semantics). New frame(s) are added as required to
 * prevent evaluation using the binding from leaking new variables into the captured frame, and when
 * cloning a binding to stop vatriables from the clone leaking to the original or vice versa.
 */
@Layout
public interface BindingLayout extends BasicObjectLayout {

    DynamicObjectFactory createBindingShape(DynamicObject logicalClass,
                                            DynamicObject metaClass);

    DynamicObject createBinding(DynamicObjectFactory factory,
            MaterializedFrame frame,
            @Nullable MaterializedFrame extras);

    boolean isBinding(DynamicObject object);

    MaterializedFrame getFrame(DynamicObject object);

    MaterializedFrame getExtras(DynamicObject object);

    void setExtras(DynamicObject object, MaterializedFrame value);
}
