/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.interop.InteropNodes.InvokeMemberNode;
import org.truffleruby.interop.InteropNodes.ReadMemberNode;
import org.truffleruby.interop.InteropNodes.WriteMemberWithoutConversionNode;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

/** foreign.some_method(*args), when #some_method is not defined in Polyglot::Foreign* */
@GenerateUncached
public abstract class CallForeignMethodNode extends RubyBaseNode {

    public static CallForeignMethodNode create() {
        return CallForeignMethodNodeGen.create();
    }

    public static CallForeignMethodNode getUncached() {
        return CallForeignMethodNodeGen.getUncached();
    }

    public abstract Object execute(Object receiver, String methodName, Object block, Object[] arguments);

    @Specialization
    protected Object call(Object receiver, String methodName, Object block, Object[] arguments,
            @Cached ForeignInvokeNode foreignInvokeNode,
            @Cached TranslateExceptionNode translateException,
            @Cached ConditionProfile hasBlock,
            @Cached BranchProfile errorProfile) {
        assert block instanceof Nil || block instanceof RubyProc : block;

        Object[] newArguments = arguments;
        if (hasBlock.profile(block != nil)) {
            newArguments = ArrayUtils.append(arguments, block);
        }

        try {
            return foreignInvokeNode.execute(receiver, methodName, newArguments);
        } catch (Throwable t) {
            errorProfile.enter();
            throw translateException.executeTranslation(t);
        }
    }

    @GenerateUncached
    public abstract static class ForeignInvokeNode extends RubyBaseNode {

        public abstract Object execute(Object receiver, String name, Object[] args);

        @Specialization(
                guards = { "name == cachedName", "!isOperatorMethod(cachedName)", "!isAssignmentMethod(cachedName)" },
                limit = "1")
        protected Object invokeOrRead(Object receiver, String name, Object[] args,
                @Cached("name") String cachedName,
                @Cached InvokeOrReadMemberNode invokeOrReadMemberNode) {
            return invokeOrReadMemberNode.execute(receiver, name, args);
        }

        @Specialization(guards = { "name == cachedName", "isOperatorMethod(cachedName)" }, limit = "1")
        protected Object operatorMethod(Object receiver, String name, Object[] args,
                @Cached("name") String cachedName,
                @Cached ConvertForOperatorAndReDispatchNode operatorNode) {
            return operatorNode.execute(receiver, name, args);
        }

        @Specialization(guards = { "name == cachedName", "isAssignmentMethod(cachedName)" }, limit = "1")
        protected Object assignmentMethod(Object receiver, String name, Object[] args,
                @Cached("name") String cachedName,
                @Cached(value = "getPropertyFromName(cachedName)", allowUncached = true) String propertyName,
                @Cached WriteMemberWithoutConversionNode writeMemberNode,
                @Cached BranchProfile errorProfile) {
            if (args.length == 1) {
                return writeMemberNode.execute(receiver, propertyName, args[0]);
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(args.length, 1, this));
            }
        }

        @TruffleBoundary
        protected static boolean isOperatorMethod(String name) {
            return !name.isEmpty() && !Character.isLetter(name.charAt(0));
        }

        @TruffleBoundary
        protected static boolean isAssignmentMethod(String name) {
            return !name.isEmpty() && Character.isLetter(name.charAt(0)) && name.charAt(name.length() - 1) == '=';
        }

        @TruffleBoundary
        protected static String getPropertyFromName(String name) {
            return name.substring(0, name.length() - 1);
        }
    }

    @GenerateUncached
    public abstract static class InvokeOrReadMemberNode extends RubyBaseNode {

        public abstract Object execute(Object receiver, String identifier, Object[] args);

        @Specialization(guards = "args.length == 0", limit = "getInteropCacheLimit()")
        protected Object readOrInvoke(Object receiver, String name, Object[] args,
                @Cached ToSymbolNode toSymbolNode,
                @Cached InvokeMemberNode invokeNode,
                @Cached ReadMemberNode readNode,
                @Cached ConditionProfile invocable,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            if (invocable.profile(receivers.isMemberInvocable(receiver, name))) {
                return invokeNode.execute(receiver, name, args);
            } else {
                return readNode.execute(receiver, toSymbolNode.execute(name));
            }
        }

        @Specialization(guards = "args.length != 0")
        protected Object invoke(Object receiver, String name, Object[] args,
                @Cached InvokeMemberNode invokeNode) {
            return invokeNode.execute(receiver, name, args);
        }
    }

    @GenerateUncached
    protected abstract static class ConvertForOperatorAndReDispatchNode extends RubyBaseNode {

        protected abstract Object execute(Object receiver, String name, Object[] args);

        @Specialization(guards = "receivers.isBoolean(receiver)", limit = "getInteropCacheLimit()")
        protected Object callBoolean(Object receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached DispatchNode dispatch) {
            try {
                return dispatch.call(receivers.asBoolean(receiver), name, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        @Specialization(guards = "receivers.isString(receiver)", limit = "getInteropCacheLimit()")
        protected Object callString(Object receiver, String name, Object[] args,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached DispatchNode dispatch) {
            try {
                Object rubyString = foreignToRubyNode.executeConvert(receivers.asString(receiver));
                return dispatch.call(rubyString, name, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        @Specialization(
                guards = { "receivers.isNumber(receiver)", "receivers.fitsInInt(receiver)" },
                limit = "getInteropCacheLimit()")
        protected Object callInt(Object receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached DispatchNode dispatch) {
            try {
                return dispatch.call(receivers.asInt(receiver), name, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        @Specialization(
                guards = {
                        "receivers.isNumber(receiver)",
                        "!receivers.fitsInInt(receiver)",
                        "receivers.fitsInLong(receiver)" },
                limit = "getInteropCacheLimit()")
        protected Object callLong(Object receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached DispatchNode dispatch) {
            try {
                return dispatch.call(receivers.asLong(receiver), name, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        @Specialization(
                guards = {
                        "receivers.isNumber(receiver)",
                        "!receivers.fitsInLong(receiver)",
                        "receivers.fitsInDouble(receiver)" },
                limit = "getInteropCacheLimit()")
        protected Object callDouble(Object receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached DispatchNode dispatch) {
            try {
                return dispatch.call(receivers.asDouble(receiver), name, args);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        @Specialization(
                guards = {
                        "!receivers.isBoolean(receiver)",
                        "!receivers.isString(receiver)",
                        "!receivers.isNumber(receiver)" },
                limit = "getInteropCacheLimit()")
        protected Object call(Object receiver, String name, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached InvokeMemberNode invokeNode) {
            return invokeNode.execute(receiver, name, args);
        }
    }
}
