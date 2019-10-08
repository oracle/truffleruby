/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.language.backtrace.Backtrace;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface ThreadBacktraceLocationLayout extends BasicObjectLayout {

    DynamicObjectFactory createThreadBacktraceLocationShape(
            DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createThreadBacktraceLocation(
            DynamicObjectFactory factory,
            Backtrace backtrace,
            int activationIndex);

    Backtrace getBacktrace(DynamicObject object);

    int getActivationIndex(DynamicObject object);

}
