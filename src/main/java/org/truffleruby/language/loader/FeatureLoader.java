/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.loader;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Log;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.Linker;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.encoding.EncodingManager;
import org.truffleruby.core.support.IONodes.GetThreadBufferNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.launcher.options.OptionsCatalog;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.TruffleNFIPlatform.NativeFunction;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FeatureLoader {

    private final RubyContext context;

    private final ReentrantLockFreeingMap<String> fileLocks = new ReentrantLockFreeingMap<>();

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
            this.getcwd = nfi.getFunction("getcwd", 2, "(pointer," + nfi.size_t() + "):pointer");
        }
    }

    private String getWorkingDirectory() {
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

    public ReentrantLockFreeingMap<String> getFileLocks() {
        return fileLocks;
    }

    @TruffleBoundary
    public String findFeature(String feature) {
        if (context.getOptions().LOG_FEATURE_LOCATION) {
            final String originalFeature = feature;

            Log.LOGGER.info(() -> {
                final Node callerNode = context.getCallStack().getTopMostUserCallNode();

                final SourceSection sourceSection;
                if (callerNode == null) {
                    sourceSection = null;
                } else {
                    sourceSection = callerNode.getEncapsulatingSourceSection();
                }

                return String.format("starting search from %s for feature %s...", context.getSourceLoader().fileLine(sourceSection), originalFeature);
            });
        }

        final String cwd = getWorkingDirectory();

        if (context.getOptions().LOG_FEATURE_LOCATION) {
            Log.LOGGER.info(String.format("current directory: %s", cwd));
        }

        if (feature.startsWith("./")) {
            feature = cwd + "/" + feature.substring(2);

            if (context.getOptions().LOG_FEATURE_LOCATION) {
                Log.LOGGER.info(String.format("feature adjusted to %s", feature));
            }
        } else if (feature.startsWith("../")) {
            feature = cwd.substring(
                    0,
                    cwd.lastIndexOf('/')) + "/" + feature.substring(3);

            if (context.getOptions().LOG_FEATURE_LOCATION) {
                Log.LOGGER.info(String.format("feature adjusted to %s", feature));
            }
        }

        String found = null;

        if (feature.startsWith(SourceLoader.RESOURCE_SCHEME)
                || new File(feature).isAbsolute()) {
            found = findFeatureWithAndWithoutExtension(cwd, feature);
        } else {
            for (Object pathObject : ArrayOperations.toIterable(context.getCoreLibrary().getLoadPath())) {
                if (context.getOptions().LOG_FEATURE_LOCATION) {
                    Log.LOGGER.info(String.format("from load path %s...", pathObject.toString()));
                }

                final String fileWithinPath = new File(pathObject.toString(), feature).getPath();
                final String result = findFeatureWithAndWithoutExtension(cwd, fileWithinPath);

                if (result != null) {
                    found = result;
                    break;
                }
            }
        }

        if (context.getOptions().LOG_FEATURE_LOCATION) {
            if (found == null) {
                Log.LOGGER.info("not found");
            } else {
                Log.LOGGER.info(String.format("found in %s", found));
            }
        }

        return found;
    }

    private String findFeatureWithAndWithoutExtension(String cwd, String path) {
        if (path.endsWith(".so")) {
            final String base = path.substring(0, path.length() - 3);

            final String asSO = findFeatureWithExactPath(cwd, base + RubyLanguage.CEXT_EXTENSION);

            if (asSO != null) {
                return asSO;
            }
        }

        final String withExtension = findFeatureWithExactPath(cwd, path + RubyLanguage.EXTENSION);

        if (withExtension != null) {
            return withExtension;
        }

        final String asSU = findFeatureWithExactPath(cwd, path + RubyLanguage.CEXT_EXTENSION);

        if (asSU != null) {
            return asSU;
        }

        final String withoutExtension = findFeatureWithExactPath(cwd, path);

        if (withoutExtension != null) {
            return withoutExtension;
        }

        return null;
    }

    private String findFeatureWithExactPath(String cwd, String path) {
        if (context.getOptions().LOG_FEATURE_LOCATION) {
            Log.LOGGER.info(String.format("trying %s...", path));
        }

        if (path.startsWith(SourceLoader.RESOURCE_SCHEME)) {
            return path;
        }

        final File file = new File(path);

        if (!file.isFile()) {
            return null;
        }

        try {
            if (file.isAbsolute()) {
                return file.getCanonicalPath();
            } else {
                return new File(cwd, file.getPath()).getCanonicalPath();
            }
        } catch (IOException e) {
            return null;
        }
    }

    @TruffleBoundary
    private boolean isSulongAvailable() {
        return context.getEnv().isMimeTypeSupported(RubyLanguage.SULONG_BITCODE_BASE64_MIME_TYPE);
    }

    @TruffleBoundary
    public void ensureCExtImplementationLoaded(String feature, RequireNode requireNode) {
        synchronized (cextImplementationLock) {
            if (cextImplementationLoaded) {
                return;
            }

            if (!isSulongAvailable()) {
                throw new RaiseException(context.getCoreExceptions().loadError("Sulong is required to support C extensions, and it doesn't appear to be available", feature, null));
            }

            final String cextRBpath = context.getRubyHome() + "/lib/truffle/truffle/cext.rb";
            requireNode.executeRequire(cextRBpath);

            String rubySUpath = loadCExtLibRuby(feature);

            sulongLoadLibraryFunction = getCExtFunction("rb_tr_load_library", rubySUpath);

            cextImplementationLoaded = true;
        }
    }

    @TruffleBoundary
    private String loadCExtLibRuby(String feature) {
        String rubySUpath = context.getRubyHome() + "/lib/cext/ruby.su";

        if (context.getOptions().CEXTS_LOG_LOAD) {
            Log.LOGGER.info(() -> String.format("loading cext implementation %s", rubySUpath));
        }

        if (!new File(rubySUpath).exists()) {
            throw new RaiseException(context.getCoreExceptions().loadError("This TruffleRuby distribution does not have the C extension implementation file ruby.su", feature, null));
        }

        loadCExtLibrary(rubySUpath);

        return rubySUpath;
    }

    @TruffleBoundary
    public void loadCExtLibrary(String path) {
        File file = new File(path);

        if (!new File(path).exists()) {
            throw new RaiseException(context.getCoreExceptions().loadError(path + " does not exists", path, null));
        }

        try {
            Linker.loadLibrary(file,
                    library -> loadNativeLibrary(path, library), this::parseSource);
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    private final Node executeSulongLoadLibraryNode = Message.createExecute(1).createNode();

    private void loadNativeLibrary(String path, String library) {
        assert sulongLoadLibraryFunction != null;

        final String remapped = remapNativeLibrary(library);

        if (context.getOptions().CEXTS_LOG_LOAD) {
            if (remapped.equals(library)) {
                Log.LOGGER.info(() -> String.format("loading native library %s", library));
            } else {
                Log.LOGGER.info(() -> String.format("loading native library %s, remapped from %s", remapped, library));
            }
        }

        DynamicObject libraryRubyString = StringOperations.createString(context, StringOperations.encodeRope(remapNativeLibrary(library), UTF8Encoding.INSTANCE));
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

            // Default remapping of libssl to use Homebrew on macOS (ignored on other platforms, thanks to dylib extension)
            nativeLibraryMap.put("libssl.dylib", "/usr/local/opt/openssl/lib/libssl.dylib");

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

    @TruffleBoundary
    public CallTarget parseSource(Source source) {
        try {
            return context.getEnv().parse(source);
        } catch (Exception e) {
            throw new JavaException(e);
        }
    }

    @TruffleBoundary
    public TruffleObject getCExtFunction(String functionName, String feature) {
        final String name = "@" + functionName;

        final Object function = context.getEnv().importSymbol(name);

        if (!(function instanceof TruffleObject)) {
            if (function == null) {
                throw new RaiseException(context.getCoreExceptions().internalError(
                        String.format("Couldn't find the cext function %s in %s", name, feature),
                        null));
            } else {
                throw new RaiseException(context.getCoreExceptions().internalError(
                        String.format("The cext function %s in %s was not a Truffle object", name, feature),
                        null));
            }
        }

        return (TruffleObject) function;
    }

    // TODO (pitr-ch 16-Mar-2016): this protects the $LOADED_FEATURES only in this class,
    // it can still be accessed and modified (rare) by Ruby code which may cause issues
    private final Object loadedFeaturesLock = new Object();

    public Object getLoadedFeaturesLock() {
        return loadedFeaturesLock;
    }

}
