/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import org.truffleruby.algorithms.Randomizer;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.object.Shape;

public final class RubyRandomizer extends RubyDynamicObject {

    public Randomizer randomizer;
    public final boolean threadSafe; // Used to configure new Randomizer instances, e.g. when setting seed manually

    public RubyRandomizer(RubyClass rubyClass, Shape shape, Randomizer randomizer, boolean threadSafe) {
        super(rubyClass, shape);
        this.randomizer = randomizer;
        this.threadSafe = threadSafe;
    }

}
