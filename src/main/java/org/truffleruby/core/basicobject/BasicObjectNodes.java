/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.basicobject;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.InstanceExecNodeFactory;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.ReferenceEqualNodeFactory;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.eval.CreateEvalSourceNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.DeclarationContext.SingletonClassOfSelfDefaultDefinee;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.ObjectIDOperations;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.supercall.SuperCallNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreClass("BasicObject")
public abstract class BasicObjectNodes {

    @CoreMethod(names = "!")
    public abstract static class NotNode extends UnaryCoreMethodNode {

        @Specialization
        protected boolean not(Object value,
                @Cached BooleanCastNode cast) {
            return !cast.executeToBoolean(value);
        }

    }

    @CoreMethod(names = "!=", required = 1)
    public abstract static class NotEqualNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode equalNode = CallDispatchHeadNode.createPrivate();
        @Child private BooleanCastNode booleanCastNode = BooleanCastNode.create();

        @Specialization
        protected boolean equal(VirtualFrame frame, Object a, Object b) {
            return !booleanCastNode.executeToBoolean(equalNode.call(a, "==", b));
        }

    }

    @CoreMethod(names = { "equal?", "==" }, required = 1)
    public abstract static class ReferenceEqualNode extends CoreMethodArrayArgumentsNode {

        public static ReferenceEqualNode create() {
            return ReferenceEqualNodeFactory.create(null);
        }

        public abstract boolean executeReferenceEqual(Object a, Object b);

        @Specialization
        protected boolean equal(boolean a, boolean b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(int a, int b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(long a, long b) {
            return a == b;
        }

        @Specialization
        protected boolean equal(double a, double b) {
            return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
        }

        @Specialization
        protected boolean equal(DynamicObject a, DynamicObject b) {
            return a == b;
        }

        @Specialization(
                guards = {
                        "isNotDynamicObject(a)",
                        "isNotDynamicObject(b)",
                        "!sameClass(a, b)",
                        "isNotIntLong(a) || isNotIntLong(b)" })
        protected boolean equalIncompatiblePrimitiveTypes(Object a, Object b) {
            return false;
        }

        @Specialization(
                guards = {
                        "isNotDynamicObject(a)",
                        "isNotDynamicObject(b)",
                        "sameClass(a, b)",
                        "isNotIntLongDouble(a) || isNotIntLongDouble(b)" })
        protected boolean equalOtherSameClass(Object a, Object b) {
            return a == b;
        }

        @Specialization(guards = "isNotDynamicObject(a)")
        protected boolean equal(Object a, DynamicObject b) {
            return false;
        }

        @Specialization(guards = "isNotDynamicObject(b)")
        protected boolean equal(DynamicObject a, Object b) {
            return false;
        }

        protected boolean isNotDynamicObject(Object value) {
            return !(value instanceof DynamicObject);
        }

        protected boolean sameClass(Object a, Object b) {
            return a.getClass() == b.getClass();
        }

        protected boolean isNotIntLong(Object v) {
            return !(v instanceof Integer) && !(v instanceof Long);
        }

        protected boolean isNotIntLongDouble(Object v) {
            return !(v instanceof Integer) && !(v instanceof Long) && !(v instanceof Double);
        }

    }

    @CoreMethod(names = "__id__")
    public abstract static class ObjectIDNode extends CoreMethodArrayArgumentsNode {

        public abstract Object executeObjectID(Object value);

        @Specialization(guards = "isNil(nil)")
        protected long objectIDNil(Object nil) {
            return ObjectIDOperations.NIL;
        }

        @Specialization(guards = "value")
        protected long objectIDTrue(boolean value) {
            return ObjectIDOperations.TRUE;
        }

        @Specialization(guards = "!value")
        protected long objectIDFalse(boolean value) {
            return ObjectIDOperations.FALSE;
        }

        @Specialization
        protected long objectID(int value) {
            return ObjectIDOperations.smallFixnumToID(value);
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        protected long objectIDSmallFixnumOverflow(long value) throws ArithmeticException {
            return ObjectIDOperations.smallFixnumToIDOverflow(value);
        }

        @Specialization
        protected Object objectID(long value,
                @Cached("createCountingProfile()") ConditionProfile smallProfile) {
            if (smallProfile.profile(ObjectIDOperations.isSmallFixnum(value))) {
                return ObjectIDOperations.smallFixnumToID(value);
            } else {
                return ObjectIDOperations.largeFixnumToID(getContext(), value);
            }
        }

        @Specialization
        protected Object objectID(double value) {
            return ObjectIDOperations.floatToID(getContext(), value);
        }

        @Specialization(guards = "!isNil(object)")
        protected long objectID(DynamicObject object,
                @Cached ReadObjectFieldNode readObjectIdNode,
                @Cached WriteObjectFieldNode writeObjectIdNode) {
            final long id = (long) readObjectIdNode.execute(object, Layouts.OBJECT_ID_IDENTIFIER, 0L);

            if (id == 0) {
                final long newId = getContext().getObjectSpaceManager().getNextObjectID();
                writeObjectIdNode.write(object, Layouts.OBJECT_ID_IDENTIFIER, newId);
                return newId;
            }

            return id;
        }

        @Fallback
        protected long objectID(Object object) {
            return Integer.toUnsignedLong(hashCode(object));
        }

        @TruffleBoundary
        private int hashCode(Object object) {
            return object.hashCode();
        }
    }

    @CoreMethod(names = "initialize", needsSelf = false)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject initialize() {
            return nil();
        }

    }

    @CoreMethod(
            names = "instance_eval",
            needsBlock = true,
            optional = 3,
            lowerFixnum = 3,
            unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class InstanceEvalNode extends CoreMethodArrayArgumentsNode {

        @Child private CreateEvalSourceNode createEvalSourceNode = new CreateEvalSourceNode();

        @Specialization(guards = { "isRubyString(string)", "isRubyString(fileName)" })
        protected Object instanceEval(VirtualFrame frame, Object receiver, DynamicObject string, DynamicObject fileName,
                int line, NotProvided block,
                @Cached ReadCallerFrameNode callerFrameNode,
                @Cached IndirectCallNode callNode) {
            final MaterializedFrame callerFrame = callerFrameNode.execute(frame);

            return instanceEvalHelper(callerFrame, receiver, string, fileName, line, callNode);
        }

        @Specialization(guards = { "isRubyString(string)", "isRubyString(fileName)" })
        protected Object instanceEval(VirtualFrame frame, Object receiver, DynamicObject string, DynamicObject fileName,
                NotProvided line, NotProvided block,
                @Cached ReadCallerFrameNode callerFrameNode,
                @Cached IndirectCallNode callNode) {
            final MaterializedFrame callerFrame = callerFrameNode.execute(frame);

            return instanceEvalHelper(callerFrame, receiver, string, fileName, 1, callNode);
        }

        @Specialization(guards = { "isRubyString(string)" })
        protected Object instanceEval(VirtualFrame frame, Object receiver, DynamicObject string, NotProvided fileName,
                NotProvided line, NotProvided block,
                @Cached ReadCallerFrameNode callerFrameNode,
                @Cached IndirectCallNode callNode) {
            final MaterializedFrame callerFrame = callerFrameNode.execute(frame);

            return instanceEvalHelper(
                    callerFrame,
                    receiver,
                    string,
                    coreStrings().EVAL_FILENAME_STRING.createInstance(),
                    1,
                    callNode);
        }

        @Specialization
        protected Object instanceEval(Object receiver, NotProvided string, NotProvided fileName, NotProvided line,
                DynamicObject block,
                @Cached InstanceExecNode instanceExecNode) {
            return instanceExecNode.executeInstanceExec(receiver, new Object[]{ receiver }, block);
        }

        @TruffleBoundary
        private Object instanceEvalHelper(MaterializedFrame callerFrame, Object receiver, DynamicObject string,
                DynamicObject fileName, int line, IndirectCallNode callNode) {
            final String fileNameString = RopeOperations.decodeRope(StringOperations.rope(fileName));

            final RubySource source = createEvalSourceNode
                    .createEvalSource(StringOperations.rope(string), "instance_eval", fileNameString, line);

            final RubyRootNode rootNode = getContext().getCodeLoader().parse(
                    source,
                    ParserContext.EVAL,
                    callerFrame,
                    null,
                    true,
                    this);

            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new SingletonClassOfSelfDefaultDefinee(receiver));

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    ParserContext.EVAL,
                    declarationContext,
                    rootNode,
                    callerFrame,
                    receiver);

            return deferredCall.call(callNode);
        }

    }

    @CoreMethod(names = "instance_exec", needsBlock = true, rest = true)
    public abstract static class InstanceExecNode extends CoreMethodArrayArgumentsNode {

        public static InstanceExecNode create() {
            return InstanceExecNodeFactory.create(null);
        }

        @Child private CallBlockNode callBlockNode = CallBlockNode.create();

        public abstract Object executeInstanceExec(Object self, Object[] args, Object block);

        @Specialization
        protected Object instanceExec(Object receiver, Object[] arguments, DynamicObject block) {
            final DeclarationContext declarationContext = new DeclarationContext(
                    Visibility.PUBLIC,
                    new SingletonClassOfSelfDefaultDefinee(receiver));
            return callBlockNode
                    .executeCallBlock(declarationContext, block, receiver, Layouts.PROC.getBlock(block), arguments);
        }

        @Specialization
        protected Object instanceExec(Object receiver, Object[] arguments, NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().localJumpError("no block given", this));
        }

    }

    @CoreMethod(names = "method_missing", needsBlock = true, rest = true, optional = 1, visibility = Visibility.PRIVATE)
    public abstract static class MethodMissingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object methodMissingNoName(Object self, NotProvided name, Object[] args, NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("no id given", this));
        }

        @Specialization
        protected Object methodMissingNoBlock(Object self, DynamicObject name, Object[] args, NotProvided block) {
            return methodMissing(self, name, args, null);
        }

        @Specialization
        protected Object methodMissingBlock(Object self, DynamicObject name, Object[] args, DynamicObject block) {
            return methodMissing(self, name, args, block);
        }

        private Object methodMissing(Object self, DynamicObject nameObject, Object[] args, DynamicObject block) {
            throw new RaiseException(getContext(), buildMethodMissingException(self, nameObject, args, block));
        }

        private static class FrameAndCallNode {
            final Frame frame;
            final Node callNode;

            private FrameAndCallNode(Frame frame, Node callNode) {
                this.frame = frame;
                this.callNode = callNode;
            }
        }

        @TruffleBoundary
        private DynamicObject buildMethodMissingException(Object self, DynamicObject nameObject, Object[] args,
                DynamicObject block) {
            final String name = nameObject.toString();
            final FrameAndCallNode relevantCallerFrame = getRelevantCallerFrame();
            Visibility visibility;

            final DynamicObject formatter;
            if (lastCallWasSuper(relevantCallerFrame)) {
                formatter = ExceptionOperations.getFormatter(ExceptionOperations.SUPER_METHOD_ERROR, getContext());
                return coreExceptions().noMethodErrorFromMethodMissing(formatter, self, name, args, this);
            } else if ((visibility = lastCallWasCallingPrivateOrProtectedMethod(
                    self,
                    name,
                    relevantCallerFrame)) != null) {
                if (visibility.isPrivate()) {
                    formatter = ExceptionOperations
                            .getFormatter(ExceptionOperations.PRIVATE_METHOD_ERROR, getContext());
                    return coreExceptions().noMethodErrorFromMethodMissing(formatter, self, name, args, this);
                } else {
                    formatter = ExceptionOperations
                            .getFormatter(ExceptionOperations.PROTECTED_METHOD_ERROR, getContext());
                    return coreExceptions().noMethodErrorFromMethodMissing(formatter, self, name, args, this);
                }
            } else if (lastCallWasVCall(relevantCallerFrame)) {
                formatter = ExceptionOperations
                        .getFormatter(ExceptionOperations.NO_LOCAL_VARIABLE_OR_METHOD_ERROR, getContext());
                return coreExceptions().nameErrorFromMethodMissing(formatter, self, name, this);
            } else {
                formatter = ExceptionOperations.getFormatter(ExceptionOperations.NO_METHOD_ERROR, getContext());
                return coreExceptions().noMethodErrorFromMethodMissing(formatter, self, name, args, this);
            }
        }

        private FrameAndCallNode getRelevantCallerFrame() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();
                if (callNode == null) {
                    // skip current frame
                    return null;
                }

                final SuperCallNode superCallNode = NodeUtil.findParent(callNode, SuperCallNode.class);
                final Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);

                final InternalMethod method = RubyArguments.tryGetMethod(frame);

                final String superMethodName;

                if (method == null) {
                    superMethodName = "(unknown)";
                } else {
                    superMethodName = method.getName();
                }

                if (superCallNode != null && superMethodName.equals("method_missing")) {
                    // skip super calls of method_missing itself
                    return null;
                }

                return new FrameAndCallNode(frame, callNode);
            });
        }

        private boolean lastCallWasSuper(FrameAndCallNode callerFrame) {
            final SuperCallNode superCallNode = NodeUtil.findParent(callerFrame.callNode, SuperCallNode.class);
            return superCallNode != null;
        }

        /**
         * See {@link org.truffleruby.language.dispatch.DispatchNode#lookup}.
         * The only way to fail if method is not null and not undefined is visibility.
         */
        private Visibility lastCallWasCallingPrivateOrProtectedMethod(Object self, String name,
                FrameAndCallNode callerFrame) {
            final DeclarationContext declarationContext = RubyArguments.tryGetDeclarationContext(callerFrame.frame);
            final InternalMethod method = ModuleOperations
                    .lookupMethodUncached(coreLibrary().getMetaClass(self), name, declarationContext);
            if (method != null && !method.isUndefined()) {
                assert method.getVisibility().isPrivate() || method.getVisibility().isProtected();
                return method.getVisibility();
            }
            return null;
        }

        private boolean lastCallWasVCall(FrameAndCallNode callerFrame) {
            final RubyCallNode callNode = NodeUtil.findParent(callerFrame.callNode, RubyCallNode.class);
            return callNode != null && callNode.isVCall();
        }

    }

    @CoreMethod(names = "__send__", needsBlock = true, rest = true, required = 1)
    public abstract static class SendNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();
        @Child private ReadCallerFrameNode readCallerFrame = ReadCallerFrameNode.create();

        @Specialization
        protected Object send(VirtualFrame frame, Object self, Object name, Object[] args, NotProvided block) {
            return send(frame, self, name, args, (DynamicObject) null);
        }

        @Specialization
        protected Object send(VirtualFrame frame, Object self, Object name, Object[] args, DynamicObject block) {
            DeclarationContext context = RubyArguments.getDeclarationContext(readCallerFrame.execute(frame));
            RubyArguments.setDeclarationContext(frame, context);

            return dispatchNode.dispatch(frame, self, name, block, args);
        }

    }

    // MRI names it the "allocator function" and it's associated per class and follows the ancestor
    // chain. We use a normal Ruby method, different that Class#allocate as Class#allocate
    // must be able to instantiate any Ruby object and should not be overridden.
    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass);
        }

    }

}
