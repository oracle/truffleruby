/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.Layouts;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.utils.UnreachableCodeException;

public class HashNode extends RubyContextNode {

    @Child private CallDispatchHeadNode hashNode;
    @Child private ObjectIDNode objectIDNode;
    @Child private CallDispatchHeadNode coerceToIntNode;

    private final ConditionProfile isIntegerProfile1 = ConditionProfile.create();
    private final ConditionProfile isLongProfile1 = ConditionProfile.create();
    private final ConditionProfile isBignumProfile1 = ConditionProfile.create();
    private final ConditionProfile isIntegerProfile2 = ConditionProfile.create();
    private final ConditionProfile isLongProfile2 = ConditionProfile.create();
    private final ConditionProfile isBignumProfile2 = ConditionProfile.create();

    public int hash(Object key, boolean compareByIdentity) {
        final Object hashedObject;
        if (compareByIdentity) {
            hashedObject = objectID(key);
        } else {
            hashedObject = hash(key);
        }

        if (isIntegerProfile1.profile(hashedObject instanceof Integer)) {
            return (int) hashedObject;
        } else if (isLongProfile1.profile(hashedObject instanceof Long)) {
            return (int) (long) hashedObject;
        } else if (isBignumProfile1.profile(Layouts.BIGNUM.isBignum(hashedObject))) {
            return BigIntegerOps.hashCode(hashedObject);
        } else {
            if (coerceToIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                coerceToIntNode = insert(CallDispatchHeadNode.createPrivate());
            }

            final Object coercedHashedObject = coerceToIntNode
                    .call(coreLibrary().truffleTypeModule, "coerce_to_int", hashedObject);

            if (isIntegerProfile2.profile(coercedHashedObject instanceof Integer)) {
                return (int) coercedHashedObject;
            } else if (isLongProfile2.profile(coercedHashedObject instanceof Long)) {
                return (int) (long) coercedHashedObject;
            } else if (isBignumProfile2.profile(Layouts.BIGNUM.isBignum(coercedHashedObject))) {
                return BigIntegerOps.hashCode(coercedHashedObject);
            } else {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new UnreachableCodeException();
            }
        }
    }

    private Object hash(Object object) {
        if (hashNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return hashNode.call(object, "hash");
    }

    private Object objectID(Object object) {
        if (objectIDNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            objectIDNode = insert(ObjectIDNode.create());
        }
        return objectIDNode.executeObjectID(object);
    }

}
