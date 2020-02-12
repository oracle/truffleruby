/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop.messages;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsFrozenNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
public class ArrayMessages extends RubyObjectMessages {

    @ExportMessage
    public static boolean hasArrayElements(DynamicObject array) {
        return true;
    }

    @ExportMessage
    public static long getArraySize(DynamicObject array) {
        return Layouts.ARRAY.getSize(array);
    }

    @ExportMessage
    public static Object readArrayElement(
            DynamicObject array,
            long index,
            @Cached @Shared("error") BranchProfile errorProfile,
            // FIXME (pitr 11-Feb-2020): use ArrayReadNormalizedNode
            // @Cached ArrayReadNormalizedNode readNode)
            @Cached @Exclusive CallDispatchHeadNode dispatch)
            throws InvalidArrayIndexException, UnsupportedMessageException {
        if (inBounds(array, index)) {
            // return readNode.executeRead(array, (int) index);
            return dispatch.call(array, "[]", index);
        } else {
            errorProfile.enter();
            if (validIndex(index)) {
                throw InvalidArrayIndexException.create(index);
            } else {
                throw UnsupportedMessageException.create();
            }
        }
    }

    @ExportMessage
    public static void writeArrayElement(
            DynamicObject array,
            long index,
            Object value,
            @Cached @Shared("error") BranchProfile errorProfile,
            // FIXME (pitr 11-Feb-2020): use ArrayWriteNormalizedNode
            // @Cached ArrayWriteNormalizedNode writeNode)
            @Cached @Exclusive CallDispatchHeadNode dispatch)
            throws UnsupportedMessageException {
        if (validIndex(index)) {
            // writeNode.executeWrite(array, (int) index, value);
            dispatch.call(array, "[]=", index, value);
        } else {
            errorProfile.enter();
            // always unsupported not just invalid index
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static void removeArrayElement(
            DynamicObject array,
            long index,
            // FIXME (pitr 11-Feb-2020): use delete-at node directly
            // @Cached ArrayNodes.DeleteAtNode deleteAtNode,
            @Cached @Exclusive CallDispatchHeadNode dispatch,
            @Cached @Shared("error") BranchProfile errorProfile)
            throws UnsupportedMessageException, InvalidArrayIndexException {

        // TODO (pitr-ch 29-Aug-2019): allow to remove element only from the end,
        //  other removals has other observable effects - elements shift
        //  It can be modified to nil instead.

        if (validIndex(index)) {
            if (index < Layouts.ARRAY.getSize(array)) {
                // deleteAtNode.executeDeleteAt(array, (int) index);
                dispatch.call(array, "delete_at", index);
            } else {
                errorProfile.enter();
                throw InvalidArrayIndexException.create(index);
            }
        } else {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public static boolean isArrayElementReadable(
            DynamicObject array, long index) {
        return inBounds(array, index);
    }

    @ExportMessage
    public static boolean isArrayElementModifiable(
            DynamicObject array, long index,
            @Cached @Shared("isFrozenNode") IsFrozenNode isFrozenNode) {
        return !isFrozenNode.execute(array) && inBounds(array, index);
    }

    @ExportMessage
    public static boolean isArrayElementRemovable(
            DynamicObject array, long index,
            @Cached @Shared("isFrozenNode") IsFrozenNode isFrozenNode) {
        return !isFrozenNode.execute(array) && inBounds(array, index);
    }

    @ExportMessage
    public static boolean isArrayElementInsertable(
            DynamicObject array, long index,
            @Cached @Shared("isFrozenNode") IsFrozenNode isFrozenNode) {
        return !isFrozenNode.execute(array) && RubyGuards.fitsInInteger(index) && index >= Layouts.ARRAY.getSize(array);
    }

    private static boolean validIndex(long index) {
        return index >= 0 && RubyGuards.fitsInInteger(index);
    }

    private static boolean inBounds(DynamicObject array, long index) {
        return index >= 0 && index < Layouts.ARRAY.getSize(array);
    }
}
