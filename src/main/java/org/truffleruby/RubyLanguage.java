/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.object.Shape;
import org.graalvm.options.OptionDescriptors;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.core.RubyHandle;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.RubyEncodingConverter;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.exception.RubyNameError;
import org.truffleruby.core.exception.RubyNoMethodError;
import org.truffleruby.core.exception.RubySystemCallError;
import org.truffleruby.core.fiber.RubyFiber;
import org.truffleruby.core.hash.RubyHash;
import org.graalvm.options.OptionValues;
import org.truffleruby.core.inlined.CoreMethodAssumptions;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.method.RubyUnboundMethod;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.mutex.RubyConditionVariable;
import org.truffleruby.core.mutex.RubyMutex;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.objectspace.RubyWeakMap;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.queue.RubyQueue;
import org.truffleruby.core.queue.RubySizedQueue;
import org.truffleruby.core.range.RubyIntRange;
import org.truffleruby.core.range.RubyLongRange;
import org.truffleruby.core.range.RubyObjectRange;
import org.truffleruby.core.regexp.RubyMatchData;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeCache;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.RubyByteArray;
import org.truffleruby.core.support.RubyIO;
import org.truffleruby.core.support.RubyRandomizer;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.RubyBacktraceLocation;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.time.RubyTime;
import org.truffleruby.core.tracepoint.RubyTracePoint;
import org.truffleruby.extra.RubyAtomicReference;
import org.truffleruby.extra.ffi.RubyPointer;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyEvalInteractiveRootNode;
import org.truffleruby.language.RubyInlineParsingRequestNode;
import org.truffleruby.language.RubyParsingRequestNode;
import org.truffleruby.language.objects.RubyObjectType;
import org.truffleruby.options.LanguageOptions;
import org.truffleruby.platform.Platform;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.stdlib.CoverageManager;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.ExecutableNode;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.truffleruby.stdlib.bigdecimal.RubyBigDecimal;
import org.truffleruby.stdlib.digest.RubyDigest;

@TruffleLanguage.Registration(
        name = "Ruby",
        contextPolicy = ContextPolicy.EXCLUSIVE,
        id = TruffleRuby.LANGUAGE_ID,
        implementationName = TruffleRuby.FORMAL_NAME,
        version = TruffleRuby.LANGUAGE_VERSION,
        characterMimeTypes = TruffleRuby.MIME_TYPE,
        defaultMimeType = TruffleRuby.MIME_TYPE,
        dependentLanguages = { "nfi", "llvm" },
        fileTypeDetectors = RubyFileTypeDetector.class)
@ProvidedTags({
        CoverageManager.LineTag.class,
        TraceManager.CallTag.class,
        TraceManager.ClassTag.class,
        TraceManager.LineTag.class,
        TraceManager.NeverTag.class,
        StandardTags.RootTag.class,
        StandardTags.StatementTag.class,
        StandardTags.ReadVariableTag.class,
        StandardTags.WriteVariableTag.class,
})
public class RubyLanguage extends TruffleLanguage<RubyContext> {

    public static final String PLATFORM = String.format(
            "%s-%s%s",
            Platform.getArchName(),
            Platform.getOSName(),
            Platform.getKernelMajorVersion());

    public static final String LLVM_BITCODE_MIME_TYPE = "application/x-llvm-ir-bitcode";

    public static final String CEXT_EXTENSION = Platform.CEXT_SUFFIX;

    public static final String RESOURCE_SCHEME = "resource:";

    public static final TruffleLogger LOGGER = TruffleLogger.getLogger(TruffleRuby.LANGUAGE_ID);

    private final CyclicAssumption tracingCyclicAssumption = new CyclicAssumption("object-space-tracing");
    @CompilationFinal private volatile Assumption tracingAssumption = tracingCyclicAssumption.getAssumption();
    public final Assumption singleContextAssumption = Truffle
            .getRuntime()
            .createAssumption("single RubyContext per RubyLanguage instance");
    public final CyclicAssumption traceFuncUnusedAssumption = new CyclicAssumption("set_trace_func is not used");

    public final CoreMethodAssumptions coreMethodAssumptions;
    public final CoreStrings coreStrings;
    public final CoreSymbols coreSymbols;
    public final PrimitiveManager primitiveManager;
    public final RopeCache ropeCache;
    public final SymbolTable symbolTable;
    @CompilationFinal public LanguageOptions options;

    @CompilationFinal private AllocationReporter allocationReporter;

    private final AtomicLong nextObjectID = new AtomicLong(ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID);

    private static final RubyObjectType objectType = new RubyObjectType();

    public final Shape basicObjectShape = createShape(RubyBasicObject.class);
    public final Shape moduleShape = createShape(RubyModule.class);
    public final Shape classShape = createShape(RubyClass.class);

