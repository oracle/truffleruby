/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqlNode;
import org.truffleruby.language.RubyBaseNode;

@GenerateUncached
public abstract class CompareHashKeysNode extends RubyBaseNode {

    public static CompareHashKeysNode create() {
        return CompareHashKeysNodeGen.create();
    }

    public static CompareHashKeysNode getUncached() {
        return CompareHashKeysNodeGen.getUncached();
    }

    public abstract boolean execute(boolean compareByIdentity, Object key, int hashed,
            Object otherKey, int otherHashed);

    /** Checks if the two keys are the same object, which is used by both modes (by identity or not) of lookup. Enables
     * to check if the two keys are the same without a method call. */
    public static boolean referenceEqualKeys(ReferenceEqualNode refEqual, boolean compareByIdentity, Object key,
            int hashed, Object otherKey, int otherHashed) {
        return compareByIdentity
                ? refEqual.executeReferenceEqual(key, otherKey)
                : hashed == otherHashed && refEqual.executeReferenceEqual(key, otherKey);
    }

    @Specialization(guards = "compareByIdentity")
    protected boolean refEquals(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed,
            @Cached ReferenceEqualNode refEqual) {
        return refEqual.executeReferenceEqual(key, otherKey);
    }

    @Specialization(guards = "!compareByIdentity")
    protected boolean same(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed,
            @Cached SameOrEqlNode same) {
        return hashed == otherHashed && same.execute(key, otherKey);
    }
}
