package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public abstract class ManagedRope extends Rope {

    protected ManagedRope(Encoding encoding, CodeRange codeRange, boolean singleByteOptimizable, int byteLength, int characterLength, int ropeDepth, byte[] bytes) {
        super(encoding, codeRange, singleByteOptimizable, byteLength, characterLength, ropeDepth, bytes);
    }

    @Override
    public final byte[] getBytes() {
        if (bytes == null) {
            bytes = getBytesSlow();
        }

        return bytes;
    }

}
