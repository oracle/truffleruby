/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import static org.truffleruby.cext.ValueWrapperManager.FALSE_HANDLE;
import static org.truffleruby.cext.ValueWrapperManager.OBJECT_TAG;
import static org.truffleruby.cext.ValueWrapperManager.TAG_MASK;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@ImportStatic({ ValueWrapperManager.class })
public abstract class IsNativeObjectNode extends RubyBaseNode {

    public abstract Object execute(Object handle);

    @Specialization(guards = "handle <= UNDEF_HANDLE")
    protected boolean isNativeObjectNilTrueFalseUndef(long handle) {
        return false;
    }

    @Specialization(guards = "isTaggedLong(handle)")
    protected boolean isNativeObjectTaggedLong(long handle) {
        return false;
    }

    @Specialization(guards = "isTaggedObject(handle)")
    protected boolean isNativeObjectTaggedObject(long handle) {
        return handle < ValueWrapperManager.ALLOCATION_BASE;
    }

    @Specialization
    protected boolean isNativeObjectValueWrapper(ValueWrapper handle) {
        return false;
    }

    @Fallback
    protected boolean isNativeObjectFallback(Object handle) {
        return false;
    }

    public static boolean isTaggedObject(long handle) {
        return handle != FALSE_HANDLE && (handle & TAG_MASK) == OBJECT_TAG;
    }

}