/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;

public final class EncodingPointerAdapter implements TruffleObject {

    private final DynamicObject encoding;

    public EncodingPointerAdapter(DynamicObject string) {
        this.encoding = string;
    }

    public DynamicObject getEncoding() {
        return encoding;
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return EncodingPointerMessageResolutionForeign.ACCESS;
    }

}
