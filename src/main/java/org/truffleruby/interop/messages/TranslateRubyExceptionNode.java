package org.truffleruby.interop.messages;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.object.DynamicObject;

@GenerateUncached
public abstract class TranslateRubyExceptionNode extends RubyBaseNode {

    public final AssertionError execute(RaiseException exception)
            throws UnsupportedMessageException {

        try {
            return execute(exception, 0, null, null);
        } catch (InvalidArrayIndexException | UnknownIdentifierException | UnsupportedTypeException e) {
            throw handleBadErrorType(e);
        }
    }

    public final AssertionError execute(RaiseException exception, long index)
            throws UnsupportedMessageException, InvalidArrayIndexException {

        try {
            return execute(exception, index, null, null);
        } catch (UnknownIdentifierException | UnsupportedTypeException e) {
            throw handleBadErrorType(e);
        }
    }

    public final AssertionError execute(RaiseException exception, long index, Object value)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {

        try {
            return execute(exception, index, null, new Object[]{ value });
        } catch (UnknownIdentifierException e) {
            throw handleBadErrorType(e);
        }
    }

    // TODO (pitr-ch 02-Mar-2020): Other execute methods for members

    protected abstract AssertionError execute(
            RaiseException exception, long index, String identifier, Object[] arguments)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnknownIdentifierException,
            UnsupportedTypeException;

    @Specialization
    protected AssertionError handle(
            RaiseException exception,
            long index,
            String identifier,
            Object[] arguments,
            @CachedContext(RubyLanguage.class) RubyContext context)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnknownIdentifierException,
            UnsupportedTypeException {

        DynamicObject logicalClass = Layouts.EXCEPTION.getLogicalClass(exception.getException());

        if (logicalClass == context.getCoreLibrary().unsupportedMessageExceptionClass) {
            UnsupportedMessageException interopException = UnsupportedMessageException.create();
            interopException.initCause(exception);
            throw interopException;
        }

        if (logicalClass == context.getCoreLibrary().invalidArrayIndexExceptionClass) {
            InvalidArrayIndexException interopException = InvalidArrayIndexException.create(index);
            interopException.initCause(exception);
            throw interopException;
        }

        if (logicalClass == context.getCoreLibrary().unknownIdentifierExceptionClass) {
            UnknownIdentifierException interopException = UnknownIdentifierException.create(identifier);
            interopException.initCause(exception);
            throw interopException;
        }

        if (logicalClass == context.getCoreLibrary().unsupportedTypeExceptionClass) {
            UnsupportedTypeException interopException = UnsupportedTypeException.create(arguments);
            interopException.initCause(exception);
            throw interopException;
        }

        throw exception;
    }

    @TruffleBoundary
    protected AssertionError handleBadErrorType(InteropException e) {
        RubyContext context = RubyLanguage.getCurrentContext();
        RaiseException raiseException = new RaiseException(
                context,
                context.getCoreExceptions().runtimeError(
                        "Wrong exception risen from a Ruby method implementing polyglot behavior: " + e.toString(),
                        this));
        raiseException.initCause(e);
        throw raiseException;
    }

}
