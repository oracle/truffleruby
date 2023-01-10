/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.basicobject;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.object.Shape;

/** This is not the common type for Ruby DynamicObjects. See {@link RubyDynamicObject} instead. This class represents
 * instances of Ruby objects which are not core types such as String, Array, etc.
 *
 * Core types such as String, Array, etc do not have inline fields for instance variables, in order to save footprint.
 * Instance variables are rare for core types. */
public final class RubyBasicObject extends RubyDynamicObject {

    // Same number of inline fields as DynamicObjectBasic
    @DynamicField private long primitive1;
    @DynamicField private long primitive2;
    @DynamicField private long primitive3;
    @DynamicField private Object object1;
    @DynamicField private Object object2;
    @DynamicField private Object object3;
    @DynamicField private Object object4;

    public RubyBasicObject(RubyClass rubyClass, Shape shape) {
        super(rubyClass, shape);
    }

}
