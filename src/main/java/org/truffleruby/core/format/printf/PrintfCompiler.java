/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.printf;

import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.format.FormatEncoding;
import org.truffleruby.core.format.FormatRootNode;

import com.oracle.truffle.api.RootCallTarget;
import org.truffleruby.core.string.StringSupport;

public class PrintfCompiler {

    private final RubyLanguage language;
    private final Node currentNode;

    public PrintfCompiler(RubyLanguage language, Node currentNode) {
        this.language = language;
        this.currentNode = currentNode;
    }

    @TruffleBoundary
    public RootCallTarget compile(AbstractTruffleString tstring, RubyEncoding encoding, Object[] arguments,
            boolean isDebug) {
        var byteArray = tstring.getInternalByteArrayUncached(encoding.tencoding);

        final PrintfSimpleParser parser = new PrintfSimpleParser(StringSupport.bytesToChars(byteArray), arguments,
                isDebug);
        final List<SprintfConfig> configs = parser.parse();
        final PrintfSimpleTreeBuilder builder = new PrintfSimpleTreeBuilder(language, configs);

        return new FormatRootNode(
                language,
                currentNode.getEncapsulatingSourceSection(),
                FormatEncoding.find(encoding, currentNode),
                builder.getNode()).getCallTarget();
    }

}
