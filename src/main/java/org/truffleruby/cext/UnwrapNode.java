package org.truffleruby.cext;

import org.truffleruby.Layouts;
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

    public abstract Object execute(Object value);

    @Specialization(guards = "isWrapper(value)")
    public Object unwrapValue(DynamicObject value) {
        return Layouts.VALUE_WRAPPER.getObject(value);
    }

    @Specialization(guards = "!isWrapper(value)")
    public Object unwrapTypeCastObject(TruffleObject value,
            @Cached("IS_POINTER.createNode()") Node isPointerNode,
            @Cached("AS_POINTER.createNode()") Node asPointerNode,
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
            return unwrapHandle(handle);
        } else {
            nonPointerProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().argumentError("Not a handle or a pointer", this));
        }
    }

    private Object unwrapHandle(long handle) {
        return ValueWrapperObjectType.getFromHandleMap(handle);
    }

    public static boolean isWrapper(TruffleObject value) {
        return ValueWrapperObjectType.isInstance(value);
    }
}
