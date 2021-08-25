/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import org.joni.Regex;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.regexp.InterpolatedRegexpNodeFactory.RegexpBuilderNodeGen;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeWithEncoding;
import org.truffleruby.language.NotOptimizedWarningNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

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

    public InterpolatedRegexpNode(ToSNode[] children, RegexpOptions options) {
        this.children = children;
        builderNode = RegexpBuilderNode.create(options);
        rubyStringLibrary = RubyStringLibrary.getFactory().createDispatched(2);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return builderNode.execute(executeChildren(frame));
    }

    @ExplodeLoop
    protected RopeWithEncoding[] executeChildren(VirtualFrame frame) {
        RopeWithEncoding[] values = new RopeWithEncoding[children.length];
        for (int i = 0; i < children.length; i++) {
            final Object value = children[i].execute(frame);
            values[i] = new RopeWithEncoding(rubyStringLibrary.getRope(value), rubyStringLibrary.getEncoding(value));
        }
        return values;
    }

    public abstract static class RegexpBuilderNode extends RubyBaseNode {

        @Child private RopeNodes.EqualNode ropesEqualNode = RopeNodes.EqualNode.create();
        @Child private DispatchNode copyNode = DispatchNode.create();
        private final RegexpOptions options;

        public static RegexpBuilderNode create(RegexpOptions options) {
            return RegexpBuilderNodeGen.create(options);
        }

        public RegexpBuilderNode(RegexpOptions options) {
            this.options = options;
        }

        public abstract Object execute(RopeWithEncoding[] parts);

        @Specialization(guards = "ropesWithEncodingsMatch(cachedParts, parts)", limit = "getDefaultCacheLimit()")
        protected Object executeFast(RopeWithEncoding[] parts,
                @Cached(value = "parts", dimensions = 1) RopeWithEncoding[] cachedParts,
                @Cached("createRegexp(cachedParts)") RubyRegexp regexp) {
            final Object clone = copyNode.call(regexp, "clone");
            return clone;
        }

        @Specialization(replaces = "executeFast")
        protected Object executeSlow(RopeWithEncoding[] parts,
                @Cached NotOptimizedWarningNode notOptimizedWarningNode) {
            notOptimizedWarningNode.warn("unstable interpolated regexps are not optimized");
            return createRegexp(parts);
        }

        @ExplodeLoop
        protected boolean ropesWithEncodingsMatch(RopeWithEncoding[] a, RopeWithEncoding[] b) {
            for (int i = 0; i < a.length; i++) {
                if (!ropesEqualNode.execute(a[i].getRope(), b[i].getRope())) {
                    return false;
                }
                if (a[i].getEncoding() != b[i].getEncoding()) {
                    return false;
                }
            }
            return true;
        }

        @TruffleBoundary
        protected RubyRegexp createRegexp(RopeWithEncoding[] strings) {
            final RopeWithEncoding preprocessed;
            final Regex regex;
            try {
                preprocessed = ClassicRegexp.preprocessDRegexp(getContext(), strings, options);
                RegexpCacheKey key = RegexpCacheKey.calculate(preprocessed, options);
                RubyRegexp regexp = getLanguage().getRegexp(key);
                RegexpOptions optionsArray[] = new RegexpOptions[]{ options };
                if (regexp == null) {
                    regex = TruffleRegexpNodes
                            .compile(
                                    getLanguage(),
                                    null,
                                    preprocessed,
                                    optionsArray,
                                    false,
                                    this);
                    regexp = new RubyRegexp(regex, optionsArray[0]);
                    getLanguage().addRegexp(key, regexp);
                }
                return regexp;
            } catch (DeferredRaiseException dre) {
                throw dre.getException(getContext());
            }
        }
    }
}
