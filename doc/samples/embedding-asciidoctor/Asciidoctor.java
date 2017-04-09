/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;

public class Asciidoctor {

    public static void main(String[] args) throws Exception {
        final PolyglotEngine polyglotEngine = PolyglotEngine
                .newBuilder()
                .globalSymbol("file", args[0])
                .build();

        polyglotEngine.eval(Source
                .newBuilder("require 'asciidoctor'")
                .name("setup")
                .mimeType("application/x-ruby")
                .build());

        polyglotEngine.eval(Source
                .newBuilder("Asciidoctor.convert_file Truffle::Interop.from_java_string(Truffle::Interop.import('file'))")
                .name("convert")
                .mimeType("application/x-ruby")
                .build());
    }

}
