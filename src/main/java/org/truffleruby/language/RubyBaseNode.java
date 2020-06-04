/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.TypeSystemReference;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.array.ArrayUtils;

/** Base of all Ruby nodes */
@TypeSystemReference(RubyTypes.class)
@ImportStatic(RubyGuards.class)
public abstract class RubyBaseNode extends Node {

    public static final Object[] EMPTY_ARGUMENTS = ArrayUtils.EMPTY_ARRAY;

    public static final Object nil = Nil.INSTANCE;

}
