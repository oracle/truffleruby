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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.dispatch.DispatchNode;


public class HashNode extends RubyContextNode {

    @Child private DispatchNode hashNode;
    @Child private ObjectIDNode objectIDNode;
    @Child private ToRubyIntegerNode toRubyInteger;

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
        } else if (isBignumProfile1.profile(hashedObject instanceof RubyBignum)) {
            return BigIntegerOps.hashCode((RubyBignum) hashedObject);
        } else {
            if (toRubyInteger == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toRubyInteger = insert(ToRubyIntegerNode.create());
            }

            final Object coercedHashedObject = toRubyInteger.execute(hashedObject);

            if (isIntegerProfile2.profile(coercedHashedObject instanceof Integer)) {
                return (int) coercedHashedObject;
            } else if (isLongProfile2.profile(coercedHashedObject instanceof Long)) {
                return (int) (long) coercedHashedObject;
            } else if (isBignumProfile2.profile(coercedHashedObject instanceof RubyBignum)) {
                return BigIntegerOps.hashCode((RubyBignum) coercedHashedObject);
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }
    }

    private Object hash(Object object) {
        if (hashNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashNode = insert(DispatchNode.create());
        }
        return hashNode.call(object, "hash");
    }

    private Object objectID(Object object) {
        if (objectIDNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            objectIDNode = insert(ObjectIDNode.create());
        }
        return objectIDNode.execute(object);
    }

}
