/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.object.PropertyGetter;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.utilities.AssumedValue;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.binding.RubyBinding;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.core.cast.BooleanCastWithDefaultNode;
import org.truffleruby.core.cast.DurationToNanoSecondsNodeGen;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.cast.ToStringOrSymbolNode;
import org.truffleruby.core.cast.ToSymbolNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.exception.GetBacktraceException;
import org.truffleruby.core.format.BytesResult;
import org.truffleruby.core.format.FormatExceptionTranslator;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.exceptions.InvalidFormatException;
import org.truffleruby.core.format.printf.PrintfCompiler;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.PackedHashStoreLibrary;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.kernel.KernelNodesFactory.SameOrEqualNodeFactory;
import org.truffleruby.core.kernel.KernelNodesFactory.SingletonMethodsNodeFactory;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.method.MethodFilter;
import org.truffleruby.core.method.RubyMethod;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.proc.ProcNodes.ProcNewNode;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.range.RangeNodes;
import org.truffleruby.core.range.RubyIntOrLongRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringHelperNodes;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.core.support.TypeNodes.CheckFrozenNode;
import org.truffleruby.core.support.TypeNodes.ObjectInstanceVariablesNode;
import org.truffleruby.core.support.TypeNodesFactory.ObjectInstanceVariablesNodeFactory;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolNodes;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.language.ImmutableRubyObject;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.WarnNode;
import org.truffleruby.language.WarningNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchConfiguration;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.InternalRespondToNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.loader.EvalLoader;
import org.truffleruby.language.globals.ReadGlobalVariableNodeGen;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.loader.RequireNode;
import org.truffleruby.language.loader.RequireNodeGen;
import org.truffleruby.language.locals.FindDeclarationVariableNodes.FindAndReadDeclarationVariableNode;
import org.truffleruby.language.methods.GetMethodObjectNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.CheckIVarNameNode;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.IsCopyableObjectNode;
import org.truffleruby.language.objects.IsCopyableObjectNodeGen;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.ShapeCachingGuards;
import org.truffleruby.language.objects.SingletonClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorEnvironment;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.object.Property;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule("Kernel")
public abstract class KernelNodes {

    /** Check if operands are the same object or call #==. Known as rb_equal() in MRI. The fact Kernel#=== uses this is
     * pure coincidence. */
    @Primitive(name = "object_same_or_equal")
    public abstract static class SameOrEqualNode extends PrimitiveArrayArgumentsNode {

        @Child private DispatchNode equalNode;
        @Child private BooleanCastNode booleanCastNode;

        private final ConditionProfile sameProfile = ConditionProfile.create();

        public static SameOrEqualNode create() {
            return SameOrEqualNodeFactory.create(null);
        }

        public abstract boolean executeSameOrEqual(Object a, Object b);

        @Specialization
        protected boolean sameOrEqual(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqualNode) {
            if (sameProfile.profile(referenceEqualNode.executeReferenceEqual(a, b))) {
                return true;
            } else {
                return areEqual(a, b);
            }
        }

        private boolean areEqual(Object left, Object right) {
            if (equalNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                equalNode = insert(DispatchNode.create());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.execute(equalNode.call(left, "==", right));
        }

    }

    @CoreMethod(names = "===", required = 1)
    public abstract static class CaseCompareNode extends CoreMethodArrayArgumentsNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization
        protected boolean caseCmp(Object a, Object b) {
            return sameOrEqualNode.executeSameOrEqual(a, b);
        }

    }

    /** Check if operands are the same object or call #eql? */
    @GenerateUncached
    public abstract static class SameOrEqlNode extends RubyBaseNode {

        public static SameOrEqlNode create() {
            return KernelNodesFactory.SameOrEqlNodeGen.create();
        }

        public static SameOrEqlNode getUncached() {
            return KernelNodesFactory.SameOrEqlNodeGen.getUncached();
        }

        public abstract boolean execute(Object a, Object b);

        @Specialization(guards = "referenceEqual.executeReferenceEqual(a, b)")
        protected boolean refEqual(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqual) {
            return true;
        }

        @Specialization(replaces = "refEqual")
        protected boolean refEqualOrEql(Object a, Object b,
                @Cached ReferenceEqualNode referenceEqual,
                @Cached DispatchNode eql,
                @Cached BooleanCastNode booleanCast) {
            return referenceEqual.executeReferenceEqual(a, b) || booleanCast.execute(eql.call(a, "eql?", b));
        }
    }

    @Primitive(name = "find_file")
    public abstract static class FindFileNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libFeatureString.isRubyString(featureString)", limit = "1")
        protected Object findFile(Object featureString,
                @Cached BranchProfile notFoundProfile,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                @Cached RubyStringLibrary libFeatureString,
                @Cached ToJavaStringNode toJavaStringNode) {
            String feature = toJavaStringNode.executeToJavaString(featureString);
            return findFileString(feature, notFoundProfile, fromJavaStringNode);
        }

