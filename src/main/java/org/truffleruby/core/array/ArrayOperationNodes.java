package org.truffleruby.core.array;

import org.truffleruby.language.RubyBaseNode;

public class ArrayOperationNodes {

    public static abstract class ArrayLengthNode extends RubyBaseNode {

        public abstract int execute(Object store);
    }

    public static abstract class ArrayGetNode extends RubyBaseNode {

        public abstract Object execute(Object store, int index);
    }

    public static abstract class ArraySetNode extends RubyBaseNode {

        public abstract void execute(Object store, int index, Object vvalue);
    }

    public static abstract class ArrayNewStoreNode extends RubyBaseNode {

        public abstract Object execute(int size);
    }

    public static abstract class ArrayCopyStoreNode extends RubyBaseNode {

        public abstract Object execute(Object store, int size);
    }

    public static abstract class ArrayCopyToNode extends RubyBaseNode {

        public abstract void execute(Object from, Object to, int sourceStart, int destinationStart, int length);
    }

    public static abstract class ArrayExtractRangeNode extends RubyBaseNode {

        public abstract Object execute(Object store, int start, int end);
    }

    public static abstract class ArraySortNode extends RubyBaseNode {

        public abstract void execute(Object store, int size);
    }
}
