/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.language.RubyBaseNode;

@GenerateUncached
public abstract class InvokeMemberNode extends RubyBaseNode {

    private static Object invoke(InteropLibrary receivers, Object receiver, String member, Object[] args,
            TranslateInteropExceptionNode translateInteropExceptionNode) {
        try {
            return receivers.invokeMember(receiver, member, args);
        } catch (InteropException e) {
            throw translateInteropExceptionNode.executeInInvokeMember(e, receiver, args);
        }
    }

    public abstract Object execute(Object receiver, Object identifier, Object[] args);

    @Specialization(limit = "getInteropCacheLimit()")
    protected Object invokeCached(Object receiver, Object identifier, Object[] args,
            @Cached ToJavaStringNode toJavaStringNode,
            @CachedLibrary("receiver") InteropLibrary receivers,
            @Cached ForeignToRubyNode foreignToRubyNode,
            @Cached TranslateInteropExceptionNode translateInteropException) {
        final String name = toJavaStringNode.executeToJavaString(identifier);
        final Object foreign = invoke(receivers, receiver, name, args, translateInteropException);
        return foreignToRubyNode.executeConvert(foreign);
    }
}
