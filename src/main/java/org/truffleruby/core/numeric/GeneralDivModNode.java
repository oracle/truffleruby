/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import java.math.BigInteger;

import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.FloatToIntegerNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.BranchProfile;

public class GeneralDivModNode extends RubyBaseNode {

    @Child private FixnumOrBignumNode fixnumOrBignumQuotient = new FixnumOrBignumNode();
    @Child private FixnumOrBignumNode fixnumOrBignumRemainder = new FixnumOrBignumNode();
    @Child private FloatToIntegerNode floatToIntegerNode = FloatToIntegerNode.create();

    private final BranchProfile bZeroProfile = BranchProfile.create();
    private final BranchProfile bMinusOneProfile = BranchProfile.create();
    private final BranchProfile nanProfile = BranchProfile.create();
    private final BranchProfile bigIntegerFixnumProfile = BranchProfile.create();
    private final BranchProfile useFixnumPairProfile = BranchProfile.create();
    private final BranchProfile useObjectPairProfile = BranchProfile.create();

    public RubyArray execute(long a, long b) {
        return divMod(a, b);
    }

    public RubyArray execute(long a, BigInteger b) {
        return divMod(BigIntegerOps.valueOf(a), b);
    }

    public RubyArray execute(long a, double b) {
        return divMod(a, b);
    }

    public RubyArray execute(BigInteger a, long b) {
        return divMod(a, BigIntegerOps.valueOf(b));
    }

    public RubyArray execute(BigInteger a, BigInteger b) {
        return divMod(a, b);
    }

    public RubyArray execute(BigInteger a, double b) {
        return divMod(BigIntegerOps.doubleValue(a), b);
    }

    public RubyArray execute(double a, long b) {
        return divMod(a, b);
    }

    public RubyArray execute(double a, BigInteger b) {
        return divMod(a, BigIntegerOps.doubleValue(b));
    }

    public RubyArray execute(double a, double b) {
        return divMod(a, b);
    }

    /* div-mod algorithms copied from org.jruby.RubyFixnum, org.jruby.RubyBignum and org.jrubyRubyFloat. See license and
     * contributors there. */

    private RubyArray divMod(long a, long b) {
        if (b == 0) {
            bZeroProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        long mod;
        Object integerDiv;

        if (b == -1) {
            bMinusOneProfile.enter();

            if (a == Long.MIN_VALUE) {
                integerDiv = BigIntegerOps.negate(a);
            } else {
                integerDiv = -a;
            }
            mod = 0;
        } else {
            long div = a / b;
            mod = a - b * div;
            if (mod < 0 && b > 0 || mod > 0 && b < 0) {
                div -= 1;
                mod += b;
            }
            integerDiv = div;
        }

        if (integerDiv instanceof Long && CoreLibrary.fitsIntoInteger((long) integerDiv) &&
                CoreLibrary.fitsIntoInteger(mod)) {
            useFixnumPairProfile.enter();
            return createArray(new int[]{ (int) (long) integerDiv, (int) mod });
        } else if (integerDiv instanceof Long) {
            useObjectPairProfile.enter();
            return createArray(new long[]{ (long) integerDiv, mod });
        } else {
            useObjectPairProfile.enter();
            return createArray(new Object[]{
                    fixnumOrBignumQuotient.fixnumOrBignum((BigInteger) integerDiv),
                    mod
            });
        }
    }

    @TruffleBoundary
    private RubyArray divMod(double a, double b) {
        if (b == 0) {
            bZeroProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        double mod = Math.IEEEremainder(a, b);

        if (Double.isNaN(mod)) {
            nanProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().floatDomainError("NaN", this));
        }

        final double div = Math.floor(a / b);

        if (b * mod < 0) {
            mod += b;
        }

        return createArray(new Object[]{ floatToIntegerNode.fixnumOrBignum(div), mod });
    }

    @TruffleBoundary
    private RubyArray divMod(BigInteger a, BigInteger b) {
        if (b.signum() == 0) {
            bZeroProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().zeroDivisionError(this));
        }

        final BigInteger[] bigIntegerResults = a.divideAndRemainder(b);

        if ((a.signum() * b.signum()) == -1 && bigIntegerResults[1].signum() != 0) {
            bigIntegerFixnumProfile.enter();
            bigIntegerResults[0] = bigIntegerResults[0].subtract(BigInteger.ONE);
            bigIntegerResults[1] = b.add(bigIntegerResults[1]);
        }

        return createArray(new Object[]{
                fixnumOrBignumQuotient.fixnumOrBignum(bigIntegerResults[0]),
                fixnumOrBignumRemainder.fixnumOrBignum(bigIntegerResults[1]) });
    }

}
