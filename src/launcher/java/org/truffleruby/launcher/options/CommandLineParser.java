/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
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
 * Copyright (C) 2007-2011 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2009 Joseph LaFata <joe@quibb.org>
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
package org.truffleruby.launcher.options;

import org.truffleruby.shared.RubyLogger;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.CommandLineOptions;
import org.truffleruby.shared.options.DefaultExecutionAction;
import org.truffleruby.shared.options.ExecutionAction;
import org.truffleruby.shared.options.OptionDescription;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.shared.options.ShowHelp;
import org.truffleruby.shared.options.StringArrayOptionDescription;
import org.truffleruby.shared.options.Verbosity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class CommandLineParser {

    private final List<String> arguments;
    private int argumentIndex;
    private boolean processArgv;
    private final boolean rubyOpts;
    final CommandLineOptions config;
    private int lastInterpreterArgumentIndex;
    private int characterIndex;

    public CommandLineParser(
            List<String> arguments,
            CommandLineOptions config,
            boolean processArgv,
            boolean rubyOpts) {

        this.argumentIndex = 0;
        this.characterIndex = 0;
        this.lastInterpreterArgumentIndex = -1;
        this.config = config;
        this.processArgv = processArgv;
        this.rubyOpts = rubyOpts;
        this.arguments = Objects.requireNonNull(arguments);
    }

    public void processArguments() throws CommandLineException {
        while (argumentIndex < arguments.size() && isInterpreterArgument(getCurrentArgument())) {
            processArgument();
            argumentIndex++;
        }

        if (!endOfInterpreterArguments()) {
            lastInterpreterArgumentIndex = argumentIndex;
        }
        assert lastInterpreterArgumentIndex >= 0;

        if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.UNSET) {
            if (argumentIndex < arguments.size()) {
                config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.FILE);
                //consume the file name
                config.setOption(OptionsCatalog.TO_EXECUTE, getCurrentArgument());
                argumentIndex++;
            }
        }

        if (processArgv) {
            processArgv();
        }
    }

    public int getLastInterpreterArgumentIndex() {
        return lastInterpreterArgumentIndex;
    }

    private boolean endOfInterpreterArguments() {
        return lastInterpreterArgumentIndex != -1;
    }

    private void processArgv() {
        ArrayList<String> arglist = new ArrayList<>();
        for (; argumentIndex < arguments.size(); argumentIndex++) {
            String arg = getCurrentArgument();
            boolean argvGlobalsOn = config.getOption(OptionsCatalog.ARGV_GLOBALS);
            if (argvGlobalsOn && arg.startsWith("-")) {
                arg = arg.substring(1);
                int split = arg.indexOf('=');
                final String key;
                final String value;
                if (split > 0) {
                    key = arg.substring(0, split);
                    value = arg.substring(split + 1);
                } else {
                    key = arg;
                    value = null;
                }

                // Switches without values are stored separately in ARGV_GLOBAL_FLAGS. Otherwise it would not be
                // possible to determine if the value is suppose to be `true` or `"true"`.
                final StringArrayOptionDescription optionDescription =
                        value != null ? OptionsCatalog.ARGV_GLOBAL_VALUES : OptionsCatalog.ARGV_GLOBAL_FLAGS;
                // replace dashes with underscores make it a valid global variable name
                config.appendOptionValue(optionDescription, key.replace('-', '_'));
                if (value != null) {
                    config.appendOptionValue(optionDescription, value);
                }
            } else {
                argvGlobalsOn = false;
                arglist.add(arg);
            }
        }
        // Remaining arguments are for the script itself
        config.setArguments(arglist.toArray(new String[arglist.size()]));
    }

    private boolean isInterpreterArgument(String argument) {
        return argument.length() > 0 && (argument.charAt(0) == '-' || argument.charAt(0) == '+') && !endOfInterpreterArguments();
    }

    private String getArgumentError(String additionalError) {
        return "invalid argument\n" + additionalError + "\n";
    }

    private void processArgument() throws CommandLineException {
        String argument = getCurrentArgument();

        if (argument.length() == 1) {
            // sole "-" means read from stdin and pass remaining args as ARGV
            lastInterpreterArgumentIndex = argumentIndex;

            if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.UNSET) {
                config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.STDIN);
            } else {
                // if other action is set then ignore '-', just threat it as a first script argument,
                // and stop option parsing
            }

            return;
        }

        FOR:
        for (characterIndex = 1; characterIndex < argument.length(); characterIndex++) {
            switch (argument.charAt(characterIndex)) {
                case '0':
                    {
                        disallowedInRubyOpts(argument);
                        String temp = grabOptionalValue();
                        if (null == temp) {
                            //config.setRecordSeparator("\u0000");
                        } else if (temp.equals("0")) {
                            //config.setRecordSeparator("\n\n");
                        } else if (temp.equals("777")) {
                            //config.setRecordSeparator("\uffff"); // Specify something that can't separate
                        } else {
                            try {
                                Integer.parseInt(temp, 8);
                                //config.setRecordSeparator(String.valueOf((char) val));
                            } catch (Exception e) {
                                throw new CommandLineException(getArgumentError(" -0 must be followed by either 0, 777, or a valid octal value"), true);
                            }
                        }
                        //break FOR;
                        throw notImplemented("-0");
                    }
                case 'a':
                    disallowedInRubyOpts(argument);
                    throw notImplemented("-a");
                case 'c':
                    disallowedInRubyOpts(argument);
                    config.setOption(OptionsCatalog.SYNTAX_CHECK, true);
                    break;
                case 'C':
                    disallowedInRubyOpts(argument);
                    final String dir = grabValue(getArgumentError(" -C must be followed by a directory expression"));
                    config.setOption(OptionsCatalog.WORKING_DIRECTORY, dir);
                    break;
                case 'd':
                    config.setOption(OptionsCatalog.DEBUG, true);
                    config.setOption(OptionsCatalog.VERBOSITY, Verbosity.TRUE);
                    break;
                case 'e':
                    disallowedInRubyOpts(argument);
                    final String nextArgument = grabValue(getArgumentError(" -e must be followed by an expression to report"));

                    final ExecutionAction currentExecutionAction = config.getOption(OptionsCatalog.EXECUTION_ACTION);
                    if (currentExecutionAction == ExecutionAction.UNSET || currentExecutionAction == ExecutionAction.INLINE) {
                        config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.INLINE);
                        config.appendOptionValue(OptionsCatalog.TO_EXECUTE, nextArgument + "\n");
                    } else {
                        // ignore option
                    }

                    break FOR;
                case 'E':
                    processEncodingOption(grabValue(getArgumentError("unknown encoding name")));
                    break FOR;
                case 'F':
                    disallowedInRubyOpts(argument);
                    throw notImplemented("-F");
                case 'h':
                    disallowedInRubyOpts(argument);
                    config.setOption(OptionsCatalog.SHOW_HELP, ShowHelp.SHORT);
                    // cancel other execution actions
                    config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.NONE);
                    break;
                case 'i':
                    disallowedInRubyOpts(argument);
                    throw notImplemented("-i");
                case 'I':
                    String s = grabValue(getArgumentError("-I must be followed by a directory name to add to lib path"));
                    for (String path : s.split(File.pathSeparator)) {
                        if (path.startsWith("~" + File.separator)) {
                            path = System.getProperty("user.home") + File.separator + path.substring(2);
                        }
                        config.appendOptionValue(OptionsCatalog.LOAD_PATHS, path);
                    }
                    break FOR;
                case 'y':
                    disallowedInRubyOpts(argument);
                    RubyLogger.LOGGER.warning("the -y switch is silently ignored as it is an internal development tool");
                    break FOR;
                case 'J':
                    String javaOption = grabOptionalValue();
                    final boolean isClasspath = javaOption.equals("-cp") || javaOption.equals("-classpath");

                    final String jvmOption = "--jvm." + javaOption.substring(1);
                    if (isClasspath) {
                        argumentIndex++;
                        final String cpValue = getCurrentArgument();
                        arguments.remove(argumentIndex);
                        argumentIndex--;
                        arguments.set(argumentIndex, jvmOption + "=" + cpValue);
                    } else {
                        arguments.set(argumentIndex, jvmOption);
                    }

                    break FOR;
                case 'K':
                    throw notImplemented("-K");
                case 'l':
                    disallowedInRubyOpts(argument);
                    throw notImplemented("-l");
                case 'n':
                    disallowedInRubyOpts(argument);
                    throw notImplemented("-n");
                case 'p':
                    disallowedInRubyOpts(argument);
                    throw notImplemented("-p");
                case 'r':
                    final String library = grabValue(getArgumentError("-r must be followed by a package to require"));
                    config.appendOptionValue(OptionsCatalog.REQUIRED_LIBRARIES, library);
                    break FOR;
                case 's':
                    disallowedInRubyOpts(argument);
                    config.setOption(OptionsCatalog.ARGV_GLOBALS, true);
                    break;
                case 'G':
                    throw notImplemented("-G");
                case 'S':
                    disallowedInRubyOpts(argument);
                    lastInterpreterArgumentIndex = argumentIndex; // set before grabValue increments index

                    String scriptName = grabValue("provide a bin script to execute");

                    if (config.getOption(OptionsCatalog.EXECUTION_ACTION) == ExecutionAction.UNSET) {
                        config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.PATH);
                        config.setOption(OptionsCatalog.TO_EXECUTE, scriptName);
                    } else {
                        // ignore the option
                    }

                    break FOR;
                case 'T':
                    throw notImplemented("-T");
                case 'U':
                    config.setOption(OptionsCatalog.INTERNAL_ENCODING, "UTF-8");
                    break;
                case 'v':
                    config.setOption(OptionsCatalog.VERBOSITY, Verbosity.TRUE);
                    config.setOption(OptionsCatalog.SHOW_VERSION, true);
                    config.setOption(OptionsCatalog.DEFAULT_EXECUTION_ACTION, DefaultExecutionAction.NONE);
                    break;
                case 'w':
                    config.setOption(OptionsCatalog.VERBOSITY, Verbosity.TRUE);
                    break;
                case 'W':
                    {
                        String temp = grabOptionalValue();
                        if (temp == null) {
                            config.setOption(OptionsCatalog.VERBOSITY, Verbosity.TRUE);
                        } else {
                            switch (temp) {
                                case "0":
                                    config.setOption(OptionsCatalog.VERBOSITY, Verbosity.NIL);
                                    break;
                                case "1":
                                    config.setOption(OptionsCatalog.VERBOSITY, Verbosity.FALSE);
                                    break;
                                case "2":
                                    config.setOption(OptionsCatalog.VERBOSITY, Verbosity.TRUE);
                                    break;
                                default:
                                    throw new CommandLineException(getArgumentError(" -W must be followed by either 0, 1, 2 or nothing"), true);
                            }
                        }
                        break FOR;
                    }
                case 'x':
                    disallowedInRubyOpts(argument);
                    config.setOption(OptionsCatalog.IGNORE_LINES_BEFORE_RUBY_SHEBANG, true);
                    String directory = grabOptionalValue();
                    if (directory != null) {
                        throw notImplemented("-x with directory");
                    }
                    break FOR;
                case 'X':
                    disallowedInRubyOpts(argument);
                    final String extendedOption = grabValue("-X must be followed by an option");
                    if (new File(extendedOption).isDirectory()) {
                        RubyLogger.LOGGER.warning("the -X option supplied also appears to be a directory name - did you intend to use -X like -C?");
                    }
                    if (extendedOption.equals("options")) {
                        System.out.println("TruffleRuby options and their default values:");
                        for (OptionDescription<?> option : OptionsCatalog.allDescriptions()) {
                            assert option.getName().startsWith(TruffleRuby.LANGUAGE_ID);
                            final String xName = option.getName().substring(TruffleRuby.LANGUAGE_ID.length() + 1);
                            final String nameValue = String.format("-X%s=%s", xName, option.valueToString(option.getDefaultValue()));
                            System.out.printf("  %s%" + (50 - nameValue.length()) + "s# %s%n", nameValue, "", option.getDescription());
                        }
                        // cancel other execution actions
                        config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.NONE);
                    } else if (extendedOption.startsWith("log=")) {
                        final String levelString = extendedOption.substring("log=".length());

                        final Level level;

                        if (levelString.equals("PERFORMANCE")) {
                            level = RubyLogger.PERFORMANCE;
                        } else {
                            level = Level.parse(levelString.toUpperCase());
                        }

                        RubyLogger.LOGGER.setLevel(level);
                    } else {
                        // Turn extra options into polyglot options and let
                        // org.graalvm.launcher.Launcher.parsePolyglotOption
                        // parse it
                        config.getUnknownArguments().add("--ruby." + extendedOption);
                    }
                    break FOR;
                case '-':
                    if (argument.equals("--copyright")) {
                        disallowedInRubyOpts(argument);
                        config.setOption(OptionsCatalog.SHOW_COPYRIGHT, true);
                        // cancel other execution actions
                        config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.NONE);
                        break FOR;
                    } else if (argument.equals("--debug")) {
                        throw notImplemented("--debug");
                    } else if (argument.equals("--yydebug")) {
                        disallowedInRubyOpts(argument);
                        RubyLogger.LOGGER.warning("the --yydebug switch is silently ignored as it is an internal development tool");
                        break FOR;
                    } else if (rubyOpts && argument.equals("--help")) {
                        disallowedInRubyOpts(argument);
                        break FOR;
                    } else if (argument.equals("--version")) {
                        disallowedInRubyOpts(argument);
                        config.setOption(OptionsCatalog.SHOW_VERSION, true);
                        // cancel other execution actions
                        config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.NONE);
                        break FOR;
                    } else if (argument.startsWith("--profile")) {
                        throw notImplemented("--profile");
                    } else if (argument.equals("--debug-frozen-string-literal")) {
                        throw notImplemented("--debug-frozen-string-literal");
                    } else if (argument.startsWith("--disable")) {
                        final int len = argument.length();
                        if (len == "--disable".length()) {
                            characterIndex = len;
                            String feature = grabValue(getArgumentError("missing argument for --disable"), false);
                            argument = "--disable=" + feature;
                        }
                        for (String disable : valueListFor(argument, "disable")) {
                            enableDisableFeature(disable, false);
                        }
                        break FOR;
                    } else if (argument.startsWith("--enable")) {
                        final int len = argument.length();
                        if (len == "--enable".length()) {
                            characterIndex = len;
                            String feature = grabValue(getArgumentError("missing argument for --enable"), false);
                            argument = "--enable=" + feature;
                        }
                        for (String enable : valueListFor(argument, "enable")) {
                            enableDisableFeature(enable, true);
                        }
                        break FOR;
                    } else if (argument.equals("--gemfile")) {
                        throw notImplemented("--gemfile");
                    } else if (argument.equals("--verbose")) {
                        config.setOption(OptionsCatalog.VERBOSITY, Verbosity.TRUE);
                        break FOR;
                    } else if (argument.startsWith("--dump=")) {
                        RubyLogger.LOGGER.warning("the --dump= switch is silently ignored as it is an internal development tool");
                        break FOR;
                    } else {
                        if (argument.equals("--")) {
                            // ruby interpreter compatibilty
                            // Usage: ruby [switches] [--] [programfile] [arguments])
                            lastInterpreterArgumentIndex = argumentIndex;
                            break;
                        }
                    }
                    config.getUnknownArguments().add(argument);
                    break FOR;
                default:
                    config.getUnknownArguments().add(argument);
                    break FOR;
            }
        }
    }

    private void enableDisableFeature(String name, boolean enable) {
        final BiConsumer<CommandLineParser, Boolean> feature = FEATURES.get(name);

        if (feature == null) {
            RubyLogger.LOGGER.warning("warning: unknown argument for --" + (enable ? "enable" : "disable") + ": `" + name + "'");
        } else {
            feature.accept(this, enable);
        }
    }

    private static String[] valueListFor(String argument, String key) throws CommandLineException {
        int length = key.length() + 3; // 3 is from -- and = (e.g. --disable=)
        String[] values = argument.substring(length).split(",");

        if (values.length == 0) {
            errorMissingEquals(key);
        }

        return values;
    }

    private void disallowedInRubyOpts(CharSequence option) throws CommandLineException {
        if (rubyOpts) {
            throw new CommandLineException("invalid switch in RUBYOPT: " + option + " (RuntimeError)");
        }
    }

    private static void errorMissingEquals(String label) throws CommandLineException {
        throw new CommandLineException("missing argument for --" + label + "\n", true);
    }

    /**
     * Split string into (limited) sub-parts.
     * @param str the string
     * @param sep the separator
     * @param lim has same effect as with {@link String#split(String, int)}
     */
    private static List<String> split(final String str, final char sep, final int lim) {
        final int len = str.length();
        if ( len == 0 ) {
            return Collections.singletonList(str);
        }

        final ArrayList<String> result = new ArrayList<>(lim <= 0 ? 8 : lim);

        int e; int s = 0; int count = 0;
        while ( (e = str.indexOf(sep, s)) != -1 ) {
            if ( lim == ++count ) { // limited (lim > 0) case
                result.add(str.substring(s));
                return result;
            }
            result.add(str.substring(s, e));
            s = e + 1;
        }
        if ( s < len || ( s == len && lim > 0 ) ) {
            result.add(str.substring(s));
        }

        return result;
    }

    private void processEncodingOption(String value) throws CommandLineException {
        List<String> encodings = split(value, ':', 3);

        if (encodings.size() >= 3) {
            throw new CommandLineException("extra argument for -E: " + encodings.get(2));
        }

        if (encodings.size() >= 2) {
            config.setOption(OptionsCatalog.INTERNAL_ENCODING, encodings.get(1));
        }

        if (encodings.size() >= 1) {
            config.setOption(OptionsCatalog.EXTERNAL_ENCODING, encodings.get(0));
        }
    }

    private String grabValue(String errorMessage) throws CommandLineException {
        return grabValue(errorMessage, true);
    }

    private String grabValue(String errorMessage, boolean usageError) throws CommandLineException {
        String optValue = grabOptionalValue();
        if (optValue != null) {
            return optValue;
        }
        argumentIndex++;
        if (argumentIndex < arguments.size()) {
            return getCurrentArgument();
        }
        throw new CommandLineException(errorMessage, usageError);
    }

    private String grabOptionalValue() {
        characterIndex++;
        String argValue = getCurrentArgument();
        if (characterIndex < argValue.length()) {
            return argValue.substring(characterIndex);
        }
        return null;
    }

    private String getCurrentArgument() {
        return arguments.get(argumentIndex);
    }

    private CommandLineException notImplemented(String option) {
        return new CommandLineException(String.format("the %s option is not implemented", option));
    }

    private static final Map<String, BiConsumer<CommandLineParser, Boolean>> FEATURES = new HashMap<>();

    static {
        FEATURES.put("all", (processor, enable) -> {
            for (Map.Entry<String, BiConsumer<CommandLineParser, Boolean>> feature : FEATURES.entrySet()) {
                if (!feature.getKey().equals("all")) {
                    feature.getValue().accept(processor, enable);
                }
            }
        });

        FEATURES.put("did_you_mean",
                (processor, enable) -> processor.config.setOption(OptionsCatalog.DID_YOU_MEAN, enable));

        FEATURES.put("did-you-mean",
            FEATURES.get("did_you_mean"));

        FEATURES.put("gem",
                (processor, enable) -> processor.config.setOption(OptionsCatalog.RUBYGEMS, enable));

        FEATURES.put("gems",
                FEATURES.get("gem"));

        FEATURES.put("frozen-string-literal",
                (processor, enable) -> processor.config.setOption(
                        OptionsCatalog.FROZEN_STRING_LITERALS,
                        enable));

        FEATURES.put("frozen_string_literal",
                FEATURES.get("frozen-string-literal"));

        FEATURES.put("rubyopt",
                (processor, enable) -> processor.config.setOption(OptionsCatalog.READ_RUBYOPT, enable));
    }
}
