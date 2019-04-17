/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage.Env;
import com.oracle.truffle.api.CompilerOptions;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptor;
import org.joni.Regex;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.cext.ValueWrapperManager;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.FinalizationService;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.MarkingService;
import org.truffleruby.core.ReferenceProcessingService.ReferenceProcessor;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.hash.PreInitializationManager;
import org.truffleruby.core.hash.ReHashable;
import org.truffleruby.core.inlined.CoreMethods;
import org.truffleruby.core.kernel.AtExitManager;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.regexp.RegexpCacheKey;
import org.truffleruby.core.rope.PathToRopeCache;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeCache;
import org.truffleruby.core.rope.RopeKey;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.FrozenStringLiterals;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.time.GetTimeZoneNode;
import org.truffleruby.interop.InteropManager;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FeatureLoader;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.options.Options;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.parser.TranslatorDriver;
import org.truffleruby.platform.Platform;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.darwin.DarwinNativeConfiguration;
import org.truffleruby.platform.linux.LinuxNativeConfiguration;
import org.truffleruby.platform.solaris.SolarisSparcV9NativeConfiguration;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.RubyOptionTypes;
import org.truffleruby.stdlib.CoverageManager;
import org.truffleruby.stdlib.readline.ConsoleHolder;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class RubyContext {

    private final RubyLanguage language;
    @CompilationFinal private TruffleLanguage.Env env;

    private final AllocationReporter allocationReporter;

    @CompilationFinal private Options options;
    @CompilationFinal private String rubyHome;

    private final PrimitiveManager primitiveManager = new PrimitiveManager();
    private final SafepointManager safepointManager = new SafepointManager(this);
    private final SymbolTable symbolTable;
    private final InteropManager interopManager = new InteropManager(this);
    private final CodeLoader codeLoader = new CodeLoader(this);
    private final FeatureLoader featureLoader = new FeatureLoader(this);
    private final TraceManager traceManager;
    private final ReferenceProcessor referenceProcessor;
    private final FinalizationService finalizationService;
    private final MarkingService markingService;
    private final ObjectSpaceManager objectSpaceManager = new ObjectSpaceManager(this);
    private final SharedObjects sharedObjects = new SharedObjects(this);
    private final AtExitManager atExitManager = new AtExitManager(this);
    private final CallStackManager callStack = new CallStackManager(this);
    private final CoreStrings coreStrings = new CoreStrings(this);
    private final FrozenStringLiterals frozenStringLiterals = new FrozenStringLiterals(this);
    private final CoreExceptions coreExceptions = new CoreExceptions(this);
    private final EncodingManager encodingManager = new EncodingManager(this);
    private final WeakValueCache<RegexpCacheKey, Regex> regexpCache = new WeakValueCache<>();
    private final PreInitializationManager preInitializationManager;
    private final NativeConfiguration nativeConfiguration;
    private final ValueWrapperManager valueWrapperManager = new ValueWrapperManager(this);
    private final WeakValueCache<RopeKey, DynamicObject> internedStringCache = new WeakValueCache<>();

    private final CompilerOptions compilerOptions = Truffle.getRuntime().createCompilerOptions();

    @CompilationFinal private SecureRandom random;
    private final Hashing hashing;
    @CompilationFinal BacktraceFormatter defaultBacktraceFormatter;
    private final BacktraceFormatter userBacktraceFormatter;
    private final RopeCache ropeCache;
    private final PathToRopeCache pathToRopeCache = new PathToRopeCache(this);
    @CompilationFinal private TruffleNFIPlatform truffleNFIPlatform;
    private final CoreLibrary coreLibrary;
    @CompilationFinal private CoreMethods coreMethods;
    private final ThreadManager threadManager;
    private final LexicalScope rootLexicalScope;
    private final CoverageManager coverageManager;
    private volatile ConsoleHolder consoleHolder;

    private final Object classVariableDefinitionLock = new Object();
    private final ReentrantLock cExtensionsLock = new ReentrantLock();

    private final boolean preInitialized;
    @CompilationFinal private boolean preInitializing;
    private boolean initialized;
    private volatile boolean finalizing;

    private Source mainSource = null;
    private String mainSourceAbsolutePath = null;

    private static boolean preInitializeContexts = TruffleRuby.PRE_INITIALIZE_CONTEXTS;

    private static final boolean LIBPOLYGLOT = Boolean.getBoolean("graalvm.libpolyglot");

    public RubyContext(RubyLanguage language, TruffleLanguage.Env env) {
        Metrics.printTime("before-context-constructor");

        this.preInitializing = preInitializeContexts;
        RubyContext.preInitializeContexts = false; // Only the first context is pre-initialized
        this.preInitialized = preInitializing;

        preInitializationManager = preInitializing ? new PreInitializationManager(this) : null;

        this.language = language;
        this.env = env;

        allocationReporter = env.lookup(AllocationReporter.class);

        options = createOptions(env);

        referenceProcessor = new ReferenceProcessor(this);
        finalizationService = new FinalizationService(this, referenceProcessor);
        markingService = new MarkingService(this, referenceProcessor);

        // We need to construct this at runtime
        random = createRandomInstance();

        hashing = new Hashing(generateHashingSeed());

        defaultBacktraceFormatter = BacktraceFormatter.createDefaultFormatter(this);
        userBacktraceFormatter = new BacktraceFormatter(this, BacktraceFormatter.USER_BACKTRACE_FLAGS);

        ropeCache = new RopeCache(this);

        rubyHome = findRubyHome(options);

        // Stuff that needs to be loaded before we load any code

            /*
             * The Graal option TimeThreshold sets how long a method has to become hot after it has started running, in ms.
             * This is designed to not try to compile cold methods that just happen to be called enough times during a
             * very long running program. We haven't worked out the best value of this for Ruby yet, and the default value
             * produces poor benchmark results. Here we just set it to a very high value, to effectively disable it.
             */

        if (compilerOptions.supportsOption("MinTimeThreshold")) {
            compilerOptions.setOption("MinTimeThreshold", 100000000);
        }

            /*
             * The Graal option InliningMaxCallerSize sets the maximum size of a method for where we consider to inline
             * calls from that method. So it's the caller method we're talking about, not the called method. The default
             * value doesn't produce good results for Ruby programs, but we aren't sure why yet. Perhaps it prevents a few
             * key methods from the core library from inlining other methods.
             */

        if (compilerOptions.supportsOption("MinInliningMaxCallerSize")) {
            compilerOptions.setOption("MinInliningMaxCallerSize", 5000);
        }

        // Load the core library classes

        Metrics.printTime("before-create-core-library");
        coreLibrary = new CoreLibrary(this);
        nativeConfiguration = loadNativeConfiguration();
        coreLibrary.initialize();
        Metrics.printTime("after-create-core-library");

        symbolTable = new SymbolTable(ropeCache, coreLibrary.getSymbolFactory(), this);
        rootLexicalScope = new LexicalScope(null, coreLibrary.getObjectClass());

        // Create objects that need core classes

        truffleNFIPlatform = isPreInitializing() ? null : createNativePlatform();

        // The encoding manager relies on POSIX having been initialized, so we can't process it during
        // normal core library initialization.
        Metrics.printTime("before-initialize-encodings");
        encodingManager.defineEncodings();
        encodingManager.initializeDefaultEncodings(truffleNFIPlatform, nativeConfiguration);
        Metrics.printTime("after-initialize-encodings");

        Metrics.printTime("before-thread-manager");
        threadManager = new ThreadManager(this);
        threadManager.initialize(truffleNFIPlatform, nativeConfiguration);
        Metrics.printTime("after-thread-manager");

        Metrics.printTime("before-instruments");
        final Instrumenter instrumenter = env.lookup(Instrumenter.class);
        traceManager = new TraceManager(this, instrumenter);
        coverageManager = new CoverageManager(this, instrumenter);
        Metrics.printTime("after-instruments");

        Metrics.printTime("after-context-constructor");
    }

    public void initialize() {
        // Load the nodes

        Metrics.printTime("before-load-nodes");
        coreLibrary.loadCoreNodes(primitiveManager);
        Metrics.printTime("after-load-nodes");

        // Capture known builtin methods

        coreMethods = new CoreMethods(this);

        // Load the part of the core library defined in Ruby

        Metrics.printTime("before-load-core");
        coreLibrary.loadRubyCoreLibraryAndPostBoot();
        Metrics.printTime("after-load-core");

        // Share once everything is loaded
        if (options.SHARED_OBJECTS_ENABLED && options.SHARED_OBJECTS_FORCE) {
            sharedObjects.startSharing();
        }

        if (isPreInitializing()) {
            // Cannot save the file descriptor in this SecureRandom in the image
            random = null;
            // Cannot save the root Java Thread instance in the image
            threadManager.resetMainThread();
        } else {
            initialized = true;
        }

        this.preInitializing = false;
    }

    /**
     * Re-initialize parts of the RubyContext depending on the running process. This is a small
     * subset of the full initialization which needs to be performed to adapt to the new process and
     * external environment. Calls are kept in the same order as during normal initialization.
     */
    protected boolean patch(Env newEnv) {
        this.env = newEnv;

        final Options oldOptions = this.options;
        final Options newOptions = createOptions(newEnv);
        final String oldHome = this.rubyHome;
        final String newHome = findRubyHome(newOptions);
        if (!compatibleOptions(oldOptions, newOptions, oldHome, newHome)) {
            return false;
        }
        this.options = newOptions;
        this.rubyHome = newHome;

        // Re-read the value of $TZ as it can be different in the new process
        GetTimeZoneNode.invalidateTZ();

        random = createRandomInstance();
        hashing.patchSeed(generateHashingSeed());

        this.defaultBacktraceFormatter = BacktraceFormatter.createDefaultFormatter(this);

        this.truffleNFIPlatform = createNativePlatform();
        encodingManager.initializeDefaultEncodings(truffleNFIPlatform, nativeConfiguration);

        threadManager.restartMainThread(Thread.currentThread());
        threadManager.initialize(truffleNFIPlatform, nativeConfiguration);

        Metrics.printTime("before-rehash");
        preInitializationManager.rehash();
        Metrics.printTime("after-rehash");

        Metrics.printTime("before-run-delayed-initialization");
        final Object toRunAtInit = Layouts.MODULE.getFields(coreLibrary.getTruffleBootModule()).getConstant("TO_RUN_AT_INIT").getValue();
        for (Object proc : ArrayOperations.toIterable((DynamicObject) toRunAtInit)) {
            final Source source = Layouts.PROC.getMethod((DynamicObject) proc).getSharedMethodInfo().getSourceSection().getSource();
            TranslatorDriver.printParseTranslateExecuteMetric("before-run-delayed-initialization", this, source);
            ProcOperations.rootCall((DynamicObject) proc);
            TranslatorDriver.printParseTranslateExecuteMetric("after-run-delayed-initialization", this, source);
        }
        Metrics.printTime("after-run-delayed-initialization");

        initialized = true;
        return true;
    }

    protected boolean patchContext(Env newEnv) {
        try {
            return patch(newEnv);
        } catch (RaiseException e) {
            System.err.println("Exception during RubyContext.patch():");
            getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr(e.getException());
            throw e;
        } catch (Throwable e) {
            System.err.println("Exception during RubyContext.patch():");
            e.printStackTrace();
            throw e;
        }
    }

    private boolean compatibleOptions(Options oldOptions, Options newOptions, String oldHome, String newHome) {
        final String notReusingContext = "not reusing pre-initialized context: ";

        if (!newOptions.PREINITIALIZATION) {
            RubyLanguage.LOGGER.fine(notReusingContext + "--preinit is false");
            return false;
        }

        if (!newOptions.CORE_LOAD_PATH.equals(OptionsCatalog.CORE_LOAD_PATH_KEY.getDefaultValue())) {
            RubyLanguage.LOGGER.fine(notReusingContext + "--core-load-path is set: " + newOptions.CORE_LOAD_PATH);
            return false; // Should load the specified core files
        }

        if ((oldHome != null) != (newHome != null)) {
            RubyLanguage.LOGGER.fine(notReusingContext + "Ruby home is " + (newHome != null ? "set (" + newHome + ")" : "unset"));
            return false;
        }

        // Libraries loaded during pre-initialization

        if (newOptions.PATCHING != oldOptions.PATCHING) {
            RubyLanguage.LOGGER.fine(notReusingContext + "loading patching is " + newOptions.PATCHING);
            return false;
        }

        if (newOptions.LAZY_RUBYGEMS != oldOptions.LAZY_RUBYGEMS) {
            RubyLanguage.LOGGER.fine(notReusingContext + "loading lazy-rubygems is " + newOptions.LAZY_RUBYGEMS);
            return false;
        }

        if (newOptions.DID_YOU_MEAN != oldOptions.DID_YOU_MEAN) {
            RubyLanguage.LOGGER.fine(notReusingContext + "loading did_you_mean is " + newOptions.DID_YOU_MEAN);
            return false;
        }

        return true;
    }

    private Options createOptions(TruffleLanguage.Env env) {
        Metrics.printTime("before-options");
        final Options options = new Options(env, env.getOptions());

        if (options.OPTIONS_LOG && RubyLanguage.LOGGER.isLoggable(Level.CONFIG)) {
            for (OptionDescriptor descriptor : OptionsCatalog.allDescriptors()) {
                assert descriptor.getName().startsWith(TruffleRuby.LANGUAGE_ID);
                final String xName = descriptor.getName().substring(TruffleRuby.LANGUAGE_ID.length() + 1);
                RubyLanguage.LOGGER.config("option " + xName + "=" + RubyOptionTypes.valueToString(options.fromDescriptor(descriptor)));
            }
        }

        Metrics.printTime("after-options");
        return options;
    }

    private long generateHashingSeed() {
        if (options.HASHING_DETERMINISTIC) {
            RubyLanguage.LOGGER.severe("deterministic hashing is enabled - this may make you vulnerable to denial of service attacks");
            return 7114160726623585955L;
        } else {
            final byte[] bytes = getRandomSeedBytes(Long.BYTES);
            final ByteBuffer buffer = ByteBuffer.wrap(bytes);
            return buffer.getLong();
        }
    }

    private TruffleNFIPlatform createNativePlatform() {
        Metrics.printTime("before-create-native-platform");
        final TruffleNFIPlatform truffleNFIPlatform = options.NATIVE_PLATFORM ? new TruffleNFIPlatform(this) : null;
        featureLoader.initialize(nativeConfiguration, truffleNFIPlatform);
        Metrics.printTime("after-create-native-platform");
        return truffleNFIPlatform;
    }

    private NativeConfiguration loadNativeConfiguration() {
        final NativeConfiguration nativeConfiguration = new NativeConfiguration();

        switch (Platform.OS) {
            case LINUX:
                LinuxNativeConfiguration.load(nativeConfiguration, this);
                break;
            case DARWIN:
                DarwinNativeConfiguration.load(nativeConfiguration, this);
                break;
            case SOLARIS:
                SolarisSparcV9NativeConfiguration.load(nativeConfiguration, this);
                break;
            default:
                RubyLanguage.LOGGER.severe("no native configuration for this platform");
                break;
        }

        return nativeConfiguration;
    }

    @TruffleBoundary
    public Object send(Object object, String methodName, Object... arguments) {
        final InternalMethod method = ModuleOperations.lookupMethodUncached(coreLibrary.getMetaClass(object), methodName, null);
        if (method == null || method.isUndefined()) {
            return null;
        }

        return method.getCallTarget().call(
                RubyArguments.pack(null, null, method, null, object, null, arguments));
    }

    public void finalizeContext() {
        if (!initialized) {
            // The RubyContext will be finalized and disposed if patching fails (potentially for
            // another language). In that case, there is nothing to clean or execute.
            return;
        }

        finalizing = true;

        atExitManager.runSystemExitHooks();
        threadManager.killAndWaitOtherThreads();
    }

    private final ReentrantLock disposeLock = new ReentrantLock();
    private boolean disposed = false;

    public void disposeContext() {
        disposeLock.lock();
        try {
            if (!disposed) {
                dispose();
                disposed = true;
            }
        } finally {
            disposeLock.unlock();
        }
    }

    private void dispose() {
        if (!initialized) {
            // The RubyContext will be finalized and disposed if patching fails (potentially for
            // another language). In that case, there is nothing to clean or execute.
            return;
        }

        threadManager.cleanupMainThread();
        safepointManager.checkNoRunningThreads();

        if (options.ROPE_PRINT_INTERN_STATS) {
            RubyLanguage.LOGGER.info("ropes re-used: " + getRopeCache().getRopesReusedCount());
            RubyLanguage.LOGGER.info("rope byte arrays re-used: " + getRopeCache().getByteArrayReusedCount());
            RubyLanguage.LOGGER.info("rope bytes saved: " + getRopeCache().getRopeBytesSaved());
            RubyLanguage.LOGGER.info("total ropes interned: " + getRopeCache().totalRopes());
        }

        if (options.COVERAGE_GLOBAL) {
            coverageManager.print(System.out);
        }
    }

    public boolean isPreInitializing() {
        return preInitializing;
    }

    public boolean wasPreInitialized() {
        return preInitialized;
    }

    public RubyLanguage getLanguage() {
        return language;
    }

    public Options getOptions() {
        return options;
    }

    public TruffleLanguage.Env getEnv() {
        return env;
    }

    /** Hashing for a RubyNode, the seed should only be used for a Ruby-level #hash method */
    public Hashing getHashing(RubyNode node) {
        return hashing;
    }

    /**
     * With context pre-initialization, the random seed must be reset at runtime. So every use of
     * the random seed through Hashing should provide a way to rehash to take the new random seed in
     * account.
     */
    public Hashing getHashing(ReHashable reHashable) {
        if (isPreInitializing()) {
            preInitializationManager.addReHashable(reHashable);
        }
        return hashing;
    }

    public BacktraceFormatter getDefaultBacktraceFormatter() {
        return defaultBacktraceFormatter;
    }

    public BacktraceFormatter getUserBacktraceFormatter() {
        return userBacktraceFormatter;
    }

    public AllocationReporter getAllocationReporter() {
        return allocationReporter;
    }

    public CoreLibrary getCoreLibrary() {
        return coreLibrary;
    }

    public CoreMethods getCoreMethods() {
        return coreMethods;
    }

    public FeatureLoader getFeatureLoader() {
        return featureLoader;
    }

    public FinalizationService getFinalizationService() {
        return finalizationService;
    }

    public MarkingService getMarkingService() {
        return markingService;
    }

    public ObjectSpaceManager getObjectSpaceManager() {
        return objectSpaceManager;
    }

    public SharedObjects getSharedObjects() {
        return sharedObjects;
    }

    public ThreadManager getThreadManager() {
        return threadManager;
    }

    public AtExitManager getAtExitManager() {
        return atExitManager;
    }

    public TraceManager getTraceManager() {
        return traceManager;
    }

    public SafepointManager getSafepointManager() {
        return safepointManager;
    }

    public LexicalScope getRootLexicalScope() {
        return rootLexicalScope;
    }

    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    public PrimitiveManager getPrimitiveManager() {
        return primitiveManager;
    }

    public CoverageManager getCoverageManager() {
        return coverageManager;
    }

    public RopeCache getRopeCache() {
        return ropeCache;
    }

    public PathToRopeCache getPathToRopeCache() {
        return pathToRopeCache;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public CodeLoader getCodeLoader() {
        return codeLoader;
    }

    public InteropManager getInteropManager() {
        return interopManager;
    }

    public CallStackManager getCallStack() {
        return callStack;
    }

    public CoreStrings getCoreStrings() {
        return coreStrings;
    }

    public DynamicObject getFrozenStringLiteral(Rope rope) {
        return frozenStringLiterals.getFrozenStringLiteral(rope);
    }

    public Object getClassVariableDefinitionLock() {
        return classVariableDefinitionLock;
    }

    public ReentrantLock getCExtensionsLock() {
        return cExtensionsLock;
    }

    public Instrumenter getInstrumenter() {
        return env.lookup(Instrumenter.class);
    }

    public CoreExceptions getCoreExceptions() {
        return coreExceptions;
    }

    public EncodingManager getEncodingManager() {
        return encodingManager;
    }

    public PreInitializationManager getPreInitializationManager() {
        return preInitializationManager;
    }

    public String getRubyHome() {
        return rubyHome;
    }

    public ConsoleHolder getConsoleHolder() {
        if (consoleHolder == null) {
            synchronized (this) {
                if (consoleHolder == null) {
                    consoleHolder = ConsoleHolder.create(this);
                }
            }
        }

        return consoleHolder;
    }

    public void setConsoleHolder(ConsoleHolder consoleHolder) {
        synchronized (this) {
            this.consoleHolder = consoleHolder;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isFinalizing() {
        return finalizing;
    }

    private String findRubyHome(Options options) {
        final String home;
        try {
            home = searchRubyHome(options);
        } catch (IOException e) {
            throw new JavaException(e);
        }
        if (RubyLanguage.LOGGER.isLoggable(Level.CONFIG)) {
            RubyLanguage.LOGGER.config("home: " + home);
        }
        return home;
    }

    // Returns a canonical path to the home
    @TruffleBoundary
    private String searchRubyHome(Options options) throws IOException {
        if (options.NO_HOME_PROVIDED) {
            RubyLanguage.LOGGER.config("--ruby.no-home-provided set");
            return null;
        }

        // Use the option if it was set

        if (!options.HOME.isEmpty()) {
            final File home = new File(options.HOME);
            RubyLanguage.LOGGER.config(() -> String.format("trying --home=%s, expanded to %s, as the Ruby home", options.HOME, home));
            if (!isRubyHome(home)) {
                RubyLanguage.LOGGER.warning(String.format("--home=%s does not look like TruffleRuby's home", options.HOME));
            }
            return home.getCanonicalPath();
        } else {
            RubyLanguage.LOGGER.config("--home not set, cannot determine home from it");
        }

        // Use the Truffle reported home

        final String truffleReported = language.getTruffleLanguageHome();

        if (truffleReported != null) {
            final File home = new File(truffleReported);
            RubyLanguage.LOGGER.config(() -> String.format("trying Truffle-reported home %s, expanded to %s, as the Ruby home", truffleReported, home));
            if (isRubyHome(home)) {
                return truffleReported;
            } else {
                RubyLanguage.LOGGER.warning(String.format("Truffle-reported home %s does not look like TruffleRuby's home", truffleReported));
            }
        } else {
            RubyLanguage.LOGGER.config("Truffle-reported home not set, cannot determine home from it");
        }

        // Use the path relative to the launcher

        if (!options.LAUNCHER.isEmpty()) {
            final File canonicalLauncherPath = new File(options.LAUNCHER).getCanonicalFile();
            final File launcherDir = canonicalLauncherPath.getParentFile();
            final File candidate = launcherDir == null ? null : launcherDir.getParentFile();
            RubyLanguage.LOGGER.config(() -> String.format("trying home %s guessed from executable %s, as the Ruby home", candidate, options.LAUNCHER));
            if (isRubyHome(candidate)) {
                return candidate.getCanonicalPath();
            } else {
                RubyLanguage.LOGGER.warning(String.format("home %s guessed from executable %s does not look like TruffleRuby's home", candidate, options.LAUNCHER));
            }
        } else {
            RubyLanguage.LOGGER.config("no launcher set, cannot determine home from it");
        }

        if (!LIBPOLYGLOT) {
            // We have no way to ever find home automatically in libpolyglot, so don't clutter with warnings
            RubyLanguage.LOGGER.warning("could not determine TruffleRuby's home - the standard library will not be available - set --home= or use --log.level=CONFIG to see details");
        }

        return null;
    }

    private boolean isRubyHome(File path) {
        final File lib = new File(path, "lib");
        return new File(lib, "truffle").isDirectory() && new File(lib, "ruby").isDirectory() && new File(lib, "patches").isDirectory();
    }

    public TruffleNFIPlatform getTruffleNFI() {
        return truffleNFIPlatform;
    }

    public NativeConfiguration getNativeConfiguration() {
        return nativeConfiguration;
    }

    public ValueWrapperManager getValueWrapperManager() {
        return valueWrapperManager;
    }

    public WeakValueCache<RopeKey, DynamicObject> getInternedStringCache() {
        return internedStringCache;
    }

    private static SecureRandom createRandomInstance() {
        try {
            /*
             * We want to use a non-blocking source because this is what MRI does (via /dev/urandom) and it's been found
             * in practice that blocking sources are a problem for deploying JRuby.
             */
            return SecureRandom.getInstance("NativePRNGNonBlocking");
        } catch (NoSuchAlgorithmException e) {
            throw new JavaException(e);
        }
    }

    @TruffleBoundary
    public byte[] getRandomSeedBytes(int numBytes) {
        final byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return bytes;
    }

    public WeakValueCache<RegexpCacheKey, Regex> getRegexpCache() {
        return regexpCache;
    }

    /**
     * Returns the path of a Source. Returns the short path for the main script (the file argument
     * given to "ruby"). The path of eval(code, nil, filename) is just filename.
     */
    public String getPath(Source source) {
        final String name = source.getName();

        if (preInitialized && name.startsWith(RubyLanguage.RUBY_HOME_SCHEME)) {
            return rubyHome + "/" + name.substring(RubyLanguage.RUBY_HOME_SCHEME.length());
        } else {
            return name;
        }
    }

    /**
     * Returns the path of a Source. Returns the canonical path for the main script. Note however
     * that the path of eval(code, nil, filename) is just filename and might not be absolute.
     */
    public String getAbsolutePath(Source source) {
        if (source == mainSource) {
            return mainSourceAbsolutePath;
        } else {
            return getPath(source);
        }
    }

    @TruffleBoundary
    public String fileLine(SourceSection section) {
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

    public void setMainSources(Source mainSource, String mainSourceAbsolutePath) {
        this.mainSource = mainSource;
        this.mainSourceAbsolutePath = mainSourceAbsolutePath;
    }

}
