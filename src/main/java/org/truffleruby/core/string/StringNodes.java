/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.graalvm.collections.Pair;
import org.jcodings.Config;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
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
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToRopeNodeGen;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.encoding.IsCharacterHeadNode;
import org.truffleruby.core.encoding.EncodingNodes.CheckEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.CheckRopeEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.GetActualEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.NegotiateCompatibleEncodingNode;
import org.truffleruby.core.encoding.Encodings;
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
import org.truffleruby.core.rope.Bytes;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.ConcatRope;
import org.truffleruby.core.rope.ConcatRope.ConcatState;
import org.truffleruby.core.rope.LazyIntRope;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.RepeatingRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeGuards;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeNodes.AreComparableRopesNode;
import org.truffleruby.core.rope.RopeNodes.AsciiOnlyNode;
import org.truffleruby.core.rope.RopeNodes.BytesNode;
import org.truffleruby.core.rope.RopeNodes.CalculateCharacterLengthNode;
import org.truffleruby.core.rope.RopeNodes.CharacterLengthNode;
import org.truffleruby.core.rope.RopeNodes.CodeRangeNode;
import org.truffleruby.core.rope.RopeNodes.CompareRopesNode;
import org.truffleruby.core.rope.RopeNodes.ConcatNode;
import org.truffleruby.core.rope.RopeNodes.FlattenNode;
import org.truffleruby.core.rope.RopeNodes.GetByteNode;
import org.truffleruby.core.rope.RopeNodes.GetBytesObjectNode;
import org.truffleruby.core.rope.RopeNodes.GetCodePointNode;
import org.truffleruby.core.rope.RopeNodes.MakeLeafRopeNode;
import org.truffleruby.core.rope.RopeNodes.RepeatNode;
import org.truffleruby.core.rope.RopeNodes.SingleByteOptimizableNode;
import org.truffleruby.core.rope.RopeNodes.SubstringNode;
import org.truffleruby.core.rope.RopeNodes.WithEncodingNode;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.rope.RopeWithEncoding;
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
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
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

        public abstract RubyString executeMake(Object payload, RubyEncoding encoding, Object codeRange);

        public RubyString fromRope(Rope rope, RubyEncoding rubyEncoding) {
            return executeMake(rope, rubyEncoding, NotProvided.INSTANCE);
        }

        public RubyString fromBuilder(RopeBuilder builder, RubyEncoding encoding, CodeRange codeRange) {
            assert builder.getEncoding() == encoding.jcoding;
            return executeMake(builder.getBytes(), encoding, codeRange);
        }

        /** All callers of this factory method must guarantee that the builder's byte array cannot change after this
         * call, otherwise the rope built from the builder will end up in an inconsistent state. */
        public RubyString fromBuilderUnsafe(RopeBuilder builder, RubyEncoding encoding, CodeRange codeRange) {
            assert builder.getEncoding() == encoding.jcoding;
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

            return executeMake(ropeBytes, encoding, codeRange);
        }

        public static MakeStringNode create() {
            return MakeStringNodeGen.create();
        }

        public static MakeStringNode getUncached() {
            return MakeStringNodeGen.getUncached();
        }

        @Specialization
        protected RubyString makeStringFromRope(Rope rope, RubyEncoding encoding, NotProvided codeRange) {
            assert rope.encoding == encoding.jcoding;
            final RubyString string = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    rope,
                    encoding);
            AllocationTracing.trace(string, this);
            return string;
        }

        @Specialization
        protected RubyString makeStringFromBytes(byte[] bytes, RubyEncoding encoding, CodeRange codeRange,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {
            final LeafRope rope = makeLeafRopeNode
                    .executeMake(bytes, encoding.jcoding, codeRange, NotProvided.INSTANCE);
            final RubyString string = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    rope,
                    encoding);
            AllocationTracing.trace(string, this);
            return string;
        }

        @Specialization(guards = "is7Bit(codeRange)")
        protected RubyString makeAsciiStringFromString(String string, RubyEncoding encoding, CodeRange codeRange) {
            final byte[] bytes = RopeOperations.encodeAsciiBytes(string);

            return executeMake(bytes, encoding, codeRange);
        }

        @Specialization(guards = "!is7Bit(codeRange)")
        protected RubyString makeStringFromString(String string, RubyEncoding encoding, CodeRange codeRange) {
            final byte[] bytes = StringOperations.encodeBytes(string, encoding.jcoding);

            return executeMake(bytes, encoding, codeRange);
        }

        protected static boolean is7Bit(CodeRange codeRange) {
            return codeRange == CR_7BIT;
        }

    }

    public abstract static class StringSubstringNode extends RubyBaseNode {

        @Child private SubstringNode substringNode = SubstringNode.create();

        public static StringSubstringNode create() {
            return StringNodesFactory.StringSubstringNodeGen.create();
        }

        public abstract RubyString executeSubstring(Object string, int offset, int byteLength);

        @Specialization
        protected RubyString substring(Object source, int offset, int byteLength,
                @CachedLibrary(limit = "2") RubyStringLibrary libSource,
                @Cached LogicalClassNode logicalClassNode) {
            final Rope rope = libSource.getRope(source);
            final RubyClass logicalClass = logicalClassNode.execute(source);
            final RubyString string = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    substringNode.executeSubstring(rope, offset, byteLength),
                    libSource.getEncoding(source));
            AllocationTracing.trace(string, this);
            return string;
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().stringShape;
            final RubyString string = new RubyString(
                    rubyClass,
                    shape,
                    false,
                    EMPTY_ASCII_8BIT_ROPE,
                    Encodings.BINARY);
            AllocationTracing.trace(string, this);
            return string;
        }

    }

    @CoreMethod(names = "+", required = 1)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(StringGuards.class)
    public abstract static class AddNode extends CoreMethodNode {

        @CreateCast("other")
        protected ToStrNode coerceOtherToString(RubyBaseNodeWithExecute other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization
        protected RubyString add(Object string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary stringLibrary,
                @Cached StringAppendNode stringAppendNode) {
            final Pair<Rope, RubyEncoding> concatRopeResult = stringAppendNode.executeStringAppend(string, other);
            final RubyClass rubyClass = coreLibrary().stringClass;
            final Shape shape = getLanguage().stringShape;
            final RubyString ret = new RubyString(
                    rubyClass,
                    shape,
                    false,
                    concatRopeResult.getLeft(),
                    concatRopeResult.getRight());
            AllocationTracing.trace(ret, this);
            return ret;
        }
    }

    @CoreMethod(names = "*", required = 1)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "times", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(StringGuards.class)
    public abstract static class MulNode extends CoreMethodNode {

        @CreateCast("times")
        protected RubyBaseNodeWithExecute coerceToInteger(RubyBaseNodeWithExecute times) {
            // Not ToIntNode, because this works with empty strings, and must throw a different error
            // for long values that don't fit in an int.
            return FixnumLowerNode.create(ToLongNode.create(times));
        }

        @Specialization(guards = "times == 0")
        protected RubyString multiplyZero(Object string, int times,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode) {

            final RubyClass logicalClass = logicalClassNode.execute(string);
            final RubyString instance = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    RopeOperations.emptyRope(libString.getRope(string).getEncoding()),
                    libString.getEncoding(string));
            AllocationTracing.trace(instance, this);
            return instance;
        }

        @Specialization(guards = "times < 0")
        protected RubyString multiplyTimesNegative(Object string, long times) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative argument", this));
        }

        @Specialization(guards = { "times > 0", "!isEmpty(libString.getRope(string))" })
        protected RubyString multiply(Object string, int times,
                @Cached @Shared("repeatNode") RepeatNode repeatNode,
                @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode,
                @Cached BranchProfile tooBigProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final Rope stringRope = libString.getRope(string);
            long length = (long) times * stringRope.byteLength();
            if (length > Integer.MAX_VALUE) {
                tooBigProfile.enter();
                throw tooBig();
            }

            final Rope repeated = repeatNode.executeRepeat(stringRope, times);
            final RubyClass logicalClass = logicalClassNode.execute(string);
            final RubyString instance = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    repeated,
                    libString.getEncoding(string));
            AllocationTracing.trace(instance, this);
            return instance;
        }

        @Specialization(guards = { "times > 0", "isEmpty(libString.getRope(string))" })
        protected RubyString multiplyEmpty(Object string, long times,
                @Cached @Shared("repeatNode") RepeatNode repeatNode,
                @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final Rope repeated = repeatNode.executeRepeat(libString.getRope(string), 0);

            final RubyClass logicalClass = logicalClassNode.execute(string);
            final RubyString instance = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    repeated,
                    libString.getEncoding(string));
            AllocationTracing.trace(instance, this);
            return instance;
        }

        @Specialization(guards = { "times > 0", "!isEmpty(strings.getRope(string))" })
        protected RubyString multiplyNonEmpty(Object string, long times,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
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

        @Specialization(guards = "libB.isRubyString(b)")
        protected boolean equalString(Object a, Object b,
                @CachedLibrary(limit = "2") RubyStringLibrary libB) {
            return stringEqualNode.executeStringEqual(a, b);
        }

        @Specialization(guards = "isNotRubyString(b)")
        protected boolean equal(Object a, Object b) {
            if (respondToNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToNode = insert(KernelNodesFactory.RespondToNodeFactory.create(null, null, null));
            }

            if (respondToNode.executeDoesRespondTo(null, b, coreStrings().TO_STR.createInstance(getContext()), false)) {
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
        protected int compare(Object a, Object b,
                @CachedLibrary(limit = "2") RubyStringLibrary libA,
                @CachedLibrary(limit = "2") RubyStringLibrary libB,
                @Cached ConditionProfile sameRopeProfile,
                @Cached CompareRopesNode compareNode) {
            // Taken from org.jruby.RubyString#op_cmp

            final Rope firstRope = libA.getRope(a);
            final Rope secondRope = libB.getRope(b);

            if (sameRopeProfile.profile(firstRope == secondRope)) {
                return 0;
            }

            return compareNode.execute(firstRope, secondRope);
        }

    }

    @CoreMethod(names = { "<<", "concat" }, optional = 1, rest = true, raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class StringConcatNode extends CoreMethodArrayArgumentsNode {

        public static StringConcatNode create() {
            return StringNodesFactory.StringConcatNodeFactory.create(null);
        }

        public abstract Object executeConcat(RubyString string, Object first, Object[] rest);

        @Specialization(guards = "rest.length == 0")
        protected RubyString concatZero(RubyString string, NotProvided first, Object[] rest) {
            return string;
        }

        @Specialization(guards = { "rest.length == 0", "libFirst.isRubyString(first)" })
        protected RubyString concat(RubyString string, Object first, Object[] rest,
                @Cached StringAppendPrimitiveNode stringAppendNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libFirst) {
            return stringAppendNode.executeStringAppend(string, first);
        }

        @Specialization(guards = { "rest.length == 0", "isNotRubyString(first)", "wasProvided(first)" })
        protected Object concatGeneric(RubyString string, Object first, Object[] rest,
                @Cached DispatchNode callNode) {
            return callNode.call(coreLibrary().truffleStringOperationsModule, "concat_internal", string, first);
        }

        @ExplodeLoop
        @Specialization(
                guards = {
                        "wasProvided(first)",
                        "rest.length > 0",
                        "rest.length == cachedLength",
                        "cachedLength <= MAX_EXPLODE_SIZE" })
        protected Object concatMany(RubyString string, Object first, Object[] rest,
                @Cached("rest.length") int cachedLength,
                @Cached StringConcatNode argConcatNode,
                @Cached ConditionProfile selfArgProfile) {
            Rope rope = string.rope;
            Object result = argConcatNode.executeConcat(string, first, EMPTY_ARGUMENTS);
            for (int i = 0; i < cachedLength; ++i) {
                final Object argOrCopy = selfArgProfile.profile(rest[i] == string)
                        ? createString(this, rope, string.encoding)
                        : rest[i];
                result = argConcatNode.executeConcat(string, argOrCopy, EMPTY_ARGUMENTS);
            }
            return result;
        }

        /** Same implementation as {@link #concatMany}, safe for the use of {@code cachedLength} */
        @Specialization(guards = { "wasProvided(first)", "rest.length > 0" }, replaces = "concatMany")
        protected Object concatManyGeneral(RubyString string, Object first, Object[] rest,
                @Cached StringConcatNode argConcatNode,
                @Cached ConditionProfile selfArgProfile) {
            Rope rope = string.rope;
            Object result = argConcatNode.executeConcat(string, first, EMPTY_ARGUMENTS);
            for (Object arg : rest) {
                if (selfArgProfile.profile(arg == string)) {
                    Object copy = createString(this, rope, string.encoding);
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
            argumentNames = { "index_start_range_string_or_regexp", "length_capture" })
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {
        //region Fields

        @Child private NormalizeIndexNode normalizeIndexNode;
        @Child private StringSubstringPrimitiveNode substringNode;
        @Child private ToLongNode toLongNode;
        @Child private CharacterLengthNode charLengthNode;
        private final BranchProfile outOfBounds = BranchProfile.create();

        // endregion
        // region GetIndex Specializations

        @Specialization
        protected Object getIndex(Object string, int index, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return index == charLength(strings.getRope(string)) // Check for the only difference from str[index, 1]
                    ? outOfBoundsNil()
                    : substring(string, index, 1);
        }

        @Specialization
        protected Object getIndex(Object string, long index, NotProvided length) {
            assert (int) index != index : "verified via lowerFixnum";
            return outOfBoundsNil();
        }

        @Specialization(
                guards = {
                        "!isRubyRange(index)",
                        "!isRubyRegexp(index)",
                        "isNotRubyString(index)" })
        protected Object getIndex(Object string, Object index, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            long indexLong = toLong(index);
            int indexInt = (int) indexLong;
            return indexInt != indexLong
                    ? outOfBoundsNil()
                    : getIndex(string, indexInt, length, strings);
        }

        // endregion
        // region Two-Arg Slice Specializations

        @Specialization
        protected Object slice(Object string, int start, int length) {
            return substring(string, start, length);
        }

        @Specialization
        protected Object slice(Object string, long start, long length) {
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
        protected Object slice(Object string, long start, Object length) {
            return slice(string, start, toLong(length));
        }

        @Specialization(
                guards = {
                        "!isRubyRange(start)",
                        "!isRubyRegexp(start)",
                        "isNotRubyString(start)",
                        "wasProvided(length)" })
        protected Object slice(Object string, Object start, Object length) {
            return slice(string, toLong(start), toLong(length));
        }

        // endregion
        // region Range Slice Specializations

        @Specialization
        protected Object sliceIntegerRange(Object string, RubyIntRange range, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            return sliceRange(string, libString, range.begin, range.end, range.excludedEnd);
        }

        @Specialization
        protected Object sliceLongRange(Object string, RubyLongRange range, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            return sliceRange(string, libString, range.begin, range.end, range.excludedEnd);
        }

        @Specialization(guards = "range.isEndless()")
        protected Object sliceEndlessRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final int stringEnd = range.excludedEnd ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
            return sliceRange(string, libString, toLong(range.begin), stringEnd, range.excludedEnd);
        }

        @Specialization(guards = "range.isBeginless()")
        protected Object sliceBeginlessRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            return sliceRange(string, libString, 0L, toLong(range.end), range.excludedEnd);
        }

        @Specialization(guards = "range.isBounded()")
        protected Object sliceObjectRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            return sliceRange(string, libString, toLong(range.begin), toLong(range.end), range.excludedEnd);
        }

        @Specialization(guards = "range.isBoundless()")
        protected Object sliceBoundlessRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final int stringEnd = range.excludedEnd ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
            return sliceRange(string, libString, 0L, stringEnd, range.excludedEnd);
        }

        // endregion
        // region Range Slice Logic

        private Object sliceRange(Object string, RubyStringLibrary libString, long begin, long end,
                boolean excludesEnd) {
            final int beginInt = (int) begin;
            if (beginInt != begin) {
                return outOfBoundsNil();
            }

            int endInt = (int) end;
            if (endInt != end) {
                // Get until the end of the string.
                endInt = excludesEnd ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
            }

            return sliceRange(string, libString, beginInt, endInt, excludesEnd);
        }

        private Object sliceRange(Object string, RubyStringLibrary libString, int begin, int end, boolean excludesEnd) {
            final int stringLength = charLength(libString.getRope(string));
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
        protected Object sliceCapture(VirtualFrame frame, Object string, RubyRegexp regexp, Object maybeCapture,
                @Cached @Exclusive DispatchNode callNode,
                @Cached ReadCallerVariablesNode readCallerStorageNode,
                @Cached ConditionProfile unsetProfile,
                @Cached ConditionProfile sameThreadProfile) {
            final Object capture = RubyGuards.wasProvided(maybeCapture) ? maybeCapture : 0;
            final Object matchStrPair = callNode.call(string, "subpattern", regexp, capture);

            final SpecialVariableStorage variables = readCallerStorageNode.execute(frame);
            if (matchStrPair == nil) {
                variables.setLastMatch(nil, getContext(), unsetProfile, sameThreadProfile);
                return nil;
            } else {
                final Object[] array = (Object[]) ((RubyArray) matchStrPair).store;
                variables.setLastMatch(array[0], getContext(), unsetProfile, sameThreadProfile);
                return array[1];
            }
        }

        // endregion
        // region String Slice Specialization

        @Specialization(guards = "stringsMatchStr.isRubyString(matchStr)")
        protected Object slice2(Object string, Object matchStr, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsMatchStr,
                @Cached @Exclusive DispatchNode includeNode,
                @Cached BooleanCastNode booleanCastNode,
                @Cached @Exclusive DispatchNode dupNode) {

            final Object included = includeNode.call(string, "include?", matchStr);

            if (booleanCastNode.executeToBoolean(included)) {
                return dupNode.call(matchStr, "dup");
            }

            return nil;
        }

        // endregion
        // region Helpers

        private Object outOfBoundsNil() {
            outOfBounds.enter();
            return nil;
        }

        private Object substring(Object string, int start, int length) {
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

        private int charLength(Rope rope) {
            if (charLengthNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                charLengthNode = insert(CharacterLengthNode.create());
            }

            return charLengthNode.execute(rope);
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
        protected boolean asciiOnly(Object string,
                @Cached CodeRangeNode codeRangeNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final CodeRange codeRange = codeRangeNode.execute(libString.getRope(string));

            return codeRange == CR_7BIT;
        }

    }

    @CoreMethod(names = "bytes", needsBlock = true)
    public abstract static class StringBytesNode extends YieldingCoreMethodNode {

        @Child private BytesNode bytesNode = BytesNode.create();

        @Specialization
        protected RubyArray bytes(Object string, Nil block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            final Rope rope = strings.getRope(string);
            final byte[] bytes = bytesNode.execute(rope);

            final int[] store = new int[bytes.length];

            for (int n = 0; n < store.length; n++) {
                store[n] = bytes[n] & 0xFF;
            }

            return createArray(store);
        }

        @Specialization
        protected Object bytes(Object string, RubyProc block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            Rope rope = strings.getRope(string);
            byte[] bytes = bytesNode.execute(rope);

            for (int i = 0; i < bytes.length; i++) {
                callBlock(block, bytes[i] & 0xff);
            }

            return string;
        }

    }

    @CoreMethod(names = "bytesize")
    public abstract static class ByteSizeNode extends CoreMethodArrayArgumentsNode {

        public static ByteSizeNode create() {
            return ByteSizeNodeFactory.create(null);
        }

        public abstract int executeByteSize(Object string);

        @Specialization
        protected int byteSize(RubyString string) {
            return string.rope.byteLength();
        }

        @Specialization
        protected int immutableByteSize(ImmutableRubyString string) {
            return string.rope.byteLength();
        }

    }

    @Primitive(name = "string_casecmp")
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyBaseNodeWithExecute.class)
    public abstract static class CaseCmpNode extends PrimitiveNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleEncodingNode
                .create();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();
        private final ConditionProfile incompatibleEncodingProfile = ConditionProfile.create();

        @CreateCast("other")
        protected ToStrNode coerceOtherToString(RubyBaseNodeWithExecute other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization(guards = "bothSingleByteOptimizable(strings.getRope(string), stringsOther.getRope(other))")
        protected Object caseCmpSingleByte(Object string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsOther) {
            // Taken from org.jruby.RubyString#casecmp19.

            final RubyEncoding encoding = negotiateCompatibleEncodingNode.executeNegotiate(string, other);
            if (incompatibleEncodingProfile.profile(encoding == null)) {
                return nil;
            }

            return RopeOperations.caseInsensitiveCmp(strings.getRope(string), stringsOther.getRope(other));
        }

        @Specialization(guards = "!bothSingleByteOptimizable(strings.getRope(string), stringsOther.getRope(other))")
        protected Object caseCmp(Object string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsOther) {
            // Taken from org.jruby.RubyString#casecmp19 and

            final RubyEncoding encoding = negotiateCompatibleEncodingNode.executeNegotiate(string, other);

            if (incompatibleEncodingProfile.profile(encoding == null)) {
                return nil;
            }

            return StringSupport
                    .multiByteCasecmp(encoding.jcoding, strings.getRope(string), stringsOther.getRope(other));
        }

        protected boolean bothSingleByteOptimizable(Rope stringRope, Rope otherRope) {
            return singleByteOptimizableNode.execute(stringRope) && singleByteOptimizableNode.execute(otherRope);
        }
    }

    /** Returns true if the last bytes in string are equal to the bytes in suffix. */
    @Primitive(name = "string_end_with?")
    public abstract static class EndWithNode extends CoreMethodArrayArgumentsNode {

        @Child IsCharacterHeadNode isCharacterHeadNode;

        @Specialization
        protected boolean endWithBytes(Object string, Object suffix, RubyEncoding enc,
                @Cached BytesNode stringBytesNode,
                @Cached BytesNode suffixBytesNode,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsSuffix,
                @Cached ConditionProfile isCharacterHeadProfile) {

            final Rope stringRope = strings.getRope(string);
            final Rope suffixRope = stringsSuffix.getRope(suffix);
            final int stringByteLength = stringRope.byteLength();
            final int suffixByteLength = suffixRope.byteLength();

            if (stringByteLength < suffixByteLength) {
                return false;
            }
            if (suffixByteLength == 0) {
                return true;
            }
            final byte[] stringBytes = stringBytesNode.execute(stringRope);
            final byte[] suffixBytes = suffixBytesNode.execute(suffixRope);

            final int offset = stringByteLength - suffixByteLength;

            if (isCharacterHeadProfile.profile(!isCharacterHead(enc, stringByteLength, stringBytes, offset))) {
                return false;
            }

            return ArrayUtils.regionEquals(stringBytes, offset, suffixBytes, 0, suffixByteLength);
        }

        private boolean isCharacterHead(RubyEncoding enc, int stringByteLength, byte[] stringBytes, int offset) {
            if (isCharacterHeadNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isCharacterHeadNode = insert(IsCharacterHeadNode.create());
            }
            return isCharacterHeadNode.execute(enc, stringBytes, offset, stringByteLength);
        }

    }

    @CoreMethod(names = "count", rest = true)
    public abstract static class CountNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStr = ToStrNode.create();
        @Child private CountRopesNode countRopesNode = CountRopesNode.create();
        @Child private RubyStringLibrary rubyStringLibrary = RubyStringLibrary.getFactory().createDispatched(2);

        @Specialization(
                guards = "args.length == size",
                limit = "getDefaultCacheLimit()")
        protected int count(VirtualFrame frame, Object string, Object[] args,
                @Cached("args.length") int size) {
            final RopeWithEncoding[] ropesWithEncs = argRopesWithEncs(frame, args, size);
            return countRopesNode.executeCount(string, ropesWithEncs);
        }

        @Specialization(replaces = "count")
        protected int countSlow(VirtualFrame frame, Object string, Object[] args) {
            final RopeWithEncoding[] ropesWithEncs = argRopesSlow(frame, args);
            return countRopesNode.executeCount(string, ropesWithEncs);
        }

        @ExplodeLoop
        protected RopeWithEncoding[] argRopesWithEncs(VirtualFrame frame, Object[] args, int size) {
            final RopeWithEncoding[] strs = new RopeWithEncoding[args.length];
            for (int i = 0; i < size; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new RopeWithEncoding(
                        rubyStringLibrary.getRope(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }

        protected RopeWithEncoding[] argRopesSlow(VirtualFrame frame, Object[] args) {
            final RopeWithEncoding[] strs = new RopeWithEncoding[args.length];
            for (int i = 0; i < args.length; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new RopeWithEncoding(
                        rubyStringLibrary.getRope(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }
    }

    @ImportStatic({ StringGuards.class, StringOperations.class })
    public abstract static class CountRopesNode extends TrTableNode {

        public static CountRopesNode create() {
            return CountRopesNodeFactory.create(null);
        }

        public abstract int executeCount(Object string, RopeWithEncoding[] ropesWithEncs);

        @Specialization(guards = "isEmpty(strings.getRope(string))")
        protected int count(Object string, Object[] args,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return 0;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!isEmpty(libString.getRope(string))",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args)",
                        "encodingsMatch(libString.getRope(string), cachedEncoding)" })
        protected int countFast(Object string, RopeWithEncoding[] args,
                @Cached(value = "args", dimensions = 1) RopeWithEncoding[] cachedArgs,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @Cached("libString.getRope(string).encoding") Encoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(stringToRopeWithEncoding(libString, string), cachedArgs)") RubyEncoding compatEncoding,
                @Cached("makeTables(cachedArgs, squeeze, compatEncoding)") TrTables tables) {
            return processStr(libString.getRope(string), squeeze, compatEncoding, tables);
        }

        @TruffleBoundary
        private int processStr(Rope rope, boolean[] squeeze, RubyEncoding compatEncoding, TrTables tables) {
            return StringSupport.strCount(rope, squeeze, tables, compatEncoding.jcoding, this);
        }

        @Specialization(guards = "!isEmpty(libString.getRope(string))")
        protected int count(Object string, RopeWithEncoding[] ropesWithEncs,
                @Cached BranchProfile errorProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            if (ropesWithEncs.length == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            RubyEncoding enc = findEncoding(
                    new RopeWithEncoding(libString.getRope(string), libString.getEncoding(string)),
                    ropesWithEncs);
            return countSlow(libString.getRope(string), ropesWithEncs, enc);
        }

        @TruffleBoundary
        private int countSlow(Rope stringRope, RopeWithEncoding[] ropesWithEncs, RubyEncoding enc) {
            final boolean[] table = squeeze();
            final StringSupport.TrTables tables = makeTables(ropesWithEncs, table, enc);
            return processStr(stringRope, table, enc, tables);
        }
    }

    public abstract static class TrTableNode extends CoreMethodArrayArgumentsNode {
        @Child protected CheckRopeEncodingNode checkEncodingNode = CheckRopeEncodingNode.create();
        @Child protected RopeNodes.EqualNode ropeEqualNode = RopeNodes.EqualNode.create();

        protected boolean[] squeeze() {
            return new boolean[StringSupport.TRANS_SIZE + 1];
        }

        protected RopeWithEncoding stringToRopeWithEncoding(RubyStringLibrary strings, Object string) {
            return new RopeWithEncoding(strings.getRope(string), strings.getEncoding(string));
        }

        protected RubyEncoding findEncoding(RopeWithEncoding ropeWithEnc, RopeWithEncoding[] ropes) {
            RubyEncoding enc = checkEncodingNode.executeCheckEncoding(ropeWithEnc, ropes[0]);
            for (int i = 1; i < ropes.length; i++) {
                enc = checkEncodingNode.executeCheckEncoding(ropeWithEnc, ropes[i]);
            }
            return enc;
        }

        protected TrTables makeTables(RopeWithEncoding[] ropesWithEncs, boolean[] squeeze, RubyEncoding enc) {
            // The trSetupTable method will consume the bytes from the rope one encoded character at a time and
            // build a TrTable from this. Previously we started with the encoding of rope zero, and at each
            // stage found a compatible encoding to build that TrTable with. Although we now calculate a single
            // encoding with which to build the tables it must be compatible with all ropes, so will not
            // affect the consumption of characters from those ropes.
            StringSupport.TrTables tables = StringSupport.trSetupTable(
                    ropesWithEncs[0].getRope(),
                    squeeze,
                    null,
                    true,
                    enc.jcoding,
                    this);

            for (int i = 1; i < ropesWithEncs.length; i++) {
                tables = StringSupport
                        .trSetupTable(ropesWithEncs[i].getRope(), squeeze, tables, false, enc.jcoding, this);
            }
            return tables;
        }

        protected boolean encodingsMatch(Rope rope, Encoding encoding) {
            return encoding == rope.getEncoding();
        }

        @ExplodeLoop
        protected boolean argsMatch(RopeWithEncoding[] cachedRopes, RopeWithEncoding[] ropes) {
            for (int i = 0; i < cachedRopes.length; i++) {
                if (!ropeEqualNode.execute(cachedRopes[i].getRope(), ropes[i].getRope())) {
                    return false;
                }
                if (cachedRopes[i].getEncoding() != ropes[i].getEncoding()) {
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
        @Child private RubyStringLibrary rubyStringLibrary = RubyStringLibrary.getFactory().createDispatched(2);

        public static DeleteBangNode create() {
            return DeleteBangNodeFactory.create(null);
        }

        public abstract Object executeDeleteBang(RubyString string, Object[] args);

        @Specialization(guards = "args.length == size", limit = "getDefaultCacheLimit()")
        protected Object deleteBang(RubyString string, Object[] args,
                @Cached("args.length") int size) {
            final RopeWithEncoding[] ropesWithEncs = argRopesWithEncs(args, size);
            return deleteBangRopesNode.executeDeleteBang(string, ropesWithEncs);
        }

        @Specialization(replaces = "deleteBang")
        protected Object deleteBangSlow(RubyString string, Object[] args) {
            final RopeWithEncoding[] ropes = argRopesWithEncsSlow(args);
            return deleteBangRopesNode.executeDeleteBang(string, ropes);
        }

        @ExplodeLoop
        protected RopeWithEncoding[] argRopesWithEncs(Object[] args, int size) {
            final RopeWithEncoding[] strs = new RopeWithEncoding[size];
            for (int i = 0; i < size; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new RopeWithEncoding(
                        rubyStringLibrary.getRope(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }

        protected RopeWithEncoding[] argRopesWithEncsSlow(Object[] args) {
            final RopeWithEncoding[] strs = new RopeWithEncoding[args.length];
            for (int i = 0; i < args.length; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new RopeWithEncoding(
                        rubyStringLibrary.getRope(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }
    }

    @ImportStatic({ StringGuards.class, StringOperations.class })
    public abstract static class DeleteBangRopesNode extends TrTableNode {

        public static DeleteBangRopesNode create() {
            return DeleteBangRopesNodeFactory.create(null);
        }

        public abstract Object executeDeleteBang(RubyString string, RopeWithEncoding[] ropesWithEncs);

        @Specialization(guards = "isEmpty(string.rope)")
        protected Object deleteBangEmpty(RubyString string, Object[] args) {
            return nil;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!isEmpty(string.rope)",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args)",
                        "encodingsMatch(libString.getRope(string), cachedEncoding)" })
        protected Object deleteBangFast(RubyString string, RopeWithEncoding[] args,
                @Cached(value = "args", dimensions = 1) RopeWithEncoding[] cachedArgs,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @Cached("libString.getRope(string).encoding") Encoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(stringToRopeWithEncoding(libString, string), cachedArgs)") RubyEncoding compatEncoding,
                @Cached("makeTables(cachedArgs, squeeze, compatEncoding)") TrTables tables,
                @Cached BranchProfile nullProfile) {
            final Rope processedRope = processStr(string, squeeze, compatEncoding, tables);
            if (processedRope == null) {
                nullProfile.enter();
                return nil;
            }

            string.setRope(processedRope);
            return string;
        }

        @Specialization(guards = "!isEmpty(string.rope)")
        protected Object deleteBang(RubyString string, RopeWithEncoding[] args,
                @Cached BranchProfile errorProfile) {
            if (args.length == 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            RubyEncoding enc = findEncoding(new RopeWithEncoding(string.rope, string.encoding), args);

            return deleteBangSlow(string, args, enc);
        }

        @TruffleBoundary
        private Object deleteBangSlow(RubyString string, RopeWithEncoding[] ropesWithEncs, RubyEncoding enc) {
            final boolean[] squeeze = new boolean[StringSupport.TRANS_SIZE + 1];

            final StringSupport.TrTables tables = makeTables(ropesWithEncs, squeeze, enc);

            final Rope processedRope = processStr(string, squeeze, enc, tables);
            if (processedRope == null) {
                return nil;
            }

            string.setRope(processedRope);
            // REVIEW encoding set

            return string;
        }

        @TruffleBoundary
        private Rope processStr(RubyString string, boolean[] squeeze, RubyEncoding enc, StringSupport.TrTables tables) {
            return StringSupport.delete_bangCommon19(string.rope, squeeze, tables, enc.jcoding, this);
        }
    }

    @Primitive(name = "string_downcase!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringDowncaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createUpperToLower()") InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("bytesNode") BytesNode bytesNode,
                @Cached CharacterLengthNode characterLengthNode,
                @Cached @Shared("codeRangeNode") CodeRangeNode codeRangeNode,
                @Cached @Shared("makeLeafRopeNode") MakeLeafRopeNode makeLeafRopeNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
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
                string.setRope(
                        makeLeafRopeNode.executeMake(outputBytes, encoding, cr, characterLengthNode.execute(rope)));
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = { "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("bytesNode") BytesNode bytesNode,
                @Cached @Shared("codeRangeNode") CodeRangeNode codeRangeNode,
                @Cached @Shared("makeLeafRopeNode") MakeLeafRopeNode makeLeafRopeNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding encoding = rope.getEncoding();

            if (dummyEncodingProfile.profile(encoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding, this));
            }

            final RopeBuilder builder = RopeBuilder.createRopeBuilder(bytesNode.execute(rope), rope.getEncoding());
            final boolean modified = StringSupport
                    .downcaseMultiByteComplex(encoding, codeRangeNode.execute(rope), builder, caseMappingOptions, this);

            if (modifiedProfile.profile(modified)) {
                string.setRope(
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
        protected Object eachByte(Object string, RubyProc block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached BytesNode bytesNode,
                @Cached BytesNode updatedBytesNode,
                @Cached ConditionProfile ropeChangedProfile) {
            Rope rope = strings.getRope(string);
            byte[] bytes = bytesNode.execute(rope);

            for (int i = 0; i < bytes.length; i++) {
                callBlock(block, bytes[i] & 0xff);

                Rope updatedRope = strings.getRope(string);
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

        @Child private SubstringNode substringNode = SubstringNode.create();
        @Child private BytesNode bytesNode = BytesNode.create();

        @Specialization
        protected Object eachChar(Object string, RubyProc block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached LogicalClassNode logicalClassNode) {
            final Rope rope = strings.getRope(string);
            final RubyEncoding encoding = strings.getEncoding(string);
            final byte[] ptrBytes = bytesNode.execute(rope);
            final int len = ptrBytes.length;
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);

            int n;

            for (int i = 0; i < len; i += n) {
                n = calculateCharacterLengthNode
                        .characterLengthWithRecovery(enc, cr, Bytes.fromRange(ptrBytes, i, len));
                callBlock(block, substr(rope, encoding, i, n, logicalClassNode.execute(string)));
            }

            return string;
        }

        // TODO (nirvdrum 10-Mar-15): This was extracted from JRuby, but likely will need to become a primitive.
        // Don't be tempted to extract the rope from the passed string. If the block being yielded to modifies the
        // source string, you'll get a different rope. Unlike String#each_byte, String#each_char does not make
        // modifications to the string visible to the rest of the iteration.
        private Object substr(Rope rope, RubyEncoding encoding, int beg, int len, RubyClass logicalClass) {
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
            final RubyString ret = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    substringRope,
                    encoding);
            AllocationTracing.trace(ret, this);
            return ret;
        }
    }

    @CoreMethod(names = "force_encoding", required = 1, raiseIfFrozenSelf = true)
    public abstract static class ForceEncodingNode extends CoreMethodArrayArgumentsNode {

        @Child private WithEncodingNode withEncodingNode = WithEncodingNode.create();
        private final ConditionProfile differentEncodingProfile = ConditionProfile.create();

        public abstract RubyString execute(Object string, Object other);

        public static ForceEncodingNode create() {
            return StringNodesFactory.ForceEncodingNodeFactory.create(null);
        }

        @Specialization(guards = "libEncoding.isRubyString(encoding)")
        protected RubyString forceEncodingString(RubyString string, Object encoding,
                @CachedLibrary(limit = "2") RubyStringLibrary libEncoding,
                @Cached BranchProfile errorProfile) {
            final String stringName = libEncoding.getJavaString(encoding);
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

            if (differentEncodingProfile.profile(string.encoding != encoding)) {
                final Encoding javaEncoding = encoding.jcoding;
                final Rope rope = string.rope;
                final Rope newRope = withEncodingNode.executeWithEncoding(rope, javaEncoding);
                string.setRope(newRope, encoding);
            }

            return string;
        }

        @Specialization(guards = { "isNotRubyString(encoding)", "!isRubyEncoding(encoding)" })
        protected RubyString forceEncoding(RubyString string, Object encoding,
                @Cached ToStrNode toStrNode,
                @Cached ForceEncodingNode forceEncodingNode) {
            return forceEncodingNode.execute(string, toStrNode.execute(encoding));
        }

    }

    @CoreMethod(names = "getbyte", required = 1, lowerFixnum = 1)
    public abstract static class StringGetByteNode extends CoreMethodArrayArgumentsNode {

        @Child private NormalizeIndexNode normalizeIndexNode = NormalizeIndexNode.create();
        @Child private GetByteNode ropeGetByteNode = GetByteNode.create();

        @Specialization
        protected Object getByte(Object string, int index,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final Rope rope = libString.getRope(string);
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, rope.byteLength());

            if (indexOutOfBoundsProfile.profile((normalizedIndex < 0) || (normalizedIndex >= rope.byteLength()))) {
                return nil;
            }

            return ropeGetByteNode.executeGetByte(rope, normalizedIndex);
        }

    }

    @GenerateUncached
    public abstract static class HashStringNode extends RubyBaseNode {

        protected static final int CLASS_SALT = 54008340; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        public static HashStringNode create() {
            return StringNodesFactory.HashStringNodeGen.create();
        }

        public abstract long execute(Object string);

        @Specialization
        protected long hash(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached RopeNodes.HashNode hashNode) {
            return getContext().getHashing(this).hash(CLASS_SALT, hashNode.execute(strings.getRope(string)));
        }
    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        protected static final int CLASS_SALT = 54008340; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        public static HashNode create() {
            return StringNodesFactory.HashNodeFactory.create(null);
        }

        public abstract long execute(Object string);

        @Specialization
        protected long hash(Object string,
                @Cached HashStringNode hash) {
            return hash.execute(string);
        }
    }

    @Primitive(name = "string_initialize")
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString initializeJavaString(RubyString string, String from, RubyEncoding encoding) {
            string.setRope(StringOperations.encodeRope(from, encoding.jcoding), encoding);
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

        @Specialization(guards = "stringsFrom.isRubyString(from)")
        protected RubyString initialize(RubyString string, Object from, Object encoding,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsFrom) {
            string.setRope(stringsFrom.getRope(from), stringsFrom.getEncoding(from));
            return string;
        }

        @Specialization(guards = { "isNotRubyString(from)", "!isString(from)" })
        protected RubyString initialize(VirtualFrame frame, RubyString string, Object from, Object encoding,
                @CachedLibrary(limit = "2") RubyStringLibrary stringLibrary,
                @Cached ToStrNode toStrNode) {
            final Object stringFrom = toStrNode.execute(from);
            string.setRope(stringLibrary.getRope(stringFrom), stringLibrary.getEncoding(stringFrom));
            return string;
        }

    }

    @Primitive(name = "string_get_coderange")
    public abstract static class GetCodeRangeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int getCodeRange(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached CodeRangeNode codeRangeNode) {
            return codeRangeNode.execute(strings.getRope(string)).toInt();
        }

    }

    @Primitive(name = "string_get_rope")
    public abstract static class GetRopeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Rope getRope(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return strings.getRope(string);
        }
    }

    public abstract static class StringGetAssociatedNode extends RubyBaseNode {

        public static StringNodes.StringGetAssociatedNode create() {
            return StringNodesFactory.StringGetAssociatedNodeGen.create();
        }

        public abstract Object execute(Object string);

        @Specialization(limit = "1")
        protected Object getAssociated(RubyString string,
                @CachedLibrary("string") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.getOrDefault(string, Layouts.ASSOCIATED_IDENTIFIER, null);
        }

        @Specialization
        protected Object getAssociatedImmutable(ImmutableRubyString string) {
            return null;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1, raiseIfFrozenSelf = true)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private WriteObjectFieldNode writeAssociatedNode; // for synchronization

        @Specialization(guards = "areEqual(self, from)")
        protected Object initializeCopySelfIsSameAsFrom(RubyString self, Object from) {
            return self;
        }

        @Specialization(
                guards = {
                        "stringsFrom.isRubyString(from)",
                        "!areEqual(self, from)",
                        "!isNativeRope(stringsFrom.getRope(from))" })
        protected Object initializeCopy(RubyString self, Object from,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsFrom,
                @Cached @Shared("stringGetAssociatedNode") StringGetAssociatedNode stringGetAssociatedNode) {
            self.setRope(stringsFrom.getRope(from), stringsFrom.getEncoding(from));
            final Object associated = stringGetAssociatedNode.execute(from);
            copyAssociated(self, associated);
            return self;
        }

        @Specialization(
                guards = {
                        "stringsFrom.isRubyString(from)",
                        "!areEqual(self, from)",
                        "isNativeRope(stringsFrom.getRope(from))" })
        protected Object initializeCopyFromNative(RubyString self, Object from,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsFrom,
                @Cached @Shared("stringGetAssociatedNode") StringGetAssociatedNode stringGetAssociatedNode) {
            self.setRope(
                    ((NativeRope) stringsFrom.getRope(from)).makeCopy(getContext()),
                    stringsFrom.getEncoding(from));
            final Object associated = stringGetAssociatedNode.execute(from);
            copyAssociated(self, associated);
            return self;
        }

        protected static boolean areEqual(Object one, Object two) {
            return one == two;
        }

        private void copyAssociated(RubyString self, Object associated) {
            if (associated != null) {
                if (writeAssociatedNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    writeAssociatedNode = insert(WriteObjectFieldNode.create());
                }

                writeAssociatedNode.execute(self, Layouts.ASSOCIATED_IDENTIFIER, associated);
            }
        }

        protected boolean isNativeRope(Rope other) {
            return other instanceof NativeRope;
        }
    }

    @CoreMethod(names = "lstrip!", raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class LstripBangNode extends CoreMethodArrayArgumentsNode {

        @Child private GetCodePointNode getCodePointNode = GetCodePointNode.create();
        @Child private SubstringNode substringNode = SubstringNode.create();

        @Specialization(guards = "isEmpty(string.rope)")
        protected Object lstripBangEmptyString(RubyString string) {
            return nil;
        }

        @Specialization(
                guards = { "!isEmpty(string.rope)", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object lstripBangSingleByte(RubyString string,
                @Cached BytesNode bytesNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached ConditionProfile noopProfile) {
            // Taken from org.jruby.RubyString#lstrip_bang19 and org.jruby.RubyString#singleByteLStrip.

            final Rope rope = string.rope;
            final int firstCodePoint = getCodePointNode.executeGetCodePoint(string.encoding, rope, 0);

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

            string.setRope(substringNode.executeSubstring(rope, p, end - p));

            return string;
        }

        @TruffleBoundary
        @Specialization(
                guards = { "!isEmpty(string.rope)", "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object lstripBang(RubyString string,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            // Taken from org.jruby.RubyString#lstrip_bang19 and org.jruby.RubyString#multiByteLStrip.

            final Rope rope = string.rope;
            final RubyEncoding enc = getActualEncodingNode.execute(rope, strings.getEncoding(string));
            final int s = 0;
            final int end = s + rope.byteLength();

            int p = s;
            while (p < end) {
                int c = getCodePointNode.executeGetCodePoint(enc, rope, p);
                if (!ASCIIEncoding.INSTANCE.isSpace(c)) {
                    break;
                }
                p += StringSupport.codeLength(enc.jcoding, c);
            }

            if (p > s) {
                string.setRope(substringNode.executeSubstring(rope, p - s, end - p));

                return string;
            }

            return nil;
        }
    }

    @CoreMethod(names = "ord")
    @ImportStatic(StringGuards.class)
    public abstract static class OrdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isEmpty(strings.getRope(string))" })
        protected int ordEmpty(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("empty string", this));
        }

        @Specialization(guards = { "!isEmpty(strings.getRope(string))" })
        protected int ord(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached GetCodePointNode getCodePointNode) {
            return getCodePointNode.executeGetCodePoint(strings.getEncoding(string), strings.getRope(string), 0);
        }

    }

    @CoreMethod(names = "replace", required = 1, raiseIfFrozenSelf = true)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyBaseNodeWithExecute.class)
    public abstract static class ReplaceNode extends CoreMethodNode {

        @CreateCast("other")
        protected ToStrNode coerceOtherToString(RubyBaseNodeWithExecute other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization(guards = "string == other")
        protected RubyString replaceStringIsSameAsOther(RubyString string, RubyString other) {
            return string;
        }


        @Specialization(guards = { "string != other" })
        protected RubyString replace(RubyString string, RubyString other) {
            string.setRope(other.rope, other.encoding);
            return string;
        }

        @Specialization
        protected RubyString replace(RubyString string, ImmutableRubyString other) {
            string.setRope(other.rope, other.getEncoding());
            return string;
        }

    }

    @CoreMethod(names = "rstrip!", raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class RstripBangNode extends CoreMethodArrayArgumentsNode {

        @Child private GetCodePointNode getCodePointNode = GetCodePointNode.create();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();
        @Child private SubstringNode substringNode = SubstringNode.create();

        @Specialization(guards = "isEmpty(string.rope)")
        protected Object rstripBangEmptyString(RubyString string) {
            return nil;
        }

        @Specialization(
                guards = { "!isEmpty(string.rope)", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object rstripBangSingleByte(RubyString string,
                @Cached BytesNode bytesNode,
                @Cached @Exclusive ConditionProfile noopProfile) {
            // Taken from org.jruby.RubyString#rstrip_bang19 and org.jruby.RubyString#singleByteRStrip19.

            final Rope rope = string.rope;
            final int lastCodePoint = getCodePointNode
                    .executeGetCodePoint(string.encoding, rope, rope.byteLength() - 1);

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

            string.setRope(substringNode.executeSubstring(rope, 0, endp + 1));

            return string;
        }

        @TruffleBoundary
        @Specialization(
                guards = { "!isEmpty(string.rope)", "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object rstripBang(RubyString string,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached @Exclusive ConditionProfile dummyEncodingProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            // Taken from org.jruby.RubyString#rstrip_bang19 and org.jruby.RubyString#multiByteRStrip19.

            final Rope rope = string.rope;
            final RubyEncoding enc = getActualEncodingNode.execute(rope, strings.getEncoding(string));

            if (dummyEncodingProfile.profile(enc.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc.jcoding, this));
            }

            final byte[] bytes = rope.getBytes();
            final int start = 0;
            final int end = rope.byteLength();

            int endp = end;
            int prev;
            while ((prev = prevCharHead(enc.jcoding, bytes, start, endp, end)) != -1) {
                int point = getCodePointNode.executeGetCodePoint(enc, rope, prev);
                if (point != 0 && !ASCIIEncoding.INSTANCE.isSpace(point)) {
                    break;
                }
                endp = prev;
            }

            if (endp < end) {
                string.setRope(substringNode.executeSubstring(rope, 0, endp - start));

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

        @Child private CallBlockNode yieldNode = CallBlockNode.create();
        @Child CodeRangeNode codeRangeNode = CodeRangeNode.create();
        @Child private ConcatNode concatNode = ConcatNode.create();
        @Child private SubstringNode substringNode = SubstringNode.create();
        @Child private MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
        @Child private CalculateCharacterLengthNode calculateCharacterLengthNode = CalculateCharacterLengthNode
                .create();
        @Child private BytesNode bytesNode = BytesNode.create();

        @Specialization(
                guards = {
                        "isBrokenCodeRange(rope, codeRangeNode)",
                        "isAsciiCompatible(rope)" })
        protected RubyString scrubAsciiCompat(Object string, RubyProc block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Bind("strings.getRope(string)") Rope rope) {
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
                int ret = calculateCharacterLengthNode.characterLength(enc, CR_BROKEN, Bytes.fromRange(pBytes, p, e));
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
                    final Rope subStringRope = substringNode.executeSubstring(rope, p, clen);
                    Object repl = yieldNode
                            .yield(block, makeStringNode.fromRope(subStringRope, strings.getEncoding(string)));
                    buf = concatNode.executeConcat(buf, strings.getRope(repl), enc);
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
                final Rope subStringRope = substringNode.executeSubstring(rope, p, e - p);
                Object repl = yieldNode
                        .yield(block, makeStringNode.fromRope(subStringRope, strings.getEncoding(string)));
                buf = concatNode.executeConcat(buf, strings.getRope(repl), enc);
            }
            return makeStringNode.fromRope(buf, strings.getEncoding(string));
        }

        @Specialization(
                guards = {
                        "isBrokenCodeRange(rope, codeRangeNode)",
                        "!isAsciiCompatible(rope)" })
        protected RubyString scrubAsciiIncompatible(Object string, RubyProc block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Bind("strings.getRope(string)") Rope rope,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode) {
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);
            Rope buf = RopeConstants.EMPTY_ASCII_8BIT_ROPE;

            final byte[] pBytes = bytesNode.execute(rope);
            final int e = pBytes.length;

            int p = 0;
            int p1 = 0;
            final int mbminlen = enc.minLength();

            while (p < e) {
                int ret = calculateCharacterLengthNode.characterLength(enc, CR_BROKEN, Bytes.fromRange(pBytes, p, e));
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
                            ret = calculateCharacterLengthNode.characterLength(enc, cr, new Bytes(pBytes, q, clen));
                            if (MBCLEN_NEEDMORE_P(ret)) {
                                break;
                            }
                        }
                    }

                    final Rope subStringRope = substringNode.executeSubstring(rope, p, clen);
                    RubyString repl = (RubyString) yieldNode.yield(
                            block,
                            makeStringNode.fromRope(subStringRope, strings.getEncoding(string)));
                    buf = concatNode.executeConcat(buf, repl.rope, enc);
                    p += clen;
                    p1 = p;
                }
            }
            if (p1 < p) {
                buf = concatNode.executeConcat(buf, substringNode.executeSubstring(rope, p1, p - p1), enc);
            }
            if (p < e) {
                final Rope subStringRope = substringNode.executeSubstring(rope, p, e - p);
                RubyString repl = (RubyString) yieldNode.yield(
                        block,
                        makeStringNode.fromRope(subStringRope, strings.getEncoding(string)));
                buf = concatNode.executeConcat(buf, repl.rope, enc);
            }

            return makeStringNode.fromRope(buf, strings.getEncoding(string));
        }

    }

    @Primitive(name = "string_swapcase!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringSwapcaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object swapcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createSwapCase()") InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object swapcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("bytesNode") BytesNode bytesNode,
                @Cached CharacterLengthNode characterLengthNode,
                @Cached @Shared("codeRangeNode") CodeRangeNode codeRangeNode,
                @Cached @Shared("makeLeafRopeNode") MakeLeafRopeNode makeLeafRopeNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
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
                string.setRope(
                        makeLeafRopeNode.executeMake(outputBytes, enc, cr, characterLengthNode.execute(rope)));
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object swapcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("bytesNode") BytesNode bytesNode,
                @Cached @Shared("codeRangeNode") CodeRangeNode codeRangeNode,
                @Cached @Shared("makeLeafRopeNode") MakeLeafRopeNode makeLeafRopeNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
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
                    .swapCaseMultiByteComplex(enc, codeRangeNode.execute(rope), builder, caseMappingOptions, this);

            if (modifiedProfile.profile(modified)) {
                string.setRope(
                        makeLeafRopeNode
                                .executeMake(builder.getBytes(), rope.getEncoding(), CR_UNKNOWN, NotProvided.INSTANCE));

                return string;
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = "dump")
    @ImportStatic(StringGuards.class)
    public abstract static class DumpNode extends CoreMethodArrayArgumentsNode {

        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();

        @TruffleBoundary
        @Specialization(guards = "isAsciiCompatible(libString.getRope(string))")
        protected RubyString dumpAsciiCompatible(Object string,
                @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // Taken from org.jruby.RubyString#dump

            RopeBuilder outputBytes = dumpCommon(libString.getRope(string));
            outputBytes.setEncoding(libString.getRope(string).getEncoding());

            final Rope rope = makeLeafRopeNode
                    .executeMake(outputBytes.getBytes(), outputBytes.getEncoding(), CR_7BIT, outputBytes.getLength());

            final RubyClass logicalClass = logicalClassNode.execute(string);
            final RubyString result = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    rope,
                    libString.getEncoding(string));
            AllocationTracing.trace(result, this);
            return result;
        }

        @TruffleBoundary
        @Specialization(guards = "!isAsciiCompatible(libString.getRope(string))")
        protected RubyString dump(Object string,
                @Cached @Shared("logicalClassNode") LogicalClassNode logicalClassNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // Taken from org.jruby.RubyString#dump

            RopeBuilder outputBytes = dumpCommon(libString.getRope(string));

            try {
                outputBytes.append(".force_encoding(\"".getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(e);
            }

            outputBytes.append(libString.getRope(string).getEncoding().getName());
            outputBytes.append((byte) '"');
            outputBytes.append((byte) ')');

            outputBytes.setEncoding(ASCIIEncoding.INSTANCE);

            final Rope rope = makeLeafRopeNode
                    .executeMake(outputBytes.getBytes(), outputBytes.getEncoding(), CR_7BIT, outputBytes.getLength());

            final RubyClass logicalClass = logicalClassNode.execute(string);
            final RubyString result = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    rope,
                    Encodings.BINARY);
            AllocationTracing.trace(result, this);
            return result;
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
                                    int cc = StringSupport.codePoint(enc, rope.getCodeRange(), bytes, p - 1, end, this);
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
                            int cc = StringSupport.codePoint(enc, cr, bytes, p - 1, end, this);
                            p += n;
                            outBytes.setLength(q);
                            outBytes.append(StringUtils.formatASCIIBytes("u%04X", cc));
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

    }

    @CoreMethod(names = "undump")
    @ImportStatic(StringGuards.class)
    public abstract static class UndumpNode extends CoreMethodArrayArgumentsNode {
        @Specialization(guards = "isAsciiCompatible(libString.getRope(string))")
        protected RubyString undumpAsciiCompatible(Object string,
                @Cached MakeStringNode makeStringNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // Taken from org.jruby.RubyString#undump
            Pair<RopeBuilder, RubyEncoding> outputBytesResult = StringSupport.undump(
                    libString.getRope(string),
                    libString.getEncoding(string),
                    getContext(),
                    this);
            final RubyEncoding rubyEncoding = outputBytesResult.getRight();
            return makeStringNode.fromBuilder(outputBytesResult.getLeft(), rubyEncoding, CR_UNKNOWN);
        }

        @Specialization(guards = "!isAsciiCompatible(libString.getRope(string))")
        protected RubyString undumpNonAsciiCompatible(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().encodingCompatibilityError(
                            Utils.concat("ASCII incompatible encoding: ", libString.getRope(string).encoding),
                            this));
        }

    }

    @CoreMethod(names = "setbyte", required = 2, raiseIfFrozenSelf = true, lowerFixnum = { 1, 2 })
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "index", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "value", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(StringGuards.class)
    public abstract static class SetByteNode extends CoreMethodNode {

        @Child private CheckIndexNode checkIndexNode = CheckIndexNodeGen.create();
        @Child private RopeNodes.SetByteNode setByteNode = RopeNodes.SetByteNode.create();

        @CreateCast("index")
        protected ToIntNode coerceIndexToInt(RubyBaseNodeWithExecute index) {
            return ToIntNode.create(index);
        }

        @CreateCast("value")
        protected ToIntNode coerceValueToInt(RubyBaseNodeWithExecute value) {
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
                string.setRope(newRope);
            }

            return value;
        }

    }

    public abstract static class CheckIndexNode extends RubyBaseNode {

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

    public abstract static class NormalizeIndexNode extends RubyBaseNode {

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

        public abstract int execute(Object string);

        @Specialization
        protected int size(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @Cached CharacterLengthNode characterLengthNode) {
            return characterLengthNode.execute(libString.getRope(string));
        }

    }

    @CoreMethod(names = "squeeze!", rest = true, raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class SqueezeBangNode extends CoreMethodArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        private final ConditionProfile singleByteOptimizableProfile = ConditionProfile.create();

        @Specialization(guards = "isEmpty(string.rope)")
        protected Object squeezeBangEmptyString(RubyString string, Object[] args) {
            return nil;
        }

        @TruffleBoundary
        @Specialization(guards = { "!isEmpty(string.rope)", "noArguments(args)" })
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
                    string.setRope(RopeOperations.ropeFromRopeBuilder(buffer));
                }
            } else {
                if (!StringSupport
                        .multiByteSqueeze(
                                buffer,
                                rope.getCodeRange(),
                                squeeze,
                                null,
                                string.rope.getEncoding(),
                                false,
                                this)) {
                    return nil;
                } else {
                    string.setRope(RopeOperations.ropeFromRopeBuilder(buffer));
                }
            }

            return string;
        }

        @Specialization(guards = { "!isEmpty(string.rope)", "!noArguments(args)" })
        protected Object squeezeBang(VirtualFrame frame, RubyString string, Object[] args,
                @Cached ToStrNode toStrNode) {
            // Taken from org.jruby.RubyString#squeeze_bang19.

            final Object[] otherStrings = new Object[args.length];

            for (int i = 0; i < args.length; i++) {
                otherStrings[i] = toStrNode.execute(args[i]);
            }

            return performSqueezeBang(string, otherStrings);
        }

        @TruffleBoundary
        private Object performSqueezeBang(RubyString string, Object[] otherStrings) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            final Rope rope = string.rope;
            final RopeBuilder buffer = RopeOperations.toRopeBuilderCopy(rope);

            Object otherStr = otherStrings[0];
            Rope otherRope = RubyStringLibrary.getUncached().getRope(otherStr);
            RubyEncoding enc = checkEncodingNode.executeCheckEncoding(string, otherStr);
            final boolean squeeze[] = new boolean[StringSupport.TRANS_SIZE + 1];

            boolean singlebyte = rope.isSingleByteOptimizable() && otherRope.isSingleByteOptimizable();

            if (singlebyte && otherRope.byteLength() == 1 && otherStrings.length == 1) {
                squeeze[otherRope.getRawBytes()[0]] = true;
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    string.setRope(RopeOperations.ropeFromRopeBuilder(buffer));
                    return string;
                }
            }

            StringSupport.TrTables tables = StringSupport
                    .trSetupTable(otherRope, squeeze, null, true, enc.jcoding, this);

            for (int i = 1; i < otherStrings.length; i++) {
                otherStr = otherStrings[i];
                otherRope = RubyStringLibrary.getUncached().getRope(otherStr);
                enc = checkEncodingNode.executeCheckEncoding(string, otherStr);
                singlebyte = singlebyte && otherRope.isSingleByteOptimizable();
                tables = StringSupport.trSetupTable(otherRope, squeeze, tables, false, enc.jcoding, this);
            }

            if (singleByteOptimizableProfile.profile(singlebyte)) {
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    string.setRope(RopeOperations.ropeFromRopeBuilder(buffer));
                }
            } else {
                if (!StringSupport
                        .multiByteSqueeze(buffer, rope.getCodeRange(), squeeze, tables, enc.jcoding, true, this)) {
                    return nil;
                } else {
                    string.setRope(RopeOperations.ropeFromRopeBuilder(buffer));
                }
            }

            return string;
        }

    }

    @CoreMethod(names = "succ!", raiseIfFrozenSelf = true)
    public abstract static class SuccBangNode extends CoreMethodArrayArgumentsNode {

        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();

        @Specialization
        protected RubyString succBang(RubyString string) {
            final Rope rope = string.rope;

            if (!rope.isEmpty()) {
                final RopeBuilder succBuilder = StringSupport.succCommon(rope, this);

                final Rope newRope = makeLeafRopeNode.executeMake(
                        succBuilder.getBytes(),
                        rope.getEncoding(),
                        CodeRange.CR_UNKNOWN,
                        NotProvided.INSTANCE);
                string.setRope(newRope);
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

        public abstract Object executeSum(Object string, Object bits);

        @Child private DispatchNode addNode = DispatchNode.create();
        private final BytesNode bytesNode = BytesNode.create();

        @Specialization
        protected Object sum(Object string, long bits,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            // Copied from JRuby

            final Rope rope = strings.getRope(string);
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
        protected Object sum(Object string, NotProvided bits,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return sum(string, 16, strings);
        }

        @Specialization(guards = { "!isImplicitLong(bits)", "wasProvided(bits)" })
        protected Object sum(Object string, Object bits,
                @Cached ToLongNode toLongNode,
                @Cached SumNode sumNode) {
            return sumNode.executeSum(string, toLongNode.execute(bits));
        }

    }

    @CoreMethod(names = "to_f")
    public abstract static class ToFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        protected double toF(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            try {
                return convertToDouble(strings.getRope(string));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @TruffleBoundary
        private double convertToDouble(Rope rope) {
            return new DoubleConverter().parse(rope, false, true);
        }
    }

    @CoreMethod(names = { "to_s", "to_str" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected ImmutableRubyString toS(ImmutableRubyString string) {
            return string;
        }

        @Specialization(guards = "!isStringSubclass(string)")
        protected RubyString toS(RubyString string) {
            return string;
        }

        @Specialization(guards = "isStringSubclass(string)")
        protected RubyString toSOnSubclass(RubyString string) {
            final Shape shape = getLanguage().stringShape;
            final RubyString result = new RubyString(
                    coreLibrary().stringClass,
                    shape,
                    false,
                    string.rope,
                    string.encoding);
            AllocationTracing.trace(result, this);
            return result;
        }

        public boolean isStringSubclass(RubyString string) {
            return string.getLogicalClass() != coreLibrary().stringClass;
        }

    }

    @CoreMethod(names = { "to_sym", "intern" })
    @ImportStatic({ StringCachingGuards.class, StringGuards.class, StringOperations.class })
    public abstract static class ToSymNode extends CoreMethodArrayArgumentsNode {

        @Child CodeRangeNode codeRangeNode = CodeRangeNode.create();

        @Specialization(
                guards = {
                        "!isBrokenCodeRange(strings.getRope(string), codeRangeNode)",
                        "equalNode.execute(strings.getRope(string),cachedRope)",
                        "strings.getEncoding(string) == cachedEncoding" },
                limit = "getDefaultCacheLimit()")
        protected RubySymbol toSymCached(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached("strings.getRope(string)") Rope cachedRope,
                @Cached("strings.getEncoding(string)") RubyEncoding cachedEncoding,
                @Cached("getSymbol(cachedRope, cachedEncoding)") RubySymbol cachedSymbol,
                @Cached RopeNodes.EqualNode equalNode) {
            return cachedSymbol;
        }

        @Specialization(guards = "!isBrokenCodeRange(strings.getRope(string), codeRangeNode)", replaces = "toSymCached")
        protected RubySymbol toSym(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return getSymbol(strings.getRope(string), strings.getEncoding(string));
        }

        @Specialization(guards = "isBrokenCodeRange(strings.getRope(string), codeRangeNode)")
        protected RubySymbol toSymBroken(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            throw new RaiseException(getContext(), coreExceptions().encodingError("invalid encoding symbol", this));
        }
    }

    @CoreMethod(names = "reverse!", raiseIfFrozenSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class ReverseBangNode extends CoreMethodArrayArgumentsNode {

        @Child CharacterLengthNode characterLengthNode = CharacterLengthNode.create();
        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();

        @Specialization(guards = "reverseIsEqualToSelf(string, characterLengthNode)")
        protected RubyString reverseNoOp(RubyString string) {
            return string;
        }

        @Specialization(
                guards = {
                        "!reverseIsEqualToSelf(string, characterLengthNode)",
                        "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected RubyString reverseSingleByteOptimizable(RubyString string,
                @Cached BytesNode bytesNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            final Rope rope = string.rope;
            final byte[] originalBytes = bytesNode.execute(rope);
            final int len = originalBytes.length;
            final byte[] reversedBytes = new byte[len];

            for (int i = 0; i < len; i++) {
                reversedBytes[len - i - 1] = originalBytes[i];
            }

            string.setRope(
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
                @Cached BytesNode bytesNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
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

            string.setRope(
                    makeLeafRopeNode.executeMake(
                            reversedBytes,
                            rope.getEncoding(),
                            codeRangeNode.execute(rope),
                            characterLengthNode.execute(rope)));

            return string;
        }

        public static boolean reverseIsEqualToSelf(RubyString string,
                CharacterLengthNode characterLengthNode) {
            return characterLengthNode.execute(string.rope) <= 1;
        }
    }

    @CoreMethod(names = "tr!", required = 2, raiseIfFrozenSelf = true)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "fromStr", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "toStr", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(StringGuards.class)
    public abstract static class TrBangNode extends CoreMethodNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child private DeleteBangNode deleteBangNode;

        @CreateCast("fromStr")
        protected ToStrNode coerceFromStrToString(RubyBaseNodeWithExecute fromStr) {
            return ToStrNodeGen.create(fromStr);
        }

        @CreateCast("toStr")
        protected ToStrNode coerceToStrToString(RubyBaseNodeWithExecute toStr) {
            return ToStrNodeGen.create(toStr);
        }

        @Specialization(guards = "isEmpty(self.rope)")
        protected Object trBangSelfEmpty(RubyString self, Object fromStr, Object toStr) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmpty(self.rope)",
                        "isEmpty(libToStr.getRope(toStr))" })
        protected Object trBangToEmpty(RubyString self, Object fromStr, Object toStr,
                @CachedLibrary(limit = "2") RubyStringLibrary libToStr) {
            if (deleteBangNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                deleteBangNode = insert(DeleteBangNode.create());
            }

            return deleteBangNode.executeDeleteBang(self, new Object[]{ fromStr });
        }

        @Specialization(
                guards = {
                        "libFromStr.isRubyString(fromStr)",
                        "!isEmpty(self.rope)",
                        "!isEmpty(libToStr.getRope(toStr))" })
        protected Object trBangNoEmpty(RubyString self, Object fromStr, Object toStr,
                @CachedLibrary(limit = "2") RubyStringLibrary libFromStr,
                @CachedLibrary(limit = "2") RubyStringLibrary libToStr) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            return StringNodesHelper.trTransHelper(
                    checkEncodingNode,
                    self,
                    self.rope,
                    fromStr,
                    libFromStr.getRope(fromStr),
                    toStr,
                    libToStr.getRope(toStr),
                    false,
                    this);
        }
    }

    @CoreMethod(names = "tr_s!", required = 2, raiseIfFrozenSelf = true)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "fromStr", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "toStrNode", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(StringGuards.class)
    public abstract static class TrSBangNode extends CoreMethodNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child private DeleteBangNode deleteBangNode;

        @CreateCast("fromStr")
        protected ToStrNode coerceFromStrToString(RubyBaseNodeWithExecute fromStr) {
            return ToStrNodeGen.create(fromStr);
        }

        @CreateCast("toStrNode")
        protected ToStrNode coerceToStrToString(RubyBaseNodeWithExecute toStr) {
            return ToStrNodeGen.create(toStr);
        }

        @Specialization(
                guards = { "isEmpty(self.rope)" })
        protected Object trSBangEmpty(RubyString self, Object fromStr, Object toStr) {
            return nil;
        }

        @Specialization(
                guards = { "libFromStr.isRubyString(fromStr)", "libToStr.isRubyString(toStr)", "!isEmpty(self.rope)" })
        protected Object trSBang(RubyString self, Object fromStr, Object toStr,
                @CachedLibrary(limit = "2") RubyStringLibrary libFromStr,
                @CachedLibrary(limit = "2") RubyStringLibrary libToStr) {
            if (libToStr.getRope(toStr).isEmpty()) {
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

            return StringNodesHelper.trTransHelper(
                    checkEncodingNode,
                    self,
                    self.rope,
                    fromStr,
                    libFromStr.getRope(fromStr),
                    toStr,
                    libToStr.getRope(toStr),
                    true,
                    this);
        }
    }

    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "format", type = RubyBaseNodeWithExecute.class)
    @CoreMethod(names = "unpack", required = 1)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class UnpackNode extends CoreMethodNode {

        private final BranchProfile exceptionProfile = BranchProfile.create();

        @CreateCast("format")
        protected ToStrNode coerceFormat(RubyBaseNodeWithExecute format) {
            return ToStrNodeGen.create(format);
        }

        @Specialization(guards = { "equalNode.execute(libFormat.getRope(format), cachedFormat)" })
        protected RubyArray unpackCached(Object string, Object format,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libFormat,
                @Cached("libFormat.getRope(format)") Rope cachedFormat,
                @Cached("create(compileFormat(libFormat.getRope(format)))") DirectCallNode callUnpackNode,
                @Cached BytesNode bytesNode,
                @Cached RopeNodes.EqualNode equalNode,
                @Cached StringGetAssociatedNode stringGetAssociatedNode) {
            final Rope rope = libString.getRope(string);

            final ArrayResult result;

            try {
                result = (ArrayResult) callUnpackNode.call(
                        new Object[]{
                                bytesNode.execute(rope),
                                rope.byteLength(),
                                stringGetAssociatedNode.execute(string) }); // TODO impl associated for ImmutableRubyString
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishUnpack(result);
        }

        @Specialization(
                guards = "libFormat.isRubyString(format)",
                replaces = "unpackCached")
        protected RubyArray unpackUncached(Object string, Object format,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libFormat,
                @Cached IndirectCallNode callUnpackNode,
                @Cached BytesNode bytesNode,
                @Cached StringGetAssociatedNode stringGetAssociatedNode) {
            final Rope rope = libString.getRope(string);

            final ArrayResult result;

            try {
                result = (ArrayResult) callUnpackNode.call(
                        compileFormat(libFormat.getRope(format)),
                        new Object[]{
                                bytesNode.execute(rope),
                                rope.byteLength(),
                                stringGetAssociatedNode.execute(string) });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishUnpack(result);
        }

        private RubyArray finishUnpack(ArrayResult result) {
            return createArray(result.getOutput(), result.getOutputLength());
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(Rope rope) {
            try {
                return new UnpackCompiler(getLanguage(), this).compile(RopeOperations.decodeRope(rope));
            } catch (DeferredRaiseException dre) {
                throw dre.getException(getContext());
            }
        }

        protected int getCacheLimit() {
            return getLanguage().options.UNPACK_CACHE;
        }

    }

    public abstract static class InvertAsciiCaseBytesNode extends RubyBaseNode {

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
                @Cached BranchProfile foundUpperCaseCharProfile,
                @Cached LoopConditionProfile loopProfile) {
            byte[] modified = null;

            int i = start;
            try {
                for (; loopProfile.inject(i < bytes.length); i++) {
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

                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, i - start);
            }

            return modified;
        }

    }

    public abstract static class InvertAsciiCaseNode extends RubyBaseNode {

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
                @Cached BytesNode bytesNode,
                @Cached CharacterLengthNode characterLengthNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached MakeLeafRopeNode makeLeafRopeNode,
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
                string.setRope(newRope);

                return string;
            }
        }

    }

    @Primitive(name = "string_upcase!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringUpcaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createLowerToUpper()") InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("bytesNode") BytesNode bytesNode,
                @Cached CharacterLengthNode characterLengthNode,
                @Cached @Shared("codeRangeNode") CodeRangeNode codeRangeNode,
                @Cached @Shared("makeLeafRopeNode") MakeLeafRopeNode makeLeafRopeNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
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
                string.setRope(
                        makeLeafRopeNode.executeMake(outputBytes, encoding, cr, characterLengthNode.execute(rope)));
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = { "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("bytesNode") BytesNode bytesNode,
                @Cached @Shared("codeRangeNode") CodeRangeNode codeRangeNode,
                @Cached @Shared("makeLeafRopeNode") MakeLeafRopeNode makeLeafRopeNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            final Rope rope = string.rope;
            final Encoding encoding = rope.getEncoding();

            if (dummyEncodingProfile.profile(encoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding, this));
            }

            final RopeBuilder builder = RopeBuilder.createRopeBuilder(bytesNode.execute(rope), rope.getEncoding());
            final boolean modified = StringSupport
                    .upcaseMultiByteComplex(encoding, codeRangeNode.execute(rope), builder, caseMappingOptions, this);
            if (modifiedProfile.profile(modified)) {
                string.setRope(
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
        protected boolean validEncoding(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @Cached CodeRangeNode codeRangeNode) {
            final CodeRange codeRange = codeRangeNode.execute(libString.getRope(string));

            return codeRange != CR_BROKEN;
        }

    }

    @Primitive(name = "string_capitalize!", raiseIfFrozen = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringCapitalizeBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private BytesNode bytesNode = BytesNode.create();
        @Child private CodeRangeNode codeRangeNode = CodeRangeNode.create();
        @Child private CharacterLengthNode characterLengthNode = CharacterLengthNode.create();
        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();

        @Specialization(guards = "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createUpperToLower()") InvertAsciiCaseBytesNode invertAsciiCaseNode,
                @Cached @Shared("emptyStringProfile") ConditionProfile emptyStringProfile,
                @Cached @Exclusive ConditionProfile firstCharIsLowerProfile,
                @Cached @Exclusive ConditionProfile otherCharsAlreadyLowerProfile,
                @Cached @Exclusive ConditionProfile mustCapitalizeFirstCharProfile) {
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

            string.setRope(
                    makeLeafRopeNode.executeMake(
                            finalBytes,
                            rope.getEncoding(),
                            codeRangeNode.execute(rope),
                            characterLengthNode.execute(rope)));

            return string;
        }

        @Specialization(guards = "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("dummyEncodingProfile") BranchProfile dummyEncodingProfile,
                @Cached @Shared("emptyStringProfile") ConditionProfile emptyStringProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
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
                string.setRope(
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
                @Cached @Shared("dummyEncodingProfile") BranchProfile dummyEncodingProfile,
                @Cached @Shared("emptyStringProfile") ConditionProfile emptyStringProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
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
                    .capitalizeMultiByteComplex(enc, codeRangeNode.execute(rope), builder, caseMappingOptions, this);
            if (modifiedProfile.profile(modified)) {
                string.setRope(
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

        @Child private SubstringNode substringNode = SubstringNode.create();

        @Specialization
        protected RubyString clear(RubyString string) {
            string.setRope(substringNode.executeSubstring(string.rope, 0, 0));
            return string;
        }
    }

    public static class StringNodesHelper {

        @TruffleBoundary
        private static Object trTransHelper(CheckEncodingNode checkEncodingNode, RubyString self, Rope selfRope,
                Object fromStr, Rope fromStrRope,
                Object toStr, Rope toStrRope, boolean sFlag, Node node) {
            final RubyEncoding e1 = checkEncodingNode.executeCheckEncoding(self, fromStr);
            final RubyEncoding e2 = checkEncodingNode.executeCheckEncoding(self, toStr);
            final RubyEncoding enc = e1 == e2 ? e1 : checkEncodingNode.executeCheckEncoding(fromStr, toStr);

            final Rope ret = StringSupport
                    .trTransHelper(selfRope, fromStrRope, toStrRope, e1.jcoding, enc.jcoding, sFlag, node);
            if (ret == null) {
                return Nil.INSTANCE;
            }

            self.setRope(ret, enc);
            return self;
        }
    }

    @Primitive(name = "character_printable_p")
    public abstract static class CharacterPrintablePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isCharacterPrintable(Object character,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached ConditionProfile is7BitProfile,
                @Cached AsciiOnlyNode asciiOnlyNode,
                @Cached GetCodePointNode getCodePointNode) {
            final Rope rope = strings.getRope(character);
            final RubyEncoding encoding = strings.getEncoding(character);
            final int codePoint = getCodePointNode.executeGetCodePoint(encoding, rope, 0);

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
    public abstract static class StringAppendPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private StringAppendNode stringAppendNode = StringAppendNode.create();

        public static StringAppendPrimitiveNode create() {
            return StringAppendPrimitiveNodeFactory.create(null);
        }

        public abstract RubyString executeStringAppend(RubyString string, Object other);

        @Specialization
        protected RubyString stringAppend(RubyString string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary otherStringLibrary) {
            final Pair<Rope, RubyEncoding> result = stringAppendNode.executeStringAppend(string, other);
            string.setRope(result.getLeft(), result.getRight());
            return string;
        }

    }

    @Primitive(name = "string_awk_split", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public abstract static class StringAwkSplitPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private BytesNode bytesNode = BytesNode.create();
        @Child private CallBlockNode yieldNode = CallBlockNode.create();
        @Child CodeRangeNode codeRangeNode = CodeRangeNode.create();
        @Child private GetCodePointNode getCodePointNode = GetCodePointNode.create();
        @Child private StringSubstringNode substringNode = StringSubstringNode.create();

        private static final int SUBSTRING_CREATED = -1;

        @Specialization(guards = "is7Bit(strings.getRope(string), codeRangeNode)")
        protected Object stringAwkSplitSingleByte(Object string, int limit, Object block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached ConditionProfile executeBlockProfile,
                @Cached ConditionProfile growArrayProfile,
                @Cached ConditionProfile trailingSubstringProfile,
                @Cached ConditionProfile trailingEmptyStringProfile) {
            Object[] ret = new Object[10];
            int storeIndex = 0;

            final Rope rope = strings.getRope(string);
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
                                block,
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
                ret = addSubstring(ret, storeIndex++, substring, block, executeBlockProfile, growArrayProfile);
            }

            if (trailingEmptyStringProfile.profile(limit < 0 && StringSupport.isAsciiSpace(bytes[bytes.length - 1]))) {
                final RubyString substring = substringNode.executeSubstring(string, bytes.length - 1, 0);
                ret = addSubstring(ret, storeIndex++, substring, block, executeBlockProfile, growArrayProfile);
            }

            if (block == nil) {
                return createArray(ret, storeIndex);
            } else {
                return string;
            }
        }

        @TruffleBoundary
        @Specialization(guards = "!is7Bit(strings.getRope(string), codeRangeNode)")
        protected Object stringAwkSplit(Object string, int limit, Object block,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached ConditionProfile executeBlockProfile,
                @Cached ConditionProfile growArrayProfile,
                @Cached ConditionProfile trailingSubstringProfile) {
            Object[] ret = new Object[10];
            int storeIndex = 0;

            final Rope rope = strings.getRope(string);
            final RubyEncoding rubyEncoding = strings.getEncoding(string);
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
                final int c = getCodePointNode.executeGetCodePoint(rubyEncoding, rope, p);
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
                                block,
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
                ret = addSubstring(ret, storeIndex++, substring, block, executeBlockProfile, growArrayProfile);
            }

            if (block == nil) {
                return createArray(ret, storeIndex);
            } else {
                return string;
            }
        }

        private Object[] addSubstring(Object[] store, int index, RubyString substring,
                Object block, ConditionProfile executeBlockProfile, ConditionProfile growArrayProfile) {
            if (executeBlockProfile.profile(block != nil)) {
                yieldNode.yield((RubyProc) block, substring);
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

    }

    @Primitive(name = "string_byte_substring", lowerFixnum = { 1, 2 })
    public abstract static class StringByteSubstringPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private NormalizeIndexNode normalizeIndexNode = NormalizeIndexNode.create();
        @Child private StringSubstringNode substringNode = StringSubstringNode.create();

        public static StringByteSubstringPrimitiveNode create() {
            return StringByteSubstringPrimitiveNodeFactory.create(null);
        }

        public abstract Object executeStringByteSubstring(Object string, Object index, Object length);

        @Specialization
        protected Object stringByteSubstring(Object string, int index, NotProvided length,
                @Cached ConditionProfile negativeLengthProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile lengthTooLongProfile,
                @Cached ConditionProfile nilSubstringProfile,
                @Cached ConditionProfile emptySubstringProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final Object subString = stringByteSubstring(
                    string,
                    index,
                    1,
                    negativeLengthProfile,
                    indexOutOfBoundsProfile,
                    lengthTooLongProfile,
                    libString);

            if (nilSubstringProfile.profile(subString == nil)) {
                return subString;
            }

            if (emptySubstringProfile.profile(((RubyString) subString).rope.isEmpty())) {
                return nil;
            }

            return subString;
        }

        @Specialization
        protected Object stringByteSubstring(Object string, int index, int length,
                @Cached ConditionProfile negativeLengthProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile lengthTooLongProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            if (negativeLengthProfile.profile(length < 0)) {
                return nil;
            }

            final Rope rope = libString.getRope(string);
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
    public abstract static class StringChrAtPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = { "indexOutOfBounds(strings.getRope(string), byteIndex)" })
        protected Object stringChrAtOutOfBounds(Object string, int byteIndex,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!indexOutOfBounds(strings.getRope(string), byteIndex)",
                        "isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)" })
        protected Object stringChrAtSingleByte(Object string, int byteIndex,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached StringByteSubstringPrimitiveNode stringByteSubstringNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            return stringByteSubstringNode.executeStringByteSubstring(string, byteIndex, 1);
        }

        @Specialization(
                guards = {
                        "!indexOutOfBounds(strings.getRope(string), byteIndex)",
                        "!isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)" })
        protected Object stringChrAt(Object string, int byteIndex,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached BytesNode bytesNode,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached MakeStringNode makeStringNode) {
            final Rope rope = strings.getRope(string);
            final RubyEncoding encoding = getActualEncodingNode.execute(rope, strings.getEncoding(string));
            final int end = rope.byteLength();
            final byte[] bytes = bytesNode.execute(rope);
            final int c = calculateCharacterLengthNode.characterLength(
                    encoding.jcoding,
                    codeRangeNode.execute(rope),
                    Bytes.fromRange(bytes, byteIndex, end));

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

        protected static boolean indexOutOfBounds(Rope rope, int byteIndex) {
            return ((byteIndex < 0) || (byteIndex >= rope.byteLength()));
        }

    }

    @ImportStatic(StringGuards.class)
    public abstract static class StringAreComparableNode extends RubyBaseNode {

        @Child AreComparableRopesNode areComparableRopesNode = AreComparableRopesNode.create();

        public abstract boolean executeAreComparable(Object first, Object second);

        @Specialization
        protected boolean areComparable(Object a, Object b,
                @CachedLibrary(limit = "2") RubyStringLibrary libA,
                @CachedLibrary(limit = "2") RubyStringLibrary libB) {
            return areComparableRopesNode.execute(libA.getRope(a), libB.getRope(b));
        }
    }

    @ImportStatic({ StringGuards.class, StringOperations.class })
    public abstract static class StringEqualNode extends RubyBaseNode {

        @Child private StringAreComparableNode areComparableNode;

        public abstract boolean executeStringEqual(Object string, Object other);

        // Same Rope implies same Encoding and therefore comparable
        @Specialization(guards = "libString.getRope(string) == libOther.getRope(other)")
        protected boolean sameRope(Object string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther) {
            return true;
        }

        @Specialization(guards = "!areComparable(string, other)")
        protected boolean notComparable(Object string, Object other) {
            return false;
        }

        @Specialization(
                guards = "areComparable(string, other)")
        protected boolean stringEquals(Object string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther,
                @Cached RopeNodes.BytesEqualNode bytesEqualNode) {
            return bytesEqualNode.execute(libString.getRope(string), libOther.getRope(other));
        }

        protected boolean areComparable(Object first, Object second) {
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
        protected RubyString string_escape(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final Rope rope = rbStrEscape(strings.getRope(string));
            return makeStringNode.fromRope(rope, Encodings.US_ASCII);
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
    public abstract static class StringFindCharacterNode extends CoreMethodArrayArgumentsNode {

        @Child private StringSubstringNode substringNode = StringSubstringNode.create();

        @Specialization(guards = "offset < 0")
        protected Object stringFindCharacterNegativeOffset(Object string, int offset) {
            return nil;
        }

        @Specialization(guards = "offsetTooLarge(strings.getRope(string), offset)")
        protected Object stringFindCharacterOffsetTooLarge(Object string, int offset,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return nil;
        }

        @Specialization(
                guards = {
                        "offset >= 0",
                        "!offsetTooLarge(strings.getRope(string), offset)",
                        "isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)" })
        protected Object stringFindCharacterSingleByte(Object string, int offset,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            // Taken from Rubinius's String::find_character.

            return substringNode.executeSubstring(string, offset, 1);
        }

        @Specialization(
                guards = {
                        "offset >= 0",
                        "!offsetTooLarge(strings.getRope(string), offset)",
                        "!isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)" })
        protected Object stringFindCharacter(Object string, int offset,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached GetBytesObjectNode getBytesObject,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            // Taken from Rubinius's String::find_character.

            final Rope rope = strings.getRope(string);
            final Encoding enc = rope.getEncoding();
            final CodeRange cr = codeRangeNode.execute(rope);

            final int clen = calculateCharacterLengthNode
                    .characterLength(enc, cr, getBytesObject.getClamped(rope, offset, enc.maxLength()));

            return substringNode.executeSubstring(string, offset, clen);
        }

        protected static boolean offsetTooLarge(Rope rope, int offset) {
            return offset >= rope.byteLength();
        }

    }

    @NonStandard
    @CoreMethod(names = "from_codepoint", onSingleton = true, required = 2, lowerFixnum = 1)
    public abstract static class StringFromCodepointPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization(guards = { "isSimple(code, rubyEncoding)", "isCodepoint(code)" })
        protected RubyString stringFromCodepointSimple(long code, RubyEncoding rubyEncoding,
                @Cached ConditionProfile isUTF8Profile,
                @Cached ConditionProfile isUSAsciiProfile,
                @Cached ConditionProfile isAscii8BitProfile) {
            final int intCode = (int) code; // isSimple() guarantees this is OK
            final Encoding encoding = rubyEncoding.jcoding;
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

            return makeStringNode.fromRope(rope, rubyEncoding);
        }

        @Specialization(guards = { "!isSimple(code, rubyEncoding)", "isCodepoint(code)" })
        protected RubyString stringFromCodepoint(long code, RubyEncoding rubyEncoding,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached BranchProfile errorProfile) {
            final Encoding encoding = rubyEncoding.jcoding;

            final int length = StringSupport.codeLength(encoding, (int) code);
            if (length <= 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            final byte[] bytes = new byte[length];
            final int codeToMbc = StringSupport.codeToMbc(encoding, (int) code, bytes, 0);
            if (codeToMbc < 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            final Bytes bytesObject = new Bytes(bytes, 0, length);
            if (calculateCharacterLengthNode.characterLength(encoding, CR_UNKNOWN, bytesObject) != length) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, rubyEncoding, this));
            }

            return makeStringNode.executeMake(bytes, rubyEncoding, CodeRange.CR_VALID);
        }

        protected boolean isCodepoint(long code) {
            // Fits in an unsigned int
            return code >= 0 && code < (1L << 32);
        }

        protected boolean isSimple(long code, RubyEncoding encoding) {
            final Encoding enc = encoding.jcoding;

            return (enc.isAsciiCompatible() && code >= 0x00 && code < 0x80) ||
                    (enc == ASCIIEncoding.INSTANCE && code >= 0x00 && code <= 0xFF);
        }

    }

    @Primitive(name = "string_to_f")
    public abstract static class StringToFPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object stringToF(Object string,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode,
                @Cached BytesNode bytesNode) {
            final Rope rope = strings.getRope(string);
            if (rope.isEmpty()) {
                return nil;
            }

            final String javaString = strings.getJavaString(string);
            if (javaString.startsWith("0x")) {
                try {
                    return Double.parseDouble(javaString);
                } catch (NumberFormatException e) {
                    // Try falling back to this implementation if the first fails, neither 100% complete
                    final Object result = ConvertBytes
                            .bytesToInum(
                                    getContext(),
                                    this,
                                    fixnumOrBignumNode,
                                    bytesNode,
                                    rope,
                                    16,
                                    true);
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
    public abstract static class StringIndexPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child CodeRangeNode codeRangeNode = CodeRangeNode.create();
        @Child SingleByteOptimizableNode singleByteNode = SingleByteOptimizableNode.create();

        @Specialization(
                guards = "isEmpty(stringsPattern.getRope(pattern))")
        protected int stringIndexEmptyPattern(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsPattern) {
            assert byteOffset >= 0;
            return byteOffset;
        }

        @Specialization(
                guards = {
                        "isSingleByteString(libPattern.getRope(pattern))",
                        "!isBrokenCodeRange(libPattern.getRope(pattern), codeRangeNode)",
                        "canMemcmp(libString.getRope(string), libPattern.getRope(pattern), singleByteNode)" })
        protected Object stringIndexSingleBytePattern(Object string, Object pattern, int byteOffset,
                @Cached BytesNode bytesNode,
                @Cached ConditionProfile offsetTooLargeProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = libString.getRope(string);
            final int end = sourceRope.byteLength();

            if (offsetTooLargeProfile.profile(byteOffset >= end)) {
                return nil;
            }

            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final byte searchByte = bytesNode.execute(libPattern.getRope(pattern))[0];

            final int index = com.oracle.truffle.api.ArrayUtils.indexOf(sourceBytes, byteOffset, end, searchByte);

            return index == -1 ? nil : index;
        }

        @Specialization(
                guards = {
                        "!isEmpty(libPattern.getRope(pattern))",
                        "!isSingleByteString(libPattern.getRope(pattern))",
                        "!isBrokenCodeRange(libPattern.getRope(pattern), codeRangeNode)",
                        "canMemcmp(libString.getRope(string), libPattern.getRope(pattern), singleByteNode)" })
        protected Object stringIndexMultiBytePattern(Object string, Object pattern, int byteOffset,
                @Cached BytesNode bytesNode,
                @Cached BranchProfile matchFoundProfile,
                @Cached BranchProfile noMatchProfile,
                @Cached LoopConditionProfile loopProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = libString.getRope(string);
            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final Rope searchRope = libPattern.getRope(pattern);
            final byte[] searchBytes = bytesNode.execute(searchRope);

            int end = sourceRope.byteLength() - searchRope.byteLength();

            int i = byteOffset;
            try {
                for (; loopProfile.inject(i <= end); i++) {
                    if (sourceBytes[i] == searchBytes[0]) {
                        if (ArrayUtils.regionEquals(sourceBytes, i, searchBytes, 0, searchRope.byteLength())) {
                            matchFoundProfile.enter();
                            return i;
                        }
                    }
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, i - byteOffset);
            }

            noMatchProfile.enter();
            return nil;
        }

        @Specialization(
                guards = {
                        "isBrokenCodeRange(stringsPattern.getRope(pattern), codeRangeNode)" })
        protected Object stringIndexBrokenPattern(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsPattern) {
            assert byteOffset >= 0;
            return nil;
        }

        @Specialization(
                guards = {
                        "!isBrokenCodeRange(libPattern.getRope(pattern), codeRangeNode)",
                        "!canMemcmp(libString.getRope(string), libPattern.getRope(pattern), singleByteNode)" })
        protected Object stringIndexGeneric(Object string, Object pattern, int byteOffset,
                @Cached ByteIndexFromCharIndexNode byteIndexFromCharIndexNode,
                @Cached StringByteCharacterIndexNode byteIndexToCharIndexNode,
                @Cached NormalizeIndexNode normalizeIndexNode,
                @Cached ConditionProfile badIndexProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            // Rubinius will pass in a byte index for the `start` value, but StringSupport.index requires a character index.
            final int charIndex = byteIndexToCharIndexNode.executeStringByteCharacterIndex(string, byteOffset);

            final Rope stringRope = libString.getRope(string);
            final int index = index(
                    stringRope,
                    libPattern.getRope(pattern),
                    charIndex,
                    stringRope.getEncoding(),
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

        private void checkEncoding(Object string, Object pattern) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            checkEncodingNode.executeCheckEncoding(string, pattern);
        }

    }

    @Primitive(name = "string_byte_character_index", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public abstract static class StringByteCharacterIndexNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();

        public abstract int executeStringByteCharacterIndex(Object string, int byteIndex);

        public static StringByteCharacterIndexNode create() {
            return StringByteCharacterIndexNodeFactory.create(null);
        }

        @Specialization(
                guards = {
                        "isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)" })
        protected int singleByte(Object string, int byteIndex,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            return byteIndex;
        }

        @Specialization(
                guards = {
                        "!isSingleByteOptimizable(libString.getRope(string), singleByteOptimizableNode)",
                        "isFixedWidthEncoding(libString.getRope(string))" })
        protected int fixedWidth(Object string, int byteIndex,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            return byteIndex / libString.getRope(string).getEncoding().minLength();
        }

        @Specialization(
                guards = {
                        "!isSingleByteOptimizable(libString.getRope(string), singleByteOptimizableNode)",
                        "!isFixedWidthEncoding(libString.getRope(string))",
                        "isValidUtf8(libString.getRope(string), codeRangeNode)" })
        protected int validUtf8(Object string, int byteIndex,
                @Cached CodeRangeNode codeRangeNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // Taken from Rubinius's String::find_byte_character_index.
            // TODO (nirvdrum 02-Apr-15) There's a way to optimize this for UTF-8, but porting all that code isn't necessary at the moment.
            return notValidUtf8(string, byteIndex, codeRangeNode, libString);
        }

        @TruffleBoundary
        @Specialization(
                guards = {
                        "!isSingleByteOptimizable(libString.getRope(string), singleByteOptimizableNode)",
                        "!isFixedWidthEncoding(libString.getRope(string))",
                        "!isValidUtf8(libString.getRope(string), codeRangeNode)" })
        protected int notValidUtf8(Object string, int byteIndex,
                @Cached CodeRangeNode codeRangeNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // Taken from Rubinius's String::find_byte_character_index and Encoding::find_byte_character_index.

            final Rope rope = libString.getRope(string);
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

    /** Search pattern in string starting after offset characters, and return a character index or nil */
    @Primitive(name = "string_character_index", lowerFixnum = 2)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "pattern", type = RubyNode.class)
    @NodeChild(value = "offset", type = RubyNode.class)
    public abstract static class StringCharacterIndexNode extends PrimitiveNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @CreateCast("string")
        protected RubyNode coerceStringToRope(RubyNode string) {
            return ToRopeNodeGen.create(string);
        }

        @CreateCast("pattern")
        protected RubyNode coercePatternToRope(RubyNode pattern) {
            return ToRopeNodeGen.create(pattern);
        }

        @Specialization(
                guards = "singleByteOptimizableNode.execute(stringRope)")
        protected Object singleByteOptimizable(Rope stringRope, Rope patternRope, int offset,
                @Cached @Shared("stringBytesNode") BytesNode stringBytesNode,
                @Cached @Shared("patternBytesNode") BytesNode patternBytesNode,
                @Cached LoopConditionProfile loopProfile) {

            assert offset >= 0;
            assert offset + patternRope.byteLength() <= stringRope
                    .byteLength() : "already checked in the caller, String#index";

            int p = offset;
            final int e = stringRope.byteLength();
            final int pe = patternRope.byteLength();
            final int l = e - pe + 1;

            final byte[] stringBytes = stringBytesNode.execute(stringRope);
            final byte[] patternBytes = patternBytesNode.execute(patternRope);

            try {
                for (; loopProfile.inject(p < l); p++) {
                    if (ArrayUtils.regionEquals(stringBytes, p, patternBytes, 0, pe)) {
                        return p;
                    }
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, p - offset);
            }

            return nil;
        }

        @TruffleBoundary
        @Specialization(
                guards = "!singleByteOptimizableNode.execute(stringRope)")
        protected Object multiByte(Rope stringRope, Rope patternRope, int offset,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached @Shared("stringBytesNode") BytesNode stringBytesNode,
                @Cached @Shared("patternBytesNode") BytesNode patternBytesNode) {

            assert offset >= 0;
            assert offset + patternRope.byteLength() <= stringRope
                    .byteLength() : "already checked in the caller, String#index";

            int p = 0;
            final int e = stringRope.byteLength();
            final int pe = patternRope.byteLength();
            final int l = e - pe + 1;

            final byte[] stringBytes = stringBytesNode.execute(stringRope);
            final byte[] patternBytes = patternBytesNode.execute(patternRope);

            final Encoding enc = stringRope.getEncoding();
            final CodeRange cr = stringRope.getCodeRange();
            int c = 0;
            int index = 0;

            while (p < e && index < offset) {
                c = calculateCharacterLengthNode.characterLength(enc, cr, Bytes.fromRange(stringBytes, p, e));
                if (StringSupport.MBCLEN_CHARFOUND_P(c)) {
                    p += c;
                    index++;
                } else {
                    return nil;
                }
            }

            for (; p < l; p += c, ++index) {
                c = calculateCharacterLengthNode.characterLength(enc, cr, Bytes.fromRange(stringBytes, p, e));
                if (!StringSupport.MBCLEN_CHARFOUND_P(c)) {
                    return nil;
                }
                if (ArrayUtils.regionEquals(stringBytes, p, patternBytes, 0, pe)) {
                    return index;
                }
            }

            return nil;
        }
    }

    /** Search pattern in string starting after offset bytes, and return a byte index or nil */
    @Primitive(name = "string_byte_index", lowerFixnum = 2)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "pattern", type = RubyNode.class)
    @NodeChild(value = "offset", type = RubyNode.class)
    public abstract static class StringByteIndexNode extends PrimitiveNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @CreateCast("string")
        protected RubyNode coerceStringToRope(RubyNode string) {
            return ToRopeNodeGen.create(string);
        }

        @CreateCast("pattern")
        protected RubyNode coercePatternToRope(RubyNode pattern) {
            return ToRopeNodeGen.create(pattern);
        }

        @Specialization(guards = "!patternFits(stringRope, patternRope, offset)")
        protected Object patternTooLarge(Rope stringRope, Rope patternRope, int offset) {
            assert offset >= 0;
            return nil;
        }

        @Specialization(
                guards = {
                        "singleByteOptimizableNode.execute(stringRope)",
                        "patternFits(stringRope, patternRope, offset)" })
        protected Object singleByteOptimizable(Rope stringRope, Rope patternRope, int offset,
                @Cached @Shared("stringBytesNode") BytesNode stringBytesNode,
                @Cached @Shared("patternBytesNode") BytesNode patternBytesNode,
                @Cached LoopConditionProfile loopProfile) {

            assert offset >= 0;
            int p = offset;
            final int e = stringRope.byteLength();
            final int pe = patternRope.byteLength();
            final int l = e - pe + 1;

            final byte[] stringBytes = stringBytesNode.execute(stringRope);
            final byte[] patternBytes = patternBytesNode.execute(patternRope);

            try {
                for (; loopProfile.inject(p < l); p++) {
                    if (ArrayUtils.regionEquals(stringBytes, p, patternBytes, 0, pe)) {
                        return p;
                    }
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, p - offset);
            }

            return nil;
        }

        @TruffleBoundary
        @Specialization(
                guards = {
                        "!singleByteOptimizableNode.execute(stringRope)",
                        "patternFits(stringRope, patternRope, offset)" })
        protected Object multiByte(Rope stringRope, Rope patternRope, int offset,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached @Shared("stringBytesNode") BytesNode stringBytesNode,
                @Cached @Shared("patternBytesNode") BytesNode patternBytesNode) {

            assert offset >= 0;
            int p = offset;
            final int e = stringRope.byteLength();
            final int pe = patternRope.byteLength();
            final int l = e - pe + 1;

            final byte[] stringBytes = stringBytesNode.execute(stringRope);
            final byte[] patternBytes = patternBytesNode.execute(patternRope);

            final Encoding enc = stringRope.getEncoding();
            final CodeRange cr = stringRope.getCodeRange();
            int c;

            for (; p < l; p += c) {
                c = calculateCharacterLengthNode.characterLength(enc, cr, Bytes.fromRange(stringBytes, p, e));
                if (!StringSupport.MBCLEN_CHARFOUND_P(c)) {
                    return nil;
                }
                if (ArrayUtils.regionEquals(stringBytes, p, patternBytes, 0, pe)) {
                    return p;
                }
            }

            return nil;
        }

        protected boolean patternFits(Rope stringRope, Rope patternRope, int offset) {
            return offset + patternRope.byteLength() <= stringRope.byteLength();
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
    public abstract static class ByteIndexFromCharIndexNode extends RubyBaseNode {

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
                @Cached BytesNode bytesNode,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached CodeRangeNode codeRangeNode) {
            // Taken from Rubinius's String::byte_index.

            final Encoding enc = rope.getEncoding();
            final byte[] bytes = bytesNode.execute(rope);
            final int e = rope.byteLength();
            int p = startByteOffset;

            int i, k = characterIndex;

            for (i = 0; i < k && p < e; i++) {
                final int c = calculateCharacterLengthNode
                        .characterLength(enc, codeRangeNode.execute(rope), Bytes.fromRange(bytes, p, e));

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
    public abstract static class StringByteIndexFromCharIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object singleByteOptimizable(Object string, int characterIndex,
                @Cached ByteIndexFromCharIndexNode byteIndexFromCharIndexNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            return byteIndexFromCharIndexNode.execute(libString.getRope(string), 0, characterIndex);
        }

    }

    // Port of Rubinius's String::previous_byte_index.
    //
    // This method takes a byte index, finds the corresponding character the byte index belongs to, and then returns
    // the byte index marking the start of the previous character in the string.
    @Primitive(name = "string_previous_byte_index", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public abstract static class StringPreviousByteIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "index < 0")
        protected Object negativeIndex(Object string, int index) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative index given", this));
        }

        @Specialization(guards = "index == 0")
        protected Object zeroIndex(Object string, int index) {
            return nil;
        }

        @Specialization(guards = {
                "index > 0",
                "isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)" })
        protected int singleByteOptimizable(Object string, int index,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            return index - 1;
        }

        @Specialization(guards = {
                "index > 0",
                "!isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)",
                "isFixedWidthEncoding(strings.getRope(string))" })
        protected int fixedWidthEncoding(Object string, int index,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached ConditionProfile firstCharacterProfile) {
            final Encoding encoding = strings.getRope(string).getEncoding();

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

        @Specialization(guards = {
                "index > 0",
                "!isSingleByteOptimizable(strings.getRope(string), singleByteOptimizableNode)",
                "!isFixedWidthEncoding(strings.getRope(string))" })
        @TruffleBoundary
        protected Object other(Object string, int index,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            final Rope rope = strings.getRope(string);
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
    public abstract static class StringRindexPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child CodeRangeNode codeRangeNode = CodeRangeNode.create();
        @Child SingleByteOptimizableNode singleByteNode = SingleByteOptimizableNode.create();

        @Specialization(guards = { "isEmpty(stringsPattern.getRope(pattern))" })
        protected Object stringRindexEmptyPattern(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsPattern) {
            assert byteOffset >= 0;
            return byteOffset;
        }

        @Specialization(guards = {
                "isSingleByteString(patternRope)",
                "!isBrokenCodeRange(patternRope, codeRangeNode)",
                "canMemcmp(libString.getRope(string), patternRope, singleByteNode)" })
        protected Object stringRindexSingleBytePattern(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern,
                @Bind("libPattern.getRope(pattern)") Rope patternRope,
                @Cached BytesNode bytesNode,
                @Cached BranchProfile startTooLargeProfile,
                @Cached BranchProfile matchFoundProfile,
                @Cached BranchProfile noMatchProfile,
                @Cached LoopConditionProfile loopProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = libString.getRope(string);
            final int end = sourceRope.byteLength();
            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final byte searchByte = bytesNode.execute(patternRope)[0];
            int normalizedStart = byteOffset;

            if (normalizedStart >= end) {
                startTooLargeProfile.enter();
                normalizedStart = end - 1;
            }

            int i = normalizedStart;
            try {
                for (; loopProfile.inject(i >= 0); i--) {
                    if (sourceBytes[i] == searchByte) {
                        matchFoundProfile.enter();
                        return i;
                    }
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, normalizedStart - i);
            }

            noMatchProfile.enter();
            return nil;
        }

        @Specialization(guards = {
                "!isEmpty(patternRope)",
                "!isSingleByteString(patternRope)",
                "!isBrokenCodeRange(patternRope, codeRangeNode)",
                "canMemcmp(libString.getRope(string), patternRope, singleByteNode)" })
        protected Object stringRindexMultiBytePattern(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern,
                @Bind("libPattern.getRope(pattern)") Rope patternRope,
                @Cached BytesNode bytesNode,
                @Cached BranchProfile startOutOfBoundsProfile,
                @Cached BranchProfile startTooCloseToEndProfile,
                @Cached BranchProfile matchFoundProfile,
                @Cached BranchProfile noMatchProfile,
                @Cached LoopConditionProfile loopProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            assert byteOffset >= 0;

            checkEncoding(string, pattern);

            final Rope sourceRope = libString.getRope(string);
            final int end = sourceRope.byteLength();
            final byte[] sourceBytes = bytesNode.execute(sourceRope);
            final int matchSize = patternRope.byteLength();
            final byte[] searchBytes = bytesNode.execute(patternRope);
            int normalizedStart = byteOffset;

            if (normalizedStart >= end) {
                startOutOfBoundsProfile.enter();
                normalizedStart = end - 1;
            }

            if (end - normalizedStart < matchSize) {
                startTooCloseToEndProfile.enter();
                normalizedStart = end - matchSize;
            }

            int i = normalizedStart;
            try {
                for (; loopProfile.inject(i >= 0); i--) {
                    if (sourceBytes[i] == searchBytes[0]) {
                        if (ArrayUtils.regionEquals(sourceBytes, i, searchBytes, 0, matchSize)) {
                            matchFoundProfile.enter();
                            return i;
                        }
                    }
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, normalizedStart - i);
            }

            noMatchProfile.enter();
            return nil;
        }

        @Specialization(guards = { "isBrokenCodeRange(stringsPattern.getRope(pattern), codeRangeNode)" })
        protected Object stringRindexBrokenPattern(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary stringsPattern) {
            assert byteOffset >= 0;
            return nil;
        }

        @Specialization(guards = {
                "!isBrokenCodeRange(patternRope, codeRangeNode)",
                "!canMemcmp(libString.getRope(string), patternRope, singleByteNode)" })
        protected Object stringRindex(Object string, Object pattern, int byteOffset,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern,
                @Bind("libPattern.getRope(pattern)") Rope patternRope,
                @Cached BytesNode stringBytes,
                @Cached BytesNode patternBytes,
                @Cached GetByteNode patternGetByteNode,
                @Cached GetByteNode stringGetByteNode,
                @Cached LoopConditionProfile loopProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // Taken from Rubinius's String::rindex.
            assert byteOffset >= 0;

            int pos = byteOffset;

            final Rope stringRope = libString.getRope(string);
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

                    try {
                        while (loopProfile.inject(cur >= 0)) {
                            if (ArrayUtils.regionEquals(
                                    stringBytes.execute(stringRope),
                                    cur,
                                    patternBytes.execute(patternRope),
                                    0,
                                    matchSize)) {
                                return cur;
                            }

                            cur--;
                            TruffleSafepoint.poll(this);
                        }
                    } finally {
                        profileAndReportLoopCount(loopProfile, pos - cur);
                    }
                }
            }

            return nil;
        }

        private void checkEncoding(Object string, Object pattern) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            checkEncodingNode.executeCheckEncoding(string, pattern);
        }

    }

    @NonStandard
    @CoreMethod(names = "pattern", constructor = true, required = 2, lowerFixnum = { 1, 2 })
    public abstract static class StringPatternPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();
        @Child private RepeatNode repeatNode = RepeatNode.create();

        @Specialization(guards = "pattern >= 0")
        protected RubyString stringPatternZero(RubyClass stringClass, int size, int pattern) {
            final Rope repeatingRope = repeatNode
                    .executeRepeat(RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES[pattern], size);

            final RubyString result = new RubyString(
                    stringClass,
                    getLanguage().stringShape,
                    false,
                    repeatingRope,
                    Encodings.BINARY);
            AllocationTracing.trace(result, this);
            return result;
        }

        @Specialization(
                guards = { "libPattern.isRubyString(pattern)", "patternFitsEvenly(libPattern.getRope(pattern), size)" })
        protected RubyString stringPatternFitsEvenly(RubyClass stringClass, int size, Object pattern,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern) {
            final Rope rope = libPattern.getRope(pattern);
            final Rope repeatingRope = repeatNode.executeRepeat(rope, size / rope.byteLength());
            final RubyString result = new RubyString(
                    stringClass,
                    getLanguage().stringShape,
                    false,
                    repeatingRope,
                    libPattern.getEncoding(pattern));
            AllocationTracing.trace(result, this);
            return result;
        }

        @TruffleBoundary
        @Specialization(guards = {
                "libPattern.isRubyString(pattern)",
                "!patternFitsEvenly(libPattern.getRope(pattern), size)" })
        protected RubyString stringPattern(RubyClass stringClass, int size, Object pattern,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern) {
            final Rope rope = libPattern.getRope(pattern);
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

            LeafRope leafRope = makeLeafRopeNode
                    .executeMake(bytes, libPattern.getRope(pattern).getEncoding(), codeRange, characterLength);

            final RubyString result = new RubyString(
                    stringClass,
                    getLanguage().stringShape,
                    false,
                    leafRope,
                    libPattern.getEncoding(pattern));
            AllocationTracing.trace(result, this);
            return result;
        }

        protected boolean patternFitsEvenly(Rope stringRope, int size) {
            final int byteLength = stringRope.byteLength();

            return byteLength > 0 && (size % byteLength) == 0;
        }

    }

    @Primitive(name = "string_splice", lowerFixnum = { 2, 3 })
    @ImportStatic(StringGuards.class)
    public abstract static class StringSplicePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "libOther.isRubyString(other)", "indexAtStartBound(spliceByteIndex)" })
        protected Object splicePrepend(
                RubyString string, Object other, int spliceByteIndex, int byteCountToReplace, RubyEncoding rubyEncoding,
                @Cached SubstringNode prependSubstringNode,
                @Cached ConcatNode prependConcatNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther) {
            final Encoding encoding = rubyEncoding.jcoding;
            final Rope original = string.rope;
            final Rope left = libOther.getRope(other);
            final Rope right = prependSubstringNode
                    .executeSubstring(original, byteCountToReplace, original.byteLength() - byteCountToReplace);

            final Rope prependResult = prependConcatNode.executeConcat(left, right, encoding);
            string.setRope(prependResult, rubyEncoding);

            return string;
        }

        @Specialization(guards = { "libOther.isRubyString(other)", "indexAtEndBound(string, spliceByteIndex)" })
        protected Object spliceAppend(
                RubyString string, Object other, int spliceByteIndex, int byteCountToReplace, RubyEncoding rubyEncoding,
                @Cached ConcatNode appendConcatNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther) {
            final Encoding encoding = rubyEncoding.jcoding;
            final Rope left = string.rope;
            final Rope right = libOther.getRope(other);

            final Rope concatResult = appendConcatNode.executeConcat(left, right, encoding);
            string.setRope(concatResult, rubyEncoding);

            return string;
        }

        @Specialization(guards = { "libOther.isRubyString(other)", "!indexAtEitherBounds(string, spliceByteIndex)" })
        protected RubyString splice(
                RubyString string, Object other, int spliceByteIndex, int byteCountToReplace, RubyEncoding rubyEncoding,
                @Cached ConditionProfile insertStringIsEmptyProfile,
                @Cached ConditionProfile splitRightIsEmptyProfile,
                @Cached SubstringNode leftSubstringNode,
                @Cached SubstringNode rightSubstringNode,
                @Cached ConcatNode leftConcatNode,
                @Cached ConcatNode rightConcatNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther) {
            final Encoding encoding = rubyEncoding.jcoding;
            final Rope source = string.rope;
            final Rope insert = libOther.getRope(other);
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

            string.setRope(joinedRight, string.encoding);
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
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "fixBase", type = RubyNode.class)
    @NodeChild(value = "strict", type = RubyNode.class)
    @NodeChild(value = "raiseOnError", type = RubyNode.class)
    public abstract static class StringToInumPrimitiveNode extends PrimitiveNode {

        @CreateCast("string")
        protected RubyNode coerceStringToRope(RubyNode string) {
            return ToRopeNodeGen.create(string);
        }

        @Specialization(guards = "isLazyIntRopeOptimizable(rope, fixBase)")
        protected int stringToInumIntRope(Rope rope, int fixBase, boolean strict, boolean raiseOnError) {
            return ((LazyIntRope) rope).getValue();
        }

        @Specialization(guards = "!isLazyIntRopeOptimizable(rope, fixBase)")
        protected Object stringToInum(Rope rope, int fixBase, boolean strict, boolean raiseOnError,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignumNode,
                @Cached BytesNode bytesNode,
                @Cached BranchProfile exceptionProfile) {
            try {
                return ConvertBytes.bytesToInum(
                        getContext(),
                        this,
                        fixnumOrBignumNode,
                        bytesNode,
                        rope,
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

        protected boolean isLazyIntRopeOptimizable(Rope rope, int base) {
            return (base == 0 || base == 10) && rope instanceof LazyIntRope;
        }
    }

    @Primitive(name = "string_byte_append")
    public abstract static class StringByteAppendPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private ConcatNode concatNode = ConcatNode.create();

        @Specialization(guards = "libOther.isRubyString(other)")
        protected RubyString stringByteAppend(RubyString string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther) {
            final Rope left = string.rope;
            final Rope right = libOther.getRope(other);

            // The semantics of this primitive are such that the original string's byte[] should be extended without
            // negotiating the encoding.
            string.setRope(concatNode.executeConcat(left, right, left.getEncoding()));
            return string;
        }

    }

    @Primitive(name = "string_substring", lowerFixnum = { 1, 2 })
    @ImportStatic(StringGuards.class)
    public abstract static class StringSubstringPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private NormalizeIndexNode normalizeIndexNode = NormalizeIndexNode.create();
        @Child CharacterLengthNode characterLengthNode = CharacterLengthNode.create();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode
                .create();
        @Child LogicalClassNode logicalClassNode = LogicalClassNode.create();
        @Child private SubstringNode substringNode;

        public abstract Object execute(Object string, int index, int length);

        @Specialization(guards = {
                "!indexTriviallyOutOfBounds(libString.getRope(string), characterLengthNode, index, length)",
                "noCharacterSearch(libString.getRope(string), singleByteOptimizableNode)" })
        protected Object stringSubstringSingleByte(Object string, int index, int length,
                @Cached @Shared("negativeIndexProfile") ConditionProfile negativeIndexProfile,
                @Cached @Shared("tooLargeTotalProfile") ConditionProfile tooLargeTotalProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final Rope rope = libString.getRope(string);
            final RubyEncoding encoding = libString.getEncoding(string);
            final int ropeCharacterLength = characterLengthNode.execute(rope);
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, ropeCharacterLength);
            int characterLength = length;

            if (negativeIndexProfile.profile(normalizedIndex < 0)) {
                return nil;
            }

            if (tooLargeTotalProfile.profile(normalizedIndex + characterLength > ropeCharacterLength)) {
                characterLength = ropeCharacterLength - normalizedIndex;
            }

            return makeRope(string, encoding, rope, normalizedIndex, characterLength);
        }

        @Specialization(guards = {
                "!indexTriviallyOutOfBounds(libString.getRope(string), characterLengthNode, index, length)",
                "!noCharacterSearch(libString.getRope(string), singleByteOptimizableNode)" })
        protected Object stringSubstringGeneric(Object string, int index, int length,
                @Cached @Shared("negativeIndexProfile") ConditionProfile negativeIndexProfile,
                @Cached @Shared("tooLargeTotalProfile") ConditionProfile tooLargeTotalProfile,
                @Cached @Exclusive ConditionProfile foundSingleByteOptimizableDescendentProfile,
                @Cached BranchProfile singleByteOptimizableBaseProfile,
                @Cached BranchProfile leafBaseProfile,
                @Cached BranchProfile slowSearchProfile,
                @Cached ByteIndexFromCharIndexNode byteIndexFromCharIndexNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            final Rope rope = libString.getRope(string);
            final RubyEncoding encoding = libString.getEncoding(string);
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
                return makeRope(
                        string,
                        encoding,
                        searchResult.rope,
                        searchResult.index,
                        characterLength);
            }

            return stringSubstringMultiByte(
                    string,
                    libString,
                    normalizedIndex,
                    characterLength,
                    byteIndexFromCharIndexNode);
        }

        @Specialization(guards = {
                "indexTriviallyOutOfBounds(strings.getRope(string), characterLengthNode, index, length)" })
        protected Object stringSubstringNegativeLength(Object string, int index, int length,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
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

                final ConcatState state = concatRope.getState();
                if (state.isFlattened()) {
                    return new SearchResult(index, base);
                } else {
                    final Rope left = state.left;
                    final Rope right = state.right;
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

        private Object stringSubstringMultiByte(Object string, RubyStringLibrary libString, int beg, int characterLen,
                ByteIndexFromCharIndexNode byteIndexFromCharIndexNode) {
            // Taken from org.jruby.RubyString#substr19 & org.jruby.RubyString#multibyteSubstr19.

            final Rope rope = libString.getRope(string);
            final RubyEncoding encoding = libString.getEncoding(string);
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

            return makeRope(string, encoding, rope, p, substringByteLength);
        }

        private RubyString makeRope(Object string, RubyEncoding encoding, Rope rope, int beg, int byteLength) {
            if (substringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                substringNode = insert(SubstringNode.create());
            }

            final RubyClass logicalClass = logicalClassNode.execute(string);
            final Rope substringRope = substringNode.executeSubstring(rope, beg, byteLength);
            final RubyString ret = new RubyString(
                    logicalClass,
                    getLanguage().stringShape,
                    false,
                    substringRope,
                    encoding);
            AllocationTracing.trace(ret, this);
            return ret;
        }

        protected static boolean indexTriviallyOutOfBounds(Rope rope,
                CharacterLengthNode characterLengthNode,
                int index, int length) {
            return (length < 0) ||
                    (index > characterLengthNode.execute(rope));
        }

        protected static boolean noCharacterSearch(Rope rope,
                SingleByteOptimizableNode singleByteOptimizableNode) {
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
    public abstract static class StringFromByteArrayPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString stringFromByteArray(
                RubyByteArray byteArray, int start, int count, RubyEncoding rubyEncoding,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final byte[] bytes = byteArray.bytes;
            final byte[] array = ArrayUtils.extractRange(bytes, start, start + count);

            return makeStringNode.executeMake(array, rubyEncoding, CR_UNKNOWN);
        }

    }

    public abstract static class StringAppendNode extends RubyBaseNode {

        @Child private CheckEncodingNode checkEncodingNode;
        @Child private ConcatNode concatNode;

        public static StringAppendNode create() {
            return StringAppendNodeGen.create();
        }

        public abstract Pair<Rope, RubyEncoding> executeStringAppend(Object string, Object other);

        @Specialization(guards = "libOther.isRubyString(other)")
        protected Pair<Rope, RubyEncoding> stringAppend(Object string, Object other,
                @CachedLibrary(limit = "2") RubyStringLibrary libString,
                @CachedLibrary(limit = "2") RubyStringLibrary libOther) {
            final Rope left = libString.getRope(string);
            final Rope right = libOther.getRope(other);

            final RubyEncoding compatibleEncoding = executeCheckEncoding(string, other);

            final Rope result = executeConcat(left, right, compatibleEncoding);
            return Pair.create(result, compatibleEncoding);
        }

        private Rope executeConcat(Rope left, Rope right, RubyEncoding compatibleEncoding) {
            if (concatNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                concatNode = insert(ConcatNode.create());
            }
            return concatNode.executeConcat(left, right, compatibleEncoding.jcoding);
        }

        private RubyEncoding executeCheckEncoding(Object string, Object other) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }
            return checkEncodingNode.executeCheckEncoding(string, other);
        }

    }

    @Primitive(name = "string_to_null_terminated_byte_array")
    public abstract static class StringToNullTerminatedByteArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libString.isRubyString(string)")
        protected Object stringToNullTerminatedByteArray(Object string,
                @Cached BytesNode bytesNode,
                @CachedLibrary(limit = "2") RubyStringLibrary libString) {
            // NOTE: we always need one copy here, as native code could modify the passed byte[]
            final byte[] bytes = bytesNode.execute(libString.getRope(string));
            final byte[] bytesWithNull = new byte[bytes.length + 1];
            System.arraycopy(bytes, 0, bytesWithNull, 0, bytes.length);

            return getContext().getEnv().asGuestValue(bytesWithNull);
        }

        @Specialization
        protected Object emptyString(Nil string) {
            return getContext().getEnv().asGuestValue(null);
        }

    }

    @Primitive(name = "string_interned?")
    public abstract static class IsInternedNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean isInterned(ImmutableRubyString string) {
            return true;
        }

        @Specialization
        protected boolean isInterned(RubyString string) {
            return false;
        }
    }

    @Primitive(name = "string_intern")
    public abstract static class InternNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected ImmutableRubyString internString(RubyString string,
                @Cached FlattenNode flattenNode) {
            final Rope flattened = flattenNode.executeFlatten(string.rope);
            return getLanguage().getFrozenStringLiteral(flattened);
        }

    }

}
