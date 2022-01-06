package org.truffleruby.core.cast;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInterface;

public interface BooleanExecute extends NodeInterface {
    boolean executeBoolean(VirtualFrame frame);

    void markAvoidedCast();

    boolean didAvoidCast();
}
