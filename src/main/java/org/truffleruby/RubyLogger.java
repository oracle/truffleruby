/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.Truffle;
import org.truffleruby.shared.TruffleRuby;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RubyLogger {

    private static class RubyLevel extends Level {

        private static final long serialVersionUID = 3759389129096588683L;

        public RubyLevel(String name, Level parent) {
            super(name, parent.intValue(), parent.getResourceBundleName());
        }

    }

    public static final Level PERFORMANCE = new RubyLevel("PERFORMANCE", Level.WARNING);
    public static final Level PATCH = new RubyLevel("PATCH", Level.CONFIG);
    public static final Level[] LEVELS = new Level[]{PERFORMANCE, PATCH};

    public static final Logger LOGGER = Truffle.getLogger(TruffleRuby.LANGUAGE_ID, "ruby");

    public static void setLevel(String levelString) {
        setLevel(LOGGER, levelString);
    }

    private static void setLevel(Logger logger, String levelString) {
        final Level level;

        if (levelString.equals("PERFORMANCE")) {
            level = RubyLogger.PERFORMANCE;
        } else {
            level = Level.parse(levelString.toUpperCase());
        }

        logger.setLevel(level);
    }

}
