/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.WrapNodeGen;
import org.truffleruby.cext.WrapNode;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.support.IONodes.GetThreadBufferNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.extra.TruffleRubyNodes;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.TruffleNFIPlatform.NativeFunction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class FeatureLoader {

    private final RubyContext context;

    private final ReentrantLockFreeingMap<String> fileLocks = new ReentrantLockFreeingMap<>();
    /**
     * Maps basename without extension -> autoload path -> autoload constant,
     * to detect when require-ing a file already registered with autoload.
     */
    private final Map<String, Map<String, RubyConstant>> registeredAutoloads = new HashMap<>();
    private final ReentrantLock registeredAutoloadsLock = new ReentrantLock();

    private final Object cextImplementationLock = new Object();
    private boolean cextImplementationLoaded = false;
    private TruffleObject sulongLoadLibraryFunction;
    private Map<String, String> nativeLibraryMap;

    private NativeFunction getcwd;
    private static final int PATH_MAX = 1024; // jnr-posix hard codes this value

    public FeatureLoader(RubyContext context) {
        this.context = context;
    }

    public void initialize(NativeConfiguration nativeConfiguration, TruffleNFIPlatform nfi) {
        if (context.getOptions().NATIVE_PLATFORM) {
            this.getcwd = nfi.getFunction("getcwd", "(pointer," + nfi.size_t() + "):pointer");
        }
    }

    public void addAutoload(RubyConstant autoloadConstant) {
        final String autoloadPath = autoloadConstant.getAutoloadConstant().getAutoloadPath();
        final String basename = basenameWithoutExtension(autoloadPath);

        registeredAutoloadsLock.lock();
        try {
            final Map<String, RubyConstant> constants = ConcurrentOperations.getOrCompute(registeredAutoloads, basename, k -> new LinkedHashMap<>());
            constants.put(autoloadPath, autoloadConstant);
        } finally {
            registeredAutoloadsLock.unlock();
        }
    }

    public RubyConstant isAutoloadPath(String expandedPath) {
        final String basename = basenameWithoutExtension(expandedPath);
        final RubyConstant[] constants;

        registeredAutoloadsLock.lock();
        try {
            final Map<String, RubyConstant> constantsMap = registeredAutoloads.get(basename);
            if (constantsMap == null) {
                return null;
            }
            constants = constantsMap.values().toArray(new RubyConstant[0]);
        } finally {
            registeredAutoloadsLock.unlock();
        }

        for (RubyConstant constant : constants) {
            final String expandedAutoloadPath = findFeature(constant.getAutoloadConstant().getAutoloadPath());
            if (expandedPath.equals(expandedAutoloadPath)) {
                return constant;
            }
        }
        return null;
    }

    public void removeAutoload(RubyConstant constant) {
        final String autoloadPath = constant.getAutoloadConstant().getAutoloadPath();
        final String basename = basenameWithoutExtension(autoloadPath);

        registeredAutoloadsLock.lock();
        try {
            final Map<String, RubyConstant> constantsMap = registeredAutoloads.get(basename);
            constantsMap.remove(autoloadPath, constant);
        } finally {
            registeredAutoloadsLock.unlock();
        }
    }

    private String basenameWithoutExtension(String path) {
        final String basename = new File(path).getName();
        int i = basename.lastIndexOf('.');
        if (i >= 0) {
            return basename.substring(0, i);
        } else {
            return basename;
        }
    }

    public String getWorkingDirectory() {
        final TruffleNFIPlatform nfi = context.getTruffleNFI();
        if (nfi == null) {
            // The current working cannot change if there are no native calls
            return System.getProperty("user.dir");
        }
        final int bufferSize = PATH_MAX;
        final Pointer buffer = GetThreadBufferNode.getBuffer(context, bufferSize);
        final long address = nfi.asPointer((TruffleObject) getcwd.call(buffer.getAddress(), bufferSize));
        if (address == 0) {
            throw new UnsupportedOperationException("getcwd() failed");
        }
        final byte[] bytes = buffer.readZeroTerminatedByteArray(context, 0);
        final Encoding localeEncoding = context.getEncodingManager().getLocaleEncoding();
        return new String(bytes, EncodingManager.charsetForEncoding(localeEncoding));
    }

    /** Make a path absolute, by expanding relative to the context CWD. */
    private String makeAbsolute(String cwd, String path) {
        final File file = new File(path);
        if (file.isAbsolute()) {
            return path;
        } else {
            return new File(cwd, path).getPath();
        }
    }

    public String canonicalize(String cwd, String path) {
        // First, make the path absolute, by expanding relative to the context CWD
        // Otherwise, getCanonicalPath() uses user.dir as CWD which is incorrect.
        final String absolutePath = makeAbsolute(cwd, path);
        try {
            return new File(absolutePath).getCanonicalPath();
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    public String dirname(String absolutePath) {
        assert new File(absolutePath).isAbsolute();

        final String parent = new File(absolutePath).getParent();
        if (parent == null) {
            return absolutePath;
        } else {
            return parent;
        }
    }

    public ReentrantLockFreeingMap<String> getFileLocks() {
        return fileLocks;
    }

    @TruffleBoundary
    public String findFeature(String feature) {
        if (context.getOptions().LOG_FEATURE_LOCATION) {
            final String originalFeature = feature;

            RubyLanguage.LOGGER.info(() -> {
                final SourceSection sourceSection = context.getCallStack().getTopMostUserSourceSection();
                return String.format("starting search from %s for feature %s...", context.fileLine(sourceSection), originalFeature);
            });
        }

        final String cwd = getWorkingDirectory();

        if (context.getOptions().LOG_FEATURE_LOCATION) {
            RubyLanguage.LOGGER.info(String.format("current directory: %s", cwd));
        }

        if (feature.startsWith("./")) {
            feature = cwd + "/" + feature.substring(2);

            if (context.getOptions().LOG_FEATURE_LOCATION) {
                RubyLanguage.LOGGER.info(String.format("feature adjusted to %s", feature));
            }
        } else if (feature.startsWith("../")) {
            feature = dirname(cwd) + "/" + feature.substring(3);

            if (context.getOptions().LOG_FEATURE_LOCATION) {
                RubyLanguage.LOGGER.info(String.format("feature adjusted to %s", feature));
            }
        }

        String found = null;

        if (feature.startsWith(RubyLanguage.RESOURCE_SCHEME) || new File(feature).isAbsolute()) {
            found = findFeatureWithAndWithoutExtension(feature);
        } else {
            for (Object pathObject : ArrayOperations.toIterable(context.getCoreLibrary().getLoadPath())) {
                // $LOAD_PATH entries are canonicalized since Ruby 2.4.4
                final String loadPath = canonicalize(cwd, pathObject.toString());

                if (context.getOptions().LOG_FEATURE_LOCATION) {
                    RubyLanguage.LOGGER.info(String.format("from load path %s...", loadPath));
                }

                final String fileWithinPath = new File(loadPath, feature).getPath();
                final String result = findFeatureWithAndWithoutExtension(fileWithinPath);

                if (result != null) {
                    found = result;
                    break;
                }
            }
        }

        if (context.getOptions().LOG_FEATURE_LOCATION) {
            if (found == null) {
                RubyLanguage.LOGGER.info("not found");
            } else {
                RubyLanguage.LOGGER.info(String.format("found in %s", found));
            }
        }

        return found;
    }

    private String findFeatureWithAndWithoutExtension(String path) {
        assert new File(path).isAbsolute();

        if (path.endsWith(".so")) {
            final String base = path.substring(0, path.length() - 3);

            final String asSO = findFeatureWithExactPath(base + RubyLanguage.CEXT_EXTENSION);

            if (asSO != null) {
                return asSO;
            }
        }

        final String withExtension = findFeatureWithExactPath(path + TruffleRuby.EXTENSION);

        if (withExtension != null) {
            return withExtension;
        }

        final String asSU = findFeatureWithExactPath(path + RubyLanguage.CEXT_EXTENSION);

        if (asSU != null) {
            return asSU;
        }

        final String withoutExtension = findFeatureWithExactPath(path);

        if (withoutExtension != null) {
            return withoutExtension;
        }

        return null;
    }

    private String findFeatureWithExactPath(String path) {
        if (context.getOptions().LOG_FEATURE_LOCATION) {
            RubyLanguage.LOGGER.info(String.format("trying %s...", path));
        }

        if (path.startsWith(RubyLanguage.RESOURCE_SCHEME)) {
            return path;
        }

        final File file = new File(path);
        if (!file.isFile()) {
            return null;
        }

        // Normalize path like File.expand_path() (e.g., remove "../"), but do not resolve symlinks
        return file.toPath().normalize().toString();
    }

    @TruffleBoundary
    public void ensureCExtImplementationLoaded(String feature, RequireNode requireNode) {
        synchronized (cextImplementationLock) {
            if (cextImplementationLoaded) {
                return;
            }

            if (!context.getOptions().CEXTS) {
                throw new RaiseException(context, context.getCoreExceptions().loadError("cannot load as C extensions are disabled with -Xcexts=false", feature, null));
            }

            if (!TruffleRubyNodes.SulongNode.isSulongAvailable(context)) {
                throw new RaiseException(context, context.getCoreExceptions().loadError("Sulong is required to support C extensions, and it doesn't appear to be available", feature, null));
            }

            Metrics.printTime("before-load-cext-support");
            try {
                requireNode.executeRequire("truffle/cext");
                final DynamicObject truffleModule = context.getCoreLibrary().getTruffleModule();
                final Object truffleCExt = Layouts.MODULE.getFields(truffleModule).getConstant("CExt").getValue();

                final String rubySUpath = context.getRubyHome() + "/lib/cext/ruby.su";
                final List<TruffleObject> libraries = loadCExtLibRuby(rubySUpath, feature);

                sulongLoadLibraryFunction = requireNode.findFunctionInLibraries(libraries, "rb_tr_load_library", rubySUpath);

                final TruffleObject initFunction = requireNode.findFunctionInLibraries(libraries, "rb_tr_init", rubySUpath);
                final Node executeInitNode = Message.EXECUTE.createNode();
                try {
                    ForeignAccess.sendExecute(executeInitNode, initFunction, truffleCExt);
                } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                    throw new JavaException(e);
                }
            } finally {
                Metrics.printTime("after-load-cext-support");
            }

            cextImplementationLoaded = true;
        }
    }

    private List<TruffleObject> loadCExtLibRuby(String rubySUpath, String feature) {
        if (context.getOptions().CEXTS_LOG_LOAD) {
            RubyLanguage.LOGGER.info(() -> String.format("loading cext implementation %s", rubySUpath));
        }

        if (!new File(rubySUpath).exists()) {
            throw new RaiseException(context, context.getCoreExceptions().loadError("this TruffleRuby distribution does not have the C extension implementation file ruby.su", feature, null));
        }

        return loadCExtLibrary("ruby.su", rubySUpath);
    }

    @TruffleBoundary
    public List<TruffleObject> loadCExtLibrary(String feature, String path) {
        if (!new File(path).exists()) {
            throw new RaiseException(context, context.getCoreExceptions().loadError(path + " does not exists", path, null));
        }

        final List<TruffleObject> libraries = new ArrayList<>();

        Metrics.printTime("before-load-cext-" + feature);
        try {
            final CExtLoader cextLoader = new CExtLoader(this::loadNativeLibrary, source -> {
                final Object result;

                try {
                    result = context.getEnv().parse(source).call();
                } catch (Exception e) {
                    throw new JavaException(e);
                }

                if (!(result instanceof TruffleObject)) {
                    throw new RaiseException(context, context.getCoreExceptions().loadError(String.format("%s returned a %s rather than a TruffleObject", path, result.getClass().getSimpleName()), path, null));
                }

                libraries.add((TruffleObject) result);
            });
            cextLoader.loadLibrary(path);
        } catch (IOException e) {
            throw new JavaException(e);
        } finally {
            Metrics.printTime("after-load-cext-" + feature);
        }

        return libraries;
    }

    private final Node executeSulongLoadLibraryNode = Message.EXECUTE.createNode();
    private final WrapNode wrapNode = WrapNodeGen.create();

    private void loadNativeLibrary(String library) {
        assert sulongLoadLibraryFunction != null;

        final String remapped = remapNativeLibrary(library);

        if (context.getOptions().CEXTS_LOG_LOAD) {
            if (remapped.equals(library)) {
                RubyLanguage.LOGGER.info(() -> String.format("loading native library %s", library));
            } else {
                RubyLanguage.LOGGER.info(() -> String.format("loading native library %s, remapped from %s", remapped, library));
            }
        }

        TruffleObject libraryRubyString = wrapNode.execute(StringOperations.createString(context, StringOperations.encodeRope(remapNativeLibrary(library), UTF8Encoding.INSTANCE)));
        try {
            ForeignAccess.sendExecute(executeSulongLoadLibraryNode, sulongLoadLibraryFunction, libraryRubyString);
        } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
            throw new JavaException(e);
        }
    }

    private String remapNativeLibrary(String library) {
        return getNativeLibraryMap().getOrDefault(library, library);
    }

    private synchronized Map<String, String> getNativeLibraryMap() {
        if (nativeLibraryMap == null) {
            nativeLibraryMap = new HashMap<>();

            for (String mapPair : context.getOptions().CEXTS_LIBRARY_REMAP) {
                final int divider = mapPair.indexOf(':');

                if (divider == -1) {
                    throw new RuntimeException(OptionsCatalog.CEXTS_LIBRARY_REMAP.getName() + " entry does not contain a : divider");
                }

                final String library = mapPair.substring(0, divider);
                final String remap = mapPair.substring(divider + 1);

                nativeLibraryMap.put(library, remap);
            }

            nativeLibraryMap = Collections.unmodifiableMap(nativeLibraryMap);
        }

        return nativeLibraryMap;
    }

    // TODO (pitr-ch 16-Mar-2016): this protects the $LOADED_FEATURES only in this class,
    // it can still be accessed and modified (rare) by Ruby code which may cause issues
    private final Object loadedFeaturesLock = new Object();

    public Object getLoadedFeaturesLock() {
        return loadedFeaturesLock;
    }

}
