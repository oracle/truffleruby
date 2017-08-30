/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.debug;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.NodeUtil;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.InternalMethod;
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

    @CoreMethod(names = "break_handle", onSingleton = true, required = 2, needsBlock = true, lowerFixnum = 2)
    public abstract static class BreakNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(file)")
        public DynamicObject setBreak(DynamicObject file, int line, final DynamicObject block) {
            final String fileString = StringOperations.decodeUTF8(file);

            final SourceSectionFilter filter = SourceSectionFilter.newBuilder()
                    .mimeTypeIs(RubyLanguage.MIME_TYPE)
                    .sourceIs(source -> source != null && source.getPath() != null && source.getPath().equals(fileString))
                    .lineIs(line)
                    .tagIs(StandardTags.StatementTag.class)
                    .build();

            final EventBinding<?> breakpoint = getContext().getInstrumenter().attachFactory(filter,
                    eventContext -> new ExecutionEventNode() {

                        @Child private YieldNode yieldNode = new YieldNode();

                        @Override
                        protected void onEnter(VirtualFrame frame) {
                            yieldNode.dispatch(block, BindingNodes.createBinding(getContext(), frame.materialize()));
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

    @CoreMethod(names = "java_to_string", onSingleton = true, required = 1)
    public abstract static class JavaToStringNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject javaToString(Object value) {
            return makeStringNode.executeMake(String.valueOf(value), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "print_backtrace", onSingleton = true)
    public abstract static class PrintBacktraceNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject printBacktrace() {
            getContext().getCallStack().printBacktrace(this);
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
            if (callTarget instanceof RootCallTarget) {
                return ast((RootCallTarget) callTarget);
            } else {
                throw new RaiseException(getContext().getCoreExceptions().internalError("call target is not a root call target", this));
            }
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
            if (callTarget instanceof RootCallTarget) {
                printAst((RootCallTarget) callTarget);
            } else {
                throw new RaiseException(getContext().getCoreExceptions().internalError("call target is not a root call target", this));
            }
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
                @Cached("create()") NameToJavaStringNode toJavaStringNode) {
            warning(toJavaStringNode.executeToJavaString(frame, value));
            return nil();
        }

        @TruffleBoundary
        public static void warning(String message) {
            Log.LOGGER.warning(message);
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
            throw new RuntimeException(message.toString());
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

    @CoreMethod(names = "foreign_object", onSingleton = true)
    public abstract static class ForeignObjectNode extends CoreMethodArrayArgumentsNode {

        private static class ForeignObject implements TruffleObject {

            @Override
            public ForeignAccess getForeignAccess() {
                throw new UnsupportedOperationException();
            }
            
        }

        @Specialization
        public Object foreignObject() {
            return new ForeignObject();
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

        @TruffleBoundary
        @Specialization
        public DynamicObject deadBlock() {
            Log.LOGGER.severe("Truffle::Debug.dead_block is being called - will lock up the interpreter");

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

}
