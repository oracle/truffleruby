/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;

import com.oracle.truffle.api.object.DynamicObject;

public interface LookupConstantInterface {

    public abstract RubyConstant lookupConstant(LexicalScope lexicalScope, DynamicObject module, String name);

}
