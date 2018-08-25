/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import com.oracle.truffle.api.CallTarget;
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
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
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
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ArgumentDescriptorUtils;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.CallBoundMethodNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.parser.ArgumentDescriptor;

@CoreClass("Method")
public abstract class MethodNodes {

    @CoreMethod(names = { "==", "eql?" }, required = 1)
    public abstract static class EqualNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyMethod(b)")
        public boolean equal(VirtualFrame frame, DynamicObject a, DynamicObject b,
                @Cached("create()") ReferenceEqualNode referenceEqualNode) {
            return referenceEqualNode.executeReferenceEqual(Layouts.METHOD.getReceiver(a), Layouts.METHOD.getReceiver(b)) &&
                    Layouts.METHOD.getMethod(a).getDeclaringModule() == Layouts.METHOD.getMethod(b).getDeclaringModule() &&
                    originalMethod(a) == originalMethod(b);
        }

        @Specialization(guards = "!isRubyMethod(b)")
        public boolean equal(DynamicObject a, Object b) {
            return false;
        }

        private SharedMethodInfo originalMethod(DynamicObject a) {
            return Layouts.METHOD.getMethod(a).getSharedMethodInfo();
        }

    }

    @CoreMethod(names = "arity")
    public abstract static class ArityNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int arity(DynamicObject method) {
            return Layouts.METHOD.getMethod(method).getSharedMethodInfo().getArity().getArityNumber();
        }

    }

    @CoreMethod(names = { "call", "[]", "===" }, needsBlock = true, rest = true)
    public abstract static class CallNode extends CoreMethodArrayArgumentsNode {

        @Child private CallBoundMethodNode callBoundMethodNode = CallBoundMethodNode.create();

        @Specialization
        protected Object call(VirtualFrame frame, DynamicObject method, Object[] arguments, Object maybeBlock) {
            return callBoundMethodNode.executeCallBoundMethod(method, arguments, maybeBlock);
        }

    }

    @CoreMethod(names = "name")
    public abstract static class NameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject name(DynamicObject method) {
            return getSymbol(Layouts.METHOD.getMethod(method).getName());
        }

    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long hash(DynamicObject rubyMethod) {
            final InternalMethod method = Layouts.METHOD.getMethod(rubyMethod);
            long h = getContext().getHashing(this).start(method.getDeclaringModule().hashCode());
            h = Hashing.update(h, Layouts.METHOD.getReceiver(rubyMethod).hashCode());
            h = Hashing.update(h, method.getSharedMethodInfo().hashCode());
            return Hashing.end(h);
        }

    }

    @CoreMethod(names = "owner")
    public abstract static class OwnerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject owner(DynamicObject method) {
            return Layouts.METHOD.getMethod(method).getDeclaringModule();
        }

    }

    @CoreMethod(names = "parameters")
    public abstract static class ParametersNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject parameters(DynamicObject method) {
            final ArgumentDescriptor[] argsDesc = Layouts.METHOD.getMethod(method).getSharedMethodInfo().getArgumentDescriptors();

            return ArgumentDescriptorUtils.argumentDescriptorsToParameters(getContext(), argsDesc, true);
        }

    }

    @CoreMethod(names = "receiver")
    public abstract static class ReceiverNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object receiver(DynamicObject method) {
            return Layouts.METHOD.getReceiver(method);
        }

    }

    @CoreMethod(names = "source_location")
    public abstract static class SourceLocationNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public Object sourceLocation(DynamicObject method) {
            SourceSection sourceSection = Layouts.METHOD.getMethod(method).getSharedMethodInfo().getSourceSection();

            if (sourceSection.getSource() == null) {
                return nil();
            } else {
                DynamicObject file = makeStringNode.executeMake(getContext().getPath(sourceSection.getSource()), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                Object[] objects = new Object[] { file, sourceSection.getStartLine() };
                return createArray(objects, objects.length);
            }
        }

    }

    @CoreMethod(names = "super_method")
    public abstract static class SuperMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @Specialization
        public DynamicObject superMethod(DynamicObject method) {
            Object receiver = Layouts.METHOD.getReceiver(method);
            InternalMethod internalMethod = Layouts.METHOD.getMethod(method);
            DynamicObject selfMetaClass = metaClassNode.executeMetaClass(receiver);
            MethodLookupResult superMethod = ModuleOperations.lookupSuperMethod(internalMethod, selfMetaClass);
            if (!superMethod.isDefined()) {
                return nil();
            } else {
                return Layouts.METHOD.createMethod(coreLibrary().getMethodFactory(), receiver, superMethod.getMethod());
            }
        }

    }

    @CoreMethod(names = "unbind")
    public abstract static class UnbindNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        public DynamicObject unbind(VirtualFrame frame, DynamicObject method) {
            final DynamicObject receiverClass = classNode.executeLogicalClass(Layouts.METHOD.getReceiver(method));
            return Layouts.UNBOUND_METHOD.createUnboundMethod(coreLibrary().getUnboundMethodFactory(), receiverClass, Layouts.METHOD.getMethod(method));
        }

    }

    @CoreMethod(names = "to_proc")
    public abstract static class ToProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "methodObject == cachedMethodObject", limit = "getCacheLimit()")
        public DynamicObject toProcCached(DynamicObject methodObject,
                @Cached("methodObject") DynamicObject cachedMethodObject,
                @Cached("toProcUncached(cachedMethodObject)") DynamicObject proc) {
            return proc;
        }

        @Specialization(guards = "cachedMethod == internalMethod(methodObject)", limit = "getCacheLimit()", replaces = "toProcCached")
        public DynamicObject toProcCachedTarget(DynamicObject methodObject,
                @Cached("internalMethod(methodObject)") InternalMethod cachedMethod,
                @Cached("methodCallTarget(cachedMethod)") CallTarget callTarget) {
            return createProc(callTarget, cachedMethod, Layouts.METHOD.getReceiver(methodObject));
        }

        @Specialization
        public DynamicObject toProcUncached(DynamicObject methodObject) {
            final InternalMethod method = internalMethod(methodObject);
            final CallTarget callTarget = methodCallTarget(method);
            final Object receiver = Layouts.METHOD.getReceiver(methodObject);
            return createProc(callTarget, method, receiver);
        }

        private DynamicObject createProc(final CallTarget callTarget, final InternalMethod method, final Object receiver) {
            final Object[] packedArgs = RubyArguments.pack(null, null, method, null, receiver, null, EMPTY_ARGUMENTS);
            final MaterializedFrame declarationFrame = Truffle.getRuntime().createMaterializedFrame(packedArgs, coreLibrary().getEmptyDescriptor());
            return ProcOperations.createRubyProc(
                    coreLibrary().getProcFactory(),
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
        protected CallTarget methodCallTarget(InternalMethod method) {
            // translate to something like:
            // lambda { |same args list| method.call(args) }
            // We need to preserve the method receiver and we want to have the same argument list

            final SourceSection sourceSection = method.getSharedMethodInfo().getSourceSection();
            final RootNode oldRootNode = ((RootCallTarget) method.getCallTarget()).getRootNode();

            final SetReceiverNode setReceiverNode = new SetReceiverNode(method.getCallTarget());
            final RootNode newRootNode = new RubyRootNode(getContext(), sourceSection, oldRootNode.getFrameDescriptor(), method.getSharedMethodInfo(), setReceiverNode);
            return Truffle.getRuntime().createCallTarget(newRootNode);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().METHOD_TO_PROC_CACHE;
        }

        protected InternalMethod internalMethod(DynamicObject a) {
            return Layouts.METHOD.getMethod(a);
        }
    }

    @Primitive(name = "method_unimplement")
    public abstract static class MethodUnimplementNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject methodUnimplement(DynamicObject rubyMethod) {
            final InternalMethod method = Layouts.METHOD.getMethod(rubyMethod);
            Layouts.MODULE.getFields(method.getDeclaringModule()).addMethod(
                    getContext(), this, method.unimplemented());
            return nil();
        }

    }

    @Primitive(name = "method_unimplemented?")
    public abstract static class MethodUnimplementedQueryNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean isMethodUnimplemented(DynamicObject rubyMethod) {
            return Layouts.METHOD.getMethod(rubyMethod).isUnimplemented();
        }

    }

    private static class SetReceiverNode extends RubyNode {

        @Child private DirectCallNode methodCallNode;

        public SetReceiverNode(CallTarget methodCallTarget) {
            this.methodCallNode = DirectCallNode.create(methodCallTarget);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object receiver = RubyArguments.getSelf(RubyArguments.getDeclarationFrame(frame));
            RubyArguments.setSelf(frame, receiver);
            return methodCallNode.call(frame.getArguments());
        }

    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

}
