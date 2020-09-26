/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.Nil;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.InternalMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexicalScope {

    public static Iterable<Scope> getLexicalScopeFor(RubyContext context, Node node, MaterializedFrame frame) {
        if (frame != null) {
            final List<Scope> scopes = new ArrayList<>();
            scopes.add(scopeForFrame(context, node, frame));

            frame = RubyArguments.getDeclarationFrame(frame);
            while (frame != null) {
                scopes.add(scopeForFrame(context, null, frame));
                frame = RubyArguments.getDeclarationFrame(frame);
            }
            return scopes;
        } else {
            return Collections.singletonList(scopeForFrame(context, node, null));
        }
    }

    private static Scope scopeForFrame(RubyContext context, Node node, MaterializedFrame frame) {
        final RootNode root = node != null ? node.getRootNode() : null;
        final String name = root != null ? root.getName() : "parent scope";

        if (frame != null) {
            final Object self = RubyArguments.getSelf(frame);
            final InternalMethod method = RubyArguments.getMethod(frame);
            // TODO BJF Jul-30-2020 Add trace allocation
            final RubyMethod boundMethod = new RubyMethod(
                    context.getCoreLibrary().methodClass,
                    RubyLanguage.methodShape,
                    self,
                    method);
            return Scope
                    .newBuilder(name, getVariables(root, frame))
                    .node(root)
                    .receiver("self", self)
                    .arguments(getArguments(frame))
                    .rootInstance(boundMethod)
                    .build();
        } else {
            return Scope
                    .newBuilder(name, getVariables(root, null))
                    .node(node)
                    .receiver("self", Nil.INSTANCE)
                    .build();
        }
    }

    private static Object getVariables(RootNode root, MaterializedFrame frame) {
        final FrameDescriptor frameDescriptor;
        if (frame != null) {
            frameDescriptor = frame.getFrameDescriptor();
        } else {
            frameDescriptor = root.getFrameDescriptor();
        }

        final Map<String, FrameSlot> slots = new HashMap<>();

        for (FrameSlot slot : frameDescriptor.getSlots()) {
            if (!isInternal(slot)) {
                slots.put(slot.getIdentifier().toString(), slot);
            }
        }

        return new LocalVariablesObject(slots, frame);
    }

    private static Object getArguments(MaterializedFrame frame) {
        return new ArgumentsArrayObject(RubyArguments.getArguments(frame));
    }

    private static boolean isInternal(FrameSlot slot) {
        return BindingNodes.isHiddenVariable(slot.getIdentifier());
    }

    @ExportLibrary(InteropLibrary.class)
    public static class LocalVariablesObject implements TruffleObject {

        private final Map<String, ? extends FrameSlot> slots;
        private final MaterializedFrame frame;

        private LocalVariablesObject(Map<String, ? extends FrameSlot> slots, MaterializedFrame frame) {
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
                return Nil.INSTANCE;
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
            return new VariableNamesObject(slots.keySet().toArray(StringUtils.EMPTY_STRING_ARRAY));
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
        protected void writeMember(String member, Object value)
                throws UnknownIdentifierException, UnsupportedMessageException {
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
        protected Object readArrayElement(long index,
                @Shared("errorProfile") @Cached BranchProfile errorProfile) throws InvalidArrayIndexException {
            if (isArrayElementReadable(index)) {
                return args[(int) index];
            } else {
                errorProfile.enter();
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
        protected void writeArrayElement(long index, Object value,
                @Shared("errorProfile") @Cached BranchProfile errorProfile) throws InvalidArrayIndexException {
            if (isArrayElementReadable(index)) {
                args[(int) index] = value;
            } else {
                errorProfile.enter();
                throw InvalidArrayIndexException.create(index);
            }
        }
    }

}
