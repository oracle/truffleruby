/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib;

import java.util.HashMap;
import java.util.Map;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.source.Source;

@CoreModule("Truffle::Coverage")
public abstract class CoverageNodes {

    @CoreMethod(names = "enable", onSingleton = true)
    public abstract static class CoverageEnableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object enable() {
            getLanguage().coverageManager.enable();
            return nil;
        }

    }

    @CoreMethod(names = "disable", onSingleton = true)
    public abstract static class CoverageDisableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object disable() {
            getLanguage().coverageManager.disable();
            return nil;
        }

    }

    @CoreMethod(names = "result_array", onSingleton = true)
    public abstract static class CoverageResultNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyArray resultArray() {

            final Map<Source, long[]> counts = getLanguage().coverageManager.getCounts();

            if (counts == null) {
                throw new RaiseException(getContext(), coreExceptions().runtimeErrorCoverageNotEnabled(this));
            }

            Map<String, RubyArray> results = new HashMap<>();
            for (Map.Entry<Source, long[]> source : counts.entrySet()) {
                final long[] countsArray = source.getValue();

                final Object[] countsStore = new Object[countsArray.length];

                for (int n = 0; n < countsArray.length; n++) {
                    if (countsArray[n] == CoverageManager.NO_CODE) {
                        countsStore[n] = nil;
                    } else {
                        countsStore[n] = countsArray[n];
                    }
                }

                final String path = getLanguage().getSourcePath(source.getKey());
                assert !results.containsKey(path) : "path already exists in coverage results";
                results.put(path, createArray(new Object[]{
                        makeStringNode.executeMake(
                                path,
                                Encodings.UTF_8,
                                CodeRange.CR_UNKNOWN),
                        createArray(countsStore)
                }));
            }

            return createArray(results.values().toArray());
        }

    }

}
