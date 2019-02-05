/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CallerFrameAccess;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory.ObjectIDNodeFactory;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.DurationToMillisecondsNodeGen;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.NameToJavaStringNodeGen;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.cast.ToPathNodeGen;
import org.truffleruby.core.cast.ToStringOrSymbolNodeGen;
import org.truffleruby.core.exception.GetBacktraceException;
import org.truffleruby.core.format.BytesResult;
import org.truffleruby.core.format.FormatExceptionTranslator;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.exceptions.InvalidFormatException;
import org.truffleruby.core.format.printf.PrintfCompiler;
import org.truffleruby.core.kernel.KernelNodesFactory.CopyNodeFactory;
import org.truffleruby.core.kernel.KernelNodesFactory.GetMethodObjectNodeGen;
import org.truffleruby.core.kernel.KernelNodesFactory.SameOrEqualNodeFactory;
import org.truffleruby.core.kernel.KernelNodesFactory.SingletonMethodsNodeFactory;
import org.truffleruby.core.kernel.KernelNodesFactory.ToHexStringNodeFactory;
import org.truffleruby.core.method.MethodFilter;
import org.truffleruby.core.proc.ProcNodes.ProcNewNode;
import org.truffleruby.core.proc.ProcNodesFactory.ProcNewNodeFactory;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.support.TypeNodes.ObjectInstanceVariablesNode;
import org.truffleruby.core.support.TypeNodesFactory.ObjectInstanceVariablesNodeFactory;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.Activation;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.eval.CreateEvalSourceNode;
import org.truffleruby.language.globals.ReadGlobalVariableNodeGen;
import org.truffleruby.language.loader.CodeLoader;
import org.truffleruby.language.loader.RequireNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.FreezeNode;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsTaintedNode;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarGetNodeGen;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;
import org.truffleruby.language.objects.PropagateTaintNode;
import org.truffleruby.language.objects.PropertyFlags;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.SelfNode;
import org.truffleruby.language.objects.ShapeCachingGuards;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.TaintNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.truffleruby.core.string.StringOperations.rope;

@CoreClass("Kernel")
public abstract class KernelNodes {

    /**
     * Check if operands are the same object or call #==.
     * Known as rb_equal() in MRI. The fact Kernel#=== uses this is pure coincidence.
     */
    @Primitive(name = "object_same_or_equal")
    public abstract static class SameOrEqualNode extends PrimitiveArrayArgumentsNode {

        @Child private CallDispatchHeadNode equalNode;
        @Child private BooleanCastNode booleanCastNode;

        private final ConditionProfile sameProfile = ConditionProfile.createBinaryProfile();

        public static SameOrEqualNode create() {
            return SameOrEqualNodeFactory.create(null);
        }

        public abstract boolean executeSameOrEqual(VirtualFrame frame, Object a, Object b);

        @Specialization
        protected boolean sameOrEqual(VirtualFrame frame, Object a, Object b,
                @Cached("create()") ReferenceEqualNode referenceEqualNode) {
            if (sameProfile.profile(referenceEqualNode.executeReferenceEqual(a, b))) {
                return true;
            } else {
                return areEqual(frame, a, b);
            }
        }

        private boolean areEqual(VirtualFrame frame, Object left, Object right) {
            if (equalNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                equalNode = insert(CallDispatchHeadNode.createPrivate());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.executeToBoolean(equalNode.call(left, "==", right));
        }

    }

    @CoreMethod(names = "===", required = 1)
    public abstract static class CaseCompareNode extends CoreMethodArrayArgumentsNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization
        protected boolean caseCmp(VirtualFrame frame, Object a, Object b) {
            return sameOrEqualNode.executeSameOrEqual(frame, a, b);
        }

    }

    /** Check if operands are the same object or call #eql? */
    public abstract static class SameOrEqlNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode eqlNode;
        @Child private BooleanCastNode booleanCastNode;

        private final ConditionProfile sameProfile = ConditionProfile.createBinaryProfile();

        public abstract boolean executeSameOrEql(Object a, Object b);

        @Specialization
        public boolean sameOrEql(Object a, Object b,
                        @Cached("create()") ReferenceEqualNode referenceEqualNode) {
            if (sameProfile.profile(referenceEqualNode.executeReferenceEqual(a, b))) {
                return true;
            } else {
                return areEql(a, b);
            }
        }

        private boolean areEql(Object left, Object right) {
            if (eqlNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                eqlNode = insert(CallDispatchHeadNode.createPrivate());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.executeToBoolean(eqlNode.call(left, "eql?", right));
        }

    }

    @CoreMethod(names = { "<=>" }, required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization
        public Object compare(VirtualFrame frame, Object self, Object other) {
            if (sameOrEqualNode.executeSameOrEqual(frame, self, other)) {
                return 0;
            } else {
                return nil();
            }
        }

    }

    @CoreMethod(names = "binding", isModuleFunction = true)
    public abstract static class BindingNode extends CoreMethodArrayArgumentsNode {

        @Child ReadCallerFrameNode callerFrameNode = new ReadCallerFrameNode(CallerFrameAccess.MATERIALIZE);

        @Specialization
        protected DynamicObject bindingUncached(VirtualFrame frame) {
            final MaterializedFrame callerFrame = callerFrameNode.execute(frame).materialize();
            final SourceSection sourceSection = getCallerSourceSection();

            return BindingNodes.createBinding(getContext(), callerFrame, sourceSection);
        }

        @TruffleBoundary
        protected SourceSection getCallerSourceSection() {
            // TODO: ignore #send
            return getContext().getCallStack().getCallerNode(0, true).getEncapsulatingSourceSection();
        }

    }

    @CoreMethod(names = "block_given?", isModuleFunction = true)
    public abstract static class BlockGivenNode extends CoreMethodArrayArgumentsNode {

        @Child ReadCallerFrameNode callerFrameNode = new ReadCallerFrameNode(CallerFrameAccess.ARGUMENTS);

        @Specialization
        public boolean blockGiven(VirtualFrame frame,
                @Cached("createBinaryProfile()") ConditionProfile blockProfile) {
            Frame callerFrame = callerFrameNode.execute(frame);
            return blockProfile.profile(RubyArguments.getBlock(callerFrame) != null);
        }

    }

