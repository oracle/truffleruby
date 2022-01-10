/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import static org.truffleruby.cext.ValueWrapperManager.isMallocAligned;

import com.oracle.truffle.api.dsl.Fallback;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

/** The IsNativeObjectNode is implemented to determine if a native pointer belongs to a natively allocated NODE* which
 * are used in Ripper. */
@GenerateUncached
public abstract class IsNativeObjectNode extends RubyBaseNode {

    /** Returns true if handle was natively allocated. */
    public abstract Object execute(Object handle);

    @Specialization
    protected boolean isNativeObjectTaggedObject(long handle) {
        return isMallocAligned(handle) && handle < ValueWrapperManager.ALLOCATION_BASE;
    }

    @Fallback
    protected boolean isNativeObjectFallback(Object handle) {
        return false;
    }

}
