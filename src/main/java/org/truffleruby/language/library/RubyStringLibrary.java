/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.library;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyGuards;

/** It is important that all messages of this library can be trivially implemented without needing any @Cached state or
 * node. That way, the generated library classes are actually global immutable singletons.
 * <p>
 * Implemented by {@link org.truffleruby.core.string.RubyString} and
 * {@link org.truffleruby.core.string.ImmutableRubyString} */
public abstract class RubyStringLibrary {

    public static RubyStringLibrary create() {
        return new Cached();
    }

    public static RubyStringLibrary getUncached() {
        return Uncached.INSTANCE;
    }

    /** Used to create separate specialization instances for RubyString and ImmutableRubyString */
    public abstract boolean seen(Object object);

    public abstract boolean isRubyString(Object object);

    public abstract AbstractTruffleString getTString(Object object);

    public abstract RubyEncoding getEncoding(Object object);

    public final TruffleString.Encoding getTEncoding(Object object) {
        return getEncoding(object).tencoding;
    }

    public abstract int byteLength(Object object);

    static final class Cached extends RubyStringLibrary {

        @CompilationFinal private boolean seenMutable, seenImmutable, seenOther;
        @CompilationFinal private Object cachedEncoding;

        private static final Object GENERIC = new Object();

        @Override
        public boolean seen(Object object) {
            assert object instanceof RubyString || object instanceof ImmutableRubyString;
            if (seenMutable) {
                return object instanceof RubyString;
            } else if (seenImmutable) {
                return object instanceof ImmutableRubyString;
            } else {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                getTString(object); // specialize
                return true;
            }
        }

        @Override
        public boolean isRubyString(Object object) {
            if (seenMutable && object instanceof RubyString) {
                return true;
            } else if (seenImmutable && object instanceof ImmutableRubyString) {
                return true;
            } else if (seenOther && RubyGuards.isNotRubyString(object)) {
                return false;
            }

            CompilerDirectives.transferToInterpreterAndInvalidate();
            return specializeIsRubyString(object);
        }

        private boolean specializeIsRubyString(Object object) {
            if (object instanceof RubyString) {
                seenMutable = true;
                return true;
            } else if (object instanceof ImmutableRubyString) {
                seenImmutable = true;
                return true;
            } else if (RubyGuards.isNotRubyString(object)) {
                seenOther = true;
                return false;
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        @Override
        public AbstractTruffleString getTString(Object object) {
            if (seenMutable && object instanceof RubyString) {
                return ((RubyString) object).tstring;
            } else if (seenImmutable && object instanceof ImmutableRubyString) {
                return ((ImmutableRubyString) object).tstring;
            }

            CompilerDirectives.transferToInterpreterAndInvalidate();
            return specializeGetTString(object);
        }

        private AbstractTruffleString specializeGetTString(Object object) {
            if (object instanceof RubyString) {
                seenMutable = true;
                return ((RubyString) object).tstring;
            } else if (object instanceof ImmutableRubyString) {
                seenImmutable = true;
                return ((ImmutableRubyString) object).tstring;
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        @Override
        public RubyEncoding getEncoding(Object object) {
            final RubyEncoding encoding;
            if (seenMutable && object instanceof RubyString) {
                encoding = ((RubyString) object).getEncodingUnprofiled();
            } else if (seenImmutable && object instanceof ImmutableRubyString) {
                encoding = ((ImmutableRubyString) object).getEncodingUnprofiled();
            } else {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeGetEncoding(object);
            }

            var localCachedEncoding = this.cachedEncoding;
            if (encoding == localCachedEncoding) {
                return (RubyEncoding) localCachedEncoding;
            } else if (localCachedEncoding == GENERIC) {
                return encoding;
            } else {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                return specializeGetEncoding(object);
            }
        }

        private RubyEncoding specializeGetEncoding(Object object) {
            final RubyEncoding encoding;
            if (object instanceof RubyString) {
                seenMutable = true;
                encoding = ((RubyString) object).getEncodingUnprofiled();
            } else if (object instanceof ImmutableRubyString) {
                seenImmutable = true;
                encoding = ((ImmutableRubyString) object).getEncodingUnprofiled();
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }

            var localCachedEncoding = this.cachedEncoding;
            if (localCachedEncoding == null) {
                this.cachedEncoding = encoding;
            } else if (encoding != localCachedEncoding) {
                this.cachedEncoding = GENERIC;
            }
            return encoding;
        }

        @Override
        public int byteLength(Object object) {
            if (seenMutable && object instanceof RubyString) {
                var mutable = (RubyString) object;
                return getTString(mutable).byteLength(getTEncoding(mutable));
            } else if (seenImmutable && object instanceof ImmutableRubyString) {
                var immutable = (ImmutableRubyString) object;
                return getTString(immutable).byteLength(getTEncoding(immutable));
            }

            CompilerDirectives.transferToInterpreterAndInvalidate();
            return specializeByteLength(object);
        }

        private int specializeByteLength(Object object) {
            // getTString() and getTEncoding() will specialize as needed
            return getTString(object).byteLength(getTEncoding(object));
        }
    }

    static final class Uncached extends RubyStringLibrary {

        static final Uncached INSTANCE = new Uncached();

        @Override
        public boolean seen(Object object) {
            assert object instanceof RubyString || object instanceof ImmutableRubyString;
            return true;
        }

        @Override
        public boolean isRubyString(Object object) {
            CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
            return object instanceof RubyString || object instanceof ImmutableRubyString;
        }

        @Override
        public AbstractTruffleString getTString(Object object) {
            CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
            if (object instanceof RubyString) {
                return ((RubyString) object).tstring;
            } else if (object instanceof ImmutableRubyString) {
                return ((ImmutableRubyString) object).tstring;
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        @Override
        public RubyEncoding getEncoding(Object object) {
            CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
            if (object instanceof RubyString) {
                return ((RubyString) object).getEncodingUncached();
            } else if (object instanceof ImmutableRubyString) {
                return ((ImmutableRubyString) object).getEncodingUncached();
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        @Override
        public int byteLength(Object object) {
            CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
            return getTString(object).byteLength(getTEncoding(object));
        }
    }

}
