/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.launcher.options;

public enum DefaultExecutionAction {

    NONE {
        @Override
        public void applyTo(CommandLineOptions config) {
            config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.NONE);
        }
    },

    STDIN {
        @Override
        public void applyTo(CommandLineOptions config) {
            config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.STDIN);
        }
    },

    IRB {
        @Override
        public void applyTo(CommandLineOptions config) {
            if (System.console() != null) {
                config.setOption(OptionsCatalog.EXECUTION_ACTION, ExecutionAction.PATH);
                config.setOption(OptionsCatalog.TO_EXECUTE, "irb");
                config.setIrbInsteadOfInputUsed(true);
            } else {
                STDIN.applyTo(config);
            }
        }
    };

    public abstract void applyTo(CommandLineOptions config);
}
