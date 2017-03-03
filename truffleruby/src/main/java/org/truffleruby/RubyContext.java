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
import com.oracle.truffle.api.CompilerOptions;
import com.oracle.truffle.api.ExecutionContext;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.builtins.PrimitiveManager;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.CoreMethods;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.exception.CoreExceptions;
import org.truffleruby.core.kernel.AtExitManager;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.rope.RopeTable;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.interop.InteropManager;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.FeatureLoader;
import org.truffleruby.language.loader.SourceLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.options.Options;
import org.truffleruby.options.OptionsBuilder;
import org.truffleruby.options.Verbosity;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.platform.NativePlatformFactory;
import org.truffleruby.stdlib.CoverageManager;
import org.truffleruby.stdlib.readline.ConsoleHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;

public class RubyContext extends ExecutionContext {

    private final TruffleLanguage.Env env;

    private final Options options;

    private final String rubyHome;
    private String originalInputFile;

    private InputStream syntaxCheckInputStream;
    private boolean verbose;

    private final RopeTable ropeTable = new RopeTable();
    private final PrimitiveManager primitiveManager = new PrimitiveManager();
    private final SafepointManager safepointManager = new SafepointManager(this);
    private final SymbolTable symbolTable;
    private final InteropManager interopManager = new InteropManager(this);
    private final CodeLoader codeLoader = new CodeLoader(this);
    private final FeatureLoader featureLoader = new FeatureLoader(this);
    private final TraceManager traceManager;
    private final ObjectSpaceManager objectSpaceManager = new ObjectSpaceManager(this);
    private final SharedObjects sharedObjects = new SharedObjects(this);
    private final AtExitManager atExitManager = new AtExitManager(this);
    private final SourceLoader sourceLoader = new SourceLoader(this);
    private final CallStackManager callStack = new CallStackManager(this);
    private final CoreStrings coreStrings = new CoreStrings(this);
    private final FrozenStrings frozenStrings = new FrozenStrings(this);
    private final CoreExceptions coreExceptions = new CoreExceptions(this);
    private final EncodingManager encodingManager = new EncodingManager(this);

    private final CompilerOptions compilerOptions = Truffle.getRuntime().createCompilerOptions();

    private final NativePlatform nativePlatform;
    private final CoreLibrary coreLibrary;
    private final CoreMethods coreMethods;
    private final ThreadManager threadManager;
    private final LexicalScope rootLexicalScope;
    private final CoverageManager coverageManager;
    private ConsoleHolder consoleHolder;

    private final Object classVariableDefinitionLock = new Object();

    private String currentDirectory;

    public static ThreadLocal<RubyContext> contextsBeingCreated = new ThreadLocal<>();

    public RubyContext(TruffleLanguage.Env env) {
        contextsBeingCreated.set(this);

        try {
            this.env = env;

            final OptionsBuilder optionsBuilder = new OptionsBuilder();
            optionsBuilder.set(System.getProperties());
            optionsBuilder.set(env.getConfig());
            options = optionsBuilder.build();

            rubyHome = findRubyHome();
            Log.LOGGER.config(() -> String.format("ruby home: %s", rubyHome));

            currentDirectory = System.getProperty("user.dir");
            verbose = options.VERBOSITY.equals(Verbosity.TRUE);

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

            coreLibrary = new CoreLibrary(this);
            coreLibrary.initialize();

            symbolTable = new SymbolTable(coreLibrary.getSymbolFactory());

            // Create objects that need core classes

            nativePlatform = NativePlatformFactory.createPlatform(this);
            rootLexicalScope = new LexicalScope(null, coreLibrary.getObjectClass());

            // The encoding manager relies on POSIX having been initialized, so we can't process it during
            // normal core library initialization.
            coreLibrary.initializeEncodingManager();

            threadManager = new ThreadManager(this);
            threadManager.initialize();

            // Load the nodes

            Main.printTruffleTimeMetric("before-load-nodes");
            coreLibrary.loadCoreNodes(primitiveManager);
            Main.printTruffleTimeMetric("after-load-nodes");

            // Capture known builtin methods

            final Instrumenter instrumenter = env.lookup(Instrumenter.class);
            traceManager = new TraceManager(this, instrumenter);
            coreMethods = new CoreMethods(this);
            coverageManager = new CoverageManager(this, instrumenter);

            // Load the reset of the core library

            Main.printTruffleTimeMetric("before-load-core");
            coreLibrary.loadRubyCore();
            Main.printTruffleTimeMetric("after-load-core");

            // Load other subsystems

            coreLibrary.initializePostBoot();

            // Share once everything is loaded
            if (options.SHARED_OBJECTS_ENABLED && options.SHARED_OBJECTS_FORCE) {
                sharedObjects.startSharing();
            }
        } finally {
            contextsBeingCreated.remove();
        }
    }

