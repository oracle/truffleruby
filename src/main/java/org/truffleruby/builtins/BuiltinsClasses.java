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

import org.truffleruby.cext.CExtNodesBuiltins;
import org.truffleruby.core.MainNodesBuiltins;
import org.truffleruby.core.MathNodesBuiltins;
import org.truffleruby.core.ProcessNodesBuiltins;
import org.truffleruby.core.TruffleGCNodesBuiltins;
import org.truffleruby.core.TruffleProcessNodesBuiltins;
import org.truffleruby.core.TruffleSystemNodesBuiltins;
import org.truffleruby.core.VMPrimitiveNodesBuiltins;
import org.truffleruby.core.array.ArrayNodesBuiltins;
import org.truffleruby.core.array.TruffleArrayNodesBuiltins;
import org.truffleruby.core.basicobject.BasicObjectNodesBuiltins;
import org.truffleruby.core.binding.BindingNodesBuiltins;
import org.truffleruby.core.binding.TruffleBindingNodesBuiltins;
import org.truffleruby.core.bool.FalseClassNodesBuiltins;
import org.truffleruby.core.bool.TrueClassNodesBuiltins;
import org.truffleruby.core.dir.DirNodesBuiltins;
import org.truffleruby.core.encoding.EncodingConverterNodesBuiltins;
import org.truffleruby.core.encoding.EncodingNodesBuiltins;
import org.truffleruby.core.encoding.TruffleEncodingNodesBuiltins;
import org.truffleruby.core.exception.ExceptionNodesBuiltins;
import org.truffleruby.core.exception.NameErrorNodesBuiltins;
import org.truffleruby.core.exception.NoMethodErrorNodesBuiltins;
import org.truffleruby.core.exception.SystemCallErrorNodesBuiltins;
import org.truffleruby.core.fiber.FiberNodesBuiltins;
import org.truffleruby.core.hash.HashNodesBuiltins;
import org.truffleruby.core.kernel.KernelNodesBuiltins;
import org.truffleruby.core.kernel.TruffleKernelNodesBuiltins;
import org.truffleruby.core.klass.ClassNodesBuiltins;
import org.truffleruby.core.method.MethodNodesBuiltins;
import org.truffleruby.core.method.UnboundMethodNodesBuiltins;
import org.truffleruby.core.module.ModuleNodesBuiltins;
import org.truffleruby.core.mutex.MutexNodesBuiltins;
import org.truffleruby.core.numeric.BignumNodesBuiltins;
import org.truffleruby.core.numeric.FixnumNodesBuiltins;
import org.truffleruby.core.numeric.FloatNodesBuiltins;
import org.truffleruby.core.numeric.IntegerNodesBuiltins;
import org.truffleruby.core.numeric.TruffleFixnumNodesBuiltins;
import org.truffleruby.core.objectspace.ObjectSpaceNodesBuiltins;
import org.truffleruby.core.proc.ProcNodesBuiltins;
import org.truffleruby.core.queue.QueueNodesBuiltins;
import org.truffleruby.core.queue.SizedQueueNodesBuiltins;
import org.truffleruby.core.range.RangeNodesBuiltins;
import org.truffleruby.core.regexp.MatchDataNodesBuiltins;
import org.truffleruby.core.regexp.RegexpNodesBuiltins;
import org.truffleruby.core.rope.TruffleRopesNodesBuiltins;
import org.truffleruby.core.rubinius.AtomicReferenceNodesBuiltins;
import org.truffleruby.core.rubinius.ByteArrayNodesBuiltins;
import org.truffleruby.core.rubinius.IOBufferNodesBuiltins;
import org.truffleruby.core.rubinius.IONodesBuiltins;
import org.truffleruby.core.rubinius.RandomizerNodesBuiltins;
import org.truffleruby.core.rubinius.StatNodesBuiltins;
import org.truffleruby.core.rubinius.TypeNodesBuiltins;
import org.truffleruby.core.rubinius.WeakRefNodesBuiltins;
import org.truffleruby.core.string.StringNodesBuiltins;
import org.truffleruby.core.string.TruffleStringNodesBuiltins;
import org.truffleruby.core.symbol.SymbolNodesBuiltins;
import org.truffleruby.core.thread.ThreadBacktraceLocationNodesBuiltins;
import org.truffleruby.core.thread.ThreadNodesBuiltins;
import org.truffleruby.core.time.TimeNodesBuiltins;
import org.truffleruby.core.tracepoint.TracePointNodesBuiltins;
import org.truffleruby.debug.TruffleDebugNodesBuiltins;
import org.truffleruby.extra.TruffleGraalNodesBuiltins;
import org.truffleruby.extra.TruffleNodesBuiltins;
import org.truffleruby.extra.TrufflePosixNodesBuiltins;
import org.truffleruby.extra.ffi.PointerNodesBuiltins;
import org.truffleruby.gem.bcrypt.BCryptNodesBuiltins;
import org.truffleruby.interop.InteropNodesBuiltins;
import org.truffleruby.interop.java.JavaUtilitiesNodesBuiltins;
import org.truffleruby.language.TruffleBootNodesBuiltins;
import org.truffleruby.stdlib.CoverageNodesBuiltins;
import org.truffleruby.stdlib.EtcNodesBuiltins;
import org.truffleruby.stdlib.ObjSpaceNodesBuiltins;
import org.truffleruby.stdlib.bigdecimal.BigDecimalNodesBuiltins;
import org.truffleruby.stdlib.digest.DigestNodesBuiltins;
import org.truffleruby.stdlib.psych.PsychEmitterNodesBuiltins;
import org.truffleruby.stdlib.psych.PsychParserNodesBuiltins;
import org.truffleruby.stdlib.readline.ReadlineHistoryNodesBuiltins;
import org.truffleruby.stdlib.readline.ReadlineNodesBuiltins;

public class BuiltinsClasses {

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

}
