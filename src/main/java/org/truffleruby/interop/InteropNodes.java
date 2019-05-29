/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ArityException;
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
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayOperationNodes;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.shared.TruffleRuby;

@CoreClass("Truffle::Interop")
public abstract class InteropNodes {

    // TODO (pitr-ch 27-Mar-2019): remove create()
    // TODO (pitr-ch 27-Mar-2019): rename methods to match new messages
    // TODO (pitr-ch 27-Mar-2019): break down to new messages

    @CoreMethod(names = "import_file", isModuleFunction = true, required = 1)
    public abstract static class ImportFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(fileName)")
        public Object importFile(DynamicObject fileName) {
            try {
                final TruffleFile file = getContext().getEnv().getTruffleFile(StringOperations.getString(fileName).intern());
                final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, file).build();
                getContext().getEnv().parse(source).call();
            } catch (IOException e) {
                throw new JavaException(e);
            }

            return nil();
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

    @CoreMethod(names = "executable?", isModuleFunction = true, required = 1)
    public abstract static class IsExecutableNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public boolean isExecutable(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isExecutable(receiver);
        }
    }

    @CoreMethod(names = "execute", isModuleFunction = true, required = 1, rest = true)
    public abstract static class ExecuteNode extends InteropCoreMethodArrayArgumentsNode {

        abstract Object execute(TruffleObject receiver, Object[] args);

        public static ExecuteNode create() {
            return InteropNodesFactory.ExecuteNodeFactory.create(null);
        }

        @Specialization(limit = "getCacheLimit()")
        public Object executeForeignCached(
                TruffleObject receiver,
                Object[] args,
                @Cached RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile exceptionProfile,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            final Object foreign;

            try {
                foreign = receivers.execute(receiver, rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException e) {
                exceptionProfile.enter();
                throw new RaiseException(getContext(), translate(e));
            } catch (ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @TruffleBoundary
        private DynamicObject translate(UnsupportedTypeException e) {
            return coreExceptions().typeError(
                    "Wrong arguments: " + Arrays.stream(e.getSuppliedValues()).map(Object::toString).collect(Collectors.joining(
                            ", ")),
                    this);
        }

    }

    @CoreMethod(names = "execute_without_conversion", isModuleFunction = true, required = 1, rest = true)
    public abstract static class ExecuteWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public Object executeWithoutConversionForeignCached(
                TruffleObject receiver,
                Object[] args,
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

    @CoreMethod(names = "invoke", isModuleFunction = true, required = 2, rest = true)
    public abstract static class InvokeNode extends InteropCoreMethodArrayArgumentsNode {

        public static InvokeNode create() {
            return InteropNodesFactory.InvokeNodeFactory.create(null);
        }

        abstract Object execute(TruffleObject receiver, Object identifier, Object[] args);

        @Specialization(limit = "getCacheLimit()")
        public Object invokeCached(
                TruffleObject receiver,
                Object identifier,
                Object[] args,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object[] arguments = rubyToForeignArgumentsNode.executeConvert(args);

            final Object foreign;
            try {
                foreign = receivers.invokeMember(receiver, name, arguments);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().noMethodErrorUnknownIdentifier(receiver, name, args, e, this));
            } catch (UnsupportedTypeException
                    | ArityException
                    | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    @CoreMethod(names = "instantiable?", isModuleFunction = true, required = 1)
    public abstract static class InstantiableNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public boolean isInstantiable(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isInstantiable(receiver);
        }

        @Fallback
        public boolean isInstantiable(Object receiver) {
            return false;
        }
    }

    @CoreMethod(names = "new", isModuleFunction = true, required = 1, rest = true)
    public abstract static class NewNode extends InteropCoreMethodArrayArgumentsNode {

        public static NewNode create() {
            return InteropNodesFactory.NewNodeFactory.create(null);
        }

        abstract Object execute(TruffleObject receiver, Object[] args);

        @Specialization(limit = "getCacheLimit()")
        public Object newCached(
                TruffleObject receiver,
                Object[] args,
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

    }

    @CoreMethod(names = "size?", isModuleFunction = true, required = 1)
    public abstract static class HasSizeNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public boolean hasSize(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasArrayElements(receiver);
        }

    }

    @CoreMethod(names = "size", isModuleFunction = true, required = 1)
    public abstract static class SizeNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization
        public Object size(String receiver) {
            return receiver.length();
        }

        @Specialization(limit = "getCacheLimit()")
        public Object size(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile exceptionProfile) {
            try {
                return receivers.getArraySize(receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "is_string?", isModuleFunction = true, required = 1)
    public abstract static class IsStringNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isString(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isString(receiver);
        }
    }

    @CoreMethod(names = "as_string", isModuleFunction = true, required = 1)
    public abstract static class AsStringNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public DynamicObject asString(
                Object receiver,
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
    @CoreMethod(names = "as_string_without_conversion", isModuleFunction = true, required = 1)
    public abstract static class AsStringWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public String asString(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asString(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_boolean?", isModuleFunction = true, required = 1)
    public abstract static class IsBooleanNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isBoolean(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isBoolean(receiver);
        }
    }

    @CoreMethod(names = "as_boolean", isModuleFunction = true, required = 1)
    public abstract static class AsBooleanNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean asBoolean(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asBoolean(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_number?", isModuleFunction = true, required = 1)
    public abstract static class IsNumberNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isNumber(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNumber(receiver);
        }
    }

    @CoreMethod(names = "fits_in_int?", isModuleFunction = true, required = 1)
    public abstract static class FitsInIntNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean fitsInInt(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInInt(receiver);
        }
    }

    @CoreMethod(names = "fits_in_long?", isModuleFunction = true, required = 1)
    public abstract static class FitsInLongNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean fitsInLong(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInLong(receiver);
        }
    }

    @CoreMethod(names = "fits_in_double?", isModuleFunction = true, required = 1)
    public abstract static class FitsInDoubleNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean fitsInDouble(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.fitsInDouble(receiver);
        }
    }

    @CoreMethod(names = "as_int", isModuleFunction = true, required = 1)
    public abstract static class AsIntNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public int asInt(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asInt(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "as_long", isModuleFunction = true, required = 1)
    public abstract static class AsLongNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public long asLong(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asLong(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "as_double", isModuleFunction = true, required = 1)
    public abstract static class AsDoubleNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public double asDouble(
                Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            try {
                return receivers.asDouble(receiver);
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "null?", isModuleFunction = true, required = 1)
    public abstract static class NullNode extends InteropCoreMethodArrayArgumentsNode {

        public static NullNode create() {
            return InteropNodesFactory.NullNodeFactory.create(null);
        }

        abstract Object execute(TruffleObject receiver);

        @Specialization(limit = "getCacheLimit()")
        public boolean isNull(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isNull(receiver);
        }

        @Fallback
        public boolean isNull(Object receiver) {
            return false;
        }

    }

    @CoreMethod(names = "pointer?", isModuleFunction = true, required = 1)
    public abstract static class PointerNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public boolean isPointer(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isPointer(receiver);
        }

        @Fallback
        public boolean isPointer(Object receiver) {
            return false;
        }

    }

    @CoreMethod(names = "as_pointer", isModuleFunction = true, required = 1)
    public abstract static class AsPointerNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public Object asPointer(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile exceptionProfile) {
            try {
                return receivers.asPointer(receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
            }
        }
    }

    @CoreMethod(names = "to_native", isModuleFunction = true, required = 1)
    public abstract static class ToNativeNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public Object toNative(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            receivers.toNative(receiver);
            // TODO (pitr-ch 27-Mar-2019): return nil instead?
            return receiver;
        }

    }

    // TODO (pitr-ch 27-Mar-2019): break down
    @CoreMethod(names = "read", isModuleFunction = true, required = 2)
    public abstract static class ReadNode extends InteropCoreMethodArrayArgumentsNode {

        public static ReadNode create() {
            return InteropNodesFactory.ReadNodeFactory.create(null);
        }

        abstract Object execute(TruffleObject receiver, Object identifier);

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        public Object readMember(
                TruffleObject receiver,
                DynamicObject identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
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
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @Specialization(limit = "getCacheLimit()")
        public Object readArrayElement(
                TruffleObject receiver,
                long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            final Object foreign;
            try {
                foreign = receivers.readArrayElement(receiver, identifier);
            } catch (InvalidArrayIndexException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }
    }

    // TODO (pitr-ch 27-Mar-2019): break down
    @CoreMethod(names = "read_without_conversion", isModuleFunction = true, required = 2)
    public abstract static class ReadWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        public Object readMember(
                TruffleObject receiver,
                DynamicObject identifier,
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
        public Object readArrayElement(
                TruffleObject receiver,
                long identifier,
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

    // TODO (pitr-ch 27-Mar-2019): break down
    @CoreMethod(names = "write", isModuleFunction = true, required = 3)
    public abstract static class WriteNode extends InteropCoreMethodArrayArgumentsNode {

        public static WriteNode create() {
            return InteropNodesFactory.WriteNodeFactory.create(null);
        }

        abstract Object execute(TruffleObject receiver, Object identifier, Object value);

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        public Object write(
                TruffleObject receiver,
                DynamicObject identifier,
                Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
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
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
            // TODO (pitr-ch 29-Mar-2019): is it ok to always return the value,
            //  the write no longer returns its own value
            return value;
        }

        @Specialization(limit = "getCacheLimit()")
        public Object write(
                TruffleObject receiver,
                long identifier, // TODO (pitr-ch 01-Apr-2019): allow only long? (unify other similar cases)
                Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached RubyToForeignNode valueToForeignNode,
                @Cached BranchProfile unknownIdentifierProfile,
                @Cached BranchProfile exceptionProfile) {
            try {
                receivers.writeArrayElement(receiver, identifier, valueToForeignNode.executeConvert(value));
            } catch (InvalidArrayIndexException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorUnknownIdentifier(receiver, identifier, e, this));
            } catch (UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
            // TODO (pitr-ch 29-Mar-2019): is it ok to always return the value,
            //  the write no longer returns its own value
            return value;
        }
    }

    // TODO (pitr-ch 01-Apr-2019): break down
    @CoreMethod(names = "remove", isModuleFunction = true, required = 2)
    public abstract static class RemoveNode extends InteropCoreMethodArrayArgumentsNode {

        abstract Object execute(TruffleObject receiver, Object identifier);

        @Specialization(guards = "isRubySymbol(identifier) || isRubyString(identifier)", limit = "getCacheLimit()")
        public Object remove(
                TruffleObject receiver,
                DynamicObject identifier,
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
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return true;
        }

        @Specialization(limit = "getCacheLimit()")
        public Object remove(
                TruffleObject receiver,
                long identifier,
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

        // @Specialization
        // public Object write(TruffleObject receiver, int identifier) {
        //     // TODO (pitr-ch 01-Apr-2019): why is it needed?
        //     return execute(receiver, (long) identifier);
        // }
    }

    @CoreMethod(names = "keys?", isModuleFunction = true, required = 1)
    public abstract static class InteropHasKeysNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public boolean hasKeys(
                TruffleObject receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMembers(receiver);
        }

        @Specialization(guards = "!isTruffleObject(receiver)")
        // TODO (pitr-ch 28-Mar-2019): really?
        public Object hasKeys(Object receiver) {
            return true;
        }

    }

    @CoreMethod(names = "keys_without_conversion", isModuleFunction = true, required = 1, optional = 1)
    public abstract static class KeysNode extends InteropPrimitiveArrayArgumentsNode {

        protected abstract Object executeKeys(TruffleObject receiver, boolean internal);

        @Specialization
        public Object keys(TruffleObject receiver, NotProvided internal) {
            return executeKeys(receiver, false);
        }

        @Specialization(limit = "getCacheLimit()")
        public Object keys(
                TruffleObject receiver,
                boolean internal,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return receivers.getMembers(receiver, internal);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "is_member_readable?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberReadableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberReadable(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberReadable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_modifiable?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberModifiableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberModifiable(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberModifiable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_insertable?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberInsertableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberInsertable(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInsertable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_removable?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberRemovableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberRemovable(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberRemovable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_invocable?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberInvocableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberInvocable(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInvocable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_internal?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberInternalNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberInternal(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInternal(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_writable?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberWritableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberWritable(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberWritable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_existing?", isModuleFunction = true, required = 2)
    public abstract static class IsMemberExistingNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isMemberExisting(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberExisting(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_read_side_effects?", isModuleFunction = true, required = 2)
    public abstract static class HasMemberReadSideEffectsNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean hasMemberReadSideEffects(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberReadSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_write_side_effects?", isModuleFunction = true, required = 2)
    public abstract static class HasMemberWriteSideEffectsNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean hasMemberWriteSideEffects(
                TruffleObject receiver,
                DynamicObject name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberWriteSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_array_element_readable?", isModuleFunction = true, required = 2)
    public abstract static class IsArrayElementReadableNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        public boolean isArrayElementReadable(
                TruffleObject receiver,
                long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementReadable(receiver, index);
        }

        public abstract boolean execute(TruffleObject receiver, long index);

        @Specialization(limit = "getCacheLimit()", guards = { "indexes.isNumber(index)", "indexes.fitsInLong(index)" })
        public boolean isArrayElementReadable(
                TruffleObject receiver,
                TruffleObject index,
                @CachedLibrary("index") InteropLibrary indexes) {
            try {
                return execute(receiver, indexes.asLong(index));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_array_element_modifiable?", isModuleFunction = true, required = 2)
    public abstract static class IsArrayElementModifiableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isArrayElementModifiable(
                TruffleObject receiver,
                long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementModifiable(receiver, index);
        }

        public abstract boolean execute(TruffleObject receiver, long index);

        @Specialization(limit = "getCacheLimit()", guards = { "indexes.isNumber(index)", "indexes.fitsInLong(index)" })
        public boolean isArrayElementModifiable(
                TruffleObject receiver,
                TruffleObject index,
                @CachedLibrary("index") InteropLibrary indexes) {
            try {
                return execute(receiver, indexes.asLong(index));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_array_element_insertable?", isModuleFunction = true, required = 2)
    public abstract static class IsArrayElementInsertableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isArrayElementInsertable(
                TruffleObject receiver,
                long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementInsertable(receiver, index);
        }

        public abstract boolean execute(TruffleObject receiver, long index);

        @Specialization(limit = "getCacheLimit()", guards = { "indexes.isNumber(index)", "indexes.fitsInLong(index)" })
        public boolean isArrayElementInsertable(
                TruffleObject receiver,
                TruffleObject index,
                @CachedLibrary("index") InteropLibrary indexes) {
            try {
                return execute(receiver, indexes.asLong(index));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_array_element_removable?", isModuleFunction = true, required = 2)
    public abstract static class IsArrayElementRemovableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isArrayElementRemovable(
                TruffleObject receiver,
                long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementRemovable(receiver, index);
        }

        public abstract boolean execute(TruffleObject receiver, long index);

        @Specialization(limit = "getCacheLimit()", guards = { "indexes.isNumber(index)", "indexes.fitsInLong(index)" })
        public boolean isArrayElementRemovable(
                TruffleObject receiver,
                TruffleObject index,
                @CachedLibrary("index") InteropLibrary indexes) {
            try {
                return execute(receiver, indexes.asLong(index));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_array_element_writable?", isModuleFunction = true, required = 2)
    public abstract static class IsArrayElementWritableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isArrayElementWritable(
                TruffleObject receiver,
                long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementWritable(receiver, index);
        }

        public abstract boolean execute(TruffleObject receiver, long index);

        @Specialization(limit = "getCacheLimit()", guards = { "indexes.isNumber(index)", "indexes.fitsInLong(index)" })
        public boolean isArrayElementWritable(
                TruffleObject receiver,
                TruffleObject index,
                @CachedLibrary("index") InteropLibrary indexes) {
            try {
                return execute(receiver, indexes.asLong(index));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "is_array_element_existing?", isModuleFunction = true, required = 2)
    public abstract static class IsArrayElementExistingNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        public boolean isArrayElementExisting(
                TruffleObject receiver,
                long index,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isArrayElementExisting(receiver, index);
        }

        public abstract boolean execute(TruffleObject receiver, long index);

        @Specialization(limit = "getCacheLimit()", guards = { "indexes.isNumber(index)", "indexes.fitsInLong(index)" })
        public boolean isArrayElementExisting(
                TruffleObject receiver,
                TruffleObject index,
                @CachedLibrary("index") InteropLibrary indexes) {
            try {
                return execute(receiver, indexes.asLong(index));
            } catch (UnsupportedMessageException e) {
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "export_without_conversion", isModuleFunction = true, required = 2)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "object", type = RubyNode.class)
    public abstract static class ExportWithoutConversionNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceNameToString(RubyNode newName) {
            return ToJavaStringNodeGen.create(newName);
        }

        @TruffleBoundary
        @Specialization
        public Object export(String name, Object object) {
            getContext().getInteropManager().exportObject(name, object);
            return object;
        }

    }

    @CoreMethod(names = "import_without_conversion", isModuleFunction = true, required = 1)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ImportWithoutConversionNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceNameToString(RubyNode newName) {
            return ToJavaStringNodeGen.create(newName);
        }

        @Specialization
        public Object importObject(
                String name,
                @Cached("create()") BranchProfile errorProfile) {
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

    @CoreMethod(names = "mime_type_supported?", isModuleFunction = true, required = 1)
    public abstract static class MimeTypeSupportedNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(mimeType)")
        public boolean isMimeTypeSupported(DynamicObject mimeType) {
            return getContext().getEnv().isMimeTypeSupported(StringOperations.getString(mimeType));
        }

    }

    @CoreMethod(names = "eval", isModuleFunction = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = {
                "isRubyString(mimeType)",
                "isRubyString(source)",
                "mimeTypeEqualNode.execute(rope(mimeType), cachedMimeType)",
                "sourceEqualNode.execute(rope(source), cachedSource)"
        }, limit = "getCacheLimit()")
        public Object evalCached(
                DynamicObject mimeType,
                DynamicObject source,
                @Cached("privatizeRope(mimeType)") Rope cachedMimeType,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("create(parse(mimeType, source))") DirectCallNode callNode,
                @Cached("create()") RopeNodes.EqualNode mimeTypeEqualNode,
                @Cached("create()") RopeNodes.EqualNode sourceEqualNode
        ) {
            return callNode.call(RubyNode.EMPTY_ARGUMENTS);
        }

        @Specialization(guards = { "isRubyString(mimeType)", "isRubyString(source)" }, replaces = "evalCached")
        public Object evalUncached(
                DynamicObject mimeType, DynamicObject source,
                @Cached("create()") IndirectCallNode callNode) {
            return callNode.call(parse(mimeType, source), RubyNode.EMPTY_ARGUMENTS);
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
                return getContext().getEnv().parse(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        protected int getCacheLimit() {
            return getContext().getOptions().EVAL_CACHE;
        }

    }

    @CoreMethod(names = "java_string?", isModuleFunction = true, required = 1)
    public abstract static class InteropIsJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isJavaString(Object value) {
            return value instanceof String;
        }

    }

    @CoreMethod(names = "java_instanceof?", isModuleFunction = true, required = 2)
    public abstract static class InteropJavaInstanceOfNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = {
                "isJavaObject(boxedInstance)",
                "isJavaClassOrInterface(boxedJavaClass)"
        })
        public boolean javaInstanceOfJava(Object boxedInstance, TruffleObject boxedJavaClass) {
            final Object hostInstance = getContext().getEnv().asHostObject(boxedInstance);
            if (hostInstance == null) {
                return false;
            } else {
                final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
                return javaClass.isAssignableFrom(hostInstance.getClass());
            }
        }

        @Specialization(guards = {
                "!isJavaObject(instance)",
                "isJavaClassOrInterface(boxedJavaClass)"
        })
        public boolean javaInstanceOfNotJava(Object instance, TruffleObject boxedJavaClass) {
            final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
            return javaClass.isInstance(instance);
        }

        protected boolean isJavaObject(Object object) {
            return object instanceof TruffleObject && getContext().getEnv().isHostObject(object);
        }

        protected boolean isJavaClassOrInterface(TruffleObject object) {
            return getContext().getEnv().isHostObject(object)
                    && getContext().getEnv().asHostObject(object) instanceof Class<?>;
        }

    }

    @CoreMethod(names = "to_java_string", isModuleFunction = true, required = 1)
    public abstract static class InteropToJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object toJavaString(
                Object value,
                @Cached("create()") RubyToForeignNode toForeignNode) {
            return toForeignNode.executeConvert(value);
        }

    }

    @CoreMethod(names = "from_java_string", isModuleFunction = true, required = 1)
    public abstract static class InteropFromJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object fromJavaString(
                Object value,
                @Cached("createForeignToRubyNode()") ForeignToRubyNode foreignToRubyNode) {
            return foreignToRubyNode.executeConvert(value);
        }

        protected ForeignToRubyNode createForeignToRubyNode() {
            return ForeignToRubyNodeGen.create();
        }

    }

    @Primitive(name = "to_java_array")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyArray(array)", "strategy.matches(array)" }, limit = "STORAGE_STRATEGIES")
        public Object toJavaArray(
                DynamicObject interopModule, DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("strategy.copyStoreNode()") ArrayOperationNodes.ArrayCopyStoreNode copyStoreNode) {
            return getContext().getEnv().asGuestValue(copyStoreNode.execute(
                    Layouts.ARRAY.getStore(array),
                    Layouts.ARRAY.getSize(array)));
        }

        @Specialization(guards = "!isRubyArray(object)")
        public Object coerce(DynamicObject interopModule, DynamicObject object) {
            return FAILURE;
        }

    }

    @Primitive(name = "to_java_list")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaListNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyArray(array)", "strategy.matches(array)" }, limit = "STORAGE_STRATEGIES")
        public Object toJavaList(
                DynamicObject interopModule, DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("strategy.boxedCopyNode()") ArrayOperationNodes.ArrayBoxedCopyNode boxedCopyNode) {
            return getContext().getEnv().asGuestValue(Arrays.asList(boxedCopyNode.execute(
                    Layouts.ARRAY.getStore(array),
                    Layouts.ARRAY.getSize(array))));
        }

        @Specialization(guards = "!isRubyArray(object)")
        public Object coerce(DynamicObject interopModule, DynamicObject object) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "deproxy", isModuleFunction = true, required = 1)
    public abstract static class DeproxyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isJavaObject(object)")
        public Object deproxyJavaObject(TruffleObject object) {
            return getContext().getEnv().asHostObject(object);
        }

        @Specialization(guards = "!isJavaObject(object)")
        public Object deproxyNotJavaObject(TruffleObject object) {
            return object;
        }

        @Specialization(guards = "!isTruffleObject(object)")
        public Object deproxyNotTruffle(Object object) {
            return object;
        }

        protected boolean isJavaObject(TruffleObject object) {
            return getContext().getEnv().isHostObject(object);
        }

    }

    @CoreMethod(names = "foreign?", isModuleFunction = true, required = 1)
    public abstract static class InteropIsForeignNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isForeign(Object value) {
            return RubyGuards.isForeignObject(value);
        }

    }

    @CoreMethod(names = "java?", isModuleFunction = true, required = 1)
    public abstract static class InteropIsJavaNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isJava(Object value) {
            return getContext().getEnv().isHostObject(value);
        }

    }

    @CoreMethod(names = "java_class?", isModuleFunction = true, required = 1)
    public abstract static class InteropIsJavaClassNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isJavaClass(Object value) {
            return getContext().getEnv().isHostObject(value)
                    && getContext().getEnv().asHostObject(value) instanceof Class;
        }

    }

    @CoreMethod(names = "meta_object", isModuleFunction = true, required = 1)
    public abstract static class InteropMetaObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object metaObject(Object value) {
            return getContext().getLanguage().findMetaObject(getContext(), value);
        }

    }

    @CoreMethod(names = "java_type", isModuleFunction = true, required = 1)
    public abstract static class JavaTypeNode extends CoreMethodArrayArgumentsNode {

        // TODO CS 17-Mar-18 we should cache this in the future

        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(name)")
        public Object javaTypeSymbol(DynamicObject name) {
            return javaType(Layouts.SYMBOL.getString(name));
        }

        @TruffleBoundary
        @Specialization(guards = "isRubyString(name)")
        public Object javaTypeString(DynamicObject name) {
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
        public TruffleObject loggingForeignObject() {
            return new LoggingForeignObject();
        }

    }

    @CoreMethod(names = "to_string", onSingleton = true, required = 1)
    public abstract static class ToStringNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject toString(Object value) {
            return makeStringNode.executeMake(String.valueOf(value), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "identity_hash_code", isModuleFunction = true, required = 1)
    public abstract static class InteropIdentityHashCodeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        public int identityHashCode(Object value) {
            final int code = System.identityHashCode(value);
            assert code >= 0;
            return code;
        }

    }

    @CoreMethod(names = "polyglot_access?", onSingleton = true)
    public abstract static class IsPolyglotAccessAllowedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isPolyglotAccessAllowed() {
            return getContext().getEnv().isPolyglotAccessAllowed();
        }

    }

}
