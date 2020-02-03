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
import org.truffleruby.interop.ForeignReadStringCachingHelperNode;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.interop.ForeignWriteStringCachingHelperNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

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
    public static Object readMember(
            DynamicObject receiver,
            String name,
            @Cached ForeignReadStringCachingHelperNode helperNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile) throws UnknownIdentifierException {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        try {
            return helperNode.executeStringCachingHelper(receiver, name);
        } catch (InvalidArrayIndexException e) {
            errorProfile.enter();
            throw new IllegalStateException("never happens");
        }
    }

    @ExportMessage
    public static void writeMember(
            DynamicObject receiver,
            String name,
            Object value,
            @Cached ForeignWriteStringCachingHelperNode helperNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) throws UnknownIdentifierException {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        helperNode.executeStringCachingHelper(receiver, name, foreignToRubyNode.executeConvert(value));
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
        // TODO (pitr-ch 19-Mar-2019): break down
        if (RubyGuards.isRubyHash(receiver)) {
            // TODO (pitr-ch 13-May-2019): remove Hash member mapping
            Object key = foreignToRubyNode.executeConvert(name);
            if (booleanCast.executeToBoolean(hashKeyNode.call(receiver, "key?", key))) {
                hashDeleteNode.call(receiver, "delete", key);
            } else {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
            return;
        }

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
