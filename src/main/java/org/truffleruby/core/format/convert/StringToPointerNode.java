/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.cext.CExtNodes;
import org.truffleruby.core.format.FormatNode;

@NodeChildren({
        @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class StringToPointerNode extends FormatNode {

    @Specialization(guards = "isRubyString(string)")
    public long toPointer(DynamicObject string,
                          @Cached("create()") CExtNodes.StringToNativeNode stringToNativeNode) {
        return stringToNativeNode.executeToNative(string).getNativePointer().getAddress();
    }

}
