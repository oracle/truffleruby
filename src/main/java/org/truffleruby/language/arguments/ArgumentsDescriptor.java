/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

/** An arguments descriptor is an object that describes something about the arguments being passed. Argument descriptors
 * are stable, immutable objects, suitable to be cached and guarded by identity. They'd normally convey some kind of
 * static information from call site to callee. */
public abstract class ArgumentsDescriptor {
}
