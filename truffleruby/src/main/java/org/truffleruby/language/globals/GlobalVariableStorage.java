/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.truffleruby.RubyContext;

public class GlobalVariableStorage {

    private final CyclicAssumption unchangedAssumption = new CyclicAssumption("global variable unchanged");
    private int changes = 0;

    // This really means @CompilationFinal for compilation and volatile in interpreter
    @CompilationFinal private volatile boolean assumeConstant = true;

    private volatile Object value;

    private final DynamicObject getter;
    private final DynamicObject setter;

    GlobalVariableStorage(Object value, DynamicObject getter, DynamicObject setter) {
        this.value = value;
        this.getter = getter;
        this.setter = setter;
        assert (getter != null) == (setter != null);
    }

    public Object getValue() {
        return value;
    }

    public boolean isSimple() {
        return !hasHooks();
    }

    public boolean hasHooks() {
        return (getter != null) && (setter != null);
    }

    public DynamicObject getGetter() {
        return getter;
    }

    public DynamicObject getSetter() {
        return setter;
    }

    public Assumption getUnchangedAssumption() {
        return unchangedAssumption.getAssumption();
    }

    public boolean isAssumeConstant() {
        return assumeConstant;
    }

    public void setValueInternal(Object value) {
        this.value = value;
    }

    @TruffleBoundary
    public void updateAssumeConstant(RubyContext context) {
        synchronized (this) {
            if (!assumeConstant) {
                // Compiled code didn't see that we do not assumeConstant anymore
                return;
            }

            if (changes <= context.getOptions().GLOBAL_VARIABLE_MAX_INVALIDATIONS) {
                changes++;
                unchangedAssumption.invalidate();
            } else {
                unchangedAssumption.getAssumption().invalidate();
                assumeConstant = false;
            }
        }
    }

}
