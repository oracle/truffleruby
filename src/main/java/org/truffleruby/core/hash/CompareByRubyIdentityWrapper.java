/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.hash.HashingNodes.ToHashByIdentity;

/** Wraps a value so that it will compared and hashed according to Ruby identity semantics. These semantics differ from
 * Java semantics notably for primitives (e.g. all the NaN are different according to Ruby, but Java compares them
 * equal). */
public final class CompareByRubyIdentityWrapper {

    public final Object value;

    public CompareByRubyIdentityWrapper(Object value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return ToHashByIdentity.getUncached().execute(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CompareByRubyIdentityWrapper &&
                ReferenceEqualNode
                        .getUncached()
                        .executeReferenceEqual(value, ((CompareByRubyIdentityWrapper) obj).value);
    }
}
