/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
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

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.BROKEN;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.VALID;
import static org.truffleruby.core.string.TStringConstants.EMPTY_BINARY;
import static org.truffleruby.core.string.StringSupport.MBCLEN_CHARFOUND_LEN;
import static org.truffleruby.core.string.StringSupport.MBCLEN_CHARFOUND_P;
import static org.truffleruby.core.string.StringSupport.MBCLEN_INVALID_P;
import static org.truffleruby.core.string.StringSupport.MBCLEN_NEEDMORE_P;

import java.nio.charset.StandardCharsets;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.MutableTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.AsTruffleStringNode;
import com.oracle.truffle.api.strings.TruffleString.CodePointLengthNode;
import com.oracle.truffle.api.strings.TruffleString.ErrorHandling;
import com.oracle.truffle.api.strings.TruffleString.GetByteCodeRangeNode;
import com.oracle.truffle.api.strings.TruffleStringIterator;
import org.graalvm.collections.Pair;
import org.jcodings.Config;
import org.jcodings.Encoding;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.encoding.EncodingNodes.NegotiateCompatibleStringEncodingNode;
import org.truffleruby.core.encoding.IsCharacterHeadNode;
import org.truffleruby.core.encoding.EncodingNodes.CheckEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.GetActualEncodingNode;
import org.truffleruby.core.encoding.EncodingNodes.NegotiateCompatibleEncodingNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
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
import org.truffleruby.core.string.StringHelperNodes.SingleByteOptimizableNode;
import org.truffleruby.core.string.StringNodesFactory.DeleteBangNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.MakeStringNodeGen;
import org.truffleruby.core.string.StringNodesFactory.StringAppendPrimitiveNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.StringSubstringPrimitiveNodeFactory;
import org.truffleruby.core.string.StringNodesFactory.SumNodeFactory;
import org.truffleruby.core.support.RubyByteArray;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ReadCallerVariablesNode;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.objects.AllocationTracing;
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
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "String", isClass = true)
public abstract class StringNodes {

    // TODO: replace it by FromByteArrayNode/FromJavaStringNode directly
    @GenerateUncached
    public abstract static class MakeStringNode extends RubyBaseNode {

        public final RubyString executeMake(String javaString, RubyEncoding encoding) {
            return executeInternal(javaString, encoding);
        }

        public final RubyString executeMake(byte[] bytes, RubyEncoding encoding) {
            return executeInternal(bytes, encoding);
        }

        protected abstract RubyString executeInternal(Object payload, RubyEncoding encoding);

        public static MakeStringNode create() {
            return MakeStringNodeGen.create();
        }

        public static MakeStringNode getUncached() {
            return MakeStringNodeGen.getUncached();
        }

        @Specialization
        protected RubyString makeStringFromBytes(byte[] bytes, RubyEncoding encoding,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            return createString(fromByteArrayNode, bytes, encoding);
        }

        @Specialization
        protected RubyString makeStringFromString(String string, RubyEncoding encoding,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            return createString(fromJavaStringNode, string, encoding);
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    @NodeChild(value = "rubyClass", type = RubyNode.class)
    public abstract static class AllocateNode extends RubySourceNode {

        public static AllocateNode create() {
            return StringNodesFactory.AllocateNodeFactory.create(null);
        }

        public abstract RubyString execute(RubyClass rubyClass);

        @Specialization
        protected RubyString allocate(RubyClass rubyClass) {
            final RubyString string = new RubyString(
                    rubyClass,
                    getLanguage().stringShape,
                    false,
                    EMPTY_BINARY,
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
                @Cached StringHelperNodes.StringAppendNode stringAppendNode) {
            return stringAppendNode.executeStringAppend(string, other);
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            final RubyEncoding encoding = libString.getEncoding(string);
            return createString(encoding.tencoding.getEmpty(), encoding);
        }

        @Specialization(guards = "times < 0")
        protected RubyString multiplyTimesNegative(Object string, long times) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative argument", this));
        }

        @Specialization(guards = { "times > 0", "!isEmpty(libString.getTString(string))" })
        protected RubyString multiply(Object string, int times,
                @Cached BranchProfile tooBigProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.RepeatNode repeatNode) {
            var tstring = libString.getTString(string);
            var encoding = libString.getEncoding(string);

            long longLength = (long) times * tstring.byteLength(encoding.tencoding);
            if (longLength > Integer.MAX_VALUE) {
                tooBigProfile.enter();
                throw tooBig();
            }

            return createString(repeatNode.execute(tstring, times, encoding.tencoding), encoding);
        }

        @Specialization(guards = { "times > 0", "libString.getTString(string).isEmpty()" })
        protected RubyString multiplyEmpty(Object string, long times,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            var encoding = libString.getEncoding(string);
            return createString(encoding.tencoding.getEmpty(), encoding);
        }

        @Specialization(guards = { "times > 0", "!isEmpty(strings.getTString(string))" })
        protected RubyString multiplyNonEmpty(Object string, long times,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
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
    public abstract static class EqualCoreMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private KernelNodes.RespondToNode respondToNode;
        @Child private DispatchNode objectEqualNode;
        @Child private BooleanCastNode booleanCastNode;

        @Specialization(guards = "libB.isRubyString(b)")
        protected boolean equalString(Object a, Object b,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libA,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libB,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
                @Cached StringHelperNodes.StringEqualInternalNode stringEqualInternalNode) {
            var tstringA = libA.getTString(a);
            var encA = libA.getEncoding(a);
            var tstringB = libB.getTString(b);
            var encB = libB.getEncoding(b);
            var compatibleEncoding = negotiateCompatibleStringEncodingNode.execute(tstringA, encA, tstringB, encB);
            return stringEqualInternalNode.executeInternal(tstringA, tstringB, compatibleEncoding);
        }

        @Specialization(guards = "isNotRubyString(b)")
        protected boolean equal(Object a, Object b) {
            if (respondToNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToNode = insert(KernelNodesFactory.RespondToNodeFactory.create());
            }

            if (respondToNode.executeDoesRespondTo(b, coreSymbols().TO_STR, false)) {
                if (objectEqualNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    objectEqualNode = insert(DispatchNode.create());
                }

                if (booleanCastNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    booleanCastNode = insert(BooleanCastNode.create());
                }

                return booleanCastNode.execute(objectEqualNode.call(b, "==", a));
            }

            return false;
        }

    }

    // compatibleEncoding is RubyEncoding or Nil in this node
    @Primitive(name = "string_cmp")
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "first.isEmpty() || second.isEmpty()")
        protected int empty(Object a, Object b, RubyEncoding compatibleEncoding,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libA,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libB,
                @Bind("libA.getTString(a)") AbstractTruffleString first,
                @Bind("libB.getTString(b)") AbstractTruffleString second,
                @Cached ConditionProfile bothEmpty) {
            if (bothEmpty.profile(first.isEmpty() && second.isEmpty())) {
                return 0;
            } else {
                return first.isEmpty() ? -1 : 1;
            }
        }

        @Specialization(guards = { "!first.isEmpty()", "!second.isEmpty()" })
        protected int compatible(Object a, Object b, RubyEncoding compatibleEncoding,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libA,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libB,
                @Bind("libA.getTString(a)") AbstractTruffleString first,
                @Bind("libB.getTString(b)") AbstractTruffleString second,
                @Cached ConditionProfile sameRopeProfile,
                @Cached TruffleString.CompareBytesNode compareBytesNode,
                @Cached ConditionProfile equalProfile,
                @Cached ConditionProfile positiveProfile) {
            if (sameRopeProfile.profile(first == second)) {
                return 0;
            }

            int result = compareBytesNode.execute(first, second, compatibleEncoding.tencoding);
            if (equalProfile.profile(result == 0)) {
                return 0;
            } else {
                return positiveProfile.profile(result > 0) ? 1 : -1;
            }
        }

        @Specialization
        protected int notCompatible(Object a, Object b, Nil compatibleEncoding,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libA,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libB,
                @Cached ConditionProfile sameRopeProfile,
                @Cached TruffleString.CompareBytesNode compareBytesNode,
                @Cached TruffleString.ForceEncodingNode forceEncoding1Node,
                @Cached TruffleString.ForceEncodingNode forceEncoding2Node,
                @Cached ConditionProfile equalProfile,
                @Cached ConditionProfile positiveProfile,
                @Cached ConditionProfile encodingIndexGreaterThanProfile) {
            var first = libA.getTString(a);
            var firstEncoding = libA.getEncoding(a);
            var second = libB.getTString(b);
            var secondEncoding = libB.getEncoding(b);

            if (sameRopeProfile.profile(first == second)) {
                return 0;
            }

            // Compare as binary as CRuby compares bytes regardless of the encodings
            var firstBinary = forceEncoding1Node.execute(first, firstEncoding.tencoding, Encodings.BINARY.tencoding);
            var secondBinary = forceEncoding2Node.execute(second, secondEncoding.tencoding, Encodings.BINARY.tencoding);
            int result = compareBytesNode.execute(firstBinary, secondBinary, Encodings.BINARY.tencoding);

            if (equalProfile.profile(result == 0)) {
                if (encodingIndexGreaterThanProfile.profile(firstEncoding.index > secondEncoding.index)) {
                    return 1;
                } else {
                    return -1;
                }
            }

            return positiveProfile.profile(result > 0) ? 1 : -1;
        }

    }

    @Primitive(name = "dup_as_string_instance")
    public abstract static class StringDupAsStringInstanceNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyString dupAsStringInstance(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached AsTruffleStringNode asTruffleStringNode) {
            final RubyEncoding encoding = strings.getEncoding(string);
            return createStringCopy(asTruffleStringNode, strings.getTString(string), encoding);
        }
    }

    @CoreMethod(names = "<<", required = 1, raiseIfNotMutableSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class StringConcatOneNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "libFirst.isRubyString(first)")
        protected RubyString concat(RubyString string, Object first,
                @Cached StringAppendPrimitiveNode stringAppendNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFirst) {
            return stringAppendNode.executeStringAppend(string, first);
        }

