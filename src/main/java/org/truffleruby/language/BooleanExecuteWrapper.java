package org.truffleruby.language;


import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanExecute;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableNode.WrapperNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.NodeCost;

public final class BooleanExecuteWrapper extends RubyNode implements BooleanExecute, WrapperNode {

    @Child private RubyBaseNode delegateNode;
    @Child private ProbeNode probeNode;

    public BooleanExecuteWrapper(RubyBaseNode delegateNode, ProbeNode probeNode) {
        this.delegateNode = delegateNode;
        this.probeNode = probeNode;
    }

    public RubyBaseNode getDelegateNode() {
        return delegateNode;
    }

    public ProbeNode getProbeNode() {
        return probeNode;
    }

    @Override
    public NodeCost getCost() {
        return NodeCost.NONE;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                probeNode.onEnter(frame);
                returnValue = ((RubyNode) delegateNode).execute(frame);
                wasOnReturnExecuted = true;
                probeNode.onReturnValue(frame, returnValue);
                break;
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                } else if (result != null) {
                    returnValue = result;
                    break;
                }
                throw t;
            }
        }
        return returnValue;
    }

    private RubyNode skipCastNode() {
        if (delegateNode instanceof BooleanCastNode) {
            return ((BooleanCastNode) delegateNode).getValue();
        }
        return (RubyNode) delegateNode;
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return skipCastNode().isDefined(frame, language, context);
    }

    @Override
    protected int getSourceCharIndex() {
        return skipCastNode().getSourceCharIndex();
    }

    @Override
    protected void setSourceCharIndex(int sourceCharIndex) {
        skipCastNode().setSourceCharIndex(sourceCharIndex);
    }

    @Override
    protected int getSourceLength() {
        return skipCastNode().getSourceLength();
    }

    @Override
    protected void setSourceLength(int sourceLength) {
        skipCastNode().setSourceLength(sourceLength);
    }

    @Override
    protected byte getFlags() {
        return skipCastNode().getFlags();
    }

    @Override
    protected void setFlags(byte flags) {
        skipCastNode().setFlags(flags);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) {
        boolean returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                probeNode.onEnter(frame);
                returnValue = ((BooleanExecute) delegateNode).executeBoolean(frame);
                wasOnReturnExecuted = true;
                probeNode.onReturnValue(frame, returnValue);
                break;
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                } else if (result != null) {
                    returnValue = (boolean) result;
                    break;
                }
                throw t;
            }
        }
        return returnValue;
    }

    @Override
    public void markAvoidedCast() {
        ((BooleanExecute) delegateNode).markAvoidedCast();
    }

    @Override
    public boolean didAvoidCast() {
        return ((BooleanExecute) delegateNode).didAvoidCast();
    }
}