    // TODO (eregon, 25 Sep 2020): These Shapes should ideally be stored in the language instance,
    // so different Engines/RubyLanguage instances can have different type profiles.
    // However that requires passing the language instance around a lot which is inconvenient
    // and does not seem worth it currently. Also these builtin types are rather unlikely to have
    // instance variables.
    public static final Shape arrayShape = createShape(RubyArray.class);
    public static final Shape atomicReferenceShape = createShape(RubyAtomicReference.class);
    public static final Shape bigDecimalShape = createShape(RubyBigDecimal.class);
    public static final Shape bindingShape = createShape(RubyBinding.class);
    public static final Shape byteArrayShape = createShape(RubyByteArray.class);
    public static final Shape conditionVariableShape = createShape(RubyConditionVariable.class);
    public static final Shape digestShape = createShape(RubyDigest.class);
    public static final Shape encodingConverterShape = createShape(RubyEncodingConverter.class);
    public static final Shape encodingShape = createShape(RubyEncoding.class);
    public static final Shape exceptionShape = createShape(RubyException.class);
    public static final Shape fiberShape = createShape(RubyFiber.class);
    public static final Shape handleShape = createShape(RubyHandle.class);
    public static final Shape hashShape = createShape(RubyHash.class);
    public static final Shape intRangeShape = createShape(RubyIntRange.class);
    public static final Shape ioShape = createShape(RubyIO.class);
    public static final Shape longRangeShape = createShape(RubyLongRange.class);
    public static final Shape matchDataShape = createShape(RubyMatchData.class);
    public static final Shape methodShape = createShape(RubyMethod.class);
    public static final Shape mutexShape = createShape(RubyMutex.class);
    public static final Shape nameErrorShape = createShape(RubyNameError.class);
    public static final Shape noMethodErrorShape = createShape(RubyNoMethodError.class);
    public static final Shape objectRangeShape = createShape(RubyObjectRange.class);
    public static final Shape procShape = createShape(RubyProc.class);
    public static final Shape queueShape = createShape(RubyQueue.class);
    public static final Shape randomizerShape = createShape(RubyRandomizer.class);
    public static final Shape regexpShape = createShape(RubyRegexp.class);
    public static final Shape sizedQueueShape = createShape(RubySizedQueue.class);
    public static final Shape stringShape = createShape(RubyString.class);
    public static final Shape systemCallErrorShape = createShape(RubySystemCallError.class);
    public static final Shape threadBacktraceLocationShape = createShape(RubyBacktraceLocation.class);
    public static final Shape threadShape = createShape(RubyThread.class);
    public static final Shape timeShape = createShape(RubyTime.class);
    public static final Shape tracePointShape = createShape(RubyTracePoint.class);
    public static final Shape truffleFFIPointerShape = createShape(RubyPointer.class);
    public static final Shape unboundMethodShape = createShape(RubyUnboundMethod.class);
    public static final Shape weakMapShape = createShape(RubyWeakMap.class);

    public RubyLanguage() {
        coreMethodAssumptions = new CoreMethodAssumptions(this);
        coreStrings = new CoreStrings(this);
        coreSymbols = new CoreSymbols();
        primitiveManager = new PrimitiveManager();
        ropeCache = new RopeCache(coreSymbols);
        symbolTable = new SymbolTable(ropeCache, coreSymbols);
    }

    @TruffleBoundary
    public RubySymbol getSymbol(String string) {
        return symbolTable.getSymbol(string);
    }


    @TruffleBoundary
    public RubySymbol getSymbol(Rope rope) {
        return symbolTable.getSymbol(rope);
    }

    public Assumption getTracingAssumption() {
        return tracingAssumption;
    }

    public void invalidateTracingAssumption() {
        tracingCyclicAssumption.invalidate();
        tracingAssumption = tracingCyclicAssumption.getAssumption();
    }

    @Override
    protected void initializeMultipleContexts() {
        // TODO Make Symbol.all_symbols per context, by having a SymbolTable per context and creating new symbols with
        //  the per-language SymbolTable.
        singleContextAssumption.invalidate();
    }

    @Override
    public RubyContext createContext(Env env) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        synchronized (this) {
            if (allocationReporter == null) {
                allocationReporter = env.lookup(AllocationReporter.class);
            }
            if (this.options == null) {
                this.options = new LanguageOptions(env, env.getOptions());
                primitiveManager.loadCoreMethodNodes(this.options);
            }
        }

