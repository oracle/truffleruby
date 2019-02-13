/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import static org.truffleruby.core.array.ArrayHelpers.createArray;

import java.util.EnumSet;

import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.encoding.EncodingOperations;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.thread.ThreadNodes.ThreadGetExceptionNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.BacktraceFormatter.FormattingFlags;
import org.truffleruby.platform.ErrnoDescriptions;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

public class CoreExceptions {

    private final RubyContext context;
    private final BacktraceFormatter debugBacktraceFormatter;

    public CoreExceptions(RubyContext context) {
        this.context = context;
        this.debugBacktraceFormatter = new BacktraceFormatter(context, EnumSet.of(FormattingFlags.OMIT_EXCEPTION));
    }

    public void showExceptionIfDebug(DynamicObject exception) {
        showExceptionIfDebug(
                Layouts.EXCEPTION.getLogicalClass(exception),
                Layouts.EXCEPTION.getMessage(exception),
                Layouts.EXCEPTION.getBacktrace(exception));
    }

    @TruffleBoundary
    public void showExceptionIfDebug(DynamicObject rubyException, Backtrace backtrace) {
        if (context.getCoreLibrary().getDebug() == Boolean.TRUE) {
            final DynamicObject rubyClass = Layouts.BASIC_OBJECT.getLogicalClass(rubyException);
            final Object message = context.send(rubyException, "to_s");
            showExceptionIfDebug(rubyClass, message, backtrace);
        }
    }

    @TruffleBoundary
    public void showExceptionIfDebug(DynamicObject rubyClass, Object message, Backtrace backtrace) {
        if (context.getCoreLibrary().getDebug() == Boolean.TRUE) {
            final String exceptionClass = Layouts.MODULE.getFields(rubyClass).getName();
            String from = "";
            if (backtrace != null && backtrace.getActivations().length > 0) {
                from = " at " + debugBacktraceFormatter.formatLine(backtrace.getActivations(), 0, null);
            }
            Object stderr = context.getCoreLibrary().getStderr();
            String output = "Exception `" + exceptionClass + "'" + from + " - " + message + "\n";
            DynamicObject outputString = StringOperations.createString(context, StringOperations.encodeRope(output, UTF8Encoding.INSTANCE));
            context.send(stderr, "write", outputString);
        }
    }

    // ArgumentError

    public DynamicObject argumentErrorOneHashRequired(Node currentNode) {
        return argumentError(coreStrings().ONE_HASH_REQUIRED.getRope(), currentNode, null);
    }

    public DynamicObject argumentError(Rope message, Node currentNode) {
        return argumentError(message, currentNode, null);
    }

    public DynamicObject argumentError(String message, Node currentNode) {
        return argumentError(message, currentNode, null);
    }

