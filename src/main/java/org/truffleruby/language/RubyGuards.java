/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.core.CoreLibrary;

public abstract class RubyGuards {

    // Basic Java types

    public static boolean isBoolean(Object value) {
        return value instanceof Boolean;
    }

    public static boolean isByte(Object value) {
        return value instanceof Byte;
    }

    public static boolean isShort(Object value) {
        return value instanceof Short;
    }

    public static boolean isInteger(Object value) {
        return value instanceof Integer;
    }

    public static boolean fitsInInteger(long value) {
        return CoreLibrary.fitsIntoInteger(value);
    }

    public static boolean fitsInInteger(double n) {
        return Integer.MIN_VALUE < n && n < Integer.MAX_VALUE;
    }

    public static boolean isLong(Object value) {
        return value instanceof Long;
    }

    public static boolean fitsInLong(double n) {
        return Long.MIN_VALUE < n && n < Long.MAX_VALUE;
    }

    public static boolean isFloat(Object value) {
        return value instanceof Float;
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
        return isByte(object) || isShort(object) || isInteger(object) || isLong(object);
    }

    public static boolean isBasicNumber(Object object) {
        return isByte(object) || isShort(object) || isInteger(object) || isLong(object) || isFloat(object) || isDouble(object);
    }

    public static boolean isPrimitive(Object object) {
        return isBoolean(object) || isByte(object) || isShort(object) || isInteger(object) || isLong(object) || isFloat(object) || isDouble(object);
    }

    public static boolean isPrimitiveClass(Class<?> clazz) {
        return clazz == Boolean.class || clazz == Byte.class || clazz == Short.class || clazz == Integer.class
                || clazz == Long.class || clazz == Float.class || clazz == Double.class;
    }
    // Ruby types

    public static boolean isRubyBasicObject(Object object) {
        return Layouts.BASIC_OBJECT.isBasicObject(object);
    }

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
        return Layouts.SYMBOL.isSymbol(value);
    }

    public static boolean isRubySymbol(DynamicObject value) {
        return Layouts.SYMBOL.isSymbol(value);
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

    public static boolean isTracePoint(DynamicObject object) {
        return Layouts.TRACE_POINT.isTracePoint(object);
    }

    public static boolean isNullPointer(DynamicObject pointer) {
        return Layouts.POINTER.getPointer(pointer).getAddress() == 0;
    }

    public static boolean isRubyInteger(Object object) {
        return isBasicInteger(object) || isRubyBignum(object);
    }

    public static boolean isRubyNumber(Object object) {
        // Doesn't include classes like BigDecimal
        return isBasicNumber(object) || isRubyBignum(object);
    }

    // Internal types

    public static boolean isTruffleObject(Object object) {
        return object instanceof TruffleObject;
    }

    public static boolean isForeignObject(Object object) {
        return object instanceof TruffleObject && !isRubyBasicObject(object);
    }

    public static boolean isBoxedPrimitive(Object object) {
        return object instanceof Boolean
                || object instanceof Byte
                || object instanceof Short
                || object instanceof Integer
                || object instanceof Long
                || object instanceof Float
                || object instanceof Double;
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

    public static boolean isPositive(double value) {
        return value >= 0;
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
