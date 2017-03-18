/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import jnr.posix.SpawnAttribute;
import jnr.posix.SpawnFileAction;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.KeyValue;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.control.RaiseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@CoreClass("Truffle::Process")
public abstract class TruffleProcessNodes {

    @CoreMethod(names = "spawn", onSingleton = true, required = 4)
    public abstract static class SpawnNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = {
                "isRubyString(command)",
                "isRubyArray(arguments)",
                "isRubyArray(environmentVariables)",
                "isRubyHash(options)" })
        public int spawn(DynamicObject command,
                         DynamicObject arguments,
                         DynamicObject environmentVariables,
                         DynamicObject options) {

            Collection<SpawnFileAction> fileActions = new ArrayList<>();
            Collection<SpawnAttribute> spawnAttributes = new ArrayList<>();
            parseOptions(options, fileActions, spawnAttributes);

            return call(
                StringOperations.getString(command),
                toStringArray(arguments),
                toStringArray(environmentVariables),
                fileActions,
                spawnAttributes);
        }

        private String[] toStringArray(DynamicObject rubyStrings) {
            final int size = Layouts.ARRAY.getSize(rubyStrings);
            final Object[] unconvertedStrings = ArrayOperations.toObjectArray(rubyStrings);
            final String[] strings = new String[size];

            for (int i = 0; i < size; i++) {
                assert Layouts.STRING.isString(unconvertedStrings[i]);
                strings[i] = StringOperations.getString((DynamicObject) unconvertedStrings[i]);
            }

            return strings;
        }

        @TruffleBoundary
        private void parseOptions(DynamicObject options, Collection<SpawnFileAction> fileActions, Collection<SpawnAttribute> spawnAttributes) {
            for (KeyValue keyValue : HashOperations.iterableKeyValues(options)) {
                final Object key = keyValue.getKey();
                final Object value = keyValue.getValue();

                if (!Layouts.SYMBOL.isSymbol(key)) {
                    throw new UnsupportedOperationException("spawn option key must be a symbol");
                }

                if (key == getSymbol("redirect_fd")) {
                    assert Layouts.ARRAY.isArray(value);
                    final DynamicObject array = (DynamicObject) value;
                    final int size = Layouts.ARRAY.getSize(array);
                    assert size % 2 == 0;
                    final Object[] store = ArrayOperations.toObjectArray(array);
                    for (int i = 0; i < size; i += 2) {
                        int from = (int) store[i];
                        int to = (int) store[i + 1];
                        if (to < 0) { // :child fd
                            to = -to - 1;
                        }
                        fileActions.add(SpawnFileAction.dup(to, from));
                    }
                } else if (key == getSymbol("assign_fd")) {
                    assert Layouts.ARRAY.isArray(value);
                    final DynamicObject array = (DynamicObject) value;
                    final int size = Layouts.ARRAY.getSize(array);
                    assert size % 4 == 0;
                    final Object[] store = ArrayOperations.toObjectArray(array);
                    for (int i = 0; i < size; i += 4) {
                        int fd = (int) store[i];
                        String path = StringOperations.getString((DynamicObject) store[i + 1]);
                        int flags = (int) store[i + 2];
                        int perms = (int) store[i + 3];
                        fileActions.add(SpawnFileAction.open(path, fd, flags, perms));
                    }
                } else if (key == getSymbol("pgroup")) {
                    long pgroup = (int) value;
                    if (pgroup >= 0) {
                        spawnAttributes.add(SpawnAttribute.flags((short) SpawnAttribute.SETPGROUP));
                        spawnAttributes.add(SpawnAttribute.pgroup(pgroup));
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported spawn option: " + key + " => " + value);
                }
            }
        }

        @TruffleBoundary
        private int call(String command, String[] arguments, String[] environmentVariables, Collection<SpawnFileAction> fileActions, Collection<SpawnAttribute> spawnAttributes) {
            return getContext().getNativePlatform().getPosix().posix_spawnp(
                    command,
                    fileActions,
                    spawnAttributes,
                    Arrays.asList(arguments),
                    Arrays.asList(environmentVariables));

        }
    }

}