        @Specialization(guards = "isNotRubyString(first)")
        protected Object concatGeneric(RubyString string, Object first,
                @Cached DispatchNode callNode) {
            return callNode.call(coreLibrary().truffleStringOperationsModule, "concat_internal", string, first);
        }

    }

    @CoreMethod(names = "concat", optional = 1, rest = true, raiseIfNotMutableSelf = true)
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFirst) {
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
                @Cached AsTruffleStringNode asTruffleStringNode,
                @Cached ConditionProfile selfArgProfile) {
            var tstring = string.tstring;
            Object result = argConcatNode.executeConcat(string, first, EMPTY_ARGUMENTS);
            for (int i = 0; i < cachedLength; ++i) {
                Object arg = rest[i];
                final Object argOrCopy = selfArgProfile.profile(arg == string)
                        ? createStringCopy(asTruffleStringNode, tstring, string.encoding)
                        : arg;
                result = argConcatNode.executeConcat(string, argOrCopy, EMPTY_ARGUMENTS);
            }
            return result;
        }

        /** Same implementation as {@link #concatMany}, safe for the use of {@code cachedLength} */
        @Specialization(guards = { "wasProvided(first)", "rest.length > 0" }, replaces = "concatMany")
        protected Object concatManyGeneral(RubyString string, Object first, Object[] rest,
                @Cached StringConcatNode argConcatNode,
                @Cached AsTruffleStringNode asTruffleStringNode,
                @Cached ConditionProfile selfArgProfile) {
            var tstring = string.tstring;
            Object result = argConcatNode.executeConcat(string, first, EMPTY_ARGUMENTS);
            for (Object arg : rest) {
                final Object argOrCopy = selfArgProfile.profile(arg == string)
                        ? createStringCopy(asTruffleStringNode, tstring, string.encoding)
                        : arg;
                result = argConcatNode.executeConcat(string, argOrCopy, EMPTY_ARGUMENTS);
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

        @Child private StringHelperNodes.NormalizeIndexNode normalizeIndexNode;
        @Child private StringSubstringPrimitiveNode substringNode;
        @Child private ToLongNode toLongNode;
        @Child private CodePointLengthNode codePointLengthNode;
        private final BranchProfile outOfBounds = BranchProfile.create();

        // endregion
        // region GetIndex Specializations

        @Specialization
        protected Object getIndex(Object string, int index, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            return index == codePointLength(strings.getTString(string), strings.getEncoding(string)) // Check for the only difference from str[index, 1]
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            return sliceRange(string, libString, range.begin, range.end, range.excludedEnd);
        }

        @Specialization
        protected Object sliceLongRange(Object string, RubyLongRange range, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            return sliceRange(string, libString, range.begin, range.end, range.excludedEnd);
        }

        @Specialization(guards = "range.isEndless()")
        protected Object sliceEndlessRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            final int stringEnd = range.excludedEnd ? Integer.MAX_VALUE : Integer.MAX_VALUE - 1;
            return sliceRange(string, libString, toLong(range.begin), stringEnd, range.excludedEnd);
        }

        @Specialization(guards = "range.isBeginless()")
        protected Object sliceBeginlessRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            return sliceRange(string, libString, 0L, toLong(range.end), range.excludedEnd);
        }

        @Specialization(guards = "range.isBounded()")
        protected Object sliceObjectRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            return sliceRange(string, libString, toLong(range.begin), toLong(range.end), range.excludedEnd);
        }

        @Specialization(guards = "range.isBoundless()")
        protected Object sliceBoundlessRange(Object string, RubyObjectRange range, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
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
            final int stringLength = codePointLength(libString.getTString(string), libString.getEncoding(string));
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
                @Cached ConditionProfile sameThreadProfile,
                @Cached ConditionProfile notMatchedProfile,
                @Cached ConditionProfile captureSetProfile) {
            final Object capture = RubyGuards.wasProvided(maybeCapture) ? maybeCapture : 0;
            final Object matchStrPair = callNode.call(
                    getContext().getCoreLibrary().truffleStringOperationsModule,
                    "subpattern",
                    string,
                    regexp,
                    capture);

            final SpecialVariableStorage variables = readCallerStorageNode.execute(frame);
            if (notMatchedProfile.profile(matchStrPair == nil)) {
                variables.setLastMatch(nil, getContext(), unsetProfile, sameThreadProfile);
                return nil;
            } else {
                final Object[] array = (Object[]) ((RubyArray) matchStrPair).getStore();
                final Object matchData = array[0];
                final Object captureStringOrNil = array[1];
                variables.setLastMatch(matchData, getContext(), unsetProfile, sameThreadProfile);
                if (captureSetProfile.profile(captureStringOrNil != nil)) {
                    return captureStringOrNil;
                } else {
                    return nil;
                }
            }
        }

        // endregion
        // region String Slice Specialization

        @Specialization(guards = "stringsMatchStr.isRubyString(matchStr)")
        protected Object slice2(Object string, Object matchStr, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsMatchStr,
                @Cached @Exclusive DispatchNode includeNode,
                @Cached BooleanCastNode booleanCastNode,
                @Cached AsTruffleStringNode asTruffleStringNode) {

            final Object included = includeNode.call(string, "include?", matchStr);

            if (booleanCastNode.execute(included)) {
                final RubyEncoding encoding = stringsMatchStr.getEncoding(matchStr);
                return createStringCopy(asTruffleStringNode, stringsMatchStr.getTString(matchStr), encoding);
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

        private int codePointLength(AbstractTruffleString string, RubyEncoding encoding) {
            if (codePointLengthNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                codePointLengthNode = insert(CodePointLengthNode.create());
            }

            return codePointLengthNode.execute(string, encoding.tencoding);
        }

        private int normalizeIndex(int index, int length) {
            if (normalizeIndexNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                normalizeIndexNode = insert(StringHelperNodes.NormalizeIndexNode.create());
            }

            return normalizeIndexNode.executeNormalize(index, length);
        }

        // endregion
    }

    @CoreMethod(names = "ascii_only?")
    public abstract static class ASCIIOnlyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean asciiOnly(Object string,
                @Cached GetByteCodeRangeNode codeRangeNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            return StringGuards.is7Bit(libString.getTString(string), libString.getEncoding(string), codeRangeNode);
        }

    }

    @CoreMethod(names = "bytes", needsBlock = true)
    public abstract static class StringBytesNode extends YieldingCoreMethodNode {

        @Specialization(limit = "LIBSTRING_CACHE")
        protected RubyArray bytes(Object string, Nil block,
                // use separate specialization instances for getTString() in the loop
                @CachedLibrary(/* ^ */"string") RubyStringLibrary strings,
                @Cached TruffleString.MaterializeNode materializeNode,
                @Cached TruffleString.ReadByteNode readByteNode) {
            var tstring = strings.getTString(string);
            var encoding = strings.getEncoding(string).tencoding;
            int arrayLength = tstring.byteLength(encoding);

            final int[] store = new int[arrayLength];

            materializeNode.execute(tstring, encoding);
            for (int i = 0; i < arrayLength; i++) {
                store[i] = readByteNode.execute(tstring, i, encoding);
            }

            return createArray(store);
        }

        @Specialization(limit = "LIBSTRING_CACHE")
        protected Object bytes(Object string, RubyProc block,
                // use separate specialization instances for getTString() in the loop
                @CachedLibrary(/* ^ */"string") RubyStringLibrary strings,
                @Cached TruffleString.MaterializeNode materializeNode,
                @Cached TruffleString.ReadByteNode readByteNode) {
            var tstring = strings.getTString(string);
            var encoding = strings.getEncoding(string).tencoding;

            // String#bytes reflects changes by the block to the string's bytes
            materializeNode.execute(tstring, encoding);
            for (int i = 0; i < tstring.byteLength(encoding); i++) {
                int singleByte = readByteNode.execute(tstring, i, encoding);
                callBlock(block, singleByte);

                tstring = strings.getTString(string);
                encoding = strings.getEncoding(string).tencoding;
            }

            return string;
        }
    }

    @CoreMethod(names = "bytesize")
    public abstract static class ByteSizeNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected int byteSize(RubyString string) {
            return string.tstring.byteLength(string.getTEncoding());
        }

        @Specialization
        protected int immutableByteSize(ImmutableRubyString string) {
            return string.tstring.byteLength(string.getTEncoding());
        }
    }

    @Primitive(name = "string_casecmp")
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyBaseNodeWithExecute.class)
    public abstract static class CaseCmpNode extends PrimitiveNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleEncodingNode
                .create();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();
        private final ConditionProfile incompatibleEncodingProfile = ConditionProfile.create();
        private final ConditionProfile sameProfile = ConditionProfile.create();

        @CreateCast("other")
        protected ToStrNode coerceOtherToString(RubyBaseNodeWithExecute other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization(
                guards = "bothSingleByteOptimizable(libString.getTString(string), libOther.getTString(other), libString.getEncoding(string), libOther.getEncoding(other))")
        protected Object caseCmpSingleByte(Object string, Object other,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libOther,
                @Cached TruffleString.GetInternalByteArrayNode byteArraySelfNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayOtherNode) {
            // Taken from org.jruby.RubyString#casecmp19.

            final RubyEncoding encoding = negotiateCompatibleEncodingNode.executeNegotiate(string, other);
            if (incompatibleEncodingProfile.profile(encoding == null)) {
                return nil;
            }

            var selfTString = libString.getTString(string);
            var selfByteArray = byteArraySelfNode.execute(selfTString, libString.getTEncoding(string));
            var otherTString = libOther.getTString(other);
            var otherByteArray = byteArrayOtherNode.execute(otherTString, libOther.getTEncoding(other));

            if (sameProfile.profile(selfTString == otherTString)) {
                return 0;
            }

            return caseInsensitiveCmp(selfByteArray, otherByteArray);
        }

        @Specialization(
                guards = "!bothSingleByteOptimizable(libString.getTString(string), libOther.getTString(other), libString.getEncoding(string), libOther.getEncoding(other))")
        protected Object caseCmp(Object string, Object other,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libOther,
                @Cached TruffleString.GetInternalByteArrayNode byteArraySelfNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayOtherNode,
                @Cached GetByteCodeRangeNode selfCodeRangeNode,
                @Cached GetByteCodeRangeNode otherCodeRangeNode) {
            // Taken from org.jruby.RubyString#casecmp19 and

            final RubyEncoding encoding = negotiateCompatibleEncodingNode.executeNegotiate(string, other);

            if (incompatibleEncodingProfile.profile(encoding == null)) {
                return nil;
            }

            var selfTString = libString.getTString(string);
            var selfEncoding = libString.getEncoding(string);
            var selfByteArray = byteArraySelfNode.execute(selfTString, selfEncoding.tencoding);
            var selfCodeRange = selfCodeRangeNode.execute(selfTString, selfEncoding.tencoding);

            var otherTString = libOther.getTString(other);
            var otherEncoding = libOther.getEncoding(other);
            var otherByteArray = byteArrayOtherNode.execute(otherTString, otherEncoding.tencoding);
            var otherCodeRange = otherCodeRangeNode.execute(otherTString, otherEncoding.tencoding);

            if (sameProfile.profile(selfTString == otherTString)) {
                return 0;
            }

            return StringSupport.multiByteCasecmp(encoding.jcoding, selfByteArray, selfCodeRange, selfEncoding,
                    otherByteArray, otherCodeRange, otherEncoding);
        }

        protected boolean bothSingleByteOptimizable(AbstractTruffleString string, AbstractTruffleString other,
                RubyEncoding stringEncoding,
                RubyEncoding otherEncoding) {
            return singleByteOptimizableNode.execute(string, stringEncoding) &&
                    singleByteOptimizableNode.execute(other, otherEncoding);
        }

        @TruffleBoundary
        private static int caseInsensitiveCmp(InternalByteArray value, InternalByteArray other) {
            // Taken from org.jruby.util.ByteList#caseInsensitiveCmp.
            final int size = value.getLength();
            final int len = Math.min(size, other.getLength());

            for (int offset = -1; ++offset < len;) {
                int myCharIgnoreCase = AsciiTables.ToLowerCaseTable[value.get(offset) & 0xff] & 0xff;
                int otherCharIgnoreCase = AsciiTables.ToLowerCaseTable[other.get(offset) & 0xff] & 0xff;
                if (myCharIgnoreCase < otherCharIgnoreCase) {
                    return -1;
                } else if (myCharIgnoreCase > otherCharIgnoreCase) {
                    return 1;
                }
            }

            return size == other.getLength() ? 0 : size == len ? -1 : 1;
        }
    }

    /** Returns true if the first bytes in string are equal to the bytes in prefix. */
    @Primitive(name = "string_start_with?")
    public abstract static class StartWithNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean startWithBytes(Object string, Object prefix, RubyEncoding enc,
                @Cached TruffleString.RegionEqualByteIndexNode regionEqualByteIndexNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsSuffix) {

            var stringTString = strings.getTString(string);
            var stringEncoding = strings.getTEncoding(string);
            final int stringByteLength = stringTString.byteLength(stringEncoding);

            var prefixTString = stringsSuffix.getTString(prefix);
            var prefixEncoding = stringsSuffix.getTEncoding(prefix);
            final int prefixByteLength = prefixTString.byteLength(prefixEncoding);

            if (stringByteLength < prefixByteLength) {
                return false;
            }

            // See truffle-string.md, section Encodings Compatibility
            if (prefixByteLength == 0) {
                return true;
            }

            return regionEqualByteIndexNode.execute(stringTString, 0, prefixTString, 0, prefixByteLength,
                    enc.tencoding);
        }

    }

    /** Returns true if the last bytes in string are equal to the bytes in suffix. */
    @Primitive(name = "string_end_with?")
    public abstract static class EndWithNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean endWithBytes(Object string, Object suffix, RubyEncoding enc,
                @Cached IsCharacterHeadNode isCharacterHeadNode,
                @Cached TruffleString.RegionEqualByteIndexNode regionEqualByteIndexNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsSuffix,
                @Cached ConditionProfile isCharacterHeadProfile) {

            var stringTString = strings.getTString(string);
            var stringEncoding = strings.getTEncoding(string);
            final int stringByteLength = stringTString.byteLength(stringEncoding);

            var suffixTString = stringsSuffix.getTString(suffix);
            var suffixEncoding = stringsSuffix.getTEncoding(suffix);
            final int suffixByteLength = suffixTString.byteLength(suffixEncoding);

            if (stringByteLength < suffixByteLength) {
                return false;
            }

            // See truffle-string.md, section Encodings Compatibility
            if (suffixByteLength == 0) {
                return true;
            }

            final int offset = stringByteLength - suffixByteLength;

            if (isCharacterHeadProfile.profile(!isCharacterHeadNode.execute(enc, stringTString, offset))) {
                return false;
            }

            return regionEqualByteIndexNode.execute(stringTString, offset, suffixTString, 0, suffixByteLength,
                    enc.tencoding);
        }

    }

    @CoreMethod(names = "count", rest = true)
    public abstract static class CountNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStr = ToStrNode.create();
        @Child private StringHelperNodes.CountRopesNode countRopesNode = StringHelperNodes.CountRopesNode.create();
        @Child private RubyStringLibrary rubyStringLibrary = RubyStringLibrary.createDispatched();
        @Child private AsTruffleStringNode asTruffleStringNode = AsTruffleStringNode.create();

        @Specialization(
                guards = "args.length == size",
                limit = "getDefaultCacheLimit()")
        protected int count(Object string, Object[] args,
                @Cached("args.length") int size) {
            final TStringWithEncoding[] ropesWithEncs = argRopesWithEncs(args, size);
            return countRopesNode.executeCount(string, ropesWithEncs);
        }

        @Specialization(replaces = "count")
        protected int countSlow(Object string, Object[] args) {
            final TStringWithEncoding[] ropesWithEncs = argRopesSlow(args);
            return countRopesNode.executeCount(string, ropesWithEncs);
        }

        @ExplodeLoop
        protected TStringWithEncoding[] argRopesWithEncs(Object[] args, int size) {
            final TStringWithEncoding[] strs = new TStringWithEncoding[args.length];
            for (int i = 0; i < size; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new TStringWithEncoding(
                        asTruffleStringNode,
                        rubyStringLibrary.getTString(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }

        protected TStringWithEncoding[] argRopesSlow(Object[] args) {
            final TStringWithEncoding[] strs = new TStringWithEncoding[args.length];
            for (int i = 0; i < args.length; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new TStringWithEncoding(
                        asTruffleStringNode,
                        rubyStringLibrary.getTString(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }
    }

    @CoreMethod(names = "delete!", rest = true, raiseIfNotMutableSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class DeleteBangNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStr = ToStrNode.create();
        @Child private StringHelperNodes.DeleteBangRopesNode deleteBangRopesNode = StringHelperNodes.DeleteBangRopesNode
                .create();
        @Child private RubyStringLibrary rubyStringLibrary = RubyStringLibrary.createDispatched();
        @Child private AsTruffleStringNode asTruffleStringNode = AsTruffleStringNode.create();

        public static DeleteBangNode create() {
            return DeleteBangNodeFactory.create(null);
        }

        public abstract Object executeDeleteBang(RubyString string, Object[] args);

        @Specialization(guards = "args.length == size", limit = "getDefaultCacheLimit()")
        protected Object deleteBang(RubyString string, Object[] args,
                @Cached("args.length") int size) {
            final TStringWithEncoding[] ropesWithEncs = argRopesWithEncs(args, size);
            return deleteBangRopesNode.executeDeleteBang(string, ropesWithEncs);
        }

        @Specialization(replaces = "deleteBang")
        protected Object deleteBangSlow(RubyString string, Object[] args) {
            final TStringWithEncoding[] ropes = argRopesWithEncsSlow(args);
            return deleteBangRopesNode.executeDeleteBang(string, ropes);
        }

        @ExplodeLoop
        protected TStringWithEncoding[] argRopesWithEncs(Object[] args, int size) {
            final TStringWithEncoding[] strs = new TStringWithEncoding[size];
            for (int i = 0; i < size; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new TStringWithEncoding(
                        asTruffleStringNode,
                        rubyStringLibrary.getTString(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }

        protected TStringWithEncoding[] argRopesWithEncsSlow(Object[] args) {
            final TStringWithEncoding[] strs = new TStringWithEncoding[args.length];
            for (int i = 0; i < args.length; i++) {
                final Object string = toStr.execute(args[i]);
                strs[i] = new TStringWithEncoding(
                        asTruffleStringNode,
                        rubyStringLibrary.getTString(string),
                        rubyStringLibrary.getEncoding(string));
            }
            return strs;
        }
    }

    @Primitive(name = "string_downcase!", raiseIfNotMutable = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringDowncaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createUpperToLower()") StringHelperNodes.InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("codeRangeNode") GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared("fromByteArrayNode") TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached @Shared("byteArrayNode") TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            var tstring = string.tstring;
            var encoding = string.encoding;

            if (dummyEncodingProfile.profile(encoding.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding.jcoding, this));
            }

            var cr = codeRangeNode.execute(string.tstring, string.getTEncoding());
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            final byte[] outputBytes = StringSupport.downcaseMultiByteAsciiSimple(encoding.jcoding, cr, byteArray);

            if (modifiedProfile.profile(byteArray.getArray() != outputBytes)) {
                string.setTString(fromByteArrayNode.execute(outputBytes, string.getTEncoding())); // cr, codePointLengthNode.execute(rope)
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = { "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object downcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("codeRangeNode") GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared("fromByteArrayNode") TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached @Shared("byteArrayNode") TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            var tstring = string.tstring;
            var encoding = string.encoding;

            if (dummyEncodingProfile.profile(encoding.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding.jcoding, this));
            }

            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);

            // TODO (nirvdrum 24-Jun-22): Make the byte array builder copy-on-write so we don't eagerly clone the source byte array.
            var builder = ByteArrayBuilder.create(byteArray);

            var cr = codeRangeNode.execute(string.tstring, string.getTEncoding());
            final boolean modified = StringSupport
                    .downcaseMultiByteComplex(encoding.jcoding, cr, builder, caseMappingOptions, this);

            if (modifiedProfile.profile(modified)) {
                string.setTString(fromByteArrayNode.execute(builder.getBytes(), string.encoding.tencoding));
                return string;
            } else {
                return nil;
            }
        }

    }

    @CoreMethod(names = "each_byte", needsBlock = true, enumeratorSize = "bytesize")
    public abstract static class EachByteNode extends YieldingCoreMethodNode {

        @Specialization(limit = "LIBSTRING_CACHE")
        protected Object eachByte(Object string, RubyProc block,
                // use separate specialization instances for getTString() in the loop
                @CachedLibrary(/* ^ */ "string") RubyStringLibrary strings,
                @Cached TruffleString.MaterializeNode materializeNode,
                @Cached TruffleString.ReadByteNode readByteNode) {
            var tstring = strings.getTString(string);
            var encoding = strings.getEncoding(string).tencoding;

            // String#each_byte reflects changes by the block to the string's bytes
            materializeNode.execute(tstring, encoding);
            for (int i = 0; i < tstring.byteLength(encoding); i++) {
                int singleByte = readByteNode.execute(tstring, i, encoding);
                callBlock(block, singleByte);

                tstring = strings.getTString(string);
                encoding = strings.getEncoding(string).tencoding;
            }

            return string;
        }

    }

    @CoreMethod(names = "each_char", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(StringGuards.class)
    public abstract static class EachCharNode extends YieldingCoreMethodNode {

        @Specialization
        protected Object eachChar(Object string, RubyProc block,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached TruffleString.SubstringByteIndexNode substringNode,
                @Cached TruffleString.ByteLengthOfCodePointNode byteLengthOfCodePointNode) {
            // Unlike String#each_byte, String#each_char does not make
            // modifications to the string visible to the rest of the iteration.
            var tstring = strings.getTString(string);
            var encoding = strings.getEncoding(string);
            final int len = tstring.byteLength(encoding.tencoding);

            int clen;
            for (int i = 0; i < len; i += clen) {
                clen = byteLengthOfCodePointNode.execute(tstring, i, encoding.tencoding);
                callBlock(block, createSubString(substringNode, tstring, encoding, i, clen));
            }

            return string;
        }

    }

    @ImportStatic(StringGuards.class)
    @CoreMethod(names = "force_encoding", required = 1, raiseIfNotMutableSelf = true)
    public abstract static class ForceEncodingNode extends CoreMethodArrayArgumentsNode {

        public abstract RubyString execute(Object string, Object other);

        protected abstract RubyString execute(Object string, RubyEncoding other);

        public static ForceEncodingNode create() {
            return StringNodesFactory.ForceEncodingNodeFactory.create(null);
        }

        @Specialization(guards = "string.encoding == newEncoding")
        protected RubyString sameEncoding(RubyString string, RubyEncoding newEncoding) {
            return string;
        }

        @Specialization(guards = { "string.encoding != newEncoding", "tstring.isImmutable()" })
        protected RubyString immutable(RubyString string, RubyEncoding newEncoding,
                @Bind("string.tstring") AbstractTruffleString tstring,
                @Cached TruffleString.ForceEncodingNode forceEncodingNode) {
            var newTString = forceEncodingNode.execute(tstring, string.encoding.tencoding, newEncoding.tencoding);
            string.setTString(newTString, newEncoding);
            return string;
        }

        @Specialization(
                guards = { "string.encoding != newEncoding", "!tstring.isImmutable()", "!tstring.isNative()" })
        protected RubyString mutableManaged(RubyString string, RubyEncoding newEncoding,
                @Bind("string.tstring") AbstractTruffleString tstring,
                @Cached MutableTruffleString.ForceEncodingNode forceEncodingNode) {
            var newTString = forceEncodingNode.execute(tstring, string.encoding.tencoding, newEncoding.tencoding);
            string.setTString(newTString, newEncoding);
            return string;
        }

        @Specialization(
                guards = { "string.encoding != newEncoding", "!tstring.isImmutable()", "tstring.isNative()" })
        protected RubyString mutableNative(RubyString string, RubyEncoding newEncoding,
                @Bind("string.tstring") AbstractTruffleString tstring,
                @Cached TruffleString.GetInternalNativePointerNode getInternalNativePointerNode,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode) {
            var currentEncoding = string.encoding.tencoding;
            var pointer = (Pointer) getInternalNativePointerNode.execute(tstring, currentEncoding);
            var byteLength = tstring.byteLength(currentEncoding);
            var newTString = fromNativePointerNode.execute(pointer, 0, byteLength, newEncoding.tencoding, false);
            string.setTString(newTString, newEncoding);
            return string;
        }

        @Specialization(guards = "libEncoding.isRubyString(newEncoding)")
        protected RubyString forceEncodingString(RubyString string, Object newEncoding,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libEncoding,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached BranchProfile errorProfile) {
            final String stringName = toJavaStringNode.executeToJavaString(newEncoding);
            final RubyEncoding rubyEncoding = getContext().getEncodingManager().getRubyEncoding(stringName);

            if (rubyEncoding == null) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(Utils.concat("unknown encoding name - ", stringName), this));
            }

            return execute(string, rubyEncoding);
        }

        @Specialization(guards = { "!isRubyEncoding(newEncoding)", "isNotRubyString(newEncoding)" })
        protected RubyString forceEncoding(RubyString string, Object newEncoding,
                @Cached ToStrNode toStrNode,
                @Cached ForceEncodingNode forceEncodingNode) {
            return forceEncodingNode.execute(string, toStrNode.execute(newEncoding));
        }
    }

    @CoreMethod(names = "getbyte", required = 1, lowerFixnum = 1)
    public abstract static class StringGetByteNode extends CoreMethodArrayArgumentsNode {

        @Child private StringHelperNodes.NormalizeIndexNode normalizeIndexNode = StringHelperNodes.NormalizeIndexNode
                .create();
        @Child private TruffleString.ReadByteNode readByteNode = TruffleString.ReadByteNode.create();

        @Specialization
        protected Object getByte(Object string, int index,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            var tstring = libString.getTString(string);
            var encoding = libString.getEncoding(string).tencoding;
            int byteLength = tstring.byteLength(encoding);

            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, byteLength);

            if (indexOutOfBoundsProfile.profile((normalizedIndex < 0) || (normalizedIndex >= byteLength))) {
                return nil;
            }

            return readByteNode.execute(tstring, normalizedIndex, encoding);
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
                @Cached StringHelperNodes.HashStringNode hash) {
            return hash.execute(string);
        }
    }

    @Primitive(name = "string_initialize")
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString initializeJavaString(RubyString string, String from, RubyEncoding encoding,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            var tstring = fromJavaStringNode.execute(from, encoding.tencoding);
            string.setTString(tstring, encoding);
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsFrom) {
            string.setTString(stringsFrom.getTString(from), stringsFrom.getEncoding(from));
            return string;
        }

        @Specialization(guards = { "isNotRubyString(from)", "!isString(from)" })
        protected RubyString initialize(VirtualFrame frame, RubyString string, Object from, Object encoding,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringLibrary,
                @Cached ToStrNode toStrNode) {
            final Object stringFrom = toStrNode.execute(from);
            string.setTString(stringLibrary.getTString(stringFrom), stringLibrary.getEncoding(stringFrom));
            return string;
        }

    }

    @Primitive(name = "string_get_coderange")
    public abstract static class GetCodeRangeAsIntNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected int getCodeRange(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached GetByteCodeRangeNode codeRangeNode) {
            final var tstring = strings.getTString(string);

            var codeRange = codeRangeNode.execute(tstring, strings.getTEncoding(string));
            if (codeRange == ASCII) {
                return 1;
            } else if (codeRange == VALID) {
                return 2;
            } else {
                assert codeRange == BROKEN;
                return 3;
            }
        }
    }

    @CoreMethod(names = "initialize_copy", required = 1, raiseIfNotMutableSelf = true)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Child private WriteObjectFieldNode writeAssociatedNode; // for synchronization

        @Specialization(guards = "areEqual(self, from)")
        protected Object initializeCopySelfIsSameAsFrom(RubyString self, Object from) {
            return self;
        }

        @Specialization(guards = {
                "stringsFrom.isRubyString(from)",
                "!areEqual(self, from)",
                "!tstring.isNative()",
                "tstring.isImmutable()" })
        protected Object initializeCopyImmutable(RubyString self, Object from,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsFrom,
                @Cached @Shared("stringGetAssociatedNode") StringHelperNodes.StringGetAssociatedNode stringGetAssociatedNode,
                @Bind("stringsFrom.getTString(from)") AbstractTruffleString tstring) {
            self.setTString(tstring, stringsFrom.getEncoding(from));

            final Object associated = stringGetAssociatedNode.execute(from);
            copyAssociated(self, associated);
            return self;
        }

        @Specialization(guards = {
                "stringsFrom.isRubyString(from)",
                "!areEqual(self, from)",
                "!tstring.isNative()",
                "tstring.isMutable()" })
        protected Object initializeCopyMutable(RubyString self, Object from,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary stringsFrom,
                @Cached @Shared("stringGetAssociatedNode") StringHelperNodes.StringGetAssociatedNode stringGetAssociatedNode,
                @Cached MutableTruffleString.SubstringByteIndexNode copyMutableTruffleStringNode,
                @Bind("stringsFrom.getTString(from)") AbstractTruffleString tstring) {
            var encoding = stringsFrom.getEncoding(from);
            var tencoding = encoding.tencoding;
            int byteLength = tstring.byteLength(tencoding);
            // TODO Should the copy be a MutableTruffleString too, or TruffleString with AsTruffleStringNode?
            MutableTruffleString copy = copyMutableTruffleStringNode.execute(tstring, 0, byteLength, tencoding);
            self.setTString(copy, encoding);

            final Object associated = stringGetAssociatedNode.execute(from);
            copyAssociated(self, associated);
            return self;
        }

        @Specialization(guards = { "!areEqual(self, from)", "tstring.isNative()" })
        protected Object initializeCopyNative(RubyString self, RubyString from,
                @Cached @Shared("stringGetAssociatedNode") StringHelperNodes.StringGetAssociatedNode stringGetAssociatedNode,
                @Cached TruffleString.GetInternalNativePointerNode getInternalNativePointerNode,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode,
                @Bind("from.tstring") AbstractTruffleString tstring) {
            var encoding = from.encoding;
            var tencoding = encoding.tencoding;
            final Pointer fromPointer = (Pointer) getInternalNativePointerNode.execute(tstring, tencoding);

            final Pointer newPointer = Pointer.mallocAutoRelease(fromPointer.getSize(), getLanguage());
            newPointer.writeBytes(0, fromPointer, 0, fromPointer.getSize());

            // TODO should we have the copy be native too, or rather take the opportunity of having to copy to be managed?
            assert tstring.isMutable();
            var copy = fromNativePointerNode.execute(newPointer, 0, tstring.byteLength(tencoding), tencoding, false);
            self.setTString(copy, encoding);

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
    }

    @CoreMethod(names = "lstrip!", raiseIfNotMutableSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class LstripBangNode extends CoreMethodArrayArgumentsNode {

        @Child StringHelperNodes.GetCodePointNode getCodePointNode = StringHelperNodes.GetCodePointNode.create();
        @Child TruffleString.SubstringByteIndexNode substringNode = TruffleString.SubstringByteIndexNode.create();

        @Specialization(guards = "isEmpty(string.tstring)")
        protected Object lstripBangEmptyString(RubyString string) {
            return nil;
        }

        @Specialization(
                guards = { "!isEmpty(string.tstring)", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object lstripBangSingleByte(RubyString string,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.MaterializeNode materializeNode,
                @Cached TruffleString.ReadByteNode readByteNode,
                @Cached ConditionProfile noopProfile) {
            // Taken from org.jruby.RubyString#lstrip_bang19 and org.jruby.RubyString#singleByteLStrip.

            var tstring = string.tstring;
            var tencoding = string.encoding.tencoding;
            materializeNode.execute(tstring, tencoding);
            int firstCodePoint = readByteNode.execute(tstring, 0, tencoding);

            // Check the first code point to see if it's a space. In the case of strings without leading spaces,
            // this check can avoid having to compile the while loop.
            if (noopProfile.profile(!StringSupport.isAsciiSpaceOrNull(firstCodePoint))) {
                return nil;
            }

            final int byteLength = tstring.byteLength(tencoding);

            int p = 1;
            while (p < byteLength && StringSupport.isAsciiSpaceOrNull(readByteNode.execute(tstring, p, tencoding))) {
                p++;
            }

            string.setTString(substringNode.execute(tstring, p, byteLength - p, tencoding, true));

            return string;
        }

        @TruffleBoundary
        @Specialization(
                guards = { "!isEmpty(string.tstring)", "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object lstripBang(RubyString string,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            // Taken from org.jruby.RubyString#lstrip_bang19 and org.jruby.RubyString#multiByteLStrip.

            var tstring = string.tstring;
            var encoding = strings.getEncoding(string);
            final RubyEncoding enc = getActualEncodingNode.execute(tstring, encoding);
            final int s = 0;
            final int end = s + tstring.byteLength(encoding.tencoding);

            int p = s;
            while (p < end) {
                int c = getCodePointNode.executeGetCodePoint(tstring, enc, p);
                if (!StringSupport.isAsciiSpaceOrNull(c)) {
                    break;
                }
                p += StringSupport.codeLength(enc.jcoding, c);
            }

            if (p > s) {
                string.setTString(substringNode.execute(tstring, p - s, end - p, enc.tencoding, true));
                return string;
            }

            return nil;
        }

    }

    @CoreMethod(names = "ord")
    @ImportStatic(StringGuards.class)
    public abstract static class OrdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isEmpty(strings.getTString(string))" })
        protected int ordEmpty(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("empty string", this));
        }

        @Specialization(guards = { "!isEmpty(strings.getTString(string))" })
        protected int ord(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached StringHelperNodes.GetCodePointNode getCodePointNode) {
            return getCodePointNode.executeGetCodePoint(strings.getTString(string), strings.getEncoding(string), 0);
        }

    }

    @Primitive(name = "string_replace", raiseIfNotMutable = 0)
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyBaseNodeWithExecute.class)
    public abstract static class ReplaceNode extends PrimitiveNode {

        @CreateCast("other")
        protected ToStrNode coerceOtherToString(RubyBaseNodeWithExecute other) {
            return ToStrNodeGen.create(other);
        }

        @Specialization(guards = "string == other")
        protected RubyString replaceStringIsSameAsOther(RubyString string, RubyString other) {
            return string;
        }

        @Specialization(guards = { "string != other" })
        protected RubyString replace(RubyString string, RubyString other,
                @Cached AsTruffleStringNode asTruffleStringNode) {
            final TruffleString immutableCopy = asTruffleStringNode.execute(other.tstring, other.encoding.tencoding);
            string.setTString(immutableCopy, other.encoding);
            return string;
        }

        @Specialization
        protected RubyString replace(RubyString string, ImmutableRubyString other) {
            string.setTString(other.tstring, other.encoding);
            return string;
        }

    }

    @CoreMethod(names = "rstrip!", raiseIfNotMutableSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class RstripBangNode extends CoreMethodArrayArgumentsNode {

        @Child StringHelperNodes.GetCodePointNode getCodePointNode = StringHelperNodes.GetCodePointNode.create();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();
        @Child TruffleString.SubstringByteIndexNode substringNode = TruffleString.SubstringByteIndexNode.create();

        @Specialization(guards = "isEmpty(string.tstring)")
        protected Object rstripBangEmptyString(RubyString string) {
            return nil;
        }

        @Specialization(
                guards = { "!isEmpty(string.tstring)", "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object rstripBangSingleByte(RubyString string,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Exclusive ConditionProfile noopProfile) {
            // Taken from org.jruby.RubyString#rstrip_bang19 and org.jruby.RubyString#singleByteRStrip19.

            var tstring = string.tstring;
            var encoding = string.encoding;
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            final int lastCodePoint = getCodePointNode.executeGetCodePoint(tstring, encoding,
                    byteArray.getLength() - 1);

            // Check the last code point to see if it's a space or NULL. In the case of strings without leading spaces,
            // this check can avoid having to materialize the entire byte[] (a potentially expensive operation
            // for ropes) and can avoid having to compile the while loop.
            final boolean willStrip = StringSupport.isAsciiSpaceOrNull(lastCodePoint);
            if (noopProfile.profile(!willStrip)) {
                return nil;
            }

            final int end = byteArray.getLength();

            int endp = end - 1;
            while (endp >= 0 && StringSupport.isAsciiSpaceOrNull(byteArray.get(endp))) {
                endp--;
            }

            string.setTString(substringNode.execute(tstring, 0, endp + 1, encoding.tencoding, true));
            return string;
        }

        @TruffleBoundary
        @Specialization(
                guards = { "!isEmpty(string.tstring)", "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected Object rstripBang(RubyString string,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Exclusive ConditionProfile dummyEncodingProfile) {
            // Taken from org.jruby.RubyString#rstrip_bang19 and org.jruby.RubyString#multiByteRStrip19.

            var tstring = string.tstring;
            var encoding = string.encoding;
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            final RubyEncoding enc = getActualEncodingNode.execute(tstring, encoding);

            if (dummyEncodingProfile.profile(enc.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc.jcoding, this));
            }

            final byte[] bytes = byteArray.getArray();
            final int start = byteArray.getOffset();
            final int end = byteArray.getEnd();

            int endp = end;
            int prev;
            while ((prev = prevCharHead(enc.jcoding, bytes, start, endp, end)) != -1) {
                int point = getCodePointNode.executeGetCodePoint(tstring, enc, prev);
                if (!StringSupport.isAsciiSpaceOrNull(point)) {
                    break;
                }
                endp = prev;
            }

            if (endp < end) {
                string.setTString(substringNode.execute(tstring, 0, endp - start, enc.tencoding, true));
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
        @Child GetByteCodeRangeNode codeRangeNode = GetByteCodeRangeNode.create();
        @Child private TruffleString.ConcatNode concatNode = TruffleString.ConcatNode.create();
        @Child private TruffleString.GetInternalByteArrayNode byteArrayNode = TruffleString.GetInternalByteArrayNode
                .create();
        @Child TruffleString.SubstringByteIndexNode substringNode = TruffleString.SubstringByteIndexNode.create();

        @Specialization(
                guards = { "isBrokenCodeRange(tstring, encoding, codeRangeNode)", "isAsciiCompatible(encoding)" })
        protected RubyString scrubAsciiCompat(Object string, RubyProc block,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached TruffleString.ByteLengthOfCodePointNode byteLengthOfCodePointNode,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            final Encoding enc = encoding.jcoding;
            var tencoding = encoding.tencoding;
            TruffleString buf = EMPTY_BINARY;

            var byteArray = byteArrayNode.execute(tstring, tencoding);
            final int e = tstring.byteLength(tencoding);

            int p = 0;
            int p1 = p;

            p = StringSupport.searchNonAscii(byteArray, p);
            if (p < 0) {
                p = e;
            }
            while (p < e) {
                int clen = byteLengthOfCodePointNode.execute(tstring, p, tencoding,
                        ErrorHandling.RETURN_NEGATIVE);
                if (MBCLEN_NEEDMORE_P(clen)) {
                    break;
                } else if (MBCLEN_CHARFOUND_P(clen)) {
                    p += MBCLEN_CHARFOUND_LEN(clen);
                } else if (MBCLEN_INVALID_P(clen)) {
                    // p1~p: valid ascii/multibyte chars
                    // p ~e: invalid bytes + unknown bytes
                    clen = enc.maxLength();
                    if (p > p1) {
                        buf = concatNode.execute(buf,
                                substringNode.execute(tstring, p1, p - p1, tencoding, true),
                                tencoding, true);
                    }

                    if (e - p < clen) {
                        clen = e - p;
                    }
                    if (clen <= 2) {
                        clen = 1;
                    } else {
                        clen--;
                        for (; clen > 1; clen--) {
                            var subTString = substringNode.execute(tstring, p, clen, tencoding, true);
                            int clen2 = byteLengthOfCodePointNode.execute(subTString, 0, tencoding,
                                    ErrorHandling.RETURN_NEGATIVE);
                            if (MBCLEN_NEEDMORE_P(clen2)) {
                                break;
                            }
                        }
                    }
                    Object repl = yieldNode.yield(block,
                            createSubString(substringNode, tstring, encoding, p, clen));
                    buf = concatNode.execute(buf, strings.getTString(repl), tencoding, true);
                    p += clen;
                    p1 = p;
                    p = StringSupport.searchNonAscii(byteArray, p);
                    if (p < 0) {
                        p = e;
                        break;
                    }
                }
            }

            if (p1 < p) {
                buf = concatNode.execute(buf,
                        substringNode.execute(tstring, p1, p - p1, tencoding, true), tencoding,
                        true);
            }

            if (p < e) {
                Object repl = yieldNode.yield(block,
                        createSubString(substringNode, tstring, encoding, p, e - p));
                buf = concatNode.execute(buf, strings.getTString(repl), tencoding, true);
            }

            return createString(buf, encoding);
        }

        @Specialization(
                guards = {
                        "isBrokenCodeRange(tstring, encoding, codeRangeNode)",
                        "!isAsciiCompatible(encoding)" })
        protected RubyString scrubAsciiIncompatible(Object string, RubyProc block,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached TruffleString.ByteLengthOfCodePointNode byteLengthOfCodePointNode,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            final Encoding enc = encoding.jcoding;
            var tencoding = encoding.tencoding;
            TruffleString buf = EMPTY_BINARY;

            final int e = tstring.byteLength(tencoding);
            int p = 0;
            int p1 = p;
            final int mbminlen = enc.minLength();

            // TODO use logical indices
            while (p < e) {
                int clen = byteLengthOfCodePointNode.execute(tstring, p, tencoding, ErrorHandling.RETURN_NEGATIVE);
                if (MBCLEN_NEEDMORE_P(clen)) {
                    break;
                } else if (MBCLEN_CHARFOUND_P(clen)) {
                    p += MBCLEN_CHARFOUND_LEN(clen);
                } else if (MBCLEN_INVALID_P(clen)) {
                    final int q = p;
                    clen = enc.maxLength();

                    if (p > p1) {
                        buf = concatNode.execute(buf,
                                substringNode.execute(tstring, p1, p - p1, tencoding, true),
                                tencoding, true);
                    }

                    if (e - p < clen) {
                        clen = e - p;
                    }
                    if (clen <= mbminlen * 2) {
                        clen = mbminlen;
                    } else {
                        clen -= mbminlen;
                        for (; clen > mbminlen; clen -= mbminlen) {
                            var subTString = substringNode.execute(tstring, q, clen, tencoding, true);
                            int clen2 = byteLengthOfCodePointNode.execute(subTString, 0, tencoding,
                                    ErrorHandling.RETURN_NEGATIVE);
                            if (MBCLEN_NEEDMORE_P(clen2)) {
                                break;
                            }
                        }
                    }

                    RubyString repl = (RubyString) yieldNode.yield(block,
                            createSubString(substringNode, tstring, encoding, p, clen));
                    buf = concatNode.execute(buf, repl.tstring, tencoding, true);
                    p += clen;
                    p1 = p;
                }
            }

            if (p1 < p) {
                buf = concatNode.execute(buf, substringNode.execute(tstring, p1, p - p1, tencoding, true),
                        tencoding,
                        true);
            }

            if (p < e) {
                RubyString repl = (RubyString) yieldNode.yield(block,
                        createSubString(substringNode, tstring, encoding, p, e - p));
                buf = concatNode.execute(buf, repl.tstring, tencoding, true);
            }

            return createString(buf, encoding);
        }

    }

    @Primitive(name = "string_swapcase!", raiseIfNotMutable = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringSwapcaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object swapcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createSwapCase()") StringHelperNodes.InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object swapcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("codeRangeNode") GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared("fromByteArrayNode") TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached @Shared("byteArrayNode") TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            // Taken from org.jruby.RubyString#swapcase_bang19.

            var tstring = string.tstring;
            var enc = string.encoding;

            if (dummyEncodingProfile.profile(enc.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc.jcoding, this));
            }

            var cr = codeRangeNode.execute(string.tstring, string.getTEncoding());
            var byteArray = byteArrayNode.execute(tstring, enc.tencoding);
            final byte[] outputBytes = StringSupport.swapcaseMultiByteAsciiSimple(enc.jcoding, cr, byteArray);

            if (modifiedProfile.profile(byteArray.getArray() != outputBytes)) {
                string.setTString(fromByteArrayNode.execute(outputBytes, string.getTEncoding())); // cr, codePointLengthNode.execute(rope)
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object swapcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("codeRangeNode") GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared("fromByteArrayNode") TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached @Shared("byteArrayNode") TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            // Taken from org.jruby.RubyString#swapcase_bang19.

            var tstring = string.tstring;
            var enc = string.encoding;

            if (dummyEncodingProfile.profile(enc.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(enc.jcoding, this));
            }

            var byteArray = byteArrayNode.execute(tstring, enc.tencoding);

            // TODO (nirvdrum 24-Jun-22): Make the byte array builder copy-on-write so we don't eagerly clone the source byte array.
            var builder = ByteArrayBuilder.create(byteArray);

            var cr = codeRangeNode.execute(string.tstring, string.getTEncoding());
            final boolean modified = StringSupport
                    .swapCaseMultiByteComplex(enc.jcoding, cr, builder, caseMappingOptions, this);

            if (modifiedProfile.profile(modified)) {
                string.setTString(fromByteArrayNode.execute(builder.getBytes(), string.encoding.tencoding));
                return string;
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = "dump")
    @ImportStatic(StringGuards.class)
    public abstract static class DumpNode extends CoreMethodArrayArgumentsNode {

        private static final byte[] FORCE_ENCODING_CALL_BYTES = StringOperations.encodeAsciiBytes(".force_encoding(\"");

        @TruffleBoundary
        @Specialization(guards = "isAsciiCompatible(libString.getEncoding(string))")
        protected RubyString dumpAsciiCompatible(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            ByteArrayBuilder outputBytes = dumpCommon(new ATStringWithEncoding(libString, string));

            return createString(fromByteArrayNode, outputBytes.getBytes(), libString.getEncoding(string));
        }

        @TruffleBoundary
        @Specialization(guards = "!isAsciiCompatible(libString.getEncoding(string))")
        protected RubyString dump(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            ByteArrayBuilder outputBytes = dumpCommon(new ATStringWithEncoding(libString, string));

            outputBytes.append(FORCE_ENCODING_CALL_BYTES);
            outputBytes.append(libString.getEncoding(string).jcoding.getName());
            outputBytes.append((byte) '"');
            outputBytes.append((byte) ')');

            return createString(fromByteArrayNode, outputBytes.getBytes(), Encodings.BINARY);
        }

        // Taken from org.jruby.RubyString#dump
        private ByteArrayBuilder dumpCommon(ATStringWithEncoding string) {
            ByteArrayBuilder buf = null;
            final var enc = string.encoding.jcoding;
            final var cr = string.getCodeRange();

            var byteArray = string.getInternalByteArray();
            int p = byteArray.getOffset();
            int end = byteArray.getEnd();
            byte[] bytes = byteArray.getArray();

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
                        len += p < end && isEVStr(bytes[p] & 0xff) ? 2 : 1;
                        break;
                    default:
                        if (ASCIIEncoding.INSTANCE.isPrint(c)) {
                            len++;
                        } else {
                            if (enc.isUTF8()) {
                                int n = StringSupport.characterLength(enc, cr, bytes, p - 1, end) - 1;
                                if (n > 0) {
                                    if (buf == null) {
                                        buf = new ByteArrayBuilder();
                                    }
                                    int cc = StringSupport.codePoint(enc, cr, bytes, p - 1, end, this);
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
                len += FORCE_ENCODING_CALL_BYTES.length + enc.getName().length + "\")".length();
            }

            TStringBuilder outBytes = new TStringBuilder();
            outBytes.unsafeEnsureSpace(len);
            byte[] out = outBytes.getUnsafeBytes();
            int q = 0;
            p = byteArray.getOffset();
            end = byteArray.getEnd();

            out[q++] = '"';
            while (p < end) {
                int c = bytes[p++] & 0xff;
                if (c == '"' || c == '\\') {
                    out[q++] = '\\';
                    out[q++] = (byte) c;
                } else if (c == '#') {
                    if (p < end && isEVStr(bytes[p] & 0xff)) {
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

        private static boolean isEVStr(int c) {
            return c == '$' || c == '@' || c == '{';
        }

    }

    @CoreMethod(names = "undump")
    @ImportStatic(StringGuards.class)
    public abstract static class UndumpNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isAsciiCompatible(libString.getEncoding(string))")
        protected RubyString undumpAsciiCompatible(Object string,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            // Taken from org.jruby.RubyString#undump
            var encoding = libString.getEncoding(string);
            Pair<TStringBuilder, RubyEncoding> outputBytesResult = StringSupport.undump(
                    new ATStringWithEncoding(libString.getTString(string), encoding),
                    encoding,
                    getContext(),
                    this);
            final RubyEncoding rubyEncoding = outputBytesResult.getRight();
            return createString(outputBytesResult.getLeft().toTStringUnsafe(fromByteArrayNode), rubyEncoding);
        }

        @Specialization(guards = "!isAsciiCompatible(libString.getEncoding(string))")
        protected RubyString undumpNonAsciiCompatible(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().encodingCompatibilityError(
                            Utils.concat("ASCII incompatible encoding: ", libString.getEncoding(string)),
                            this));
        }
    }

    @CoreMethod(names = "setbyte", required = 2, raiseIfNotMutableSelf = true, lowerFixnum = { 1, 2 })
    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "index", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "value", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(StringGuards.class)
    public abstract static class SetByteNode extends CoreMethodNode {

        @Child private StringHelperNodes.CheckIndexNode checkIndexNode = StringHelperNodesFactory.CheckIndexNodeGen
                .create();

        @CreateCast("index")
        protected ToIntNode coerceIndexToInt(RubyBaseNodeWithExecute index) {
            return ToIntNode.create(index);
        }

        @CreateCast("value")
        protected ToIntNode coerceValueToInt(RubyBaseNodeWithExecute value) {
            return ToIntNode.create(value);
        }

        @Specialization(guards = "tstring.isMutable()")
        protected int mutable(RubyString string, int index, int value,
                @Bind("string.tstring") AbstractTruffleString tstring,
                @Cached MutableTruffleString.WriteByteNode writeByteNode) {
            var tencoding = string.encoding.tencoding;
            final int normalizedIndex = checkIndexNode.executeCheck(index, tstring.byteLength(tencoding));

            writeByteNode.execute((MutableTruffleString) tstring, normalizedIndex, (byte) value, tencoding);
            return value;
        }

        @Specialization(guards = "!tstring.isMutable()")
        protected int immutable(RubyString string, int index, int value,
                @Bind("string.tstring") AbstractTruffleString tstring,
                @Cached MutableTruffleString.AsMutableTruffleStringNode asMutableTruffleStringNode,
                @Cached MutableTruffleString.WriteByteNode writeByteNode) {
            var tencoding = string.encoding.tencoding;
            final int normalizedIndex = checkIndexNode.executeCheck(index, tstring.byteLength(tencoding));

            MutableTruffleString mutableTString = asMutableTruffleStringNode.execute(tstring, tencoding);
            writeByteNode.execute(mutableTString, normalizedIndex, (byte) value, tencoding);
            string.setTString(mutableTString);
            return value;
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached CodePointLengthNode codePointLengthNode) {
            return codePointLengthNode.execute(libString.getTString(string), libString.getTEncoding(string));
        }

    }

    @CoreMethod(names = "squeeze!", rest = true, raiseIfNotMutableSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class SqueezeBangNode extends CoreMethodArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode;
        private final ConditionProfile singleByteOptimizableProfile = ConditionProfile.create();

        @Specialization(guards = "isEmpty(string.tstring)")
        protected Object squeezeBangEmptyString(RubyString string, Object[] args) {
            return nil;
        }

        @TruffleBoundary
        @Specialization(guards = { "!isEmpty(string.tstring)", "noArguments(args)" })
        protected Object squeezeBangZeroArgs(RubyString string, Object[] args,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            // Taken from org.jruby.RubyString#squeeze_bang19.

            final TStringBuilder buffer = TStringBuilder.create(string);

            final boolean[] squeeze = new boolean[StringSupport.TRANS_SIZE];
            for (int i = 0; i < StringSupport.TRANS_SIZE; i++) {
                squeeze[i] = true;
            }

            if (singleByteOptimizableProfile
                    .profile(StringGuards.isSingleByteOptimizable(string, singleByteOptimizableNode))) {
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    string.setTString(buffer.toTString(), buffer.getRubyEncoding());
                }
            } else {
                var codeRange = string.tstring.getByteCodeRangeUncached(string.getTEncoding());
                if (!StringSupport.multiByteSqueeze(buffer, codeRange, squeeze, null, string.encoding.jcoding, false,
                        this)) {
                    return nil;
                } else {
                    string.setTString(buffer.toTString(), buffer.getRubyEncoding());
                }
            }

            return string;
        }

        @Specialization(guards = { "!isEmpty(string.tstring)", "!noArguments(args)" })
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

            final TStringBuilder buffer = TStringBuilder.create(string);

            Object otherStr = otherStrings[0];
            var otherRope = RubyStringLibrary.getUncached().getTString(otherStr);
            var otherEncoding = RubyStringLibrary.getUncached().getEncoding(otherStr);
            RubyEncoding enc = checkEncodingNode.executeCheckEncoding(string, otherStr);
            final boolean squeeze[] = new boolean[StringSupport.TRANS_SIZE + 1];

            boolean singlebyte = TStringUtils.isSingleByteOptimizable(string.tstring, string.encoding) &&
                    TStringUtils.isSingleByteOptimizable(otherRope, otherEncoding);

            if (singlebyte && otherRope.byteLength(otherEncoding.tencoding) == 1 && otherStrings.length == 1) {
                squeeze[otherRope.readByteUncached(0, otherEncoding.tencoding)] = true;
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    string.setTString(buffer.toTString(), buffer.getRubyEncoding());
                    return string;
                }
            }

            StringSupport.TrTables tables = StringSupport
                    .trSetupTable(otherRope, otherEncoding, squeeze, null, true, enc.jcoding, this);

            for (int i = 1; i < otherStrings.length; i++) {
                otherStr = otherStrings[i];
                otherRope = RubyStringLibrary.getUncached().getTString(otherStr);
                otherEncoding = RubyStringLibrary.getUncached().getEncoding(otherStr);
                enc = checkEncodingNode.executeCheckEncoding(string, otherStr);
                singlebyte = singlebyte && TStringUtils.isSingleByteOptimizable(otherRope, otherEncoding);
                tables = StringSupport.trSetupTable(otherRope, otherEncoding, squeeze, tables, false, enc.jcoding,
                        this);
            }

            if (singleByteOptimizableProfile.profile(singlebyte)) {
                if (!StringSupport.singleByteSqueeze(buffer, squeeze)) {
                    return nil;
                } else {
                    string.setTString(buffer.toTString(), buffer.getRubyEncoding());
                }
            } else {
                var codeRange = string.tstring.getByteCodeRangeUncached(string.getTEncoding());
                if (!StringSupport.multiByteSqueeze(buffer, codeRange, squeeze, tables, enc.jcoding, true, this)) {
                    return nil;
                } else {
                    string.setTString(buffer.toTString(), buffer.getRubyEncoding());
                }
            }

            return string;
        }

    }

    @CoreMethod(names = "succ!", raiseIfNotMutableSelf = true)
    public abstract static class SuccBangNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyString succBang(RubyString string,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            if (!string.tstring.isEmpty()) {
                final TStringBuilder succBuilder = StringSupport.succCommon(string, this);
                string.setTString(fromByteArrayNode.execute(succBuilder.getBytes(), string.encoding.tencoding));
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
        @Child private TruffleString.GetInternalByteArrayNode byteArrayNode = TruffleString.GetInternalByteArrayNode
                .create();

        @Specialization
        protected Object sum(Object string, long bits,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            // Copied from JRuby

            var tstring = strings.getTString(string);
            var encoding = strings.getEncoding(string).tencoding;
            var byteArray = byteArrayNode.execute(tstring, encoding);

            var bytes = byteArray.getArray();
            int p = byteArray.getOffset();
            final int end = byteArray.getEnd();

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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            try {
                return convertToDouble(strings.getTString(string), strings.getEncoding(string));
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        @TruffleBoundary
        private double convertToDouble(AbstractTruffleString rope, RubyEncoding encoding) {
            return new DoubleConverter().parse(rope, encoding, false, true);
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
        protected RubyString toSOnSubclass(RubyString string,
                @Cached AsTruffleStringNode asTruffleStringNode) {
            return createStringCopy(asTruffleStringNode, string.tstring, string.encoding);
        }

        public boolean isStringSubclass(RubyString string) {
            return string.getLogicalClass() != coreLibrary().stringClass;
        }
    }

    @CoreMethod(names = { "to_sym", "intern" })
    @ImportStatic(StringGuards.class)
    public abstract static class ToSymNode extends CoreMethodArrayArgumentsNode {

        @Child GetByteCodeRangeNode codeRangeNode = GetByteCodeRangeNode.create();

        @Specialization(
                guards = {
                        "!isBrokenCodeRange(tstring, encoding, codeRangeNode)",
                        "equalNode.execute(tstring, encoding, cachedTString, cachedEncoding)" },
                limit = "getDefaultCacheLimit()")
        protected RubySymbol toSymCached(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached("asTruffleStringUncached(string)") TruffleString cachedTString,
                @Cached("strings.getEncoding(string)") RubyEncoding cachedEncoding,
                @Cached("getSymbol(cachedTString, cachedEncoding)") RubySymbol cachedSymbol,
                @Cached StringHelperNodes.EqualSameEncodingNode equalNode,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            return cachedSymbol;
        }

        @Specialization(guards = "!isBrokenCodeRange(tstring, encoding, codeRangeNode)", replaces = "toSymCached")
        protected RubySymbol toSym(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            return getSymbol(strings.getTString(string), strings.getEncoding(string));
        }

        @Specialization(guards = "isBrokenCodeRange(tstring, encoding, codeRangeNode)")
        protected RubySymbol toSymBroken(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            throw new RaiseException(getContext(), coreExceptions().encodingError("invalid encoding symbol", this));
        }
    }

    @CoreMethod(names = "reverse!", raiseIfNotMutableSelf = true)
    @ImportStatic(StringGuards.class)
    public abstract static class ReverseBangNode extends CoreMethodArrayArgumentsNode {

        @Child CodePointLengthNode codePointLengthNode = CodePointLengthNode.create();
        @Child private TruffleString.FromByteArrayNode fromByteArrayNode = TruffleString.FromByteArrayNode.create();

        @Specialization(guards = "reverseIsEqualToSelf(string, codePointLengthNode)")
        protected RubyString reverseNoOp(RubyString string) {
            return string;
        }

        @Specialization(
                guards = {
                        "!reverseIsEqualToSelf(string, codePointLengthNode)",
                        "isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected RubyString reverseSingleByteOptimizable(RubyString string,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode) {
            var encoding = string.encoding.tencoding;
            var byteArray = byteArrayNode.execute(string.tstring, encoding);

            final int len = byteArray.getLength();
            final byte[] reversedBytes = new byte[len];

            for (int i = 0; i < len; i++) {
                reversedBytes[len - i - 1] = byteArray.get(i);
            }

            string.setTString(fromByteArrayNode.execute(reversedBytes, encoding)); // codeRangeNode.execute(rope), codePointLengthNode.execute(rope)
            return string;
        }

        @Specialization(
                guards = {
                        "!reverseIsEqualToSelf(string, codePointLengthNode)",
                        "!isSingleByteOptimizable(string, singleByteOptimizableNode)" })
        protected RubyString reverse(RubyString string,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.ByteLengthOfCodePointNode byteLengthOfCodePointNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode) {
            // Taken from org.jruby.RubyString#reverse!

            var tstring = string.tstring;
            var encoding = string.encoding.tencoding;
            var byteArray = byteArrayNode.execute(tstring, encoding);

            var originalBytes = byteArray.getArray();
            int byteOffset = byteArray.getOffset();
            int p = byteOffset;
            final int len = byteArray.getLength();

            final int end = p + len;
            int op = len;
            final byte[] reversedBytes = new byte[len];

            while (p < end) {
                int cl = byteLengthOfCodePointNode.execute(tstring, p - byteOffset, encoding);
                if (cl > 1 || (originalBytes[p] & 0x80) != 0) {
                    op -= cl;
                    System.arraycopy(originalBytes, p, reversedBytes, op, cl);
                    p += cl;
                } else {
                    reversedBytes[--op] = originalBytes[p++];
                }
            }

            string.setTString(fromByteArrayNode.execute(reversedBytes, encoding)); // codeRangeNode.execute(rope), codePointLengthNode.execute(rope)
            return string;
        }

        public static boolean reverseIsEqualToSelf(RubyString string,
                CodePointLengthNode codePointLengthNode) {
            return codePointLengthNode.execute(string.tstring, string.encoding.tencoding) <= 1;
        }
    }

    @CoreMethod(names = "tr!", required = 2, raiseIfNotMutableSelf = true)
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

        @Specialization(guards = "isEmpty(self.tstring)")
        protected Object trBangSelfEmpty(RubyString self, Object fromStr, Object toStr) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmpty(self.tstring)",
                        "isEmpty(libToStr.getTString(toStr))" })
        protected Object trBangToEmpty(RubyString self, Object fromStr, Object toStr,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libToStr) {
            if (deleteBangNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                deleteBangNode = insert(DeleteBangNode.create());
            }

            return deleteBangNode.executeDeleteBang(self, new Object[]{ fromStr });
        }

        @Specialization(
                guards = {
                        "libFromStr.isRubyString(fromStr)",
                        "!isEmpty(self.tstring)",
                        "!isEmpty(libToStr.getTString(toStr))" })
        protected Object trBangNoEmpty(RubyString self, Object fromStr, Object toStr,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFromStr,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libToStr) {
            if (checkEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                checkEncodingNode = insert(CheckEncodingNode.create());
            }

            return StringHelperNodes.trTransHelper(checkEncodingNode, self, libFromStr, fromStr, libToStr, toStr, false,
                    this);
        }
    }

    @CoreMethod(names = "tr_s!", required = 2, raiseIfNotMutableSelf = true)
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
                guards = { "isEmpty(self.tstring)" })
        protected Object trSBangEmpty(RubyString self, Object fromStr, Object toStr) {
            return nil;
        }

        @Specialization(
                guards = {
                        "libFromStr.isRubyString(fromStr)",
                        "libToStr.isRubyString(toStr)",
                        "!isEmpty(self.tstring)" })
        protected Object trSBang(RubyString self, Object fromStr, Object toStr,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFromStr,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libToStr) {
            if (libToStr.getTString(toStr).isEmpty()) {
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

            return StringHelperNodes.trTransHelper(checkEncodingNode, self, libFromStr, fromStr, libToStr, toStr, true,
                    this);
        }
    }

    @NodeChild(value = "string", type = RubyNode.class)
    @NodeChild(value = "format", type = RubyBaseNodeWithExecute.class)
    @CoreMethod(names = "unpack", required = 1)
    @ReportPolymorphism
    public abstract static class UnpackNode extends CoreMethodNode {

        private final BranchProfile exceptionProfile = BranchProfile.create();

        @CreateCast("format")
        protected ToStrNode coerceFormat(RubyBaseNodeWithExecute format) {
            return ToStrNodeGen.create(format);
        }

        @Specialization(guards = { "equalNode.execute(libFormat, format, cachedFormat, cachedEncoding)" })
        protected RubyArray unpackCached(Object string, Object format,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFormat,
                @Cached("asTruffleStringUncached(format)") TruffleString cachedFormat,
                @Cached("libFormat.getEncoding(format)") RubyEncoding cachedEncoding,
                @Cached("create(compileFormat(getJavaString(format)))") DirectCallNode callUnpackNode,
                @Cached StringHelperNodes.EqualNode equalNode,
                @Cached StringHelperNodes.StringGetAssociatedNode stringGetAssociatedNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode) {
            var byteArray = byteArrayNode.execute(libString.getTString(string), libString.getTEncoding(string));

            final ArrayResult result;

            try {
                result = (ArrayResult) callUnpackNode.call(
                        new Object[]{
                                byteArray.getArray(),
                                byteArray.getEnd(),
                                byteArray.getOffset(),
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFormat,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached IndirectCallNode callUnpackNode,
                @Cached StringHelperNodes.StringGetAssociatedNode stringGetAssociatedNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode) {
            var byteArray = byteArrayNode.execute(libString.getTString(string), libString.getTEncoding(string));

            final ArrayResult result;

            try {
                result = (ArrayResult) callUnpackNode.call(
                        compileFormat(toJavaStringNode.executeToJavaString(format)),
                        new Object[]{
                                byteArray.getArray(),
                                byteArray.getEnd(),
                                byteArray.getOffset(),
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
        protected RootCallTarget compileFormat(String format) {
            try {
                return new UnpackCompiler(getLanguage(), this).compile(format);
            } catch (DeferredRaiseException dre) {
                throw dre.getException(getContext());
            }
        }

        protected int getCacheLimit() {
            return getLanguage().options.UNPACK_CACHE;
        }

    }

    @Primitive(name = "string_upcase!", raiseIfNotMutable = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringUpcaseBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @Specialization(guards = { "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createLowerToUpper()") StringHelperNodes.InvertAsciiCaseNode invertAsciiCaseNode) {
            return invertAsciiCaseNode.executeInvert(string);
        }

        @Specialization(guards = { "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("codeRangeNode") GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared("fromByteArrayNode") TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached @Shared("byteArrayNode") TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            var tstring = string.tstring;
            var encoding = string.encoding;

            if (dummyEncodingProfile.profile(encoding.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding.jcoding, this));
            }

            var cr = codeRangeNode.execute(string.tstring, string.getTEncoding());
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            final byte[] outputBytes = StringSupport.upcaseMultiByteAsciiSimple(encoding, cr, byteArray);

            if (modifiedProfile.profile(byteArray.getArray() != outputBytes)) {
                string.setTString(fromByteArrayNode.execute(outputBytes, string.encoding.tencoding)); // cr, codePointLengthNode.execute(rope)
                return string;
            } else {
                return nil;
            }
        }

        @Specialization(guards = { "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)" })
        protected Object upcaseMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("codeRangeNode") GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared("fromByteArrayNode") TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached @Shared("byteArrayNode") TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared("dummyEncodingProfile") ConditionProfile dummyEncodingProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            var tstring = string.tstring;
            var encoding = string.encoding;

            if (dummyEncodingProfile.profile(encoding.jcoding.isDummy())) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding.jcoding, this));
            }

            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);

            // TODO (nirvdrum 24-Jun-22): Make the byte array builder copy-on-write so we don't eagerly clone the source byte array.
            var builder = ByteArrayBuilder.create(byteArray);

            final boolean modified = StringSupport
                    .upcaseMultiByteComplex(encoding.jcoding,
                            codeRangeNode.execute(string.tstring, string.getTEncoding()),
                            builder, caseMappingOptions, this);
            if (modifiedProfile.profile(modified)) {
                string.setTString(fromByteArrayNode.execute(builder.getBytes(), string.getTEncoding()));
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.IsValidNode isValidNode) {
            return isValidNode.execute(libString.getTString(string), libString.getTEncoding(string));
        }

    }

    @Primitive(name = "string_capitalize!", raiseIfNotMutable = 0, lowerFixnum = 1)
    @ImportStatic({ StringGuards.class, Config.class })
    public abstract static class StringCapitalizeBangPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private GetByteCodeRangeNode codeRangeNode;
        @Child private TruffleString.GetInternalByteArrayNode byteArrayNode = TruffleString.GetInternalByteArrayNode
                .create();
        @Child private TruffleString.CopyToByteArrayNode copyToByteArrayNode;
        @Child private TruffleString.FromByteArrayNode fromByteArrayNode;
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @Specialization(guards = "isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeSingleByte(RubyString string, int caseMappingOptions,
                @Cached("createUpperToLower()") StringHelperNodes.InvertAsciiCaseBytesNode invertAsciiCaseNode,
                @Cached @Shared("emptyStringProfile") ConditionProfile emptyStringProfile,
                @Cached @Exclusive ConditionProfile firstCharIsLowerProfile,
                @Cached @Exclusive ConditionProfile otherCharsAlreadyLowerProfile,
                @Cached @Exclusive ConditionProfile mustCapitalizeFirstCharProfile) {
            var tstring = string.tstring;
            var encoding = string.encoding.tencoding;

            if (emptyStringProfile.profile(tstring.isEmpty())) {
                return nil;
            }

            var byteArray = byteArrayNode.execute(tstring, encoding);
            final byte[] sourceBytes = byteArray.getArray();
            final byte[] finalBytes;

            final byte[] processedBytes = invertAsciiCaseNode.executeInvert(string, 1);

            if (otherCharsAlreadyLowerProfile.profile(processedBytes == null)) {
                // Bytes 1..N are either not letters or already lowercased. Time to check the first byte.

                if (firstCharIsLowerProfile.profile(StringSupport.isAsciiLowercase(sourceBytes[0]))) {
                    // The first char requires capitalization, but the remaining bytes in the original string are
                    // already properly cased.
                    finalBytes = copyByteArray(tstring, encoding);
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

            string.setTString(makeTString(finalBytes, encoding));

            return string;
        }

        @Specialization(guards = "isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeMultiByteAsciiSimple(RubyString string, int caseMappingOptions,
                @Cached @Shared("dummyEncodingProfile") BranchProfile dummyEncodingProfile,
                @Cached @Shared("emptyStringProfile") ConditionProfile emptyStringProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            // Taken from org.jruby.RubyString#capitalize_bang19.
            var tstring = string.tstring;
            var encoding = string.encoding;

            if (encoding.jcoding.isDummy()) {
                dummyEncodingProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding.jcoding, this));
            }

            if (emptyStringProfile.profile(tstring.isEmpty())) {
                return nil;
            }

            var cr = getCodeRange(tstring, encoding.tencoding);
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            final byte[] outputBytes = StringSupport.capitalizeMultiByteAsciiSimple(encoding.jcoding, cr, byteArray);

            if (modifiedProfile.profile(byteArray.getArray() != outputBytes)) {
                string.setTString(
                        makeTString(outputBytes, encoding.tencoding, 0, byteArray.getLength()));
                return string;
            }

            return nil;
        }

        @Specialization(guards = "isComplexCaseMapping(string, caseMappingOptions, singleByteOptimizableNode)")
        protected Object capitalizeMultiByteComplex(RubyString string, int caseMappingOptions,
                @Cached @Shared("dummyEncodingProfile") BranchProfile dummyEncodingProfile,
                @Cached @Shared("emptyStringProfile") ConditionProfile emptyStringProfile,
                @Cached @Shared("modifiedProfile") ConditionProfile modifiedProfile) {
            var tstring = string.tstring;
            var encoding = string.encoding;

            if (encoding.jcoding.isDummy()) {
                dummyEncodingProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatibleWithOperation(encoding.jcoding, this));
            }

            if (emptyStringProfile.profile(tstring.isEmpty())) {
                return nil;
            }

            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);

            // TODO (nirvdrum 26-May-22): Make the byte array builder copy-on-write so we don't eagerly clone the source byte array.
            var builder = ByteArrayBuilder.create(byteArray);

            var cr = getCodeRange(tstring, encoding.tencoding);
            final boolean modified = StringSupport
                    .capitalizeMultiByteComplex(encoding.jcoding, cr, builder, caseMappingOptions, this);

            if (modifiedProfile.profile(modified)) {
                string.setTString(makeTString(builder.getUnsafeBytes(), encoding.tencoding));
                return string;
            } else {
                return nil;
            }
        }

        private byte[] copyByteArray(AbstractTruffleString string, TruffleString.Encoding encoding) {
            if (copyToByteArrayNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                copyToByteArrayNode = insert(TruffleString.CopyToByteArrayNode.create());
            }

            return copyToByteArrayNode.execute(string, encoding);
        }

        private TruffleString.CodeRange getCodeRange(AbstractTruffleString string, TruffleString.Encoding encoding) {
            if (codeRangeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                codeRangeNode = insert(GetByteCodeRangeNode.create());
            }

            return codeRangeNode.execute(string, encoding);
        }

        private AbstractTruffleString makeTString(byte[] bytes, TruffleString.Encoding encoding, int byteOffset,
                int byteLength) {
            if (fromByteArrayNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fromByteArrayNode = insert(TruffleString.FromByteArrayNode.create());
            }

            return fromByteArrayNode.execute(bytes, byteOffset, byteLength, encoding, false);
        }

        private AbstractTruffleString makeTString(byte[] bytes, TruffleString.Encoding encoding) {
            return makeTString(bytes, encoding, 0, bytes.length);
        }

    }

    @CoreMethod(names = "clear", raiseIfNotMutableSelf = true)
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyString clear(RubyString string) {
            string.setTString(string.encoding.tencoding.getEmpty());
            return string;
        }
    }

    @Primitive(name = "character_printable_p")
    public abstract static class CharacterPrintablePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isCharacterPrintable(Object character,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached ConditionProfile is7BitProfile,
                @Cached StringHelperNodes.GetCodePointNode getCodePointNode,
                @Cached GetByteCodeRangeNode getCodeRangeNode) {
            final RubyEncoding encoding = strings.getEncoding(character);
            final var tString = strings.getTString(character);

            final int codePoint = getCodePointNode.executeGetCodePoint(tString, encoding, 0);
            final boolean asciiOnly = StringGuards.is7Bit(tString, encoding, getCodeRangeNode);

            if (is7BitProfile.profile(asciiOnly)) {
                return StringSupport.isAsciiPrintable(codePoint);
            } else {
                return isMBCPrintable(encoding.jcoding, codePoint);
            }
        }

        @TruffleBoundary
        protected boolean isMBCPrintable(Encoding encoding, int codePoint) {
            return encoding.isPrint(codePoint);
        }

    }

    @Primitive(name = "string_append")
    public abstract static class StringAppendPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Child private StringHelperNodes.StringAppendNode stringAppendNode = StringHelperNodes.StringAppendNode
                .create();

        public static StringAppendPrimitiveNode create() {
            return StringAppendPrimitiveNodeFactory.create(null);
        }

        public abstract RubyString executeStringAppend(RubyString string, Object other);

        @Specialization
        protected RubyString stringAppend(RubyString string, Object other) {
            final RubyString result = stringAppendNode.executeStringAppend(string, other);
            string.setTString(result.tstring, result.encoding);
            return string;
        }

    }

    @Primitive(name = "string_awk_split", lowerFixnum = 1)
    @ImportStatic(StringGuards.class)
    public abstract static class StringAwkSplitPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private CallBlockNode yieldNode = CallBlockNode.create();
        @Child GetByteCodeRangeNode codeRangeNode = GetByteCodeRangeNode.create();

        private static final int SUBSTRING_CREATED = -1;

        @Specialization(guards = "is7Bit(tstring, encoding, codeRangeNode)")
        protected Object stringAwkSplitSingleByte(Object string, int limit, Object block,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached ConditionProfile executeBlockProfile,
                @Cached ConditionProfile growArrayProfile,
                @Cached ConditionProfile trailingSubstringProfile,
                @Cached ConditionProfile trailingEmptyStringProfile,
                @Cached TruffleString.MaterializeNode materializeNode,
                @Cached TruffleString.ReadByteNode readByteNode,
                @Cached TruffleString.SubstringByteIndexNode substringNode,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            Object[] ret = new Object[10];
            int storeIndex = 0;

            int byteLength = tstring.byteLength(encoding.tencoding);
            materializeNode.execute(tstring, encoding.tencoding);

            int substringStart = 0;
            boolean findingSubstringEnd = false;
            for (int i = 0; i < byteLength; i++) {
                if (StringSupport.isAsciiSpace(readByteNode.execute(tstring, i, encoding.tencoding))) {
                    if (findingSubstringEnd) {
                        findingSubstringEnd = false;

                        final RubyString substring = createSubString(substringNode, tstring, encoding, substringStart,
                                i - substringStart);
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

            if (trailingSubstringProfile.profile(findingSubstringEnd)) {
                final RubyString substring = createSubString(substringNode, tstring, encoding, substringStart,
                        byteLength - substringStart);
                ret = addSubstring(ret, storeIndex++, substring, block, executeBlockProfile, growArrayProfile);
            }

            if (trailingEmptyStringProfile.profile(limit < 0 &&
                    StringSupport.isAsciiSpace(readByteNode.execute(tstring, byteLength - 1, encoding.tencoding)))) {
                final RubyString substring = createSubString(substringNode, tstring, encoding, byteLength - 1, 0);
                ret = addSubstring(ret, storeIndex++, substring, block, executeBlockProfile, growArrayProfile);
            }

            if (block == nil) {
                return createArray(ret, storeIndex);
            } else {
                return string;
            }
        }

        @Specialization(guards = "isValid(tstring, encoding, codeRangeNode)")
        protected Object stringAwkSplit(Object string, int limit, Object block,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached ConditionProfile executeBlockProfile,
                @Cached ConditionProfile growArrayProfile,
                @Cached ConditionProfile trailingSubstringProfile,
                @Cached TruffleString.CreateCodePointIteratorNode createCodePointIteratorNode,
                @Cached TruffleStringIterator.NextNode nextNode,
                @Cached TruffleString.SubstringByteIndexNode substringNode,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            Object[] ret = new Object[10];
            int storeIndex = 0;

            final boolean limitPositive = limit > 0;
            int i = limit > 0 ? 1 : 0;

            var tencoding = encoding.tencoding;
            final int len = tstring.byteLength(tencoding);

            // TODO error if broken like getCodePointNode.executeGetCodePoint(tstring, encoding, p - byteOffset);
            var iterator = createCodePointIteratorNode.execute(tstring, tencoding);

            boolean skip = true;
            int e = 0, b = 0;
            while (iterator.hasNext()) {
                int c = nextNode.execute(iterator);
                int p = iterator.getByteIndex();

                if (skip) {
                    if (StringSupport.isAsciiSpace(c)) {
                        b = p;
                    } else {
                        e = p;
                        skip = false;
                        if (limitPositive && limit <= i) {
                            break;
                        }
                    }
                } else {
                    if (StringSupport.isAsciiSpace(c)) {
                        var substring = createSubString(substringNode, tstring, encoding, b, e - b);
                        ret = addSubstring(
                                ret,
                                storeIndex++,
                                substring,
                                block,
                                executeBlockProfile,
                                growArrayProfile);
                        skip = true;
                        b = p;
                        if (limitPositive) {
                            i++;
                        }
                    } else {
                        e = p;
                    }
                }
            }

            if (trailingSubstringProfile.profile(len > 0 && (limitPositive || len > b || limit < 0))) {
                var substring = createSubString(substringNode, tstring, encoding, b, len - b);
                ret = addSubstring(ret, storeIndex++, substring, block, executeBlockProfile, growArrayProfile);
            }

            if (block == nil) {
                return createArray(ret, storeIndex);
            } else {
                return string;
            }
        }

        @Specialization(guards = "isBrokenCodeRange(tstring, encoding, codeRangeNode)")
        protected Object broken(Object string, int limit, Object block,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorInvalidByteSequence(encoding, this));
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

        @Child private StringHelperNodes.NormalizeIndexNode normalizeIndexNode = StringHelperNodes.NormalizeIndexNode
                .create();

        @Specialization
        protected Object stringByteSubstring(Object string, int index, NotProvided length,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.SubstringByteIndexNode substringNode) {
            var tString = libString.getTString(string);
            var encoding = libString.getEncoding(string);
            final int stringByteLength = tString.byteLength(encoding.tencoding);
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, stringByteLength);

            if (indexOutOfBoundsProfile.profile(normalizedIndex < 0 || normalizedIndex >= stringByteLength)) {
                return nil;
            }

            return createSubString(substringNode, tString, encoding, normalizedIndex, 1);
        }

        @Specialization
        protected Object stringByteSubstring(Object string, int index, int length,
                @Cached ConditionProfile negativeLengthProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile lengthTooLongProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.SubstringByteIndexNode substringNode) {
            if (negativeLengthProfile.profile(length < 0)) {
                return nil;
            }

            var tString = libString.getTString(string);
            var encoding = libString.getEncoding(string);
            final int stringByteLength = tString.byteLength(encoding.tencoding);
            final int normalizedIndex = normalizeIndexNode.executeNormalize(index, stringByteLength);

            if (indexOutOfBoundsProfile.profile(normalizedIndex < 0 || normalizedIndex > stringByteLength)) {
                return nil;
            }

            if (lengthTooLongProfile.profile(normalizedIndex + length > stringByteLength)) {
                length = stringByteLength - normalizedIndex;
            }

            return createSubString(substringNode, tString, encoding, normalizedIndex, length);
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
                guards = { "indexOutOfBounds(strings.byteLength(string), byteIndex)" })
        protected Object stringChrAtOutOfBounds(Object string, int byteIndex,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!indexOutOfBounds(tstring.byteLength(encoding.tencoding), byteIndex)",
                        "isSingleByteOptimizable(tstring, encoding, singleByteOptimizableNode)" })
        protected Object stringChrAtSingleByte(Object string, int byteIndex,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached TruffleString.SubstringByteIndexNode substringByteIndexNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Bind("strings.getTString(string)") AbstractTruffleString tstring,
                @Bind("strings.getEncoding(string)") RubyEncoding encoding) {
            return createSubString(substringByteIndexNode, tstring, encoding, byteIndex, 1);
        }

        @Specialization(
                guards = {
                        "!indexOutOfBounds(originalTString.byteLength(originalEncoding.tencoding), byteIndex)",
                        "!isSingleByteOptimizable(originalTString, originalEncoding, singleByteOptimizableNode)" })
        protected Object stringChrAt(Object string, int byteIndex,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.SubstringByteIndexNode substringByteIndexNode,
                @Cached TruffleString.ForceEncodingNode forceEncodingNode,
                @Cached TruffleString.ByteLengthOfCodePointNode byteLengthOfCodePointNode,
                @Bind("strings.getTString(string)") AbstractTruffleString originalTString,
                @Bind("strings.getEncoding(string)") RubyEncoding originalEncoding) {
            final RubyEncoding actualEncoding = getActualEncodingNode.execute(originalTString, originalEncoding);
            var tstring = forceEncodingNode.execute(originalTString, originalEncoding.tencoding,
                    actualEncoding.tencoding);

            final int clen = byteLengthOfCodePointNode.execute(tstring, byteIndex, actualEncoding.tencoding,
                    ErrorHandling.RETURN_NEGATIVE);

            if (!StringSupport.MBCLEN_CHARFOUND_P(clen)) {
                return nil;
            }

            assert byteIndex + clen <= tstring.byteLength(actualEncoding.tencoding);

            return createSubString(substringByteIndexNode, tstring, actualEncoding, byteIndex, clen);
        }

        protected static boolean indexOutOfBounds(int byteLength, int byteIndex) {
            return byteIndex < 0 || byteIndex >= byteLength;
        }

    }

    @Primitive(name = "string_escape")
    public abstract static class StringEscapePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyString string_escape(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached GetByteCodeRangeNode getByteCodeRangeNode) {
            var tstring = strings.getTString(string);
            var encoding = strings.getEncoding(string);
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            var codeRange = getByteCodeRangeNode.execute(tstring, encoding.tencoding);
            final TruffleString escaped = rbStrEscape(encoding, byteArray, codeRange);
            return createString(escaped, Encodings.US_ASCII);
        }

        // MRI: rb_str_escape
        @TruffleBoundary
        private static TruffleString rbStrEscape(RubyEncoding rubyEncoding, InternalByteArray byteArray,
                TruffleString.CodeRange cr) {
            final Encoding enc = rubyEncoding.jcoding;
            final byte[] pBytes = byteArray.getArray();

            int p = byteArray.getOffset();
            int pend = byteArray.getEnd();
            int prev = p;
            TStringBuilder result = new TStringBuilder();
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

            result.setEncoding(Encodings.US_ASCII);
            return result.toTString(); // CodeRange.CR_7BIT
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

        @Specialization(guards = "offset < 0")
        protected Object stringFindCharacterNegativeOffset(Object string, int offset) {
            return nil;
        }

        @Specialization(guards = "offsetTooLarge(strings.byteLength(string), offset)")
        protected Object stringFindCharacterOffsetTooLarge(Object string, int offset,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            return nil;
        }

        @Specialization(
                guards = {
                        "offset >= 0",
                        "!offsetTooLarge(strings.byteLength(string), offset)",
                        "isSingleByteOptimizable(strings.getTString(string), strings.getEncoding(string), singleByteOptimizableNode)" })
        protected Object stringFindCharacterSingleByte(Object string, int offset,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.SubstringByteIndexNode substringNode) {
            // Taken from Rubinius's String::find_character.
            return createSubString(substringNode, strings, string, offset, 1);
        }

        @Specialization(
                guards = {
                        "offset >= 0",
                        "!offsetTooLarge(strings.byteLength(string), offset)",
                        "!isSingleByteOptimizable(strings.getTString(string), strings.getEncoding(string), singleByteOptimizableNode)" })
        protected Object stringFindCharacter(Object string, int offset,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached TruffleString.ByteLengthOfCodePointNode byteLengthOfCodePointNode,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.SubstringByteIndexNode substringNode) {
            // Taken from Rubinius's String::find_character.
            var tstring = strings.getTString(string);
            var tencoding = strings.getTEncoding(string);

            int clen = byteLengthOfCodePointNode.execute(tstring, offset, tencoding, ErrorHandling.BEST_EFFORT);
            return createSubString(substringNode, strings, string, offset, clen);
        }

        protected static boolean offsetTooLarge(int byteLength, int offset) {
            return offset >= byteLength;
        }

    }

    @Primitive(name = "string_from_codepoint", lowerFixnum = 0)
    public abstract static class StringFromCodepointPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isSimple(code, encoding)")
        protected RubyString stringFromCodepointSimple(int code, RubyEncoding encoding,
                @Cached ConditionProfile isUTF8Profile,
                @Cached ConditionProfile isUSAsciiProfile,
                @Cached ConditionProfile isAscii8BitProfile,
                @Cached TruffleString.FromCodePointNode fromCodePointNode) {
            final TruffleString tstring;
            if (isUTF8Profile.profile(encoding == Encodings.UTF_8)) {
                tstring = TStringConstants.UTF8_SINGLE_BYTE[code];
            } else if (isUSAsciiProfile.profile(encoding == Encodings.US_ASCII)) {
                tstring = TStringConstants.US_ASCII_SINGLE_BYTE[code];
            } else if (isAscii8BitProfile.profile(encoding == Encodings.BINARY)) {
                tstring = TStringConstants.BINARY_SINGLE_BYTE[code];
            } else {
                tstring = fromCodePointNode.execute(code, encoding.tencoding, false);
                assert tstring != null;
            }

            return createString(tstring, encoding);
        }

        @Specialization(guards = "!isSimple(code, encoding)")
        protected RubyString stringFromCodepoint(int code, RubyEncoding encoding,
                @Cached TruffleString.FromCodePointNode fromCodePointNode,
                @Cached BranchProfile errorProfile) {
            var tstring = fromCodePointNode.execute(code, encoding.tencoding, false);
            if (tstring == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().rangeError(code, encoding, this));
            }

            return createString(tstring, encoding);
        }

        @Specialization(guards = "!fitsInInteger(code)")
        protected RubyString stringFromCodepoint(long code, RubyEncoding encoding) {
            throw new RaiseException(getContext(), coreExceptions().rangeError(code, encoding, this));
        }

        protected boolean isSimple(int codepoint, RubyEncoding encoding) {
            final Encoding enc = encoding.jcoding;

            return (enc.isAsciiCompatible() && codepoint >= 0x00 && codepoint < 0x80) ||
                    (encoding == Encodings.BINARY && codepoint >= 0x00 && codepoint <= 0xFF);
        }

    }

    @Primitive(name = "string_to_f")
    public abstract static class StringToFPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object stringToF(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached FixnumOrBignumNode fixnumOrBignumNode) {
            var rope = strings.getTString(string);
            var encoding = strings.getEncoding(string);
            if (rope.isEmpty()) {
                return nil;
            }

            final String javaString = RubyGuards.getJavaString(string);
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
                                    rope,
                                    encoding,
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
                return new DoubleConverter().parse(rope, encoding, true, true);
            } catch (NumberFormatException e) {
                return nil;
            }
        }

    }

    @Primitive(name = "find_string", lowerFixnum = 2)
    @ImportStatic(StringGuards.class)
    public abstract static class StringIndexPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object findStringByteIndex(Object rubyString, Object rubyPattern, int byteOffset,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern,
                @Cached CheckEncodingNode checkEncodingNode,
                @Cached TruffleString.ByteIndexOfStringNode indexOfStringNode,
                @Cached ConditionProfile offsetTooLargeProfile,
                @Cached ConditionProfile notFoundProfile) {
            assert byteOffset >= 0;

            // Throw an exception if the encodings are not compatible.
            checkEncodingNode.executeCheckEncoding(rubyString, rubyPattern);

            var string = libString.getTString(rubyString);
            var stringEncoding = libString.getEncoding(rubyString).tencoding;
            int stringByteLength = string.byteLength(stringEncoding);

            if (offsetTooLargeProfile.profile(byteOffset >= stringByteLength)) {
                return nil;
            }

            int patternByteIndex = indexOfStringNode.execute(string, libPattern.getTString(rubyPattern), byteOffset,
                    stringByteLength, stringEncoding);

            if (notFoundProfile.profile(patternByteIndex < 0)) {
                return nil;
            }

            return patternByteIndex;
        }

    }

    @Primitive(name = "string_byte_character_index", lowerFixnum = 1)
    public abstract static class StringByteCharacterIndexNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected int byteIndexToCodePointIndex(Object string, int byteIndex,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.ByteIndexToCodePointIndexNode byteIndexToCodePointIndexNode,
                @Bind("libString.getTString(string)") AbstractTruffleString tstring,
                @Bind("libString.getEncoding(string)") RubyEncoding encoding) {
            return byteIndexToCodePointIndexNode.execute(tstring, 0, byteIndex, encoding.tencoding);
        }
    }

    /** Search pattern in string starting after offset characters, and return a character index or nil */
    @Primitive(name = "string_character_index", lowerFixnum = 2)
    public abstract static class StringCharacterIndexNode extends PrimitiveArrayArgumentsNode {

        @Child protected RubyStringLibrary libString = RubyStringLibrary.createDispatched();
        @Child protected RubyStringLibrary libPattern = RubyStringLibrary.createDispatched();
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        @Specialization(guards = "singleByteOptimizableNode.execute(string, stringEncoding)")
        protected Object singleByteOptimizable(Object rubyString, Object rubyPattern, int codePointOffset,
                @Bind("libString.getTString(rubyString)") AbstractTruffleString string,
                @Bind("libString.getEncoding(rubyString)") RubyEncoding stringEncoding,
                @Bind("libPattern.getTString(rubyPattern)") AbstractTruffleString pattern,
                @Bind("libPattern.getEncoding(rubyPattern)") RubyEncoding patternEncoding,
                @Cached TruffleString.ByteIndexOfStringNode byteIndexOfStringNode,
                @Cached ConditionProfile foundProfile) {

            assert codePointOffset >= 0;

            // When single-byte optimizable, the byte length and the codepoint length are the same.
            int stringByteLength = string.byteLength(stringEncoding.tencoding);

            assert codePointOffset + pattern.byteLength(
                    patternEncoding.tencoding) <= stringByteLength : "already checked in the caller, String#index";

            int found = byteIndexOfStringNode.execute(string, pattern, codePointOffset,
                    stringByteLength,
                    stringEncoding.tencoding);

            if (foundProfile.profile(found >= 0)) {
                return found;
            }

            return nil;
        }

        @Specialization(guards = "!singleByteOptimizableNode.execute(string, stringEncoding)")
        protected Object multiByte(Object rubyString, Object rubyPattern, int codePointOffset,
                @Bind("libString.getTString(rubyString)") AbstractTruffleString string,
                @Bind("libString.getEncoding(rubyString)") RubyEncoding stringEncoding,
                @Bind("libPattern.getTString(rubyPattern)") AbstractTruffleString pattern,
                @Bind("libPattern.getEncoding(rubyPattern)") RubyEncoding patternEncoding,
                @Cached CodePointLengthNode codePointLengthNode,
                @Cached TruffleString.IndexOfStringNode indexOfStringNode,
                @Cached ConditionProfile foundProfile) {

            assert codePointOffset >= 0;
            assert codePointOffset + pattern.codePointLengthUncached(patternEncoding.tencoding) <= string
                    .codePointLengthUncached(stringEncoding.tencoding) : "already checked in the caller, String#index";

            int stringCodePointLength = codePointLengthNode.execute(string, stringEncoding.tencoding);
            int found = indexOfStringNode.execute(string, pattern, codePointOffset, stringCodePointLength,
                    stringEncoding.tencoding);

            if (foundProfile.profile(found >= 0)) {
                return found;
            }

            return nil;
        }
    }

    /** Search pattern in string starting after offset bytes, and return a byte index or nil */
    @Primitive(name = "string_byte_index", lowerFixnum = 2)
    public abstract static class StringByteIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object stringByteIndex(Object rubyString, Object rubyPattern, int byteOffset,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern,
                @Cached TruffleString.ByteIndexOfStringNode byteIndexOfStringNode,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile foundProfile) {
            assert byteOffset >= 0;

            var string = libString.getTString(rubyString);
            var stringEncoding = libString.getEncoding(rubyString).tencoding;
            int stringByteLength = string.byteLength(stringEncoding);

            var pattern = libPattern.getTString(rubyPattern);
            var patternEncoding = libPattern.getEncoding(rubyPattern).tencoding;
            int patternByteLength = pattern.byteLength(patternEncoding);

            if (indexOutOfBoundsProfile.profile(byteOffset + patternByteLength > stringByteLength)) {
                return nil;
            }

            int found = byteIndexOfStringNode.execute(string, pattern, byteOffset, stringByteLength, stringEncoding);
            if (foundProfile.profile(found >= 0)) {
                return found;
            }

            return nil;
        }
    }

    // Named 'string_byte_index' in Rubinius.
    @Primitive(name = "string_byte_index_from_char_index", lowerFixnum = 1)
    public abstract static class StringByteIndexFromCharIndexNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected Object byteIndexFromCharIndex(Object string, int characterIndex,
                @Cached TruffleString.CodePointIndexToByteIndexNode codePointIndexToByteIndexNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            return codePointIndexToByteIndexNode.execute(libString.getTString(string), 0, characterIndex,
                    libString.getTEncoding(string));
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
                "isSingleByteOptimizable(strings.getTString(string), strings.getEncoding(string), singleByteOptimizableNode)" })
        protected int singleByteOptimizable(Object string, int index,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode) {
            return index - 1;
        }

        @Specialization(guards = {
                "index > 0",
                "!isSingleByteOptimizable(strings.getTString(string), strings.getEncoding(string), singleByteOptimizableNode)",
                "isFixedWidthEncoding(strings.getEncoding(string))" })
        protected int fixedWidthEncoding(Object string, int index,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached ConditionProfile firstCharacterProfile) {
            final Encoding encoding = strings.getEncoding(string).jcoding;

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
                "!isSingleByteOptimizable(strings.getTString(string), strings.getEncoding(string), singleByteOptimizableNode)",
                "!isFixedWidthEncoding(strings.getEncoding(string))" })
        @TruffleBoundary
        protected Object other(Object string, int index,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached SingleByteOptimizableNode singleByteOptimizableNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode) {
            var encoding = strings.getEncoding(string);
            var byteArray = byteArrayNode.execute(strings.getTString(string), encoding.tencoding);
            final int p = byteArray.getOffset();
            final int end = byteArray.getEnd();

            final int b = encoding.jcoding.prevCharHead(byteArray.getArray(), p, p + index, end);

            if (b == -1) {
                return nil;
            }

            return b - p;
        }

    }

    @Primitive(name = "find_string_reverse", lowerFixnum = 2)
    @ImportStatic(StringGuards.class)
    public abstract static class StringRindexPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object stringRindex(Object rubyString, Object rubyPattern, int byteOffset,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached CheckEncodingNode checkEncodingNode,
                @Cached TruffleString.LastByteIndexOfStringNode lastByteIndexOfStringNode,
                @Cached BranchProfile startOutOfBoundsProfile,
                @Cached BranchProfile startTooCloseToEndProfile,
                @Cached BranchProfile noMatchProfile) {
            assert byteOffset >= 0;

            // Throw an exception if the encodings are not compatible.
            checkEncodingNode.executeCheckEncoding(rubyString, rubyPattern);

            var string = libString.getTString(rubyString);
            var stringEncoding = libString.getEncoding(rubyString).tencoding;
            int stringByteLength = string.byteLength(stringEncoding);

            var pattern = libPattern.getTString(rubyPattern);
            var patternEncoding = libPattern.getEncoding(rubyPattern).tencoding;
            int patternByteLength = pattern.byteLength(patternEncoding);

            int normalizedStart = byteOffset;

            if (normalizedStart >= stringByteLength) {
                startOutOfBoundsProfile.enter();
                normalizedStart = stringByteLength - 1;
            }

            if (stringByteLength - normalizedStart < patternByteLength) {
                startTooCloseToEndProfile.enter();
                normalizedStart = stringByteLength - patternByteLength;
            }

            int result = lastByteIndexOfStringNode.execute(string, pattern, normalizedStart + patternByteLength, 0,
                    stringEncoding);

            if (result < 0) {
                noMatchProfile.enter();
                return nil;
            }

            return result;
        }
    }

    @Primitive(name = "string_splice", lowerFixnum = { 2, 3 })
    @ImportStatic(StringGuards.class)
    public abstract static class StringSplicePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "libOther.isRubyString(other)", "indexAtStartBound(spliceByteIndex)" })
        protected Object splicePrepend(
                RubyString string, Object other, int spliceByteIndex, int byteCountToReplace, RubyEncoding rubyEncoding,
                @Cached TruffleString.SubstringByteIndexNode prependSubstringNode,
                @Cached TruffleString.ConcatNode prependConcatNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libOther) {
            var original = string.tstring;
            var originalTEncoding = string.encoding.tencoding;
            var left = libOther.getTString(other);
            var right = prependSubstringNode.execute(original, byteCountToReplace,
                    original.byteLength(originalTEncoding) - byteCountToReplace, originalTEncoding, true);

            var prependResult = prependConcatNode.execute(left, right, rubyEncoding.tencoding, true);
            string.setTString(prependResult, rubyEncoding);

            return string;
        }

        @Specialization(guards = { "libOther.isRubyString(other)", "indexAtEndBound(string, spliceByteIndex)" })
        protected Object spliceAppend(
                RubyString string, Object other, int spliceByteIndex, int byteCountToReplace, RubyEncoding rubyEncoding,
                @Cached TruffleString.ConcatNode appendConcatNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libOther) {
            var left = string.tstring;
            var right = libOther.getTString(other);

            var concatResult = appendConcatNode.execute(left, right, rubyEncoding.tencoding, true);
            string.setTString(concatResult, rubyEncoding);

            return string;
        }

        @Specialization(guards = { "libOther.isRubyString(other)", "!indexAtEitherBounds(string, spliceByteIndex)" })
        protected RubyString splice(
                RubyString string, Object other, int spliceByteIndex, int byteCountToReplace, RubyEncoding rubyEncoding,
                @Cached ConditionProfile insertStringIsEmptyProfile,
                @Cached ConditionProfile splitRightIsEmptyProfile,
                @Cached TruffleString.SubstringByteIndexNode leftSubstringNode,
                @Cached TruffleString.SubstringByteIndexNode rightSubstringNode,
                @Cached TruffleString.ConcatNode leftConcatNode,
                @Cached TruffleString.ConcatNode rightConcatNode,
                @Cached TruffleString.ForceEncodingNode forceEncodingNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libOther) {
            var sourceTEncoding = string.encoding.tencoding;
            var resultTEncoding = rubyEncoding.tencoding;
            var source = string.tstring;
            var insert = libOther.getTString(other);
            final int rightSideStartingIndex = spliceByteIndex + byteCountToReplace;

            var splitLeft = leftSubstringNode.execute(source, 0, spliceByteIndex, sourceTEncoding, true);
            var splitRight = rightSubstringNode.execute(source, rightSideStartingIndex,
                    source.byteLength(sourceTEncoding) - rightSideStartingIndex, sourceTEncoding, true);

            final TruffleString joinedLeft; // always in resultTEncoding
            if (insertStringIsEmptyProfile.profile(insert.isEmpty())) {
                joinedLeft = forceEncodingNode.execute(splitLeft, sourceTEncoding, resultTEncoding);
            } else {
                joinedLeft = leftConcatNode.execute(splitLeft, insert, resultTEncoding, true);
            }

            final TruffleString joinedRight; // always in resultTEncoding
            if (splitRightIsEmptyProfile.profile(splitRight.isEmpty())) {
                joinedRight = joinedLeft;
            } else {
                joinedRight = rightConcatNode.execute(joinedLeft, splitRight, resultTEncoding, true);
            }

            string.setTString(joinedRight, rubyEncoding);
            return string;
        }

        protected boolean indexAtStartBound(int index) {
            return index == 0;
        }

        protected boolean indexAtEndBound(RubyString string, int index) {
            return index == string.byteLength();
        }

        protected boolean indexAtEitherBounds(RubyString string, int index) {
            return indexAtStartBound(index) || indexAtEndBound(string, index);
        }
    }

    @Primitive(name = "string_to_inum", lowerFixnum = 1)
    @NodeChild(value = "string", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "fixBase", type = RubyNode.class)
    @NodeChild(value = "strict", type = RubyNode.class)
    @NodeChild(value = "raiseOnError", type = RubyNode.class)
    public abstract static class StringToInumPrimitiveNode extends PrimitiveNode {

        @Specialization(guards = "base == 10")
        protected Object base10(Object string, int base, boolean strict, boolean raiseOnError,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.ParseLongNode parseLongNode,
                @Cached BranchProfile notLazyLongProfile,
                @Cached FixnumOrBignumNode fixnumOrBignumNode,
                @Cached BranchProfile exceptionProfile) {
            var tstring = libString.getTString(string);
            try {
                return parseLongNode.execute(tstring, 10);
            } catch (TruffleString.NumberFormatException e) {
                notLazyLongProfile.enter();
                var rope = libString.getTString(string);
                var encoding = libString.getEncoding(string);
                return bytesToInum(rope, encoding, base, strict, raiseOnError, fixnumOrBignumNode, exceptionProfile);
            }
        }

        @Specialization(guards = "base == 0")
        protected Object base0(Object string, int base, boolean strict, boolean raiseOnError,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached TruffleString.ParseLongNode parseLongNode,
                @Cached TruffleString.CodePointAtByteIndexNode codePointNode,
                @Cached ConditionProfile notEmptyProfile,
                @Cached BranchProfile notLazyLongProfile,
                @Cached FixnumOrBignumNode fixnumOrBignumNode,
                @Cached BranchProfile exceptionProfile) {
            var tstring = libString.getTString(string);
            var enc = libString.getEncoding(string);
            var tenc = enc.tencoding;
            var len = tstring.byteLength(tenc);

            if (notEmptyProfile.profile(enc.jcoding.isAsciiCompatible() && len >= 1)) {
                int first = codePointNode.execute(tstring, 0, tenc, ErrorHandling.RETURN_NEGATIVE);
                int second;
                if ((first >= '1' && first <= '9') || (len >= 2 && (first == '-' || first == '+') &&
                        (second = codePointNode.execute(tstring, 1, tenc, ErrorHandling.RETURN_NEGATIVE)) >= '1' &&
                        second <= '9')) {
                    try {
                        return parseLongNode.execute(tstring, 10);
                    } catch (TruffleString.NumberFormatException e) {
                        notLazyLongProfile.enter();
                    }
                }
            }

            var rope = libString.getTString(string);
            var encoding = libString.getEncoding(string);
            return bytesToInum(rope, encoding, base, strict, raiseOnError, fixnumOrBignumNode, exceptionProfile);
        }

        @Specialization(guards = { "base != 10", "base != 0" })
        protected Object otherBase(Object string, int base, boolean strict, boolean raiseOnError,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Cached FixnumOrBignumNode fixnumOrBignumNode,
                @Cached BranchProfile exceptionProfile) {
            var rope = libString.getTString(string);
            var encoding = libString.getEncoding(string);
            return bytesToInum(rope, encoding, base, strict, raiseOnError, fixnumOrBignumNode, exceptionProfile);
        }

        private Object bytesToInum(AbstractTruffleString rope, RubyEncoding encoding, int base, boolean strict,
                boolean raiseOnError, FixnumOrBignumNode fixnumOrBignumNode,
                BranchProfile exceptionProfile) {
            try {
                return ConvertBytes.bytesToInum(
                        getContext(),
                        this,
                        fixnumOrBignumNode,
                        rope,
                        encoding,
                        base,
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
    public abstract static class StringByteAppendPrimitiveNode extends CoreMethodArrayArgumentsNode {
        @Specialization(guards = "libOther.isRubyString(other)")
        protected RubyString stringByteAppend(RubyString string, Object other,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libOther,
                @Cached TruffleString.ConcatNode concatNode,
                @Cached TruffleString.ForceEncodingNode forceEncodingNode) {
            // The semantics of this primitive are such that the original string's byte[] should be extended without
            // negotiating the encoding.
            var encoding = string.encoding;
            var left = string.tstring;
            var right = forceEncodingNode.execute(libOther.getTString(other), libOther.getEncoding(other).tencoding,
                    encoding.tencoding);
            string.setTString(concatNode.execute(left, right, encoding.tencoding, true), encoding);
            return string;
        }
    }

    @Primitive(name = "string_substring", lowerFixnum = { 1, 2 })
    @ImportStatic(StringGuards.class)
    public abstract static class StringSubstringPrimitiveNode extends CoreMethodArrayArgumentsNode {

        public abstract Object execute(Object string, int codePointOffset, int codePointLength);

        @Specialization
        protected Object stringSubstringGeneric(Object string, int codePointOffset, int codePointLength,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
                @Bind("libString.getTString(string)") AbstractTruffleString tstring,
                @Bind("libString.getEncoding(string)") RubyEncoding encoding,
                @Cached StringHelperNodes.NormalizeIndexNode normalizeIndexNode,
                @Cached CodePointLengthNode codePointLengthNode,
                @Cached TruffleString.SubstringNode substringNode,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile tooLargeTotalProfile,
                @Cached ConditionProfile triviallyOutOfBoundsProfile) {
            int stringCodePointLength = codePointLengthNode.execute(tstring, encoding.tencoding);
            if (triviallyOutOfBoundsProfile.profile(codePointLength < 0 || codePointOffset > stringCodePointLength)) {
                return nil;
            }

            int normalizedCodePointOffset = normalizeIndexNode.executeNormalize(codePointOffset, stringCodePointLength);
            if (negativeIndexProfile.profile(normalizedCodePointOffset < 0)) {
                return nil;
            }

            int normalizedCodePointLength = codePointLength;
            if (tooLargeTotalProfile
                    .profile(normalizedCodePointOffset + normalizedCodePointLength > stringCodePointLength)) {
                normalizedCodePointLength = stringCodePointLength - normalizedCodePointOffset;
            }

            return createSubString(substringNode, tstring, encoding, normalizedCodePointOffset,
                    normalizedCodePointLength);
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

            return makeStringNode.executeMake(array, rubyEncoding);
        }

    }

    @Primitive(name = "string_to_null_terminated_byte_array")
    public abstract static class StringToNullTerminatedByteArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libString.isRubyString(string)")
        protected Object stringToNullTerminatedByteArray(Object string,
                @Cached TruffleString.CopyToByteArrayNode copyToByteArrayNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            final var encoding = libString.getEncoding(string);
            final var tstring = libString.getTString(string);
            final int bytesToCopy = tstring.byteLength(encoding.tencoding);
            final var bytesWithNull = new byte[bytesToCopy + 1];

            // NOTE: we always need one copy here, as native code could modify the passed byte[]
            copyToByteArrayNode.execute(tstring, 0,
                    bytesWithNull, 0, bytesToCopy, encoding.tencoding);

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
                @Cached TruffleString.AsManagedNode asManagedNode) {
            var encoding = string.encoding;
            TruffleString immutableManagedString = asManagedNode.execute(string.tstring, encoding.tencoding);
            return getLanguage().getFrozenStringLiteral(immutableManagedString, encoding);
        }
    }

    @Primitive(name = "string_truncate", lowerFixnum = 1)
    public abstract static class TruncateNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "newByteLength < 0")
        @TruffleBoundary
        protected RubyString truncateLengthNegative(RubyString string, int newByteLength) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().argumentError(formatNegativeError(newByteLength), this));
        }

        @Specialization(guards = { "newByteLength >= 0", "isNewLengthTooLarge(string, newByteLength)" })
        @TruffleBoundary
        protected RubyString truncateLengthTooLong(RubyString string, int newByteLength) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().argumentError(formatTooLongError(newByteLength, string), this));
        }

        @Specialization(guards = { "newByteLength >= 0", "!isNewLengthTooLarge(string, newByteLength)" })
        protected RubyString tuncate(RubyString string, int newByteLength,
                @Cached TruffleString.SubstringByteIndexNode substringNode) {
            var tencoding = string.encoding.tencoding;
            string.setTString(substringNode.execute(string.tstring, 0, newByteLength, tencoding, true));
            return string;
        }

        protected static boolean isNewLengthTooLarge(RubyString string, int newByteLength) {
            return newByteLength > string.byteLength();
        }

        @TruffleBoundary
        private String formatNegativeError(int count) {
            return StringUtils.format("Invalid byte count: %d is negative", count);
        }

        @TruffleBoundary
        private String formatTooLongError(int count, RubyString string) {
            return StringUtils
                    .format("Invalid byte count: %d exceeds string size of %d bytes", count, string.byteLength());
        }

    }

}
