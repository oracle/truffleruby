/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import org.truffleruby.language.Nil;

public class ThreadLocalGlobals {

    public Object exception; // $!
    public Object processStatus; // $?

    public ThreadLocalGlobals() {
        this.exception = Nil.INSTANCE;
        this.processStatus = Nil.INSTANCE;
    }
}
