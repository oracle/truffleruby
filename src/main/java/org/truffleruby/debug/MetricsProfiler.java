/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.shared.options.Profile;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;

public class MetricsProfiler {

    private final RubyLanguage language;
    private final RubyContext context;
    /** We need to use the same CallTarget for the same name to appear as one entry to the profiler */
    private final Map<String, RootCallTarget> summaryCallTargets = new ConcurrentHashMap<>();

    public MetricsProfiler(RubyLanguage language, RubyContext context) {
        this.language = language;
        this.context = context;
    }

    @TruffleBoundary
    public <T> T callWithMetrics(String metricKind, String feature, Supplier<T> supplier) {
        if (context.getOptions().METRICS_PROFILE_REQUIRE != Profile.NONE) {
            final RootCallTarget callTarget = getCallTarget(metricKind, feature);
            return callAndCast(callTarget, supplier);
        } else {
            return supplier.get();
        }
    }

    private <T> RootCallTarget getCallTarget(String metricKind, String feature) {
        final String name;
        if (context.getOptions().METRICS_PROFILE_REQUIRE == Profile.DETAIL) {
            name = "metrics " + metricKind + " " + language.getPathRelativeToHome(feature);
            return newCallTarget(name);
        } else {
            name = "metrics " + metricKind;
            return ConcurrentOperations.getOrCompute(summaryCallTargets, name, this::newCallTarget);
        }
    }

    private <T> RootCallTarget newCallTarget(String name) {
        final MetricsBodyNode<T> body = new MetricsBodyNode<>();
        final MetricsInternalRootNode rootNode = new MetricsInternalRootNode(context, name, body);
        return rootNode.getCallTarget();
    }

    @SuppressWarnings("unchecked")
    private static <T> T callAndCast(RootCallTarget callTarget, Supplier<T> supplier) {
        return (T) callTarget.call(supplier);
    }

}
