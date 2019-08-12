/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.truffleruby.RubyContext;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;

public class LexicalScope {

    public static Iterable<Scope> getLexicalScopeFor(RubyContext context, Node node, Frame frame) {
        final RootNode root = node.getRootNode();

        final Object receiver;

        if (frame == null) {
            receiver = context.getCoreLibrary().getNil();
        } else {
            receiver = RubyArguments.getSelf(frame);
        }

        final Scope topScope = Scope.newBuilder(root.getName(), getVariables(context, root, frame)).node(root).receiver("self", receiver).arguments(getArguments(frame)).build();

        // TODO CS 22-Apr-19 we only support the top-most scope at the moment - not scopes captured in blocks

        return Collections.singletonList(topScope);
    }

    private static Object getVariables(RubyContext context, RootNode root, Frame frame) {
        final FrameDescriptor frameDescriptor;

        if (frame == null) {
            frameDescriptor = root.getFrameDescriptor();
        } else {
            frameDescriptor = frame.getFrameDescriptor();
        }

        final Map<String, FrameSlot> slots = new HashMap<>();

        for (FrameSlot slot : frameDescriptor.getSlots()) {
            if (!isInternal(slot)) {
                slots.put(slot.getIdentifier().toString(), slot);
            }
        }

        return new LocalVariablesObject(context, slots, frame);
    }

    private static Object getArguments(Frame frame) {
        final Object[] args;

        if (frame == null) {
            args = new Object[0];
        } else {
            args = RubyArguments.getArguments(frame);
        }

        return new ArgumentsArrayObject(args);
    }

    private static boolean isInternal(FrameSlot slot) {
        return BindingNodes.isHiddenVariable(slot.getIdentifier());
    }

    @ExportLibrary(InteropLibrary.class)
    public static class LocalVariablesObject implements TruffleObject {

        private final RubyContext context;
        private final Map<String, ? extends FrameSlot> slots;
        private final Frame frame;

        private LocalVariablesObject(RubyContext context, Map<String, ? extends FrameSlot> slots, Frame frame) {
            this.context = context;
            this.slots = slots;
            this.frame = frame;
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean hasMembers() {
            return true;
        }

        @ExportMessage
        @TruffleBoundary
        protected Object readMember(String member) throws UnknownIdentifierException {
            if (frame == null) {
                return context.getCoreLibrary().getNil();
            } else {
                final FrameSlot slot = slots.get(member);
                if (slot == null) {
                    throw UnknownIdentifierException.create(member);
                } else {
                    return frame.getValue(slot);
                }
            }
        }

        @ExportMessage
        @TruffleBoundary
        protected Object getMembers(@SuppressWarnings("unused") boolean includeInternal) {
            return new VariableNamesObject(slots.keySet().toArray(new String[slots.size()]));
        }

        @ExportMessage
        @TruffleBoundary
        protected boolean isMemberReadable(String member) {
            return slots.containsKey(member);
        }

        @ExportMessage
        @TruffleBoundary
        protected boolean isMemberModifiable(String member) {
            return frame != null && slots.containsKey(member);
        }

        @ExportMessage
        @TruffleBoundary
        protected void writeMember(String member, Object value) throws UnknownIdentifierException, UnsupportedMessageException {
            if (frame == null) {
                throw UnsupportedMessageException.create();
            } else {
                final FrameSlot slot = slots.get(member);
                if (slot == null) {
                    throw UnknownIdentifierException.create(member);
                } else {
                    frame.setObject(slot, value);
                }
            }
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean isMemberInsertable(@SuppressWarnings("unused") String member) {
            return false;
        }

    }

    @ExportLibrary(InteropLibrary.class)
    public static class ArgumentsArrayObject implements TruffleObject {

        private final Object[] args;

        protected ArgumentsArrayObject(Object[] args) {
            this.args = args;
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean hasArrayElements() {
            return true;
        }

        @ExportMessage
        protected long getArraySize() {
            return args.length;
        }

        @ExportMessage
        @TruffleBoundary
        protected Object readArrayElement(long index) throws InvalidArrayIndexException {
            if (isArrayElementReadable(index)) {
                return args[(int) index];
            } else {
                throw InvalidArrayIndexException.create(index);
            }
        }

        @ExportMessage(name = "isArrayElementReadable")
        @ExportMessage(name = "isArrayElementModifiable")
        protected boolean isArrayElementReadable(long index) {
            return index >= 0 && index < args.length;
        }

        @SuppressWarnings("static-method")
        @ExportMessage
        protected boolean isArrayElementInsertable(@SuppressWarnings("unused") long index) {
            return false;
        }

        @ExportMessage
        protected void writeArrayElement(long index, Object value) throws InvalidArrayIndexException {
            if (isArrayElementReadable(index)) {
                args[(int) index] = value;
            } else {
                throw InvalidArrayIndexException.create(index);
            }
        }
    }

}
