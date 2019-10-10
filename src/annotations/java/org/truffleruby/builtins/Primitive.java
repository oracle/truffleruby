/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Primitive {

    String name();

    // TODO (eregon, 28 Sept 2019): is this still needed?
    boolean needsSelf() default true;

    /**
     * Try to lower argument <code>i</code> (starting at 1) to an int if its value is a long.
     * Use 0 for <code>self</code>.
     */
    int[] lowerFixnum() default {};

    /**
     * Raise an error if self is frozen.
     */
    boolean raiseIfFrozenSelf() default false;

    /**
     * Use these names in Ruby core methods stubs, ignore argument names in Java specializations.
     */
    String[] argumentNames() default {};
}
