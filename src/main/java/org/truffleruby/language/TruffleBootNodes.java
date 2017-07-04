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
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.TranslatorDriver;

import java.io.IOException;

@CoreClass("Truffle::Boot")
public abstract class TruffleBootNodes {

    @CoreMethod(names = "ruby_home", onSingleton = true)
    public abstract static class RubyHomeNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject rubyHome() {
            if (getContext().getRubyHome() == null) {
                return nil();
            } else {
                return createString(StringOperations.encodeRope(getContext().getRubyHome(), UTF8Encoding.INSTANCE));
            }
        }

    }

    @CoreMethod(names = "ruby_launcher", onSingleton = true)
    public abstract static class RubyLauncherNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject rubyLauncher() {
            if (TruffleOptions.AOT) {
                final String path = (String) Compiler.command(new Object[]{"com.oracle.svm.core.posix.GetExecutableName"});
                return createString(StringOperations.encodeRope(path, UTF8Encoding.INSTANCE));
            } else {
                if (getContext().getOptions().LAUNCHER.isEmpty()) {
                    return nil();
                } else {
                    return createString(StringOperations.encodeRope(getContext().getOptions().LAUNCHER, UTF8Encoding.INSTANCE));
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

    @CoreMethod(names = "main", onSingleton = true, optional = 1)
    public abstract static class MainNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int main(VirtualFrame frame, NotProvided path,
                @Cached("create()") IndirectCallNode callNode) {

            return loadMain(callNode, getContext().getOptions().ORIGINAL_INPUT_FILE);
        }

        @Specialization(guards = "isRubyString(path)")
        public int loadMain(VirtualFrame frame, DynamicObject path,
                @Cached("create()") IndirectCallNode callNode) {
            final String inputFile = StringOperations.getString(path);
            return loadMain(callNode, inputFile);
        }

        private int loadMain(IndirectCallNode callNode, String inputFile) {
            final Source source = getMainSource(inputFile);

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

        @TruffleBoundary
        private synchronized Source getMainSource(String path) {
            try {
                return getContext().getSourceLoader().loadMain(this, path);
            } catch (IOException e) {
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "original_argv", onSingleton = true)
    public abstract static class OriginalArgvNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject originalArgv() {
            final String[] argv = getContext().getEnv().getApplicationArguments();
            final Object[] array = new Object[argv.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = StringOperations.createString(getContext(), StringOperations.encodeRope(argv[n], getContext().getEncodingManager().getDefaultExternalEncoding()));
            }

            return createArray(array, array.length);
        }

    }

    @CoreMethod(names = "original_input_file", onSingleton = true)
    public abstract static class OriginalInputFileNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject originalInputFile() {
            return createString(StringOperations.encodeRope(getContext().getOptions().ORIGINAL_INPUT_FILE, UTF8Encoding.INSTANCE));
        }

    }

    @CoreMethod(names = "original_load_path", onSingleton = true)
    public abstract static class OriginalLoadPathNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject originalLoadPath() {
            final String[] paths = getContext().getOptions().LOAD_PATHS;
            final Object[] array = new Object[paths.length];

            for (int n = 0; n < array.length; n++) {
                array[n] = StringOperations.createString(getContext(), StringOperations.encodeRope(paths[n], UTF8Encoding.INSTANCE));
            }

            return createArray(array, array.length);
        }

    }

    @CoreMethod(names = "rubygems_enabled?", onSingleton = true)
    public abstract static class RubygemsEnabledNode extends CoreMethodNode {

        @Specialization
        public boolean isRubygemsEnabled() {
            return getContext().getOptions().RUBYGEMS;
        }

    }

    @CoreMethod(names = "patching_enabled?", onSingleton = true)
    public abstract static class PatchingEnabledNode extends CoreMethodNode {

        @Specialization
        public boolean isPatchingEnabled() {
            return getContext().getOptions().PATCHING;
        }

    }

    @CoreMethod(names = "patching_openssl_enabled?", onSingleton = true)
    public abstract static class PatchingOpensslEnabledNode extends CoreMethodNode {

        @Specialization
        public boolean isPatchingEnabled() {
            final boolean patching_openssl = getContext().getOptions().PATCHING_OPENSSL;
            if (patching_openssl && !getContext().getOptions().PATCHING) {
                throw new RaiseException(getContext().getCoreExceptions().runtimeError(
                        "Cannot patch openssl without patching enabled", this));
            }
            return patching_openssl;
        }

    }

    @CoreMethod(names = "did_you_mean_enabled?", onSingleton = true)
    public abstract static class DidYouMeanEnabledNode extends CoreMethodNode {

        @Specialization
        public boolean isDidYouMeanEnabled() {
            return getContext().getOptions().DID_YOU_MEAN;
        }

    }


    @CoreMethod(names = "source_of_caller", isModuleFunction = true)
    public abstract static class SourceOfCallerNode extends CoreMethodArrayArgumentsNode {

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

            return createString(StringOperations.encodeRope(source, UTF8Encoding.INSTANCE));
        }

    }

    @CoreMethod(names = "inner_check_syntax", onSingleton = true, required = 1)
    public abstract static class InnerCheckSyntaxNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject innerCheckSyntax(DynamicObject path) {
            final Source source;

            try {
                source = getContext().getSourceLoader().loadMain(this, StringOperations.getString(path));
            } catch (IOException e) {
                throw new JavaException(e);
            }

            final TranslatorDriver translator = new TranslatorDriver(getContext());

            translator.parse(source, UTF8Encoding.INSTANCE,
                    ParserContext.TOP_LEVEL, new String[]{}, null, null, true, null);

            return nil();
        }
    }

    @CoreMethod(names = "sync_stdio?", onSingleton = true)
    public abstract static class SyncStdioNode extends CoreMethodNode {

        @Specialization
        public boolean syncStdio() {
            return getContext().getOptions().SYNC_STDIO;
        }

    }

}
