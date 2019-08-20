/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.transcode.EConvFlags;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.aot.ParserCache;
import org.truffleruby.builtins.CoreMethodNodeManager;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.core.klass.ClassNodes;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.thread.ThreadBacktraceLocationLayoutImpl;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TruffleFatalException;
import org.truffleruby.language.globals.GlobalVariableReader;
import org.truffleruby.language.globals.GlobalVariables;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FileLoader;
import org.truffleruby.language.loader.ResourceLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorDriver;
import org.truffleruby.parser.ast.RootParseNode;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.NativeTypes;
import org.truffleruby.shared.BuildInformationImpl;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.Layout;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class CoreLibrary {

    private static final String ERRNO_CONFIG_PREFIX = NativeConfiguration.PREFIX + "errno.";

    private static final Property ALWAYS_FROZEN_PROPERTY = Property.create(Layouts.FROZEN_IDENTIFIER, Layout.createLayout().createAllocator().constantLocation(true), 0);

    private final RubyContext context;

    private final SourceSection sourceSection;

    private final DynamicObject argumentErrorClass;
    private final DynamicObject arrayClass;
    private final DynamicObjectFactory arrayFactory;
    private final DynamicObject basicObjectClass;
    private final DynamicObjectFactory bignumFactory;
    private final DynamicObject bindingClass;
    private final DynamicObjectFactory bindingFactory;
    private final DynamicObject classClass;
    private final DynamicObject complexClass;
    private final DynamicObject dirClass;
    private final DynamicObject encodingClass;
    private final DynamicObjectFactory encodingFactory;
    private final DynamicObject encodingConverterClass;
    private final DynamicObject encodingErrorClass;
    private final DynamicObject exceptionClass;
    private final DynamicObject falseClass;
    private final DynamicObject fiberClass;
    private final DynamicObjectFactory fiberFactory;
    private final DynamicObject floatClass;
    private final DynamicObject floatDomainErrorClass;
    private final DynamicObject frozenErrorClass;
    private final DynamicObject hashClass;
    private final DynamicObjectFactory hashFactory;
    private final DynamicObject integerClass;
    private final DynamicObject indexErrorClass;
    private final DynamicObject keyErrorClass;
    private final DynamicObject ioErrorClass;
    private final DynamicObject loadErrorClass;
    private final DynamicObject localJumpErrorClass;
    private final DynamicObject matchDataClass;
    private final DynamicObjectFactory matchDataFactory;
    private final DynamicObject moduleClass;
    private final DynamicObject nameErrorClass;
    private final DynamicObjectFactory nameErrorFactory;
    private final DynamicObject nilClass;
    private final DynamicObject noMemoryErrorClass;
    private final DynamicObject noMethodErrorClass;
    private final DynamicObjectFactory noMethodErrorFactory;
    private final DynamicObject notImplementedErrorClass;
    private final DynamicObject numericClass;
    private final DynamicObject objectClass;
    private final DynamicObjectFactory objectFactory;
    private final DynamicObject procClass;
    private final DynamicObjectFactory procFactory;
    private final DynamicObject processModule;
    private final DynamicObject rangeClass;
    private final DynamicObjectFactory intRangeFactory;
    private final DynamicObjectFactory longRangeFactory;
    private final DynamicObject rangeErrorClass;
    private final DynamicObject rationalClass;
    private final DynamicObject regexpClass;
    private final DynamicObjectFactory regexpFactory;
    private final DynamicObject regexpErrorClass;
    private final DynamicObject graalErrorClass;
    private final DynamicObject runtimeErrorClass;
    private final DynamicObject systemStackErrorClass;
    private final DynamicObject securityErrorClass;
    private final DynamicObject standardErrorClass;
    private final DynamicObject stringClass;
    private final DynamicObjectFactory stringFactory;
    private final DynamicObject symbolClass;
    private final DynamicObjectFactory symbolFactory;
    private final DynamicObject syntaxErrorClass;
    private final DynamicObject systemCallErrorClass;
    private final DynamicObject systemExitClass;
    private final DynamicObject threadClass;
    private final DynamicObjectFactory threadFactory;
    private final DynamicObject threadBacktraceClass;
    private final DynamicObject threadBacktraceLocationClass;
    private final DynamicObjectFactory threadBacktraceLocationFactory;
    private final DynamicObject timeClass;
    private final DynamicObjectFactory timeFactory;
    private final DynamicObject trueClass;
    private final DynamicObject typeErrorClass;
    private final DynamicObject zeroDivisionErrorClass;
    private final DynamicObject enumerableModule;
    private final DynamicObject errnoModule;
    private final DynamicObject kernelModule;
    private final DynamicObject truffleFFIModule;
    private final DynamicObject truffleFFIPointerClass;
    private final DynamicObject truffleFFINullPointerErrorClass;
    private final DynamicObject truffleTypeModule;
    private final DynamicObject truffleModule;
    private final DynamicObject truffleInternalModule;
    private final DynamicObject truffleBootModule;
    private final DynamicObject truffleExceptionOperationsModule;
    private final DynamicObject truffleInteropModule;
    private final DynamicObject truffleInteropForeignClass;
    private final DynamicObject truffleKernelOperationsModule;
    private final DynamicObject truffleRegexpOperationsModule;
    private final DynamicObject truffleThreadOperationsModule;
    private final DynamicObject bigDecimalClass;
    private final DynamicObject bigDecimalOperationsModule;
    private final DynamicObject encodingCompatibilityErrorClass;
    private final DynamicObject encodingUndefinedConversionErrorClass;
    private final DynamicObject methodClass;
    private final DynamicObjectFactory methodFactory;
    private final DynamicObject unboundMethodClass;
    private final DynamicObjectFactory unboundMethodFactory;
    private final DynamicObject byteArrayClass;
    private final DynamicObjectFactory byteArrayFactory;
    private final DynamicObject fiberErrorClass;
    private final DynamicObject threadErrorClass;
    private final DynamicObject objectSpaceModule;
    private final DynamicObject randomizerClass;
    private final DynamicObjectFactory randomizerFactory;
    private final DynamicObject atomicReferenceClass;
    private final DynamicObject handleClass;
    private final DynamicObjectFactory handleFactory;
    private final DynamicObject ioClass;
    private final DynamicObject stopIterationClass;
    private final DynamicObject closedQueueErrorClass;
    private final DynamicObject warningModule;

    private final DynamicObject argv;
    private final GlobalVariables globalVariables;
    private final DynamicObject mainObject;
    private final DynamicObject nil;
    private final Object supportUndefined;
    private final DynamicObject digestClass;
    private final DynamicObjectFactory digestFactory;
    private final DynamicObject structClass;

    private final FrameDescriptor emptyDescriptor;

    @CompilationFinal private DynamicObject eagainWaitReadable;
    @CompilationFinal private DynamicObject eagainWaitWritable;

    private final Map<String, DynamicObject> errnoClasses = new HashMap<>();
    private final Map<Integer, String> errnoValueToNames = new HashMap<>();

    @CompilationFinal private SharedMethodInfo basicObjectSendInfo;
    @CompilationFinal private SharedMethodInfo kernelPublicSendInfo;
    @CompilationFinal private SharedMethodInfo truffleBootMainInfo;

    @CompilationFinal private GlobalVariableReader loadPathReader;
    @CompilationFinal private GlobalVariableReader loadedFeaturesReader;
    @CompilationFinal private GlobalVariableReader debugReader;
    @CompilationFinal private GlobalVariableReader verboseReader;
    @CompilationFinal private GlobalVariableReader stdinReader;
    @CompilationFinal private GlobalVariableReader stderrReader;

    private final ConcurrentMap<String, Boolean> patchFiles;

    private final String coreLoadPath;

    @TruffleBoundary
    private SourceSection initCoreSourceSection(RubyContext context) {
        final Source.SourceBuilder builder = Source.newBuilder(TruffleRuby.LANGUAGE_ID, "", "(core)");
        if (context.getOptions().CORE_AS_INTERNAL) {
            builder.internal(true);
        }

        final Source source;

        try {
            source = builder.build();
        } catch (IOException e) {
            throw new JavaException(e);
        }

        return source.createUnavailableSection();
    }

    private String buildCoreLoadPath() {
        String path = context.getOptions().CORE_LOAD_PATH;

        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.startsWith(RubyLanguage.RESOURCE_SCHEME)) {
            return path;
        }

        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    private enum State {
        INITIALIZING,
        LOADING_RUBY_CORE,
        LOADED
    }

    private State state = State.INITIALIZING;

    private static class CoreLibraryNode extends RubyBaseNode {

        @Child SingletonClassNode singletonClassNode;

        public CoreLibraryNode() {
            this.singletonClassNode = SingletonClassNode.create();
            adoptChildren();
        }

        public SingletonClassNode getSingletonClassNode() {
            return singletonClassNode;
        }

        public DynamicObject getSingletonClass(Object object) {
            return singletonClassNode.executeSingletonClass(object);
        }

    }

    private final CoreLibraryNode node;

    public CoreLibrary(RubyContext context) {
        this.context = context;
        this.coreLoadPath = buildCoreLoadPath();
        this.sourceSection = initCoreSourceSection(context);
        this.node = new CoreLibraryNode();

        // Nothing in this constructor can use RubyContext.getCoreLibrary() as we are building it!
        // Therefore, only initialize the core classes and modules here.

        // Create the cyclic classes and modules

        classClass = ClassNodes.createClassClass(context, null);

        basicObjectClass = ClassNodes.createBootClass(context, null, classClass, null, "BasicObject");
        Layouts.CLASS.setInstanceFactoryUnsafe(basicObjectClass, Layouts.BASIC_OBJECT.createBasicObjectShape(basicObjectClass, basicObjectClass));

        objectClass = ClassNodes.createBootClass(context, null, classClass, basicObjectClass, "Object");
        objectFactory = Layouts.BASIC_OBJECT.createBasicObjectShape(objectClass, objectClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(objectClass, objectFactory);

        moduleClass = ClassNodes.createBootClass(context, null, classClass, objectClass, "Module");
        Layouts.CLASS.setInstanceFactoryUnsafe(moduleClass, Layouts.MODULE.createModuleShape(moduleClass, moduleClass));

        // Close the cycles
        // Set superclass of Class to Module
        Layouts.MODULE.getFields(classClass).setSuperClass(moduleClass, true);

        // Set constants in Object and lexical parents
        Layouts.MODULE.getFields(classClass).getAdoptedByLexicalParent(context, objectClass, "Class", node);
        Layouts.MODULE.getFields(basicObjectClass).getAdoptedByLexicalParent(context, objectClass, "BasicObject", node);
        Layouts.MODULE.getFields(objectClass).getAdoptedByLexicalParent(context, objectClass, "Object", node);
        Layouts.MODULE.getFields(moduleClass).getAdoptedByLexicalParent(context, objectClass, "Module", node);

        // Create Exception classes

        // Exception
        exceptionClass = defineClass("Exception");
        Layouts.CLASS.setInstanceFactoryUnsafe(exceptionClass, Layouts.EXCEPTION.createExceptionShape(exceptionClass, exceptionClass));

        // fatal
        defineClass(exceptionClass, "fatal");

        // NoMemoryError
        noMemoryErrorClass = defineClass(exceptionClass, "NoMemoryError");

        // StandardError
        standardErrorClass = defineClass(exceptionClass, "StandardError");
        argumentErrorClass = defineClass(standardErrorClass, "ArgumentError");
        encodingErrorClass = defineClass(standardErrorClass, "EncodingError");
        fiberErrorClass = defineClass(standardErrorClass, "FiberError");
        ioErrorClass = defineClass(standardErrorClass, "IOError");
        localJumpErrorClass = defineClass(standardErrorClass, "LocalJumpError");
        regexpErrorClass = defineClass(standardErrorClass, "RegexpError");
        threadErrorClass = defineClass(standardErrorClass, "ThreadError");
        typeErrorClass = defineClass(standardErrorClass, "TypeError");
        zeroDivisionErrorClass = defineClass(standardErrorClass, "ZeroDivisionError");

        // StandardError > RuntimeError
        runtimeErrorClass = defineClass(standardErrorClass, "RuntimeError");
        frozenErrorClass = defineClass(runtimeErrorClass, "FrozenError");

        // StandardError > RangeError
        rangeErrorClass = defineClass(standardErrorClass, "RangeError");
        floatDomainErrorClass = defineClass(rangeErrorClass, "FloatDomainError");

        // StandardError > IndexError
        indexErrorClass = defineClass(standardErrorClass, "IndexError");
        keyErrorClass = defineClass(indexErrorClass, "KeyError");
        stopIterationClass = defineClass(indexErrorClass, "StopIteration");
        closedQueueErrorClass = defineClass(stopIterationClass, "ClosedQueueError");

        // StandardError > IOError
        defineClass(ioErrorClass, "EOFError");

        // StandardError > NameError
        nameErrorClass = defineClass(standardErrorClass, "NameError");
        nameErrorFactory = Layouts.NAME_ERROR.createNameErrorShape(nameErrorClass, nameErrorClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(nameErrorClass, nameErrorFactory);
        noMethodErrorClass = defineClass(nameErrorClass, "NoMethodError");
        noMethodErrorFactory = Layouts.NO_METHOD_ERROR.createNoMethodErrorShape(noMethodErrorClass, noMethodErrorClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(noMethodErrorClass, noMethodErrorFactory);

        // StandardError > SystemCallError
        systemCallErrorClass = defineClass(standardErrorClass, "SystemCallError");
        Layouts.CLASS.setInstanceFactoryUnsafe(systemCallErrorClass, Layouts.SYSTEM_CALL_ERROR.createSystemCallErrorShape(systemCallErrorClass, systemCallErrorClass));

        errnoModule = defineModule("Errno");

        // ScriptError
        DynamicObject scriptErrorClass = defineClass(exceptionClass, "ScriptError");
        loadErrorClass = defineClass(scriptErrorClass, "LoadError");
        notImplementedErrorClass = defineClass(scriptErrorClass, "NotImplementedError");
        syntaxErrorClass = defineClass(scriptErrorClass, "SyntaxError");

        // SecurityError
        securityErrorClass = defineClass(exceptionClass, "SecurityError");

        // SignalException
        DynamicObject signalExceptionClass = defineClass(exceptionClass, "SignalException");
        defineClass(signalExceptionClass, "Interrupt");

        // SystemExit
        systemExitClass = defineClass(exceptionClass, "SystemExit");

        // SystemStackError
        systemStackErrorClass = defineClass(exceptionClass, "SystemStackError");

        // Create core classes and modules

        numericClass = defineClass("Numeric");
        complexClass = defineClass(numericClass, "Complex");
        floatClass = defineClass(numericClass, "Float");
        integerClass = defineClass(numericClass, "Integer");
        bignumFactory = alwaysFrozen(Layouts.BIGNUM.createBignumShape(integerClass, integerClass));
        rationalClass = defineClass(numericClass, "Rational");

        // Classes defined in Object

        arrayClass = defineClass("Array");
        arrayFactory = Layouts.ARRAY.createArrayShape(arrayClass, arrayClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(arrayClass, arrayFactory);
        bindingClass = defineClass("Binding");
        bindingFactory = Layouts.BINDING.createBindingShape(bindingClass, bindingClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(bindingClass, bindingFactory);
        defineClass("Data"); // Needed by Socket::Ifaddr and defined in core MRI
        dirClass = defineClass("Dir");
        encodingClass = defineClass("Encoding");
        encodingFactory = Layouts.ENCODING.createEncodingShape(encodingClass, encodingClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(encodingClass, encodingFactory);
        falseClass = defineClass("FalseClass");
        fiberClass = defineClass("Fiber");
        fiberFactory = Layouts.FIBER.createFiberShape(fiberClass, fiberClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(fiberClass, fiberFactory);
        defineModule("FileTest");
        hashClass = defineClass("Hash");
        final DynamicObjectFactory originalHashFactory = Layouts.HASH.createHashShape(hashClass, hashClass);
        if (context.isPreInitializing()) {
            hashFactory = context.getPreInitializationManager().hookIntoHashFactory(originalHashFactory);
        } else {
            hashFactory = originalHashFactory;
        }
        Layouts.CLASS.setInstanceFactoryUnsafe(hashClass, hashFactory);
        matchDataClass = defineClass("MatchData");
        matchDataFactory = Layouts.MATCH_DATA.createMatchDataShape(matchDataClass, matchDataClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(matchDataClass, matchDataFactory);
        methodClass = defineClass("Method");
        methodFactory = Layouts.METHOD.createMethodShape(methodClass, methodClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(methodClass, methodFactory);
        final DynamicObject mutexClass = defineClass("Mutex");
        Layouts.CLASS.setInstanceFactoryUnsafe(mutexClass, Layouts.MUTEX.createMutexShape(mutexClass, mutexClass));
        nilClass = defineClass("NilClass");
        final DynamicObjectFactory nilFactory = alwaysShared(alwaysFrozen(Layouts.CLASS.getInstanceFactory(nilClass)));
        Layouts.CLASS.setInstanceFactoryUnsafe(nilClass, nilFactory);
        procClass = defineClass("Proc");
        procFactory = Layouts.PROC.createProcShape(procClass, procClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(procClass, procFactory);
        processModule = defineModule("Process");
        DynamicObject queueClass = defineClass("Queue");
        Layouts.CLASS.setInstanceFactoryUnsafe(queueClass, Layouts.QUEUE.createQueueShape(queueClass, queueClass));
        DynamicObject sizedQueueClass = defineClass(queueClass, "SizedQueue");
        Layouts.CLASS.setInstanceFactoryUnsafe(sizedQueueClass, Layouts.SIZED_QUEUE.createSizedQueueShape(sizedQueueClass, sizedQueueClass));
        rangeClass = defineClass("Range");
        Layouts.CLASS.setInstanceFactoryUnsafe(rangeClass, Layouts.OBJECT_RANGE.createObjectRangeShape(rangeClass, rangeClass));
        intRangeFactory = Layouts.INT_RANGE.createIntRangeShape(rangeClass, rangeClass);
        longRangeFactory = Layouts.LONG_RANGE.createLongRangeShape(rangeClass, rangeClass);
        regexpClass = defineClass("Regexp");
        regexpFactory = Layouts.REGEXP.createRegexpShape(regexpClass, regexpClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(regexpClass, regexpFactory);
        stringClass = defineClass("String");
        stringFactory = Layouts.STRING.createStringShape(stringClass, stringClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(stringClass, stringFactory);
        symbolClass = defineClass("Symbol");
        symbolFactory = alwaysShared(alwaysFrozen(Layouts.SYMBOL.createSymbolShape(symbolClass, symbolClass)));
        Layouts.CLASS.setInstanceFactoryUnsafe(symbolClass, symbolFactory);

        threadClass = defineClass("Thread");
        threadClass.define("@abort_on_exception", false);
        threadFactory = Layouts.THREAD.createThreadShape(threadClass, threadClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(threadClass, threadFactory);

        threadBacktraceClass = defineClass(threadClass, objectClass, "Backtrace");
        threadBacktraceLocationClass = defineClass(threadBacktraceClass, objectClass, "Location");
        threadBacktraceLocationFactory = ThreadBacktraceLocationLayoutImpl.INSTANCE.createThreadBacktraceLocationShape(threadBacktraceLocationClass, threadBacktraceLocationClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(threadBacktraceLocationClass, threadBacktraceLocationFactory);
        timeClass = defineClass("Time");
        timeFactory = Layouts.TIME.createTimeShape(timeClass, timeClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(timeClass, timeFactory);
        trueClass = defineClass("TrueClass");
        unboundMethodClass = defineClass("UnboundMethod");
        unboundMethodFactory = Layouts.UNBOUND_METHOD.createUnboundMethodShape(unboundMethodClass, unboundMethodClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(unboundMethodClass, unboundMethodFactory);
        ioClass = defineClass("IO");
        Layouts.CLASS.setInstanceFactoryUnsafe(ioClass, Layouts.IO.createIOShape(ioClass, ioClass));
        defineClass(ioClass, "File");
        structClass = defineClass("Struct");

        final DynamicObject tracePointClass = defineClass("TracePoint");
        Layouts.CLASS.setInstanceFactoryUnsafe(tracePointClass, Layouts.TRACE_POINT.createTracePointShape(tracePointClass, tracePointClass));

        // Modules

        DynamicObject comparableModule = defineModule("Comparable");
        enumerableModule = defineModule("Enumerable");
        defineModule("GC");
        kernelModule = defineModule("Kernel");
        defineModule("Math");
        objectSpaceModule = defineModule("ObjectSpace");

        // The rest

        DynamicObject conditionVariableClass = defineClass("ConditionVariable");
        Layouts.CLASS.setInstanceFactoryUnsafe(conditionVariableClass, Layouts.CONDITION_VARIABLE.createConditionVariableShape(conditionVariableClass, conditionVariableClass));
        encodingCompatibilityErrorClass = defineClass(encodingClass, encodingErrorClass, "CompatibilityError");
        encodingUndefinedConversionErrorClass = defineClass(encodingClass, encodingErrorClass, "UndefinedConversionError");

        encodingConverterClass = defineClass(encodingClass, objectClass, "Converter");
        Layouts.CLASS.setInstanceFactoryUnsafe(encodingConverterClass, Layouts.ENCODING_CONVERTER.createEncodingConverterShape(encodingConverterClass, encodingConverterClass));

        final DynamicObject truffleRubyModule = defineModule("TruffleRuby");
        atomicReferenceClass = defineClass(truffleRubyModule, objectClass, "AtomicReference");
        Layouts.CLASS.setInstanceFactoryUnsafe(atomicReferenceClass,
                Layouts.ATOMIC_REFERENCE.createAtomicReferenceShape(atomicReferenceClass, atomicReferenceClass));
        truffleModule = defineModule("Truffle");
        truffleInternalModule = defineModule(truffleModule, "Internal");
        graalErrorClass = defineClass(truffleModule, exceptionClass, "GraalError");
        truffleExceptionOperationsModule = defineModule(truffleModule, "ExceptionOperations");
        truffleInteropModule = defineModule(truffleModule, "Interop");
        truffleInteropForeignClass = defineClass(truffleInteropModule, objectClass, "Foreign");
        defineModule(truffleModule, "CExt");
        defineModule(truffleModule, "Debug");
        defineModule(truffleModule, "Digest");
        defineModule(truffleModule, "ObjSpace");
        defineModule(truffleModule, "Coverage");
        defineModule(truffleModule, "Graal");
        defineModule(truffleModule, "Ropes");
        truffleRegexpOperationsModule = defineModule(truffleModule, "RegexpOperations");
        defineModule(truffleModule, "StringOperations");
        truffleBootModule = defineModule(truffleModule, "Boot");
        defineModule(truffleModule, "System");
        truffleKernelOperationsModule = defineModule(truffleModule, "KernelOperations");
        defineModule(truffleModule, "Binding");
        defineModule(truffleModule, "POSIX");
        defineModule(truffleModule, "Readline");
        defineModule(truffleModule, "ReadlineHistory");
        truffleThreadOperationsModule = defineModule(truffleModule, "ThreadOperations");
        defineModule(truffleModule, "WeakRefOperations");
        handleClass = defineClass(truffleModule, objectClass, "Handle");
        handleFactory = Layouts.HANDLE.createHandleShape(handleClass, handleClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(handleClass, handleFactory);
        defineModule("Polyglot");
        warningModule = defineModule("Warning");

        bigDecimalClass = defineClass(numericClass, "BigDecimal");
        Layouts.CLASS.setInstanceFactoryUnsafe(bigDecimalClass, Layouts.BIG_DECIMAL.createBigDecimalShape(bigDecimalClass, bigDecimalClass));
        bigDecimalOperationsModule = defineModule(truffleModule, "BigDecimalOperations");

        truffleFFIModule = defineModule(truffleModule, "FFI");
        DynamicObject truffleFFIAbstractMemoryClass = defineClass(truffleFFIModule, objectClass, "AbstractMemory");
        truffleFFIPointerClass = defineClass(truffleFFIModule, truffleFFIAbstractMemoryClass, "Pointer");
        Layouts.CLASS.setInstanceFactoryUnsafe(truffleFFIPointerClass, Layouts.POINTER.createPointerShape(truffleFFIPointerClass, truffleFFIPointerClass));
        truffleFFINullPointerErrorClass = defineClass(truffleFFIModule, runtimeErrorClass, "NullPointerError");

        truffleTypeModule = defineModule(truffleModule, "Type");

        byteArrayClass = defineClass(truffleModule, objectClass, "ByteArray");
        byteArrayFactory = Layouts.BYTE_ARRAY.createByteArrayShape(byteArrayClass, byteArrayClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(byteArrayClass, byteArrayFactory);
        defineClass(truffleModule, objectClass, "StringData");
        defineClass(encodingClass, objectClass, "Transcoding");
        randomizerClass = defineClass(truffleModule, objectClass, "Randomizer");
        randomizerFactory = Layouts.RANDOMIZER.createRandomizerShape(randomizerClass, randomizerClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(randomizerClass, randomizerFactory);

        // Standard library

        digestClass = defineClass(truffleModule, basicObjectClass, "Digest");
        digestFactory = Layouts.DIGEST.createDigestShape(digestClass, digestClass);
        Layouts.CLASS.setInstanceFactoryUnsafe(digestClass, digestFactory);

        // Include the core modules

        includeModules(comparableModule);

        // Create some key objects

        mainObject = objectFactory.newInstance();
        nil = nilFactory.newInstance();
        emptyDescriptor = new FrameDescriptor(nil);
        argv = Layouts.ARRAY.createArray(arrayFactory, null, 0);
        supportUndefined = NotProvided.INSTANCE;

        globalVariables = new GlobalVariables(nil);

        // No need for new version since it's null before which is not cached
        assert Layouts.CLASS.getSuperclass(basicObjectClass) == null;
        Layouts.CLASS.setSuperclass(basicObjectClass, nil);

        patchFiles = initializePatching(context);
    }

    private ConcurrentMap<String, Boolean> initializePatching(RubyContext context) {
        defineModule(truffleModule, "Patching");
        final ConcurrentMap<String, Boolean> patchFiles = new ConcurrentHashMap<>();

        if (context.getOptions().PATCHING) {
            try {
                final Path patchesDirectory = Paths.get(context.getRubyHome(), "lib", "patches");
                Files.walkFileTree(
                        patchesDirectory,
                        new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                                String relativePath = patchesDirectory.relativize(path).toString();
                                if (relativePath.endsWith(".rb")) {
                                    patchFiles.put(relativePath.substring(0, relativePath.length() - 3), false);
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
            } catch (IOException ignored) {
                // bad ruby home
            }
        }
        return patchFiles;
    }

    private static DynamicObjectFactory alwaysFrozen(DynamicObjectFactory factory) {
        return factory.getShape().addProperty(ALWAYS_FROZEN_PROPERTY).createFactory();
    }

    private static DynamicObjectFactory alwaysShared(DynamicObjectFactory factory) {
        return factory.getShape().makeSharedShape().createFactory();
    }

    private void includeModules(DynamicObject comparableModule) {
        assert RubyGuards.isRubyModule(comparableModule);

        Layouts.MODULE.getFields(objectClass).include(context, node, kernelModule);

        Layouts.MODULE.getFields(numericClass).include(context, node, comparableModule);
        Layouts.MODULE.getFields(symbolClass).include(context, node, comparableModule);

        Layouts.MODULE.getFields(arrayClass).include(context, node, enumerableModule);
        Layouts.MODULE.getFields(dirClass).include(context, node, enumerableModule);
        Layouts.MODULE.getFields(hashClass).include(context, node, enumerableModule);
        Layouts.MODULE.getFields(rangeClass).include(context, node, enumerableModule);
    }

    public void initialize() {
        initializeConstants();
    }

    public void loadCoreNodes(PrimitiveManager primitiveManager) {
        final CoreMethodNodeManager coreMethodNodeManager = new CoreMethodNodeManager(context, node.getSingletonClassNode(), primitiveManager);

        coreMethodNodeManager.loadCoreMethodNodes();

        basicObjectSendInfo = getMethod(basicObjectClass, "__send__").getSharedMethodInfo();
        kernelPublicSendInfo = getMethod(kernelModule, "public_send").getSharedMethodInfo();
        truffleBootMainInfo = getMethod(node.getSingletonClass(truffleBootModule), "main").getSharedMethodInfo();
    }

    private InternalMethod getMethod(DynamicObject module, String name) {
        InternalMethod method = Layouts.MODULE.getFields(module).getMethod(name);
        if (method == null || method.isUndefined()) {
            throw new Error("method " + module + "#" + name + " not found during CoreLibrary initialization");
        }
        return method;
    }

    private Object verbosityOption() {
        switch (context.getOptions().VERBOSITY) {
            case NIL:
                return nil;
            case FALSE:
                return false;
            case TRUE:
                return true;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void findGlobalVariableStorage() {
        loadPathReader = globalVariables.getReader("$LOAD_PATH");
        loadedFeaturesReader = globalVariables.getReader("$LOADED_FEATURES");
        debugReader = globalVariables.getReader("$DEBUG");
        verboseReader = globalVariables.getReader("$VERBOSE");
        stdinReader = globalVariables.getReader("$stdin");
        stderrReader = globalVariables.getReader("$stderr");
    }

    private void initializeConstants() {
        setConstant(truffleFFIModule, "TYPE_CHAR", NativeTypes.TYPE_CHAR);
        setConstant(truffleFFIModule, "TYPE_UCHAR", NativeTypes.TYPE_UCHAR);
        setConstant(truffleFFIModule, "TYPE_BOOL", NativeTypes.TYPE_BOOL);
        setConstant(truffleFFIModule, "TYPE_SHORT", NativeTypes.TYPE_SHORT);
        setConstant(truffleFFIModule, "TYPE_USHORT", NativeTypes.TYPE_USHORT);
        setConstant(truffleFFIModule, "TYPE_INT", NativeTypes.TYPE_INT);
        setConstant(truffleFFIModule, "TYPE_UINT", NativeTypes.TYPE_UINT);
        setConstant(truffleFFIModule, "TYPE_LONG", NativeTypes.TYPE_LONG);
        setConstant(truffleFFIModule, "TYPE_ULONG", NativeTypes.TYPE_ULONG);
        setConstant(truffleFFIModule, "TYPE_LL", NativeTypes.TYPE_LL);
        setConstant(truffleFFIModule, "TYPE_ULL", NativeTypes.TYPE_ULL);
        setConstant(truffleFFIModule, "TYPE_FLOAT", NativeTypes.TYPE_FLOAT);
        setConstant(truffleFFIModule, "TYPE_DOUBLE", NativeTypes.TYPE_DOUBLE);
        setConstant(truffleFFIModule, "TYPE_PTR", NativeTypes.TYPE_PTR);
        setConstant(truffleFFIModule, "TYPE_VOID", NativeTypes.TYPE_VOID);
        setConstant(truffleFFIModule, "TYPE_STRING", NativeTypes.TYPE_STRING);
        setConstant(truffleFFIModule, "TYPE_STRPTR", NativeTypes.TYPE_STRPTR);
        setConstant(truffleFFIModule, "TYPE_CHARARR", NativeTypes.TYPE_CHARARR);
        setConstant(truffleFFIModule, "TYPE_ENUM", NativeTypes.TYPE_ENUM);
        setConstant(truffleFFIModule, "TYPE_VARARGS", NativeTypes.TYPE_VARARGS);

        setConstant(objectClass, "RUBY_VERSION", frozenUSASCIIString(TruffleRuby.LANGUAGE_VERSION));
        setConstant(truffleModule, "RUBY_BASE_VERSION", frozenUSASCIIString(TruffleRuby.LANGUAGE_BASE_VERSION));
        setConstant(objectClass, "RUBY_PATCHLEVEL", 0);
        setConstant(objectClass, "RUBY_REVISION", TruffleRuby.LANGUAGE_REVISION);
        setConstant(objectClass, "RUBY_ENGINE", frozenUSASCIIString(TruffleRuby.ENGINE_ID));
        setConstant(objectClass, "RUBY_ENGINE_VERSION", frozenUSASCIIString(TruffleRuby.getEngineVersion()));
        setConstant(objectClass, "RUBY_PLATFORM", frozenUSASCIIString(RubyLanguage.PLATFORM));
        setConstant(objectClass, "RUBY_RELEASE_DATE", frozenUSASCIIString(BuildInformationImpl.INSTANCE.getCompileDate()));
        setConstant(objectClass, "RUBY_DESCRIPTION", frozenUSASCIIString(TruffleRuby.getVersionString(Truffle.getRuntime().getName(), TruffleOptions.AOT)));
        setConstant(objectClass, "RUBY_COPYRIGHT", frozenUSASCIIString(TruffleRuby.RUBY_COPYRIGHT));

        // BasicObject knows itself
        setConstant(basicObjectClass, "BasicObject", basicObjectClass);

        setConstant(objectClass, "ARGV", argv);

        setConstant(truffleModule, "UNDEFINED", supportUndefined);

        setConstant(encodingConverterClass, "INVALID_MASK", EConvFlags.INVALID_MASK);
        setConstant(encodingConverterClass, "INVALID_REPLACE", EConvFlags.INVALID_REPLACE);
        setConstant(encodingConverterClass, "UNDEF_MASK", EConvFlags.UNDEF_MASK);
        setConstant(encodingConverterClass, "UNDEF_REPLACE", EConvFlags.UNDEF_REPLACE);
        setConstant(encodingConverterClass, "UNDEF_HEX_CHARREF", EConvFlags.UNDEF_HEX_CHARREF);
        setConstant(encodingConverterClass, "PARTIAL_INPUT", EConvFlags.PARTIAL_INPUT);
        setConstant(encodingConverterClass, "AFTER_OUTPUT", EConvFlags.AFTER_OUTPUT);
        setConstant(encodingConverterClass, "UNIVERSAL_NEWLINE_DECORATOR", EConvFlags.UNIVERSAL_NEWLINE_DECORATOR);
        setConstant(encodingConverterClass, "CRLF_NEWLINE_DECORATOR", EConvFlags.CRLF_NEWLINE_DECORATOR);
        setConstant(encodingConverterClass, "CR_NEWLINE_DECORATOR", EConvFlags.CR_NEWLINE_DECORATOR);
        setConstant(encodingConverterClass, "XML_TEXT_DECORATOR", EConvFlags.XML_TEXT_DECORATOR);
        setConstant(encodingConverterClass, "XML_ATTR_CONTENT_DECORATOR", EConvFlags.XML_ATTR_CONTENT_DECORATOR);
        setConstant(encodingConverterClass, "XML_ATTR_QUOTE_DECORATOR", EConvFlags.XML_ATTR_QUOTE_DECORATOR);

        // Errno classes and constants
        for (Entry<String, Object> entry : context.getNativeConfiguration().getSection(ERRNO_CONFIG_PREFIX)) {
            final String name = entry.getKey().substring(ERRNO_CONFIG_PREFIX.length());
            if (name.equals("EWOULDBLOCK") && getErrnoValue("EWOULDBLOCK") == getErrnoValue("EAGAIN")) {
                continue; // Don't define it as a class, define it as constant later.
            }
            errnoValueToNames.put((int) entry.getValue(), name);
            final DynamicObject rubyClass = defineClass(errnoModule, systemCallErrorClass, name);
            setConstant(rubyClass, "Errno", entry.getValue());
            errnoClasses.put(name, rubyClass);
        }

        if (getErrnoValue("EWOULDBLOCK") == getErrnoValue("EAGAIN")) {
            setConstant(errnoModule, "EWOULDBLOCK", errnoClasses.get("EAGAIN"));
        }
    }

    private void setConstant(DynamicObject module, String name, Object value) {
        Layouts.MODULE.getFields(module).setConstant(context, node, name, value);
    }

    private DynamicObject frozenUSASCIIString(String string) {
        final Rope rope = context.getRopeCache().getRope(StringOperations.encodeRope(string, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT));
        return StringOperations.createFrozenString(context, rope);
    }

    private DynamicObject defineClass(String name) {
        return defineClass(objectClass, name);
    }

    private DynamicObject defineClass(DynamicObject superclass, String name) {
        assert RubyGuards.isRubyClass(superclass);
        return ClassNodes.createInitializedRubyClass(context, null, objectClass, superclass, name);
    }

    private DynamicObject defineClass(DynamicObject lexicalParent, DynamicObject superclass, String name) {
        assert RubyGuards.isRubyModule(lexicalParent);
        assert RubyGuards.isRubyClass(superclass);
        return ClassNodes.createInitializedRubyClass(context, null, lexicalParent, superclass, name);
    }

    private DynamicObject defineModule(String name) {
        return defineModule(null, objectClass, name);
    }

    private DynamicObject defineModule(DynamicObject lexicalParent, String name) {
        return defineModule(null, lexicalParent, name);
    }

    private DynamicObject defineModule(SourceSection sourceSection, DynamicObject lexicalParent, String name) {
        assert RubyGuards.isRubyModule(lexicalParent);
        return ModuleNodes.createModule(context, sourceSection, moduleClass, lexicalParent, name, node);
    }

    @SuppressFBWarnings("ES")
    public void loadRubyCoreLibraryAndPostBoot() {
        state = State.LOADING_RUBY_CORE;

        try {
            for (int n = 0; n < CORE_FILES.length; n++) {
                final String file = CORE_FILES[n];
                if (file == POST_BOOT_FILE) {
                    afterLoadCoreLibrary();
                    state = State.LOADED;
                }

                final RubySource source = loadCoreFile(getCoreLoadPath() + file);
                final RubyRootNode rootNode = context.getCodeLoader().parse(source, ParserContext.TOP_LEVEL, null, null, true, node);

                final CodeLoader.DeferredCall deferredCall = context.getCodeLoader().prepareExecute(
                        ParserContext.TOP_LEVEL,
                        DeclarationContext.topLevel(context),
                        rootNode,
                        null,
                        context.getCoreLibrary().getMainObject());

                TranslatorDriver.printParseTranslateExecuteMetric("before-execute", context, source.getSource());
                deferredCall.callWithoutCallNode();
                TranslatorDriver.printParseTranslateExecuteMetric("after-execute", context, source.getSource());
            }
        } catch (IOException e) {
            throw new JavaException(e);
        } catch (RaiseException e) {
            context.getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr(e.getException());
            throw new TruffleFatalException("couldn't load the core library", e);
        }
    }

    public RubySource loadCoreFile(String feature) throws IOException {
        if (feature.startsWith(RubyLanguage.RESOURCE_SCHEME)) {
            if (TruffleOptions.AOT || ParserCache.INSTANCE != null) {
                final RootParseNode rootParseNode = ParserCache.INSTANCE.get(feature);
                return new RubySource(rootParseNode.getSource());
            } else {
                final ResourceLoader resourceLoader = new ResourceLoader();
                return resourceLoader.loadResource(feature, context.getOptions().CORE_AS_INTERNAL);
            }
        } else {
            final FileLoader fileLoader = new FileLoader(context);
            return fileLoader.loadFile(context.getEnv(), feature);
        }
    }

    private void afterLoadCoreLibrary() {
        // Get some references to things defined in the Ruby core

        eagainWaitReadable = (DynamicObject) Layouts.MODULE.getFields(ioClass).getConstant("EAGAINWaitReadable").getValue();
        assert Layouts.CLASS.isClass(eagainWaitReadable);

        eagainWaitWritable = (DynamicObject) Layouts.MODULE.getFields(ioClass).getConstant("EAGAINWaitWritable").getValue();
        assert Layouts.CLASS.isClass(eagainWaitWritable);

        findGlobalVariableStorage();

        // Initialize $0 so it is set to a String as RubyGems expect, also when not run from the RubyLauncher
        DynamicObject dollarZeroValue = StringOperations.createString(context, StringOperations.encodeRope("-", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT));
        getContext().getCoreLibrary().getGlobalVariables().getStorage("$0").setValueInternal(dollarZeroValue);
    }

    @TruffleBoundary
    public DynamicObject getMetaClass(Object object) {
        if (RubyGuards.isRubyBasicObject(object)) {
            return Layouts.BASIC_OBJECT.getMetaClass((DynamicObject) object);
        } else {
            return getLogicalClass(object);
        }
    }

    @TruffleBoundary
    public DynamicObject getLogicalClass(Object object) {
        if (RubyGuards.isRubyBasicObject(object)) {
            return Layouts.BASIC_OBJECT.getLogicalClass((DynamicObject) object);
        } else if (object instanceof Boolean) {
            if ((boolean) object) {
                return trueClass;
            } else {
                return falseClass;
            }
        } else if (object instanceof Byte) {
            return integerClass;
        } else if (object instanceof Short) {
            return integerClass;
        } else if (object instanceof Integer) {
            return integerClass;
        } else if (object instanceof Long) {
            return integerClass;
        } else if (object instanceof Float) {
            return floatClass;
        } else if (object instanceof Double) {
            return floatClass;
        } else {
            return truffleInteropForeignClass;
        }
    }

    /**
     * Convert a value to a {@code Float}, without doing any lookup.
     */
    public static double toDouble(Object value, DynamicObject nil) {
        assert value != null;

        if (value == nil) {
            return 0;
        }

        if (value instanceof Integer) {
            return (int) value;
        }

        if (value instanceof Long) {
            return (long) value;
        }

        if (RubyGuards.isRubyBignum(value)) {
            return Layouts.BIGNUM.getValue((DynamicObject) value).doubleValue();
        }

        if (value instanceof Double) {
            return (double) value;
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException();
    }

    public static boolean fitsIntoInteger(long value) {
        return ((int) value) == value;
    }

    public static boolean fitsIntoUnsignedInteger(long value) {
        return value == (value & 0xffffffffL) || value < 0 && value >= Integer.MIN_VALUE;
    }

    public RubyContext getContext() {
        return context;
    }

    public SourceSection getSourceSection() {
        return sourceSection;
    }

    public String getCoreLoadPath() {
        return coreLoadPath;
    }

    public DynamicObject getArrayClass() {
        return arrayClass;
    }

    public DynamicObject getBasicObjectClass() {
        return basicObjectClass;
    }

    public DynamicObject getBigDecimalClass() {
        return bigDecimalClass;
    }

    public DynamicObject getBigDecimalOperationsModule() {
        return bigDecimalOperationsModule;
    }

    public DynamicObjectFactory getBindingFactory() {
        return bindingFactory;
    }

    public DynamicObject getClassClass() {
        return classClass;
    }

    public DynamicObject getFalseClass() {
        return falseClass;
    }

    public DynamicObjectFactory getFiberFactory() {
        return fiberFactory;
    }

    public DynamicObject getIntegerClass() {
        return integerClass;
    }

    public DynamicObject getFloatClass() {
        return floatClass;
    }

    public DynamicObject getStandardErrorClass() {
        return standardErrorClass;
    }

    public DynamicObject getLoadErrorClass() {
        return loadErrorClass;
    }

    public DynamicObject getMatchDataClass() {
        return matchDataClass;
    }

    public DynamicObject getModuleClass() {
        return moduleClass;
    }

    public DynamicObject getNameErrorClass() {
        return nameErrorClass;
    }

    public DynamicObjectFactory getNameErrorFactory() {
        return nameErrorFactory;
    }

    public DynamicObject getNilClass() {
        return nilClass;
    }

    public FrameDescriptor getEmptyDescriptor() {
        return emptyDescriptor;
    }

    public DynamicObject getNoMemoryErrorClass() {
        return noMemoryErrorClass;
    }

    public DynamicObject getNoMethodErrorClass() {
        return noMethodErrorClass;
    }

    public DynamicObjectFactory getNoMethodErrorFactory() {
        return noMethodErrorFactory;
    }

    public DynamicObject getObjectClass() {
        return objectClass;
    }

    public DynamicObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public DynamicObject getProcClass() {
        return procClass;
    }

    public DynamicObject getProcessModule() {
        return processModule;
    }

    public DynamicObject getRangeClass() {
        return rangeClass;
    }

    public DynamicObject getRationalClass() {
        return rationalClass;
    }

    public DynamicObjectFactory getRegexpFactory() {
        return regexpFactory;
    }

    public DynamicObject getTruffleFFIPointerClass() {
        return truffleFFIPointerClass;
    }

    public DynamicObject getTruffleFFINullPointerErrorClass() {
        return truffleFFINullPointerErrorClass;
    }

    public DynamicObject getTruffleTypeModule() {
        return truffleTypeModule;
    }

    public DynamicObject getGraalErrorClass() {
        return graalErrorClass;
    }

    public DynamicObject getStringClass() {
        return stringClass;
    }

    public DynamicObject getThreadClass() {
        return threadClass;
    }

    public DynamicObjectFactory getThreadFactory() {
        return threadFactory;
    }

    public DynamicObject getTypeErrorClass() {
        return typeErrorClass;
    }

    public DynamicObject getTrueClass() {
        return trueClass;
    }

    public DynamicObject getZeroDivisionErrorClass() {
        return zeroDivisionErrorClass;
    }

    public DynamicObject getKernelModule() {
        return kernelModule;
    }

    public DynamicObject getErrnoModule() {
        return errnoModule;
    }

    public GlobalVariables getGlobalVariables() {
        return globalVariables;
    }

    public DynamicObject getLoadPath() {
        return (DynamicObject) loadPathReader.getValue(globalVariables);
    }

    public DynamicObject getLoadedFeatures() {
        return (DynamicObject) loadedFeaturesReader.getValue(globalVariables);
    }

    public Object getDebug() {
        if (debugReader != null) {
            return debugReader.getValue(globalVariables);
        } else {
            return context.getOptions().DEBUG;
        }
    }

    private Object verbosity() {
        if (verboseReader != null) {
            return verboseReader.getValue(globalVariables);
        } else {
            return verbosityOption();
        }
    }

    /** true if $VERBOSE is true or false, but not nil */
    public boolean warningsEnabled() {
        return verbosity() != nil;
    }

    /** true only if $VERBOSE is true */
    public boolean isVerbose() {
        return verbosity() == Boolean.TRUE;
    }

    public Object getStdin() {
        return stdinReader.getValue(globalVariables);
    }

    public Object getStderr() {
        return stderrReader.getValue(globalVariables);
    }

    public DynamicObject getMainObject() {
        return mainObject;
    }

    public DynamicObject getNil() {
        return nil;
    }

    public DynamicObject getENV() {
        return (DynamicObject) Layouts.MODULE.getFields(objectClass).getConstant("ENV").getValue();
    }

    public DynamicObject getNumericClass() {
        return numericClass;
    }

    public DynamicObjectFactory getUnboundMethodFactory() {
        return unboundMethodFactory;
    }

    public DynamicObjectFactory getMethodFactory() {
        return methodFactory;
    }

    public DynamicObject getComplexClass() {
        return complexClass;
    }

    public DynamicObjectFactory getByteArrayFactory() {
        return byteArrayFactory;
    }

    @TruffleBoundary
    public int getErrnoValue(String errnoName) {
        return (int) context.getNativeConfiguration().get(ERRNO_CONFIG_PREFIX + errnoName);
    }

    @TruffleBoundary
    public String getErrnoName(int errnoValue) {
        return errnoValueToNames.get(errnoValue);
    }

    @TruffleBoundary
    public DynamicObject getErrnoClass(String name) {
        return errnoClasses.get(name);
    }

    public DynamicObjectFactory getSymbolFactory() {
        return symbolFactory;
    }

    public DynamicObjectFactory getThreadBacktraceLocationFactory() {
        return threadBacktraceLocationFactory;
    }

    public ConcurrentMap<String, Boolean> getPatchFiles() {
        return patchFiles;
    }

    public boolean isInitializing() {
        return state == State.INITIALIZING;
    }

    public boolean isLoadingRubyCore() {
        return state == State.LOADING_RUBY_CORE;
    }

    public boolean isLoaded() {
        return state == State.LOADED;
    }

    public boolean isSend(InternalMethod method) {
        return isSend(method.getSharedMethodInfo());
    }

    public boolean isSend(SharedMethodInfo sharedMethodInfo) {
        return sharedMethodInfo == basicObjectSendInfo || sharedMethodInfo == kernelPublicSendInfo;
    }

    public boolean isTruffleBootMainMethod(SharedMethodInfo info) {
        return info == truffleBootMainInfo;
    }

    public DynamicObjectFactory getIntRangeFactory() {
        return intRangeFactory;
    }

    public DynamicObjectFactory getLongRangeFactory() {
        return longRangeFactory;
    }

    public DynamicObjectFactory getDigestFactory() {
        return digestFactory;
    }

    public DynamicObjectFactory getArrayFactory() {
        return arrayFactory;
    }

    public DynamicObjectFactory getBignumFactory() {
        return bignumFactory;
    }

    public DynamicObjectFactory getProcFactory() {
        return procFactory;
    }

    public DynamicObjectFactory getStringFactory() {
        return stringFactory;
    }

    public DynamicObject getHashClass() {
        return hashClass;
    }

    public DynamicObjectFactory getHashFactory() {
        return hashFactory;
    }

    public Object getObjectSpaceModule() {
        return objectSpaceModule;
    }

    public DynamicObjectFactory getRandomizerFactory() {
        return randomizerFactory;
    }

    public DynamicObject getSystemExitClass() {
        return systemExitClass;
    }

    public DynamicObjectFactory getHandleFactory() {
        return handleFactory;
    }

    public DynamicObject getRuntimeErrorClass() {
        return runtimeErrorClass;
    }

    public DynamicObject getSystemStackErrorClass() {
        return systemStackErrorClass;
    }

    public DynamicObject getArgumentErrorClass() {
        return argumentErrorClass;
    }

    public DynamicObject getIndexErrorClass() {
        return indexErrorClass;
    }

    public DynamicObject getKeyErrorClass() {
        return keyErrorClass;
    }

    public DynamicObject getLocalJumpErrorClass() {
        return localJumpErrorClass;
    }

    public DynamicObject getNotImplementedErrorClass() {
        return notImplementedErrorClass;
    }

    public DynamicObject getSyntaxErrorClass() {
        return syntaxErrorClass;
    }

    public DynamicObject getFrozenErrorClass() {
        return frozenErrorClass;
    }

    public DynamicObject getFloatDomainErrorClass() {
        return floatDomainErrorClass;
    }

    public DynamicObject getIOErrorClass() {
        return ioErrorClass;
    }

    public DynamicObject getRangeErrorClass() {
        return rangeErrorClass;
    }

    public DynamicObject getRegexpErrorClass() {
        return regexpErrorClass;
    }

    public DynamicObject getEncodingClass() {
        return encodingClass;
    }

    public DynamicObjectFactory getEncodingFactory() {
        return encodingFactory;
    }

    public DynamicObject getEncodingErrorClass() {
        return encodingErrorClass;
    }

    public DynamicObject getEncodingCompatibilityErrorClass() {
        return encodingCompatibilityErrorClass;
    }

    public DynamicObject getEncodingUndefinedConversionErrorClass() {
        return encodingUndefinedConversionErrorClass;
    }

    public DynamicObject getFiberErrorClass() {
        return fiberErrorClass;
    }

    public DynamicObject getThreadErrorClass() {
        return threadErrorClass;
    }

    public DynamicObject getSecurityErrorClass() {
        return securityErrorClass;
    }

    public DynamicObject getSystemCallErrorClass() {
        return systemCallErrorClass;
    }

    public DynamicObject getTruffleModule() {
        return truffleModule;
    }

    public DynamicObject getTruffleInternalModule() {
        return truffleInternalModule;
    }

    public DynamicObject getTruffleBootModule() {
        return truffleBootModule;
    }

    public DynamicObject getTruffleExceptionOperationsModule() {
        return truffleExceptionOperationsModule;
    }

    public DynamicObject getTruffleInteropModule() {
        return truffleInteropModule;
    }

    public DynamicObject getTruffleInteropForeignClass() {
        return truffleInteropForeignClass;
    }

    public DynamicObject getTruffleKernelOperationsModule() {
        return truffleKernelOperationsModule;
    }

    public DynamicObject getTruffleThreadOperationsModule() {
        return truffleThreadOperationsModule;
    }

    public DynamicObject getTruffleRegexpOperationsModule() {
        return truffleRegexpOperationsModule;
    }

    public DynamicObject getExceptionClass() {
        return exceptionClass;
    }

    public DynamicObject getClosedQueueError() {
        return closedQueueErrorClass;
    }

    public DynamicObject getWarningModule() {
        return warningModule;
    }

    public DynamicObject getStructClass() {
        return structClass;
    }

    private static final String POST_BOOT_FILE = "/post-boot/post-boot.rb";

    public static final String[] CORE_FILES = {
            "/core/pre.rb",
            "/core/basic_object.rb",
            "/core/array.rb",
            "/core/channel.rb",
            "/core/configuration.rb",
            "/core/false.rb",
            "/core/gc.rb",
            "/core/nil.rb",
            "/core/truffle/platform.rb",
            "/core/support.rb",
            "/core/string.rb",
            "/core/random.rb",
            "/core/truffle/io_operations.rb",
            "/core/truffle/kernel_operations.rb",
            "/core/thread.rb",
            "/core/true.rb",
            "/core/type.rb",
            "/core/truffle/ffi/pointer.rb",
            "/core/truffle/ffi/pointer_access.rb",
            "/core/truffle/internal.rb",
            "/core/kernel.rb",
            "/core/truffle/boot.rb",
            "/core/truffle/debug.rb",
            "/core/truffle/encoding_operations.rb",
            "/core/truffle/exception_operations.rb",
            "/core/truffle/hash_operations.rb",
            "/core/truffle/numeric_operations.rb",
            "/core/truffle/proc_operations.rb",
            "/core/truffle/range_operations.rb",
            "/core/truffle/regexp_operations.rb",
            "/core/truffle/stat_operations.rb",
            "/core/truffle/string_operations.rb",
            "/core/truffle/backward.rb",
            "/core/truffle/truffleruby.rb",
            "/core/splitter.rb",
            "/core/stat.rb",
            "/core/io.rb",
            "/core/immediate.rb",
            "/core/module.rb",
            "/core/proc.rb",
            "/core/enumerable_helper.rb",
            "/core/enumerable.rb",
            "/core/enumerator.rb",
            "/core/argf.rb",
            "/core/exception.rb",
            "/core/hash.rb",
            "/core/comparable.rb",
            "/core/numeric.rb",
            "/core/truffle/ctype.rb",
            "/core/integer.rb",
            "/core/regexp.rb",
            "/core/transcoding.rb",
            "/core/encoding.rb",
            "/core/env.rb",
            "/core/errno.rb",
            "/core/file.rb",
            "/core/dir.rb",
            "/core/dir_glob.rb",
            "/core/file_test.rb",
            "/core/float.rb",
            "/core/marshal.rb",
            "/core/object_space.rb",
            "/core/range.rb",
            "/core/struct.rb",
            "/core/tms.rb",
            "/core/process.rb",
            "/core/truffle/process_operations.rb", // Must load after /core/regexp.rb
            "/core/signal.rb",
            "/core/symbol.rb",
            "/core/mutex.rb",
            "/core/throw_catch.rb",
            "/core/time.rb",
            "/core/rational.rb",
            "/core/rationalizer.rb",
            "/core/complex.rb",
            "/core/complexifier.rb",
            "/core/class.rb",
            "/core/binding.rb",
            "/core/math.rb",
            "/core/method.rb",
            "/core/unbound_method.rb",
            "/core/warning.rb",
            "/core/tracepoint.rb",
            "/core/truffle/interop.rb",
            "/core/truffle/polyglot.rb",
            "/core/posix.rb",
            "/core/main.rb",
            "/core/post.rb",
            POST_BOOT_FILE
    };

}
