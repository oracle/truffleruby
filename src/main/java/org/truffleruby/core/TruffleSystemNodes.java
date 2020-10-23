/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 * Copyright (C) 2005 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2006 Nick Sieger
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.core;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Set;
import java.util.logging.Level;

import org.graalvm.nativeimage.ProcessProperties;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.interop.FromJavaStringNode;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.platform.Platform;
import org.truffleruby.shared.BasicPlatform;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleFile;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule("Truffle::System")
public abstract class TruffleSystemNodes {

    @CoreMethod(names = "initial_environment_variables", onSingleton = true)
    public abstract static class InitEnvVarsNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyArray envVars() {
            final Set<String> variables = System.getenv().keySet();
            final int size = variables.size();
            final Encoding localeEncoding = getContext().getEncodingManager().getLocaleEncoding();
            final Object[] store = new Object[size];
            int i = 0;
            for (String variable : variables) {
                store[i++] = makeStringNode.executeMake(variable, localeEncoding, CodeRange.CR_UNKNOWN);
            }
            return createArray(store);
        }

    }

    @Primitive(name = "java_get_env")
    public abstract static class JavaGetEnv extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object javaGetEnv(RubyString name,
                @Cached ToJavaStringNode toJavaStringNode,
                @Cached FromJavaStringNode fromJavaStringNode,
                @Cached ConditionProfile nullValueProfile) {
            final String javaName = toJavaStringNode.executeToJavaString(name);
            final String value = getEnv(javaName);

            if (nullValueProfile.profile(value == null)) {
                return nil;
            } else {
                return fromJavaStringNode.executeFromJavaString(value);
            }
        }

        @TruffleBoundary
        private String getEnv(String name) {
            return System.getenv(name);
        }

    }

    @Primitive(name = "dir_set_truffle_working_directory")
    public abstract static class SetTruffleWorkingDirNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object setTruffleWorkingDir(RubyString dir) {
            TruffleFile truffleFile = getContext().getEnv().getPublicTruffleFile(dir.getJavaString());
            final TruffleFile canonicalFile;
            try {
                canonicalFile = truffleFile.getCanonicalFile();
            } catch (NoSuchFileException e) {
                // Let the following chdir() fail
                return nil;
            } catch (IOException e) {
                throw new RaiseException(getContext(), coreExceptions().ioError(e, this));
            }
            getContext().getEnv().setCurrentWorkingDirectory(canonicalFile);
            getContext().getFeatureLoader().setWorkingDirectory(canonicalFile.getPath());
            return dir;
        }
    }

    @Primitive(name = "working_directory")
    public abstract static class GetTruffleWorkingDirNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyString getTruffleWorkingDir(
                @Cached StringNodes.MakeStringNode makeStringNode) {
            final String cwd = getContext().getFeatureLoader().getWorkingDirectory();
            final Encoding externalEncoding = getContext().getEncodingManager().getDefaultExternalEncoding();
            return makeStringNode.executeMake(cwd, externalEncoding, CodeRange.CR_UNKNOWN);
        }
    }

    @CoreMethod(names = "get_java_property", onSingleton = true, required = 1)
    public abstract static class GetJavaPropertyNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected Object getJavaProperty(RubyString property) {
            String value = getProperty(property.getJavaString());
            if (value == null) {
                return nil;
            } else {
                return makeStringNode.executeMake(value, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
            }
        }

        @TruffleBoundary
        private static String getProperty(String key) {
            return System.getProperty(key);
        }
    }

    @CoreMethod(names = "host_cpu", onSingleton = true)
    public abstract static class HostCPUNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString hostCPU() {
            return makeStringNode.executeMake(BasicPlatform.getArchName(), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

    }

    @CoreMethod(names = "host_os", onSingleton = true)
    public abstract static class HostOSNode extends CoreMethodNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization
        protected RubyString hostOS() {
            return makeStringNode.executeMake(Platform.getOSName(), UTF8Encoding.INSTANCE, CodeRange.CR_7BIT);
        }

    }

    @CoreMethod(names = "synchronized", onSingleton = true, required = 1, needsBlock = true)
    public abstract static class SynchronizedPrimitiveNode extends YieldingCoreMethodNode {

        // We must not allow to synchronize on boxed primitives.
        @Specialization
        protected Object synchronize(RubyDynamicObject object, RubyProc block) {
            synchronized (object) {
                return yield(block);
            }
        }
    }

    @CoreMethod(names = "log", onSingleton = true, required = 2)
    public abstract static class LogNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "level == cachedLevel" })
        protected Object logCached(RubySymbol level, RubyString message,
                @Cached("level") RubySymbol cachedLevel,
                @Cached("getLevel(cachedLevel)") Level javaLevel) {
            log(javaLevel, message.getJavaString());
            return nil;
        }

        @Specialization(replaces = "logCached")
        protected Object log(RubySymbol level, RubyString message) {
            log(getLevel(level), message.getJavaString());
            return nil;
        }

        @TruffleBoundary
        protected Level getLevel(RubySymbol level) {
            try {
                return Level.parse(level.getString());
            } catch (IllegalArgumentException e) {
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError(
                        "Could not find log level for: " + level,
                        this));
            }
        }

        @TruffleBoundary
        public static void log(Level level, String message) {
            RubyLanguage.LOGGER.log(level, message);
        }

    }

    @CoreMethod(names = "native_set_process_title", onSingleton = true, required = 1)
    public abstract static class SetProcessTitleNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyString setProcessTitle(RubyString name) {
            if (TruffleOptions.AOT) {
                ProcessProperties.setArgumentVectorProgramName(name.getJavaString());
            } else {
                // already checked in the caller
                throw CompilerDirectives.shouldNotReachHere();
            }
            return name;
        }

    }

    @CoreMethod(names = "available_processors", onSingleton = true)
    public abstract static class AvailableProcessorsNode extends CoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected int availableProcessors() {
            return Runtime.getRuntime().availableProcessors();
        }

    }

}
