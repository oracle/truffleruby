/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.java;

import com.oracle.truffle.api.TruffleOptions;
import org.truffleruby.platform.ProcessName;

public class JavaProcessName implements ProcessName {

    @Override
    public boolean canSet() {
        return TruffleOptions.AOT;
    }

    @Override
    public void set(String name) {
        if (TruffleOptions.AOT) {
            Compiler.command(new Object[] { "com.oracle.svm.core.JavaMainWrapper.setCRuntimeArgument0(String)boolean", name });
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
