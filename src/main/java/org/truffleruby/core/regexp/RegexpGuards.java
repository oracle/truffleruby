/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.regexp;

import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.RubyString;

public class RegexpGuards {

    public static boolean isInitialized(RubyRegexp regexp) {
        return regexp.regex != null;
    }

    public static boolean isRegexpLiteral(RubyRegexp regexp) {
        return regexp.options.isLiteral();
    }

    public static boolean isValidEncoding(RubyString string, RopeNodes.CodeRangeNode rangeNode) {
        return rangeNode.execute(string.rope) != CodeRange.CR_BROKEN;
    }

    public static boolean isSameRegexp(RubyRegexp a, RubyRegexp b) {
        return a.regex == b.regex;
    }

}
