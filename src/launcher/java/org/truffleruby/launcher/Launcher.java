/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * Version: EPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004-2006 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2005 Kiel Hodges <jruby-devel@selfsosoft.com>
 * Copyright (C) 2005 Jason Voegele <jason@jvoegele.com>
 * Copyright (C) 2005 Tim Azzopardi <tim@tigerfive.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.launcher;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.truffleruby.launcher.options.CommandLineException;
import org.truffleruby.launcher.options.CommandLineOptions;
import org.truffleruby.launcher.options.CommandLineParser;
import org.truffleruby.launcher.options.ExecutionAction;
import org.truffleruby.launcher.options.OptionsCatalog;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;

public class Launcher {

    public static final boolean IS_AOT = Boolean.logicalOr(
            Boolean.getBoolean("com.oracle.graalvm.isaot"),
            Boolean.getBoolean("com.oracle.truffle.aot"));
    public static final String LANGUAGE_ID = "ruby";
    public static final String LANGUAGE_VERSION = "2.3.3";
    public static final String BOOT_SOURCE_NAME = "main_boot_source";
    public static final String RUBY_COPYRIGHT = "truffleruby - Copyright (c) 2013-2017 Oracle and/or its affiliates";

    // These system properties are used before outside the SDK option system

    private static final boolean METRICS_TIME =
            Boolean.getBoolean("truffleruby.metrics.time");
    private static final boolean METRICS_MEMORY_USED_ON_EXIT =
            Boolean.getBoolean("truffleruby.metrics.memory_used_on_exit");

    public static void main(boolean isGraal, String[] args) throws Exception {
        printTruffleTimeMetric("before-main");

        int exitCode = 0;

        try {
            final CommandLineOptions config = new CommandLineOptions();

            try {
                processArguments(config, args, true);
            } catch (CommandLineException commandLineException) {
                System.err.println("truffleruby: " + commandLineException.getMessage());
                if (commandLineException.isUsageError()) {
                    printHelp(System.err);
                }
                System.exit(1);
            }

            if (config.getOption(OptionsCatalog.SHOW_VERSION)) {
                System.out.println(getVersionString(isGraal));
            }

            if (config.getOption(OptionsCatalog.SHOW_COPYRIGHT)) {
                System.out.println(RUBY_COPYRIGHT);
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

            try (Context context = createContext(Context.newBuilder(), config)) {
                printTruffleTimeMetric("before-run");
                final Source source = Source.newBuilder(
                        LANGUAGE_ID,
                        // language=ruby
                        "Truffle::Boot.main",
                        BOOT_SOURCE_NAME).build();
                exitCode = context.eval(source).asInt();
                printTruffleTimeMetric("after-run");
            }
        } catch (PolyglotException e) {
            System.err.println("truffleruby: " + e.getMessage());
            e.printStackTrace();
            exitCode = 1;
        }

        printTruffleTimeMetric("after-main");
        printTruffleMemoryMetric();
        System.exit(exitCode);
    }

    public static Context createContext(Context.Builder builder, CommandLineOptions config) {
        /*
         * We turn off using the polyglot IO streams when running from our launcher, because they don't act like
         * normal file descriptors and this can cause problems in some advanced IO functionality, such as pipes and
         * blocking behaviour. We also turn off sync on stdio and so revert to Ruby's default logic for looking
         * at whether a file descriptor looks like a TTY for deciding whether to make it synchronous or not.
         */
        builder.option(OptionsCatalog.POLYGLOT_STDIO.getName(), Boolean.FALSE.toString());
        builder.option(OptionsCatalog.SYNC_STDIO.getName(), Boolean.FALSE.toString());

        builder.options(config.getOptions());
        builder.arguments(LANGUAGE_ID, config.getArguments());

        return builder.build();
    }

    public static void processArguments(
            CommandLineOptions config,
            String[] arguments,
            boolean parseHelpEtc) throws CommandLineException {

        config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.UNSET);

        new CommandLineParser(arguments, parseHelpEtc, config).processArguments();
        if (!config.getUnknownArguments().isEmpty()) {
            throw new CommandLineException("unknown option " + config.getUnknownArguments().get(0));
        }

        if (config.getOption(OptionsCatalog.READ_RUBYOPT)) {
            CommandLineParser.processEnvironmentVariable("RUBYOPT", config, true);
            CommandLineParser.processEnvironmentVariable("TRUFFLERUBYOPT", config, false);
        }

        if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.UNSET) {
            if (System.console() != null) {
                config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.PATH);
                config.setOption(OptionsCatalog.TO_EXECUTE, "irb");
            } else {
                config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.STDIN);
            }
        }
    }

    public static void printTruffleTimeMetric(String id) {
        if (METRICS_TIME) {
            final long millis = System.currentTimeMillis();
            System.err.printf("%s %d.%03d%n", id, millis / 1000, millis % 1000);
        }
    }

    private static void printTruffleMemoryMetric() {
        // Memory stats aren't available on AOT.
        if (!IS_AOT && METRICS_MEMORY_USED_ON_EXIT) {
            for (int n = 0; n < 10; n++) {
                System.gc();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.err.printf("allocated %d%n", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed());
        }
    }

    public static String getVersionString(boolean isGraal) {
        return String.format(
                "truffleruby %s, like ruby %s <%s %s %s> [%s-%s]",
                System.getProperty("graalvm.version", "unknown version"),
                LANGUAGE_VERSION,
                IS_AOT ? "AOT" : System.getProperty("java.vm.name", "unknown JVM"),
                IS_AOT ? "build" : System.getProperty(
                        "java.runtime.version",
                        System.getProperty("java.version", "unknown runtime version")),
                isGraal ? "with Graal" : "without Graal",
                BasicPlatform.getOSName(),
                BasicPlatform.getArchitecture()
        );
    }

    public static void printHelp(PrintStream out) {
        out.println("Usage: truffleruby [switches] [--] [programfile] [arguments]");
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
        out.println("Features:");
        out.println("  gems            rubygems (default: enabled)");
        out.println("  did_you_mean    did_you_mean (default: enabled)");
        out.println("  rubyopt         RUBYOPT environment variable (default: enabled)");
        out.println("  frozen-string-literal");
        out.println("                  freeze all string literals (default: disabled)");
        out.println("TruffleRuby switches:");
        out.println("  -Xlog=severe,warning,performance,info,config,fine,finer,finest");
        out.println("                  set the TruffleRuby logging level");
        out.println("  -Xoptions       print available TruffleRuby options");
        out.println("  -Xname=value    set a TruffleRuby option (omit value to set to true)");

        if (IS_AOT) {
            out.println("SVM switches:");
            out.println("  -XX:arg         pass arg to the SVM");
            out.println("  -Dname=value    set a system property");
        } else {
            out.println("JVM switches:");
            out.println("  -J-arg, -J:arg  pass arg to the JVM");
        }
    }

    public static void printShortHelp(PrintStream out) {
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
