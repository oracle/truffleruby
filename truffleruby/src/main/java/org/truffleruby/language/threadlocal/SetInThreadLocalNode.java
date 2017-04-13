package org.truffleruby.language.threadlocal;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.parser.ReadLocalNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class SetInThreadLocalNode extends RubyNode {

    @Child private RubyNode variableNode;
    @Child private RubyNode writeNode;
    @Child private RubyNode valueNode;

    public SetInThreadLocalNode(ReadLocalNode variable, RubyNode value) {
        this.variableNode = variable;
        writeNode = variable.makeWriteNode(new NewThreadLocalObjectNode());
        this.valueNode = value;
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return variableNode.isDefined(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object variableObject = variableNode.execute(frame);
        final ThreadLocalObject threadLocal;
        if (RubyGuards.isThreadLocal(variableObject)) {
            threadLocal = (ThreadLocalObject) variableObject;
        } else {
            threadLocal = (ThreadLocalObject) writeNode.execute(frame);
        }

        Object result = valueNode.execute(frame);
        threadLocal.set(result);
        return result;
    }
}
