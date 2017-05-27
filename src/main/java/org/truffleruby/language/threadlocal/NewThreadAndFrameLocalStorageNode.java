package org.truffleruby.language.threadlocal;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class NewThreadAndFrameLocalStorageNode extends RubyNode {

    @Override
    public ThreadAndFrameLocalStorage execute(VirtualFrame frame) {
        return new ThreadAndFrameLocalStorage(getContext());
    }
}