        LOGGER.fine("createContext()");
        Metrics.printTime("before-create-context");
        // TODO CS 3-Dec-16 need to parse RUBYOPT here if it hasn't been already?
        final RubyContext context = new RubyContext(this, env);
        Metrics.printTime("after-create-context");
        return context;
    }

    @Override
    protected void initializeContext(RubyContext context) throws Exception {
        LOGGER.fine("initializeContext()");

        try {
            Metrics.printTime("before-initialize-context");
            context.initialize();
            Metrics.printTime("after-initialize-context");
        } catch (Throwable e) {
            if (context.getOptions().EXCEPTIONS_PRINT_JAVA || context.getOptions().EXCEPTIONS_PRINT_UNCAUGHT_JAVA) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    @Override
    protected boolean patchContext(RubyContext context, Env newEnv) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        LOGGER.fine("patchContext()");
        Metrics.printTime("before-patch-context");
        boolean patched = context.patchContext(newEnv);
        Metrics.printTime("after-patch-context");
        return patched;
    }

    @Override
    protected void finalizeContext(RubyContext context) {
        LOGGER.fine("finalizeContext()");
        context.finalizeContext();
    }

    @Override
    protected void disposeContext(RubyContext context) {
        LOGGER.fine("disposeContext()");
        context.disposeContext();
    }

    public static RubyContext getCurrentContext() {
        return getCurrentContext(RubyLanguage.class);
    }

    public static RubyLanguage getCurrentLanguage() {
        return getCurrentLanguage(RubyLanguage.class);
    }

    @Override
    protected RootCallTarget parse(ParsingRequest request) {
        if (request.getSource().isInteractive()) {
            return Truffle.getRuntime().createCallTarget(new RubyEvalInteractiveRootNode(this, request.getSource()));
        } else {
            final RubyContext context = Objects.requireNonNull(getCurrentContext());
            return Truffle.getRuntime().createCallTarget(
                    new RubyParsingRequestNode(
                            this,
                            context,
                            request.getSource(),
                            request.getArgumentNames().toArray(StringUtils.EMPTY_STRING_ARRAY)));
        }
    }

    @Override
    protected ExecutableNode parse(InlineParsingRequest request) {
        final RubyContext context = Objects.requireNonNull(getCurrentContext());
        return new RubyInlineParsingRequestNode(this, context, request.getSource(), request.getFrame());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Object findExportedSymbol(RubyContext context, String symbolName, boolean onlyExplicit) {
        final Object explicit = context.getInteropManager().findExportedObject(symbolName);

        if (explicit != null) {
            return explicit;
        }

        if (onlyExplicit) {
            return null;
        }

        Object implicit = context.send(
                context.getCoreLibrary().truffleInteropModule,
                "lookup_symbol",
                symbolTable.getSymbol(symbolName));
        if (implicit == NotProvided.INSTANCE) {
            return null;
        } else {
            return implicit;
        }
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return OptionDescriptors.create(Arrays.asList(OptionsCatalog.allDescriptors()));
    }

    @Override
    protected boolean isThreadAccessAllowed(Thread thread, boolean singleThreaded) {
        return true;
    }

    @Override
    protected void initializeThread(RubyContext context, Thread thread) {
        if (thread == context.getThreadManager().getOrInitializeRootJavaThread()) {
            // Already initialized when creating the context
            return;
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            // Already initialized by the Ruby-provided Runnable
            return;
        }

        final RubyThread foreignThread = context.getThreadManager().createForeignThread();
        context.getThreadManager().startForeignThread(foreignThread, thread);
    }

    @Override
    protected void disposeThread(RubyContext context, Thread thread) {
        if (thread == context.getThreadManager().getRootJavaThread()) {
            // Let the context shutdown cleanup the main thread
            return;
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            // Already disposed by the Ruby-provided Runnable
            return;
        }

        final RubyThread rubyThread = context.getThreadManager().getForeignRubyThread(thread);
        context.getThreadManager().cleanup(rubyThread, thread);
    }

    @Override
    protected Object getScope(RubyContext context) {
        return context.getTopScopeObject();
    }

    public String getTruffleLanguageHome() {
        return getLanguageHome();
    }

    public AllocationReporter getAllocationReporter() {
        return allocationReporter;
    }

    public long getNextObjectID() {
        final long id = nextObjectID.getAndAdd(ObjectSpaceManager.OBJECT_ID_INCREMENT_BY);

        if (id == ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID - ObjectSpaceManager.OBJECT_ID_INCREMENT_BY) {
            throw CompilerDirectives.shouldNotReachHere("Language Object IDs exhausted");
        }

        return id;
    }

    private static Shape createShape(Class<? extends RubyDynamicObject> layoutClass) {
        return Shape
                .newBuilder()
                .allowImplicitCastIntToLong(true)
                .layout(layoutClass)
                .dynamicType(RubyLanguage.objectType)
                .build();
    }

    @Override
    protected boolean areOptionsCompatible(OptionValues firstOptions, OptionValues newOptions) {
        return LanguageOptions.areOptionsCompatible(firstOptions, newOptions);
    }

}
