/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.loader.FileLoader;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.source.Source;

/** Specs for these methods are in spec/truffle/interop/matrix_spec.rb and in spec/truffle/interop/methods_spec.rb */
@CoreModule("Truffle::Interop")
public abstract class InteropNodes {

    public static Object execute(Node node, Object receiver, Object[] args, InteropLibrary receivers,
            TranslateInteropExceptionNode translateInteropExceptionNode) {
        try {
            return receivers.execute(receiver, args);
        } catch (InteropException e) {
            throw translateInteropExceptionNode.execute(node, e);
        }
    }

    public static Object readMember(Node node, InteropLibrary interop, Object receiver, String name,
            TranslateInteropExceptionNode translateInteropException) {
        try {
            return interop.readMember(receiver, name);
        } catch (InteropException e) {
            throw translateInteropException.execute(node, e);
        }
    }

    // region Misc
    @Primitive(name = "interop_library_all_methods")
    public abstract static class AllMethodsOfInteropLibrary extends PrimitiveArrayArgumentsNode {

        private static final String[] METHODS = publicInteropLibraryMethods();

        @TruffleBoundary
        @Specialization
        protected RubyArray allMethodsOfInteropLibrary(
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            Object[] store = new Object[METHODS.length];
            for (int i = 0; i < METHODS.length; i++) {
                store[i] = createString(fromJavaStringNode, METHODS[i], Encodings.UTF_8);
            }
            return createArray(store);
        }

