/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.ObjectType;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.array.ArrayReflector;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.interop.BoxedValue;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@CoreClass("Truffle::Debug")
public abstract class TruffleDebugNodes {

    @CoreMethod(names = "print", onSingleton = true, required = 1)
    public abstract static class DebugPrintNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject debugPrint(Object string) {
            System.err.println(string.toString());
            return nil();
        }

    }

    @CoreMethod(names = "break_handle", onSingleton = true, required = 2, needsBlock = true, lowerFixnum = 2)
    public abstract static class BreakNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(file)")
        public DynamicObject setBreak(DynamicObject file, int line, DynamicObject block) {
            final String fileString = StringOperations.getString(file);

            final SourceSectionFilter filter = SourceSectionFilter.newBuilder()
                    .mimeTypeIs(RubyLanguage.MIME_TYPE)
                    .sourceIs(source -> source != null && getContext().getPath(source).equals(fileString))
                    .lineIs(line)
                    .tagIs(StandardTags.StatementTag.class)
                    .build();

            final EventBinding<?> breakpoint = getContext().getInstrumenter().attachExecutionEventFactory(filter,
                    eventContext -> new ExecutionEventNode() {

                        @Child private YieldNode yieldNode = new YieldNode();

                        @Override
                        protected void onEnter(VirtualFrame frame) {
                            yieldNode.dispatch(block,
                                    BindingNodes.createBinding(getContext(),
                                            frame.materialize(),
                                            eventContext.getInstrumentedSourceSection()));
                        }

                    });

            return Layouts.HANDLE.createHandle(coreLibrary().getHandleFactory(), breakpoint);
        }

    }

    @CoreMethod(names = "remove_handle", onSingleton = true, required = 1)
    public abstract static class RemoveNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isHandle(handle)")
        public DynamicObject remove(DynamicObject handle) {
            EventBinding.class.cast(Layouts.HANDLE.getObject(handle)).dispose();
            return nil();
        }

    }

    @CoreMethod(names = "java_class_of", onSingleton = true, required = 1)
    public abstract static class JavaClassOfNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject javaClassOf(Object value) {
            return makeStringNode.executeMake(value.getClass().getSimpleName(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "print_backtrace", onSingleton = true)
    public abstract static class PrintBacktraceNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject printBacktrace() {
            getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            return nil();
        }

    }

    @CoreMethod(names = "ast", onSingleton = true, optional = 1, needsBlock = true)
    public abstract static class ASTNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyMethod(method)")
        public DynamicObject astMethod(DynamicObject method, NotProvided block) {
            ast(Layouts.METHOD.getMethod(method));
            return nil();
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyUnboundMethod(method)")
        public DynamicObject astUnboundMethod(DynamicObject method, NotProvided block) {
            ast(Layouts.UNBOUND_METHOD.getMethod(method));
            return nil();
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyProc(proc)")
        public DynamicObject astProc(DynamicObject proc, NotProvided block) {
            ast(Layouts.PROC.getCallTargetForType(proc));
            return nil();
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject astBlock(NotProvided proc, DynamicObject block) {
            ast(Layouts.PROC.getCallTargetForType(block));
            return nil();
        }

        private DynamicObject ast(InternalMethod method) {
            return ast(method.getCallTarget());
        }

        private DynamicObject ast(CallTarget callTarget) {
            return ast((RootCallTarget) callTarget);
        }

        private DynamicObject ast(RootCallTarget rootCallTarget) {
            return ast(rootCallTarget.getRootNode());
        }

        private DynamicObject ast(Node node) {
            if (node == null) {
                return nil();
            }

            final List<Object> array = new ArrayList<>();

            array.add(getSymbol(node.getClass().getSimpleName()));

            for (Node child : node.getChildren()) {
                array.add(ast(child));
            }

            return createArray(array.toArray(), array.size());
        }

    }

    @CoreMethod(names = "print_ast", onSingleton = true, optional = 1, needsBlock = true)
    public abstract static class PrintASTNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyMethod(method)")
        public DynamicObject astMethod(DynamicObject method, NotProvided block) {
            printAst(Layouts.METHOD.getMethod(method));
            return nil();
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyUnboundMethod(method)")
        public DynamicObject astUnboundMethod(DynamicObject method, NotProvided block) {
            printAst(Layouts.UNBOUND_METHOD.getMethod(method));
            return nil();
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyProc(proc)")
        public DynamicObject astProc(DynamicObject proc, NotProvided block) {
            printAst(Layouts.PROC.getCallTargetForType(proc));
            return nil();
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject astBlock(NotProvided proc, DynamicObject block) {
            printAst(Layouts.PROC.getCallTargetForType(block));
            return nil();
        }

        private void printAst(InternalMethod method) {
            NodeUtil.printCompactTree(System.err, ((RootCallTarget) method.getCallTarget()).getRootNode());
        }

        private void printAst(CallTarget callTarget) {
            printAst((RootCallTarget) callTarget);
        }

        private void printAst(RootCallTarget callTarget) {
            NodeUtil.printCompactTree(System.err, callTarget.getRootNode());
        }

    }

    @CoreMethod(names = "object_type_of", onSingleton = true, required = 1)
    public abstract static class ObjectTypeOfNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject objectTypeOf(DynamicObject value) {
            return getSymbol(value.getShape().getObjectType().getClass().getSimpleName());
        }
    }

    @CoreMethod(names = "shape", onSingleton = true, required = 1)
    public abstract static class ShapeNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject shape(DynamicObject object) {
            return makeStringNode.executeMake(object.getShape().toString(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "array_storage", onSingleton = true, required = 1)
    public abstract static class ArrayStorageNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "isRubyArray(array)")
        public DynamicObject arrayStorage(DynamicObject array) {
            String storage = ArrayStrategy.of(array).toString();
            return makeStringNode.executeMake(storage, USASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "hash_storage", onSingleton = true, required = 1)
    public abstract static class HashStorageNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "isRubyHash(hash)")
        public DynamicObject hashStorage(DynamicObject hash) {
            Object store = Layouts.HASH.getStore(hash);
            String storage = store == null ? "null" : store.getClass().toString();
            return makeStringNode.executeMake(storage, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @CoreMethod(names = "shared?", onSingleton = true, required = 1)
    @ImportStatic(SharedObjects.class)
    public abstract static class IsSharedNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "object.getShape() == cachedShape",
                assumptions = "cachedShape.getValidAssumption()", limit = "getCacheLimit()")
        public boolean isSharedCached(DynamicObject object,
                @Cached("object.getShape()") Shape cachedShape,
                @Cached("isShared(getContext(), cachedShape)") boolean shared) {
            return shared;
        }

        @Specialization(replaces = "isSharedCached")
        public boolean isShared(DynamicObject object) {
            return SharedObjects.isShared(getContext(), object);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INSTANCE_VARIABLE_CACHE;
        }

    }

    @CoreMethod(names = "log_warning", isModuleFunction = true, required = 1)
    public abstract static class LogWarningNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject logWarning(
                VirtualFrame frame, Object value,
                @Cached("create()") ToJavaStringNode toJavaStringNode) {
            warning(toJavaStringNode.executeToJavaString(value));
            return nil();
        }

        @TruffleBoundary
        public static void warning(String message) {
            RubyLanguage.LOGGER.warning(message);
        }

    }

    @CoreMethod(names = "throw_java_exception", onSingleton = true, required = 1)
    public abstract static class ThrowJavaExceptionNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject throwJavaException(Object message) {
            callingMethod(message.toString());
            return nil();
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
        @Specialization
        public DynamicObject throwJavaExceptionWithCause(Object message) {
            throw new RuntimeException(message.toString(), new RuntimeException("cause 1", new RuntimeException("cause 2")));
        }

    }

    @CoreMethod(names = "assert", onSingleton = true, required = 1)
    public abstract static class AssertNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject throwJavaException(boolean condition) {
            assert condition;
            return nil();
        }

    }

    @CoreMethod(names = "java_class", onSingleton = true)
    public abstract static class JavaClassNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object javaObject() {
            return getContext().getEnv().asGuestValue(BigInteger.class);
        }

    }

    @CoreMethod(names = "java_object", onSingleton = true)
    public abstract static class JavaObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object javaObject() {
            return getContext().getEnv().asGuestValue(new BigInteger("14"));
        }

    }

    @CoreMethod(names = "java_null", onSingleton = true)
    public abstract static class JavaNullNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object javaNull() {
            return getContext().getEnv().asGuestValue(null);
        }

    }

    @CoreMethod(names = "foreign_null", onSingleton = true)
    public abstract static class ForeignNullNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignNull implements TruffleObject {

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignNullMessageResolutionForeign.ACCESS;
            }

        }

        @MessageResolution(receiverType = ForeignNull.class)
        public static class ForeignNullMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignNull;
                }

            }

            @Resolve(message = "IS_NULL")
            public static abstract class ForeignIsNullNode extends Node {

                protected Object access(VirtualFrame frame, ForeignNull noll) {
                    return true;
                }

            }

        }

        @TruffleBoundary
        @Specialization
        public Object foreignNull() {
            return new ForeignNull();
        }

    }

    @CoreMethod(names = "foreign_pointer", required = 1, onSingleton = true)
    public abstract static class ForeignPointerNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignPointer implements TruffleObject {

            private final long address;

            public ForeignPointer(long address) {
                this.address = address;
            }

            public long getAddress() {
                return address;
            }

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignPointerMessageResolutionForeign.ACCESS;
            }

        }

        @MessageResolution(receiverType = ForeignPointer.class)
        public static class ForeignPointerMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignPointer;
                }

            }

            @Resolve(message = "IS_POINTER")
            public static abstract class ForeignIsPointerNode extends Node {

                protected Object access(VirtualFrame frame, ForeignPointer pointer) {
                    return true;
                }

            }

            @Resolve(message = "AS_POINTER")
            public static abstract class ForeignAsPointerNode extends Node {

                protected Object access(VirtualFrame frame, ForeignPointer pointer) {
                    return pointer.getAddress();
                }

            }

        }

        @TruffleBoundary
        @Specialization
        public Object foreignPointer(long address) {
            return new ForeignPointer(address);
        }

    }

    @CoreMethod(names = "foreign_object",  onSingleton = true)
    public abstract static class ForeignObjectNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignObject implements TruffleObject {

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignObjectMessageResolutionForeign.ACCESS;
            }

        }

        @MessageResolution(receiverType = ForeignObject.class)
        public static class ForeignObjectMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignObject;
                }

            }

            @Resolve(message = "IS_BOXED")
            public static abstract class ForeignIsBoxedNode extends Node {

                protected Object access(VirtualFrame frame, ForeignObject number) {
                    return false;
                }

            }

        }

        @TruffleBoundary
        @Specialization
        public Object foreignObject() {
            return new ForeignObject();
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @CoreMethod(names = "foreign_object_from_map", required = 1, onSingleton = true)
    public abstract static class ForeignObjectFromMapNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignObjectFromMap implements TruffleObject {

            private final Map map;

            public ForeignObjectFromMap(Map map) {
                this.map = map;
            }

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignObjectFromMapMessageResolutionForeign.ACCESS;
            }

            public Map getMap() {
                return map;
            }
        }


        @MessageResolution(receiverType = ForeignObjectFromMap.class)
        public static class ForeignObjectFromMapMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignObjectFromMap;
                }

            }

            @Resolve(message = "KEYS")
            public static abstract class ForeignKeysNode extends Node {

                @CompilationFinal private ContextReference<RubyContext> contextReference;

                protected Object access(VirtualFrame frame, ForeignObjectFromMap object) {
                    final Map map = object.getMap();
                    return getContext().getEnv().asGuestValue(map.keySet().toArray(new String[map.size()]));
                }

                private RubyContext getContext() {
                    if (contextReference == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        contextReference = RubyLanguage.getCurrentContextReference();
                    }

                    return contextReference.get();
                }

            }

            @Resolve(message = "READ")
            public static abstract class ForeignReadNode extends Node {

                protected Object access(VirtualFrame frame, ForeignObjectFromMap object, Object key) {
                    final Map map = object.getMap();
                    return map.get(key);
                }

            }

        }

        @TruffleBoundary
        @Specialization
        public Object foreignObjectFromMap(TruffleObject map) {
            return new ForeignObjectFromMap((Map) getContext().getEnv().asHostObject(map));
        }

    }

    @CoreMethod(names = "foreign_array_from_java", required = 1, onSingleton = true)
    public abstract static class ForeignArrayFromJavaNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignArrayFromJava implements TruffleObject {

            private final Object[] array;

            public ForeignArrayFromJava(Object[] array) {
                this.array = array;
            }

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignForeignArrayFromJavaMessageResolutionForeign.ACCESS;
            }

            public Object[] getArray() {
                return array;
            }
        }


        @MessageResolution(receiverType = ForeignArrayFromJava.class)
        public static class ForeignForeignArrayFromJavaMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignArrayFromJava;
                }

            }

            @Resolve(message = "HAS_SIZE")
            public static abstract class ForeignHasSizeNode extends Node {

                protected Object access(VirtualFrame frame, ForeignArrayFromJava object) {
                    return true;
                }

            }

            @Resolve(message = "GET_SIZE")
            public static abstract class ForeignSizeNode extends Node {

                protected Object access(VirtualFrame frame, ForeignArrayFromJava object) {
                    final Object[] array = object.getArray();
                    return array.length;
                }

            }

            @Resolve(message = "READ")
            public static abstract class ForeignReadNode extends Node {

                protected Object access(VirtualFrame frame, ForeignArrayFromJava object, Object key) {
                    final Object[] array = object.getArray();
                    return array[(int) key];
                }

            }

        }

        @TruffleBoundary
        @Specialization
        public Object foreignArrayFromJava(TruffleObject array) {
            return new ForeignArrayFromJava(ArrayReflector.reflect(getContext().getEnv().asHostObject(array)).getBoxedCopy());
        }

    }

    @CoreMethod(names = "foreign_executable", required = 1, onSingleton = true)
    public abstract static class ForeignExecutableNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignExecutable implements TruffleObject {

            private final Object value;

            public ForeignExecutable(Object value) {
                this.value = value;
            }

            public Object getValue() {
                return value;
            }

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignExecutableMessageResolutionForeign.ACCESS;
            }

        }

        @MessageResolution(receiverType = ForeignExecutable.class)
        public static class ForeignExecutableMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignExecutable;
                }

            }

            @Resolve(message = "IS_EXECUTABLE")
            public static abstract class ForeignIsExecutableNode extends Node {

                protected Object access(VirtualFrame frame, ForeignExecutable executable) {
                    return true;
                }

            }

            @Resolve(message = "EXECUTE")
            public static abstract class ForeignExecuteNode extends Node {

                protected Object access(VirtualFrame frame, ForeignExecutable executable, Object... arguments) {
                    return executable.getValue();
                }

            }

        }

        @TruffleBoundary
        @Specialization
        public Object foreignExecutable(Object value) {
            return new ForeignExecutable(value);
        }

    }

    @CoreMethod(names = "foreign_string", onSingleton = true, required = 1)
    public abstract static class ForeignStringNode extends CoreMethodArrayArgumentsNode {

        public static class ForeignString implements TruffleObject {

            private final String string;

            public ForeignString(String string) {
                this.string = string;
            }

            public String getString() {
                return string;
            }

            @Override
            public ForeignAccess getForeignAccess() {
                return ForeignStringMessageResolutionForeign.ACCESS;
            }

        }

        @MessageResolution(receiverType = ForeignString.class)
        public static class ForeignStringMessageResolution {

            @CanResolve
            public abstract static class Check extends Node {

                protected static boolean test(TruffleObject receiver) {
                    return receiver instanceof ForeignString;
                }

            }

            @Resolve(message = "IS_BOXED")
            public static abstract class ForeignIsBoxedNode extends Node {

                protected Object access(VirtualFrame frame, ForeignString string) {
                    return true;
                }

            }

            @Resolve(message = "UNBOX")
            public static abstract class ForeignUnboxNode extends Node {

                protected Object access(VirtualFrame frame, ForeignString string) {
                    return string.getString();
                }

            }

        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(string)")
        public Object foreignString(DynamicObject string) {
            return new ForeignString(string.toString());
        }

    }

    @CoreMethod(names = "foreign_boxed_number", onSingleton = true, required = 1)
    public abstract static class ForeignBoxedNumberNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object foreignBoxedNumber(Number number) {
            return new BoxedValue(number);
        }

    }

    @CoreMethod(names = "float", onSingleton = true, required = 1)
    public abstract static class FloatNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public float foreignBoxedNumber(long value) {
            return value;
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyBignum(value)")
        public float foreignBoxedNumber(DynamicObject value) {
            return (float) Layouts.BIGNUM.getValue(value).doubleValue();
        }

        @Specialization
        public float foreignBoxedNumber(double value) {
            return (float) value;
        }

    }

    @CoreMethod(names = "thread_info", onSingleton = true)
    public abstract static class ThreadInfoNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject threadInfo() {
            return makeStringNode.executeMake(getThreadDebugInfo(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

        @TruffleBoundary
        private String getThreadDebugInfo() {
            return getContext().getThreadManager().getThreadDebugInfo()
                    + getContext().getSafepointManager().getSafepointDebugInfo() + "\n";
        }

    }

    @CoreMethod(names = "dead_block", onSingleton = true)
    public abstract static class DeadBlockNode extends CoreMethodArrayArgumentsNode {

        @SuppressFBWarnings("UW")
        @TruffleBoundary
        @Specialization
        public DynamicObject deadBlock() {
            RubyLanguage.LOGGER.severe("Truffle::Debug.dead_block is being called - will lock up the interpreter");

            final Object monitor = new Object();

            synchronized (monitor) {
                while (true) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
        }

    }

    @CoreMethod(names = "associated", onSingleton = true, required = 1)
    public abstract static class AssociatedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject associated(DynamicObject value,
                                        @Cached("createReadAssociatedNode()") ReadObjectFieldNode readAssociatedNode) {
            Pointer[] associated = (Pointer[]) readAssociatedNode.execute(value);

            if (associated == null) {
                associated = new Pointer[]{};
            }

            final long[] associatedValues = new long[associated.length];

            for (int n = 0; n < associated.length; n++) {
                associatedValues[n] = associated[n].getAddress();
            }

            return Layouts.ARRAY.createArray(getContext().getCoreLibrary().getArrayFactory(), associatedValues, associated.length);
        }

        protected ReadObjectFieldNode createReadAssociatedNode() {
            return ReadObjectFieldNodeGen.create(Layouts.ASSOCIATED_IDENTIFIER, null);
        }

    }

}
