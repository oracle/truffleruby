/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.language.RubyBaseNode;

/** A core method that should always be executed inline, without going through a CallTarget. That enables accessing the
 * caller frame efficiently and reliably. If called from a foreign language, then the caller frame will be null. Such a
 * method will not appear in backtraces. However, Ruby exceptions emitted from this node will be resent through a
 * CallTarget to get the proper backtrace. Such a core method should not emit significantly more Graal nodes than a
 * non-inlined call, as Truffle cannot decide to not inline it, and that could lead to too big methods to compile. */
@GenerateNodeFactory
public abstract class AlwaysInlinedMethodNode extends RubyBaseNode {

    public abstract Object execute(Frame callerFrame, Object self, Object[] args, Object block);

}
