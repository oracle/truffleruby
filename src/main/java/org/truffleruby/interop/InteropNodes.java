/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.Source;

@CoreModule("Truffle::Interop")
public abstract class InteropNodes {

    // TODO (pitr-ch 27-Mar-2019): remove create()
    // TODO (pitr-ch 27-Mar-2019): rename methods to match new messages
    // TODO (pitr-ch 27-Mar-2019): break down to new messages

    @CoreMethod(names = "import_file", onSingleton = true, required = 1)
    public abstract static class ImportFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(fileName)")
        protected Object importFile(DynamicObject fileName) {
            try {
                //intern() to improve footprint
                final TruffleFile file = getContext()
                        .getEnv()
                        .getPublicTruffleFile(StringOperations.getString(fileName).intern());
                final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, file).build();
                getContext().getEnv().parsePublic(source).call();
            } catch (IOException e) {
                throw new JavaException(e);
            }

            return nil;
        }

    }

    private abstract static class InteropCoreMethodArrayArgumentsNode extends CoreMethodArrayArgumentsNode {
        protected int getCacheLimit() {
            return getContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    private abstract static class InteropPrimitiveArrayArgumentsNode extends PrimitiveArrayArgumentsNode {
        protected int getCacheLimit() {
            return getContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @CoreMethod(names = "executable?", onSingleton = true, required = 1)
    public abstract static class IsExecutableNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean isExecutable(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isExecutable(receiver);
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "execute", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteNode extends RubySourceNode {

        abstract Object execute(Object receiver, Object[] args);

        public static ExecuteNode create() {
            return InteropNodesFactory.ExecuteNodeFactory.create(null);
        }

        @Specialization(limit = "getCacheLimit()")
        protected Object executeForeignCached(Object receiver, Object[] args,
                @Cached RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object foreign;

            try {
                foreign = receivers.execute(receiver, rubyToForeignArgumentsNode.executeConvert(args));
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @TruffleBoundary
        private DynamicObject translate(RubyContext context, UnsupportedTypeException e) {
            String message = "Wrong arguments: " +
                    Arrays.stream(e.getSuppliedValues()).map(Object::toString).collect(Collectors.joining(", "));
            return context.getCoreExceptions().typeError(message, this);
        }

        @TruffleBoundary
        private DynamicObject translate(RubyContext context, UnsupportedMessageException e) {
            return context.getCoreExceptions().typeError(e.getMessage(), this);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @CoreMethod(names = "execute_without_conversion", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object executeWithoutConversionForeignCached(TruffleObject receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached() BranchProfile exceptionProfile) {
            try {
                return receivers.execute(receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "invoke", onSingleton = true, required = 2, rest = true)
    public abstract static class InvokeNode extends RubySourceNode {

        public static InvokeNode create() {
            return InteropNodesFactory.InvokeNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier, Object[] args);

        @Specialization(limit = "getCacheLimit()")
        protected Object invokeCached(Object receiver, Object identifier, Object[] args,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object[] arguments = rubyToForeignArgumentsNode.executeConvert(args);

            final Object foreign;
            try {
                foreign = receivers.invokeMember(receiver, name, arguments);
            } catch (UnknownIdentifierException | UnsupportedMessageException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().noMethodErrorUnknownIdentifier(receiver, name, args, e, this));
            } catch (UnsupportedTypeException | ArityException e) {
                exceptionProfile.enter();
                throw new RaiseException(context, context.getCoreExceptions().argumentError(e.getMessage(), this));
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @CoreMethod(names = "instantiable?", onSingleton = true, required = 1)
    public abstract static class InstantiableNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean isInstantiable(TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isInstantiable(receiver);
        }

        @Fallback
        protected boolean isInstantiable(Object receiver) {
            return false;
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "new", onSingleton = true, required = 1, rest = true)
    public abstract static class NewNode extends RubySourceNode {

        public static NewNode create() {
            return InteropNodesFactory.NewNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object[] args);

        @Specialization(limit = "getCacheLimit()")
        protected Object newCached(Object receiver, Object[] args,
                @Cached RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile exceptionProfile) {
            final Object foreign;

            try {
                foreign = receivers.instantiate(receiver, rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException
                    | ArityException
                    | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @CoreMethod(names = "size?", onSingleton = true, required = 1)
    public abstract static class HasSizeNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean hasSize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasArrayElements(receiver);
        }

    }

    @CoreMethod(names = "array_size", onSingleton = true, required = 1)
    public abstract static class ArraySizeNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object arraySize(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {

            try {
                return receivers.getArraySize(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "is_string?", onSingleton = true, required = 1)
    public abstract static class IsStringNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isString(receiver);
        }
    }

    @CoreMethod(names = "as_string", onSingleton = true, required = 1)
    public abstract static class AsStringNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected DynamicObject asString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached FromJavaStringNode fromJavaStringNode) {
            try {
                return fromJavaStringNode.executeFromJavaString(receivers.asString(receiver));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    // TODO (pitr-ch 01-Apr-2019): turn conversion into argument
    @CoreMethod(names = "as_string_without_conversion", onSingleton = true, required = 1)
    public abstract static class AsStringWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected String asString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asString(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "boolean?", onSingleton = true, required = 1)
    public abstract static class IsBooleanNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isBoolean(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isBoolean(receiver);
        }
    }

    @CoreMethod(names = "as_boolean", onSingleton = true, required = 1)
    public abstract static class AsBooleanNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean asBoolean(Object receiver,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @CachedLibrary("receiver") InteropLibrary receivers) {

            try {
                return receivers.asBoolean(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "is_number?", onSingleton = true, required = 1)
    public abstract static class IsNumberNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isNumber(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNumber(receiver);
        }
    }

    @CoreMethod(names = "fits_in_int?", onSingleton = true, required = 1)
    public abstract static class FitsInIntNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean fitsInInt(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInInt(receiver);
        }
    }

    @CoreMethod(names = "fits_in_long?", onSingleton = true, required = 1)
    public abstract static class FitsInLongNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean fitsInLong(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInLong(receiver);
        }
    }

    @CoreMethod(names = "fits_in_double?", onSingleton = true, required = 1)
    public abstract static class FitsInDoubleNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean fitsInDouble(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInDouble(receiver);
        }
    }

    @CoreMethod(names = "as_int", onSingleton = true, required = 1)
    public abstract static class AsIntNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected int asInt(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asInt(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "as_long", onSingleton = true, required = 1)
    public abstract static class AsLongNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected long asLong(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asLong(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "as_double", onSingleton = true, required = 1)
    public abstract static class AsDoubleNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected double asDouble(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asDouble(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "null?", onSingleton = true, required = 1)
    public abstract static class NullNode extends RubySourceNode {

        public static NullNode create() {
            return InteropNodesFactory.NullNodeFactory.create(null);
        }

        abstract Object execute(Object receiver);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isNull(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNull(receiver);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }

    }

    @CoreMethod(names = "pointer?", onSingleton = true, required = 1)
    public abstract static class PointerNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean isPointer(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isPointer(receiver);
        }

    }

    @CoreMethod(names = "as_pointer", onSingleton = true, required = 1)
    public abstract static class AsPointerNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected long asPointer(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asPointer(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "to_native", onSingleton = true, required = 1)
    public abstract static class ToNativeNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Nil toNative(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            receivers.toNative(receiver);
            return Nil.INSTANCE;
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "read_member", onSingleton = true, required = 2)
    public abstract static class ReadMemberNode extends RubySourceNode {

        public static ReadMemberNode create() {
            return InteropNodesFactory.ReadMemberNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier);

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        protected Object readMember(Object receiver, DynamicObject identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object foreign;
            try {
                foreign = receivers.readMember(receiver, name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "read_array_element", onSingleton = true, required = 2)
    public abstract static class ReadArrayElementNode extends RubySourceNode {

        public static ReadArrayElementNode create() {
            return InteropNodesFactory.ReadArrayElementNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier);

        @Specialization(limit = "getCacheLimit()")
        protected Object readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object foreign;
            try {
                foreign = receivers.readArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "remove_array_element", onSingleton = true, required = 2)
    public abstract static class RemoveArrayElementNode extends RubySourceNode {

        public static ReadArrayElementNode create() {
            return InteropNodesFactory.ReadArrayElementNodeFactory.create(null);
        }

        abstract Nil execute(Object receiver, Object identifier);

        @Specialization(limit = "getCacheLimit()")
        protected Nil readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                receivers.removeArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return Nil.INSTANCE;
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    // TODO (pitr-ch 27-Mar-2019): break down
    @CoreMethod(names = "read_without_conversion", onSingleton = true, required = 2)
    public abstract static class ReadWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        protected Object readMember(TruffleObject receiver, DynamicObject identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile,
                @Cached ToJavaStringNode toJavaStringNode) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                return receivers.readMember(receiver, name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

        @Specialization(limit = "getCacheLimit()")
        protected Object readArrayElement(TruffleObject receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            try {
                return receivers.readArrayElement(receiver, identifier);
            } catch (InvalidArrayIndexException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "write_member", onSingleton = true, required = 3)
    public abstract static class WriteMemberNode extends RubySourceNode {

        public static WriteMemberNode create() {
            return InteropNodesFactory.WriteMemberNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier, Object value);

        @Specialization(
                guards = "isRubySymbol(identifier) || isRubyString(identifier)",
                limit = "getCacheLimit()")
        protected Object write(Object receiver, DynamicObject identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @CachedContext(RubyLanguage.class) RubyContext context,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached RubyToForeignNode valueToForeignNode,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.writeMember(receiver, name, valueToForeignNode.executeConvert(value));
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return value;
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "write_array_element", onSingleton = true, required = 3)
    public abstract static class WriteArrayElementNode extends RubySourceNode {

        public static WriteArrayElementNode create() {
            return InteropNodesFactory.WriteArrayElementNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier, Object value);

        @Specialization(limit = "getCacheLimit()")
        protected Object write(Object receiver, long identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached RubyToForeignNode valueToForeignNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                receivers.writeArrayElement(receiver, identifier, valueToForeignNode.executeConvert(value));
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return value;
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    // TODO (pitr-ch 01-Apr-2019): break down
    @CoreMethod(names = "remove", onSingleton = true, required = 2)
    public abstract static class RemoveNode extends InteropCoreMethodArrayArgumentsNode {

        abstract Object execute(TruffleObject receiver, Object identifier);

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        protected Object remove(TruffleObject receiver, DynamicObject identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.removeMember(receiver, name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedMessageException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnsuportedMessage(receiver, identifier, e, this));
            }

            return true;
        }

        @Specialization(limit = "getCacheLimit()")
        protected Object remove(TruffleObject receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            try {
                receivers.removeArrayElement(receiver, identifier);
            } catch (InvalidArrayIndexException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            // TODO (pitr-ch 29-Mar-2019): is it ok to always return true
            //  the remove no longer returns true/false
            return true;
        }
    }

    @CoreMethod(names = "keys?", onSingleton = true, required = 1)
    public abstract static class InteropHasKeysNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean hasKeys(TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMembers(receiver);
        }

        // TODO (pitr-ch 28-Mar-2019): delete this specialization and fix failing tests
        //   or rather implement members for primitive types, so Ruby Integer methods can be invoked on long
        @Specialization(guards = "!isTruffleObject(receiver)")
        protected Object hasKeys(Object receiver) {
            return true;
        }
    }

    @CoreMethod(names = "keys_without_conversion", onSingleton = true, required = 1, optional = 1)
    public abstract static class KeysNode extends InteropPrimitiveArrayArgumentsNode {

        protected abstract Object executeKeys(TruffleObject receiver, boolean internal);

        @Specialization
        protected Object keys(TruffleObject receiver, NotProvided internal) {
            return executeKeys(receiver, false);
        }

        @Specialization(limit = "getCacheLimit()")
        protected Object keys(TruffleObject receiver, boolean internal,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile exceptionProfile) {
            try {
                return receivers.getMembers(receiver, internal);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "is_member_readable?", onSingleton = true, required = 2)
    public abstract static class IsMemberReadableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberReadable(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberReadable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_modifiable?", onSingleton = true, required = 2)
    public abstract static class IsMemberModifiableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberModifiable(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberModifiable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_insertable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInsertableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberInsertable(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInsertable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_removable?", onSingleton = true, required = 2)
    public abstract static class IsMemberRemovableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberRemovable(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberRemovable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_invocable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInvocableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberInvocable(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInvocable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_internal?", onSingleton = true, required = 2)
    public abstract static class IsMemberInternalNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberInternal(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInternal(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_writable?", onSingleton = true, required = 2)
    public abstract static class IsMemberWritableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberWritable(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberWritable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_existing?", onSingleton = true, required = 2)
    public abstract static class IsMemberExistingNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberExisting(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberExisting(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_read_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberReadSideEffectsNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean hasMemberReadSideEffects(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberReadSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_write_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberWriteSideEffectsNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean hasMemberWriteSideEffects(TruffleObject receiver, DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberWriteSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "array_element_readable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementReadableNode extends InteropCoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isArrayElementReadable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementReadable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_modifiable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementModifiableNode extends InteropCoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isArrayElementModifiable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementModifiable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_insertable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementInsertableNode extends InteropCoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isArrayElementInsertable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementInsertable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_removable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementRemovableNode extends InteropCoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isArrayElementRemovable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementRemovable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_writable?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementWritableNode extends InteropCoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isArrayElementWritable(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementWritable(receiver, index);
        }
    }

    @CoreMethod(names = "array_element_existing?", onSingleton = true, required = 2)
    public abstract static class IsArrayElementExistingNode extends InteropCoreMethodArrayArgumentsNode {

        public abstract boolean execute(Object receiver, long index);

        @Specialization(limit = "getCacheLimit()")
        protected boolean isArrayElementExisting(Object receiver, long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementExisting(receiver, index);
        }
    }

    @CoreMethod(names = "export_without_conversion", onSingleton = true, required = 2)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "object", type = RubyNode.class)
    public abstract static class ExportWithoutConversionNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceNameToString(RubyNode newName) {
            return ToJavaStringNodeGen.RubyNodeWrapperNodeGen.create(newName);
        }

        @TruffleBoundary
        @Specialization
        protected Object export(String name, Object object) {
            getContext().getInteropManager().exportObject(name, object);
            return object;
        }

    }

    @CoreMethod(names = "import_without_conversion", onSingleton = true, required = 1)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ImportWithoutConversionNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceNameToString(RubyNode newName) {
            return ToJavaStringNodeGen.RubyNodeWrapperNodeGen.create(newName);
        }

        @Specialization
        protected Object importObject(String name,
                @Cached BranchProfile errorProfile) {
            final Object value = doImport(name);
            if (value != null) {
                return value;
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().nameErrorImportNotFound(name, this));
            }
        }

        @TruffleBoundary
        private Object doImport(String name) {
            return getContext().getInteropManager().importObject(name);
        }

    }

    @CoreMethod(names = "mime_type_supported?", onSingleton = true, required = 1)
    public abstract static class MimeTypeSupportedNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(mimeType)")
        protected boolean isMimeTypeSupported(DynamicObject mimeType) {
            return getContext().getEnv().isMimeTypeSupported(StringOperations.getString(mimeType));
        }

    }

    @CoreMethod(names = "eval", onSingleton = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = {
                        "isRubyString(mimeType)",
                        "isRubyString(source)",
                        "mimeTypeEqualNode.execute(rope(mimeType), cachedMimeType)",
                        "sourceEqualNode.execute(rope(source), cachedSource)" },
                limit = "getCacheLimit()")
        protected Object evalCached(DynamicObject mimeType, DynamicObject source,
                @Cached("privatizeRope(mimeType)") Rope cachedMimeType,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("create(parse(mimeType, source))") DirectCallNode callNode,
                @Cached RopeNodes.EqualNode mimeTypeEqualNode,
                @Cached RopeNodes.EqualNode sourceEqualNode) {
            return callNode.call(EMPTY_ARGUMENTS);
        }

        @Specialization(guards = { "isRubyString(mimeType)", "isRubyString(source)" }, replaces = "evalCached")
        protected Object evalUncached(DynamicObject mimeType, DynamicObject source,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(mimeType, source), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(DynamicObject mimeType, DynamicObject code) {
            final String mimeTypeString = StringOperations.getString(mimeType);
            final String codeString = StringOperations.getString(code);
            String language = Source.findLanguage(mimeTypeString);
            if (language == null) {
                // Give the original string to get the nice exception from Truffle
                language = mimeTypeString;
            }
            final Source source = Source.newBuilder(language, codeString, "(eval)").build();
            try {
                return getContext().getEnv().parsePublic(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        protected int getCacheLimit() {
            return getContext().getOptions().EVAL_CACHE;
        }

    }

    @Primitive(name = "interop_eval_nfi")
    public abstract static class InteropEvalNFINode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyString(code)")
        protected Object evalNFI(DynamicObject code,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(code), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(DynamicObject code) {
            final String codeString = StringOperations.getString(code);
            final Source source = Source.newBuilder("nfi", codeString, "(eval)").build();

            try {
                return getContext().getEnv().parseInternal(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

    }

    @CoreMethod(names = "java_string?", onSingleton = true, required = 1)
    public abstract static class InteropIsJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isJavaString(Object value) {
            return value instanceof String;
        }

    }

    @CoreMethod(names = "java_instanceof?", onSingleton = true, required = 2)
    public abstract static class InteropJavaInstanceOfNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isJavaObject(object)", "isJavaClassOrInterface(boxedJavaClass)" })
        protected boolean javaInstanceOfJava(Object object, TruffleObject boxedJavaClass) {
            final Object hostInstance = getContext().getEnv().asHostObject(object);
            if (hostInstance == null) {
                return false;
            } else {
                final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
                return javaClass.isAssignableFrom(hostInstance.getClass());
            }
        }

        @Specialization(guards = { "!isJavaObject(object)", "isJavaClassOrInterface(boxedJavaClass)" })
        protected boolean javaInstanceOfNotJava(Object object, TruffleObject boxedJavaClass) {
            final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
            return javaClass.isInstance(object);
        }

        protected boolean isJavaObject(Object object) {
            return object instanceof TruffleObject && getContext().getEnv().isHostObject(object);
        }

        protected boolean isJavaClassOrInterface(TruffleObject object) {
            return getContext().getEnv().isHostObject(object) &&
                    getContext().getEnv().asHostObject(object) instanceof Class<?>;
        }

    }

    @CoreMethod(names = "to_java_string", onSingleton = true, required = 1)
    public abstract static class InteropToJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object toJavaString(Object value,
                @Cached RubyToForeignNode toForeignNode) {
            return toForeignNode.executeConvert(value);
        }

    }

    @CoreMethod(names = "from_java_string", onSingleton = true, required = 1)
    public abstract static class InteropFromJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object fromJavaString(Object value,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            return foreignToRubyNode.executeConvert(value);
        }

    }

    @Primitive(name = "interop_to_java_array")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyArray(array)", "stores.accepts(getStore(array))" })
        protected Object toJavaArray(DynamicObject array,
                @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
            return getContext().getEnv().asGuestValue(stores.toJavaArrayCopy(
                    Layouts.ARRAY.getStore(array),
                    Layouts.ARRAY.getSize(array)));
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected Object coerce(DynamicObject array) {
            return FAILURE;
        }

    }

    @Primitive(name = "interop_to_java_list")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaListNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyArray(array)", "stores.accepts(getStore(array))" })
        protected Object toJavaList(DynamicObject array,
                @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
            int size = Layouts.ARRAY.getSize(array);
            Object[] copy = stores.boxedCopyOfRange(Layouts.ARRAY.getStore(array), 0, size);
            return getContext().getEnv().asGuestValue(Arrays.asList(copy));
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected Object coerce(DynamicObject array) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "deproxy", onSingleton = true, required = 1)
    public abstract static class DeproxyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isJavaObject(object)")
        protected Object deproxyJavaObject(TruffleObject object) {
            return getContext().getEnv().asHostObject(object);
        }

        @Specialization(guards = "!isJavaObject(object)")
        protected Object deproxyNotJavaObject(TruffleObject object) {
            return object;
        }

        @Specialization(guards = "!isTruffleObject(object)")
        protected Object deproxyNotTruffle(Object object) {
            return object;
        }

        protected boolean isJavaObject(TruffleObject object) {
            return getContext().getEnv().isHostObject(object);
        }

    }

    @CoreMethod(names = "foreign?", onSingleton = true, required = 1)
    public abstract static class InteropIsForeignNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isForeign(Object value) {
            return RubyGuards.isForeignObject(value);
        }

    }

    @CoreMethod(names = "java?", onSingleton = true, required = 1)
    public abstract static class InteropIsJavaNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isJava(Object value) {
            return getContext().getEnv().isHostObject(value);
        }

    }

    @CoreMethod(names = "java_class?", onSingleton = true, required = 1)
    public abstract static class InteropIsJavaClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isJavaClass(Object value) {
            return getContext().getEnv().isHostObject(value) &&
                    getContext().getEnv().asHostObject(value) instanceof Class;
        }

    }

    @CoreMethod(names = "meta_object", onSingleton = true, required = 1)
    public abstract static class InteropMetaObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object metaObject(Object value) {
            return getContext().getLanguage().findMetaObject(getContext(), value);
        }

    }

    @CoreMethod(names = "java_type", onSingleton = true, required = 1)
    public abstract static class JavaTypeNode extends CoreMethodArrayArgumentsNode {

        // TODO CS 17-Mar-18 we should cache this in the future

        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(name)")
        protected Object javaTypeSymbol(DynamicObject name) {
            return javaType(Layouts.SYMBOL.getString(name));
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(name)")
        protected Object javaTypeString(DynamicObject name) {
            return javaType(StringOperations.getString(name));
        }

        private Object javaType(String name) {
            final TruffleLanguage.Env env = getContext().getEnv();

            if (!env.isHostLookupAllowed()) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().securityError("host access is not allowed", this));
            }

            return env.lookupHostSymbol(name);
        }

    }

    @CoreMethod(names = "logging_foreign_object", onSingleton = true)
    public abstract static class LoggingForeignObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected TruffleObject loggingForeignObject() {
            return new LoggingForeignObject();
        }

    }

    @CoreMethod(names = "to_string", onSingleton = true, required = 1)
    public abstract static class ToStringNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected DynamicObject toString(Object value) {
            return makeStringNode.executeMake(String.valueOf(value), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "identity_hash_code", onSingleton = true, required = 1)
    public abstract static class InteropIdentityHashCodeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        protected int identityHashCode(Object value) {
            final int code = System.identityHashCode(value);
            assert code >= 0;
            return code;
        }

    }

    @CoreMethod(names = "polyglot_bindings_access?", onSingleton = true)
    public abstract static class IsPolyglotBindingsAccessAllowedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isPolyglotBindingsAccessAllowed() {
            return getContext().getEnv().isPolyglotBindingsAccessAllowed();
        }

    }

}
