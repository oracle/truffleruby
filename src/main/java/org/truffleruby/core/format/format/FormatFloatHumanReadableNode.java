/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.format;

import org.truffleruby.core.format.FormatNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.string.StringOperations;

@NodeChild("value")
public abstract class FormatFloatHumanReadableNode extends FormatNode {

    @TruffleBoundary
    @Specialization(guards = "isIntegerValue(value)")
    protected byte[] formatInteger(double value) {
        return StringOperations.encodeAsciiBytes(String.valueOf((long) value));
    }

    @TruffleBoundary
    @Specialization(guards = "!isIntegerValue(value)")
    protected byte[] format(double value) {
        return StringOperations.encodeAsciiBytes(String.valueOf(value));
    }

    protected boolean isIntegerValue(double value) {
        /** General approach taken from StackOverflow:
         * http://stackoverflow.com/questions/703396/how-to-nicely-format-floating-numbers-to-string-without-unnecessary
         * -decimal-0 Answers provided by JasonD (http://stackoverflow.com/users/1288598/jasond) and Darthenius
         * (http://stackoverflow.com/users/974531/darthenius) Licensed by cc-wiki license:
         * http://creativecommons.org/licenses/by-sa/3.0/ */

        // TODO (nirvdrum 09-Mar-15) Make this adhere to the MRI invariant: "single-precision, network (big-endian) byte order"

        return value - Math.rint(value) == 0;
    }

}
