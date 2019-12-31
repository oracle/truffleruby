/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import com.oracle.truffle.api.dsl.ReportPolymorphism;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.SymbolProcNode;
import org.truffleruby.parser.ArgumentDescriptor;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Symbol", isClass = true)
public abstract class SymbolNodes {

    @CoreMethod(names = "all_symbols", onSingleton = true)
    public abstract static class AllSymbolsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject allSymbols() {
            Object[] store = getContext().getSymbolTable().allSymbols().toArray();
            return createArray(store, store.length);
        }

    }

    @CoreMethod(names = { "==", "eql?" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(b)")
        protected boolean equal(DynamicObject a, DynamicObject b) {
            return a == b;
        }

        @Specialization(guards = "!isRubySymbol(b)")
        protected boolean equal(VirtualFrame frame, DynamicObject a, Object b) {
            return false;
        }

    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        // Cannot cache a Symbol's hash while pre-initializing, as it will change in SymbolTable#rehash()
        @Specialization(guards = { "symbol == cachedSymbol", "!preInitializing" }, limit = "getIdentityCacheLimit()")
        protected long hashCached(DynamicObject symbol,
                @Cached("isPreInitializing()") boolean preInitializing,
                @Cached("symbol") DynamicObject cachedSymbol,
                @Cached("hash(cachedSymbol)") long cachedHash) {
            return cachedHash;
        }

        @Specialization
        protected long hash(DynamicObject symbol) {
            return Layouts.SYMBOL.getHashCode(symbol);
        }

        protected boolean isPreInitializing() {
            return getContext().isPreInitializing();
        }

    }

    @ReportPolymorphism
    @CoreMethod(names = "to_proc")
    public abstract static class ToProcNode extends CoreMethodArrayArgumentsNode {

        public static final Arity ARITY = new Arity(0, 0, true);

        @Child private ReadCallerFrameNode readCallerFrame = ReadCallerFrameNode.create();

        @Specialization(
                guards = { "cachedSymbol == symbol", "getDeclarationContext(frame) == cachedDeclarationContext" },
                limit = "getIdentityCacheLimit()")
        protected DynamicObject toProcCached(VirtualFrame frame, DynamicObject symbol,
                @Cached("symbol") DynamicObject cachedSymbol,
                @Cached("getDeclarationContext(frame)") DeclarationContext cachedDeclarationContext,
                @Cached("createProc(cachedDeclarationContext, getMethod(frame), symbol)") DynamicObject cachedProc) {
            return cachedProc;
        }

        @Specialization
        protected DynamicObject toProcUncached(VirtualFrame frame, DynamicObject symbol) {
            final InternalMethod method = getMethod(frame);
            DeclarationContext declarationContext = getDeclarationContext(frame);
            return createProc(declarationContext, method, symbol);
        }

        @TruffleBoundary
        protected DynamicObject createProc(DeclarationContext declarationContext, InternalMethod method,
                DynamicObject symbol) {
            final SourceSection sourceSection = CoreLibrary.UNAVAILABLE_SOURCE_SECTION;
            final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                    sourceSection,
                    method.getLexicalScope(),
                    ARITY,
                    null,
                    Layouts.SYMBOL.getString(symbol),
                    0,
                    "proc",
                    ArgumentDescriptor.ANON_REST,
                    false);
            final Object[] args = RubyArguments
                    .pack(null, null, method, declarationContext, null, nil(), null, EMPTY_ARGUMENTS);
            // MRI raises an error on Proc#binding if you attempt to access the binding of a procedure generated
            // by Symbol#to_proc. We generate a declaration frame here so that all procedures will have a
            // binding as this simplifies the logic elsewhere in the runtime.
            final MaterializedFrame declarationFrame = Truffle
                    .getRuntime()
                    .createMaterializedFrame(args, coreLibrary().emptyDescriptor);
            final RubyRootNode rootNode = new RubyRootNode(
                    getContext(),
                    sourceSection,
                    new FrameDescriptor(nil()),
                    sharedMethodInfo,
                    new SymbolProcNode(Layouts.SYMBOL.getString(symbol)),
                    true);

            final RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

            return ProcOperations.createRubyProc(
                    coreLibrary().procFactory,
                    ProcType.PROC,
                    sharedMethodInfo,
                    callTarget,
                    callTarget,
                    declarationFrame,
                    method,
                    null,
                    null,
                    declarationContext == null ? DeclarationContext.NONE : declarationContext);
        }

        protected InternalMethod getMethod(VirtualFrame frame) {
            return RubyArguments.getMethod(frame);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().SYMBOL_TO_PROC_CACHE;
        }

        protected DeclarationContext getDeclarationContext(VirtualFrame frame) {
            return RubyArguments.tryGetDeclarationContext(readCallerFrame.execute(frame));
        }

    }

    @CoreMethod(names = "to_s")
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject toS(DynamicObject symbol,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            return makeStringNode.fromRope(Layouts.SYMBOL.getRope(symbol));
        }

    }

    @CoreMethod(names = {"__allocate__", "__dynamic_object_factory__"}, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

}
