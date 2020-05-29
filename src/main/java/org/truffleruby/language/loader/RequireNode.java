/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.WarningNode;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.TranslateExceptionNode;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.Metrics;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

public abstract class RequireNode extends RubyContextNode {

    @Child private IndirectCallNode callNode = IndirectCallNode.create();
    @Child private CallDispatchHeadNode isInLoadedFeatures = CallDispatchHeadNode.createPrivate();
    @Child private BooleanCastNode booleanCastNode = BooleanCastNode.create();
    @Child private CallDispatchHeadNode addToLoadedFeatures = CallDispatchHeadNode.createPrivate();

    @Child private WarningNode warningNode;

    public abstract boolean executeRequire(String feature, DynamicObject expandedPath);

    @Specialization
    protected boolean require(String feature, DynamicObject expandedPathString) {
        final String expandedPath = StringOperations.getString(expandedPathString);
        return requireWithMetrics(feature, expandedPath, expandedPathString);
    }

    @TruffleBoundary(transferToInterpreterOnException = false)
    private boolean requireWithMetrics(String feature, String expandedPathRaw, DynamicObject pathString) {
        requireMetric("before-require-" + feature);
        try {
            //intern() to improve footprint
            return getContext().getMetricsProfiler().callWithMetrics(
                    "require",
                    feature,
                    () -> requireConsideringAutoload(feature, expandedPathRaw.intern(), pathString));
        } finally {
            requireMetric("after-require-" + feature);
        }
    }

    private boolean requireConsideringAutoload(String feature, String expandedPath, DynamicObject pathString) {
        final FeatureLoader featureLoader = getContext().getFeatureLoader();
        final List<RubyConstant> autoloadConstants = featureLoader.getAutoloadConstants(expandedPath);
        if (autoloadConstants != null) {
            if (getContext().getOptions().LOG_AUTOLOAD) {
                String info = autoloadConstants
                        .stream()
                        .map(c -> c + " with " + c.getAutoloadConstant().getAutoloadPath())
                        .collect(Collectors.joining(" and "));
                RubyLanguage.LOGGER
                        .info(() -> String.format(
                                "%s: requiring %s which is registered as an autoload for %s",
                                RubyContext.fileLine(getContext().getCallStack().getTopMostUserSourceSection()),
                                feature,
                                info));
            }

            for (RubyConstant autoloadConstant : autoloadConstants) {
                GetConstantNode.autoloadConstantStart(autoloadConstant);
            }
            try {
                return doRequire(feature, expandedPath, pathString);
            } finally {
                for (RubyConstant autoloadConstant : autoloadConstants) {
                    GetConstantNode.autoloadUndefineConstantIfStillAutoload(autoloadConstant);
                    GetConstantNode.autoloadConstantStop(autoloadConstant);
                    featureLoader.removeAutoload(autoloadConstant);
                }
            }
        } else {
            return doRequire(feature, expandedPath, pathString);
        }
    }

