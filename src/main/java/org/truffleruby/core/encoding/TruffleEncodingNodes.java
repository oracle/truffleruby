/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.EncodingDB;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.Hash;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.language.control.RaiseException;

@CoreClass("Truffle::EncodingOperations")
public abstract class TruffleEncodingNodes {

    @CoreMethod(names = "default_external=", onSingleton = true, required = 1)
    public abstract static class SetDefaultExternalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject defaultExternalEncoding(DynamicObject encoding) {
            getContext().getEncodingManager().setDefaultExternalEncoding(EncodingOperations.getEncoding(encoding));
            return encoding;
        }

        @Specialization(guards = "isNil(nil)")
        public DynamicObject defaultExternal(Object nil) {
            throw new RaiseException(coreExceptions().argumentError("default external can not be nil", this));
        }

    }

    @CoreMethod(names = "default_internal=", onSingleton = true, required = 1)
    public abstract static class SetDefaultInternalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject defaultInternal(DynamicObject encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(EncodingOperations.getEncoding(encoding));
            return encoding;
        }

        @Specialization(guards = "isNil(encoding)")
        public DynamicObject defaultInternal(Object encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(null);
            return nil();
        }

    }

    @CoreMethod(names = "each_alias", onSingleton = true, needsBlock = true)
    public abstract static class EachAliasNode extends YieldingCoreMethodNode {

        @Child private org.truffleruby.core.string.StringNodes.MakeStringNode makeStringNode = org.truffleruby.core.string.StringNodes.MakeStringNode.create();

        @Specialization
        public DynamicObject eachAlias(DynamicObject block) {
            CompilerAsserts.neverPartOfCompilation();
            for (Hash.HashEntry<EncodingDB.Entry> entry : EncodingDB.getAliases().entryIterator()) {
                final CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e = (CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry>) entry;
                final DynamicObject aliasName = makeStringNode.executeMake(ArrayUtils.extractRange(e.bytes, e.p, e.end), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                yield(block, aliasName, entry.value.getIndex());
            }
            return nil();
        }
    }

}
