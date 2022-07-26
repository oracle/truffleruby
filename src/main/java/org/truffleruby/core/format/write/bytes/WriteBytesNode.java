/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.write.bytes;

import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.format.FormatNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.library.RubyStringLibrary;

@NodeChild("value")
public abstract class WriteBytesNode extends FormatNode {

    @Specialization
    protected Object write(VirtualFrame frame, byte[] bytes) {
        writeBytes(frame, bytes);
        return null;
    }

    @Specialization(guards = "libString.isRubyString(string)", limit = "1")
    protected Object writeString(VirtualFrame frame, Object string,
            @Cached RubyStringLibrary libString,
            @Cached TruffleString.GetInternalByteArrayNode getInternalByteArrayNode) {
        var tstring = libString.getTString(string);
        var byteArray = getInternalByteArrayNode.execute(tstring, libString.getTEncoding(string));
        writeBytes(frame, byteArray.getArray(), byteArray.getOffset(), byteArray.getLength());
        return null;
    }

}
