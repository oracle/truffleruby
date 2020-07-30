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

import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@ExportLibrary(value = InteropLibrary.class, receiverType = DynamicObject.class)
@ExportLibrary(value = RubyLibrary.class, receiverType = DynamicObject.class)
public class ArrayMessages extends RubyObjectMessages {

    @ExportMessage
    protected static boolean hasArrayElements(DynamicObject array) {
        return true;
    }

    @ExportMessage
    protected static long getArraySize(DynamicObject array) {
        return ((RubyArray) array).size;
    }

    @ExportMessage
    protected static Object readArrayElement(
            DynamicObject array,
            long index,
            @Cached @Shared("error") BranchProfile errorProfile,
            // FIXME (pitr 11-Feb-2020): use ArrayReadNormalizedNode
            // @Cached ArrayReadNormalizedNode readNode)
            @Cached @Exclusive CallDispatchHeadNode dispatch) throws InvalidArrayIndexException {
        if (inBounds((RubyArray) array, index)) {
            // return readNode.executeRead(array, (int) index);
            return dispatch.call(array, "[]", index);
        } else {
            errorProfile.enter();
            throw InvalidArrayIndexException.create(index);
        }
    }

    @ExportMessage
    protected static void writeArrayElement(
            DynamicObject array,
            long index,
            Object value,
            @Cached @Shared("error") BranchProfile errorProfile,
            // FIXME (pitr 11-Feb-2020): use ArrayWriteNormalizedNode
            // @Cached ArrayWriteNormalizedNode writeNode)
            @Cached @Exclusive CallDispatchHeadNode dispatch) throws InvalidArrayIndexException {
        if (index >= 0 && RubyGuards.fitsInInteger(index)) {
            // writeNode.executeWrite(array, (int) index, value);
            dispatch.call(array, "[]=", index, value);
        } else {
            errorProfile.enter();
            throw InvalidArrayIndexException.create(index);
        }
    }

    @ExportMessage
    protected static void removeArrayElement(
            DynamicObject array,
            long index,
            // FIXME (pitr 11-Feb-2020): use delete-at node directly
            // @Cached ArrayNodes.DeleteAtNode deleteAtNode,
            @Cached @Exclusive CallDispatchHeadNode dispatch,
            @Cached @Shared("error") BranchProfile errorProfile) throws InvalidArrayIndexException {

        if (inBounds((RubyArray) array, index)) {
            // deleteAtNode.executeDeleteAt(array, (int) index);
            dispatch.call(array, "delete_at", index);
        } else {
            errorProfile.enter();
            throw InvalidArrayIndexException.create(index);
        }
    }

    @ExportMessage
    protected static boolean isArrayElementReadable(DynamicObject array, long index) {
        return inBounds((RubyArray) array, index);
    }

    @ExportMessage
    protected static boolean isArrayElementModifiable(DynamicObject array, long index,
            @CachedLibrary("array") RubyLibrary rubyLibrary) {
        return !rubyLibrary.isFrozen(array) && inBounds((RubyArray) array, index);
    }

    @ExportMessage
    protected static boolean isArrayElementRemovable(DynamicObject array, long index,
            @CachedLibrary("array") RubyLibrary rubyLibrary) {
        return !rubyLibrary.isFrozen(array) && inBounds((RubyArray) array, index);
    }

    @ExportMessage
    protected static boolean isArrayElementInsertable(DynamicObject array, long index,
            @CachedLibrary("array") RubyLibrary rubyLibrary) {
        return !rubyLibrary.isFrozen(array) && RubyGuards.fitsInInteger(index) &&
                index >= ((RubyArray) array).size;
    }

    private static boolean inBounds(RubyArray array, long index) {
        return index >= 0 && index < array.size;
    }
}
