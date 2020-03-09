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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.cast.LongCastNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
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

    @ExportMessage
    protected static boolean hasArrayElements(
            DynamicObject receiver,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_has_array_elements?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    protected static long getArraySize(
            DynamicObject receiver,
            @Cached IntegerCastNode integerCastNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws UnsupportedMessageException {

        Object value;
        try {
            value = dispatchNode.call(receiver, "polyglot_array_size");
        } catch (RaiseException e) {
            throw translateRubyException.execute(e);
        }
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return integerCastNode.executeCastInt(value);
    }

    @ExportMessage
    @SuppressWarnings("unused") // because of throws in ArrayMessages
    protected static Object readArrayElement(
            DynamicObject receiver, long index,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws InvalidArrayIndexException, UnsupportedMessageException {

        try {
            Object value = dispatchNode.call(receiver, "polyglot_read_array_element", index);
            if (value == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
            return value;
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, index);
        }
    }

    @ExportMessage
    @SuppressWarnings("unused") // because of throws in ArrayMessages
    protected static void writeArrayElement(
            DynamicObject receiver, long index, Object value,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {

        try {
            Object result = dispatchNode.call(receiver, "polyglot_write_array_element", index, value);
            if (result == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, index, value);
        }

    }

    @ExportMessage
    protected static void removeArrayElement(
            DynamicObject receiver, long index,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode)
            throws UnsupportedMessageException, InvalidArrayIndexException {

        try {
            Object result = dispatchNode.call(receiver, "polyglot_remove_array_element", index);
            if (result == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, index);
        }
    }

    @ExportMessage
    protected static boolean isArrayElementReadable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_element_readable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    protected static boolean isArrayElementModifiable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_element_modifiable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    protected static boolean isArrayElementInsertable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_element_insertable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    protected static boolean isArrayElementRemovable(
            DynamicObject receiver, long index,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_array_element_removable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    protected static boolean isPointer(
            DynamicObject receiver,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(receiver, "polyglot_pointer?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    protected static long asPointer(
            DynamicObject receiver,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Cached LongCastNode longCastNode) throws UnsupportedMessageException {

        Object value;
        try {
            value = dispatchNode.call(receiver, "polyglot_as_pointer");
        } catch (RaiseException e) {
            throw translateRubyException.execute(e);
        }
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return longCastNode.executeCastLong(value);
    }

    @ExportMessage
    protected static void toNative(
            DynamicObject receiver,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode) {

        dispatchNode.call(receiver, "polyglot_to_native");
        // we ignore the method missing, toNative never throws
    }

    @ExportMessage
    protected static boolean hasMembers(DynamicObject receiver,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object dynamic = dispatchNode.call(receiver, "polyglot_has_members?");
        return dynamic == DispatchNode.MISSING || booleanCastNode.executeToBoolean(dynamic);
    }

    @ExportMessage
    protected static Object getMembers(
            DynamicObject receiver,
            boolean internal,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode) {

        return dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                // language=ruby prefix=Truffle::Interop.
                "get_members_implementation",
                receiver,
                internal);
    }

    protected static boolean isIVar(String name) {
        return !name.isEmpty() && name.charAt(0) == '@';
    }

    @ExportMessage
    protected static Object readMember(DynamicObject receiver, String name,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Cached @Exclusive CallDispatchHeadNode dispatch,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached("createBinaryProfile()") ConditionProfile ivarFoundProfile,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_read_member", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object iVar = readObjectFieldNode.execute(receiver, name, null);
            if (ivarFoundProfile.profile(iVar != null)) {
                return iVar;
            } else if (definedNode.doesRespondTo(null, name, receiver)) {
                return dispatch.call(receiver, "method", rubyName);
            } else {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
        } else {
            return dynamic;
        }
    }

    @ExportMessage
    protected static void writeMember(DynamicObject receiver, String name, Object value,
            @Cached WriteObjectFieldNode writeObjectFieldNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_write_member", rubyName, value);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (isIVar(name)) {
                writeObjectFieldNode.write(receiver, name, value);
            } else {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
        }
    }

    @ExportMessage
    protected static void removeMember(
            DynamicObject receiver,
            String name,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode,
            @Exclusive @Cached CallDispatchHeadNode removeInstanceVariableNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException, UnsupportedMessageException {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_remove_member", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (!isIVar(name)) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
            try {
                removeInstanceVariableNode.call(receiver, "remove_instance_variable", rubyName);
            } catch (RaiseException e) { // raises only if the name is missing
                errorProfile.enter();
                UnknownIdentifierException unknownIdentifier = UnknownIdentifierException.create(name);
                ExceptionOperations.initCause(unknownIdentifier, e);
                throw unknownIdentifier;
            }
        }
    }

    @ExportMessage
    protected static Object invokeMember(
            DynamicObject receiver,
            String name,
            Object[] arguments,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchDynamic,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchMember,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile) throws UnknownIdentifierException {

        Object[] convertedArguments = foreignToRubyArgumentsNode.executeConvert(arguments);
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchDynamic.call(receiver, "polyglot_invoke_member", rubyName, convertedArguments);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object result = dispatchMember.call(receiver, name, convertedArguments);
            if (result == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
            return result;
        }

        return dynamic;
    }

    @ExportMessage
    protected static boolean isMemberReadable(DynamicObject receiver, String name,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached("createBinaryProfile()") ConditionProfile ivarFoundProfile) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_member_readable?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object iVar = readObjectFieldNode.execute(receiver, name, null);
            if (ivarFoundProfile.profile(iVar != null)) {
                return true;
            } else {
                return definedNode.doesRespondTo(null, name, receiver);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean isMemberModifiable(DynamicObject receiver, String name,
            @Cached @Shared("frozen") IsFrozenNode isFrozenNode,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_member_modifiable?", rubyName);
        return isMemberModifiableRemovable(
                dynamic,
                receiver,
                name,
                isFrozenNode,
                readObjectFieldNode,
                booleanCastNode,
                dynamicProfile);
    }

    @ExportMessage
    protected static boolean isMemberRemovable(DynamicObject receiver, String name,
            @Cached @Shared("frozen") IsFrozenNode isFrozenNode,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_member_removable?", rubyName);
        return isMemberModifiableRemovable(
                dynamic,
                receiver,
                name,
                isFrozenNode,
                readObjectFieldNode,
                booleanCastNode,
                dynamicProfile);
    }

    private static boolean isMemberModifiableRemovable(Object dynamic, DynamicObject receiver, String name,
            IsFrozenNode isFrozenNode,
            ReadObjectFieldNode readObjectFieldNode,
            BooleanCastNode booleanCastNode,
            ConditionProfile dynamicProfile) {
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (isFrozenNode.execute(receiver)) {
                return false;
            } else {
                return readObjectFieldNode.execute(receiver, name, null) != null;
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean isMemberInsertable(DynamicObject receiver, String name,
            @Cached @Shared("frozen") IsFrozenNode isFrozenNode,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_member_insertable?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (isFrozenNode.execute(receiver) || !isIVar(name)) {
                return false;
            } else {
                return readObjectFieldNode.execute(receiver, name, null) == null;
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean isMemberInvocable(DynamicObject receiver, String name,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached("createBinaryProfile()") ConditionProfile ivarFoundProfile) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_member_invocable?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object iVar = readObjectFieldNode.execute(receiver, name, null);
            if (ivarFoundProfile.profile(iVar != null)) {
                return false;
            } else {
                return definedNode.doesRespondTo(null, name, receiver);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean isMemberInternal(DynamicObject receiver, String name,
            @Cached @Shared("readObjectFieldNode") ReadObjectFieldNode readObjectFieldNode,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode,
            @Exclusive @Cached(parameters = "PUBLIC") DoesRespondDispatchHeadNode definedPublicNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached("createBinaryProfile()") ConditionProfile ivarFoundProfile) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_member_internal?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object result = readObjectFieldNode.execute(receiver, name, null);
            if (ivarFoundProfile.profile(result != null)) {
                return true;
            } else {
                // defined but not publicly
                return definedNode.doesRespondTo(null, name, receiver) &&
                        !definedPublicNode.doesRespondTo(null, name, receiver);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean hasMemberReadSideEffects(DynamicObject receiver, String name,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_has_member_read_side_effects?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            return false;
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean hasMemberWriteSideEffects(DynamicObject receiver, String name,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchNode,
            @Shared("dynamicProfile") @Cached("createBinaryProfile()") ConditionProfile dynamicProfile,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(receiver, "polyglot_has_member_write_side_effects?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            return false;
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    protected static boolean isInstantiable(DynamicObject receiver,
            @Exclusive @Cached(parameters = "PUBLIC") DoesRespondDispatchHeadNode doesRespond) {
        return doesRespond.doesRespondTo(null, "new", receiver);
    }

    @ExportMessage
    protected static Object instantiate(DynamicObject receiver, Object[] arguments,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "PUBLIC_RETURN_MISSING") CallDispatchHeadNode dispatchNode,
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
