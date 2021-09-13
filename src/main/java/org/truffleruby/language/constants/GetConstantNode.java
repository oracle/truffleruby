/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.constants;

import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.loader.FeatureLoader;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

public abstract class GetConstantNode extends RubyBaseNode {

    private final boolean callConstMissing;

    @Child private DispatchNode constMissingNode;

    public static GetConstantNode create() {
        return create(true);
    }

    public static GetConstantNode create(boolean callConstMissing) {
        return GetConstantNodeGen.create(callConstMissing);
    }

    public Object lookupAndResolveConstant(LexicalScope lexicalScope, RubyModule module, String name,
            LookupConstantInterface lookupConstantNode) {
        final RubyConstant constant = lookupConstantNode.lookupConstant(lexicalScope, module, name, true);
        return executeGetConstant(lexicalScope, module, name, constant, lookupConstantNode);
    }

    public Object lookupAndResolveConstant(LexicalScope lexicalScope, RubyModule module, String name, boolean checkName,
            LookupConstantInterface lookupConstantNode) {
        final RubyConstant constant = lookupConstantNode.lookupConstant(lexicalScope, module, name, checkName);
        return executeGetConstant(lexicalScope, module, name, constant, lookupConstantNode);
    }

    // name is always the same as constant.getName(), but is needed as constant can be null.
    protected abstract Object executeGetConstant(LexicalScope lexicalScope, RubyModule module, String name,
            Object constant, LookupConstantInterface lookupConstantNode);

    public GetConstantNode(boolean callConstMissing) {
        this.callConstMissing = callConstMissing;
    }

    @Specialization(guards = { "constant != null", "constant.hasValue()" })
    protected Object getConstant(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            RubyConstant constant,
            LookupConstantInterface lookupConstantNode) {
        return constant.getValue();
    }

    @TruffleBoundary
    @Specialization(guards = { "autoloadConstant != null", "autoloadConstant.isAutoload()" })
    protected Object autoloadConstant(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            RubyConstant autoloadConstant,
            LookupConstantInterface lookupConstantNode,
            @Cached DispatchNode callRequireNode) {

        final Object feature = autoloadConstant.getAutoloadConstant().getFeature();

        if (autoloadConstant.getAutoloadConstant().isAutoloadingThread()) {
            // Pretend the constant does not exist while it is autoloading
            return doMissingConstant(module, name, getSymbol(name));
        }

        final FeatureLoader featureLoader = getContext().getFeatureLoader();
        final String expandedPath = featureLoader
                .findFeature(autoloadConstant.getAutoloadConstant().getAutoloadPath());
        if (expandedPath != null && featureLoader.getFileLocks().isCurrentThreadHoldingLock(expandedPath)) {
            // We found an autoload constant while we are already require-ing the autoload file,
            // consider it missing to avoid circular require warnings and calling #require twice.
            // For instance, autoload :RbConfig, "rbconfig"; require "rbconfig" causes this.
            // Also see https://github.com/oracle/truffleruby/pull/1779 and GR-14590
            if (getContext().getOptions().LOG_AUTOLOAD) {
                RubyLanguage.LOGGER.info(() -> String.format(
                        "%s: %s::%s is being treated as missing while loading %s",
                        RubyLanguage.fileLine(getContext().getCallStack().getTopMostUserSourceSection()),
                        module.fields.getName(),
                        name,
                        expandedPath));
            }
            return doMissingConstant(module, name, getSymbol(name));
        }

        if (getContext().getOptions().LOG_AUTOLOAD) {
            RubyLanguage.LOGGER.info(() -> String.format(
                    "%s: autoloading %s with %s",
                    RubyLanguage.fileLine(getContext().getCallStack().getTopMostUserSourceSection()),
                    autoloadConstant,
                    autoloadConstant.getAutoloadConstant().getAutoloadPath()));
        }

        // Mark the autoload constant as loading already here and not in RequireNode so that recursive lookups act as "being loaded"
        autoloadConstantStart(getContext(), autoloadConstant, this);
        try {
            callRequireNode.call(coreLibrary().mainObject, "require", feature);

            // This needs to run while the autoload is marked as isAutoloading(), to avoid infinite recursion
            return autoloadResolveConstant(lexicalScope, module, name, autoloadConstant, lookupConstantNode);
        } finally {
            autoloadConstantStop(autoloadConstant);
        }
    }

    @TruffleBoundary
    public static void autoloadConstantStart(RubyContext context, RubyConstant autoloadConstant, Node currentNode) {
        autoloadConstant.getAutoloadConstant().startAutoLoad(context, currentNode);

        // We need to notify cached lookup that we are autoloading the constant, as constant
        // lookup changes based on whether an autoload constant is loading or not (constant
        // lookup ignores being-autoloaded constants).
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
            final String message = RubyLanguage.fileLine(section) + ": " + constant + " " +
                    (undefined
                            ? "was marked as undefined as it was not assigned in "
                            : "was successfully autoloaded from ") +
                    constant.getAutoloadConstant().getAutoloadPath();
            RubyLanguage.LOGGER.info(message);
        }
    }

    @TruffleBoundary
    public Object autoloadResolveConstant(LexicalScope lexicalScope, RubyModule module, String name,
            RubyConstant autoloadConstant, LookupConstantInterface lookupConstantNode) {
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

        return executeGetConstant(lexicalScope, module, name, resolvedConstant, lookupConstantNode);
    }

    @Specialization(
            guards = { "isNullOrUndefined(constant)", "guardName(name, cachedName, sameNameProfile)" },
            limit = "getCacheLimit()")
    protected Object missingConstantCached(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            Object constant,
            LookupConstantInterface lookupConstantNode,
            @Cached("name") String cachedName,
            @Cached("getSymbol(name)") RubySymbol symbolName,
            @Cached ConditionProfile sameNameProfile) {
        return doMissingConstant(module, name, symbolName);
    }

    @Specialization(guards = "isNullOrUndefined(constant)")
    protected Object missingConstantUncached(
            LexicalScope lexicalScope,
            RubyModule module,
            String name,
            Object constant,
            LookupConstantInterface lookupConstantNode) {
        return doMissingConstant(module, name, getSymbol(name));
    }

    private Object doMissingConstant(RubyModule module, String name, RubySymbol symbolName) {
        if (callConstMissing) {
            if (constMissingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                constMissingNode = insert(DispatchNode.create());
            }

            return constMissingNode.call(module, "const_missing", symbolName);
        } else {
            return null;
        }
    }

    protected boolean isNullOrUndefined(Object constant) {
        return constant == null || ((RubyConstant) constant).isUndefined();
    }

    @SuppressFBWarnings("ES")
    protected boolean guardName(String name, String cachedName, ConditionProfile sameNameProfile) {
        // This is likely as for literal constant lookup the name does not change and Symbols
        // always return the same String.
        if (sameNameProfile.profile(name == cachedName)) {
            return true;
        } else {
            return name.equals(cachedName);
        }
    }

    protected int getCacheLimit() {
        return getLanguage().options.CONSTANT_CACHE;
    }

}
