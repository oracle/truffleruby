/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.truffleruby.RubyContext;
import org.truffleruby.language.NotProvided;

public class GlobalVariableStorage {

    private static final Object UNSET_VALUE = NotProvided.INSTANCE;

    private final Assumption validAssumption = Truffle.getRuntime().createAssumption("global variable not aliased");
    private final CyclicAssumption unchangedAssumption = new CyclicAssumption("global variable unchanged");
    private int changes = 0;

    // This really means @CompilationFinal for compilation and volatile in interpreter
    @CompilationFinal private volatile boolean assumeConstant = true;

    private volatile Object value;

    private final Object defaultValue;
    private final DynamicObject getter;
    private final DynamicObject setter;
    private final DynamicObject isDefined;

    GlobalVariableStorage(Object defaultValue, DynamicObject getter, DynamicObject setter, DynamicObject isDefined) {
        this(UNSET_VALUE, defaultValue, getter, setter, isDefined);
    }

    GlobalVariableStorage(Object value, Object defaultValue, DynamicObject getter, DynamicObject setter, DynamicObject isDefined) {
        assert ((getter == null) == (setter == null)) & ((getter == null) == (isDefined == null));

        this.defaultValue = defaultValue;
        this.value = value;
        this.getter = getter;
        this.setter = setter;
        this.isDefined = isDefined;
    }

    public Object getValue() {
        Object currentValue = value;
        return currentValue == UNSET_VALUE ? defaultValue : currentValue;
    }

    public boolean isDefined() {
        return value != UNSET_VALUE;
    }

    public boolean isSimple() {
        return !hasHooks();
    }

    public boolean hasHooks() {
        return (getter != null) && (setter != null) && (isDefined != null);
    }

    public DynamicObject getGetter() {
        return getter;
    }

    public DynamicObject getSetter() {
        return setter;
    }

    public DynamicObject getIsDefined() {
        return isDefined;
    }

    public Assumption getValidAssumption() {
        return validAssumption;
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

            // <= because the initial assignment is counted as an invalidation
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
