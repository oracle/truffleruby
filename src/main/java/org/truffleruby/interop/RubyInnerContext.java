/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.TruffleContext;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.object.Shape;

public final class RubyInnerContext extends RubyDynamicObject {

    public final TruffleContext innerContext;

    public RubyInnerContext(RubyClass rubyClass, Shape shape, TruffleContext innerContext) {
        super(rubyClass, shape);
        this.innerContext = innerContext;
    }

}
