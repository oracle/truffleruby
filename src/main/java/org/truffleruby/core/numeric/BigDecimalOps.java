package org.truffleruby.core.numeric;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import java.math.BigDecimal;

/** Wrapper for methods of {@link BigDecimal} decorated with a {@link TruffleBoundary} annotation, as these methods are
 * blacklisted by SVM. */
public final class BigDecimalOps {

    @TruffleBoundary
    public static int compare(BigDecimal a, BigDecimal b) {
        return a.compareTo(b);
    }
}
