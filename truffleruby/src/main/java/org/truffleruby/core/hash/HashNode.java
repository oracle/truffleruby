/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.ObjectIDNodeFactory;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.SnippetNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;

public class HashNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode hashNode;
    @Child private ObjectIDNode objectIDNode;
    @Child private SnippetNode snippetNode;

    private final ConditionProfile isIntegerProfile1 = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isLongProfile1 = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isBignumProfile1 = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isIntegerProfile2 = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isLongProfile2 = ConditionProfile.createBinaryProfile();
    private final ConditionProfile isBignumProfile2 = ConditionProfile.createBinaryProfile();

    public int hash(VirtualFrame frame, Object key, boolean compareByIdentity) {
        final Object hashedObject;
        if (compareByIdentity) {
            hashedObject = objectID(key);
        } else {
            hashedObject = hash(frame, key);
        }

        if (isIntegerProfile1.profile(hashedObject instanceof Integer)) {
            return (int) hashedObject;
        } else if (isLongProfile1.profile(hashedObject instanceof Long)) {
            return (int) (long) hashedObject;
        } else if (isBignumProfile1.profile(Layouts.BIGNUM.isBignum(hashedObject))) {
            return Layouts.BIGNUM.getValue((DynamicObject) hashedObject).hashCode();
        } else {
            if (snippetNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                snippetNode = insert(new SnippetNode());
            }

            final Object coercedHashedObject = snippetNode.execute(frame,
                    "Rubinius::Type.coerce_to_int hashedObject",
                    "hashedObject", hashedObject);

            if (isIntegerProfile2.profile(coercedHashedObject instanceof Integer)) {
                return (int) coercedHashedObject;
            } else if (isLongProfile2.profile(coercedHashedObject instanceof Long)) {
                return (int) (long) coercedHashedObject;
            } else if (isBignumProfile2.profile(Layouts.BIGNUM.isBignum(coercedHashedObject))) {
                return Layouts.BIGNUM.getValue((DynamicObject) coercedHashedObject).hashCode();
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private Object hash(VirtualFrame frame, Object object) {
        if (hashNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashNode = insert(DispatchHeadNodeFactory.createMethodCall(true));
        }
        return hashNode.call(frame, object, "hash");
    }

    private Object objectID(Object object) {
        if (objectIDNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            objectIDNode = insert(ObjectIDNodeFactory.create(null));
        }
        return objectIDNode.executeObjectID(object);
    }

}
