/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.basicobject;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.InstanceExecNodeFactory;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.ReferenceEqualNodeFactory;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;
import org.truffleruby.language.methods.DeclarationContext.SingletonClassOfSelfDefaultDefinee;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.ObjectIDOperations;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;
import org.truffleruby.language.supercall.SuperCallNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.ParserContext;

@CoreClass("BasicObject")
public abstract class BasicObjectNodes {

    @CoreMethod(names = "!")
    public abstract static class NotNode extends UnaryCoreMethodNode {

        @CreateCast("operand")
        public RubyNode createCast(RubyNode operand) {
            return BooleanCastNodeGen.create(operand);
        }

        @Specialization
        public boolean not(boolean value) {
            return !value;
        }

    }

    @CoreMethod(names = "!=", required = 1)
    public abstract static class NotEqualNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode equalNode = CallDispatchHeadNode.create();

        @Specialization
        public boolean equal(VirtualFrame frame, Object a, Object b) {
            return !equalNode.callBoolean(frame, a, "==", b);
        }

    }

    @CoreMethod(names = {"equal?", "=="}, required = 1)
    public abstract static class ReferenceEqualNode extends CoreMethodArrayArgumentsNode {

        public static ReferenceEqualNode create() {
            return ReferenceEqualNodeFactory.create(null);
        }

        public abstract boolean executeReferenceEqual(Object a, Object b);

        @Specialization
        public boolean equal(boolean a, boolean b) {
            return a == b;
        }

        @Specialization
        public boolean equal(int a, int b) {
            return a == b;
        }

        @Specialization
        public boolean equal(long a, long b) {
            return a == b;
        }

        @Specialization
        public boolean equal(double a, double b) {
            return Double.doubleToRawLongBits(a) == Double.doubleToRawLongBits(b);
        }

        @Specialization
        public boolean equal(DynamicObject a, DynamicObject b) {
            return a == b;
        }

        @Specialization(guards = { "isNotDynamicObject(a)", "isNotDynamicObject(b)", "!sameClass(a, b)", "isNotIntLong(a) || isNotIntLong(b)" })
        public boolean equalIncompatiblePrimitiveTypes(Object a, Object b) {
            return false;
        }

        @Specialization(guards = { "isNotDynamicObject(a)", "isNotDynamicObject(b)", "sameClass(a, b)", "isNotIntLongDouble(a) || isNotIntLongDouble(b)" })
        public boolean equalOtherSameClass(Object a, Object b) {
            return a == b;
        }

        @Specialization(guards = "isNotDynamicObject(a)")
        public boolean equal(Object a, DynamicObject b) {
            return false;
        }

        @Specialization(guards = "isNotDynamicObject(b)")
        public boolean equal(DynamicObject a, Object b) {
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
        public long objectIDNil(Object nil) {
            return ObjectIDOperations.NIL;
        }

        @Specialization(guards = "value")
        public long objectIDTrue(boolean value) {
            return ObjectIDOperations.TRUE;
        }

        @Specialization(guards = "!value")
        public long objectIDFalse(boolean value) {
            return ObjectIDOperations.FALSE;
        }

        @Specialization
        public long objectID(int value) {
            return ObjectIDOperations.smallFixnumToID(value);
        }

        @Specialization(rewriteOn = ArithmeticException.class)
        public long objectIDSmallFixnumOverflow(long value) throws ArithmeticException {
            return ObjectIDOperations.smallFixnumToIDOverflow(value);
        }

        @Specialization
        public Object objectID(long value,
                @Cached("createCountingProfile()") ConditionProfile smallProfile) {
            if (smallProfile.profile(ObjectIDOperations.isSmallFixnum(value))) {
                return ObjectIDOperations.smallFixnumToID(value);
            } else {
                return ObjectIDOperations.largeFixnumToID(getContext(), value);
            }
        }

        @Specialization
        public Object objectID(double value) {
            return ObjectIDOperations.floatToID(getContext(), value);
        }

        @Specialization(guards = "!isNil(object)")
        public long objectID(DynamicObject object,
                @Cached("createReadObjectIDNode()") ReadObjectFieldNode readObjectIdNode,
                @Cached("createWriteObjectIDNode()") WriteObjectFieldNode writeObjectIdNode) {
            final long id = (long) readObjectIdNode.execute(object);

            if (id == 0) {
                final long newId = getContext().getObjectSpaceManager().getNextObjectID();
                writeObjectIdNode.write(object, newId);
                return newId;
            }

            return id;
        }

        @Fallback
        public long objectID(Object object) {
            return Integer.toUnsignedLong(hashCode(object));
        }

        @TruffleBoundary
        private int hashCode(Object object) {
            return object.hashCode();
        }

        protected ReadObjectFieldNode createReadObjectIDNode() {
            return ReadObjectFieldNodeGen.create(Layouts.OBJECT_ID_IDENTIFIER, 0L);
        }

        protected WriteObjectFieldNode createWriteObjectIDNode() {
            return WriteObjectFieldNodeGen.create(Layouts.OBJECT_ID_IDENTIFIER);
        }

    }

    @CoreMethod(names = "initialize", needsSelf = false)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject initialize() {
            return nil();
        }

    }

    @CoreMethod(names = "instance_eval", needsBlock = true, optional = 3, lowerFixnum = 3, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class InstanceEvalNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isRubyString(string)", "isRubyString(fileName)" })
        public Object instanceEval(Object receiver, DynamicObject string, DynamicObject fileName, int line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            final Rope code = StringOperations.rope(string);
            final String fileNameString = RopeOperations.decodeRope(StringOperations.rope(fileName));

            final Source source = loadFragment(KernelNodes.EvalNode.offsetSource("instance_eval", RopeOperations.decodeRope(code), fileNameString, line), fileNameString);

            final RubyRootNode rootNode = getContext().getCodeLoader().parse(source, code.getEncoding(), ParserContext.EVAL, null, true, this);
            final DeclarationContext declarationContext = new DeclarationContext(Visibility.PUBLIC, new SingletonClassOfSelfDefaultDefinee(receiver));
            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(ParserContext.EVAL, declarationContext, rootNode, null, receiver);
            return deferredCall.call(callNode);
        }

        @Specialization(guards = { "isRubyString(string)", "isRubyString(fileName)" })
        public Object instanceEval(Object receiver, DynamicObject string, DynamicObject fileName, NotProvided line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            return instanceEval(receiver, string, fileName, 1, block, callNode);
        }

        @Specialization(guards = { "isRubyString(string)" })
        public Object instanceEval(Object receiver, DynamicObject string, NotProvided fileName, NotProvided line, NotProvided block,
                @Cached("create()") IndirectCallNode callNode) {
            return instanceEval(receiver, string, coreStrings().EVAL_FILENAME_STRING.createInstance(), 1, block, callNode);
        }

        @Specialization
        public Object instanceEval(Object receiver, NotProvided string, NotProvided fileName, NotProvided line, DynamicObject block,
                @Cached("create()") InstanceExecNode instanceExecNode) {
            return instanceExecNode.executeInstanceExec(receiver, new Object[]{ receiver }, block);
        }

        private Source loadFragment(String fragment, String name) {
            return Source.newBuilder(fragment).name(name.intern()).mimeType(RubyLanguage.MIME_TYPE).build();
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
        public Object instanceExec(Object receiver, Object[] arguments, DynamicObject block) {
            final DeclarationContext declarationContext = new DeclarationContext(Visibility.PUBLIC, new SingletonClassOfSelfDefaultDefinee(receiver));
            return callBlockNode.executeCallBlock(declarationContext, block, receiver, Layouts.PROC.getBlock(block), arguments);
        }

        @Specialization
        public Object instanceExec(Object receiver, Object[] arguments, NotProvided block) {
            throw new RaiseException(coreExceptions().localJumpError("no block given", this));
        }

    }

    @CoreMethod(names = "method_missing", needsBlock = true, rest = true, optional = 1, visibility = Visibility.PRIVATE)
    public abstract static class MethodMissingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object methodMissingNoName(Object self, NotProvided name, Object[] args, NotProvided block) {
            throw new RaiseException(coreExceptions().argumentError("no id given", this));
        }

        @Specialization
        public Object methodMissingNoBlock(Object self, DynamicObject name, Object[] args, NotProvided block) {
            return methodMissing(self, name, args, null);
        }

        @Specialization
        public Object methodMissingBlock(Object self, DynamicObject name, Object[] args, DynamicObject block) {
            return methodMissing(self, name, args, block);
        }

        private Object methodMissing(Object self, DynamicObject nameObject, Object[] args, DynamicObject block) {
            throw new RaiseException(buildMethodMissingException(self, nameObject, args, block));
        }

        @TruffleBoundary
        private DynamicObject buildMethodMissingException(Object self, DynamicObject nameObject, Object[] args, DynamicObject block) {
            final String name = nameObject.toString();
            final FrameInstance relevantCallerFrame = getRelevantCallerFrame();
            Visibility visibility;

            final DynamicObject formatter;
            if (lastCallWasSuper(relevantCallerFrame)) {
                formatter = ExceptionOperations.getFormatter(ExceptionOperations.SUPER_METHOD_ERROR, getContext());
                return coreExceptions().noMethodError(formatter, self, name, args, this);
            } else if ((visibility = lastCallWasCallingPrivateOrProtectedMethod(self, name, relevantCallerFrame)) != null) {
                if (visibility.isPrivate()) {
                    formatter = ExceptionOperations.getFormatter(ExceptionOperations.PRIVATE_METHOD_ERROR, getContext());
                    return coreExceptions().noMethodError(formatter, self, name, args, this);
                } else {
                    formatter = ExceptionOperations.getFormatter(ExceptionOperations.PROTECTED_METHOD_ERROR, getContext());
                    return coreExceptions().noMethodError(formatter, self, name, args, this);
                }
            } else if (lastCallWasVCall(relevantCallerFrame)) {
                return coreExceptions().nameErrorUndefinedLocalVariableOrMethod(name, self, this);
            } else {
                formatter = ExceptionOperations.getFormatter(ExceptionOperations.NO_METHOD_ERROR, getContext());
                return coreExceptions().noMethodError(formatter, self, name, args, this);
            }
        }

        private FrameInstance getRelevantCallerFrame() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();
                if (callNode == null) {
                    // skip current frame
                    return null;
                }

                final SuperCallNode superCallNode = NodeUtil.findParent(callNode, SuperCallNode.class);
                final Frame frame = frameInstance.getFrame(FrameInstance.FrameAccess.READ_ONLY);

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

                return frameInstance;
            });
        }

        private boolean lastCallWasSuper(FrameInstance callerFrame) {
            final SuperCallNode superCallNode = NodeUtil.findParent(callerFrame.getCallNode(), SuperCallNode.class);
            return superCallNode != null;
        }

        /**
         * See {@link org.truffleruby.language.dispatch.DispatchNode#lookup}.
         * The only way to fail if method is not null and not undefined is visibility.
         */
        private Visibility lastCallWasCallingPrivateOrProtectedMethod(Object self, String name, FrameInstance callerFrame) {
            final DeclarationContext declarationContext = RubyArguments.tryGetDeclarationContext(callerFrame.getFrame(FrameAccess.READ_ONLY));
            final InternalMethod method = ModuleOperations.lookupMethodUncached(coreLibrary().getMetaClass(self), name, declarationContext);
            if (method != null && !method.isUndefined()) {
                assert method.getVisibility().isPrivate() || method.getVisibility().isProtected();
                return method.getVisibility();
            }
            return null;
        }

        private boolean lastCallWasVCall(FrameInstance callerFrame) {
            final RubyCallNode callNode = NodeUtil.findParent(callerFrame.getCallNode(), RubyCallNode.class);
            return callNode != null && callNode.isVCall();
        }

    }

    @CoreMethod(names = "__send__", needsBlock = true, rest = true, required = 1)
    public abstract static class SendNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();

        @Specialization
        public Object send(VirtualFrame frame, Object self, Object name, Object[] args, NotProvided block) {
            return send(frame, self, name, args, (DynamicObject) null);
        }

        @Specialization
        public Object send(VirtualFrame frame, Object self, Object name, Object[] args, DynamicObject block) {
            return dispatchNode.callWithBlock(frame, self, name, block, args);
        }

    }

    // MRI names it the "allocator function" and it's associated per class and follows the ancestor
    // chain. We use a normal Ruby method, different that Class#allocate as Class#allocate
    // must be able to instantiate any Ruby object and should not be overridden.
    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass);
        }

    }

    @NonStandard
    @CoreMethod(names = "internal_allocate", constructor = true)
    public abstract static class InternalAllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject internal_allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass);
        }

    }

}
