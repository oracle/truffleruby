package org.truffleruby.core.numeric;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import java.math.BigDecimal;
import java.math.BigInteger;

/** Wrapper for methods of {@link BigInteger} decorated with a {@link TruffleBoundary} annotation, as these methods are
 * blacklisted by SVM. */
public final class BigIntegerOps {
    @TruffleBoundary
    public static BigInteger valueOf(long value) {
        return BigInteger.valueOf(value);
    }

    @TruffleBoundary
    public static BigInteger negate(BigInteger value) {
        return value.negate();
    }

    @TruffleBoundary
    public static BigInteger negate(long value) {
        return valueOf(value).negate();
    }

    @TruffleBoundary
    public static BigInteger add(BigInteger a, BigInteger b) {
        return a.add(b);
    }

    @TruffleBoundary
    public static BigInteger add(long a, long b) {
        return valueOf(a).add(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger add(BigInteger a, long b) {
        return a.add(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger subtract(BigInteger a, BigInteger b) {
        return a.subtract(b);
    }

    @TruffleBoundary
    public static BigInteger subtract(long a, long b) {
        return valueOf(a).subtract(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger subtract(BigInteger a, long b) {
        return a.subtract(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger subtract(long a, BigInteger b) {
        return valueOf(a).subtract(b);
    }

    @TruffleBoundary
    public static BigInteger multiply(BigInteger a, BigInteger b) {
        return a.multiply(b);
    }

    @TruffleBoundary
    public static BigInteger multiply(long a, long b) {
        return valueOf(a).multiply(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger multiply(BigInteger a, long b) {
        return a.multiply(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger not(BigInteger value) {
        return value.not();
    }

    @TruffleBoundary
    public static BigInteger and(BigInteger a, BigInteger b) {
        return a.and(b);
    }

    @TruffleBoundary
    public static BigInteger and(BigInteger a, long b) {
        return a.and(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger or(BigInteger a, BigInteger b) {
        return a.or(b);
    }

    @TruffleBoundary
    public static BigInteger or(BigInteger a, long b) {
        return a.or(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger xor(BigInteger a, BigInteger b) {
        return a.xor(b);
    }

    @TruffleBoundary
    public static BigInteger xor(BigInteger a, long b) {
        return a.xor(valueOf(b));
    }

    @TruffleBoundary
    public static BigInteger shiftLeft(BigInteger value, int n) {
        return value.shiftLeft(n);
    }

    @TruffleBoundary
    public static BigInteger shiftLeft(long value, int n) {
        return valueOf(value).shiftLeft(n);
    }

    @TruffleBoundary
    public static BigInteger shiftRight(BigInteger value, int n) {
        return value.shiftRight(n);
    }

    @TruffleBoundary
    public static BigInteger shiftRight(long value, int n) {
        return valueOf(value).shiftRight(n);
    }

    @TruffleBoundary
    public static BigInteger pow(BigInteger value, int exponent) {
        return value.pow(exponent);
    }

    @TruffleBoundary
    public static double pow(BigInteger value, double exponent) {
        return Math.pow(value.doubleValue(), exponent);
    }

    @TruffleBoundary
    public static BigInteger abs(BigInteger value) {
        return value.abs();
    }

    @TruffleBoundary
    public static BigInteger abs(long value) {
        return valueOf(value).abs();
    }

    @TruffleBoundary
    public static int compare(BigInteger a, BigInteger b) {
        return a.compareTo(b);
    }

    @TruffleBoundary
    public static int compare(BigInteger a, long b) {
        return a.compareTo(valueOf(b));
    }

    @TruffleBoundary
    public static int compare(long a, BigInteger b) {
        return valueOf(a).compareTo(b);
    }

    @TruffleBoundary
    public static int signum(BigInteger value) {
        return value.signum();
    }

    @TruffleBoundary
    public static int bitLength(BigInteger value) {
        return value.bitLength();
    }

    @TruffleBoundary
    public static boolean testBit(BigInteger value, int n) {
        return value.testBit(n);
    }

    @TruffleBoundary
    public static double doubleValue(BigInteger value) {
        return value.doubleValue();
    }

    @TruffleBoundary
    public static long longValue(BigInteger value) {
        return value.longValue();
    }

    @TruffleBoundary
    public static BigInteger fromDouble(double value) {
        return new BigDecimal(value).toBigInteger();
    }

    @TruffleBoundary
    public static boolean equals(BigInteger a, BigInteger b) {
        return a.equals(b);
    }

    @TruffleBoundary
    public static String toString(BigInteger value) {
        return value.toString();
    }

    @TruffleBoundary
    public static String toString(BigInteger value, int base) {
        return value.toString(base);
    }
}
