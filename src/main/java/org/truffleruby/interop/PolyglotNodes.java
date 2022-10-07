/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.utils.Utils;

@CoreModule("Polyglot")
public abstract class PolyglotNodes {

    @CoreMethod(names = "eval", onSingleton = true, required = 2)
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = {
                        "idLib.isRubyString(langId)",
                        "codeLib.isRubyString(code)",
                        "idEqualNode.execute(idLib, langId, cachedLangId, cachedLangIdEnc)",
                        "codeEqualNode.execute(codeLib, code, cachedCode, cachedCodeEnc)" },
                limit = "getCacheLimit()")
        protected Object evalCached(Object langId, Object code,
                @Cached RubyStringLibrary idLib,
                @Cached RubyStringLibrary codeLib,
                @Cached("asTruffleStringUncached(langId)") TruffleString cachedLangId,
                @Cached("idLib.getEncoding(langId)") RubyEncoding cachedLangIdEnc,
                @Cached("asTruffleStringUncached(code)") TruffleString cachedCode,
                @Cached("codeLib.getEncoding(code)") RubyEncoding cachedCodeEnc,
                @Cached("create(parse(getJavaString(langId), getJavaString(code)))") DirectCallNode callNode,
                @Cached StringHelperNodes.EqualNode idEqualNode,
                @Cached StringHelperNodes.EqualNode codeEqualNode) {
            return callNode.call(EMPTY_ARGUMENTS);
        }

        @Specialization(
                guards = { "stringsId.isRubyString(langId)", "stringsSource.isRubyString(code)" },
                replaces = "evalCached", limit = "1")
        protected Object evalUncached(Object langId, Object code,
                @Cached RubyStringLibrary stringsId,
                @Cached RubyStringLibrary stringsSource,
                @Cached ToJavaStringNode toJavaStringLandNode,
                @Cached ToJavaStringNode toJavaStringCodeNode,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(toJavaStringLandNode.executeToJavaString(langId),
                    toJavaStringCodeNode.executeToJavaString(code)), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(String langId, String code) {
            final Source source = Source.newBuilder(langId, code, "(eval)").build();
            try {
                return getContext().getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(e.getMessage(), this));
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
        @Specialization(guards = "strings.isRubyString(fileName)", limit = "1")
        protected Object evalFile(Object fileName, NotProvided id,
                @Cached RubyStringLibrary strings) {
            final Source source;
            // intern() to improve footprint
            final String path = RubyGuards.getJavaString(fileName).intern();
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
        @Specialization(guards = { "stringsId.isRubyString(id)", "stringsFileName.isRubyString(fileName)" },
                limit = "1")
        protected Object evalFile(Object id, Object fileName,
                @Cached RubyStringLibrary stringsId,
                @Cached RubyStringLibrary stringsFileName) {
            final String idString = RubyGuards.getJavaString(id);
            final Source source = getSource(idString, RubyGuards.getJavaString(fileName));
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

        @TruffleBoundary
        @Specialization
        protected RubyInnerContext newInnerContext(
                RubyClass rubyClass,
                RubyArray languages,
                RubyArray languageOptions,
                boolean inheritAllAccess,
                Object codeSharing,
                RubyProc onCancelledCallback) {
            String[] permittedLanguages = new String[languages.size];
            int i = 0;
            for (Object language : ArrayOperations.toIterable(languages)) {
                permittedLanguages[i++] = RubyGuards.getJavaString(language);
            }

            var builder = getContext().getEnv().newInnerContextBuilder(permittedLanguages);

            var iterator = ArrayOperations.toIterable(languageOptions).iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                Object value = iterator.next();
                builder.option(RubyGuards.getJavaString(key), RubyGuards.getJavaString(value));
            }

            Boolean codeSharingBoolean = codeSharing == nil ? null : (boolean) codeSharing;

            final TruffleContext innerContext = builder
                    .initializeCreatorContext(false)
                    .inheritAllAccess(inheritAllAccess)
                    .forceSharing(codeSharingBoolean)
                    .onCancelled(() -> CallBlockNode.getUncached().yield(onCancelledCallback))
                    .build();

            return new RubyInnerContext(
                    rubyClass,
                    getLanguage().innerContextShape,
                    innerContext);
        }

    }

    @Primitive(name = "inner_context_eval")
    public abstract static class InnerContextEvalNode extends PrimitiveArrayArgumentsNode {
        @Specialization(guards = {
                "idLib.isRubyString(langId)",
                "codeLib.isRubyString(code)",
                "filenameLib.isRubyString(filename)",
                "idEqualNode.execute(idLib, langId, cachedLangId, cachedLangIdEnc)",
                "codeEqualNode.execute(codeLib, code, cachedCode, cachedCodeEnc)",
                "filenameEqualNode.execute(filenameLib, filename, cachedFilename, cachedFilenameEnc)" },
                limit = "getCacheLimit()")
        protected Object evalCached(RubyInnerContext rubyInnerContext, Object langId, Object code, Object filename,
                @Cached RubyStringLibrary idLib,
                @Cached RubyStringLibrary codeLib,
                @Cached RubyStringLibrary filenameLib,
                @Cached("asTruffleStringUncached(langId)") TruffleString cachedLangId,
                @Cached("idLib.getEncoding(langId)") RubyEncoding cachedLangIdEnc,
                @Cached("asTruffleStringUncached(code)") TruffleString cachedCode,
                @Cached("codeLib.getEncoding(code)") RubyEncoding cachedCodeEnc,
                @Cached("asTruffleStringUncached(filename)") TruffleString cachedFilename,
                @Cached("filenameLib.getEncoding(filename)") RubyEncoding cachedFilenameEnc,
                @Cached("createSource(getJavaString(langId), getJavaString(code), getJavaString(filename))") Source cachedSource,
                @Cached StringHelperNodes.EqualNode idEqualNode,
                @Cached StringHelperNodes.EqualNode codeEqualNode,
                @Cached StringHelperNodes.EqualNode filenameEqualNode,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile errorProfile) {
            return eval(rubyInnerContext, cachedSource, foreignToRubyNode, errorProfile);
        }

        @Specialization(
                guards = { "idLib.isRubyString(langId)", "codeLib.isRubyString(code)" },
                replaces = "evalCached", limit = "1")
        protected Object evalUncached(RubyInnerContext rubyInnerContext, Object langId, Object code, Object filename,
                @Cached RubyStringLibrary idLib,
                @Cached RubyStringLibrary codeLib,
                @Cached RubyStringLibrary filenameLib,
                @Cached ToJavaStringNode toJavaStringIDNode,
                @Cached ToJavaStringNode toJavaStringCodeNode,
                @Cached ToJavaStringNode toJavaStringFileNode,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile errorProfile) {
            final String idString = toJavaStringIDNode.executeToJavaString(langId);
            final String codeString = toJavaStringCodeNode.executeToJavaString(code);
            final String filenameString = toJavaStringFileNode.executeToJavaString(filename);

            final Source source = createSource(idString, codeString, filenameString);

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
            } catch (ThreadDeath closed) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Polyglot::InnerContext was terminated forcefully", this));
            } catch (IllegalArgumentException unknownLanguage) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(Utils.concat("Unknown language: ", source.getLanguage()), this));
            }
            return foreignToRubyNode.executeConvert(result);
        }

        @TruffleBoundary
        protected Source createSource(String idString, String codeString, String filename) {
            return Source.newBuilder(idString, codeString, filename).build();
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

    @Primitive(name = "inner_context_close_force")
    public abstract static class InnerContextCloseExitedNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object closeExited(RubyInnerContext rubyInnerContext) {
            rubyInnerContext.innerContext.closeCancelled(this, "force terminate");
            return nil;
        }
    }

}
