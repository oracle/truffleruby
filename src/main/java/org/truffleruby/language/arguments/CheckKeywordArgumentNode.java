/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;
import org.truffleruby.language.locals.WriteLocalVariableNode;

public class CheckKeywordArgumentNode extends RubyContextSourceNode {

    public static final CheckKeywordArgumentNode[] EMPTY_ARRAY = new CheckKeywordArgumentNode[0];

    private final FrameSlot slot;
    private final RubySymbol name;

    @Child protected ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child protected HashStoreLibrary hashStoreLibrary = HashStoreLibrary.createDispatched();
    @Child protected WriteLocalVariableNode writeDefaultNode;

    public CheckKeywordArgumentNode(FrameSlot slot, int minimum, RubyNode defaultValue, RubySymbol name) {
        this.slot = slot;
        this.name = name;
        writeDefaultNode = new WriteLocalVariableNode(slot, defaultValue);
        readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimum);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return execute(frame, RubyArguments.getKeywordArgumentsDescriptorUnsafe(frame));
    }

    public Object execute(VirtualFrame frame, KeywordDescriptor descriptor) {
        // Get the current value of the parameter.

        final Object value = frame.getValue(slot);

        // If it has a value, then we're done.

        if (value != ReadArgumentsNode.MISSING) {
            return null;
        }

        if (descriptor instanceof NonEmptyKeywordDescriptor) {
            final NonEmptyKeywordDescriptor nonEmpty = (NonEmptyKeywordDescriptor) descriptor;

            if (nonEmpty.isAlsoSplat()) {
                // If it doesn't have a value, then we should try looking up in the full keyword argument hash, if there is one.

                final RubyHash hash = (RubyHash) RubyArguments.getArgument(frame, nonEmpty.getHashIndex());

                final Object valueFromHash = hashStoreLibrary
                        .lookupOrDefault(hash.store, frame, hash, name, (f, h, k) -> null);

                if (valueFromHash != null) {
                    // If we found a value in the hash, then store it in the local and we're done.

                    frame.setObject(slot, valueFromHash);
                    return null;
                }
            }
        }

        // We didn't find a value in the local or we didn't have a hash or we didn't find a value in the hash - run
        // the default expression, which may either assign a value to the local, or it may raise an exception in
        // the case of a required keyword argument.

        writeDefaultNode.execute(frame);
        return null;
    }
}
