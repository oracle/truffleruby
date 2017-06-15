package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public abstract class OnHeapRope extends Rope {

    protected OnHeapRope(Encoding encoding, CodeRange codeRange, boolean singleByteOptimizable, int byteLength, int characterLength, int ropeDepth, byte[] bytes) {
        super(encoding, codeRange, singleByteOptimizable, byteLength, characterLength, ropeDepth, bytes);
    }

    public final boolean bytesSet() {
        return bytes != null;
    }

    public final byte[] getBytesFast() {
        return bytes;
    }

    @Override
    public final byte[] getBytes() {
        if (bytes == null) {
            bytes = getBytesSlow();
        }

        return bytes;
    }

}
