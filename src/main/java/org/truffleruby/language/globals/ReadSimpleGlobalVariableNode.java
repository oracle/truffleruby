package org.truffleruby.language.globals;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class ReadSimpleGlobalVariableNode extends RubyBaseNode {

    protected final String name;

    public static ReadSimpleGlobalVariableNode create(String name) {
        return ReadSimpleGlobalVariableNodeGen.create(name);
    }

    public ReadSimpleGlobalVariableNode(String name) {
        this.name = name;
    }

    public abstract Object execute();

    @Specialization(assumptions = "storage.getUnchangedAssumption()")
    public Object readConstant(
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("storage.getValue()") Object value) {
        return value;
    }

    @Specialization
    public Object read(
            @Cached("getStorage()") GlobalVariableStorage storage) {
        return storage.getValue();
    }

    protected GlobalVariableStorage getStorage() {
        return coreLibrary().getGlobalVariables().getStorage(name);
    }

}
