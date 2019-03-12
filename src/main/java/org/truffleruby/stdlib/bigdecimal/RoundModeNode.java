/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import com.oracle.truffle.api.dsl.Specialization;

import java.math.RoundingMode;

public abstract class RoundModeNode extends BigDecimalCoreMethodNode {

    @Specialization
    public RoundingMode doGetRoundMode() {
        return getRoundMode();
    }

}
