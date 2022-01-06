package org.truffleruby.core.inlined;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanExecute;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.Assumption;

public abstract class BinaryInlinedBooleanNode extends BinaryInlinedOperationNode implements BooleanExecute {

    private boolean avoidedCast;

    public BinaryInlinedBooleanNode(
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
}
