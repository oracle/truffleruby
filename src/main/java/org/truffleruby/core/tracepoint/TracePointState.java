/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.tracepoint;

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.symbol.RubySymbol;

public final class TracePointState {

    boolean insideProc = false;
    RubySymbol event = null;
    DynamicObject path = null;
    int line = 0;
    DynamicObject binding = null;

}
