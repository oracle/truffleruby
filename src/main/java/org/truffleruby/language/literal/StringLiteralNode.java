/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
/*
 * Copyright (c) 2013, 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.literal;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class StringLiteralNode extends RubyContextSourceNode {

    @Child AllocateHelperNode allocateNode = AllocateHelperNode.create();

    private final Rope rope;
    private final RubyLanguage language;

    public StringLiteralNode(RubyLanguage language, Rope rope) {
        this.language = language;
        this.rope = rope;
    }

    @Override
    public RubyString execute(VirtualFrame frame) {
        final RubyString string = new RubyString(
                coreLibrary().stringClass,
                RubyLanguage.stringShape,
                false,
                false,
                rope);
        allocateNode.trace(string, this, language);
        return string;
    }

}
