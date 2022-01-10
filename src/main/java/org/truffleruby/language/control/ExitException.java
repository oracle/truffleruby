/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.nodes.Node;

/** Exception sent by the hard Kernel#exit! */
public final class ExitException extends TerminationException {

    private static final long serialVersionUID = 8152389017577849952L;

    private final int code;

    public ExitException(int code, Node location) {
        super("Kernel#exit!", location);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
