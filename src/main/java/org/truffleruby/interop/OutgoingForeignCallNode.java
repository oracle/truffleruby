/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.util.Arrays;

import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.methods.ExceptionTranslatingNode;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;

@NodeChildren({
        @NodeChild("receiver"),
        @NodeChild("args")
})
public abstract class OutgoingForeignCallNode extends RubyNode {

    @Child private ExceptionTranslatingNode exceptionTranslatingNode;

    private final String name;
    private final BranchProfile errorProfile = BranchProfile.create();

    public OutgoingForeignCallNode(String name) {
        this.name = name;
        this.exceptionTranslatingNode = new ExceptionTranslatingNode(null, UnsupportedOperationBehavior.TYPE_ERROR);
    }

    public abstract Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args);

    @Specialization
    public Object callCached(VirtualFrame frame, TruffleObject receiver, Object[] args,
            @Cached("createHelperNode()") OutgoingNode outgoingNode) {
        try {
            return outgoingNode.executeCall(frame, receiver, args);
        } catch (Throwable t) {
            errorProfile.enter();
            throw exceptionTranslatingNode.translate(t);
        }
    }

    @TruffleBoundary
    protected OutgoingNode createHelperNode() {
        if (name.equals("[]")) {
            return new IndexReadOutgoingNode();
        } else if (name.equals("[]=")) {
            return new IndexWriteOutgoingNode();
        } else if (name.equals("call")) {
            return new CallOutgoingNode();
        } else if (name.equals("new")) {
            return new NewOutgoingNode();
        } else if (name.equals("to_a") || name.equals("to_ary")) {
            return new ToAOutgoingNode();
        } else if (name.equals("respond_to?")) {
            return new RespondToOutgoingNode();
        } else if (name.equals("inspect")) {
            return new InspectOutgoingNode();
        } else if (name.equals("__send__")) {
            return new SendOutgoingNode();
        } else if (name.equals("nil?")) {
            return new IsNilOutgoingNode();
        } else if (name.equals("equal?")) {
            return new IsReferenceEqualOutgoingNode();
        } else if (name.equals("delete")
                || name.equals("size")
                || name.equals("keys")
                || name.equals("class")
                || name.equals("to_s")
                || name.equals("to_str")
                || name.equals("is_a?")) {
            final int expectedArgsLength;

            switch (name) {
                case "delete":
                case "is_a?":
                    expectedArgsLength = 1;
                    break;
                case "size":
                case "keys":
                case "class":
                case "to_s":
                case "to_str":
                    expectedArgsLength = 0;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return new SpecialFormOutgoingNode(getContext().getSymbolTable().getSymbol(name), expectedArgsLength);
        } else if (isOperatorMethod(name)) {
            return new UnboxForOperatorAndReDispatchOutgoingNode(name);
        } else {
            return new InvokeOutgoingNode(name);
        }
    }

    @TruffleBoundary
    private static boolean isOperatorMethod(String name) {
        return !name.isEmpty() && !Character.isLetter(name.charAt(0));
    }

    protected int getCacheLimit() {
        return getContext().getOptions().INTEROP_EXECUTE_CACHE;
    }

    protected abstract static class OutgoingNode extends Node {

        protected final BranchProfile argumentErrorProfile = BranchProfile.create();
        protected final BranchProfile exceptionProfile = BranchProfile.create();
        protected final BranchProfile unknownIdentifierProfile = BranchProfile.create();

        public abstract Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args);

    }

    protected class IndexReadOutgoingNode extends OutgoingNode {

        @Child private Node node;
        @Child private RubyToForeignNode rubyToForeignNode = RubyToForeignNode.create();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        public IndexReadOutgoingNode() {
            node = Message.READ.createNode();
        }

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 1, this));
            }

            final Object name = rubyToForeignNode.executeConvert(args[0]);
            final Object foreign;
            try {
                foreign = ForeignAccess.sendRead(node, receiver, name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }

    protected class IndexWriteOutgoingNode extends OutgoingNode {

        @Child private Node node;
        @Child private RubyToForeignNode identifierToForeignNode = RubyToForeignNode.create();
        @Child private RubyToForeignNode valueToForeignNode = RubyToForeignNode.create();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        public IndexWriteOutgoingNode() {
            node = Message.WRITE.createNode();
        }

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 2) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 2, this));
            }

            final Object foreign;

            try {
                foreign = ForeignAccess.sendWrite(
                        node,
                        receiver,
                        identifierToForeignNode.executeConvert(args[0]),
                        valueToForeignNode.executeConvert(args[1]));
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException | UnsupportedTypeException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }

    protected class CallOutgoingNode extends OutgoingNode {

        @Child private Node node = Message.EXECUTE.createNode();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();
        @Child private RubyToForeignArgumentsNode rubyToForeignArgumentsNode = RubyToForeignArgumentsNode.create();

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            final Object foreign;

            try {
                foreign = ForeignAccess.sendExecute(
                        node,
                        receiver,
                        rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }

    protected class SendOutgoingNode extends OutgoingNode {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createReturnMissing();

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length < 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 1, this));
            }

            final Object name = args[0];
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);

            final Object result = dispatchNode.dispatch(frame, receiver, name, null, sendArgs);

            assert result != DispatchNode.MISSING;

            return result;
        }

    }

    protected class NewOutgoingNode extends OutgoingNode {

        @Child private Node node = Message.NEW.createNode();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();
        @Child private RubyToForeignArgumentsNode rubyToForeignArgumentsNode = RubyToForeignArgumentsNode.create();

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            final Object foreign;

            try {
                foreign = ForeignAccess.sendNew(
                        node,
                        receiver,
                        rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }

    protected class ToAOutgoingNode extends OutgoingNode {

        @Child private CallDispatchHeadNode callToArray = CallDispatchHeadNode.createPrivate();

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 0) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 0, this));
            }

            return callToArray.call(coreLibrary().getTruffleInteropModule(), "to_array", receiver);
        }

    }

    protected class RespondToOutgoingNode extends OutgoingNode {

        @Child private CallDispatchHeadNode callRespondTo = CallDispatchHeadNode.createPrivate();

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 1, this));
            }

            return callRespondTo.call(coreLibrary().getTruffleInteropModule(), "respond_to?", receiver, args[0]);
        }

    }

    protected class SpecialFormOutgoingNode extends OutgoingNode {

        private final DynamicObject name;
        private final int argsLength;

        @Child private CallDispatchHeadNode callSpecialForm = CallDispatchHeadNode.createPrivate();

        public SpecialFormOutgoingNode(DynamicObject name, int argsLength) {
            this.name = name;
            this.argsLength = argsLength;
        }

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != argsLength) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, this.argsLength, this));
            }

            final Object[] prependedArgs = new Object[args.length + 2];
            prependedArgs[0] = receiver;
            prependedArgs[1] = name;
            System.arraycopy(args, 0, prependedArgs, 2, args.length);

            return callSpecialForm.call(coreLibrary().getTruffleInteropModule(), "special_form", prependedArgs);
        }

    }

    protected class InspectOutgoingNode extends OutgoingNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 0) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 0, this));
            }

            return makeStringNode.executeMake(inspect(receiver), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

        @TruffleBoundary
        private String inspect(TruffleObject receiver) {
            return String.format("#<Truffle::Interop::Foreign@%x>", System.identityHashCode(receiver));
        }

    }

    protected class IsNilOutgoingNode extends OutgoingNode {

        @Child private Node node;

        public IsNilOutgoingNode() {
            node = Message.IS_NULL.createNode();
        }

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 0) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 0, this));
            }

            return ForeignAccess.sendIsNull(node, receiver);
        }

    }

    protected class IsReferenceEqualOutgoingNode extends OutgoingNode {

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(args.length, 1, this));
            }

            final TruffleObject a = receiver;

            if (!(args[0] instanceof TruffleObject)) {
                return false;
            }

            final TruffleObject b = (TruffleObject) args[0];

            if (getContext().getEnv().isHostObject(a) && getContext().getEnv().isHostObject(b)) {
                return getContext().getEnv().asHostObject(a) == getContext().getEnv().asHostObject(b);
            } else {
                return a == b;
            }
        }

    }

    protected class UnboxForOperatorAndReDispatchOutgoingNode extends OutgoingNode {

        private final String name;

        @Child private Node isBoxedNode = Message.IS_BOXED.createNode();
        @Child private Node unboxNode;
        @Child private ForeignToRubyNode foreignToRubyNode;
        @Child private CallDispatchHeadNode redispatchNode;
        @Child private InvokeOutgoingNode invokeOutgoingNode;

        public UnboxForOperatorAndReDispatchOutgoingNode(String name) {
            this.name = name;
        }

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (ForeignAccess.sendIsBoxed(isBoxedNode, receiver)) { // implicit profiling as a result of lazy nodes
                final Object unboxedReceiver = convertToRuby(unbox(receiver));
                return reDispatch(frame, unboxedReceiver, args);
            } else {
                return invoke(frame, receiver, args);
            }
        }

        private Object unbox(TruffleObject receiver) {
            if (unboxNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                unboxNode = insert(Message.UNBOX.createNode());
            }

            try {
                return ForeignAccess.sendUnbox(unboxNode, receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        private Object convertToRuby(final Object unboxedReceiver) {
            if (foreignToRubyNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                foreignToRubyNode = insert(ForeignToRubyNode.create());
            }

            return foreignToRubyNode.executeConvert(unboxedReceiver);
        }

        private Object reDispatch(VirtualFrame frame, Object receiver, Object[] args) {
            if (redispatchNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                // ignore visibility like ForeignInvokeNode
                redispatchNode = insert(CallDispatchHeadNode.createPrivate());
            }

            return redispatchNode.call(receiver, name, args);
        }

        private Object invoke(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            if (invokeOutgoingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                invokeOutgoingNode = insert(new InvokeOutgoingNode(name));
            }

            return invokeOutgoingNode.executeCall(frame, receiver, args);
        }

    }

    protected class InvokeOutgoingNode extends OutgoingNode {

        private final String name;

        @Child private Node node = Message.INVOKE.createNode();
        @Child private RubyToForeignArgumentsNode rubyToForeignArgumentsNode = RubyToForeignArgumentsNode.create();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        public InvokeOutgoingNode(String name) {
            this.name = name;
        }

        @Override
        public Object executeCall(VirtualFrame frame, TruffleObject receiver, Object[] args) {
            final Object[] arguments = rubyToForeignArgumentsNode.executeConvert(args);

            final Object foreign;
            try {
                foreign = ForeignAccess.sendInvoke(
                        node,
                        receiver,
                        name,
                        arguments);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().noMethodErrorUnknownIdentifier(receiver, name, args, e, this));
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }


}
