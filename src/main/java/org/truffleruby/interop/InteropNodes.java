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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ExceptionType;
import com.oracle.truffle.api.interop.NodeLibrary;
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
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
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
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
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
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.Source;

/** Specs for these methods are in spec/truffle/interop/matrix_spec.rb and in spec/truffle/interop/methods_spec.rb */
@CoreModule("Truffle::Interop")
public abstract class InteropNodes {

    public static Object execute(Object receiver, Object[] args, InteropLibrary receivers,
            TranslateInteropExceptionNode translateInteropExceptionNode) {
        try {
            return receivers.execute(receiver, args);
        } catch (InteropException e) {
            throw translateInteropExceptionNode.execute(e);
        }
    }

    public static Object invoke(InteropLibrary receivers, Object receiver, String member, Object[] args,
            TranslateInteropExceptionNode translateInteropExceptionNode) {
        try {
            return receivers.invokeMember(receiver, member, args);
        } catch (InteropException e) {
            throw translateInteropExceptionNode.executeInInvokeMember(e, receiver, args);
        }
    }

    public static Object readMember(InteropLibrary interop, Object receiver, String name,
            TranslateInteropExceptionNode translateInteropException) {
        try {
            return interop.readMember(receiver, name);
        } catch (InteropException e) {
            throw translateInteropException.execute(e);
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

    @Primitive(name = "interop_execute")
    public abstract static class InteropExecuteNode extends PrimitiveArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object executeWithoutConversion(Object receiver, RubyArray argsArray,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);
            return InteropNodes.execute(receiver, args, receivers, translateInteropException);
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
                @Cached RubyStringLibrary stringsMimeType,
                @Cached RubyStringLibrary stringsSource,
                @Cached("asTruffleStringUncached(mimeType)") TruffleString cachedMimeType,
                @Cached("stringsMimeType.getEncoding(mimeType)") RubyEncoding cachedMimeTypeEnc,
                @Cached("asTruffleStringUncached(source)") TruffleString cachedSource,
                @Cached("stringsSource.getEncoding(source)") RubyEncoding cachedSourceEnc,
                @Cached("create(parse(getJavaString(mimeType), getJavaString(source)))") DirectCallNode callNode,
                @Cached StringHelperNodes.EqualNode mimeTypeEqualNode,
                @Cached StringHelperNodes.EqualNode sourceEqualNode) {
            return callNode.call(EMPTY_ARGUMENTS);
        }

        @Specialization(
                guards = { "stringsMimeType.isRubyString(mimeType)", "stringsSource.isRubyString(source)" },
                replaces = "evalCached", limit = "1")
        protected Object evalUncached(Object mimeType, RubyString source,
                @Cached RubyStringLibrary stringsMimeType,
                @Cached RubyStringLibrary stringsSource,
                @Cached ToJavaStringNode toJavaStringMimeNode,
                @Cached ToJavaStringNode toJavaStringSourceNode,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(toJavaStringMimeNode.executeToJavaString(mimeType),
                    toJavaStringSourceNode.executeToJavaString(source)), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(String mimeTypeString, String codeString) {
            String language = Source.findLanguage(mimeTypeString);
            if (language == null) {
                // Give the original string to get the nice exception from Truffle
                language = mimeTypeString;
            }
            final Source source = Source.newBuilder(language, codeString, "(eval)").build();
            try {
                return getContext().getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
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
        protected Object getExceptionCause(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.getExceptionCause(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "exception_exit_status", onSingleton = true, required = 1)
    public abstract static class ExceptionExitStatusSourceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected int getExceptionExitStatus(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.getExceptionExitStatus(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "exception_incomplete_source?", onSingleton = true, required = 1)
    public abstract static class IsExceptionIncompleteSourceNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isExceptionIncompleteSource(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.isExceptionIncompleteSource(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object getExceptionMessage(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.getExceptionMessage(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object getExceptionStackTrace(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.getExceptionStackTrace(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "exception_type", onSingleton = true, required = 1)
    public abstract static class ExceptionTypeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected RubySymbol getExceptionType(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                final ExceptionType exceptionType = receivers.getExceptionType(receiver);
                return getLanguage().getSymbol(exceptionType.name());
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "throw_exception", onSingleton = true, required = 1)
    public abstract static class ThrowExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object throwException(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                throw receivers.throwException(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object getExecutableName(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.getExecutableName(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "execute", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object executeForeignCached(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object foreign = InteropNodes.execute(receiver, args, receivers, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    @CoreMethod(names = "execute_without_conversion", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object executeWithoutConversionForeignCached(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            return InteropNodes.execute(receiver, args, receivers, translateInteropException);
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
        protected Object newCached(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object foreign;
            try {
                foreign = receivers.instantiate(receiver, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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
        protected Object arraySize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {

            try {
                return receivers.getArraySize(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "read_array_element", onSingleton = true, required = 2)
    public abstract static class ReadArrayElementNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object foreign;
            try {
                foreign = receivers.readArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "write_array_element", onSingleton = true, required = 3)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class WriteArrayElementNode extends RubySourceNode {

        public static WriteArrayElementNode create() {
            return InteropNodesFactory.WriteArrayElementNodeFactory.create(null);
        }

        public static WriteArrayElementNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.WriteArrayElementNodeFactory.create(argumentNodes);
        }

        abstract Object execute(Object receiver, Object identifier, Object value);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object write(Object receiver, long identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                receivers.writeArrayElement(receiver, identifier, value);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return value;
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "remove_array_element", onSingleton = true, required = 2)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class RemoveArrayElementNode extends RubySourceNode {

        public static RemoveArrayElementNode create() {
            return InteropNodesFactory.RemoveArrayElementNodeFactory.create(null);
        }

        public static RemoveArrayElementNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.RemoveArrayElementNodeFactory.create(argumentNodes);
        }

        abstract Nil execute(Object receiver, Object identifier);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Nil readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                receivers.removeArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return Nil.INSTANCE;
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
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
        protected RubySourceLocation getSourceLocation(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final SourceSection sourceLocation;
            try {
                sourceLocation = receivers.getSourceLocation(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
            return new RubySourceLocation(
                    coreLibrary().sourceLocationClass,
                    getLanguage().sourceLocationShape,
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
        protected String asString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_truffle_string", onSingleton = true, required = 1)
    public abstract static class AsTruffleStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected TruffleString asTruffleString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asTruffleString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @Primitive(name = "foreign_string_to_ruby_string")
    public abstract static class ForeignStringToRubyStringNode extends PrimitiveArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected RubyString foreignStringToRubyString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached TruffleString.SwitchEncodingNode switchEncodingNode) {
            final TruffleString truffleString;
            try {
                truffleString = receivers.asTruffleString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            var asUTF8 = switchEncodingNode.execute(truffleString, TruffleString.Encoding.UTF_8);
            var rubyString = createString(asUTF8, Encodings.UTF_8);
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

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString toString(Object value) {
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
        protected boolean asBoolean(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {

            try {
                return receivers.asBoolean(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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
        protected Object asDate(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return getContext().getEnv().asGuestValue(receivers.asDate(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object asDuration(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return getContext().getEnv().asGuestValue(receivers.asDuration(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object asInstant(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return getContext().getEnv().asGuestValue(receivers.asInstant(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object asTime(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return getContext().getEnv().asGuestValue(receivers.asTime(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object asTimeZone(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return getContext().getEnv().asGuestValue(receivers.asTimeZone(receiver));
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected int as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asByte(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_short", onSingleton = true, required = 1)
    public abstract static class AsShortNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected int as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asShort(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_int", onSingleton = true, required = 1)
    public abstract static class AsIntNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected int as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asInt(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_long", onSingleton = true, required = 1)
    public abstract static class AsLongNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected long as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asLong(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_float", onSingleton = true, required = 1)
    public abstract static class AsFloatNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected double as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asFloat(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_double", onSingleton = true, required = 1)
    public abstract static class AsDoubleNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected double as(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asDouble(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }
    // endregion

    // region Null
    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "null?", onSingleton = true, required = 1)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class IsNullNode extends RubySourceNode {

        public static IsNullNode create() {
            return InteropNodesFactory.IsNullNodeFactory.create(null);
        }

        public static IsNullNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.IsNullNodeFactory.create(argumentNodes);
        }

        abstract Object execute(Object receiver);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isNull(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNull(receiver);
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
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
        protected long asPointer(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asPointer(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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
        protected Object members(Object receiver, boolean internal,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.getMembers(receiver, internal);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "read_member", onSingleton = true, required = 2)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class ReadMemberNode extends RubySourceNode {

        public static ReadMemberNode create() {
            return InteropNodesFactory.ReadMemberNodeFactory.create(null);
        }

        public static ReadMemberNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.ReadMemberNodeFactory.create(argumentNodes);
        }

        public abstract Object execute(Object receiver, Object identifier);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object readMember(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object foreign = InteropNodes.readMember(receivers, receiver, name, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "read_member_without_conversion", onSingleton = true, required = 2)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class ReadMemberWithoutConversionNode extends RubySourceNode {

        public static ReadMemberWithoutConversionNode create() {
            return InteropNodesFactory.ReadMemberWithoutConversionNodeFactory.create(null);
        }

        public static ReadMemberWithoutConversionNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.ReadMemberWithoutConversionNodeFactory.create(argumentNodes);
        }

        abstract Object execute(Object receiver, Object identifier);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object readMember(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached ToJavaStringNode toJavaStringNode) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            return InteropNodes.readMember(receivers, receiver, name, translateInteropException);
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "write_member", onSingleton = true, required = 3)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class WriteMemberNode extends RubySourceNode {

        public static WriteMemberNode create() {
            return InteropNodesFactory.WriteMemberNodeFactory.create(null);
        }

        public static WriteMemberNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.WriteMemberNodeFactory.create(argumentNodes);
        }

        public abstract Object execute(Object receiver, Object identifier, Object value);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object write(Object receiver, Object identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.writeMember(receiver, name, value);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return value;
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "write_member_without_conversion", onSingleton = true, required = 3)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class WriteMemberWithoutConversionNode extends RubySourceNode {

        public static WriteMemberWithoutConversionNode create() {
            return InteropNodesFactory.WriteMemberWithoutConversionNodeFactory.create(null);
        }

        public static WriteMemberWithoutConversionNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.WriteMemberWithoutConversionNodeFactory.create(argumentNodes);
        }

        public abstract Object execute(Object receiver, Object identifier, Object value);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object write(Object receiver, Object identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.writeMember(receiver, name, value);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return value;
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
        }

    }

    @CoreMethod(names = "remove_member", onSingleton = true, required = 2)
    public abstract static class RemoveMemberNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbolOrString(identifier)", limit = "getInteropCacheLimit()")
        protected Nil remove(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.removeMember(receiver, name);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return Nil.INSTANCE;
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "invoke_member", onSingleton = true, required = 2, rest = true)
    @NodeChild(value = "argumentNodes", type = RubyNode[].class)
    public abstract static class InvokeMemberNode extends RubySourceNode {

        public static InvokeMemberNode create() {
            return InteropNodesFactory.InvokeMemberNodeFactory.create(null);
        }

        public static InvokeMemberNode create(RubyNode[] argumentNodes) {
            return InteropNodesFactory.InvokeMemberNodeFactory.create(argumentNodes);
        }

        public abstract Object execute(Object receiver, Object identifier, Object[] args);

        abstract RubyNode[] getArgumentNodes();

        @Specialization(limit = "getInteropCacheLimit()")
        protected Object invokeCached(Object receiver, Object identifier, Object[] args,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object foreign = invoke(receivers, receiver, name, args, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }

        @Override
        public RubyNode cloneUninitialized() {
            return create(cloneUninitialized(getArgumentNodes())).copyFlags(this);
        }

    }

    @CoreMethod(names = "member_readable?", onSingleton = true, required = 2)
    public abstract static class IsMemberReadableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberReadable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberReadable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_modifiable?", onSingleton = true, required = 2)
    public abstract static class IsMemberModifiableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberModifiable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberModifiable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_insertable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInsertableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberInsertable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInsertable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_removable?", onSingleton = true, required = 2)
    public abstract static class IsMemberRemovableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberRemovable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberRemovable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_invocable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInvocableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberInvocable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInvocable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_internal?", onSingleton = true, required = 2)
    public abstract static class IsMemberInternalNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberInternal(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInternal(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_writable?", onSingleton = true, required = 2)
    public abstract static class IsMemberWritableNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberWritable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberWritable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "member_existing?", onSingleton = true, required = 2)
    public abstract static class IsMemberExistingNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMemberExisting(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberExisting(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_read_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberReadSideEffectsNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasMemberReadSideEffects(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberReadSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_write_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberWriteSideEffectsNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasMemberWriteSideEffects(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberWriteSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }
    // endregion

    // region Import/Export
    @CoreMethod(names = "export", onSingleton = true, required = 2)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "object", type = RubyNode.class)
    public abstract static class ExportNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceNameToString(RubyNode name) {
            return ToJavaStringNode.create(name);
        }

        @TruffleBoundary
        @Specialization
        protected Object export(String name, Object object) {
            getContext().getEnv().exportSymbol(name, object);
            return object;
        }
    }

    @CoreMethod(names = "import", onSingleton = true, required = 1)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ImportNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceNameToString(RubyNode name) {
            return ToJavaStringNode.create(name);
        }

        @Specialization
        protected Object importObject(String name,
                @Cached BranchProfile errorProfile,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            final Object value = doImport(name);
            if (value != null) {
                return foreignToRubyNode.executeConvert(value);
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorImportNotFound(name, this));
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
        protected Object getLanguage(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached FromJavaStringNode fromJavaStringNode) {
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
            return fromJavaStringNode.executeFromJavaString(name);
        }

        @TruffleBoundary
        private String languageClassToLanguageName(Class<? extends TruffleLanguage<?>> language) {
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
            return toJavaStringNode.executeToJavaString(value);
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

    @CoreMethod(names = "deproxy", onSingleton = true, required = 1)
    public abstract static class DeproxyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isJavaObject(object)")
        protected Object deproxyJavaObject(Object object) {
            return getContext().getEnv().asHostObject(object);
        }

        @Specialization(guards = "!isJavaObject(object)")
        protected Object deproxyNotJavaObject(Object object) {
            return object;
        }

        protected boolean isJavaObject(Object object) {
            return getContext().getEnv().isHostObject(object);
        }

    }

    @CoreMethod(names = "java_type", onSingleton = true, required = 1)
    public abstract static class JavaTypeNode extends CoreMethodArrayArgumentsNode {

        // TODO CS 17-Mar-18 we should cache this in the future

        @Specialization
        protected Object javaType(Object name,
                @Cached ToJavaStringNode toJavaStringNode) {
            return lookupJavaType(toJavaStringNode.executeToJavaString(name));
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
        protected Object metaObject(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached BranchProfile errorProfile,
                @Cached LogicalClassNode logicalClassNode) {
            if (interop.hasMetaObject(value)) {
                try {
                    return interop.getMetaObject(value);
                } catch (UnsupportedMessageException e) {
                    errorProfile.enter();
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
        protected Object declaringMetaObject(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getDeclaringMetaObject(value);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "meta_instance?", onSingleton = true, required = 2)
    public abstract static class IsMetaInstanceNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean isMetaInstance(Object metaObject, Object instance,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.isMetaInstance(metaObject, instance);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "meta_simple_name", onSingleton = true, required = 1)
    public abstract static class GetMetaSimpleNameNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object getMetaSimpleName(Object metaObject,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getMetaSimpleName(metaObject);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "meta_qualified_name", onSingleton = true, required = 1)
    public abstract static class GetMetaQualifiedNameNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object getMetaQualifiedName(Object metaObject,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getMetaQualifiedName(metaObject);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object getMetaParents(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getMetaParents(value);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object hashEntriesIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getHashEntriesIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected Object hashKeysIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getHashKeysIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "hash_size", onSingleton = true, required = 1)
    public abstract static class HashSizeNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected long hashSize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getHashSize(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "hash_values_iterator", onSingleton = true, required = 1)
    public abstract static class HashValuesIteratorNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object hashValuesIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getHashValuesIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }


    @CoreMethod(names = "read_hash_value", onSingleton = true, required = 2)
    public abstract static class ReadHashValueNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object readHashValue(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readHashValue(receiver, key);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "read_hash_value_or_default", onSingleton = true, required = 3)
    public abstract static class ReadHashValueOrDefaultNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object readHashValueOrDefault(Object receiver, Object key, Object defaultValue,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readHashValueOrDefault(receiver, key, defaultValue);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "remove_hash_entry", onSingleton = true, required = 2)
    public abstract static class RemoveHashEntryNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object removeHashEntry(Object receiver, Object key,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                interop.removeHashEntry(receiver, key);
                return nil;
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "write_hash_entry", onSingleton = true, required = 3)
    public abstract static class WriteHashEntryNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object writeHashEntry(Object receiver, Object key, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                interop.writeHashEntry(receiver, key, value);
                return value;
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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
        protected int identityHashCode(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            if (interop.hasIdentity(value)) {
                try {
                    return interop.identityHashCode(value);
                } catch (UnsupportedMessageException e) {
                    throw translateInteropException.execute(e);
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
        protected Object getScope(Object scope,
                @CachedLibrary("scope") InteropLibrary interopLibrary,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            if (interopLibrary.hasScopeParent(scope)) {
                try {
                    return interopLibrary.getScopeParent(scope);
                } catch (UnsupportedMessageException e) {
                    throw translateInteropException.execute(e);
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
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return nodeLibrary.getScope(this, frame, true);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
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
        protected boolean isBufferWritable(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.isBufferWritable(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "buffer_size", onSingleton = true, required = 1)
    public abstract static class GetBufferSizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected long getBufferSize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getBufferSize(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "read_buffer_byte", onSingleton = true, required = 2)
    public abstract static class ReadBufferByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()")
        protected byte readBufferByte(Object receiver, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readBufferByte(receiver, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_byte", onSingleton = true, required = 3)
    public abstract static class WriteBufferByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInByte(value)")
        protected Object writeBufferByte(Object receiver, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final byte byteValue = interopValue.asByte(value);
                interop.writeBufferByte(receiver, byteOffset, byteValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_short", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferShortNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()")
        protected short readBufferShort(Object receiver, ByteOrder byteOrder, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readBufferShort(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_short", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferShortNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInShort(value)")
        protected Object writeBufferShort(Object receiver, ByteOrder byteOrder, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final short shortValue = interopValue.asShort(value);
                interop.writeBufferShort(receiver, byteOrder, byteOffset, shortValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_int", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferIntNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()")
        protected int readBufferInt(Object receiver, ByteOrder byteOrder, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readBufferInt(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_int", onSingleton = true, required = 4, lowerFixnum = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferIntNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInInt(value)")
        protected Object writeBufferInt(Object receiver, ByteOrder order, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final int intValue = interopValue.asInt(value);
                interop.writeBufferInt(receiver, order, byteOffset, intValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_long", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferLongNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()")
        protected long readBufferLong(Object receiver, ByteOrder byteOrder, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readBufferLong(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_long", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferLongNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInLong(value)")
        protected Object writeBufferLong(Object receiver, ByteOrder order, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final long longValue = interopValue.asLong(value);
                interop.writeBufferLong(receiver, order, byteOffset, longValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_float", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferFloatNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        // must return double so Ruby nodes can deal with it
        @Specialization(limit = "getInteropCacheLimit()")
        protected double readBufferFloat(Object receiver, ByteOrder byteOrder, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readBufferFloat(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_float", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferFloatNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInDouble(value)")
        protected Object writeBufferFloat(Object receiver, ByteOrder order, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final float floatValue = (float) interopValue.asDouble(value);
                interop.writeBufferFloat(receiver, order, byteOffset, floatValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return value;
        }

    }

    @CoreMethod(names = "read_buffer_double", onSingleton = true, required = 3)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    public abstract static class ReadBufferDoubleNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()")
        protected double readBufferDouble(Object receiver, ByteOrder byteOrder, long byteOffset,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.readBufferDouble(receiver, byteOrder, byteOffset);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "write_buffer_double", onSingleton = true, required = 4)
    @NodeChild(value = "receiver", type = RubyNode.class)
    @NodeChild(value = "byteOrder", type = RubyNode.class)
    @NodeChild(value = "byteOffset", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class WriteBufferDoubleNode extends CoreMethodNode {

        @CreateCast("byteOrder")
        protected RubyNode coerceSymbolToByteOrder(RubyNode byteOrder) {
            return SymbolToByteOrderNode.create(byteOrder);
        }

        @Specialization(limit = "getInteropCacheLimit()", guards = "interopValue.fitsInDouble(value)")
        protected Object writeBufferDouble(Object receiver, ByteOrder order, long byteOffset, Object value,
                @CachedLibrary("receiver") InteropLibrary interop,
                @CachedLibrary("value") InteropLibrary interopValue,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final double doubleValue = interopValue.asDouble(value);
                interop.writeBufferDouble(receiver, order, byteOffset, doubleValue);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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
        protected Object getIterator(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getIterator(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "has_iterator_next_element?", onSingleton = true, required = 1)
    public abstract static class HasIteratorNextElementNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected boolean hasIteratorNextElement(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.hasIteratorNextElement(receiver);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "iterator_next_element", onSingleton = true, required = 1)
    public abstract static class GetIteratorNextElementNode extends CoreMethodArrayArgumentsNode {
        @Specialization(limit = "getInteropCacheLimit()")
        protected Object getIteratorNextElement(Object receiver,
                @CachedLibrary("receiver") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return interop.getIteratorNextElement(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }
    // endregion

}
