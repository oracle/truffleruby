/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.interop.StopIterationException;
import com.oracle.truffle.api.interop.UnknownKeyException;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.cast.LongCastNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.interop.TranslateInteropRubyExceptionNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchConfiguration;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.InternalRespondToNode;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.methods.GetMethodObjectNode;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.utilities.TriState;
import org.truffleruby.language.objects.shared.SharedObjects;

/** All Ruby DynamicObjects extend this. */
@ExportLibrary(RubyLibrary.class)
@ExportLibrary(InteropLibrary.class)
public abstract class RubyDynamicObject extends DynamicObject {

    private RubyClass metaClass;

    public RubyDynamicObject(RubyClass metaClass, Shape shape) {
        super(shape);
        assert metaClass != null;
        this.metaClass = metaClass;
    }

    protected RubyDynamicObject(Shape classShape, String constructorOnlyForClassClass) {
        super(classShape);
        this.metaClass = (RubyClass) this;
    }

    public final RubyClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(RubyClass metaClass) {
        SharedObjects.assertPropagateSharing(this, metaClass);
        this.metaClass = metaClass;
    }

    public final RubyClass getLogicalClass() {
        return metaClass.nonSingletonClass;
    }

    @Override
    @TruffleBoundary
    public String toString() {
        final String className = getLogicalClass().fields.getName();
        return StringUtils.format("%s@%x<%s>", getClass().getSimpleName(), System.identityHashCode(this), className);
    }

    // region RubyLibrary messages
    @ExportMessage
    public void freeze(
            @Exclusive @Cached WriteObjectFieldNode writeFrozenNode) {
        writeFrozenNode.execute(this, Layouts.FROZEN_IDENTIFIER, true);
    }

    @ExportMessage
    public boolean isFrozen(
            @CachedLibrary("this") DynamicObjectLibrary readFrozenNode) {
        return (boolean) readFrozenNode.getOrDefault(this, Layouts.FROZEN_IDENTIFIER, false);
    }
    // endregion

    // region InteropLibrary messages
    @ExportMessage
    public boolean hasLanguage() {
        return true;
    }

    @ExportMessage
    public Class<RubyLanguage> getLanguage() {
        return RubyLanguage.class;
    }

    @ExportMessage
    public Object toDisplayString(boolean allowSideEffects,
            @Exclusive @Cached DispatchNode dispatchNode,
            @CachedLibrary(limit = "2") RubyStringLibrary libString,
            @Cached KernelNodes.ToSNode kernelToSNode) {
        if (allowSideEffects) {
            Object inspect = dispatchNode.call(this, "inspect");
            if (libString.isRubyString(inspect)) {
                return inspect;
            } else {
                return kernelToSNode.executeToS(this);
            }
        } else {
            return kernelToSNode.executeToS(this);
        }
    }

    // region Identity
    /** Like {@link org.truffleruby.core.hash.HashingNodes} but simplified since {@link ObjectIDNode} for
     * RubyDynamicObject can only return long. */
    @ExportMessage
    public int identityHashCode(
            @Cached ObjectIDNode objectIDNode) {
        return (int) objectIDNode.execute(this);
    }

    @ExportMessage
    public TriState isIdenticalOrUndefined(Object other,
            @Exclusive @Cached ConditionProfile rubyObjectProfile) {
        if (rubyObjectProfile.profile(other instanceof RubyDynamicObject)) {
            return this == other ? TriState.TRUE : TriState.FALSE;
        } else {
            return TriState.UNDEFINED;
        }
    }
    // endregion

    // region MetaObject
    @ExportMessage
    public boolean hasMetaObject() {
        return true;
    }

    @ExportMessage
    public RubyClass getMetaObject(
            @Cached LogicalClassNode classNode) {
        return classNode.execute(this);
    }
    // endregion

