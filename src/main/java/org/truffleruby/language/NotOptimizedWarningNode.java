/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.RubyLanguage;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Displays a warning when code is compiled that will compile successfully but is very low performance. We don't want
 * to bail out, as this isn't visible to users - we want them to see if they're using code like this in something
 * like a benchmark.
 *
 * Ideally you should not use this node, and instead you should optimise the code which would use it.
 */
public class NotOptimizedWarningNode extends RubyBaseNode {

    @CompilationFinal private boolean warned;

    // The remembered set of displayed warnings is global to the VM
    private static final Set<String> DISPLAYED_WARNINGS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @SuppressWarnings("unused")
    private static volatile boolean[] sideEffect;

    public void warn(String message) {
        // The message should be a constant, because we don't want to do anything expensive to create it
        CompilerAsserts.compilationConstant(message);

        // If we've already warned from this node then don't warn again
        if (warned) {
            return;
        }

        // If we didn't cause the value to escape, the transfer would float above the inInterpreter
        final boolean[] inInterpreter = new boolean[]{ CompilerDirectives.inInterpreter() };
        sideEffect = inInterpreter;

        // If we're in the interpreter then don't warn
        if (inInterpreter[0]) {
            return;
        }

        // This is the first time in compiled code, so transfer to the interpreter to warn and remember that we've warned
        CompilerDirectives.transferToInterpreterAndInvalidate();

        // We want the topmost user source section, as otherwise lots of warnings will come from the same core methods
        final SourceSection userSourceSection = getContext().getCallStack().getTopMostUserSourceSection();

        final String displayedWarning = String.format("%s: %s",
                getContext().fileLine(userSourceSection), message);

        if (DISPLAYED_WARNINGS.add(displayedWarning)) {
            RubyLanguage.LOGGER.log(Level.WARNING, displayedWarning);
        }

        warned = true;
    }

}
