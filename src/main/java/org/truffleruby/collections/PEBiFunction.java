/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.collections;

import java.util.function.BiFunction;

import com.oracle.truffle.api.frame.Frame;

/** A {@link BiFunction} that can be partially evaluated. */
public interface PEBiFunction {
    Object accept(Frame frame, Object hash, Object key);
}
