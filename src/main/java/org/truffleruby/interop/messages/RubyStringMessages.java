/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop.messages;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyLibrary;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
@ExportLibrary(value = RubyLibrary.class, receiverType = DynamicObject.class)
public class RubyStringMessages extends RubyObjectMessages {

    @ExportMessage
    protected static boolean isString(DynamicObject string) {
        return true;
    }

    @ExportMessage
    protected static String asString(DynamicObject string,
            @Cached ToJavaStringNode toJavaStringNode) {
        return toJavaStringNode.executeToJavaString(string);
    }

    @ExportMessage
    protected static void freeze(DynamicObject object) {
        ((RubyString) object).frozen = true;
    }


    @ExportMessage
    protected static boolean isFrozen(DynamicObject object) {
        return ((RubyString) object).frozen;
    }

    @ExportMessage
    protected static boolean isTainted(DynamicObject object) {
        return ((RubyString) object).tainted;
    }

    @ExportMessage
    protected static void taint(DynamicObject object,
            @CachedLibrary("object") RubyLibrary rubyLibrary,
            @Cached.Exclusive @Cached BranchProfile errorProfile,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        RubyString self = (RubyString) object;

        if (!self.tainted && self.frozen) {
            errorProfile.enter();
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().frozenError(object, RubyObjectMessages.getNode(rubyLibrary)));
        }

        self.tainted = true;
    }

    @ExportMessage
    protected static void untaint(DynamicObject object,
            @CachedLibrary("object") RubyLibrary rubyLibrary,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached.Exclusive @Cached BranchProfile errorProfile) {
        RubyString self = (RubyString) object;
        if (!self.tainted) {
            return;
        }

        if (self.frozen) {
            errorProfile.enter();
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().frozenError(object, RubyObjectMessages.getNode(rubyLibrary)));
        }

        self.tainted = false;
    }


}
