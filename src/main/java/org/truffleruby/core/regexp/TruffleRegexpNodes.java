/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.regexp;

import static org.truffleruby.language.RubyGuards.isRubyRegexp;

import org.joni.Regex;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.regexp.RegexpNodes.ToSNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringNodes.StringAppendPrimitiveNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;

@CoreClass("Truffle::RegexpOperations")
public class TruffleRegexpNodes {

    @CoreMethod(names = "union", onSingleton = true, required = 2, rest = true)
    public static abstract class RegexpUnionNode extends CoreMethodArrayArgumentsNode {

        @Child StringAppendPrimitiveNode appendNode = StringAppendPrimitiveNode.create();
        @Child ToSNode toSNode = ToSNode.create();
        @Child CallDispatchHeadNode copyNode = CallDispatchHeadNode.create();

        @Specialization(guards = "argsMatch(cachedArgs, args)", limit = "getCacheLimit()")
        public Object executeFastUnion(VirtualFrame frame, DynamicObject str, DynamicObject sep, Object[] args,
                @Cached(value = "args", dimensions = 1) Object[] cachedArgs,
                @Cached("buildUnion(frame, str, sep, args)") DynamicObject union) {
            return copyNode.call(frame, union, "clone");
        }

        @Specialization(replaces = "executeFastUnion")
        public Object executeSlowUnion(VirtualFrame frame, DynamicObject str, DynamicObject sep, Object[] args) {
            return buildUnion(frame, str, sep, args);
        }

        public DynamicObject buildUnion(VirtualFrame frame, DynamicObject str, DynamicObject sep, Object[] strs) {
            DynamicObject regexpString = null;
            for (int i = 0; i < strs.length; i++) {
                if (regexpString == null) {
                    regexpString = appendNode.executeStringAppend(str, string(frame, strs[i]));
                } else {
                    regexpString = appendNode.executeStringAppend(regexpString, sep);
                    regexpString = appendNode.executeStringAppend(regexpString, string(frame, strs[i]));
                }
            }
            return createRegexp(StringOperations.rope(regexpString));
        }

        public DynamicObject string(VirtualFrame frame, Object obj) {
            if (obj instanceof Rope) {
                final Rope rope = (Rope) obj;
                final boolean isAsciiOnly = rope.getEncoding().isAsciiCompatible() && rope.getCodeRange() == CodeRange.CR_7BIT;
                return createString(ClassicRegexp.quote19(rope, isAsciiOnly));
            } else {
                return toSNode.execute((DynamicObject) obj);
            }
        }

        @ExplodeLoop
        protected boolean argsMatch(Object[] cachedStrs, Object[] strs) {
            if (cachedStrs.length != strs.length) {
                return false;
            } else {
                for (int i = 0; i < cachedStrs.length; i++) {
                    Object a = cachedStrs[i];
                    Object b = strs[i];
                    if (a == b) {
                        continue;
                    }
                    if (isRubyRegexp(a) && isRubyRegexp(b) &&
                            Layouts.REGEXP.getRegex((DynamicObject) a) == Layouts.REGEXP.getRegex((DynamicObject) b)) {
                        continue;
                    }
                    return false;
                }
                return true;
            }
        }

        @TruffleBoundary
        public DynamicObject createRegexp(Rope pattern) {
            final RegexpOptions regexpOptions = RegexpOptions.fromEmbeddedOptions(0);
            final Regex regex = RegexpNodes.compile(this, getContext(), pattern, regexpOptions);

            final DynamicObjectFactory factory = getContext().getCoreLibrary().getRegexpFactory();
            return Layouts.REGEXP.createRegexp(factory, regex, (Rope) regex.getUserObject(), regexpOptions, new EncodingCache());
        }

        protected int getCacheLimit() {
            return getContext().getOptions().DEFAULT_CACHE;
        }
    }

}
