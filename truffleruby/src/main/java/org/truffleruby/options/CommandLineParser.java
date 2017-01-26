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
package org.truffleruby.options;

import com.oracle.truffle.api.TruffleOptions;
import jnr.posix.POSIXFactory;
import org.truffleruby.Log;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.string.KCode;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.platform.NativePlatformFactory;
import org.truffleruby.platform.Platform;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class CommandLineParser {

    private static final class Argument {
        final String originalValue;
        private String dashedValue;
        Argument(String value, boolean dashed) {
            this.originalValue = value;
            this.dashedValue = dashed ? null : value;
        }

        final String getDashedValue() {
            String dashedValue = this.dashedValue;
            if ( dashedValue == null ) {
                final String value = originalValue;
                dashedValue = ! value.startsWith("-") ? ('-' + value) : value;
                this.dashedValue = dashedValue;
            }
            return dashedValue;
        }

        @Override
        public String toString() {
            return getDashedValue();
        }
    }

    private final List<Argument> arguments;
    private int argumentIndex = 0;
    private boolean processArgv;
    private final boolean rubyOpts;
    final RubyInstanceConfig config;
    private boolean endOfArguments = false;
    private int characterIndex = 0;

    public CommandLineParser(String[] arguments, RubyInstanceConfig config) {
        this(arguments, true, false, false, config);
    }

    public CommandLineParser(String[] arguments, boolean processArgv, boolean dashed, boolean rubyOpts, RubyInstanceConfig config) {
        this.config = config;
        if (arguments != null && arguments.length > 0) {
            this.arguments = new ArrayList<>(arguments.length);
            for (String argument : arguments) {
                this.arguments.add(new Argument(argument, dashed));
            }
        }
        else {
            this.arguments = new ArrayList<>(0);
        }
        this.processArgv = processArgv;
        this.rubyOpts = rubyOpts;
    }

    public void processArguments() {
        processArguments(true);
    }

    public void processArguments(boolean inline) {
        while (argumentIndex < arguments.size() && isInterpreterArgument(arguments.get(argumentIndex).originalValue)) {
            processArgument();
            argumentIndex++;
        }
        if (inline && !config.isInlineScript() && config.getScriptFileName() == null && !config.isForceStdin()) {
            if (argumentIndex < arguments.size()) {
                config.setScriptFileName(arguments.get(argumentIndex).originalValue); //consume the file name
                argumentIndex++;
            }
        }
        if (processArgv) {
            processArgv();
        }
    }

    private void processArgv() {
        ArrayList<String> arglist = new ArrayList<>();
        for (; argumentIndex < arguments.size(); argumentIndex++) {
            String arg = arguments.get(argumentIndex).originalValue;
            if (config.isArgvGlobalsOn() && arg.startsWith("-")) {
                arg = arg.substring(1);
                int split = arg.indexOf('=');
                if (split > 0) {
                    final String key = arg.substring(0, split);
                    final String val = arg.substring(split + 1);
                    // argv globals getService their dashes replaced with underscores
                    String globalName = key.replace('-', '_');
                    config.getOptionGlobals().put(globalName, val);
                } else {
                    config.getOptionGlobals().put(arg, null);
                }
            } else {
                config.setArgvGlobalsOn(false);
                arglist.add(arg);
            }
        }
        // Remaining arguments are for the script itself
        arglist.addAll(Arrays.asList(OptionsCatalog.ARGUMENTS.checkValue(config.getOptions().get(OptionsCatalog.ARGUMENTS))));
        config.getOptions().put(OptionsCatalog.ARGUMENTS.getName(), arglist.toArray(new String[arglist.size()]));
    }

    private boolean isInterpreterArgument(String argument) {
        return argument.length() > 0 && (argument.charAt(0) == '-' || argument.charAt(0) == '+') && !endOfArguments;
    }

    private String getArgumentError(String additionalError) {
        return "jruby: invalid argument\n" + additionalError + "\n";
    }

    private void processArgument() {
        String argument = arguments.get(argumentIndex).getDashedValue();

        if (argument.length() == 1) {
            // sole "-" means read from stdin and pass remaining args as ARGV
            endOfArguments = true;
            config.setForceStdin(true);
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
                            throw new UnsupportedOperationException();
                        } else if (temp.equals("0")) {
                            //config.setRecordSeparator("\n\n");
                            throw new UnsupportedOperationException();
                        } else if (temp.equals("777")) {
                            //config.setRecordSeparator("\uffff"); // Specify something that can't separate
                            throw new UnsupportedOperationException();
                        } else {
                            try {
                                Integer.parseInt(temp, 8);
                                //config.setRecordSeparator(String.valueOf((char) val));
                                throw new UnsupportedOperationException();
                            } catch (Exception e) {
                                MainExitException mee = new MainExitException(1, getArgumentError(" -0 must be followed by either 0, 777, or a valid octal value"));
                                mee.setUsageError(true);
                                throw mee;
                            }
                        }
                        //break FOR;
                    }
                case 'a':
                    disallowedInRubyOpts(argument);
                    config.setSplit(true);
                    break;
                case 'c':
                    disallowedInRubyOpts(argument);
                    config.setShouldCheckSyntax(true);
                    break;
                case 'C':
                    disallowedInRubyOpts(argument);
                    final String dir = grabValue(getArgumentError(" -C must be followed by a directory expression"));
                    // We haven't created the context yet, so we don't have a POSIX from there
                    POSIXFactory.getNativePOSIX(null).chdir(dir);
                    break;
                case 'd':
                    config.getOptions().put(OptionsCatalog.DEBUG.getName(), true);
                    config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), true);
                    break;
                case 'e':
                    disallowedInRubyOpts(argument);
                    config.getInlineScript().append(grabValue(getArgumentError(" -e must be followed by an expression to report")));
                    config.getInlineScript().append('\n');
                    config.setHasInlineScript(true);
                    break FOR;
                case 'E':
                    processEncodingOption(grabValue(getArgumentError("unknown encoding name")));
                    break FOR;
                case 'F':
                    disallowedInRubyOpts(argument);
                    throw new UnsupportedOperationException();
                case 'h':
                    disallowedInRubyOpts(argument);
                    config.setShouldPrintShortUsage(true);
                    config.setShouldRunInterpreter(false);
                    break;
                case 'i':
                    disallowedInRubyOpts(argument);
                    config.setInPlaceBackupExtension(grabOptionalValue());
                    if (config.getInPlaceBackupExtension() == null) {
                        config.setInPlaceBackupExtension("");
                    }
                    break FOR;
                case 'I':
                    String s = grabValue(getArgumentError("-I must be followed by a directory name to add to lib path"));
                    for (String path : s.split(File.pathSeparator)) {
                        if (path.startsWith("~" + File.separator)) {
                            path = System.getProperty("user.home") + File.separator + path.substring(2);
                        }
                        config.getLoadPaths().add(path);
                    }
                    break FOR;
                case 'y':
                    disallowedInRubyOpts(argument);
                    Log.LOGGER.warning("the -y switch is silently ignored as it is an internal development tool");
                    break FOR;
                case 'J':
                    String js = grabOptionalValue();
                    System.err.println("warning: " + argument + " argument ignored (launched in same VM?)");
                    if (js.equals("-cp") || js.equals("-classpath")) {
                        for(;grabOptionalValue() != null;) {}
                        grabValue(getArgumentError(" -J-cp must be followed by a path expression"));
                    }
                    break FOR;
                case 'K':
                    throw new UnsupportedOperationException();
                case 'l':
                    disallowedInRubyOpts(argument);
                    throw new UnsupportedOperationException();
                case 'n':
                    disallowedInRubyOpts(argument);
                    throw new UnsupportedOperationException();
                case 'p':
                    disallowedInRubyOpts(argument);
                    throw new UnsupportedOperationException();
                case 'r':
                    config.getRequiredLibraries().add(grabValue(getArgumentError("-r must be followed by a package to require")));
                    break FOR;
                case 's':
                    disallowedInRubyOpts(argument);
                    config.setArgvGlobalsOn(true);
                    break;
                case 'G':
                    throw new UnsupportedOperationException();
                case 'S':
                    disallowedInRubyOpts(argument);
                    runBinScript();
                    break FOR;
                case 'T':
                    {
                        grabOptionalValue();
                        break FOR;
                    }
                case 'U':
                    config.getOptions().put(OptionsCatalog.INTERNAL_ENCODING.getName(), "UTF-8");
                    break;
                case 'v':
                    config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), true);
                    config.setShowVersion(true);
                    break;
                case 'w':
                    config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), true);
                    break;
                case 'W':
                    {
                        String temp = grabOptionalValue();
                        if (temp == null) {
                            config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), true);
                        } else {
                            if (temp.equals("0")) {
                                config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), 0);
                            } else if (temp.equals("1")) {
                                config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), 1);
                            } else if (temp.equals("2")) {
                                config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), 2);
                            } else {
                                MainExitException mee = new MainExitException(1, getArgumentError(" -W must be followed by either 0, 1, 2 or nothing"));
                                mee.setUsageError(true);
                                throw mee;
                            }
                        }
                        break FOR;
                    }
                case 'x':
                    disallowedInRubyOpts(argument);
                    throw new UnsupportedOperationException();
                case 'X':
                    disallowedInRubyOpts(argument);
                    String extendedOption = grabValue("-X must be followed by an option");
                    if (new File(extendedOption).isDirectory()) {
                        Log.LOGGER.warning("the -X option supplied also appears to be a directory name - did you intend to use -X like -C?");
                    }
                    if (extendedOption.equals("options")) {
                        System.out.println("TruffleRuby options and their default values:");
                        for (OptionDescription option : OptionsCatalog.allDescriptions()) {
                            final String nameValue = String.format("-X%s=%s", option.getName(), option.toString(option.getDefaultValue()));
                            System.out.printf("  %s%" + (50 - nameValue.length()) + "s# %s%n", nameValue, "", option.getDescription());
                        }
                        config.setShouldRunInterpreter(false);
                    } else if (extendedOption.startsWith("log=")) {
                        final String levelString = extendedOption.substring("log=".length());

                        final Level level;

                        if (levelString.equals("PERFORMANCE")) {
                            level = Log.PERFORMANCE;
                        } else {
                            level = Level.parse(levelString.toUpperCase());
                        }

                        Log.LOGGER.setLevel(level);
                    } else {
                        if (extendedOption.startsWith("truffle.")) {
                            Log.LOGGER.warning("-Xtruffle. is now just -X - switch your scripts as -Xtruffle. will stop working soon");
                            extendedOption = extendedOption.substring("truffle.".length());
                        }

                        final String value;

                        final int equals = extendedOption.indexOf('=');
                        if (equals == -1) {
                            value = "true";
                        } else {
                            value = extendedOption.substring(equals + 1);
                            extendedOption = extendedOption.substring(0, equals);
                        }

                        config.getOptions().put(extendedOption, value);
                    }
                    break FOR;
                case '-':
                    if (argument.equals("--copyright")) {
                        disallowedInRubyOpts(argument);
                        config.setShowCopyright(true);
                        config.setShouldRunInterpreter(false);
                        break FOR;
                    } else if (argument.equals("--debug")) {
                        throw new UnsupportedOperationException();
                    } else if (argument.equals("--yydebug")) {
                        disallowedInRubyOpts(argument);
                        Log.LOGGER.warning("the --yydebug switch is silently ignored as it is an internal development tool");
                    } else if (argument.equals("--help")) {
                        disallowedInRubyOpts(argument);
                        config.setShouldPrintUsage(true);
                        config.setShouldRunInterpreter(false);
                        break;
                    } else if (argument.equals("--version")) {
                        disallowedInRubyOpts(argument);
                        config.setShowVersion(true);
                        config.setShouldRunInterpreter(false);
                        break FOR;
                    } else if (argument.startsWith("--profile")) {
                        throw new UnsupportedOperationException();
                    } else if (argument.equals("--debug-frozen-string-literal")) {
                        throw new UnsupportedOperationException();
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
                        throw new UnsupportedOperationException();
                    } else if (argument.equals("--verbose")) {
                        config.getOptions().put(OptionsCatalog.VERBOSITY.getName(), true);
                        break FOR;
                    } else if (argument.startsWith("--dump=")) {
                        Log.LOGGER.warning("the --dump= switch is silently ignored as it is an internal development tool");
                        break FOR;
                    } else {
                        if (argument.equals("--")) {
                            // ruby interpreter compatibilty
                            // Usage: ruby [switches] [--] [programfile] [arguments])
                            endOfArguments = true;
                            break;
                        }
                    }
                    throw new MainExitException(1, "jruby: unknown option " + argument);
                default:
                    throw new MainExitException(1, "jruby: unknown option " + argument);
            }
        }
    }

    private void enableDisableFeature(String name, boolean enable) {
        BiFunction<CommandLineParser, Boolean, Boolean> feature = FEATURES.get(name);

        if (feature == null) {
            System.err.println("warning: unknown argument for --" + (enable ? "enable" : "disable") + ": `" + name + "'");
        } else {
            feature.apply(this, enable);
        }
    }

    private static String[] valueListFor(String argument, String key) {
        int length = key.length() + 3; // 3 is from -- and = (e.g. --disable=)
        String[] values = argument.substring(length).split(",");

        if (values.length == 0) errorMissingEquals(key);

        return values;
    }

    private void disallowedInRubyOpts(CharSequence option) {
        if (rubyOpts) {
            throw new MainExitException(1, "jruby: invalid switch in RUBYOPT: " + option + " (RuntimeError)");
        }
    }

    private static void errorMissingEquals(String label) {
        MainExitException mee;
        mee = new MainExitException(1, "missing argument for --" + label + "\n");
        mee.setUsageError(true);
        throw mee;
    }

    @SuppressWarnings("fallthrough")
    private void processEncodingOption(String value) {
        List<String> encodings = StringSupport.split(value, ':', 3);
        switch (encodings.size()) {
            case 3:
                throw new MainExitException(1, "extra argument for -E: " + encodings.get(2));
            case 2:
                config.getOptions().put(OptionsCatalog.INTERNAL_ENCODING.getName(), encodings.get(1));
            case 1:
                config.getOptions().put(OptionsCatalog.EXTERNAL_ENCODING.getName(), encodings.get(1));
            // Zero is impossible
        }
    }

    private void runBinScript() {
        String scriptName = grabValue("jruby: provide a bin script to execute");
        config.setUsePathScript(scriptName);
        endOfArguments = true;
    }

    private String grabValue(String errorMessage) {
        return grabValue(errorMessage, true);
    }

    private String grabValue(String errorMessage, boolean usageError) {
        String optValue = grabOptionalValue();
        if (optValue != null) {
            return optValue;
        }
        argumentIndex++;
        if (argumentIndex < arguments.size()) {
            return arguments.get(argumentIndex).originalValue;
        }
        MainExitException mee = new MainExitException(1, errorMessage);
        if (usageError) mee.setUsageError(true);
        throw mee;
    }

    private String grabOptionalValue() {
        characterIndex++;
        String argValue = arguments.get(argumentIndex).originalValue;
        if (characterIndex < argValue.length()) {
            return argValue.substring(characterIndex);
        }
        return null;
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
        out.println("  -Xoptions       print available TrufleRuby options");
        out.println("  -Xname=value    set a TruffleRuby option (omit value to set to true)");

        if (TruffleOptions.AOT) {
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

    private static final Map<String, BiFunction<CommandLineParser, Boolean, Boolean>> FEATURES;

    static {
        Map<String, BiFunction<CommandLineParser, Boolean, Boolean>> features = new HashMap<>(12, 1);
        BiFunction<CommandLineParser, Boolean, Boolean> function2;

        features.put("all", new BiFunction<CommandLineParser, Boolean, Boolean>() {
            public Boolean apply(CommandLineParser processor, Boolean enable) {
                // disable all features
                for (Map.Entry<String, BiFunction<CommandLineParser, Boolean, Boolean>> entry : FEATURES.entrySet()) {
                    if (entry.getKey().equals("all")) continue; // skip self
                    entry.getValue().apply(processor, enable);
                }
                return true;
            }
        });
        features.put("gem", new BiFunction<CommandLineParser, Boolean, Boolean>() {
            public Boolean apply(CommandLineParser processor, Boolean enable) {
                processor.config.getOptions().put(OptionsCatalog.RUBYGEMS.getName(), enable);
                return true;
            }
        });
        features.put("gems", new BiFunction<CommandLineParser, Boolean, Boolean>() {
            public Boolean apply(CommandLineParser processor, Boolean enable) {
                processor.config.getOptions().put(OptionsCatalog.RUBYGEMS.getName(), enable);
                return true;
            }
        });
        features.put("frozen-string-literal", function2 = new BiFunction<CommandLineParser, Boolean, Boolean>() {
            public Boolean apply(CommandLineParser processor, Boolean enable) {
                processor.config.getOptions().put(OptionsCatalog.FROZEN_STRING_LITERALS.getName(), enable);
                return true;
            }
        });
        features.put("frozen_string_literal", function2); // alias

        FEATURES = features;
    }
}
