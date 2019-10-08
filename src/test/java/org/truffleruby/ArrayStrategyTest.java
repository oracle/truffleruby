/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.truffleruby.core.array.ArrayStrategy;

public class ArrayStrategyTest {

    @Test
    public void testGeneraliseIntToObjectGeneralizationArrayStrategyLHS() {
        ArrayStrategy intStrategy = ArrayStrategy.ofStore(new int[1]);
        ArrayStrategy objectStrategy = ArrayStrategy.ofStore(new Object[1]);
        ArrayStrategy longStrategy = ArrayStrategy.ofStore(new long[1]);
        ArrayStrategy intToObjectStrategy = intStrategy.generalize(objectStrategy);
        ArrayStrategy generaliedStrategy = intToObjectStrategy.generalize(longStrategy);
        assertEquals(objectStrategy, generaliedStrategy);
    }

    @Test
    public void testGeneraliseIntToObjectGeneralizationArrayStrategyRHS() {
        ArrayStrategy intStrategy = ArrayStrategy.ofStore(new int[1]);
        ArrayStrategy objectStrategy = ArrayStrategy.ofStore(new Object[1]);
        ArrayStrategy longStrategy = ArrayStrategy.ofStore(new long[1]);
        ArrayStrategy intToObjectStrategy = intStrategy.generalize(objectStrategy);
        ArrayStrategy generaliedStrategy = longStrategy.generalize(intToObjectStrategy);
        assertEquals(objectStrategy, generaliedStrategy);
    }
}
