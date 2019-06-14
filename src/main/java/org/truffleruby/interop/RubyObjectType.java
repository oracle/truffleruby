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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
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
import com.oracle.truffle.api.object.ObjectType;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.cast.LongCastNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class RubyObjectType extends ObjectType {

    // TODO (pitr-ch 19-Mar-2019): return exceptions like UnsupportedMessageException correctly
    // TODO (pitr-ch 19-Mar-2019): replace allowUncached = true

    @Override
    public Class<?> dispatch() {
        return RubyObjectType.class;
    }

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

    @ExportMessage
    public static boolean isNull(
            DynamicObject object,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        return object == context.getCoreLibrary().getNil();
    }

    @ExportMessage
    public static boolean hasArrayElements(
            DynamicObject receiver,
            @Exclusive @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode) {
        // FIXME (pitr 18-Mar-2019): where is respond_to? :size tested
        //   rather have more explicit check then just presence of a [] method, marker module with abstract methods
        return RubyGuards.isRubyArray(receiver) ||
                (respondNode.doesRespondTo(null, "[]", receiver) &&
                        !RubyGuards.isRubyHash(receiver) &&
                        !RubyGuards.isRubyString(receiver) &&
                        !RubyGuards.isRubyInteger(receiver) &&
                        !RubyGuards.isRubyMethod(receiver) &&
                        !RubyGuards.isRubyProc(receiver));
    }

    @ExportMessage()
    public static long getArraySize(
            DynamicObject receiver,
            @Exclusive @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Cached IntegerCastNode integerCastNode,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) throws UnsupportedMessageException {
        // TODO (pitr-ch 19-Mar-2019): profile, breakdown
        if (RubyGuards.isRubyArray(receiver)) {
            return Layouts.ARRAY.getSize(receiver);
        } else if (respondNode.doesRespondTo(null, "size", receiver)) {
            return integerCastNode.executeCastInt(dispatchNode.call(receiver, "size"));
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static boolean isString(DynamicObject receiver) {
        return RubyGuards.isRubyString(receiver) || RubyGuards.isRubySymbol(receiver);
    }

    @ExportMessage
    public static String asString(
            DynamicObject receiver,
            @Cached ToJavaStringNode toJavaStringNode) throws UnsupportedMessageException {

        // TODO (pitr-ch 19-Mar-2019): profile, breakdown
        if (RubyGuards.isRubyString(receiver) || RubyGuards.isRubySymbol(receiver)) {
            return toJavaStringNode.executeToJavaString(receiver);
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    // FIXME (pitr 18-Mar-2019): replace #unbox support with testing #to_int etc.
    //   since if an object had un-box method it could be have been un-boxed
    @ExportMessage
    public static boolean isPointer(
            DynamicObject receiver,
            // TODO (pitr-ch 29-May-2019): it should share the dispatch nodes for respond to and call
            @Exclusive @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached BooleanCastNode booleanCastNode) {

        // TODO (pitr-ch 18-Mar-2019): branchProfile?
        // FIXME (pitr 26-Mar-2019): the method should have a prefix, or a marker module
        if (respondNode.doesRespondTo(null, "__pointer__?", receiver)) {
            return booleanCastNode.executeToBoolean(dispatchNode.call(receiver, "__pointer__?"));
        } else {
            return false;
        }
    }

    // FIXME (pitr 11-May-2019): allow Ruby objects to implement interop subProtocols, e.g. for array, or numbers. Not for members though.

    // FIXME (pitr 21-Mar-2019): "if-and-only-if" relation between isPointer == true and "asPointer does not throw an UnsupportedMessageException"
    // TODO (pitr-ch 18-Mar-2019): assert #pointer? #address invariant - both has to be defined
    @ExportMessage
    public static long asPointer(
            DynamicObject receiver,
            @Exclusive @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached LongCastNode longCastNode) throws UnsupportedMessageException {

        // FIXME (pitr 26-Mar-2019): the method should have a prefix, or a marker module
        if (respondNode.doesRespondTo(null, "__address__", receiver)) {
            return longCastNode.executeCastLong(dispatchNode.call(receiver, "__address__"));
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static void toNative(
            DynamicObject receiver,
            @Exclusive @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Exclusive @Cached(value = "createPrivate()",
                    allowUncached = true) CallDispatchHeadNode dispatchNode) {

        // TODO (pitr-ch 18-Mar-2019): branch profile?
        if (respondNode.doesRespondTo(null, "to_native", receiver)) {
            // FIXME (pitr 18-Mar-2019): now it returns no value;
            dispatchNode.call(receiver, "to_native");

        }

    }

    @ExportMessage
    public static Object readMember(
            DynamicObject receiver,
            String name,
            @Shared("readHelperNode") @Cached ForeignReadStringCachingHelperNode helperNode) throws UnknownIdentifierException {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        return helperNode.executeStringCachingHelper(receiver, name);
    }

    @ExportMessage
    public static Object readArrayElement(
            DynamicObject receiver,
            long index,
            @Shared("readHelperNode") @Cached ForeignReadStringCachingHelperNode helperNode) {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        try {
            return helperNode.executeStringCachingHelper(receiver, index);
        } catch (UnknownIdentifierException e) {
            throw new IllegalStateException("never happens");
        }
    }

    // TODO (pitr-ch 19-Mar-2019): move to arrayType
    @ExportMessage
    public static boolean isArrayElementReadable(
            DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_readable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_readable?",
                receiver,
                index);
    }

    @ExportMessage
    public static boolean isArrayElementModifiable(DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_modifiable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_modifiable?",
                receiver,
                index);
    }

    @ExportMessage
    public static boolean isArrayElementInsertable(DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_insertable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_insertable?",
                receiver,
                index);
    }

    @ExportMessage
    public static boolean isArrayElementRemovable(DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_removable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_removable?",
                receiver,
                index);
    }

    @ExportMessage
    public static void writeArrayElement(
            DynamicObject receiver,
            long index,
            Object value,
            @Shared("writeHelperNode") @Cached ForeignWriteStringCachingHelperNode helperNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        try {
            helperNode.executeStringCachingHelper(receiver, index, foreignToRubyNode.executeConvert(value));
        } catch (UnknownIdentifierException e) {
            throw new IllegalStateException("never happens");
        }
    }

    @ExportMessage
    public static void writeMember(
            DynamicObject receiver,
            String name,
            Object value,
            @Shared("writeHelperNode") @Cached ForeignWriteStringCachingHelperNode helperNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) throws UnknownIdentifierException {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        helperNode.executeStringCachingHelper(receiver, name, foreignToRubyNode.executeConvert(value));
    }

    @ExportMessage
    public static void removeArrayElement(
            DynamicObject receiver,
            long index,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true)
                    CallDispatchHeadNode arrayDeleteAtNode) throws UnsupportedMessageException, InvalidArrayIndexException {

        // TODO (pitr-ch 19-Mar-2019): profile
        if (RubyGuards.isRubyArray(receiver)) {
            // TODO (pitr-ch 19-Mar-2019): it was only checking that it fits into int before
            if (RubyGuards.fitsInInteger(index)) {
                arrayDeleteAtNode.call(receiver, "delete_at", index);
            } else {
                throw InvalidArrayIndexException.create(index);
            }
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static void removeMember(
            DynamicObject receiver,
            String name,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true)
                    CallDispatchHeadNode hashDeleteNode,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true)
                    CallDispatchHeadNode removeInstanceVariableNode) throws UnknownIdentifierException {

        // TODO (pitr-ch 19-Mar-2019): profile
        // TODO (pitr-ch 19-Mar-2019): break down
        if (RubyGuards.isRubyHash(receiver)) {
            // TODO (pitr-ch 13-May-2019): remove Hash member mapping
            Object key = foreignToRubyNode.executeConvert(name);
            hashDeleteNode.call(receiver, "delete", key);
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
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_keys",
                receiver,
                internal);
    }

    @ExportMessage
    public static boolean isMemberReadable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_readable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_readable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberModifiable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_modifiable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_modifiable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberInsertable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_insertable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_insertable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberRemovable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Shared("object_key_removable") @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_removable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberInvocable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
                "object_key_invocable?",
                receiver,
                convertedName);
    }

    @ExportMessage
    public static boolean isMemberInternal(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return (boolean) dispatchNode.call(
                context.getCoreLibrary().getTruffleInteropModule(),
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
    public static boolean isExecutable(DynamicObject receiver) {
        // TODO (pitr-ch 19-Mar-2019): break down to types
        return RubyGuards.isRubyMethod(receiver) || RubyGuards.isRubyProc(receiver);
    }

    @ExportMessage
    public static Object execute(
            DynamicObject receiver,
            Object[] arguments,
            @Cached ForeignExecuteHelperNode executeMethodNode,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return executeMethodNode.executeCall(
                receiver,
                foreignToRubyArgumentsNode.executeConvert(arguments));
    }

    @ExportMessage
    public static Object invokeMember(
            DynamicObject receiver,
            String name,
            Object[] arguments,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return dispatchNode.call(receiver, name, foreignToRubyArgumentsNode.executeConvert(arguments));
    }

    @ExportMessage
    public static boolean isInstantiable(
            DynamicObject receiver,
            @Exclusive @Cached(allowUncached = true) DoesRespondDispatchHeadNode doesRespond) {
        return doesRespond.doesRespondTo(null, "new", receiver);
    }

    @ExportMessage
    public static Object instantiate(
            DynamicObject receiver,
            Object[] arguments,
            @Exclusive @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return dispatchNode.call(receiver, "new", foreignToRubyArgumentsNode.executeConvert(arguments));
    }

}
