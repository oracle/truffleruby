/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.digest;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyDigestMessages;
import org.truffleruby.language.RubyDynamicObject;

import java.security.MessageDigest;

public class RubyDigest extends RubyDynamicObject {


    final DigestAlgorithm algorithm;
    final MessageDigest digest;

    public RubyDigest(Shape shape, DigestAlgorithm algorithm, MessageDigest digest) {
        super(shape);
        this.algorithm = algorithm;
        this.digest = digest;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyDigestMessages.class;
    }

}
