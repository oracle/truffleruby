package org.truffleruby.language.globals;

import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class WriteSimpleGlobalVariableNode extends RubyBaseNode {

    protected final GlobalVariableStorage storage;
    @Child protected ReferenceEqualNode referenceEqualNode = ReferenceEqualNode.create();
    @Child protected WriteBarrierNode writeBarrierNode = WriteBarrierNode.create();

    public static WriteSimpleGlobalVariableNode create(GlobalVariableStorage storage) {
        return WriteSimpleGlobalVariableNodeGen.create(storage);
    }

    public WriteSimpleGlobalVariableNode(GlobalVariableStorage storage) {
        this.storage = storage;
    }

    public abstract Object execute(Object value);

    @Specialization(guards = "referenceEqualNode.executeReferenceEqual(value, previousValue)", assumptions = "storage.getUnchangedAssumption()")
    public Object writeTryToKeepConstant(Object value,
            @Cached("storage.getValue()") Object previousValue) {
        // NOTE: we still do the volatile write to get the proper memory barrier,
        // as the global variable could be used as a publication mechanism.
        storage.setValueInternal(value);
        return previousValue;
    }

    @Specialization(guards = "storage.isAssumeConstant()", assumptions = "storage.getUnchangedAssumption()")
    public Object writeAssumeConstant(Object value) {
        if (getContext().getSharedObjects().isSharing()) {
            writeBarrierNode.executeWriteBarrier(value);
        }
        storage.setValueInternal(value);
        storage.updateAssumeConstant(getContext());
        return value;
    }

    @Specialization(guards = "!storage.isAssumeConstant()", replaces = "writeAssumeConstant")
    public Object write(Object value) {
        if (getContext().getSharedObjects().isSharing()) {
            writeBarrierNode.executeWriteBarrier(value);
        }
        storage.setValueInternal(value);
        return value;
    }

}
