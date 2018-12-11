package org.truffleruby.cext;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class UnwrapNode extends RubyBaseNode {

    public abstract Object execute(Object value);

    @Specialization(guards = "isWrapper(value)")
    public Object unwrapValue(DynamicObject value) {
        return Layouts.VALUE_WRAPPER.getObject(value);
    }

    @Fallback
    public Object unwrapTypeCastObject(Object value) {
        throw new RaiseException(getContext(), coreExceptions().argumentError("Unwrapping something that isn't a wrapper", this));
    }

    public static boolean isWrapper(TruffleObject value) {
        return ValueWrapperObjectType.isInstance(value);
    }
}
