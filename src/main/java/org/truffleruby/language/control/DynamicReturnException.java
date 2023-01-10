/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.nodes.ControlFlowException;

@SuppressWarnings("serial")
public final class DynamicReturnException extends ControlFlowException {

    private final ReturnID returnID;
    private final Object value;

    public DynamicReturnException(ReturnID returnID, Object value) {
        this.returnID = returnID;
        this.value = value;
    }

    public ReturnID getReturnID() {
        return returnID;
    }

    public Object getValue() {
        return value;
    }

}
