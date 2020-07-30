/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.jcodings.transcode.EConv;
import org.truffleruby.interop.messages.RubyEncodingConverterMessages;
import org.truffleruby.language.RubyDynamicObject;

public class RubyEncodingConverter extends RubyDynamicObject {

    EConv econv;

    public RubyEncodingConverter(Shape shape, EConv econv) {
        super(shape);
        this.econv = econv;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyEncodingConverterMessages.class;
    }

}
