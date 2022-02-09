/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.proc.ProcCallTargets;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyLambdaRootNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ArgumentDescriptorUtils;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.CallInternalMethodNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.parser.ArgumentDescriptor;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Method", isClass = true)
public abstract class MethodNodes {

    public static boolean areInternalMethodEqual(InternalMethod a, InternalMethod b) {
        if (a == b || a.getSharedMethodInfo() == b.getSharedMethodInfo()) {
            return true;
        }

        // For builtin aliases to be == such as String#size and String#length, even though they have
        // different CallTarget, InternalMethod and SharedMethodInfo.
        return a.getSharedMethodInfo().getArity() == b.getSharedMethodInfo().getArity();
    }

    public static int hashInternalMethod(InternalMethod internalMethod) {
        // Hash the Arity object to guarantee same hash values for areInternalMethodEqual() methods.
        return internalMethod.getSharedMethodInfo().getArity().hashCode();
    }

    @Primitive(name = "same_methods?")
    public abstract static class SameMethodsNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean same(RubyMethod self, RubyMethod other) {
            return MethodNodes.areInternalMethodEqual(self.method, other.method);
        }

        @Specialization
        protected boolean same(RubyMethod self, RubyUnboundMethod other) {
            return MethodNodes.areInternalMethodEqual(self.method, other.method);
        }

        @Specialization
        protected boolean same(RubyUnboundMethod self, RubyMethod other) {
            return MethodNodes.areInternalMethodEqual(self.method, other.method);
        }

