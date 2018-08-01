/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;

public class BoxedValue implements TruffleObject {

    private final Object value;

    public BoxedValue(Object value) {
        this.value = value;
    }

    public Object getNumber() {
        return value;
    }

    @Override
    public ForeignAccess getForeignAccess() {
        return BoxedValueMessageResolutionForeign.ACCESS;
    }

}
