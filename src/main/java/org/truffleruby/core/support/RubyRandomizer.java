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
import org.truffleruby.interop.messages.RandomizerMessages;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

public final class RubyRandomizer extends RubyDynamicObject {

    public Randomizer randomizer;

    public RubyRandomizer(Shape shape, Randomizer randomizer) {
        super(shape);
        this.randomizer = randomizer;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RandomizerMessages.class;
    }

}
