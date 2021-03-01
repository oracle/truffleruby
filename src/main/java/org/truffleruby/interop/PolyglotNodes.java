/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.io.IOException;

import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
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
import com.oracle.truffle.api.source.Source;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.utils.Utils;

@CoreModule("Polyglot")
public abstract class PolyglotNodes {

    @CoreMethod(names = "eval", onSingleton = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = {
                        "idLib.isRubyString(langId)",
                        "codeLib.isRubyString(code)",
                        "idEqualNode.execute(idLib.getRope(langId), cachedLangId)",
                        "codeEqualNode.execute(codeLib.getRope(code), cachedCode)" },
                limit = "getCacheLimit()")
        protected Object evalCached(Object langId, Object code,
                @CachedLibrary(limit = "2") RubyStringLibrary idLib,
                @CachedLibrary(limit = "2") RubyStringLibrary codeLib,
                @Cached("idLib.getRope(langId)") Rope cachedLangId,
                @Cached("codeLib.getRope(code)") Rope cachedCode,
                @Cached("create(parse(idLib.getRope(langId), codeLib.getRope(code)))") DirectCallNode callNode,
                @Cached RopeNodes.EqualNode idEqualNode,
                @Cached RopeNodes.EqualNode codeEqualNode) {
            return callNode.call(EMPTY_ARGUMENTS);
        }

        @Specialization(
                guards = { "stringsId.isRubyString(langId)", "stringsSource.isRubyString(code)" },
                replaces = "evalCached")
        protected Object evalUncached(Object langId, Object code,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsId,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsSource,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(stringsId.getRope(langId), stringsSource.getRope(code)), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(Rope id, Rope code) {
            return parse(getContext(), id, code, this);
        }

        @TruffleBoundary
        public static CallTarget parse(RubyContext context, Rope id, Rope code, Node currentNode) {
            final String idString = RopeOperations.decodeRope(id);
            final String codeString = RopeOperations.decodeRope(code);
            final Source source = Source.newBuilder(idString, codeString, "(eval)").build();
            try {
                return context.getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().argumentError(e.getMessage(), currentNode));
            }
        }

        protected int getCacheLimit() {
            return getLanguage().options.EVAL_CACHE;
        }

    }

    @CoreMethod(
            names = "eval_file",
            onSingleton = true,
            required = 1,
            optional = 1,
            argumentNames = { "file_name_or_id", "file_name" })
    public abstract static class EvalFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(fileName)")
        protected Object evalFile(Object fileName, NotProvided id,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            final Source source;
            //intern() to improve footprint
            final String path = strings.getJavaString(fileName).intern();
            try {
                final TruffleFile file = getContext().getEnv().getPublicTruffleFile(path);
                String language = Source.findLanguage(file);
                if (language == null) {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().argumentError(
                                    "Could not find language of file " + path,
                                    this));
                }
                source = Source.newBuilder(language, file).build();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }

            return eval(source);
        }

        @TruffleBoundary
        @Specialization(
                guards = {
                        "stringsId.isRubyString(id)",
                        "stringsFileName.isRubyString(fileName)" })
        protected Object evalFile(Object id, Object fileName,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsId,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsFileName) {
            final String idString = stringsId.getJavaString(id);
            final Source source = getSource(idString, stringsFileName.getJavaString(fileName));
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

        private Source getSource(String language, String fileName) {
            //intern() to improve footprint
            final String path = fileName.intern();
            try {
                final TruffleFile file = getContext().getEnv().getPublicTruffleFile(path);
                return Source.newBuilder(language, file).build();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }
        }

    }

    @Primitive(name = "inner_context_new")
    public abstract static class InnerContextNewNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyInnerContext newInnerContext(RubyClass rubyClass) {
            final TruffleContext innerContext = getContext()
                    .getEnv()
                    .newContextBuilder()
                    .initializeCreatorContext(false)
                    .build();

            return new RubyInnerContext(
                    rubyClass,
                    getLanguage().innerContextShape,
                    innerContext);
        }
    }

    @Primitive(name = "inner_context_eval")
    public abstract static class InnerContextEvalNode extends CoreMethodArrayArgumentsNode {
        @Specialization(guards = {
                "idLib.isRubyString(langId)",
                "codeLib.isRubyString(code)",
                "idEqualNode.execute(langIdRope, cachedLangId)",
                "codeEqualNode.execute(codeRope, cachedCode)" }, limit = "getCacheLimit()")
        protected Object evalCached(RubyInnerContext rubyInnerContext, Object langId, Object code,
                @CachedLibrary(limit = "2") RubyStringLibrary idLib,
                @CachedLibrary(limit = "2") RubyStringLibrary codeLib,
                @Bind("idLib.getRope(langId)") Rope langIdRope,
                @Bind("codeLib.getRope(code)") Rope codeRope,
                @Cached("langIdRope") Rope cachedLangId,
                @Cached("codeRope") Rope cachedCode,
                @Cached("createSource(idLib.getJavaString(langId), codeLib.getJavaString(code))") Source cachedSource,
                @Cached RopeNodes.EqualNode idEqualNode,
                @Cached RopeNodes.EqualNode codeEqualNode,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile errorProfile) {
            return eval(rubyInnerContext, cachedSource, foreignToRubyNode, errorProfile);
        }

        @Specialization(
                guards = { "idLib.isRubyString(langId)", "codeLib.isRubyString(code)" },
                replaces = "evalCached")
        protected Object evalUncached(RubyInnerContext rubyInnerContext, Object langId, Object code,
                @CachedLibrary(limit = "2") RubyStringLibrary idLib,
                @CachedLibrary(limit = "2") RubyStringLibrary codeLib,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile errorProfile) {
            final String idString = idLib.getJavaString(langId);
            final String codeString = codeLib.getJavaString(code);

            final Source source = createSource(idString, codeString);

            return eval(rubyInnerContext, source, foreignToRubyNode, errorProfile);
        }

        private Object eval(RubyInnerContext rubyInnerContext, Source source,
                ForeignToRubyNode foreignToRubyNode, BranchProfile errorProfile) {
            final Object result;
            try {
                result = rubyInnerContext.innerContext.evalPublic(this, source);
            } catch (IllegalStateException closed) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("This Polyglot::InnerContext is closed", this));
            } catch (IllegalArgumentException unknownLanguage) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(Utils.concat("Unknown language: ", source.getLanguage()), this));
            }

            return foreignToRubyNode.executeConvert(result);
        }

        @TruffleBoundary
        protected Source createSource(String idString, String codeString) {
            return Source.newBuilder(idString, codeString, "(eval)").build();
        }

        protected int getCacheLimit() {
            return getLanguage().options.EVAL_CACHE;
        }
    }

    @Primitive(name = "inner_context_close")
    public abstract static class InnerContextCloseNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object close(RubyInnerContext rubyInnerContext) {
            rubyInnerContext.innerContext.close();
            return nil;
        }
    }

}
