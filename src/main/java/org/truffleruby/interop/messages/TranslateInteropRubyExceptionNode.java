package org.truffleruby.interop.messages;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.LongCastNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.LogicalClassNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
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
import org.truffleruby.utils.Utils;

@GenerateUncached
abstract class TranslateInteropRubyExceptionNode extends RubyBaseNode {

    public final AssertionError execute(RaiseException exception)
            throws UnsupportedMessageException {

        try {
            return execute(exception, 0, null, null);
        } catch (InvalidArrayIndexException | UnknownIdentifierException | UnsupportedTypeException
                | ArityException e) {
            throw handleBadErrorType(e);
        }
    }

    public final AssertionError execute(RaiseException exception, long index)
            throws UnsupportedMessageException, InvalidArrayIndexException {

        try {
            return execute(exception, index, null, null);
        } catch (UnknownIdentifierException | UnsupportedTypeException | ArityException e) {
            throw handleBadErrorType(e);
        }
    }

    public final AssertionError execute(RaiseException exception, String name)
            throws UnsupportedMessageException, UnknownIdentifierException {

        try {
            return execute(exception, 0, name, null);
        } catch (InvalidArrayIndexException | UnsupportedTypeException | ArityException e) {
            throw handleBadErrorType(e);
        }
    }

    public final AssertionError execute(RaiseException exception, long index, Object value)
            throws UnsupportedMessageException, InvalidArrayIndexException, UnsupportedTypeException {

        try {
            return execute(exception, index, null, new Object[]{ value });
        } catch (UnknownIdentifierException | ArityException e) {
            throw handleBadErrorType(e);
        }
    }

    public final AssertionError execute(RaiseException exception, String name, Object... arguments)
            throws UnsupportedMessageException, UnknownIdentifierException, UnsupportedTypeException, ArityException {

        try {
            return execute(exception, 0, name, arguments);
        } catch (InvalidArrayIndexException e) {
            throw handleBadErrorType(e);
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

        UnsupportedMessageException interopException = UnsupportedMessageException.create();
        initCause(interopException, exception);
        throw interopException;
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

        InvalidArrayIndexException interopException = InvalidArrayIndexException.create(index);
        initCause(interopException, exception);
        throw interopException;
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

        UnknownIdentifierException interopException = UnknownIdentifierException.create(identifier);
        initCause(interopException, exception);
        throw interopException;
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

        UnsupportedTypeException interopException = UnsupportedTypeException.create(arguments);
        initCause(interopException, exception);
        throw interopException;
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
            @Cached CallDispatchHeadNode dispatch,
            @Cached LongCastNode longCastNode,
            @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) throws ArityException {

        int expected = Math
                .toIntExact(longCastNode.executeCastLong(dispatch.call(exception.getException(), "expected")));
        ArityException interopException = ArityException.create(expected, arguments.length);
        initCause(interopException, exception);
        throw interopException;
    }

    @Fallback
    protected AssertionError fallback(RaiseException exception, long index, String identifier, Object[] arguments) {
        throw exception;
    }

    protected AssertionError handleBadErrorType(InteropException e) {
        RubyContext context = RubyLanguage.getCurrentContext();
        RaiseException raiseException = new RaiseException(
                context,
                context.getCoreExceptions().runtimeError(
                        Utils.concat("Wrong exception raised from a Ruby method implementing polyglot behavior: ", e),
                        this));
        initCause(raiseException, e);
        throw raiseException;
    }

    @TruffleBoundary
    private static void initCause(Throwable e, Exception cause) {
        e.initCause(cause);
    }

}
