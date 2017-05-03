/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;

@NodeChildren({
        @NodeChild("receiver"),
        @NodeChild("name"),
        @NodeChild("stringName"),
        @NodeChild("isIVar")
})
abstract class ForeignReadStringCachedHelperNode extends RubyNode {

    @Child private DoesRespondDispatchHeadNode definedNode;
    @Child private DoesRespondDispatchHeadNode indexDefinedNode;
    @Child private CallDispatchHeadNode callNode;

    protected final static String INDEX_METHOD_NAME = "[]";

    public abstract Object executeStringCachedHelper(VirtualFrame frame, DynamicObject receiver, Object name, Object stringName, boolean isIVar);

    @Specialization(guards = "isIVar")
    public Object readInstanceVariable(
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar,
            @Cached("createReadObjectFieldNode(stringName)") ReadObjectFieldNode readObjectFieldNode) {
        return readObjectFieldNode.execute(receiver);
    }

    protected ReadObjectFieldNode createReadObjectFieldNode(Object name) {
        return ReadObjectFieldNodeGen.create(name, nil());
    }

    @Specialization(guards = {
            "!isIVar",
            "methodDefined(frame, receiver, stringName, getDefinedNode())"
    })
    public Object callMethod(
            VirtualFrame frame,
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar) {
        return getCallNode().call(frame, receiver, stringName);
    }

    @Specialization(guards = {
            "!isIVar",
            "!methodDefined(frame, receiver, stringName, getDefinedNode())",
            "methodDefined(frame, receiver, INDEX_METHOD_NAME, getIndexDefinedNode())"
    })
    public Object index(
            VirtualFrame frame,
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar) {
        return getCallNode().call(frame, receiver, "[]", name);
    }

    @Specialization(guards = {
            "!isIVar",
            "!methodDefined(frame, receiver, stringName, getDefinedNode())",
            "!methodDefined(frame, receiver, INDEX_METHOD_NAME, getIndexDefinedNode())"
    })
    public Object indexUnknown(
            VirtualFrame frame,
            DynamicObject receiver,
            Object name,
            Object stringName,
            boolean isIVar) {
        throw UnknownIdentifierException.raise(toString(name));
    }

    @TruffleBoundary
    private String toString(Object name) {
        return name.toString();
    }

    protected DoesRespondDispatchHeadNode getDefinedNode() {
        if (definedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            definedNode = insert(new DoesRespondDispatchHeadNode(true));
        }

        return definedNode;
    }

    protected DoesRespondDispatchHeadNode getIndexDefinedNode() {
        if (indexDefinedNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            indexDefinedNode = insert(new DoesRespondDispatchHeadNode(true));
        }

        return indexDefinedNode;
    }

    protected boolean methodDefined(VirtualFrame frame, DynamicObject receiver, Object stringName,
                                    DoesRespondDispatchHeadNode definedNode) {
        if (stringName == null) {
            return false;
        } else {
            return definedNode.doesRespondTo(frame, stringName, receiver);
        }
    }

    protected CallDispatchHeadNode getCallNode() {
        if (callNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callNode = insert(DispatchHeadNodeFactory.createMethodCall(true));
        }

        return callNode;
    }

}
