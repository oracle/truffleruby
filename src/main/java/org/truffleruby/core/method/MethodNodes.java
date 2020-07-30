/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ArgumentDescriptorUtils;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.CallBoundMethodNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.AllocateHelperNode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.parser.ArgumentDescriptor;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
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

    @CoreMethod(names = { "==", "eql?" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean equal(VirtualFrame frame, RubyMethod a, RubyMethod b,
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
            return method.method.getSharedMethodInfo().getArity().getArityNumber();
        }

    }

    @CoreMethod(names = { "call", "[]", "===" }, needsBlock = true, rest = true)
    public abstract static class CallNode extends CoreMethodArrayArgumentsNode {

        @Child private CallBoundMethodNode callBoundMethodNode = CallBoundMethodNode.create();

        @Specialization
        protected Object call(VirtualFrame frame, RubyMethod method, Object[] arguments, Object maybeBlock) {
            return callBoundMethodNode.executeCallBoundMethod(method, arguments, maybeBlock);
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
        protected DynamicObject owner(RubyMethod method) {
            return method.method.getDeclaringModule();
        }

    }

    @CoreMethod(names = "parameters")
    public abstract static class ParametersNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject parameters(RubyMethod method) {
            final ArgumentDescriptor[] argsDesc = method.method
                    .getSharedMethodInfo()
                    .getArgumentDescriptors();

            return ArgumentDescriptorUtils.argumentDescriptorsToParameters(getContext(), argsDesc, true);
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
                DynamicObject file = makeStringNode.executeMake(
                        RubyContext.getPath(sourceSection.getSource()),
                        UTF8Encoding.INSTANCE,
                        CodeRange.CR_UNKNOWN);
                return createArray(new Object[]{ file, sourceSection.getStartLine() });
            }
        }

    }

    @CoreMethod(names = "super_method")
    public abstract static class SuperMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();
        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @Specialization
        protected Object superMethod(RubyMethod method) {
            Object receiver = method.receiver;
            InternalMethod internalMethod = method.method;
            DynamicObject selfMetaClass = metaClassNode.executeMetaClass(receiver);
            MethodLookupResult superMethod = ModuleOperations.lookupSuperMethod(internalMethod, selfMetaClass);
            if (!superMethod.isDefined()) {
                return nil;
            } else {
                final RubyMethod instance = new RubyMethod(
                        coreLibrary().methodShape,
                        receiver,
                        superMethod.getMethod());
                allocateNode.trace(instance, this);
                return instance;
            }
        }

    }

    @CoreMethod(names = "unbind")
    public abstract static class UnbindNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        protected DynamicObject unbind(VirtualFrame frame, RubyMethod method) {
            final DynamicObject receiverClass = classNode.executeLogicalClass(method.receiver);
            return Layouts.UNBOUND_METHOD.createUnboundMethod(
                    coreLibrary().unboundMethodFactory,
                    receiverClass,
                    method.method);
        }

    }

    @CoreMethod(names = "to_proc")
    public abstract static class ToProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "methodObject == cachedMethodObject", limit = "getCacheLimit()")
        protected DynamicObject toProcCached(RubyMethod methodObject,
                @Cached("methodObject") RubyMethod cachedMethodObject,
                @Cached("toProcUncached(cachedMethodObject)") DynamicObject proc) {
            return proc;
        }

        @Specialization(
                guards = "cachedMethod == methodObject.method",
                limit = "getCacheLimit()",
                replaces = "toProcCached")
        protected DynamicObject toProcCachedTarget(RubyMethod methodObject,
                @Cached("methodObject.method") InternalMethod cachedMethod,
                @Cached("methodCallTarget(cachedMethod)") RootCallTarget callTarget) {
            return createProc(callTarget, cachedMethod, methodObject.receiver);
        }

        @Specialization
        protected DynamicObject toProcUncached(RubyMethod methodObject) {
            final InternalMethod method = methodObject.method;
            final RootCallTarget callTarget = methodCallTarget(method);
            final Object receiver = methodObject.receiver;
            return createProc(callTarget, method, receiver);
        }

        private DynamicObject createProc(RootCallTarget callTarget, InternalMethod method, Object receiver) {
            final Object[] packedArgs = RubyArguments.pack(null, null, method, null, receiver, null, EMPTY_ARGUMENTS);
            final MaterializedFrame declarationFrame = Truffle
                    .getRuntime()
                    .createMaterializedFrame(packedArgs, coreLibrary().emptyDescriptor);
            return ProcOperations.createRubyProc(
                    coreLibrary().procFactory,
                    ProcType.LAMBDA,
                    method.getSharedMethodInfo(),
                    callTarget,
                    callTarget,
                    declarationFrame,
                    method,
                    null,
                    null,
                    method.getDeclarationContext());
        }

        @TruffleBoundary
        protected RootCallTarget methodCallTarget(InternalMethod method) {
            // translate to something like:
            // lambda { |same args list| method.call(args) }
            // We need to preserve the method receiver and we want to have the same argument list

            final SourceSection sourceSection = method.getSharedMethodInfo().getSourceSection();
            final RootNode oldRootNode = method.getCallTarget().getRootNode();

            final SetReceiverNode setReceiverNode = new SetReceiverNode(method.getCallTarget());
            final RootNode newRootNode = new RubyRootNode(
                    getContext(),
                    sourceSection,
                    oldRootNode.getFrameDescriptor(),
                    method.getSharedMethodInfo(),
                    setReceiverNode,
                    true);
            return Truffle.getRuntime().createCallTarget(newRootNode);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().METHOD_TO_PROC_CACHE;
        }

    }

    @Primitive(name = "method_unimplement")
    public abstract static class MethodUnimplementNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object methodUnimplement(RubyMethod rubyMethod) {
            final InternalMethod method = rubyMethod.method;
            Layouts.MODULE.getFields(method.getDeclaringModule()).addMethod(
                    getContext(),
                    this,
                    method.unimplemented());
            return nil;
        }

    }

    @Primitive(name = "method_unimplemented?")
    public abstract static class MethodUnimplementedQueryNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isMethodUnimplemented(RubyMethod rubyMethod) {
            return rubyMethod.method.isUnimplemented();
        }

    }

    private static class SetReceiverNode extends RubyContextSourceNode {

        @Child private DirectCallNode methodCallNode;

        public SetReceiverNode(RootCallTarget methodCallTarget) {
            this.methodCallNode = DirectCallNode.create(methodCallTarget);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object receiver = RubyArguments.getSelf(RubyArguments.getDeclarationFrame(frame));
            RubyArguments.setSelf(frame, receiver);
            return methodCallNode.call(frame.getArguments());
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

}
