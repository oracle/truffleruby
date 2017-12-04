/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.language.RubyNode;

@NodeChildren({ @NodeChild("object"), @NodeChild("name") })
public abstract class ObjectIVarGetNode extends RubyNode {

    private final boolean checkName;
    @Child private CheckInstanceVariableNameNode checkInstanceVariableNameNode;

    public ObjectIVarGetNode(boolean checkName) {
        this.checkName = checkName;
    }

    public abstract Object executeIVarGet(VirtualFrame frame, DynamicObject object, Object name);

    @Specialization(guards = "name == cachedName", limit = "getCacheLimit()")
    public Object ivarGetCached(VirtualFrame frame, DynamicObject object, Object name,
                                @Cached("name") Object cachedName,
                                @Cached("createReadFieldNode(checkName(frame, cachedName, object))") ReadObjectFieldNode readObjectFieldNode) {
        return readObjectFieldNode.execute(object);
    }

    @Specialization(replaces = "ivarGetCached")
    public Object ivarGetUncached(VirtualFrame frame, DynamicObject object, Object name) {
        return read(object, checkName(frame, name, object));
    }

    @TruffleBoundary
    protected Object read(DynamicObject object, Object name){
        return ReadObjectFieldNode.read(object, name, nil());
    }

    protected Object checkName(VirtualFrame frame, Object name, DynamicObject object) {
        if(checkName){
            return checkInstanceVariableName(frame, name, object);
        } else {
            return name;
        }
   }

   protected String checkInstanceVariableName(VirtualFrame frame, Object name, DynamicObject object){
       if (checkInstanceVariableNameNode == null) {
           CompilerDirectives.transferToInterpreterAndInvalidate();
           checkInstanceVariableNameNode = insert(CheckInstanceVariableNameNode.create());
       }
       return checkInstanceVariableNameNode.executeCheck(frame, object, name);
   }

    protected ReadObjectFieldNode createReadFieldNode(Object name) {
        return ReadObjectFieldNodeGen.create(name, nil());
    }

    protected int getCacheLimit() {
        return getContext().getOptions().INSTANCE_VARIABLE_CACHE;
    }

}
