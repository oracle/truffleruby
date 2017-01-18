/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.thread;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.language.backtrace.Activation;

@Layout
public interface ThreadBacktraceLocationLayout extends BasicObjectLayout {

    DynamicObjectFactory createThreadBacktraceLocationShape(
            DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createThreadBacktraceLocation(
            DynamicObjectFactory factory,
            Activation activation);

    Activation getActivation(DynamicObject object);

}
