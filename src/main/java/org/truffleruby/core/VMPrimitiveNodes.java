/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 * Some of the code in this class is transliterated from C++ code in Rubinius.
 *
 * Copyright (c) 2007-2014, Evan Phoenix and contributors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of Rubinius nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.truffleruby.core;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ThrowException;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.LookupMethodNode;
import org.truffleruby.language.methods.LookupMethodNodeGen;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.yield.YieldNode;
import org.truffleruby.platform.sunmisc.SunMiscSignalManager;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@CoreClass(value = "VM primitives")
public abstract class VMPrimitiveNodes {

    @Primitive(name = "vm_catch", needsSelf = false)
    public abstract static class CatchNode extends PrimitiveArrayArgumentsNode {

        @Child private YieldNode dispatchNode = new YieldNode();

        @Specialization
        public Object doCatch(VirtualFrame frame, Object tag, DynamicObject block,
                @Cached("create()") BranchProfile catchProfile,
                @Cached("createBinaryProfile()") ConditionProfile matchProfile,
                @Cached("create()") ReferenceEqualNode referenceEqualNode) {
            try {
                return dispatchNode.dispatch(block, tag);
            } catch (ThrowException e) {
                catchProfile.enter();
                if (matchProfile.profile(referenceEqualNode.executeReferenceEqual(e.getTag(), tag))) {
                    return e.getValue();
                } else {
                    throw e;
                }
            }
        }
    }

