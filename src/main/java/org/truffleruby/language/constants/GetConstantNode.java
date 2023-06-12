/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.AutoloadConstant;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.LazyDispatchNode;
import org.truffleruby.language.loader.FeatureLoader;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.SourceSection;

public abstract class GetConstantNode extends RubyBaseNode {


    @NeverDefault
    public static GetConstantNode create() {
        return GetConstantNodeGen.create();
    }

    public Object lookupAndResolveConstant(LexicalScope lexicalScope, RubyModule module, String name,
            LookupConstantInterface lookupConstantNode, boolean callConstMissing) {
        final RubyConstant constant = lookupConstantNode.lookupConstant(lexicalScope, module, name, true);
        return executeGetConstant(lexicalScope, module, name, constant, lookupConstantNode, callConstMissing);
    }

    public Object lookupAndResolveConstant(LexicalScope lexicalScope, RubyModule module, String name, boolean checkName,
            LookupConstantInterface lookupConstantNode, boolean callConstMissing) {
        final RubyConstant constant = lookupConstantNode.lookupConstant(lexicalScope, module, name, checkName);
        return executeGetConstant(lexicalScope, module, name, constant, lookupConstantNode, callConstMissing);
    }

    // name is always the same as constant.getName(), but is needed as constant can be null.
    protected abstract Object executeGetConstant(LexicalScope lexicalScope, RubyModule module, String name,
            Object constant, LookupConstantInterface lookupConstantNode, boolean callConstMissing);


    @Specialization(guards = { "constant != null", "constant.hasValue()" })
    protected Object getConstant(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            RubyConstant constant,
            LookupConstantInterface lookupConstantNode,
            boolean callConstMissing) {
        return constant.getValue();
    }

    @TruffleBoundary
    @Specialization(guards = { "constant != null", "constant.isAutoload()" })
    protected Object autoloadConstant(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            RubyConstant constant,
            LookupConstantInterface lookupConstantNode,
            boolean callConstMissing,
            @Cached @Shared LazyDispatchNode constMissingNode,
            @Cached DispatchNode callRequireNode) {

        final AutoloadConstant autoloadConstant = constant.getAutoloadConstant();
        final Object feature = autoloadConstant.getFeature();

        if (autoloadConstant.isAutoloadingThread()) {
            var unpublishedValue = autoloadConstant.getUnpublishedValue();
            if (unpublishedValue != null) {
                return unpublishedValue;
            } else {
                // Pretend the constant does not exist while it is autoloading
                return doMissingConstant(module, name, getSymbol(name), callConstMissing, constMissingNode.get(this));
            }
        }

        final FeatureLoader featureLoader = getContext().getFeatureLoader();
        final String expandedPath = featureLoader
                .findFeature(autoloadConstant.getAutoloadPath());
        if (expandedPath != null && featureLoader.getFileLocks().isCurrentThreadHoldingLock(expandedPath)) {
            // We found an autoload constant while we are already require-ing the autoload file,
            // consider it missing to avoid circular require warnings and calling #require twice.
            // For instance, autoload :RbConfig, "rbconfig"; require "rbconfig" causes this.
            // Also see https://github.com/oracle/truffleruby/pull/1779 and GR-14590
            if (getContext().getOptions().LOG_AUTOLOAD) {
                RubyLanguage.LOGGER.info(() -> String.format(
                        "%s: %s::%s is being treated as missing while loading %s",
                        getContext().fileLine(getContext().getCallStack().getTopMostUserSourceSection()),
                        module.fields.getName(),
                        name,
                        expandedPath));
            }
            return doMissingConstant(module, name, getSymbol(name), callConstMissing, constMissingNode.get(this));
        }

        if (getContext().getOptions().LOG_AUTOLOAD) {
            RubyLanguage.LOGGER.info(() -> String.format(
                    "%s: autoloading %s with %s",
                    getContext().fileLine(getContext().getCallStack().getTopMostUserSourceSection()),
                    constant,
                    autoloadConstant.getAutoloadPath()));
        }

        // Mark the autoload constant as loading already here and not in RequireNode so that recursive lookups act as "being loaded"
        autoloadConstantStart(getContext(), constant, this);
        // TODO return early for other threads if the constant has been set
        try {
            try {
                callRequireNode.call(coreLibrary().mainObject, "require", feature);
            } finally {
                if (autoloadConstant.shouldPublish()) {
                    autoloadConstant.publish(getContext(), constant);
                } // TODO else remove the constant
            }

            // This needs to run while the autoload is marked as isAutoloading(), to avoid infinite recursion
            return autoloadResolveConstant(lexicalScope, module, name, constant, lookupConstantNode,
                    callConstMissing);
        } finally {
            autoloadConstantStop(constant);
        }
    }

