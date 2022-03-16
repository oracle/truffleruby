/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.hash.HashNodes.CopyHashAndSetRuby2KeywordsNode;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.ArgumentsDescriptor;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptor;

/** A literal call site in Ruby code: one of foo(), super or yield. */
public abstract class LiteralCallNode extends RubyContextSourceNode {

    protected final ArgumentsDescriptor descriptor;
    @Child private CopyHashAndSetRuby2KeywordsNode copyHashAndSetRuby2KeywordsNode;

    protected final boolean isSplatted;
    @CompilationFinal private boolean notRuby2KeywordsHashProfile, emptyKeywordsProfile, notEmptyKeywordsProfile;

    protected LiteralCallNode(boolean isSplatted, ArgumentsDescriptor descriptor) {
        this.isSplatted = isSplatted;
        this.descriptor = descriptor;
    }

    // NOTE: args is either frame args or user args
    protected ArgumentsDescriptor getArgumentsDescriptorAndCheckRuby2KeywordsHash(Object[] args, int userArgsCount) {
        assert isSplatted : "this is only needed if isSplatted";

        if (descriptor == EmptyArgumentsDescriptor.INSTANCE) { // *rest and no kwargs passed explicitly (k: v/k => v/**kw)
            if (userArgsCount > 0) {
                final Object lastArgument = ArrayUtils.getLast(args);
                assert lastArgument != null;
                if (isRuby2KeywordsHash(lastArgument)) { // both branches profiled
                    if (copyHashAndSetRuby2KeywordsNode == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        copyHashAndSetRuby2KeywordsNode = insert(CopyHashAndSetRuby2KeywordsNode.create());
                    }

                    RubyHash unmarked = copyHashAndSetRuby2KeywordsNode.execute((RubyHash) lastArgument, false);
                    ArrayUtils.setLast(args, unmarked);
                    return KeywordArgumentsDescriptor.INSTANCE;
                } else {
                    if (!notRuby2KeywordsHashProfile) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        notRuby2KeywordsHashProfile = true;
                    }
                }
            }
        }

        return descriptor;
    }

    private boolean isRuby2KeywordsHash(Object lastArgument) {
        return lastArgument instanceof RubyHash && ((RubyHash) lastArgument).ruby2_keywords;
    }

    // NOTE: args is either frame args or user args
    protected boolean emptyKeywordArguments(Object[] args) {
        if (((RubyHash) ArrayUtils.getLast(args)).empty()) {
            if (!emptyKeywordsProfile) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                emptyKeywordsProfile = true;
            }
            return true;
        } else {
            if (!notEmptyKeywordsProfile) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                notEmptyKeywordsProfile = true;
            }
            return false;
        }
    }

    // NOTE: args is either frame args or user args
    public static Object[] removeEmptyKeywordArguments(Object[] args) {
        return ArrayUtils.extractRange(args, 0, args.length - 1);
    }

}