    private String findRubyHome() {
        // Use the option if it was set

        if (options.HOME != null) {
            return new File(options.HOME).getAbsolutePath();
        }

        // Try to find it automatically from the location of the JAR, but this won't work from the JRuby launcher as it uses the boot classpath

        if (!TruffleOptions.AOT) {
            final CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();

            if (codeSource != null && codeSource.getLocation().getProtocol().equals("file")) {
                final File jar = new File(codeSource.getLocation().getFile());
                final File jarDir = jar.getParentFile();

                if (jarDir.getName().equals("ruby") && new File(jarDir, "lib").exists()) {
                    // GraalVM build or distribution
                    return jarDir.getAbsolutePath();
                }
            }
        }

        Log.LOGGER.config("home not explicitly set, and couldn't determine it from the source of the Java classfiles or the TruffleRuby launcher");

        return null;
    }

    public Object send(Object object, String methodName, DynamicObject block, Object... arguments) {
        CompilerAsserts.neverPartOfCompilation();

        assert block == null || RubyGuards.isRubyProc(block);

        final MethodLookupResult method = ModuleOperations.lookupMethod(coreLibrary.getMetaClass(object), methodName);

        if (!method.isDefined()) {
            return null;
        }

        return method.getMethod().getCallTarget().call(
                RubyArguments.pack(null, null, method.getMethod(), DeclarationContext.METHOD, null, object, block, arguments));
    }

    public void shutdown() {
        if (options.ROPE_PRINT_INTERN_STATS) {
            Log.LOGGER.info("ropes re-used: " + getRopeTable().getRopesReusedCount());
            Log.LOGGER.info("rope byte arrays re-used: " + getRopeTable().getByteArrayReusedCount());
            Log.LOGGER.info("rope bytes saved: " + getRopeTable().getRopeBytesSaved());
            Log.LOGGER.info("total ropes interned: " + getRopeTable().totalRopes());
        }

        atExitManager.runSystemExitHooks();

        threadManager.shutdown();

        if (options.COVERAGE_GLOBAL) {
            coverageManager.print(System.out);
        }
    }

    public Options getOptions() {
        return options;
    }

    public TruffleLanguage.Env getEnv() {
        return env;
    }

    public NativePlatform getNativePlatform() {
        return nativePlatform;
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

    @Override
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    public PrimitiveManager getPrimitiveManager() {
        return primitiveManager;
    }

    public CoverageManager getCoverageManager() {
        return coverageManager;
    }

    public static RubyContext getInstance() {
        final RubyContext ret = contextsBeingCreated.get();

        if (ret != null) {
            return ret;
        }

        try {
            return RubyLanguage.INSTANCE.unprotectedFindContext(RubyLanguage.INSTANCE.unprotectedCreateFindContextNode());
        } catch (IllegalStateException e) {
            return null;
        }
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

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setOriginalInputFile(String originalInputFile) {
        this.originalInputFile = originalInputFile;
    }

    public String getOriginalInputFile() {
        return originalInputFile;
    }

    public String getRubyHome() {
        return rubyHome;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setVerboseNil() {
        verbose = false;
    }

    public boolean warningsEnabled() {
        return verbose;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public InputStream getSyntaxCheckInputStream() {
        return syntaxCheckInputStream;
    }

    public void setSyntaxCheckInputStream(InputStream syntaxCheckInputStream) {
        this.syntaxCheckInputStream = syntaxCheckInputStream;
    }

    public ConsoleHolder getConsoleHolder() {
        if (consoleHolder == null) {
            synchronized (this) {
                if (consoleHolder == null) {
                    consoleHolder = new ConsoleHolder();
                }
            }
        }

        return consoleHolder;
    }

}
