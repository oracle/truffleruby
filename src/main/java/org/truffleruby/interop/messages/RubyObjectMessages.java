/*
 * Copyright (c) 2013, 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop.messages;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.cast.LongCastNode;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class RubyObjectMessages {

    public final Class<?> dispatch() {
        return null;
    }

    // TODO (pitr-ch 19-Mar-2019): return exceptions like UnsupportedMessageException correctly

    @ExportMessage
    public static boolean hasArrayElements(
            DynamicObject receiver,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        // FIXME (pitr 26-Mar-2019): the method should have a marker module
        Object value = dispatchNode.call(receiver, "polyglot_array?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage()
    public static long getArraySize(
            DynamicObject receiver,
            @Cached IntegerCastNode integerCastNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws UnsupportedMessageException {

        Object value = dispatchNode.call(receiver, "polyglot_array_size");
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return integerCastNode.executeCastInt(value);

    }

    @ExportMessage
    @SuppressWarnings("unused") // has to throw here-unused InvalidArrayIndexException because of ArrayMessages
    public static Object readArrayElement(
            DynamicObject receiver, long index,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws InvalidArrayIndexException, UnsupportedMessageException {

        Object value = dispatchNode.call(receiver, "polyglot_array_read", index);
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return value;
    }

    @ExportMessage
    public static void writeArrayElement(
            DynamicObject receiver, long index, Object value,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws UnsupportedMessageException {

        Object result = dispatchNode.call(receiver, "polyglot_array_write", index, value);
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static void removeArrayElement(
            DynamicObject receiver, long index,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws UnsupportedMessageException {

        Object result = dispatchNode.call(receiver, "polyglot_array_remove", index);
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static boolean isArrayElementReadable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_readable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public static boolean isArrayElementModifiable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_modifiable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public static boolean isArrayElementInsertable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_insertable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public static boolean isArrayElementRemovable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_removable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    // FIXME (pitr 18-Mar-2019): replace #unbox support with testing #to_int etc.
    //   since if an object had un-box method it could be have been un-boxed

    @ExportMessage
    public static boolean isPointer(
            DynamicObject receiver,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_pointer?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    // FIXME (pitr 11-May-2019): allow Ruby objects to implement interop subProtocols, e.g. for array, or numbers. Not for members though.

    // FIXME (pitr 21-Mar-2019): "if-and-only-if" relation between isPointer == true and "asPointer does not throw an UnsupportedMessageException"
    // TODO (pitr-ch 18-Mar-2019): assert #pointer? #address invariant - both has to be defined

    @ExportMessage
    public static long asPointer(
            DynamicObject receiver,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Cached LongCastNode longCastNode) throws UnsupportedMessageException {

        // FIXME (pitr 26-Mar-2019): the method should have a marker module
        Object value = dispatchNode.call(receiver, "polyglot_address");
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return longCastNode.executeCastLong(value);
    }

    @ExportMessage
    public static void toNative(
            DynamicObject receiver,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode) {

        dispatchNode.call(receiver, "polyglot_to_native");
        // we ignore the method missing, toNative never throws
    }

    @ExportMessage
    public static boolean hasMembers(DynamicObject receiver) {
        return true;
    }

    @ExportMessage
    public static Object getMembers(
            DynamicObject receiver,
            boolean internal,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode) {
        return dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_keys",
                receiver,
                internal);
    }

    protected static boolean isIVar(String name) {
        return !name.isEmpty() && name.charAt(0) == '@';
    }

    @ExportMessage
    public static class ReadMember {

        protected final static String INDEX_METHOD_NAME = "[]";
        protected final static String METHOD_NAME = "method";

        @Specialization(guards = { "isIVar(name)" })
        protected static Object readInstanceVariable(DynamicObject receiver, String name,
                @Cached ReadObjectFieldNode readObjectFieldNode) throws UnknownIdentifierException {

            Object result = readObjectFieldNode.execute(receiver, name, null);
            if (result != null) {
                return result;
            } else {
                throw UnknownIdentifierException.create(name);
            }
        }

        @Specialization(guards = { "!isIVar(name)", "indexMethod(definedIndexNode, receiver)" })
        protected static Object callIndex(DynamicObject receiver, String name,
                @Cached DoesRespondDispatchHeadNode definedIndexNode,
                @Cached("createBinaryProfile()") ConditionProfile errorProfile,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached ForeignToRubyNode nameToRubyNode,
                @Cached CallDispatchHeadNode dispatch)
                throws UnknownIdentifierException {
            try {
                return dispatch.call(receiver, INDEX_METHOD_NAME, nameToRubyNode.executeConvert(name));
            } catch (RaiseException ex) {
                // translate NameError to UnknownIdentifierException
                DynamicObject logicalClass = Layouts.BASIC_OBJECT.getLogicalClass(ex.getException());
                if (errorProfile.profile(logicalClass == context.getCoreLibrary().nameErrorClass)) {
                    throw UnknownIdentifierException.create(name);
                } else {
                    throw ex;
                }
            }
        }

        @Specialization(guards = {
                "!isIVar(name)",
                "!indexMethod(definedIndexNode, receiver)",
                "methodDefined(receiver, name, definedNode)" })
        protected static Object getBoundMethod(DynamicObject receiver, String name,
                @Cached DoesRespondDispatchHeadNode definedIndexNode,
                @Cached DoesRespondDispatchHeadNode definedNode,
                @Cached ForeignToRubyNode nameToRubyNode,
                @Cached CallDispatchHeadNode dispatch) {
            return dispatch.call(receiver, METHOD_NAME, nameToRubyNode.executeConvert(name));
        }

        @Specialization(guards = {
                "!isIVar(name)",
                "!indexMethod(definedIndexNode, receiver)",
                "!methodDefined(receiver, name, definedNode)" })
        protected static Object unknownIdentifier(DynamicObject receiver, String name,
                @Cached DoesRespondDispatchHeadNode definedIndexNode,
                @Cached DoesRespondDispatchHeadNode definedNode) throws UnknownIdentifierException {
            throw UnknownIdentifierException.create(toString(name));
        }

        @TruffleBoundary
        private static String toString(Object name) {
            return name.toString();
        }

        protected static boolean indexMethod(DoesRespondDispatchHeadNode definedIndexNode, DynamicObject receiver) {
            return methodDefined(receiver, INDEX_METHOD_NAME, definedIndexNode) &&
                    !RubyGuards.isRubyArray(receiver) &&
                    !RubyGuards.isRubyHash(receiver) &&
                    !RubyGuards.isRubyProc(receiver) &&
                    !RubyGuards.isRubyClass(receiver);
        }

        protected static boolean methodDefined(
                DynamicObject receiver,
                String name,
                DoesRespondDispatchHeadNode definedNode) {

            if (name == null) {
                return false;
            } else {
                return definedNode.doesRespondTo(null, name, receiver);
            }
        }
    }


    @ExportMessage
    public static class WriteMember {

        protected final static String INDEX_SET_METHOD_NAME = "[]=";

        @Specialization(guards = { "isIVar(name)" })
        protected static void writeInstanceVariable(DynamicObject receiver, String name, Object value,
                @Cached WriteObjectFieldNode writeObjectFieldNode) {
            writeObjectFieldNode.write(receiver, name, value);
        }

        @Specialization(guards = { "!isIVar(name)", "indexSetMethod(receiver, doesRespond)" })
        protected static void index(DynamicObject receiver, String name, Object value,
                @Cached ForeignToRubyNode nameToRubyNode,
                @Cached CallDispatchHeadNode dispatch,
                @Cached DoesRespondDispatchHeadNode doesRespond) {
            dispatch.call(receiver, INDEX_SET_METHOD_NAME, nameToRubyNode.executeConvert(name), value);
        }

        @Specialization(guards = { "!isIVar(name)", "!indexSetMethod(receiver, doesRespond)" })
        protected static void unknownIdentifier(DynamicObject receiver, String name, Object value,
                @Cached DoesRespondDispatchHeadNode doesRespond) throws UnknownIdentifierException {
            throw UnknownIdentifierException.create(StringUtils.toString(name));
        }

        protected static boolean indexSetMethod(DynamicObject receiver, DoesRespondDispatchHeadNode doesRespond) {
            return methodDefined(receiver, INDEX_SET_METHOD_NAME, doesRespond) &&
                    !RubyGuards.isRubyArray(receiver) &&
                    !RubyGuards.isRubyHash(receiver);
        }

        protected static boolean methodDefined(DynamicObject receiver, Object stringName,
                DoesRespondDispatchHeadNode definedNode) {
            if (stringName == null) {
                return false;
            } else {
                return definedNode.doesRespondTo(null, stringName, receiver);
            }
        }
    }

    @ExportMessage
    public static void removeMember(
            DynamicObject receiver,
            String name,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode,
            @Exclusive @Cached CallDispatchHeadNode hashDeleteNode,
            @Exclusive @Cached CallDispatchHeadNode hashKeyNode,
            @Exclusive @Cached BooleanCastNode booleanCast,
            @Exclusive @Cached CallDispatchHeadNode removeInstanceVariableNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile) throws UnknownIdentifierException {

        // TODO (pitr-ch 19-Mar-2019): profile
        if (!name.isEmpty() && name.charAt(0) == '@') {
            removeInstanceVariableNode.call(
                    receiver,
                    "remove_instance_variable",
                    foreignToRubyNode.executeConvert(name));
            return;
        }

        // TODO (pitr-ch 19-Mar-2019): error or not defined ivar?
        // TODO (pitr-ch 19-Mar-2019): use UnsupportedMessageException on name not starting with @?
        errorProfile.enter();
        throw UnknownIdentifierException.create(name);
    }

    @ExportMessage
    public static boolean isMemberReadable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_key_readable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberModifiable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_key_modifiable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberInsertable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_key_insertable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberRemovable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_key_removable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberInvocable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_key_invocable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberInternal(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                "object_key_internal?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean hasMemberReadSideEffects(DynamicObject receiver, String name) {
        // TODO (pitr-ch 29-May-2019): is that always true?
        return false;
    }

    @ExportMessage
    public static boolean hasMemberWriteSideEffects(DynamicObject receiver, String name) {
        // TODO (pitr-ch 29-May-2019): is that always true?
        return false;
    }

    @ExportMessage
    public static Object invokeMember(
            DynamicObject receiver,
            String name,
            Object[] arguments,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return dispatchNode.call(receiver, name, foreignToRubyArgumentsNode.executeConvert(arguments));
    }

    @ExportMessage
    public static boolean isInstantiable(
            DynamicObject receiver,
            @Exclusive @Cached DoesRespondDispatchHeadNode doesRespond) {
        return doesRespond.doesRespondTo(null, "new", receiver);
    }

    @ExportMessage
    public static Object instantiate(
            DynamicObject receiver, Object[] arguments,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode)
            throws UnsupportedMessageException {

        Object instance = dispatchNode.call(receiver, "new", foreignToRubyArgumentsNode.executeConvert(arguments));

        // TODO (pitr-ch 28-Jan-2020): we should translate argument-error caused by bad arity to ArityException
        // TODO (pitr-ch 04-Feb-2020): should we throw UnsupportedTypeException? Defined - if one of the arguments is not compatible to the executable signature
        if (instance == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return instance;
    }

}
