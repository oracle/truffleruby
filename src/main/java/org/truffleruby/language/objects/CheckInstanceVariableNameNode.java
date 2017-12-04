/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.parser.Identifiers;

@NodeChildren({@NodeChild("object"), @NodeChild("name")})
public abstract class CheckInstanceVariableNameNode extends RubyNode {

    public static CheckInstanceVariableNameNode create() {
        return CheckInstanceVariableNameNodeGen.create(null, null);
    }

    public abstract String executeCheck(VirtualFrame frame, DynamicObject object, Object name);

    @Specialization
    public String check(VirtualFrame frame, DynamicObject object, Object name,
                        @Cached("create()") NameToJavaStringNode nameToJavaStringNode) {
        String nameAsString = nameToJavaStringNode.executeToJavaString(frame, name);
        if (!Identifiers.isValidInstanceVariableName(nameAsString)) {
            throw new RaiseException(getContext().getCoreExceptions().nameErrorInstanceNameNotAllowable(
                nameAsString,
                name,
                object,
                this));
        }
        return nameAsString;
    }

}
