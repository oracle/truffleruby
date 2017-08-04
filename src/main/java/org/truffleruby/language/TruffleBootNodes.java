/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.launcher.options.OptionDescription;
import org.truffleruby.launcher.options.OptionsCatalog;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.TranslatorDriver;

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

    @CoreMethod(names = "ruby_launcher", onSingleton = true)
    public abstract static class RubyLauncherNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject rubyLauncher() {
            if (TruffleOptions.AOT) {
                final String path = (String) Compiler.command(new Object[]{"com.oracle.svm.core.posix.GetExecutableName"});
                return makeStringNode.executeMake(path, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            } else {
                if (getContext().getOptions().LAUNCHER.isEmpty()) {
                    return nil();
                } else {
                    return makeStringNode.executeMake(getContext().getOptions().LAUNCHER, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                }
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

    @CoreMethod(names = "main", onSingleton = true)
    public abstract static class MainNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int main(VirtualFrame frame,
                @Cached("create()") IndirectCallNode callNode,
                @Cached("createOnSelf()") CallDispatchHeadNode findSFile,
                @Cached("createOnSelf()") CallDispatchHeadNode checkSyntax,
                @Cached("create()") StringNodes.MakeStringNode makeStringNode) {

            setArgvGlobals(makeStringNode);

            Source source = loadMainSourceSettingDollarZero(frame, findSFile, makeStringNode);

            if (source == null) {
                // EXECUTION_ACTION was set to NONE
                return 0;
            }

            if (getContext().getOptions().SYNTAX_CHECK) {
                return (int) checkSyntax.call(
                        frame,
                        getContext().getCoreLibrary().getTruffleBootModule(),
                        "check_syntax",
                        source);
            } else {
                final RubyRootNode rootNode = getContext().getCodeLoader().parse(
                        source,
                        UTF8Encoding.INSTANCE,
                        ParserContext.TOP_LEVEL_FIRST,
                        null,
                        true,
                        null);

                final CodeLoader.DeferredCall deferredCall = getContext().getCodeLoader().prepareExecute(
                        ParserContext.TOP_LEVEL_FIRST,
                        DeclarationContext.TOP_LEVEL,
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

        private Source loadMainSourceSettingDollarZero(
                VirtualFrame frame,
                CallDispatchHeadNode findSFile,
                StringNodes.MakeStringNode makeStringNode) {
            final Source source;
            final Object dollarZeroValue;

            try {
                final String to_execute = getContext().getOptions().TO_EXECUTE;
                switch (getContext().getOptions().EXECUTION_ACTION) {
                    case UNSET:
                        throw new IllegalArgumentException("ExecutionAction.UNSET should never reach RubyContext");

                    case NONE:
                        source = null;
                        dollarZeroValue = nil();
                        break;

                    case FILE:
                        source = getContext().getSourceLoader().loadMainFile(this, to_execute);
                        dollarZeroValue = makeStringNode.executeMake(to_execute, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                        break;

                    case PATH:
                        final DynamicObject path = (DynamicObject) findSFile.call(
                                frame,
                                getContext().getCoreLibrary().getTruffleBootModule(),
                                "find_s_file",
                                makeStringNode.executeMake(to_execute, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN));
                        source = getContext().getSourceLoader().loadMainFile(
                                this,
                                StringOperations.getString(path));
                        dollarZeroValue = path;
                        break;

                    case STDIN:
                        source = getContext().getSourceLoader().loadMainStdin(
                                this,
                                to_execute);
                        dollarZeroValue = makeStringNode.executeMake("-", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                        break;

                    case INLINE:
                        source = getContext().getSourceLoader().loadMainEval();
                        dollarZeroValue = makeStringNode.executeMake("-e", USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                        break;

                    default:
                        throw new IllegalStateException();
                }
            } catch (IOException e) {
                throw new JavaException(e);
            }

            getContext().getCoreLibrary().getGlobalVariables().put("$0", dollarZeroValue);

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

    @CoreMethod(names = "original_load_path", onSingleton = true)
    public abstract static class OriginalLoadPathNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject originalLoadPath() {
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

            final String source = Truffle.getRuntime().iterateFrames(frameInstance -> {
                if (frameCount.get() == 2) {
                    return frameInstance.getCallNode().getEncapsulatingSourceSection().getSource().getName();
                } else {
                    frameCount.set(frameCount.get() + 1);
                    return null;
                }
            });

            if (source == null) {
                return nil();
            }

            return makeStringNode.executeMake(source, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "inner_check_syntax", onSingleton = true, required = 1)
    public abstract static class InnerCheckSyntaxNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject innerCheckSyntax(Source source) {
            final TranslatorDriver translator = new TranslatorDriver(getContext());

            translator.parse(source, UTF8Encoding.INSTANCE,
                    ParserContext.TOP_LEVEL, new String[]{}, null, null, true, null);

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
                return getSymbol(((Enum) value).name());
            } else {
                assert value instanceof Integer || value instanceof Boolean;
                return value;
            }
        }

    }

}
