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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.RubyContext;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.PackedHashStoreLibrary;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;

@NodeChild("args")
@NodeChild("descriptor")
@NodeChild("offset")
public abstract class ExpandKeywordArgumentsNode extends RubyContextSourceNode {

    public static ExpandKeywordArgumentsNode create() {
        return ExpandKeywordArgumentsNodeGen.create(null, null, null);
    }

    public abstract RubyHash execute(Object[] args, NonEmptyKeywordDescriptor descriptor, int offset);

    @Specialization(guards = "isAlsoSplat(descriptor)")
    @TruffleBoundary
    protected RubyHash expandAlsoSplat(Object[] args, NonEmptyKeywordDescriptor descriptor, int offset) {
        final RubyHash hash = (RubyHash) RubyContext
                .send(RubyContext.send(args[offset + descriptor.getHashIndex()], "to_hash"), "dup");

        for (int n = 0; n < descriptor.getLength(); n++) {
            final RubySymbol symbol = getSymbol(descriptor.getKeyword(n));
            RubyContext.send(hash, "[]=", symbol, args[args.length - descriptor.getLength() + n]);
        }

        return hash;
    }

    @Specialization(guards = { "!isAlsoSplat(descriptor)", "isShort(descriptor)" })
    @ExplodeLoop
    protected RubyHash expandShort(Object[] args, NonEmptyKeywordDescriptor descriptor, int offset) {
        final Object[] store = PackedHashStoreLibrary.createStore();

        for (int n = 0; n < descriptor.getLength(); n++) {
            final RubySymbol symbol = descriptor.getKeywordSymbol(n);

            PackedHashStoreLibrary.setHashedKeyValue(
                    store,
                    n,
                    (int) symbol.computeHashCode(getContext().getHashing()),
                    symbol,
                    args[args.length - descriptor.getLength() + n]);
        }

        return new RubyHash(
                getContext().getCoreLibrary().hashClass,
                getLanguage().hashShape,
                getContext(),
                store,
                descriptor.getLength());
    }

    @Specialization(guards = { "!isAlsoSplat(descriptor)", "!isShort(descriptor)" })
    @TruffleBoundary
    protected RubyHash expandLong(Object[] args, NonEmptyKeywordDescriptor descriptor, int offset) {
        final Object[] keyValues = new Object[descriptor.getLength() * 2];

        for (int n = 0; n < descriptor.getLength(); n++) {
            keyValues[n * 2] = getSymbol(descriptor.getKeyword(n));
            keyValues[n * 2 + 1] = args[args.length - descriptor.getLength() + n];
        }

        return (RubyHash) RubyContext.send(getContext().getCoreLibrary().hashClass, "[]", keyValues);
    }

    protected boolean isAlsoSplat(NonEmptyKeywordDescriptor descriptor) {
        return descriptor.isAlsoSplat();
    }

    protected boolean isShort(NonEmptyKeywordDescriptor descriptor) {
        return descriptor.getLength() <= PackedHashStoreLibrary.MAX_ENTRIES;
    }

}
