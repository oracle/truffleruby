/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.io.IOException;

import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.nodes.LanguageInfo;
import org.graalvm.options.OptionDescriptor;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.MainLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.options.OptionsCatalog;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.api.Toolchain;

@CoreModule("Truffle::Boot")
public abstract class TruffleBootNodes {

    @CoreMethod(names = "ruby_home", onSingleton = true)
    public abstract static class RubyHomeNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject rubyHome() {
            if (getContext().getRubyHome() == null) {
                return nil();
            } else {
                return makeStringNode
                        .executeMake(getContext().getRubyHome(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
        }

    }

    @CoreMethod(names = "force_context", onSingleton = true)
    public abstract static class ForceContextNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject forceContext() {
            return nil();
        }
    }

    @CoreMethod(names = "preinitializing?", onSingleton = true)
    public abstract static class IsPreinitializingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isPreinitializingContext() {
            return getContext().isPreInitializing();
        }
    }

    @CoreMethod(names = "was_preinitialized?", onSingleton = true)
    public abstract static class WasPreinitializedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean wasPreinitializedContext() {
            return getContext().wasPreInitialized();
        }
    }

    @CoreMethod(names = "main", onSingleton = true, required = 2)
    public abstract static class MainNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected int main(DynamicObject kind, DynamicObject toExecute,
                @Cached IndirectCallNode callNode,
                @Cached("createPrivate()") CallDispatchHeadNode checkSyntax,
                @Cached StringNodes.MakeStringNode makeStringNode) {

            setArgvGlobals(makeStringNode);

            final RubySource source;

            try {
                source = loadMainSourceSettingDollarZero(
                        makeStringNode,
                        StringOperations.getString(kind),
                        StringOperations.getString(toExecute).intern());
            } catch (RaiseException e) {
                getContext().getDefaultBacktraceFormatter().printRubyExceptionMessageOnEnvStderr(e.getException());
                return 1;
            }

            if (getContext().getOptions().SYNTAX_CHECK) {
                try {
                    return (int) checkSyntax.call(
                            getContext().getCoreLibrary().getTruffleBootModule(),
                            "check_syntax",
                            source);
                } catch (RaiseException e) {
                    getContext().getDefaultBacktraceFormatter().printRubyExceptionMessageOnEnvStderr(e.getException());
                    return 1;
                }
            } else {
                final RubyRootNode rootNode;

                try {
                    rootNode = getContext().getCodeLoader().parse(
                            source,
                            ParserContext.TOP_LEVEL_FIRST,
                            null,
                            null,
                            true,
                            null);
                } catch (RaiseException e) {
                    getContext().getDefaultBacktraceFormatter().printRubyExceptionMessageOnEnvStderr(e.getException());
                    return 1;
                }

                final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                        ParserContext.TOP_LEVEL_FIRST,
                        DeclarationContext.topLevel(getContext()),
                        rootNode,
                        null,
                        coreLibrary().getMainObject());

                // The TopLevelRaiseHandler returns an int
                return (int) deferredCall.call(callNode);
            }
        }

        private void setArgvGlobals(StringNodes.MakeStringNode makeStringNode) {
            if (getContext().getOptions().ARGV_GLOBALS) {
                String[] global_values = getContext().getOptions().ARGV_GLOBAL_VALUES;
                assert global_values.length % 2 == 0;
                for (int i = 0; i < global_values.length; i += 2) {
                    String key = global_values[i];
                    String value = global_values[i + 1];

                    getContext().getCoreLibrary().getGlobalVariables().define(
                            "$" + key,
                            makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN));
                }

                String[] global_flags = getContext().getOptions().ARGV_GLOBAL_FLAGS;
                for (String flag : global_flags) {
                    getContext().getCoreLibrary().getGlobalVariables().define("$" + flag, true);
                }
            }
        }

        private RubySource loadMainSourceSettingDollarZero(StringNodes.MakeStringNode makeStringNode, String kind,
                String toExecute) {
            final RubySource source;
            final Object dollarZeroValue;
            try {
                switch (kind) {
                    case "FILE": {
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromFile(getContext().getEnv(), this, toExecute);
                        dollarZeroValue = makeStringNode
                                .executeMake(toExecute, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                    }
                        break;

                    case "STDIN": {
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromStandardIn(this, "-");
                        dollarZeroValue = makeStringNode.executeMake("-", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                    }
                        break;

                    case "INLINE": {
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromCommandLineArgument(toExecute);
                        dollarZeroValue = makeStringNode.executeMake("-e", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                    }
                        break;

                    default:
                        throw new IllegalStateException();
                }
            } catch (IOException e) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().ioError(e.getMessage(), toExecute, null));
            }

            getContext().getCoreLibrary().getGlobalVariables().getStorage("$0").setValueInternal(dollarZeroValue);
            return source;
        }

    }

    @CoreMethod(names = "original_argv", onSingleton = true)
    public abstract static class OriginalArgvNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject originalArgv() {
            final String[] argv = getContext().getEnv().getApplicationArguments();
            final Object[] array = new Object[argv.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = makeStringNode.executeMake(
                        argv[n],
                        getContext().getEncodingManager().getDefaultExternalEncoding(),
                        CodeRange.CR_UNKNOWN);
            }

            return createArray(array, array.length);
        }

    }

    @CoreMethod(names = "extra_load_paths", onSingleton = true)
    public abstract static class ExtraLoadPathsNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject extraLoadPaths() {
            final String[] paths = getContext().getOptions().LOAD_PATHS;
            final Object[] array = new Object[paths.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = makeStringNode.executeMake(paths[n], UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }

            return createArray(array, array.length);
        }

    }

    @CoreMethod(names = "source_of_caller", isModuleFunction = true)
    public abstract static class SourceOfCallerNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject sourceOfCaller() {
            final Memo<Integer> frameCount = new Memo<>(0);

            final Source source = Truffle.getRuntime().iterateFrames(frameInstance -> {
                if (frameCount.get() == 2) {
                    return frameInstance.getCallNode().getEncapsulatingSourceSection().getSource();
                } else {
                    frameCount.set(frameCount.get() + 1);
                    return null;
                }
            });

            if (source == null) {
                return nil();
            }

            return makeStringNode
                    .executeMake(getContext().getPath(source), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "inner_check_syntax", onSingleton = true, required = 1)
    public abstract static class InnerCheckSyntaxNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject innerCheckSyntax(RubySource source) {
            getContext().getCodeLoader().parse(source, ParserContext.TOP_LEVEL, null, null, true, null);

            return nil();
        }

    }

    @CoreMethod(names = "get_option", onSingleton = true, required = 1)
    public abstract static class GetOptionNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "isRubyString(optionName)")
        protected Object getOption(DynamicObject optionName) {
            final String optionNameString = StringOperations.getString(optionName);
            final OptionDescriptor descriptor = OptionsCatalog.fromName("ruby." + optionNameString);

            if (descriptor == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameError("option not defined", nil(), optionNameString, this));
            }

            final Object value = getContext().getOptions().fromDescriptor(descriptor);

            if (value instanceof Boolean || value instanceof Integer) {
                return value;
            } else if (value instanceof Enum) {
                return getSymbol(((Enum<?>) value).name());
            } else if (value instanceof String) {
                return makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else if (value instanceof String[]) {
                return toRubyArray((String[]) value);
            } else {
                throw new UnsupportedOperationException();
            }
        }

        private DynamicObject toRubyArray(String[] strings) {
            final Object[] objects = new Object[strings.length];
            for (int n = 0; n < strings.length; n++) {
                objects[n] = makeStringNode.executeMake(strings[n], UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
            return createArray(objects);
        }

    }

    @CoreMethod(names = "resilient_gem_home?", onSingleton = true)
    public abstract static class IsResilientGemHomeNode extends CoreMethodArrayArgumentsNode {

        private static final boolean RESILIENT_GEM_HOME = TruffleOptions.AOT
                ? Boolean.getBoolean("truffleruby.native.resilient_gem_home")
                : false;

        @TruffleBoundary
        @Specialization
        protected boolean resilientGemHome() {
            if (!getContext().getOptions().NATIVE_PLATFORM) {
                return false; // Cannot remove environment variables
            }
            if (RESILIENT_GEM_HOME) {
                return true;
            }
            final String envVar = System.getenv("TRUFFLERUBY_RESILIENT_GEM_HOME");
            return envVar != null && !envVar.isEmpty();
        }

    }

    @CoreMethod(names = "print_time_metric", onSingleton = true, required = 1)
    public abstract static class PrintTimeMetricNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(name)")
        protected Object printTimeMetric(DynamicObject name) {
            Metrics.printTime(Layouts.SYMBOL.getString(name));
            return nil();
        }

    }

    @CoreMethod(names = "single_threaded?", onSingleton = true)
    public abstract static class SingleThreadedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean singleThreaded() {
            return getContext().getOptions().SINGLE_THREADED;
        }

    }

    @CoreMethod(names = "tool_path", onSingleton = true, required = 1)
    public abstract static class ToolPathNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(toolName)")
        protected DynamicObject toolPath(DynamicObject toolName,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final LanguageInfo llvmInfo = getContext().getEnv().getInternalLanguages().get("llvm");
            final Toolchain toolchain = getContext().getEnv().lookup(llvmInfo, Toolchain.class);
            if (toolchain == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("Could not find the LLVM Toolchain", this));
            }
            final TruffleFile path = toolchain.getToolPath(Layouts.SYMBOL.getString(toolName));
            if (path != null) {
                return makeStringNode.executeMake(path.getPath(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else {
                return nil();
            }
        }

    }

}
