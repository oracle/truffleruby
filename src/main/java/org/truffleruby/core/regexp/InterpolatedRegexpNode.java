/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.AsTruffleStringNode;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.regexp.InterpolatedRegexpNodeFactory.RegexpBuilderNodeGen;
import org.truffleruby.core.rope.TStringWithEncoding;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.control.DeferredRaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.language.library.RubyStringLibrary;

public class InterpolatedRegexpNode extends RubyContextSourceNode {

    @Children private final ToSNode[] children;
    @Child private RegexpBuilderNode builderNode;
    @Child private RubyStringLibrary rubyStringLibrary;
    @Child private AsTruffleStringNode asTruffleStringNode = AsTruffleStringNode.create();

    public InterpolatedRegexpNode(ToSNode[] children, RegexpOptions options) {
        this.children = children;
        builderNode = RegexpBuilderNode.create(options);
        rubyStringLibrary = RubyStringLibrary.createDispatched();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return builderNode.execute(executeChildren(frame));
    }

    @ExplodeLoop
    protected TStringWithEncoding[] executeChildren(VirtualFrame frame) {
        TStringWithEncoding[] values = new TStringWithEncoding[children.length];
        for (int i = 0; i < children.length; i++) {
            final Object value = children[i].execute(frame);
            values[i] = new TStringWithEncoding(asTruffleStringNode,
                    rubyStringLibrary.getTString(value),
                    rubyStringLibrary.getEncoding(value));
        }
        return values;
    }

    public abstract static class RegexpBuilderNode extends RubyBaseNode {

        @Child private TruffleString.EqualNode equalNode = TruffleString.EqualNode.create();
        private final RegexpOptions options;

        public static RegexpBuilderNode create(RegexpOptions options) {
            return RegexpBuilderNodeGen.create(options);
        }

        public RegexpBuilderNode(RegexpOptions options) {
            this.options = options;
        }

        public abstract Object execute(TStringWithEncoding[] parts);

        @Specialization(guards = "ropesWithEncodingsMatch(cachedParts, parts)", limit = "getDefaultCacheLimit()")
        protected Object executeFast(TStringWithEncoding[] parts,
                @Cached(value = "parts", dimensions = 1) TStringWithEncoding[] cachedParts,
                @Cached("createRegexp(cachedParts)") RubyRegexp regexp) {
            return regexp;
        }

        @Specialization(replaces = "executeFast")
        protected Object executeSlow(TStringWithEncoding[] parts,
                @Cached NotOptimizedWarningNode notOptimizedWarningNode) {
            notOptimizedWarningNode.warn("unstable interpolated regexps are not optimized");
            return createRegexp(parts);
        }

        @ExplodeLoop
        protected boolean ropesWithEncodingsMatch(TStringWithEncoding[] a, TStringWithEncoding[] b) {
            for (int i = 0; i < a.length; i++) {
                var aEncoding = a[i].encoding;
                if (aEncoding != b[i].encoding) {
                    return false;
                }
                if (!equalNode.execute(a[i].tstring, b[i].tstring, aEncoding.tencoding)) {
                    return false;
                }
            }
            return true;
        }

        @TruffleBoundary
        protected RubyRegexp createRegexp(TStringWithEncoding[] strings) {
            try {
                var preprocessed = ClassicRegexp.preprocessDRegexp(getContext(), strings, options);
                return RubyRegexp.create(getLanguage(), preprocessed.tstring, preprocessed.encoding, options, this);
            } catch (DeferredRaiseException dre) {
                throw dre.getException(getContext());
            }
        }
    }
}
