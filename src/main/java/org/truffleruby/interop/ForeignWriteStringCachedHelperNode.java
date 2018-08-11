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
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;

@NodeChildren({
        @NodeChild("receiver"),
        @NodeChild("name"),
        @NodeChild("stringName"),
        @NodeChild("isIVar"),
        @NodeChild("value")
})
abstract class ForeignWriteStringCachedHelperNode extends RubyNode {

    @Child private DoesRespondDispatchHeadNode indexDefinedNode;
    @Child private CallDispatchHeadNode callNode;

    protected final static String INDEX_SET_METHOD_NAME = "[]=";

    public abstract Object executeStringCachedHelper(VirtualFrame frame, DynamicObject receiver, Object name,
            Object stringName, boolean isIVar, Object value);

    @Specialization(guards = "isRubyArray(receiver) || isRubyHash(receiver)")
    public Object writeArrayHash(
            VirtualFrame frame,
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached("create()") ForeignToRubyNode nameToRubyNode) {
        return call(receiver, INDEX_SET_METHOD_NAME, nameToRubyNode.executeConvert(name), value);
    }

    @Specialization(guards = {"!isRubyArray(receiver)", "!isRubyHash(receiver)", "isIVar", "stringName.equals(cachedStringName)"})
    public Object writeInstanceVariable(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached("stringName") Object cachedStringName,
            @Cached("createWriteObjectFieldNode(stringName)") WriteObjectFieldNode writeObjectFieldNode) {
        writeObjectFieldNode.write(receiver, value);
        return value;
    }

    protected WriteObjectFieldNode createWriteObjectFieldNode(Object name) {
        return WriteObjectFieldNodeGen.create(name);
    }

    @Specialization(guards = {
            "!isRubyArray(receiver)", "!isRubyHash(receiver)", "!isIVar",
            "methodDefined(frame, receiver, INDEX_SET_METHOD_NAME, getIndexDefinedNode())"
    })
    public Object index(
            VirtualFrame frame,
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value,
            @Cached("create()") ForeignToRubyNode nameToRubyNode) {
        return call(receiver, INDEX_SET_METHOD_NAME, nameToRubyNode.executeConvert(name), value);
    }

    @Specialization(guards = {
            "!isRubyArray(receiver)", "!isRubyHash(receiver)", "!isIVar",
            "!methodDefined(frame, receiver, INDEX_SET_METHOD_NAME, getIndexDefinedNode())"
    })
    public Object unknownIdentifier(
            VirtualFrame frame,
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            Object value) {
        throw UnknownIdentifierException.raise(toString(name));
    }

    @CompilerDirectives.TruffleBoundary
    private String toString(Object name) {
        return name.toString();
    }

    protected DoesRespondDispatchHeadNode getIndexDefinedNode() {
        if (indexDefinedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            indexDefinedNode = insert(DoesRespondDispatchHeadNode.create());
        }

        return indexDefinedNode;
    }

    // TODO CS 9-Aug-17 test method defined once and then run specialisations

    protected boolean methodDefined(VirtualFrame frame, DynamicObject receiver, Object stringName,
                                    DoesRespondDispatchHeadNode definedNode) {
        if (stringName == null) {
            return false;
        } else {
            return definedNode.doesRespondTo(frame, stringName, receiver);
        }
    }

    protected Object call(DynamicObject receiver, String methodName, Object... arguments) {
        if (callNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callNode = insert(CallDispatchHeadNode.createOnSelf());
        }

        return callNode.call(receiver, methodName, arguments);
    }

}
