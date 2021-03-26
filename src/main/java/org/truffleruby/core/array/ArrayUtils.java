/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.RubyLanguage;

public abstract class ArrayUtils {

    public static final Object[] EMPTY_ARRAY = new Object[0];

    public static boolean assertNoNullElement(Object[] array) {
        return assertNoNullElement(array, array.length);
    }

    public static boolean assertNoNullElement(Object[] array, int size) {
        assert size <= array.length;
        for (int i = 0; i < size; i++) {
            final Object element = array[i];
            assert element != null : nullElementAt(array, i);
        }
        return true;
    }

    @TruffleBoundary
    private static String nullElementAt(Object[] array, int index) {
        return "null element in Object[] at index " + index + ": " + Arrays.toString(array);
    }

    /** Extracts part of an array into a newly allocated byte[] array. Does not perform safety checks on parameters.
     * 
     * @param source the source array whose values should be extracted
     * @param start the start index, must be >= 0 and <= source.length
     * @param end the end index (exclusive), must be >= 0 and <= source.length and >= start
     * @return a newly allocated array with the extracted elements and length (end - start) */
    public static byte[] extractRange(byte[] source, int start, int end) {
        assert assertExtractRangeArgs(source, start, end);
        int length = end - start;
        byte[] result = new byte[length];
        System.arraycopy(source, start, result, 0, length);
        return result;
    }

    /** Extracts part of an array into a newly allocated int[] array. Does not perform safety checks on parameters.
     * 
     * @param source the source array whose values should be extracted
     * @param start the start index, must be >= 0 and <= source.length
     * @param end the end index (exclusive), must be >= 0 and <= source.length and >= start
     * @return a newly allocated array with the extracted elements and length (end - start) */
    public static int[] extractRange(int[] source, int start, int end) {
        assert assertExtractRangeArgs(source, start, end);
        int length = end - start;
        int[] result = new int[length];
        System.arraycopy(source, start, result, 0, length);
        return result;
    }

    /** Extracts part of an array into a newly allocated long[] array. Does not perform safety checks on parameters.
     * 
     * @param source the source array whose values should be extracted
     * @param start the start index, must be >= 0 and <= source.length
     * @param end the end index (exclusive), must be >= 0 and <= source.length and >= start
     * @return a newly allocated array with the extracted elements and length (end - start) */
    public static long[] extractRange(long[] source, int start, int end) {
        assert assertExtractRangeArgs(source, start, end);
        int length = end - start;
        long[] result = new long[length];
        System.arraycopy(source, start, result, 0, length);
        return result;
    }

    /** Extracts part of an array into a newly allocated double[] array. Does not perform safety checks on parameters.
     * 
     * @param source the source array whose values should be extracted
     * @param start the start index, must be >= 0 and <= source.length
     * @param end the end index (exclusive), must be >= 0 and <= source.length and >= start
     * @return a newly allocated array with the extracted elements and length (end - start) */
    public static double[] extractRange(double[] source, int start, int end) {
        assert assertExtractRangeArgs(source, start, end);
        int length = end - start;
        double[] result = new double[length];
        System.arraycopy(source, start, result, 0, length);
        return result;
    }

    /** Extracts part of an array into a newly allocated Object[] array. Does not perform safety checks on parameters.
     * 
     * @param source the source array whose values should be extracted
     * @param start the start index, must be >= 0 and <= source.length
     * @param end the end index (exclusive), must be >= 0 and <= source.length and >= start
     * @return a newly allocated array with the extracted elements and length (end - start) */
    public static Object[] extractRange(Object[] source, int start, int end) {
        assert assertExtractRangeArgs(source, start, end);
        int length = end - start;
        Object[] result = new Object[length];
        System.arraycopy(source, start, result, 0, length);
        return result;
    }

    private static boolean assertExtractRangeArgs(Object source, int start, int end) {
        assert source != null;
        assert start >= 0;
        assert start <= Array.getLength(source);
        assert end >= start;
        assert end <= Array.getLength(source);
        return true;
    }

    public static boolean contains(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }

        return false;
    }

    /** Compares by identity using Java {@code ==} */
    public static <T> boolean contains(T[] array, T value) {
        for (T element : array) {
            if (element == value) {
                return true;
            }
        }

        return false;
    }

    public static int capacity(RubyLanguage language, int current, int needed) {
        if (needed == 0) {
            return 0;
        }

        assert current < needed;

        if (needed < language.options.ARRAY_UNINITIALIZED_SIZE) {
            return language.options.ARRAY_UNINITIALIZED_SIZE;
        } else {
            final int newCapacity = current << 1;
            if (newCapacity >= needed) {
                return newCapacity;
            } else {
                return needed;
            }
        }
    }

    public static int capacityForOneMore(RubyLanguage language, int current) {
        if (current < language.options.ARRAY_UNINITIALIZED_SIZE) {
            return language.options.ARRAY_UNINITIALIZED_SIZE;
        } else {
            return current << 1;
        }
    }

    public static long capacityForOneMore(RubyLanguage language, long current) {
        if (current < language.options.ARRAY_UNINITIALIZED_SIZE) {
            return language.options.ARRAY_UNINITIALIZED_SIZE;
        } else {
            return current << 1;
        }
    }

    public static void arraycopy(Object[] src, int srcPos, Object[] dest, int destPos, int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static Object[] copyOf(Object[] array, int newLength) {
        final Object[] copy = new Object[newLength];
        System.arraycopy(array, 0, copy, 0, Math.min(array.length, newLength));
        return copy;
    }

    public static int[] grow(int[] array, int newLength) {
        assert newLength >= array.length;
        final int[] copy = new int[newLength];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public static long[] grow(long[] array, int newLength) {
        assert newLength >= array.length;
        final long[] copy = new long[newLength];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public static double[] grow(double[] array, int newLength) {
        assert newLength >= array.length;
        final double[] copy = new double[newLength];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public static Object[] grow(Object[] array, int newLength) {
        assert newLength >= array.length;
        final Object[] copy = new Object[newLength];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public static Object[] copy(Object[] array) {
        final Object[] copy = new Object[array.length];
        System.arraycopy(array, 0, copy, 0, array.length);
        return copy;
    }

    public static Object[] unshift(Object[] array, Object element) {
        final Object[] newArray = new Object[1 + array.length];
        newArray[0] = element;
        arraycopy(array, 0, newArray, 1, array.length);
        return newArray;
    }

    public static Object[] append(Object[] array, Object element) {
        final Object[] newArray = grow(array, array.length + 1);
        newArray[array.length] = element;
        return newArray;
    }

    public static int memcmp(final byte[] first, final int firstStart, final byte[] second, final int secondStart,
            int size) {
        assert firstStart + size <= first.length;
        assert secondStart + size <= second.length;

        int cmp;

        for (int i = 0; i < size; i++) {
            if ((cmp = (first[firstStart + i] & 0xff) - (second[secondStart + i] & 0xff)) != 0) {
                return cmp;
            }
        }

        return 0;
    }

    public static int memchr(byte[] array, int start, byte find, int size) {
        for (int i = start; i < start + size; i++) {
            if (array[i] == find) {
                return i;
            }
        }
        return -1;
    }

    @TruffleBoundary
    public static void sort(Object[] elements, int length) {
        Arrays.sort(elements, 0, length);
    }

    @TruffleBoundary
    public static List<Object> asList(Object[] array) {
        return Arrays.asList(array);
    }

}
