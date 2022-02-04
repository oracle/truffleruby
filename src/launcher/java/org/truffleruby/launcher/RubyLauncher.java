/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.launcher;

import java.io.PrintStream;
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
    private String implementationName = null;

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
        System.out.println(TruffleRuby.getVersionString(getImplementationNameFromEngine()));
        System.out.println();
        printPolyglotVersions();
    }

    @Override
    protected List<String> preprocessArguments(List<String> args, Map<String, String> polyglotOptions) {
        // Set default options for the launcher which don't match the OptionKey's default.
        // These options can still be overridden if set explicitly.
        polyglotOptions.put(OptionsCatalog.EMBEDDED.getName(), "false");
        if (isAOT()) {
            final String launcher = ProcessProperties.getExecutableName();
            polyglotOptions.put(OptionsCatalog.LAUNCHER.getName(), launcher);
        }

        // TruffleRuby is never distributed without the GraalVM compiler, so this warning is not necessary
        polyglotOptions.put("engine.WarnInterpreterOnly", "false");

        config = new CommandLineOptions(args);

        try {
            config.executionAction = ExecutionAction.UNSET;

            final CommandLineParser argumentCommandLineParser = new CommandLineParser(args, config, true, false);
            argumentCommandLineParser.processArguments();

            if (config.readRubyOptEnv) {
                /* Calling processArguments() here will also add any unrecognized arguments such as
                 * --jvm/--native/--vm.* arguments and polyglot options to `config.getUnknownArguments()`, which will
                 * then be processed by AbstractLanguageLauncher and Launcher. If we are going to run Native, Launcher
                 * will apply VM options to the current process. If we are going to run on JVM, Launcher will collect
                 * them and pass them when execve()'ing to bin/java. Polyglot options are parsed by
                 * AbstractLanguageLauncher in the final process. */
                // Process RUBYOPT
                final List<String> rubyoptArgs = getArgsFromEnvVariable("RUBYOPT");
                new CommandLineParser(rubyoptArgs, config, false, true).processArguments();
                // Process TRUFFLERUBYOPT
                final List<String> trufflerubyoptArgs = getArgsFromEnvVariable("TRUFFLERUBYOPT");
                new CommandLineParser(trufflerubyoptArgs, config, false, false).processArguments();
            }

            // Process RUBYLIB, must be after arguments and RUBYOPT
            final List<String> rubyLibPaths = getPathListFromEnvVariable("RUBYLIB");
            for (String path : rubyLibPaths) {
                config.appendOptionValue(OptionsCatalog.LOAD_PATHS, path);
            }

            if (config.isGemOrBundle()) {
                // Apply options to run gem/bundle more efficiently
                if (isAOT()) {
                    config.getUnknownArguments().add(0, "--vm.Xmn1g");
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
        final int exitValue = runRubyMain(contextBuilder, config);
        Metrics.end();
        System.exit(exitValue);
    }

    /** This is only used to provide suggestions when an option is misspelled. It should only list options which are
     * parsed directly by the CommandLineParser. Normal SDK options are already handled by the common Launcher code. */
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

        if (config.isGemOrBundle() && getImplementationNameFromEngine().contains("Graal")) {
            // Apply options to run gem/bundle more efficiently
            contextBuilder.option("engine.Mode", "latency");
            if (Boolean.getBoolean("truffleruby.launcher.log")) {
                System.err.println("[ruby] CONFIG: detected gem or bundle command, using --engine.Mode=latency");
            }
        }

        contextBuilder.options(config.getOptions());

        contextBuilder.arguments(TruffleRuby.LANGUAGE_ID, config.getArguments());

        int result = runContext(contextBuilder, config);

        final boolean runTwice = config.getUnknownArguments().contains("--run-twice") ||
                config.getUnknownArguments().contains("--run-twice=true");
        if (runTwice) {
            final int secondResult = runContext(contextBuilder, config);
            if (secondResult != 0 && result == 0) {
                result = secondResult;
            }
        }

        return result;
    }

    private int runContext(Context.Builder builder, CommandLineOptions config) {
        try (Context context = builder.build()) {
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

            if (config.logProcessArguments) {
                Value logInfo = context.eval(
                        "ruby",
                        // language=ruby
                        "-> message { Truffle::Debug.log_info(message) }");
                String message = "new process: truffleruby " + String.join(" ", config.initialArguments);
                logInfo.executeVoid(message);
            }

            final Source source = Source.newBuilder(
                    TruffleRuby.LANGUAGE_ID,
                    // language=ruby
                    "-> argc, argv, kind, to_execute { Truffle::Boot.main(argc, argv, kind, to_execute) }",
                    TruffleRuby.BOOT_SOURCE_NAME).internal(true).buildLiteral();

            final int argc = getNativeArgc();
            final long argv = getNativeArgv();
            final String kind = config.executionAction.name();
            final int exitCode = context.eval(source).execute(argc, argv, kind, config.toExecute).asInt();
            Metrics.printTime("after-run");
            return exitCode;
        } catch (PolyglotException e) {
            if (e.isHostException()) { // GR-22071
                System.err.println("truffleruby: a host exception reached the top level:");
            } else {
                System.err.println(
                        "truffleruby: an exception escaped out of the interpreter - this is an implementation bug");
            }
            e.printStackTrace();
            return 1;
        }
    }

    private static List<String> getArgsFromEnvVariable(String name) {
        String value = System.getenv(name);
        if (value != null) {
            value = value.strip();
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

    private void printPreRunInformation(CommandLineOptions config) {
        if (config.showVersion) {
            System.out.println(TruffleRuby.getVersionString(getImplementationNameFromEngine()));
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

    private String getImplementationNameFromEngine() {
        if (implementationName == null) {
            try (Engine engine = Engine.create()) {
                implementationName = engine.getImplementationName();
            }
        }

        return implementationName;
    }

    // To update this, use:
    // ruby --help | ruby -e 'puts STDIN.readlines.map{|line|"out.println(#{line.chomp.inspect});"}'
    // replace ruby by truffleruby for the first line, and remove unsupported flags.
    // Also add an extra out.println(); before out.println("Features:"); and out.println("Warning categories:");
    // Remove the "Dump List:" section and jit-related lines.
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
        out.println("  -v              print the version number, then turn on verbose mode");
        out.println("  -w              turn warnings on for your script");
        out.println("  -W[level=2|:category]");
        out.println("                  set warning level; 0=silence, 1=medium, 2=verbose");
        out.println("  -x[directory]   strip off text before #!ruby line and perhaps cd to directory");
        out.println("  --copyright     print the copyright");
        out.println("  --enable={gems|rubyopt|...}[,...], --disable={gems|rubyopt|...}[,...]");
        out.println("                  enable or disable features. see below for available features");
        out.println("  --external-encoding=encoding, --internal-encoding=encoding");
        out.println("                  specify the default external or internal character encoding");
        out.println("  --verbose       turn on verbose mode and disable script from stdin");
        out.println("  --version       print the version number, then exit");
        out.println("  --help          show this message, -h for short message");
        out.println("  --backtrace-limit=num");
        out.println("                  limit the maximum length of backtrace");
        out.println();
        out.println("Features:");
        out.println("  gems            rubygems (default: enabled)");
        out.println("  did_you_mean    did_you_mean (default: enabled)");
        out.println("  rubyopt         RUBYOPT environment variable (default: enabled)");
        out.println("  frozen-string-literal");
        out.println("                  freeze all string literals (default: disabled)");
        out.println();
        out.println("Warning categories:");
        out.println("  deprecated      deprecated features");
        out.println("  experimental    experimental features");
    }

    // Same as above, but with "ruby -h"
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
        out.println("  -v              print the version number, then turn on verbose mode");
        out.println("  -w              turn warnings on for your script");
        out.println("  -W[level=2|:category]     set warning level; 0=silence, 1=medium, 2=verbose");
        out.println("  -x[directory]   strip off text before #!ruby line and perhaps cd to directory");
        out.println("  -h              show this message, --help for more info");
    }

}
