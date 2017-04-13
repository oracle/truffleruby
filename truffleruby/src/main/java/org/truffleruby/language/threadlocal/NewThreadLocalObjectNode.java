package org.truffleruby.language.threadlocal;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class NewThreadLocalObjectNode extends RubyNode {

    @Override
    public ThreadLocalObject execute(VirtualFrame frame) {
        return new ThreadLocalObject(getContext());
    }
}
