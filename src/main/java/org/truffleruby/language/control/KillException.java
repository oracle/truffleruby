/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
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
@SuppressWarnings("serial")
public final class KillException extends TerminationException {
    public KillException(Node location) {
        super("Thread#kill", location);
    }
}
