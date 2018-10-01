/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import com.oracle.truffle.api.object.DynamicObject;

public class ThreadLocalGlobals {

    public DynamicObject exception; // $!
    public DynamicObject processStatus; // $?

    public ThreadLocalGlobals(DynamicObject exception, DynamicObject processStatus) {
        this.exception = exception;
        this.processStatus = processStatus;
    }
}
