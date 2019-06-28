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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.methods.ExceptionTranslatingNode;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

public abstract class OutgoingForeignCallNode extends RubyBaseNode {

    @Child private ExceptionTranslatingNode exceptionTranslatingNode;

    private final String name;
    private final BranchProfile errorProfile = BranchProfile.create();

    public OutgoingForeignCallNode(String name) {
        this.name = name;
        this.exceptionTranslatingNode = new ExceptionTranslatingNode(null, UnsupportedOperationBehavior.TYPE_ERROR);
    }

    public abstract Object executeCall(TruffleObject receiver, Object[] args);

    @Specialization
    public Object callCached(
            TruffleObject receiver, Object[] args,
            @Cached("createHelperNode()") OutgoingNode outgoingNode) {
        try {
            return outgoingNode.executeCall(receiver, args);
        } catch (Throwable t) {
            errorProfile.enter();
            throw exceptionTranslatingNode.translate(t);
        }
    }

    @TruffleBoundary
    protected OutgoingNode createHelperNode() {
        // TODO (pitr-ch 01-Apr-2019): support to_int with new interop
        if (name.equals("[]")) {
            return OutgoingForeignCallNodeGen.IndexReadOutgoingNodeGen.create();
        } else if (name.equals("[]=")) {
            return OutgoingForeignCallNodeGen.IndexWriteOutgoingNodeGen.create();
        } else if (name.equals("call")) {
            return OutgoingForeignCallNodeGen.CallOutgoingNodeGen.create();
        } else if (name.equals("new")) {
            return OutgoingForeignCallNodeGen.NewOutgoingNodeGen.create();
        } else if (name.equals("to_a") || name.equals("to_ary")) {
            return new ToAOutgoingNode();
        } else if (name.equals("respond_to?")) {
            return new RespondToOutgoingNode();
        } else if (name.equals("__send__")) {
            return new SendOutgoingNode();
        } else if (name.equals("nil?")) {
            return OutgoingForeignCallNodeGen.IsNilOutgoingNodeGen.create();
        } else if (name.equals("equal?")) {
            return new IsReferenceEqualOutgoingNode();
        } else if (name.equals("delete")
                || name.equals("size")
                || name.equals("keys")
                || name.equals("class")
                || name.equals("inspect")
                || name.equals("to_s")
                || name.equals("to_str")
                || name.equals("is_a?")
                || name.equals("kind_of?")) {
            final int expectedArgsLength;

            switch (name) {
                case "delete":
                case "is_a?":
                case "kind_of?":
                    expectedArgsLength = 1;
                    break;
                case "size":
                case "keys":
                case "class":
                case "inspect":
                case "to_s":
                case "to_str":
                    expectedArgsLength = 0;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            return new SpecialFormOutgoingNode(getContext().getSymbolTable().getSymbol(name), expectedArgsLength);
        } else if (isOperatorMethod(name)) {
            return OutgoingForeignCallNodeGen.UnboxForOperatorAndReDispatchOutgoingNodeGen.create(name);
        } else {
            return OutgoingForeignCallNodeGen.InvokeOutgoingNodeGen.create(name);
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

        public abstract Object executeCall(TruffleObject receiver, Object[] args);

        protected int getCacheLimit() {
            // TODO (pitr-ch 30-Mar-2019): is usage of RubyLanguage.getCurrentContext here ok?
            //  Could it break when shared with more contexts?
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }

    }

    public abstract static class IndexReadOutgoingNode extends OutgoingNode {
        @Specialization(guards = "args.length == 1")
        protected Object call(
                TruffleObject receiver, Object[] args,
                @Cached InteropNodes.ReadNode readNode) {
            return readNode.execute(receiver, args[0]);
        }

        @Specialization(guards = "args.length != 1")
        protected Object callWithBadArguments(
                TruffleObject receiver,
                Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
            throw new RaiseException(
                    rubyContext,
                    rubyContext.getCoreExceptions().argumentError(args.length, 1, this));
        }
    }

    protected abstract static class IndexWriteOutgoingNode extends OutgoingNode {

        @Specialization(guards = "args.length == 2")
        protected Object call(
                TruffleObject receiver, Object[] args,
                @Cached InteropNodes.WriteNode writeNode) {
            return writeNode.execute(receiver, args[0], args[1]);
        }

        @Specialization(guards = "args.length != 2")
        protected Object callWithBadArguments(
                TruffleObject receiver,
                Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
            throw new RaiseException(
                    rubyContext,
                    rubyContext.getCoreExceptions().argumentError(args.length, 2, this));
        }
    }

    protected abstract static class CallOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object call(
                TruffleObject receiver,
                Object[] args,
                // TODO (pitr-ch 29-Mar-2019): use this node directly instead?
                @Cached InteropNodes.ExecuteNode executeNode) {
            return executeNode.execute(receiver, args);
        }
    }

    protected class SendOutgoingNode extends OutgoingNode {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createReturnMissing();

        @Override
        public Object executeCall(TruffleObject receiver, Object[] args) {
            if (args.length < 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(args.length, 1, this));
            }

            final Object name = args[0];
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);

            final Object result = dispatchNode.dispatch(null, receiver, name, null, sendArgs);

            assert result != DispatchNode.MISSING;

            return result;
        }

    }

    protected abstract static class NewOutgoingNode extends OutgoingNode {
        @Specialization
        public Object call(
                TruffleObject receiver,
                Object[] args,
                // TODO (pitr-ch 29-Mar-2019): use this node directly?
                @Cached InteropNodes.NewNode newNode) {
            return newNode.execute(receiver, args);
        }
    }

    protected class ToAOutgoingNode extends OutgoingNode {

        @Child private CallDispatchHeadNode callToArray = CallDispatchHeadNode.createPrivate();

        @Override
        public Object executeCall(TruffleObject receiver, Object[] args) {
            if (args.length != 0) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(args.length, 0, this));
            }

            return callToArray.call(coreLibrary().getTruffleInteropModule(), "to_array", receiver);
        }

    }

    protected class RespondToOutgoingNode extends OutgoingNode {

        @Child private CallDispatchHeadNode callRespondTo = CallDispatchHeadNode.createPrivate();

        @Override
        public Object executeCall(TruffleObject receiver, Object[] args) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(args.length, 1, this));
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
        public Object executeCall(TruffleObject receiver, Object[] args) {
            if (args.length != argsLength) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(args.length, this.argsLength, this));
            }

            final Object[] prependedArgs = new Object[args.length + 2];
            prependedArgs[0] = receiver;
            prependedArgs[1] = name;
            System.arraycopy(args, 0, prependedArgs, 2, args.length);

            return callSpecialForm.call(coreLibrary().getTruffleInteropModule(), "special_form", prependedArgs);
        }

    }

    protected abstract static class IsNilOutgoingNode extends OutgoingNode {

        @Specialization(guards = "args.length == 0")
        protected Object call(
                TruffleObject receiver, Object[] args,
                @Cached InteropNodes.NullNode nullNode) {
            return nullNode.execute(receiver);
        }

        @Specialization(guards = "args.length != 0")
        protected Object callWithBadArguments(
                TruffleObject receiver,
                Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
            throw new RaiseException(
                    rubyContext,
                    rubyContext.getCoreExceptions().argumentError(args.length, 0, this));
        }
    }

    protected class IsReferenceEqualOutgoingNode extends OutgoingNode {

        @Override
        public Object executeCall(TruffleObject receiver, Object[] args) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(args.length, 1, this));
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

    // TODO (pitr-ch 30-Mar-2019): rename, drop unbox name
    protected abstract static class UnboxForOperatorAndReDispatchOutgoingNode extends OutgoingNode {

        private final String name;

        public UnboxForOperatorAndReDispatchOutgoingNode(String name) {
            this.name = name;
        }

        @Specialization(guards = "receivers.isBoolean(receiver)", limit = "getCacheLimit()")
        public Object callBoolean(
                TruffleObject receiver,
                Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asBoolean(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = "receivers.isString(receiver)", limit = "getCacheLimit()")
        public Object callString(
                TruffleObject receiver,
                Object[] args,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                Object rubyString = foreignToRubyNode.executeConvert(receivers.asString(receiver));
                return dispatch.call(rubyString, name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        // TODO (pitr-ch 30-Mar-2019): does it make sense to have paths for smaller numbers?

        @Specialization(guards = { "receivers.isNumber(receiver)", "receivers.fitsInInt(receiver)" },
                limit = "getCacheLimit()")
        public Object callInt(
                TruffleObject receiver,
                Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asInt(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = { "receivers.isNumber(receiver)", "!receivers.fitsInInt(receiver)", "receivers.fitsInLong(receiver)" },
                limit = "getCacheLimit()")
        public Object callLong(
                TruffleObject receiver,
                Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asLong(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = { "receivers.isNumber(receiver)", "!receivers.fitsInLong(receiver)", "receivers.fitsInDouble(receiver)" },
                limit = "getCacheLimit()")
        public Object callDouble(
                TruffleObject receiver,
                Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asDouble(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = {
                "!receivers.isBoolean(receiver)",
                "!receivers.isString(receiver)",
                "!receivers.isNumber(receiver)"
        }, limit = "getCacheLimit()")
        public Object call(
                TruffleObject receiver,
                Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached InteropNodes.InvokeNode invokeNode) {
            return invokeNode.execute(receiver, name, args);
        }
    }

    protected abstract static class InvokeOutgoingNode extends OutgoingNode {

        private final String name;

        public InvokeOutgoingNode(String name) {
            this.name = name;
        }

        @Specialization
        public Object call(
                TruffleObject receiver,
                Object[] args,
                @Cached InteropNodes.InvokeNode invokeNode) {
            // TODO (pitr-ch 20-May-2019): better translation of interop errors
            return invokeNode.execute(receiver, name, args);
        }
    }

}
