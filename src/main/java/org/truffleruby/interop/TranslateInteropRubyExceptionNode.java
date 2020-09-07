/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;

@GenerateUncached
public abstract class TranslateInteropRubyExceptionNode extends RubyBaseNode {

    public final AssertionError execute(RaiseException exception)
            throws UnsupportedMessageException {
        try {
            return execute(exception, 0, null, null);
        } catch (InvalidArrayIndexException | UnknownIdentifierException | UnsupportedTypeException
                | ArityException e) {
            throw handleBadErrorType(e, exception);
        }
    }

    public final AssertionError execute(RaiseException exception, long index)
            throws UnsupportedMessageException, InvalidArrayIndexException {
        try {
            return execute(exception, index, null, null);
        } catch (UnknownIdentifierException | UnsupportedTypeException | ArityException e) {
            throw handleBadErrorType(e, exception);
        }
    }

    public final AssertionError execute(RaiseException exception, String name)
            throws UnsupportedMessageException, UnknownIdentifierException {
        try {
            return execute(exception, 0, name, null);
        } catch (InvalidArrayIndexException | UnsupportedTypeException | ArityException e) {
            throw handleBadErrorType(e, exception);
        }
    }

    public final AssertionError execute(RaiseException exception, long index, Object value)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {
        try {
            return execute(exception, index, null, new Object[]{ value });
        } catch (UnknownIdentifierException | ArityException e) {
            throw handleBadErrorType(e, exception);
        }
    }

    public final AssertionError execute(RaiseException exception, String name, Object... arguments)
            throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException, ArityException {
        try {
            return execute(exception, 0, name, arguments);
        } catch (InvalidArrayIndexException e) {
            throw handleBadErrorType(e, exception);
        }
    }

    protected abstract AssertionError execute(
            RaiseException exception, long index, String identifier, Object[] arguments)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnknownIdentifierException,
            UnsupportedTypeException, ArityException;

    @Specialization(
            guards = "logicalClassNode.executeLogicalClass(exception.getException()) == context.getCoreLibrary().unsupportedMessageExceptionClass",
            limit = "1")
    protected AssertionError unsupportedMessageExceptionClass(
            RaiseException exception,
            long index,
            String identifier,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) throws UnsupportedMessageException {
        throw UnsupportedMessageException.create(exception);
    }

    @Specialization(
            guards = "logicalClassNode.executeLogicalClass(exception.getException()) == context.getCoreLibrary().invalidArrayIndexExceptionClass",
            limit = "1")
    protected AssertionError invalidArrayIndexExceptionClass(
            RaiseException exception,
            long index,
            String identifier,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) throws InvalidArrayIndexException {
        throw InvalidArrayIndexException.create(index, exception);
    }

    @Specialization(
            guards = "logicalClassNode.executeLogicalClass(exception.getException()) == context.getCoreLibrary().unknownIdentifierExceptionClass",
            limit = "1")
    protected AssertionError unknownIdentifierExceptionClass(
            RaiseException exception,
            long index,
            String identifier,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) throws UnknownIdentifierException {
        throw UnknownIdentifierException.create(identifier, exception);
    }

    @Specialization(
            guards = "logicalClassNode.executeLogicalClass(exception.getException()) == context.getCoreLibrary().unsupportedTypeExceptionClass",
            limit = "1")
    protected AssertionError unsupportedTypeExceptionClass(
            RaiseException exception,
            long index,
            String identifier,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) throws UnsupportedTypeException {
        throw UnsupportedTypeException.create(arguments, null, exception);
    }

    @Specialization(
            guards = "logicalClassNode.executeLogicalClass(exception.getException()) == context.getCoreLibrary().arityExceptionClass",
            limit = "1")
    protected AssertionError arityExceptionClass(
            RaiseException exception,
            long index,
            String identifier,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached DispatchNode dispatch,
            @Cached IntegerCastNode intCastNode,
            @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) throws ArityException {
        int expected = intCastNode.executeCastInt(dispatch.call(exception.getException(), "expected"));
        throw ArityException.create(expected, arguments.length, exception);
    }

    @Fallback
    protected AssertionError fallback(RaiseException exception, long index, String identifier, Object[] arguments) {
        throw exception;
    }

    protected AssertionError handleBadErrorType(InteropException e, RaiseException rubyException) {
        RubyContext context = RubyLanguage.getCurrentContext();
        final RubyException exception = context.getCoreExceptions().runtimeError(
                Utils.concat("Wrong exception raised from a Ruby method implementing polyglot behavior: ", e),
                this);
        exception.cause = rubyException;
        throw new RaiseException(context, exception);
    }

}
