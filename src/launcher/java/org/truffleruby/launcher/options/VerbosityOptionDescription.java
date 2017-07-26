/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.launcher.options;

import org.graalvm.options.OptionType;

public class VerbosityOptionDescription extends OptionDescription<Verbosity> {

    private final Verbosity defaultValue;

    public VerbosityOptionDescription(String name, String description, Verbosity defaultValue) {
        super(name, description);
        this.defaultValue = defaultValue;
    }

    @Override
    public Verbosity getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Verbosity checkValue(Object value) {
        try {
            return checkValueInner(value);
        } catch (IllegalArgumentException e) {
            throw new OptionTypeException(getName(), value.toString());
        }
    }

    private static Verbosity checkValueInner(Object value) {
        if (value == null) {
            return Verbosity.NIL;
        } else if (value instanceof Boolean) {
            if ((boolean) value) {
                return Verbosity.TRUE;
            } else {
                return Verbosity.FALSE;
            }
        } else if (value instanceof Integer) {
            switch ((int) value) {
                case 0:
                    return Verbosity.NIL;
                case 1:
                    return Verbosity.FALSE;
                case 2:
                    return Verbosity.TRUE;
                default:
                    throw new IllegalArgumentException();
            }
        } else if (value instanceof String) {
            switch ((String) value) {
                case "nil":
                case "NIL":
                    return Verbosity.NIL;
                case "false":
                case "FALSE":
                    return Verbosity.FALSE;
                case "true":
                case "TRUE":
                    return Verbosity.TRUE;
                case "0":
                    return Verbosity.NIL;
                case "1":
                    return Verbosity.FALSE;
                case "2":
                    return Verbosity.TRUE;
                default:
                    throw new IllegalArgumentException();
            }
        } else if (value instanceof Verbosity) {
            return (Verbosity) value;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static final OptionType<Verbosity> OPTION_TYPE = new OptionType<>("Verbosity", Verbosity.FALSE, VerbosityOptionDescription::checkValueInner);

    @Override
    protected OptionType<Verbosity> getOptionType() {
        return OPTION_TYPE;
    }

}
