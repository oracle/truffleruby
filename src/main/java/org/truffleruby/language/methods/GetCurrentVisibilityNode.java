/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;

public class GetCurrentVisibilityNode extends RubyBaseNode {

    public Visibility getVisibility(VirtualFrame frame) {
        final Visibility visibility = RubyArguments.getDeclarationContext(frame).visibility;
        if (visibility != null) {
            return visibility;
        } else {
            return DeclarationContext.findVisibility(RubyArguments.getDeclarationFrame(frame));
        }
    }

    @TruffleBoundary
    public static Visibility getVisibilityFromNameAndFrame(String name, Frame frame) {
        if (ModuleOperations.isMethodPrivateFromName(name)) {
            return Visibility.PRIVATE;
        } else {
            return DeclarationContext.findVisibility(frame);
        }
    }

}
