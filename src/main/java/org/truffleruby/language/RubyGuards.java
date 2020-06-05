/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.Layouts;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.object.DynamicObject;

public abstract class RubyGuards {

    private static final long NEGATIVE_ZERO_DOUBLE_BITS = Double.doubleToRawLongBits(-0.0);

    // Basic Java types

    public static boolean isInteger(Object value) {
        return value instanceof Integer;
    }

    public static boolean fitsInInteger(long value) {
        return CoreLibrary.fitsIntoInteger(value);
    }

    public static boolean fitsInInteger(Object value) {
        return isBasicInteger(value) && CoreLibrary.fitsIntoInteger(((Number) value).longValue());
    }

    public static boolean isLong(Object value) {
        return value instanceof Long;
    }

    public static boolean isDouble(Object value) {
        return value instanceof Double;
    }

    public static boolean isCharacter(Object value) {
        return value instanceof Character;
    }

    public static boolean isString(Object value) {
        return value instanceof String;
    }

    public static boolean isBasicInteger(Object object) {
        return object instanceof Byte || object instanceof Short || object instanceof Integer || object instanceof Long;
    }

    public static boolean isBasicNumber(Object object) {
        return object instanceof Byte || object instanceof Short || object instanceof Integer ||
                object instanceof Long || object instanceof Float || object instanceof Double;
    }

    public static boolean isPrimitive(Object object) {
        return object instanceof Boolean || object instanceof Byte || object instanceof Short ||
                object instanceof Integer || object instanceof Long || object instanceof Float ||
                object instanceof Double;
    }

    public static boolean isPrimitiveClass(Class<?> clazz) {
        return clazz == Boolean.class || clazz == Byte.class || clazz == Short.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Float.class || clazz == Double.class;
    }

    // Ruby types

    public static boolean isRubyBignum(Object value) {
        return Layouts.BIGNUM.isBignum(value);
    }

    public static boolean isRubyBignum(DynamicObject value) {
        return Layouts.BIGNUM.isBignum(value);
    }

    public static boolean isRubyBigDecimal(Object value) {
        return Layouts.BIG_DECIMAL.isBigDecimal(value);
    }

    public static boolean isRubyBigDecimal(DynamicObject value) {
        return Layouts.BIG_DECIMAL.isBigDecimal(value);
    }

    public static boolean isIntRange(Object object) {
        return Layouts.INT_RANGE.isIntRange(object);
    }

    public static boolean isIntRange(DynamicObject object) {
        return Layouts.INT_RANGE.isIntRange(object);
    }

    public static boolean isLongRange(Object object) {
        return Layouts.LONG_RANGE.isLongRange(object);
    }

    public static boolean isLongRange(DynamicObject object) {
        return Layouts.LONG_RANGE.isLongRange(object);
    }

    public static boolean isObjectRange(Object object) {
        return Layouts.OBJECT_RANGE.isObjectRange(object);
    }

    public static boolean isObjectRange(DynamicObject object) {
        return Layouts.OBJECT_RANGE.isObjectRange(object);
    }

    public static boolean isEndlessRange(DynamicObject object) {
        assert isObjectRange(object);
        return Layouts.OBJECT_RANGE.getEnd(object) == Nil.INSTANCE;
    }

    public static boolean isRubyRange(Object value) {
        return isIntRange(value) || isLongRange(value) || isObjectRange(value);
    }

    public static boolean isRubyArray(Object value) {
        return Layouts.ARRAY.isArray(value);
    }

    public static boolean isRubyArray(DynamicObject value) {
        return Layouts.ARRAY.isArray(value);
    }

    public static boolean isRubyBinding(DynamicObject object) {
        return Layouts.BINDING.isBinding(object);
    }

    public static boolean isRubyClass(Object value) {
        return Layouts.CLASS.isClass(value);
    }

    public static boolean isRubyClass(DynamicObject value) {
        return Layouts.CLASS.isClass(value);
    }

    public static boolean isRubyHash(Object value) {
        return Layouts.HASH.isHash(value);
    }

    public static boolean isRubyHash(DynamicObject value) {
        return Layouts.HASH.isHash(value);
    }

    public static boolean isRubyModule(Object value) {
        return Layouts.MODULE.isModule(value);
    }

    public static boolean isRubyModule(DynamicObject value) {
        return Layouts.MODULE.isModule(value);
    }

    public static boolean isRubyRegexp(Object value) {
        return Layouts.REGEXP.isRegexp(value);
    }

    public static boolean isRubyRegexp(DynamicObject value) {
        return Layouts.REGEXP.isRegexp(value);
    }

