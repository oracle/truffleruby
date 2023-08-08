/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
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
import java.lang.ref.Cleaner;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.ContextThreadLocal;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.options.OptionDescriptors;
import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.cext.ValueWrapperManager;
import org.truffleruby.collections.SharedIndicesMap;
import org.truffleruby.collections.SharedIndicesMap.LanguageArray;
import org.truffleruby.core.RubyHandle;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
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
import org.truffleruby.core.range.RubyObjectRange;
import org.truffleruby.core.regexp.RegexpCacheKey;
import org.truffleruby.core.regexp.RegexpTable;
import org.truffleruby.core.regexp.RubyMatchData;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.string.PathToTStringCache;
import org.truffleruby.core.string.TStringCache;
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
import org.truffleruby.interop.RubyInnerContext;
import org.truffleruby.interop.RubySourceLocation;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyEvalInteractiveRootNode;
import org.truffleruby.language.RubyInlineParsingRequestNode;
import org.truffleruby.language.RubyParsingRequestNode;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptorManager;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.objects.RubyObjectType;
import org.truffleruby.language.objects.classvariables.ClassVariableStorage;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.options.LanguageOptions;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.ParsingParameters;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorEnvironment;
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
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.truffleruby.stdlib.digest.RubyDigest;

import static org.truffleruby.language.RubyBaseNode.createArray;
import static org.truffleruby.language.RubyBaseNode.createString;
import static org.truffleruby.language.RubyBaseNode.nil;