    @CoreMethod(names = "__callee__", isModuleFunction = true)
    public abstract static class CalleeNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject calleeName() {
            // the "called name" of a method.
            return getSymbol(getContext().getCallStack().getCallingMethodIgnoringSend().getName());
        }
    }

    @CoreMethod(names = "caller_locations", isModuleFunction = true, optional = 2, lowerFixnum = { 1, 2 })
    public abstract static class CallerLocationsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject callerLocations(NotProvided omit, NotProvided length) {
            return innerCallerLocations(1, GetBacktraceException.UNLIMITED);
        }

        @Specialization
        public DynamicObject callerLocations(int omit, NotProvided length) {
            return innerCallerLocations(omit, GetBacktraceException.UNLIMITED);
        }

        @Specialization(guards = "length >= 0")
        public DynamicObject callerLocations(int omit, int length) {
            return innerCallerLocations(omit, length);
        }

        private DynamicObject innerCallerLocations(int omit, int length) {
            final int omitted = 1 /* always skip #caller_locations */ + omit;
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(this, omitted);
            final int limit = (length == GetBacktraceException.UNLIMITED) ? GetBacktraceException.UNLIMITED : omitted + length;

            int locationsCount = backtrace.getActivations(new GetBacktraceException(this, limit)).length;

            if (length != GetBacktraceException.UNLIMITED && length < locationsCount) {
                locationsCount = length;
            }

            final Object[] locations = new Object[locationsCount];

            for (int n = 0; n < locationsCount; n++) {
                locations[n] = Layouts.THREAD_BACKTRACE_LOCATION.createThreadBacktraceLocation(
                        coreLibrary().getThreadBacktraceLocationFactory(), backtrace, n);
            }

            return createArray(locations, locations.length);
        }
    }

    @CoreMethod(names = "class")
    public abstract static class KernelClassNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        public DynamicObject getClass(VirtualFrame frame, Object self) {
            return classNode.executeLogicalClass(self);
        }

    }

    @ImportStatic(ShapeCachingGuards.class)
    public abstract static class CopyNode extends UnaryCoreMethodNode {

        @Child private CallDispatchHeadNode allocateNode = CallDispatchHeadNode.createPrivate();

        public abstract DynamicObject executeCopy(VirtualFrame frame, DynamicObject self);

        @ExplodeLoop
        @Specialization(guards = "self.getShape() == cachedShape", limit = "getCacheLimit()")
        protected DynamicObject copyCached(VirtualFrame frame, DynamicObject self,
                @Cached("self.getShape()") Shape cachedShape,
                @Cached("getLogicalClass(cachedShape)") DynamicObject logicalClass,
                @Cached(value = "getCopiedProperties(cachedShape)", dimensions = 1) Property[] properties,
                @Cached("createReadFieldNodes(properties)") ReadObjectFieldNode[] readFieldNodes,
                @Cached("createWriteFieldNodes(properties)") WriteObjectFieldNode[] writeFieldNodes) {
            final DynamicObject newObject = (DynamicObject) allocateNode.call(logicalClass, "__allocate__");

            for (int i = 0; i < properties.length; i++) {
                final Object value = readFieldNodes[i].execute(self);
                writeFieldNodes[i].write(newObject, value);
            }

            return newObject;
        }

        @Specialization(guards = "updateShape(self)")
        protected Object updateShapeAndCopy(VirtualFrame frame, DynamicObject self) {
            return executeCopy(frame, self);
        }

        @Specialization(replaces = { "copyCached", "updateShapeAndCopy" })
        protected DynamicObject copyUncached(VirtualFrame frame, DynamicObject self) {
            final DynamicObject rubyClass = Layouts.BASIC_OBJECT.getLogicalClass(self);
            final DynamicObject newObject = (DynamicObject) allocateNode.call(rubyClass, "__allocate__");
            copyInstanceVariables(self, newObject);
            return newObject;
        }

        protected DynamicObject getLogicalClass(Shape shape) {
            return Layouts.BASIC_OBJECT.getLogicalClass(shape.getObjectType());
        }

        protected Property[] getCopiedProperties(Shape shape) {
            final List<Property> copiedProperties = new ArrayList<>();

            for (Property property : shape.getProperties()) {
                if (property.getKey() instanceof String) {
                    copiedProperties.add(property);
                }
            }

            final Property associatedProperty = shape.getProperty(Layouts.ASSOCIATED_IDENTIFIER);

            if (associatedProperty != null) {
                copiedProperties.add(associatedProperty);
            }

            return copiedProperties.toArray(new Property[copiedProperties.size()]);
        }

        protected ReadObjectFieldNode[] createReadFieldNodes(Property[] properties) {
            final ReadObjectFieldNode[] nodes = new ReadObjectFieldNode[properties.length];
            for (int i = 0; i < properties.length; i++) {
                nodes[i] = ReadObjectFieldNodeGen.create(properties[i].getKey(), nil());
            }
            return nodes;
        }

        protected WriteObjectFieldNode[] createWriteFieldNodes(Property[] properties) {
            final WriteObjectFieldNode[] nodes = new WriteObjectFieldNode[properties.length];
            for (int i = 0; i < properties.length; i++) {
                nodes[i] = WriteObjectFieldNodeGen.create(properties[i].getKey());
            }
            return nodes;
        }

        @TruffleBoundary
        private void copyInstanceVariables(DynamicObject from, DynamicObject to) {
            // Concurrency: OK if callers create the object and publish it after copy
            // Only copy user-level instance variables, hidden ones are initialized later with #initialize_copy.
            for (Property property : getCopiedProperties(from.getShape())) {
                to.define(property.getKey(), property.get(from, from.getShape()), property.getFlags());
            }
        }

        protected int getCacheLimit() {
            return getContext().getOptions().INSTANCE_VARIABLE_CACHE;
        }

    }

    @CoreMethod(names = "clone", keywordAsOptional = "freeze")
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "self"),
            @NodeChild(type = RubyNode.class, value = "freeze")
    })
    public abstract static class CloneNode extends CoreMethodNode {

        @Child private CopyNode copyNode = CopyNodeFactory.create(null);
        @Child private CallDispatchHeadNode initializeCloneNode = CallDispatchHeadNode.createPrivate();
        @Child private IsFrozenNode isFrozenNode = IsFrozenNode.create();
        @Child private FreezeNode freezeNode;
        @Child private PropagateTaintNode propagateTaintNode = PropagateTaintNode.create();
        @Child private SingletonClassNode singletonClassNode;

        @CreateCast("freeze")
        public RubyNode coerceToBoolean(RubyNode freeze) {
            return BooleanCastWithDefaultNodeGen.create(true, freeze);
        }

        @Specialization
        public DynamicObject clone(VirtualFrame frame, DynamicObject self, boolean freeze,
                @Cached("createBinaryProfile()") ConditionProfile isSingletonProfile,
                @Cached("createBinaryProfile()") ConditionProfile freezeProfile,
                @Cached("createBinaryProfile()") ConditionProfile isFrozenProfile,
                @Cached("createBinaryProfile()") ConditionProfile isRubyClass) {
            final DynamicObject newObject = copyNode.executeCopy(frame, self);

            // Copy the singleton class if any.
            final DynamicObject selfMetaClass = Layouts.BASIC_OBJECT.getMetaClass(self);
            if (isSingletonProfile.profile(Layouts.CLASS.getIsSingleton(selfMetaClass))) {
                final DynamicObject newObjectMetaClass = executeSingletonClass(newObject);
                Layouts.MODULE.getFields(newObjectMetaClass).initCopy(selfMetaClass);
            }

            initializeCloneNode.call(newObject, "initialize_clone", self);

            propagateTaintNode.propagate(self, newObject);

            if (freezeProfile.profile(freeze) && isFrozenProfile.profile(isFrozenNode.executeIsFrozen(self))) {
                if (freezeNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    freezeNode = insert(FreezeNode.create());
                }

                freezeNode.executeFreeze(newObject);
            }

            if (isRubyClass.profile(RubyGuards.isRubyClass(self))) {
                Layouts.CLASS.setSuperclass(newObject, Layouts.CLASS.getSuperclass(self));
            }

            return newObject;
        }

        private DynamicObject executeSingletonClass(DynamicObject newObject) {
            if (singletonClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singletonClassNode = insert(SingletonClassNode.create());
            }

            return singletonClassNode.executeSingletonClass(newObject);
        }

    }

    @CoreMethod(names = "dup", taintFrom = 0)
    public abstract static class DupNode extends CoreMethodArrayArgumentsNode {

        @Child private CopyNode copyNode = CopyNodeFactory.create(null);
        @Child private CallDispatchHeadNode initializeDupNode = CallDispatchHeadNode.createPrivate();

        @Specialization(guards = "!isSpecialDup(self)")
        public DynamicObject dup(VirtualFrame frame, DynamicObject self) {
            final DynamicObject newObject = copyNode.executeCopy(frame, self);

            initializeDupNode.call(newObject, "initialize_dup", self);

            return newObject;
        }

        @Specialization(guards = "isSpecialDup(self)")
        public DynamicObject dupSpecial(DynamicObject self) {
            return self;
        }

        @Specialization
        public Object dup(boolean self) {
            return self;
        }

        @Specialization
        public Object dup(int self) {
            return self;
        }

        @Specialization
        public Object dup(long self) {
            return self;
        }

        @Specialization
        public Object dup(double self) {
            return self;
        }

        protected boolean isSpecialDup(DynamicObject object) {
            return isNil(object) || RubyGuards.isRubyInteger(object) || RubyGuards.isRubySymbol(object);
        }

    }

    @Primitive(name = "kernel_eval", needsSelf = false, lowerFixnum = 5)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class EvalNode extends PrimitiveArrayArgumentsNode {

        @Child private CreateEvalSourceNode createEvalSourceNode = new CreateEvalSourceNode();
        @Child private BindingNodes.CallerBindingNode bindingNode;

        protected static class RootNodeWrapper {
            private final RubyRootNode rootNode;

            public RootNodeWrapper(RubyRootNode rootNode) {
                this.rootNode = rootNode;
            }

            public RubyRootNode getRootNode() {
                return rootNode;
            }
        }

        public abstract Object execute(VirtualFrame frame, Object self, DynamicObject str, DynamicObject binding, DynamicObject file, int line);

        @Specialization(guards = {
                "equalNode.execute(rope(source), cachedSource)",
                "equalNode.execute(rope(file), cachedFile)",
                "line == cachedLine",
                "assignsNoNewVariables(cachedRootNode)",
                "bindingDescriptor == getBindingDescriptor(binding)"
        }, limit = "getCacheLimit()")
        public Object evalBindingNoAddsVarsCached(
                Object self,
                DynamicObject source,
                DynamicObject binding,
                DynamicObject file,
                int line,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("privatizeRope(file)") Rope cachedFile,
                @Cached("line") int cachedLine,
                @Cached("getBindingDescriptor(binding)") FrameDescriptor bindingDescriptor,
                @Cached("compileSource(cachedSource, getBindingFrame(binding), cachedFile, cachedLine)") RootNodeWrapper cachedRootNode,
                @Cached("createCallTarget(cachedRootNode)") RootCallTarget cachedCallTarget,
                @Cached("create(cachedCallTarget)") DirectCallNode callNode,
                @Cached("create()") RopeNodes.EqualNode equalNode) {
            final MaterializedFrame parentFrame = BindingNodes.getFrame(binding);
            return eval(self, cachedRootNode, cachedCallTarget, callNode, parentFrame);
        }

        @Specialization(guards = {
                "equalNode.execute(rope(source), cachedSource)",
                "equalNode.execute(rope(file), cachedFile)",
                "line == cachedLine",
                "!assignsNoNewVariables(cachedRootNode)",
                "assignsNoNewVariables(rootNodeToEval)",
                "bindingDescriptor == getBindingDescriptor(binding)"
        }, limit = "getCacheLimit()")
        public Object evalBindingAddsVarsCached(
                Object self,
                DynamicObject source,
                DynamicObject binding,
                DynamicObject file,
                int line,
                @Cached("privatizeRope(source)") Rope cachedSource,
                @Cached("privatizeRope(file)") Rope cachedFile,
                @Cached("line") int cachedLine,
                @Cached("getBindingDescriptor(binding)") FrameDescriptor bindingDescriptor,
                @Cached("compileSource(cachedSource, getBindingFrame(binding), cachedFile, cachedLine)") RootNodeWrapper cachedRootNode,
                @Cached("newBindingDescriptor(getContext(), cachedRootNode)") FrameDescriptor newBindingDescriptor,
                @Cached("compileSource(cachedSource, getBindingFrame(binding), newBindingDescriptor, cachedFile, cachedLine)") RootNodeWrapper rootNodeToEval,
                @Cached("createCallTarget(rootNodeToEval)") RootCallTarget cachedCallTarget,
                @Cached("create(cachedCallTarget)") DirectCallNode callNode,
                @Cached("create()") RopeNodes.EqualNode equalNode) {
            final MaterializedFrame parentFrame = BindingNodes.newFrame(binding,
                    newBindingDescriptor);
            return eval(self, rootNodeToEval, cachedCallTarget, callNode, parentFrame);
        }

        @Specialization
        public Object evalBindingUncached(Object self, DynamicObject source, DynamicObject binding, DynamicObject file, int line,
                @Cached("create()") IndirectCallNode callNode) {
            final CodeLoader.DeferredCall deferredCall = doEvalX(self, rope(source), binding, rope(file), line, false);
            return deferredCall.call(callNode);
        }

        private Object eval(Object self, RootNodeWrapper rootNode, RootCallTarget callTarget, DirectCallNode callNode, MaterializedFrame parentFrame) {
            final InternalMethod method = new InternalMethod(
                    getContext(),
                    rootNode.getRootNode().getSharedMethodInfo(),
                    RubyArguments.getMethod(parentFrame).getLexicalScope(),
                    RubyArguments.getDeclarationContext(parentFrame),
                    rootNode.getRootNode().getSharedMethodInfo().getName(),
                    RubyArguments.getMethod(parentFrame).getDeclaringModule(),
                    Visibility.PUBLIC,
                    callTarget);

            return callNode.call(RubyArguments.pack(parentFrame, null, method, null, self, null, RubyNode.EMPTY_ARGUMENTS));
        }

        @TruffleBoundary
        private CodeLoader.DeferredCall doEvalX(Object self, Rope source,
                DynamicObject binding,
                Rope file,
                int line,
                boolean ownScopeForAssignments) {
            final MaterializedFrame frame = BindingNodes.newFrame(getContext(), BindingNodes.getFrame(binding));
            final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
            final FrameDescriptor descriptor = frame.getFrameDescriptor();
            RubyRootNode rootNode = buildRootNode(source, frame, file, line, false);
            if (!frameHasOnlySelf(descriptor)) {
                Layouts.BINDING.setFrame(binding, frame);
            }
            return getContext().getCodeLoader().prepareExecute(
                    ParserContext.EVAL, declarationContext, rootNode, frame, self);
        }

        protected RubyRootNode buildRootNode(Rope sourceText, MaterializedFrame parentFrame, Rope file, int line, boolean ownScopeForAssignments) {
            final String sourceFile = RopeOperations.decodeRope(file);
            final RubySource source = createEvalSourceNode.createEvalSource(sourceText, "eval", sourceFile, line);
            return getContext().getCodeLoader().parse(source, ParserContext.EVAL, parentFrame, ownScopeForAssignments, this);
        }

        protected RootNodeWrapper compileSource(Rope sourceText, MaterializedFrame parentFrame, Rope file, int line) {
            return new RootNodeWrapper(buildRootNode(sourceText, parentFrame, file, line, true));
        }

        protected RootNodeWrapper compileSource(Rope sourceText, MaterializedFrame parentFrame, FrameDescriptor additionalVariables, Rope file, int line) {
            return compileSource(sourceText, BindingNodes.newFrame(parentFrame, additionalVariables), file, line);
        }

        protected RootCallTarget createCallTarget(RootNodeWrapper rootNode) {
            return Truffle.getRuntime().createCallTarget(rootNode.rootNode);
        }

        protected FrameDescriptor getBindingDescriptor(DynamicObject binding) {
            return BindingNodes.getFrameDescriptor(binding);
        }

        protected FrameDescriptor newBindingDescriptor(RubyContext context, RootNodeWrapper rootNode) {
            FrameDescriptor descriptor = rootNode.getRootNode().getFrameDescriptor();
            FrameDescriptor newDescriptor = new FrameDescriptor(context.getCoreLibrary().getNil());
            for (FrameSlot frameSlot : descriptor.getSlots()) {
                newDescriptor.findOrAddFrameSlot(frameSlot.getIdentifier());
            }
            return newDescriptor;
        }

        protected MaterializedFrame getBindingFrame(DynamicObject binding) {
            return BindingNodes.getFrame(binding);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().EVAL_CACHE;
        }

        protected boolean assignsNoNewVariables(RootNodeWrapper rootNode) {
            FrameDescriptor descriptor = rootNode.getRootNode().getFrameDescriptor();
            return frameHasOnlySelf(descriptor);
        }

        private boolean frameHasOnlySelf(FrameDescriptor descriptor) {
            return descriptor.getSize() == 1 && SelfNode.SELF_IDENTIFIER.equals(descriptor.getSlots().get(0).getIdentifier());
        }

    }

    @CoreMethod(names = "freeze")
    public abstract static class KernelFreezeNode extends CoreMethodArrayArgumentsNode {

        @Child private FreezeNode freezeNode = FreezeNode.create();

        @Specialization
        public Object freeze(Object self) {
            return freezeNode.executeFreeze(self);
        }

    }

    @CoreMethod(names = "frozen?")
    public abstract static class KernelFrozenNode extends CoreMethodArrayArgumentsNode {

        @Child private IsFrozenNode isFrozenNode;

        @Specialization
        public boolean isFrozen(Object self) {
            if (isFrozenNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isFrozenNode = insert(IsFrozenNode.create());
            }

            return isFrozenNode.executeIsFrozen(self);
        }

    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        private static final int CLASS_SALT = 55927484; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        @Specialization
        public long hash(int value) {
            return getContext().getHashing(this).hash(CLASS_SALT, value);
        }

        @Specialization
        public long hash(long value) {
            return getContext().getHashing(this).hash(CLASS_SALT, value);
        }

        @Specialization
        public long hash(double value) {
            return getContext().getHashing(this).hash(CLASS_SALT, Double.doubleToRawLongBits(value));
        }

        @Specialization
        public long hash(boolean value) {
            return getContext().getHashing(this).hash(CLASS_SALT, Boolean.valueOf(value).hashCode());
        }

        @Specialization(guards = "isRubyBignum(value)")
        public long hashBignum(DynamicObject value) {
            return getContext().getHashing(this).hash(CLASS_SALT, Layouts.BIGNUM.getValue(value).hashCode());
        }

        @TruffleBoundary
        @Specialization(guards = "!isRubyBignum(self)")
        public int hash(DynamicObject self) {
            // TODO(CS 8 Jan 15) we shouldn't use the Java class hierarchy like this - every class should define it's
            // own @CoreMethod hash
            return System.identityHashCode(self);
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        private final BranchProfile errorProfile = BranchProfile.create();

        @Specialization
        public Object initializeCopy(DynamicObject self, DynamicObject from) {
            if (Layouts.BASIC_OBJECT.getLogicalClass(self) != Layouts.BASIC_OBJECT.getLogicalClass(from)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeError("initialize_copy should take same class object", this));
            }

            return self;
        }

    }

    @CoreMethod(names = { "initialize_dup", "initialize_clone" }, required = 1)
    public abstract static class InitializeDupCloneNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode initializeCopyNode = CallDispatchHeadNode.createPrivate();

        @Specialization
        public Object initializeDup(VirtualFrame frame, DynamicObject self, DynamicObject from) {
            return initializeCopyNode.call(self, "initialize_copy", from);
        }

    }

    @CoreMethod(names = "instance_of?", required = 1)
    public abstract static class InstanceOfNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization(guards = "isRubyModule(rubyClass)")
        public boolean instanceOf(Object self, DynamicObject rubyClass) {
            return classNode.executeLogicalClass(self) == rubyClass;
        }

    }

    @CoreMethod(names = "instance_variable_defined?", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class InstanceVariableDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public boolean isInstanceVariableDefinedBoolean(boolean object, String name) {
            return false;
        }

        @Specialization
        public boolean isInstanceVariableDefinedInt(int object, String name) {
            return false;
        }

        @Specialization
        public boolean isInstanceVariableDefinedLong(long object, String name) {
            return false;
        }

        @Specialization
        public boolean isInstanceVariableDefinedDouble(double object, String name) {
            return false;
        }

        @Specialization(guards = "isRubySymbol(object) || isNil(object)")
        public boolean isInstanceVariableDefinedSymbolOrNil(DynamicObject object, String name) {
            return false;
        }

        @TruffleBoundary
        @Specialization(guards = {"!isRubySymbol(object)", "!isNil(object)"})
        public boolean isInstanceVariableDefined(DynamicObject object, String name) {
            final String ivar = SymbolTable.checkInstanceVariableName(getContext(), name, object, this);
            final Property property = object.getShape().getProperty(ivar);
            return PropertyFlags.isDefined(property);
        }

    }

    @CoreMethod(names = "instance_variable_get", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class InstanceVariableGetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceName(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public Object instanceVariableGetSymbol(DynamicObject object, String name,
                @Cached("createObjectIVarGetNode()") ObjectIVarGetNode iVarGetNode) {
            return iVarGetNode.executeIVarGet(object, name);
        }

        protected ObjectIVarGetNode createObjectIVarGetNode() {
            return ObjectIVarGetNodeGen.create(true);
        }

    }

    @CoreMethod(names = "instance_variable_set", raiseIfFrozenSelf = true, required = 2)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "value")
    })
    public abstract static class InstanceVariableSetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceName(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public Object instanceVariableSet(DynamicObject object, String name, Object value,
                @Cached("createObjectIVarSetNode()") ObjectIVarSetNode iVarSetNode) {
            return iVarSetNode.executeIVarSet(object, name, value);
        }

        protected ObjectIVarSetNode createObjectIVarSetNode() {
            return ObjectIVarSetNodeGen.create(true);
        }

    }

    @CoreMethod(names = "remove_instance_variable", raiseIfFrozenSelf = true, required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class RemoveInstanceVariableNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @TruffleBoundary
        @Specialization
        public Object removeInstanceVariable(DynamicObject object, String name) {
            final String ivar = SymbolTable.checkInstanceVariableName(getContext(), name, object, this);
            final Object value = ReadObjectFieldNode.read(object, ivar, nil());

            if (SharedObjects.isShared(getContext(), object)) {
                synchronized (object) {
                    removeField(object, name);
                }
            } else {
                if (!object.delete(name)) {
                    throw new RaiseException(getContext(), coreExceptions().nameErrorInstanceVariableNotDefined(name, object, this));
                }
            }
            return value;
        }

        private void removeField(DynamicObject object, String name) {
            Shape shape = object.getShape();
            Property property = shape.getProperty(name);
            if (!PropertyFlags.isDefined(property)) {
                throw new RaiseException(getContext(), coreExceptions().nameErrorInstanceVariableNotDefined(name, object, this));
            }

            Shape newShape = shape.replaceProperty(property, PropertyFlags.asRemoved(property));
            object.setShapeAndGrow(shape, newShape);
        }
    }

    @CoreMethod(names = "instance_variables")
    public abstract static class InstanceVariablesNode extends CoreMethodArrayArgumentsNode {

        @Child private ObjectInstanceVariablesNode instanceVariablesNode = ObjectInstanceVariablesNodeFactory.create(null);

        @Specialization
        public DynamicObject instanceVariables(Object self) {
            return instanceVariablesNode.executeGetIVars(self);
        }

    }

    @CoreMethod(names = { "is_a?", "kind_of?" }, required = 1)
    public abstract static class KernelIsANode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isA(Object self, DynamicObject module,
                @Cached("create()") IsANode isANode) {
            return isANode.executeIsA(self, module);
        }

        @Specialization(guards = "!isRubyModule(module)")
        public boolean isATypeError(Object self, Object module) {
            throw new RaiseException(getContext(), coreExceptions().typeError("class or module required", this));
        }

    }

    @CoreMethod(names = "lambda", isModuleFunction = true, needsBlock = true)
    public abstract static class LambdaNode extends CoreMethodArrayArgumentsNode {

        @Child private WarnNode warnNode;

        @TruffleBoundary
        @Specialization
        public DynamicObject lambda(NotProvided block) {
            final Frame parentFrame = getContext().getCallStack().getCallerFrameIgnoringSend(0).getFrame(FrameAccess.READ_ONLY);
            final DynamicObject parentBlock = RubyArguments.getBlock(parentFrame);

            if (parentBlock == null) {
                throw new RaiseException(getContext(), coreExceptions().argumentError("tried to create Proc object without a block", this));
            } else {
                warnProcWithoutBlock();
            }

            Node callNode = getContext().getCallStack().getCallerFrameIgnoringSend(1).getCallNode();
            if (isLiteralBlock(callNode)) {
                return lambdaFromBlock(parentBlock);
            } else {
                return parentBlock;
            }
        }

        @Specialization(guards = "isLiteralBlock(block)")
        public DynamicObject lambdaFromBlock(DynamicObject block) {
            return ProcOperations.createLambdaFromBlock(getContext(), block);
        }

        @Specialization(guards = "!isLiteralBlock(block)")
        public DynamicObject lambdaFromExistingProc(DynamicObject block) {
            return block;
        }

        @TruffleBoundary
        protected boolean isLiteralBlock(DynamicObject block) {
            Node callNode = getContext().getCallStack().getCallerFrameIgnoringSend().getCallNode();
            return isLiteralBlock(callNode);
        }

        private boolean isLiteralBlock(Node callNode) {
            if (callNode.getParent() instanceof DispatchNode) {
                RubyCallNode rubyCallNode = ((DispatchNode) callNode.getParent()).findRubyCallNode();
                if (rubyCallNode != null) {
                    return rubyCallNode.hasLiteralBlock();
                }
            }
            return false;
        }

        private void warnProcWithoutBlock() {
            if (warnNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                warnNode = insert(new WarnNode());
            }
            final SourceSection sourceSection = getContext().getCallStack().getTopMostUserSourceSection();
            warnNode.warningMessage(sourceSection, "tried to create Proc object without a block");
        }

    }

    @CoreMethod(names = "__method__", isModuleFunction = true)
    public abstract static class MethodNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject methodName() {
            // the "original/definition name" of the method.
            return getSymbol(getContext().getCallStack().getCallingMethodIgnoringSend().getSharedMethodInfo().getName());
        }

    }

    @CoreMethod(names = "method", required = 1)
    @NodeChild(type = RubyNode.class, value = "receiver")
    @NodeChild(type = RubyNode.class, value = "name")
    public abstract static class MethodNode extends CoreMethodNode {

        @Child private GetMethodObjectNode getMethodObjectNode = GetMethodObjectNode.create(true);

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return ToStringOrSymbolNodeGen.create(name);
        }

        @Specialization
        protected DynamicObject method(VirtualFrame frame, Object self, DynamicObject name) {
            return getMethodObjectNode.executeGetMethodObject(frame, self, name);
        }

    }

    public abstract static class GetMethodObjectNode extends RubyBaseNode {

        public static GetMethodObjectNode create(boolean ignoreVisibility) {
            return GetMethodObjectNodeGen.create(ignoreVisibility);
        }

        private final boolean ignoreVisibility;

        @Child private NameToJavaStringNode nameToJavaStringNode = NameToJavaStringNode.create();
        @Child private LookupMethodNode lookupMethodNode;
        @Child private CallDispatchHeadNode respondToMissingNode = CallDispatchHeadNode.createPrivate();
        @Child private BooleanCastNode booleanCastNode = BooleanCastNode.create();

        public GetMethodObjectNode(boolean ignoreVisibility) {
            this.ignoreVisibility = ignoreVisibility;
            lookupMethodNode = LookupMethodNodeGen.create(ignoreVisibility, !ignoreVisibility);
        }

        public abstract DynamicObject executeGetMethodObject(VirtualFrame frame, Object self, DynamicObject name);

        @Specialization
        protected DynamicObject methods(VirtualFrame frame, Object self, DynamicObject name,
                @Cached("createBinaryProfile()") ConditionProfile notFoundProfile,
                @Cached("createBinaryProfile()") ConditionProfile respondToMissingProfile) {
            final String normalizedName = nameToJavaStringNode.executeToJavaString(name);
            InternalMethod method = lookupMethodNode.executeLookupMethod(frame, self, normalizedName);

            if (notFoundProfile.profile(method == null)) {
                final Object respondToMissing = respondToMissingNode.call(self, "respond_to_missing?", name, ignoreVisibility);
                if (respondToMissingProfile.profile(booleanCastNode.executeToBoolean(respondToMissing))) {
                    final InternalMethod methodMissing = lookupMethodNode.executeLookupMethod(frame, self, "method_missing");
                    method = createMissingMethod(self, name, normalizedName, methodMissing);
                } else {
                    throw new RaiseException(getContext(), coreExceptions().nameErrorUndefinedMethod(normalizedName, coreLibrary().getLogicalClass(self), this));
                }
            }

            return Layouts.METHOD.createMethod(coreLibrary().getMethodFactory(), self, method);
        }

        @TruffleBoundary
        private InternalMethod createMissingMethod(Object self, DynamicObject name, String normalizedName, InternalMethod methodMissing) {
            final SharedMethodInfo info = methodMissing.getSharedMethodInfo().withMethodName(normalizedName);

            final RubyNode newBody = new CallMethodMissingWithStaticName(name);
            final RubyRootNode newRootNode = new RubyRootNode(getContext(), info.getSourceSection(), new FrameDescriptor(nil()), info, newBody);
            final RootCallTarget newCallTarget = Truffle.getRuntime().createCallTarget(newRootNode);

            final DynamicObject module = coreLibrary().getMetaClass(self);
            return new InternalMethod(getContext(), info, methodMissing.getLexicalScope(), DeclarationContext.NONE,
                    normalizedName, module, Visibility.PUBLIC, newCallTarget);
        }

        private static class CallMethodMissingWithStaticName extends RubyNode {

            private final DynamicObject methodName;
            @Child private CallDispatchHeadNode methodMissing = CallDispatchHeadNode.createPrivate();

            public CallMethodMissingWithStaticName(DynamicObject methodName) {
                this.methodName = methodName;
            }

            @Override
            public Object execute(VirtualFrame frame) {
                final Object[] originalUserArguments = RubyArguments.getArguments(frame);
                final Object[] newUserArguments = ArrayUtils.unshift(originalUserArguments, methodName);
                return methodMissing.callWithBlock(RubyArguments.getSelf(frame), "method_missing", RubyArguments.getBlock(frame), newUserArguments);
            }
        }

    }

    @CoreMethod(names = "methods", optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "regular")
    })
    public abstract static class MethodsNode extends CoreMethodNode {

        @CreateCast("regular")
        public RubyNode coerceToBoolean(RubyNode regular) {
            return BooleanCastWithDefaultNodeGen.create(true, regular);
        }

        @TruffleBoundary
        @Specialization(guards = "regular")
        public DynamicObject methodsRegular(Object self, boolean regular,
                                            @Cached("createMetaClassNode()") MetaClassNode metaClassNode) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(self);

            Object[] objects = Layouts.MODULE.getFields(metaClass).filterMethodsOnObject(getContext(), regular, MethodFilter.PUBLIC_PROTECTED).toArray();
            return createArray(objects, objects.length);
        }

        @Specialization(guards = "!regular")
        public DynamicObject methodsSingleton(VirtualFrame frame, Object self, boolean regular,
                                              @Cached("createSingletonMethodsNode()") SingletonMethodsNode singletonMethodsNode) {
            return singletonMethodsNode.executeSingletonMethods(frame, self, false);
        }

        protected MetaClassNode createMetaClassNode() {
            return MetaClassNode.create();
        }

        protected SingletonMethodsNode createSingletonMethodsNode() {
            return SingletonMethodsNodeFactory.create(null, null);
        }

    }

    @CoreMethod(names = "nil?", needsSelf = false)
    public abstract static class NilNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean isNil() {
            return false;
        }
    }

    // A basic Kernel#p for debugging core, overridden later in kernel.rb
    @NonStandard
    @CoreMethod(names = "p", isModuleFunction = true, required = 1)
    public abstract static class DebugPrintNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode callInspectNode = CallDispatchHeadNode.createPrivate();

        @Specialization
        public Object p(VirtualFrame frame, Object value) {
            Object inspected = callInspectNode.call(value, "inspect");
            print(inspected);
            return value;
        }

        @TruffleBoundary
        private void print(Object inspected) {
            final PrintStream stream = new PrintStream(getContext().getEnv().out(), true);
            stream.println(inspected.toString());
        }
    }

    @CoreMethod(names = "private_methods", optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "includeAncestors")
    })
    public abstract static class PrivateMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("includeAncestors")
        public RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject privateMethods(Object self, boolean includeAncestors) {
            DynamicObject metaClass = metaClassNode.executeMetaClass(self);

            Object[] objects = Layouts.MODULE.getFields(metaClass).filterMethodsOnObject(getContext(), includeAncestors, MethodFilter.PRIVATE).toArray();
            return createArray(objects, objects.length);
        }

    }

    @CoreMethod(names = "proc", isModuleFunction = true, needsBlock = true)
    public abstract static class ProcNode extends CoreMethodArrayArgumentsNode {

        @Child private ProcNewNode procNewNode = ProcNewNodeFactory.create(null);

        @Specialization
        public DynamicObject proc(VirtualFrame frame, Object maybeBlock) {
            return procNewNode.executeProcNew(frame, coreLibrary().getProcClass(), ArrayUtils.EMPTY_ARRAY, maybeBlock);
        }

    }

    @CoreMethod(names = "protected_methods", optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "includeAncestors")
    })
    public abstract static class ProtectedMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("includeAncestors")
        public RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject protectedMethods(Object self, boolean includeAncestors) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(self);

            Object[] objects = Layouts.MODULE.getFields(metaClass).filterMethodsOnObject(getContext(), includeAncestors, MethodFilter.PROTECTED).toArray();
            return createArray(objects, objects.length);
        }

    }

    @CoreMethod(names = "public_method", required = 1)
    @NodeChild(type = RubyNode.class, value = "receiver")
    @NodeChild(type = RubyNode.class, value = "name")
    public abstract static class PublicMethodNode extends CoreMethodNode {

        @Child private GetMethodObjectNode getMethodObjectNode = GetMethodObjectNode.create(false);

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return ToStringOrSymbolNodeGen.create(name);
        }

        @Specialization
        protected DynamicObject publicMethod(VirtualFrame frame, Object self, DynamicObject name) {
            return getMethodObjectNode.executeGetMethodObject(frame, self, name);
        }

    }

    @CoreMethod(names = "public_methods", optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "includeAncestors")
    })
    public abstract static class PublicMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("includeAncestors")
        public RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject publicMethods(Object self, boolean includeAncestors) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(self);

            Object[] objects = Layouts.MODULE.getFields(metaClass).filterMethodsOnObject(getContext(), includeAncestors, MethodFilter.PUBLIC).toArray();
            return createArray(objects, objects.length);
        }

    }

    @CoreMethod(names = "public_send", needsBlock = true, required = 1, rest = true)
    public abstract static class PublicSendNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode dispatchNode = CallDispatchHeadNode.createPublic();
        @Child private ReadCallerFrameNode readCallerFrame = ReadCallerFrameNode.create();

        @Specialization
        public Object send(VirtualFrame frame, Object self, Object name, Object[] args, NotProvided block) {
            return send(frame, self, name, args, (DynamicObject) null);
        }

        @Specialization
        public Object send(VirtualFrame frame, Object self, Object name, Object[] args, DynamicObject block) {
            DeclarationContext context = RubyArguments.getDeclarationContext(readCallerFrame.execute(frame));
            RubyArguments.setDeclarationContext(frame, context);

            return dispatchNode.dispatch(frame, self, name, block, args);
        }

    }

    @CoreMethod(names = "require", isModuleFunction = true, required = 1)
    @NodeChild(type = RubyNode.class, value = "feature")
    public abstract static class KernelRequireNode extends CoreMethodNode {

        @CreateCast("feature")
        public RubyNode coerceFeatureToPath(RubyNode feature) {
            return ToPathNodeGen.create(feature);
        }

        @Specialization(guards = "isRubyString(featureString)")
        public boolean require(DynamicObject featureString,
                @Cached("create()") RequireNode requireNode) {

            String feature = StringOperations.getString(featureString);

            // TODO CS 1-Mar-15 ERB will use strscan if it's there, but strscan is not yet complete, so we need to hide it
            if (feature.equals("strscan") && callerIs("mri/erb.rb")) {
                throw new RaiseException(getContext(), coreExceptions().loadErrorCannotLoad(feature, this));
            }

            return requireNode.executeRequire(feature);
        }

        @TruffleBoundary
        private boolean callerIs(String caller) {
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(this);

            for (Activation activation : backtrace.getActivations()) {
                final Node callNode = activation.getCallNode();
                final Source source = callNode == null ? null : callNode.getEncapsulatingSourceSection().getSource();

                if (source != null && source.getName().endsWith(caller)) {
                    return true;
                }
            }

            return false;
        }
    }

    @CoreMethod(names = "require_relative", isModuleFunction = true, required = 1)
    @NodeChild(type = RubyNode.class, value = "feature")
    public abstract static class RequireRelativeNode extends CoreMethodNode {

        @CreateCast("feature")
        public RubyNode coerceToPath(RubyNode feature) {
            return NameToJavaStringNodeGen.create(ToPathNodeGen.create(feature));
        }

        @Specialization
        public boolean requireRelative(String feature,
                @Cached("create()") RequireNode requireNode) {
            return requireNode.executeRequire(getFullPath(feature));
        }

        @TruffleBoundary
        private String getFullPath(String featureString) {
            final String featurePath;

            if (new File(featureString).isAbsolute()) {
                featurePath = featureString;
            } else {
                final Source source = getContext().getCallStack().getCallerFrameIgnoringSend().getCallNode().getEncapsulatingSourceSection().getSource();

                String sourcePath = getContext().getAbsolutePath(source);
                if (sourcePath == null) {
                    // Use the filename passed to eval as basepath
                    sourcePath = source.getName();
                }

                if (sourcePath == null) {
                    throw new RaiseException(getContext(), coreExceptions().loadError("cannot infer basepath", featureString, this));
                }

                final String cwd = getContext().getFeatureLoader().getWorkingDirectory();
                sourcePath = getContext().getFeatureLoader().canonicalize(cwd, sourcePath);

                featurePath = getContext().getFeatureLoader().dirname(sourcePath) + "/" + featureString;
            }

            // Normalize the path like File.expand_path() (e.g., remove "../"), but do not resolve
            // symlinks. MRI does this for #require_relative always, but not for #require, so we
            // need to do it to be compatible in the case the path does not exist, so the
            // LoadError's #path is the same as MRI's.
            return Paths.get(featurePath).normalize().toString();
        }
    }

    @CoreMethod(names = "respond_to?", required = 1, optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "includeProtectedAndPrivate")
    })
    public abstract static class RespondToNode extends CoreMethodNode {

        @Child private DoesRespondDispatchHeadNode dispatch;
        @Child private DoesRespondDispatchHeadNode dispatchIgnoreVisibility;
        @Child private DoesRespondDispatchHeadNode dispatchRespondToMissing;
        @Child private CallDispatchHeadNode respondToMissingNode;
        @Child private BooleanCastNode booleanCastNode;
        private final ConditionProfile ignoreVisibilityProfile = ConditionProfile.createBinaryProfile();

        public RespondToNode() {
            dispatch = DoesRespondDispatchHeadNode.createPublic();
            dispatchIgnoreVisibility = DoesRespondDispatchHeadNode.create();
            dispatchRespondToMissing = DoesRespondDispatchHeadNode.create();
        }

        public abstract boolean executeDoesRespondTo(VirtualFrame frame, Object object, Object name, boolean includeProtectedAndPrivate);

        @CreateCast("includeProtectedAndPrivate")
        public RubyNode coerceToBoolean(RubyNode includeProtectedAndPrivate) {
            return BooleanCastWithDefaultNodeGen.create(false, includeProtectedAndPrivate);
        }

        @Specialization(guards = "isRubyString(name)")
        public boolean doesRespondToString(VirtualFrame frame, Object object, DynamicObject name, boolean includeProtectedAndPrivate) {
            final boolean ret;

            if (ignoreVisibilityProfile.profile(includeProtectedAndPrivate)) {
                ret = dispatchIgnoreVisibility.doesRespondTo(frame, name, object);
            } else {
                ret = dispatch.doesRespondTo(frame, name, object);
            }

            if (ret) {
                return true;
            } else if (dispatchRespondToMissing.doesRespondTo(frame, "respond_to_missing?", object)) {
                return respondToMissing(frame, object, getSymbol(StringOperations.rope(name)), includeProtectedAndPrivate);
            } else {
                return false;
            }
        }

        @Specialization(guards = "isRubySymbol(name)")
        public boolean doesRespondToSymbol(VirtualFrame frame, Object object, DynamicObject name, boolean includeProtectedAndPrivate) {
            final boolean ret;

            if (ignoreVisibilityProfile.profile(includeProtectedAndPrivate)) {
                ret = dispatchIgnoreVisibility.doesRespondTo(frame, name, object);
            } else {
                ret = dispatch.doesRespondTo(frame, name, object);
            }

            if (ret) {
                return true;
            } else if (dispatchRespondToMissing.doesRespondTo(frame, "respond_to_missing?", object)) {
                return respondToMissing(frame, object, name, includeProtectedAndPrivate);
            } else {
                return false;
            }
        }

        private boolean respondToMissing(VirtualFrame frame, Object object, DynamicObject name, boolean includeProtectedAndPrivate) {
            if (respondToMissingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToMissingNode = insert(CallDispatchHeadNode.createPrivate());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.executeToBoolean(respondToMissingNode.call(object, "respond_to_missing?", name, includeProtectedAndPrivate));
        }
    }

    @CoreMethod(names = "respond_to_missing?", required = 2)
    public abstract static class RespondToMissingNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(name)")
        public boolean doesRespondToMissingString(Object object, DynamicObject name, Object unusedIncludeAll) {
            return false;
        }

        @Specialization(guards = "isRubySymbol(name)")
        public boolean doesRespondToMissingSymbol(Object object, DynamicObject name, Object unusedIncludeAll) {
            return false;
        }

    }

    @CoreMethod(names = "set_trace_func", isModuleFunction = true, required = 1)
    public abstract static class SetTraceFuncNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNil(nil)")
        public DynamicObject setTraceFunc(Object nil) {
            getContext().getTraceManager().setTraceFunc(null);
            return nil();
        }

        @Specialization(guards = "isRubyProc(traceFunc)")
        public DynamicObject setTraceFunc(DynamicObject traceFunc) {
            getContext().getTraceManager().setTraceFunc(traceFunc);
            return traceFunc;
        }
    }

    @CoreMethod(names = "singleton_class")
    public abstract static class SingletonClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();

        @Specialization
        public DynamicObject singletonClass(Object self) {
            return singletonClassNode.executeSingletonClass(self);
        }

    }

    @CoreMethod(names = "singleton_method", required = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class SingletonMethodNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization
        public DynamicObject singletonMethod(Object self, String name,
                @Cached("create()") BranchProfile errorProfile,
                @Cached("createBinaryProfile()") ConditionProfile singletonProfile,
                @Cached("createBinaryProfile()") ConditionProfile methodProfile) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(self);

            if (singletonProfile.profile(Layouts.CLASS.getIsSingleton(metaClass))) {
                final InternalMethod method = Layouts.MODULE.getFields(metaClass).getMethod(name);
                if (methodProfile.profile(method != null && !method.isUndefined())) {
                    return Layouts.METHOD.createMethod(coreLibrary().getMethodFactory(), self, method);
                }
            }

            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().nameErrorUndefinedSingletonMethod(name, self, this));
        }

    }

    @CoreMethod(names = "singleton_methods", optional = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "object"),
            @NodeChild(type = RubyNode.class, value = "includeAncestors")
    })
    public abstract static class SingletonMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        public abstract DynamicObject executeSingletonMethods(VirtualFrame frame, Object self, boolean includeAncestors);

        @CreateCast("includeAncestors")
        public RubyNode coerceToBoolean(RubyNode includeAncestors) {
            return BooleanCastWithDefaultNodeGen.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject singletonMethods(Object self, boolean includeAncestors) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(self);

            if (!Layouts.CLASS.getIsSingleton(metaClass)) {
                return createArray(null, 0);
            }

            Object[] objects = Layouts.MODULE.getFields(metaClass).filterSingletonMethods(getContext(), includeAncestors, MethodFilter.PUBLIC_PROTECTED).toArray();
            return createArray(objects, objects.length);
        }

    }

    @NodeChild(value = "duration", type = RubyNode.class)
    @CoreMethod(names = "sleep", isModuleFunction = true, optional = 1)
    public abstract static class SleepNode extends CoreMethodNode {

        @CreateCast("duration")
        public RubyNode coerceDuration(RubyNode duration) {
            return DurationToMillisecondsNodeGen.create(false, duration);
        }

        @Specialization
        public long sleep(VirtualFrame frame, long durationInMillis,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached("create()") BranchProfile errorProfile) {
            if (durationInMillis < 0) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError("time interval must be positive", this));
            }

            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);

            // Clear the wakeUp flag, following Ruby semantics:
            // it should only be considered if we are inside the sleep when Thread#{run,wakeup} is called.
            Layouts.THREAD.getWakeUp(thread).set(false);

            return sleepFor(this, getContext(), thread, durationInMillis);
        }

        @TruffleBoundary
        public static long sleepFor(Node currentNode, RubyContext context, DynamicObject thread, long durationInMillis) {
            assert durationInMillis >= 0;

            // We want a monotonic clock to measure sleep duration
            final long startInNanos = System.nanoTime();

            context.getThreadManager().runUntilResult(currentNode, () -> {
                final long nowInNanos = System.nanoTime();
                final long sleptInNanos = nowInNanos - startInNanos;
                final long sleptInMillis = TimeUnit.NANOSECONDS.toMillis(sleptInNanos);

                if (sleptInMillis >= durationInMillis || Layouts.THREAD.getWakeUp(thread).getAndSet(false)) {
                    return BlockingAction.SUCCESS;
                }

                Thread.sleep(durationInMillis - sleptInMillis);
                return BlockingAction.SUCCESS;
            });

            return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startInNanos);
        }

    }

    @CoreMethod(names = { "format", "sprintf" }, isModuleFunction = true, rest = true, required = 1, taintFrom = 1)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class SprintfNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode;
        @Child private TaintNode taintNode;
        @Child private BooleanCastNode readDebugGlobalNode = BooleanCastNodeGen.create(ReadGlobalVariableNodeGen.create("$DEBUG"));

        private final BranchProfile exceptionProfile = BranchProfile.create();
        private final ConditionProfile resizeProfile = ConditionProfile.createBinaryProfile();

        @Specialization(guards = { "isRubyString(format)", "equalNode.execute(rope(format), cachedFormat)", "isDebug(frame) == cachedIsDebug" })
        public DynamicObject formatCached(
                VirtualFrame frame,
                DynamicObject format,
                Object[] arguments,
                @Cached("isDebug(frame)") boolean cachedIsDebug,
                @Cached("privatizeRope(format)") Rope cachedFormat,
                @Cached("ropeLength(cachedFormat)") int cachedFormatLength,
                @Cached("create(compileFormat(format, arguments, isDebug(frame)))") DirectCallNode callPackNode,
                @Cached("create()") RopeNodes.EqualNode equalNode,
                @Cached("create()") IsTaintedNode isTaintedNode) {
            final BytesResult result;

            try {
                result = (BytesResult) callPackNode.call(
                        new Object[]{ arguments, arguments.length, isTaintedNode.isTainted(format), null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(this, e);
            }

            return finishFormat(cachedFormatLength, result);
        }

        @Specialization(guards = "isRubyString(format)", replaces = "formatCached")
        public DynamicObject formatUncached(
                VirtualFrame frame,
                DynamicObject format,
                Object[] arguments,
                @Cached("create()") IndirectCallNode callPackNode,
                @Cached("create()") IsTaintedNode isTaintedNode) {
            final BytesResult result;

            final boolean isDebug = readDebugGlobalNode.executeBoolean(frame);

            try {
                result = (BytesResult) callPackNode.call(compileFormat(format, arguments, isDebug),
                        new Object[]{ arguments, arguments.length, isTaintedNode.isTainted(format), null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(this, e);
            }

            return finishFormat(Layouts.STRING.getRope(format).byteLength(), result);
        }

        private DynamicObject finishFormat(int formatLength, BytesResult result) {
            byte[] bytes = result.getOutput();

            if (resizeProfile.profile(bytes.length != result.getOutputLength())) {
                bytes = Arrays.copyOf(bytes, result.getOutputLength());
            }

            if (makeStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeStringNode = insert(StringNodes.MakeStringNode.create());
            }

            final DynamicObject string = makeStringNode.executeMake(
                    bytes,
                    result.getEncoding().getEncodingForLength(formatLength),
                    result.getStringCodeRange());

            if (result.isTainted()) {
                if (taintNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    taintNode = insert(TaintNode.create());
                }

                taintNode.executeTaint(string);
            }

            return string;
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(DynamicObject format, Object[] arguments, boolean isDebug) {
            try {
                return new PrintfCompiler(getContext(), this)
                        .compile(StringOperations.rope(format), arguments, isDebug);
            } catch (InvalidFormatException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        protected boolean isDebug(VirtualFrame frame) {
            return readDebugGlobalNode.executeBoolean(frame);
        }

    }

    @Primitive(name = "kernel_global_variables", needsSelf = false)
    public abstract static class KernelGlobalVariablesPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject globalVariables() {
            final Collection<String> keys = coreLibrary().getGlobalVariables().keys();
            final Object[] store = new Object[keys.size()];
            int i = 0;
            for (String key : keys) {
                store[i] = getSymbol(key);
                i++;
            }
            return createArray(store, store.length);
        }

    }

    @CoreMethod(names = "taint")
    public abstract static class KernelTaintNode extends CoreMethodArrayArgumentsNode {

        @Child private TaintNode taintNode;

        @Specialization
        public Object taint(Object object) {
            if (taintNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                taintNode = insert(TaintNode.create());
            }
            return taintNode.executeTaint(object);
        }

    }

    @CoreMethod(names = "tainted?")
    public abstract static class KernelIsTaintedNode extends CoreMethodArrayArgumentsNode {

        @Child private IsTaintedNode isTaintedNode;

        @Specialization
        public boolean isTainted(Object object) {
            if (isTaintedNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isTaintedNode = insert(IsTaintedNode.create());
            }
            return isTaintedNode.executeIsTainted(object);
        }

    }

    public abstract static class ToHexStringNode extends CoreMethodArrayArgumentsNode {

        public abstract String executeToHexString(Object value);

        @Specialization
        public String toHexString(int value) {
            return toHexString((long) value);
        }

        @Specialization
        public String toHexString(long value) {
            return Long.toHexString(value);
        }

        @Specialization(guards = "isRubyBignum(value)")
        public String toHexString(DynamicObject value) {
            return Layouts.BIGNUM.getValue(value).toString(16);
        }

    }

    @CoreMethod(names = {"to_s", "inspect"}) // Basic inspect, refined later in core
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        public static ToSNode create() {
            return KernelNodesFactory.ToSNodeFactory.create(null);
        }

        @Child private LogicalClassNode classNode = LogicalClassNode.create();
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();
        @Child private ObjectIDNode objectIDNode = ObjectIDNodeFactory.create(null);
        @Child private TaintResultNode taintResultNode = new TaintResultNode();
        @Child private ToHexStringNode toHexStringNode = ToHexStringNodeFactory.create(null);

        public abstract DynamicObject executeToS(Object self);

        @Specialization
        public DynamicObject toS(Object self) {
            String className = Layouts.MODULE.getFields(classNode.executeLogicalClass(self)).getName();
            Object id = objectIDNode.executeObjectID(self);
            String hexID = toHexStringNode.executeToHexString(id);

            final DynamicObject string = makeStringNode.executeMake("#<" + className + ":0x" + hexID + ">", UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            taintResultNode.maybeTaint(self, string);
            return string;
        }

    }

    @Primitive(name = "object_to_s")
    public abstract static class ObjectToSNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject toS(Object obj,
                                 @Cached("create()") ToSNode kernelToSNode) {
            return kernelToSNode.executeToS(obj);
        }

    }

    @CoreMethod(names = "untaint")
    public abstract static class UntaintNode extends CoreMethodArrayArgumentsNode {

        @Child private IsFrozenNode isFrozenNode;
        @Child private IsTaintedNode isTaintedNode = IsTaintedNode.create();
        @Child private WriteObjectFieldNode writeTaintNode = WriteObjectFieldNodeGen.create(Layouts.TAINTED_IDENTIFIER);

        @Specialization
        public int untaint(int num) {
            return num;
        }

        @Specialization
        public long untaint(long num) {
            return num;
        }

        @Specialization
        public double untaint(double num) {
            return num;
        }

        @Specialization
        public boolean untaint(boolean bool) {
            return bool;
        }

        @Specialization
        public Object taint(DynamicObject object) {
            if (!isTaintedNode.executeIsTainted(object)) {
                return object;
            }

            checkFrozen(object);
            writeTaintNode.write(object, false);
            return object;
        }

        protected void checkFrozen(Object object) {
            if (isFrozenNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isFrozenNode = insert(IsFrozenNode.create());
            }
            isFrozenNode.raiseIfFrozen(object);
        }

    }

}
