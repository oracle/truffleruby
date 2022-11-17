/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.array.library.DelegatedArrayStorage;

public class ArrayStoreTest {

    @Test
    public void zeroLengthGeneralisesToInteger() {
        Object store = ArrayStoreLibrary.initialStorage(false);
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        int newValue = 7;
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), int[].class);
    }

    @Test
    public void zeroLengthGeneralisesToLong() {
        Object store = ArrayStoreLibrary.initialStorage(false);
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        long newValue = 7;
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), long[].class);
    }

    @Test
    public void zeroLengthGeneralisesToDouble() {
        Object store = ArrayStoreLibrary.initialStorage(false);
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        double newValue = 7.2;
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), double[].class);
    }

    @Test
    public void zeroLengthGeneralisesToObject() {
        Object store = ArrayStoreLibrary.initialStorage(false);
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        Object newValue = new Object();
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), Object[].class);
    }

    @Test
    public void intArrayAcceptsInt() {
        int[] store = new int[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        int newValue = 1;
        assertTrue(stores.acceptsValue(store, newValue));
    }

    @Test
    public void intArrayGeneralisesToLong() {
        int[] store = new int[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        long newValue = 1L << 33;
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), long[].class);
    }

    @Test
    public void intArrayGeneralisesToObject() {
        int[] store = new int[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        Object newValue = new Object();
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), Object[].class);
    }

    @Test
    public void longArrayAcceptsLong() {
        long[] store = new long[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        long newValue = 1;
        assertTrue(stores.acceptsValue(store, newValue));
    }

    @Test
    public void longArrayGeneralisesToObject() {
        long[] store = new long[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        Object newValue = new Object();
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), Object[].class);
    }

    @Test
    public void doubleArrayAcceptsDouble() {
        double[] store = new double[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        double newValue = 1;
        assertTrue(stores.acceptsValue(store, newValue));
    }

    @Test
    public void doubleArrayGeneralisesToObject() {
        double[] store = new double[1];
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();

        Object newValue = new Object();
        assertFalse(stores.acceptsValue(store, newValue));
        Object newStore = stores.generalizeForValue(store, newValue).allocate(1);
        assertEquals(newStore.getClass(), Object[].class);
    }

    @Test
    public void extractRangeOnArrayReturnsDelegatedStorage() {
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();
        assertEquals(stores.extractRange(new int[10], 0, 10).getClass(), DelegatedArrayStorage.class);
        assertEquals(stores.extractRange(new long[10], 0, 10).getClass(), DelegatedArrayStorage.class);
        assertEquals(stores.extractRange(new double[10], 0, 10).getClass(), DelegatedArrayStorage.class);
        assertEquals(stores.extractRange(new Object[10], 0, 10).getClass(), DelegatedArrayStorage.class);
    }

    @Test
    public void extractRangeOnZeroLengthArrayReturnssZeroLengthArray() {
        ArrayStoreLibrary stores = ArrayStoreLibrary.getUncached();
        assertEquals(stores.extractRange(ArrayStoreLibrary.initialStorage(false), 0, 0),
                ArrayStoreLibrary.initialStorage(false));
    }
}
