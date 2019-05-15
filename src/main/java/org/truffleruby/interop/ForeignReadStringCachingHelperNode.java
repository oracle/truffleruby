/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.language.RubyBaseNode;

@ImportStatic(StringCachingGuards.class)
public abstract class ForeignReadStringCachingHelperNode extends RubyBaseNode {

    @Child private IsStringLikeNode isStringLikeNode;

    public abstract Object executeStringCachingHelper(DynamicObject receiver, Object name) throws UnknownIdentifierException;

    @Specialization(guards = "isStringLike(name)")
    public Object cacheStringLikeAndForward(DynamicObject receiver, Object name,
            @Cached("create()") ToJavaStringNode toJavaStringNode,
            @Cached("createNextHelper()") ForeignReadStringCachedHelperNode nextHelper) throws UnknownIdentifierException {
        String nameAsJavaString = toJavaStringNode.executeToJavaString(name);
        boolean isIVar = isIVar(nameAsJavaString);
        return nextHelper.executeStringCachedHelper(receiver, name, nameAsJavaString, isIVar);
    }

    @Specialization(guards = "!isStringLike(name)")
    public Object indexObject(DynamicObject receiver, Object name,
            @Cached("createNextHelper()") ForeignReadStringCachedHelperNode nextHelper) throws UnknownIdentifierException {
        return nextHelper.executeStringCachedHelper(receiver, name, null, false);
    }

    protected boolean isStringLike(Object value) {
        if (isStringLikeNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isStringLikeNode = insert(IsStringLikeNode.create());
        }

        return isStringLikeNode.executeIsStringLike(value);
    }

    protected boolean isIVar(String name) {
        return !name.isEmpty() && name.charAt(0) == '@';
    }

    protected ForeignReadStringCachedHelperNode createNextHelper() {
        return ForeignReadStringCachedHelperNodeGen.create();
    }

}
