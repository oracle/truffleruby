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
import java.util.Map;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.NodeLibrary;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.LanguageInfo;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.Source;

@CoreModule("Truffle::Interop")
public abstract class InteropNodes {

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

    public static Object execute(Object receiver, Object[] args, InteropLibrary receivers,
            TranslateInteropExceptionNode translateInteropExceptionNode) {
        try {
            return receivers.execute(receiver, args);
        } catch (InteropException e) {
            throw translateInteropExceptionNode.execute(e);
        }
    }

    @CoreMethod(names = "import_file", onSingleton = true, required = 1)
    public abstract static class ImportFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object importFile(RubyString fileName) {
            try {
                //intern() to improve footprint
                final TruffleFile file = getContext()
                        .getEnv()
                        .getPublicTruffleFile(fileName.getJavaString().intern());
                final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, file).build();
                getContext().getEnv().parsePublic(source).call();
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }

            return nil;
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
            final Object[] convertedArgs = rubyToForeignArgumentsNode.executeConvert(args);
            final Object foreign = InteropNodes.execute(receiver, convertedArgs, receivers, translateInteropException);
            return foreignToRubyNode.executeConvert(foreign);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @CoreMethod(names = "execute_without_conversion", onSingleton = true, required = 1, rest = true)
    public abstract static class ExecuteWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object executeWithoutConversionForeignCached(Object receiver, Object[] args,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            return InteropNodes.execute(receiver, args, receivers, translateInteropException);
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
                @Cached ForeignToRubyNode foreignToRubyNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object[] arguments = rubyToForeignArgumentsNode.executeConvert(args);

            final Object foreign;
            try {
                foreign = receivers.invokeMember(receiver, name, arguments);
            } catch (InteropException e) {
                throw translateInteropException.executeInInvokeMember(e, receiver, args);
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
        protected boolean isInstantiable(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isInstantiable(receiver);
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
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object foreign;

            try {
                foreign = receivers.instantiate(receiver, rubyToForeignArgumentsNode.executeConvert(args));
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return foreignToRubyNode.executeConvert(foreign);
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @CoreMethod(names = "has_array_elements?", onSingleton = true, required = 1)
    public abstract static class HasArrayElementsNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean hasArrayElements(Object receiver,
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
        protected RubyString asString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached FromJavaStringNode fromJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String string;
            try {
                string = receivers.asString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return fromJavaStringNode.executeFromJavaString(string);
        }
    }

    @CoreMethod(names = "as_string_without_conversion", onSingleton = true, required = 1)
    public abstract static class AsStringWithoutConversionNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected String asString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {

            try {
                return receivers.asString(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "to_display_string", onSingleton = true, required = 1)
    public abstract static class ToDisplayStringNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected RubyString toDisplayString(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @CachedLibrary(limit = "1") InteropLibrary asStrings,
                @Cached FromJavaStringNode fromJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final Object displayString = receivers.toDisplayString(receiver, true);
            final String string;
            try {
                string = asStrings.asString(displayString);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
            return fromJavaStringNode.executeFromJavaString(string);
        }
    }

    @CoreMethod(names = "language", onSingleton = true, required = 1)
    public abstract static class GetLanguageNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object getLanguage(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached FromJavaStringNode fromJavaStringNode) {
            if (!receivers.hasLanguage(receiver)) {
                return nil;
            }

            final Class<? extends TruffleLanguage<?>> language;
            try {
                language = receivers.getLanguage(receiver);
            } catch (UnsupportedMessageException e) {
                return nil;
            }

            final String name = languageClassToLanguageName(language);
            return fromJavaStringNode.executeFromJavaString(name);
        }

        @TruffleBoundary
        private String languageClassToLanguageName(Class<? extends TruffleLanguage<?>> language) {
            String name = language.getSimpleName();
            if (name.endsWith("Language")) {
                name = name.substring(0, name.length() - "Language".length());
            }
            if (name.equals("Host")) {
                name = "Java";
            }
            return name;
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
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asInt(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_long", onSingleton = true, required = 1)
    public abstract static class AsLongNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected long asLong(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asLong(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }
    }

    @CoreMethod(names = "as_double", onSingleton = true, required = 1)
    public abstract static class AsDoubleNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected double asDouble(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.asDouble(receiver);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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

        @Specialization(guards = "isRubySymbolOrString(identifier)", limit = "getCacheLimit()")
        protected Object readMember(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached ForeignToRubyNode foreignToRubyNode) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            final Object foreign;
            try {
                foreign = receivers.readMember(receiver, name);
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

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "read_array_element_without_conversion", onSingleton = true, required = 2)
    public abstract static class ReadArrayElementWithoutConversionNode extends RubySourceNode {

        public static ReadArrayElementNode create() {
            return InteropNodesFactory.ReadArrayElementNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier);

        @Specialization(limit = "getCacheLimit()")
        protected Object readArrayElement(Object receiver, long identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.readArrayElement(receiver, identifier);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
        }
    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "arguments", type = RubyNode[].class)
    @CoreMethod(names = "read_member_without_conversion", onSingleton = true, required = 2)
    public abstract static class ReadMemberWithoutConversionNode extends RubySourceNode {

        public static ReadMemberNode create() {
            return InteropNodesFactory.ReadMemberNodeFactory.create(null);
        }

        abstract Object execute(Object receiver, Object identifier);

        @Specialization(guards = "isRubySymbolOrString(identifier)", limit = "getCacheLimit()")
        protected Object readMember(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException,
                @Cached ToJavaStringNode toJavaStringNode) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                return receivers.readMember(receiver, name);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

        protected static int getCacheLimit() {
            return RubyLanguage.getCurrentContext().getOptions().METHOD_LOOKUP_CACHE;
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
                guards = "isRubySymbolOrString(identifier)",
                limit = "getCacheLimit()")
        protected Object write(Object receiver, Object identifier, Object value,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached RubyToForeignNode valueToForeignNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.writeMember(receiver, name, valueToForeignNode.executeConvert(value));
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
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

    @CoreMethod(names = "remove_member", onSingleton = true, required = 2)
    public abstract static class RemoveMemberNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbolOrString(identifier)", limit = "getCacheLimit()")
        protected Nil remove(Object receiver, Object identifier,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            final String name = toJavaStringNode.executeToJavaString(identifier);
            try {
                receivers.removeMember(receiver, name);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }

            return Nil.INSTANCE;
        }
    }

    @CoreMethod(names = "has_members?", onSingleton = true, required = 1)
    public abstract static class HasMembersNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected boolean hasMembers(Object receiver,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMembers(receiver);
        }
    }

    @CoreMethod(names = "members_without_conversion", onSingleton = true, required = 1, optional = 1)
    public abstract static class GetMembersNode extends InteropPrimitiveArrayArgumentsNode {

        protected abstract Object executeMembers(Object receiver, boolean internal);

        @Specialization
        protected Object members(Object receiver, NotProvided internal) {
            return executeMembers(receiver, false);
        }

        @Specialization(limit = "getCacheLimit()")
        protected Object members(Object receiver, boolean internal,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return receivers.getMembers(receiver, internal);
            } catch (InteropException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "is_member_readable?", onSingleton = true, required = 2)
    public abstract static class IsMemberReadableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberReadable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberReadable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_modifiable?", onSingleton = true, required = 2)
    public abstract static class IsMemberModifiableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberModifiable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberModifiable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_insertable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInsertableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberInsertable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInsertable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_removable?", onSingleton = true, required = 2)
    public abstract static class IsMemberRemovableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberRemovable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberRemovable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_invocable?", onSingleton = true, required = 2)
    public abstract static class IsMemberInvocableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberInvocable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInvocable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_internal?", onSingleton = true, required = 2)
    public abstract static class IsMemberInternalNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberInternal(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberInternal(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_writable?", onSingleton = true, required = 2)
    public abstract static class IsMemberWritableNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberWritable(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberWritable(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "is_member_existing?", onSingleton = true, required = 2)
    public abstract static class IsMemberExistingNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean isMemberExisting(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.isMemberExisting(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_read_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberReadSideEffectsNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean hasMemberReadSideEffects(Object receiver, Object name,
                @Cached ToJavaStringNode toJavaStringNode,
                @CachedLibrary("receiver") InteropLibrary receivers) {
            return receivers.hasMemberReadSideEffects(receiver, toJavaStringNode.executeToJavaString(name));
        }
    }

    @CoreMethod(names = "has_member_write_side_effects?", onSingleton = true, required = 2)
    public abstract static class HasMemberWriteSideEffectsNode extends InteropCoreMethodArrayArgumentsNode {
        @Specialization(limit = "getCacheLimit()")
        protected boolean hasMemberWriteSideEffects(Object receiver, Object name,
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
            return ToJavaStringNode.create(newName);
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
            return ToJavaStringNode.create(newName);
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
        @Specialization
        protected boolean isMimeTypeSupported(RubyString mimeType) {
            return getContext().getEnv().isMimeTypeSupported(mimeType.getJavaString());
        }

    }

    @CoreMethod(names = "languages", onSingleton = true, required = 0)
    public abstract static class LanguagesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray languages() {
            final Map<String, LanguageInfo> languages = getContext().getEnv().getPublicLanguages();
            final String[] languagesArray = languages.keySet().toArray(StringUtils.EMPTY_STRING_ARRAY);
            final Object[] rubyStringArray = new Object[languagesArray.length];
            for (int i = 0; i < languagesArray.length; i++) {
                rubyStringArray[i] = StringOperations.createString(
                        getContext(),
                        StringOperations.encodeRope(languagesArray[i], UTF8Encoding.INSTANCE));
            }
            return createArray(rubyStringArray);
        }

    }

    @CoreMethod(names = "other_languages?", onSingleton = true, required = 0)
    public abstract static class HasOtherLanguagesNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean hasOtherlanguages() {
            return getContext().hasOtherPublicLanguages();
        }

    }

    @CoreMethod(names = "eval", onSingleton = true, required = 2)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class EvalNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = {
                        "mimeTypeEqualNode.execute(mimeType.rope, cachedMimeType)",
                        "sourceEqualNode.execute(source.rope, cachedSource)" },
                limit = "getCacheLimit()")
        protected Object evalCached(RubyString mimeType, RubyString source,
                @Cached("privatizeRope(mimeType)") Rope cachedMimeType,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("create(parse(mimeType, source))") DirectCallNode callNode,
                @Cached RopeNodes.EqualNode mimeTypeEqualNode,
                @Cached RopeNodes.EqualNode sourceEqualNode) {
            return callNode.call(EMPTY_ARGUMENTS);
        }

        @Specialization(replaces = "evalCached")
        protected Object evalUncached(RubyString mimeType, RubyString source,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(mimeType, source), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(RubyString mimeType, RubyString code) {
            final String mimeTypeString = mimeType.getJavaString();
            final String codeString = code.getJavaString();
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

        @Specialization
        protected Object evalNFI(RubyString code,
                @Cached IndirectCallNode callNode) {
            return callNode.call(parse(code), EMPTY_ARGUMENTS);
        }

        @TruffleBoundary
        protected CallTarget parse(RubyString code) {
            final String codeString = code.getJavaString();
            final Source source = Source.newBuilder("nfi", codeString, "(eval)").build();

            try {
                return getContext().getEnv().parseInternal(source);
            } catch (IllegalStateException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

    }

    @Primitive(name = "dispatch_missing")
    public abstract static class DispatchMissingNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object dispatchMissing() {
            return DispatchNode.MISSING;
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
        protected boolean javaInstanceOfJava(Object object, Object boxedJavaClass) {
            final Object hostInstance = getContext().getEnv().asHostObject(object);
            if (hostInstance == null) {
                return false;
            } else {
                final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
                return javaClass.isAssignableFrom(hostInstance.getClass());
            }
        }

        @Specialization(guards = { "!isJavaObject(object)", "isJavaClassOrInterface(boxedJavaClass)" })
        protected boolean javaInstanceOfNotJava(Object object, Object boxedJavaClass) {
            final Class<?> javaClass = (Class<?>) getContext().getEnv().asHostObject(boxedJavaClass);
            return javaClass.isInstance(object);
        }

        protected boolean isJavaObject(Object object) {
            return getContext().getEnv().isHostObject(object);
        }

        protected boolean isJavaClassOrInterface(Object object) {
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

        @Specialization(guards = "stores.accepts(array.store)")
        protected Object toJavaArray(RubyArray array,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return getContext().getEnv().asGuestValue(stores.toJavaArrayCopy(
                    array.store,
                    array.size));
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected Object coerce(Object array) {
            return FAILURE;
        }

    }

    @Primitive(name = "interop_to_java_list")
    @ImportStatic(ArrayGuards.class)
    public abstract static class InteropToJavaListNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "stores.accepts(array.store)")
        protected Object toJavaList(RubyArray array,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            int size = array.size;
            Object[] copy = stores.boxedCopyOfRange(array.store, 0, size);
            return getContext().getEnv().asGuestValue(ArrayUtils.asList(copy));
        }

        @Specialization(guards = "!isRubyArray(array)")
        protected Object coerce(Object array) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "deproxy", onSingleton = true, required = 1)
    public abstract static class DeproxyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isJavaObject(object)")
        protected Object deproxyJavaObject(Object object) {
            return getContext().getEnv().asHostObject(object);
        }

        @Specialization(guards = "!isJavaObject(object)")
        protected Object deproxyNotJavaObject(Object object) {
            return object;
        }

        protected boolean isJavaObject(Object object) {
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

    @CoreMethod(names = "is_java_map?", onSingleton = true, visibility = Visibility.PRIVATE, required = 1)
    public abstract static class InteropIsJavaMapNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isJavaMap(Object value) {
            return getContext().getEnv().isHostObject(value) &&
                    getContext().getEnv().asHostObject(value) instanceof Map;
        }
    }

    @CoreMethod(names = "meta_object", onSingleton = true, required = 1)
    public abstract static class InteropMetaObjectNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object metaObject(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached BranchProfile errorProfile) {
            if (interop.hasMetaObject(value)) {
                try {
                    return interop.getMetaObject(value);
                } catch (UnsupportedMessageException e) {
                    errorProfile.enter();
                    return coreLibrary().getLogicalClass(value);
                }
            } else {
                return coreLibrary().getLogicalClass(value);
            }
        }

    }

    @CoreMethod(names = "meta_qualified_name", onSingleton = true, required = 1)
    public abstract static class GetMetaQualifiedNameNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object metaObject(Object metaObject,
                @CachedLibrary("metaObject") InteropLibrary interop,
                @CachedLibrary(limit = "1") InteropLibrary asStrings,
                @Cached FromJavaStringNode fromJavaStringNode,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                final Object qualifiedName = interop.getMetaQualifiedName(metaObject);
                final String string = asStrings.asString(qualifiedName);
                return fromJavaStringNode.executeFromJavaString(string);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @CoreMethod(names = "java_type", onSingleton = true, required = 1)
    public abstract static class JavaTypeNode extends CoreMethodArrayArgumentsNode {

        // TODO CS 17-Mar-18 we should cache this in the future

        @TruffleBoundary
        @Specialization
        protected Object javaTypeSymbol(RubySymbol name) {
            return javaType(name.getString());
        }

        @TruffleBoundary
        @Specialization
        protected Object javaTypeString(RubyString name) {
            return javaType(name.getJavaString());
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

    @CoreMethod(names = "proxy_foreign_object", onSingleton = true, required = 1, optional = 1)
    public abstract static class ProxyForeignObjectNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object proxyForeignObject(Object delegate, NotProvided logger) {
            return new ProxyForeignObject(delegate);
        }

        @TruffleBoundary
        @Specialization(guards = "wasProvided(logger)")
        protected Object proxyForeignObject(Object delegate, Object logger) {
            return new ProxyForeignObject(delegate, logger);
        }

    }

    @CoreMethod(names = "to_string", onSingleton = true, required = 1)
    public abstract static class ToStringNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString toString(Object value) {
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

    @Primitive(name = "current_scope")
    public abstract static class GetCurrentScopeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getScope(VirtualFrame frame,
                @CachedLibrary(limit = "1") NodeLibrary nodeLibrary,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            try {
                return nodeLibrary.getScope(this, frame, true);
            } catch (UnsupportedMessageException e) {
                throw translateInteropException.execute(e);
            }
        }

    }

    @Primitive(name = "top_scope")
    public abstract static class GetTopScopeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getTopScope(
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return context.getTopScopeObject();
        }

    }

    @CoreMethod(names = "scope_parent", onSingleton = true, required = 1)
    public abstract static class GetScopeParentNode extends InteropCoreMethodArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object getScope(Object scope,
                @CachedLibrary("scope") InteropLibrary interopLibrary,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            if (interopLibrary.hasScopeParent(scope)) {
                try {
                    return interopLibrary.getScopeParent(scope);
                } catch (UnsupportedMessageException e) {
                    throw translateInteropException.execute(e);
                }
            } else {
                return nil;
            }
        }
    }

}
