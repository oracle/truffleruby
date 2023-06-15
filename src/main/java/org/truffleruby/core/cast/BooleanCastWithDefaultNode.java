/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.NotProvided;

import com.oracle.truffle.api.dsl.Specialization;

/** Casts a value into a boolean and defaults to the given value if not provided. */
public abstract class BooleanCastWithDefaultNode extends RubyBaseNode {

    public abstract boolean execute(Object value, boolean defaultValue);

    @Specialization
    protected boolean doDefault(NotProvided value, boolean defaultValue) {
        return defaultValue;
    }

    @Fallback
    protected boolean fallback(Object value, boolean defaultValue,
            @Cached BooleanCastNode booleanCastNode) {
        return booleanCastNode.execute(value);
    }
}
