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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.methods.ExceptionTranslatingNode;
import org.truffleruby.language.methods.TranslateExceptionNode;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateUncached
public abstract class OutgoingForeignCallNode extends RubyBaseWithoutContextNode {

    public abstract Object executeCall(Object receiver, String name, Object[] args);

    // uncached name implementation is not needed since the name is cached in CachedForeignDispatchNode
    // TODO (pitr-ch 30-Jul-2019): make this clearer when cleaning up this file
    @Specialization(guards = "name == cachedName", limit = "1")
    protected Object callCached(
            Object receiver, String name, Object[] args,
            @Cached("name") String cachedName,
            @Cached BranchProfile errorProfile,
            @Cached TranslateExceptionNode translateExceptionNode,
            @Cached(value = "createOutgoingNode(cachedName, false)", uncached = "createOutgoingNode(cachedName, true)") OutgoingNode outgoingNode) {
        try {
            return outgoingNode.executeCall(receiver, cachedName, args);
        } catch (Throwable t) {
            errorProfile.enter();
            throw translateExceptionNode.executeTranslation(t, UnsupportedOperationBehavior.TYPE_ERROR);
        }
    }

    protected static ExceptionTranslatingNode createExceptionTranslating() {
        return new ExceptionTranslatingNode(null, UnsupportedOperationBehavior.TYPE_ERROR);
    }

