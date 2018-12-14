package org.truffleruby.cext;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class WrapNode extends RubyBaseNode {

    public abstract TruffleObject execute(Object value);

    @Specialization
    public DynamicObject wrapInt(int value) {
        return getContext().getValueWrapperManager().longWrapper(value);
    }

    @Specialization
    public DynamicObject wrapLong(long value) {
        return getContext().getValueWrapperManager().longWrapper(value);
    }

    @Specialization
    public DynamicObject wrapDouble(double value) {
        return getContext().getValueWrapperManager().doubleWrapper(value);
    }

    @Specialization
    public DynamicObject wrapBoolean(boolean value) {
        return getContext().getValueWrapperManager().booleanWrapper(value);
    }

    @Specialization
    public DynamicObject wrapUndef(NotProvided value) {
        return getContext().getValueWrapperManager().undefWrapper();
    }

    @Specialization(guards = "isWrapped(value)")
    public DynamicObject wrapWrappedValue(DynamicObject value) {
        throw new RaiseException(getContext(), coreExceptions().argumentError(RopeOperations.encodeAscii("Wrapping wrapped object", UTF8Encoding.INSTANCE), this));
    }

    @Specialization(guards = "isRubyBasicObject(value)")
    public DynamicObject wrapValue(DynamicObject value,
            @Cached("createReader()") ReadObjectFieldNode readWrapperNode,
            @Cached("createWriter()") WriteObjectFieldNode writeWrapperNode) {
        DynamicObject wrapper = (DynamicObject) readWrapperNode.execute(value);
        if (wrapper == null) {
            synchronized (value) {
                wrapper = (DynamicObject) readWrapperNode.execute(value);
                if (wrapper == null) {
                    wrapper = ValueWrapperObjectType.createValueWrapper(value);
                    writeWrapperNode.write(value, wrapper);
                }
            }
        }
        return wrapper;
    }

    @Specialization(guards = "!isRubyBasicObject(value)")
    public TruffleObject wrapNonRubyObject(TruffleObject value) {
        throw new RaiseException(getContext(), coreExceptions().argumentError("Attempt to wrap something that isn't an Ruby object", this));
    }

    public ReadObjectFieldNode createReader() {
        return ReadObjectFieldNodeGen.create(Layouts.VALUE_WRAPPER_IDENTIFIER, null);
    }

    public WriteObjectFieldNode createWriter() {
        return WriteObjectFieldNodeGen.create(Layouts.VALUE_WRAPPER_IDENTIFIER);
    }

    public boolean isWrapped(TruffleObject value) {
        return ValueWrapperObjectType.isInstance(value);
    }

}
