/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.nodes.Node;

/** Used by Thread#kill and to terminate threads. */
public final class KillException extends TerminationException {

    private static final long serialVersionUID = 4546683467567415385L;

    public KillException(Node location) {
        super("Thread#kill", location);
    }
}
