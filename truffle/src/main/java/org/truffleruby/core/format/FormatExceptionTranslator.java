/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.format;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.format.exceptions.CantCompressNegativeException;
import org.truffleruby.core.format.exceptions.CantConvertException;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.exceptions.InvalidFormatException;
import org.truffleruby.core.format.exceptions.NoImplicitConversionException;
import org.truffleruby.core.format.exceptions.OutsideOfStringException;
import org.truffleruby.core.format.exceptions.RangeException;
import org.truffleruby.core.format.exceptions.TooFewArgumentsException;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

public abstract class FormatExceptionTranslator {

    @CompilerDirectives.TruffleBoundary
    public static RuntimeException translate(RubyNode currentNode, FormatException exception) {
        final CoreExceptions coreExceptions = currentNode.getContext().getCoreExceptions();

        if (exception instanceof TooFewArgumentsException) {
            return new RaiseException(coreExceptions.argumentErrorTooFewArguments(currentNode));
        } else if (exception instanceof NoImplicitConversionException) {
            final NoImplicitConversionException e = (NoImplicitConversionException) exception;
            return new RaiseException(coreExceptions.typeErrorNoImplicitConversion(e.getObject(), e.getTarget(), currentNode));
        } else if (exception instanceof OutsideOfStringException) {
            return new RaiseException(coreExceptions.argumentErrorXOutsideOfString(currentNode));
        } else if (exception instanceof CantCompressNegativeException) {
            return new RaiseException(coreExceptions.argumentErrorCantCompressNegativeNumbers(currentNode));
        } else if (exception instanceof RangeException) {
            final RangeException e = (RangeException) exception;
            return new RaiseException(coreExceptions.rangeError(e.getMessage(), currentNode));
        } else if (exception instanceof CantConvertException) {
            final CantConvertException e = (CantConvertException) exception;
            return new RaiseException(coreExceptions.typeError(e.getMessage(), currentNode));
        } else if (exception instanceof InvalidFormatException) {
            final InvalidFormatException e = (InvalidFormatException) exception;
            return new RaiseException(coreExceptions.argumentError(e.getMessage(), currentNode));

        } else {
            throw new IllegalArgumentException();
        }
    }

}