    @TruffleBoundary
    public static void autoloadConstantStart(RubyContext context, RubyConstant autoloadConstant, Node currentNode) {
        autoloadConstant.getAutoloadConstant().startAutoLoad(context, currentNode);

        // We need to notify cached lookup that we are autoloading the constant, as constant
        // lookup changes based on whether an autoload constant is loading or not (constant
        // lookup ignores being-autoloaded constants).
        // TODO skip if already has unpublishedValue
        autoloadConstant.getDeclaringModule().fields.newConstantVersion(autoloadConstant.getName());
    }

    @TruffleBoundary
    public static void autoloadConstantStop(RubyConstant autoloadConstant) {
        autoloadConstant.getAutoloadConstant().stopAutoLoad();
    }

    /** Subset of {@link #autoloadResolveConstant} which does not try to resolve the constant. */
    @TruffleBoundary
    public static boolean autoloadUndefineConstantIfStillAutoload(RubyConstant autoloadConstant) {
        final RubyModule autoloadConstantModule = autoloadConstant.getDeclaringModule();
        final ModuleFields fields = autoloadConstantModule.fields;
        return fields.undefineConstantIfStillAutoload(autoloadConstant);
    }

    @TruffleBoundary
    public static void logAutoloadResult(RubyContext context, RubyConstant constant, boolean undefined) {
        if (context.getOptions().LOG_AUTOLOAD) {
            final SourceSection section = context.getCallStack().getTopMostUserSourceSection();
            final String message = context.fileLine(section) + ": " + constant + " " +
                    (undefined
                            ? "was marked as undefined as it was not assigned in "
                            : "was successfully autoloaded from ") +
                    constant.getAutoloadConstant().getAutoloadPath();
            RubyLanguage.LOGGER.info(message);
        }
    }

    @TruffleBoundary
    public Object autoloadResolveConstant(LexicalScope lexicalScope, RubyModule module, String name,
            RubyConstant autoloadConstant, LookupConstantInterface lookupConstantNode, boolean callConstMissing) {
        final RubyModule autoloadConstantModule = autoloadConstant.getDeclaringModule();
        final ModuleFields fields = autoloadConstantModule.fields;

        RubyConstant resolvedConstant = lookupConstantNode.lookupConstant(lexicalScope, module, name, true);

        // check if the constant was set in the ancestors of autoloadConstantModule
        if (resolvedConstant != null &&
                (ModuleOperations.inAncestorsOf(resolvedConstant.getDeclaringModule(), autoloadConstantModule) ||
                        resolvedConstant.getDeclaringModule() == coreLibrary().objectClass)) {
            // all is good, just return that constant
            logAutoloadResult(getContext(), autoloadConstant, false);
        } else {
            // If the autoload constant was not set in the ancestors, undefine the constant
            boolean undefined = fields.undefineConstantIfStillAutoload(autoloadConstant);
            logAutoloadResult(getContext(), autoloadConstant, undefined);

            // redo lookup, to consider the undefined constant
            resolvedConstant = lookupConstantNode.lookupConstant(lexicalScope, module, name, true);
        }

        return executeGetConstant(lexicalScope, module, name, resolvedConstant, lookupConstantNode, callConstMissing);
    }

    @Specialization(
            guards = { "isNullOrUndefined(constant)", "guardName(node, name, cachedName, sameNameProfile)" },
            limit = "getCacheLimit()")
    protected static Object missingConstantCached(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            Object constant,
            LookupConstantInterface lookupConstantNode,
            boolean callConstMissing,
            @Cached("name") String cachedName,
            @Cached("getSymbol(name)") RubySymbol symbolName,
            @Cached InlinedConditionProfile sameNameProfile,
            @Cached @Shared LazyDispatchNode constMissingNode,
            @Bind("this") Node node) {
        return doMissingConstant(module, name, symbolName, callConstMissing, constMissingNode.get(node));
    }

    @Specialization(guards = "isNullOrUndefined(constant)")
    protected Object missingConstantUncached(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            Object constant,
            LookupConstantInterface lookupConstantNode,
            boolean callConstMissing,
            @Cached @Shared LazyDispatchNode constMissingNode) {
        return doMissingConstant(module, name, getSymbol(name), callConstMissing, constMissingNode.get(this));
    }

    private static Object doMissingConstant(RubyModule module, String name, RubySymbol symbolName,
            boolean callConstMissing, DispatchNode constMissingNode) {
        CompilerAsserts.partialEvaluationConstant(callConstMissing);
        if (callConstMissing) {
            return constMissingNode.call(module, "const_missing", symbolName);
        } else {
            return null;
        }
    }

    protected boolean isNullOrUndefined(Object constant) {
        return constant == null || ((RubyConstant) constant).isUndefined();
    }

    @SuppressFBWarnings("ES")
    protected boolean guardName(Node node, String name, String cachedName, InlinedConditionProfile sameNameProfile) {
        // This is likely as for literal constant lookup the name does not change and Symbols
        // always return the same String.
        if (sameNameProfile.profile(node, name == cachedName)) {
            return true;
        } else {
            return name.equals(cachedName);
        }
    }

    protected int getCacheLimit() {
        return getLanguage().options.CONSTANT_CACHE;
    }

}
