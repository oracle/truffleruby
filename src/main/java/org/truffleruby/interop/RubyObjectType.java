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
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.KeyInfo;
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
            @CachedContext(RubyLanguage.class) RubyContext rubyContext) {
        return object == rubyContext.getCoreLibrary().getNil();
    }

    @ExportMessage
    public static boolean hasArrayElements(
            DynamicObject receiver,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode) {
        // FIXME (pitr 18-Mar-2019): where is respond_to? :size tested
        return RubyGuards.isRubyArray(receiver) ||
                (respondNode.doesRespondTo(null, "[]", receiver) &&
                        !RubyGuards.isRubyHash(receiver) &&
                        !RubyGuards.isRubyString(receiver) &&
                        !RubyGuards.isRubyInteger(receiver) &&
                        !RubyGuards.isRubyProc(receiver));
    }

    @ExportMessage()
    public static long getArraySize(
            DynamicObject receiver,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Cached(allowUncached = true) IntegerCastNode integerCastNode,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) throws UnsupportedMessageException {
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
            @Cached(allowUncached = true) ToJavaStringNode toJavaStringNode) throws UnsupportedMessageException {

        // TODO (pitr-ch 19-Mar-2019): profile, breakdown
        if (RubyGuards.isRubyString(receiver) || RubyGuards.isRubySymbol(receiver)) {
            return toJavaStringNode.executeToJavaString(receiver);
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    // FIXME (pitr 18-Mar-2019): replace #unbox support with testing #to_int etc.
    // if an object had un-box method it could be have been un-boxed
    @ExportMessage
    public static boolean isPointer(
            DynamicObject receiver,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) BooleanCastNode booleanCastNode) {

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
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Cached(value = "createPrivate()",
                    allowUncached = true) CallDispatchHeadNode dispatchNode) throws UnsupportedMessageException {

        // FIXME (pitr 26-Mar-2019): the method should have a prefix, or a marker module
        if (respondNode.doesRespondTo(null, "__address__", receiver)) {
            // TODO (pitr-ch 18-Mar-2019): cast to long with a node
            Object result = dispatchNode.call(receiver, "__address__");
            if (result instanceof Integer) {
                // TODO (pitr-ch 18-Mar-2019): missing comment
                result = (long) ((int) result);
            }
            return (long) result;
        } else {
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static void toNative(
            DynamicObject receiver,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode respondNode,
            @Cached(value = "createPrivate()",
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
            @Cached(allowUncached = true) ForeignReadStringCachingHelperNode helperNode) throws UnknownIdentifierException {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        return helperNode.executeStringCachingHelper(receiver, name);
    }

    @ExportMessage
    public static Object readArrayElement(
            DynamicObject receiver,
            long index,
            @Cached(allowUncached = true) ForeignReadStringCachingHelperNode helperNode) {
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
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return KeyInfo.isReadable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                index));
    }

    @ExportMessage
    public static boolean isArrayElementModifiable(DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return KeyInfo.isModifiable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                index));
    }

    @ExportMessage
    public static boolean isArrayElementInsertable(DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return KeyInfo.isInsertable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                index));
    }

    @ExportMessage
    public static boolean isArrayElementRemovable(DynamicObject receiver, long index,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return KeyInfo.isRemovable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                index));
    }

    @ExportMessage
    public static void writeArrayElement(
            DynamicObject receiver,
            long index,
            Object value,
            @Cached(allowUncached = true) ForeignWriteStringCachingHelperNode helperNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
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
            @Cached(allowUncached = true) ForeignWriteStringCachingHelperNode helperNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) throws UnknownIdentifierException {
        // TODO (pitr-ch 19-Mar-2019): break down the helper nodes into type objects
        helperNode.executeStringCachingHelper(receiver, name, foreignToRubyNode.executeConvert(value));
    }

    @ExportMessage
    public static void removeArrayElement(
            DynamicObject receiver,
            long index,
            @Cached(value = "createPrivate()", allowUncached = true)
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
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode,
            @Cached(value = "createPrivate()", allowUncached = true)
                    CallDispatchHeadNode hashDeleteNode,
            @Cached(value = "createPrivate()", allowUncached = true)
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
        // TODO (pitr-ch 19-Mar-2019): use Unsupported on name not starting with @?
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
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode) {
        return dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_keys",
                receiver,
                internal);
    }

    @ExportMessage
    public static boolean isMemberReadable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.isReadable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean isMemberModifiable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.isModifiable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean isMemberInsertable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.isInsertable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean isMemberRemovable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.isRemovable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean isMemberInvocable(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.isInvocable((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean isMemberInternal(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.isInternal((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean hasMemberReadSideEffects(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.hasReadSideEffects((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
    }

    @ExportMessage
    public static boolean hasMemberWriteSideEffects(
            DynamicObject receiver,
            String name,
            @CachedContext(RubyLanguage.class) RubyContext rubyContext,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyNode foreignToRubyNode) {
        // TODO (pitr-ch 19-Mar-2019): breakdown
        final Object convertedName = foreignToRubyNode.executeConvert(name);
        return KeyInfo.hasWriteSideEffects((int) dispatchNode.call(
                rubyContext.getCoreLibrary().getTruffleInteropModule(),
                "object_key_info",
                receiver,
                convertedName));
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
            @Cached(allowUncached = true) ForeignExecuteHelperNode executeMethodNode,
            @Cached(allowUncached = true) ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return executeMethodNode.executeCall(
                receiver,
                foreignToRubyArgumentsNode.executeConvert(arguments));
    }

    @ExportMessage
    public static Object invokeMember(
            DynamicObject receiver,
            String name,
            Object[] arguments,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return dispatchNode.call(receiver, name, foreignToRubyArgumentsNode.executeConvert(arguments));
    }

    @ExportMessage
    public static boolean isInstantiable(
            DynamicObject receiver,
            @Cached(allowUncached = true) DoesRespondDispatchHeadNode doesRespond) {
        return doesRespond.doesRespondTo(null, "new", receiver);
    }

    @ExportMessage
    public static Object instantiate(
            DynamicObject receiver,
            Object[] arguments,
            @Cached(value = "createPrivate()", allowUncached = true) CallDispatchHeadNode dispatchNode,
            @Cached(allowUncached = true) ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return dispatchNode.call(receiver, "new", foreignToRubyArgumentsNode.executeConvert(arguments));
    }

}
