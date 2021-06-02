/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyContextSourceNode;

import java.util.Random;

@NodeChild(value = "value", type = RubyBaseNodeWithExecute.class)
public abstract class ChaosNode extends RubyContextSourceNode {

    private static final Random RANDOM = new Random(0);

    public static ChaosNode create() {
        return ChaosNodeGen.create(null);
    }

    public static ChaosNode create(RubyBaseNodeWithExecute child) {
        return ChaosNodeGen.create(child);
    }

    @Specialization
    protected Object chaos(int value) {
        if (randomBoolean()) {
            return value;
        } else {
            return (long) value;
        }
    }

    @Specialization(guards = "fitsIntoInteger(value)")
    protected Object chaos(long value) {
        if (randomBoolean()) {
            return value;
        } else {
            return (int) value;
        }
    }

    @Specialization(guards = "!fitsIntoInteger(value)")
    protected long passThrough(long value) {
        return value;
    }

    protected static boolean fitsIntoInteger(long value) {
        return CoreLibrary.fitsIntoInteger(value);
    }

    @Fallback
    protected Object chaos(Object value) {
        return value;
    }

    @TruffleBoundary
    private boolean randomBoolean() {
        return RANDOM.nextBoolean();
    }

}