    @TruffleBoundary
    protected OutgoingNode createOutgoingNode(String name, boolean uncached) {
        // TODO (pitr-ch 01-Apr-2019): support to_int with new interop
        if (name.equals("[]")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.IndexReadOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.IndexReadOutgoingNodeGen.create();
            }
        } else if (name.equals("[]=")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.IndexWriteOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.IndexWriteOutgoingNodeGen.create();
            }
        } else if (name.equals("call")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.CallOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.CallOutgoingNodeGen.create();
            }
        } else if (name.equals("new")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.NewOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.NewOutgoingNodeGen.create();
            }
        } else if (name.equals("to_a") || name.equals("to_ary")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.ToAOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.ToAOutgoingNodeGen.create();
            }
        } else if (name.equals("respond_to?")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.RespondToOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.RespondToOutgoingNodeGen.create();
            }
        } else if (name.equals("__send__")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.SendOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.SendOutgoingNodeGen.create();
            }
        } else if (name.equals("nil?")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.IsNilOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.IsNilOutgoingNodeGen.create();
            }
        } else if (name.equals("equal?")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.IsReferenceEqualOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.IsReferenceEqualOutgoingNodeGen.create();
            }
        } else if (name.equals("delete") || name.equals("size") || name.equals("keys") || name.equals("class") || name.equals("inspect") || name.equals("to_s") || name.equals("to_str") ||
                name.equals("is_a?") || name.equals("kind_of?")) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.SpecialFormOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.SpecialFormOutgoingNodeGen.create();
            }
        } else if (isOperatorMethod(name)) {
            if (uncached) {
                return OutgoingForeignCallNodeGen.UnboxForOperatorAndReDispatchOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.UnboxForOperatorAndReDispatchOutgoingNodeGen.create();
            }
        } else {
            if (uncached) {
                return OutgoingForeignCallNodeGen.InvokeOutgoingNodeGen.getUncached();
            } else {
                return OutgoingForeignCallNodeGen.InvokeOutgoingNodeGen.create();
            }
        }
    }

    @TruffleBoundary
    private static boolean isOperatorMethod(String name) {
        return !name.isEmpty() && !Character.isLetter(name.charAt(0));
    }

    // TODO (pitr 27-Jul-2019): cleanup this file, turn nodes into specializations?

    protected abstract static class OutgoingNode extends RubyBaseWithoutContextNode {

        protected int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }

        protected abstract Object executeCall(Object receiver, String name, Object[] args);
    }

    @GenerateUncached
    public abstract static class IndexReadOutgoingNode extends OutgoingNode {
        @Specialization(guards = "args.length == 1")
        protected Object call(
                Object receiver, String name, Object[] args,
                @Cached InteropNodes.ReadUncacheableNode readNode) {
            return readNode.execute(receiver, args[0]);
        }

        @Specialization(guards = "args.length != 1")
        protected Object callWithBadArguments(
                Object receiver, String name, Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(args.length, 1, this));
        }
    }

    @GenerateUncached
    protected abstract static class IndexWriteOutgoingNode extends OutgoingNode {

        @Specialization(guards = "args.length == 2")
        protected Object call(
                Object receiver, String name, Object[] args,
                @Cached InteropNodes.WriteUncacheableNode writeUncacheableNode) {
            return writeUncacheableNode.execute(receiver, args[0], args[1]);
        }

        @Specialization(guards = "args.length != 2")
        protected Object callWithBadArguments(
                Object receiver, String name, Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(args.length, 2, this));
        }
    }

    @GenerateUncached
    protected abstract static class CallOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object call(
                Object receiver, String name, Object[] args,
                // TODO (pitr-ch 29-Mar-2019): use this node directly instead?
                @Cached InteropNodes.ExecuteUncacheableNode executeUncacheableNode) {
            return executeUncacheableNode.execute(receiver, args);
        }
    }


    @GenerateUncached
    protected abstract static class SendOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object sendOutgoing(Object receiver, String nameSend, Object[] args,
                @Cached BranchProfile argumentErrorProfile,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached("createPrivate()") CallDispatchHeadNode dispatchNode) {
            if (args.length < 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().argumentError(args.length, 1, this));
            }

            final Object name = args[0];
            final Object[] sendArgs = Arrays.copyOfRange(args, 1, args.length);

            // FIXME (pitr 29-Jul-2019): it had return missing instead
            return dispatchNode.dispatch(null, receiver, name, null, sendArgs);
        }

    }

    @GenerateUncached
    protected abstract static class NewOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object newOutgoing(
                Object receiver, String name, Object[] args,
                // TODO (pitr-ch 29-Mar-2019): use this node directly?
                @Cached InteropNodes.NewUncacheableNode newNode) {
            return newNode.execute(receiver, args);
        }

    }

    @GenerateUncached
    protected abstract static class ToAOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object toAOutgoing(Object receiver, String name, Object[] args,
                @Cached BranchProfile argumentErrorProfile,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached("createPrivate()") CallDispatchHeadNode callToArray) {
            if (args.length != 0) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().argumentError(args.length, 0, this));
            }

            return callToArray.call(context.getCoreLibrary().getTruffleInteropModule(), "to_array", receiver);
        }

    }

    @GenerateUncached
    protected abstract static class RespondToOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object respondToOutgoing(Object receiver, String name, Object[] args,
                @Cached BranchProfile argumentErrorProfile,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached("createPrivate()") CallDispatchHeadNode callRespondTo) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().argumentError(args.length, 1, this));
            }

            return callRespondTo.call(context.getCoreLibrary().getTruffleInteropModule(), "respond_to?", receiver, args[0]);
        }

    }

    @GenerateUncached
    protected abstract static class SpecialFormOutgoingNode extends OutgoingNode {

        @Specialization(guards = "contextReference.get() == cachedContext", limit = "getCacheLimit()")
        protected Object specialFormOutgoingCached(TruffleObject receiver, String cachedName, Object[] args,
                @CachedContext(RubyLanguage.class) TruffleLanguage.ContextReference<RubyContext> contextReference,
                @Cached("contextReference.get()") RubyContext cachedContext,
                @Cached(value = "cachedContext.getSymbolTable().getSymbol(cachedName)", allowUncached = true) DynamicObject symbol,
                @Cached(value = "getExpectedArgsLength(cachedName)", allowUncached = true) int expectedArgsLength,
                @Cached BranchProfile argumentErrorProfile,
                @Cached("createPrivate()") CallDispatchHeadNode callSpecialForm) {
            return specialFormOutgoing(cachedContext, argumentErrorProfile, callSpecialForm, receiver, symbol, expectedArgsLength, args);
        }

        @Specialization(replaces = "specialFormOutgoingCached")
        protected Object specialFormOutgoingUncached(TruffleObject receiver, String cachedName, Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached(value = "getExpectedArgsLength(cachedName)", allowUncached = true) int expectedArgsLength,
                @Cached BranchProfile argumentErrorProfile,
                @Cached("createPrivate()") CallDispatchHeadNode callSpecialForm) {

            DynamicObject symbol = context.getSymbolTable().getSymbol(cachedName);
            return specialFormOutgoing(context, argumentErrorProfile, callSpecialForm, receiver, symbol, expectedArgsLength, args);
        }

        protected Object specialFormOutgoing(
                RubyContext context, BranchProfile argumentErrorProfile, CallDispatchHeadNode callSpecialForm,
                TruffleObject receiver, DynamicObject symbol, int expectedArgsLength, Object[] args) {

            if (args.length != expectedArgsLength) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().argumentError(args.length, expectedArgsLength, this));
            }

            final Object[] prependedArgs = new Object[args.length + 2];
            prependedArgs[0] = receiver;
            prependedArgs[1] = symbol;
            System.arraycopy(args, 0, prependedArgs, 2, args.length);

            return callSpecialForm.call(context.getCoreLibrary().getTruffleInteropModule(), "special_form", prependedArgs);
        }

        protected static RubyContext getCurrentContext() {
            return RubyLanguage.getCurrentContext();
        }

        protected int getExpectedArgsLength(String name) {
            int expectedArgsLength;
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
            return expectedArgsLength;
        }


    }

    @GenerateUncached
    protected abstract static class IsNilOutgoingNode extends OutgoingNode {

        @Specialization(guards = "args.length == 0")
        protected Object call(
                Object receiver, String name, Object[] args,
                @Cached InteropNodes.NullUncacheableNode nullUncacheableNode) {
            return nullUncacheableNode.execute(receiver);
        }

        @Specialization(guards = "args.length != 0")
        protected Object callWithBadArguments(
                Object receiver, String name, Object[] args,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().argumentError(args.length, 0, this));
        }
    }

    @GenerateUncached
    protected abstract static class IsReferenceEqualOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object isReferenceEqualOutgoing(TruffleObject receiver, String name, Object[] args,
                @Cached BranchProfile argumentErrorProfile,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            if (args.length != 1) {
                argumentErrorProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().argumentError(args.length, 1, this));
            }

            final TruffleObject a = receiver;

            if (!(args[0] instanceof TruffleObject)) {
                return false;
            }

            final TruffleObject b = (TruffleObject) args[0];

            if (context.getEnv().isHostObject(a) && context.getEnv().isHostObject(b)) {
                return context.getEnv().asHostObject(a) == context.getEnv().asHostObject(b);
            } else {
                return a == b;
            }
        }

    }

    // TODO (pitr-ch 30-Mar-2019): rename, drop unbox name
    @GenerateUncached
    protected abstract static class UnboxForOperatorAndReDispatchOutgoingNode extends OutgoingNode {

        @Specialization(guards = "receivers.isBoolean(receiver)", limit = "getCacheLimit()")
        protected Object callBoolean(
                TruffleObject receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asBoolean(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = "receivers.isString(receiver)", limit = "getCacheLimit()")
        protected Object callString(
                TruffleObject receiver, String name, Object[] args,
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

        @Specialization(guards = { "receivers.isNumber(receiver)", "receivers.fitsInInt(receiver)" }, limit = "getCacheLimit()")
        protected Object callInt(
                TruffleObject receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asInt(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = { "receivers.isNumber(receiver)", "!receivers.fitsInInt(receiver)", "receivers.fitsInLong(receiver)" }, limit = "getCacheLimit()")
        protected Object callLong(
                TruffleObject receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("createPrivate()") CallDispatchHeadNode dispatch) {
            try {
                return dispatch.call(receivers.asLong(receiver), name, args);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

        @Specialization(guards = { "receivers.isNumber(receiver)", "!receivers.fitsInLong(receiver)", "receivers.fitsInDouble(receiver)" }, limit = "getCacheLimit()")
        protected Object callDouble(
                TruffleObject receiver, String name, Object[] args,
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
        protected Object call(
                TruffleObject receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached InteropNodes.InvokeUncacheableNode invokeUncacheableNode) {
            return invokeUncacheableNode.execute(receiver, name, args);
        }

    }

    @GenerateUncached
    protected abstract static class InvokeOutgoingNode extends OutgoingNode {

        @Specialization
        protected Object call(
                TruffleObject receiver, String name, Object[] args,
                @Cached InteropNodes.InvokeUncacheableNode invokeUncacheableNode) {
            // TODO (pitr-ch 20-May-2019): better translation of interop errors
            return invokeUncacheableNode.execute(receiver, name, args);
        }
    }

}