    // region Array elements
    @ExportMessage
    public boolean hasArrayElements(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object value = dispatchNode.call(this, "polyglot_has_array_elements?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public long getArraySize(
            @Cached IntegerCastNode integerCastNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode)
            throws UnsupportedMessageException {
        Object value;
        try {
            value = dispatchNode.call(this, "polyglot_array_size");
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
    public Object readArrayElement(long index,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode)
            throws InvalidArrayIndexException, UnsupportedMessageException {
        try {
            Object value = dispatchNode.call(this, "polyglot_read_array_element", index);
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
    public void writeArrayElement(long index, Object value,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {
        try {
            Object result = dispatchNode.call(this, "polyglot_write_array_element", index, value);
            if (result == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, index, value);
        }

    }

    @ExportMessage
    public void removeArrayElement(long index,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode)
            throws UnsupportedMessageException, InvalidArrayIndexException {
        try {
            Object result = dispatchNode.call(this, "polyglot_remove_array_element", index);
            if (result == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, index);
        }
    }

    @ExportMessage
    public boolean isArrayElementReadable(long index,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object value = dispatchNode.call(this, "polyglot_array_element_readable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public boolean isArrayElementModifiable(long index,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object value = dispatchNode.call(this, "polyglot_array_element_modifiable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public boolean isArrayElementInsertable(long index,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object value = dispatchNode.call(this, "polyglot_array_element_insertable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public boolean isArrayElementRemovable(long index,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object value = dispatchNode.call(this, "polyglot_array_element_removable?", index);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }
    // endregion

    // region Hash entries
    @ExportMessage
    public boolean hasHashEntries(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        final Object value = dispatchNode.call(this, "polyglot_has_hash_entries?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public long getHashSize(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached ToLongNode toInt) throws UnsupportedMessageException {
        final Object value = dispatchNode.call(this, "polyglot_hash_size");
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return toInt.execute(value);
    }

    @ExportMessage
    public boolean isHashEntryReadable(Object key,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        final Object value = dispatchNode.call(this, "polyglot_hash_entry_readable?", key);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public boolean isHashEntryModifiable(Object key,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        final Object value = dispatchNode.call(this, "polyglot_hash_entry_modifiable?", key);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public boolean isHashEntryInsertable(Object key,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        final Object value = dispatchNode.call(this, "polyglot_hash_entry_insertable?", key);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public boolean isHashEntryRemovable(Object key,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        final Object value = dispatchNode.call(this, "polyglot_hash_entry_removable?", key);
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public Object readHashValue(Object key,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnsupportedMessageException, UnknownKeyException {
        final Object value;
        try {
            value = dispatchNode.call(this, "polyglot_read_hash_entry", key);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, key);
        }
        if (value == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return value;
    }

    @ExportMessage
    public void writeHashEntry(Object key, Object value,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnsupportedMessageException, UnknownKeyException {
        final Object result;
        try {
            result = dispatchNode.call(this, "polyglot_write_hash_entry", key, value);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, key);
        }
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public void removeHashEntry(Object key,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnsupportedMessageException, UnknownKeyException {
        final Object result;
        try {
            result = dispatchNode.call(this, "polyglot_remove_hash_entry", key);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, key);
        }
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
    }

    @ExportMessage
    public Object getHashEntriesIterator(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile) throws UnsupportedMessageException {
        final Object result = dispatchNode.call(this, "polyglot_hash_entries_iterator");
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return result;
    }

    @ExportMessage
    public Object getHashKeysIterator(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile) throws UnsupportedMessageException {
        final Object result = dispatchNode.call(this, "polyglot_hash_keys_iterator");
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return result;
    }

    @ExportMessage
    public Object getHashValuesIterator(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile) throws UnsupportedMessageException {
        final Object result = dispatchNode.call(this, "polyglot_hash_values_iterator");
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return result;
    }

    // endregion

    // region Iterable Messages
    @ExportMessage
    public boolean hasIterator(
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached IsANode isANode) {
        return isANode.executeIsA(this, context.getCoreLibrary().enumerableModule);
    }

    @ExportMessage
    public Object getIterator(
            @CachedLibrary("this") InteropLibrary interopLibrary,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached DispatchNode dispatchNode) throws UnsupportedMessageException {
        if (!interopLibrary.hasIterator(this)) {
            throw UnsupportedMessageException.create();
        }
        return dispatchNode.call(context.getCoreLibrary().truffleInteropOperationsModule, "get_iterator", this);
    }

    @ExportMessage
    public boolean isIterator(
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached IsANode isANode) {
        return isANode.executeIsA(this, context.getCoreLibrary().enumeratorClass);
    }

    @ExportMessage
    public boolean hasIteratorNextElement(
            @CachedLibrary("this") InteropLibrary interopLibrary,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) throws UnsupportedMessageException {
        if (!interopLibrary.isIterator(this)) {
            throw UnsupportedMessageException.create();
        }
        return booleanCastNode.executeToBoolean(
                dispatchNode.call(
                        context.getCoreLibrary().truffleInteropOperationsModule,
                        "enumerator_has_next?",
                        this));
    }

    @ExportMessage
    public Object getIteratorNextElement(
            @CachedLibrary("this") InteropLibrary interopLibrary,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached DispatchNode dispatchNode,
            @Exclusive @Cached IsANode isANode,
            @Exclusive @Cached ConditionProfile stopIterationProfile)
            throws UnsupportedMessageException, StopIterationException {
        if (!interopLibrary.isIterator(this)) {
            throw UnsupportedMessageException.create();
        }
        try {
            return dispatchNode.call(this, "next");
        } catch (RaiseException e) {
            if (stopIterationProfile
                    .profile(isANode.executeIsA(e.getException(), context.getCoreLibrary().stopIterationClass))) {
                throw StopIterationException.create(e);
            }
            throw e;
        }
    }
    // endregion

    // region Pointer
    @ExportMessage
    public boolean isPointer(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {

        Object value = dispatchNode.call(this, "polyglot_pointer?");
        return value != DispatchNode.MISSING && booleanCastNode.executeToBoolean(value);
    }

    @ExportMessage
    public long asPointer(
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Cached LongCastNode longCastNode) throws UnsupportedMessageException {

        Object value;
        try {
            value = dispatchNode.call(this, "polyglot_as_pointer");
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
    public void toNative(
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode) {

        dispatchNode.call(this, "polyglot_to_native");
        // we ignore the method missing, toNative never throws
    }
    // endregion

    // region Members
    @ExportMessage
    public boolean hasMembers(
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object dynamic = dispatchNode.call(this, "polyglot_has_members?");
        return dynamic == DispatchNode.MISSING || booleanCastNode.executeToBoolean(dynamic);
    }

    @ExportMessage
    public Object getMembers(boolean internal,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached DispatchNode dispatchNode) {
        return dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                // language=ruby prefix=Truffle::Interop.
                "get_members_implementation",
                this,
                internal);
    }

    private static boolean isIVar(String name) {
        return !name.isEmpty() && name.charAt(0) == '@';
    }

    @ExportMessage
    public Object readMember(String name,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Cached @Shared("definedNode") InternalRespondToNode definedNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Cached GetMethodObjectNode getMethodObjectNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached ConditionProfile ivarFoundProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException, UnsupportedMessageException {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic;
        try {
            dynamic = dispatchNode.call(this, "polyglot_read_member", rubyName);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, name);
        }

        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object iVar = objectLibrary.getOrDefault(this, name, null);
            if (ivarFoundProfile.profile(iVar != null)) {
                return iVar;
            } else if (definedNode.execute(null, this, name)) {
                return getMethodObjectNode.execute(null, this, rubyName, DispatchConfiguration.PRIVATE, null);
            } else {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
        } else {
            return dynamic;
        }
    }

    @ExportMessage
    public void writeMember(String name, Object value,
            @Cached WriteObjectFieldNode writeObjectFieldNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException, UnsupportedMessageException {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic;
        try {
            dynamic = dispatchNode.call(this, "polyglot_write_member", rubyName, value);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, name);
        }

        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (rubyLibrary.isFrozen(this)) {
                errorProfile.enter();
                throw UnsupportedMessageException.create();
            }
            if (isIVar(name)) {
                writeObjectFieldNode.execute(this, name, value);
            } else {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
        }
    }

    @ExportMessage
    public void removeMember(String name,
            @Exclusive @Cached ForeignToRubyNode foreignToRubyNode,
            @Exclusive @Cached DispatchNode removeInstanceVariableNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @CachedLibrary("this") InteropLibrary interopLibrary)
            throws UnknownIdentifierException, UnsupportedMessageException {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic;
        try {
            dynamic = dispatchNode.call(this, "polyglot_remove_member", rubyName);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, name);
        }

        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (!interopLibrary.isMemberRemovable(this, name)) {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
            try {
                removeInstanceVariableNode.call(this, "remove_instance_variable", rubyName);
            } catch (RaiseException e) { // raises only if the name is missing
                // concurrent change in whether the member is removable
                errorProfile.enter();
                throw UnknownIdentifierException.create(name, e);
            }
        }
    }

    @ExportMessage
    public Object invokeMember(String name, Object[] arguments,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchDynamic,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchMember,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Shared("translateRubyException") @Cached TranslateInteropRubyExceptionNode translateRubyException,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException, UnsupportedTypeException, UnsupportedMessageException, ArityException {
        Object[] convertedArguments = foreignToRubyArgumentsNode.executeConvert(arguments);
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object[] combinedArguments = ArrayUtils.unshift(convertedArguments, rubyName);
        Object dynamic;
        try {
            dynamic = dispatchDynamic.call(this, "polyglot_invoke_member", combinedArguments);
        } catch (RaiseException e) {
            throw translateRubyException.execute(e, name, arguments);
        }

        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object result = dispatchMember.call(this, name, convertedArguments);
            if (result == DispatchNode.MISSING) {
                errorProfile.enter();
                throw UnknownIdentifierException.create(name);
            }
            return result;
        }
        return dynamic;
    }

    @ExportMessage
    public boolean isMemberReadable(String name,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Cached @Shared("definedNode") InternalRespondToNode definedNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached ConditionProfile ivarFoundProfile) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_member_readable?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (ivarFoundProfile.profile(objectLibrary.containsKey(this, name))) {
                return true;
            } else {
                return definedNode.execute(null, this, name);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    public boolean isMemberModifiable(String name,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_member_modifiable?", rubyName);
        return isMemberModifiableRemovable(
                dynamic,
                name,
                rubyLibrary,
                objectLibrary,
                booleanCastNode,
                dynamicProfile);
    }

    @ExportMessage
    public boolean isMemberRemovable(String name,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_member_removable?", rubyName);
        return isMemberModifiableRemovable(
                dynamic,
                name,
                rubyLibrary,
                objectLibrary,
                booleanCastNode,
                dynamicProfile);
    }

    private boolean isMemberModifiableRemovable(Object dynamic,
            String name,
            RubyLibrary rubyLibrary,
            DynamicObjectLibrary objectLibrary,
            BooleanCastNode booleanCastNode,
            ConditionProfile dynamicProfile) {
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (rubyLibrary.isFrozen(this)) {
                return false;
            } else {
                return objectLibrary.containsKey(this, name);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    public boolean isMemberInsertable(String name,
            @CachedLibrary("this") RubyLibrary rubyLibrary,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_member_insertable?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            if (rubyLibrary.isFrozen(this) || !isIVar(name)) {
                return false;
            } else {
                return !objectLibrary.containsKey(this, name);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    public boolean isMemberInvocable(String name,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Cached @Shared("definedNode") InternalRespondToNode definedNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached ConditionProfile ivarFoundProfile) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_member_invocable?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object iVar = objectLibrary.getOrDefault(this, name, null);
            if (ivarFoundProfile.profile(iVar != null)) {
                return false;
            } else {
                return definedNode.execute(null, this, name);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    public boolean isMemberInternal(String name,
            @CachedLibrary("this") DynamicObjectLibrary objectLibrary,
            @Cached @Shared("definedNode") InternalRespondToNode definedNode,
            @Exclusive @Cached(parameters = "PUBLIC") InternalRespondToNode definedPublicNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached BooleanCastNode booleanCastNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Shared("ivarFoundProfile") @Cached ConditionProfile ivarFoundProfile) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_member_internal?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            Object result = objectLibrary.getOrDefault(this, name, null);
            if (ivarFoundProfile.profile(result != null)) {
                return true;
            } else {
                // defined but not publicly
                return definedNode.execute(null, this, name) &&
                        !definedPublicNode.execute(null, this, name);
            }
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    public boolean hasMemberReadSideEffects(String name,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_has_member_read_side_effects?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            return false;
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }

    @ExportMessage
    public boolean hasMemberWriteSideEffects(String name,
            @Cached @Shared("nameToRubyNode") ForeignToRubyNode nameToRubyNode,
            @Exclusive @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode dispatchNode,
            @Shared("dynamicProfile") @Cached ConditionProfile dynamicProfile,
            @Exclusive @Cached BooleanCastNode booleanCastNode) {
        Object rubyName = nameToRubyNode.executeConvert(name);
        Object dynamic = dispatchNode.call(this, "polyglot_has_member_write_side_effects?", rubyName);
        if (dynamicProfile.profile(dynamic == DispatchNode.MISSING)) {
            return false;
        } else {
            return booleanCastNode.executeToBoolean(dynamic);
        }
    }
    // endregion

    // region Instantiable
    @ExportMessage
    public boolean isInstantiable(
            @Exclusive @Cached(parameters = "PUBLIC") InternalRespondToNode doesRespond) {
        return doesRespond.execute(null, this, "new");
    }

    @ExportMessage
    public Object instantiate(Object[] arguments,
            @Shared("errorProfile") @Cached BranchProfile errorProfile,
            @Exclusive @Cached(parameters = "PUBLIC_RETURN_MISSING") DispatchNode dispatchNode,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode)
            throws UnsupportedMessageException {
        Object instance = dispatchNode.call(this, "new", foreignToRubyArgumentsNode.executeConvert(arguments));

        // TODO (pitr-ch 28-Jan-2020): we should translate argument-error caused by bad arity to ArityException
        if (instance == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnsupportedMessageException.create();
        }
        return instance;
    }
    // endregion
    // endregion

    public static Node getNode(RubyLibrary node) {
        if (!node.isAdoptable()) {
            return EncapsulatingNodeReference.getCurrent().get();
        }
        return node;
    }

}
