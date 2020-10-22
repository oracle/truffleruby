/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is transposed from org.jruby.RubyString
 * and String Support and licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1
 * used throughout.
 *
 * Copyright (C) 2001 Alan Moore <alan_moore@gmx.net>
 * Copyright (C) 2001-2002 Benoit Cerrina <b.cerrina@wanadoo.fr>
 * Copyright (C) 2001-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2006 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2004 David Corbin <dcorbin@users.sourceforge.net>
 * Copyright (C) 2005 Tim Azzopardi <tim@tigerfive.com>
 * Copyright (C) 2006 Miguel Covarrubias <mlcovarrubias@gmail.com>
 * Copyright (C) 2006 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 *
 * Some of the code in this class is transposed from org.jruby.util.ByteList,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 *
 * Copyright (C) 2007-2010 JRuby Community
 * Copyright (C) 2007 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2007 Nick Sieger <nicksieger@gmail.com>
 * Copyright (C) 2007 Ola Bini <ola@ologix.com>
 * Copyright (C) 2007 William N Dortch <bill.dortch@gmail.com>
 *
 * Some of the code in this class is transliterated from C++ code in Rubinius.
 *
 * Copyright (c) 2007-2014, Evan Phoenix and contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Rubinius nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.truffleruby.core.string;

import static org.truffleruby.core.rope.CodeRange.CR_7BIT;
import static org.truffleruby.core.rope.CodeRange.CR_BROKEN;
import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;
import static org.truffleruby.core.rope.RopeConstants.EMPTY_ASCII_8BIT_ROPE;
import static org.truffleruby.core.string.StringOperations.createString;
import static org.truffleruby.core.string.StringSupport.MBCLEN_CHARFOUND_LEN;
import static org.truffleruby.core.string.StringSupport.MBCLEN_CHARFOUND_P;
import static org.truffleruby.core.string.StringSupport.MBCLEN_INVALID_P;
import static org.truffleruby.core.string.StringSupport.MBCLEN_NEEDMORE_P;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.jcodings.Config;
import org.jcodings.Encoding;
import org.jcodings.exception.EncodingException;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.ProcOrNullNode;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.encoding.EncodingNodes.CheckEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.CheckRopeEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.NegotiateCompatibleEncodingNode;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.format.FormatExceptionTranslator;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.unpack.ArrayResult;
import org.truffleruby.core.format.unpack.UnpackCompiler;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.FixnumLowerNode;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.range.RubyIntRange;
import org.truffleruby.core.range.RubyLongRange;
import org.truffleruby.core.range.RubyObjectRange;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.ConcatRope;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.RepeatingRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeGuards;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeNodes.RepeatNode;
import org.truffleruby.core.rope.RopeNodes.SingleByteOptimizableNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.SubstringRope;
import org.truffleruby.core.string.StringNodesFactory.ByteIndexFromCharIndexNodeGen;
import org.truffleruby.core.string.StringNodesFactory.ByteSizeNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.CheckIndexNodeGen;
import org.truffleruby.core.string.StringNodesFactory.CountRopesNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.DeleteBangNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.DeleteBangRopesNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.InvertAsciiCaseBytesNodeGen;
import org.truffleruby.core.string.StringNodesFactory.InvertAsciiCaseNodeGen;
import org.truffleruby.core.string.StringNodesFactory.MakeStringNodeGen;
import org.truffleruby.core.string.StringNodesFactory.NormalizeIndexNodeGen;
import org.truffleruby.core.string.StringNodesFactory.StringAppendNodeGen;
import org.truffleruby.core.string.StringNodesFactory.StringAppendPrimitiveNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.StringAreComparableNodeGen;
import org.truffleruby.core.string.StringNodesFactory.StringByteCharacterIndexNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.StringByteSubstringPrimitiveNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.StringEqualNodeGen;
import org.truffleruby.core.string.StringNodesFactory.StringSubstringPrimitiveNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.SumNodeFactory;
import org.truffleruby.core.string.StringSupport.TrTables;
import org.truffleruby.core.support.RubyByteArray;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ReadCallerStorageNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.objects.AllocateHelperNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.language.yield.YieldNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;


@CoreModule(value = "String", isClass = true)
public abstract class StringNodes {

    @GenerateUncached
    public abstract static class MakeStringNode extends RubyBaseNode {

        public abstract RubyString executeMake(Object payload, Object encoding, Object codeRange);

        public RubyString fromRope(Rope rope) {
            return executeMake(rope, NotProvided.INSTANCE, NotProvided.INSTANCE);
        }

        public RubyString fromBuilder(RopeBuilder builder, CodeRange codeRange) {
            return executeMake(builder.getBytes(), builder.getEncoding(), codeRange);
        }

        /** All callers of this factory method must guarantee that the builder's byte array cannot change after this
         * call, otherwise the rope built from the builder will end up in an inconsistent state. */
        public RubyString fromBuilderUnsafe(RopeBuilder builder, CodeRange codeRange) {
            final byte[] unsafeBytes = builder.getUnsafeBytes();
            final byte[] ropeBytes;

            // While the caller must guarantee the builder's byte[] cannot change after this call, it's possible
            // the builder has allocated more space than it needs. Ropes require that the backing byte array
            // is the exact length required. If the builder doesn't satisfy this constraint, we must make a copy.
            // Alternatively, we could make a leaf rope and then take a substring of it, but that would complicate
            // the specializations here.
            if (unsafeBytes.length == builder.getLength()) {
                ropeBytes = unsafeBytes;
            } else {
                ropeBytes = builder.getBytes();
            }

            return executeMake(ropeBytes, builder.getEncoding(), codeRange);
        }

        public static MakeStringNode create() {
            return MakeStringNodeGen.create();
        }

        @Specialization
        protected RubyString makeStringFromRope(Rope rope, NotProvided encoding, NotProvided codeRange,
                @Cached @Shared("allocateHelper") AllocateHelperNode allocateHelperNode,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @CachedLanguage RubyLanguage language) {
            final RubyString string = new RubyString(
                    context.getCoreLibrary().stringClass,
                    RubyLanguage.stringShape,
                    false,
                    false,
                    rope);
            allocateHelperNode.trace(language, context, string);
            return string;
        }

        @Specialization
        protected RubyString makeStringFromBytes(byte[] bytes, Encoding encoding, CodeRange codeRange,
                @Cached @Shared("allocateHelper") AllocateHelperNode allocateHelperNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @CachedLanguage RubyLanguage language) {
            final LeafRope rope = makeLeafRopeNode.executeMake(bytes, encoding, codeRange, NotProvided.INSTANCE);
            final RubyString string = new RubyString(
                    context.getCoreLibrary().stringClass,
                    RubyLanguage.stringShape,
                    false,
                    false,
                    rope);
            allocateHelperNode.trace(language, context, string);
            return string;
        }

        @Specialization(guards = "is7Bit(codeRange)")
        protected RubyString makeAsciiStringFromString(String string, Encoding encoding, CodeRange codeRange) {
            final byte[] bytes = RopeOperations.encodeAsciiBytes(string);

            return executeMake(bytes, encoding, codeRange);
        }

        @Specialization(guards = "!is7Bit(codeRange)")
        protected RubyString makeStringFromString(String string, Encoding encoding, CodeRange codeRange) {
            final byte[] bytes = StringOperations.encodeBytes(string, encoding);

            return executeMake(bytes, encoding, codeRange);
        }

        protected static boolean is7Bit(CodeRange codeRange) {
            return codeRange == CR_7BIT;
        }

    }

    public static abstract class SubstringNode extends RubyContextNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        public static SubstringNode create() {
            return StringNodesFactory.SubstringNodeGen.create();
        }

        public abstract RubyString executeSubstring(RubyString string, int offset, int byteLength);

        @Specialization
        protected RubyString substring(RubyString source, int offset, int byteLength,
                @Cached AllocateHelperNode allocateHelperNode,
                @CachedLanguage RubyLanguage language) {
            final Rope rope = source.rope;

            final RubyClass logicalClass = source.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString string = new RubyString(
                    logicalClass,
                    shape,
                    false,
                    source.tainted,
                    substringNode.executeSubstring(rope, offset, byteLength));
            allocateHelperNode.trace(language, getContext(), string);
            return string;
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString allocate(RubyClass rubyClass,
                @Cached AllocateHelperNode allocateHelperNode,
                @CachedLanguage RubyLanguage language) {
            final Shape shape = allocateHelperNode.getCachedShape(rubyClass);
            final RubyString string = new RubyString(rubyClass, shape, false, false, EMPTY_ASCII_8BIT_ROPE);
            allocateHelperNode.trace(string, this, language);
            return string;
        }

    }

