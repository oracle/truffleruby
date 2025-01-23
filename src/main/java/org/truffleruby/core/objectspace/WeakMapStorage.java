/*
 * Copyright (c) 2020, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.objectspace;

import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.hash.CompareByRubyIdentityWrapper;

public final class WeakMapStorage extends WeakValueCache<CompareByRubyIdentityWrapper, Object> {
}
