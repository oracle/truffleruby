package org.truffleruby.core.array;

public class DelegatedArrayStorage {
    public Object storage;
    public int offset;
    public int length;

    public DelegatedArrayStorage(Object storage, int offset, int length) {
        assert offset >= 0;
        assert length >= 0;
        this.storage = storage;
        this.offset = offset;
        this.length = length;
    }
}
