/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.joni.Regex;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.dispatch.DispatchNode;

@ExportLibrary(InteropLibrary.class)
public class RubyRegexp extends ImmutableRubyObject implements TruffleObject {

    public Regex regex;
    public Rope source;
    public RubyEncoding encoding;
    public RegexpOptions options;
    public EncodingCache cachedEncodings;
    public TRegexCache tregexCache;

    public RubyRegexp(
            Regex regex,
            Rope source,
            RubyEncoding encoding,
            RegexpOptions options,
            EncodingCache encodingCache,
            TRegexCache tregexCache) {
        assert source.encoding == encoding.jcoding;
        this.regex = regex;
        this.source = source;
        this.encoding = encoding;
        this.options = options;
        this.cachedEncodings = encodingCache;
        this.tregexCache = tregexCache;
    }

    // region InteropLibrary messages
    @ExportMessage
    protected Object toDisplayString(boolean allowSideEffects,
            @Cached DispatchNode dispatchNode,
            @Cached KernelNodes.ToSNode kernelToSNode) {
        if (allowSideEffects) {
            return dispatchNode.call(this, "inspect");
        } else {
            return kernelToSNode.executeToS(this);
        }
    }

    @ExportMessage
    protected boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    protected RubyClass getMetaObject(
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return context.getCoreLibrary().regexpClass;
    }
    // endregion

}
