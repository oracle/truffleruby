/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreClass;
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
import org.truffleruby.shared.options.ExecutionAction;
import org.truffleruby.shared.options.OptionDescription;
import org.truffleruby.shared.options.OptionsCatalog;

import java.io.IOException;

@CoreClass("Truffle::Boot")
public abstract class TruffleBootNodes {

    @CoreMethod(names = "ruby_home", onSingleton = true)
    public abstract static class RubyHomeNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject rubyHome() {
            if (getContext().getRubyHome() == null) {
                return nil();
            } else {
                return makeStringNode.executeMake(getContext().getRubyHome(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
        }

    }

    @CoreMethod(names = "force_context", onSingleton = true)
    public abstract static class ForceContextNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject forceContext() {
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

    @CoreMethod(names = "main", onSingleton = true)
    public abstract static class MainNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int main(
                @Cached("create()") IndirectCallNode callNode,
                @Cached("createPrivate()") CallDispatchHeadNode findSFile,
                @Cached("createPrivate()") CallDispatchHeadNode checkSyntax,
                @Cached("create()") StringNodes.MakeStringNode makeStringNode) {

            setArgvGlobals(makeStringNode);

            final RubySource source;

            try {
                source = loadMainSourceSettingDollarZero(
                        findSFile, makeStringNode,
                        getContext().getOptions().EXECUTION_ACTION,
                        getContext().getOptions().TO_EXECUTE.intern());
            } catch (RaiseException e) {
                getContext().getDefaultBacktraceFormatter().printRubyExceptionMessageOnEnvStderr(e.getException());
                return 1;
            }

            if (source == null) {
                // EXECUTION_ACTION was set to NONE
                return 0;
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

                    getContext().getCoreLibrary().getGlobalVariables().put(
                            "$" + key,
                            makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN));
                }

                String[] global_flags = getContext().getOptions().ARGV_GLOBAL_FLAGS;
                for (String flag : global_flags) {
                    getContext().getCoreLibrary().getGlobalVariables().put("$" + flag, true);
                }
            }
        }

        private RubySource loadMainSourceSettingDollarZero(CallDispatchHeadNode findSFile, StringNodes.MakeStringNode makeStringNode, ExecutionAction executionAction, String toExecute) {
            final RubySource source;
            final Object dollarZeroValue;
            try {
                switch (executionAction) {
                    case UNSET: {
                        switch (getContext().getOptions().DEFAULT_EXECUTION_ACTION) {
                            case NONE:
                                return loadMainSourceSettingDollarZero(findSFile, makeStringNode, ExecutionAction.NONE, toExecute);
                            case STDIN:
                                return loadMainSourceSettingDollarZero(findSFile, makeStringNode, ExecutionAction.STDIN, toExecute);
                            case IRB:
                                if (System.console() != null) {
                                    RubyLanguage.LOGGER.warning(
                                            "truffleruby starts IRB when stdin is a TTY instead of reading from stdin, use '-' to read from stdin");
                                    return loadMainSourceSettingDollarZero(findSFile, makeStringNode, ExecutionAction.PATH, "irb");
                                } else {
                                    return loadMainSourceSettingDollarZero(findSFile, makeStringNode, ExecutionAction.STDIN, toExecute);
                                }
                            default:
                                throw new UnsupportedOperationException("unreachable");
                        }
                    }

                    case NONE: {
                        source = null;
                        dollarZeroValue = nil();
                    } break;

                    case FILE: {
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromFile(this, toExecute);
                        dollarZeroValue = makeStringNode.executeMake(toExecute, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                    } break;

                    case PATH: {
                        final DynamicObject path = (DynamicObject) findSFile.call(
                                getContext().getCoreLibrary().getTruffleBootModule(),
                                // language=ruby
                                "find_s_file",
                                makeStringNode.executeMake(toExecute, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN));
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromFile(this, StringOperations.getString(path));
                        dollarZeroValue = path;
                    } break;

                    case STDIN: {
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromStandardIn(this, toExecute);
                        dollarZeroValue = makeStringNode.executeMake("-", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                    } break;

                    case INLINE: {
                        final MainLoader mainLoader = new MainLoader(getContext());
                        source = mainLoader.loadFromCommandLineArgument();
                        dollarZeroValue = makeStringNode.executeMake("-e", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                    } break;

                    default:
                        throw new IllegalStateException();
                }
            } catch (IOException e) {
                throw new RaiseException(getContext(), getContext().getCoreExceptions().ioError(e.getMessage(), toExecute, null));
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
        public DynamicObject originalArgv() {
            final String[] argv = getContext().getEnv().getApplicationArguments();
            final Object[] array = new Object[argv.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = makeStringNode.executeMake(argv[n], getContext().getEncodingManager().getDefaultExternalEncoding(), CodeRange.CR_UNKNOWN);
            }

            return createArray(array, array.length);
        }

    }

    @CoreMethod(names = "extra_load_paths", onSingleton = true)
    public abstract static class ExtraLoadPathsNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject extraLoadPaths() {
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
        public DynamicObject sourceOfCaller() {
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

            return makeStringNode.executeMake(getContext().getPath(source), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "inner_check_syntax", onSingleton = true, required = 1)
    public abstract static class InnerCheckSyntaxNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject innerCheckSyntax(RubySource source) {
            getContext().getCodeLoader().parse(source, ParserContext.TOP_LEVEL, null, true, null);

            return nil();
        }

    }

    @CoreMethod(names = "get_option", onSingleton = true, required = 1)
    public abstract static class GetOptionNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "isRubyString(optionName)")
        public Object getOption(DynamicObject optionName) {
            final OptionDescription<?> description = OptionsCatalog.fromName("ruby." + StringOperations.getString(optionName));
            assert description != null;
            final Object value = getContext().getOptions().fromDescription(description);

            if (value instanceof String) {
                return makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else if (value instanceof Enum) {
                return getSymbol(((Enum<?>) value).name());
            } else {
                assert value instanceof Integer || value instanceof Boolean;
                return value;
            }
        }

    }

    @CoreMethod(names = "resilient_gem_home?", onSingleton = true)
    public abstract static class IsResilientGemHomeNode extends CoreMethodArrayArgumentsNode {

        private static final boolean RESILIENT_GEM_HOME = TruffleOptions.AOT ?
                Boolean.getBoolean("truffleruby.native.resilient_gem_home") : false;

        @TruffleBoundary
        @Specialization
        public boolean resilientGemHome() {
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
        public Object printTimeMetric(DynamicObject name) {
            Metrics.printTime(Layouts.SYMBOL.getString(name));
            return nil();
        }

    }

    @CoreMethod(names = "single_threaded?", onSingleton = true)
    public abstract static class SingleThreadedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean singleThreaded() {
            return getContext().getOptions().SINGLE_THREADED;
        }

    }

}
