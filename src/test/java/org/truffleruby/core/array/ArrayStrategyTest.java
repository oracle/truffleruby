package org.truffleruby.core.array;

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
        ArrayStrategy longStrategy = ArrayStrategy.ofStore(new int[1]);
        ArrayStrategy intToObjectStrategy = intStrategy.generalize(objectStrategy);
        ArrayStrategy generaliedStrategy = longStrategy.generalize(intToObjectStrategy);
        assertEquals(objectStrategy, generaliedStrategy);
    }
}
