/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import static org.truffleruby.cext.ValueWrapperManager.FALSE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.MAX_FIXNUM_VALUE;
import static org.truffleruby.cext.ValueWrapperManager.MIN_FIXNUM_VALUE;
import static org.truffleruby.cext.ValueWrapperManager.NIL_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.TRUE_HANDLE;

import java.math.BigInteger;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.cext.ValueWrapperManager;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;

/**
 * <pre>
 * Object IDs distribution
 *
 * We try to respect MRI scheme when it makes sense (Fixnum for the moment).
 * Have a look at include/ruby/ruby.h below ruby_special_consts.
 *
 * Encoding for Fixnum (long):
 * ... 0000 = false
 * ... 0010 = true
 * ... 0100 = nil
 *
 * ... xxx1 = Fixnum of value (id-1)/2 if -2^62 <= value < 2^62
 * ... xxx0 = BasicObject generated id (for id > 4)
 *
 * Encoding for Bignum:
 * ... 0001 | 64-bit long = Fixnum if value < -2^62 or value >= 2^62
 * ... 0010 | 64-bit raw double bits = Float
 * </pre>
 */
public abstract class ObjectIDOperations {

    public static final long FALSE = FALSE_HANDLE;
    public static final long TRUE = TRUE_HANDLE;
    public static final long NIL = NIL_HANDLE;

    private static final BigInteger LARGE_FIXNUM_FLAG = BigInteger.ONE.shiftLeft(64);
    private static final BigInteger FLOAT_FLAG = BigInteger.ONE.shiftLeft(65);

    // primitive => ID

    public static boolean isSmallFixnum(long fixnum) {
        // TODO: optimize
        return MIN_FIXNUM_VALUE <= fixnum && fixnum <= MAX_FIXNUM_VALUE;
    }

    public static long smallFixnumToIDOverflow(long fixnum) throws ArithmeticException {
        return Math.addExact(Math.multiplyExact(fixnum, 2), 1);
    }

    public static long smallFixnumToID(long fixnum) {
        assert isSmallFixnum(fixnum);
        return fixnum * 2 + 1;
    }

    public static DynamicObject largeFixnumToID(RubyContext context, long fixnum) {
        assert !isSmallFixnum(fixnum);
        BigInteger big = unsignedBigInteger(fixnum);
        return BignumOperations.createBignum(context, big.or(LARGE_FIXNUM_FLAG));
    }

    public static DynamicObject floatToID(RubyContext context, double value) {
        long bits = Double.doubleToRawLongBits(value);
        BigInteger big = unsignedBigInteger(bits);
        return BignumOperations.createBignum(context, big.or(FLOAT_FLAG));
    }

    // ID => primitive

    public static boolean isSmallFixnumID(long id) {
        return id % 2 != 0;
    }

    public static long toFixnum(long id) {
        return (id - 1) / 2;
    }

    public static boolean isLargeFixnumID(BigInteger id) {
        return !id.and(LARGE_FIXNUM_FLAG).equals(BigInteger.ZERO);
    }

    public static boolean isFloatID(BigInteger id) {
        return !id.and(FLOAT_FLAG).equals(BigInteger.ZERO);
    }

    public static boolean isBasicObjectID(long id) {
        return id != 0 && (id & ValueWrapperManager.TAG_MASK) == 0;
    }

    private static BigInteger unsignedBigInteger(long value) {
        BigInteger big = BigInteger.valueOf(value);
        if (value < 0) {
            big = new BigInteger(1, big.toByteArray()); // consider as unsigned
        }
        return big;
    }

    @CompilerDirectives.TruffleBoundary
    public static long verySlowGetObjectID(RubyContext context, DynamicObject object) {
        // TODO(CS): we should specialise on reading this in the #object_id method and anywhere else it's used
        Property property = object.getShape().getProperty(Layouts.OBJECT_ID_IDENTIFIER);

        if (property != null) {
            long value = (long) property.get(object, false);
            if (value != 0) {
                return value;
            }
        }

        final long objectID = context.getObjectSpaceManager().getNextObjectID();

        if (SharedObjects.isShared(context, object)) {
            synchronized (object) {
                // no need for a write barrier here, objectID is a long.
                object.define(Layouts.OBJECT_ID_IDENTIFIER, objectID);
            }
        } else {
            object.define(Layouts.OBJECT_ID_IDENTIFIER, objectID);
        }

        return objectID;
    }
}
