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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.basicobject.BasicObjectLayoutImpl;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;

@GenerateUncached
public abstract class ForeignReadStringCachedHelperNode extends RubyBaseWithoutContextNode {

    public static ForeignReadStringCachedHelperNode create() {
        return ForeignReadStringCachedHelperNodeGen.create();
    }

    protected final static String INDEX_METHOD_NAME = "[]";
    protected final static String FETCH_METHOD_NAME = "fetch";
    protected final static String METHOD_NAME = "method";

    public abstract Object executeStringCachedHelper(DynamicObject receiver, Object name, Object stringName, boolean isIVar) throws UnknownIdentifierException, InvalidArrayIndexException;

    protected static boolean arrayIndex(DynamicObject receiver, Object stringName) {
        return RubyGuards.isRubyArray(receiver) && stringName == null;
    }

    @Specialization(guards = "arrayIndex(receiver, stringName)")
    public Object readArray(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatch,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("createBinaryProfile()") ConditionProfile errorProfile) throws InvalidArrayIndexException {
        Object index = nameToRubyNode.executeConvert(name);
        try {
            return dispatch.call(receiver, FETCH_METHOD_NAME, index);
        } catch (RaiseException ex) {
            DynamicObject logicalClass = ((BasicObjectLayoutImpl.BasicObjectType) ex.getException().getShape().getObjectType()).getLogicalClass();
            if (errorProfile.profile(logicalClass == context.getCoreLibrary().getIndexErrorClass())) {
                throw InvalidArrayIndexException.create((Long) index);
            } else {
                throw ex;
            }
        }
    }

    @Specialization(guards = "isRubyHash(receiver)")
    public Object readArrayHash(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatch,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached("createBinaryProfile()") ConditionProfile errorProfile) throws UnknownIdentifierException {
        Object key = nameToRubyNode.executeConvert(name);
        try {
            return dispatch.call(receiver, FETCH_METHOD_NAME, key);
        } catch (RaiseException ex) {
            DynamicObject logicalClass = ((BasicObjectLayoutImpl.BasicObjectType) ex.getException().getShape().getObjectType()).getLogicalClass();
            if (errorProfile.profile(logicalClass == context.getCoreLibrary().getKeyErrorClass())) {
                throw UnknownIdentifierException.create((String) stringName);
            } else {
                throw ex;
            }
        }
    }

    @Specialization(guards = { "!isRubyHash(receiver)", "isIVar" })
    public Object readInstanceVariable(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached ReadObjectFieldNode readObjectFieldNode) throws UnknownIdentifierException {
        Object result = readObjectFieldNode.execute(receiver, stringName, null);
        if (result != null) {
            return result;
        } else {
            throw UnknownIdentifierException.create((String) stringName);
        }
    }

    @Specialization(guards = {
            "!isRubyArray(receiver)", "!isRubyHash(receiver)", "!isIVar", "!isRubyProc(receiver)",
            "methodDefined(receiver, INDEX_METHOD_NAME, definedIndexNode)"
    })
    public Object callIndex(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode definedIndexNode,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatch) {
        return dispatch.call(receiver, INDEX_METHOD_NAME, nameToRubyNode.executeConvert(name));
    }

    @Specialization(guards = {
            "!isRubyHash(receiver)", "!isIVar",
            "!methodDefined(receiver, INDEX_METHOD_NAME, definedIndexNode) || (isRubyArray(receiver) || isRubyProc(receiver))",
            "methodDefined(receiver, stringName, definedNode)"
    })
    public Object getBoundMethod(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode definedIndexNode,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode definedNode,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatch) {
        return dispatch.call(receiver, METHOD_NAME, nameToRubyNode.executeConvert(name));
    }

    @Specialization(guards = {
            "!isRubyArray(receiver)", "!isRubyHash(receiver)", "!isIVar",
            "!methodDefined(receiver, INDEX_METHOD_NAME, definedIndexNode) || (isRubyArray(receiver) || isRubyProc(receiver))",
            "!methodDefined(receiver, stringName, definedNode)"
    })
    public Object unknownIdentifier(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode definedIndexNode,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode definedNode) throws UnknownIdentifierException {
        throw UnknownIdentifierException.create(toString(name));
    }

    @TruffleBoundary
    private String toString(Object name) {
        return name.toString();
    }

    protected static boolean methodDefined(DynamicObject receiver, Object stringName, DoesRespondDispatchHeadNode definedNode) {
        if (stringName == null) {
            return false;
        } else {
            return definedNode.doesRespondTo(null, stringName, receiver);
        }
    }
}
