/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.io.IOException;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

@CoreModule("Polyglot")
public abstract class PolyglotNodes {

    @CoreMethod(names = "eval", isModuleFunction = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = {
                        "isRubyString(id)",
                        "isRubyString(source)",
                        "idEqualNode.execute(rope(id), cachedMimeType)",
                        "sourceEqualNode.execute(rope(source), cachedSource)" },
                limit = "getCacheLimit()")
        protected Object evalCached(
                DynamicObject id,
                DynamicObject source,
                @Cached("privatizeRope(id)") Rope cachedMimeType,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("create(parse(id, source))") DirectCallNode callNode,
                @Cached RopeNodes.EqualNode idEqualNode,
                @Cached RopeNodes.EqualNode sourceEqualNode) {
            return callNode.call(RubyNode.EMPTY_ARGUMENTS);
        }

        @Specialization(guards = { "isRubyString(id)", "isRubyString(source)" }, replaces = "evalCached")
        protected Object evalUncached(DynamicObject id, DynamicObject source,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(id, source), RubyNode.EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(DynamicObject id, DynamicObject code) {
            final String idString = StringOperations.getString(id);
            final String codeString = StringOperations.getString(code);
            final Source source = Source.newBuilder(idString, codeString, "(eval)").build();
            try {
                return getContext().getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        protected int getCacheLimit() {
            return getContext().getOptions().EVAL_CACHE;
        }

    }

    @CoreMethod(
            names = "eval_file",
            isModuleFunction = true,
            required = 1,
            optional = 1,
            argumentNames = { "file_name_or_id", "file_name" })
    public abstract static class EvalFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(fileName)")
        protected Object evalFile(DynamicObject fileName, NotProvided id) {
            final Source source;
            final String path = StringOperations.getString(fileName).intern();
            try {
                final TruffleFile file = getContext().getEnv().getPublicTruffleFile(path);
                String language = Source.findLanguage(file);
                if (language == null) {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentError("Could not find language of file " + fileName, this));
                }
                source = Source.newBuilder(language, file).build();
            } catch (IOException e) {
                throw new JavaException(e);
            }

            return eval(source);
        }

        @TruffleBoundary
        @Specialization(guards = { "isRubyString(id)", "isRubyString(fileName)" })
        protected Object evalFile(DynamicObject id, DynamicObject fileName) {
            final String idString = StringOperations.getString(id);
            final Source source = getSource(idString, fileName);
            return eval(source);
        }

        private Object eval(Source source) {
            final CallTarget callTarget;
            try {
                callTarget = getContext().getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
            return callTarget.call();
        }

        private Source getSource(String language, DynamicObject fileName) {
            final String path = StringOperations.getString(fileName).intern();
            try {
                final TruffleFile file = getContext().getEnv().getPublicTruffleFile(path);
                return Source.newBuilder(language, file).build();
            } catch (IOException e) {
                throw new JavaException(e);
            }
        }

    }

}
