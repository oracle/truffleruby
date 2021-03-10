/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import org.truffleruby.language.control.RaiseException;

/** Wraps a {@link RaiseException} occuring during a module autoload, whenever the autoloaded constant is itself the
 * module for another constant read. */
public class AutoloadException extends AbstractTruffleException {

    private static final long serialVersionUID = 1L;

    public final RaiseException raiseException;

    public AutoloadException(RaiseException raiseException) {
        this.raiseException = raiseException;
    }
}
