/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;

@MessageResolution(receiverType = BoxedValue.class)
public class RubyObjectType extends ObjectType {

    @Override
    @TruffleBoundary
    public String toString(DynamicObject object) {
        if (RubyGuards.isRubyString(object)) {
            return StringOperations.getString(object);
        } else if (RubyGuards.isRubySymbol(object)) {
            return Layouts.SYMBOL.getString(object);
        } else if (RubyGuards.isRubyException(object)) {
            return Layouts.EXCEPTION.getMessage(object).toString();
        } else if (RubyGuards.isRubyModule(object)) {
            return Layouts.MODULE.getFields(object).getName();
        } else {
            final String className = Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(object)).getName();
            return StringUtils.format("DynamicObject@%x<%s>", System.identityHashCode(object), className);
        }
    }


    @CanResolve
    public abstract static class Check extends Node {

        protected static boolean test(TruffleObject receiver) {
            return RubyGuards.isRubyBasicObject(receiver);
        }

    }

    @Resolve(message = "IS_NULL")
    public static abstract class IsNullNode extends Node {

        @CompilationFinal private ContextReference<RubyContext> contextReference;

        protected Object access(DynamicObject object) {
            return object == getContext().getCoreLibrary().getNil();
        }

        private RubyContext getContext() {
            if (contextReference == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                contextReference = RubyLanguage.getCurrentContextReference();
            }

            return contextReference.get();
        }

    }

    @Resolve(message = "HAS_SIZE")
    public static abstract class HasSizeNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return RubyGuards.isRubyArray(object);
        }

    }

    @Resolve(message = "GET_SIZE")
    public static abstract class GetSizeNode extends Node {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return dispatchNode.call(object, "size");
        }

    }

    @Resolve(message = "IS_BOXED")
    public static abstract class IsBoxedNode extends Node {

        private final ConditionProfile stringProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile symbolProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile pointerProfile = ConditionProfile.createBinaryProfile();

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (stringProfile.profile(RubyGuards.isRubyString(object))) {
                return true;
            } else if (symbolProfile.profile(RubyGuards.isRubySymbol(object))) {
                return true;
            } else if (pointerProfile.profile(Layouts.POINTER.isPointer(object))) {
                return true;
            } else {
                return doesRespond.doesRespondTo(frame, "unbox", object);
            }
        }

    }

    @Resolve(message = "UNBOX")
    public static abstract class UnboxNode extends Node {

        private final ConditionProfile stringSymbolProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile pointerProfile = ConditionProfile.createBinaryProfile();

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();
        @Child private ToJavaStringNode toJavaStringNode;

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (stringSymbolProfile.profile(RubyGuards.isRubyString(object) || RubyGuards.isRubySymbol(object))) {
                if (toJavaStringNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    toJavaStringNode = insert(ToJavaStringNode.create());
                }

                return toJavaStringNode.executeToJavaString(object);
            } else if (pointerProfile.profile(Layouts.POINTER.isPointer(object))) {
                return Layouts.POINTER.getPointer(object).getAddress();
            } else if (doesRespond.doesRespondTo(frame, "unbox", object)) {
                return dispatchNode.call(object, "unbox");
            } else {
                throw UnsupportedMessageException.raise(Message.UNBOX);
            }
        }

    }

    @Resolve(message = "IS_POINTER")
    public static abstract class IsPointerNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (doesRespond.doesRespondTo(frame, "pointer?", object)) {
                return dispatchNode.call(object, "pointer?");
            } else {
                return false;
            }
        }

    }

    @Resolve(message = "AS_POINTER")
    public static abstract class AsPointerNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();

        private final ConditionProfile intProfile = ConditionProfile.createBinaryProfile();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (doesRespond.doesRespondTo(frame, "address", object)) {
                Object result = dispatchNode.call(object, "address");

                if (intProfile.profile(result instanceof Integer)) {
                    result = (long) ((int) result);
                }

                return result;
            } else {
                throw UnsupportedMessageException.raise(Message.AS_POINTER);
            }
        }

    }

    @Resolve(message = "TO_NATIVE")
    public static abstract class ToNativeNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (doesRespond.doesRespondTo(frame, "to_native", object)) {
                return dispatchNode.call(object, "to_native");
            } else {
                throw UnsupportedMessageException.raise(Message.TO_NATIVE);
            }
        }

    }

    @Resolve(message = "READ")
    public static abstract class ReadNode extends Node {

        @Child private ForeignReadStringCachingHelperNode helperNode = ForeignReadStringCachingHelperNodeGen.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object name) {
            return helperNode.executeStringCachingHelper(frame, object, name);
        }

    }

    @Resolve(message = "WRITE")
    public static abstract class WriteNode extends Node {

        @Child private ForeignWriteStringCachingHelperNode helperNode = ForeignWriteStringCachingHelperNodeGen.create();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object name, Object value) {
            return helperNode.executeStringCachingHelper(
                    frame,
                    object,
                    name,
                    foreignToRubyNode.executeConvert(value));
        }

    }

    @Resolve(message = "REMOVE")
    public static abstract class RemoveNode extends Node {

        private final ConditionProfile arrayReceiverProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile hashReceiverProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile stringProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile ivarProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile validArrayIndexProfile = ConditionProfile.createBinaryProfile();

        @Child private ToJavaStringNode toJavaStringNode = ToJavaStringNode.create();
        @Child private CallDispatchHeadNode arrayDeleteAtNode = CallDispatchHeadNode.createPrivate();
        @Child private CallDispatchHeadNode hashDeleteNode = CallDispatchHeadNode.createPrivate();
        @Child private CallDispatchHeadNode removeInstanceVariableNode = CallDispatchHeadNode.createPrivate();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        protected boolean access(VirtualFrame frame, DynamicObject object, Object name) {
            if (arrayReceiverProfile.profile(RubyGuards.isRubyArray(object))) {
                if (validArrayIndexProfile.profile(name instanceof Integer ||
                        (name instanceof Long && RubyGuards.fitsInInteger((long) name)))) {
                    arrayDeleteAtNode.call(object, "delete_at", name);
                } else {
                    throw UnknownIdentifierException.raise(toJavaStringNode.executeToJavaString(name));
                }
            } else if (hashReceiverProfile.profile(RubyGuards.isRubyHash(object))) {
                hashDeleteNode.call(object, "delete", foreignToRubyNode.executeConvert(name));
            } else if (stringProfile.profile(name instanceof String)) {
                final String stringName = (String) name;

                if (ivarProfile.profile(!stringName.isEmpty() && stringName.charAt(0) == '@')) {
                    removeInstanceVariableNode.call(object, "remove_instance_variable", foreignToRubyNode.executeConvert(name));
                } else {
                    throw UnsupportedMessageException.raise(Message.REMOVE);
                }
            } else {
                throw UnsupportedMessageException.raise(Message.REMOVE);
            }

            return true;
        }

    }

    @Resolve(message = "HAS_KEYS")
    public static abstract class HasKeysNode extends Node {

        private final ConditionProfile hasKeysProfile = ConditionProfile.createBinaryProfile();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return !hasKeysProfile.profile(RubyGuards.isRubyArray(object) || RubyGuards.isRubyString(object));
        }

    }

    @Resolve(message = "KEYS")
    public static abstract class KeysNode extends Node {

        @CompilationFinal private RubyContext context;

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();

        protected Object access(VirtualFrame frame, DynamicObject object, boolean internal) {
            return dispatchNode.call(getContext().getCoreLibrary().getTruffleInteropModule(), "object_keys", object, internal);
        }

        private RubyContext getContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = RubyLanguage.getCurrentContext();
            }

            return context;
        }

    }

    @Resolve(message = "KEY_INFO")
    public static abstract class KeyInfoNode extends Node {

        @CompilationFinal private RubyContext context;

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object name) {
            final Object convertedName = foreignToRubyNode.executeConvert(name);
            return dispatchNode.call(getContext().getCoreLibrary().getTruffleInteropModule(), "object_key_info", object, convertedName);
        }

        private RubyContext getContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = RubyLanguage.getCurrentContext();
            }

            return context;
        }

    }

    @Resolve(message = "IS_EXECUTABLE")
    public static abstract class IsExecutableNode extends Node {

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return RubyGuards.isRubyMethod(object) || RubyGuards.isRubyProc(object);
        }

    }

    @Resolve(message = "EXECUTE")
    public static abstract class ExecuteNode extends Node {

        @Child private ForeignExecuteHelperNode executeMethodNode = ForeignExecuteHelperNodeGen.create();
        @Child private ForeignToRubyArgumentsNode foreignToRubyArgumentsNode = ForeignToRubyArgumentsNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object[] arguments) {
            return executeMethodNode.executeCall(
                    object,
                    foreignToRubyArgumentsNode.executeConvert(arguments));
        }

    }

    @Resolve(message = "INVOKE")
    public static abstract class InvokeNode extends Node {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();
        @Child private ForeignToRubyArgumentsNode foreignToRubyArgumentsNode = ForeignToRubyArgumentsNode.create();

        protected Object access(VirtualFrame frame, DynamicObject receiver, String name, Object[] arguments) {
            return dispatchNode.call(receiver, name, foreignToRubyArgumentsNode.executeConvert(arguments));
        }

    }

    @Resolve(message = "IS_INSTANTIABLE")
    public static abstract class IsInstantiableNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();

        protected Object access(VirtualFrame frame, DynamicObject receiver) {
            return doesRespond.doesRespondTo(frame, "new", receiver);
        }

    }

    @Resolve(message = "NEW")
    public static abstract class NewNode extends Node {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPrivate();
        @Child private ForeignToRubyArgumentsNode foreignToRubyArgumentsNode = ForeignToRubyArgumentsNode.create();

        protected Object access(VirtualFrame frame, DynamicObject receiver, Object[] arguments) {
            return dispatchNode.call(receiver, "new", foreignToRubyArgumentsNode.executeConvert(arguments));
        }

    }

    @Override
    public ForeignAccess getForeignAccessFactory(DynamicObject object) {
        return RubyObjectTypeForeign.ACCESS;
    }

}
