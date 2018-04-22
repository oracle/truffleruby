/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.debug;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.impl.DefaultCallTarget;
import com.oracle.truffle.api.nodes.RootNode;

@TruffleLanguage.Registration(
        name = "RubySingleThreadedTestLanguage",
        id = "ruby-single-threaded-test",
        mimeType = "application/x-ruby-single-threaded-test",
        version = "0",
        internal = true,
        interactive = false)
public class RubySingleThreadedTestLanguage extends TruffleLanguage<Object> {

    @Override
    protected Object createContext(Env env) {
        return new Object();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return false;
    }

    @Override
    protected CallTarget parse(ParsingRequest request) {
        final String source = request.getSource().getCharacters().toString();

        return Truffle.getRuntime().createCallTarget(new RootNode(this) {

            @Override
            public Object execute(VirtualFrame frame) {
                return Integer.parseInt(source);
            }

        });
    }

}
