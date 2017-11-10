/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.CompilerOptions;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import com.oracle.truffle.api.instrumentation.Instrumenter;

import org.joni.Regex;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.collections.WeakValuedMap;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.FinalizationService;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.inlined.CoreMethods;
import org.truffleruby.core.kernel.AtExitManager;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.rope.RopeKey;
import org.truffleruby.core.rope.RopeTable;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.interop.InteropManager;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FeatureLoader;
import org.truffleruby.language.loader.SourceLoader;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.launcher.Launcher;
import org.truffleruby.launcher.options.Options;
import org.truffleruby.launcher.options.OptionsBuilder;
import org.truffleruby.platform.Platform;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.darwin.DarwinNativeConfiguration;
import org.truffleruby.platform.linux.LinuxNativeConfiguration;
import org.truffleruby.platform.solaris.SolarisSparcV9NativeConfiguration;
import org.truffleruby.stdlib.CoverageManager;
import org.truffleruby.stdlib.readline.ConsoleHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class RubyContext {

    public static RubyContext FIRST_INSTANCE = null;

    private final RubyLanguage language;
    private final TruffleLanguage.Env env;

    private final AllocationReporter allocationReporter;

    private final Options options;
    private final String rubyHome;

    private final PrimitiveManager primitiveManager = new PrimitiveManager();
    private final SafepointManager safepointManager = new SafepointManager(this);
    private final SymbolTable symbolTable;
    private final InteropManager interopManager = new InteropManager(this);
    private final CodeLoader codeLoader = new CodeLoader(this);
    private final FeatureLoader featureLoader = new FeatureLoader(this);
    private final TraceManager traceManager;
    private final FinalizationService finalizationService = new FinalizationService(this);
    private final ObjectSpaceManager objectSpaceManager = new ObjectSpaceManager(this, finalizationService);
    private final SharedObjects sharedObjects = new SharedObjects(this);
    private final AtExitManager atExitManager = new AtExitManager(this);
    private final SourceLoader sourceLoader = new SourceLoader(this);
    private final CallStackManager callStack = new CallStackManager(this);
    private final CoreStrings coreStrings = new CoreStrings(this);
    private final FrozenStrings frozenStrings = new FrozenStrings(this);
    private final CoreExceptions coreExceptions = new CoreExceptions(this);
    private final EncodingManager encodingManager = new EncodingManager(this);
    private final WeakValuedMap<RopeKey, Regex> regexpCache = new WeakValuedMap<>();
    private final NativeConfiguration nativeConfiguration;

    private final CompilerOptions compilerOptions = Truffle.getRuntime().createCompilerOptions();

    private final SecureRandom random;
    private final Hashing hashing;
    private final RopeTable ropeTable;
    private final TruffleNFIPlatform truffleNFIPlatform;
    private final CoreLibrary coreLibrary;
    private CoreMethods coreMethods;
    private final ThreadManager threadManager;
    private final LexicalScope rootLexicalScope;
    private final CoverageManager coverageManager;
    @CompilationFinal private volatile ConsoleHolder consoleHolder;

    private final Object classVariableDefinitionLock = new Object();

    private boolean initialized;
    private volatile boolean finalizing;

    public RubyContext(RubyLanguage language, TruffleLanguage.Env env) {
        Launcher.printTruffleTimeMetric("before-context-constructor");

        if (FIRST_INSTANCE == null) {
            FIRST_INSTANCE = this;
        }

        this.language = language;
        this.env = env;

        allocationReporter = env.lookup(AllocationReporter.class);

        Launcher.printTruffleTimeMetric("before-options");
        final OptionsBuilder optionsBuilder = new OptionsBuilder();
        optionsBuilder.set(env.getConfig());            // Legacy config - used by unit tests for example
        optionsBuilder.set(env.getOptions());           // SDK options
        options = optionsBuilder.build();
        Launcher.printTruffleTimeMetric("after-options");

        if (!org.truffleruby.Main.isGraal() && options.GRAAL_WARNING_UNLESS) {
            Log.performanceOnce(
                    "this JVM does not have the Graal compiler - performance will be limited - see doc/user/using-graalvm.md");
        }

        // We need to construct this at runtime
        random = new SecureRandom();

        final long hashingSeed;

        if (options.HASHING_DETERMINISTIC) {
            Log.LOGGER.severe("deterministic hashing is enabled - this may make you vulnerable to denial of service attacks");
            hashingSeed = 7114160726623585955L;
        } else {
            hashingSeed = random.nextLong();
        }

        hashing = new Hashing(hashingSeed);
        ropeTable = new RopeTable(hashing);

        try {
            rubyHome = findRubyHome();
        } catch (IOException e) {
            throw new JavaException(e);
        }
        if (Log.LOGGER.isLoggable(Level.CONFIG)) {
            Log.LOGGER.config("ruby home: " + rubyHome);
        }

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

        Launcher.printTruffleTimeMetric("before-create-core-library");
        coreLibrary = new CoreLibrary(this);
        nativeConfiguration = loadNativeConfiguration();
        coreLibrary.initialize();
        Launcher.printTruffleTimeMetric("after-create-core-library");

        symbolTable = new SymbolTable(coreLibrary.getSymbolFactory(), hashing);
        rootLexicalScope = new LexicalScope(null, coreLibrary.getObjectClass());

        // Create objects that need core classes

        Launcher.printTruffleTimeMetric("before-create-native-platform");
        truffleNFIPlatform = options.NATIVE_PLATFORM ? new TruffleNFIPlatform(this) : null;
        featureLoader.initialize(nativeConfiguration, truffleNFIPlatform);
        Launcher.printTruffleTimeMetric("after-create-native-platform");

        // The encoding manager relies on POSIX having been initialized, so we can't process it during
        // normal core library initialization.
        Launcher.printTruffleTimeMetric("before-initialize-encodings");
        coreLibrary.initializeEncodingManager();
        Launcher.printTruffleTimeMetric("after-initialize-encodings");

        Launcher.printTruffleTimeMetric("before-thread-manager");
        threadManager = new ThreadManager(this);
        threadManager.initialize();
        Launcher.printTruffleTimeMetric("after-thread-manager");

        Launcher.printTruffleTimeMetric("before-instruments");
        final Instrumenter instrumenter = env.lookup(Instrumenter.class);
        traceManager = new TraceManager(this, instrumenter);
        coverageManager = new CoverageManager(this, instrumenter);
        Launcher.printTruffleTimeMetric("after-instruments");

        Launcher.printTruffleTimeMetric("after-context-constructor");
    }

    public void initialize() {
        // Load the nodes

        Launcher.printTruffleTimeMetric("before-load-nodes");
        coreLibrary.loadCoreNodes(primitiveManager);
        Launcher.printTruffleTimeMetric("after-load-nodes");

        // Capture known builtin methods

        coreMethods = new CoreMethods(this);

        // Load the part of the core library defined in Ruby

        Launcher.printTruffleTimeMetric("before-load-core");
        coreLibrary.loadRubyCore();
        Launcher.printTruffleTimeMetric("after-load-core");

        // Load other subsystems

        Launcher.printTruffleTimeMetric("before-post-boot");
        coreLibrary.initializePostBoot();
        Launcher.printTruffleTimeMetric("after-post-boot");

        // Share once everything is loaded
        if (options.SHARED_OBJECTS_ENABLED && options.SHARED_OBJECTS_FORCE) {
            sharedObjects.startSharing();
        }

        initialized = true;
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
                Log.LOGGER.severe("no native configuration for this platform");
                break;
        }

        return nativeConfiguration;
    }

    public Object send(Object object, String methodName, Object... arguments) {
        CompilerAsserts.neverPartOfCompilation();

        final InternalMethod method = ModuleOperations.lookupMethodUncached(coreLibrary.getMetaClass(object), methodName, null);
        if (method == null || method.isUndefined()) {
            return null;
        }

        return method.getCallTarget().call(
                RubyArguments.pack(null, null, method, null, object, null, arguments));
    }

    public void finalizeContext() {
        finalizing = true;
        atExitManager.runSystemExitHooks();
    }

    private final ReentrantLock shutdownLock = new ReentrantLock();
    private boolean disposed = false;

    public void shutdown() {
        shutdownLock.lock();
        try {
            if (!disposed) {
                doShutdown();
                disposed = true;
            }
        } finally {
            shutdownLock.unlock();
        }
    }

    private void doShutdown() {
        if (options.ROPE_PRINT_INTERN_STATS) {
            Log.LOGGER.info("ropes re-used: " + getRopeTable().getRopesReusedCount());
            Log.LOGGER.info("rope byte arrays re-used: " + getRopeTable().getByteArrayReusedCount());
            Log.LOGGER.info("rope bytes saved: " + getRopeTable().getRopeBytesSaved());
            Log.LOGGER.info("total ropes interned: " + getRopeTable().totalRopes());
        }

        if (options.COVERAGE_GLOBAL) {
            coverageManager.print(System.out);
        }

        threadManager.shutdown();
        safepointManager.shutdown();
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

    public Hashing getHashing() {
        return hashing;
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

    public SourceLoader getSourceLoader() {
        return sourceLoader;
    }

    public RopeTable getRopeTable() {
        return ropeTable;
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

    public FrozenStrings getFrozenStrings() {
        return frozenStrings;
    }

    public Object getClassVariableDefinitionLock() {
        return classVariableDefinitionLock;
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

    public String getRubyHome() {
        return rubyHome;
    }

    public ConsoleHolder getConsoleHolder() {
        if (consoleHolder == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            synchronized (this) {
                if (consoleHolder == null) {
                    consoleHolder = new ConsoleHolder();
                }
            }
        }

        return consoleHolder;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isFinalizing() {
        return finalizing;
    }

    // Returns a canonical path to the home
    @TruffleBoundary
    private String findRubyHome() throws IOException {
        // Use the option if it was set

        if (!options.HOME.isEmpty()) {
            final File home = new File(options.HOME);
            if (!isRubyHome(home)) {
                Log.LOGGER.warning(home + " does not look like truffleruby's home");
            }
            return home.getCanonicalPath();
        }

        StringBuilder warning = new StringBuilder("TruffleRuby's home was not explicitly set.\n");

        if (!options.LAUNCHER.isEmpty()) {
            final Path canonicalLauncherPath = Paths.get(new File(options.LAUNCHER).getCanonicalPath());
            final File candidate = canonicalLauncherPath.getParent().getParent().toFile();
            if (isRubyHome(candidate)) {
                return candidate.getCanonicalPath();
            } else {
                warning.append("* Default path '").
                        append(candidate).
                        append("' derived from executable '").
                        append(options.LAUNCHER).
                        append("' does not appear to be TruffleRuby's home.\n");
            }
        } else {
            warning.append("* Launcher not set, home path could not be derived.\n");
        }

        final String graalVMHome = System.getProperty("graalvm.home");
        if (graalVMHome != null) {
            final File candidate = Paths.get(graalVMHome).resolve("jre/languages/ruby").toFile();
            if (isRubyHome(candidate)) {
                return candidate.getCanonicalPath();
            } else {
                warning.append("* Path '").
                        append(candidate).
                        append("' derived from GraalVM home '").
                        append(graalVMHome).
                        append("' does not appear to be TruffleRuby's home.\n");

            }
        } else {
            warning.append("* GraalVM home not found.\n");
        }

        warning.append("* Try to set home using -Xhome=PATH option.");
        Log.LOGGER.warning(warning.toString());

        return null;
    }

    private boolean isRubyHome(File path) {
        return Paths.get(path.toString(), "lib", "truffle").toFile().isDirectory() &&
                Paths.get(path.toString(), "lib", "ruby").toFile().isDirectory();
    }

    public TruffleNFIPlatform getTruffleNFI() {
        return truffleNFIPlatform;
    }

    public NativeConfiguration getNativeConfiguration() {
        return nativeConfiguration;
    }

    public byte[] getRandomSeedBytes(int numBytes) {
        // We'd like to use /dev/urandom each time here by using NativePRNGNonBlocking, but this is not supported on the SVM
        final byte[] bytes = new byte[numBytes];
        random.nextBytes(bytes);
        return bytes;
    }

    public WeakValuedMap<RopeKey, Regex> getRegexpCache() {
        return regexpCache;
    }

}
