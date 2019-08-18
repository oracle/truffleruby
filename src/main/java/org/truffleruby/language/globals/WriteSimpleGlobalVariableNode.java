package org.truffleruby.language.globals;

import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class WriteSimpleGlobalVariableNode extends RubyBaseNode {

    protected final String name;
    @Child protected ReferenceEqualNode referenceEqualNode = ReferenceEqualNode.create();
    @Child protected WriteBarrierNode writeBarrierNode = WriteBarrierNode.create();

    public static WriteSimpleGlobalVariableNode create(String name) {
        return WriteSimpleGlobalVariableNodeGen.create(name);
    }

    public WriteSimpleGlobalVariableNode(String name) {
        this.name = name;
    }

    public abstract Object execute(Object value);

    @Specialization(guards = "referenceEqualNode.executeReferenceEqual(value, previousValue)", assumptions = { "storage.getUnchangedAssumption()", "storage.getValidAssumption()" })
    protected Object writeTryToKeepConstant(Object value,
            @Cached("getStorage()") GlobalVariableStorage storage,
            @Cached("storage.getValue()") Object previousValue) {
        // NOTE: we still do the volatile write to get the proper memory barrier,
        // as the global variable could be used as a publication mechanism.
        storage.setValueInternal(value);
        return previousValue;
    }

    @Specialization(guards = "storage.isAssumeConstant()", assumptions = { "storage.getUnchangedAssumption()", "storage.getValidAssumption()" })
    protected Object writeAssumeConstant(Object value,
            @Cached("getStorage()") GlobalVariableStorage storage) {
        if (getContext().getSharedObjects().isSharing()) {
            writeBarrierNode.executeWriteBarrier(value);
        }
        storage.setValueInternal(value);
        storage.updateAssumeConstant(getContext());
        return value;
    }

    @Specialization(guards = "!storage.isAssumeConstant()", assumptions = "storage.getValidAssumption()", replaces = "writeAssumeConstant")
    protected Object write(Object value,
            @Cached("getStorage()") GlobalVariableStorage storage) {
        if (getContext().getSharedObjects().isSharing()) {
            writeBarrierNode.executeWriteBarrier(value);
        }
        storage.setValueInternal(value);
        return value;
    }

    protected GlobalVariableStorage getStorage() {
        return coreLibrary().getGlobalVariables().getStorage(name);
    }

}
