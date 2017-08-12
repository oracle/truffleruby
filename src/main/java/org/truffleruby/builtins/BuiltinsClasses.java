/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.builtins;

import java.util.Arrays;
import java.util.List;

import org.truffleruby.cext.CExtNodesBuiltins;
import org.truffleruby.cext.CExtNodesFactory;
import org.truffleruby.core.MainNodesBuiltins;
import org.truffleruby.core.MainNodesFactory;
import org.truffleruby.core.MathNodesBuiltins;
import org.truffleruby.core.MathNodesFactory;
import org.truffleruby.core.ProcessNodesBuiltins;
import org.truffleruby.core.ProcessNodesFactory;
import org.truffleruby.core.TruffleGCNodesBuiltins;
import org.truffleruby.core.TruffleGCNodesFactory;
import org.truffleruby.core.TruffleProcessNodesBuiltins;
import org.truffleruby.core.TruffleProcessNodesFactory;
import org.truffleruby.core.TruffleSystemNodesBuiltins;
import org.truffleruby.core.TruffleSystemNodesFactory;
import org.truffleruby.core.VMPrimitiveNodesBuiltins;
import org.truffleruby.core.VMPrimitiveNodesFactory;
import org.truffleruby.core.array.ArrayNodesBuiltins;
import org.truffleruby.core.array.ArrayNodesFactory;
import org.truffleruby.core.array.TruffleArrayNodesBuiltins;
import org.truffleruby.core.array.TruffleArrayNodesFactory;
import org.truffleruby.core.basicobject.BasicObjectNodesBuiltins;
import org.truffleruby.core.basicobject.BasicObjectNodesFactory;
import org.truffleruby.core.binding.BindingNodesBuiltins;
import org.truffleruby.core.binding.BindingNodesFactory;
import org.truffleruby.core.binding.TruffleBindingNodesBuiltins;
import org.truffleruby.core.binding.TruffleBindingNodesFactory;
import org.truffleruby.core.bool.FalseClassNodesBuiltins;
import org.truffleruby.core.bool.FalseClassNodesFactory;
import org.truffleruby.core.bool.TrueClassNodesBuiltins;
import org.truffleruby.core.bool.TrueClassNodesFactory;
import org.truffleruby.core.dir.DirNodesBuiltins;
import org.truffleruby.core.dir.DirNodesFactory;
import org.truffleruby.core.encoding.EncodingConverterNodesBuiltins;
import org.truffleruby.core.encoding.EncodingConverterNodesFactory;
import org.truffleruby.core.encoding.EncodingNodesBuiltins;
import org.truffleruby.core.encoding.EncodingNodesFactory;
import org.truffleruby.core.encoding.TruffleEncodingNodesBuiltins;
import org.truffleruby.core.encoding.TruffleEncodingNodesFactory;
import org.truffleruby.core.exception.ExceptionNodesBuiltins;
import org.truffleruby.core.exception.ExceptionNodesFactory;
import org.truffleruby.core.exception.NameErrorNodesBuiltins;
import org.truffleruby.core.exception.NameErrorNodesFactory;
import org.truffleruby.core.exception.NoMethodErrorNodesBuiltins;
import org.truffleruby.core.exception.NoMethodErrorNodesFactory;
import org.truffleruby.core.exception.SystemCallErrorNodesBuiltins;
import org.truffleruby.core.exception.SystemCallErrorNodesFactory;
import org.truffleruby.core.fiber.FiberNodesBuiltins;
import org.truffleruby.core.fiber.FiberNodesFactory;
import org.truffleruby.core.hash.HashNodesBuiltins;
import org.truffleruby.core.hash.HashNodesFactory;
import org.truffleruby.core.kernel.KernelNodesBuiltins;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.kernel.TruffleKernelNodesBuiltins;
import org.truffleruby.core.kernel.TruffleKernelNodesFactory;
import org.truffleruby.core.klass.ClassNodesBuiltins;
import org.truffleruby.core.klass.ClassNodesFactory;
import org.truffleruby.core.method.MethodNodesBuiltins;
import org.truffleruby.core.method.MethodNodesFactory;
import org.truffleruby.core.method.UnboundMethodNodesBuiltins;
import org.truffleruby.core.method.UnboundMethodNodesFactory;
import org.truffleruby.core.module.ModuleNodesBuiltins;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.core.mutex.MutexNodesBuiltins;
import org.truffleruby.core.mutex.MutexNodesFactory;
import org.truffleruby.core.numeric.BignumNodesBuiltins;
import org.truffleruby.core.numeric.BignumNodesFactory;
import org.truffleruby.core.numeric.FixnumNodesBuiltins;
import org.truffleruby.core.numeric.FixnumNodesFactory;
import org.truffleruby.core.numeric.FloatNodesBuiltins;
import org.truffleruby.core.numeric.FloatNodesFactory;
import org.truffleruby.core.numeric.IntegerNodesBuiltins;
import org.truffleruby.core.numeric.IntegerNodesFactory;
import org.truffleruby.core.numeric.TruffleFixnumNodesBuiltins;
import org.truffleruby.core.numeric.TruffleFixnumNodesFactory;
import org.truffleruby.core.objectspace.ObjectSpaceNodesBuiltins;
import org.truffleruby.core.objectspace.ObjectSpaceNodesFactory;
import org.truffleruby.core.proc.ProcNodesBuiltins;
import org.truffleruby.core.proc.ProcNodesFactory;
import org.truffleruby.core.queue.QueueNodesBuiltins;
import org.truffleruby.core.queue.QueueNodesFactory;
import org.truffleruby.core.queue.SizedQueueNodesBuiltins;
import org.truffleruby.core.queue.SizedQueueNodesFactory;
import org.truffleruby.core.range.RangeNodesBuiltins;
import org.truffleruby.core.range.RangeNodesFactory;
import org.truffleruby.core.regexp.MatchDataNodesBuiltins;
import org.truffleruby.core.regexp.MatchDataNodesFactory;
import org.truffleruby.core.regexp.RegexpNodesBuiltins;
import org.truffleruby.core.regexp.RegexpNodesFactory;
import org.truffleruby.core.rope.TruffleRopesNodesBuiltins;
import org.truffleruby.core.rope.TruffleRopesNodesFactory;
import org.truffleruby.core.rubinius.AtomicReferenceNodesBuiltins;
import org.truffleruby.core.rubinius.AtomicReferenceNodesFactory;
import org.truffleruby.core.rubinius.ByteArrayNodesBuiltins;
import org.truffleruby.core.rubinius.ByteArrayNodesFactory;
import org.truffleruby.core.rubinius.IOBufferNodesBuiltins;
import org.truffleruby.core.rubinius.IOBufferNodesFactory;
import org.truffleruby.core.rubinius.IONodesBuiltins;
import org.truffleruby.core.rubinius.IONodesFactory;
import org.truffleruby.core.rubinius.RandomizerNodesBuiltins;
import org.truffleruby.core.rubinius.RandomizerNodesFactory;
import org.truffleruby.core.rubinius.StatNodesBuiltins;
import org.truffleruby.core.rubinius.StatNodesFactory;
import org.truffleruby.core.rubinius.TypeNodesBuiltins;
import org.truffleruby.core.rubinius.TypeNodesFactory;
import org.truffleruby.core.rubinius.WeakRefNodesBuiltins;
import org.truffleruby.core.rubinius.WeakRefNodesFactory;
import org.truffleruby.core.string.StringNodesBuiltins;
import org.truffleruby.core.string.StringNodesFactory;
import org.truffleruby.core.string.TruffleStringNodesBuiltins;
import org.truffleruby.core.string.TruffleStringNodesFactory;
import org.truffleruby.core.symbol.SymbolNodesBuiltins;
import org.truffleruby.core.symbol.SymbolNodesFactory;
import org.truffleruby.core.thread.ThreadBacktraceLocationNodesBuiltins;
import org.truffleruby.core.thread.ThreadBacktraceLocationNodesFactory;
import org.truffleruby.core.thread.ThreadNodesBuiltins;
import org.truffleruby.core.thread.ThreadNodesFactory;
import org.truffleruby.core.time.TimeNodesBuiltins;
import org.truffleruby.core.time.TimeNodesFactory;
import org.truffleruby.core.tracepoint.TracePointNodesBuiltins;
import org.truffleruby.core.tracepoint.TracePointNodesFactory;
import org.truffleruby.debug.TruffleDebugNodesBuiltins;
import org.truffleruby.debug.TruffleDebugNodesFactory;
import org.truffleruby.extra.TruffleGraalNodesBuiltins;
import org.truffleruby.extra.TruffleGraalNodesFactory;
import org.truffleruby.extra.TruffleNodesBuiltins;
import org.truffleruby.extra.TruffleNodesFactory;
import org.truffleruby.extra.TrufflePosixNodesBuiltins;
import org.truffleruby.extra.TrufflePosixNodesFactory;
import org.truffleruby.extra.ffi.PointerNodesBuiltins;
import org.truffleruby.extra.ffi.PointerNodesFactory;
import org.truffleruby.gem.bcrypt.BCryptNodesBuiltins;
import org.truffleruby.gem.bcrypt.BCryptNodesFactory;
import org.truffleruby.interop.InteropNodesBuiltins;
import org.truffleruby.interop.InteropNodesFactory;
import org.truffleruby.interop.java.JavaUtilitiesNodesBuiltins;
import org.truffleruby.interop.java.JavaUtilitiesNodesFactory;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.TruffleBootNodesBuiltins;
import org.truffleruby.language.TruffleBootNodesFactory;
import org.truffleruby.stdlib.CoverageNodesBuiltins;
import org.truffleruby.stdlib.CoverageNodesFactory;
import org.truffleruby.stdlib.EtcNodesBuiltins;
import org.truffleruby.stdlib.EtcNodesFactory;
import org.truffleruby.stdlib.ObjSpaceNodesBuiltins;
import org.truffleruby.stdlib.ObjSpaceNodesFactory;
import org.truffleruby.stdlib.bigdecimal.BigDecimalNodesBuiltins;
import org.truffleruby.stdlib.bigdecimal.BigDecimalNodesFactory;
import org.truffleruby.stdlib.digest.DigestNodesBuiltins;
import org.truffleruby.stdlib.digest.DigestNodesFactory;
import org.truffleruby.stdlib.psych.PsychEmitterNodesBuiltins;
import org.truffleruby.stdlib.psych.PsychEmitterNodesFactory;
import org.truffleruby.stdlib.psych.PsychParserNodesBuiltins;
import org.truffleruby.stdlib.psych.PsychParserNodesFactory;
import org.truffleruby.stdlib.readline.ReadlineHistoryNodesBuiltins;
import org.truffleruby.stdlib.readline.ReadlineHistoryNodesFactory;
import org.truffleruby.stdlib.readline.ReadlineNodesBuiltins;
import org.truffleruby.stdlib.readline.ReadlineNodesFactory;

