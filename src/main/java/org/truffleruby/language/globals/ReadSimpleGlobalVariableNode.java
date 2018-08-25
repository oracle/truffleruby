package org.truffleruby.language.globals;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ReadSimpleGlobalVariableNode extends RubyBaseNode {

    protected final GlobalVariableStorage storage;

    public static ReadSimpleGlobalVariableNode create(GlobalVariableStorage storage) {
        return ReadSimpleGlobalVariableNodeGen.create(storage);
    }

    public ReadSimpleGlobalVariableNode(GlobalVariableStorage storage) {
        this.storage = storage;
    }

    public abstract Object execute();

    @Specialization(assumptions = "storage.getUnchangedAssumption()")
    public Object readConstant(
            @Cached("storage.getValue()") Object value) {
        return value;
    }

    @Specialization
    public Object read() {
        return storage.getValue();
    }

}
