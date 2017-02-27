/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes.GetByteNode;
import org.truffleruby.core.rope.RopeNodesFactory.GetByteNodeGen;
import org.truffleruby.core.string.StringNodes.SetByteNode;
import org.truffleruby.core.string.StringNodesFactory.SetByteNodeFactory;

import static org.truffleruby.core.string.StringOperations.rope;

@MessageResolution(
        receiverType = StringCharPointerAdapter.class,
        language = RubyLanguage.class
)
public class StringCharPointerMessageResolution {

    // TODO 28-Nov-16 could this be a Ruby class?

    @CanResolve
    public abstract static class CharPointerCheckNode extends Node {

        protected static boolean test(TruffleObject receiver) {
            return receiver instanceof StringCharPointerAdapter;
        }

    }

    @Resolve(message = "HAS_SIZE")
    public static abstract class CharPointerHasSizeNode extends Node {

        protected Object access(StringCharPointerAdapter object) {
            return true;
        }

    }

    @Resolve(message = "GET_SIZE")
    public static abstract class CharPointerGetSizeNode extends Node {

        protected Object access(StringCharPointerAdapter stringCharPointerAdapter) {
            return rope(stringCharPointerAdapter.getString()).byteLength();
        }

    }

    @Resolve(message = "READ")
    public static abstract class CharPointerReadNode extends Node {

        @Child private GetByteNode getByteNode;

        private final ConditionProfile beyondEndProfile = ConditionProfile.createBinaryProfile();

        protected Object access(StringCharPointerAdapter stringCharPointerAdapter, int index) {
            final Rope rope = rope(stringCharPointerAdapter.getString());

            if (beyondEndProfile.profile(index >= rope.byteLength())) {
                return 0;
            } else {
                return getHelperNode().executeGetByte(rope, index);
            }
        }

        private GetByteNode getHelperNode() {
            if (getByteNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                getByteNode = insert(GetByteNodeGen.create(null, null));
            }
            return getByteNode;
        }

    }

    @Resolve(message = "WRITE")
    public static abstract class CharPointerWriteNode extends Node {

        @Child private Node findContextNode;
        @Child private SetByteNode setByteNode;

        protected Object access(StringCharPointerAdapter stringCharPointerAdapter, int index, Object value) {
            return getHelperNode().executeSetByte(stringCharPointerAdapter.getString(), index, value);
        }

        private SetByteNode getHelperNode() {
            if (setByteNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                setByteNode = insert(SetByteNodeFactory.create(null, null, null));
            }
            return setByteNode;
        }

    }

    @Resolve(message = "UNBOX")
    public static abstract class CharPointerUnboxNode extends Node {

        @Child private Node findContextNode;

        private final ConditionProfile convertProfile = ConditionProfile.createBinaryProfile();

        @CompilationFinal private RubyContext context;

        protected Object access(StringCharPointerAdapter stringCharPointerAdapter) {
            final Rope currentRope = Layouts.STRING.getRope(stringCharPointerAdapter.getString());

            final NativeRope nativeRope;

            if (convertProfile.profile(currentRope instanceof NativeRope)) {
                nativeRope = (NativeRope) currentRope;
            } else {
                if (findContextNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    findContextNode = insert(RubyLanguage.INSTANCE.unprotectedCreateFindContextNode());
                    context = RubyLanguage.INSTANCE.unprotectedFindContext(findContextNode);
                }

                nativeRope = new NativeRope(context.getNativePlatform().getMemoryManager(), currentRope.getBytes(), currentRope.getEncoding(), currentRope.characterLength());
                Layouts.STRING.setRope(stringCharPointerAdapter.getString(), nativeRope);
            }

            return nativeRope.getNativePointer().address();
        }

    }

}