    public DynamicObject argumentErrorProcWithoutBlock(Node currentNode) {
        return argumentError(coreStrings().PROC_WITHOUT_BLOCK.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorTooFewArguments(Node currentNode) {
        return argumentError(coreStrings().TOO_FEW_ARGUMENTS.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorTimeIntervalPositive(Node currentNode) {
        return argumentError(coreStrings().TIME_INTERVAL_MUST_BE_POS.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorXOutsideOfString(Node currentNode) {
        return argumentError(coreStrings().X_OUTSIDE_OF_STRING.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorCantCompressNegativeNumbers(Node currentNode) {
        return argumentError(coreStrings().CANT_COMPRESS_NEGATIVE.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorOutOfRange(Node currentNode) {
        return argumentError(coreStrings().OUT_OF_RANGE.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorNegativeArraySize(Node currentNode) {
        return argumentError(coreStrings().NEGATIVE_ARRAY_SIZE.getRope(), currentNode, null);
    }

    public DynamicObject argumentErrorCharacterRequired(Node currentNode) {
        return argumentError("%c requires a character", currentNode);
    }

    public DynamicObject argumentErrorCantOmitPrecision(Node currentNode) {
        return argumentError("can't omit precision for a Float.", currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorUnknownKeyword(Object name, Node currentNode) {
        return argumentError("unknown keyword: " + name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorInvalidRadix(int radix, Node currentNode) {
        return argumentError(StringUtils.format("invalid radix %d", radix), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorMissingKeyword(String name, Node currentNode) {
        return argumentError(StringUtils.format("missing keyword: %s", name), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentError(int passed, int required, Node currentNode) {
        return argumentError(StringUtils.format("wrong number of arguments (given %d, expected %d)", passed, required), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorPlus(int passed, int required, Node currentNode) {
        return argumentError(StringUtils.format("wrong number of arguments (given %d, expected %d+)", passed, required), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentError(int passed, int required, int optional, Node currentNode) {
        return argumentError(StringUtils.format("wrong number of arguments (given %d, expected %d..%d)", passed, required, required + optional), currentNode);
    }

    public DynamicObject argumentErrorEmptyVarargs(Node currentNode) {
        return argumentError(coreStrings().WRONG_ARGS_ZERO_PLUS_ONE.getRope(), currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorWrongArgumentType(Object object, String expectedType, Node currentNode) {
        String badClassName = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(object)).getName();
        return argumentError(StringUtils.format("wrong argument type %s (expected %s)", badClassName, expectedType), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorInvalidStringToInteger(Object object, Node currentNode) {
        assert RubyGuards.isRubyString(object);

        // TODO (nirvdrum 19-Apr-18): Guard against String#inspect being redefined to return something other than a String.
        final String formattedObject = StringOperations.getString((DynamicObject) context.send(object, "inspect"));
        return argumentError(StringUtils.format("invalid value for Integer(): %s", formattedObject), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorNoReceiver(Node currentNode) {
        return argumentError("no receiver is available", currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentErrorEncodingAlreadyRegistered(String nameString, Node currentNode) {
        return argumentError(StringUtils.format("encoding %s is already registered", nameString), currentNode);
    }

    @TruffleBoundary
    public DynamicObject argumentError(String message, Node currentNode, Throwable javaThrowable) {
        return argumentError(StringOperations.encodeRope(message, UTF8Encoding.INSTANCE), currentNode, javaThrowable);
    }

    public DynamicObject argumentError(Rope message, Node currentNode, Throwable javaThrowable) {
        DynamicObject exceptionClass = context.getCoreLibrary().getArgumentErrorClass();
        return ExceptionOperations.createRubyException(context, exceptionClass, StringOperations.createString(context, message), currentNode, javaThrowable);
    }

    // RuntimeError

    @TruffleBoundary
    public DynamicObject frozenError(Object object, Node currentNode) {
        String className = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(object)).getName();
        return runtimeError(StringUtils.format("can't modify frozen %s", className), currentNode);
    }

    public DynamicObject runtimeErrorNotConstant(Node currentNode) {
        return runtimeError("Truffle::Graal.assert_constant can only be called lexically", currentNode);
    }

    public DynamicObject runtimeErrorCompiled(Node currentNode) {
        return runtimeError("Truffle::Graal.assert_not_compiled can only be called lexically", currentNode);
    }

    public DynamicObject runtimeErrorCoverageNotEnabled(Node currentNode) {
        return runtimeError("coverage measurement is not enabled", currentNode);
    }

    @TruffleBoundary
    public DynamicObject runtimeError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getRuntimeErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject runtimeError(String fullMessage, Node currentNode, Throwable javaThrowable) {
        DynamicObject exceptionClass = context.getCoreLibrary().getRuntimeErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(fullMessage, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, javaThrowable);
    }

    // SystemStackError

    @TruffleBoundary
    public DynamicObject systemStackErrorStackLevelTooDeep(Node currentNode, StackOverflowError javaThrowable) {
        DynamicObject exceptionClass = context.getCoreLibrary().getSystemStackErrorClass();
        return ExceptionOperations.createRubyException(context, exceptionClass, coreStrings().STACK_LEVEL_TOO_DEEP.createInstance(), currentNode, javaThrowable);
    }

    // NoMemoryError

    @TruffleBoundary
    public DynamicObject noMemoryError(Node currentNode, OutOfMemoryError javaThrowable) {
        DynamicObject exceptionClass = context.getCoreLibrary().getNoMemoryErrorClass();
        return ExceptionOperations.createRubyException(context, exceptionClass, coreStrings().FAILED_TO_ALLOCATE_MEMORY.createInstance(), currentNode, javaThrowable);
    }

    // Errno

    @TruffleBoundary
    public DynamicObject mathDomainErrorAcos(Node currentNode) {
        return mathDomainError("acos", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorAcosh(Node currentNode) {
        return mathDomainError("acosh", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorAsin(Node currentNode) {
        return mathDomainError("asin", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorAtanh(Node currentNode) {
        return mathDomainError("atanh", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorGamma(Node currentNode) {
        return mathDomainError("gamma", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorLog2(Node currentNode) {
        return mathDomainError("log2", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorLog10(Node currentNode) {
        return mathDomainError("log10", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainErrorLog(Node currentNode) {
        return mathDomainError("log", currentNode);
    }

    @TruffleBoundary
    public DynamicObject mathDomainError(String method, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getErrnoClass("EDOM");
        Rope rope = StringOperations.encodeRope(StringUtils.format("Numerical argument is out of domain - \"%s\"", method), UTF8Encoding.INSTANCE);
        DynamicObject errorMessage = StringOperations.createString(context, rope);
        return ExceptionOperations.createSystemCallError(context, exceptionClass, errorMessage, currentNode, context.getCoreLibrary().getErrnoValue("EDOM"));
    }

    @TruffleBoundary
    public DynamicObject errnoError(int errno, String extraMessage, Node currentNode) {
        final String errnoName = context.getCoreLibrary().getErrnoName(errno);
        if (errnoName == null) {
            return systemCallError(StringUtils.format("Unknown Error (%s)%s", errno, extraMessage), errno, currentNode);
        }

        final DynamicObject errnoClass = context.getCoreLibrary().getErrnoClass(errnoName);
        final String fullMessage = ErrnoDescriptions.getDescription(errnoName) + extraMessage;
        final DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(fullMessage, UTF8Encoding.INSTANCE));

        return ExceptionOperations.createSystemCallError(context, errnoClass, errorMessage, currentNode, errno);
    }

    // IndexError

    @TruffleBoundary
    public DynamicObject indexErrorOutOfString(int index, Node currentNode) {
        return indexError(StringUtils.format("index %d out of string", index), currentNode);
    }

    @TruffleBoundary
    public DynamicObject indexError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getIndexErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject indexTooSmallError(String type, int index, int length, Node currentNode) {
        return indexError(StringUtils.format("index %d too small for %s; minimum: -%d", index, type, length), currentNode);
    }

    @TruffleBoundary
    public DynamicObject negativeLengthError(int length, Node currentNode) {
        return indexError(StringUtils.format("negative length (%d)", length), currentNode);
    }

    @TruffleBoundary
    public DynamicObject indexErrorInvalidIndex(Node currentNode) {
        return indexError("invalid index", currentNode);
    }

    // LocalJumpError

    @TruffleBoundary
    public DynamicObject localJumpError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getLocalJumpErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    public DynamicObject noBlockGiven(Node currentNode) {
        return localJumpError("no block given", currentNode);
    }

    public DynamicObject breakFromProcClosure(Node currentNode) {
        return localJumpError("break from proc-closure", currentNode);
    }

    public DynamicObject unexpectedReturn(Node currentNode) {
        return localJumpError("unexpected return", currentNode);
    }

    public DynamicObject noBlockToYieldTo(Node currentNode) {
        return localJumpError("no block given (yield)", currentNode);
    }

    // TypeError

    public DynamicObject typeErrorCantCreateInstanceOfSingletonClass(Node currentNode) {
        return typeError("can't create instance of singleton class", currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject superclassMismatch(String name, Node currentNode) {
        return typeError("superclass mismatch for class " + name, currentNode);
    }

    public DynamicObject typeError(String message, Node currentNode) {
        return typeError(message, currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject typeErrorAllocatorUndefinedFor(DynamicObject rubyClass, Node currentNode) {
        String className = Layouts.MODULE.getFields(rubyClass).getName();
        return typeError(StringUtils.format("allocator undefined for %s", className), currentNode);
    }

    public DynamicObject typeErrorCantDefineSingleton(Node currentNode) {
        return typeError("can't define singleton", currentNode);
    }

    public DynamicObject typeErrorCantBeCastedToBigDecimal(Node currentNode) {
        return typeError("could not be casted to BigDecimal", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorCantConvertTo(Object from, String toClass, String methodUsed, Object result, Node currentNode) {
        String fromClass = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(from)).getName();
        return typeError(StringUtils.format("can't convert %s to %s (%s#%s gives %s)",
                fromClass, toClass, fromClass, methodUsed, context.getCoreLibrary().getLogicalClass(result).toString()), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorCantConvertInto(Object from, String toClass, Node currentNode) {
        return typeError(StringUtils.format("can't convert %s into %s", Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(from)).getName(), toClass), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorIsNotA(Object value, String expectedType, Node currentNode) {
        return typeErrorIsNotA(value.toString(), expectedType, currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorIsNotA(String value, String expectedType, Node currentNode) {
        return typeError(StringUtils.format("%s is not a %s", value, expectedType), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorIsNotAClassModule(Object value, Node currentNode) {
        return typeError(StringUtils.format("%s is not a class/module", value), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorNoImplicitConversion(Object from, String to, Node currentNode) {
        return typeError(StringUtils.format("no implicit conversion of %s into %s", Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(from)).getName(), to), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorBadCoercion(Object from, String to, String coercionMethod, Object coercedTo, Node currentNode) {
        String badClassName = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(from)).getName();
        return typeError(StringUtils.format("can't convert %s to %s (%s#%s gives %s)",
                badClassName,
                to,
                badClassName,
                coercionMethod,
                Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(coercedTo)).getName()), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorCantDump(Object object, Node currentNode) {
        String logicalClass = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(object)).getName();
        return typeError(StringUtils.format("can't dump %s", logicalClass), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorWrongArgumentType(Object object, String expectedType, Node currentNode) {
        String badClassName = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(object)).getName();
        return typeError(StringUtils.format("wrong argument type %s (expected %s)", badClassName, expectedType), currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorAlreadyInitializedClass(Node currentNode) {
        return typeError("already initialized class", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorSubclassSingletonClass(Node currentNode) {
        return typeError("can't make subclass of singleton class", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorSubclassClass(Node currentNode) {
        return typeError("can't make subclass of Class", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorSuperclassMustBeClass(Node currentNode) {
        return typeError("superclass must be a Class", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorInheritUninitializedClass(Node currentNode) {
        return typeError("can't inherit uninitialized class", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeErrorRescueInvalidClause(Node currentNode) {
        return typeError("class or module required for rescue clause", currentNode);
    }

    @TruffleBoundary
    public DynamicObject typeError(String message, Node currentNode, Throwable javaThrowable) {
        DynamicObject exceptionClass = context.getCoreLibrary().getTypeErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, javaThrowable);
    }

    // NameError

    @TruffleBoundary
    public DynamicObject nameErrorWrongConstantName(String name, Node currentNode) {
        return nameError(StringUtils.format("wrong constant name %s", name), null, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorConstantNotDefined(DynamicObject module, String name, Node currentNode) {
        return nameError(StringUtils.format("constant %s not defined", ModuleOperations.constantName(context, module, name)), null, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorUninitializedConstant(DynamicObject module, String name, Node currentNode) {
        assert RubyGuards.isRubyModule(module);
        final String message = StringUtils.format("uninitialized constant %s", ModuleOperations.constantNameNoLeadingColon(context, module, name));
        return nameError(message, module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorPrivateConstant(DynamicObject module, String name, Node currentNode) {
        return nameError(StringUtils.format("private constant %s referenced", ModuleOperations.constantName(context, module, name)), module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorUninitializedClassVariable(DynamicObject module, String name, Node currentNode) {
        assert RubyGuards.isRubyModule(module);
        return nameError(StringUtils.format("uninitialized class variable %s in %s", name, Layouts.MODULE.getFields(module).getName()), module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorInstanceNameNotAllowable(String name, Object receiver, Node currentNode) {
        return nameError(StringUtils.format("`%s' is not allowable as an instance variable name", name), receiver, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorInstanceVariableNotDefined(String name, Object receiver, Node currentNode) {
        return nameError(StringUtils.format("instance variable %s not defined", name), receiver, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorUndefinedMethod(String name, DynamicObject module, Node currentNode) {
        assert RubyGuards.isRubyModule(module);
        return nameError(StringUtils.format("undefined method `%s' for %s", name, Layouts.MODULE.getFields(module).getName()), module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorUndefinedSingletonMethod(String name, Object receiver, Node currentNode) {
        String className = Layouts.MODULE.getFields(context.getCoreLibrary().getLogicalClass(receiver)).getName();
        return nameError(StringUtils.format("undefined singleton method `%s' for %s", name, className), receiver, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorMethodNotDefinedIn(DynamicObject module, String name, Node currentNode) {
        return nameError(StringUtils.format("method `%s' not defined in %s", name, Layouts.MODULE.getFields(module).getName()), module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorPrivateMethod(String name, DynamicObject module, Node currentNode) {
        return nameError(StringUtils.format("method `%s' for %s is private", name, Layouts.MODULE.getFields(module).getName()), module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorLocalVariableNotDefined(String name, DynamicObject binding, Node currentNode) {
        assert RubyGuards.isRubyBinding(binding);
        return nameError(StringUtils.format("local variable `%s' not defined for %s", name, binding.toString()), binding, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorClassVariableNotDefined(String name, DynamicObject module, Node currentNode) {
        assert RubyGuards.isRubyModule(module);
        return nameError(StringUtils.format("class variable `%s' not defined for %s", name, Layouts.MODULE.getFields(module).getName()), module, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorImportNotFound(String name, Node currentNode) {
        return nameError(StringUtils.format("import '%s' not found", name), null, name, currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameErrorUnknownIdentifier(TruffleObject receiver, Object name, UnknownIdentifierException exception, Node currentNode) {
        return nameError(exception.getMessage(), receiver, name.toString(), currentNode);
    }

    @TruffleBoundary
    public DynamicObject nameError(String message, Object receiver, String name, Node currentNode) {
        final DynamicObject messageString = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        final DynamicObject exceptionClass = context.getCoreLibrary().getNameErrorClass();
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode);
        final DynamicObject cause = ThreadGetExceptionNode.getLastException(context);
        showExceptionIfDebug(exceptionClass, messageString, backtrace);
        return context.getCoreLibrary().getNameErrorFactory().newInstance(Layouts.NAME_ERROR.build(
                messageString,
                null,
                backtrace,
                cause,
                receiver,
                context.getSymbolTable().getSymbol(name)));
    }

    public DynamicObject nameErrorFromMethodMissing(DynamicObject formatter, Object receiver, String name, Node currentNode) {
        // omit = 1 to skip over the call to `method_missing'. MRI does not show this is the backtrace.
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode, 1);
        final DynamicObject cause = ThreadGetExceptionNode.getLastException(context);
        final DynamicObject exception = context.getCoreLibrary().getNameErrorFactory().newInstance(Layouts.NAME_ERROR.build(
                null,
                formatter,
                backtrace,
                cause,
                receiver,
                context.getSymbolTable().getSymbol(name)));
        showExceptionIfDebug(exception, backtrace);
        return exception;
    }

    // NoMethodError

    public DynamicObject noMethodError(String message, Object receiver, String name, Object[] args, Node currentNode) {
        final DynamicObject messageString = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        final DynamicObject argsArray =  createArray(context, args);
        final DynamicObject exceptionClass = context.getCoreLibrary().getNoMethodErrorClass();
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode);
        final DynamicObject cause = ThreadGetExceptionNode.getLastException(context);
        showExceptionIfDebug(exceptionClass, messageString, backtrace);
        return context.getCoreLibrary().getNoMethodErrorFactory().newInstance(Layouts.NO_METHOD_ERROR.build(
                messageString,
                null,
                backtrace,
                cause,
                receiver,
                context.getSymbolTable().getSymbol(name),
                argsArray));
    }

    public DynamicObject noMethodErrorFromMethodMissing(DynamicObject formatter, Object receiver, String name, Object[] args, Node currentNode) {
        final DynamicObject argsArray = createArray(context, args);

        // omit = 1 to skip over the call to `method_missing'. MRI does not show this is the backtrace.
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode, 1);
        final DynamicObject cause = ThreadGetExceptionNode.getLastException(context);
        final DynamicObject exception = context.getCoreLibrary().getNoMethodErrorFactory().newInstance(Layouts.NO_METHOD_ERROR.build(
                null,
                formatter,
                backtrace,
                cause,
                receiver,
                context.getSymbolTable().getSymbol(name),
                argsArray));
        showExceptionIfDebug(exception, backtrace);
        return exception;
    }

    @TruffleBoundary
    public DynamicObject noSuperMethodOutsideMethodError(Node currentNode) {
        final DynamicObject messageString = StringOperations.createString(context, StringOperations.encodeRope("super called outside of method", UTF8Encoding.INSTANCE));
        final DynamicObject exceptionClass = context.getCoreLibrary().getNameErrorClass();
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode);
        final DynamicObject cause = ThreadGetExceptionNode.getLastException(context);
        showExceptionIfDebug(exceptionClass, messageString, backtrace);
        // TODO BJF Jul 21, 2016 Review to add receiver
        return context.getCoreLibrary().getNoMethodErrorFactory().newInstance(Layouts.NAME_ERROR.build(
                messageString,
                null,
                backtrace,
                cause,
                null,
                // FIXME: the name of the method is not known in this case currently
                context.getSymbolTable().getSymbol("<unknown>")));
    }

    public DynamicObject noMethodErrorUnknownIdentifier(TruffleObject receiver, Object name, Object[] args, UnknownIdentifierException exception, Node currentNode) {
        return noMethodError(exception.getMessage(), receiver, name.toString(), args, currentNode);
    }

    // LoadError

    @TruffleBoundary
    public DynamicObject loadError(String message, String path, Node currentNode) {
        DynamicObject messageString = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        DynamicObject exceptionClass = context.getCoreLibrary().getLoadErrorClass();
        DynamicObject loadError = ExceptionOperations.createRubyException(context, exceptionClass, messageString, currentNode, null);
        if ("openssl.so".equals(path)) {
            // This is a workaround for the rubygems/security.rb file expecting the error path to be openssl
            path = "openssl";
        }
        loadError.define("@path", StringOperations.createString(context, StringOperations.encodeRope(path, UTF8Encoding.INSTANCE)));
        return loadError;
    }

    @TruffleBoundary
    public DynamicObject loadErrorCannotLoad(String name, Node currentNode) {
        return loadError(StringUtils.format("cannot load such file -- %s", name), name, currentNode);
    }

    // ZeroDivisionError

    public DynamicObject zeroDivisionError(Node currentNode) {
        return zeroDivisionError(currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject zeroDivisionError(Node currentNode, ArithmeticException exception) {
        DynamicObject exceptionClass = context.getCoreLibrary().getZeroDivisionErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope("divided by 0", UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, exception);
    }

    // NotImplementedError

    @TruffleBoundary
    public DynamicObject notImplementedError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getNotImplementedErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(StringUtils.format("Method %s not implemented", message),
                UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    // SyntaxError

    @TruffleBoundary
    public DynamicObject syntaxErrorInvalidRetry(Node currentNode) {
        return syntaxError("Invalid retry", currentNode, currentNode.getEncapsulatingSourceSection());
    }

    @TruffleBoundary
    public DynamicObject syntaxError(String message, Node currentNode, SourceSection sourceLocation) {
        DynamicObject exceptionClass = context.getCoreLibrary().getSyntaxErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, sourceLocation, null);
    }

    // FloatDomainError

    @TruffleBoundary
    public DynamicObject floatDomainError(String value, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getFloatDomainErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(value, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    public DynamicObject floatDomainErrorResultsToNaN(Node currentNode) {
        return floatDomainError("Computation results to 'NaN'(Not a Number)", currentNode);
    }

    public DynamicObject floatDomainErrorResultsToInfinity(Node currentNode) {
        return floatDomainError("Computation results to 'Infinity'", currentNode);
    }

    public DynamicObject floatDomainErrorResultsToNegInfinity(Node currentNode) {
        return floatDomainError("Computation results to '-Infinity'", currentNode);
    }

    public DynamicObject floatDomainErrorSqrtNegative(Node currentNode) {
        return floatDomainError("(VpSqrt) SQRT(negative value)", currentNode);
    }

    // IOError

    @TruffleBoundary
    public DynamicObject ioError(String error, String fileName, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getIOErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(StringUtils.format("%s -- %s", error, fileName), UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject ioError(String fileName, Node currentNode) {
        return ioError("Error reading file", fileName, currentNode);
    }

    // RangeError

    @TruffleBoundary
    public DynamicObject rangeError(long code, DynamicObject encoding, Node currentNode) {
        assert RubyGuards.isRubyEncoding(encoding);
        return rangeError(StringUtils.format("invalid codepoint %x in %s", code, EncodingOperations.getEncoding(encoding)), currentNode);
    }

    @TruffleBoundary
    public DynamicObject rangeError(DynamicObject range, Node currentNode) {
        assert RubyGuards.isIntRange(range);
        return rangeError(StringUtils.format("%d..%s%d out of range",
                Layouts.INT_RANGE.getBegin(range),
                Layouts.INT_RANGE.getExcludedEnd(range) ? "." : "",
                Layouts.INT_RANGE.getEnd(range)), currentNode);
    }

    @TruffleBoundary
    public DynamicObject rangeErrorConvertToInt(long value, Node currentNode) {
        final String direction;

        if (value < Integer.MIN_VALUE) {
            direction = "small";
        } else if (value > Integer.MAX_VALUE) {
            direction = "big";
        } else {
            throw new IllegalArgumentException();
        }

        return rangeError(StringUtils.format("integer %d too %s to convert to `int'", value, direction), currentNode);
    }

    @TruffleBoundary
    public DynamicObject rangeError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getRangeErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    // Truffle::GraalError

    public DynamicObject graalErrorAssertConstantNotConstant(Node currentNode) {
        return graalError("value in Truffle::Graal.assert_constant was not constant", currentNode);
    }

    public DynamicObject graalErrorAssertNotCompiledCompiled(Node currentNode) {
        return graalError("call to Truffle::Graal.assert_not_compiled was compiled", currentNode);
    }

    @TruffleBoundary
    private DynamicObject graalError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getGraalErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    // RegexpError

    @TruffleBoundary
    public DynamicObject regexpError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getRegexpErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    // Encoding conversion errors.

    @TruffleBoundary
    public DynamicObject encodingError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getEncodingErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject encodingCompatibilityErrorIncompatible(Encoding a, Encoding b, Node currentNode) {
        return encodingCompatibilityError(StringUtils.format("incompatible character encodings: %s and %s", a, b), currentNode);
    }

    @TruffleBoundary
    public DynamicObject encodingCompatibilityErrorIncompatibleWithOperation(Encoding encoding, Node currentNode) {
        return encodingCompatibilityError(StringUtils.format("incompatible encoding with this operation: %s", encoding), currentNode);
    }

    @TruffleBoundary
    public DynamicObject encodingCompatibilityError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getEncodingCompatibilityErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    @TruffleBoundary
    public DynamicObject encodingUndefinedConversionError(Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getEncodingUndefinedConversionErrorClass();
        return ExceptionOperations.createRubyException(context, exceptionClass, coreStrings().REPLACEMENT_CHARACTER_SETUP_FAILED.createInstance(), currentNode, null);
    }

    // FiberError

    @TruffleBoundary
    public DynamicObject fiberError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getFiberErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    public DynamicObject deadFiberCalledError(Node currentNode) {
        return fiberError("dead fiber called", currentNode);
    }

    public DynamicObject yieldFromRootFiberError(Node currentNode) {
        return fiberError("can't yield from root fiber", currentNode);
    }

    // ThreadError

    @TruffleBoundary
    public DynamicObject threadError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getThreadErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    public DynamicObject threadErrorKilledThread(Node currentNode) {
        return threadError("killed thread", currentNode);
    }

    public DynamicObject threadErrorRecursiveLocking(Node currentNode) {
        return threadError("deadlock; recursive locking", currentNode);
    }

    public DynamicObject threadErrorUnlockNotLocked(Node currentNode) {
        return threadError("Attempt to unlock a mutex which is not locked", currentNode);
    }

    public DynamicObject threadErrorAlreadyLocked(Node currentNode) {
        return threadError("Attempt to unlock a mutex which is locked by another thread", currentNode);
    }

    public DynamicObject threadErrorQueueFull(Node currentNode) {
        return threadError("queue full", currentNode);
    }

    // SecurityError

    @TruffleBoundary
    public DynamicObject securityError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getSecurityErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    // SystemCallError

    @TruffleBoundary
    public DynamicObject systemCallError(String message, int errno, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getSystemCallErrorClass();
        DynamicObject errorMessage;
        if (message == null) {
            errorMessage = context.getCoreLibrary().getNil();
        } else {
            errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        }
        return ExceptionOperations.createSystemCallError(context, exceptionClass, errorMessage, currentNode, errno);
    }

    // FFI::NullPointerError

    @TruffleBoundary
    public DynamicObject ffiNullPointerError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getTruffleFFINullPointerErrorClass();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    // SystemExit

    @TruffleBoundary
    public DynamicObject systemExit(int exitStatus, Node currentNode) {
        final DynamicObject message = StringOperations.createString(context, StringOperations.encodeRope("exit", UTF8Encoding.INSTANCE));
        DynamicObject exceptionClass = context.getCoreLibrary().getSystemExitClass();
        final DynamicObject systemExit = ExceptionOperations.createRubyException(context, exceptionClass, message, currentNode, null);
        systemExit.define("@status", exitStatus);
        return systemExit;
    }

    // ClosedQueueError

    @TruffleBoundary
    public DynamicObject closedQueueError(String message, Node currentNode) {
        DynamicObject exceptionClass = context.getCoreLibrary().getClosedQueueError();
        DynamicObject errorMessage = StringOperations.createString(context, StringOperations.encodeRope(message, UTF8Encoding.INSTANCE));
        return ExceptionOperations.createRubyException(context, exceptionClass, errorMessage, currentNode, null);
    }

    public DynamicObject closedQueueError(Node currentNode) {
        return closedQueueError("queue closed", currentNode);
    }

    // Helpers

    private CoreStrings coreStrings() {
        return context.getCoreStrings();
    }

}
