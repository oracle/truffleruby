/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import java.io.File;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptors;
import org.jcodings.Encoding;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.cext.ValueWrapperManager;
import org.truffleruby.collections.SharedIndicesMap;
import org.truffleruby.collections.SharedIndicesMap.LanguageArray;
import org.truffleruby.core.FinalizationService;
import org.truffleruby.core.RubyHandle;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.encoding.RubyEncodingConverter;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.exception.RubyFrozenError;
import org.truffleruby.core.exception.RubyNameError;
import org.truffleruby.core.exception.RubyNoMethodError;
import org.truffleruby.core.exception.RubySyntaxError;
import org.truffleruby.core.exception.RubySystemCallError;
import org.truffleruby.core.exception.RubySystemExit;
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
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.PathToRopeCache;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeCache;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.FrozenStringLiterals;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.RubyByteArray;
import org.truffleruby.core.support.RubyCustomRandomizer;
import org.truffleruby.core.support.RubyIO;
import org.truffleruby.core.support.RubyPRNGRandomizer;
import org.truffleruby.core.support.RubySecureRandomizer;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.RubyBacktraceLocation;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.time.RubyTime;
import org.truffleruby.core.tracepoint.RubyTracePoint;
import org.truffleruby.extra.RubyAtomicReference;
import org.truffleruby.extra.RubyConcurrentMap;
import org.truffleruby.extra.ffi.RubyPointer;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyEvalInteractiveRootNode;
import org.truffleruby.language.RubyInlineParsingRequestNode;
import org.truffleruby.language.RubyParsingRequestNode;
import org.truffleruby.language.objects.RubyObjectType;
import org.truffleruby.language.objects.classvariables.ClassVariableStorage;
import org.truffleruby.options.LanguageOptions;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.ParsingParameters;
import org.truffleruby.parser.RubySource;
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
import org.truffleruby.stdlib.digest.RubyDigest;

