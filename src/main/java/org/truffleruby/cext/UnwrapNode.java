package org.truffleruby.cext;

import static org.truffleruby.cext.ValueWrapperManager.LONG_TAG;
import static org.truffleruby.cext.ValueWrapperManager.OBJECT_TAG;

import org.truffleruby.Layouts;

import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@ImportStatic(Message.class)
public abstract class UnwrapNode extends RubyBaseNode {

    @ImportStatic(ValueWrapperManager.class)
    public static abstract class UnwrapNativeNode extends RubyBaseNode {

        public abstract Object execute(long handle);

        @Specialization(guards = "handle == FALSE_HANDLE")
        public Object unwrapFalse(long handle) {
            return false;
        }

        @Specialization(guards = "handle == TRUE_HANDLE")
        public Object unwrapTrue(long handle) {
            return true;
        }

        @Specialization(guards = "handle == UNDEF_HANDLE")
        public Object unwrapUndef(long handle) {
            return NotProvided.INSTANCE;
        }

        @Specialization(guards = "handle == NIL_HANDLE")
        public Object unwrapNil(long handle) {
            return nil();
        }

        @Specialization(guards = "isTaggedLong(handle)")
        public Object unwrapTaggedLong(long handle) {
            return handle >> 3;
        }

        @Specialization(guards = "isTaggedObject(handle)")
        public Object unwrapTaggedObject(long handle) {
            return getContext().getValueWrapperManager().getFromHandleMap(handle);
        }

        public boolean isTaggedLong(long handle) {
            return (handle & 0x7L) == LONG_TAG;
        }

        public boolean isTaggedObject(long handle) {
            return (handle & 0x7L) == OBJECT_TAG;
        }

        public static UnwrapNativeNode create() {
            return UnwrapNativeNodeGen.create();
        }
    }

    public abstract Object execute(Object value);

    @Specialization(guards = "isWrapper(value)")
    public Object unwrapValue(DynamicObject value) {
        return Layouts.VALUE_WRAPPER.getObject(value);
    }

    @Specialization(guards = "!isWrapper(value)")
    public Object unwrapTypeCastObject(TruffleObject value,
            @Cached("IS_POINTER.createNode()") Node isPointerNode,
            @Cached("AS_POINTER.createNode()") Node asPointerNode,
            @Cached("create()") UnwrapNativeNode unwrapNativeNode,
            @Cached("create()") BranchProfile unsupportedProfile,
            @Cached("create()") BranchProfile nonPointerProfile) {
        if (ForeignAccess.sendIsPointer(isPointerNode, value)) {
            long handle = 0;
            try {
                handle = ForeignAccess.sendAsPointer(asPointerNode, value);
            } catch (UnsupportedMessageException e) {
                unsupportedProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
            }
            return unwrapNativeNode.execute(handle);
        } else {
            nonPointerProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentError("Not a handle or a pointer", this));
        }
    }

    public static boolean isWrapper(TruffleObject value) {
        return ValueWrapperObjectType.isInstance(value);
    }
}
