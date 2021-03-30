/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.interop.OutgoingForeignCallNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;

@GenerateUncached
public abstract class CallForeignMethodNode extends RubyBaseNode {

    public static CallForeignMethodNode create() {
        return CallForeignMethodNodeGen.create();
    }

    public static CallForeignMethodNode getUncached() {
        return CallForeignMethodNodeGen.getUncached();
    }

    public abstract Object execute(Object receiver, String methodName, Object block, Object[] arguments);

    @Specialization
    protected Object call(Object receiver, String methodName, Object block, Object[] arguments,
            @Cached OutgoingForeignCallNode foreignCall,
            @Cached TranslateExceptionNode translateException,
            @Cached ConditionProfile hasBlock,
            @Cached BranchProfile errorProfile) {
        assert block instanceof Nil || block instanceof RubyProc : block;

        Object[] newArguments = arguments;
        if (hasBlock.profile(block != nil)) {
            newArguments = new Object[arguments.length + 1];
            System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
            newArguments[arguments.length] = block;
        }

        try {
            return foreignCall.executeCall(receiver, methodName, newArguments);
        } catch (Throwable t) {
            errorProfile.enter();
            throw translateException.executeTranslation(t);
        }
    }
}
