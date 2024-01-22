/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.readline;

import java.io.Console;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class IsTTYHelper {

    private static final Method IS_TERMINAL_METHOD = getIsTerminalMethod();

    private static Method getIsTerminalMethod() {
        try {
            return Console.class.getMethod("isTerminal");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public static boolean isTTY() {
        Console console = System.console();
        if (console == null) {
            return false;
        }
        if (IS_TERMINAL_METHOD != null) {
            try {
                return (boolean) IS_TERMINAL_METHOD.invoke(console);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new Error(e);
            }
        } else {
            return true;
        }
    }

}
