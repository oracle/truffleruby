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

@ImportStatic(Message.class)
public abstract class UnwrapNode extends RubyBaseNode {

    public abstract Object execute(Object value);

    @Specialization(guards = "isWrapper(value)")
    public Object unwrapValue(DynamicObject value) {
        return Layouts.VALUE_WRAPPER.getObject(value);
    }

    @Specialization(guards = "!isWrapper(value)")
    public Object unwrapTypeCastObject(TruffleObject value,
                                       @Cached("IS_POINTER.createNode()") Node isPOinterNode,
                                       @Cached("AS_POINTER.createNode()") Node asPOinterNode) {
        if (ForeignAccess.sendIsPointer(isPOinterNode, value)) {
            try {
                long handle = ForeignAccess.sendAsPointer(asPOinterNode, value);
                return unwrapHandle(handle);
            } catch (UnsupportedMessageException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this, e));
            }
        }
        return value;
    }

    public Object unwrapHandle(long handle) {
        final Object value = ValueWrapperObjectType.getFromHandleMap(handle);
        if (value == null) {
            throw new ValueWrapperObjectType.HandleNotFoundException();
        }
        return value;
    }

    public static boolean isWrapper(TruffleObject value) {
        return ValueWrapperObjectType.isInstance(value);
    }
}
