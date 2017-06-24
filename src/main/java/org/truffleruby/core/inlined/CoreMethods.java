/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

public class CoreMethods {

    private final RubyContext context;

    final Assumption fixnumAddAssumption, floatAddAssumption;
    final Assumption fixnumSubAssumption, floatSubAssumption;
    final Assumption fixnumMulAssumption, floatMulAssumption;
    final Assumption fixnumDivAssumption, floatDivAssumption;
    final Assumption fixnumModAssumption, floatModAssumption;

    final Assumption fixnumLeftShiftAssumption;
    final Assumption fixnumRightShiftAssumption;
    final Assumption fixnumBitOrAssumption;
    final Assumption fixnumBitAndAssumption;

    final Assumption fixnumEqualAssumption;
    final Assumption fixnumLessThanAssumption, fixnumLessOrEqualAssumption;
    final Assumption fixnumGreaterThanAssumption, fixnumGreaterOrEqualAssumption;

    public CoreMethods(RubyContext context) {
        this.context = context;
        final DynamicObject fixnumClass = context.getCoreLibrary().getFixnumClass();
        final DynamicObject floatClass = context.getCoreLibrary().getFloatClass();

        fixnumAddAssumption = registerAssumption(fixnumClass, "+");
        floatAddAssumption = registerAssumption(floatClass, "+");

        fixnumSubAssumption = registerAssumption(fixnumClass, "-");
        floatSubAssumption = registerAssumption(floatClass, "-");

        fixnumMulAssumption = registerAssumption(fixnumClass, "*");
        floatMulAssumption = registerAssumption(floatClass, "*");

        fixnumDivAssumption = registerAssumption(fixnumClass, "/");
        floatDivAssumption = registerAssumption(floatClass, "/");

        fixnumModAssumption = registerAssumption(fixnumClass, "%");
        floatModAssumption = registerAssumption(floatClass, "%");

        fixnumLeftShiftAssumption = registerAssumption(fixnumClass, "<<");
        fixnumRightShiftAssumption = registerAssumption(fixnumClass, ">>");
        fixnumBitOrAssumption = registerAssumption(fixnumClass, "|");
        fixnumBitAndAssumption = registerAssumption(fixnumClass, "&");

        fixnumEqualAssumption = registerAssumption(fixnumClass, "==");
        fixnumLessThanAssumption = registerAssumption(fixnumClass, "<");
        fixnumLessOrEqualAssumption = registerAssumption(fixnumClass, "<=");
        fixnumGreaterThanAssumption = registerAssumption(fixnumClass, ">");
        fixnumGreaterOrEqualAssumption = registerAssumption(fixnumClass, ">=");
    }

    private Assumption registerAssumption(DynamicObject klass, String methodName) {
        return Layouts.MODULE.getFields(klass).registerAssumption(methodName);
    }

    public RubyNode createCallNode(RubyCallNodeParameters callParameters) {
        if (!context.getOptions().BASICOPS_INLINE ||
                callParameters.getBlock() != null || callParameters.isSplatted() || callParameters.isSafeNavigation()) {
            return new RubyCallNode(callParameters);
        }

        final RubyNode self = callParameters.getReceiver();
        final RubyNode[] args = callParameters.getArguments();
        int n = 1 /* self */ + args.length;

        if (n == 2) {
            switch (callParameters.getMethodName()) {
                case "+":
                    return InlinedAddNodeGen.create(context, callParameters, self, args[0]);
                case "-":
                    return InlinedSubNodeGen.create(context, callParameters, self, args[0]);
                case "*":
                    return InlinedMulNodeGen.create(context, callParameters, self, args[0]);
                case "/":
                    return InlinedDivNodeGen.create(context, callParameters, self, args[0]);
                case "%":
                    return InlinedModNodeGen.create(context, callParameters, self, args[0]);
                case "<<":
                    return InlinedLeftShiftNodeGen.create(context, callParameters, self, args[0]);
                case ">>":
                    return InlinedRightShiftNodeGen.create(context, callParameters, self, args[0]);
                case "&":
                    return InlinedBitAndNodeGen.create(context, callParameters, self, args[0]);
                case "|":
                    return InlinedBitOrNodeGen.create(context, callParameters, self, args[0]);
                case "==":
                    return InlinedEqualNodeGen.create(context, callParameters, self, args[0]);
                case "<":
                    return InlinedLessThanNodeGen.create(context, callParameters, self, args[0]);
                case "<=":
                    return InlinedLessOrEqualNodeGen.create(context, callParameters, self, args[0]);
                case ">":
                    return InlinedGreaterThanNodeGen.create(context, callParameters, self, args[0]);
                case ">=":
                    return InlinedGreaterOrEqualNodeGen.create(context, callParameters, self, args[0]);
                default:
            }
        }

        return new RubyCallNode(callParameters);
    }


}
