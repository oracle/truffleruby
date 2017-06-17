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

    final Assumption fixnumLeftShiftAssumption;
    final Assumption fixnumRightShiftAssumption;

    final Assumption fixnumEqualAssumption;

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

        fixnumLeftShiftAssumption = registerAssumption(fixnumClass, "<<");

        fixnumRightShiftAssumption = registerAssumption(fixnumClass, ">>");

        fixnumEqualAssumption = registerAssumption(fixnumClass, "==");
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
                case "<<":
                    return InlinedLeftShiftNodeGen.create(context, callParameters, self, args[0]);
                case ">>":
                    return InlinedRightShiftNodeGen.create(context, callParameters, self, args[0]);
                case "==":
                    return InlinedEqualNodeGen.create(context, callParameters, self, args[0]);
                default:
            }
        }

        return new RubyCallNode(callParameters);
    }


}
