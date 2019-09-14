/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2006 Ola Bini <ola@ologix.com>
 * Copyright (C) 2009 Joseph LaFata <joe@quibb.org>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.stdlib.bigdecimal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsANode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

@ImportStatic(BigDecimalCoreMethodNode.class)
public abstract class BigDecimalCastNode extends RubyBaseNode {

    private static final int RMPD_COMPONENT_FIGURES = 19;

    public static BigDecimalCastNode create() {
        return BigDecimalCastNodeGen.create();
    }

    public abstract Object execute(Object value, int digits, RoundingMode roundingMode);

    @Specialization
    protected BigDecimal doInt(long value, int digits, RoundingMode roundingMode) {
        return BigDecimal.valueOf(value);
    }

    @TruffleBoundary
    @Specialization
    protected BigDecimal doDouble(double value, int digits, RoundingMode roundingMode) {
        assert !RubyGuards.isNegativeZero(value);
        return BigDecimal.valueOf(value);
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected BigDecimal doBignum(DynamicObject value, int digits, RoundingMode roundingMode) {
        return new BigDecimal(Layouts.BIGNUM.getValue(value));
    }

    @Specialization(guards = "isNormalRubyBigDecimal(value)")
    protected BigDecimal doBigDecimal(DynamicObject value, int digits, RoundingMode roundingMode) {
        return Layouts.BIG_DECIMAL.getValue(value);
    }

    @Specialization(guards = "isSpecialRubyBigDecimal(value)")
    protected DynamicObject doSpecialBigDecimal(DynamicObject value, int digits, RoundingMode roundingMode) {
        return value;
    }

    @Specialization(guards = { "!isRubyNumber(value)", "!isRubyBigDecimal(value)" })
    protected Object doOther(Object value, int digits, RoundingMode roundingMode,
            @Cached IsANode isRationalNode,
            @Cached("createPrivate()") CallDispatchHeadNode numeratorCallNode,
            @Cached("createPrivate()") CallDispatchHeadNode denominatorCallNode) {
        if (isRationalNode.executeIsA(value, coreLibrary().getRationalClass())) {
            final Object numerator = numeratorCallNode.call(value, "numerator");
            final Object denominator = denominatorCallNode.call(value, "denominator");

            try {
                return toBigDecimal(numerator, denominator, digits, roundingMode);
            } catch (Exception e) {
                throw e;
            }
        } else {
            return value;
        }
    }

    @TruffleBoundary
    private BigDecimal toBigDecimal(Object numerator, Object denominator, int digits, RoundingMode roundingMode) {
        final BigDecimal numeratorDecimal = toBigDecimal(numerator);
        final BigDecimal denominatorDecimal = toBigDecimal(denominator);
        if (digits == 0) {
            digits = RMPD_COMPONENT_FIGURES;
        }
        final MathContext mathContext = new MathContext(digits, roundingMode);
        return numeratorDecimal.divide(denominatorDecimal, mathContext);
    }

    @TruffleBoundary
    private BigDecimal toBigDecimal(Object object) {
        if (object instanceof Byte) {
            return BigDecimal.valueOf((byte) object);
        } else if (object instanceof Short) {
            return BigDecimal.valueOf((short) object);
        } else if (object instanceof Integer) {
            return BigDecimal.valueOf((int) object);
        } else if (object instanceof Long) {
            return BigDecimal.valueOf((long) object);
        } else if (object instanceof Float) {
            return BigDecimal.valueOf((float) object);
        } else if (object instanceof Double) {
            return BigDecimal.valueOf((double) object);
        } else if (RubyGuards.isRubyBignum(object)) {
            return BigDecimal.valueOf(Layouts.BIGNUM.getValue((DynamicObject) object).doubleValue());
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