    private boolean doRequire(String originalFeature, String expandedPath, DynamicObject pathString) {
        final ReentrantLockFreeingMap<String> fileLocks = getContext().getFeatureLoader().getFileLocks();
        final ConcurrentMap<String, Boolean> patchFiles = getContext().getCoreLibrary().getPatchFiles();
        String relativeFeature = originalFeature;
        if (new File(originalFeature).isAbsolute()) {
            int i = originalFeature.lastIndexOf("/lib/");
            if (i != -1) {
                relativeFeature = originalFeature.substring(i + "/lib/".length());
            }
            if (relativeFeature.endsWith(".rb")) {
                relativeFeature = relativeFeature.substring(0, relativeFeature.length() - ".rb".length());
            }
        }
        Boolean patchLoaded = patchFiles.get(relativeFeature);
        final boolean isPatched = patchLoaded != null;

        while (true) {
            final ReentrantLock lock = fileLocks.get(expandedPath);

            if (lock.isHeldByCurrentThread()) {
                if (isPatched && !patchLoaded) {
                    // it is loading the original of the patched file for the first time
                    // it has to allow this one case of circular require where the first require was the patch
                    patchLoaded = true;
                    patchFiles.put(relativeFeature, true);
                } else {
                    warnCircularRequire(expandedPath);
                    return false;
                }
            }

            if (!fileLocks.lock(this, getContext().getThreadManager(), expandedPath, lock)) {
                continue;
            }

            try {
                if (isPatched && !patchLoaded) {
                    String expandedPatchPath = getContext().getRubyHome() + "/lib/patches/" + relativeFeature + ".rb";
                    RubyLanguage.LOGGER.config("patch file used: " + expandedPatchPath);
                    final boolean loaded = parseAndCall(expandedPatchPath, expandedPatchPath);
                    assert loaded;

                    final boolean originalLoaded = patchFiles.get(relativeFeature);
                    if (!originalLoaded) {
                        addToLoadedFeatures(pathString);
                        // if original is not loaded make sure we set the patch to loaded
                        patchFiles.put(relativeFeature, true);
                    }

                    return true;
                }

                if (isFeatureLoaded(pathString)) {
                    return false;
                }

                if (!parseAndCall(originalFeature, expandedPath)) {
                    return false;
                }

                addToLoadedFeatures(pathString);

                return true;
            } finally {
                fileLocks.unlock(expandedPath, lock);
            }
        }
    }

