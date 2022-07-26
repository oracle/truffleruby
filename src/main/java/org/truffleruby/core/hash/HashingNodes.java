/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.cast.ToRubyIntegerNode;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolNodes;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

public abstract class HashingNodes {

    @GenerateUncached
    public abstract static class ToHash extends RubyBaseNode {

        public static ToHash create() {
            return HashingNodesFactory.ToHashNodeGen.create();
        }

        public static ToHash getUncached() {
            return HashingNodesFactory.ToHashNodeGen.getUncached();
        }

        public abstract int execute(Object key, boolean compareByIdentity);

        @Specialization(guards = "!compareByIdentity")
        protected int hash(Object key, boolean compareByIdentity,
                @Cached ToHashByHashCode toHashByHashCode) {
            return toHashByHashCode.execute(key);
        }


        @Specialization(guards = "compareByIdentity")
        protected int hashCompareByIdentity(Object key, boolean compareByIdentity,
                @Cached ToHashByIdentity toHashByIdentity) {
            return toHashByIdentity.execute(key);
        }
    }

    // MRI: any_hash
    /** Keep consistent with {@link org.truffleruby.core.kernel.KernelNodes.HashNode} */
    @GenerateUncached
    @ReportPolymorphism
    public abstract static class ToHashByHashCode extends RubyBaseNode {

        public static ToHashByHashCode create() {
            return HashingNodesFactory.ToHashByHashCodeNodeGen.create();
        }

        public abstract int execute(Object key);

        @Specialization
        protected int hashBoolean(boolean value) {
            return (int) HashOperations.hashBoolean(value, getContext(), this);
        }

        @Specialization
        protected int hashInt(int value) {
            return (int) HashOperations.hashLong(value, getContext(), this);
        }

        @Specialization
        protected int hashLong(long value) {
            return (int) HashOperations.hashLong(value, getContext(), this);
        }

        @Specialization
        protected int hashDouble(double value) {
            return (int) HashOperations.hashDouble(value, getContext(), this);
        }

        @Specialization
        protected int hashBignum(RubyBignum value) {
            return (int) HashOperations.hashBignum(value, getContext(), this);
        }

        @Specialization
        protected int hashString(RubyString value,
                @Cached StringHelperNodes.HashStringNode stringHashNode) {
            return (int) stringHashNode.execute(value);
        }

        @Specialization
        protected int hashImmutableString(ImmutableRubyString value,
                @Cached StringHelperNodes.HashStringNode stringHashNode) {
            return (int) stringHashNode.execute(value);
        }

        @Specialization
        protected int hashSymbol(RubySymbol value,
                @Cached SymbolNodes.HashSymbolNode symbolHashNode) {
            return (int) symbolHashNode.execute(value);
        }

        @Fallback
        protected int hashOther(Object value,
                @Cached DispatchNode callHash,
                @Cached HashCastResultNode cast) {
            return cast.execute(callHash.call(value, "hash"));
        }
    }

    @GenerateUncached
    public abstract static class ToHashByIdentity extends RubyBaseNode {

        public static ToHashByIdentity create() {
            return HashingNodesFactory.ToHashByIdentityNodeGen.create();
        }

        public static ToHashByIdentity getUncached() {
            return HashingNodesFactory.ToHashByIdentityNodeGen.getUncached();
        }

        public abstract int execute(Object key);

        @Specialization
        protected int toHashByIdentity(Object hashed,
                @Cached ObjectIDNode objectIDNode,
                @Cached HashCastResultNode hashCastResultNode) {
            return hashCastResultNode.execute(objectIDNode.execute(hashed));
        }
    }

    @GenerateUncached
    public abstract static class HashCastResultNode extends RubyBaseNode {

        public abstract int execute(Object key);

        public static HashCastResultNode create() {
            return HashingNodesFactory.HashCastResultNodeGen.create();
        }

        @Specialization
        protected int castInt(int hashed) {
            return hashed;
        }

        @Specialization
        protected int castLong(long hashed) {
            return (int) hashed;
        }

        @Specialization
        protected int castBignum(RubyBignum hashed) {
            return BigIntegerOps.hashCode(hashed);
        }

        @Specialization(guards = "!isRubyInteger(hashed)")
        protected int castOther(Object hashed,
                @Cached ToRubyIntegerNode toRubyInteger,
                @Cached HashCastResultNode hashCastResult) {
            return hashCastResult.execute(toRubyInteger.execute(hashed));
        }
    }
}
