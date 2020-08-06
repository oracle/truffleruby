/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.util.function.BiConsumer;

import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.thread.RubyThread;

public interface SafepointAction extends BiConsumer<RubyThread, Node> {

}

