/*
 * Copyright (c) 2017, 2018, Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
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
import org.truffleruby.shared.options.DefaultExecutionAction;
import org.truffleruby.shared.options.ExecutionAction;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RubyLauncher extends AbstractLanguageLauncher {

    private CommandLineOptions config;

    public static void main(String[] args) {
        new RubyLauncher().launch(args);
    }

    static boolean isSulongAvailable() {
        try (Engine engine = Engine.create()) {
            return engine.getLanguages().containsKey(TruffleRuby.LLVM_ID);
        }
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
        System.out.println(TruffleRuby.getVersionString(isAOT()));
        System.out.println();
        printPolyglotVersions();
    }

    @Override
    protected List<String> preprocessArguments(List<String> args, Map<String, String> polyglotOptions) {
        config = new CommandLineOptions(polyglotOptions);

        try {
            config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.UNSET);

            final CommandLineParser argumentCommandLineParser = new CommandLineParser(args, config, true, false);
            argumentCommandLineParser.processArguments();

            if (config.getOption(OptionsCatalog.READ_RUBYOPT)) {
                // Process RUBYOPT
                final List<String> rubyoptArgs = getArgsFromEnvVariable("RUBYOPT");
                new CommandLineParser(rubyoptArgs, config, false, true).processArguments();
                // Process TRUFFLERUBYOPT
                final List<String> trufflerubyoptArgs = getArgsFromEnvVariable("TRUFFLERUBYOPT");
                new CommandLineParser(trufflerubyoptArgs, config, false, false).processArguments();

                if (isAOT()) {
                    /*
                     * Append options from ENV variables to args after the last interpreter option,
                     * which makes sure that maybeExec() processes the --(native|jvm)* options.
                     * These options are removed and are not passed to the new process if exec() is
                     * being called as these options need to be passed when starting the new VM
                     * process. The new process gets all arguments and options including those from
                     * ENV variables. To avoid processing options from ENV variables twice,
                     * READ_RUBYOPT is set to false. Only the native launcher can apply native and
                     * jvm options (it is too late for the running JVM to apply --jvm options),
                     * therefore this is not done on JVM.
                     */
                    final int index = argumentCommandLineParser.getLastInterpreterArgumentIndex();
                    args.add(index, "-Xread_rubyopt=false");
                    args.addAll(index + 1, rubyoptArgs);
                    args.addAll(index + 1 + rubyoptArgs.size(), trufflerubyoptArgs);
                }
            }

            // Process RUBYLIB, must be after arguments and RUBYOPT
            final List<String> rubyLibPaths = getPathListFromEnvVariable("RUBYLIB");
            for (String path : rubyLibPaths) {
                config.appendOptionValue(OptionsCatalog.LOAD_PATHS, path);
            }

            if (isAOT()) {
                final String launcher = setRubyLauncher();
                if (launcher != null) {
                    polyglotOptions.put(OptionsCatalog.LAUNCHER.getName(), launcher);
                }

                // In a native standalone distribution outside of GraalVM, we need to give the path to libsulong
                if (!isGraalVMAvailable() && isSulongAvailable()) {
                    final String rubyHome = new File(launcher).getParentFile().getParent();
                    final String libSulongPath = rubyHome + "/lib/cext/sulong-libs";

                    String libraryPath = System.getProperty("polyglot.llvm.libraryPath");
                    if (libraryPath == null || libraryPath.isEmpty()) {
                        libraryPath = libSulongPath;
                    } else {
                        libraryPath = libraryPath + ":" + libSulongPath;
                    }
                    polyglotOptions.put("llvm.libraryPath", libraryPath);
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
        Metrics.begin();
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
        final String description;
        if (argument.startsWith("--ruby.")) {
            description = argument + " (-X" + argument.substring(7) + ")";
        } else {
            description = argument;
        }

        throw abortInvalidArgument(argument,
                "truffleruby: invalid option " + description + "  (Use --help for usage instructions.)");
    }

    private int runRubyMain(Context.Builder contextBuilder, CommandLineOptions config) {
        if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.NONE) {
            return 0;
        }

        if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.UNSET &&
                config.getOption(OptionsCatalog.DEFAULT_EXECUTION_ACTION) == DefaultExecutionAction.NONE) {
            return 0;
        }

        try (Context context = createContext(contextBuilder, config)) {
            Metrics.printTime("before-run");
            final Source source;
            try {
                source = Source.newBuilder(
                        TruffleRuby.LANGUAGE_ID,
                        // language=ruby
                        "Truffle::Boot.main",
                        TruffleRuby.BOOT_SOURCE_NAME).internal(true).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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

    private Context createContext(Context.Builder builder, CommandLineOptions config) {
        if (!config.getOptions().containsKey(OptionsCatalog.EMBEDDED.getName())) {
            builder.option(OptionsCatalog.EMBEDDED.getName(), Boolean.FALSE.toString());
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

    private static List<String> getPathListFromEnvVariable(String name) {
        final String value = System.getenv(name);
        if (value != null && value.length() != 0) {
            return new ArrayList<>(Arrays.asList(value.split(":")));
        }
        return Collections.emptyList();
    }

    private String setRubyLauncher() {
        if (config.getOption(OptionsCatalog.LAUNCHER).isEmpty()) {
            final String launcher = (String) Compiler.command(
                    new Object[]{ "com.oracle.svm.core.posix.GetExecutableName" });
            config.setOption(OptionsCatalog.LAUNCHER, launcher);
            return launcher;
        }
        return null;
    }

    private static void printPreRunInformation(CommandLineOptions config) {
        if (config.getOption(OptionsCatalog.SHOW_VERSION)) {
            System.out.println(TruffleRuby.getVersionString(isAOT()));
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

    // To update this, use:
    // ruby -h | ruby -e 'puts STDIN.readlines.map{|line|"out.println(#{line.chomp.inspect});"}'
    // and replace ruby by truffleruby for the first line.
    // Also add an extra out.println(); before out.println("Features:");
    private static void printHelp(PrintStream out) {
        out.println("Usage: truffleruby [switches] [--] [programfile] [arguments]");
        out.println("  -0[octal]       specify record separator (\\0, if no argument)");
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
        // Extra output for TruffleRuby
        out.println();
        out.println("TruffleRuby:");
        out.println("  -Xlog=SEVERE,WARNING,INFO,CONFIG,FINE,FINER,FINEST");
        out.println("                  set the TruffleRuby logging level");
        out.println("  -Xoptions       print available TruffleRuby options");
        out.println("  -Xname=value    set a TruffleRuby option (omit value to set to true)");
        out.println("  -J-option=value Translates to --jvm.option=value");
    }

    private static void printShortHelp(PrintStream out) {
        out.println("Usage: truffleruby [switches] [--] [programfile] [arguments]");
        out.println("  -0[octal]       specify record separator (\\0, if no argument)");
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
