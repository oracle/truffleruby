/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.objectspace;

import org.truffleruby.collections.ConcurrentWeakKeysMap;
import org.truffleruby.core.hash.CompareByRubyHashEqlWrapper;

// An alias of ConcurrentWeakKeysMap<CompareByRubyEqlWrapper, Object> to keep things short
public final class WeakKeyMapStorage extends ConcurrentWeakKeysMap<CompareByRubyHashEqlWrapper, Object> {
}
