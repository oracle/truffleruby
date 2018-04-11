/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.truffleruby.launcher;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.truffleruby.launcher.options.CommandLineException;
import org.truffleruby.shared.options.CommandLineOptions;
import org.truffleruby.launcher.options.CommandLineParser;
import org.truffleruby.shared.options.ExecutionAction;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.RubyLogger;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RubyLauncher extends AbstractLanguageLauncher {

    // Properties set directly on the java command-line with -D for image building
    private static final String LIBSULONG_DIR = isAOT() ? System.getProperty("truffleruby.native.libsulong_dir") : null;

    private CommandLineOptions config;

    public static void main(String[] args) {
        new RubyLauncher().launch(args);
    }

    static boolean isGraal() {
        return Engine.create().getImplementationName().contains("Graal");
    }

    @Override
    protected String getLanguageId() {
        return TruffleRuby.LANGUAGE_ID;
    }

    @Override
    protected String getMainClass() {
        return RubyLauncher.class.getName();
    }

    @Override
    protected void validateArguments(Map<String, String> polyglotOptions) {
    }

    @Override
    protected void printVersion() {
        System.out.println(TruffleRuby.getVersionString(isGraal(), isAOT()));
        System.out.println();
        printPolyglotVersions();
    }

    @Override
    protected List<String> preprocessArguments(List<String> args, Map<String, String> polyglotOptions) {
        Metrics.begin();

        config = new CommandLineOptions(polyglotOptions);

        try {
            config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.UNSET);

            final CommandLineParser argumentCommandLineParser = new CommandLineParser(args, config, true, false);
            argumentCommandLineParser.processArguments();

            if (config.getOption(OptionsCatalog.READ_RUBYOPT)) {
                final List<String> rubyoptArgs = getArgsFromEnvVariable("RUBYOPT");
                final List<String> trufflerubyoptArgs = getArgsFromEnvVariable("TRUFFLERUBYOPT");
                new CommandLineParser(rubyoptArgs, config, false, true).processArguments();
                new CommandLineParser(trufflerubyoptArgs, config, false, false).processArguments();

                if (isAOT()) {
                    // Append options from ENV variables to args after last interpreter option, which makes sure that
                    // maybeExec processes --(native|jvm)* options. The options are removed and are not passed to the
                    // new process if exec is being called.
                    // The new process gets all arguments and options including those from ENV variables.
                    // To avoid processing options from ENV variables twice READ_RUBYOPT option is set to false.
                    // Only native launcher can apply native and jvm options, therefore this is not done on JVM.
                    final int index = argumentCommandLineParser.getLastInterpreterArgumentIndex();
                    args.add(index, "-Xread_rubyopt=false");
                    args.addAll(index + 1, rubyoptArgs);
                    args.addAll(index + 1 + rubyoptArgs.size(), trufflerubyoptArgs);
                }
            }

            final String launcher = isAOT() ? setRubyLauncher() : null;
            if (isAOT() && IN_GRAALVM) {
                // if applied store the options in polyglotOptions otherwise it would be lost when
                // switched to --jvm
                if (config.getOption(OptionsCatalog.HOME).isEmpty()) {
                    final String rubyHome = getGraalVMHome().resolve(Paths.get("jre", "languages", "ruby")).toString();
                    config.setOption(OptionsCatalog.HOME, rubyHome);
                    polyglotOptions.put(OptionsCatalog.HOME.getName(), rubyHome);
                }

                if (launcher != null) {
                    polyglotOptions.put(OptionsCatalog.LAUNCHER.getName(), launcher);
                }
            }

        } catch (CommandLineException commandLineException) {
            System.err.println("truffleruby: " + commandLineException.getMessage());
            if (commandLineException.isUsageError()) {
                printHelp(System.err);
            }
            System.exit(1);
        }

        return config.getUnknownArguments();
    }

    @Override
    protected void launch(Context.Builder contextBuilder) {
        printPreRunInformation(config);
        debugPreInitialization();
        final int exitValue = runRubyMain(contextBuilder, config);
        Metrics.end(isAOT());
        System.exit(exitValue);
    }

    @Override
    protected void collectArguments(Set<String> options) {
        options.addAll(Arrays.asList(
                "-0",
                "-a",
                "-c",
                "-C",
                "-d", "--debug",
                "-e",
                "-E", "--encoding",
                "-F",
                "-i",
                "-I",
                "-l",
                "-n",
                "-p",
                "-r",
                "-s",
                "-S",
                "-T",
                "-v", "--verbose",
                "-w",
                "-W",
                "-x",
                "--copyright",
                "--enable", "--disable",
                "--external-encoding", "--internal-encoding",
                "--version",
                "--help",
                "-Xlog",
                "-Xoptions"));
    }

    @Override
    protected String[] getDefaultLanguages() {
        return new String[]{ getLanguageId(), "llvm" };
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        printHelp(System.out);
    }

    @Override
    protected AbortException abortUnrecognizedArgument(String argument) {
        throw abortInvalidArgument(argument, "truffleruby: invalid option " + argument + "  (Use --help for usage instructions.)");
    }

    private static int runRubyMain(Context.Builder contextBuilder, CommandLineOptions config) {
        if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.UNSET) {
            config.getOption(OptionsCatalog.DEFAULT_EXECUTION_ACTION).applyTo(config);
        }

        if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.NONE) {
            return 0;
        }

        try (Context context = createContext(contextBuilder, config)) {
            Metrics.printTime("before-run");
            final Source source = Source.newBuilder(
                    TruffleRuby.LANGUAGE_ID,
                    // language=ruby
                    "Truffle::Boot.main",
                    TruffleRuby.BOOT_SOURCE_NAME).internal(true).buildLiteral();
            final int exitCode = context.eval(source).asInt();
            Metrics.printTime("after-run");
            return exitCode;
        } catch (PolyglotException e) {
            System.err.print("truffleruby: ");
            e.printStackTrace(System.err);
            return 1;
        }
    }

    private static void debugPreInitialization() {
        if (!isAOT() && TruffleRuby.PRE_INITIALIZE_CONTEXTS) {
            try {
                final Class<?> holderClz = Class.forName("org.graalvm.polyglot.Engine$ImplHolder");
                final Method preInitMethod = holderClz.getDeclaredMethod("preInitializeEngine");
                preInitMethod.setAccessible(true);
                preInitMethod.invoke(null);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
    }

    private static Context createContext(Context.Builder builder, CommandLineOptions config) {
        builder.allowCreateThread(true);
        builder.allowHostAccess(true);

        builder.option(OptionsCatalog.EMBEDDED.getName(), Boolean.FALSE.toString());

        // When building a native image outside of GraalVM, we need to give the path to libsulong
        if (LIBSULONG_DIR != null) {
            final String launcher = config.getOption(OptionsCatalog.LAUNCHER);
            final String rubyHome = new File(launcher).getParentFile().getParent();
            final String libSulongPath = rubyHome + File.separator + LIBSULONG_DIR;

            String libraryPath = System.getProperty("polyglot.llvm.libraryPath");
            if (libraryPath == null || libraryPath.isEmpty()) {
                libraryPath = libSulongPath;
            } else {
                libraryPath = libraryPath + ":" + libSulongPath;
            }
            builder.option("llvm.libraryPath", libraryPath);
        }

        builder.arguments(TruffleRuby.LANGUAGE_ID, config.getArguments());

        return builder.build();
    }

    private static List<String> getArgsFromEnvVariable(String name) {
        String value = System.getenv(name);
        if (value != null) {
            value = value.trim();
            if (value.length() != 0) {
                return new ArrayList<>(Arrays.asList(value.split("\\s+")));
            }
        }
        return Collections.emptyList();
    }

    private String setRubyLauncher() {
        if (config.getOption(OptionsCatalog.LAUNCHER).isEmpty()) {
            final String launcher = (String) Compiler.
                    command(new Object[]{ "com.oracle.svm.core.posix.GetExecutableName" });
            config.setOption(OptionsCatalog.LAUNCHER, launcher);
            return launcher;
        }
        return null;
    }

    private static Path getGraalVMHome() {
        final String graalVMHome = System.getProperty("org.graalvm.home");
        assert graalVMHome != null;
        return Paths.get(graalVMHome);
    }

    private static void printPreRunInformation(CommandLineOptions config) {
        if (config.isIrbInsteadOfInputUsed()) {
            RubyLogger.LOGGER.warning(
                    "by default truffleruby drops into IRB instead of reading stdin as MRI - " +
                            "use '-' to explicitly read from stdin");
        }

        if (config.getOption(OptionsCatalog.SHOW_VERSION)) {
            System.out.println(TruffleRuby.getVersionString(isGraal(), isAOT()));
        }

        if (config.getOption(OptionsCatalog.SHOW_COPYRIGHT)) {
            System.out.println(TruffleRuby.RUBY_COPYRIGHT);
        }

        switch (config.getOption(OptionsCatalog.SHOW_HELP)) {
            case NONE:
                break;
            case SHORT:
                printShortHelp(System.out);
                break;
            case LONG:
                printHelp(System.out);
                break;
        }
    }

    private static void printHelp(PrintStream out) {
        out.printf("Usage: %s [switches] [--] [programfile] [arguments]%n", TruffleRuby.ENGINE_ID);
        out.println("  -0[octal]       specify record separator (\0, if no argument)");
        out.println("  -a              autosplit mode with -n or -p (splits $_ into $F)");
        out.println("  -c              check syntax only");
        out.println("  -Cdirectory     cd to directory before executing your script");
        out.println("  -d, --debug     set debugging flags (set $DEBUG to true)");
        out.println("  -e 'command'    one line of script. Several -e's allowed. Omit [programfile]");
        out.println("  -Eex[:in], --encoding=ex[:in]");
        out.println("                  specify the default external and internal character encodings");
        out.println("  -Fpattern       split() pattern for autosplit (-a)");
        out.println("  -i[extension]   edit ARGV files in place (make backup if extension supplied)");
        out.println("  -Idirectory     specify $LOAD_PATH directory (may be used more than once)");
        out.println("  -l              enable line ending processing");
        out.println("  -n              assume 'while gets(); ... end' loop around your script");
        out.println("  -p              assume loop like -n but print line also like sed");
        out.println("  -rlibrary       require the library before executing your script");
        out.println("  -s              enable some switch parsing for switches after script name");
        out.println("  -S              look for the script using PATH environment variable");
        out.println("  -T[level=1]     turn on tainting checks");
        out.println("  -v, --verbose   print version number, then turn on verbose mode");
        out.println("  -w              turn warnings on for your script");
        out.println("  -W[level=2]     set warning level; 0=silence, 1=medium, 2=verbose");
        out.println("  -x[directory]   strip off text before #!ruby line and perhaps cd to directory");
        out.println("  --copyright     print the copyright");
        out.println("  --enable=feature[,...], --disable=feature[,...]");
        out.println("                  enable or disable features");
        out.println("  --external-encoding=encoding, --internal-encoding=encoding");
        out.println("                  specify the default external or internal character encoding");
        out.println("  --version       print the version");
        out.println("  --help          show this message, -h for short message");
        out.println();
        out.println("Features:");
        out.println("  gems            rubygems (default: enabled)");
        out.println("  did_you_mean    did_you_mean (default: enabled)");
        out.println("  rubyopt         RUBYOPT environment variable (default: enabled)");
        out.println("  frozen-string-literal");
        out.println("                  freeze all string literals (default: disabled)");
        out.println();
        out.println("TruffleRuby:");
        out.println("  -Xlog=severe,warning,performance,info,config,fine,finer,finest");
        out.println("                  set the TruffleRuby logging level");
        out.println("  -Xoptions       print available TruffleRuby options");
        out.println("  -Xname=value    set a TruffleRuby option (omit value to set to true)");
        out.println("  -J-option=value Translates to --jvm.option=value");
    }

    private static void printShortHelp(PrintStream out) {
        out.println("Usage: truffleruby [switches] [--] [programfile] [arguments]");
        out.println("  -0[octal]       specify record separator (\0, if no argument)");
        out.println("  -a              autosplit mode with -n or -p (splits $_ into $F)");
        out.println("  -c              check syntax only");
        out.println("  -Cdirectory     cd to directory before executing your script");
        out.println("  -d              set debugging flags (set $DEBUG to true)");
        out.println("  -e 'command'    one line of script. Several -e's allowed. Omit [programfile]");
        out.println("  -Eex[:in]       specify the default external and internal character encodings");
        out.println("  -Fpattern       split() pattern for autosplit (-a)");
        out.println("  -i[extension]   edit ARGV files in place (make backup if extension supplied)");
        out.println("  -Idirectory     specify $LOAD_PATH directory (may be used more than once)");
        out.println("  -l              enable line ending processing");
        out.println("  -n              assume 'while gets(); ... end' loop around your script");
        out.println("  -p              assume loop like -n but print line also like sed");
        out.println("  -rlibrary       require the library before executing your script");
        out.println("  -s              enable some switch parsing for switches after script name");
        out.println("  -S              look for the script using PATH environment variable");
        out.println("  -T[level=1]     turn on tainting checks");
        out.println("  -v              print version number, then turn on verbose mode");
        out.println("  -w              turn warnings on for your script");
        out.println("  -W[level=2]     set warning level; 0=silence, 1=medium, 2=verbose");
        out.println("  -x[directory]   strip off text before #!ruby line and perhaps cd to directory");
        out.println("  -h              show this message, --help for more info");
    }

}
