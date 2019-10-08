/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser.parser;

import org.jcodings.Encoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodesFactory;

public class ParserRopeOperations {

    public Rope withEncoding(Rope rope, Encoding encoding) {
        return RopeNodesFactory.WithEncodingNodeGen.getUncached().executeWithEncoding(rope, encoding);
    }

    public Rope makeShared(Rope rope, int sharedStart, int sharedLength) {
        return RopeNodesFactory.SubstringNodeGen.getUncached().executeSubstring(rope, sharedStart, sharedLength);
    }

}