        @Specialization
        protected Object findFileString(String featureString,
                @Cached BranchProfile notFoundProfile,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            final String expandedPath = getContext().getFeatureLoader().findFeature(featureString);
            if (expandedPath == null) {
                notFoundProfile.enter();
                return nil;
            }
            return createString(fromJavaStringNode, expandedPath, Encodings.UTF_8);
        }

    }

    @Primitive(name = "get_caller_path")
    public abstract static class GetCallerPathNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libFeature.isRubyString(feature)", limit = "1")
        @TruffleBoundary
        protected RubyString getCallerPath(Object feature,
                @Cached RubyStringLibrary libFeature,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            final String featureString = RubyGuards.getJavaString(feature);
            final String featurePath;
            if (new File(featureString).isAbsolute()) {
                featurePath = featureString;
            } else {
                final SourceSection sourceSection = getContext()
                        .getCallStack()
                        .getCallerNode()
                        .getEncapsulatingSourceSection();
                if (!BacktraceFormatter.isAvailable(sourceSection)) {
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().loadError("cannot infer basepath", featureString, this));
                }

                String sourcePath = getLanguage().getSourcePath(sourceSection.getSource());

                sourcePath = getContext().getFeatureLoader().canonicalize(sourcePath);

                featurePath = getContext().getFeatureLoader().dirname(sourcePath) + "/" + featureString;
            }

            // Normalize the path like File.expand_path() (e.g., remove "../"), but do not resolve
            // symlinks. MRI does this for #require_relative always, but not for #require, so we
            // need to do it to be compatible in the case the path does not exist, so the
            // LoadError's #path is the same as MRI's.
            return createString(fromJavaStringNode, Paths.get(featurePath).normalize().toString(), Encodings.UTF_8);
        }

    }

    @Primitive(name = "load_feature")
    public abstract static class LoadFeatureNode extends PrimitiveArrayArgumentsNode {

        @Child private RequireNode requireNode = RequireNodeGen.create();

        @Specialization(guards = "libFeatureString.isRubyString(featureString)", limit = "1")
        protected boolean loadFeature(Object featureString, Object expandedPathString,
                @Cached RubyStringLibrary libFeatureString,
                @Cached ToJavaStringNode toJavaStringNode) {
            return requireNode.executeRequire(
                    toJavaStringNode.executeToJavaString(featureString),
                    expandedPathString);
        }

    }

    @CoreMethod(names = { "<=>" }, required = 1)
    public abstract static class CompareNode extends CoreMethodArrayArgumentsNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization
        protected Object compare(Object self, Object other) {
            if (sameOrEqualNode.executeSameOrEqual(self, other)) {
                return 0;
            } else {
                return nil;
            }
        }

    }

    @GenerateUncached
    @CoreMethod(names = "binding", isModuleFunction = true, alwaysInlined = true)
    public abstract static class BindingNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected RubyBinding binding(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached(
                        value = "getNode().getEncapsulatingSourceSection()",
                        allowUncached = true) SourceSection sourceSection) {
            needCallerFrame(callerFrame, target);
            return BindingNodes.createBinding(getContext(), getLanguage(), callerFrame.materialize(), sourceSection);
        }
    }

    @GenerateUncached
    @CoreMethod(names = { "block_given?", "iterator?" }, isModuleFunction = true, alwaysInlined = true)
    public abstract static class BlockGivenNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected boolean blockGiven(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached FindAndReadDeclarationVariableNode readNode,
                @Cached ConditionProfile blockProfile) {
            needCallerFrame(callerFrame, target);
            return blockProfile
                    .profile(readNode.execute(callerFrame, TranslatorEnvironment.METHOD_BLOCK_NAME, nil) != nil);
        }
    }

    @CoreMethod(names = "__callee__", isModuleFunction = true)
    public abstract static class CalleeNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySymbol calleeName() {
            // the "called name" of a method.
            return getSymbol(getContext().getCallStack().getCallingMethod().getName());
        }
    }

    @Primitive(name = "canonicalize_path")
    public abstract static class CanonicalizePathNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(string)", limit = "1")
        @TruffleBoundary
        protected RubyString canonicalPath(Object string,
                @Cached RubyStringLibrary strings,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            final String expandedPath = getContext()
                    .getFeatureLoader()
                    .canonicalize(RubyGuards.getJavaString(string));
            return createString(fromJavaStringNode, expandedPath, Encodings.UTF_8);
        }

    }

    @Primitive(name = "kernel_caller_locations", lowerFixnum = { 0, 1 })
    public abstract static class CallerLocationsNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object callerLocations(int omit, NotProvided length) {
            return innerCallerLocations(omit, GetBacktraceException.UNLIMITED);
        }

        @Specialization
        protected Object callerLocations(int omit, int length) {
            return innerCallerLocations(omit, length);
        }

        private Object innerCallerLocations(int omit, int length) {
            // Always skip #caller_locations.
            final int omitted = omit + 1;
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(this, omitted);
            return backtrace.getBacktraceLocations(getContext(), getLanguage(), length, this);
        }
    }

    @CoreMethod(names = "class")
    public abstract static class KernelClassNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        protected RubyClass getClass(Object self) {
            return classNode.execute(self);
        }

    }

    @ImportStatic(ShapeCachingGuards.class)
    @GenerateUncached
    public abstract static class CopyInstanceVariablesNode extends RubyBaseNode {

        public static final PropertyGetter[] EMPTY_PROPERTY_GETTER_ARRAY = new PropertyGetter[0];

        public abstract RubyDynamicObject execute(RubyDynamicObject newObject, RubyDynamicObject from);

        @ExplodeLoop
        @Specialization(
                guards = { "from.getShape() == cachedShape", "propertyGetters.length <= MAX_EXPLODE_SIZE" },
                limit = "getDynamicObjectCacheLimit()")
        protected RubyDynamicObject copyCached(RubyDynamicObject newObject, RubyDynamicObject from,
                @Cached("from.getShape()") Shape cachedShape,
                @Cached(value = "getCopiedProperties(cachedShape)", dimensions = 1) PropertyGetter[] propertyGetters,
                @Cached("createWriteFieldNodes(propertyGetters)") DynamicObjectLibrary[] writeFieldNodes) {
            for (int i = 0; i < propertyGetters.length; i++) {
                final PropertyGetter propertyGetter = propertyGetters[i];
                final Object value = propertyGetter.get(from);
                writeFieldNodes[i].putWithFlags(newObject, propertyGetter.getKey(), value, propertyGetter.getFlags());
            }

            return newObject;
        }

        @Specialization(guards = "updateShape(from)")
        protected RubyDynamicObject updateShapeAndCopy(RubyDynamicObject newObject, RubyDynamicObject from) {
            return execute(newObject, from);
        }

        @Specialization(replaces = { "copyCached", "updateShapeAndCopy" })
        protected RubyDynamicObject copyUncached(RubyDynamicObject newObject, RubyDynamicObject from) {
            copyInstanceVariables(from, newObject);
            return newObject;
        }

        protected PropertyGetter[] getCopiedProperties(Shape shape) {
            final List<PropertyGetter> copiedProperties = new ArrayList<>();

            for (Property property : shape.getProperties()) {
                if (property.getKey() instanceof String) {
                    copiedProperties.add(Objects.requireNonNull(shape.makePropertyGetter(property.getKey())));
                }
            }

            return copiedProperties.toArray(EMPTY_PROPERTY_GETTER_ARRAY);
        }

        protected DynamicObjectLibrary[] createWriteFieldNodes(PropertyGetter[] propertyGetters) {
            final DynamicObjectLibrary[] nodes = new DynamicObjectLibrary[propertyGetters.length];
            for (int i = 0; i < propertyGetters.length; i++) {
                nodes[i] = DynamicObjectLibrary.getFactory().createDispatched(1);
            }
            return nodes;
        }

        @TruffleBoundary
        private void copyInstanceVariables(RubyDynamicObject from, RubyDynamicObject to) {
            // Concurrency: OK if callers create the object and publish it after copy
            // Only copy user-level instance variables, hidden ones are initialized later with #initialize_copy.
            Shape shape = from.getShape();
            for (PropertyGetter propertyGetter : getCopiedProperties(shape)) {
                DynamicObjectLibrary.getUncached().putWithFlags(
                        to,
                        propertyGetter.getKey(),
                        propertyGetter.get(from),
                        propertyGetter.getFlags());
            }
        }
    }

    @GenerateUncached
    public abstract static class CopyNode extends RubyBaseNode {

        public abstract RubyDynamicObject executeCopy(Object self);

        @Specialization(guards = "!isRubyClass(self)")
        protected RubyDynamicObject copyRubyDynamicObject(RubyDynamicObject self,
                @Cached DispatchNode allocateNode,
                @Cached CopyInstanceVariablesNode copyInstanceVariablesNode) {
            var newObject = (RubyDynamicObject) allocateNode.call(self.getLogicalClass(), "__allocate__");
            copyInstanceVariablesNode.execute(newObject, self);
            return newObject;
        }

        @Specialization
        protected RubyDynamicObject copyRubyClass(RubyClass self,
                @Cached CopyInstanceVariablesNode copyInstanceVariablesNode) {
            var newClass = new RubyClass(coreLibrary().classClass, getLanguage(), getEncapsulatingSourceSection(),
                    null, null, false, null, self.superclass);
            copyInstanceVariablesNode.execute(newClass, self);
            return newClass;
        }

        @Specialization
        protected RubyDynamicObject copy(ImmutableRubyString string,
                @Cached StringNodes.AllocateNode allocateStringNode) {
            return allocateStringNode.execute(coreLibrary().stringClass);
        }

        @Specialization
        protected RubyDynamicObject copy(RubyIntOrLongRange range,
                @Cached RangeNodes.AllocateNode allocateRangeNode) {
            return allocateRangeNode.execute(coreLibrary().rangeClass);
        }
    }

    @Primitive(name = "object_clone") // "clone"
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "freeze", type = RubyBaseNodeWithExecute.class)
    public abstract static class CloneNode extends PrimitiveNode {

        @Child IsCopyableObjectNode isCopyableObjectNode = IsCopyableObjectNodeGen.create();
        @Child SingletonClassNode singletonClassNode;
        private final BranchProfile cantUnfreezeErrorProfile = BranchProfile.create();

        @Specialization(guards = "isCopyableObjectNode.execute(object)", limit = "getRubyLibraryCacheLimit()")
        protected RubyDynamicObject copyable(Object object, Object freeze,
                @Cached MetaClassNode metaClassNode,
                @Cached CopyNode copyNode,
                @Cached DispatchNode initializeCloneNode,
                @Cached ConditionProfile isSingletonProfile,
                @Cached HashingNodes.ToHashByHashCode hashNode,
                @CachedLibrary("object") RubyLibrary rubyLibrary,
                @CachedLibrary(limit = "getRubyLibraryCacheLimit()") RubyLibrary rubyLibraryFreeze) {
            final RubyDynamicObject newObject = copyNode.executeCopy(object);

            // Copy the singleton class if any.
            final RubyClass selfMetaClass = metaClassNode.execute(object);
            if (isSingletonProfile.profile(selfMetaClass.isSingleton)) {
                final RubyClass newObjectMetaClass = executeSingletonClass(newObject);
                newObjectMetaClass.fields.initCopy(selfMetaClass);
            }

            final boolean copyFrozen = freeze instanceof Nil;

            if (copyFrozen) {
                initializeCloneNode.call(newObject, "initialize_clone", object);
            } else {
                // pass :freeze keyword argument to #initialize_clone
                final RubyHash keywordArguments = createFreezeBooleanHash((boolean) freeze, hashNode);
                initializeCloneNode.callWithKeywords(newObject, "initialize_clone", object, keywordArguments);
            }

            // Default behavior - is just to copy the frozen state of the original object
            if (forceFrozen(freeze) || (copyFrozen && rubyLibrary.isFrozen(object))) { // Profiled through lazy usage of rubyLibraryFreeze
                rubyLibraryFreeze.freeze(newObject);
            }

            return newObject;
        }

        @Specialization(guards = "!isCopyableObjectNode.execute(object)")
        protected Object notCopyable(Object object, Object freeze) {
            if (forceNotFrozen(freeze)) {
                raiseCantUnfreezeError(object);
            }
            return object;
        }

        private RubyHash createFreezeBooleanHash(boolean freeze, HashingNodes.ToHashByHashCode hashNode) {
            final RubySymbol key = coreSymbols().FREEZE;

            final Object[] newStore = PackedHashStoreLibrary.createStore();
            final int hashed = hashNode.execute(key);
            PackedHashStoreLibrary.setHashedKeyValue(newStore, 0, hashed, key, freeze);

            return new RubyHash(coreLibrary().hashClass, getLanguage().hashShape, getContext(), newStore, 1, false);
        }

        private boolean forceFrozen(Object freeze) {
            return freeze instanceof Boolean && (boolean) freeze;

        }

        private boolean forceNotFrozen(Object freeze) {
            return freeze instanceof Boolean && !(boolean) freeze;
        }

        private void raiseCantUnfreezeError(Object object) {
            cantUnfreezeErrorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentErrorCantUnfreeze(object, this));
        }

        private RubyClass executeSingletonClass(RubyDynamicObject newObject) {
            if (singletonClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singletonClassNode = insert(SingletonClassNode.create());
            }

            return singletonClassNode.executeSingletonClass(newObject);
        }

    }

    // Worth always splitting to have monomorphic #__allocate__, Shape, #initialize_dup and #initialize_copy.
    // OK to always inline as the graph is likely to be simplified a lot as the allocation is visible,
    // and a non-inlined call to #__allocate__ would allocate the arguments Object[] which is about the same number of
    // nodes as the object allocation. Also avoids many frame and Object[] allocations when dup'ing a new object.
    @GenerateUncached
    @CoreMethod(names = "dup", alwaysInlined = true)
    public abstract static class DupNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object dup(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached IsCopyableObjectNode isCopyableObjectNode,
                @Cached ConditionProfile isCopyableProfile,
                @Cached CopyNode copyNode,
                @Cached DispatchNode initializeDupNode) {
            if (isCopyableProfile.profile(isCopyableObjectNode.execute(self))) {
                final RubyDynamicObject copy = copyNode.executeCopy(self);

                initializeDupNode.call(copy, "initialize_dup", self);

                return copy;
            } else {
                return self;
            }
        }
    }

    @NodeChild(value = "self", type = RubyNode.class)
    @GenerateNodeFactory
    public abstract static class DupASTNode extends RubyContextSourceNode {
        @Specialization
        protected Object execute(VirtualFrame frame, Object self,
                @Cached DupNode dupNode) {
            return dupNode.execute(frame, self, ArrayUtils.EMPTY_ARRAY, null);
        }
    }

    @GenerateUncached
    @CoreMethod(names = "eval", isModuleFunction = true, required = 1, optional = 3, alwaysInlined = true)
    public abstract static class EvalPrepareArgsNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object eval(Frame callerFrame, Object callerSelf, Object[] rubyArgs, RootCallTarget target,
                @Cached ToStrNode toStrNode,
                @Cached ToIntNode toIntNode,
                @Cached BranchProfile errorProfile,
                @Cached ConditionProfile hasBindingArgument,
                @Cached EvalInternalNode evalInternalNode,
                @Cached ConditionProfile fileAndLineProfile,
                @Cached ConditionProfile fileNoLineProfile) {

            final Object[] args = RubyArguments.getPositionalArguments(rubyArgs, false);
            final Object source = toStrNode.execute(args[0]);

            final RubyBinding binding;
            final Object self;
            if (hasBindingArgument.profile(args.length > 1 && args[1] != nil)) {
                final Object bindingArg = args[1];
                if (!(bindingArg instanceof RubyBinding)) {
                    errorProfile.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().typeErrorWrongArgumentType(bindingArg, "binding", getNode()));
                }
                binding = (RubyBinding) bindingArg;
                self = RubyArguments.getSelf(binding.getFrame());
            } else {
                needCallerFrame(callerFrame, "Kernel#eval with no Binding argument");
                binding = BindingNodes.createBinding(getContext(), getLanguage(), callerFrame.materialize());
                self = callerSelf;
            }

            final Object file;
            final int line;

            if (fileAndLineProfile.profile(args.length > 3 && args[3] != nil)) {
                line = toIntNode.execute(args[3]);
                file = toStrNode.execute(args[2]);
            } else if (fileNoLineProfile.profile(args.length > 2 && args[2] != nil)) {
                file = toStrNode.execute(args[2]);
                line = 1;
            } else {
                file = coreStrings().EVAL_FILENAME_STRING.createInstance(getContext());
                line = 1;
            }

            return evalInternalNode.execute(self, source, binding, file, line);
        }
    }

    @ReportPolymorphism
    @GenerateUncached
    public abstract static class EvalInternalNode extends RubyBaseNode {

        public abstract Object execute(Object self, Object source, RubyBinding binding, Object file, int line);

        @Specialization(
                guards = {
                        "libSource.isRubyString(source)",
                        "libFile.isRubyString(file)",
                        "codeEqualNode.execute(libSource, source, cachedSource, cachedSourceEnc)",
                        "fileEqualNode.execute(libFile, file, cachedFile, cachedFileEnc)",
                        "line == cachedLine",
                        "bindingDescriptor == getBindingDescriptor(binding)" },
                limit = "getCacheLimit()")
        protected Object evalCached(Object self, Object source, RubyBinding binding, Object file, int line,
                @Cached RubyStringLibrary libSource,
                @Cached RubyStringLibrary libFile,
                @Cached("asTruffleStringUncached(source)") TruffleString cachedSource,
                @Cached("libSource.getEncoding(source)") RubyEncoding cachedSourceEnc,
                @Cached("asTruffleStringUncached(file)") TruffleString cachedFile,
                @Cached("libFile.getEncoding(file)") RubyEncoding cachedFileEnc,
                @Cached("line") int cachedLine,
                @Cached("getBindingDescriptor(binding)") FrameDescriptor bindingDescriptor,
                @Cached("parse(cachedSource, cachedSourceEnc, binding.getFrame(), getJavaString(file), cachedLine)") RootCallTarget callTarget,
                @Cached("assignsNewUserVariables(getDescriptor(callTarget))") boolean assignsNewUserVariables,
                @Cached("create(callTarget)") DirectCallNode callNode,
                @Cached StringHelperNodes.EqualSameEncodingNode codeEqualNode,
                @Cached StringHelperNodes.EqualNode fileEqualNode) {
            Object[] rubyArgs = prepareEvalArgs(callTarget, assignsNewUserVariables, self, binding);
            return callNode.call(rubyArgs);
        }

        @Specialization(
                guards = { "libSource.isRubyString(source)", "libFile.isRubyString(file)" },
                replaces = "evalCached", limit = "1")
        protected Object evalBindingUncached(Object self, Object source, RubyBinding binding, Object file, int line,
                @Cached IndirectCallNode callNode,
                @Cached RubyStringLibrary libFile,
                @Cached RubyStringLibrary libSource,
                @Cached ToJavaStringNode toJavaStringNode) {

            var callTarget = parse(libSource.getTString(source), libSource.getEncoding(source), binding.getFrame(),
                    toJavaStringNode.executeToJavaString(file), line);
            boolean assignsNewUserVariables = assignsNewUserVariables(getDescriptor(callTarget));

            Object[] rubyArgs = prepareEvalArgs(callTarget, assignsNewUserVariables, self, binding);
            return callNode.call(callTarget, rubyArgs);
        }

        private Object[] prepareEvalArgs(RootCallTarget callTarget, boolean assignsNewUserVariables, Object self,
                RubyBinding binding) {
            final MaterializedFrame parentFrame = Objects.requireNonNull(binding.getFrame());

            Object[] args = assignsNewUserVariables ? new Object[]{ binding } : RubyNode.EMPTY_ARGUMENTS;

            return getContext().getCodeLoader().prepareArgs(callTarget,
                    ParserContext.EVAL,
                    RubyArguments.getDeclarationContext(parentFrame),
                    parentFrame,
                    self,
                    RubyArguments.getMethod(parentFrame).getLexicalScope(),
                    args);
        }

        @TruffleBoundary
        protected RootCallTarget parse(AbstractTruffleString sourceText, RubyEncoding encoding,
                MaterializedFrame parentFrame, String file, int line) {
            //intern() to improve footprint
            final String sourceFile = file.intern();
            final RubySource source = EvalLoader.createEvalSource(getContext(), sourceText, encoding, "eval",
                    sourceFile, line, this);
            final LexicalScope lexicalScope = RubyArguments.getMethod(parentFrame).getLexicalScope();
            return getContext()
                    .getCodeLoader()
                    .parse(source, ParserContext.EVAL, parentFrame, lexicalScope, this);
        }

        protected FrameDescriptor getBindingDescriptor(RubyBinding binding) {
            return BindingNodes.getFrameDescriptor(binding);
        }

        protected FrameDescriptor getDescriptor(RootCallTarget callTarget) {
            return RubyRootNode.of(callTarget).getFrameDescriptor();
        }

        public static boolean assignsNewUserVariables(FrameDescriptor descriptor) {
            return BindingNodes.assignsNewUserVariables(descriptor);
        }

        protected int getCacheLimit() {
            return getLanguage().options.EVAL_CACHE;
        }
    }

    @CoreMethod(names = "freeze")
    public abstract static class KernelFreezeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getRubyLibraryCacheLimit()", guards = "!isRubyDynamicObject(self)")
        protected Object freeze(Object self,
                @CachedLibrary("self") RubyLibrary rubyLibrary) {
            rubyLibrary.freeze(self);
            return self;
        }

        @Specialization(limit = "getRubyLibraryCacheLimit()", guards = "isRubyDynamicObject(self)")
        protected Object freezeDynamicObject(Object self,
                @CachedLibrary("self") RubyLibrary rubyLibrary,
                @CachedLibrary(limit = "1") RubyLibrary rubyLibraryMetaClass,
                @Cached ConditionProfile singletonClassUnfrozenProfile,
                @Cached MetaClassNode metaClassNode) {
            final RubyClass metaClass = metaClassNode.execute(self);
            if (singletonClassUnfrozenProfile.profile(metaClass.isSingleton &&
                    !(RubyGuards.isRubyClass(self) && ((RubyClass) self).isSingleton) &&
                    !rubyLibraryMetaClass.isFrozen(metaClass))) {
                rubyLibraryMetaClass.freeze(metaClass);
            }
            rubyLibrary.freeze(self);
            return self;
        }

    }

    @GenerateUncached
    @CoreMethod(names = "frozen?", alwaysInlined = true)
    public abstract static class KernelFrozenNode extends AlwaysInlinedMethodNode {
        @Specialization(limit = "getRubyLibraryCacheLimit()")
        protected boolean isFrozen(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @CachedLibrary("self") RubyLibrary rubyLibrary) {
            return rubyLibrary.isFrozen(self);
        }
    }

    /** Keep consistent with {@link org.truffleruby.core.hash.HashingNodes.ToHashByHashCode} */
    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        public static HashNode create() {
            return KernelNodesFactory.HashNodeFactory.create(null);
        }

        public abstract Object execute(Object value);

        @Specialization
        protected long hashBoolean(boolean value) {
            return HashOperations.hashBoolean(value, getContext(), this);
        }

        @Specialization
        protected long hashInt(int value) {
            return HashOperations.hashLong(value, getContext(), this);
        }

        @Specialization
        protected long hashLong(long value) {
            return HashOperations.hashLong(value, getContext(), this);
        }

        @Specialization
        protected long hashDouble(double value) {
            return HashOperations.hashDouble(value, getContext(), this);
        }

        @Specialization
        protected long hashBignum(RubyBignum value) {
            return HashOperations.hashBignum(value, getContext(), this);
        }

        @Specialization
        protected long hashString(RubyString value,
                @Cached StringHelperNodes.HashStringNode stringHashNode) {
            return stringHashNode.execute(value);
        }

        @Specialization
        protected long hashImmutableString(ImmutableRubyString value,
                @Cached StringHelperNodes.HashStringNode stringHashNode) {
            return stringHashNode.execute(value);
        }

        @Specialization
        protected long hashSymbol(RubySymbol value,
                @Cached SymbolNodes.HashSymbolNode symbolHashNode) {
            return symbolHashNode.execute(value);
        }

        // Default hash for Kernel#hash, can be overwritten by defining a #hash method

        @Specialization(guards = { "!isRubyBignum(value)", "!isImmutableRubyString(value)", "!isRubySymbol(value)" })
        protected int hashImmutableRubyObject(ImmutableRubyObject value) {
            return System.identityHashCode(value);
        }

        @Specialization(guards = "isNotRubyString(value)")
        protected int hashRubyDynamicObject(RubyDynamicObject value) {
            return System.identityHashCode(value);
        }

        @Specialization(guards = "isForeignObject(value)", limit = "getInteropCacheLimit()")
        protected int hashForeign(Object value,
                @CachedLibrary("value") InteropLibrary interop,
                @Cached TranslateInteropExceptionNode translateInteropException) {
            if (interop.hasIdentity(value)) {
                try {
                    return interop.identityHashCode(value);
                } catch (UnsupportedMessageException e) {
                    throw translateInteropException.execute(e);
                }
            } else {
                return System.identityHashCode(value);
            }
        }
    }

    @ImportStatic(RubyArguments.class)
    @GenerateUncached
    @CoreMethod(names = "initialize_copy", required = 1, alwaysInlined = true)
    public abstract static class InitializeCopyNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object initializeCopy(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached ReferenceEqualNode equalNode,
                @Cached ConditionProfile sameProfile,
                @Cached CheckFrozenNode checkFrozenNode,
                @Cached LogicalClassNode lhsClassNode,
                @Cached LogicalClassNode rhsClassNode,
                @Cached BranchProfile errorProfile) {
            Object from = RubyArguments.getArgument(rubyArgs, 0);

            // GR-36575: should be separate specialization but Truffle does not support @Shared("equalNode") for this node
            if (sameProfile.profile(equalNode.executeReferenceEqual(self, from))) {
                return self;
            }

            checkFrozenNode.execute(self);

            if (lhsClassNode.execute(self) != rhsClassNode.execute(from)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeError("initialize_copy should take same class object", this));
            }

            return self;
        }
    }

    @GenerateUncached
    @CoreMethod(names = "initialize_dup", required = 1, alwaysInlined = true)
    public abstract static class InitializeDupNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object initializeDup(
                Frame callerFrame, RubyDynamicObject self, Object[] rubyArgs, RootCallTarget target,
                @Cached DispatchNode initializeCopyNode) {
            Object from = RubyArguments.getArgument(rubyArgs, 0);
            return initializeCopyNode.call(self, "initialize_copy", from);
        }
    }

    @CoreMethod(names = "instance_of?", required = 1)
    public abstract static class InstanceOfNode extends CoreMethodArrayArgumentsNode {

        @Child private LogicalClassNode classNode = LogicalClassNode.create();

        @Specialization
        protected boolean instanceOf(Object self, RubyModule module) {
            return classNode.execute(self) == module;
        }

    }

    @CoreMethod(names = "instance_variable_defined?", required = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class InstanceVariableDefinedNode extends CoreMethodNode {

        @Specialization
        protected boolean isInstanceVariableDefined(RubyDynamicObject object, Object name,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            return objectLibrary.containsKey(object, nameString);
        }

        @Fallback
        protected boolean immutable(Object object, Object name) {
            return false;
        }
    }

    @CoreMethod(names = "instance_variable_get", required = 1)
    public abstract static class InstanceVariableGetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object instanceVariableGetSymbol(RubyDynamicObject object, Object name,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @CachedLibrary(limit = "getDynamicObjectCacheLimit()") DynamicObjectLibrary objectLibrary,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            return objectLibrary.getOrDefault(object, nameString, nil);
        }

        @Fallback
        protected Object immutable(Object object, Object name,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            return nil;
        }
    }

    @CoreMethod(names = "instance_variable_set", required = 2)
    public abstract static class InstanceVariableSetNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object instanceVariableSet(RubyDynamicObject object, Object name, Object value,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @Cached WriteObjectFieldNode writeNode,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached TypeNodes.CheckFrozenNode raiseIfFrozenNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            raiseIfFrozenNode.execute(object);
            writeNode.execute(object, nameString, value);
            return value;
        }

        @Fallback
        protected Object immutable(Object object, Object name, Object value,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            throw new RaiseException(getContext(), coreExceptions().frozenError(object, this));
        }
    }

    @CoreMethod(names = "remove_instance_variable", required = 1)
    public abstract static class RemoveInstanceVariableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object removeInstanceVariable(RubyDynamicObject object, Object name,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @Cached NameToJavaStringNode nameToJavaStringNode,
                @Cached TypeNodes.CheckFrozenNode raiseIfFrozenNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            raiseIfFrozenNode.execute(object);
            return removeIVar(object, nameString);
        }

        @Fallback
        protected Object immutable(Object object, Object name,
                @Cached CheckIVarNameNode checkIVarNameNode,
                @Cached NameToJavaStringNode nameToJavaStringNode) {
            final String nameString = nameToJavaStringNode.execute(name);
            checkIVarNameNode.execute(object, nameString, name);
            throw new RaiseException(getContext(), coreExceptions().frozenError(object, this));
        }

        @TruffleBoundary
        private Object removeIVar(RubyDynamicObject object, String name) {
            final Object value = DynamicObjectLibrary.getUncached().getOrDefault(object, name, nil);

            if (SharedObjects.isShared(object)) {
                synchronized (object) {
                    removeField(object, name);
                }
            } else {
                removeField(object, name);
            }
            return value;
        }

        private void removeField(RubyDynamicObject object, String name) {
            if (!DynamicObjectLibrary.getUncached().removeKey(object, name)) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorInstanceVariableNotDefined(name, object, this));
            }
        }
    }

    @CoreMethod(names = "instance_variables")
    public abstract static class InstanceVariablesNode extends CoreMethodArrayArgumentsNode {

        @Child private ObjectInstanceVariablesNode instanceVariablesNode = ObjectInstanceVariablesNodeFactory
                .create(null);

        @Specialization
        protected RubyArray instanceVariables(Object self) {
            return instanceVariablesNode.executeGetIVars(self);
        }

    }

    @Primitive(name = "any_instance_variable?")
    public abstract static class AnyInstanceVariableNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected boolean any(RubyDynamicObject self,
                @CachedLibrary("self") DynamicObjectLibrary objectLibrary) {
            Object[] keys = objectLibrary.getKeyArray(self);

            for (Object key : keys) {
                if (key instanceof String) {
                    return true;
                }
            }

            return false;
        }

        @Specialization(guards = "!isRubyDynamicObject(self)")
        protected boolean noVariablesInImmutableObject(Object self) {
            return false;
        }
    }

    @CoreMethod(names = { "is_a?", "kind_of?" }, required = 1)
    public abstract static class KernelIsANode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isA(Object self, RubyModule module,
                @Cached IsANode isANode) {
            return isANode.executeIsA(self, module);
        }

        @Specialization(guards = "!isRubyModule(module)")
        protected boolean isATypeError(Object self, Object module) {
            throw new RaiseException(getContext(), coreExceptions().typeError("class or module required", this));
        }

    }

    @CoreMethod(names = "lambda", isModuleFunction = true, needsBlock = true)
    public abstract static class LambdaNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyProc lambda(Nil block) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorProcWithoutBlock(this));
        }

        @Specialization(guards = { "isLiteralBlock(block)", "block.isLambda()" })
        protected RubyProc lambdaFromLambdaBlock(RubyProc block) {
            return block;
        }

        @Specialization(guards = { "isLiteralBlock(block)", "block.isProc()" })
        protected RubyProc lambdaFromProcBlock(RubyProc block) {
            return ProcOperations.createLambdaFromBlock(getContext(), getLanguage(), block);
        }

        @Specialization(guards = { "!isLiteralBlock(block)", "block.isProc()" })
        protected RubyProc lambdaFromExistingProc(RubyProc block,
                @Cached("new()") WarnNode warnNode) {
            if (warnNode.shouldWarnForDeprecation()) {
                warnNode.warningMessage(
                        getContext().getCallStack().getTopMostUserSourceSection(),
                        "lambda without a literal block is deprecated; use the proc without lambda instead");
            }

            // If the argument isn't a literal, its original behaviour (proc or lambda) is preserved.
            return block;
        }

        @Specialization(guards = { "!isLiteralBlock(block)", "block.isLambda()" })
        protected RubyProc lambdaFromExistingProc(RubyProc block) {
            // If the argument isn't a literal, its original behaviour (proc or lambda) is preserved.
            return block;
        }

        @TruffleBoundary
        protected boolean isLiteralBlock(RubyProc block) {
            Node callNode = getContext().getCallStack().getCallerNode();
            RubyCallNode rubyCallNode = NodeUtil.findParent(callNode, RubyCallNode.class);
            return rubyCallNode != null && rubyCallNode.hasLiteralBlock();
        }
    }

    @GenerateUncached
    @CoreMethod(names = "local_variables", isModuleFunction = true, alwaysInlined = true)
    public abstract static class KernelLocalVariablesNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object localVariables(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached DispatchNode callLocalVariables) {
            needCallerFrame(callerFrame, target);
            final RubyBinding binding = BindingNodes
                    .createBinding(getContext(), getLanguage(), callerFrame.materialize());
            return callLocalVariables.call(binding, "local_variables");
        }
    }

    @CoreMethod(names = "__method__", isModuleFunction = true)
    public abstract static class MethodNameNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySymbol methodName() {
            // the "original/definition name" of the method.
            InternalMethod internalMethod = getContext().getCallStack().getCallingMethod();
            return getSymbol(internalMethod.getSharedMethodInfo().getMethodNameForNotBlock());
        }

    }

    @GenerateUncached
    @CoreMethod(names = "method", required = 1, alwaysInlined = true)
    public abstract static class MethodNode extends AlwaysInlinedMethodNode {

        @Specialization
        protected RubyMethod method(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached ToStringOrSymbolNode toStringOrSymbolNode,
                @Cached GetMethodObjectNode getMethodObjectNode) {
            Object name = toStringOrSymbolNode.execute(RubyArguments.getArgument(rubyArgs, 0));
            return getMethodObjectNode.execute(callerFrame, self, name,
                    DispatchConfiguration.PRIVATE);
        }

    }

    @CoreMethod(names = "methods", optional = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "regular", type = RubyBaseNodeWithExecute.class)
    public abstract static class MethodsNode extends CoreMethodNode {

        @CreateCast("regular")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute regular) {
            return BooleanCastWithDefaultNode.create(true, regular);
        }

        @TruffleBoundary
        @Specialization(guards = "regular")
        protected RubyArray methodsRegular(Object self, boolean regular,
                @Cached MetaClassNode metaClassNode) {
            final RubyModule metaClass = metaClassNode.execute(self);

            Object[] objects = metaClass.fields
                    .filterMethodsOnObject(getLanguage(), regular, MethodFilter.PUBLIC_PROTECTED)
                    .toArray();
            return createArray(objects);
        }

        @Specialization(guards = "!regular")
        protected RubyArray methodsSingleton(VirtualFrame frame, Object self, boolean regular,
                @Cached SingletonMethodsNode singletonMethodsNode) {
            return singletonMethodsNode.executeSingletonMethods(frame, self, false);
        }

    }

    @CoreMethod(names = "nil?", needsSelf = false)
    public abstract static class IsNilNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isNil() {
            return false;
        }
    }

    // A basic Kernel#p for debugging core, overridden later in kernel.rb
    @NonStandard
    @CoreMethod(names = "p", isModuleFunction = true, required = 1)
    public abstract static class DebugPrintNode extends CoreMethodArrayArgumentsNode {

        @Child private DispatchNode callInspectNode = DispatchNode.create();

        @Specialization
        protected Object p(VirtualFrame frame, Object value) {
            Object inspected = callInspectNode.call(value, "inspect");
            print(inspected);
            return value;
        }

        @TruffleBoundary
        private void print(Object inspected) {
            getContext().getEnvOutStream().println(inspected.toString());
        }
    }

    @CoreMethod(names = "private_methods", optional = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyBaseNodeWithExecute.class)
    public abstract static class PrivateMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("includeAncestors")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute includeAncestors) {
            return BooleanCastWithDefaultNode.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray privateMethods(Object self, boolean includeAncestors) {
            RubyClass metaClass = metaClassNode.execute(self);

            Object[] objects = metaClass.fields
                    .filterMethodsOnObject(getLanguage(), includeAncestors, MethodFilter.PRIVATE)
                    .toArray();
            return createArray(objects);
        }

    }

    @CoreMethod(names = "proc", isModuleFunction = true, needsBlock = true)
    public abstract static class ProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyProc proc(VirtualFrame frame, Object maybeBlock,
                @Cached ProcNewNode procNewNode) {
            return procNewNode.executeProcNew(frame, coreLibrary().procClass, ArrayUtils.EMPTY_ARRAY, maybeBlock);
        }

    }

    @CoreMethod(names = "protected_methods", optional = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyBaseNodeWithExecute.class)
    public abstract static class ProtectedMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("includeAncestors")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute includeAncestors) {
            return BooleanCastWithDefaultNode.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray protectedMethods(Object self, boolean includeAncestors) {
            final RubyClass metaClass = metaClassNode.execute(self);

            Object[] objects = metaClass.fields
                    .filterMethodsOnObject(getLanguage(), includeAncestors, MethodFilter.PROTECTED)
                    .toArray();
            return createArray(objects);
        }

    }

    @GenerateUncached
    @CoreMethod(names = "public_method", required = 1, alwaysInlined = true)
    public abstract static class PublicMethodNode extends AlwaysInlinedMethodNode {

        @Specialization
        protected RubyMethod method(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached ToStringOrSymbolNode toStringOrSymbolNode,
                @Cached GetMethodObjectNode getMethodObjectNode) {
            Object name = toStringOrSymbolNode.execute(RubyArguments.getArgument(rubyArgs, 0));
            return getMethodObjectNode.execute(callerFrame, self, name,
                    DispatchConfiguration.PUBLIC);
        }

    }

    @CoreMethod(names = "public_methods", optional = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyBaseNodeWithExecute.class)
    public abstract static class PublicMethodsNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("includeAncestors")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute includeAncestors) {
            return BooleanCastWithDefaultNode.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray publicMethods(Object self, boolean includeAncestors) {
            final RubyModule metaClass = metaClassNode.execute(self);

            Object[] objects = metaClass.fields
                    .filterMethodsOnObject(getLanguage(), includeAncestors, MethodFilter.PUBLIC)
                    .toArray();
            return createArray(objects);
        }

    }

    @GenerateUncached
    @CoreMethod(names = "public_send", needsBlock = true, required = 1, rest = true, alwaysInlined = true)
    public abstract static class PublicSendNode extends AlwaysInlinedMethodNode {

        @Specialization
        protected Object send(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached(parameters = "PUBLIC") DispatchNode dispatchNode,
                @Cached NameToJavaStringNode nameToJavaString) {
            Object name = RubyArguments.getArgument(rubyArgs, 0);
            Object[] newArgs = RubyArguments.repack(rubyArgs, self, 1);
            return dispatchNode.dispatch(callerFrame, self, nameToJavaString.execute(name), newArgs);
        }

    }

    @ImportStatic(DispatchConfiguration.class)
    @GenerateUncached
    @CoreMethod(names = "respond_to?", required = 1, optional = 1, alwaysInlined = true)
    public abstract static class RespondToNode extends AlwaysInlinedMethodNode {

        public final boolean executeDoesRespondTo(Object self, Object name, boolean includeProtectedAndPrivate) {
            final Object[] rubyArgs = RubyArguments.allocate(2);
            RubyArguments.setArgument(rubyArgs, 0, name);
            RubyArguments.setArgument(rubyArgs, 1, includeProtectedAndPrivate);
            return (boolean) execute(null, self, rubyArgs, null);
        }

        @Specialization
        protected boolean doesRespondTo(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target,
                @Cached BranchProfile notSymbolOrStringProfile,
                @Cached ToJavaStringNode toJavaString,
                @Cached ToSymbolNode toSymbolNode,
                @Cached BooleanCastNode castArgumentNode,
                @Cached ConditionProfile ignoreVisibilityProfile,
                @Cached ConditionProfile isTrueProfile,
                @Cached ConditionProfile respondToMissingProfile,
                @Cached(parameters = "PUBLIC") InternalRespondToNode dispatchPublic,
                @Cached InternalRespondToNode dispatchPrivate,
                @Cached InternalRespondToNode dispatchRespondToMissing,
                @Cached DispatchNode respondToMissingNode,
                @Cached BooleanCastNode castMissingResultNode) {
            final Object name = RubyArguments.getArgument(rubyArgs, 0);
            final int nArgs = RubyArguments.getPositionalArgumentsCount(rubyArgs, false);
            final boolean includeProtectedAndPrivate = nArgs >= 2 &&
                    castArgumentNode.execute(RubyArguments.getArgument(rubyArgs, 1));

            if (!RubyGuards.isRubySymbolOrString(name)) {
                notSymbolOrStringProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorIsNotAOrB(self, "symbol", "string", this));
            }

            final String methodName = toJavaString.executeToJavaString(name);
            final boolean found;
            if (ignoreVisibilityProfile.profile(includeProtectedAndPrivate)) {
                found = dispatchPrivate.execute(callerFrame, self, methodName);
            } else {
                found = dispatchPublic.execute(callerFrame, self, methodName);
            }

            if (isTrueProfile.profile(found)) {
                return true;
            } else if (respondToMissingProfile
                    .profile(dispatchRespondToMissing.execute(callerFrame, self, "respond_to_missing?"))) {
                return castMissingResultNode.execute(respondToMissingNode.call(self, "respond_to_missing?",
                        toSymbolNode.execute(name), includeProtectedAndPrivate));
            } else {
                return false;
            }
        }
    }

    @GenerateUncached
    @CoreMethod(names = "respond_to_missing?", required = 2, alwaysInlined = true)
    public abstract static class RespondToMissingNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected boolean respondToMissing(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target) {
            return false;
        }
    }

    @CoreMethod(names = "set_trace_func", isModuleFunction = true, required = 1)
    public abstract static class SetTraceFuncNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object setTraceFunc(Nil traceFunc) {
            getContext().getTraceManager().setTraceFunc(null);
            return nil;
        }

        @Specialization
        protected RubyProc setTraceFunc(RubyProc traceFunc) {
            getContext().getTraceManager().setTraceFunc(traceFunc);
            return traceFunc;
        }
    }

    @CoreMethod(names = "singleton_class")
    public abstract static class SingletonClassMethodNode extends CoreMethodArrayArgumentsNode {

        @Child private SingletonClassNode singletonClassNode = SingletonClassNode.create();

        public abstract RubyClass executeSingletonClass(Object self);

        @Specialization
        protected RubyClass singletonClass(Object self) {
            return singletonClassNode.executeSingletonClass(self);
        }

    }

    @CoreMethod(names = "singleton_method", required = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class SingletonMethodNode extends CoreMethodNode {

        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization
        protected RubyMethod singletonMethod(Object self, String name,
                @Cached BranchProfile errorProfile,
                @Cached ConditionProfile singletonProfile,
                @Cached ConditionProfile methodProfile) {
            final RubyClass metaClass = metaClassNode.execute(self);

            if (singletonProfile.profile(metaClass.isSingleton)) {
                final InternalMethod method = metaClass.fields.getMethod(name);
                if (methodProfile.profile(method != null && !method.isUndefined())) {
                    final RubyMethod instance = new RubyMethod(
                            coreLibrary().methodClass,
                            getLanguage().methodShape,
                            self,
                            method);
                    AllocationTracing.trace(instance, this);
                    return instance;
                }
            }

            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameErrorUndefinedSingletonMethod(name, self, this));
        }

    }

    @CoreMethod(names = "singleton_methods", optional = 1)
    @NodeChild(value = "object", type = RubyNode.class)
    @NodeChild(value = "includeAncestors", type = RubyBaseNodeWithExecute.class)
    public abstract static class SingletonMethodsNode extends CoreMethodNode {

        public static SingletonMethodsNode create() {
            return SingletonMethodsNodeFactory.create(null, null);
        }

        public abstract RubyArray executeSingletonMethods(VirtualFrame frame, Object self, boolean includeAncestors);

        @CreateCast("includeAncestors")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute includeAncestors) {
            return BooleanCastWithDefaultNode.create(true, includeAncestors);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray singletonMethods(Object self, boolean includeAncestors,
                @Cached MetaClassNode metaClassNode) {
            final RubyClass metaClass = metaClassNode.execute(self);

            if (!metaClass.isSingleton) {
                return createEmptyArray();
            }

            Object[] objects = metaClass.fields
                    .filterSingletonMethods(getLanguage(), includeAncestors, MethodFilter.PUBLIC_PROTECTED)
                    .toArray();
            return createArray(objects);
        }

    }

    @Primitive(name = "singleton_methods?")
    public abstract static class HasSingletonMethodsNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean hasSingletonMethods(Object self,
                @Cached MetaClassNode metaClassNode) {
            final RubyClass metaClass = metaClassNode.execute(self);

            if (!metaClass.isSingleton) {
                return false;
            }

            return metaClass.fields.anyMethodDefined();
        }

    }

    @NodeChild(value = "duration", type = RubyBaseNodeWithExecute.class)
    @CoreMethod(names = "sleep", isModuleFunction = true, optional = 1)
    public abstract static class SleepNode extends CoreMethodNode {

        @CreateCast("duration")
        protected RubyBaseNodeWithExecute coerceDuration(RubyBaseNodeWithExecute duration) {
            return DurationToNanoSecondsNodeGen.create(false, duration);
        }

        @Specialization
        protected long sleep(long durationInNanos,
                @Cached BranchProfile errorProfile) {
            if (durationInNanos < 0) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("time interval must be positive", this));
            }

            final RubyThread thread = getLanguage().getCurrentThread();

            // Clear the wakeUp flag, following Ruby semantics:
            // it should only be considered if we are inside the sleep when Thread#{run,wakeup} is called.
            thread.wakeUp.set(false);

            return sleepFor(getContext(), thread, durationInNanos, this);
        }

        @TruffleBoundary
        public static long sleepFor(RubyContext context, RubyThread thread, long durationInNanos,
                Node currentNode) {
            assert durationInNanos >= 0;

            // We want a monotonic clock to measure sleep duration
            final long startInNanos = System.nanoTime();

            context.getThreadManager().runUntilResult(currentNode, () -> {
                final long nowInNanos = System.nanoTime();
                final long sleptInNanos = nowInNanos - startInNanos;

                if (sleptInNanos >= durationInNanos || thread.wakeUp.getAndSet(false)) {
                    return BlockingAction.SUCCESS;
                }

                TimeUnit.NANOSECONDS.sleep(durationInNanos - sleptInNanos);
                return BlockingAction.SUCCESS;
            });

            return TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startInNanos);
        }

    }

    @CoreMethod(names = { "format", "sprintf" }, isModuleFunction = true, rest = true, required = 1)
    @ReportPolymorphism
    @NodeChild(value = "format", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "arguments", type = RubyBaseNodeWithExecute.class)
    public abstract static class SprintfNode extends CoreMethodNode {

        @Child private TruffleString.FromByteArrayNode fromByteArrayNode;
        @Child private BooleanCastNode readDebugGlobalNode = BooleanCastNodeGen
                .create(ReadGlobalVariableNodeGen.create("$DEBUG"));

        private final BranchProfile exceptionProfile = BranchProfile.create();
        private final ConditionProfile resizeProfile = ConditionProfile.create();

        @CreateCast("format")
        protected ToStrNode coerceFormatToString(RubyBaseNodeWithExecute format) {
            return ToStrNodeGen.create(format);
        }

        @Specialization(
                guards = {
                        "libFormat.isRubyString(format)",
                        "equalNode.execute(libFormat, format, cachedTString, cachedEncoding)",
                        "isDebug(frame) == cachedIsDebug" },
                limit = "3")
        protected RubyString formatCached(VirtualFrame frame, Object format, Object[] arguments,
                @Cached RubyStringLibrary libFormat,
                @Cached("isDebug(frame)") boolean cachedIsDebug,
                @Cached("asTruffleStringUncached(format)") TruffleString cachedTString,
                @Cached("libFormat.getEncoding(format)") RubyEncoding cachedEncoding,
                @Cached("cachedTString.byteLength(cachedEncoding.tencoding)") int cachedFormatLength,
                @Cached("create(compileFormat(cachedTString, cachedEncoding, arguments, isDebug(frame)))") DirectCallNode callPackNode,
                @Cached StringHelperNodes.EqualSameEncodingNode equalNode) {
            final BytesResult result;
            try {
                result = (BytesResult) callPackNode.call(
                        new Object[]{ arguments, arguments.length, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishFormat(cachedFormatLength, result);
        }

        @Specialization(
                guards = "libFormat.isRubyString(format)",
                replaces = "formatCached", limit = "1")
        protected RubyString formatUncached(VirtualFrame frame, Object format, Object[] arguments,
                @Cached IndirectCallNode callPackNode,
                @Cached RubyStringLibrary libFormat) {
            final BytesResult result;
            final boolean isDebug = readDebugGlobalNode.execute(frame);
            var tstring = libFormat.getTString(format);
            var encoding = libFormat.getEncoding(format);
            try {
                result = (BytesResult) callPackNode.call(
                        compileFormat(tstring, encoding, arguments, isDebug),
                        new Object[]{ arguments, arguments.length, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishFormat(tstring.byteLength(encoding.tencoding), result);
        }

        private RubyString finishFormat(int formatLength, BytesResult result) {
            byte[] bytes = result.getOutput();

            if (resizeProfile.profile(bytes.length != result.getOutputLength())) {
                bytes = Arrays.copyOf(bytes, result.getOutputLength());
            }

            if (fromByteArrayNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fromByteArrayNode = insert(TruffleString.FromByteArrayNode.create());
            }

            return createString(fromByteArrayNode, bytes, result.getEncoding().getEncodingForLength(formatLength));
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(AbstractTruffleString tstring, RubyEncoding encoding, Object[] arguments,
                boolean isDebug) {
            try {
                return new PrintfCompiler(getLanguage(), this)
                        .compile(tstring, encoding, arguments, isDebug);
            } catch (InvalidFormatException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }

        protected boolean isDebug(VirtualFrame frame) {
            return readDebugGlobalNode.execute(frame);
        }

    }

    @CoreMethod(names = "global_variables", isModuleFunction = true)
    public abstract static class KernelGlobalVariablesNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray globalVariables() {
            final String[] keys = coreLibrary().globalVariables.keys();
            final Object[] store = new Object[keys.length];
            for (int i = 0; i < keys.length; i++) {
                store[i] = getSymbol(keys[i]);
            }
            return createArray(store);
        }

    }

    @CoreMethod(names = "taint")
    public abstract static class KernelTaintNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object taint(Object object,
                @Cached("new()") WarningNode warningNode) {
            if (warningNode.shouldWarn()) {
                warningNode.warningMessage(
                        getSourceSection(),
                        "Object#taint is deprecated and will be removed in Ruby 3.2.");
            }
            return object;
        }

    }

    @CoreMethod(names = "trust")
    public abstract static class KernelTrustNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object trust(Object object,
                @Cached("new()") WarningNode warningNode) {
            if (warningNode.shouldWarn()) {
                warningNode.warningMessage(
                        getSourceSection(),
                        "Object#trust is deprecated and will be removed in Ruby 3.2.");
            }
            return object;
        }

    }

    @CoreMethod(names = "tainted?")
    public abstract static class KernelIsTaintedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isTainted(Object object,
                @Cached("new()") WarningNode warningNode) {
            if (warningNode.shouldWarn()) {
                warningNode.warningMessage(
                        getSourceSection(),
                        "Object#tainted? is deprecated and will be removed in Ruby 3.2.");
            }
            return false;
        }

    }

    @CoreMethod(names = "untrusted?")
    public abstract static class KernelIsUntrustedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isUntrusted(Object object,
                @Cached("new()") WarningNode warningNode) {
            if (warningNode.shouldWarn()) {
                warningNode.warningMessage(
                        getSourceSection(),
                        "Object#untrusted? is deprecated and will be removed in Ruby 3.2.");
            }
            return false;
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "value", type = RubyNode.class)
    @Primitive(name = "kernel_to_hex")
    public abstract static class ToHexStringNode extends RubySourceNode {

        public static ToHexStringNode create() {
            return KernelNodesFactory.ToHexStringNodeFactory.create(null);
        }

        public static ToHexStringNode getUncached() {
            return KernelNodesFactory.ToHexStringNodeFactory.getUncached();
        }

        public abstract String executeToHexString(Object value);

        @Specialization
        protected String toHexString(int value) {
            return toHexString((long) value);
        }

        @TruffleBoundary
        @Specialization
        protected String toHexString(long value) {
            return Long.toHexString(value);
        }

        @Specialization
        protected String toHexString(RubyBignum value) {
            return BigIntegerOps.toString(value.value, 16);
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "self", type = RubyNode.class)
    @CoreMethod(names = { "to_s", "inspect" }) // Basic #inspect, refined later in core
    public abstract static class ToSNode extends RubySourceNode {

        public static ToSNode create() {
            return KernelNodesFactory.ToSNodeFactory.create(null);
        }

        public abstract RubyString executeToS(Object self);

        @Specialization
        protected RubyString toS(Object self,
                @Cached LogicalClassNode classNode,
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode,
                @Cached ObjectIDNode objectIDNode,
                @Cached ToHexStringNode toHexStringNode) {
            String className = classNode.execute(self).fields.getName();
            Object id = objectIDNode.execute(self);
            String hexID = toHexStringNode.executeToHexString(id);

            String javaString = Utils.concat("#<", className, ":0x", hexID, ">");

            return createString(
                    fromJavaStringNode,
                    javaString,
                    Encodings.UTF_8);
        }

        @TruffleBoundary
        public static String uncachedBasicToS(Object self) {
            String className = LogicalClassNode.getUncached().execute(self).fields.getName();
            Object id = ObjectIDNode.getUncached().execute(self);
            String hexID = ToHexStringNode.getUncached().executeToHexString(id);

            return "#<" + className + ":0x" + hexID + ">";
        }

        @TruffleBoundary
        public static String uncachedBasicToS(RubyDynamicObject self) {
            String className = self.getLogicalClass().fields.getName();
            long id = ObjectIDNode.getUncached().execute(self);
            String hexID = Long.toHexString(id);

            return "#<" + className + ":0x" + hexID + ">";
        }

    }

    @CoreMethod(names = "untaint")
    public abstract static class UntaintNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object untaint(Object object,
                @Cached("new()") WarningNode warningNode) {
            if (warningNode.shouldWarn()) {
                warningNode.warningMessage(
                        getSourceSection(),
                        "Object#untaint is deprecated and will be removed in Ruby 3.2.");
            }
            return object;
        }

    }

    @CoreMethod(names = "untrust")
    public abstract static class UntrustNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object untrust(Object object,
                @Cached("new()") WarningNode warningNode) {
            if (warningNode.shouldWarn()) {
                warningNode.warningMessage(
                        getSourceSection(),
                        "Object#untrust is deprecated and will be removed in Ruby 3.2.");
            }
            return object;
        }

    }

    @Primitive(name = "warning_get_category")
    public abstract static class WarningGetCategoryNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "category == coreSymbols().DEPRECATED")
        protected boolean getCategoryDeprecated(RubySymbol category) {
            return getContext().getWarningCategoryDeprecated().get();
        }

        @Specialization(guards = "category == coreSymbols().EXPERIMENTAL")
        protected boolean getCategoryExperimental(RubySymbol category) {
            return getContext().getWarningCategoryExperimental().get();
        }

    }

    @Primitive(name = "warning_set_category")
    public abstract static class WarningSetCategoryNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean setCategory(RubySymbol category, boolean newValue) {
            final AssumedValue<Boolean> existingValue;
            if (category == coreSymbols().DEPRECATED) {
                existingValue = getContext().getWarningCategoryDeprecated();
            } else if (category == coreSymbols().EXPERIMENTAL) {
                existingValue = getContext().getWarningCategoryExperimental();
            } else {
                throw CompilerDirectives.shouldNotReachHere("unexpected warning category");
            }

            if (existingValue.get() != newValue) {
                existingValue.set(newValue);
            }
            return newValue;
        }

    }

}
