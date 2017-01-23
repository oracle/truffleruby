/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.tools;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.truffleruby.Log;
import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.SafepointAction;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.JavaException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

@SuppressWarnings("restriction")
public class InstrumentationServerManager {

    private final RubyContext context;
    private final HttpServer server;

    private volatile boolean shuttingDown = false;

    public InstrumentationServerManager(RubyContext context, int port) {
        this.context = context;

        final InetAddress host = InetAddress.getLoopbackAddress();
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(host, port), 0);
        } catch (BindException e) {
            Log.LOGGER.warning("instrumentation server not started as port " + port + " was already in use: " + e);
        } catch (IOException e) {
            throw new JavaException(e);
        }

        this.server = server;
    }

    public void start() {
        server.createContext("/stacks", new HttpHandler() {

            @Override
            public void handle(HttpExchange httpExchange) {
                try {
                    final StringBuilder builder = new StringBuilder();

                    context.getSafepointManager().pauseAllThreadsAndExecuteFromNonRubyThread(false, new SafepointAction() {
                        @Override
                        public void accept(DynamicObject thread, Node currentNode) {
                            synchronized (this) {
                                try {
                                    final Backtrace backtrace = context.getCallStack().getBacktrace(null);

                                    final List<String> lines = BacktraceFormatter.createDefaultFormatter(context)
                                            .formatBacktrace(context, null, backtrace);

                                    builder.append(StringUtils.format("#%d %s",
                                            Thread.currentThread().getId(),
                                            Thread.currentThread().getName()));

                                    builder.append("\n");

                                    for (String line : lines) {
                                        builder.append(line);
                                        builder.append("\n");
                                    }

                                    builder.append("\n");
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });

                    final byte[] bytes = builder.toString().getBytes("UTF-8");

                    httpExchange.getResponseHeaders().set("Content-Type", "text/plain");
                    httpExchange.sendResponseHeaders(200, bytes.length);

                    final OutputStream stream = httpExchange.getResponseBody();
                    stream.write(bytes);
                    stream.close();
                } catch (IOException e) {
                    if (shuttingDown) {
                        return;
                    }

                    e.printStackTrace();
                }
            }

        });

        server.start();
    }

    public void shutdown() {
        if (server != null) {
            shuttingDown = true;
            // Leave it some time to send the remaining bytes.
            server.stop(1);
        }
    }

}
