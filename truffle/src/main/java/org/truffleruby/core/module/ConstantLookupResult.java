package org.truffleruby.core.module;

import java.util.ArrayList;

import org.truffleruby.RubyContext;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyConstant;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.object.DynamicObject;

public class ConstantLookupResult {

    private final RubyConstant constant;
    private final Assumption[] assumptions;

    public ConstantLookupResult(RubyConstant constant, Assumption assumption) {
        this.constant = constant;
        this.assumptions = new Assumption[]{ assumption };
    }

    public ConstantLookupResult(RubyConstant constant, ArrayList<Assumption> assumptions) {
        this.constant = constant;
        this.assumptions = assumptions.toArray(new Assumption[assumptions.size()]);
    }

    public boolean isFound() {
        return constant != null;
    }

    public boolean isDeprecated() {
        return constant != null && constant.isDeprecated();
    }

    public boolean isVisibleTo(RubyContext context, LexicalScope lexicalScope, DynamicObject module) {
        return constant == null || constant.isVisibleTo(context, lexicalScope, module);
    }

    public RubyConstant getConstant() {
        return constant;
    }

    public Assumption[] getAssumptions() {
        return assumptions;
    }

}
