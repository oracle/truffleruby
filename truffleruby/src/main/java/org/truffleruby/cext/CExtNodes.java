/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.cast.NameToJavaStringNodeGen;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsFrozenNodeGen;
import org.truffleruby.language.objects.MetaClassNode;

import java.util.HashMap;
import java.util.Map;

@CoreClass("Truffle::CExt")
public class CExtNodes {

    @CoreMethod(names = "NUM2INT", isModuleFunction = true, required = 1)
    public abstract static class NUM2INTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int num2int(int num) {
            return num;
        }

        @Specialization
        public int num2int(long num) {
            return (int) num;
        }

    }

    @CoreMethod(names = "NUM2UINT", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2UINTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int num2uint(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "NUM2LONG", isModuleFunction = true, required = 1)
    public abstract static class NUM2LONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long num2long(int num) {
            return num;
        }


        @Specialization
        public long num2long(long num) {
            return num;
        }
    }

    @CoreMethod(names = "NUM2ULONG", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2ULONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long num2ulong(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "NUM2DBL", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2DBLNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public double num2dbl(int num) {
            return num;
        }

    }

    @CoreMethod(names = "FIX2INT", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class FIX2INTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int fix2int(int num) {
            return num;
        }

    }

    @CoreMethod(names = "FIX2UINT", isModuleFunction = true, required = 1)
    public abstract static class FIX2UINTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int fix2uint(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

        @Specialization
        public long fix2uint(long num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "FIX2LONG", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class FIX2LONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long fix2long(int num) {
            return num;
        }

    }

    @CoreMethod(names = "INT2NUM", isModuleFunction = true, required = 1)
    public abstract static class INT2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int int2num(int num) {
            return num;
        }

        @Specialization
        public long int2num(long num) {
            return num;
        }

    }

    @CoreMethod(names = "INT2FIX", isModuleFunction = true, required = 1)
    public abstract static class INT2FIXNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int int2fix(int num) {
            return num;
        }

        @Specialization
        public long int2fix(long num) {
            return num;
        }

    }

    @CoreMethod(names = "UINT2NUM", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class UINT2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int uint2num(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "LONG2NUM", isModuleFunction = true, required = 1)
    public abstract static class LONG2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long long2num(long num) {
            return num;
        }

    }

    @CoreMethod(names = "ULONG2NUM", isModuleFunction = true, required = 1)
    public abstract static class ULONG2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long ulong2num(long num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "LONG2FIX", isModuleFunction = true, required = 1)
    public abstract static class LONG2FIXNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long long2fix(long num) {
            return num;
        }

    }

    @CoreMethod(names = "CLASS_OF", isModuleFunction = true, required = 1)
    public abstract static class CLASSOFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject class_of(DynamicObject object,
                                      @Cached("create()") MetaClassNode metaClassNode) {
            return metaClassNode.executeMetaClass(object);
        }

    }

    @CoreMethod(names = "rb_long2int", isModuleFunction = true, required = 1)
    public abstract static class Long2Int extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int long2fix(int num) {
            return num;
        }

        @Specialization(guards = "fitsIntoInteger(num)")
        public int long2fixInRange(long num) {
            return (int) num;
        }

        @Specialization(guards = "!fitsIntoInteger(num)")
        public int long2fixOutOfRange(long num) {
            throw new RaiseException(coreExceptions().rangeErrorConvertToInt(num, this));
        }

        protected boolean fitsIntoInteger(long num) {
            return CoreLibrary.fitsIntoInteger(num);
        }

    }

    @CoreMethod(names = "RSTRING_PTR", isModuleFunction = true, required = 1)
    public abstract static class StringPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public StringCharPointerAdapter stringPointer(DynamicObject string) {
            return new StringCharPointerAdapter(string);
        }

    }

    @CoreMethod(names = "to_ruby_string", isModuleFunction = true, required = 1)
    public abstract static class ToRubyStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject toRubyString(StringCharPointerAdapter stringCharPointerAdapter) {
            return stringCharPointerAdapter.getString();
        }

        @Specialization(guards = "isRubyString(string)")
        public DynamicObject toRubyString(DynamicObject string) {
            return string;
        }

    }

    @CoreMethod(names = "rb_block_given_p", isModuleFunction = true, needsCallerFrame = true)
    public abstract static class BlockGivenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int blockGiven(MaterializedFrame callerFrame,
                                  @Cached("createBinaryProfile()") ConditionProfile blockProfile) {
            return blockProfile.profile(RubyArguments.tryGetBlock(callerFrame) != null) ? 1 : 0;
        }

        @TruffleBoundary
        @Specialization
        public int blockGiven(NotProvided noCallerFrame) {
            return RubyArguments.tryGetBlock(Truffle.getRuntime().getCallerFrame().getFrame(FrameInstance.FrameAccess.READ_ONLY)) != null ? 1 : 0;
        }

    }

    @CoreMethod(names = "rb_block_proc", isModuleFunction = true, needsCallerFrame = true)
    public abstract static class BlockProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject blockProc(MaterializedFrame callerFrame) {
            return RubyArguments.tryGetBlock(callerFrame);
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject blockProc(NotProvided noCallerFrame) {
            return RubyArguments.tryGetBlock(Truffle.getRuntime().getCallerFrame().getFrame(FrameInstance.FrameAccess.READ_ONLY));
        }

    }

    @CoreMethod(names = "rb_check_frozen", isModuleFunction = true, required = 1)
    public abstract static class CheckFrozenNode extends CoreMethodArrayArgumentsNode {

        @Child private IsFrozenNode isFrozenNode = IsFrozenNodeGen.create(null);

        @Specialization
        public boolean rb_check_frozen(Object object) {
            isFrozenNode.raiseIfFrozen(object);
            return true;
        }

    }

    @CoreMethod(names = "get_block", isModuleFunction = true)
    public abstract static class GetBlockNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject getBlock() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);
                return RubyArguments.tryGetBlock(frame);
            });
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    @CoreMethod(names = "rb_const_get_from", isModuleFunction = true, required = 2)
    public abstract static class ConstGetFromNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, false);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @Specialization
        public Object constGetFrom(VirtualFrame frame, DynamicObject module, String name) {
            final RubyConstant constant = lookupConstantNode.lookupConstant(frame, module, name);
            return getConstantNode.executeGetConstant(frame, module, name, constant, lookupConstantNode);
        }

    }

    @CoreMethod(names = "rb_tr_io_handle", isModuleFunction = true, required = 1)
    public abstract static class IOHandleNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyIO(io)")
        public int ioHandle(DynamicObject io) {
            return Layouts.IO.getDescriptor(io);
        }

    }

    @CoreMethod(names = "cext_module_function", isModuleFunction = true, required = 2)
    public abstract static class CextModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child
        ModuleNodes.SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION, null, null);

        @Specialization(guards = {"isRubyModule(module)", "isRubySymbol(name)"})
        public DynamicObject cextModuleFunction(VirtualFrame frame, DynamicObject module, DynamicObject name) {
            return setVisibilityNode.executeSetVisibility(frame, module, new Object[]{name});
        }

    }

    @CoreMethod(names = "caller_frame_visibility", isModuleFunction = true, required = 1)
    public abstract static class CallerFrameVisibilityNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(visibility)")
        public boolean toRubyString(DynamicObject visibility) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.MATERIALIZE);
            final Visibility callerVisibility = DeclarationContext.findVisibility(callerFrame);

            switch (visibility.toString()) {
                case "private":
                    return callerVisibility.isPrivate();
                case "protected":
                    return callerVisibility.isProtected();
                case "module_function":
                    return callerVisibility.isModuleFunction();
                default:
                    throw new UnsupportedOperationException();
            }
        }

    }

    @CoreMethod(names = "rb_tr_adapt_rdata", isModuleFunction = true, required = 1)
    public abstract static class AdaptRDataNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object adaptRData(DynamicObject object) {
            return new DataAdapter(object);
        }

    }

    protected static final Object handlesLock = new Object();
    protected static final Map<DynamicObject, Long> toNative = new HashMap<>();
    protected static final Map<Long, DynamicObject> toManaged = new HashMap<>();

    @CoreMethod(names = "rb_tr_to_native_handle", isModuleFunction = true, required = 1)
    public abstract static class ToNativeHandleNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long toNativeHandle(DynamicObject object) {
            synchronized (handlesLock) {
                return toNative.computeIfAbsent(object, (k) -> {
                    final long handle = getContext().getNativePlatform().getMallocFree().malloc(Long.BYTES);
                    memoryManager().newPointer(handle).putLong(0, 0xdeadbeef);
                    Log.LOGGER.info(String.format("native handle 0x%x -> %s", handle, object));
                    toManaged.put(handle, object);
                    return handle;
                });
            }
        }

    }

    @CoreMethod(names = "rb_tr_from_native_handle", isModuleFunction = true, required = 1)
    public abstract static class FromNativeHandleNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject fromNativeHandle(long handle) {
            synchronized (handlesLock) {
                final DynamicObject object = toManaged.get(handle);

                if (object == null) {
                    throw new UnsupportedOperationException();
                }

                return object;
            }
        }

    }

    @CoreMethod(names = "rb_iter_break", isModuleFunction = true)
    public abstract static class IterBreakNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object iterBreak() {
            throw new BreakException(BreakID.ANY, nil());
        }

    }

}