@TruffleLanguage.Registration(
        name = "Ruby",
        contextPolicy = ContextPolicy.SHARED,
        id = TruffleRuby.LANGUAGE_ID,
        implementationName = TruffleRuby.FORMAL_NAME,
        version = TruffleRuby.LANGUAGE_VERSION,
        characterMimeTypes = {
                RubyLanguage.MIME_TYPE,
                RubyLanguage.MIME_TYPE_COVERAGE,
                RubyLanguage.MIME_TYPE_MAIN_SCRIPT },
        defaultMimeType = RubyLanguage.MIME_TYPE,
        dependentLanguages = { "nfi", "llvm", "regex" },
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
public final class RubyLanguage extends TruffleLanguage<RubyContext> {

    /** Do not access directly, instead use {@link #getMimeType(boolean)} */
    static final String MIME_TYPE = "application/x-ruby";
    public static final String MIME_TYPE_COVERAGE = "application/x-ruby;coverage=true";
    public static final String MIME_TYPE_MAIN_SCRIPT = "application/x-ruby;main-script=true";
    public static final String[] MIME_TYPES = { MIME_TYPE, MIME_TYPE_COVERAGE, MIME_TYPE_MAIN_SCRIPT };

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

    @CompilationFinal public boolean singleContext = true;
    @CompilationFinal public Optional<RubyContext> contextIfSingleContext;

    public final CyclicAssumption traceFuncUnusedAssumption = new CyclicAssumption("set_trace_func is not used");

    private final ReentrantLock safepointLock = new ReentrantLock();
    @CompilationFinal private Assumption safepointAssumption = Truffle
            .getRuntime()
            .createAssumption("SafepointManager");

    @CompilationFinal public String coreLoadPath;
    @CompilationFinal public String corePath;
    public final CoreMethodAssumptions coreMethodAssumptions;
    public final CoreStrings coreStrings;
    public final CoreSymbols coreSymbols;
    public final PrimitiveManager primitiveManager;
    public final RopeCache ropeCache;
    public final SymbolTable symbolTable;
    public final FrozenStringLiterals frozenStringLiterals;

    public final ReferenceQueue<Object> sharedReferenceQueue = new ReferenceQueue<>();
    public final FinalizationService sharedFinzationService = new FinalizationService(sharedReferenceQueue);
    public volatile ValueWrapperManager.HandleBlockWeakReference[] handleBlockSharedMap = new ValueWrapperManager.HandleBlockWeakReference[0];
    public final ValueWrapperManager.HandleBlockAllocator handleBlockAllocator = new ValueWrapperManager.HandleBlockAllocator();

    @CompilationFinal public LanguageOptions options;

    @CompilationFinal private AllocationReporter allocationReporter;
    @CompilationFinal public CoverageManager coverageManager;

    private final AtomicLong nextObjectID = new AtomicLong(ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID);
    private final PathToRopeCache pathToRopeCache = new PathToRopeCache(this);

    public final SharedIndicesMap globalVariablesMap = new SharedIndicesMap();
    private final LanguageArray<Assumption> globalVariableNeverAliasedAssumptions = new LanguageArray<>(
            globalVariablesMap,
            Assumption[]::new,
            () -> Truffle.getRuntime().createAssumption("global variable was never aliased: "));

    private static final RubyObjectType objectType = new RubyObjectType();

    public final Shape basicObjectShape = createShape(RubyBasicObject.class);
    public final Shape moduleShape = createShape(RubyModule.class);
    public final Shape classShape = createShape(RubyClass.class);

    public final Shape arrayShape = createShape(RubyArray.class);
    public final Shape atomicReferenceShape = createShape(RubyAtomicReference.class);
    public final Shape bindingShape = createShape(RubyBinding.class);
    public final Shape byteArrayShape = createShape(RubyByteArray.class);
    public final Shape concurrentMapShape = createShape(RubyConcurrentMap.class);
    public final Shape conditionVariableShape = createShape(RubyConditionVariable.class);
    public final Shape customRandomizerShape = createShape(RubyCustomRandomizer.class);
    public final Shape digestShape = createShape(RubyDigest.class);
    public final Shape encodingConverterShape = createShape(RubyEncodingConverter.class);
    public final Shape exceptionShape = createShape(RubyException.class);
    public final Shape fiberShape = createShape(RubyFiber.class);
    public final Shape frozenErrorShape = createShape(RubyFrozenError.class);
    public final Shape handleShape = createShape(RubyHandle.class);
    public final Shape hashShape = createShape(RubyHash.class);
    public final Shape intRangeShape = createShape(RubyIntRange.class);
    public final Shape ioShape = createShape(RubyIO.class);
    public final Shape longRangeShape = createShape(RubyLongRange.class);
    public final Shape matchDataShape = createShape(RubyMatchData.class);
    public final Shape methodShape = createShape(RubyMethod.class);
    public final Shape mutexShape = createShape(RubyMutex.class);
    public final Shape nameErrorShape = createShape(RubyNameError.class);
    public final Shape noMethodErrorShape = createShape(RubyNoMethodError.class);
    public final Shape objectRangeShape = createShape(RubyObjectRange.class);
    public final Shape procShape = createShape(RubyProc.class);
    public final Shape queueShape = createShape(RubyQueue.class);
    public final Shape prngRandomizerShape = createShape(RubyPRNGRandomizer.class);
    public final Shape secureRandomizerShape = createShape(RubySecureRandomizer.class);
    public final Shape sizedQueueShape = createShape(RubySizedQueue.class);
    public final Shape stringShape = createShape(RubyString.class);
    public final Shape syntaxErrorShape = createShape(RubySyntaxError.class);
    public final Shape systemCallErrorShape = createShape(RubySystemCallError.class);
    public final Shape systemExitShape = createShape(RubySystemExit.class);
    public final Shape threadBacktraceLocationShape = createShape(RubyBacktraceLocation.class);
    public final Shape threadShape = createShape(RubyThread.class);
    public final Shape timeShape = createShape(RubyTime.class);
    public final Shape tracePointShape = createShape(RubyTracePoint.class);
    public final Shape truffleFFIPointerShape = createShape(RubyPointer.class);
    public final Shape unboundMethodShape = createShape(RubyUnboundMethod.class);
    public final Shape weakMapShape = createShape(RubyWeakMap.class);

    public final Shape classVariableShape = Shape
            .newBuilder()
            .allowImplicitCastIntToLong(true)
            .layout(ClassVariableStorage.class)
            .build();

    public final ThreadLocal<ParsingParameters> parsingRequestParams = new ThreadLocal<>();

    public RubyLanguage() {
        coreMethodAssumptions = new CoreMethodAssumptions(this);
        coreStrings = new CoreStrings(this);
        coreSymbols = new CoreSymbols();
        primitiveManager = new PrimitiveManager();
        ropeCache = new RopeCache(coreSymbols);
        symbolTable = new SymbolTable(ropeCache, coreSymbols);
        frozenStringLiterals = new FrozenStringLiterals(ropeCache);
    }

    public static String getMimeType(boolean coverageEnabled) {
        return coverageEnabled ? MIME_TYPE_COVERAGE : MIME_TYPE;
    }

    @TruffleBoundary
    public static String fileLine(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = getPath(section.getSource());

            if (section.isAvailable()) {
                return path + ":" + section.getStartLine();
            } else {
                return path;
            }
        }
    }

    @TruffleBoundary
    public static String filenameLine(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = getPath(section.getSource());
            final String filename = new File(path).getName();

            if (section.isAvailable()) {
                return filename + ":" + section.getStartLine();
            } else {
                return filename;
            }
        }
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

    public Assumption getSafepointAssumption() {
        return safepointAssumption;
    }

    public void invalidateSafepointAssumption(String reason) {
        safepointLock.lock();
        safepointAssumption.invalidate(reason);
    }

    public void resetSafepointAssumption() {
        safepointAssumption = Truffle.getRuntime().createAssumption("SafepointManager");
        safepointLock.unlock();
    }

    @Override
    protected void initializeMultipleContexts() {
        LOGGER.fine("initializeMultipleContexts()");

        // TODO Make Symbol.all_symbols per context, by having a SymbolTable per context and creating new symbols with
        //  the per-language SymbolTable.

        if (contextIfSingleContext == null) { // before first context created
            this.singleContext = false;
        } else {
            throw CompilerDirectives.shouldNotReachHere("RubyLanguage#initializeMultipleContexts() called after" +
                    " context created and areOptionsCompatible() returned false");
        }
    }

    @Override
    public RubyContext createContext(Env env) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        synchronized (this) {
            if (this.options == null) { // First context
                this.allocationReporter = env.lookup(AllocationReporter.class);
                this.options = new LanguageOptions(env, env.getOptions(), singleContext);
                this.coreLoadPath = buildCoreLoadPath(this.options.CORE_LOAD_PATH);
                this.corePath = coreLoadPath + File.separator + "core" + File.separator;
                this.coverageManager = new CoverageManager(options, env.lookup(Instrumenter.class));
                primitiveManager.loadCoreMethodNodes(this.options);
            }
        }

        LOGGER.fine("createContext()");
        Metrics.printTime("before-create-context");
        // TODO CS 3-Dec-16 need to parse RUBYOPT here if it hasn't been already?
        final RubyContext context = new RubyContext(this, env);
        Metrics.printTime("after-create-context");
        if (singleContext) {
            contextIfSingleContext = Optional.of(context);
        } else {
            contextIfSingleContext = Optional.empty();
        }
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

        applicationStarts();
    }

    private void applicationStarts() {
        // Set breakpoints on this line to break when user code is about to be loaded
        return;
    }

    @Override
    protected boolean patchContext(RubyContext context, Env newEnv) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        LOGGER.fine("patchContext()");
        Metrics.printTime("before-patch-context");
        final LanguageOptions oldOptions = Objects.requireNonNull(this.options);
        final LanguageOptions newOptions = new LanguageOptions(newEnv, newEnv.getOptions(), singleContext);
        if (!LanguageOptions.areOptionsCompatibleOrLog(LOGGER, oldOptions, newOptions)) {
            return false;
        }

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

        if (options.COVERAGE_GLOBAL) {
            coverageManager.print(this, System.out);
        }
    }

    public static RubyContext getCurrentContext() {
        CompilerAsserts.neverPartOfCompilation("Use getContext() or @CachedContext instead in PE code");
        return getCurrentContext(RubyLanguage.class);
    }

    public static RubyLanguage getCurrentLanguage() {
        CompilerAsserts.neverPartOfCompilation("Use getLanguage() or @CachedLanguage instead in PE code");
        return getCurrentLanguage(RubyLanguage.class);
    }

    @Override
    protected RootCallTarget parse(ParsingRequest request) {
        final Source source = request.getSource();

        final ParsingParameters parsingParameters = parsingRequestParams.get();
        if (parsingParameters != null) { // from #require or core library
            assert parsingParameters.getSource().equals(source);
            final RubySource rubySource = new RubySource(
                    source,
                    parsingParameters.getPath(),
                    parsingParameters.getRope());
            final ParserContext parserContext = MIME_TYPE_MAIN_SCRIPT.equals(source.getMimeType())
                    ? ParserContext.TOP_LEVEL_FIRST
                    : ParserContext.TOP_LEVEL;
            final LexicalScope lexicalScope = contextIfSingleContext.map(RubyContext::getRootLexicalScope).orElse(null);
            return RubyLanguage.getCurrentContext().getCodeLoader().parse(
                    rubySource,
                    parserContext,
                    null,
                    lexicalScope,
                    true,
                    parsingParameters.getCurrentNode());
        }

        if (source.isInteractive()) {
            return Truffle.getRuntime().createCallTarget(new RubyEvalInteractiveRootNode(this, source));
        } else {
            final RubyContext context = Objects.requireNonNull(getCurrentContext());
            return Truffle.getRuntime().createCallTarget(
                    new RubyParsingRequestNode(
                            this,
                            context,
                            source,
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

        Object implicit = RubyContext.send(
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
    public void initializeThread(RubyContext context, Thread thread) {
        LOGGER.fine(() -> "initializeThread(#" + thread.getId() + " " + thread + ")");

        if (thread == context.getThreadManager().getOrInitializeRootJavaThread()) {
            // Already initialized when creating the context
            return;
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            final RubyThread rubyThread = context.getThreadManager().getCurrentThreadOrNull();
            if (rubyThread != null && rubyThread.thread == thread) { // new Ruby Thread
                if (thread != Thread.currentThread()) {
                    throw CompilerDirectives
                            .shouldNotReachHere("Ruby threads should be initialized on their Java thread");
                }
                context.getThreadManager().start(rubyThread, thread);
            } else {
                // Fiber
            }
            return;
        }

        final RubyThread foreignThread = context.getThreadManager().createForeignThread();
        context.getThreadManager().startForeignThread(foreignThread, thread);
    }

    @Override
    public void disposeThread(RubyContext context, Thread thread) {
        LOGGER.fine(
                () -> "disposeThread(#" + thread.getId() + " " + thread + " " +
                        context.getThreadManager().getCurrentThreadOrNull() + ")");

        if (thread == context.getThreadManager().getRootJavaThread()) {
            if (context.getEnv().isPreInitialization()) {
                // Cannot save the root Java Thread instance in the image
                context.getThreadManager().resetMainThread();
                context.getThreadManager().dispose();
                return;
            } else if (!context.isInitialized()) {
                // Context patching failed, we cannot cleanup the main thread as it was not initialized
                return;
            } else {
                // Cleanup the main thread, this is done between finalizeContext() and disposeContext()
                context.getThreadManager().cleanupThreadState(context.getThreadManager().getRootThread(), thread);
                return;
            }
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            final RubyThread rubyThread = context.getThreadManager().getCurrentThreadOrNull();
            if (rubyThread != null && rubyThread.thread == thread) { // Thread
                if (thread != Thread.currentThread()) {
                    throw CompilerDirectives.shouldNotReachHere("Ruby threads should be disposed on their Java thread");
                }
                context.getThreadManager().cleanupThreadState(rubyThread, thread);
            } else { // (non-root) Fiber
                // Fibers are always cleaned up by their thread's cleanup with FiberManager#killOtherFibers()
            }
            return;
        }

        // A foreign Thread, its Fibers are considered isRubyManagedThread()
        final RubyThread rubyThread = context.getThreadManager().getRubyThread(thread);
        context.getThreadManager().cleanup(rubyThread, thread);
    }

    @Override
    protected Object getScope(RubyContext context) {
        return context.getTopScopeObject();
    }

    public String getTruffleLanguageHome() {
        return getLanguageHome();
    }

    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    public AllocationReporter getAllocationReporter() {
        return allocationReporter;
    }

    public ImmutableRubyString getFrozenStringLiteral(byte[] bytes, Encoding encoding, CodeRange codeRange) {
        return frozenStringLiterals.getFrozenStringLiteral(bytes, encoding, codeRange);
    }

    public ImmutableRubyString getFrozenStringLiteral(Rope rope) {
        return frozenStringLiterals.getFrozenStringLiteral(rope);
    }

    public long getNextObjectID() {
        final long id = nextObjectID.getAndAdd(ObjectSpaceManager.OBJECT_ID_INCREMENT_BY);

        if (id == ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID - ObjectSpaceManager.OBJECT_ID_INCREMENT_BY) {
            throw CompilerDirectives.shouldNotReachHere("Language Object IDs exhausted");
        }

        return id;
    }

    public PathToRopeCache getPathToRopeCache() {
        return pathToRopeCache;
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
        final boolean compatible = checkAreOptionsCompatible(firstOptions, newOptions);
        LOGGER.fine(compatible ? "areOptionsCompatible() -> true" : "areOptionsCompatible() -> false");
        return compatible;
    }

    private boolean checkAreOptionsCompatible(OptionValues firstOptions, OptionValues newOptions) {
        if (singleContext) {
            return false;
        }

        if (options.RUN_TWICE || options.EXPERIMENTAL_ENGINE_CACHING) {
            return LanguageOptions.areOptionsCompatible(firstOptions, newOptions);
        } else {
            return false;
        }
    }

    /** {@link RubyLanguage#getSourcePath(Source)} should be used instead whenever possible (i.e., when we can access
     * the language).
     *
     * Returns the path of a Source. Returns the short, potentially relative, path for the main script. Note however
     * that the path of {@code eval(code, nil, filename)} is just {@code filename} and might not be absolute. */
    public static String getPath(Source source) {
        final String path = source.getPath();
        if (path != null) {
            return path;
        } else {
            // non-file sources: eval(), main_boot_source, etc
            final String name = source.getName();
            assert name != null;
            return name;
        }
    }

    /** {@link RubyLanguage#getPath(Source)} but also handles core library sources. Ideally this method would be static
     * but for now the core load path is an option and it also depends on the current working directory. Once we have
     * Source metadata in Truffle we could use that to identify core library sources without needing the language. */
    public String getSourcePath(Source source) {
        final String path = getPath(source);
        if (path.startsWith(coreLoadPath)) {
            return "<internal:core> " + path.substring(coreLoadPath.length() + 1);
        } else {
            return getPath(source);
        }
    }

    public int getGlobalVariableIndex(String name) {
        return globalVariablesMap.lookup(name);
    }

    @TruffleBoundary
    public Assumption getGlobalVariableNeverAliasedAssumption(int index) {
        return globalVariableNeverAliasedAssumptions.get(index);
    }

    private static String buildCoreLoadPath(String coreLoadPath) {
        while (coreLoadPath.endsWith("/")) {
            coreLoadPath = coreLoadPath.substring(0, coreLoadPath.length() - 1);
        }

        if (coreLoadPath.startsWith(RubyLanguage.RESOURCE_SCHEME)) {
            return coreLoadPath;
        }

        try {
            return new File(coreLoadPath).getCanonicalPath();
        } catch (IOException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

}