    @Primitive(name = "vm_gc_start", needsSelf = false)
    public static abstract class VMGCStartPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject vmGCStart() {
            System.gc();
            return nil();
        }

    }

    // The hard #exit!
    @Primitive(name = "vm_exit", needsSelf = false, lowerFixnum = 1)
    public static abstract class VMExitPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object vmExit(int status) {
            throw new ExitException(status);
        }

        @Fallback
        public Object vmExit(Object status) {
            return FAILURE;
        }

    }

    @Primitive(name = "vm_extended_modules", needsSelf = false)
    public static abstract class VMExtendedModulesNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object vmExtendedModules(Object object, DynamicObject block,
                @Cached("create()") MetaClassNode metaClassNode,
                @Cached("new()") YieldNode yieldNode,
                @Cached("createBinaryProfile()") ConditionProfile isSingletonProfile) {
            final DynamicObject metaClass = metaClassNode.executeMetaClass(object);

            if (isSingletonProfile.profile(Layouts.CLASS.getIsSingleton(metaClass))) {
                for (DynamicObject included : Layouts.MODULE.getFields(metaClass).prependedAndIncludedModules()) {
                    yieldNode.dispatch(block, included);
                }
            }

            return nil();
        }

    }

    @Primitive(name = "vm_method_is_basic", needsSelf = false)
    public static abstract class VMMethodIsBasicNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean vmMethodIsBasic(VirtualFrame frame, DynamicObject method) {
            return Layouts.METHOD.getMethod(method).isBuiltIn();
        }

    }

    @Primitive(name = "vm_method_lookup", needsSelf = false)
    public static abstract class VMMethodLookupNode extends PrimitiveArrayArgumentsNode {

        @Child private NameToJavaStringNode nameToJavaStringNode;
        @Child private LookupMethodNode lookupMethodNode;

        public VMMethodLookupNode() {
            nameToJavaStringNode = NameToJavaStringNode.create();
            lookupMethodNode = LookupMethodNodeGen.create(true, false, null, null);
        }

        @Specialization
        public DynamicObject vmMethodLookup(VirtualFrame frame, Object self, Object name) {
            // TODO BJF Sep 14, 2016 Handle private
            final String normalizedName = nameToJavaStringNode.executeToJavaString(frame, name);
            InternalMethod method = lookupMethodNode.executeLookupMethod(frame, self, normalizedName);
            if (method == null) {
                return nil();
            }
            return Layouts.METHOD.createMethod(coreLibrary().getMethodFactory(), self, method);
        }

    }

    @Primitive(name = "vm_object_respond_to", needsSelf = false)
    public static abstract class VMObjectRespondToPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private KernelNodes.RespondToNode respondToNode = KernelNodesFactory.RespondToNodeFactory.create(null, null, null);

        @Specialization
        public boolean vmObjectRespondTo(VirtualFrame frame, Object object, Object name, boolean includePrivate) {
            return respondToNode.executeDoesRespondTo(frame, object, name, includePrivate);
        }

    }


    @Primitive(name = "vm_object_singleton_class", needsSelf = false)
    public static abstract class VMObjectSingletonClassPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private KernelNodes.SingletonClassMethodNode singletonClassNode = KernelNodesFactory.SingletonClassMethodNodeFactory.create(null);

        @Specialization
        public Object vmObjectClass(Object object) {
            return singletonClassNode.singletonClass(object);
        }

    }

    @Primitive(name = "vm_raise_exception", needsSelf = false)
    public static abstract class VMRaiseExceptionPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyException(exception)")
        public DynamicObject vmRaiseException(DynamicObject exception) {
            throw new RaiseException(exception);
        }
    }

    @Primitive(name = "vm_set_module_name", needsSelf = false)
    public static abstract class VMSetModuleNamePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object vmSetModuleName(Object object) {
            throw new UnsupportedOperationException("vm_set_module_name");
        }

    }

    @Primitive(name = "vm_throw", needsSelf = false)
    public abstract static class ThrowNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object doThrow(Object tag, Object value) {
            throw new ThrowException(tag, value);
        }

    }

    @Primitive(name = "vm_time", needsSelf = false)
    public abstract static class TimeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public long time() {
            return System.currentTimeMillis() / 1000;
        }

    }

    @Primitive(name = "vm_watch_signal", needsSelf = false)
    public static abstract class VMWatchSignalPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRubyString(signalName)", "isRubyString(action)" })
        public boolean watchSignal(DynamicObject signalName, DynamicObject action) {
            if (!StringOperations.getString(action).equals("DEFAULT")) {
                throw new UnsupportedOperationException();
            }

            return handleDefault(signalName);
        }

        @Specialization(guards = { "isRubyString(signalName)", "isNil(nil)" })
        public boolean watchSignal(DynamicObject signalName, Object nil) {
            return handle(signalName, (signal) -> {});
        }

        @Specialization(guards = { "isRubyString(signalName)", "isRubyProc(proc)" })
        public boolean watchSignalProc(DynamicObject signalName, DynamicObject proc) {
            if (getContext().getThreadManager().getCurrentThread() != getContext().getThreadManager().getRootThread()) {
                // The proc will be executed on the main thread
                SharedObjects.writeBarrier(getContext(), proc);
            }

            final RubyContext context = getContext();

            return handle(signalName, signal -> {
                final DynamicObject rootThread = context.getThreadManager().getRootThread();
                final FiberManager fiberManager = Layouts.THREAD.getFiberManager(rootThread);

                // Workaround: we need to use a "Truffle-created thread" so that NFI can get its context (GR-7014)
                final Thread thread = context.getEnv().createThread(() -> {
                    context.getSafepointManager().pauseAllThreadsAndExecuteFromNonRubyThread(true, (rubyThread, currentNode) -> {
                        if (rubyThread == rootThread &&
                                fiberManager.getRubyFiberFromCurrentJavaThread() == fiberManager.getCurrentFiber()) {
                            ProcOperations.rootCall(proc);
                        }
                    });
                });

                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    throw new UnsupportedOperationException("InterruptedException in sun.misc.Signal's Thread: " + Thread.currentThread());
                }
            });
        }

        @TruffleBoundary
        private boolean handleDefault(DynamicObject signalName) {
            Signal signal = new Signal(StringOperations.getString(signalName));
            try {
                final sun.misc.SignalHandler defaultHandler = SunMiscSignalManager.DEFAULT_HANDLERS.get(signal);
                if (defaultHandler != null) { // otherwise it is already the default signal
                    Signal.handle(signal, defaultHandler);
                }
            } catch (IllegalArgumentException e) {
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

        @TruffleBoundary
        private boolean handle(DynamicObject signalName, Consumer<Signal> newHandler) {
            Signal signal = new Signal(StringOperations.getString(signalName));
            try {
                final sun.misc.SignalHandler oldSunHandler = Signal.handle(
                        signal, wrappedSignal -> newHandler.accept(signal));

                SunMiscSignalManager.DEFAULT_HANDLERS.putIfAbsent(signal, oldSunHandler);
            } catch (IllegalArgumentException e) {
                throw new RaiseException(coreExceptions().argumentError(e.getMessage(), this));
            }
            return true;
        }

    }

    @Primitive(name = "vm_get_config_item", needsSelf = false)
    public abstract static class VMGetConfigItemPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(key)")
        public Object get(DynamicObject key) {
            final Object value = getContext().getNativePlatform().getRubiniusConfiguration().get(StringOperations.getString(key));

            if (value == null) {
                return nil();
            } else {
                return value;
            }
        }

    }

    @Primitive(name = "vm_get_config_section", needsSelf = false)
    public abstract static class VMGetConfigSectionPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private org.truffleruby.core.string.StringNodes.MakeStringNode makeStringNode = org.truffleruby.core.string.StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization(guards = "isRubyString(section)")
        public DynamicObject getSection(DynamicObject section) {
            final List<DynamicObject> sectionKeyValues = new ArrayList<>();

            for (String key : getContext().getNativePlatform().getRubiniusConfiguration().getSection(StringOperations.getString(section))) {
                Object value = getContext().getNativePlatform().getRubiniusConfiguration().get(key);
                final String stringValue;
                if (RubyGuards.isRubyBignum(value)) {
                    stringValue = Layouts.BIGNUM.getValue((DynamicObject) value).toString();
                } else if (RubyGuards.isRubyString(value)) {
                    stringValue = StringOperations.getString((DynamicObject) value);
                } else if (value instanceof Boolean || value instanceof Integer || value instanceof Long) {
                    stringValue = value.toString();
                } else {
                    throw new UnsupportedOperationException(value.getClass().toString());
                }

                Object[] objects = new Object[] {
                        makeStringNode.executeMake(key, UTF8Encoding.INSTANCE, CodeRange.CR_7BIT),
                        makeStringNode.executeMake(stringValue, UTF8Encoding.INSTANCE, CodeRange.CR_7BIT) };
                sectionKeyValues.add(createArray(objects, objects.length));
            }

            Object[] objects = sectionKeyValues.toArray();
            return createArray(objects, objects.length);
        }

    }

    @Primitive(name = "vm_set_class", needsSelf = false)
    public abstract static class VMSetClassPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyClass(newClass)")
        public DynamicObject setClass(DynamicObject object, DynamicObject newClass) {
            SharedObjects.propagate(getContext(), object, newClass);
            synchronized (object) {
                Layouts.BASIC_OBJECT.setLogicalClass(object, newClass);
                Layouts.BASIC_OBJECT.setMetaClass(object, newClass);
            }
            return object;
        }

    }

}