import com.oracle.truffle.api.dsl.NodeFactory;

public abstract class BuiltinsClasses {

    // These two lists need to be kept in sync

    // Sorted alphabetically to avoid duplicates
    public static void setupBuiltinsLazy(CoreMethodNodeManager coreManager, PrimitiveManager primitiveManager) {
        ArrayNodesBuiltins.setup(coreManager, primitiveManager);
        AtomicReferenceNodesBuiltins.setup(coreManager, primitiveManager);
        BasicObjectNodesBuiltins.setup(coreManager, primitiveManager);
        BCryptNodesBuiltins.setup(coreManager, primitiveManager);
        BigDecimalNodesBuiltins.setup(coreManager, primitiveManager);
        BignumNodesBuiltins.setup(coreManager, primitiveManager);
        BindingNodesBuiltins.setup(coreManager, primitiveManager);
        ByteArrayNodesBuiltins.setup(coreManager, primitiveManager);
        CExtNodesBuiltins.setup(coreManager, primitiveManager);
        ClassNodesBuiltins.setup(coreManager, primitiveManager);
        CoverageNodesBuiltins.setup(coreManager, primitiveManager);
        DigestNodesBuiltins.setup(coreManager, primitiveManager);
        DirNodesBuiltins.setup(coreManager, primitiveManager);
        EncodingConverterNodesBuiltins.setup(coreManager, primitiveManager);
        EncodingNodesBuiltins.setup(coreManager, primitiveManager);
        EtcNodesBuiltins.setup(coreManager, primitiveManager);
        ExceptionNodesBuiltins.setup(coreManager, primitiveManager);
        FalseClassNodesBuiltins.setup(coreManager, primitiveManager);
        FiberNodesBuiltins.setup(coreManager, primitiveManager);
        FixnumNodesBuiltins.setup(coreManager, primitiveManager);
        FloatNodesBuiltins.setup(coreManager, primitiveManager);
        JavaUtilitiesNodesBuiltins.setup(coreManager, primitiveManager);
        HashNodesBuiltins.setup(coreManager, primitiveManager);
        IntegerNodesBuiltins.setup(coreManager, primitiveManager);
        InteropNodesBuiltins.setup(coreManager, primitiveManager);
        IOBufferNodesBuiltins.setup(coreManager, primitiveManager);
        IONodesBuiltins.setup(coreManager, primitiveManager);
        KernelNodesBuiltins.setup(coreManager, primitiveManager);
        MainNodesBuiltins.setup(coreManager, primitiveManager);
        MatchDataNodesBuiltins.setup(coreManager, primitiveManager);
        MathNodesBuiltins.setup(coreManager, primitiveManager);
        MethodNodesBuiltins.setup(coreManager, primitiveManager);
        ModuleNodesBuiltins.setup(coreManager, primitiveManager);
        MutexNodesBuiltins.setup(coreManager, primitiveManager);
        NameErrorNodesBuiltins.setup(coreManager, primitiveManager);
        NoMethodErrorNodesBuiltins.setup(coreManager, primitiveManager);
        ObjectSpaceNodesBuiltins.setup(coreManager, primitiveManager);
        ObjSpaceNodesBuiltins.setup(coreManager, primitiveManager);
        PointerNodesBuiltins.setup(coreManager, primitiveManager);
        ProcessNodesBuiltins.setup(coreManager, primitiveManager);
        ProcNodesBuiltins.setup(coreManager, primitiveManager);
        PsychEmitterNodesBuiltins.setup(coreManager, primitiveManager);
        PsychParserNodesBuiltins.setup(coreManager, primitiveManager);
        QueueNodesBuiltins.setup(coreManager, primitiveManager);
        RandomizerNodesBuiltins.setup(coreManager, primitiveManager);
        RangeNodesBuiltins.setup(coreManager, primitiveManager);
        ReadlineNodesBuiltins.setup(coreManager, primitiveManager);
        ReadlineHistoryNodesBuiltins.setup(coreManager, primitiveManager);
        RegexpNodesBuiltins.setup(coreManager, primitiveManager);
        SizedQueueNodesBuiltins.setup(coreManager, primitiveManager);
        StatNodesBuiltins.setup(coreManager, primitiveManager);
        StringNodesBuiltins.setup(coreManager, primitiveManager);
        SymbolNodesBuiltins.setup(coreManager, primitiveManager);
        SystemCallErrorNodesBuiltins.setup(coreManager, primitiveManager);
        ThreadBacktraceLocationNodesBuiltins.setup(coreManager, primitiveManager);
        ThreadNodesBuiltins.setup(coreManager, primitiveManager);
        TimeNodesBuiltins.setup(coreManager, primitiveManager);
        TracePointNodesBuiltins.setup(coreManager, primitiveManager);
        TrueClassNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleArrayNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleBindingNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleBootNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleDebugNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleEncodingNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleFixnumNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleGCNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleGraalNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleKernelNodesBuiltins.setup(coreManager, primitiveManager);
        TrufflePosixNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleProcessNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleRopesNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleStringNodesBuiltins.setup(coreManager, primitiveManager);
        TruffleSystemNodesBuiltins.setup(coreManager, primitiveManager);
        TypeNodesBuiltins.setup(coreManager, primitiveManager);
        UnboundMethodNodesBuiltins.setup(coreManager, primitiveManager);
        VMPrimitiveNodesBuiltins.setup(coreManager, primitiveManager);
        WeakRefNodesBuiltins.setup(coreManager, primitiveManager);
    }

