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

import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;

@GenerateUncached
abstract class ForeignWriteStringCachedHelperNode extends RubyBaseWithoutContextNode {

    protected final static String INDEX_SET_METHOD_NAME = "[]=";

    public static ForeignWriteStringCachedHelperNode create() {
        return ForeignWriteStringCachedHelperNodeGen.create();
    }

    public abstract Object executeStringCachedHelper(DynamicObject receiver, Object name,
            Object stringName, boolean isIVar, Object value) throws UnknownIdentifierException;

    @Specialization(guards = "isRubyArray(receiver) || isRubyHash(receiver)")
    protected Object writeArrayHash(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached(value = "createPrivate()") CallDispatchHeadNode dispatch) {
        return dispatch.call(receiver, INDEX_SET_METHOD_NAME, nameToRubyNode.executeConvert(name), value);
    }

    @Specialization(guards = { "!isRubyArray(receiver)", "!isRubyHash(receiver)", "isIVar" })
    protected Object writeInstanceVariable(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached WriteObjectFieldNode writeObjectFieldNode) {
        writeObjectFieldNode.write(receiver, stringName, value);
        return value;
    }

    @Specialization(
            guards = {
                    "!isRubyArray(receiver)",
                    "!isRubyHash(receiver)",
                    "!isIVar",
                    "methodDefined(receiver, INDEX_SET_METHOD_NAME, doesRespond)" })
    protected Object index(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached(value = "createPrivate()") CallDispatchHeadNode dispatch,
            @Cached DoesRespondDispatchHeadNode doesRespond) {
        return dispatch.call(receiver, INDEX_SET_METHOD_NAME, nameToRubyNode.executeConvert(name), value);
    }

    @Specialization(
            guards = {
                    "!isRubyArray(receiver)",
                    "!isRubyHash(receiver)",
                    "!isIVar",
                    "!methodDefined(receiver, INDEX_SET_METHOD_NAME, doesRespond)" })
    protected Object unknownIdentifier(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached DoesRespondDispatchHeadNode doesRespond) throws UnknownIdentifierException {
        throw UnknownIdentifierException.create(toString(name));
    }

    @CompilerDirectives.TruffleBoundary
    private String toString(Object name) {
        return name.toString();
    }

    // TODO CS 9-Aug-17 test method defined once and then run specialisations

    protected static boolean methodDefined(DynamicObject receiver, Object stringName,
            DoesRespondDispatchHeadNode definedNode) {
        if (stringName == null) {
            return false;
        } else {
            return definedNode.doesRespondTo(null, stringName, receiver);
        }
    }

}
