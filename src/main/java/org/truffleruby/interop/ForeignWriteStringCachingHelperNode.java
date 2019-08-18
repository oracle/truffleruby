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

import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.language.RubyBaseWithoutContextNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;

@GenerateUncached
@ImportStatic(StringCachingGuards.class)
public abstract class ForeignWriteStringCachingHelperNode extends RubyBaseWithoutContextNode {

    public abstract Object executeStringCachingHelper(
            DynamicObject receiver,
            Object name,
            Object value) throws UnknownIdentifierException;

    @Specialization(guards = "isStringLike.executeIsStringLike(name)")
    protected Object cacheStringLikeAndForward(
            DynamicObject receiver, Object name, Object value,
            @Cached ToJavaStringNode toJavaStringNode,
            @Cached ForeignWriteStringCachedHelperNode nextHelper,
            @Cached IsStringLikeNode isStringLike) throws UnknownIdentifierException {
        String nameAsJavaString = toJavaStringNode.executeToJavaString(name);
        boolean isIVar = isIVar(nameAsJavaString);
        return nextHelper.executeStringCachedHelper(receiver, name, nameAsJavaString, isIVar, value);
    }

    @Specialization(guards = "!isStringLike.executeIsStringLike(name)")
    protected Object passThroughNonString(
            DynamicObject receiver, Object name, Object value,
            @Cached ForeignWriteStringCachedHelperNode nextHelper,
            @Cached IsStringLikeNode isStringLike) throws UnknownIdentifierException {
        return nextHelper.executeStringCachedHelper(receiver, name, null, false, value);
    }

    protected boolean isIVar(String name) {
        return !name.isEmpty() && name.charAt(0) == '@';
    }

}
