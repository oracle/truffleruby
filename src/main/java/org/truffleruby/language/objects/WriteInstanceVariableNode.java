/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.library.RubyLibrary;

public class WriteInstanceVariableNode extends RubyContextSourceNode implements AssignableNode {

    private final String name;

    @Child private RubyNode receiver;
    @Child private RubyLibrary rubyLibrary;
    @Child private RubyNode rhs;
    @Child private WriteObjectFieldNode writeNode;

    @CompilationFinal private boolean frozenProfile;

    public WriteInstanceVariableNode(String name, RubyNode receiver, RubyNode rhs) {
        this.name = name;
        this.receiver = receiver;
        this.rhs = rhs;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object object = receiver.execute(frame);
        final Object value = rhs.execute(frame);
        write(object, value);
        return value;
    }

    @Override
    public void assign(VirtualFrame frame, Object value) {
        final Object object = receiver.execute(frame);
        write(object, value);
    }

    private void write(Object object, Object value) {
        if (getRubyLibrary().isFrozen(object)) {
            if (!frozenProfile) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                frozenProfile = true;
            }
            throw new RaiseException(getContext(), coreExceptions().frozenError(object, this));
        }

        if (writeNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            writeNode = insert(WriteObjectFieldNode.create());
        }

        writeNode.execute((RubyDynamicObject) object, name, value);
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return FrozenStrings.ASSIGNMENT;
    }

    private RubyLibrary getRubyLibrary() {
        if (rubyLibrary == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            rubyLibrary = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
        }
        return rubyLibrary;
    }

    @Override
    public AssignableNode toAssignableNode() {
        this.rhs = null;
        return this;
    }

    @Override
    public AssignableNode cloneUninitializedAssignable() {
        return (AssignableNode) cloneUninitialized();
    }

    @Override
    public RubyNode cloneUninitialized() {
        var rhsCopy = (rhs == null) ? null : rhs.cloneUninitialized();
        var copy = new WriteInstanceVariableNode(
                name,
                receiver.cloneUninitialized(),
                rhsCopy);
        copy.copyFlags(this);
        return copy;
    }

}