    private boolean parseAndCall(String feature, String expandedPath) {
        if (isCExtension(expandedPath)) {
            requireCExtension(feature, expandedPath);
        } else {
            // All other files are assumed to be Ruby, the file type detection is not enough
            final RubySource source;
            try {
                final FileLoader fileLoader = new FileLoader(getContext());
                source = fileLoader.loadFile(getContext().getEnv(), expandedPath);
            } catch (IOException e) {
                return false;
            }

            final RubyRootNode rootNode = getContext().getCodeLoader().parse(
                    source,
                    ParserContext.TOP_LEVEL,
                    null,
                    null,
                    true,
                    this);

            final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                    ParserContext.TOP_LEVEL,
                    DeclarationContext.topLevel(getContext()),
                    rootNode,
                    null,
                    coreLibrary().mainObject);

            requireMetric("before-execute-" + feature);
            try {
                getContext().getMetricsProfiler().callWithMetrics(
                        "execute",
                        feature,
                        () -> deferredCall.call(callNode));
            } finally {
                requireMetric("after-execute-" + feature);
            }
        }
        return true;
    }

    private boolean isCExtension(String path) {
        return path.toLowerCase(Locale.ENGLISH).endsWith(RubyLanguage.CEXT_EXTENSION);
    }

    @TruffleBoundary
    private void requireCExtension(String feature, String expandedPath) {
        final FeatureLoader featureLoader = getContext().getFeatureLoader();

        final TruffleObject library;

        try {
            featureLoader.ensureCExtImplementationLoaded(feature, this);

            if (getContext().getOptions().CEXTS_LOG_LOAD) {
                RubyLanguage.LOGGER
                        .info(String.format("loading cext module %s (requested as %s)", expandedPath, feature));
            }

            library = featureLoader.loadCExtLibrary(feature, expandedPath);
        } catch (Exception e) {
            handleCExtensionException(feature, e);
            throw e;
        }

        final String initFunctionName = "Init_" + getBaseName(expandedPath);

        final TruffleObject initFunction = findFunctionInLibrary(library, initFunctionName, expandedPath);

        InteropLibrary initFunctionInteropLibrary = InteropLibrary.getFactory().getUncached(initFunction);
        if (!initFunctionInteropLibrary.isExecutable(initFunction)) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().loadError(initFunctionName + "() is not executable", expandedPath, null));
        }

        requireMetric("before-execute-" + feature);
        try {
            initFunctionInteropLibrary.execute(initFunction);
        } catch (InteropException e) {
            throw new JavaException(e);
        } finally {
            requireMetric("after-execute-" + feature);
        }
    }

    TruffleObject findFunctionInLibrary(TruffleObject library, String functionName, String path) {
        final Object function;
        try {
            function = InteropLibrary.getFactory().getUncached(library).readMember(library, functionName);
        } catch (UnknownIdentifierException e) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().loadError(String.format("%s() not found", functionName), path, null));
        } catch (UnsupportedMessageException e) {
            throw new JavaException(e);
        }

        if (function == null) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions()
                            .loadError(String.format("%s() not found (READ returned null)", functionName), path, null));
        }

        if (!(function instanceof TruffleObject)) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().loadError(
                            String.format(
                                    "%s() was a %s rather than a TruffleObject",
                                    functionName,
                                    function.getClass().getSimpleName()),
                            path,
                            null));
        }

        return (TruffleObject) function;
    }

    @TruffleBoundary
    private void handleCExtensionException(String feature, Exception e) {
        TranslateExceptionNode.logJavaException(getContext(), this, e);

        final UnsatisfiedLinkError linkErrorException = searchForException(UnsatisfiedLinkError.class, e);
        if (linkErrorException != null) {
            final String linkError = linkErrorException.getMessage();

            if (getContext().getOptions().CEXTS_LOG_LOAD) {
                RubyLanguage.LOGGER.info("unsatisfied link error " + linkError);
            }

            final String message;

            if (feature.equals("openssl.so")) {
                message = String.format(
                        "%s (%s)",
                        "you may need to install the system OpenSSL library libssl - see https://github.com/oracle/truffleruby/blob/master/doc/user/installing-libssl.md",
                        linkError);
            } else {
                message = linkError;
            }

            throw new RaiseException(getContext(), getContext().getCoreExceptions().runtimeError(message, this));
        }

        final Throwable linkerException = searchForException("LLVMLinkerException", e);
        if (linkerException != null) {
            final String linkError = linkerException.getMessage();
            final String message;
            final String home = getContext().getRubyHome();
            final String postInstallHook = (home != null ? home + "/" : "") + "lib/truffle/post_install_hook.sh";

            // Mismatches between the libssl compiled against and the libssl used at runtime (typically on a different machine)
            if (feature.contains("openssl")) {
                message = String.format(
                        "%s (%s)",
                        "the OpenSSL C extension was compiled against a different libssl than the one used on this system - recompile by running " +
                                postInstallHook,
                        linkError);
            } else {
                message = linkError;
            }

            throw new RaiseException(getContext(), getContext().getCoreExceptions().runtimeError(message, this));
        }
    }

    private <T extends Throwable> T searchForException(Class<T> exceptionClass, Throwable exception) {
        while (exception != null) {
            if (exceptionClass.isInstance(exception)) {
                return exceptionClass.cast(exception);
            }
            exception = exception.getCause();
        }

        return null;
    }

    private Throwable searchForException(String exceptionClass, Throwable exception) {
        while (exception != null) {
            if (exception.getClass().getSimpleName().equals(exceptionClass)) {
                return exception;
            }
            exception = exception.getCause();
        }

        return null;
    }

    @TruffleBoundary
    private String getBaseName(String path) {
        final String name = new File(path).getName();
        final int firstDot = name.indexOf('.');
        if (firstDot == -1) {
            return name;
        } else {
            return name.substring(0, firstDot);
        }
    }

    public boolean isFeatureLoaded(DynamicObject feature) {
        final Object included;
        synchronized (getContext().getFeatureLoader().getLoadedFeaturesLock()) {
            included = isInLoadedFeatures
                    .call(coreLibrary().truffleFeatureLoaderModule, "feature_provided?", feature, true);
        }
        return booleanCastNode.executeToBoolean(included);
    }

    private void addToLoadedFeatures(DynamicObject feature) {
        synchronized (getContext().getFeatureLoader().getLoadedFeaturesLock()) {
            addToLoadedFeatures.call(coreLibrary().truffleFeatureLoaderModule, "provide_feature", feature);
        }
    }

    private void warnCircularRequire(String path) {
        if (warningNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            warningNode = insert(new WarningNode());
        }

        if (warningNode.shouldWarn()) {
            final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
            warningNode.warningMessage(
                    sourceSection,
                    "loading in progress, circular require considered harmful - " + path);
        }
    }

    private void requireMetric(String id) {
        if (Metrics.getMetricsTime() && getContext().getOptions().METRICS_TIME_REQUIRE) {
            Metrics.printTime(id);
        }
    }
}
