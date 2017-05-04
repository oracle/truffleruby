package org.truffleruby.language.threadlocal;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.parser.ReadLocalNode;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class SetInThreadAndFrameLocalStorageNode extends RubyNode {

    @Child private RubyNode variableNode;
    @Child private RubyNode writeNode;
    @Child private RubyNode valueNode;
    private final ConditionProfile isStorageProfile = ConditionProfile.createBinaryProfile();

    public SetInThreadAndFrameLocalStorageNode(ReadLocalNode variable, RubyNode value) {
        this.variableNode = variable;
        writeNode = variable.makeWriteNode(new NewThreadAndFrameLocalStorageNode());
        this.valueNode = value;
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return variableNode.isDefined(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object variableObject = variableNode.execute(frame);
        final ThreadAndFrameLocalStorage storage;
        if (isStorageProfile.profile(RubyGuards.isThreadLocal(variableObject))) {
            storage = (ThreadAndFrameLocalStorage) variableObject;
        } else {
            storage = (ThreadAndFrameLocalStorage) writeNode.execute(frame);
        }

        Object result = valueNode.execute(frame);
        storage.set(result);
        return result;
    }
}
