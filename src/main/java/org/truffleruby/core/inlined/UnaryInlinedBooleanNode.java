package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanExecute;
import org.truffleruby.language.BooleanExecuteWrapper;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.instrumentation.ProbeNode;

public abstract class UnaryInlinedBooleanNode extends UnaryInlinedOperationNode implements BooleanExecute {

    private boolean avoidedCast;

    public UnaryInlinedBooleanNode(
            RubyLanguage language,
            RubyCallNodeParameters callNodeParameters,
            Assumption... assumptions) {
        super(language, callNodeParameters, assumptions);
    }

    @Override
    public boolean needsBooleanCastNode() {
        return false;
    }

    public void markAvoidedCast() {
        avoidedCast = true;
    }

    public boolean didAvoidCast() {
        return avoidedCast;
    }

    @Override
    public WrapperNode createWrapper(ProbeNode probe) {
        return new BooleanExecuteWrapper(this, probe);
    }
}
