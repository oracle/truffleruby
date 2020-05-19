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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.interop.ForeignToRubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

/** A subset of messages from {@link org.truffleruby.interop.messages.RubyObjectMessages} for immutable objects. Such
 * objects have no instance variables, so the logic is simpler. We cannot easily reuse RubyObjectMessages here. */
@ExportLibrary(value = InteropLibrary.class)
public abstract class ImmutableRubyObject implements TruffleObject {

    @ExportMessage
    public boolean hasMembers() {
        return true;
    }

    @ExportMessage
    public Object getMembers(boolean internal,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Exclusive @Cached CallDispatchHeadNode dispatchNode) {
        return dispatchNode.call(
                context.getCoreLibrary().truffleInteropModule,
                // language=ruby prefix=Truffle::Interop.
                "get_members_implementation",
                this,
                internal);
    }

    @ExportMessage
    public boolean isMemberReadable(String name,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode) {
        return definedNode.doesRespondTo(null, name, this);
    }

    @ExportMessage
    public Object readMember(String name,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode,
            @Cached ForeignToRubyNode nameToRubyNode,
            @Cached @Exclusive CallDispatchHeadNode dispatch,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnsupportedMessageException, UnknownIdentifierException {
        if (definedNode.doesRespondTo(null, name, this)) {
            Object rubyName = nameToRubyNode.executeConvert(name);
            return dispatch.call(this, "method", rubyName);
        } else {
            errorProfile.enter();
            throw UnknownIdentifierException.create(name);
        }
    }

    @ExportMessage
    public boolean isMemberInvocable(String name,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode) {
        return definedNode.doesRespondTo(null, name, this);
    }

    @ExportMessage
    public Object invokeMember(String name, Object[] arguments,
            @Exclusive @Cached(parameters = "RETURN_MISSING") CallDispatchHeadNode dispatchMember,
            @Exclusive @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode,
            @Shared("errorProfile") @Cached BranchProfile errorProfile)
            throws UnknownIdentifierException {
        Object[] convertedArguments = foreignToRubyArgumentsNode.executeConvert(arguments);
        Object result = dispatchMember.call(this, name, convertedArguments);
        if (result == DispatchNode.MISSING) {
            errorProfile.enter();
            throw UnknownIdentifierException.create(name);
        }
        return result;
    }

    @ExportMessage
    public boolean isMemberInternal(String name,
            @Cached @Shared("definedNode") DoesRespondDispatchHeadNode definedNode,
            @Exclusive @Cached(parameters = "PUBLIC") DoesRespondDispatchHeadNode definedPublicNode) {
        // defined but not publicly
        return definedNode.doesRespondTo(null, name, this) &&
                !definedPublicNode.doesRespondTo(null, name, this);
    }

}
