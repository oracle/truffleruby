package org.truffleruby.interop;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;

@GenerateUncached
public abstract class TranslateInteropExceptionNode extends RubyBaseNode {
    public final RuntimeException execute(InteropException exception) {
        return execute(exception, false, null, null);
    }

    public final RuntimeException executeInInvokeMember(
            InteropException exception, Object receiver, Object[] args) {
        return execute(exception, true, receiver, args);
    }

    protected abstract RuntimeException execute(
            InteropException exception,
            boolean inInvokeMember,
            Object receiver,
            Object[] args);

    @Specialization
    protected RuntimeException handle(
            UnsupportedMessageException exception,
            boolean inInvokeMember,
            Object receiver,
            Object[] args,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        RaiseException raiseException = new RaiseException(
                context,
                context.getCoreExceptions().unsupportedMessageError(exception.getMessage(), this));
        raiseException.initCause(exception);
        return raiseException;
    }

    @Specialization
    protected RuntimeException handle(
            InvalidArrayIndexException exception,
            boolean inInvokeMember,
            Object receiver,
            Object[] args,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        RaiseException raiseException = new RaiseException(
                context,
                context.getCoreExceptions().indexErrorInvalidArrayIndexException(exception, this));
        raiseException.initCause(exception);
        return raiseException;
    }

    @Specialization
    protected RuntimeException handle(
            UnknownIdentifierException exception,
            boolean inInvokeMember,
            Object receiver,
            Object[] args,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        final RaiseException raiseException;
        if (inInvokeMember) {
            raiseException = new RaiseException(
                    context,
                    context.getCoreExceptions().noMethodErrorUnknownIdentifier(
                            receiver,
                            exception.getUnknownIdentifier(),
                            args,
                            exception,
                            this));
        } else {
            raiseException = new RaiseException(
                    context,
                    context.getCoreExceptions().nameErrorUnknownIdentifierException(exception, this));
        }

        raiseException.initCause(exception);
        return raiseException;
    }

    @Specialization
    protected RuntimeException handle(
            UnsupportedTypeException exception,
            boolean inInvokeMember,
            Object receiver,
            Object[] args,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        RaiseException raiseException = new RaiseException(
                context,
                context.getCoreExceptions().typeErrorUnsupportedTypeException(exception, this));
        raiseException.initCause(exception);
        return raiseException;
    }

    @Specialization
    protected RuntimeException handle(ArityException exception, boolean inInvokeMember, Object receiver, Object[] args,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        RaiseException raiseException = new RaiseException(
                context,
                context.getCoreExceptions().argumentError(
                        exception.getActualArity(),
                        exception.getExpectedArity(),
                        this));
        raiseException.initCause(exception);
        return raiseException;
    }
}
