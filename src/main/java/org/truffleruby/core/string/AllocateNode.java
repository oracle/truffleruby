/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.AllocationTracing;

import static org.truffleruby.core.string.TStringConstants.EMPTY_BINARY;

@GenerateUncached
public abstract class AllocateNode extends RubyBaseNode {

    public abstract RubyString execute(RubyClass rubyClass);

    @Specialization
    protected RubyString allocate(RubyClass rubyClass) {
        final RubyString string = new RubyString(
                rubyClass,
                getLanguage().stringShape,
                false,
                EMPTY_BINARY,
                Encodings.BINARY);
        AllocationTracing.trace(string, this);
        return string;
    }
}
