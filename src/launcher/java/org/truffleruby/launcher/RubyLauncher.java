/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.launcher;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graalvm.launcher.AbstractLanguageLauncher;
import org.graalvm.nativeimage.ProcessProperties;
import org.graalvm.options.OptionCategory;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.OptionsCatalog;

public class RubyLauncher extends AbstractLanguageLauncher {

    private CommandLineOptions config;

    public static void main(String[] args) {
        new RubyLauncher().launch(args);
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
        System.out.println(TruffleRuby.getVersionString(getImplementationNameFromEngine(), isAOT()));
        System.out.println();
        printPolyglotVersions();
    }

    @Override
    protected List<String> preprocessArguments(List<String> args, Map<String, String> polyglotOptions) {
        config = new CommandLineOptions(polyglotOptions);

        try {
            config.executionAction = ExecutionAction.UNSET;

            final CommandLineParser argumentCommandLineParser = new CommandLineParser(args, config, true, false);
            argumentCommandLineParser.processArguments();

            if (config.readRubyOptEnv) {
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
                    args.add(index, "--disable=rubyopt");
                    args.addAll(index + 1, rubyoptArgs);
                    args.addAll(index + 1 + rubyoptArgs.size(), trufflerubyoptArgs);
                }
            }

            // Process RUBYLIB, must be after arguments and RUBYOPT
            final List<String> rubyLibPaths = getPathListFromEnvVariable("RUBYLIB");
            for (String path : rubyLibPaths) {
                config.appendOptionValue(OptionsCatalog.LOAD_PATHS, path);
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

    /**
     * This is only used to provide suggestions when an option is misspelled.
     * It should only list options which are parsed directly by the CommandLineParser.
     * Normal SDK options are already handled by the common Launcher code.
     */
    @Override
    protected void collectArguments(Set<String> options) {
        options.addAll(Arrays.asList(
                "-0",
                "-a",
                "-c",
                "-C",
                "-d",
                "-e",
                "-E",
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
                "-v",
                "-w",
                "-W",
                "-x",
                "--copyright",
                "--disable",
                "--enable",
                "--encoding",
                "--version"));
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
        throw abortInvalidArgument(
                argument,
                "truffleruby: invalid option " + argument + "  (Use --help for usage instructions.)");
    }

    private int runRubyMain(Context.Builder contextBuilder, CommandLineOptions config) {
        if (config.executionAction == ExecutionAction.UNSET) {
            switch (config.defaultExecutionAction) {
                case NONE:
                    return 0;
                case STDIN:
                    config.executionAction = ExecutionAction.STDIN;
                    break;
                case IRB:
                    config.executionAction = ExecutionAction.PATH;
                    if (System.console() != null) {
                        System.err.println(
                                "[ruby] WARNING: truffleruby starts IRB when stdin is a TTY instead of reading from stdin, use '-' to read from stdin");
                        config.executionAction = ExecutionAction.PATH;
                        config.toExecute = "irb";
                    } else {
                        config.executionAction = ExecutionAction.STDIN;
                    }
                    break;
            }
        }

        if (config.executionAction == ExecutionAction.NONE) {
            return 0;
        }

        try (Context context = createContext(contextBuilder, config)) {
            Metrics.printTime("before-run");

            if (config.executionAction == ExecutionAction.PATH) {
                final Source source = Source.newBuilder(
                        TruffleRuby.LANGUAGE_ID,
                        // language=ruby
                        "-> name { Truffle::Boot.find_s_file(name) }",
                        TruffleRuby.BOOT_SOURCE_NAME).internal(true).buildLiteral();

                config.executionAction = ExecutionAction.FILE;
                final Value file = context.eval(source).execute(config.toExecute);
                if (file.isString()) {
                    config.toExecute = file.asString();
                } else {
                    System.err
                            .println("truffleruby: No such file or directory -- " + config.toExecute + " (LoadError)");
                    return 1;
                }
            }

            final Source source = Source.newBuilder(
                    TruffleRuby.LANGUAGE_ID,
                    // language=ruby
                    "-> kind, to_execute { Truffle::Boot.main(kind, to_execute) }",
                    TruffleRuby.BOOT_SOURCE_NAME).internal(true).buildLiteral();

            final String kind = config.executionAction.name();
            final int exitCode = context.eval(source).execute(kind, config.toExecute).asInt();
            Metrics.printTime("after-run");
            return exitCode;
        } catch (PolyglotException e) {
            e.printStackTrace();
            return 1;
        }
    }

    private static void debugPreInitialization() {
        if (!isAOT() && TruffleRuby.PRE_INITIALIZE_CONTEXTS) {
            // This is only run when saying that you are pre-initialising a context but actually you're not running in the image generator

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
        if (isAOT() && !config.isSetInPolyglotOptions(OptionsCatalog.LAUNCHER.getName())) {
            final String launcher = ProcessProperties.getExecutableName();
            builder.option(OptionsCatalog.LAUNCHER.getName(), launcher);
        }

        if (!config.isSetInPolyglotOptions(OptionsCatalog.EMBEDDED.getName())) {
            builder.option(OptionsCatalog.EMBEDDED.getName(), "false");
        }

        builder.options(config.getOptions());

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

    private static void printPreRunInformation(CommandLineOptions config) {
        if (config.showVersion) {
            System.out.println(TruffleRuby.getVersionString(getImplementationNameFromEngine(), isAOT()));
        }

        if (config.showCopyright) {
            System.out.println(TruffleRuby.RUBY_COPYRIGHT);
        }

        switch (config.showHelp) {
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

    private static String getImplementationNameFromEngine() {
        try (Engine engine = Engine.create()) {
            return engine.getImplementationName();
        }
    }

    // To update this, use:
    // ruby --help | ruby -e 'puts STDIN.readlines.map{|line|"out.println(#{line.chomp.inspect});"}'
    // replace ruby by truffleruby for the first line, and remove unsupported flags.
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
        out.println("  -v              print the version number, then turn on verbose mode");
        out.println("  -w              turn warnings on for your script");
        out.println("  -W[level=2]     set warning level; 0=silence, 1=medium, 2=verbose");
        out.println("  -x[directory]   strip off text before #!ruby line and perhaps cd to directory");
        out.println("  --copyright     print the copyright");
        out.println("  --enable={gems|rubyopt|...}[,...], --disable={gems|rubyopt|...}[,...]");
        out.println("                  enable or disable features. see below for available features");
        out.println("  --external-encoding=encoding, --internal-encoding=encoding");
        out.println("                  specify the default external or internal character encoding");
        out.println("  --verbose       turn on verbose mode and disable script from stdin");
        out.println("  --version       print the version number, then exit");
        out.println("  --help          show this message, -h for short message");
        out.println();
        out.println("Features:");
        out.println("  gems            rubygems (default: enabled)");
        out.println("  did_you_mean    did_you_mean (default: enabled)");
        out.println("  rubyopt         RUBYOPT environment variable (default: enabled)");
        out.println("  frozen-string-literal");
        out.println("                  freeze all string literals (default: disabled)");
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
