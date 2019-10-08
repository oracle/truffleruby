/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class EnumeratorSizeNode extends RubyNode {

    @Child private RubyNode method;
    @Child private CallDispatchHeadNode toEnumWithSize;

    private final ConditionProfile noBlockProfile = ConditionProfile.createBinaryProfile();

    private final DynamicObject methodName;
    private final DynamicObject sizeMethodName;

    public EnumeratorSizeNode(String enumeratorSize, String methodName, RubyNode method) {
        this.method = method;
        this.methodName = getSymbol(methodName);
        this.sizeMethodName = getSymbol(enumeratorSize);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final DynamicObject block = RubyArguments.getBlock(frame);

        if (noBlockProfile.profile(block == null)) {
            if (toEnumWithSize == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toEnumWithSize = insert(CallDispatchHeadNode.createPrivate());
            }

            final Object self = RubyArguments.getSelf(frame);
            return toEnumWithSize.call(
                    coreLibrary().getTruffleKernelOperationsModule(),
                    "to_enum_with_size",
                    self,
                    methodName,
                    sizeMethodName);
        } else {
            return method.execute(frame);
        }
    }

}
