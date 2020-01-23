package org.truffleruby.language;

import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Can be used as a parent of Ruby nodes which need @GenerateUncached. */
@NodeField(name = "sourceCharIndex", type = int.class)
@NodeField(name = "sourceLength", type = int.class)
@NodeField(name = "flags", type = byte.class)
public abstract class UncacheableSourceRubyNode extends RubyNode {

    @Override
    public Object isDefined(VirtualFrame frame) {
        assert !(this instanceof WrapperNode);
        // TODO (pitr-ch 18-Jan-2020): the context should be cached
        return RubyLanguage.getCurrentContext().getCoreStrings().EXPRESSION.createInstance();
    }

}
