/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@GenerateUncached
public abstract class ForeignReadStringCachedHelperNode extends RubyBaseNode {

    public static ForeignReadStringCachedHelperNode create() {
        return ForeignReadStringCachedHelperNodeGen.create();
    }

    protected final static String INDEX_METHOD_NAME = "[]";
    protected final static String FETCH_METHOD_NAME = "fetch";
    protected final static String METHOD_NAME = "method";

    public abstract Object executeStringCachedHelper(
            DynamicObject receiver, Object name, Object stringName, boolean isIVar)
            throws UnknownIdentifierException, InvalidArrayIndexException;

    @Specialization(guards = { "isIVar" })
    protected Object readInstanceVariable(
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

    @Specialization(guards = { "!isIVar", "indexMethod(definedIndexNode, receiver)" })
    protected Object callIndex(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached DoesRespondDispatchHeadNode definedIndexNode,
            @Cached("createBinaryProfile()") ConditionProfile errorProfile,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached CallDispatchHeadNode dispatch) throws UnknownIdentifierException {
        try {
            return dispatch.call(receiver, INDEX_METHOD_NAME, nameToRubyNode.executeConvert(name));
        } catch (RaiseException ex) {
            // translate NameError to UnknownIdentifierException
            DynamicObject logicalClass = Layouts.BASIC_OBJECT.getLogicalClass(ex.getException());
            if (errorProfile.profile(logicalClass == context.getCoreLibrary().nameErrorClass)) {
                throw UnknownIdentifierException.create((String) stringName);
            } else {
                throw ex;
            }
        }
    }

    @Specialization(guards = {
            "!isIVar",
            "!indexMethod(definedIndexNode, receiver)",
            "methodDefined(receiver, stringName, definedNode)" })
    protected Object getBoundMethod(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached DoesRespondDispatchHeadNode definedIndexNode,
            @Cached DoesRespondDispatchHeadNode definedNode,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached CallDispatchHeadNode dispatch) {
        return dispatch.call(receiver, METHOD_NAME, nameToRubyNode.executeConvert(name));
    }

    @Specialization(guards = {
            "!isIVar",
            "!indexMethod(definedIndexNode, receiver)",
            "!methodDefined(receiver, stringName, definedNode)" })
    protected Object unknownIdentifier(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached DoesRespondDispatchHeadNode definedIndexNode,
            @Cached DoesRespondDispatchHeadNode definedNode)
            throws UnknownIdentifierException {
        throw UnknownIdentifierException.create(toString(name));
    }

    @TruffleBoundary
    private String toString(Object name) {
        return name.toString();
    }

    protected static boolean indexMethod(DoesRespondDispatchHeadNode definedIndexNode, DynamicObject receiver) {
        return methodDefined(receiver, INDEX_METHOD_NAME, definedIndexNode) &&
                !RubyGuards.isRubyArray(receiver) &&
                !RubyGuards.isRubyHash(receiver) &&
                !RubyGuards.isRubyProc(receiver) &&
                !RubyGuards.isRubyClass(receiver);
    }

    protected static boolean methodDefined(DynamicObject receiver, Object stringName,
            DoesRespondDispatchHeadNode definedNode) {
        if (stringName == null) {
            return false;
        } else {
            return definedNode.doesRespondTo(null, stringName, receiver);
        }
    }
}