        private static String[] publicInteropLibraryMethods() {
            List<String> methods = new ArrayList<>();
            for (Method method : InteropLibrary.class.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers())) {
                    if (!methods.contains(method.getName())) {
                        methods.add(method.getName());
                    }
                }
            }
            return methods.toArray(StringUtils.EMPTY_STRING_ARRAY);
        }
    }

    @Primitive(name = "interop_null?")
    public abstract static class InteropIsNullNode extends PrimitiveArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isNull(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNull(receiver);
        }
    }

    @Primitive(name = "interop_execute")
    public abstract static class InteropExecuteNode extends PrimitiveArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object interopExecuteWithoutConversion(Object receiver, RubyArray argsArray,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);
            return InteropNodes.execute(node, receiver, args, receivers, translateInteropException);
        }
    }

    @Primitive(name = "dispatch_missing")
    public abstract static class DispatchMissingNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object dispatchMissing() {
            return DispatchNode.MISSING;
        }
    }

    @CoreMethod(names = "foreign?", onSingleton = true, required = 1)
    public abstract static class InteropIsForeignNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isForeign(Object value) {
            return RubyGuards.isForeignObject(value);
        }
    }

    @CoreMethod(names = "proxy_foreign_object", onSingleton = true, required = 1, optional = 1)
    public abstract static class ProxyForeignObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object proxyForeignObject(Object delegate, NotProvided logger) {
            return new ProxyForeignObject(delegate);
        }

        @TruffleBoundary
        @Specialization(guards = "wasProvided(logger)")
        protected Object proxyForeignObject(Object delegate, Object logger) {
            return new ProxyForeignObject(delegate, logger);
        }
    }
    // endregion

    // region eval
    @CoreMethod(names = "mime_type_supported?", onSingleton = true, required = 1)
    public abstract static class MimeTypeSupportedNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(mimeType)", limit = "1")
        protected boolean isMimeTypeSupported(RubyString mimeType,
                @Cached RubyStringLibrary strings) {
            return getContext().getEnv().isMimeTypeSupported(RubyGuards.getJavaString(mimeType));
        }

    }

    @CoreMethod(names = "import_file", onSingleton = true, required = 1)
    public abstract static class ImportFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(fileName)", limit = "1")
        protected Object importFile(Object fileName,
                @Cached RubyStringLibrary strings) {
            try {
                //intern() to improve footprint
                final TruffleFile file = getContext()
                        .getEnv()
                        .getPublicTruffleFile(RubyGuards.getJavaString(fileName).intern());
                final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, file).build();
                getContext().getEnv().parsePublic(source).call();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }

            return nil;
        }

    }

    @CoreMethod(names = "eval", onSingleton = true, required = 2)
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = {
                        "stringsMimeType.isRubyString(mimeType)",
                        "stringsSource.isRubyString(source)",
                        "mimeTypeEqualNode.execute(stringsMimeType, mimeType, cachedMimeType, cachedMimeTypeEnc)",
                        "sourceEqualNode.execute(stringsSource, source, cachedSource, cachedSourceEnc)" },
                limit = "getEvalCacheLimit()")
        protected Object evalCached(Object mimeType, Object source,
                @Shared @Cached RubyStringLibrary stringsMimeType,
                @Shared @Cached RubyStringLibrary stringsSource,
                @Cached("asTruffleStringUncached(mimeType)") TruffleString cachedMimeType,
                @Cached("stringsMimeType.getEncoding(mimeType)") RubyEncoding cachedMimeTypeEnc,
                @Cached("asTruffleStringUncached(source)") TruffleString cachedSource,
                @Cached("stringsSource.getEncoding(source)") RubyEncoding cachedSourceEnc,
                @Bind("this") Node node,
                @Cached("create(parse(node, getJavaString(mimeType), getJavaString(source)))") DirectCallNode callNode,
                @Cached StringHelperNodes.EqualNode mimeTypeEqualNode,
                @Cached StringHelperNodes.EqualNode sourceEqualNode) {
            return callNode.call(EMPTY_ARGUMENTS);
        }

        @Specialization(
                guards = { "stringsMimeType.isRubyString(mimeType)", "stringsSource.isRubyString(source)" },
                replaces = "evalCached", limit = "1")
        protected static Object evalUncached(Object mimeType, RubyString source,
                @Shared @Cached RubyStringLibrary stringsMimeType,
                @Shared @Cached RubyStringLibrary stringsSource,
                @Cached ToJavaStringNode toJavaStringMimeNode,
                @Cached ToJavaStringNode toJavaStringSourceNode,
                @Cached IndirectCallNode callNode,
                @Bind("this") Node node) {
            return callNode.call(parse(node, toJavaStringMimeNode.execute(node, mimeType),
                    toJavaStringSourceNode.execute(node, source)), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected static CallTarget parse(Node node, String mimeTypeString, String codeString) {
            String language = Source.findLanguage(mimeTypeString);
            if (language == null) {
                // Give the original string to get the nice exception from Truffle
                language = mimeTypeString;
            }
            final Source source = Source.newBuilder(language, codeString, "(eval)").build();
            try {
                return getContext(node).getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(node), coreExceptions(node).argumentError(e.getMessage(), node));
            }
        }

        protected int getEvalCacheLimit() {
            return getLanguage().options.EVAL_CACHE;
        }

    }

    @Primitive(name = "interop_eval_nfi")
    public abstract static class InteropEvalNFINode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "library.isRubyString(code)", limit = "1")
        protected Object evalNFI(Object code,
                @Cached RubyStringLibrary library,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(code), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(Object code) {
            final Source source = Source.newBuilder("nfi", RubyGuards.getJavaString(code), "(eval)").build();

            try {
                return getContext().getEnv().parseInternal(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

    }

    @CoreMethod(names = "polyglot_bindings_access?", onSingleton = true)
    public abstract static class IsPolyglotBindingsAccessAllowedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isPolyglotBindingsAccessAllowed() {
            return getContext().getEnv().isPolyglotBindingsAccessAllowed();
        }

    }
    // endregion

    // region Exception
    @CoreMethod(names = "exception?", onSingleton = true, required = 1)
    public abstract static class IsExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isException(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isException(receiver);
        }
    }

    @CoreMethod(names = "has_exception_cause?", onSingleton = true, required = 1)
    public abstract static class HasExceptionCauseNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasExceptionCause(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasExceptionCause(receiver);
        }
    }

    @CoreMethod(names = "exception_cause", onSingleton = true, required = 1)
    public abstract static class ExceptionCauseNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getExceptionCause(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.getExceptionCause(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "exception_exit_status", onSingleton = true, required = 1)
    public abstract static class ExceptionExitStatusSourceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static int getExceptionExitStatus(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.getExceptionExitStatus(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "exception_incomplete_source?", onSingleton = true, required = 1)
    public abstract static class IsExceptionIncompleteSourceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isExceptionIncompleteSource(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.isExceptionIncompleteSource(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "has_exception_message?", onSingleton = true, required = 1)
    public abstract static class HasExceptionMessageNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasExceptionMessage(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasExceptionMessage(receiver);
        }
    }

    @CoreMethod(names = "exception_message", onSingleton = true, required = 1)
    public abstract static class ExceptionMessageNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getExceptionMessage(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.getExceptionMessage(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "has_exception_stack_trace?", onSingleton = true, required = 1)
    public abstract static class HasExceptionStackTraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasExceptionStackTrace(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasExceptionStackTrace(receiver);
        }
    }

    @CoreMethod(names = "exception_stack_trace", onSingleton = true, required = 1)
    public abstract static class ExceptionStackTraceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getExceptionStackTrace(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.getExceptionStackTrace(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "exception_type", onSingleton = true, required = 1)
    public abstract static class ExceptionTypeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static RubySymbol getExceptionType(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                final ExceptionType exceptionType = receivers.getExceptionType(receiver);
                return getLanguage(node).getSymbol(exceptionType.name());
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "throw_exception", onSingleton = true, required = 1)
    public abstract static class ThrowExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object throwException(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                throw receivers.throwException(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    // endregion

    // region Executable
    @CoreMethod(names = "executable?", onSingleton = true, required = 1)
    public abstract static class IsExecutableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isExecutable(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isExecutable(receiver);
        }
    }


    @CoreMethod(names = "has_executable_name?", onSingleton = true, required = 1)
    public abstract static class HasExecutableNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasExecutableName(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasExecutableName(receiver);
        }
    }

    @CoreMethod(names = "executable_name", onSingleton = true, required = 1)
    public abstract static class ExecutableNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getExecutableName(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.getExecutableName(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "execute", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object interopExecute(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final Object foreign = InteropNodes.execute(node, receiver, args, receivers, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    @CoreMethod(names = "execute_without_conversion", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object interopExecuteWithoutConversion(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            return InteropNodes.execute(node, receiver, args, receivers, translateInteropException);
        }
    }
    // endregion

    // region Instantiable
    @CoreMethod(names = "instantiable?", onSingleton = true, required = 1)
    public abstract static class InstantiableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isInstantiable(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isInstantiable(receiver);
        }
    }

    @CoreMethod(names = "instantiate", onSingleton = true, required = 1, rest = true)
    public abstract static class InstantiateNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object newCached(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final Object foreign;
            try {
                foreign = receivers.instantiate(receiver, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }
    }
    // endregion

    // region Array elements
    @CoreMethod(names = "has_array_elements?", onSingleton = true, required = 1)
    public abstract static class HasArrayElementsNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasArrayElements(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasArrayElements(receiver);
        }

    }

    @CoreMethod(names = "array_size", onSingleton = true, required = 1)
    public abstract static class ArraySizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object arraySize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {

            try {
                return receivers.getArraySize(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "read_array_element", onSingleton = true, required = 2)
    public abstract static class ReadArrayElementNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final Object foreign;
            try {
                foreign = receivers.readArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    @CoreMethod(names = "write_array_element", onSingleton = true, required = 3)
    public abstract static class WriteArrayElementNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object write(Object receiver, long identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                receivers.writeArrayElement(receiver, identifier, value);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return value;
        }
    }

    @CoreMethod(names = "remove_array_element", onSingleton = true, required = 2)
    public abstract static class RemoveArrayElementNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Nil readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                receivers.removeArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return Nil.INSTANCE;
        }
    }

    @CoreMethod(names = "array_element_readable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementReadableNode extends CoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isArrayElementReadable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementReadable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_modifiable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementModifiableNode extends CoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isArrayElementModifiable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementModifiable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_insertable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementInsertableNode extends CoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isArrayElementInsertable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementInsertable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_removable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementRemovableNode extends CoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isArrayElementRemovable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementRemovable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_writable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementWritableNode extends CoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isArrayElementWritable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementWritable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_existing?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementExistingNode extends CoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isArrayElementExisting(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementExisting(receiver, index);
        }
    }
    // endregion

    // region SourceLocation
    @CoreMethod(names = "has_source_location?", onSingleton = true, required = 1)
    public abstract static class HasSourceLocationNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasSourceLocation(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasSourceLocation(receiver);
        }
    }

    @CoreMethod(names = "source_location", onSingleton = true, required = 1)
    public abstract static class GetSourceLocationNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static RubySourceLocation getSourceLocation(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final SourceSection sourceLocation;
            try {
                sourceLocation = receivers.getSourceLocation(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
            return new RubySourceLocation(
                    coreLibrary(node).sourceLocationClass,
                    getLanguage(node).sourceLocationShape,
                    sourceLocation);
        }
    }
    // endregion

    // region String
    @CoreMethod(names = "string?", onSingleton = true, required = 1)
    public abstract static class IsStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isString(receiver);
        }
    }

    @CoreMethod(names = "as_string", onSingleton = true, required = 1)
    public abstract static class AsStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static String asString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_truffle_string", onSingleton = true, required = 1)
    public abstract static class AsTruffleStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static TruffleString asTruffleString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asTruffleString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @Primitive(name = "foreign_string_to_ruby_string")
    public abstract static class ForeignStringToRubyStringNode extends PrimitiveArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static RubyString foreignStringToRubyString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached TruffleString.SwitchEncodingNode switchEncodingNode,
                @Bind("this") Node node) {
            final TruffleString truffleString;
            try {
                truffleString = receivers.asTruffleString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            var asUTF8 = switchEncodingNode.execute(truffleString, TruffleString.Encoding.UTF_8);
            var rubyString = createString(node, asUTF8, Encodings.UTF_8);
            rubyString.freeze();
            return rubyString;
        }
    }

    @CoreMethod(names = "to_display_string", onSingleton = true, required = 1)
    public abstract static class ToDisplayStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object toDisplayString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.toDisplayString(receiver, true);
        }
    }

    @CoreMethod(names = "to_string", onSingleton = true, required = 1)
    public abstract static class ToStringNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyString toString(Object value,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            return createString(fromJavaStringNode, String.valueOf(value), Encodings.UTF_8);
        }

    }
    // endregion

    // region Boolean
    @CoreMethod(names = "boolean?", onSingleton = true, required = 1)
    public abstract static class IsBooleanNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isBoolean(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isBoolean(receiver);
        }
    }

    @CoreMethod(names = "as_boolean", onSingleton = true, required = 1)
    public abstract static class AsBooleanNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean asBoolean(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            try {
                return receivers.asBoolean(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }
    // endregion

    // region DateTime
    @CoreMethod(names = "date?", onSingleton = true, required = 1)
    public abstract static class IsDateNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isDate(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isDate(receiver);
        }
    }

    @CoreMethod(names = "as_date", onSingleton = true, required = 1)
    public abstract static class AsDateNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object asDate(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return getContext(node).getEnv().asGuestValue(receivers.asDate(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "duration?", onSingleton = true, required = 1)
    public abstract static class IsDurationNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isDuration(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isDuration(receiver);
        }
    }

    @CoreMethod(names = "as_duration", onSingleton = true, required = 1)
    public abstract static class AsDurationNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object asDuration(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return getContext(node).getEnv().asGuestValue(receivers.asDuration(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "instant?", onSingleton = true, required = 1)
    public abstract static class IsInstantNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isInstant(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isInstant(receiver);
        }
    }

    @CoreMethod(names = "as_instant", onSingleton = true, required = 1)
    public abstract static class AsInstantNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object asInstant(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return getContext(node).getEnv().asGuestValue(receivers.asInstant(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "time?", onSingleton = true, required = 1)
    public abstract static class IsTimeNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isTime(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isTime(receiver);
        }
    }

    @CoreMethod(names = "as_time", onSingleton = true, required = 1)
    public abstract static class AsTimeNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object asTime(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return getContext(node).getEnv().asGuestValue(receivers.asTime(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "time_zone?", onSingleton = true, required = 1)
    public abstract static class IsTimeZoneNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isTimeZone(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isTimeZone(receiver);
        }
    }

    @CoreMethod(names = "as_time_zone", onSingleton = true, required = 1)
    public abstract static class AsTimeZoneNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object asTimeZone(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return getContext(node).getEnv().asGuestValue(receivers.asTimeZone(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }
    // endregion

    // region Number
    @CoreMethod(names = "number?", onSingleton = true, required = 1)
    public abstract static class IsNumberNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isNumber(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNumber(receiver);
        }
    }

    @CoreMethod(names = "fits_in_byte?", onSingleton = true, required = 1)
    public abstract static class FitsInByteNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInByte(receiver);
        }
    }

    @CoreMethod(names = "fits_in_short?", onSingleton = true, required = 1)
    public abstract static class FitsInShortNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInShort(receiver);
        }
    }

    @CoreMethod(names = "fits_in_int?", onSingleton = true, required = 1)
    public abstract static class FitsInIntNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInInt(receiver);
        }
    }

    @CoreMethod(names = "fits_in_long?", onSingleton = true, required = 1)
    public abstract static class FitsInLongNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInLong(receiver);
        }
    }

    @CoreMethod(names = "fits_in_big_integer?", onSingleton = true, required = 1)
    public abstract static class FitsInBigIntegerNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInBigInteger(receiver);
        }
    }

    @CoreMethod(names = "fits_in_float?", onSingleton = true, required = 1)
    public abstract static class FitsInFloatNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInFloat(receiver);
        }
    }

    @CoreMethod(names = "fits_in_double?", onSingleton = true, required = 1)
    public abstract static class FitsInDoubleNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean fits(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInDouble(receiver);
        }
    }

    @CoreMethod(names = "as_byte", onSingleton = true, required = 1)
    public abstract static class AsByteNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static int as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asByte(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_short", onSingleton = true, required = 1)
    public abstract static class AsShortNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static int as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asShort(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_int", onSingleton = true, required = 1)
    public abstract static class AsIntNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static int as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asInt(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_long", onSingleton = true, required = 1)
    public abstract static class AsLongNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static long as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asLong(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_big_integer", onSingleton = true, required = 1)
    public abstract static class AsBigIntegerNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached FixnumOrBignumNode fixnumOrBignumNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return fixnumOrBignumNode.execute(node, receivers.asBigInteger(receiver));
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_float", onSingleton = true, required = 1)
    public abstract static class AsFloatNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static double as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asFloat(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "as_double", onSingleton = true, required = 1)
    public abstract static class AsDoubleNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static double as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asDouble(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }
    // endregion

    // region Null
    @CoreMethod(names = "null?", onSingleton = true, required = 1)
    public abstract static class IsNullNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isNull(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNull(receiver);
        }
    }
    // endregion

    // region Pointer
    @CoreMethod(names = "pointer?", onSingleton = true, required = 1)
    public abstract static class PointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isPointer(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isPointer(receiver);
        }

    }

    @CoreMethod(names = "as_pointer", onSingleton = true, required = 1)
    public abstract static class AsPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static long asPointer(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.asPointer(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "to_native", onSingleton = true, required = 1)
    public abstract static class ToNativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected Nil toNative(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            receivers.toNative(receiver);
            return Nil.INSTANCE;
        }

    }
    // endregion

    // region Members
    @CoreMethod(names = "has_members?", onSingleton = true, required = 1)
    public abstract static class HasMembersNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasMembers(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMembers(receiver);
        }
    }

    @CoreMethod(names = "members", onSingleton = true, required = 1, optional = 1)
    public abstract static class GetMembersNode extends CoreMethodArrayArgumentsNode {

        protected abstract Object executeMembers(Object receiver, boolean internal);

        @Specialization
        protected Object members(Object receiver, NotProvided internal) {
            return executeMembers(receiver, false);
        }

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object members(Object receiver, boolean internal,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return receivers.getMembers(receiver, internal);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "read_member", onSingleton = true, required = 2)
    public abstract static class InteropReadMemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object readMember(Object receiver, Object identifier,
                @Cached ReadMemberNode readMemberNode) {
            return readMemberNode.execute(receiver, identifier);
        }
    }

    @GenerateUncached
    public abstract static class ReadMemberNode extends RubyBaseNode {

        public abstract Object execute(Object receiver, Object identifier);

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object readMember(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, identifier);
            final Object foreign = InteropNodes.readMember(node, receivers, receiver, name, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    @CoreMethod(names = "read_member_without_conversion", onSingleton = true, required = 2)
    public abstract static class ReadMemberWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object readMember(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached ToJavaStringNode toJavaStringNode,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, identifier);
            return InteropNodes.readMember(node, receivers, receiver, name, translateInteropException);
        }
    }

    @CoreMethod(names = "write_member", onSingleton = true, required = 3)
    public abstract static class WriteMemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object write(Object receiver, Object identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, identifier);
            try {
                receivers.writeMember(receiver, name, value);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return value;
        }
    }

    @CoreMethod(names = "write_member_without_conversion", onSingleton = true, required = 3)
    public abstract static class InteropWriteMemberWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object write(Object receiver, Object identifier, Object value,
                @Cached WriteMemberWithoutConversionNode writeMemberWithoutConversionNode) {
            return writeMemberWithoutConversionNode.execute(receiver, identifier, value);
        }
    }

    @GenerateUncached
    public abstract static class WriteMemberWithoutConversionNode extends RubyBaseNode {

        public abstract Object execute(Object receiver, Object identifier, Object value);

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object write(Object receiver, Object identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, identifier);
            try {
                receivers.writeMember(receiver, name, value);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return value;
        }
    }

    @CoreMethod(names = "remove_member", onSingleton = true, required = 2)
    public abstract static class RemoveMemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbolOrString(identifier)", limit = "getInteropCacheLimit()")
        protected static Nil remove(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, identifier);
            try {
                receivers.removeMember(receiver, name);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }

            return Nil.INSTANCE;
        }
    }

    @CoreMethod(names = "invoke_member", onSingleton = true, required = 2, rest = true)
    public abstract static class InteropInvokeMemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object invokeMember(Object receiver, Object identifier, Object[] args,
                @Cached InvokeMemberNode invokeMemberNode) {
            return invokeMemberNode.execute(this, receiver, identifier, args);
        }
    }

    @GenerateUncached
    @GenerateInline
    @GenerateCached(false)
    public abstract static class InvokeMemberNode extends RubyBaseNode {

        private static Object invoke(Node node, InteropLibrary receivers, Object receiver, String member, Object[] args,
                TranslateInteropExceptionNode translateInteropExceptionNode) {
            try {
                return receivers.invokeMember(receiver, member, args);
            } catch (InteropException e) {
                throw translateInteropExceptionNode.executeInInvokeMember(node, e, receiver, args);
            }
        }

        public abstract Object execute(Node node, Object receiver, Object identifier, Object[] args);

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object invokeCached(Node node, Object receiver, Object identifier, Object[] args,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.execute(node, identifier);
            final Object foreign = invoke(node, receivers, receiver, name, args, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }
    }


    @CoreMethod(names = "member_readable?", onSingleton = true, required = 2)
    public abstract static class IsMemberReadableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberReadable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberReadable(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_modifiable?", onSingleton = true, required = 2)
    public abstract static class IsMemberModifiableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberModifiable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberModifiable(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_insertable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInsertableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberInsertable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberInsertable(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_removable?", onSingleton = true, required = 2)
    public abstract static class IsMemberRemovableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberRemovable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberRemovable(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_invocable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInvocableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberInvocable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberInvocable(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_internal?", onSingleton = true, required = 2)
    public abstract static class IsMemberInternalNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberInternal(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberInternal(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_writable?", onSingleton = true, required = 2)
    public abstract static class IsMemberWritableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberWritable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberWritable(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "member_existing?", onSingleton = true, required = 2)
    public abstract static class IsMemberExistingNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMemberExisting(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.isMemberExisting(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "has_member_read_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberReadSideEffectsNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean hasMemberReadSideEffects(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.hasMemberReadSideEffects(receiver, toJavaStringNode.execute(node, name));
        }
    }

    @CoreMethod(names = "has_member_write_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberWriteSideEffectsNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean hasMemberWriteSideEffects(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Bind("this") Node node) {
            return receivers.hasMemberWriteSideEffects(receiver, toJavaStringNode.execute(node, name));
        }
    }
    // endregion

    // region Import/Export
    @CoreMethod(names = "export", onSingleton = true, required = 2)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "object", type = RubyNode.class)
    public abstract static class ExportNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected Object export(Object name, Object object,
                @Cached ToJavaStringNode toJavaStringNode) {
            final var nameAsString = toJavaStringNode.execute(this, name);
            getContext().getEnv().exportSymbol(nameAsString, object);
            return object;
        }
    }

    @CoreMethod(names = "import", onSingleton = true, required = 1)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ImportNode extends CoreMethodNode {

        @Specialization
        protected Object importObject(Object name,
                @Cached InlinedBranchProfile errorProfile,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached ToJavaStringNode toJavaStringNode) {
            final var nameAsString = toJavaStringNode.execute(this, name);
            final Object value = doImport(nameAsString);
            if (value != null) {
                return foreignToRubyNode.executeConvert(value);
            } else {
                errorProfile.enter(this);
                throw new RaiseException(getContext(), coreExceptions().nameErrorImportNotFound(nameAsString, this));
            }
        }

        @TruffleBoundary
        private Object doImport(String name) {
            return getContext().getEnv().importSymbol(name);
        }

    }
    // endregion

    // region Language
    @CoreMethod(names = "has_language?", onSingleton = true, required = 1)
    public abstract static class HasLanguageNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasLanguage(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasLanguage(receiver);
        }
    }

    @CoreMethod(names = "language", onSingleton = true, required = 1)
    public abstract static class GetLanguageNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getLanguage(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached FromJavaStringNode fromJavaStringNode,
                @Bind("this") Node node) {
            if (!receivers.hasLanguage(receiver)) {
                return nil;
            }

            final Class<? extends TruffleLanguage<?>> language;
            try {
                language = receivers.getLanguage(receiver);
            } catch (UnsupportedMessageException e) {
                return nil;
            }

            final String name = languageClassToLanguageName(language);
            return fromJavaStringNode.executeFromJavaString(node, name);
        }

        @TruffleBoundary
        private static String languageClassToLanguageName(Class<? extends TruffleLanguage<?>> language) {
            String name = language.getSimpleName();
            if (name.endsWith("Language")) {
                name = name.substring(0, name.length() - "Language".length());
            }
            if (name.equals("Host")) {
                name = "Java";
            }
            return name;
        }
    }

    @CoreMethod(names = "languages", onSingleton = true, required = 0)
    public abstract static class LanguagesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray languages() {
            final Map<String, LanguageInfo> languages = getContext().getEnv().getPublicLanguages();
            final String[] languagesArray = languages.keySet().toArray(StringUtils.EMPTY_STRING_ARRAY);
            final Object[] rubyStringArray = new Object[languagesArray.length];
            for (int i = 0; i < languagesArray.length; i++) {
                rubyStringArray[i] = StringOperations.createUTF8String(getContext(), getLanguage(), languagesArray[i]);
            }
            return createArray(rubyStringArray);
        }

    }

    @CoreMethod(names = "other_languages?", onSingleton = true, required = 0)
    public abstract static class HasOtherLanguagesNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean hasOtherlanguages() {
            return getContext().hasOtherPublicLanguages();
        }

    }
    // endregion

    // region Java
    @CoreMethod(names = "java?", onSingleton = true, required = 1)
    public abstract static class InteropIsJavaNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isJava(Object value) {
            return getContext().getEnv().isHostObject(value);
        }
    }

    @CoreMethod(names = "java_class?", onSingleton = true, required = 1)
    public abstract static class InteropIsJavaClassNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isJavaClass(Object value) {
            return getContext().getEnv().isHostObject(value) &&
                    getContext().getEnv().asHostObject(value) instanceof Class;
        }
    }

    @CoreMethod(names = "java_string?", onSingleton = true, required = 1)
    public abstract static class InteropIsJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isJavaString(Object value) {
            return value instanceof String;
        }
    }

    @CoreMethod(names = "java_instanceof?", onSingleton = true, required = 2)
    public abstract static class InteropJavaInstanceOfNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isJavaObject(object)", "isJavaClassOrInterface(boxedJavaClass)" })
        protected boolean javaInstanceOfJava(Object object, Object boxedJavaClass) {
            final Object hostInstance = getContext().getEnv().asHostObject(object);
            if (hostInstance == null) {
                return false;
            } else {
                final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
                return javaClass.isAssignableFrom(hostInstance.getClass());
            }
        }

        @Specialization(guards = { "!isJavaObject(object)", "isJavaClassOrInterface(boxedJavaClass)" })
        protected boolean javaInstanceOfNotJava(Object object, Object boxedJavaClass) {
            final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
            return javaClass.isInstance(object);
        }

        protected boolean isJavaObject(Object object) {
            return getContext().getEnv().isHostObject(object);
        }

        protected boolean isJavaClassOrInterface(Object object) {
            return getContext().getEnv().isHostObject(object) &&
                    getContext().getEnv().asHostObject(object) instanceof Class<?>;
        }

    }

    @Primitive(name = "to_java_string")
    public abstract static class ToJavaStringPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected String toJavaString(Object value,
                @Cached ToJavaStringNode toJavaStringNode) {
            return toJavaStringNode.execute(this, value);
        }
    }

    @Primitive(name = "interop_to_java_array")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "stores.accepts(array.getStore())")
        protected Object toJavaArray(RubyArray array,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return getContext().getEnv().asGuestValue(stores.toJavaArrayCopy(
                    array.getStore(),
                    array.size));
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected Object coerce(Object array) {
            return FAILURE;
        }

    }

    @Primitive(name = "interop_to_java_list")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaListNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "stores.accepts(array.getStore())")
        protected Object toJavaList(RubyArray array,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            int size = array.size;
            Object[] copy = stores.boxedCopyOfRange(array.getStore(), 0, size);
            return getContext().getEnv().asGuestValue(ArrayUtils.asList(copy));
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected Object coerce(Object array) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "java_type", onSingleton = true, required = 1)
    public abstract static class JavaTypeNode extends CoreMethodArrayArgumentsNode {

        // TODO CS 17-Mar-18 we should cache this in the future

        @Specialization
        protected Object javaType(Object name,
                @Cached ToJavaStringNode toJavaStringNode) {
            return lookupJavaType(toJavaStringNode.execute(this, name));
        }

        @TruffleBoundary
        private Object lookupJavaType(String name) {
            final TruffleLanguage.Env env = getContext().getEnv();

            if (!env.isHostLookupAllowed()) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().securityError("host access is not allowed", this));
            }

            return env.lookupHostSymbol(name);
        }

    }

    @Primitive(name = "java_add_to_classpath")
    public abstract static class JavaAddToClasspathNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(path)", limit = "1")
        protected boolean javaAddToClasspath(Object path,
                @Cached RubyStringLibrary strings) {
            TruffleLanguage.Env env = getContext().getEnv();
            try {
                TruffleFile file = FileLoader.getSafeTruffleFile(getLanguage(), getContext(),
                        RubyGuards.getJavaString(path));
                env.addToHostClassPath(file);
                return true;
            } catch (SecurityException e) {
                throw new RaiseException(getContext(),
                        coreExceptions().securityError("unable to add to classpath", this), e);
            }
        }

    }
    // endregion

    // region MetaObject
    @CoreMethod(names = "meta_object?", onSingleton = true, required = 1)
    public abstract static class IsMetaObjectNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMetaObject(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isMetaObject(receiver);
        }
    }

    @CoreMethod(names = "has_meta_object?", onSingleton = true, required = 1)
    public abstract static class HasMetaObjectNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasMetaObject(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasMetaObject(receiver);
        }
    }

    @CoreMethod(names = "meta_object", onSingleton = true, required = 1)
    public abstract static class InteropMetaObjectNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object metaObject(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached InlinedBranchProfile errorProfile,
                @Cached LogicalClassNode logicalClassNode,
                @Bind("this") Node node) {
            if (interop.hasMetaObject(value)) {
                try {
                    return interop.getMetaObject(value);
                } catch (UnsupportedMessageException e) {
                    errorProfile.enter(node);
                    return logicalClassNode.execute(value);
                }
            } else {
                return logicalClassNode.execute(value);
            }
        }
    }

    @CoreMethod(names = "has_declaring_meta_object?", onSingleton = true, required = 1)
    public abstract static class HasDeclaringMetaObjectNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasDeclaringMetaObject(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasDeclaringMetaObject(receiver);
        }
    }

    @CoreMethod(names = "declaring_meta_object", onSingleton = true, required = 1)
    public abstract static class DeclaringMetaObjectNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object declaringMetaObject(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getDeclaringMetaObject(value);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "meta_instance?", onSingleton = true, required = 2)
    public abstract static class IsMetaInstanceNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isMetaInstance(Object metaObject, Object instance,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.isMetaInstance(metaObject, instance);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "meta_simple_name", onSingleton = true, required = 1)
    public abstract static class GetMetaSimpleNameNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getMetaSimpleName(Object metaObject,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getMetaSimpleName(metaObject);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "meta_qualified_name", onSingleton = true, required = 1)
    public abstract static class GetMetaQualifiedNameNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getMetaQualifiedName(Object metaObject,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getMetaQualifiedName(metaObject);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "has_meta_parents?", onSingleton = true, required = 1)
    public abstract static class HasMetaParentsNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasMetaParents(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasMetaParents(receiver);
        }
    }

    @CoreMethod(names = "meta_parents", onSingleton = true, required = 1)
    public abstract static class GetMetaParentsNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getMetaParents(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getMetaParents(value);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }
    // endregion

    // region Hash entries
    @CoreMethod(names = "has_hash_entries?", onSingleton = true, required = 1)
    public abstract static class HasHashEntriesNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasHashEntriesNode(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasHashEntries(receiver);
        }
    }


    @CoreMethod(names = "hash_entries_iterator", onSingleton = true, required = 1)
    public abstract static class HashEntriesIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object hashEntriesIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getHashEntriesIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "hash_entry_existing?", onSingleton = true, required = 2)
    public abstract static class HashEntryExistingNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hashEntryExisting(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isHashEntryExisting(receiver, key);
        }
    }

    @CoreMethod(names = "hash_entry_insertable?", onSingleton = true, required = 2)
    public abstract static class HashEntryInsertableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hashEntryInsertable(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isHashEntryInsertable(receiver, key);
        }
    }

    @CoreMethod(names = "hash_entry_modifiable?", onSingleton = true, required = 2)
    public abstract static class HashEntryModifiableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hashEntryModifiable(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isHashEntryModifiable(receiver, key);
        }
    }

    @CoreMethod(names = "hash_entry_readable?", onSingleton = true, required = 2)
    public abstract static class HashEntryReadableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hashEntryReadable(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isHashEntryReadable(receiver, key);
        }
    }

    @CoreMethod(names = "hash_entry_removable?", onSingleton = true, required = 2)
    public abstract static class HashEntryRemovableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hashEntryRemovable(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isHashEntryRemovable(receiver, key);
        }
    }


    @CoreMethod(names = "hash_entry_writable?", onSingleton = true, required = 2)
    public abstract static class HashEntryWritableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hashEntryWritable(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isHashEntryWritable(receiver, key);
        }
    }

    @CoreMethod(names = "hash_keys_iterator", onSingleton = true, required = 1)
    public abstract static class HashKeysIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object hashKeysIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getHashKeysIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "hash_size", onSingleton = true, required = 1)
    public abstract static class HashSizeNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static long hashSize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getHashSize(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "hash_values_iterator", onSingleton = true, required = 1)
    public abstract static class HashValuesIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object hashValuesIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getHashValuesIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }


    @CoreMethod(names = "read_hash_value", onSingleton = true, required = 2)
    public abstract static class ReadHashValueNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object readHashValue(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.readHashValue(receiver, key);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "read_hash_value_or_default", onSingleton = true, required = 3)
    public abstract static class ReadHashValueOrDefaultNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object readHashValueOrDefault(Object receiver, Object key, Object defaultValue,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.readHashValueOrDefault(receiver, key, defaultValue);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "remove_hash_entry", onSingleton = true, required = 2)
    public abstract static class RemoveHashEntryNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object removeHashEntry(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                interop.removeHashEntry(receiver, key);
                return nil;
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "write_hash_entry", onSingleton = true, required = 3)
    public abstract static class WriteHashEntryNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object writeHashEntry(Object receiver, Object key, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                interop.writeHashEntry(receiver, key, value);
                return value;
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }
    // endregion

    // region Identity
    @CoreMethod(names = "identical?", onSingleton = true, required = 2)
    public abstract static class IsIdenticalNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isIdentical(Object receiver, Object other,
                @CachedLibrary("receiver") InteropLibrary lhsInterop,
                @CachedLibrary("other") InteropLibrary rhsInterop) {
            return lhsInterop.isIdentical(receiver, other, rhsInterop);
        }
    }

    @CoreMethod(names = "has_identity?", onSingleton = true, required = 1)
    public abstract static class HasIdentityNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasIdentity(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasIdentity(receiver);
        }
    }

    @CoreMethod(names = "identity_hash_code", onSingleton = true, required = 1)
    public abstract static class InteropIdentityHashCodeNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static int identityHashCode(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            if (interop.hasIdentity(value)) {
                try {
                    return interop.identityHashCode(value);
                } catch (UnsupportedMessageException e) {
                    throw translateInteropException.execute(node, e);
                }
            } else {
                return System.identityHashCode(value);
            }
        }
    }
    // endregion

    // region Scope
    @CoreMethod(names = "scope?", onSingleton = true, required = 1)
    public abstract static class IsScopeNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isScope(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isScope(receiver);
        }
    }

    @CoreMethod(names = "has_scope_parent?", onSingleton = true, required = 1)
    public abstract static class HasScopeParentNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasScopeParent(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasScopeParent(receiver);
        }
    }

    @CoreMethod(names = "scope_parent", onSingleton = true, required = 1)
    public abstract static class GetScopeParentNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getScope(Object scope,
                @CachedLibrary("scope") InteropLibrary interopLibrary,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            if (interopLibrary.hasScopeParent(scope)) {
                try {
                    return interopLibrary.getScopeParent(scope);
                } catch (UnsupportedMessageException e) {
                    throw translateInteropException.execute(node, e);
                }
            } else {
                return nil;
            }
        }
    }

    @Primitive(name = "current_scope")
    public abstract static class GetCurrentScopeNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object getScope(VirtualFrame frame,
                @CachedLibrary(limit = "1") NodeLibrary nodeLibrary,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return nodeLibrary.getScope(this, frame, true);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @Primitive(name = "top_scope")
    public abstract static class GetTopScopeNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object getTopScope() {
            return getContext().getTopScopeObject();
        }
    }
    // endregion

    // region Buffer Messages
    @CoreMethod(names = "has_buffer_elements?", onSingleton = true, required = 1)
    public abstract static class HasBufferElementsNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasBufferElements(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasBufferElements(receiver);
        }

    }

    @CoreMethod(names = "buffer_writable?", onSingleton = true, required = 1)
    public abstract static class IsBufferWritableNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean isBufferWritable(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.isBufferWritable(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "buffer_size", onSingleton = true, required = 1)
    public abstract static class GetBufferSizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static long getBufferSize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getBufferSize(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "read_buffer_byte", onSingleton = true, required = 2)
    public abstract static class ReadBufferByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static byte readBufferByte(Object receiver, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.readBufferByte(receiver, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_byte", onSingleton = true, required = 3)
    public abstract static class WriteBufferByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInByte(value)")
        protected static Object writeBufferByte(Object receiver, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final byte byteValue = interopValue.asByte(value);
                interop.writeBufferByte(receiver, byteOffset, byteValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_short", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferShortNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static short readBufferShort(Object receiver, Object byteOrderObject, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var byteOrder = symbolToByteOrderNode.execute(node, byteOrderObject);
                return interop.readBufferShort(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_short", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferShortNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInShort(value)")
        protected static Object writeBufferShort(Object receiver, Object byteOrderObject, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var byteOrder = symbolToByteOrderNode.execute(node, byteOrderObject);
                final short shortValue = interopValue.asShort(value);
                interop.writeBufferShort(receiver, byteOrder, byteOffset, shortValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_int", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferIntNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static int readBufferInt(Object receiver, Object byteOrderObject, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var byteOrder = symbolToByteOrderNode.execute(node, byteOrderObject);
                return interop.readBufferInt(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_int", onSingleton = true, required = 4, lowerFixnum = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferIntNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInInt(value)")
        protected static Object writeBufferInt(Object receiver, Object orderObject, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var order = symbolToByteOrderNode.execute(node, orderObject);
                final int intValue = interopValue.asInt(value);
                interop.writeBufferInt(receiver, order, byteOffset, intValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_long", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferLongNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static long readBufferLong(Object receiver, Object byteOrderObject, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var byteOrder = symbolToByteOrderNode.execute(node, byteOrderObject);
                return interop.readBufferLong(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_long", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferLongNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInLong(value)")
        protected static Object writeBufferLong(Object receiver, Object orderObject, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var order = symbolToByteOrderNode.execute(node, orderObject);
                final long longValue = interopValue.asLong(value);
                interop.writeBufferLong(receiver, order, byteOffset, longValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_float", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferFloatNode extends CoreMethodNode {

        // must return double so Ruby nodes can deal with it
        @Specialization(limit = "getInteropCacheLimit()")
        protected static double readBufferFloat(Object receiver, Object byteOrderObject, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var byteOrder = symbolToByteOrderNode.execute(node, byteOrderObject);
                return interop.readBufferFloat(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_float", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferFloatNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInDouble(value)")
        protected static Object writeBufferFloat(Object receiver, Object orderObject, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var order = symbolToByteOrderNode.execute(node, orderObject);
                final float floatValue = (float) interopValue.asDouble(value);
                interop.writeBufferFloat(receiver, order, byteOffset, floatValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_double", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferDoubleNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected static double readBufferDouble(Object receiver, Object byteOrderObject, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var byteOrder = symbolToByteOrderNode.execute(node, byteOrderObject);
                return interop.readBufferDouble(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_double", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferDoubleNode extends CoreMethodNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInDouble(value)")
        protected static Object writeBufferDouble(Object receiver, Object orderObject, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached SymbolToByteOrderNode symbolToByteOrderNode,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                final var order = symbolToByteOrderNode.execute(node, orderObject);
                final double doubleValue = interopValue.asDouble(value);
                interop.writeBufferDouble(receiver, order, byteOffset, doubleValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
            return value;
        }

    }
    // endregion

    // region Iterator
    @CoreMethod(names = "has_iterator?", onSingleton = true, required = 1)
    public abstract static class HasIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.hasIterator(receiver);
        }
    }

    @CoreMethod(names = "iterator?", onSingleton = true, required = 1)
    public abstract static class IsIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop) {
            return interop.isIterator(receiver);
        }
    }

    @CoreMethod(names = "iterator", onSingleton = true, required = 1)
    public abstract static class GetIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "has_iterator_next_element?", onSingleton = true, required = 1)
    public abstract static class HasIteratorNextElementNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static boolean hasIteratorNextElement(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.hasIteratorNextElement(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }

    @CoreMethod(names = "iterator_next_element", onSingleton = true, required = 1)
    public abstract static class GetIteratorNextElementNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected static Object getIteratorNextElement(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Bind("this") Node node) {
            try {
                return interop.getIteratorNextElement(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(node, e);
            }
        }
    }
    // endregion

}
