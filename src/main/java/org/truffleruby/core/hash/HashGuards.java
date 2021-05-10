/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.RubyLanguage;

public abstract class HashGuards {

    // Storage strategies

    public static int hashStrategyLimit() {
        return 3;
    }

    public static int packedHashLimit() {
        // + 1 for packed Hash with size = 0
        return RubyLanguage.getCurrentLanguage().options.HASH_PACKED_ARRAY_MAX + 1;
    }

    // Higher level properties

    public static boolean isEmptyHash(RubyHash hash) {
        return hash.size == 0;
    }

    public static boolean isCompareByIdentity(RubyHash hash) {
        return hash.compareByIdentity;
    }
}