        @Specialization
        protected boolean same(RubyUnboundMethod self, RubyUnboundMethod other) {
            return MethodNodes.areInternalMethodEqual(self.method, other.method);
        }
    }

    @CoreMethod(names = { "==", "eql?" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean equal(RubyMethod a, RubyMethod b,
                @Cached ReferenceEqualNode referenceEqualNode) {
            return referenceEqualNode
                    .executeReferenceEqual(a.receiver, b.receiver) &&
                    a.method.getDeclaringModule() == b.method.getDeclaringModule() &&
                    MethodNodes.areInternalMethodEqual(a.method, b.method);
        }

        @Specialization(guards = "!isRubyMethod(b)")
        protected boolean equal(RubyMethod a, Object b) {
            return false;
        }

    }

    @CoreMethod(names = "arity")
    public abstract static class ArityNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int arity(RubyMethod method) {
            return method.method.getArityNumber();
        }

    }

    @GenerateUncached
    @CoreMethod(names = { "call", "[]", "===" }, needsBlock = true, rest = true, alwaysInlined = true)
    public abstract static class CallNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object call(Frame callerFrame, RubyMethod method, Object[] rubyArgs, RootCallTarget target,
                @Cached CallInternalMethodNode callInternalMethodNode) {
            final InternalMethod internalMethod = method.method;
            final Object[] newArgs = RubyArguments.repack(rubyArgs, method.receiver);
            RubyArguments.setMethod(newArgs, internalMethod);
            assert RubyArguments.assertFrameArguments(newArgs);
            return callInternalMethodNode.execute(callerFrame, internalMethod, method.receiver, newArgs);
        }
    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySymbol name(RubyMethod method) {
            return getSymbol(method.method.getName());
        }

    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected long hash(RubyMethod rubyMethod) {
            final InternalMethod method = rubyMethod.method;
            long h = getContext().getHashing(this).start(method.getDeclaringModule().hashCode());
            h = Hashing.update(h, rubyMethod.receiver.hashCode());
            h = Hashing.update(h, hashInternalMethod(method));
            return Hashing.end(h);
        }

    }

    @CoreMethod(names = "owner")
    public abstract static class OwnerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyModule owner(RubyMethod method) {
            return method.method.getDeclaringModule();
        }

    }

    @CoreMethod(names = "parameters")
    public abstract static class ParametersNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray parameters(RubyMethod method) {
            final ArgumentDescriptor[] argsDesc = method.method
                    .getSharedMethodInfo()
                    .getArgumentDescriptors();

            return ArgumentDescriptorUtils.argumentDescriptorsToParameters(getLanguage(), getContext(), argsDesc, true);
        }

    }

    @CoreMethod(names = "receiver")
    public abstract static class ReceiverNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object receiver(RubyMethod method) {
            return method.receiver;
        }

    }

    @CoreMethod(names = "source_location")
    public abstract static class SourceLocationNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object sourceLocation(RubyMethod method) {
            SourceSection sourceSection = method.method.getSharedMethodInfo().getSourceSection();

            if (!sourceSection.isAvailable()) {
                return nil;
            } else {
                RubyString file = makeStringNode.executeMake(
                        getLanguage().getSourcePath(sourceSection.getSource()),
                        Encodings.UTF_8,
                        CodeRange.CR_UNKNOWN);
                return createArray(new Object[]{ file, sourceSection.getStartLine() });
            }
        }

    }

    @CoreMethod(names = "super_method")
    public abstract static class SuperMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @Specialization
        protected Object superMethod(RubyMethod method) {
            Object receiver = method.receiver;
            InternalMethod internalMethod = method.method;
            RubyClass selfMetaClass = metaClassNode.execute(receiver);
            MethodLookupResult superMethod = ModuleOperations.lookupSuperMethod(internalMethod, selfMetaClass);
            if (!superMethod.isDefined()) {
                return nil;
            } else {
                final RubyMethod instance = new RubyMethod(
                        coreLibrary().methodClass,
                        getLanguage().methodShape,
                        receiver,
                        superMethod.getMethod());
                AllocationTracing.trace(instance, this);
                return instance;
            }
        }

    }

    @CoreMethod(names = "unbind")
    public abstract static class UnbindNode extends CoreMethodArrayArgumentsNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @Specialization
        protected RubyUnboundMethod unbind(RubyMethod method) {
            final RubyClass receiverClass = metaClassNode.execute(method.receiver);
            final RubyUnboundMethod instance = new RubyUnboundMethod(
                    coreLibrary().unboundMethodClass,
                    getLanguage().unboundMethodShape,
                    receiverClass,
                    method.method);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "to_proc")
    public abstract static class ToProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "methodObject == cachedMethodObject", limit = "getCacheLimit()")
        protected RubyProc toProcCached(RubyMethod methodObject,
                @Cached("methodObject") RubyMethod cachedMethodObject,
                @Cached("toProcUncached(cachedMethodObject)") RubyProc proc) {
            return proc;
        }

        @Specialization(
                guards = "cachedMethod == methodObject.method",
                limit = "getCacheLimit()",
                replaces = "toProcCached")
        protected RubyProc toProcCachedTarget(RubyMethod methodObject,
                @Cached("methodObject.method") InternalMethod cachedMethod,
                @Cached("methodCallTarget(cachedMethod)") RootCallTarget callTarget) {
            return createProc(callTarget, cachedMethod, methodObject.receiver);
        }

        @Specialization
        protected RubyProc toProcUncached(RubyMethod methodObject) {
            final InternalMethod method = methodObject.method;
            final RootCallTarget callTarget = methodCallTarget(method);
            final Object receiver = methodObject.receiver;
            return createProc(callTarget, method, receiver);
        }

        private RubyProc createProc(RootCallTarget callTarget, InternalMethod method, Object receiver) {
            final Object[] packedArgs = RubyArguments
                    .pack(null, null, method, null, receiver, nil, EMPTY_ARGUMENTS);
            final var variables = new SpecialVariableStorage();
            final MaterializedFrame declarationFrame = getLanguage().createEmptyDeclarationFrame(packedArgs, variables);
            return ProcOperations.createRubyProc(
                    coreLibrary().procClass,
                    getLanguage().procShape,
                    ProcType.LAMBDA,
                    method.getSharedMethodInfo(),
                    new ProcCallTargets(callTarget),
                    declarationFrame,
                    variables,
                    method,
                    nil,
                    null,
                    method.getDeclarationContext());
        }

        @TruffleBoundary
        protected RootCallTarget methodCallTarget(InternalMethod method) {
            // translate to something like:
            // lambda { |same args list| method.call(args) }
            // We need to preserve the method receiver and we want to have the same argument list.
            // We create a new CallTarget for the Proc that calls the method CallTarget and passes the correct receiver.

            final SourceSection sourceSection = method.getSharedMethodInfo().getSourceSection();
            final RubyRootNode methodRootNode = RubyRootNode.of(method.getCallTarget());

            final SetReceiverNode setReceiverNode = new SetReceiverNode(method);
            final RubyLambdaRootNode wrapRootNode = new RubyLambdaRootNode(
                    getLanguage(),
                    sourceSection,
                    methodRootNode.getFrameDescriptor(),
                    method.getSharedMethodInfo(),
                    setReceiverNode,
                    methodRootNode.getSplit(),
                    methodRootNode.returnID,
                    BreakID.INVALID,
                    method.getSharedMethodInfo().getArity());
            return wrapRootNode.getCallTarget();
        }

        protected int getCacheLimit() {
            return getLanguage().options.METHOD_TO_PROC_CACHE;
        }

    }

    private static class SetReceiverNode extends RubyContextSourceNode {
        private final InternalMethod method;
        @Child private CallInternalMethodNode callInternalMethodNode = CallInternalMethodNode.create();

        public SetReceiverNode(InternalMethod method) {
            this.method = method;
        }

        @Override
        public Object execute(VirtualFrame frame) {
            final Object originalBoundMethodReceiver = RubyArguments.getSelf(RubyArguments.getDeclarationFrame(frame));
            Object[] rubyArgs = RubyArguments.pack(null, null, method, null, originalBoundMethodReceiver,
                    RubyArguments.getBlock(frame), RubyArguments.getArguments(frame));
            return callInternalMethodNode.execute(frame, method, originalBoundMethodReceiver, rubyArgs);
        }
    }

    @Primitive(name = "method_unimplement")
    public abstract static class MethodUnimplementNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object bound(RubyMethod rubyMethod) {
            unimplement(rubyMethod.method);
            return nil;
        }

        @Specialization
        protected Object unbound(RubyUnboundMethod rubyMethod) {
            unimplement(rubyMethod.method);
            return nil;
        }

        @TruffleBoundary
        private void unimplement(InternalMethod method) {
            method.getDeclaringModule().fields.addMethod(getContext(), this, method.unimplemented());
        }
    }

    @Primitive(name = "method_unimplemented?")
    public abstract static class MethodIsUnimplementedNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean bound(RubyMethod rubyMethod) {
            return rubyMethod.method.isUnimplemented();
        }

        @Specialization
        protected boolean unbound(RubyUnboundMethod rubyMethod) {
            return rubyMethod.method.isUnimplemented();
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected Object allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

}
