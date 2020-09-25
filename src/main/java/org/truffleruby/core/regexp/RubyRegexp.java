/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import com.oracle.truffle.api.object.Shape;

import org.joni.Regex;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.RubyDynamicObject;

public class RubyRegexp extends RubyDynamicObject {

    public Regex regex;
    public Rope source;
    public RegexpOptions options;
    public EncodingCache cachedEncodings;

    public RubyRegexp(
            RubyClass rubyClass,
            Shape shape,
            Regex regex,
            Rope source,
            RegexpOptions options,
            EncodingCache encodingCache) {
        super(rubyClass, shape);
        this.regex = regex;
        this.source = source;
        this.options = options;
        this.cachedEncodings = encodingCache;
    }

}
