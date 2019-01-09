/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.backtrace;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.shared.TruffleRuby;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BacktraceFormatter {

    public enum FormattingFlags {
        OMIT_EXCEPTION,
        OMIT_FROM_PREFIX,
        INCLUDE_CORE_FILES,
        INTERLEAVE_JAVA
    }

    /**
     * Flags for a backtrace exposed to Ruby via #caller, #caller_locations, Exception#backtrace and
     * Thread#backtrace.
     */
    public static final EnumSet<FormattingFlags> USER_BACKTRACE_FLAGS =
            EnumSet.of(FormattingFlags.OMIT_FROM_PREFIX, FormattingFlags.OMIT_EXCEPTION);

    private final RubyContext context;
    private final EnumSet<FormattingFlags> flags;

    @TruffleBoundary
    public static BacktraceFormatter createDefaultFormatter(RubyContext context) {
        final EnumSet<FormattingFlags> flags = EnumSet.noneOf(FormattingFlags.class);

        if (!context.getOptions().BACKTRACES_HIDE_CORE_FILES) {
            flags.add(FormattingFlags.INCLUDE_CORE_FILES);
        }

        if (context.getOptions().BACKTRACES_INTERLEAVE_JAVA) {
            flags.add(FormattingFlags.INTERLEAVE_JAVA);
        }

        return new BacktraceFormatter(context, flags);
    }

    // For debugging:
    // org.truffleruby.language.backtrace.BacktraceFormatter.printableRubyBacktrace(getContext(), this)
    public static String printableRubyBacktrace(RubyContext context, Node node) {
        final BacktraceFormatter backtraceFormatter = new BacktraceFormatter(context, EnumSet.of(FormattingFlags.INCLUDE_CORE_FILES));
        final String backtrace = backtraceFormatter.formatBacktrace(null, context.getCallStack().getBacktrace(node));
        if (backtrace.isEmpty()) {
            return "<empty backtrace>";
        } else {
            return backtrace;
        }
    }

    /** For debug purposes. */
    public static boolean isApplicationCode(RubyContext context, SourceSection sourceSection) {
        return isUserSourceSection(context, sourceSection) &&
                !sourceSection.getSource().getName().contains("/lib/stdlib/rubygems");
    }

    public BacktraceFormatter(RubyContext context, EnumSet<FormattingFlags> flags) {
        this.context = context;
        this.flags = flags;
    }

    @TruffleBoundary
    public void printRubyExceptionOnEnvStderr(DynamicObject rubyException) {
        final PrintWriter printer = new PrintWriter(context.getEnv().err(), true);
        // can be null, if @custom_backtrace is used
        final Backtrace backtrace = Layouts.EXCEPTION.getBacktrace(rubyException);
        if (backtrace != null) {
            printer.println(formatBacktrace(rubyException, backtrace));
        } else {
            final Object fullMessage = context.send(rubyException, "full_message");
            final Object fullMessageString;
            if (RubyGuards.isRubyString(fullMessage)) {
                fullMessageString = StringOperations.getString((DynamicObject) fullMessage);
            } else {
                fullMessageString = fullMessage.toString() + "\n";
            }
            printer.print(fullMessageString);
        }
    }

    @TruffleBoundary
    public void printBacktraceOnEnvStderr(Node currentNode) {
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode);
        final PrintWriter printer = new PrintWriter(context.getEnv().err(), true);
        printer.println(formatBacktrace(null, backtrace));
    }

    /** Format the backtrace as a String with \n between each line, but no trailing \n. */
    @TruffleBoundary
    public String formatBacktrace(DynamicObject exception, Backtrace backtrace) {
        return String.join("\n", formatBacktraceAsStringArray(exception, backtrace));
    }

    public DynamicObject formatBacktraceAsRubyStringArray(DynamicObject exception, Backtrace backtrace) {
        final String[] lines = formatBacktraceAsStringArray(exception, backtrace);

        final Object[] array = new Object[lines.length];

        for (int n = 0; n < lines.length; n++) {
            array[n] = StringOperations.createString(context,
                    StringOperations.encodeRope(lines[n], UTF8Encoding.INSTANCE));
        }

        return ArrayHelpers.createArray(context, array);
    }

    @TruffleBoundary
    private String[] formatBacktraceAsStringArray(DynamicObject exception, Backtrace backtrace) {
        if (backtrace == null) {
            backtrace = context.getCallStack().getBacktrace(null);
        }

        final Activation[] activations = backtrace.getActivations();
        final ArrayList<String> lines = new ArrayList<>();

        if (activations.length == 0 && !flags.contains(FormattingFlags.OMIT_EXCEPTION) && exception != null) {
            lines.add(formatException(exception));
            return lines.toArray(new String[lines.size()]);
        }

        for (int n = 0; n < activations.length; n++) {
            lines.add(formatLine(activations, n, exception));
        }

        if (backtrace.getJavaThrowable() != null && flags.contains(FormattingFlags.INTERLEAVE_JAVA)) {
            final List<String> interleaved = BacktraceInterleaver.interleave(lines, backtrace.getJavaThrowable().getStackTrace(), backtrace.getOmitted());
            return interleaved.toArray(new String[interleaved.size()]);
        }

        return lines.toArray(new String[lines.size()]);
    }

    @TruffleBoundary
    public String formatLine(Activation[] activations, int n, DynamicObject exception) {
        try {
            return formatLineInternal(activations, n, exception);
        } catch (Exception e) {
            if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
                e.printStackTrace();

                if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                    printBacktraceOnEnvStderr(null);
                }
            }

            final String firstFrame = e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "";
            return StringUtils.format("(exception %s %s %s", e.getClass().getName(), e.getMessage(), firstFrame);
        }
    }

    private String formatLineInternal(Activation[] activations, int n, DynamicObject exception) {
        final Activation activation = activations[n];

        final StringBuilder builder = new StringBuilder();

        if (!flags.contains(FormattingFlags.OMIT_FROM_PREFIX) && n > 0) {
            builder.append("\tfrom ");
        }

        final Node callNode = activation.getCallNode();

        if (activation.getMethod() != null) { // A Ruby frame
            final SourceSection sourceSection = callNode == null ? null : callNode.getEncapsulatingSourceSection();
            final SourceSection reportedSourceSection;
            final String reportedName;

            // Unavailable SourceSections are always skipped, as there is no source position information.
            // Only show core library SourceSections if the flags contain the option.
            if (sourceSection != null && sourceSection.isAvailable() &&
                    (flags.contains(FormattingFlags.INCLUDE_CORE_FILES) || isUserSourceSection(context, sourceSection))) {
                reportedSourceSection = sourceSection;
                final RootNode rootNode = callNode.getRootNode();
                reportedName = ((RubyRootNode) rootNode).getSharedMethodInfo().getName();
            } else {
                final SourceSection nextUserSourceSection = nextUserSourceSection(activations, n);
                // if there is no next source section use a core one to avoid ???
                reportedSourceSection = nextUserSourceSection != null ? nextUserSourceSection : sourceSection;
                reportedName = activation.getMethod().getName();
            }

            if (reportedSourceSection == null) {
                builder.append("???");
            } else {
                builder.append(context.getPath(reportedSourceSection.getSource()));
                builder.append(":");
                builder.append(reportedSourceSection.getStartLine());
            }
            builder.append(":in `");
            builder.append(reportedName);
            builder.append("'");
        } else { // A foreign frame
            builder.append(formatForeign(callNode));
        }

        if (!flags.contains(FormattingFlags.OMIT_EXCEPTION) && exception != null && n == 0) {
            builder.append(": ");
            builder.append(formatException(exception));
        }

        return builder.toString();
    }

    private String formatForeign(Node callNode) {
        final StringBuilder builder = new StringBuilder();
        final SourceSection sourceSection = callNode.getEncapsulatingSourceSection();

        if (sourceSection != null) {
            final Source source = sourceSection.getSource();
            final String path = source.getPath() != null ? source.getPath() : source.getName();

            builder.append(path);
            if (sourceSection.isAvailable()) {
                builder.append(":").append(sourceSection.getStartLine());
            }

            final RootNode rootNode = callNode.getRootNode();

            String identifier = rootNode.getName();

            if (identifier != null && !identifier.isEmpty()) {
                if (rootNode.getLanguageInfo().getId().equals(TruffleRuby.LLVM_ID) && identifier.startsWith("@")) {
                    identifier = identifier.substring(1);
                }

                builder.append(":in `");
                builder.append(identifier);
                builder.append("'");
            }
        } else {
            builder.append(getRootOrTopmostNode(callNode).getClass().getSimpleName());
        }

        return builder.toString();
    }

    private String formatException(DynamicObject exception) {
        final StringBuilder builder = new StringBuilder();

        final String message = ExceptionOperations.messageToString(context, exception);

        final String exceptionClass = Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(exception)).getName();

        // Show the exception class at the end of the first line of the message
        final int firstLn = message.indexOf('\n');
        if (firstLn >= 0) {
            builder.append(message, 0, firstLn);
            builder.append(" (").append(exceptionClass).append(")");
            builder.append(message.substring(firstLn));
        } else {
            builder.append(message);
            builder.append(" (").append(exceptionClass).append(")");
        }
        return builder.toString();
    }

    public SourceSection nextUserSourceSection(Activation[] activations, int n) {
        while (n < activations.length) {
            final Node callNode = activations[n].getCallNode();

            if (callNode != null) {
                final SourceSection sourceSection = callNode.getEncapsulatingSourceSection();

                if (isUserSourceSection(context, sourceSection)) {
                    return sourceSection;
                }
            }

            n++;
        }
        return null;
    }

    public static boolean isCore(RubyContext context, SourceSection sourceSection) {
        if (sourceSection == null || !sourceSection.isAvailable()) {
            return true;
        }

        final Source source = sourceSection.getSource();
        if (source == null) {
            return true;
        }

        final String name = source.getName();
        if (name == null) {
            return true;
        }

        if (name.startsWith(RubyLanguage.RESOURCE_SCHEME)) {
            return true;
        } else if (name.endsWith("truffle/lazy-rubygems.rb")) {
            // Sinatra manually filters RubyGems files patching #require from caller():
            // https://github.com/sinatra/sinatra/blob/v2.0.3/lib/sinatra/base.rb#L1165-L1174
            // lazy-rubygems.rb should be as transparent as possible, including user backtraces,
            // so we hide it in user backtraces like core files.
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUserSourceSection(RubyContext context, SourceSection sourceSection) {
        return !isCore(context, sourceSection);
    }

    private Node getRootOrTopmostNode(Node node) {
        while (node.getParent() != null) {
            node = node.getParent();
        }

        return node;
    }

}