    public static boolean isRubyString(Object value) {
        return Layouts.STRING.isString(value);
    }

    public static boolean isRubyString(DynamicObject value) {
        return Layouts.STRING.isString(value);
    }

    public static boolean isRubyEncoding(Object object) {
        return Layouts.ENCODING.isEncoding(object);
    }

    public static boolean isRubyEncoding(DynamicObject object) {
        return Layouts.ENCODING.isEncoding(object);
    }

    public static boolean isRubySymbol(Object value) {
        return value instanceof RubySymbol;
    }

    public static boolean isRubyMethod(Object value) {
        return Layouts.METHOD.isMethod(value);
    }

    public static boolean isRubyMethod(DynamicObject value) {
        return Layouts.METHOD.isMethod(value);
    }

    public static boolean isRubyUnboundMethod(Object value) {
        return Layouts.UNBOUND_METHOD.isUnboundMethod(value);
    }

    public static boolean isRubyUnboundMethod(DynamicObject value) {
        return Layouts.UNBOUND_METHOD.isUnboundMethod(value);
    }

    public static boolean isDynamicObject(Object value) {
        return value instanceof DynamicObject;
    }

    public static boolean isRubyPointer(DynamicObject value) {
        return Layouts.POINTER.isPointer(value);
    }

    public static boolean isByteArray(DynamicObject value) {
        return Layouts.BYTE_ARRAY.isByteArray(value);
    }

    public static boolean isRubyProc(Object object) {
        return Layouts.PROC.isProc(object);
    }

    public static boolean isRubyProc(DynamicObject object) {
        return Layouts.PROC.isProc(object);
    }

    public static boolean isRubyEncodingConverter(DynamicObject encodingConverter) {
        return Layouts.ENCODING_CONVERTER.isEncodingConverter(encodingConverter);
    }

    public static boolean isRubyTime(DynamicObject object) {
        return Layouts.TIME.isTime(object);
    }

    public static boolean isRubyException(DynamicObject object) {
        return Layouts.EXCEPTION.isException(object);
    }

    public static boolean isRubyFiber(DynamicObject object) {
        return Layouts.FIBER.isFiber(object);
    }

    public static boolean isRubyThread(DynamicObject object) {
        return Layouts.THREAD.isThread(object);
    }

    public static boolean isRubyMatchData(DynamicObject object) {
        return Layouts.MATCH_DATA.isMatchData(object);
    }

    public static boolean isHandle(DynamicObject object) {
        return Layouts.HANDLE.isHandle(object);
    }

    public static boolean isRubyInteger(Object object) {
        return isBasicInteger(object) || isRubyBignum(object);
    }

    public static boolean isRubyNumber(Object object) {
        // Doesn't include classes like BigDecimal
        return isBasicNumber(object) || isRubyBignum(object);
    }

    public static boolean isNil(Object object) {
        return object == Nil.INSTANCE;
    }

    // Internal types

    public static boolean isRubyDynamicObject(Object object) {
        return Layouts.BASIC_OBJECT.isBasicObject(object);
    }

    public static boolean isRubyValue(Object object) {
        return isRubyDynamicObject(object) || isPrimitive(object) || object instanceof Nil ||
                object instanceof RubySymbol;
    }

    public static boolean isForeignObject(Object object) {
        return !isRubyValue(object);
    }

    // Sentinels

    public static boolean wasProvided(Object value) {
        return !(wasNotProvided(value));
    }

    public static boolean wasNotProvided(Object value) {
        return value == NotProvided.INSTANCE;
    }

    // Values

    public static boolean isNaN(double value) {
        return Double.isNaN(value);
    }

    public static boolean isInfinity(double value) {
        return Double.isInfinite(value);
    }

    public static boolean isFinite(double value) {
        return Double.isFinite(value);
    }

    public static boolean isPositive(double value) {
        return value >= 0;
    }

    public static boolean isNegativeZero(double value) {
        return Double.doubleToRawLongBits(value) == NEGATIVE_ZERO_DOUBLE_BITS;
    }

    // Composite

    public static boolean isSingletonClass(DynamicObject value) {
        return isRubyClass(value) && Layouts.CLASS.getIsSingleton(value);
    }

    public static boolean isMetaClass(DynamicObject value) {
        return isSingletonClass(value) && RubyGuards.isRubyModule(Layouts.CLASS.getAttached(value));
    }

    // Arguments

    public static boolean noArguments(Object[] args) {
        return args.length == 0;
    }

    public static boolean singleArgument(Object[] args) {
        return args.length == 1;
    }

}
