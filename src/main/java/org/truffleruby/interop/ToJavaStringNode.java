/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.control.RaiseException;

@GenerateUncached
@NodeChild(value = "valueNode", type = RubyNode.class)
public abstract class ToJavaStringNode extends RubySourceNode {

    public static ToJavaStringNode create() {
        return ToJavaStringNodeGen.create(null);
    }

    public static ToJavaStringNode create(RubyNode string) {
        return ToJavaStringNodeGen.create(string);
    }

    public abstract String executeToJavaString(Object name);

    abstract RubyNode getValueNode();

    @Specialization(guards = "interopLibrary.isString(value)", limit = "getLimit()")
    protected String interopString(Object value,
            @CachedLibrary("value") InteropLibrary interopLibrary,
            @Cached TranslateInteropExceptionNode translateInteropException) {
        try {
            return interopLibrary.asString(value);
        } catch (UnsupportedMessageException e) {
            throw translateInteropException.execute(e);
        }
    }

    @Specialization(guards = "!interopLibrary.isString(value)", limit = "getLimit()")
    protected String notInteropString(Object value,
            @CachedLibrary("value") InteropLibrary interopLibrary) {
        throw new RaiseException(
                getContext(),
                coreExceptions()
                        .typeError("This interop message requires a String or Symbol for the member name", this));
    }

    protected int getLimit() {
        return getLanguage().options.INTEROP_CONVERT_CACHE;
    }

    @Override
    public RubyNode cloneUninitialized() {
        return ToJavaStringNodeGen.create(getValueNode().cloneUninitialized()).copyFlags(this);
    }

}
