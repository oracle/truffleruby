/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForeignCodeNode extends RubyNode {

    private final DynamicObject mimeType;
    private final DynamicObject code;
    private final DynamicObject name;

    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
    @Child private CallDispatchHeadNode interopEvalNode = CallDispatchHeadNode.createOnSelf();
    @Child private CallDispatchHeadNode importMethodNode = CallDispatchHeadNode.createOnSelf();

    private static final Pattern NAME_PATTERN = Pattern.compile(".*function\\s+(\\w+)\\s*\\(.*", Pattern.DOTALL);

    public ForeignCodeNode(RubyContext context, String mimeType, String code) {
        final Matcher matcher = NAME_PATTERN.matcher(code);
        matcher.find();
        final String functionName = matcher.group(1);
        this.mimeType = makeStringNode.executeMake(mimeType, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        this.code = makeStringNode.executeMake(code + "\nInterop.export('" + functionName + "', " + functionName +  ".bind(this));", UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        this.name = context.getSymbolTable().getSymbol(functionName);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        interopEvalNode.call(frame, coreLibrary().getTruffleInteropModule(), "eval", mimeType, code);
        importMethodNode.call(frame, coreLibrary().getTruffleInteropModule(), "import_method", name);
        return nil();
    }

}
