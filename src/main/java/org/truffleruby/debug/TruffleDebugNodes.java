/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.StopIterationException;
import com.oracle.truffle.api.interop.UnknownKeyException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.collections.Pair;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.RubyHandle;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.cast.ToCallTargetNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.method.RubyUnboundMethod;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.interop.BoxedValue;
import org.truffleruby.interop.FromJavaStringNodeGen;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.InternalRootNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.utilities.TriState;
import org.truffleruby.parser.RubyDeferredWarnings;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorDriver;
import org.truffleruby.parser.parser.ParserConfiguration;
import org.truffleruby.parser.scope.StaticScope;

@CoreModule("Truffle::Debug")
public abstract class TruffleDebugNodes {

    @CoreMethod(names = "print", onSingleton = true, required = 1)
    public abstract static class DebugPrintNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object debugPrint(Object string,
                @Cached RubyStringLibrary strings) {
            final String javaString;
            if (strings.isRubyString(string)) {
                javaString = RubyGuards.getJavaString(string);
            } else {
                javaString = string.toString();
            }

            getContext().getEnvErrStream().println(javaString);
            return nil;
        }

    }

    @CoreMethod(names = "tstring_to_debug_string", onSingleton = true, required = 1)
    public abstract static class TStringToDebugPrintNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)", limit = "1")
        protected RubyString toStringDebug(Object string,
                @Cached RubyStringLibrary strings,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            return createString(fromJavaStringNode, strings.getTString(string).toStringDebug(), Encodings.US_ASCII);
        }
    }

    @CoreMethod(names = "flatten_string", onSingleton = true, required = 1)
    public abstract static class FlattenStringNode extends CoreMethodArrayArgumentsNode {
        // Also flattens the original String, but that one might still have an offset
        @TruffleBoundary
        @Specialization(guards = "libString.isRubyString(string)", limit = "1")
        protected RubyString flattenString(Object string,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached RubyStringLibrary libString) {
            final RubyEncoding rubyEncoding = libString.getEncoding(string);
            var tstring = libString.getTString(string);
            // Use GetInternalByteArrayNode as a way to flatten the TruffleString.
            // Ensure the result has offset = 0 and length = byte[].length for image build time checks
            byte[] byteArray = TStringUtils.getBytesOrCopy(tstring, rubyEncoding);
            return createString(fromByteArrayNode, byteArray, rubyEncoding);
        }
    }

    @CoreMethod(names = "break_handle", onSingleton = true, required = 2, needsBlock = true, split = Split.NEVER,
            lowerFixnum = 2)
    public abstract static class BreakNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(file)", limit = "1")
        protected RubyHandle setBreak(Object file, int line, RubyProc block,
                @Cached RubyStringLibrary strings) {
            final String fileString = RubyGuards.getJavaString(file);

            final SourceSectionFilter filter = SourceSectionFilter
                    .newBuilder()
                    .mimeTypeIs(RubyLanguage.MIME_TYPES)
                    .sourceIs(source -> source != null && getLanguage().getSourcePath(source).equals(fileString))
                    .lineIs(line)
                    .tagIs(StandardTags.StatementTag.class)
                    .build();

            final EventBinding<?> breakpoint = getContext().getInstrumenter().attachExecutionEventFactory(
                    filter,
                    eventContext -> new ExecutionEventNode() {

                        @Child private CallBlockNode yieldNode = CallBlockNode.create();

                        @Override
                        protected void onEnter(VirtualFrame frame) {
                            yieldNode.yield(
                                    block,
                                    BindingNodes.createBinding(
                                            getContext(),
                                            getLanguage(),
                                            frame.materialize(),
                                            eventContext.getInstrumentedSourceSection()));
                        }

                    });

            final RubyHandle instance = new RubyHandle(
                    coreLibrary().handleClass,
                    getLanguage().handleShape,
                    breakpoint);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "remove_handle", onSingleton = true, required = 1)
    public abstract static class RemoveNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object remove(RubyHandle handle) {
            ((EventBinding<?>) handle.object).dispose();
            return nil;
        }

    }

    @CoreMethod(names = "java_class_of", onSingleton = true, required = 1)
    public abstract static class JavaClassOfNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString javaClassOf(Object value) {
            return createString(fromJavaStringNode, value.getClass().getSimpleName(), Encodings.UTF_8);
        }

    }

    @CoreMethod(names = "print_backtrace", onSingleton = true)
    public abstract static class PrintBacktraceNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected Object printBacktrace() {
            getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            return nil;
        }

    }

    @CoreMethod(names = "parse_ast", onSingleton = true, required = 1)
    public abstract static class ParseASTNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(code)", limit = "1")
        protected Object ast(Object code,
                @Cached RubyStringLibrary strings,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            String codeString = RubyGuards.getJavaString(code);
            String name = "<parse_ast>";
            var source = Source.newBuilder("ruby", codeString, name).build();
            var rubySource = new RubySource(source, name);

            var staticScope = new StaticScope(StaticScope.Type.LOCAL, null);
            var parserConfiguration = new ParserConfiguration(null, false, true, false);
            var rubyWarnings = new RubyDeferredWarnings();
            var rootParseNode = TranslatorDriver
                    .parseToJRubyAST(getContext(), rubySource, staticScope, parserConfiguration, rubyWarnings);

            return createString(fromJavaStringNode, rootParseNode.toString(), Encodings.UTF_8);
        }
    }

    @CoreMethod(names = "ast", onSingleton = true, required = 1)
    public abstract static class ASTNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object ast(Object executable,
                @Cached ToCallTargetNode toCallTargetNode) {
            final RootCallTarget callTarget = toCallTargetNode.execute(executable);
            return ast(callTarget.getRootNode());
        }

        private Object ast(Node node) {
            if (node == null) {
                return nil;
            }

            final List<Object> array = new ArrayList<>();

            array.add(getSymbol(node.getClass().getSimpleName()));

            for (Node child : node.getChildren()) {
                array.add(ast(child));
            }

            return createArray(array.toArray());
        }
    }

    @CoreMethod(names = "print_ast", onSingleton = true, required = 1)
    public abstract static class PrintASTNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object printAST(Object executable,
                @Cached ToCallTargetNode toCallTargetNode) {
            final RootCallTarget callTarget = toCallTargetNode.execute(executable);
            NodeUtil.printCompactTree(getContext().getEnvErrStream(), callTarget.getRootNode());
            return nil;
        }
    }

    @CoreMethod(names = "print_source_sections", onSingleton = true, required = 1)
    public abstract static class PrintSourceSectionsNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object printSourceSections(Object executable,
                @Cached ToCallTargetNode toCallTargetNode) {
            final RootCallTarget callTarget = toCallTargetNode.execute(executable);
            NodeUtil.printSourceAttributionTree(getContext().getEnvErrStream(), callTarget.getRootNode());
            return nil;
        }
    }

    @CoreMethod(names = "ast_size", onSingleton = true, required = 1)
    public abstract static class ASTSizeNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected int astSize(Object executable,
                @Cached ToCallTargetNode toCallTargetNode) {
            final RootCallTarget callTarget = toCallTargetNode.execute(executable);
            return NodeUtil.countNodes(callTarget.getRootNode());
        }
    }

    @CoreMethod(names = "shape", onSingleton = true, required = 1)
    public abstract static class ShapeNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString shape(RubyDynamicObject object) {
            return createString(fromJavaStringNode, object.getShape().toString(), Encodings.UTF_8);
        }

    }

    @CoreMethod(names = "array_storage", onSingleton = true, required = 1)
    public abstract static class ArrayStorageNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString arrayStorage(RubyArray array) {
            String storage = ArrayStoreLibrary.getFactory().getUncached().toString(array.getStore());
            return createString(fromJavaStringNode, storage, Encodings.US_ASCII);
        }

    }

    @CoreMethod(names = "array_capacity", onSingleton = true, required = 1)
    @ImportStatic(ArrayGuards.class)
    public abstract static class ArrayCapacityNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected long arrayStorage(RubyArray array,
                @Bind("array.getStore()") Object store,
                @CachedLibrary("store") ArrayStoreLibrary stores) {
            return stores.capacity(store);
        }

    }

    @CoreMethod(names = "hash_storage", onSingleton = true, required = 1)
    public abstract static class HashStorageNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString hashStorage(RubyHash hash) {
            Object store = hash.store;
            String storage = store == null ? "null" : store.getClass().toString();
            return createString(fromJavaStringNode, storage, Encodings.US_ASCII);
        }

    }

    @CoreMethod(names = "shared?", onSingleton = true, required = 1)
    @ImportStatic(SharedObjects.class)
    public abstract static class IsSharedNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = "object.getShape() == cachedShape",
                assumptions = "cachedShape.getValidAssumption()",
                limit = "getDynamicObjectCacheLimit()")
        protected boolean isSharedCached(RubyDynamicObject object,
                @Cached("object.getShape()") Shape cachedShape,
                @Cached("cachedShape.isShared()") boolean shared) {
            return shared;
        }

        @Specialization(replaces = "isSharedCached")
        protected boolean isShared(RubyDynamicObject object) {
            return SharedObjects.isShared(object);
        }

        @Specialization
        protected boolean isSharedImmutable(ImmutableRubyObject object) {
            return true;
        }
    }

    @CoreMethod(names = "log_warning", onSingleton = true, required = 1)
    public abstract static class LogWarningNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object logWarning(Object value,
                @Cached ToJavaStringNode toJavaStringNode) {
            warning(toJavaStringNode.executeToJavaString(value));
            return nil;
        }

        @TruffleBoundary
        static void warning(String message) {
            RubyLanguage.LOGGER.warning(message);
        }

    }

    @CoreMethod(names = "log_info", onSingleton = true, required = 1)
    public abstract static class LogInfoNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object logInfo(Object value,
                @Cached ToJavaStringNode toJavaStringNode) {
            info(toJavaStringNode.executeToJavaString(value));
            return nil;
        }

        @TruffleBoundary
        static void info(String message) {
            RubyLanguage.LOGGER.info(message);
        }

    }

    @CoreMethod(names = "log_config", onSingleton = true, required = 1)
    public abstract static class LogConfigNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object logConfig(Object value,
                @Cached ToJavaStringNode toJavaStringNode) {
            config(toJavaStringNode.executeToJavaString(value));
            return nil;
        }

        @TruffleBoundary
        static void config(String message) {
            RubyLanguage.LOGGER.config(message);
        }

    }

    @CoreMethod(names = "throw_java_exception", onSingleton = true, required = 1)
    public abstract static class ThrowJavaExceptionNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(message)", limit = "1")
        protected Object throwJavaException(Object message,
                @Cached RubyStringLibrary strings) {
            callingMethod(RubyGuards.getJavaString(message));
            return nil;
        }

        // These two named methods makes it easy to test that the backtrace for a Java exception is what we expect

        private static void callingMethod(String message) {
            throwingMethod(message);
        }

        private static void throwingMethod(String message) {
            throw new RuntimeException(message);
        }

    }

    @CoreMethod(names = "throw_java_exception_with_cause", onSingleton = true, required = 1)
    public abstract static class ThrowJavaExceptionWithCauseNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(message)", limit = "1")
        protected Object throwJavaExceptionWithCause(Object message,
                @Cached RubyStringLibrary strings) {
            throw new RuntimeException(
                    RubyGuards.getJavaString(message),
                    new RuntimeException("cause 1", new RuntimeException("cause 2")));
        }

    }

    @CoreMethod(names = "throw_assertion_error", onSingleton = true, required = 1)
    public abstract static class ThrowAssertionErrorNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(message)", limit = "1")
        protected Object throwAssertionError(Object message,
                @Cached RubyStringLibrary strings) {
            throw new AssertionError(RubyGuards.getJavaString(message));
        }

    }

    @CoreMethod(names = "assert", onSingleton = true, required = 1)
    public abstract static class AssertNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object doAssert(boolean condition) {
            assert condition;
            return nil;
        }
    }

    @Primitive(name = "assert")
    public abstract static class AssertPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object doAssert(boolean condition) {
            assert condition;
            return nil;
        }
    }

    @CoreMethod(names = "java_class", onSingleton = true)
    public abstract static class JavaClassNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object javaObject() {
            return getContext().getEnv().asGuestValue(BigInteger.class);
        }

    }

    @CoreMethod(names = "java_object", onSingleton = true)
    public abstract static class JavaObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object javaObject() {
            return getContext().getEnv().asGuestValue(new BigInteger("14"));
        }

    }

    @CoreMethod(names = "java_null", onSingleton = true)
    public abstract static class JavaNullNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object javaNull() {
            return getContext().getEnv().asGuestValue(null);
        }

    }

    @CoreMethod(names = "java_character", onSingleton = true)
    public abstract static class JavaCharacterNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Character javaCharacter() {
            return 'C';
        }
    }

    @CoreMethod(names = "foreign_null", onSingleton = true)
    public abstract static class ForeignNullNode extends CoreMethodArrayArgumentsNode {

        @ExportLibrary(InteropLibrary.class)
        public static class ForeignNull implements TruffleObject {

            @ExportMessage
            protected boolean isNull() {
                return true;
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign null]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignNull() {
            return new ForeignNull();
        }

    }

    @CoreMethod(names = "foreign_pointer", required = 1, onSingleton = true)
    public abstract static class ForeignPointerNode extends CoreMethodArrayArgumentsNode {

        @ExportLibrary(InteropLibrary.class)
        public static class ForeignPointer implements TruffleObject {

            private final long address;

            public ForeignPointer(long address) {
                this.address = address;
            }

            @ExportMessage
            protected boolean isPointer() {
                return true;
            }

            @ExportMessage
            protected long asPointer() {
                return address;
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign pointer]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignPointer(long address) {
            return new ForeignPointer(address);
        }

    }

    @CoreMethod(names = "foreign_object", onSingleton = true)
    public abstract static class ForeignObjectNode extends CoreMethodArrayArgumentsNode {

        @ExportLibrary(InteropLibrary.class)
        public static class ForeignObject implements TruffleObject {
            @ExportMessage
            protected TriState isIdenticalOrUndefined(Object other) {
                return other instanceof ForeignObject ? TriState.valueOf(this == other) : TriState.UNDEFINED;
            }

            @ExportMessage
            protected int identityHashCode() {
                return System.identityHashCode(this);
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign object]";
            }
        }

        @Specialization
        protected Object foreignObject() {
            return new ForeignObject();
        }

    }

    @CoreMethod(names = "foreign_object_with_members", onSingleton = true)
    public abstract static class ForeignObjectWithMembersNode extends CoreMethodArrayArgumentsNode {

        @ExportLibrary(InteropLibrary.class)
        public static class ForeignObjectWithMembers implements TruffleObject {

            private final Map<String, Object> map;

            public ForeignObjectWithMembers() {
                map = new HashMap<>();
                map.put("a", 1);
                map.put("b", 2);
                map.put("c", 3);
            }

            @ExportMessage
            protected boolean hasMembers() {
                return true;
            }

            @ExportMessage
            @TruffleBoundary
            protected Object getMembers(boolean includeInternal) {
                return new ForeignArrayNode.ForeignArray("a", "b", "c", "method1", "method2");
            }

            @TruffleBoundary
            @ExportMessage
            protected boolean isMemberReadable(String member) {
                return map.containsKey(member) || isMemberInvocable(member);
            }

            @TruffleBoundary
            @ExportMessage
            protected boolean isMemberInvocable(String member) {
                return "method1".equals(member) || "method2".equals(member);
            }

            @TruffleBoundary
            @ExportMessage
            protected Object readMember(String member) throws UnknownIdentifierException {
                if (member.equals("method1")) {
                    return new ForeignExecutableNode.ForeignExecutable(42);
                } else if (member.equals("method2")) {
                    return new ForeignExecutableNode.ForeignExecutable(44);
                }

                final Object value = map.get(member);
                if (value == null) {
                    throw UnknownIdentifierException.create(member);
                }
                return value;
            }

            @TruffleBoundary
            @ExportMessage
            protected Object invokeMember(String member, Object[] arguments) throws UnsupportedMessageException,
                    ArityException, UnknownIdentifierException, UnsupportedTypeException {
                if (!isMemberInvocable(member)) {
                    throw UnknownIdentifierException.create(member);
                }

                return InteropLibrary.getUncached().execute(readMember("method1"));
            }


            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign object with members]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignObjectWithMembers() {
            return new ForeignObjectWithMembers();
        }

    }

    @CoreMethod(names = "foreign_array", onSingleton = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class ForeignArrayNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignArray implements TruffleObject {

            private final Object[] array;

            public ForeignArray(Object... values) {
                this.array = values;
            }

            @ExportMessage
            protected boolean hasArrayElements() {
                return true;
            }

            @ExportMessage(name = "isArrayElementReadable")
            @ExportMessage(name = "isArrayElementModifiable")
            protected boolean isArrayElement(long index) {
                return 0 >= index && index < array.length;
            }

            @TruffleBoundary
            @ExportMessage
            protected Object readArrayElement(long index) throws InvalidArrayIndexException {
                try {
                    return array[(int) index];
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw InvalidArrayIndexException.create(index);
                }
            }

            @TruffleBoundary
            @ExportMessage
            protected void writeArrayElement(long index, Object value) throws InvalidArrayIndexException {
                try {
                    array[(int) index] = value;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw InvalidArrayIndexException.create(index);
                }
            }

            @ExportMessage
            protected final boolean isArrayElementInsertable(long index) {
                return false;
            }

            @ExportMessage
            protected long getArraySize() {
                return array.length;
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign array]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignArray() {
            return new ForeignArray(1, 2, 3);
        }
    }

    @CoreMethod(names = "foreign_pointer_array", onSingleton = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class ForeignPointerArrayNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignPointerArray extends ForeignArrayNode.ForeignArray {
            public ForeignPointerArray(Object... values) {
                super(values);
            }

            @ExportMessage
            protected boolean isPointer() {
                return true;
            }

            @ExportMessage
            protected long asPointer() {
                return 0; // shouldn't be used
            }

            @Override
            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign pointer array]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignPointerArray() {
            return new ForeignPointerArray(1, 2, 3);
        }
    }

    @CoreMethod(names = "foreign_iterator", onSingleton = true)
    public abstract static class ForeignIteratorNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignIterator implements TruffleObject {
            final int[] values = { 1, 2, 3 };
            int index = 0;

            @ExportMessage
            protected boolean isIterator() {
                return true;
            }

            @ExportMessage
            protected boolean hasIteratorNextElement() {
                return index < values.length;
            }

            @TruffleBoundary
            @ExportMessage
            protected Object getIteratorNextElement() throws StopIterationException {
                if (hasIteratorNextElement()) {
                    Object value = values[index];
                    index++;
                    return value;
                } else {
                    throw StopIterationException.create();
                }
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign iterator]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignIterator() {
            return new ForeignIterator();
        }
    }

    @CoreMethod(names = "foreign_iterable", onSingleton = true)
    public abstract static class ForeignIterableNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignIterable implements TruffleObject {
            @ExportMessage
            protected boolean hasIterator() {
                return true;
            }

            @ExportMessage
            protected Object getIterator() {
                return new ForeignIteratorNode.ForeignIterator();
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign iterable]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignIterable() {
            return new ForeignIterable();
        }
    }

    @CoreMethod(names = "foreign_hash", onSingleton = true)
    public abstract static class ForeignHashNode extends CoreMethodArrayArgumentsNode {

        @ExportLibrary(InteropLibrary.class)
        public static class ForeignHashEntriesIterator implements TruffleObject {
            final ForeignHash foreignHash;
            int index = 0;

            public ForeignHashEntriesIterator(ForeignHash foreignHash) {
                this.foreignHash = foreignHash;
            }

            @ExportMessage
            protected boolean isIterator() {
                return true;
            }

            @ExportMessage
            protected boolean hasIteratorNextElement() {
                return index < 2;
            }

            @TruffleBoundary
            @ExportMessage
            protected Object getIteratorNextElement() throws StopIterationException {
                if (index == 0) {
                    index++;
                    return new ForeignArrayNode.ForeignArray(foreignHash.key1, foreignHash.value1);
                } else if (index == 1) {
                    index++;
                    return new ForeignArrayNode.ForeignArray(foreignHash.key2, foreignHash.value2);
                } else {
                    throw StopIterationException.create();
                }
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign hash entries iterator]";
            }
        }

        @ExportLibrary(InteropLibrary.class)
        public static class ForeignHash implements TruffleObject {
            private final RubySymbol key1;
            private final int value1;
            private final RubySymbol key2;
            private final int value2;

            public ForeignHash(RubySymbol key1, int value1, RubySymbol key2, int value2) {
                this.key1 = key1;
                this.value1 = value1;
                this.key2 = key2;
                this.value2 = value2;
            }

            @ExportMessage
            protected boolean hasHashEntries() {
                return true;
            }

            @ExportMessage
            protected long getHashSize() {
                return 2;
            }

            @TruffleBoundary
            @ExportMessage
            protected boolean isHashEntryReadable(Object key) {
                return key == key1 || key == key2;
            }

            @TruffleBoundary
            @ExportMessage
            protected Object readHashValue(Object key) throws UnknownKeyException {
                if (key == key1) {
                    return value1;
                } else if (key == key2) {
                    return value2;
                } else {
                    throw UnknownKeyException.create(key);
                }
            }

            @ExportMessage
            protected Object getHashEntriesIterator() {
                return new ForeignHashEntriesIterator(this);
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign hash]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignHash() {
            return new ForeignHash(getSymbol("a"), 1, getSymbol("b"), 2);
        }
    }

    @CoreMethod(names = "foreign_executable", required = 1, onSingleton = true)
    public abstract static class ForeignExecutableNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignExecutable implements TruffleObject {

            private final Object value;

            public ForeignExecutable(Object value) {
                this.value = value;
            }

            @ExportMessage
            protected boolean isExecutable() {
                return true;
            }

            @ExportMessage
            protected Object execute(Object... arguments) {
                return value;
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign executable]";
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignExecutable(Object value) {
            return new ForeignExecutable(value);
        }
    }

    @CoreMethod(names = "foreign_identity_function", onSingleton = true)
    public abstract static class ForeignIdentityFunctionNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignIdentityFunction implements TruffleObject {
            @ExportMessage
            protected final boolean isExecutable() {
                return true;
            }

            @ExportMessage
            protected final Object execute(Object[] args) {
                return args[0];
            }
        }

        @TruffleBoundary
        @Specialization
        protected Object foreignIdentityFunction() {
            return new ForeignIdentityFunction();
        }
    }

    @CoreMethod(names = "foreign_string", onSingleton = true, required = 1)
    public abstract static class ForeignStringNode extends CoreMethodArrayArgumentsNode {
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignString implements TruffleObject {

            private final String string;

            public ForeignString(String string) {
                this.string = string;
            }

            @ExportMessage
            protected boolean isString() {
                return true;
            }

            @ExportMessage
            protected String asString() {
                return string;
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign string]";
            }
        }

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)", limit = "1")
        protected Object foreignString(Object string,
                @Cached RubyStringLibrary strings) {
            return new ForeignString(RubyGuards.getJavaString(string));
        }
    }

    @CoreMethod(names = "foreign_exception", required = 1, onSingleton = true)
    public abstract static class ForeignExceptionNode extends CoreMethodArrayArgumentsNode {
        @SuppressWarnings("serial")
        @ExportLibrary(InteropLibrary.class)
        public static class ForeignException extends AbstractTruffleException {

            public ForeignException(String message) {
                super(message);
            }

            @ExportMessage
            protected boolean isException() {
                return true;
            }

            @ExportMessage
            public RuntimeException throwException() {
                throw this;
            }

            @ExportMessage
            protected String toDisplayString(boolean allowSideEffects) {
                return "[foreign exception]";
            }
        }

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(message)", limit = "1")
        protected Object foreignException(Object message,
                @Cached RubyStringLibrary strings) {
            return new ForeignException(RubyGuards.getJavaString(message));
        }
    }

    @CoreMethod(names = "foreign_boxed_value", onSingleton = true, required = 1)
    public abstract static class ForeignBoxedNumberNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected Object foreignBoxedNumber(Object number) {
            return new BoxedValue(number);
        }
    }

    @CoreMethod(names = "long", onSingleton = true, required = 1)
    public abstract static class LongNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long asLong(int value) {
            return value;
        }

        @Specialization
        protected long asLong(long value) {
            return value;
        }

    }

    @CoreMethod(names = "associated", onSingleton = true, required = 1)
    public abstract static class AssociatedNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray associated(RubyString string) {
            final DynamicObjectLibrary objectLibrary = DynamicObjectLibrary.getUncached();
            Pointer[] associated = (Pointer[]) objectLibrary.getOrDefault(string, Layouts.ASSOCIATED_IDENTIFIER, null);

            if (associated == null) {
                associated = Pointer.EMPTY_ARRAY;
            }

            final long[] associatedValues = new long[associated.length];

            for (int n = 0; n < associated.length; n++) {
                associatedValues[n] = associated[n].getAddress();
            }

            return ArrayHelpers.createArray(getContext(), getLanguage(), associatedValues);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray associated(ImmutableRubyString string) {
            return ArrayHelpers.createEmptyArray(getContext(), getLanguage());
        }

    }

    @CoreMethod(names = "drain_finalization_queue", onSingleton = true)
    public abstract static class DrainFinalizationQueueNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object drainFinalizationQueue() {
            getContext().getFinalizationService().drainFinalizationQueue(getContext());
            return nil;
        }

    }

    @Primitive(name = "frame_declaration_context_to_string")
    public abstract static class FrameDeclarationContextToStringNode extends PrimitiveArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @Specialization
        protected RubyString getDeclarationContextToString(VirtualFrame frame) {
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
            return createString(fromJavaStringNode, declarationContext.toString(), Encodings.UTF_8);
        }
    }

    @CoreMethod(names = "get_frame_bindings", onSingleton = true)
    public abstract static class IterateFrameBindingsNode extends YieldingCoreMethodNode {

        /** This logic should be kept in sync with
         * {@link org.truffleruby.language.backtrace.BacktraceFormatter#nextAvailableSourceSection(TruffleStackTraceElement[], int) } */
        @TruffleBoundary
        @Specialization
        protected RubyArray getFrameBindings() {
            Deque<Pair<MaterializedFrame, SourceSection>> stack = new ArrayDeque<>();
            getContext().getCallStack().iterateFrameBindings(5, frameInstance -> {
                final RootNode rootNode = ((RootCallTarget) frameInstance.getCallTarget()).getRootNode();
                if (rootNode instanceof RubyRootNode) {
                    final Frame frame = frameInstance.getFrame(FrameInstance.FrameAccess.MATERIALIZE);
                    final SourceSection sourceSection;
                    if (frameInstance.getCallNode() != null &&
                            BacktraceFormatter
                                    .isAvailable(frameInstance.getCallNode().getEncapsulatingSourceSection())) {
                        sourceSection = frameInstance.getCallNode().getEncapsulatingSourceSection();
                    } else {
                        sourceSection = null;
                    }
                    stack.push(Pair.create(frame.materialize(), sourceSection));
                } else if (!(rootNode instanceof InternalRootNode) &&
                        frameInstance.getCallNode().getEncapsulatingSourceSection() != null) {
                    stack.push(Pair.create(null, null));
                }
                return null;
            });

            while (!stack.isEmpty() && stack.peek().getRight() == null) {
                stack.pop();
            }

            final List<Object> frameBindings = new ArrayList<>();
            SourceSection lastAvailableSourceSection = null;
            while (!stack.isEmpty()) {
                final Pair<MaterializedFrame, SourceSection> frameAndSource = stack.pop();
                final MaterializedFrame frame = frameAndSource.getLeft();
                final SourceSection source = frameAndSource.getRight();
                if (frame != null) {
                    SourceSection sourceSection;
                    if (source != null) {
                        sourceSection = source;
                        lastAvailableSourceSection = source;
                    } else {
                        sourceSection = lastAvailableSourceSection;
                    }
                    final RubyBinding binding = BindingNodes
                            .createBinding(getContext(), getLanguage(), frame, sourceSection);
                    frameBindings.add(binding);
                } else {
                    frameBindings.add(nil);
                }
            }
            Collections.reverse(frameBindings);
            return createArray(frameBindings.toArray());
        }
    }

    @CoreMethod(names = "parse_name_of_method", onSingleton = true, required = 1)
    public abstract static class ParseNameOfMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleString.FromJavaStringNode fromJavaStringNode = TruffleString.FromJavaStringNode.create();

        @Specialization
        protected RubyString parseName(RubyMethod method) {
            return parseName(method.method);
        }

        @Specialization
        protected RubyString parseName(RubyUnboundMethod method) {
            return parseName(method.method);
        }

        protected RubyString parseName(InternalMethod method) {
            String parseName = method.getSharedMethodInfo().getParseName();
            return createString(fromJavaStringNode, parseName, Encodings.UTF_8);
        }

    }

    /** Creates a Truffle thread which is not {@link ThreadManager#isRubyManagedThread(java.lang.Thread)}}. */
    @CoreMethod(names = "create_polyglot_thread", onSingleton = true, required = 1)
    public abstract static class CreatePolyglotThread extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object parseName(Object hostRunnable) {
            Runnable runnable = (Runnable) getContext().getEnv().asHostObject(hostRunnable);
            final Thread thread = getContext().getEnv().createThread(runnable);
            return getContext().getEnv().asGuestValue(thread);
        }
    }

    @CoreMethod(names = "primitive_names", onSingleton = true)
    public abstract static class PrimitiveNamesNode extends CoreMethodArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        protected RubyArray primitiveNames() {
            var primitiveNames = getLanguage().primitiveManager.getPrimitiveNames().stream()
                    .map(name -> FromJavaStringNodeGen.getUncached().executeFromJavaString(name));

            return createArray(primitiveNames.toArray());
        }
    }

    @CoreMethod(names = "cexts_to_native_count", onSingleton = true)
    public abstract static class HandleCreationCountNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long handleCount() {
            return getContext().getValueWrapperManager().totalHandleAllocations();
        }
    }

}
