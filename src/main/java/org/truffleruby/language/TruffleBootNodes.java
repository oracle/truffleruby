/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.NodeUtil;
import org.graalvm.options.OptionDescriptor;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.exceptions.TopLevelRaiseHandler;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.MainLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.options.OptionsCatalog;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.api.Toolchain;

@CoreModule("Truffle::Boot")
public abstract class TruffleBootNodes {

    @CoreMethod(names = "ruby_home", onSingleton = true)
    public abstract static class RubyHomeNode extends CoreMethodNode {

        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object rubyHome() {
            if (getContext().getRubyHome() == null) {
                return nil;
            } else {
                return makeStringNode
                        .executeMake(getContext().getRubyHome(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
        }

    }

    @CoreMethod(names = "force_context", onSingleton = true)
    public abstract static class ForceContextNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object forceContext() {
            return nil;
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

        @Child TopLevelRaiseHandler topLevelRaiseHandler = new TopLevelRaiseHandler();
        @Child DispatchNode checkSyntax = DispatchNode.create();
        @Child IndirectCallNode callNode = IndirectCallNode.create();
        @Child DispatchNode requireNode = DispatchNode.create();
        @Child MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = { "stringsKind.isRubyString(kind)", "stringsToExecute.isRubyString(toExecute)" })
        protected int main(Object kind, Object toExecute,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsKind,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsToExecute) {
            return topLevelRaiseHandler.execute(() -> {
                setArgvGlobals();

                // Need to set $0 before loading required libraries
                // Also, a non-existing main script file errors out before loading required libraries
                final RubySource source = loadMainSourceSettingDollarZero(
                        stringsKind.getJavaString(kind),
                        stringsToExecute.getJavaString(toExecute).intern()); //intern() to improve footprint

                // Load libraries required from the command line (-r LIBRARY)
                for (String requiredLibrary : getContext().getOptions().REQUIRED_LIBRARIES) {
                    requireNode.call(coreLibrary().mainObject, "require", utf8(requiredLibrary));
                }

                if (getContext().getOptions().SYNTAX_CHECK) {
                    checkSyntax.call(coreLibrary().truffleBootModule, "check_syntax", source);
                } else {
                    final RubyRootNode rootNode = getContext().getCodeLoader().parse(
                            source,
                            ParserContext.TOP_LEVEL_FIRST,
                            null,
                            null,
                            true,
                            null);

                    final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                            ParserContext.TOP_LEVEL_FIRST,
                            DeclarationContext.topLevel(getContext()),
                            rootNode,
                            null,
                            coreLibrary().mainObject);

                    deferredCall.call(callNode);
                }
            });
        }

        private void setArgvGlobals() {
            if (getContext().getOptions().ARGV_GLOBALS) {
                String[] global_values = getContext().getOptions().ARGV_GLOBAL_VALUES;
                assert global_values.length % 2 == 0;
                for (int i = 0; i < global_values.length; i += 2) {
                    String key = global_values[i];
                    String value = global_values[i + 1];

                    getContext().getCoreLibrary().globalVariables.define("$" + key, utf8(value));
                }

                String[] global_flags = getContext().getOptions().ARGV_GLOBAL_FLAGS;
                for (String flag : global_flags) {
                    getContext().getCoreLibrary().globalVariables.define("$" + flag, true);
                }
            }
        }

        private RubySource loadMainSourceSettingDollarZero(String kind, String toExecute) {
            final RubySource source;
            final Object dollarZeroValue;
            final MainLoader mainLoader = new MainLoader(getContext(), getLanguage());
            try {
                switch (kind) {
                    case "FILE": {
                        source = mainLoader.loadFromFile(getContext().getEnv(), this, toExecute);
                        dollarZeroValue = utf8(toExecute);
                    }
                        break;

                    case "STDIN": {
                        source = mainLoader.loadFromStandardIn(this, "-");
                        dollarZeroValue = utf8("-");
                    }
                        break;

                    case "INLINE": {
                        source = mainLoader.loadFromCommandLineArgument(toExecute);
                        dollarZeroValue = utf8("-e");
                    }
                        break;

                    default:
                        throw new IllegalStateException();
                }
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }

            int index = getLanguage().getGlobalVariableIndex("$0");
            getContext().getGlobalVariableStorage(index).setValueInternal(dollarZeroValue);
            return source;
        }

        private RubyString utf8(String string) {
            return makeStringNode.executeMake(string, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "original_argv", onSingleton = true)
    public abstract static class OriginalArgvNode extends CoreMethodNode {

        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyArray originalArgv() {
            final String[] argv = getContext().getEnv().getApplicationArguments();
            final Object[] array = new Object[argv.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = makeStringNode.executeMake(
                        argv[n],
                        getContext().getEncodingManager().getDefaultExternalEncoding(),
                        CodeRange.CR_UNKNOWN);
            }

            return createArray(array);
        }

    }

    @CoreMethod(names = "extra_load_paths", onSingleton = true)
    public abstract static class ExtraLoadPathsNode extends CoreMethodNode {

        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyArray extraLoadPaths() {
            final String[] paths = getContext().getOptions().LOAD_PATHS;
            final Object[] array = new Object[paths.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = makeStringNode.executeMake(paths[n], UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }

            return createArray(array);
        }

    }

    @CoreMethod(names = "source_of_caller", onSingleton = true)
    public abstract static class SourceOfCallerNode extends CoreMethodArrayArgumentsNode {

        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object sourceOfCaller() {
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
                return nil;
            }

            return makeStringNode
                    .executeMake(getLanguage().getSourcePath(source), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "inner_check_syntax", onSingleton = true, required = 1)
    public abstract static class InnerCheckSyntaxNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object innerCheckSyntax(RubySource source) {
            RubyContext context = getContext();
            RubyRootNode rubyRootNode = context
                    .getCodeLoader()
                    .parse(source, ParserContext.TOP_LEVEL, null, null, true, null);
            EmitWarningsNode emitWarningsNode = NodeUtil.findFirstNodeInstance(rubyRootNode, EmitWarningsNode.class);
            if (emitWarningsNode != null) {
                emitWarningsNode.printWarnings(context);
            }
            return nil;
        }

    }

    @CoreMethod(names = "get_option", onSingleton = true, required = 1)
    public abstract static class GetOptionNode extends CoreMethodArrayArgumentsNode {

        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "libOptionName.isRubyString(optionName)")
        protected Object getOption(Object optionName,
                @CachedLibrary(limit = "2") RubyStringLibrary libOptionName) {
            final String optionNameString = libOptionName.getJavaString(optionName);
            final OptionDescriptor descriptor = OptionsCatalog.fromName("ruby." + optionNameString);
            if (descriptor == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameError("option not defined", nil, optionNameString, this));
            }

            Object value = getContext().getOptions().fromDescriptor(descriptor);
            if (value != null && getContext().isPreInitializing()) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError(
                                "Truffle::Boot.get_option() should not be called during pre-initialization as context options might change at runtime.\n" +
                                        "Use Truffle::Boot.{delay,redo} to delay such a check to runtime, or make the option a language option.",
                                this));
            }

            if (value == null) {
                value = getLanguage().options.fromDescriptor(descriptor);
            }

            if (value instanceof Boolean || value instanceof Integer) {
                return value;
            } else if (value instanceof Enum) {
                return getSymbol(((Enum<?>) value).name());
            } else if (value instanceof String) {
                return makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else if (value instanceof String[]) {
                return toRubyArray((String[]) value);
            } else {
                throw CompilerDirectives
                        .shouldNotReachHere("Unknown value for option " + optionNameString + ": " + value);
            }
        }

        private RubyArray toRubyArray(String[] strings) {
            final Object[] objects = new Object[strings.length];
            for (int n = 0; n < strings.length; n++) {
                objects[n] = makeStringNode.executeMake(strings[n], UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
            return createArray(objects);
        }

    }

    @CoreMethod(names = "print_time_metric", onSingleton = true, required = 1)
    public abstract static class PrintTimeMetricNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object printTimeMetric(RubySymbol name) {
            Metrics.printTime(name.getString());
            return nil;
        }

    }

    @CoreMethod(names = "single_threaded?", onSingleton = true)
    public abstract static class SingleThreadedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean singleThreaded() {
            return getContext().getOptions().SINGLE_THREADED;
        }

    }

