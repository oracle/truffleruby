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
import org.truffleruby.launcher.options.CommandLineOptions;
import org.truffleruby.launcher.options.OptionsCatalog;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RubyLauncher extends AbstractLanguageLauncher {

    private final CommandLineOptions config = new CommandLineOptions();

    public static void main(String[] args) {
        System.setProperty("truffleruby.single_threaded", Boolean.FALSE.toString());
        new RubyLauncher().launch(args);
    }

    @Override
    protected String getLanguageId() {
        return Launcher.LANGUAGE_ID;
    }

    @Override
    protected String getMainClass() {
        return RubyLauncher.class.getName();
    }

    @Override
    protected void validateArguments(Map<String, String> polyglotOptions) {
    }

    @Override
    protected void printHelp(OptionCategory maxCategory) {
        Launcher.printHelp(System.out);
    }

    @Override
    protected void printVersion() {
        printPolyglotVersions();
        System.out.println();
        System.out.println(Launcher.getVersionString(true));
    }

    @Override
    protected List<String> preprocessArguments(List<String> args, Map<String, String> polyglotOptions) {
        Launcher.metricsBegin();
        Launcher.processArguments(config, args, false, false, isAOT());

        if (isAOT()) {
            // if applied store the options in polyglotOptions otherwise it would be lost when
            // switched to --jvm
            if (config.getOption(OptionsCatalog.HOME).isEmpty()) {
                final String rubyHome = getGraalVMHome().resolve(Paths.get("jre", "languages", "ruby")).toString();
                config.setOption(OptionsCatalog.HOME, rubyHome);
                polyglotOptions.put(OptionsCatalog.HOME.getName(), rubyHome);
            }
            final String launcher = Launcher.setRubyLauncherIfNative(config);
            if (launcher != null) {
                polyglotOptions.put(OptionsCatalog.LAUNCHER.getName(), launcher);
            }
        }

        return config.getUnknownArguments();
    }

    @Override
    protected void launch(Context.Builder contextBuilder) {
        Launcher.printPreRunInformation(true, config);
        final int exitValue = Launcher.runRubyMain(contextBuilder, config);
        Launcher.metricsEnd();
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
    protected VMType getDefaultVMType() {
        return VMType.JVM;
    }

    @Override
    protected String[] getDefaultLanguages() {
        return new String[]{getLanguageId(), "llvm"};
    }

    private static Path getGraalVMHome() {
        final String graalVMHome = System.getProperty("org.graalvm.home");
        assert graalVMHome != null;
        return Paths.get(graalVMHome);
    }

}
