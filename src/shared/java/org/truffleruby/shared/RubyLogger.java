/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.shared;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
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

    public static final Logger LOGGER = createLogger();

    public static class RubyHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            System.err.printf("[ruby] %s %s%n", record.getLevel().getName(), record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

    }

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

    private static Logger createLogger() {
        final Logger logger = Logger.getLogger("org.truffleruby");

        if (LogManager.getLogManager().getProperty("org.truffleruby.handlers") == null) {
            logger.setUseParentHandlers(false);
            logger.addHandler(new RubyHandler());
        }

        final String propertyLogLevel = System.getProperty("truffleruby.log");

        if (propertyLogLevel != null) {
            setLevel(logger, propertyLogLevel);
        } else {
            final String environmentLogLevel = System.getenv("TRUFFLERUBY_LOG");

            if (environmentLogLevel != null) {
                setLevel(logger, environmentLogLevel);
            }
        }

        return logger;
    }

}
