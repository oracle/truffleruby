/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
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
public abstract class ForeignWriteStringCachingHelperNode extends RubyBaseNode {

    @Child private IsStringLikeNode isStringLikeNode;

    public abstract Object executeStringCachingHelper(DynamicObject receiver, Object name, Object value) throws UnknownIdentifierException;

    @Specialization(guards = "isStringLike(name)")
    public Object cacheStringLikeAndForward(DynamicObject receiver, Object name, Object value,
            @Cached("create()") ToJavaStringNode toJavaStringNode,
            @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) throws UnknownIdentifierException {
        String nameAsJavaString = toJavaStringNode.executeToJavaString(name);
        boolean isIVar = isIVar(nameAsJavaString);
        return nextHelper.executeStringCachedHelper(receiver, name, nameAsJavaString, isIVar, value);
    }

    @Specialization(guards = "!isStringLike(name)")
    public Object passThroughNonString(DynamicObject receiver, Object name, Object value,
            @Cached("createNextHelper()") ForeignWriteStringCachedHelperNode nextHelper) throws UnknownIdentifierException {
        return nextHelper.executeStringCachedHelper(receiver, name, null, false, value);
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

    protected ForeignWriteStringCachedHelperNode createNextHelper() {
        return ForeignWriteStringCachedHelperNodeGen.create();
    }

}
