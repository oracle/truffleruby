/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.KeyInfo;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnknownIdentifierException;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;

import java.io.File;
import java.io.IOException;

@CoreClass("Truffle::Interop")
public abstract class InteropNodes {

    @CoreMethod(names = "import_file", isModuleFunction = true, required = 1)
    public abstract static class ImportFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(fileName)")
        public Object importFile(DynamicObject fileName) {
            try {
                final Source sourceObject = Source.newBuilder(new File(fileName.toString().intern())).build();
                getContext().getEnv().parse(sourceObject).call();
            } catch (IOException e) {
                throw new JavaException(e);
            }

            return nil();
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "executable?", isModuleFunction = true, required = 1)
    public abstract static class IsExecutableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isExecutable(
                TruffleObject receiver,
                @Cached("IS_EXECUTABLE.createNode()") Node isExecutableNode) {
            return ForeignAccess.sendIsExecutable(isExecutableNode, receiver);
        }

    }

    @CoreMethod(names = "execute", isModuleFunction = true, required = 1, rest = true)
    public abstract static class ExecuteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = "args.length == cachedArgsLength",
                limit = "getCacheLimit()"
        )
        public Object executeForeignCached(TruffleObject receiver, Object[] args,
                @Cached("args.length") int cachedArgsLength,
                @Cached("create()") RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @Cached("createExecuteNode(cachedArgsLength)") Node executeNode,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            final Object foreign;

            try {
                foreign = ForeignAccess.sendExecute(
                        executeNode,
                        receiver,
                        rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @Specialization(replaces = "executeForeignCached")
        public Object executeForeignUncached(TruffleObject receiver, Object[] args,
                @Cached("create()") RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            Log.notOptimizedOnce("megamorphic interop EXECUTE message send");

            final Node executeNode = createExecuteNode(args.length);

            final Object foreign;

            try {
                foreign = ForeignAccess.sendExecute(
                        executeNode,
                        receiver,
                        rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @TruffleBoundary
        protected Node createExecuteNode(int argsLength) {
            return Message.createExecute(argsLength).createNode();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INTEROP_EXECUTE_CACHE;
        }

    }

    @CoreMethod(names = "execute_without_conversion", isModuleFunction = true, required = 1, rest = true)
    public abstract static class ExecuteWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = "args.length == cachedArgsLength",
                limit = "getCacheLimit()"
        )
        public Object executeWithoutConversionForeignCached(TruffleObject receiver, Object[] args,
                @Cached("args.length") int cachedArgsLength,
                @Cached("createExecuteNode(cachedArgsLength)") Node executeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendExecute(executeNode, receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

        @Specialization(replaces = "executeWithoutConversionForeignCached")
        public Object executeWithoutConversionForeignUncached(TruffleObject receiver, Object[] args,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            Log.notOptimizedOnce("megamorphic interop EXECUTE without conversion message send");

            final Node executeNode = createExecuteNode(args.length);

            final Object foreign;

            try {
                foreign = ForeignAccess.sendExecute(executeNode, receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @TruffleBoundary
        protected Node createExecuteNode(int argsLength) {
            return Message.createExecute(argsLength).createNode();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INTEROP_EXECUTE_CACHE;
        }

    }

    @CoreMethod(names = "invoke", isModuleFunction = true, required = 2, rest = true)
    public abstract static class InvokeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = "args.length == cachedArgsLength",
                limit = "getCacheLimit()"
        )
        public Object invokeCached(TruffleObject receiver, Object identifier, Object[] args,
                @Cached("args.length") int cachedArgsLength,
                @Cached("create()") ToJavaStringNode toJavaStringNode,
                @Cached("create()") RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @Cached("createInvokeNode(cachedArgsLength)") Node invokeNode,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode,
                @Cached("create()") BranchProfile unknownIdentifierProfile,
                @Cached("create()") BranchProfile exceptionProfile) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object[] arguments = rubyToForeignArgumentsNode.executeConvert(args);

            final Object foreign;
            try {
                foreign = ForeignAccess.sendInvoke(
                        invokeNode,
                        receiver,
                        name,
                        arguments);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(coreExceptions().noMethodErrorUnknownIdentifier(receiver, name, args, e, this));
            } catch (UnsupportedTypeException
                    | ArityException
                    | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @Specialization(replaces = "invokeCached")
        public Object invokeUncached(TruffleObject receiver, DynamicObject identifier, Object[] args,
                @Cached("create()") ToJavaStringNode toJavaStringNode,
                @Cached("create()") RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode,
                @Cached("create()") BranchProfile unknownIdentifierProfile,
                @Cached("create()") BranchProfile exceptionProfile) {
            Log.notOptimizedOnce("megamorphic interop INVOKE message send");

            final Node invokeNode = createInvokeNode(args.length);
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object[] arguments = rubyToForeignArgumentsNode.executeConvert(args);

            final Object foreign;
            try {
                foreign = ForeignAccess.sendInvoke(
                        invokeNode,
                        receiver,
                        name,
                        arguments);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(coreExceptions().noMethodErrorUnknownIdentifier(receiver, name, args, e, this));
            } catch (UnsupportedTypeException
                    | ArityException
                    | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @TruffleBoundary
        protected Node createInvokeNode(int argsLength) {
            return Message.createInvoke(argsLength).createNode();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INTEROP_INVOKE_CACHE;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "instantiable?", isModuleFunction = true, required = 1)
    public abstract static class InstantiableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isInstantiable(
                TruffleObject receiver,
                @Cached("IS_INSTANTIABLE.createNode()") Node isInstantiableNode) {
            return ForeignAccess.sendIsInstantiable(isInstantiableNode, receiver);
        }

        @Fallback
        public boolean isInstantiable(Object receiver) {
            return false;
        }

    }

    @CoreMethod(names = "new", isModuleFunction = true, required = 1, rest = true)
    public abstract static class NewNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = "args.length == cachedArgsLength",
                limit = "getCacheLimit()"
        )
        public Object newCached(TruffleObject receiver, Object[] args,
                @Cached("args.length") int cachedArgsLength,
                @Cached("create()") RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @Cached("createNewNode(cachedArgsLength)") Node newNode,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            final Object foreign;

            try {
                foreign = ForeignAccess.sendNew(
                        newNode,
                        receiver,
                        rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException
                    | ArityException
                    | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @Specialization(replaces = "newCached")
        public Object newUncached(TruffleObject receiver, Object[] args,
                @Cached("create()") RubyToForeignArgumentsNode rubyToForeignArgumentsNode,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            Log.notOptimizedOnce("megamorphic interop NEW message send");

            final Node invokeNode = createNewNode(args.length);

            final Object foreign;

            try {
                foreign = ForeignAccess.sendNew(
                        invokeNode,
                        receiver,
                        rubyToForeignArgumentsNode.executeConvert(args));
            } catch (UnsupportedTypeException
                    | ArityException
                    | UnsupportedMessageException e) {
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @TruffleBoundary
        protected Node createNewNode(int argsLength) {
            return Message.createNew(argsLength).createNode();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INTEROP_NEW_CACHE;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "size?", isModuleFunction = true, required = 1)
    public abstract static class HasSizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean hasSize(
                TruffleObject receiver,
                @Cached("HAS_SIZE.createNode()") Node hasSizeNode) {
            return ForeignAccess.sendHasSize(hasSizeNode, receiver);
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "size", isModuleFunction = true, required = 1)
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object size(String receiver) {
            return receiver.length();
        }

        @Specialization
        public Object size(
                TruffleObject receiver,
                @Cached("GET_SIZE.createNode()") Node getSizeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendGetSize(getSizeNode, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "boxed?", isModuleFunction = true, required = 1)
    public abstract static class BoxedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isBoxed(
                TruffleObject receiver,
                @Cached("IS_BOXED.createNode()") Node isBoxedNode) {
            return ForeignAccess.sendIsBoxed(isBoxedNode, receiver);
        }

        @Specialization(guards = "!isTruffleObject(receiver)")
        public boolean isBoxed(Object receiver) {
            return false;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "unbox", isModuleFunction = true, required = 1)
    public abstract static class UnboxNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object unbox(TruffleObject receiver,
                @Cached("UNBOX.createNode()") Node unboxNode,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            final Object foreign;

            try {
                foreign = ForeignAccess.sendUnbox(unboxNode, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this, e));
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        @Specialization
        public DynamicObject unbox(String receiver,
                                   @Cached("create()") FromJavaStringNode fromJavaStringNode) {
            return fromJavaStringNode.executeFromJavaString(receiver);
        }

        @Specialization(guards = {
                "!isTruffleObject(receiver)",
                "!isString(receiver)"
        })
        public Object unbox(Object receiver) {
            return receiver;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "unbox_without_conversion", isModuleFunction = true, required = 1)
    public abstract static class UnboxWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object unbox(
                TruffleObject receiver,
                @Cached("UNBOX.createNode()") Node unboxNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendUnbox(unboxNode, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this, e));
            }
        }

        @Specialization(guards = "!isTruffleObject(receiver)")
        public Object unbox(Object receiver) {
            return receiver;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "null?", isModuleFunction = true, required = 1)
    public abstract static class NullNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isNull(TruffleObject receiver,
                @Cached("IS_NULL.createNode()") Node isNullNode) {
            return ForeignAccess.sendIsNull(isNullNode, receiver);
        }

        @Fallback
        public boolean isNull(Object receiver) {
            return false;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "pointer?", isModuleFunction = true, required = 1)
    public abstract static class PointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isPointer(
                TruffleObject receiver,
                @Cached("IS_POINTER.createNode()") Node isPointerNode) {
            return ForeignAccess.sendIsPointer(isPointerNode, receiver);
        }

        @Fallback
        public boolean isPointer(Object receiver) {
            return false;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "as_pointer", isModuleFunction = true, required = 1)
    public abstract static class AsPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object asPointer(
                TruffleObject receiver,
                @Cached("AS_POINTER.createNode()") Node asPointerNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendAsPointer(asPointerNode, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this, e));
            }
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "to_native", isModuleFunction = true, required = 1)
    public abstract static class ToNativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object toNative(
                TruffleObject receiver,
                @Cached("TO_NATIVE.createNode()") Node toNativeNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendToNative(toNativeNode, receiver);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this, e));
            }
        }

    }

    @CoreMethod(names = "read", isModuleFunction = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class, Message.class })
    public abstract static class ReadNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object read(TruffleObject receiver, Object identifier,
                @Cached("READ.createNode()") Node readNode,
                @Cached("create()") BranchProfile unknownIdentifierProfile,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("create()") RubyToForeignNode rubyToForeignNode,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            final Object name = rubyToForeignNode.executeConvert(identifier);
            final Object foreign;
            try {
                foreign = ForeignAccess.sendRead(
                        readNode,
                        receiver,
                        name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }

    @CoreMethod(names = "read_without_conversion", isModuleFunction = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class, Message.class })
    public abstract static class ReadWithoutConversionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object read(TruffleObject receiver, Object identifier,
                           @Cached("READ.createNode()") Node readNode,
                           @Cached("create()") BranchProfile unknownIdentifierProfile,
                           @Cached("create()") BranchProfile exceptionProfile,
                           @Cached("create()") RubyToForeignNode rubyToForeignNode) {
            final Object name = rubyToForeignNode.executeConvert(identifier);
            try {
                return ForeignAccess.sendRead(
                        readNode,
                        receiver,
                        name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "write", isModuleFunction = true, required = 3)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class, Message.class })
    public abstract static class WriteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object write(TruffleObject receiver, Object identifier, Object value,
                @Cached("create()") RubyToForeignNode identifierToForeignNode,
                @Cached("create()") RubyToForeignNode valueToForeignNode,
                @Cached("WRITE.createNode()") Node writeNode,
                @Cached("create()") BranchProfile unknownIdentifierProfile,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("create()") ForeignToRubyNode foreignToRubyNode) {
            final Object name = identifierToForeignNode.executeConvert(identifier);
            final Object foreign;
            try {
                foreign = ForeignAccess.sendWrite(
                        writeNode,
                        receiver,
                        name,
                        valueToForeignNode.executeConvert(value));
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedTypeException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

    }

    @CoreMethod(names = "remove", isModuleFunction = true, required = 2)
    @ImportStatic(Message.class)
    public abstract static class RemoveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean remove(TruffleObject receiver, Object identifier,
                @Cached("create()") RubyToForeignNode identifierToForeignNode,
                @Cached("REMOVE.createNode()") Node removeNode,
                @Cached("create()") BranchProfile unknownIdentifierProfile,
                @Cached("create()") BranchProfile exceptionProfile) {
            final Object name = identifierToForeignNode.executeConvert(identifier);
            final boolean foreign;
            try {
                foreign = ForeignAccess.sendRemove(removeNode, receiver, name);
            } catch (UnknownIdentifierException e) {
                unknownIdentifierProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorUnknownIdentifier(receiver, name, e, this));
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }

            return foreign;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "keys?", isModuleFunction = true, required = 1)
    public abstract static class InteropHasKeysNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean hasKeys(
                TruffleObject receiver,
                @Cached("HAS_KEYS.createNode()") Node hasKeysNode) {
            return ForeignAccess.sendHasKeys(hasKeysNode, receiver);
        }

        @Specialization(guards = "!isTruffleObject(receiver)")
        public Object hasKeys(VirtualFrame frame, Object receiver) {
            return true;
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "keys_without_conversion", isModuleFunction = true, required = 1, optional = 1)
    public abstract static class KeysNode extends PrimitiveArrayArgumentsNode {

        protected abstract Object executeKeys(TruffleObject receiver, boolean internal);

        @Specialization
        public Object keys(TruffleObject receiver, NotProvided internal) {
            return executeKeys(receiver, false);
        }

        @Specialization
        public Object keys(TruffleObject receiver, boolean internal,
                @Cached("KEYS.createNode()") Node keysNode,
                @Cached("create()") BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendKeys(keysNode, receiver, internal);
            } catch (UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @ImportStatic(Message.class)
    @CoreMethod(names = "key_info_bits", isModuleFunction = true, required = 2)
    public abstract static class KeyInfoBitsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int keyInfo(VirtualFrame frame, TruffleObject receiver, Object name,
                              @Cached("create()") RubyToForeignNode rubyToForeignNode,
                              @Cached("KEY_INFO.createNode()") Node keyInfoNode) {
            return ForeignAccess.sendKeyInfo(keyInfoNode, receiver, rubyToForeignNode.executeConvert(name));
        }

    }

    @CoreMethod(names = "export", isModuleFunction = true, required = 2)
    public abstract static class ExportNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(name) || isRubySymbol(name)")
        public Object export(DynamicObject name, TruffleObject object) {
            getContext().getInteropManager().exportObject(name.toString(), object);
            return object;
        }

    }

    @CoreMethod(names = "import", isModuleFunction = true, required = 1)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class ImportNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coercetNameToString(RubyNode newName) {
            return ToJavaStringNodeGen.create(newName);
        }

        @Specialization
        public Object importObject(String name,
                @Cached("create()") BranchProfile errorProfile) {
            final Object value = doImport(name);
            if (value != null) {
                return value;
            } else {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().nameErrorImportNotFound(name, this));
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
            return getContext().getEnv().isMimeTypeSupported(mimeType.toString());
        }

    }

    @CoreMethod(names = "eval", isModuleFunction = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
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

        @Specialization(guards = {"isRubyString(mimeType)", "isRubyString(source)"}, replaces = "evalCached")
        public Object evalUncached(DynamicObject mimeType, DynamicObject source,
                @Cached("create()") IndirectCallNode callNode) {
            return callNode.call(parse(mimeType, source), RubyNode.EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(DynamicObject mimeType, DynamicObject source) {
            final String mimeTypeString = mimeType.toString();
            final Source sourceObject = Source.newBuilder(source.toString()).name("(eval)").mimeType(mimeTypeString).build();
            return getContext().getEnv().parse(sourceObject);
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

    @CoreMethod(names = "to_java_string", isModuleFunction = true, required = 1)
    public abstract static class InteropToJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object toJavaString(Object value,
                @Cached("create()") ToJavaStringNode toJavaStringNode) {
            return toJavaStringNode.executeToJavaString(value);
        }

    }

    @CoreMethod(names = "from_java_string", isModuleFunction = true, required = 1)
    public abstract static class InteropFromJavaStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object fromJavaString(Object value,
                                     @Cached("createForeignToRubyNode()") ForeignToRubyNode foreignToRubyNode) {
            return foreignToRubyNode.executeConvert(value);
        }

        protected ForeignToRubyNode createForeignToRubyNode() {
            return ForeignToRubyNodeGen.create(null);
        }

    }

    @Primitive(name = "to_java_array")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaArrayNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyArray(array)", "strategy.matches(array)" }, limit = "STORAGE_STRATEGIES")
        public Object toJavaArray(DynamicObject interopModule, DynamicObject array,
                                  @Cached("of(array)") ArrayStrategy strategy) {
            return getContext().getEnv().asGuestValue(strategy.newMirror(array).copyArrayAndMirror().getArray());
        }

        @Specialization(guards = "!isRubyArray(object)")
        public Object coerce(DynamicObject interopModule, DynamicObject object) {
            return null;
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

    @CoreMethod(names = "existing_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasExistingBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean readableBit(int bits) {
            return KeyInfo.isExisting(bits);
        }

    }

    @CoreMethod(names = "readable_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasReadableBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean readableBit(int bits) {
            return KeyInfo.isReadable(bits);
        }

    }

    @CoreMethod(names = "writable_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasWritableBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean writableBit(int bits) {
            return KeyInfo.isWritable(bits);
        }

    }

    @CoreMethod(names = "invocable_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasInvocableBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean invocableBit(int bits) {
            return KeyInfo.isInvocable(bits);
        }

    }

    @CoreMethod(names = "internal_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasInternalBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean internalBit(int bits) {
            return KeyInfo.isInternal(bits);
        }

    }

    @CoreMethod(names = "removable_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasRemovableBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean removableBit(int bits) {
            return KeyInfo.isRemovable(bits);
        }

    }

    @CoreMethod(names = "modifiable_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasModifiableBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean modifiableBit(int bits) {
            return KeyInfo.isModifiable(bits);
        }

    }

    @CoreMethod(names = "insertable_bit?", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class HasInsertableBitNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean insertableBit(int bits) {
            return KeyInfo.isInsertable(bits);
        }

    }

    @CoreMethod(names = "key_info_flags_to_bits", isModuleFunction = true, required = 6)
    public abstract static class KeyInfoFlagsToBitsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int keyInfoFlagsToBitsNode(boolean readable, boolean invocable, boolean internal,
                                          boolean insertable, boolean modifiable, boolean removable) {
            int keyInfo = KeyInfo.NONE;

            if (readable) {
                keyInfo |= KeyInfo.READABLE;
            }

            if (invocable) {
                keyInfo |= KeyInfo.INVOCABLE;
            }

            if (internal) {
                keyInfo |= KeyInfo.INTERNAL;
            }

            if (insertable) {
                keyInfo |= KeyInfo.INSERTABLE;
            }

            if (modifiable) {
                keyInfo |= KeyInfo.MODIFIABLE;
            }

            if (removable) {
                keyInfo |= KeyInfo.REMOVABLE;
            }

            return keyInfo;
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
                throw new RaiseException(getContext().getCoreExceptions().securityError("host access is not allowed", this));
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

    @CoreMethod(names = "single_threaded?", onSingleton = true)
    public abstract static class SingleThreadedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean singleThreaded() {
            return getContext().getOptions().SINGLE_THREADED;
        }

    }

}