    @CoreMethod(names = "+", required = 1)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyNode.class)
    @ImportStatic(StringGuards.class)
    public abstract static class AddNode extends CoreMethodNode {

        @CreateCast("other")
        protected RubyNode coerceOtherToString(RubyNode other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization
        protected RubyString add(RubyString string, RubyString other,
                @Cached StringAppendNode stringAppendNode,
                @Cached AllocateHelperNode allocateHelperNode,
                @CachedLanguage RubyLanguage language) {
            final Rope concatRope = stringAppendNode.executeStringAppend(string, other);
            final boolean eitherPartTainted = string.tainted || other.tainted;

            final RubyString ret = new RubyString(
                    coreLibrary().stringClass,
                    RubyLanguage.stringShape,
                    false,
                    eitherPartTainted,
                    concatRope);
            allocateHelperNode.trace(ret, this, language);
            return ret;
        }

    }

    @CoreMethod(names = "*", required = 1, taintFrom = 0)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "times", type = RubyNode.class)
    @ImportStatic(StringGuards.class)
    public abstract static class MulNode extends CoreMethodNode {

        @Child private AllocateHelperNode allocateHelperNode = AllocateHelperNode.create();

        @CreateCast("times")
        protected RubyNode coerceToInteger(RubyNode times) {
            // Not ToIntNode, because this works with empty strings, and must throw a different error
            // for long values that don't fit in an int.
            return FixnumLowerNode.create(ToLongNode.create(times));
        }

        @Specialization(guards = "times == 0")
        protected RubyString multiplyZero(RubyString string, int times,
                @CachedLanguage RubyLanguage language) {

            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString instance = new RubyString(
                    logicalClass,
                    shape,
                    false,
                    false,
                    RopeOperations.emptyRope(string.rope.getEncoding()));
            allocateHelperNode.trace(instance, this, language);
            return instance;
        }

        @Specialization(guards = "times < 0")
        protected RubyString multiplyTimesNegative(RubyString string, long times) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative argument", this));
        }

        @Specialization(guards = { "times > 0", "!isEmpty(string)" })
        protected RubyString multiply(RubyString string, int times,
                @Cached RepeatNode repeatNode,
                @Cached BranchProfile tooBigProfile,
                @CachedLanguage RubyLanguage language) {

            long length = (long) times * string.rope.byteLength();
            if (length > Integer.MAX_VALUE) {
                tooBigProfile.enter();
                throw tooBig();
            }

            final Rope repeated = repeatNode.executeRepeat(string.rope, times);
            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString instance = new RubyString(logicalClass, shape, false, false, repeated);
            allocateHelperNode.trace(instance, this, language);
            return instance;
        }

        @Specialization(guards = { "times > 0", "isEmpty(string)" })
        protected RubyString multiplyEmpty(RubyString string, long times,
                @Cached RopeNodes.RepeatNode repeatNode,
                @CachedLanguage RubyLanguage language) {
            final Rope repeated = repeatNode.executeRepeat(string.rope, 0);

            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString instance = new RubyString(logicalClass, shape, false, false, repeated);
            allocateHelperNode.trace(instance, this, language);
            return instance;
        }

        @Specialization(guards = { "times > 0", "!isEmpty(string)" })
        protected RubyString multiplyNonEmpty(RubyString string, long times) {
            assert !CoreLibrary.fitsIntoInteger(times);
            throw tooBig();
        }

        private RaiseException tooBig() {
            // MRI throws this error whenever the total size of the resulting string would exceed LONG_MAX.
            // In TruffleRuby, strings have max length Integer.MAX_VALUE.
            return new RaiseException(getContext(), coreExceptions().argumentError("argument too big", this));
        }
    }

    @CoreMethod(names = { "==", "===", "eql?" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Child private StringEqualNode stringEqualNode = StringEqualNodeGen.create();
        @Child private KernelNodes.RespondToNode respondToNode;
        @Child private DispatchNode objectEqualNode;
        @Child private BooleanCastNode booleanCastNode;

        @Specialization
        protected boolean equal(RubyString a, RubyString b) {
            return stringEqualNode.executeStringEqual(a, b);
        }

        @Specialization(guards = "!isRubyString(b)")
        protected boolean equal(RubyString a, Object b) {
            if (respondToNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToNode = insert(KernelNodesFactory.RespondToNodeFactory.create(null, null, null));
            }

            if (respondToNode
                    .executeDoesRespondTo(null, b, coreStrings().TO_STR.createInstance(getContext()), false)) {
                if (objectEqualNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    objectEqualNode = insert(DispatchNode.create());
                }

                if (booleanCastNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    booleanCastNode = insert(BooleanCastNode.create());
                }

                return booleanCastNode.executeToBoolean(objectEqualNode.call(b, "==", a));
            }

            return false;
        }

    }

    @Primitive(name = "string_cmp")
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int compare(RubyString a, RubyString b,
                @Cached ConditionProfile sameRopeProfile,
                @Cached RopeNodes.CompareRopesNode compareNode) {
            // Taken from org.jruby.RubyString#op_cmp

            final Rope firstRope = a.rope;
            final Rope secondRope = b.rope;

            if (sameRopeProfile.profile(firstRope == secondRope)) {
                return 0;
            }

            return compareNode.execute(firstRope, secondRope);
        }

    }

    @CoreMethod(names = { "<<", "concat" }, optional = 1, rest = true, taintFrom = 1, raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class ConcatNode extends CoreMethodArrayArgumentsNode {

        public static ConcatNode create() {
            return StringNodesFactory.ConcatNodeFactory.create(null);
        }

        public abstract Object executeConcat(RubyString string, Object first, Object[] rest);

        @Specialization(guards = "rest.length == 0")
        protected RubyString concatZero(RubyString string, NotProvided first, Object[] rest) {
            return string;
        }

        @Specialization(guards = { "rest.length == 0" })
        protected RubyString concat(RubyString string, RubyString first, Object[] rest,
                @Cached StringAppendPrimitiveNode stringAppendNode) {
            return stringAppendNode.executeStringAppend(string, first);
        }

        @Specialization(guards = { "rest.length == 0", "wasProvided(first)", "!isRubyString(first)" })
        protected Object concatGeneric(RubyString string, Object first, Object[] rest,
                @Cached DispatchNode callNode) {
            return callNode.call(coreLibrary().truffleStringOperationsModule, "concat_internal", string, first);
        }

        @ExplodeLoop
        @Specialization(guards = { "wasProvided(first)", "rest.length > 0", "rest.length == " +
                "cachedLength", "cachedLength <= 8" })
        protected Object concatMany(RubyString string, Object first, Object[] rest,
                @Cached("rest.length") int cachedLength,
                @Cached ConcatNode argConcatNode,
                @Cached ConditionProfile selfArgProfile) {
            Rope rope = string.rope;
            Object result = argConcatNode.executeConcat(string, first, EMPTY_ARGUMENTS);
            for (int i = 0; i < cachedLength; ++i) {
                final Object argOrCopy = selfArgProfile.profile(rest[i] == string)
                        ? createString(getContext(), rope)
                        : rest[i];
                result = argConcatNode.executeConcat(string, argOrCopy, EMPTY_ARGUMENTS);
            }
            return result;
        }

        /** Same implementation as {@link #concatMany}, safe for the use of {@code cachedLength} */
        @Specialization(guards = { "wasProvided(first)", "rest.length > 0" }, replaces = "concatMany")
        protected Object concatManyGeneral(RubyString string, Object first, Object[] rest,
                @Cached ConcatNode argConcatNode,
                @Cached ConditionProfile selfArgProfile) {
            Rope rope = string.rope;
            Object result = argConcatNode.executeConcat(string, first, EMPTY_ARGUMENTS);
            for (Object arg : rest) {
                if (selfArgProfile.profile(arg == string)) {
                    Object copy = createString(getContext(), rope);
                    result = argConcatNode.executeConcat(string, copy, EMPTY_ARGUMENTS);
                } else {
                    result = argConcatNode.executeConcat(string, arg, EMPTY_ARGUMENTS);
                }
            }
            return result;
        }
    }

    @CoreMethod(
            names = { "[]", "slice" },
            required = 1,
            optional = 1,
            lowerFixnum = { 1, 2 },
            taintFrom = 0,
            argumentNames = { "index_start_range_string_or_regexp", "length_capture" })
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {
        //region Fields

        @Child private NormalizeIndexNode normalizeIndexNode;
        @Child private StringSubstringPrimitiveNode substringNode;
        @Child private ToLongNode toLongNode;
        @Child private RopeNodes.CharacterLengthNode charLengthNode;
        private final BranchProfile outOfBounds = BranchProfile.create();

        // endregion
        // region GetIndex Specializations

        @Specialization
        protected Object getIndex(RubyString string, int index, NotProvided length) {
            return index == charLength(string) // Check for the only difference from str[index, 1]
                    ? outOfBoundsNil()
                    : substring(string, index, 1);
        }

        @Specialization
        protected Object getIndex(RubyString string, long index, NotProvided length) {
            assert (int) index != index; // verified via lowerFixnum
            return outOfBoundsNil();
        }

        @Specialization(guards = { "!isRubyRange(index)", "!isRubyRegexp(index)", "!isRubyString(index)" })
        protected Object getIndex(RubyString string, Object index, NotProvided length) {
            long indexLong = toLong(index);
            int indexInt = (int) indexLong;
            return indexInt != indexLong
                    ? outOfBoundsNil()
                    : getIndex(string, indexInt, length);
        }

        // endregion
        // region Two-Arg Slice Specializations

        @Specialization
        protected Object slice(RubyString string, int start, int length) {
            return substring(string, start, length);
        }

        @Specialization
        protected Object slice(RubyString string, long start, long length) {
            int lengthInt = (int) length;
            if (lengthInt != length) {
                lengthInt = Integer.MAX_VALUE; // go to end of string
            }

            final int startInt = (int) start;
            return startInt != start
                    ? outOfBoundsNil()
                    : substring(string, startInt, lengthInt);
        }

        @Specialization(guards = "wasProvided(length)")
        protected Object slice(RubyString string, long start, Object length) {
            return slice(string, start, toLong(length));
        }

        @Specialization(
                guards = {
                        "!isRubyRange(start)",
                        "!isRubyRegexp(start)",
                        "!isRubyString(start)",
                        "wasProvided(length)" })
        protected Object slice(RubyString string, Object start, Object length) {
            return slice(string, toLong(start), toLong(length));
        }

        // endregion
        // region Range Slice Specializations

        @Specialization
        protected Object sliceIntegerRange(RubyString string, RubyIntRange range, NotProvided length) {
            return sliceRange(string, range.begin, range.end, range.excludedEnd);
        }

        @Specialization
        protected Object sliceLongRange(RubyString string, RubyLongRange range, NotProvided length) {
            return sliceRange(string, range.begin, range.end, range.excludedEnd);
        }

        @Specialization(guards = "range.isEndless()")
        protected Object sliceEndlessRange(RubyString string, RubyObjectRange range, NotProvided length) {
            final int stringEnd = range.excludedEnd ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
            return sliceRange(string, toLong(range.begin), stringEnd, range.excludedEnd);
        }

        @Specialization(guards = "range.isBounded()")
        protected Object sliceObjectRange(RubyString string, RubyObjectRange range, NotProvided length) {
            return sliceRange(string, toLong(range.begin), toLong(range.end), range.excludedEnd);
        }

        // endregion
        // region Range Slice Logic

        private Object sliceRange(RubyString string, long begin, long end, boolean excludesEnd) {
            final int beginInt = (int) begin;
            if (beginInt != begin) {
                return outOfBoundsNil();
            }

            int endInt = (int) end;
            if (endInt != end) {
                // Get until the end of the string.
                endInt = excludesEnd ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
            }

            return sliceRange(string, beginInt, endInt, excludesEnd);
        }

        private Object sliceRange(RubyString string, int begin, int end, boolean excludesEnd) {
            final int stringLength = charLength(string);
            begin = normalizeIndex(begin, stringLength);
            if (begin < 0 || begin > stringLength) {
                return outOfBoundsNil();
            }
            end = normalizeIndex(end, stringLength);
            int length = StringOperations.clampExclusiveIndex(stringLength, excludesEnd ? end : end + 1) - begin;
            return substring(string, begin, Math.max(length, 0));
        }

        // endregion
        // region Regexp Slice Specializations

        @Specialization
        protected Object sliceCapture0(VirtualFrame frame, RubyString string, RubyRegexp regexp, NotProvided capture,
                @Cached DispatchNode callNode,
                @Cached ReadCallerStorageNode readCallerNode,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            return sliceCapture(
                    frame,
                    string,
                    regexp,
                    0,
                    callNode,
                    readCallerNode,
                    unsetProfile,
                    sameThreadProfile);
        }

        @Specialization(guards = "wasProvided(capture)")
        protected Object sliceCapture(VirtualFrame frame, RubyString string, RubyRegexp regexp, Object capture,
                @Cached DispatchNode callNode,
                @Cached ReadCallerStorageNode readCallerStorageNode,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            final Object matchStrPair = callNode.call(string, "subpattern", regexp, capture);

            final SpecialVariableStorage storage = readCallerStorageNode.execute(frame);
            if (matchStrPair == nil) {
                storage.setLastMatch(nil, getContext(), unsetProfile, sameThreadProfile);
                return nil;
            } else {
                final Object[] array = (Object[]) ((RubyArray) matchStrPair).store;
                storage.setLastMatch(array[0], getContext(), unsetProfile, sameThreadProfile);
                return array[1];
            }
        }

        // endregion
        // region String Slice Specialization

        @Specialization
        protected Object slice2(RubyString string, RubyString matchStr, NotProvided length,
                @Cached DispatchNode includeNode,
                @Cached BooleanCastNode booleanCastNode,
                @Cached DispatchNode dupNode) {

            final Object included = includeNode.call(string, "include?", matchStr);

            if (booleanCastNode.executeToBoolean(included)) {
                throw new TaintResultNode.DoNotTaint(dupNode.call(matchStr, "dup"));
            }

            return nil;
        }

        // endregion
        // region Helpers

        private Object outOfBoundsNil() {
            outOfBounds.enter();
            return nil;
        }

        private Object substring(RubyString string, int start, int length) {
            if (substringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                substringNode = insert(StringSubstringPrimitiveNodeFactory.create(null));
            }

            return substringNode.execute(string, start, length);
        }

        private long toLong(Object value) {
            if (toLongNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toLongNode = insert(ToLongNode.create());
            }

            // The long cast is necessary to avoid the invalid `(long) Integer` situation.
            return toLongNode.execute(value);
        }

        private int charLength(RubyString string) {
            if (charLengthNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                charLengthNode = insert(RopeNodes.CharacterLengthNode.create());
            }

            return charLengthNode.execute(string.rope);
        }

        private int normalizeIndex(int index, int length) {
            if (normalizeIndexNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                normalizeIndexNode = insert(NormalizeIndexNode.create());
            }

            return normalizeIndexNode.executeNormalize(index, length);
        }

        // endregion
    }

    @CoreMethod(names = "ascii_only?")
    public abstract static class ASCIIOnlyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean asciiOnly(RubyString string,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            final CodeRange codeRange = codeRangeNode.execute(string.rope);

            return codeRange == CR_7BIT;
        }

    }

    @CoreMethod(names = "bytes", needsBlock = true)
    public abstract static class BytesNode extends YieldingCoreMethodNode {

        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();

        @Specialization
        protected RubyArray bytes(VirtualFrame frame, RubyString string, NotProvided block) {
            final Rope rope = string.rope;
            final byte[] bytes = bytesNode.execute(rope);

            final int[] store = new int[bytes.length];

            for (int n = 0; n < store.length; n++) {
                store[n] = bytes[n] & 0xFF;
            }

            return createArray(store);
        }

        @Specialization
        protected RubyString bytes(RubyString string, RubyProc block) {
            Rope rope = string.rope;
            byte[] bytes = bytesNode.execute(rope);

            for (int i = 0; i < bytes.length; i++) {
                yield(block, bytes[i] & 0xff);
            }

            return string;
        }

    }

    @CoreMethod(names = "bytesize")
    public abstract static class ByteSizeNode extends CoreMethodArrayArgumentsNode {

        public static ByteSizeNode create() {
            return ByteSizeNodeFactory.create(null);
        }

        public abstract int executeByteSize(RubyString string);

        @Specialization
        protected int byteSize(RubyString string) {
            return string.rope.byteLength();
        }

    }

    @Primitive(name = "string_casecmp")
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyNode.class)
    public abstract static class CaseCmpNode extends PrimitiveNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleEncodingNode
                .create();
        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();
        private final ConditionProfile incompatibleEncodingProfile = ConditionProfile.create();

        @CreateCast("other")
        protected RubyNode coerceOtherToString(RubyNode other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization(guards = { "bothSingleByteOptimizable(string, other)" })
        protected Object caseCmpSingleByte(RubyString string, RubyString other) {
            // Taken from org.jruby.RubyString#casecmp19.

            final Encoding encoding = negotiateCompatibleEncodingNode.executeNegotiate(string, other);
            if (incompatibleEncodingProfile.profile(encoding == null)) {
                return nil;
            }

            return RopeOperations.caseInsensitiveCmp(string.rope, other.rope);
        }

        @Specialization(guards = { "!bothSingleByteOptimizable(string, other)" })
        protected Object caseCmp(RubyString string, RubyString other) {
            // Taken from org.jruby.RubyString#casecmp19 and

            final Encoding encoding = negotiateCompatibleEncodingNode.executeNegotiate(string, other);

            if (incompatibleEncodingProfile.profile(encoding == null)) {
                return nil;
            }

            return StringSupport.multiByteCasecmp(encoding, string.rope, other.rope);
        }

        protected boolean bothSingleByteOptimizable(RubyString string, RubyString other) {
            return singleByteOptimizableNode.execute(string.rope) && singleByteOptimizableNode.execute(other.rope);
        }
    }

    @CoreMethod(names = "count", rest = true)
    public abstract static class CountNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStr = ToStrNode.create();
        @Child private CountRopesNode countRopesNode = CountRopesNode.create();

        @Specialization(guards = "args.length == size", limit = "getDefaultCacheLimit()")
        protected int count(VirtualFrame frame, RubyString string, Object[] args,
                @Cached("args.length") int size) {
            final Rope[] ropes = argRopes(frame, args, size);
            return countRopesNode.executeCount(string, ropes);
        }

        @Specialization(replaces = "count")
        protected int countSlow(VirtualFrame frame, RubyString string, Object[] args) {
            final Rope[] ropes = argRopesSlow(frame, args);
            return countRopesNode.executeCount(string, ropes);
        }

        @ExplodeLoop
        protected Rope[] argRopes(VirtualFrame frame, Object[] args, int size) {
            final Rope[] strs = new Rope[args.length];
            for (int i = 0; i < size; i++) {
                strs[i] = toStr.executeToStr(args[i]).rope;
            }
            return strs;
        }

        protected Rope[] argRopesSlow(VirtualFrame frame, Object[] args) {
            final Rope[] strs = new Rope[args.length];
            for (int i = 0; i < args.length; i++) {
                strs[i] = toStr.executeToStr(args[i]).rope;
            }
            return strs;
        }
    }

    @ImportStatic({ StringGuards.class, StringOperations.class })
    public abstract static class CountRopesNode extends TrTableNode {

        public static CountRopesNode create() {
            return CountRopesNodeFactory.create(null);
        }

        public abstract int executeCount(RubyString string, Rope[] ropes);

        @Specialization(guards = "isEmpty(string)")
        protected int count(RubyString string, Object[] args) {
            return 0;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!isEmpty(string)",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args)",
                        "encodingsMatch(string, cachedEncoding)" })
        protected int countFast(RubyString string, Rope[] args,
                @Cached(value = "args", dimensions = 1) Rope[] cachedArgs,
                @Cached("string.rope.encoding") Encoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(string, cachedArgs)") Encoding compatEncoding,
                @Cached("makeTables(string, cachedArgs, squeeze, compatEncoding)") TrTables tables) {
            return processStr(string, squeeze, compatEncoding, tables);
        }

        @TruffleBoundary
        private int processStr(RubyString string, boolean[] squeeze, Encoding compatEncoding, TrTables tables) {
            return StringSupport.strCount(string.rope, squeeze, tables, compatEncoding);
        }

        @Specialization(guards = "!isEmpty(string)")
        protected int count(RubyString string, Rope[] ropes,
                @Cached BranchProfile errorProfile) {
            if (ropes.length == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            Encoding enc = findEncoding(string, ropes);
            return countSlow(string, ropes, enc);
        }

        @TruffleBoundary
        private int countSlow(RubyString string, Rope[] ropes, Encoding enc) {
            final boolean[] table = squeeze();
            final StringSupport.TrTables tables = makeTables(string, ropes, table, enc);
            return processStr(string, table, enc, tables);
        }
    }

    public abstract static class TrTableNode extends CoreMethodArrayArgumentsNode {
        @Child protected CheckRopeEncodingNode checkEncodingNode = CheckRopeEncodingNode.create();
        @Child protected RopeNodes.EqualNode ropeEqualNode = RopeNodes.EqualNode.create();

        protected boolean[] squeeze() {
            return new boolean[StringSupport.TRANS_SIZE + 1];
        }

        protected Encoding findEncoding(RubyString string, Rope[] ropes) {
            final Rope rope = string.rope;
            Encoding enc = checkEncodingNode.executeCheckEncoding(rope, ropes[0]);
            for (int i = 1; i < ropes.length; i++) {
                enc = checkEncodingNode.executeCheckEncoding(rope, ropes[i]);
            }
            return enc;
        }

        protected TrTables makeTables(RubyString string, Rope[] ropes, boolean[] squeeze, Encoding enc) {
            // The trSetupTable method will consume the bytes from the rope one encoded character at a time and
            // build a TrTable from this. Previously we started with the encoding of rope zero, and at each
            // stage found a compatible encoding to build that TrTable with. Although we now calculate a single
            // encoding with which to build the tables it must be compatible with all ropes, so will not
            // affect the consumption of characters from those ropes.
            StringSupport.TrTables tables = StringSupport.trSetupTable(ropes[0], squeeze, null, true, enc);

            for (int i = 1; i < ropes.length; i++) {
                tables = StringSupport.trSetupTable(ropes[i], squeeze, tables, false, enc);
            }
            return tables;
        }

        protected boolean encodingsMatch(RubyString string, Encoding encoding) {
            return encoding == string.rope.getEncoding();
        }

        @ExplodeLoop
        protected boolean argsMatch(Rope[] cachedRopes, Rope[] ropes) {
            for (int i = 0; i < cachedRopes.length; i++) {
                if (!ropeEqualNode.execute(cachedRopes[i], ropes[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    @CoreMethod(names = "delete!", rest = true, raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class DeleteBangNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStr = ToStrNode.create();
        @Child private DeleteBangRopesNode deleteBangRopesNode = DeleteBangRopesNode.create();

        public static DeleteBangNode create() {
            return DeleteBangNodeFactory.create(null);
        }

        public abstract Object executeDeleteBang(RubyString string, Object[] args);

        @Specialization(guards = "args.length == size", limit = "getDefaultCacheLimit()")
        protected Object deleteBang(RubyString string, Object[] args,
                @Cached("args.length") int size) {
            final Rope[] ropes = argRopes(args, size);
            return deleteBangRopesNode.executeDeleteBang(string, ropes);
        }

        @Specialization(replaces = "deleteBang")
        protected Object deleteBangSlow(RubyString string, Object[] args) {
            final Rope[] ropes = argRopesSlow(args);
            return deleteBangRopesNode.executeDeleteBang(string, ropes);
        }

        @ExplodeLoop
        protected Rope[] argRopes(Object[] args, int size) {
            final Rope[] strs = new Rope[size];
            for (int i = 0; i < size; i++) {
                strs[i] = toStr.executeToStr(args[i]).rope;
            }
            return strs;
        }

        protected Rope[] argRopesSlow(Object[] args) {
            final Rope[] strs = new Rope[args.length];
            for (int i = 0; i < args.length; i++) {
                strs[i] = toStr.executeToStr(args[i]).rope;
            }
            return strs;
        }
    }

    @ImportStatic({ StringGuards.class, StringOperations.class })
    public abstract static class DeleteBangRopesNode extends TrTableNode {

        public static DeleteBangRopesNode create() {
            return DeleteBangRopesNodeFactory.create(null);
        }

        public abstract Object executeDeleteBang(RubyString string, Rope[] ropes);

        @Specialization(guards = "isEmpty(string)")
        protected Object deleteBangEmpty(RubyString string, Object[] args) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmpty(string)",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args)",
                        "encodingsMatch(string, cachedEncoding)" })
        protected Object deleteBangFast(RubyString string, Rope[] args,
                @Cached(value = "args", dimensions = 1) Rope[] cachedArgs,
                @Cached("string.rope.encoding") Encoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(string, cachedArgs)") Encoding compatEncoding,
                @Cached("makeTables(string, cachedArgs, squeeze, compatEncoding)") TrTables tables,
                @Cached BranchProfile nullProfile) {
            final Rope processedRope = processStr(string, squeeze, compatEncoding, tables);
            if (processedRope == null) {
                nullProfile.enter();
                return nil;
            }

            StringOperations.setRope(string, processedRope);
            return string;
        }

        @Specialization(guards = "!isEmpty(string)")
        protected Object deleteBang(RubyString string, Rope[] args,
                @Cached BranchProfile errorProfile) {
            if (args.length == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            Encoding enc = findEncoding(string, args);

            return deleteBangSlow(string, args, enc);
        }

        @TruffleBoundary
        private Object deleteBangSlow(RubyString string, Rope[] ropes, Encoding enc) {
            final boolean[] squeeze = new boolean[StringSupport.TRANS_SIZE + 1];

            final StringSupport.TrTables tables = makeTables(string, ropes, squeeze, enc);

            final Rope processedRope = processStr(string, squeeze, enc, tables);
            if (processedRope == null) {
                return nil;
            }

            StringOperations.setRope(string, processedRope);

            return string;
        }

        @TruffleBoundary
        private Rope processStr(RubyString string, boolean[] squeeze, Encoding enc, StringSupport.TrTables tables) {
            return StringSupport.delete_bangCommon19(string.rope, squeeze, tables, enc);
        }
    }

    @Primitive(name = "string_downcase!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringDowncaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createUpperToLower()") InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CharacterLengthNode characterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile dummyEncodingProfile,
                @Cached ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding encoding = rope.getEncoding();

            if (dummyEncodingProfile.profile(encoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding, this));
            }

            final CodeRange cr = codeRangeNode.execute(rope);
            final byte[] inputBytes = bytesNode.execute(rope);
            final byte[] outputBytes = StringSupport.downcaseMultiByteAsciiSimple(encoding, cr, inputBytes);

            if (modifiedProfile.profile(inputBytes != outputBytes)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode.executeMake(outputBytes, encoding, cr, characterLengthNode.execute(rope)));
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = { "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile dummyEncodingProfile,
                @Cached ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding encoding = rope.getEncoding();

            if (dummyEncodingProfile.profile(encoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding, this));
            }

            final RopeBuilder builder = RopeBuilder.createRopeBuilder(bytesNode.execute(rope), rope.getEncoding());
            final boolean modified = StringSupport
                    .downcaseMultiByteComplex(encoding, codeRangeNode.execute(rope), builder, caseMappingOptions);

            if (modifiedProfile.profile(modified)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode
                                .executeMake(builder.getBytes(), rope.getEncoding(), CR_UNKNOWN, NotProvided.INSTANCE));

                return string;
            } else {
                return nil;
            }
        }

    }

    @CoreMethod(names = "each_byte", needsBlock = true, enumeratorSize = "bytesize")
    public abstract static class EachByteNode extends YieldingCoreMethodNode {

        @SuppressFBWarnings("SA")
        @Specialization
        protected RubyString eachByte(RubyString string, RubyProc block,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.BytesNode updatedBytesNode,
                @Cached ConditionProfile ropeChangedProfile) {
            Rope rope = string.rope;
            byte[] bytes = bytesNode.execute(rope);

            for (int i = 0; i < bytes.length; i++) {
                yield(block, bytes[i] & 0xff);

                Rope updatedRope = string.rope;
                if (ropeChangedProfile.profile(rope != updatedRope)) {
                    rope = updatedRope;
                    bytes = updatedBytesNode.execute(updatedRope);
                }
            }

            return string;
        }

    }

    @CoreMethod(names = "each_char", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(StringGuards.class)
    public abstract static class EachCharNode extends YieldingCoreMethodNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();
        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();

        @Specialization
        protected RubyString eachChar(RubyString string, RubyProc block,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached AllocateHelperNode allocateHelperNode,
                @CachedLanguage RubyLanguage language) {
            final Rope rope = string.rope;
            final byte[] ptrBytes = bytesNode.execute(rope);
            final int len = ptrBytes.length;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);

            int n;

            for (int i = 0; i < len; i += n) {
                n = calculateCharacterLengthNode.characterLengthWithRecovery(enc, cr, ptrBytes, i, len);

                yield(block, substr(language, allocateHelperNode, rope, string, i, n));
            }

            return string;
        }

        // TODO (nirvdrum 10-Mar-15): This was extracted from JRuby, but likely will need to become a primitive.
        // Don't be tempted to extract the rope from the passed string. If the block being yielded to modifies the
        // source string, you'll get a different rope. Unlike String#each_byte, String#each_char does not make
        // modifications to the string visible to the rest of the iteration.
        private Object substr(RubyLanguage language, AllocateHelperNode allocateHelperNode, Rope rope,
                RubyString string, int beg, int len) {
            int length = rope.byteLength();
            if (len < 0 || beg > length) {
                return nil;
            }

            if (beg < 0) {
                beg += length;
                if (beg < 0) {
                    return nil;
                }
            }

            int end = Math.min(length, beg + len);

            final Rope substringRope = substringNode.executeSubstring(rope, beg, end - beg);

            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString ret = new RubyString(logicalClass, shape, false, string.tainted, substringRope);
            allocateHelperNode.trace(ret, this, language);
            return ret;
        }
    }

    @CoreMethod(names = "force_encoding", required = 1, raiseIfFrozenSelf = true)
    public abstract static class ForceEncodingNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.WithEncodingNode withEncodingNode = RopeNodes.WithEncodingNode.create();
        private final ConditionProfile differentEncodingProfile = ConditionProfile.create();

        @Specialization
        protected RubyString forceEncodingString(RubyString string, RubyString encoding,
                @Cached BranchProfile errorProfile) {
            final String stringName = encoding.getJavaString();
            final RubyEncoding rubyEncoding = getContext().getEncodingManager().getRubyEncoding(stringName);

            if (rubyEncoding == null) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(Utils.concat("unknown encoding name - ", stringName), this));
            }

            return forceEncodingEncoding(string, rubyEncoding);
        }

        @Specialization
        protected RubyString forceEncodingEncoding(RubyString string, RubyEncoding encoding) {
            final Encoding javaEncoding = encoding.encoding;
            final Rope rope = string.rope;

            if (differentEncodingProfile.profile(rope.getEncoding() != javaEncoding)) {
                final Rope newRope = withEncodingNode.executeWithEncoding(rope, javaEncoding);
                StringOperations.setRope(string, newRope);
            }

            return string;
        }

        @Specialization(guards = { "!isRubyString(encoding)", "!isRubyEncoding(encoding)" })
        protected RubyString forceEncoding(VirtualFrame frame, RubyString string, Object encoding,
                @Cached ToStrNode toStrNode,
                @Cached BranchProfile errorProfile) {
            return forceEncodingString(string, toStrNode.executeToStr(encoding), errorProfile);
        }

    }

    @CoreMethod(names = "getbyte", required = 1, lowerFixnum = 1)
    public abstract static class GetByteNode extends CoreMethodArrayArgumentsNode {

        @Child private NormalizeIndexNode normalizeIndexNode = NormalizeIndexNode.create();
        @Child private RopeNodes.GetByteNode ropeGetByteNode = RopeNodes.GetByteNode.create();

        @Specialization
        protected Object getByte(RubyString string, int index,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile) {
            final Rope rope = string.rope;
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, rope.byteLength());

            if (indexOutOfBoundsProfile.profile((normalizedIndex < 0) || (normalizedIndex >= rope.byteLength()))) {
                return nil;
            }

            return ropeGetByteNode.executeGetByte(rope, normalizedIndex);
        }

    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        protected static final int CLASS_SALT = 54008340; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        public static HashNode create() {
            return StringNodesFactory.HashNodeFactory.create(null);
        }

        public abstract long execute(RubyString string);

        @Specialization
        protected long hash(RubyString string,
                @Cached RopeNodes.HashNode hashNode) {
            return getContext().getHashing(this).hash(CLASS_SALT, hashNode.execute(string.rope));
        }

    }

    @Primitive(name = "string_initialize")
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString initializeJavaString(RubyString string, String from, RubyEncoding encoding) {
            StringOperations
                    .setRope(string, StringOperations.encodeRope(from, encoding.encoding));
            return string;
        }

        @Specialization
        protected RubyString initializeJavaStringNoEncoding(RubyString string, String from, Nil encoding) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().argumentError(
                            "String.new(javaString) needs to be called with an Encoding like String.new(javaString, encoding: someEncoding)",
                            this));
        }

        @Specialization
        protected RubyString initialize(RubyString string, RubyString from, Object encoding) {
            StringOperations.setRope(string, from.rope);
            return string;
        }

        @Specialization(guards = { "!isRubyString(from)", "!isString(from)" })
        protected RubyString initialize(VirtualFrame frame, RubyString string, Object from, Object encoding,
                @Cached ToStrNode toStrNode) {
            StringOperations.setRope(string, toStrNode.executeToStr(from).rope);
            return string;
        }

    }

    @Primitive(name = "string_get_coderange")
    public abstract static class GetCodeRangeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int getCodeRange(RubyString str,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            return codeRangeNode.execute(str.rope).toInt();
        }

    }

    @Primitive(name = "string_get_rope")
    public abstract static class GetRopeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Rope getRope(RubyString str) {
            return str.rope;
        }
    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private WriteObjectFieldNode writeAssociatedNode; // for synchronization

        @Specialization(guards = "self == from")
        protected Object initializeCopySelfIsSameAsFrom(RubyString self, RubyString from) {
            return self;
        }


        @Specialization(guards = { "self != from", "!isNativeRope(from)" }, limit = "getDynamicObjectCacheLimit()")
        protected Object initializeCopy(RubyString self, RubyString from,
                @CachedLibrary("from") DynamicObjectLibrary fromLibrary) {
            StringOperations.setRope(self, from.rope);
            copyAssociated(self, from, fromLibrary);
            return self;
        }

        @Specialization(guards = { "self != from", "isNativeRope(from)" }, limit = "getDynamicObjectCacheLimit()")
        protected Object initializeCopyFromNative(RubyString self, RubyString from,
                @CachedLibrary("from") DynamicObjectLibrary fromLibrary) {
            StringOperations.setRope(self, ((NativeRope) from.rope).makeCopy(getContext().getFinalizationService()));
            copyAssociated(self, from, fromLibrary);
            return self;
        }

        private void copyAssociated(RubyString self, RubyString from, DynamicObjectLibrary fromLibrary) {
            final Object associated = fromLibrary.getOrDefault(from, Layouts.ASSOCIATED_IDENTIFIER, null);
            if (associated != null) {
                if (writeAssociatedNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    writeAssociatedNode = insert(WriteObjectFieldNode.create());
                }

                writeAssociatedNode.execute(self, Layouts.ASSOCIATED_IDENTIFIER, associated);
            }
        }

        protected boolean isNativeRope(RubyString other) {
            return other.rope instanceof NativeRope;
        }
    }

    @CoreMethod(names = "lstrip!", raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class LstripBangNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.GetCodePointNode getCodePointNode = RopeNodes.GetCodePointNode.create();
        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        @Specialization(guards = "isEmpty(string)")
        protected Object lstripBangEmptyString(RubyString string) {
            return nil;
        }

        @Specialization(guards = { "!isEmpty(string)", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object lstripBangSingleByte(RubyString string,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached ConditionProfile noopProfile) {
            // Taken from org.jruby.RubyString#lstrip_bang19 and org.jruby.RubyString#singleByteLStrip.

            final Rope rope = string.rope;
            final int firstCodePoint = getCodePointNode.executeGetCodePoint(rope, 0);

            // Check the first code point to see if it's a space. In the case of strings without leading spaces,
            // this check can avoid having to materialize the entire byte[] (a potentially expensive operation
            // for ropes) and can avoid having to compile the while loop.
            if (noopProfile.profile(!StringSupport.isAsciiSpace(firstCodePoint))) {
                return nil;
            }

            final int end = rope.byteLength();
            final byte[] bytes = bytesNode.execute(rope);

            int p = 0;
            while (p < end && StringSupport.isAsciiSpace(bytes[p])) {
                p++;
            }

            StringOperations.setRope(string, substringNode.executeSubstring(rope, p, end - p));

            return string;
        }

        @TruffleBoundary
        @Specialization(guards = { "!isEmpty(string)", "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object lstripBang(RubyString string,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached EncodingNodes.GetActualEncodingNode getActualEncodingNode) {
            // Taken from org.jruby.RubyString#lstrip_bang19 and org.jruby.RubyString#multiByteLStrip.

            final Rope rope = string.rope;
            final Encoding enc = getActualEncodingNode.execute(rope);
            final int s = 0;
            final int end = s + rope.byteLength();

            int p = s;
            while (p < end) {
                int c = getCodePointNode.executeGetCodePoint(rope, p);
                if (!ASCIIEncoding.INSTANCE.isSpace(c)) {
                    break;
                }
                p += StringSupport.codeLength(enc, c);
            }

            if (p > s) {
                StringOperations.setRope(string, substringNode.executeSubstring(rope, p - s, end - p));

                return string;
            }

            return nil;
        }
    }

    @CoreMethod(names = "ord")
    @ImportStatic(StringGuards.class)
    public abstract static class OrdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isEmpty(string)")
        protected int ordEmpty(RubyString string) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("empty string", this));
        }

        @Specialization(guards = "!isEmpty(string)")
        protected int ord(RubyString string,
                @Cached RopeNodes.GetCodePointNode getCodePointNode) {
            return getCodePointNode.executeGetCodePoint(string.rope, 0);
        }

    }

    @CoreMethod(names = "replace", required = 1, raiseIfFrozenSelf = true, taintFrom = 1)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyNode.class)
    public abstract static class ReplaceNode extends CoreMethodNode {

        @CreateCast("other")
        protected RubyNode coerceOtherToString(RubyNode other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization(guards = "string == other")
        protected RubyString replaceStringIsSameAsOther(RubyString string, RubyString other) {
            return string;
        }


        @Specialization(guards = { "string != other" })
        protected RubyString replace(RubyString string, RubyString other) {
            StringOperations.setRope(string, other.rope);
            return string;
        }

    }

    @CoreMethod(names = "rstrip!", raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class RstripBangNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.GetCodePointNode getCodePointNode = RopeNodes.GetCodePointNode.create();
        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();
        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        @Specialization(guards = "isEmpty(string)")
        protected Object rstripBangEmptyString(RubyString string) {
            return nil;
        }

        @Specialization(guards = { "!isEmpty(string)", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object rstripBangSingleByte(RubyString string,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached ConditionProfile noopProfile) {
            // Taken from org.jruby.RubyString#rstrip_bang19 and org.jruby.RubyString#singleByteRStrip19.

            final Rope rope = string.rope;
            final int lastCodePoint = getCodePointNode.executeGetCodePoint(rope, rope.byteLength() - 1);

            // Check the last code point to see if it's a space or NULL. In the case of strings without leading spaces,
            // this check can avoid having to materialize the entire byte[] (a potentially expensive operation
            // for ropes) and can avoid having to compile the while loop.
            final boolean willStrip = lastCodePoint == 0x00 || StringSupport.isAsciiSpace(lastCodePoint);
            if (noopProfile.profile(!willStrip)) {
                return nil;
            }

            final int end = rope.byteLength();
            final byte[] bytes = bytesNode.execute(rope);

            int endp = end - 1;
            while (endp >= 0 && (bytes[endp] == 0 || StringSupport.isAsciiSpace(bytes[endp]))) {
                endp--;
            }

            StringOperations.setRope(string, substringNode.executeSubstring(rope, 0, endp + 1));

            return string;
        }

        @TruffleBoundary
        @Specialization(guards = { "!isEmpty(string)", "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object rstripBang(RubyString string,
                @Cached EncodingNodes.GetActualEncodingNode getActualEncodingNode,
                @Cached ConditionProfile dummyEncodingProfile) {
            // Taken from org.jruby.RubyString#rstrip_bang19 and org.jruby.RubyString#multiByteRStrip19.

            final Rope rope = string.rope;
            final Encoding enc = getActualEncodingNode.execute(rope);

            if (dummyEncodingProfile.profile(enc.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc, this));
            }

            final byte[] bytes = rope.getBytes();
            final int start = 0;
            final int end = rope.byteLength();

            int endp = end;
            int prev;
            while ((prev = prevCharHead(enc, bytes, start, endp, end)) != -1) {
                int point = getCodePointNode.executeGetCodePoint(rope, prev);
                if (point != 0 && !ASCIIEncoding.INSTANCE.isSpace(point)) {
                    break;
                }
                endp = prev;
            }

            if (endp < end) {
                StringOperations.setRope(string, substringNode.executeSubstring(rope, 0, endp - start));

                return string;
            }
            return nil;
        }

        @TruffleBoundary
        private int prevCharHead(Encoding enc, byte[] bytes, int p, int s, int end) {
            return enc.prevCharHead(bytes, p, s, end);
        }
    }

    @Primitive(name = "string_scrub")
    @ImportStatic(StringGuards.class)
    public abstract static class ScrubNode extends PrimitiveArrayArgumentsNode {

        @Child private YieldNode yieldNode = YieldNode.create();
        @Child RopeNodes.CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();
        @Child private RopeNodes.ConcatNode concatNode = RopeNodes.ConcatNode.create();
        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();
        @Child private MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
        @Child private RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode = RopeNodes.CalculateCharacterLengthNode
                .create();
        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();

        @Specialization(guards = { "isBrokenCodeRange(string, codeRangeNode)", "isAsciiCompatible(string)" })
        protected RubyString scrubAsciiCompat(RubyString string, RubyProc block) {
            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);
            Rope buf = RopeConstants.EMPTY_ASCII_8BIT_ROPE;

            final byte[] pBytes = bytesNode.execute(rope);
            final int e = pBytes.length;

            int p = 0;
            int p1 = 0;

            p = StringSupport.searchNonAscii(pBytes, p, e);
            if (p == -1) {
                p = e;
            }
            while (p < e) {
                int ret = calculateCharacterLengthNode.characterLength(enc, CR_BROKEN, pBytes, p, e);
                if (MBCLEN_NEEDMORE_P(ret)) {
                    break;
                } else if (MBCLEN_CHARFOUND_P(ret)) {
                    p += MBCLEN_CHARFOUND_LEN(ret);
                } else if (MBCLEN_INVALID_P(ret)) {
                    // p1~p: valid ascii/multibyte chars
                    // p ~e: invalid bytes + unknown bytes
                    int clen = enc.maxLength();
                    if (p1 < p) {
                        buf = concatNode.executeConcat(buf, substringNode.executeSubstring(rope, p1, p - p1), enc);
                    }

                    if (e - p < clen) {
                        clen = e - p;
                    }
                    if (clen <= 2) {
                        clen = 1;
                    } else {
                        clen--;
                        for (; clen > 1; clen--) {
                            ret = StringSupport.characterLength(enc, cr, pBytes, p, p + clen);
                            if (MBCLEN_NEEDMORE_P(ret)) {
                                break;
                            }
                        }
                    }
                    RubyString repl = (RubyString) yield(
                            block,
                            makeStringNode.fromRope(substringNode.executeSubstring(rope, p, clen)));
                    buf = concatNode.executeConcat(buf, repl.rope, enc);
                    p += clen;
                    p1 = p;
                    p = StringSupport.searchNonAscii(pBytes, p, e);
                    if (p == -1) {
                        p = e;
                        break;
                    }
                }
            }
            if (p1 < p) {
                buf = concatNode.executeConcat(buf, substringNode.executeSubstring(rope, p1, p - p1), enc);
            }
            if (p < e) {
                RubyString repl = (RubyString) yield(
                        block,
                        makeStringNode.fromRope(substringNode.executeSubstring(rope, p, e - p)));
                buf = concatNode.executeConcat(buf, repl.rope, enc);
            }

            return makeStringNode.fromRope(buf);
        }

        @Specialization(guards = { "isBrokenCodeRange(string, codeRangeNode)", "!isAsciiCompatible(string)" })
        protected RubyString scrubAsciiIncompatible(RubyString string, RubyProc block,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode) {
            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);
            Rope buf = RopeConstants.EMPTY_ASCII_8BIT_ROPE;

            final byte[] pBytes = bytesNode.execute(rope);
            final int e = pBytes.length;

            int p = 0;
            int p1 = 0;
            final int mbminlen = enc.minLength();

            while (p < e) {
                int ret = calculateCharacterLengthNode.characterLength(enc, CR_BROKEN, pBytes, p, e);
                if (MBCLEN_NEEDMORE_P(ret)) {
                    break;
                } else if (MBCLEN_CHARFOUND_P(ret)) {
                    p += MBCLEN_CHARFOUND_LEN(ret);
                } else if (MBCLEN_INVALID_P(ret)) {
                    final int q = p;
                    int clen = enc.maxLength();

                    if (p1 < p) {
                        buf = concatNode.executeConcat(buf, substringNode.executeSubstring(rope, p1, p - p1), enc);
                    }

                    if (e - p < clen) {
                        clen = e - p;
                    }
                    if (clen <= mbminlen * 2) {
                        clen = mbminlen;
                    } else {
                        clen -= mbminlen;
                        for (; clen > mbminlen; clen -= mbminlen) {
                            ret = calculateCharacterLengthNode.characterLength(enc, cr, pBytes, q, q + clen);
                            if (MBCLEN_NEEDMORE_P(ret)) {
                                break;
                            }
                        }
                    }

                    RubyString repl = (RubyString) yield(
                            block,
                            makeStringNode.fromRope(substringNode.executeSubstring(rope, p, clen)));
                    buf = concatNode.executeConcat(buf, repl.rope, enc);
                    p += clen;
                    p1 = p;
                }
            }
            if (p1 < p) {
                buf = concatNode.executeConcat(buf, substringNode.executeSubstring(rope, p1, p - p1), enc);
            }
            if (p < e) {
                RubyString repl = (RubyString) yield(
                        block,
                        makeStringNode.fromRope(substringNode.executeSubstring(rope, p, e - p)));
                buf = concatNode.executeConcat(buf, repl.rope, enc);
            }

            return makeStringNode.fromRope(buf);
        }

        public Object yield(RubyProc block, Object... arguments) {
            return yieldNode.executeDispatch(block, arguments);
        }

    }

    @Primitive(name = "string_swapcase!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringSwapcaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object swapcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createSwapCase()") InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object swapcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CharacterLengthNode characterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile dummyEncodingProfile,
                @Cached ConditionProfile modifiedProfile) {
            // Taken from org.jruby.RubyString#swapcase_bang19.

            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();

            if (dummyEncodingProfile.profile(enc.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc, this));
            }

            final CodeRange cr = codeRangeNode.execute(rope);
            final byte[] inputBytes = bytesNode.execute(rope);
            final byte[] outputBytes = StringSupport.swapcaseMultiByteAsciiSimple(enc, cr, inputBytes);

            if (modifiedProfile.profile(inputBytes != outputBytes)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode.executeMake(outputBytes, enc, cr, characterLengthNode.execute(rope)));
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object swapcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile dummyEncodingProfile,
                @Cached ConditionProfile modifiedProfile) {
            // Taken from org.jruby.RubyString#swapcase_bang19.

            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();

            if (dummyEncodingProfile.profile(enc.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc, this));
            }

            final RopeBuilder builder = RopeBuilder.createRopeBuilder(bytesNode.execute(rope), rope.getEncoding());
            final boolean modified = StringSupport
                    .swapCaseMultiByteComplex(enc, codeRangeNode.execute(rope), builder, caseMappingOptions);

            if (modifiedProfile.profile(modified)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode
                                .executeMake(builder.getBytes(), rope.getEncoding(), CR_UNKNOWN, NotProvided.INSTANCE));

                return string;
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = "dump", taintFrom = 0)
    @ImportStatic(StringGuards.class)
    public abstract static class DumpNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateHelperNode = AllocateHelperNode.create();
        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode = RopeNodes.MakeLeafRopeNode.create();

        @Specialization(guards = "isAsciiCompatible(string)")
        protected RubyString dumpAsciiCompatible(RubyString string,
                @CachedLanguage RubyLanguage language) {
            // Taken from org.jruby.RubyString#dump

            RopeBuilder outputBytes = dumpCommon(string);
            outputBytes.setEncoding(string.rope.getEncoding());

            final Rope rope = makeLeafRopeNode
                    .executeMake(outputBytes.getBytes(), outputBytes.getEncoding(), CR_7BIT, outputBytes.getLength());

            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString result = new RubyString(logicalClass, shape, false, false, rope);
            allocateHelperNode.trace(result, this, language);
            return result;
        }

        @TruffleBoundary
        @Specialization(guards = "!isAsciiCompatible(string)")
        protected RubyString dump(RubyString string,
                @CachedLanguage RubyLanguage language) {
            // Taken from org.jruby.RubyString#dump

            RopeBuilder outputBytes = dumpCommon(string);

            try {
                outputBytes.append(".force_encoding(\"".getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(e);
            }

            outputBytes.append(string.rope.getEncoding().getName());
            outputBytes.append((byte) '"');
            outputBytes.append((byte) ')');

            outputBytes.setEncoding(ASCIIEncoding.INSTANCE);

            final Rope rope = makeLeafRopeNode
                    .executeMake(outputBytes.getBytes(), outputBytes.getEncoding(), CR_7BIT, outputBytes.getLength());

            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString result = new RubyString(logicalClass, shape, false, false, rope);
            allocateHelperNode.trace(result, this, language);
            return result;
        }

        @TruffleBoundary
        private RopeBuilder dumpCommon(RubyString string) {
            return dumpCommon(string.rope);
        }

        private RopeBuilder dumpCommon(Rope rope) {
            RopeBuilder buf = null;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = rope.getCodeRange();

            int p = 0;
            int end = rope.byteLength();
            byte[] bytes = rope.getBytes();

            int len = 2;
            while (p < end) {
                int c = bytes[p++] & 0xff;

                switch (c) {
                    case '"':
                    case '\\':
                    case '\n':
                    case '\r':
                    case '\t':
                    case '\f':
                    case '\013':
                    case '\010':
                    case '\007':
                    case '\033':
                        len += 2;
                        break;
                    case '#':
                        len += isEVStr(bytes, p, end) ? 2 : 1;
                        break;
                    default:
                        if (ASCIIEncoding.INSTANCE.isPrint(c)) {
                            len++;
                        } else {
                            if (enc.isUTF8()) {
                                int n = StringSupport.characterLength(enc, cr, bytes, p - 1, end) - 1;
                                if (n > 0) {
                                    if (buf == null) {
                                        buf = new RopeBuilder();
                                    }
                                    int cc = codePointX(enc, rope.getCodeRange(), bytes, p - 1, end);
                                    buf.append(StringUtils.formatASCIIBytes("%x", cc));
                                    len += buf.getLength() + 4;
                                    buf.setLength(0);
                                    p += n;
                                    break;
                                }
                            }
                            len += 4;
                        }
                        break;
                }
            }

            if (!enc.isAsciiCompatible()) {
                len += ".force_encoding(\"".length() + enc.getName().length + "\")".length();
            }

            RopeBuilder outBytes = new RopeBuilder();
            outBytes.unsafeEnsureSpace(len);
            byte out[] = outBytes.getUnsafeBytes();
            int q = 0;
            p = 0;
            end = rope.byteLength();

            out[q++] = '"';
            while (p < end) {
                int c = bytes[p++] & 0xff;
                if (c == '"' || c == '\\') {
                    out[q++] = '\\';
                    out[q++] = (byte) c;
                } else if (c == '#') {
                    if (isEVStr(bytes, p, end)) {
                        out[q++] = '\\';
                    }
                    out[q++] = '#';
                } else if (c == '\n') {
                    out[q++] = '\\';
                    out[q++] = 'n';
                } else if (c == '\r') {
                    out[q++] = '\\';
                    out[q++] = 'r';
                } else if (c == '\t') {
                    out[q++] = '\\';
                    out[q++] = 't';
                } else if (c == '\f') {
                    out[q++] = '\\';
                    out[q++] = 'f';
                } else if (c == '\013') {
                    out[q++] = '\\';
                    out[q++] = 'v';
                } else if (c == '\010') {
                    out[q++] = '\\';
                    out[q++] = 'b';
                } else if (c == '\007') {
                    out[q++] = '\\';
                    out[q++] = 'a';
                } else if (c == '\033') {
                    out[q++] = '\\';
                    out[q++] = 'e';
                } else if (ASCIIEncoding.INSTANCE.isPrint(c)) {
                    out[q++] = (byte) c;
                } else {
                    out[q++] = '\\';
                    if (enc.isUTF8()) {
                        int n = StringSupport.characterLength(enc, cr, bytes, p - 1, end) - 1;
                        if (n > 0) {
                            int cc = codePointX(enc, cr, bytes, p - 1, end);
                            p += n;
                            outBytes.setLength(q);
                            outBytes.append(StringUtils.formatASCIIBytes("u{%x}", cc));
                            q = outBytes.getLength();
                            continue;
                        }
                    }
                    outBytes.setLength(q);
                    outBytes.append(StringUtils.formatASCIIBytes("x%02X", c));
                    q = outBytes.getLength();
                }
            }
            out[q++] = '"';
            outBytes.setLength(q);
            assert out == outBytes.getUnsafeBytes(); // must not reallocate

            return outBytes;
        }

        private static boolean isEVStr(byte[] bytes, int p, int end) {
            return p < end ? isEVStr(bytes[p] & 0xff) : false;
        }

        private static boolean isEVStr(int c) {
            return c == '$' || c == '@' || c == '{';
        }

        private int codePointX(Encoding enc, CodeRange codeRange, byte[] bytes, int p, int end) {
            try {
                return StringSupport.codePoint(enc, codeRange, bytes, p, end);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(e.getMessage(), this));
            }
        }
    }

    @CoreMethod(names = "setbyte", required = 2, raiseIfFrozenSelf = true, lowerFixnum = { 1, 2 })
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "index", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    @ImportStatic(StringGuards.class)
    public abstract static class SetByteNode extends CoreMethodNode {

        @Child private CheckIndexNode checkIndexNode = CheckIndexNodeGen.create();
        @Child private RopeNodes.SetByteNode setByteNode = RopeNodes.SetByteNode.create();

        @CreateCast("index")
        protected RubyNode coerceIndexToInt(RubyNode index) {
            return ToIntNode.create(index);
        }

        @CreateCast("value")
        protected RubyNode coerceValueToInt(RubyNode value) {
            return ToIntNode.create(value);
        }

        public abstract int executeSetByte(RubyString string, int index, Object value);

        @Specialization
        protected int setByte(RubyString string, int index, int value,
                @Cached ConditionProfile newRopeProfile) {
            final Rope rope = string.rope;
            final int normalizedIndex = checkIndexNode.executeCheck(index, rope.byteLength());

            final Rope newRope = setByteNode.executeSetByte(rope, normalizedIndex, value);
            if (newRopeProfile.profile(newRope != rope)) {
                StringOperations.setRope(string, newRope);
            }

            return value;
        }

    }

    public static abstract class CheckIndexNode extends RubyContextNode {

        public abstract int executeCheck(int index, int length);

        @Specialization
        protected int checkIndex(int index, int length,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached BranchProfile errorProfile) {
            if (index >= length) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().indexErrorOutOfString(index, this));
            }

            if (negativeIndexProfile.profile(index < 0)) {
                index += length;
                if (index < 0) {
                    errorProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            getContext().getCoreExceptions().indexErrorOutOfString(index, this));
                }
            }

            return index;
        }

    }

    public static abstract class NormalizeIndexNode extends RubyContextNode {

        public abstract int executeNormalize(int index, int length);

        public static NormalizeIndexNode create() {
            return NormalizeIndexNodeGen.create();
        }

        @Specialization
        protected int normalizeIndex(int index, int length,
                @Cached ConditionProfile negativeIndexProfile) {
            if (negativeIndexProfile.profile(index < 0)) {
                return index + length;
            }

            return index;
        }

    }

    @CoreMethod(names = { "size", "length" })
    @ImportStatic(StringGuards.class)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        public static SizeNode create() {
            return StringNodesFactory.SizeNodeFactory.create(null);
        }

        public abstract int execute(RubyString string);

        @Specialization
        protected int size(RubyString string,
                @Cached RopeNodes.CharacterLengthNode characterLengthNode) {
            return characterLengthNode.execute(string.rope);
        }

    }

    @CoreMethod(names = "squeeze!", rest = true, raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class SqueezeBangNode extends CoreMethodArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        private final ConditionProfile singleByteOptimizableProfile = ConditionProfile.create();

        @Specialization(guards = "isEmpty(string)")
        protected Object squeezeBangEmptyString(RubyString string, Object[] args) {
            return nil;
        }

        @TruffleBoundary
        @Specialization(guards = { "!isEmpty(string)", "noArguments(args)" })
        protected Object squeezeBangZeroArgs(RubyString string, Object[] args) {
            // Taken from org.jruby.RubyString#squeeze_bang19.

            final Rope rope = string.rope;
            final RopeBuilder buffer = RopeOperations.toRopeBuilderCopy(rope);

            final boolean squeeze[] = new boolean[StringSupport.TRANS_SIZE];
            for (int i = 0; i < StringSupport.TRANS_SIZE; i++) {
                squeeze[i] = true;
            }

            if (singleByteOptimizableProfile.profile(rope.isSingleByteOptimizable())) {
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    StringOperations.setRope(string, RopeOperations.ropeFromRopeBuilder(buffer));
                }
            } else {
                if (!StringSupport
                        .multiByteSqueeze(
                                buffer,
                                rope.getCodeRange(),
                                squeeze,
                                null,
                                string.rope.getEncoding(),
                                false)) {
                    return nil;
                } else {
                    StringOperations.setRope(string, RopeOperations.ropeFromRopeBuilder(buffer));
                }
            }

            return string;
        }

        @Specialization(guards = { "!isEmpty(string)", "!noArguments(args)" })
        protected Object squeezeBang(VirtualFrame frame, RubyString string, Object[] args,
                @Cached ToStrNode toStrNode) {
            // Taken from org.jruby.RubyString#squeeze_bang19.

            final RubyString[] otherStrings = new RubyString[args.length];

            for (int i = 0; i < args.length; i++) {
                otherStrings[i] = toStrNode.executeToStr(args[i]);
            }

            return performSqueezeBang(string, otherStrings);
        }

        @TruffleBoundary
        private Object performSqueezeBang(RubyString string, RubyString[] otherStrings) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            final Rope rope = string.rope;
            final RopeBuilder buffer = RopeOperations.toRopeBuilderCopy(rope);

            RubyString otherStr = otherStrings[0];
            Rope otherRope = otherStr.rope;
            Encoding enc = checkEncodingNode.executeCheckEncoding(string, otherStr);
            final boolean squeeze[] = new boolean[StringSupport.TRANS_SIZE + 1];
            StringSupport.TrTables tables = StringSupport.trSetupTable(otherRope, squeeze, null, true, enc);

            boolean singlebyte = rope.isSingleByteOptimizable() && otherRope.isSingleByteOptimizable();

            for (int i = 1; i < otherStrings.length; i++) {
                otherStr = otherStrings[i];
                otherRope = otherStr.rope;
                enc = checkEncodingNode.executeCheckEncoding(string, otherStr);
                singlebyte = singlebyte && otherRope.isSingleByteOptimizable();
                tables = StringSupport.trSetupTable(otherRope, squeeze, tables, false, enc);
            }

            if (singleByteOptimizableProfile.profile(singlebyte)) {
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    StringOperations.setRope(string, RopeOperations.ropeFromRopeBuilder(buffer));
                }
            } else {
                if (!StringSupport.multiByteSqueeze(buffer, rope.getCodeRange(), squeeze, tables, enc, true)) {
                    return nil;
                } else {
                    StringOperations.setRope(string, RopeOperations.ropeFromRopeBuilder(buffer));
                }
            }

            return string;
        }

    }

    @CoreMethod(names = "succ!", raiseIfFrozenSelf = true)
    public abstract static class SuccBangNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode = RopeNodes.MakeLeafRopeNode.create();

        @Specialization
        protected RubyString succBang(RubyString string) {
            final Rope rope = string.rope;

            if (!rope.isEmpty()) {
                final RopeBuilder succBuilder = StringSupport.succCommon(rope);

                final Rope newRope = makeLeafRopeNode.executeMake(
                        succBuilder.getBytes(),
                        rope.getEncoding(),
                        CodeRange.CR_UNKNOWN,
                        NotProvided.INSTANCE);
                StringOperations.setRope(string, newRope);
            }

            return string;
        }
    }

    // String#sum is in Java because without OSR we can't warm up the Rubinius implementation

    @CoreMethod(names = "sum", optional = 1)
    public abstract static class SumNode extends CoreMethodArrayArgumentsNode {

        public static SumNode create() {
            return SumNodeFactory.create(null);
        }

        public abstract Object executeSum(VirtualFrame frame, RubyString string, Object bits);

        @Child private DispatchNode addNode = DispatchNode.create();
        @Child private DispatchNode subNode = DispatchNode.create();
        @Child private DispatchNode shiftNode = DispatchNode.create();
        @Child private DispatchNode andNode = DispatchNode.create();
        private final RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();

        @Specialization
        protected Object sum(VirtualFrame frame, RubyString string, long bits) {
            // Copied from JRuby

            final Rope rope = string.rope;
            final byte[] bytes = bytesNode.execute(rope);
            int p = 0;
            final int len = rope.byteLength();
            final int end = p + len;

            if (bits >= 8 * 8) { // long size * bits in byte
                Object sum = 0;
                while (p < end) {
                    sum = addNode.call(sum, "+", bytes[p++] & 0xff);
                }
                return sum;
            } else {
                long sum = 0;
                while (p < end) {
                    sum += bytes[p++] & 0xff;
                }
                return bits == 0 ? sum : sum & (1L << bits) - 1L;
            }
        }

        @Specialization
        protected Object sum(VirtualFrame frame, RubyString string, NotProvided bits) {
            return sum(frame, string, 16);
        }

        @Specialization(guards = { "!isInteger(bits)", "!isLong(bits)", "wasProvided(bits)" })
        protected Object sum(VirtualFrame frame, RubyString string, Object bits,
                @Cached ToLongNode toLongNode,
                @Cached SumNode sumNode) {
            return sumNode.executeSum(frame, string, toLongNode.execute(bits));
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        protected double toF(RubyString string) {
            try {
                return convertToDouble(string);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @TruffleBoundary
        private double convertToDouble(RubyString string) {
            return new DoubleConverter().parse(string.rope, false, true);
        }
    }

    @CoreMethod(names = { "to_s", "to_str" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "!isStringSubclass(string)")
        protected RubyString toS(RubyString string) {
            return string;
        }

        @Specialization(guards = "isStringSubclass(string)")
        protected RubyString toSOnSubclass(RubyString string,
                @Cached AllocateHelperNode allocateHelperNode,
                @CachedLanguage RubyLanguage language) {
            final RubyString result = new RubyString(
                    coreLibrary().stringClass,
                    RubyLanguage.stringShape,
                    false,
                    string.tainted,
                    string.rope);
            allocateHelperNode.trace(result, this, language);
            return result;
        }

        public boolean isStringSubclass(RubyString string) {
            return string.getLogicalClass() != coreLibrary().stringClass;
        }

    }

    @CoreMethod(names = { "to_sym", "intern" })
    @ImportStatic({ StringCachingGuards.class, StringGuards.class, StringOperations.class })
    public abstract static class ToSymNode extends CoreMethodArrayArgumentsNode {

        @Child RopeNodes.CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();

        @Specialization(
                guards = { "!isBrokenCodeRange(string, codeRangeNode)", "equalNode.execute(string.rope,cachedRope)" },
                limit = "getDefaultCacheLimit()")
        protected RubySymbol toSymCached(RubyString string,
                @Cached("privatizeRope(string)") Rope cachedRope,
                @Cached("getSymbol(cachedRope)") RubySymbol cachedSymbol,
                @Cached RopeNodes.EqualNode equalNode) {
            return cachedSymbol;
        }

        @Specialization(guards = "!isBrokenCodeRange(string, codeRangeNode)", replaces = "toSymCached")
        protected RubySymbol toSym(RubyString string) {
            return getSymbol(string.rope);
        }

        @Specialization(guards = "isBrokenCodeRange(string, codeRangeNode)")
        protected RubySymbol toSymBroken(RubyString string) {
            throw new RaiseException(getContext(), coreExceptions().encodingError("invalid encoding symbol", this));
        }
    }

    @CoreMethod(names = "reverse!", raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class ReverseBangNode extends CoreMethodArrayArgumentsNode {

        @Child RopeNodes.CharacterLengthNode characterLengthNode = RopeNodes.CharacterLengthNode.create();
        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode = RopeNodes.MakeLeafRopeNode.create();

        @Specialization(guards = "reverseIsEqualToSelf(string, characterLengthNode)")
        protected RubyString reverseNoOp(RubyString string) {
            return string;
        }

        @Specialization(
                guards = {
                        "!reverseIsEqualToSelf(string, characterLengthNode)",
                        "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected RubyString reverseSingleByteOptimizable(RubyString string,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            final Rope rope = string.rope;
            final byte[] originalBytes = bytesNode.execute(rope);
            final int len = originalBytes.length;
            final byte[] reversedBytes = new byte[len];

            for (int i = 0; i < len; i++) {
                reversedBytes[len - i - 1] = originalBytes[i];
            }

            StringOperations.setRope(
                    string,
                    makeLeafRopeNode.executeMake(
                            reversedBytes,
                            rope.getEncoding(),
                            codeRangeNode.execute(rope),
                            characterLengthNode.execute(rope)));

            return string;
        }

        @Specialization(
                guards = {
                        "!reverseIsEqualToSelf(string, characterLengthNode)",
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected RubyString reverse(RubyString string,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            // Taken from org.jruby.RubyString#reverse!

            final Rope rope = string.rope;
            final byte[] originalBytes = bytesNode.execute(rope);
            int p = 0;
            final int len = originalBytes.length;

            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);
            final int end = p + len;
            int op = len;
            final byte[] reversedBytes = new byte[len];

            while (p < end) {
                int cl = StringSupport.characterLength(enc, cr, originalBytes, p, end, true);
                if (cl > 1 || (originalBytes[p] & 0x80) != 0) {
                    op -= cl;
                    System.arraycopy(originalBytes, p, reversedBytes, op, cl);
                    p += cl;
                } else {
                    reversedBytes[--op] = originalBytes[p++];
                }
            }

            StringOperations.setRope(
                    string,
                    makeLeafRopeNode.executeMake(
                            reversedBytes,
                            rope.getEncoding(),
                            codeRangeNode.execute(rope),
                            characterLengthNode.execute(rope)));

            return string;
        }

        public static boolean reverseIsEqualToSelf(RubyString string,
                RopeNodes.CharacterLengthNode characterLengthNode) {
            return characterLengthNode.execute(string.rope) <= 1;
        }
    }

    @CoreMethod(names = "tr!", required = 2, raiseIfFrozenSelf = true)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "fromStr", type = RubyNode.class)
    @NodeChild(value = "toStr", type = RubyNode.class)
    @ImportStatic(StringGuards.class)
    public abstract static class TrBangNode extends CoreMethodNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child private DeleteBangNode deleteBangNode;

        @CreateCast("fromStr")
        protected RubyNode coerceFromStrToString(RubyNode fromStr) {
            return ToStrNodeGen.create(fromStr);
        }

        @CreateCast("toStr")
        protected RubyNode coerceToStrToString(RubyNode toStr) {
            return ToStrNodeGen.create(toStr);
        }

        @Specialization(guards = "isEmpty(self)")
        protected Object trBangSelfEmpty(RubyString self, RubyString fromStr, RubyString toStr) {
            return nil;
        }

        @Specialization(guards = { "!isEmpty(self)", "isEmpty(toStr)" })
        protected Object trBangToEmpty(RubyString self, RubyString fromStr, RubyString toStr) {
            if (deleteBangNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                deleteBangNode = insert(DeleteBangNode.create());
            }

            return deleteBangNode.executeDeleteBang(self, new Object[]{ fromStr });
        }

        @Specialization(guards = { "!isEmpty(self)", "!isEmpty(toStr)" })
        protected Object trBangNoEmpty(RubyString self, RubyString fromStr, RubyString toStr) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            return StringNodesHelper.trTransHelper(checkEncodingNode, self, fromStr, toStr, false);
        }
    }

    @CoreMethod(names = "tr_s!", required = 2, raiseIfFrozenSelf = true)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "fromStr", type = RubyNode.class)
    @NodeChild(value = "toStrNode", type = RubyNode.class)
    @ImportStatic(StringGuards.class)
    public abstract static class TrSBangNode extends CoreMethodNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child private DeleteBangNode deleteBangNode;

        @CreateCast("fromStr")
        protected RubyNode coerceFromStrToString(RubyNode fromStr) {
            return ToStrNodeGen.create(fromStr);
        }

        @CreateCast("toStrNode")
        protected RubyNode coerceToStrToString(RubyNode toStr) {
            return ToStrNodeGen.create(toStr);
        }

        @Specialization(guards = "isEmpty(self)")
        protected Object trSBangEmpty(RubyString self, RubyString fromStr, RubyString toStr) {
            return nil;
        }

        @Specialization(guards = "!isEmpty(self)")
        protected Object trSBang(RubyString self, RubyString fromStr, RubyString toStr) {
            if (toStr.rope.isEmpty()) {
                if (deleteBangNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    deleteBangNode = insert(DeleteBangNode.create());
                }

                return deleteBangNode.executeDeleteBang(self, new Object[]{ fromStr });
            }

            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            return StringNodesHelper.trTransHelper(checkEncodingNode, self, fromStr, toStr, true);
        }
    }

    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "format", type = RubyNode.class)
    @CoreMethod(names = "unpack", required = 1)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class UnpackNode extends CoreMethodNode {

        @Child private RubyLibrary rubyLibrary;
        @Child private DynamicObjectLibrary associatedLibrary;

        private final BranchProfile exceptionProfile = BranchProfile.create();

        @CreateCast("format")
        protected RubyNode coerceFormat(RubyNode format) {
            return ToStrNodeGen.create(format);
        }

        @Specialization(guards = "equalNode.execute(format.rope, cachedFormat)", limit = "getCacheLimit()")
        protected RubyArray unpackCached(RubyString string, RubyString format,
                @Cached("privatizeRope(format)") Rope cachedFormat,
                @Cached("create(compileFormat(format))") DirectCallNode callUnpackNode,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.EqualNode equalNode) {
            final Rope rope = string.rope;

            final ArrayResult result;

            try {
                result = (ArrayResult) callUnpackNode.call(
                        new Object[]{
                                bytesNode.execute(rope),
                                rope.byteLength(),
                                string.tainted,
                                readAssociated(string) });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishUnpack(result);
        }

        @Specialization(replaces = "unpackCached")
        protected RubyArray unpackUncached(RubyString string, RubyString format,
                @Cached IndirectCallNode callUnpackNode,
                @Cached RopeNodes.BytesNode bytesNode) {
            final Rope rope = string.rope;

            final ArrayResult result;

            try {
                result = (ArrayResult) callUnpackNode.call(
                        compileFormat(format),
                        new Object[]{
                                bytesNode.execute(rope),
                                rope.byteLength(),
                                string.tainted,
                                readAssociated(string) });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishUnpack(result);
        }

        private Object readAssociated(RubyString string) {
            if (associatedLibrary == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                associatedLibrary = insert(
                        DynamicObjectLibrary.getFactory().createDispatched(getDynamicObjectCacheLimit()));
            }
            return associatedLibrary.getOrDefault(string, Layouts.ASSOCIATED_IDENTIFIER, null);
        }

        private RubyArray finishUnpack(ArrayResult result) {
            final RubyArray array = createArray(result.getOutput(), result.getOutputLength());

            if (result.isTainted()) {
                if (rubyLibrary == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    rubyLibrary = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
                }
                rubyLibrary.taint(array);
            }

            return array;
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(RubyString format) {
            return new UnpackCompiler(getContext(), this).compile(format.getJavaString());
        }

        protected int getCacheLimit() {
            return getContext().getOptions().UNPACK_CACHE;
        }

    }

    public abstract static class InvertAsciiCaseBytesNode extends RubyContextNode {

        private final boolean lowerToUpper;
        private final boolean upperToLower;

        public static InvertAsciiCaseBytesNode createLowerToUpper() {
            return InvertAsciiCaseBytesNodeGen.create(true, false);
        }

        public static InvertAsciiCaseBytesNode createUpperToLower() {
            return InvertAsciiCaseBytesNodeGen.create(false, true);
        }

        public static InvertAsciiCaseBytesNode createSwapCase() {
            return InvertAsciiCaseBytesNodeGen.create(true, true);
        }

        protected InvertAsciiCaseBytesNode(boolean lowerToUpper, boolean upperToLower) {
            this.lowerToUpper = lowerToUpper;
            this.upperToLower = upperToLower;
        }

        public abstract byte[] executeInvert(byte[] bytes, int start);

        @Specialization
        protected byte[] invert(byte[] bytes, int start,
                @Cached BranchProfile foundLowerCaseCharProfile,
                @Cached BranchProfile foundUpperCaseCharProfile) {
            byte[] modified = null;

            for (int i = start; i < bytes.length; i++) {
                final byte b = bytes[i];

                if (lowerToUpper && StringSupport.isAsciiLowercase(b)) {
                    foundLowerCaseCharProfile.enter();

                    if (modified == null) {
                        modified = bytes.clone();
                    }

                    // Convert lower-case ASCII char to upper-case.
                    modified[i] ^= 0x20;
                }

                if (upperToLower && StringSupport.isAsciiUppercase(b)) {
                    foundUpperCaseCharProfile.enter();

                    if (modified == null) {
                        modified = bytes.clone();
                    }

                    // Convert upper-case ASCII char to lower-case.
                    modified[i] ^= 0x20;
                }
            }

            return modified;
        }

    }

    public abstract static class InvertAsciiCaseNode extends RubyContextNode {

        @Child private InvertAsciiCaseBytesNode invertNode;

        public static InvertAsciiCaseNode createLowerToUpper() {
            return InvertAsciiCaseNodeGen.create(InvertAsciiCaseBytesNode.createLowerToUpper());
        }

        public static InvertAsciiCaseNode createUpperToLower() {
            return InvertAsciiCaseNodeGen.create(InvertAsciiCaseBytesNode.createUpperToLower());
        }

        public static InvertAsciiCaseNode createSwapCase() {
            return InvertAsciiCaseNodeGen.create(InvertAsciiCaseBytesNode.createSwapCase());
        }

        public InvertAsciiCaseNode(InvertAsciiCaseBytesNode invertNode) {
            this.invertNode = invertNode;
        }

        public abstract Object executeInvert(RubyString string);

        @Specialization
        protected Object invert(RubyString string,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CharacterLengthNode characterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile noopProfile) {
            final Rope rope = string.rope;

            final byte[] bytes = bytesNode.execute(rope);
            byte[] modified = invertNode.executeInvert(bytes, 0);

            if (noopProfile.profile(modified == null)) {
                return nil;
            } else {
                final Rope newRope = makeLeafRopeNode.executeMake(
                        modified,
                        rope.getEncoding(),
                        codeRangeNode.execute(rope),
                        characterLengthNode.execute(rope));
                StringOperations.setRope(string, newRope);

                return string;
            }
        }

    }

    @Primitive(name = "string_upcase!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringUpcaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createLowerToUpper()") InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CharacterLengthNode characterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile dummyEncodingProfile,
                @Cached ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding encoding = rope.getEncoding();

            if (dummyEncodingProfile.profile(encoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding, this));
            }

            final CodeRange cr = codeRangeNode.execute(rope);
            final byte[] inputBytes = bytesNode.execute(rope);
            final byte[] outputBytes = StringSupport.upcaseMultiByteAsciiSimple(encoding, cr, inputBytes);

            if (modifiedProfile.profile(inputBytes != outputBytes)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode.executeMake(outputBytes, encoding, cr, characterLengthNode.execute(rope)));
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = { "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode,
                @Cached ConditionProfile dummyEncodingProfile,
                @Cached ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding encoding = rope.getEncoding();

            if (dummyEncodingProfile.profile(encoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding, this));
            }

            final RopeBuilder builder = RopeBuilder.createRopeBuilder(bytesNode.execute(rope), rope.getEncoding());
            final boolean modified = StringSupport
                    .upcaseMultiByteComplex(encoding, codeRangeNode.execute(rope), builder, caseMappingOptions);
            if (modifiedProfile.profile(modified)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode
                                .executeMake(builder.getBytes(), rope.getEncoding(), CR_UNKNOWN, NotProvided.INSTANCE));

                return string;
            } else {
                return nil;
            }
        }

    }

    @CoreMethod(names = "valid_encoding?")
    public abstract static class ValidEncodingQueryNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean validEncoding(RubyString string,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            final CodeRange codeRange = codeRangeNode.execute(string.rope);

            return codeRange != CR_BROKEN;
        }

    }

    @Primitive(name = "string_capitalize!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringCapitalizeBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();
        @Child private RopeNodes.CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();
        @Child private RopeNodes.CharacterLengthNode characterLengthNode = RopeNodes.CharacterLengthNode.create();
        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode = RopeNodes.MakeLeafRopeNode.create();
        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();

        @Specialization(guards = "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createUpperToLower()") InvertAsciiCaseBytesNode invertAsciiCaseNode,
                @Cached ConditionProfile emptyStringProfile,
                @Cached ConditionProfile firstCharIsLowerProfile,
                @Cached ConditionProfile otherCharsAlreadyLowerProfile,
                @Cached ConditionProfile mustCapitalizeFirstCharProfile) {
            final Rope rope = string.rope;

            if (emptyStringProfile.profile(rope.isEmpty())) {
                return nil;
            }

            final byte[] sourceBytes = bytesNode.execute(rope);
            final byte[] finalBytes;

            final byte[] processedBytes = invertAsciiCaseNode.executeInvert(sourceBytes, 1);

            if (otherCharsAlreadyLowerProfile.profile(processedBytes == null)) {
                // Bytes 1..N are either not letters or already lowercased. Time to check the first byte.

                if (firstCharIsLowerProfile.profile(StringSupport.isAsciiLowercase(sourceBytes[0]))) {
                    // The first char requires capitalization, but the remaining bytes in the original string are
                    // already properly cased.
                    finalBytes = sourceBytes.clone();
                } else {
                    // The string is already capitalized.
                    return nil;
                }
            } else {
                // At least one char was lowercased when looking at bytes 1..N. We still must check the first byte.
                finalBytes = processedBytes;
            }

            if (mustCapitalizeFirstCharProfile.profile(StringSupport.isAsciiLowercase(sourceBytes[0]))) {
                finalBytes[0] ^= 0x20;
            }

            StringOperations.setRope(
                    string,
                    makeLeafRopeNode.executeMake(
                            finalBytes,
                            rope.getEncoding(),
                            codeRangeNode.execute(rope),
                            characterLengthNode.execute(rope)));

            return string;
        }

        @Specialization(guards = "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached BranchProfile dummyEncodingProfile,
                @Cached ConditionProfile emptyStringProfile,
                @Cached ConditionProfile modifiedProfile) {
            // Taken from org.jruby.RubyString#capitalize_bang19.

            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();

            if (enc.isDummy()) {
                dummyEncodingProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc, this));
            }

            if (emptyStringProfile.profile(rope.isEmpty())) {
                return nil;
            }

            final CodeRange cr = codeRangeNode.execute(rope);
            final byte[] inputBytes = bytesNode.execute(rope);
            final byte[] outputBytes = StringSupport.capitalizeMultiByteAsciiSimple(enc, cr, inputBytes);

            if (modifiedProfile.profile(inputBytes != outputBytes)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode.executeMake(
                                outputBytes,
                                enc,
                                cr,
                                characterLengthNode.execute(rope)));
                return string;
            }

            return nil;
        }

        @Specialization(guards = "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached BranchProfile dummyEncodingProfile,
                @Cached ConditionProfile emptyStringProfile,
                @Cached ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();

            if (enc.isDummy()) {
                dummyEncodingProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc, this));
            }

            if (emptyStringProfile.profile(rope.isEmpty())) {
                return nil;
            }

            final RopeBuilder builder = RopeBuilder.createRopeBuilder(bytesNode.execute(rope), rope.getEncoding());
            final boolean modified = StringSupport
                    .capitalizeMultiByteComplex(enc, codeRangeNode.execute(rope), builder, caseMappingOptions);
            if (modifiedProfile.profile(modified)) {
                StringOperations.setRope(
                        string,
                        makeLeafRopeNode
                                .executeMake(builder.getBytes(), rope.getEncoding(), CR_UNKNOWN, NotProvided.INSTANCE));
                return string;
            } else {
                return nil;
            }
        }

    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        @Specialization
        protected RubyString clear(RubyString string) {
            StringOperations.setRope(string, substringNode.executeSubstring(string.rope, 0, 0));
            return string;
        }
    }

    public static class StringNodesHelper {

        @TruffleBoundary
        private static Object trTransHelper(CheckEncodingNode checkEncodingNode, RubyString self, RubyString fromStr,
                RubyString toStr, boolean sFlag) {
            final Encoding e1 = checkEncodingNode.executeCheckEncoding(self, fromStr);
            final Encoding e2 = checkEncodingNode.executeCheckEncoding(self, toStr);
            final Encoding enc = e1 == e2 ? e1 : checkEncodingNode.executeCheckEncoding(fromStr, toStr);

            final Rope ret = StringSupport.trTransHelper(self.rope, fromStr.rope, toStr.rope, e1, enc, sFlag);
            if (ret == null) {
                return Nil.INSTANCE;
            }

            StringOperations.setRope(self, ret);
            return self;
        }
    }

    @Primitive(name = "character_printable_p")
    public static abstract class CharacterPrintablePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isCharacterPrintable(RubyString character,
                @Cached ConditionProfile is7BitProfile,
                @Cached RopeNodes.AsciiOnlyNode asciiOnlyNode,
                @Cached RopeNodes.GetCodePointNode getCodePointNode) {
            final Rope rope = character.rope;
            final int codePoint = getCodePointNode.executeGetCodePoint(rope, 0);

            if (is7BitProfile.profile(asciiOnlyNode.execute(rope))) {
                return StringSupport.isAsciiPrintable(codePoint);
            } else {
                return isMBCPrintable(rope.getEncoding(), codePoint);
            }
        }

        @TruffleBoundary
        protected boolean isMBCPrintable(Encoding encoding, int codePoint) {
            return encoding.isPrint(codePoint);
        }

    }

    @Primitive(name = "string_append")
    public static abstract class StringAppendPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private StringAppendNode stringAppendNode = StringAppendNode.create();

        public static StringAppendPrimitiveNode create() {
            return StringAppendPrimitiveNodeFactory.create(null);
        }

        public abstract RubyString executeStringAppend(RubyString string, RubyString other);

        @Specialization
        protected RubyString stringAppend(RubyString string, RubyString other) {
            StringOperations.setRope(string, stringAppendNode.executeStringAppend(string, other));
            return string;
        }

    }

    @Primitive(name = "string_awk_split", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public static abstract class StringAwkSplitPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();
        @Child private YieldNode yieldNode = YieldNode.create();
        @Child RopeNodes.CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();
        @Child private RopeNodes.GetCodePointNode getCodePointNode = RopeNodes.GetCodePointNode.create();
        @Child private SubstringNode substringNode = SubstringNode.create();

        private static final int SUBSTRING_CREATED = -1;

        @Specialization(guards = "is7Bit(string, codeRangeNode)")
        protected Object stringAwkSplitSingleByte(RubyString string, int limit, Object block,
                @Cached ConditionProfile executeBlockProfile,
                @Cached ConditionProfile growArrayProfile,
                @Cached ConditionProfile trailingSubstringProfile,
                @Cached ConditionProfile trailingEmptyStringProfile,
                @Cached ProcOrNullNode procOrNullNode) {
            Object[] ret = new Object[10];
            int storeIndex = 0;
            final RubyProc calledBlock = procOrNullNode.executeProcOrNull(block);

            final Rope rope = string.rope;
            final byte[] bytes = bytesNode.execute(rope);

            int substringStart = 0;
            boolean findingSubstringEnd = false;
            for (int i = 0; i < bytes.length; i++) {
                if (StringSupport.isAsciiSpace(bytes[i])) {
                    if (findingSubstringEnd) {
                        findingSubstringEnd = false;

                        final RubyString substring = substringNode
                                .executeSubstring(string, substringStart, i - substringStart);
                        ret = addSubstring(
                                ret,
                                storeIndex++,
                                substring,
                                calledBlock,
                                executeBlockProfile,
                                growArrayProfile);
                        substringStart = SUBSTRING_CREATED;
                    }
                } else {
                    if (!findingSubstringEnd) {
                        substringStart = i;
                        findingSubstringEnd = true;

                        if (storeIndex == limit - 1) {
                            break;
                        }
                    }
                }
            }

            if (trailingSubstringProfile.profile(substringStart != SUBSTRING_CREATED)) {
                final RubyString substring = substringNode
                        .executeSubstring(string, substringStart, bytes.length - substringStart);
                ret = addSubstring(ret, storeIndex++, substring, calledBlock, executeBlockProfile, growArrayProfile);
            }

            if (trailingEmptyStringProfile.profile(limit < 0 && StringSupport.isAsciiSpace(bytes[bytes.length - 1]))) {
                final RubyString substring = substringNode.executeSubstring(string, bytes.length - 1, 0);
                ret = addSubstring(ret, storeIndex++, substring, calledBlock, executeBlockProfile, growArrayProfile);
            }

            if (calledBlock == null) {
                return createArray(ret, storeIndex);
            } else {
                return string;
            }
        }

        @TruffleBoundary
        @Specialization(guards = "!is7Bit(string, codeRangeNode)")
        protected RubyArray stringAwkSplit(RubyString string, int limit, Object block,
                @Cached ConditionProfile executeBlockProfile,
                @Cached ConditionProfile growArrayProfile,
                @Cached ConditionProfile trailingSubstringProfile,
                @Cached ProcOrNullNode procOrNullNode) {
            Object[] ret = new Object[10];
            int storeIndex = 0;
            final RubyProc calledBlock = procOrNullNode.executeProcOrNull(block);

            final Rope rope = string.rope;
            final boolean limitPositive = limit > 0;
            int i = limit > 0 ? 1 : 0;

            final byte[] bytes = bytesNode.execute(rope);
            int p = 0;
            int ptr = p;
            int len = rope.byteLength();
            int end = p + len;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = rope.getCodeRange();
            boolean skip = true;

            int e = 0, b = 0;
            while (p < end) {
                final int c = getCodePointNode.executeGetCodePoint(rope, p);
                p += StringSupport.characterLength(enc, cr, bytes, p, end, true);

                if (skip) {
                    if (StringSupport.isSpace(enc, c)) {
                        b = p - ptr;
                    } else {
                        e = p - ptr;
                        skip = false;
                        if (limitPositive && limit <= i) {
                            break;
                        }
                    }
                } else {
                    if (StringSupport.isSpace(enc, c)) {
                        final RubyString substring = substringNode.executeSubstring(string, b, e - b);
                        ret = addSubstring(
                                ret,
                                storeIndex++,
                                substring,
                                calledBlock,
                                executeBlockProfile,
                                growArrayProfile);
                        skip = true;
                        b = p - ptr;
                        if (limitPositive) {
                            i++;
                        }
                    } else {
                        e = p - ptr;
                    }
                }
            }

            if (trailingSubstringProfile.profile(len > 0 && (limitPositive || len > b || limit < 0))) {
                final RubyString substring = substringNode.executeSubstring(string, b, len - b);
                ret = addSubstring(ret, storeIndex++, substring, calledBlock, executeBlockProfile, growArrayProfile);
            }

            return createArray(ret, storeIndex);
        }

        private Object[] addSubstring(Object[] store, int index, RubyString substring,
                RubyProc block, ConditionProfile executeBlockProfile, ConditionProfile growArrayProfile) {
            if (executeBlockProfile.profile(block != null)) {
                yield(block, substring);
            } else {
                if (growArrayProfile.profile(index < store.length)) {
                    store[index] = substring;
                } else {
                    store = ArrayUtils.grow(store, store.length * 2);
                    store[index] = substring;
                }
            }

            return store;
        }

        private Object yield(RubyProc block, Object... arguments) {
            return yieldNode.executeDispatch(block, arguments);
        }
    }

    @Primitive(name = "string_byte_substring", lowerFixnum = { 1, 2 })
    public static abstract class StringByteSubstringPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private NormalizeIndexNode normalizeIndexNode = NormalizeIndexNode.create();
        @Child private SubstringNode substringNode = SubstringNode.create();

        public static StringByteSubstringPrimitiveNode create() {
            return StringByteSubstringPrimitiveNodeFactory.create(null);
        }

        public abstract Object executeStringByteSubstring(RubyString string, Object index, Object length);

        @Specialization
        protected Object stringByteSubstring(RubyString string, int index, NotProvided length,
                @Cached ConditionProfile negativeLengthProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile lengthTooLongProfile,
                @Cached ConditionProfile nilSubstringProfile,
                @Cached ConditionProfile emptySubstringProfile) {
            final Object subString = stringByteSubstring(
                    string,
                    index,
                    1,
                    negativeLengthProfile,
                    indexOutOfBoundsProfile,
                    lengthTooLongProfile);

            if (nilSubstringProfile.profile(subString == nil)) {
                return subString;
            }

            if (emptySubstringProfile.profile(((RubyString) subString).rope.isEmpty())) {
                return nil;
            }

            return subString;
        }

        @Specialization
        protected Object stringByteSubstring(RubyString string, int index, int length,
                @Cached ConditionProfile negativeLengthProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile lengthTooLongProfile) {
            if (negativeLengthProfile.profile(length < 0)) {
                return nil;
            }

            final Rope rope = string.rope;
            final int stringByteLength = rope.byteLength();
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, stringByteLength);

            if (indexOutOfBoundsProfile.profile(normalizedIndex < 0 || normalizedIndex > stringByteLength)) {
                return nil;
            }

            if (lengthTooLongProfile.profile(normalizedIndex + length > stringByteLength)) {
                length = rope.byteLength() - normalizedIndex;
            }

            return substringNode.executeSubstring(string, normalizedIndex, length);
        }

        @Fallback
        protected Object stringByteSubstring(Object string, Object range, Object length) {
            return FAILURE;
        }

    }

    @Primitive(name = "string_chr_at", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public static abstract class StringChrAtPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "indexOutOfBounds(string, byteIndex)")
        protected Object stringChrAtOutOfBounds(RubyString string, int byteIndex) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!indexOutOfBounds(string, byteIndex)",
                        "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object stringChrAtSingleByte(RubyString string, int byteIndex,
                @Cached StringByteSubstringPrimitiveNode stringByteSubstringNode,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            return stringByteSubstringNode.executeStringByteSubstring(string, byteIndex, 1);
        }

        @Specialization(
                guards = {
                        "!indexOutOfBounds(string, byteIndex)",
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object stringChrAt(RubyString string, int byteIndex,
                @Cached EncodingNodes.GetActualEncodingNode getActualEncodingNode,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached MakeStringNode makeStringNode) {
            final Rope rope = string.rope;
            final Encoding encoding = getActualEncodingNode.execute(rope);
            final int end = rope.byteLength();
            final byte[] bytes = bytesNode.execute(rope);
            final int c = calculateCharacterLengthNode.characterLength(
                    encoding,
                    codeRangeNode.execute(rope),
                    bytes,
                    byteIndex,
                    end);

            if (!StringSupport.MBCLEN_CHARFOUND_P(c)) {
                return nil;
            }

            if (c + byteIndex > end) {
                return nil;
            }

            return makeStringNode.executeMake(
                    ArrayUtils.extractRange(bytes, byteIndex, byteIndex + c),
                    encoding,
                    CR_UNKNOWN);
        }

        protected static boolean indexOutOfBounds(RubyString string, int byteIndex) {
            return ((byteIndex < 0) || (byteIndex >= string.rope.byteLength()));
        }

    }

    @ImportStatic(StringGuards.class)
    public static abstract class StringAreComparableNode extends RubyContextNode {

        @Child RopeNodes.AreComparableRopesNode areComparableRopesNode = RopeNodes.AreComparableRopesNode.create();

        public abstract boolean executeAreComparable(RubyString first, RubyString second);

        @Specialization
        protected boolean areComparable(RubyString a, RubyString b) {
            return areComparableRopesNode.execute(a.rope, b.rope);
        }
    }

    @ImportStatic({ StringGuards.class, StringOperations.class })
    public static abstract class StringEqualNode extends RubyContextNode {

        @Child private StringAreComparableNode areComparableNode;

        public abstract boolean executeStringEqual(RubyString string, RubyString other);

        // Same Rope implies same Encoding and therefore comparable
        @Specialization(guards = "string.rope == other.rope")
        protected boolean sameRope(RubyString string, RubyString other) {
            return true;
        }

        @Specialization(guards = "!areComparable(string, other)")
        protected boolean notComparable(RubyString string, RubyString other) {
            return false;
        }

        @Specialization(guards = "areComparable(string, other)")
        protected boolean stringEquals(RubyString string, RubyString other,
                @Cached RopeNodes.BytesEqualNode bytesEqualNode) {
            return bytesEqualNode.execute(string.rope, other.rope);
        }

        protected boolean areComparable(RubyString first, RubyString second) {
            if (areComparableNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                areComparableNode = insert(StringAreComparableNodeGen.create());
            }
            return areComparableNode.executeAreComparable(first, second);
        }

    }

    @Primitive(name = "string_escape")
    public abstract static class StringEscapePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyString string_escape(RubyString string,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final RubyString result = makeStringNode.fromRope(rbStrEscape(string.rope));
            result.tainted = string.tainted;
            return result;
        }

        // MRI: rb_str_escape
        @TruffleBoundary
        private static Rope rbStrEscape(Rope str) {
            final Encoding enc = str.getEncoding();
            final byte[] pBytes = str.getBytes();
            final CodeRange cr = str.getCodeRange();

            int p = 0;
            int pend = str.byteLength();
            int prev = p;
            RopeBuilder result = new RopeBuilder();
            boolean unicode_p = enc.isUnicode();
            boolean asciicompat = enc.isAsciiCompatible();

            while (p < pend) {
                int c, cc;
                int n = StringSupport.characterLength(enc, cr, pBytes, p, pend, false);
                if (!MBCLEN_CHARFOUND_P(n)) {
                    if (p > prev) {
                        result.append(pBytes, prev, p - prev);
                    }
                    n = enc.minLength();
                    if (pend < p + n) {
                        n = (pend - p);
                    }
                    while ((n--) > 0) {
                        result.append(
                                String.format("\\x%02X", (long) (pBytes[p] & 0377)).getBytes(
                                        StandardCharsets.US_ASCII));
                        prev = ++p;
                    }
                    continue;
                }
                n = MBCLEN_CHARFOUND_LEN(n);
                c = enc.mbcToCode(pBytes, p, pend);
                p += n;
                switch (c) {
                    case '\n':
                        cc = 'n';
                        break;
                    case '\r':
                        cc = 'r';
                        break;
                    case '\t':
                        cc = 't';
                        break;
                    case '\f':
                        cc = 'f';
                        break;
                    case '\013':
                        cc = 'v';
                        break;
                    case '\010':
                        cc = 'b';
                        break;
                    case '\007':
                        cc = 'a';
                        break;
                    case 033:
                        cc = 'e';
                        break;
                    default:
                        cc = 0;
                        break;
                }
                if (cc != 0) {
                    if (p - n > prev) {
                        result.append(pBytes, prev, p - n - prev);
                    }
                    result.append('\\');
                    result.append((byte) cc);
                    prev = p;
                } else if (asciicompat && Encoding.isAscii(c) && (c < 0x7F && c > 31 /* ISPRINT(c) */)) {
                } else {
                    if (p - n > prev) {
                        result.append(pBytes, prev, p - n - prev);
                    }

                    if (unicode_p && (c & 0xFFFFFFFFL) < 0x7F && Encoding.isAscii(c) &&
                            ASCIIEncoding.INSTANCE.isPrint(c)) {
                        result.append(StringUtils.formatASCIIBytes("%c", (char) (c & 0xFFFFFFFFL)));
                    } else {
                        result.append(StringUtils.formatASCIIBytes(escapedCharFormat(c, unicode_p), c & 0xFFFFFFFFL));
                    }

                    prev = p;
                }
            }
            if (p > prev) {
                result.append(pBytes, prev, p - prev);
            }

            result.setEncoding(USASCIIEncoding.INSTANCE);
            return result.toRope(CodeRange.CR_7BIT);
        }

        private static int MBCLEN_CHARFOUND_LEN(int r) {
            return r;
        }

        // MBCLEN_CHARFOUND_P, ONIGENC_MBCLEN_CHARFOUND_P
        private static boolean MBCLEN_CHARFOUND_P(int r) {
            return 0 < r;
        }

        private static String escapedCharFormat(int c, boolean isUnicode) {
            String format;
            // c comparisons must be unsigned 32-bit
            if (isUnicode) {

                if ((c & 0xFFFFFFFFL) < 0x7F && Encoding.isAscii(c) && ASCIIEncoding.INSTANCE.isPrint(c)) {
                    throw new UnsupportedOperationException();
                } else if (c < 0x10000) {
                    format = "\\u%04X";
                } else {
                    format = "\\u{%X}";
                }
            } else {
                if ((c & 0xFFFFFFFFL) < 0x100) {
                    format = "\\x%02X";
                } else {
                    format = "\\x{%X}";
                }
            }
            return format;
        }

    }

    @Primitive(name = "string_find_character", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public static abstract class StringFindCharacterNode extends CoreMethodArrayArgumentsNode {

        @Child private SubstringNode substringNode = SubstringNode.create();

        @Specialization(guards = "offset < 0")
        protected Object stringFindCharacterNegativeOffset(RubyString string, int offset) {
            return nil;
        }

        @Specialization(guards = "offsetTooLarge(string, offset)")
        protected Object stringFindCharacterOffsetTooLarge(RubyString string, int offset) {
            return nil;
        }

        @Specialization(
                guards = {
                        "offset >= 0",
                        "!offsetTooLarge(string, offset)",
                        "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object stringFindCharacterSingleByte(RubyString string, int offset,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            // Taken from Rubinius's String::find_character.

            return substringNode.executeSubstring(string, offset, 1);
        }

        @Specialization(
                guards = {
                        "offset >= 0",
                        "!offsetTooLarge(string, offset)",
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object stringFindCharacter(RubyString string, int offset,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            // Taken from Rubinius's String::find_character.

            final Rope rope = string.rope;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);

            final int clen = calculateCharacterLengthNode
                    .characterLength(enc, cr, rope.getBytes(), offset, offset + enc.maxLength());

            return substringNode.executeSubstring(string, offset, clen);
        }

        protected static boolean offsetTooLarge(RubyString string, int offset) {
            return offset >= string.rope.byteLength();
        }

    }

    @NonStandard
    @CoreMethod(names = "from_codepoint", onSingleton = true, required = 2, lowerFixnum = 1)
    public static abstract class StringFromCodepointPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization(guards = { "isSimple(code, rubyEncoding)", "isCodepoint(code)" })
        protected RubyString stringFromCodepointSimple(long code, RubyEncoding rubyEncoding,
                @Cached ConditionProfile isUTF8Profile,
                @Cached ConditionProfile isUSAsciiProfile,
                @Cached ConditionProfile isAscii8BitProfile) {
            final int intCode = (int) code; // isSimple() guarantees this is OK
            final Encoding encoding = rubyEncoding.encoding;
            final Rope rope;

            if (isUTF8Profile.profile(encoding == UTF8Encoding.INSTANCE)) {
                rope = RopeConstants.UTF8_SINGLE_BYTE_ROPES[intCode];
            } else if (isUSAsciiProfile.profile(encoding == USASCIIEncoding.INSTANCE)) {
                rope = RopeConstants.US_ASCII_SINGLE_BYTE_ROPES[intCode];
            } else if (isAscii8BitProfile.profile(encoding == ASCIIEncoding.INSTANCE)) {
                rope = RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES[intCode];
            } else {
                rope = RopeOperations.create(new byte[]{ (byte) intCode }, encoding, CodeRange.CR_UNKNOWN);
            }

            return makeStringNode.fromRope(rope);
        }

        @TruffleBoundary
        @Specialization(guards = { "!isSimple(code, rubyEncoding)", "isCodepoint(code)" })
        protected RubyString stringFromCodepoint(long code, RubyEncoding rubyEncoding,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode) {
            final Encoding encoding = rubyEncoding.encoding;
            final int length;

            try {
                length = encoding.codeToMbcLength((int) code);
            } catch (EncodingException e) {
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            if (length <= 0) {
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            final byte[] bytes = new byte[length];

            final int codeToMbc = encoding.codeToMbc((int) code, bytes, 0);
            if (codeToMbc < 0) {
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            if (calculateCharacterLengthNode.characterLength(encoding, CR_UNKNOWN, bytes, 0, length) != length) {
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            return makeStringNode.executeMake(bytes, encoding, CodeRange.CR_VALID);
        }

        protected boolean isCodepoint(long code) {
            // Fits in an unsigned int
            return code >= 0 && code < (1L << 32);
        }

        protected boolean isSimple(long code, RubyEncoding encoding) {
            final Encoding enc = encoding.encoding;

            return (enc.isAsciiCompatible() && code >= 0x00 && code < 0x80) ||
                    (enc == ASCIIEncoding.INSTANCE && code >= 0x00 && code <= 0xFF);
        }

    }

    @Primitive(name = "string_to_f")
    public static abstract class StringToFPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object stringToF(RubyString string,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode,
                @Cached RopeNodes.BytesNode bytesNode) {
            final Rope rope = string.rope;
            if (rope.isEmpty()) {
                return nil;
            }
            final String javaString = string.getJavaString();
            if (javaString.startsWith("0x")) {
                try {
                    return Double.parseDouble(javaString);
                } catch (NumberFormatException e) {
                    // Try falling back to this implementation if the first fails, neither 100% complete
                    final Object result = ConvertBytes
                            .byteListToInum19(getContext(), this, fixnumOrBignumNode, bytesNode, string, 16, true);
                    if (result instanceof Integer) {
                        return ((Integer) result).doubleValue();
                    } else if (result instanceof Long) {
                        return ((Long) result).doubleValue();
                    } else if (result instanceof Double) {
                        return result;
                    } else {
                        return nil;
                    }
                }
            }
            try {
                return new DoubleConverter().parse(rope, true, true);
            } catch (NumberFormatException e) {
                return nil;
            }
        }

    }

    @Primitive(name = "find_string", lowerFixnum = 2)
    @ImportStatic(StringGuards.class)
    public static abstract class StringIndexPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child RopeNodes.CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();
        @Child RopeNodes.SingleByteOptimizableNode singleByteNode = RopeNodes.SingleByteOptimizableNode.create();

        @Specialization(guards = "isEmpty(pattern)")
        protected int stringIndexEmptyPattern(RubyString string, RubyString pattern, int byteOffset) {
            assert byteOffset >= 0;

            return byteOffset;
        }

        @Specialization(
                guards = {
                        "isSingleByteString(pattern)",
                        "!isBrokenCodeRange(pattern, codeRangeNode)",
                        "canMemcmp(string, pattern, singleByteNode)" })
        protected Object stringIndexSingleBytePattern(RubyString string, RubyString pattern, int byteOffset,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached ConditionProfile offsetTooLargeProfile) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = string.rope;
            final int end = sourceRope.byteLength();

            if (offsetTooLargeProfile.profile(byteOffset >= end)) {
                return nil;
            }

            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final byte searchByte = bytesNode.execute(pattern.rope)[0];

            final int index = com.oracle.truffle.api.ArrayUtils.indexOf(sourceBytes, byteOffset, end, searchByte);

            return index == -1 ? nil : index;
        }

        @Specialization(
                guards = {
                        "!isEmpty(pattern)",
                        "!isSingleByteString(pattern)",
                        "!isBrokenCodeRange(pattern, codeRangeNode)",
                        "canMemcmp(string, pattern, singleByteNode)" })
        protected Object stringIndexMultiBytePattern(RubyString string, RubyString pattern, int byteOffset,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached BranchProfile matchFoundProfile,
                @Cached BranchProfile noMatchProfile) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = string.rope;
            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final Rope searchRope = pattern.rope;
            final byte[] searchBytes = bytesNode.execute(searchRope);

            int end = sourceRope.byteLength() - searchRope.byteLength();

            for (int i = byteOffset; i <= end; i++) {
                if (sourceBytes[i] == searchBytes[0]) {
                    if (ArrayUtils.memcmp(sourceBytes, i, searchBytes, 0, searchRope.byteLength()) == 0) {
                        matchFoundProfile.enter();
                        return i;
                    }
                }
            }

            noMatchProfile.enter();
            return nil;
        }

        @Specialization(guards = "isBrokenCodeRange(pattern, codeRangeNode)")
        protected Object stringIndexBrokenPattern(RubyString string, RubyString pattern, int byteOffset) {
            assert byteOffset >= 0;

            return nil;
        }

        @Specialization(
                guards = {
                        "!isBrokenCodeRange(pattern, codeRangeNode)",
                        "!canMemcmp(string, pattern, singleByteNode)" })
        protected Object stringIndexGeneric(RubyString string, RubyString pattern, int byteOffset,
                @Cached ByteIndexFromCharIndexNode byteIndexFromCharIndexNode,
                @Cached StringByteCharacterIndexNode byteIndexToCharIndexNode,
                @Cached NormalizeIndexNode normalizeIndexNode,
                @Cached ConditionProfile badIndexProfile) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            // Rubinius will pass in a byte index for the `start` value, but StringSupport.index requires a character index.
            final int charIndex = byteIndexToCharIndexNode.executeStringByteCharacterIndex(string, byteOffset);

            final int index = index(
                    string.rope,
                    pattern.rope,
                    charIndex,
                    string.rope.getEncoding(),
                    normalizeIndexNode,
                    byteIndexFromCharIndexNode);

            if (badIndexProfile.profile(index == -1)) {
                return nil;
            }

            return index;
        }

        @TruffleBoundary
        private int index(Rope source, Rope other, int byteOffset, Encoding enc, NormalizeIndexNode normalizeIndexNode,
                ByteIndexFromCharIndexNode byteIndexFromCharIndexNode) {
            // Taken from org.jruby.util.StringSupport.index.
            assert byteOffset >= 0;

            int sourceLen = source.characterLength();
            int otherLen = other.characterLength();

            byteOffset = normalizeIndexNode.executeNormalize(byteOffset, sourceLen);

            if (sourceLen - byteOffset < otherLen) {
                return -1;
            }
            byte[] bytes = source.getBytes();
            int p = 0;
            final int end = source.byteLength();
            if (byteOffset != 0) {
                if (!source.isSingleByteOptimizable()) {
                    final int pp = byteIndexFromCharIndexNode.execute(source, 0, byteOffset);
                    byteOffset = StringSupport.offset(0, end, pp);
                }
                p += byteOffset;
            }
            if (otherLen == 0) {
                return byteOffset;
            }

            while (true) {
                int pos = indexOf(source, other, p);
                if (pos < 0) {
                    return pos;
                }
                pos -= p;
                int t = enc.rightAdjustCharHead(bytes, p, p + pos, end);
                if (t == p + pos) {
                    return pos + byteOffset;
                }
                if ((sourceLen -= t - p) <= 0) {
                    return -1;
                }
                byteOffset += t - p;
                p = t;
            }
        }

        @TruffleBoundary
        private int indexOf(Rope sourceRope, Rope otherRope, int fromIndex) {
            // Taken from org.jruby.util.ByteList.indexOf.

            final byte[] source = sourceRope.getBytes();
            final int sourceOffset = 0;
            final int sourceCount = sourceRope.byteLength();
            final byte[] target = otherRope.getBytes();
            final int targetOffset = 0;
            final int targetCount = otherRope.byteLength();

            if (fromIndex >= sourceCount) {
                return (targetCount == 0 ? sourceCount : -1);
            }
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            if (targetCount == 0) {
                return fromIndex;
            }

            byte first = target[targetOffset];
            int max = sourceOffset + (sourceCount - targetCount);

            for (int i = sourceOffset + fromIndex; i <= max; i++) {
                if (source[i] != first) {
                    while (++i <= max && source[i] != first) {
                    }
                }

                if (i <= max) {
                    int j = i + 1;
                    int end = j + targetCount - 1;
                    for (int k = targetOffset + 1; j < end && source[j] == target[k]; j++, k++) {
                    }

                    if (j == end) {
                        return i - sourceOffset;
                    }
                }
            }
            return -1;
        }

        private void checkEncoding(RubyString string, RubyString pattern) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            checkEncodingNode.executeCheckEncoding(string, pattern);
        }

    }

    @Primitive(name = "string_byte_character_index", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public static abstract class StringByteCharacterIndexNode extends PrimitiveArrayArgumentsNode {

        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();

        public abstract int executeStringByteCharacterIndex(RubyString string, int byteIndex);

        public static StringByteCharacterIndexNode create() {
            return StringByteCharacterIndexNodeFactory.create(null);
        }

        @Specialization(guards = "isSingleByteOptimizable(string, singleByteOptimizableNode)")
        protected int singleByte(RubyString string, int byteIndex) {
            return byteIndex;
        }

        @Specialization(
                guards = {
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)",
                        "isFixedWidthEncoding(string)" })
        protected int fixedWidth(RubyString string, int byteIndex) {
            return byteIndex / string.rope.getEncoding().minLength();
        }

        @Specialization(
                guards = {
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)",
                        "!isFixedWidthEncoding(string)",
                        "isValidUtf8(string, codeRangeNode)" })
        protected int validUtf8(RubyString string, int byteIndex,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            // Taken from Rubinius's String::find_byte_character_index.
            // TODO (nirvdrum 02-Apr-15) There's a way to optimize this for UTF-8, but porting all that code isn't necessary at the moment.
            return notValidUtf8(string, byteIndex, codeRangeNode);
        }

        @TruffleBoundary
        @Specialization(
                guards = {
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)",
                        "!isFixedWidthEncoding(string)",
                        "!isValidUtf8(string, codeRangeNode)" })
        protected int notValidUtf8(RubyString string, int byteIndex,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            // Taken from Rubinius's String::find_byte_character_index and Encoding::find_byte_character_index.

            final Rope rope = string.rope;
            final byte[] bytes = rope.getBytes();
            final Encoding encoding = rope.getEncoding();
            final CodeRange codeRange = rope.getCodeRange();
            int p = 0;
            final int end = bytes.length;
            int charIndex = 0;

            while (p < end && byteIndex > 0) {
                final int charLen = StringSupport.characterLength(encoding, codeRange, bytes, p, end, true);
                p += charLen;
                byteIndex -= charLen;
                charIndex++;
            }

            return charIndex;
        }
    }

    @Primitive(name = "string_character_index", lowerFixnum = 2)
    public static abstract class StringCharacterIndexPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object stringCharacterIndex(RubyString string, RubyString pattern, int offset,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode) {
            if (offset < 0) {
                return nil;
            }

            final Rope stringRope = string.rope;
            final Rope patternRope = pattern.rope;

            final int total = stringRope.byteLength();
            int p = 0;
            final int e = p + total;
            final int pe = patternRope.byteLength();
            final int l = e - pe + 1;

            final byte[] stringBytes = stringRope.getBytes();
            final byte[] patternBytes = patternRope.getBytes();

            if (stringRope.isSingleByteOptimizable()) {
                for (p += offset; p < l; p++) {
                    if (ArrayUtils.memcmp(stringBytes, p, patternBytes, 0, pe) == 0) {
                        return p;
                    }
                }

                return nil;
            }

            final Encoding enc = stringRope.getEncoding();
            final CodeRange cr = stringRope.getCodeRange();
            int index = 0;
            int c = 0;

            while (p < e && index < offset) {
                c = calculateCharacterLengthNode.characterLength(enc, cr, stringBytes, p, e);

                if (StringSupport.MBCLEN_CHARFOUND_P(c)) {
                    p += c;
                    index++;
                } else {
                    return nil;
                }
            }

            for (; p < l; p += c, ++index) {
                c = calculateCharacterLengthNode.characterLength(enc, cr, stringBytes, p, e);
                if (!StringSupport.MBCLEN_CHARFOUND_P(c)) {
                    return nil;
                }
                if (ArrayUtils.memcmp(stringBytes, p, patternBytes, 0, pe) == 0) {
                    return index;
                }
            }

            return nil;
        }
    }

    @Primitive(name = "string_byte_index", lowerFixnum = 2)
    public static abstract class StringByteIndexPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object stringCharacterIndex(RubyString string, RubyString pattern, int offset,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode) {
            if (offset < 0) {
                return nil;
            }

            final Rope stringRope = string.rope;
            final Rope patternRope = pattern.rope;

            final int total = stringRope.byteLength();
            int p = 0;
            final int e = p + total;
            final int pe = patternRope.byteLength();
            final int l = e - pe + 1;

            final byte[] stringBytes = stringRope.getBytes();
            final byte[] patternBytes = patternRope.getBytes();

            p += offset;

            if (stringRope.isSingleByteOptimizable()) {
                for (; p < l; p++) {
                    if (ArrayUtils.memcmp(stringBytes, p, patternBytes, 0, pe) == 0) {
                        return p;
                    }
                }

                return nil;
            }

            final Encoding enc = stringRope.getEncoding();
            final CodeRange cr = stringRope.getCodeRange();
            int c = 0;

            for (; p < l; p += c) {
                c = calculateCharacterLengthNode.characterLength(enc, cr, stringBytes, p, e);
                if (!StringSupport.MBCLEN_CHARFOUND_P(c)) {
                    return nil;
                }
                if (ArrayUtils.memcmp(stringBytes, p, patternBytes, 0, pe) == 0) {
                    return p;
                }
            }

            return nil;
        }
    }

    /** Calculates the byte offset of a character, indicated by a character index, starting from a provided byte offset
     * into the rope. Providing a 0 starting offset simply finds the byte offset for the nth character into the rope,
     * according to the rope's encoding. Providing a non-zero starting byte offset effectively allows for calculating a
     * character's byte offset into a substring of the rope without having to creating a SubstringRope.
     *
     * @rope - The rope/string being indexed.
     * @startByteOffset - Starting position in the rope for the calculation of the character's byte offset.
     * @characterIndex - The character index into the rope, starting from the provided byte offset. */
    @ImportStatic({ RopeGuards.class, StringGuards.class, StringOperations.class })
    public static abstract class ByteIndexFromCharIndexNode extends RubyContextNode {

        public static ByteIndexFromCharIndexNode create() {
            return ByteIndexFromCharIndexNodeGen.create();
        }

        @Child protected SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        public abstract int execute(Rope rope, int startByteOffset, int characterIndex);

        @Specialization(guards = "isSingleByteOptimizable(rope)")
        protected int singleByteOptimizable(Rope rope, int startByteOffset, int characterIndex) {
            return startByteOffset + characterIndex;
        }

        @Specialization(guards = { "!isSingleByteOptimizable(rope)", "isFixedWidthEncoding(rope)" })
        protected int fixedWidthEncoding(Rope rope, int startByteOffset, int characterIndex) {
            final Encoding encoding = rope.getEncoding();
            return startByteOffset + characterIndex * encoding.minLength();
        }

        @Specialization(
                guards = { "!isSingleByteOptimizable(rope)", "!isFixedWidthEncoding(rope)", "characterIndex == 0" })
        protected int multiByteZeroIndex(Rope rope, int startByteOffset, int characterIndex) {
            return startByteOffset;
        }

        @Specialization(guards = { "!isSingleByteOptimizable(rope)", "!isFixedWidthEncoding(rope)" })
        protected int multiBytes(Rope rope, int startByteOffset, int characterIndex,
                @Cached ConditionProfile indexTooLargeProfile,
                @Cached ConditionProfile invalidByteProfile,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            // Taken from Rubinius's String::byte_index.

            final Encoding enc = rope.getEncoding();
            final byte[] bytes = bytesNode.execute(rope);
            final int e = rope.byteLength();
            int p = startByteOffset;

            int i, k = characterIndex;

            for (i = 0; i < k && p < e; i++) {
                final int c = calculateCharacterLengthNode
                        .characterLength(enc, codeRangeNode.execute(rope), bytes, p, e);

                // TODO (nirvdrum 22-Dec-16): Consider having a specialized version for CR_BROKEN strings to avoid these checks.
                // If it's an invalid byte, just treat it as a single byte
                if (invalidByteProfile.profile(!StringSupport.MBCLEN_CHARFOUND_P(c))) {
                    ++p;
                } else {
                    p += StringSupport.MBCLEN_CHARFOUND_LEN(c);
                }
            }

            // TODO (nirvdrum 22-Dec-16): Since we specialize elsewhere on index being too large, do we need this? Can character boundary search in a CR_BROKEN string cause us to encounter this case?
            if (indexTooLargeProfile.profile(i < k)) {
                return -1;
            } else {
                return p;
            }
        }

        protected boolean isSingleByteOptimizable(Rope rope) {
            return singleByteOptimizableNode.execute(rope);
        }

    }

    // Named 'string_byte_index' in Rubinius.
    @Primitive(name = "string_byte_index_from_char_index", lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, StringOperations.class })
    public static abstract class StringByteIndexFromCharIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object singleByteOptimizable(RubyString string, int characterIndex,
                @Cached ByteIndexFromCharIndexNode byteIndexFromCharIndexNode) {
            return byteIndexFromCharIndexNode.execute(string.rope, 0, characterIndex);
        }

    }

    // Port of Rubinius's String::previous_byte_index.
    //
    // This method takes a byte index, finds the corresponding character the byte index belongs to, and then returns
    // the byte index marking the start of the previous character in the string.
    @Primitive(name = "string_previous_byte_index", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public static abstract class StringPreviousByteIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "index < 0")
        protected Object negativeIndex(RubyString string, int index) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative index given", this));
        }

        @Specialization(guards = "index == 0")
        protected Object zeroIndex(RubyString string, int index) {
            return nil;
        }

        @Specialization(guards = { "index > 0", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected int singleByteOptimizable(RubyString string, int index,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            return index - 1;
        }

        @Specialization(
                guards = {
                        "index > 0",
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)",
                        "isFixedWidthEncoding(string)" })
        protected int fixedWidthEncoding(RubyString string, int index,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached ConditionProfile firstCharacterProfile) {
            final Encoding encoding = string.rope.getEncoding();

            // TODO (nirvdrum 11-Apr-16) Determine whether we need to be bug-for-bug compatible with Rubinius.
            // Implement a bug in Rubinius. We already special-case the index == 0 by returning nil. For all indices
            // corresponding to a given character, we treat them uniformly. However, for the first character, we only
            // return nil if the index is 0. If any other index into the first character is encountered, we return 0.
            // It seems unlikely this will ever be encountered in practice, but it's here for completeness.
            if (firstCharacterProfile.profile(index < encoding.maxLength())) {
                return 0;
            }

            return (index / encoding.maxLength() - 1) * encoding.maxLength();
        }

        @Specialization(
                guards = {
                        "index > 0",
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)",
                        "!isFixedWidthEncoding(string)" })
        @TruffleBoundary
        protected Object other(RubyString string, int index,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            final Rope rope = string.rope;
            final int p = 0;
            final int end = p + rope.byteLength();

            final int b = rope.getEncoding().prevCharHead(rope.getBytes(), p, p + index, end);

            if (b == -1) {
                return nil;
            }

            return b - p;
        }

    }

    @Primitive(name = "find_string_reverse", lowerFixnum = 2)
    @ImportStatic(StringGuards.class)
    public static abstract class StringRindexPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child RopeNodes.CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();
        @Child RopeNodes.SingleByteOptimizableNode singleByteNode = RopeNodes.SingleByteOptimizableNode.create();

        @Specialization(guards = "isEmpty(pattern)")
        protected Object stringRindexEmptyPattern(RubyString string, RubyString pattern, int byteOffset) {
            assert byteOffset >= 0;

            return byteOffset;
        }

        @Specialization(
                guards = {
                        "isSingleByteString(pattern)",
                        "!isBrokenCodeRange(pattern, codeRangeNode)",
                        "canMemcmp(string, pattern, singleByteNode)" })
        protected Object stringRindexSingleBytePattern(RubyString string, RubyString pattern, int byteOffset,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached BranchProfile startTooLargeProfile,
                @Cached BranchProfile matchFoundProfile,
                @Cached BranchProfile noMatchProfile) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = string.rope;
            final int end = sourceRope.byteLength();
            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final byte searchByte = bytesNode.execute(pattern.rope)[0];
            int normalizedStart = byteOffset;

            if (normalizedStart >= end) {
                startTooLargeProfile.enter();
                normalizedStart = end - 1;
            }

            for (int i = normalizedStart; i >= 0; i--) {
                if (sourceBytes[i] == searchByte) {
                    matchFoundProfile.enter();
                    return i;
                }
            }

            noMatchProfile.enter();
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmpty(pattern)",
                        "!isSingleByteString(pattern)",
                        "!isBrokenCodeRange(pattern, codeRangeNode)",
                        "canMemcmp(string, pattern, singleByteNode)" })
        protected Object stringRindexMultiBytePattern(RubyString string, RubyString pattern, int byteOffset,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached BranchProfile startOutOfBoundsProfile,
                @Cached BranchProfile startTooCloseToEndProfile,
                @Cached BranchProfile matchFoundProfile,
                @Cached BranchProfile noMatchProfile) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = string.rope;
            final int end = sourceRope.byteLength();
            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final Rope searchRope = pattern.rope;
            final int matchSize = searchRope.byteLength();
            final byte[] searchBytes = bytesNode.execute(searchRope);
            int normalizedStart = byteOffset;

            if (normalizedStart >= end) {
                startOutOfBoundsProfile.enter();
                normalizedStart = end - 1;
            }

            if (end - normalizedStart < matchSize) {
                startTooCloseToEndProfile.enter();
                normalizedStart = end - matchSize;
            }

            for (int i = normalizedStart; i >= 0; i--) {
                if (sourceBytes[i] == searchBytes[0]) {
                    if (ArrayUtils.memcmp(sourceBytes, i, searchBytes, 0, matchSize) == 0) {
                        matchFoundProfile.enter();
                        return i;
                    }
                }
            }

            noMatchProfile.enter();
            return nil;
        }

        @Specialization(guards = "isBrokenCodeRange(pattern, codeRangeNode)")
        protected Object stringRindexBrokenPattern(RubyString string, RubyString pattern, int byteOffset) {
            assert byteOffset >= 0;

            return nil;
        }

        @Specialization(
                guards = {
                        "!isBrokenCodeRange(pattern, codeRangeNode)",
                        "!canMemcmp(string, pattern, singleByteNode)" })
        protected Object stringRindex(RubyString string, RubyString pattern, int byteOffset,
                @Cached RopeNodes.BytesNode stringBytes,
                @Cached RopeNodes.BytesNode patternBytes,
                @Cached RopeNodes.GetByteNode patternGetByteNode,
                @Cached RopeNodes.GetByteNode stringGetByteNode) {
            // Taken from Rubinius's String::rindex.
            assert byteOffset >= 0;

            int pos = byteOffset;

            final Rope stringRope = string.rope;
            final Rope patternRope = pattern.rope;
            final int total = stringRope.byteLength();
            final int matchSize = patternRope.byteLength();

            if (pos >= total) {
                pos = total - 1;
            }

            switch (matchSize) {
                case 0: {
                    return byteOffset;
                }

                case 1: {
                    final int matcher = patternGetByteNode.executeGetByte(patternRope, 0);

                    while (pos >= 0) {
                        if (stringGetByteNode.executeGetByte(stringRope, pos) == matcher) {
                            return pos;
                        }

                        pos--;
                    }

                    return nil;
                }

                default: {
                    if (total - pos < matchSize) {
                        pos = total - matchSize;
                    }

                    int cur = pos;

                    while (cur >= 0) {
                        // TODO (nirvdrum 21-Jan-16): Investigate a more rope efficient memcmp.
                        if (ArrayUtils.memcmp(
                                stringBytes.execute(stringRope),
                                cur,
                                patternBytes.execute(patternRope),
                                0,
                                matchSize) == 0) {
                            return cur;
                        }

                        cur--;
                    }
                }
            }

            return nil;
        }

        private void checkEncoding(RubyString string, RubyString pattern) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            checkEncodingNode.executeCheckEncoding(string, pattern);
        }

    }

    @NonStandard
    @CoreMethod(names = "pattern", constructor = true, required = 2, lowerFixnum = { 1, 2 })
    public static abstract class StringPatternPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateHelperNode = AllocateHelperNode.create();
        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode = RopeNodes.MakeLeafRopeNode.create();
        @Child private RopeNodes.RepeatNode repeatNode = RopeNodes.RepeatNode.create();

        @Specialization(guards = "pattern >= 0")
        protected RubyString stringPatternZero(RubyClass stringClass, int size, int pattern,
                @CachedLanguage RubyLanguage language) {
            final Rope repeatingRope = repeatNode
                    .executeRepeat(RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES[pattern], size);

            final Shape shape = allocateHelperNode.getCachedShape(stringClass);
            final RubyString result = new RubyString(stringClass, shape, false, false, repeatingRope);
            allocateHelperNode.trace(result, this, language);
            return result;
        }

        @Specialization(guards = { "patternFitsEvenly(pattern, size)" })
        protected RubyString stringPatternFitsEvenly(RubyClass stringClass, int size, RubyString pattern,
                @CachedLanguage RubyLanguage language) {
            final Rope rope = pattern.rope;
            final Rope repeatingRope = repeatNode.executeRepeat(rope, size / rope.byteLength());

            final Shape shape = allocateHelperNode.getCachedShape(stringClass);
            final RubyString result = new RubyString(stringClass, shape, false, false, repeatingRope);
            allocateHelperNode.trace(result, this, language);
            return result;
        }

        @TruffleBoundary
        @Specialization(guards = { "!patternFitsEvenly(pattern, size)" })
        protected RubyString stringPattern(RubyClass stringClass, int size, RubyString pattern,
                @CachedLanguage RubyLanguage language) {
            final Rope rope = pattern.rope;
            final byte[] bytes = new byte[size];

            // TODO (nirvdrum 21-Jan-16): Investigate whether using a ConcatRope (potentially combined with a RepeatingRope) would be better here.
            if (!rope.isEmpty()) {
                for (int n = 0; n < size; n += rope.byteLength()) {
                    System.arraycopy(rope.getBytes(), 0, bytes, n, Math.min(rope.byteLength(), size - n));
                }
            }

            // If we reach this specialization, the `size` attribute will cause a truncated `string` to appear at the
            // end of the resulting string in order to pad the value out. A truncated CR_7BIT string is always CR_7BIT.
            // A truncated CR_VALID string could be any of the code range values.
            final CodeRange codeRange = rope.getCodeRange() == CodeRange.CR_7BIT
                    ? CodeRange.CR_7BIT
                    : CodeRange.CR_UNKNOWN;
            final Object characterLength = codeRange == CodeRange.CR_7BIT ? size : NotProvided.INSTANCE;

            final Shape shape = allocateHelperNode.getCachedShape(stringClass);
            final RubyString result = new RubyString(
                    stringClass,
                    shape,
                    false,
                    false,
                    makeLeafRopeNode.executeMake(bytes, pattern.rope.getEncoding(), codeRange, characterLength));
            allocateHelperNode.trace(result, this, language);
            return result;
        }

        protected boolean patternFitsEvenly(RubyString string, int size) {
            final int byteLength = string.rope.byteLength();

            return byteLength > 0 && (size % byteLength) == 0;
        }

    }

    @Primitive(name = "string_splice", lowerFixnum = { 2, 3 })
    @ImportStatic(StringGuards.class)
    public static abstract class StringSplicePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "indexAtStartBound(spliceByteIndex)" })
        protected Object splicePrepend(
                RubyString string,
                RubyString other,
                int spliceByteIndex,
                int byteCountToReplace,
                RubyEncoding rubyEncoding,
                @Cached RopeNodes.SubstringNode prependSubstringNode,
                @Cached RopeNodes.ConcatNode prependConcatNode) {

            final Encoding encoding = rubyEncoding.encoding;
            final Rope original = string.rope;
            final Rope left = other.rope;
            final Rope right = prependSubstringNode
                    .executeSubstring(original, byteCountToReplace, original.byteLength() - byteCountToReplace);

            StringOperations.setRope(string, prependConcatNode.executeConcat(left, right, encoding));

            return string;
        }

        @Specialization(guards = { "indexAtEndBound(string, spliceByteIndex)" })
        protected Object spliceAppend(
                RubyString string,
                RubyString other,
                int spliceByteIndex,
                int byteCountToReplace,
                RubyEncoding rubyEncoding,
                @Cached RopeNodes.ConcatNode appendConcatNode) {
            final Encoding encoding = rubyEncoding.encoding;
            final Rope left = string.rope;
            final Rope right = other.rope;

            StringOperations.setRope(string, appendConcatNode.executeConcat(left, right, encoding));

            return string;
        }

        @Specialization(guards = "!indexAtEitherBounds(string, spliceByteIndex)")
        protected RubyString splice(
                RubyString string,
                RubyString other,
                int spliceByteIndex,
                int byteCountToReplace,
                RubyEncoding rubyEncoding,
                @Cached ConditionProfile insertStringIsEmptyProfile,
                @Cached ConditionProfile splitRightIsEmptyProfile,
                @Cached RopeNodes.SubstringNode leftSubstringNode,
                @Cached RopeNodes.SubstringNode rightSubstringNode,
                @Cached RopeNodes.ConcatNode leftConcatNode,
                @Cached RopeNodes.ConcatNode rightConcatNode) {

            final Encoding encoding = rubyEncoding.encoding;
            final Rope source = string.rope;
            final Rope insert = other.rope;
            final int rightSideStartingIndex = spliceByteIndex + byteCountToReplace;

            final Rope splitLeft = leftSubstringNode.executeSubstring(source, 0, spliceByteIndex);
            final Rope splitRight = rightSubstringNode
                    .executeSubstring(source, rightSideStartingIndex, source.byteLength() - rightSideStartingIndex);

            final Rope joinedLeft;
            if (insertStringIsEmptyProfile.profile(insert.isEmpty())) {
                joinedLeft = splitLeft;
            } else {
                joinedLeft = leftConcatNode.executeConcat(splitLeft, insert, encoding);
            }

            final Rope joinedRight;
            if (splitRightIsEmptyProfile.profile(splitRight.isEmpty())) {
                joinedRight = joinedLeft;
            } else {
                joinedRight = rightConcatNode.executeConcat(joinedLeft, splitRight, encoding);
            }

            StringOperations.setRope(string, joinedRight);
            return string;
        }

        protected boolean indexAtStartBound(int index) {
            return index == 0;
        }

        protected boolean indexAtEndBound(RubyString string, int index) {
            return index == string.rope.byteLength();
        }

        protected boolean indexAtEitherBounds(RubyString string, int index) {
            return indexAtStartBound(index) || indexAtEndBound(string, index);
        }

    }

    @Primitive(name = "string_to_inum", lowerFixnum = 1)
    public static abstract class StringToInumPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object stringToInum(RubyString string, int fixBase, boolean strict, boolean raiseOnError,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached BranchProfile exceptionProfile) {
            try {
                return ConvertBytes.byteListToInum19(
                        getContext(),
                        this,
                        fixnumOrBignumNode,
                        bytesNode,
                        string,
                        fixBase,
                        strict);
            } catch (RaiseException e) {
                exceptionProfile.enter();
                if (!raiseOnError) {
                    return nil;
                }
                throw e;
            }
        }

    }

    @Primitive(name = "string_byte_append")
    public static abstract class StringByteAppendPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.ConcatNode concatNode = RopeNodes.ConcatNode.create();

        @Specialization
        protected RubyString stringByteAppend(RubyString string, RubyString other) {
            final Rope left = string.rope;
            final Rope right = other.rope;

            // The semantics of this primitive are such that the original string's byte[] should be extended without
            // negotiating the encoding.
            StringOperations.setRope(string, concatNode.executeConcat(left, right, left.getEncoding()));
            return string;
        }

    }

    @Primitive(name = "string_substring", lowerFixnum = { 1, 2 })
    @ImportStatic(StringGuards.class)
    public static abstract class StringSubstringPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateHelperNode;
        @Child private NormalizeIndexNode normalizeIndexNode = NormalizeIndexNode.create();
        @Child RopeNodes.CharacterLengthNode characterLengthNode = RopeNodes.CharacterLengthNode.create();
        @Child RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode = RopeNodes.SingleByteOptimizableNode
                .create();
        @Child private RopeNodes.SubstringNode substringNode;

        public abstract Object execute(RubyString string, int index, int length);

        @Specialization(
                guards = {
                        "!indexTriviallyOutOfBounds(string, characterLengthNode, index, length)",
                        "noCharacterSearch(string, singleByteOptimizableNode)" })
        protected Object stringSubstringSingleByte(RubyString string, int index, int length,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile tooLargeTotalProfile,
                @CachedLanguage RubyLanguage language) {
            final Rope rope = string.rope;
            final int ropeCharacterLength = characterLengthNode.execute(rope);
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, ropeCharacterLength);
            int characterLength = length;

            if (negativeIndexProfile.profile(normalizedIndex < 0)) {
                return nil;
            }

            if (tooLargeTotalProfile.profile(normalizedIndex + characterLength > ropeCharacterLength)) {
                characterLength = ropeCharacterLength - normalizedIndex;
            }

            return makeRope(language, string, rope, normalizedIndex, characterLength);
        }

        @Specialization(
                guards = {
                        "!indexTriviallyOutOfBounds(string, characterLengthNode, index, length)",
                        "!noCharacterSearch(string, singleByteOptimizableNode)" })
        protected Object stringSubstringGeneric(RubyString string, int index, int length,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile tooLargeTotalProfile,
                @Cached ConditionProfile foundSingleByteOptimizableDescendentProfile,
                @Cached BranchProfile singleByteOptimizableBaseProfile,
                @Cached BranchProfile leafBaseProfile,
                @Cached BranchProfile slowSearchProfile,
                @Cached ByteIndexFromCharIndexNode byteIndexFromCharIndexNode,
                @CachedLanguage RubyLanguage language) {
            final Rope rope = string.rope;
            final int ropeCharacterLength = characterLengthNode.execute(rope);
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, ropeCharacterLength);
            int characterLength = length;

            if (negativeIndexProfile.profile(normalizedIndex < 0)) {
                return nil;
            }

            if (tooLargeTotalProfile.profile(normalizedIndex + characterLength > ropeCharacterLength)) {
                characterLength = ropeCharacterLength - normalizedIndex;
            }

            final SearchResult searchResult = searchForSingleByteOptimizableDescendant(
                    rope,
                    normalizedIndex,
                    characterLength,
                    singleByteOptimizableBaseProfile,
                    leafBaseProfile,
                    slowSearchProfile);

            if (foundSingleByteOptimizableDescendentProfile
                    .profile(singleByteOptimizableNode.execute(searchResult.rope))) {
                return makeRope(language, string, searchResult.rope, searchResult.index, characterLength);
            }

            return stringSubstringMultiByte(
                    language,
                    string,
                    normalizedIndex,
                    characterLength,
                    byteIndexFromCharIndexNode);
        }

        @Specialization(guards = "indexTriviallyOutOfBounds(string, characterLengthNode, index, length)")
        protected Object stringSubstringNegativeLength(RubyString string, int index, int length) {
            return nil;
        }

        private SearchResult searchForSingleByteOptimizableDescendant(Rope base, int index, int characterLength,
                BranchProfile singleByteOptimizableBaseProfile,
                BranchProfile leafBaseProfile,
                BranchProfile slowSearchProfile) {

            if (singleByteOptimizableNode.execute(base)) {
                singleByteOptimizableBaseProfile.enter();
                return new SearchResult(index, base);
            }

            if (base instanceof LeafRope) {
                leafBaseProfile.enter();
                return new SearchResult(index, base);
            }

            slowSearchProfile.enter();
            return searchForSingleByteOptimizableDescendantSlow(base, index, characterLength);
        }

        @TruffleBoundary
        private SearchResult searchForSingleByteOptimizableDescendantSlow(Rope base, int index, int characterLength) {
            // If we've found something that's single-byte optimizable, we can halt the search. Taking a substring of
            // a single byte optimizable rope is a fast operation.
            if (base.isSingleByteOptimizable()) {
                return new SearchResult(index, base);
            }

            if (base instanceof LeafRope) {
                return new SearchResult(index, base);
            } else if (base instanceof SubstringRope) {
                final SubstringRope substringRope = (SubstringRope) base;
                if (substringRope.isSingleByteOptimizable()) {
                    // the substring byte offset is also a character offset
                    return searchForSingleByteOptimizableDescendantSlow(
                            substringRope.getChild(),
                            index + substringRope.getByteOffset(),
                            characterLength);
                } else {
                    return new SearchResult(index, substringRope);
                }
            } else if (base instanceof ConcatRope) {
                final ConcatRope concatRope = (ConcatRope) base;
                final Rope left = concatRope.getLeft();
                final Rope right = concatRope.getRight();

                if (index + characterLength <= left.characterLength()) {
                    return searchForSingleByteOptimizableDescendantSlow(left, index, characterLength);
                } else if (index >= left.characterLength()) {
                    return searchForSingleByteOptimizableDescendantSlow(
                            right,
                            index - left.characterLength(),
                            characterLength);
                } else {
                    return new SearchResult(index, concatRope);
                }
            } else if (base instanceof RepeatingRope) {
                final RepeatingRope repeatingRope = (RepeatingRope) base;

                if (index + characterLength <= repeatingRope.getChild().characterLength()) {
                    return searchForSingleByteOptimizableDescendantSlow(
                            repeatingRope.getChild(),
                            index,
                            characterLength);
                } else {
                    return new SearchResult(index, repeatingRope);
                }
            } else if (base instanceof NativeRope) {
                final NativeRope nativeRope = (NativeRope) base;
                return new SearchResult(index, nativeRope.toLeafRope());
            } else {
                throw new UnsupportedOperationException(
                        "Don't know how to traverse rope type: " + base.getClass().getName());
            }
        }

        private Object stringSubstringMultiByte(RubyLanguage language, RubyString string, int beg, int characterLen,
                ByteIndexFromCharIndexNode byteIndexFromCharIndexNode) {
            // Taken from org.jruby.RubyString#substr19 & org.jruby.RubyString#multibyteSubstr19.

            final Rope rope = string.rope;
            final int length = rope.byteLength();

            int p;
            final int end = length;
            int substringByteLength;

            p = byteIndexFromCharIndexNode.execute(rope, 0, beg);
            if (p == end) {
                substringByteLength = 0;
            } else {
                int pp = byteIndexFromCharIndexNode.execute(rope, p, characterLen);
                substringByteLength = StringSupport.offset(p, end, pp);
            }

            return makeRope(language, string, rope, p, substringByteLength);
        }

        private RubyString makeRope(RubyLanguage language, RubyString string, Rope rope, int beg, int byteLength) {
            if (allocateHelperNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateHelperNode = insert(AllocateHelperNode.create());
            }

            if (substringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                substringNode = insert(RopeNodes.SubstringNode.create());
            }

            final RubyClass logicalClass = string.getLogicalClass();
            final Shape shape = allocateHelperNode.getCachedShape(logicalClass);
            final RubyString ret = new RubyString(
                    logicalClass,
                    shape,
                    false,
                    string.tainted,
                    substringNode.executeSubstring(rope, beg, byteLength));
            allocateHelperNode.trace(ret, this, language);
            return ret;
        }

        protected static boolean indexTriviallyOutOfBounds(RubyString string,
                RopeNodes.CharacterLengthNode characterLengthNode,
                int index, int length) {
            return (length < 0) || (index > characterLengthNode.execute(string.rope));
        }

        protected static boolean noCharacterSearch(RubyString string,
                RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
            final Rope rope = string.rope;
            return rope.isEmpty() || singleByteOptimizableNode.execute(rope);
        }

        private static final class SearchResult {
            public final int index;
            public final Rope rope;

            public SearchResult(final int index, final Rope rope) {
                this.index = index;
                this.rope = rope;
            }
        }

    }

    @NonStandard
    @CoreMethod(names = "from_bytearray", onSingleton = true, required = 4, lowerFixnum = { 2, 3 })
    public static abstract class StringFromByteArrayPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString stringFromByteArray(
                RubyByteArray byteArray,
                int start,
                int count,
                RubyEncoding rubyEncoding,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final byte[] bytes = byteArray.bytes;
            final byte[] array = ArrayUtils.extractRange(bytes, start, start + count);
            final Encoding encoding = rubyEncoding.encoding;

            return makeStringNode.executeMake(array, encoding, CR_UNKNOWN);
        }

    }

    public static abstract class StringAppendNode extends RubyContextNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child private RopeNodes.ConcatNode concatNode;

        public static StringAppendNode create() {
            return StringAppendNodeGen.create();
        }

        public abstract Rope executeStringAppend(RubyString string, RubyString other);

        @Specialization
        protected Rope stringAppend(RubyString string, RubyString other) {
            final Rope left = string.rope;
            final Rope right = other.rope;

            final Encoding compatibleEncoding = executeCheckEncoding(string, other);

            return executeConcat(left, right, compatibleEncoding);
        }

        private Rope executeConcat(Rope left, Rope right, Encoding compatibleEncoding) {
            if (concatNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                concatNode = insert(RopeNodes.ConcatNode.create());
            }
            return concatNode.executeConcat(left, right, compatibleEncoding);
        }

        private Encoding executeCheckEncoding(RubyString string, RubyString other) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }
            return checkEncodingNode.executeCheckEncoding(string, other);
        }

    }

    @Primitive(name = "string_to_null_terminated_byte_array")
    public static abstract class StringToNullTerminatedByteArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object stringToNullTerminatedByteArray(RubyString string,
                @Cached RopeNodes.BytesNode bytesNode) {
            // NOTE: we always need one copy here, as native code could modify the passed byte[]
            final byte[] bytes = bytesNode.execute(string.rope);
            final byte[] bytesWithNull = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, bytesWithNull, 0, bytes.length);

            return getContext().getEnv().asGuestValue(bytesWithNull);
        }

        @Specialization
        protected Object emptyString(Nil string) {
            return getContext().getEnv().asGuestValue(null);
        }

    }

    @Primitive(name = "string_intern")
    public abstract static class InternNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyString internString(RubyString string) {
            return getContext().getInternedString(string);
        }

    }

}
