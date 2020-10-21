/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.backtrace;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.methods.TranslateExceptionNode;
import org.truffleruby.parser.RubySource;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class BacktraceFormatter {

    public enum FormattingFlags {
        OMIT_EXCEPTION,
        OMIT_FROM_PREFIX,
        INTERLEAVE_JAVA
    }

    /** Flags for a backtrace exposed to Ruby via #caller, #caller_locations, Exception#backtrace and
     * Thread#backtrace. */
    public static final EnumSet<FormattingFlags> USER_BACKTRACE_FLAGS = EnumSet
            .of(FormattingFlags.OMIT_FROM_PREFIX, FormattingFlags.OMIT_EXCEPTION);

    private final RubyContext context;
    private final EnumSet<FormattingFlags> flags;

    @TruffleBoundary
    public static BacktraceFormatter createDefaultFormatter(RubyContext context) {
        final EnumSet<FormattingFlags> flags = EnumSet.noneOf(FormattingFlags.class);

        if (context.getOptions().BACKTRACES_INTERLEAVE_JAVA) {
            flags.add(FormattingFlags.INTERLEAVE_JAVA);
        }

        return new BacktraceFormatter(context, flags);
    }

    // For debugging:
    // org.truffleruby.language.backtrace.BacktraceFormatter.printableRubyBacktrace(getContext(), this)
    // When outside a Ruby node:
    // org.truffleruby.language.backtrace.BacktraceFormatter.printableRubyBacktrace(RubyLanguage.getCurrentContext(), null)
    public static String printableRubyBacktrace(RubyContext context, Node node) {
        final BacktraceFormatter backtraceFormatter = new BacktraceFormatter(
                context,
                EnumSet.noneOf(FormattingFlags.class));
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
                !context.getSourcePath(sourceSection.getSource()).contains("/lib/stdlib/rubygems");
    }

    public BacktraceFormatter(RubyContext context, EnumSet<FormattingFlags> flags) {
        this.context = context;
        this.flags = flags;
    }

    @TruffleBoundary
    public void printTopLevelRubyExceptionOnEnvStderr(RubyException rubyException) {
        final Backtrace backtrace = rubyException.backtrace;
        if (backtrace != null && backtrace.getStackTrace().length == 0) {
            // An Exception with a non-null empty stacktrace, so an Exception from Truffle::Boot.main
            printRubyExceptionOnEnvStderr("truffleruby: ", rubyException);
        } else {
            printRubyExceptionOnEnvStderr("", rubyException);
        }
    }

    @SuppressFBWarnings("OS")
    @TruffleBoundary
    public void printRubyExceptionOnEnvStderr(String info, RubyException rubyException) {
        final PrintStream printer = printStreamFor(context.getEnv().err());
        if (!info.isEmpty()) {
            printer.print(info);
        }

        final Object fullMessage = context.send(
                context.getCoreLibrary().truffleExceptionOperationsModule,
                "get_formatted_backtrace",
                rubyException);
        final String formatted = fullMessage != null ? ((RubyString) fullMessage).getJavaString() : "<no message>";
        if (formatted.endsWith("\n")) {
            printer.print(formatted);
        } else {
            printer.println(formatted);
        }
    }

    @SuppressFBWarnings("OS")
    @TruffleBoundary
    public void printBacktraceOnEnvStderr(Node currentNode) {
        final Backtrace backtrace = context.getCallStack().getBacktrace(currentNode);
        final PrintStream printer = printStreamFor(context.getEnv().err());
        printer.println(formatBacktrace(null, backtrace));
    }

    public static PrintStream printStreamFor(OutputStream outputStream) {
        try {
            return new PrintStream(outputStream, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

    /** Format the backtrace as a String with \n between each line, but no trailing \n. */
    public String formatBacktrace(RubyException exception, Backtrace backtrace) {
        return formatBacktrace(exception, backtrace, Integer.MAX_VALUE);
    }

    /** Formats at most {@code length} elements of the backtrace (starting from the top of the call stack) as a String
     * with \n between each line, but no trailing \n. */
    @TruffleBoundary
    public String formatBacktrace(RubyException exception, Backtrace backtrace, int length) {
        return String.join("\n", formatBacktraceAsStringArray(exception, backtrace, length));
    }

    public RubyArray formatBacktraceAsRubyStringArray(RubyException exception, Backtrace backtrace) {
        return formatBacktraceAsRubyStringArray(exception, backtrace, Integer.MAX_VALUE);
    }

    public RubyArray formatBacktraceAsRubyStringArray(RubyException exception, Backtrace backtrace, int length) {
        final String[] lines = formatBacktraceAsStringArray(exception, backtrace, length);

        final Object[] array = new Object[lines.length];

        for (int n = 0; n < lines.length; n++) {
            array[n] = StringOperations.createString(
                    context,
                    StringOperations.encodeRope(lines[n], UTF8Encoding.INSTANCE));
        }

        return ArrayHelpers.createArray(context, array);
    }

    @TruffleBoundary
    private String[] formatBacktraceAsStringArray(RubyException exception, Backtrace backtrace, int length) {
        if (backtrace == null) {
            backtrace = context.getCallStack().getBacktrace(null);
        }

        final TruffleStackTraceElement[] stackTrace = backtrace.getStackTrace();
        length = Math.min(length, stackTrace.length);
        final ArrayList<String> lines = new ArrayList<>(length);

        if (length == 0 && !flags.contains(FormattingFlags.OMIT_EXCEPTION) && exception != null) {
            lines.add(formatException(exception));
            return lines.toArray(StringUtils.EMPTY_STRING_ARRAY);
        }

        for (int n = 0; n < length; n++) {
            lines.add(formatLine(stackTrace, n, exception));
        }

        if (backtrace.getJavaThrowable() != null && flags.contains(FormattingFlags.INTERLEAVE_JAVA)) {
            final List<String> interleaved = BacktraceInterleaver
                    .interleave(lines, backtrace.getJavaThrowable().getStackTrace(), backtrace.getOmitted());
            return interleaved.toArray(StringUtils.EMPTY_STRING_ARRAY);
        }

        return lines.toArray(StringUtils.EMPTY_STRING_ARRAY);
    }

    @TruffleBoundary
    public String formatLine(TruffleStackTraceElement[] stackTrace, int n, RubyException exception) {
        try {
            return formatLineInternal(stackTrace, n, exception);
        } catch (Exception e) {
            TranslateExceptionNode.logJavaException(context, null, e);

            final String firstFrame = e.getStackTrace().length > 0 ? e.getStackTrace()[0].toString() : "";
            return StringUtils.format("(exception %s %s %s", e.getClass().getName(), e.getMessage(), firstFrame);
        }
    }

    private String formatLineInternal(TruffleStackTraceElement[] stackTrace, int n, RubyException exception) {
        final TruffleStackTraceElement element = stackTrace[n];

        final StringBuilder builder = new StringBuilder();

        if (!flags.contains(FormattingFlags.OMIT_FROM_PREFIX) && n > 0) {
            builder.append("\tfrom ");
        }

        final Node callNode = element.getLocation();

        if (callNode == null || callNode.getRootNode() instanceof RubyRootNode) { // A Ruby frame
            final SourceSection sourceSection = callNode == null ? null : callNode.getEncapsulatingSourceSection();
            final SourceSection reportedSourceSection;
            final String reportedName;

            // Unavailable SourceSections are always skipped, as there is no source position information.
            if (isAvailable(sourceSection)) {
                reportedSourceSection = sourceSection;
                final RootNode rootNode = callNode.getRootNode();
                reportedName = ((RubyRootNode) rootNode).getSharedMethodInfo().getName();
            } else {
                final SourceSection nextUserSourceSection = nextAvailableSourceSection(stackTrace, n);
                // if there is no next source section use a core one to avoid ???
                reportedSourceSection = nextUserSourceSection != null ? nextUserSourceSection : sourceSection;
                reportedName = Backtrace.labelFor(element);
            }

            if (reportedSourceSection == null) {
                builder.append("???");
            } else {
                builder.append(context.getSourcePath(reportedSourceSection.getSource()));
                builder.append(":");
                builder.append(RubySource.getStartLineAdjusted(context, reportedSourceSection));
            }
            builder.append(":in `");
            builder.append(reportedName);
            builder.append("'");
        } else { // A foreign frame
            builder.append(formatForeign(callNode, Backtrace.labelFor(element)));
        }

        if (!flags.contains(FormattingFlags.OMIT_EXCEPTION) && exception != null && n == 0) {
            builder.append(": ");
            builder.append(formatException(exception));
        }

        return builder.toString();
    }

    public static String formatJava(StackTraceElement stackTraceElement) {
        return stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() +
                ":in `" + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "'";
    }

    private String formatForeign(Node callNode, String methodName) {
        final StringBuilder builder = new StringBuilder();
        final SourceSection sourceSection = callNode == null ? null : callNode.getEncapsulatingSourceSection();

        if (sourceSection != null) {
            final Source source = sourceSection.getSource();
            final String path = context.getSourcePath(source);

            builder.append(path);
            if (sourceSection.isAvailable()) {
                builder.append(":").append(sourceSection.getStartLine());
            }

            final RootNode rootNode = callNode.getRootNode();
            if (rootNode != null) {
                String identifier = rootNode.getName();

                if (identifier != null && !identifier.isEmpty()) {
                    if (rootNode.getLanguageInfo().getId().equals("llvm") && identifier.startsWith("@")) {
                        identifier = identifier.substring(1);
                    }

                    builder.append(":in `");
                    builder.append(identifier);
                    builder.append("'");
                }
            }
        } else if (callNode != null) {
            builder.append(getRootOrTopmostNode(callNode).getClass().getSimpleName());
        } else {
            builder.append(methodName);
        }

        return builder.toString();
    }

    private String formatException(RubyException exception) {
        final StringBuilder builder = new StringBuilder();

        final String message = ExceptionOperations.messageToString(context, exception);

        final String exceptionClass = exception.getLogicalClass().fields
                .getName();

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

    public SourceSection nextAvailableSourceSection(TruffleStackTraceElement[] stackTrace, int n) {
        while (n < stackTrace.length) {
            final Node callNode = stackTrace[n].getLocation();

            if (callNode != null) {
                final SourceSection sourceSection = callNode.getEncapsulatingSourceSection();

                if (isAvailable(sourceSection)) {
                    return sourceSection;
                }
            }

            n++;
        }
        return null;
    }

    public static boolean isAvailable(SourceSection sourceSection) {
        return sourceSection != null && sourceSection.isAvailable();
    }

    public static boolean isUserSourceSection(RubyContext context, SourceSection sourceSection) {
        return isAvailable(sourceSection) && !isRubyCore(context, sourceSection.getSource());
    }

    public static boolean isRubyCore(RubyContext context, Source source) {
        final String path = RubyContext.getPath(source);
        return path.startsWith(context.getCoreLibrary().coreLoadPath);
    }

    private Node getRootOrTopmostNode(Node node) {
        while (node.getParent() != null) {
            node = node.getParent();
        }

        return node;
    }

    public static String formatJavaThrowableMessage(Throwable t) {
        final String message = t.getMessage();
        return (message != null ? message : "<no message>") + " (" + t.getClass().getSimpleName() + ")";
    }

}
