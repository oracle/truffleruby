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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.truffleruby.Log;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.JavaException;

import com.oracle.truffle.api.dsl.NodeFactory;

public class BuiltinsCache {

    private final RubyContext context;
    private final CoreMethodNodeManager coreMethodNodeManager;
    private final PrimitiveManager primitiveManager;

    private final File cacheDir;
    private final File coreMethodsCacheFile;
    private final File primitivesCacheFile;

    public BuiltinsCache(RubyContext context, CoreMethodNodeManager coreMethodNodeManager, PrimitiveManager primitiveManager) {
        this.context = context;
        this.coreMethodNodeManager = coreMethodNodeManager;
        this.primitiveManager = primitiveManager;

        cacheDir = Paths.get(context.getRubyHome(), "lib", "truffle").toFile();
        coreMethodsCacheFile = new File(cacheDir, "core-methods.txt");
        primitivesCacheFile = new File(cacheDir, "primitives.txt");
    }

    private static final char SEPARATOR = ';';
    private static final Pattern SPLITTER = Pattern.compile("" + SEPARATOR);
    private static final Pattern COMMA = Pattern.compile(",");

    public boolean shouldUseCache() {
        if (context.getOptions().LAZY_BUILTINS) {
            final CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation().getProtocol().equals("file") && cacheDir.canWrite()) {
                return true;
            }
        }
        return false;
    }

    public boolean isCacheUpToDate() {
        final CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        final File jar = new File(codeSource.getLocation().getFile());
        return coreMethodsCacheFile.exists() && coreMethodsCacheFile.lastModified() > jar.lastModified() &&
                primitivesCacheFile.exists() && primitivesCacheFile.lastModified() > jar.lastModified();
    }

    private static class LockingFileWriter implements AutoCloseable {

        private final FileChannel channel;
        private final FileLock lock;
        private final Writer writer;

        public LockingFileWriter(File file) throws IOException {
            channel = FileChannel.open(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            writer = Channels.newWriter(channel, "UTF-8");
            lock = channel.lock();
        }

        public void close() throws IOException {
            writer.flush();
            lock.release();
            writer.close();
            channel.close();
        }

        public Writer getWriter() {
            return writer;
        }

    }

    private static class LockingFileReader implements AutoCloseable {

        private final FileChannel channel;
        private final FileLock lock;
        private final Reader reader;

        public LockingFileReader(File file) throws IOException {
            channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
            reader = Channels.newReader(channel, "UTF-8");
            lock = channel.lock(0, Long.MAX_VALUE, true);
        }

        public void close() throws IOException {
            lock.release();
            reader.close();
            channel.close();
        }

        public Reader getReader() {
            return reader;
        }

    }

    public void cachedCoreMethodsAndPrimitives(List<List<? extends NodeFactory<? extends RubyNode>>> coreNodeFactories) {
        Log.LOGGER.config("Regenerating builtins cache");

        try (LockingFileWriter methodsWriter = new LockingFileWriter(coreMethodsCacheFile);
                LockingFileWriter primitivesWriter = new LockingFileWriter(primitivesCacheFile);) {
            final Writer methods = methodsWriter.getWriter();
            final Writer primitives = primitivesWriter.getWriter();

            for (List<? extends NodeFactory<? extends RubyNode>> nodeFactories : coreNodeFactories) {
                for (NodeFactory<? extends RubyNode> nodeFactory : nodeFactories) {
                    final Class<?> nodeClass = nodeFactory.getNodeClass();
                    final CoreMethod method = nodeClass.getAnnotation(CoreMethod.class);
                    Primitive primitiveAnnotation;
                    if (method != null) {
                        String moduleName = nodeClass.getEnclosingClass().getAnnotation(CoreClass.class).value();
                        String type = method.isModuleFunction() ? "&" : method.onSingleton() || method.constructor() ? "." : "#";
                        String visibility = method.visibility().name();
                        methods.append(nodeFactory.getClass().getName()).append(SEPARATOR);
                        methods.append(moduleName).append(SEPARATOR);
                        methods.append(visibility).append(SEPARATOR);
                        methods.append(type).append(SEPARATOR);
                        final int rest = method.rest() ? 1 : 0;
                        methods.append("" + method.required() + method.optional() + rest).append(SEPARATOR);
                        final StringJoiner joiner = new StringJoiner(",");
                        for (String name : method.names()) {
                            joiner.add(name);
                        }
                        methods.append(joiner.toString());
                        methods.append('\n');
                    } else if ((primitiveAnnotation = nodeClass.getAnnotation(Primitive.class)) != null) {
                        primitives.append(nodeFactory.getClass().getName()).append(SEPARATOR);
                        primitives.append(primitiveAnnotation.name()).append('\n');
                    }
                }
            }
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

    public void loadLazilyFromCache() {
        try (LockingFileReader methodsReader = new LockingFileReader(coreMethodsCacheFile)) {
            final BufferedReader reader = new BufferedReader(methodsReader.getReader());
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = SPLITTER.split(line);
                String nodeFactoryName = parts[0];
                String moduleName = parts[1];
                Visibility visibility = Visibility.valueOf(parts[2]);
                char type = parts[3].charAt(0);
                String arity = parts[4];
                int required = arity.charAt(0) - '0';
                int optional = arity.charAt(1) - '0';
                boolean rest = arity.charAt(2) == '1';
                String[] names = COMMA.split(parts[5]);
                coreMethodNodeManager.addLazyCoreMethod(nodeFactoryName, moduleName, visibility,
                        type == '&', type == '.', required, optional, rest, names);
            }
        } catch (IOException e) {
            throw new JavaException(e);
        }

        try (LockingFileReader primitivesReader = new LockingFileReader(primitivesCacheFile)) {
            final BufferedReader reader = new BufferedReader(primitivesReader.getReader());
            String line;
            while ((line = reader.readLine()) != null) {
                int colon = line.indexOf(SEPARATOR);
                String className = line.substring(0, colon);
                String primitive = line.substring(colon + 1);
                primitiveManager.addLazyPrimitive(primitive, className);
            }
        } catch (IOException e) {
            throw new JavaException(e);
        }
    }

}