    @CoreMethod(names = "toolchain_executable", onSingleton = true, required = 1)
    public abstract static class ToolchainExecutableNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object toolchainExecutable(RubySymbol executable,
                @Cached MakeStringNode makeStringNode) {
            final String name = executable.getString();
            final Toolchain toolchain = getToolchain(getContext(), this);
            final TruffleFile path = toolchain.getToolPath(name);
            if (path != null) {
                return makeStringNode.executeMake(path.getPath(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("Toolchain executable " + name + " not found", this));
            }
        }

    }

    @CoreMethod(names = "toolchain_paths", onSingleton = true, required = 1)
    public abstract static class ToolchainPathsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object toolchainPaths(RubySymbol pathName,
                @Cached MakeStringNode makeStringNode) {
            final String name = pathName.getString();
            final Toolchain toolchain = getToolchain(getContext(), this);
            final List<TruffleFile> paths = toolchain.getPaths(name);
            if (paths != null) {
                String path = paths
                        .stream()
                        .map(file -> file.getPath())
                        .collect(Collectors.joining(File.pathSeparator));
                return makeStringNode.executeMake(path, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("Toolchain path " + name + " not found", this));
            }
        }

    }

    private static Toolchain getToolchain(RubyContext context, Node currentNode) {
        final LanguageInfo llvmInfo = context.getEnv().getInternalLanguages().get("llvm");
        if (llvmInfo == null) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().runtimeError(
                            "Could not find Sulong in internal languages",
                            currentNode));
        }
        final Toolchain toolchain = context.getEnv().lookup(llvmInfo, Toolchain.class);
        if (toolchain == null) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().runtimeError("Could not find the LLVM Toolchain", currentNode));
        }
        return toolchain;
    }

    @CoreMethod(names = "basic_abi_version", onSingleton = true)
    public abstract static class BasicABIVersionNode extends CoreMethodNode {

        private static final String ABI_VERSION_FILE = "lib/cext/ABI_version.txt";

        @TruffleBoundary
        @Specialization
        protected RubyString basicABIVersion(
                @Cached MakeStringNode makeStringNode) {
            TruffleFile file = getContext().getRubyHomeTruffleFile().resolve(ABI_VERSION_FILE);
            byte[] bytes;
            try {
                bytes = file.readAllBytes();
            } catch (IOException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }

            String basicVersion = RopeOperations.decodeAscii(bytes).trim();
            return makeStringNode.executeMake(basicVersion, UTF8Encoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

}
