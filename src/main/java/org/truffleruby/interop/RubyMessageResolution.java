/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyObjectType;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;

@MessageResolution(receiverType = RubyObjectType.class)
public class RubyMessageResolution {

    @CanResolve
    public abstract static class Check extends Node {

        protected static boolean test(TruffleObject receiver) {
            return RubyGuards.isRubyBasicObject(receiver);
        }

    }

    @Resolve(message = "IS_NULL")
    public static abstract class ForeignIsNullNode extends Node {

        @CompilationFinal RubyContext context;

        protected Object access(DynamicObject object) {
            return object == getContext().getCoreLibrary().getNil();
        }

        private RubyContext getContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = RubyLanguage.getCurrentContext();
            }

            return context;
        }

    }

    @Resolve(message = "HAS_SIZE")
    public static abstract class ForeignHasSizeNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return doesRespond.doesRespondTo(frame, "size", object);
        }

    }

    @Resolve(message = "GET_SIZE")
    public static abstract class ForeignGetSizeNode extends Node {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return dispatchNode.call(frame, object, "size");
        }

    }

    @Resolve(message = "IS_BOXED")
    public static abstract class ForeignIsBoxedNode extends Node {

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
    public static abstract class ForeignUnboxNode extends Node {

        private final ConditionProfile stringSymbolProfile = ConditionProfile.createBinaryProfile();
        private final ConditionProfile pointerProfile = ConditionProfile.createBinaryProfile();

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();
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
                return dispatchNode.dispatch(frame, object, "unbox", null, RubyNode.EMPTY_ARGUMENTS);
            } else {
                throw UnsupportedMessageException.raise(Message.UNBOX);
            }
        }

    }

    @Resolve(message = "IS_POINTER")
    public static abstract class ForeignIsPointerNode extends Node {

        protected Object access(DynamicObject object) {
            return Layouts.POINTER.isPointer(object);
        }

    }

    @Resolve(message = "AS_POINTER")
    public static abstract class ForeignAsPointerNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();

        private final ConditionProfile intProfile = ConditionProfile.createBinaryProfile();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (doesRespond.doesRespondTo(frame, "address", object)) {
                Object result = dispatchNode.call(frame, object, "address");

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
    public static abstract class ForeignToNativeNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();
        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            if (doesRespond.doesRespondTo(frame, "to_native", object)) {
                return dispatchNode.dispatch(frame, object, "to_native", null, RubyNode.EMPTY_ARGUMENTS);
            } else {
                throw UnsupportedMessageException.raise(Message.TO_NATIVE);
            }
        }

    }

    @Resolve(message = "READ")
    public static abstract class ForeignReadNode extends Node {

        @Child private ForeignReadStringCachingHelperNode helperNode = ForeignReadStringCachingHelperNodeGen.create(null, null);

        private final BranchProfile exceptionProfile = BranchProfile.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object name) {
            try {
                return helperNode.executeStringCachingHelper(frame, object, name);
            } catch (RaiseException e) {
                exceptionProfile.enter();
                throw UnknownIdentifierException.raise(toString(name));
            }
        }

        @TruffleBoundary
        private String toString(Object name) {
            return name.toString();
        }

    }

    @Resolve(message = "WRITE")
    public static abstract class ForeignWriteNode extends Node {

        @Child private ForeignWriteStringCachingHelperNode helperNode = ForeignWriteStringCachingHelperNodeGen.create(null, null, null);
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object name, Object value) {
            return helperNode.executeStringCachingHelper(
                    frame,
                    object,
                    name,
                    foreignToRubyNode.executeConvert(value));
        }

    }

    @Resolve(message = "HAS_KEYS")
    public static abstract class ForeignHasKeysNode extends Node {

        @Child private HasKeysNode hasKeysNode = HasKeysNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return hasKeysNode.executeHasKeys(object);
        }

    }

    @Resolve(message = "KEYS")
    public static abstract class ForeignKeysNode extends Node {

        @CompilationFinal private RubyContext context;

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();

        protected Object access(VirtualFrame frame, DynamicObject object, boolean internal) {
            return dispatchNode.call(frame, getContext().getCoreLibrary().getTruffleInteropModule(), "object_keys", object, internal);
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
    public static abstract class ForeignKeyInfoNode extends Node {

        @CompilationFinal private RubyContext context;

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createOnSelf();
        @Child private ForeignToRubyNode foreignToRubyNode = ForeignToRubyNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object, String name) {
            return dispatchNode.call(frame, getContext().getCoreLibrary().getTruffleInteropModule(), "object_key_info", object, foreignToRubyNode.executeConvert(name));
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
    public static abstract class ForeignIsExecutableNode extends Node {

        protected Object access(VirtualFrame frame, DynamicObject object) {
            return RubyGuards.isRubyMethod(object) || RubyGuards.isRubyProc(object);
        }

    }

    @Resolve(message = "EXECUTE")
    public static abstract class ForeignExecuteNode extends Node {

        @Child private ForeignExecuteHelperNode executeMethodNode = ForeignExecuteHelperNodeGen.create(null, null);
        @Child private ForeignToRubyArgumentsNode foreignToRubyArgumentsNode = ForeignToRubyArgumentsNode.create();

        protected Object access(VirtualFrame frame, DynamicObject object, Object[] arguments) {
            return executeMethodNode.executeCall(
                    object,
                    foreignToRubyArgumentsNode.executeConvert(arguments));
        }

    }

    @Resolve(message = "INVOKE")
    public static abstract class ForeignInvokeNode extends Node {

        @Child private DispatchHeadNode dispatchHeadNode = CallDispatchHeadNode.createOnSelf();
        @Child private ForeignToRubyArgumentsNode foreignToRubyArgumentsNode = ForeignToRubyArgumentsNode.create();

        protected Object access(VirtualFrame frame, DynamicObject receiver, String name, Object[] arguments) {
            return dispatchHeadNode.dispatch(
                    frame,
                    receiver,
                    name,
                    null,
                    foreignToRubyArgumentsNode.executeConvert(arguments));
        }

    }

    @Resolve(message = "IS_INSTANTIABLE")
    public static abstract class ForeignIsInstantiableNode extends Node {

        @Child private DoesRespondDispatchHeadNode doesRespond = DoesRespondDispatchHeadNode.create();

        protected Object access(VirtualFrame frame, DynamicObject receiver) {
            return doesRespond.doesRespondTo(frame, "new", receiver);
        }

    }

    @Resolve(message = "NEW")
    public static abstract class ForeignNewNode extends Node {

        @Child private DispatchHeadNode dispatchHeadNode = CallDispatchHeadNode.createOnSelf();
        @Child private ForeignToRubyArgumentsNode foreignToRubyArgumentsNode = ForeignToRubyArgumentsNode.create();

        protected Object access(VirtualFrame frame, DynamicObject receiver, Object[] arguments) {
            return dispatchHeadNode.dispatch(
                    frame,
                    receiver,
                    "new",
                    null,
                    foreignToRubyArgumentsNode.executeConvert(arguments));
        }

    }

}