    // Sorted alphabetically to avoid duplicates
    public static List<List<? extends NodeFactory<? extends RubyNode>>> getCoreNodeFactories() {
        return Arrays.asList(
            ArrayNodesFactory.getFactories(),
            AtomicReferenceNodesFactory.getFactories(),
            BasicObjectNodesFactory.getFactories(),
            BCryptNodesFactory.getFactories(),
            BigDecimalNodesFactory.getFactories(),
            BignumNodesFactory.getFactories(),
            BindingNodesFactory.getFactories(),
            ByteArrayNodesFactory.getFactories(),
            CExtNodesFactory.getFactories(),
            ClassNodesFactory.getFactories(),
            CoverageNodesFactory.getFactories(),
            DigestNodesFactory.getFactories(),
            DirNodesFactory.getFactories(),
            EncodingConverterNodesFactory.getFactories(),
            EncodingNodesFactory.getFactories(),
            EtcNodesFactory.getFactories(),
            ExceptionNodesFactory.getFactories(),
            FalseClassNodesFactory.getFactories(),
            FiberNodesFactory.getFactories(),
            FixnumNodesFactory.getFactories(),
            FloatNodesFactory.getFactories(),
            JavaUtilitiesNodesFactory.getFactories(),
            HashNodesFactory.getFactories(),
            IntegerNodesFactory.getFactories(),
            InteropNodesFactory.getFactories(),
            IOBufferNodesFactory.getFactories(),
            IONodesFactory.getFactories(),
            KernelNodesFactory.getFactories(),
            MainNodesFactory.getFactories(),
            MatchDataNodesFactory.getFactories(),
            MathNodesFactory.getFactories(),
            MethodNodesFactory.getFactories(),
            ModuleNodesFactory.getFactories(),
            MutexNodesFactory.getFactories(),
            NameErrorNodesFactory.getFactories(),
            NoMethodErrorNodesFactory.getFactories(),
            ObjectSpaceNodesFactory.getFactories(),
            ObjSpaceNodesFactory.getFactories(),
            PointerNodesFactory.getFactories(),
            ProcessNodesFactory.getFactories(),
            ProcNodesFactory.getFactories(),
            PsychEmitterNodesFactory.getFactories(),
            PsychParserNodesFactory.getFactories(),
            QueueNodesFactory.getFactories(),
            RandomizerNodesFactory.getFactories(),
            RangeNodesFactory.getFactories(),
            ReadlineNodesFactory.getFactories(),
            ReadlineHistoryNodesFactory.getFactories(),
            RegexpNodesFactory.getFactories(),
            SizedQueueNodesFactory.getFactories(),
            StatNodesFactory.getFactories(),
            StringNodesFactory.getFactories(),
            SymbolNodesFactory.getFactories(),
            SystemCallErrorNodesFactory.getFactories(),
            ThreadBacktraceLocationNodesFactory.getFactories(),
            ThreadNodesFactory.getFactories(),
            TimeNodesFactory.getFactories(),
            TracePointNodesFactory.getFactories(),
            TrueClassNodesFactory.getFactories(),
            TruffleArrayNodesFactory.getFactories(),
            TruffleBindingNodesFactory.getFactories(),
            TruffleBootNodesFactory.getFactories(),
            TruffleDebugNodesFactory.getFactories(),
            TruffleEncodingNodesFactory.getFactories(),
            TruffleFixnumNodesFactory.getFactories(),
            TruffleGCNodesFactory.getFactories(),
            TruffleNodesFactory.getFactories(),
            TruffleGraalNodesFactory.getFactories(),
            TruffleKernelNodesFactory.getFactories(),
            TrufflePosixNodesFactory.getFactories(),
            TruffleProcessNodesFactory.getFactories(),
            TruffleRopesNodesFactory.getFactories(),
            TruffleStringNodesFactory.getFactories(),
            TruffleSystemNodesFactory.getFactories(),
            TypeNodesFactory.getFactories(),
            UnboundMethodNodesFactory.getFactories(),
            VMPrimitiveNodesFactory.getFactories(),
            WeakRefNodesFactory.getFactories());
    }

}