@TruffleLanguage.Registration(
        name = "Ruby",
        website = "https://www.graalvm.org/ruby/",
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
        fileTypeDetectors = RubyFileTypeDetector.class,
        needsAllEncodings = true)
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

    /** This is a truly empty frame descriptor and should only by dummy root nodes which require no variables. Any other
     * root nodes should should use
     * {@link TranslatorEnvironment#newFrameDescriptorBuilder(org.truffleruby.parser.ParentFrameDescriptor, boolean)}. */
    public static final FrameDescriptor EMPTY_FRAME_DESCRIPTOR = new FrameDescriptor(nil);

    private RubyThread getOrCreateForeignThread(RubyContext context, Thread thread) {
        RubyThread foreignThread = rubyThreadInitMap.remove(thread);
        if (foreignThread == null) {
            foreignThread = context.getThreadManager().createForeignThread();
            rubyThreadInitMap.put(thread, foreignThread);
        }
        return foreignThread;
    }

    public final Map<Thread, RubyThread> rubyThreadInitMap = new ConcurrentHashMap<>();
    private final ContextThreadLocal<RubyThread> rubyThread = locals.createContextThreadLocal(
            (context, thread) -> {
                if (thread == context.getThreadManager().getOrInitializeRootJavaThread()) {
                    // Already initialized when creating the context
                    return context.getThreadManager().getRootThread();
                }

                if (context.getThreadManager().isRubyManagedThread(thread)) {
                    return Objects.requireNonNull(rubyThreadInitMap.remove(thread));
                }

                return getOrCreateForeignThread(context, thread);
            });

    public final Map<Thread, RubyFiber> rubyFiberInitMap = new ConcurrentHashMap<>();
    private final ContextThreadLocal<RubyFiber> rubyFiber = locals.createContextThreadLocal(
            (context, thread) -> {
                if (thread == context.getThreadManager().getOrInitializeRootJavaThread()) {
                    // Already initialized when creating the context
                    return context.getThreadManager().getRootThread().getRootFiber();
                }

                if (context.getThreadManager().isRubyManagedThread(thread)) {
                    return Objects.requireNonNull(rubyFiberInitMap.remove(thread));
                }

                return getOrCreateForeignThread(context, thread).getRootFiber();
            });

    private final CyclicAssumption tracingCyclicAssumption = new CyclicAssumption("object-space-tracing");
    @CompilationFinal private volatile Assumption tracingAssumption = tracingCyclicAssumption.getAssumption();

    @CompilationFinal public boolean singleContext = true;
    @CompilationFinal public Optional<RubyContext> contextIfSingleContext;
    private int numberOfContexts = 0;

    public final CyclicAssumption traceFuncUnusedAssumption = new CyclicAssumption("set_trace_func is not used");

    @CompilationFinal public String coreLoadPath;
    @CompilationFinal public String corePath;
    public final CoreMethodAssumptions coreMethodAssumptions;
    public final CoreStrings coreStrings;
    public final CoreSymbols coreSymbols;
    public final PrimitiveManager primitiveManager;
    public final TStringCache tstringCache;
    public final RegexpTable regexpTable;
    public final SymbolTable symbolTable;
    public final KeywordArgumentsDescriptorManager keywordArgumentsDescriptorManager = new KeywordArgumentsDescriptorManager();
    public final FrozenStringLiterals frozenStringLiterals;

    // GR-44025: We store the cleanerThread explicitly here to make it a clear image building failure if it would still be set.
    public Thread cleanerThread = null;
    @CompilationFinal public Cleaner cleaner = null;

    @SuppressFBWarnings("VO_VOLATILE_REFERENCE_TO_ARRAY") public volatile ValueWrapperManager.HandleBlockWeakReference[] handleBlockSharedMap = new ValueWrapperManager.HandleBlockWeakReference[0];
    public final ValueWrapperManager.HandleBlockAllocator handleBlockAllocator = new ValueWrapperManager.HandleBlockAllocator();

    @CompilationFinal public LanguageOptions options;
    @CompilationFinal private String rubyHome;
    @CompilationFinal public String cextPath;

    private TruffleFile rubyHomeTruffleFile;

    @CompilationFinal private AllocationReporter allocationReporter;
    @CompilationFinal public CoverageManager coverageManager;

    private final AtomicLong nextObjectID = new AtomicLong(ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID);
    private final PathToTStringCache pathToTStringCache = new PathToTStringCache(this);

    public final SharedIndicesMap globalVariablesMap = new SharedIndicesMap();
    private final LanguageArray<Assumption> globalVariableNeverAliasedAssumptions = new LanguageArray<>(
            globalVariablesMap,
            Assumption[]::new,
            () -> Assumption.create("global variable was never aliased: "));

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
    public final Shape innerContextShape = createShape(RubyInnerContext.class);
    public final Shape ioShape = createShape(RubyIO.class);
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
    public final Shape sourceLocationShape = createShape(RubySourceLocation.class);
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

    /* Some things (such as procs created from symbols) require a declaration frame, and this should include a slot for
     * special variable storage. This frame descriptor should be used for those frames to provide a constant frame
     * descriptor in those cases. */
    public final FrameDescriptor emptyDeclarationDescriptor = TranslatorEnvironment
            .newFrameDescriptorBuilder(null, true).build();

    public MaterializedFrame createEmptyDeclarationFrame(Object[] packedArgs, SpecialVariableStorage variables) {
        // createVirtualFrame().materialize() compiles better if this is in PE code
        final MaterializedFrame declarationFrame = Truffle
                .getRuntime()
                .createVirtualFrame(packedArgs, emptyDeclarationDescriptor)
                .materialize();

        SpecialVariableStorage.set(declarationFrame, variables);
        return declarationFrame;
    }

    private static final LanguageReference<RubyLanguage> REFERENCE = LanguageReference.create(RubyLanguage.class);

    public static RubyLanguage get(Node node) {
        return REFERENCE.get(node);
    }

    public static String getMimeType(boolean coverageEnabled) {
        return coverageEnabled ? MIME_TYPE_COVERAGE : MIME_TYPE;
    }

    public RubyLanguage() {
        coreMethodAssumptions = new CoreMethodAssumptions(this);
        coreStrings = new CoreStrings(this);
        coreSymbols = new CoreSymbols();
        primitiveManager = new PrimitiveManager();
        tstringCache = new TStringCache(coreSymbols);
        symbolTable = new SymbolTable(tstringCache, coreSymbols);
        regexpTable = new RegexpTable();
        frozenStringLiterals = new FrozenStringLiterals(tstringCache);
    }

    public RubyThread getCurrentThread() {
        return rubyThread.get();
    }

    public RubyFiber getCurrentFiber() {
        return rubyFiber.get();
    }

    @TruffleBoundary
    public RubyRegexp getRegexp(RegexpCacheKey regexp) {
        return regexpTable.getRegexpIfExists(regexp);
    }

    @TruffleBoundary
    public void addRegexp(RegexpCacheKey key, RubyRegexp regexp) {
        regexpTable.addRegexp(key, regexp);
    }

    @TruffleBoundary
    public RubySymbol getSymbol(String string) {
        return symbolTable.getSymbol(string);
    }

    @TruffleBoundary
    public RubySymbol getSymbol(AbstractTruffleString name, RubyEncoding encoding) {
        return symbolTable.getSymbol(name, encoding, false);
    }

    @TruffleBoundary
    public RubySymbol getSymbol(AbstractTruffleString name, RubyEncoding encoding, boolean preserveSymbol) {
        return symbolTable.getSymbol(name, encoding, preserveSymbol);
    }

    public Assumption getTracingAssumption() {
        return tracingAssumption;
    }

    public void invalidateTracingAssumption() {
        tracingCyclicAssumption.invalidate();
        tracingAssumption = tracingCyclicAssumption.getAssumption();
    }

    private boolean multiThreading = false;

    public boolean isMultiThreaded() {
        return multiThreading;
    }

    @Override
    protected void initializeMultiThreading(RubyContext context) {
        this.multiThreading = true;
    }

    @Override
    protected void initializeMultipleContexts() {
        LOGGER.fine("initializeMultipleContexts()");

        // TODO Make Symbol.all_symbols per context, by having a SymbolTable per context and creating new symbols with
        //  the per-language SymbolTable.

        if (contextIfSingleContext == null) { // before first context created
            this.singleContext = false;
        } else {
            throw CompilerDirectives
                    .shouldNotReachHere("#initializeMultipleContexts() called after a context was created");
        }
    }

    @Override
    public RubyContext createContext(Env env) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        synchronized (this) {
            numberOfContexts++;
            setupCleaner();

            if (this.options == null) { // First context
                this.allocationReporter = env.lookup(AllocationReporter.class);
                this.options = new LanguageOptions(env, env.getOptions(), singleContext);
                setRubyHome(env, findRubyHome());
                this.coreLoadPath = buildCoreLoadPath(this.options.CORE_LOAD_PATH);
                this.corePath = coreLoadPath + File.separator + "core" + File.separator;
                this.coverageManager = new CoverageManager(options, env.lookup(Instrumenter.class));
                primitiveManager.loadCoreMethodNodes(this.options);
            }
        }

        // Set rubyHomeTruffleFile every time, as pre-initialized contexts use a different FileSystem
        final String oldHome = this.rubyHome;
        final String newHome = findRubyHome();
        if (!Objects.equals(newHome, oldHome)) {
            throw CompilerDirectives.shouldNotReachHere(
                    "home changed for the same RubyLanguage instance: " + oldHome + " vs " + newHome);
        }
        setRubyHomeTruffleFile(env, newHome);

        LOGGER.fine("createContext() on " + Thread.currentThread());
        Metrics.printTime("before-create-context");
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
    protected void initializeContext(RubyContext context) {
        LOGGER.fine("initializeContext() on " + Thread.currentThread());

        try {
            Metrics.printTime("before-initialize-context");
            context.initialize();

            if (context.isPreInitializing()) {
                synchronized (this) {
                    setRubyHome(context.getEnv(), null);
                    resetCleaner();
                }
            }
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

        LOGGER.fine("patchContext() on " + Thread.currentThread() + ")");
        Metrics.printTime("before-patch-context");
        final LanguageOptions oldOptions = Objects.requireNonNull(this.options);
        final LanguageOptions newOptions = new LanguageOptions(newEnv, newEnv.getOptions(), singleContext);
        if (!LanguageOptions.areOptionsCompatibleOrLog(LOGGER, oldOptions, newOptions)) {
            return false;
        }

        synchronized (this) {
            setRubyHome(newEnv, findRubyHome());
            setupCleaner();
        }

        boolean patched = context.patchContext(newEnv);
        Metrics.printTime("after-patch-context");
        return patched;
    }

    @Override
    protected void finalizeContext(RubyContext context) {
        LOGGER.fine("finalizeContext() on " + Thread.currentThread());
        context.finalizeContext();
    }

    @Override
    protected void disposeContext(RubyContext context) {
        LOGGER.fine("disposeContext() on " + Thread.currentThread());
        context.disposeContext();

        if (options.COVERAGE_GLOBAL) {
            coverageManager.print(this, System.out);
        }

        synchronized (this) {
            // GR-28354: Workaround for no "before CacheStore hook"
            numberOfContexts--;
            if (numberOfContexts == 0 && !singleContext) {
                resetCleaner();
            }
        }
    }

    public static RubyContext getCurrentContext() {
        CompilerAsserts.neverPartOfCompilation("Use getContext() or RubyContext.get(Node) instead in PE code");
        return RubyContext.get(null);
    }

    public static RubyLanguage getCurrentLanguage() {
        CompilerAsserts.neverPartOfCompilation("Use getLanguage() or RubyLanguage.get(Node) instead in PE code");
        return RubyLanguage.get(null);
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
                    parsingParameters.getTStringWithEnc());
            final ParserContext parserContext = MIME_TYPE_MAIN_SCRIPT.equals(source.getMimeType())
                    ? ParserContext.TOP_LEVEL_FIRST
                    : ParserContext.TOP_LEVEL;
            final LexicalScope lexicalScope = contextIfSingleContext.map(RubyContext::getRootLexicalScope).orElse(null);
            return getCurrentContext().getCodeLoader().parse(
                    rubySource,
                    parserContext,
                    null,
                    lexicalScope,
                    parsingParameters.getCurrentNode());
        }

        RootNode root;
        if (source.isInteractive()) {
            root = new RubyEvalInteractiveRootNode(this, source);
        } else {
            final RubyContext context = Objects.requireNonNull(getCurrentContext());
            root = new RubyParsingRequestNode(
                    this,
                    context,
                    source,
                    request.getArgumentNames().toArray(StringUtils.EMPTY_STRING_ARRAY));
        }
        return root.getCallTarget();
    }

    @Override
    protected ExecutableNode parse(InlineParsingRequest request) {
        final RubyContext context = Objects.requireNonNull(getCurrentContext());
        return new RubyInlineParsingRequestNode(this, context, request.getSource(), request.getFrame());
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
        LOGGER.fine(() -> "initializeThread(" + showThread(thread) + ") on " + Thread.currentThread());

        if (thread == context.getThreadManager().getOrInitializeRootJavaThread()) {
            // Already initialized when creating the context
            return;
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            final RubyThread rubyThread = this.rubyThread.get(thread);
            if (rubyThread.thread == thread) { // new Ruby Thread
                if (thread != Thread.currentThread()) {
                    throw CompilerDirectives
                            .shouldNotReachHere("Ruby threads should be initialized on their Java thread");
                }
                context.getThreadManager().start(rubyThread, thread);
            } else { // (non-root) Fiber
                var fiber = this.rubyFiber.get(thread);
                rubyThread.setCurrentFiber(fiber);
            }
            return;
        }

        final RubyThread foreignThread = this.rubyThread.get(thread);
        context.getThreadManager().startForeignThread(foreignThread, thread);
    }

    @Override
    public void disposeThread(RubyContext context, Thread thread) {
        LOGGER.fine(() -> "disposeThread(" + showThread(thread) + ") on " + Thread.currentThread());

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
            final RubyThread rubyThread = this.rubyThread.get(thread);
            if (rubyThread.thread == thread) { // Thread
                if (thread != Thread.currentThread()) {
                    throw CompilerDirectives.shouldNotReachHere("Ruby threads should be disposed on their Java thread");
                }
                context.getThreadManager().cleanupThreadState(rubyThread, thread);
            } else { // (non-root) Fiber
                var fiber = this.rubyFiber.get(thread);
                context.fiberManager.cleanup(fiber, thread);
            }
            return;
        }

        // A foreign Thread, its Fibers are considered isRubyManagedThread()
        final RubyThread foreignThread = this.rubyThread.get(thread);
        context.getThreadManager().cleanup(foreignThread, thread);
    }

    private String showThread(Thread thread) {
        return "#" + getThreadId(thread) + " " + thread + " = " + this.rubyThread.get(thread);
    }

    @Override
    protected Object getScope(RubyContext context) {
        return context.getTopScopeObject();
    }

    private void setupCleaner() {
        assert Thread.holdsLock(this);
        if (cleaner == null) {
            cleaner = Cleaner.create(runnable -> this.cleanerThread = new Thread(runnable, "Ruby-Cleaner"));
        }
    }

    private void resetCleaner() {
        assert Thread.holdsLock(this);
        cleanerThread = null;
        cleaner = null;
    }

    public String getRubyHome() {
        return rubyHome;
    }

    public TruffleFile getRubyHomeTruffleFile() {
        return rubyHomeTruffleFile;
    }

    public String getPathRelativeToHome(String path) {
        final String home = rubyHome;
        if (home != null && path.startsWith(home) && path.length() > home.length()) {
            return path.substring(home.length() + 1);
        } else {
            return path;
        }
    }

    private void setRubyHome(Env env, String home) {
        assert Thread.holdsLock(this);
        rubyHome = home;
        setRubyHomeTruffleFile(env, home);
        cextPath = home == null ? null : home + "/lib/truffle/truffle/cext_ruby.rb";
    }

    private void setRubyHomeTruffleFile(Env env, String home) {
        rubyHomeTruffleFile = home == null ? null : env.getInternalTruffleFile(rubyHome);
    }

    private String findRubyHome() {
        final String home = searchRubyHome();
        if (RubyLanguage.LOGGER.isLoggable(Level.CONFIG)) {
            RubyLanguage.LOGGER.config("home: " + home);
        }
        return home;
    }

    // Returns a canonical path to the home
    private String searchRubyHome() {
        if (options.NO_HOME_PROVIDED) {
            RubyLanguage.LOGGER.config("--ruby.no-home-provided set");
            return null;
        }

        final String truffleReported = getLanguageHome();
        if (truffleReported != null) {
            final File home = new File(truffleReported);
            if (isRubyHome(home)) {
                RubyLanguage.LOGGER.config(
                        () -> String.format("Using Truffle-reported home %s as the Ruby home", truffleReported));
                return truffleReported;
            } else {
                RubyLanguage.LOGGER.warning(
                        String.format(
                                "Truffle-reported home %s does not look like TruffleRuby's home",
                                truffleReported));
            }
        } else {
            RubyLanguage.LOGGER.config("Truffle-reported home not set, cannot determine home from it");
        }

        RubyLanguage.LOGGER.warning(
                "could not determine TruffleRuby's home - the standard library will not be available - use --log.level=CONFIG to see details");
        return null;
    }

    private boolean isRubyHome(File path) {
        final File lib = new File(path, "lib");
        return new File(lib, "truffle").isDirectory() &&
                new File(lib, "gems").isDirectory() &&
                new File(lib, "patches").isDirectory();
    }

    @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
    public AllocationReporter getAllocationReporter() {
        return allocationReporter;
    }

    public ImmutableRubyString getFrozenStringLiteral(TruffleString tstring, RubyEncoding encoding) {
        return frozenStringLiterals.getFrozenStringLiteral(tstring, encoding);
    }

    public ImmutableRubyString getFrozenStringLiteral(InternalByteArray byteArray, boolean isImmutable,
            RubyEncoding encoding) {
        return frozenStringLiterals.getFrozenStringLiteral(byteArray, isImmutable, encoding);
    }

    public long getNextObjectID() {
        final long id = nextObjectID.getAndAdd(ObjectSpaceManager.OBJECT_ID_INCREMENT_BY);

        if (id == ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID - ObjectSpaceManager.OBJECT_ID_INCREMENT_BY) {
            throw CompilerDirectives.shouldNotReachHere("Language Object IDs exhausted");
        }

        return id;
    }

    public PathToTStringCache getPathToTStringCache() {
        return pathToTStringCache;
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
        if (firstOptions.get(OptionsCatalog.RUN_TWICE_KEY) ||
                firstOptions.get(OptionsCatalog.EXPERIMENTAL_ENGINE_CACHING_KEY)) {
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
    @TruffleBoundary
    public String getSourcePath(Source source) {
        final String path = getPath(source);
        if (path.startsWith(coreLoadPath)) {
            return "<internal:core> " + path.substring(coreLoadPath.length() + 1);
        } else {
            return path;
        }
    }

    /** Only use when no language/context is available (e.g. Node#toString). Prefer
     * {@link RubyContext#fileLine(SourceSection)} as it accounts for coreLoadPath and line offsets. */
    @TruffleBoundary
    public static String fileLineRange(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = getPath(section.getSource());

            if (section.isAvailable()) {
                if (section.getStartLine() != section.getEndLine()) {
                    return path + ":" + section.getStartLine() + "-" + section.getEndLine();
                } else {
                    return path + ":" + section.getStartLine();
                }
            } else {
                return path;
            }
        }
    }

    /** Prefer {@link RubyContext#fileLine(SourceSection)} as it is more concise. */
    @TruffleBoundary
    String fileLine(RubyContext context, SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final String path = getSourcePath(section.getSource());

            if (section.isAvailable()) {
                return path + ":" + RubySource.getStartLineAdjusted(context, section);
            } else {
                return path;
            }
        }
    }

    /** Only use when no language/context is available (e.g. Node#toString). Prefer
     * {@link RubyContext#fileLine(SourceSection)} as it accounts for coreLoadPath and line offsets. */
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

    public Object rubySourceLocation(RubyContext context, SourceSection section,
            TruffleString.FromJavaStringNode fromJavaStringNode,
            Node node) {
        if (!BacktraceFormatter.isAvailable(section)) {
            return nil;
        } else {
            var file = createString(node, fromJavaStringNode, getSourcePath(section.getSource()), Encodings.UTF_8);
            Object[] objects = new Object[]{ file, RubySource.getStartLineAdjusted(context, section) };
            return createArray(node, objects);
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

    @SuppressWarnings("deprecation") // deprecated on JDK19 by Thread#threadId, but that's added in JDK19
    public static long getThreadId(Thread thread) {
        return thread.getId();
    }
}
