/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Copyright the JRuby team.
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.core.support;

import java.math.BigInteger;

import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.algorithms.Randomizer;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.language.Visibility;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule(value = "Truffle::Randomizer", isClass = true)
public abstract class RandomizerNodes {

    public static RubyRandomizer newRandomizer(RubyContext context) {
        final RubyBignum seed = RandomizerGenSeedNode.randomSeedBignum(context);
        final Randomizer randomizer = RandomizerSetSeedNode.randomFromBignum(seed);
        return new RubyRandomizer(
                context.getCoreLibrary().randomizerClass,
                RubyLanguage.randomizerShape,
                randomizer);
    }

    public static void resetSeed(RubyContext context, RubyRandomizer random) {
        final RubyBignum seed = RandomizerGenSeedNode.randomSeedBignum(context);
        final Randomizer randomizer = RandomizerSetSeedNode.randomFromBignum(seed);
        random.randomizer = randomizer;
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public static abstract class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyRandomizer randomizerAllocate(RubyClass randomizerClass) {
            return new RubyRandomizer(coreLibrary().randomizerClass, RubyLanguage.randomizerShape, new Randomizer());
        }

    }

    @CoreMethod(names = "seed")
    public static abstract class RandomizerSeedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object seed(RubyRandomizer randomizer) {
            return randomizer.randomizer.getSeed();
        }

    }

    @CoreMethod(names = "seed=", required = 1)
    public static abstract class RandomizerSetSeedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyRandomizer setSeed(RubyRandomizer randomizer, long seed) {
            randomizer.randomizer = randomFromLong(seed);
            return randomizer;
        }

        @Specialization
        protected RubyRandomizer setSeed(RubyRandomizer randomizer, RubyBignum seed) {
            randomizer.randomizer = randomFromBignum(seed);
            return randomizer;
        }

        @TruffleBoundary
        public static Randomizer randomFromLong(long seed) {
            long v = Math.abs(seed);
            if (v == (v & 0xffffffffL)) {
                return new Randomizer(seed, (int) v);
            } else {
                int[] ints = new int[2];
                ints[0] = (int) v;
                ints[1] = (int) (v >> 32);
                return new Randomizer(seed, ints);
            }
        }

        public static final int N = 624;

        @TruffleBoundary
        public static Randomizer randomFromBignum(RubyBignum seed) {
            BigInteger big = seed.value;
            if (big.signum() < 0) {
                big = big.abs();
            }
            byte[] buf = big.toByteArray();
            int buflen = buf.length;
            if (buf[0] == 0) {
                buflen -= 1;
            }
            int len = Math.min((buflen + 3) / 4, N);
            int[] ints = bigEndianToInts(buf, len);
            if (len <= 1) {
                return new Randomizer(seed, ints[0]);
            } else {
                return new Randomizer(seed, ints);
            }
        }

        private static int[] bigEndianToInts(byte[] buf, int initKeyLen) {
            int[] initKey = new int[initKeyLen];
            for (int idx = 0; idx < initKey.length; ++idx) {
                initKey[idx] = getIntBigIntegerBuffer(buf, idx);
            }
            return initKey;
        }

        static int getIntBigIntegerBuffer(byte[] src, int loc) {
            int v = 0;
            int idx = src.length - loc * 4 - 1;
            if (idx >= 0) {
                v |= (src[idx--] & 0xff);
                if (idx >= 0) {
                    v |= (src[idx--] & 0xff) << 8;
                    if (idx >= 0) {
                        v |= (src[idx--] & 0xff) << 16;
                        if (idx >= 0) {
                            v |= (src[idx--] & 0xff) << 24;
                        }
                    }
                }
            }
            return v;
        }

    }

    // Generate a random Float, in the range 0...1.0
    @CoreMethod(names = "random_float")
    public static abstract class RandomFloatNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected double randomFloat(RubyRandomizer randomizer) {
            // Logic copied from org.jruby.util.Random
            final Randomizer r = randomizer.randomizer;
            final int a = randomInt(r) >>> 5;
            final int b = randomInt(r) >>> 6;
            return (a * 67108864.0 + b) * (1.0 / 9007199254740992.0);
        }

    }

    // Generate a random Integer, in the range 0...limit
    @CoreMethod(names = "random_integer", required = 1)
    public static abstract class RandomIntNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int randomizerRandInt(RubyRandomizer randomizer, int limit) {
            final Randomizer r = randomizer.randomizer;
            return (int) randInt(r, limit);
        }

        @Specialization
        protected long randomizerRandInt(RubyRandomizer randomizer, long limit) {
            final Randomizer r = randomizer.randomizer;
            return randInt(r, limit);
        }

        @Specialization
        protected Object randomizerRandInt(RubyRandomizer randomizer, RubyBignum limit,
                @Cached("new()") FixnumOrBignumNode fixnumOrBignum) {
            final Randomizer r = randomizer.randomizer;
            return fixnumOrBignum.fixnumOrBignum(randLimitedBignum(r, limit.value));
        }

        @TruffleBoundary
        protected static long randInt(Randomizer r, long limit) {
            return randLimitedFixnumInner(r, limit);
        }

        public static long randLimitedFixnumInner(Randomizer randomizer, long limit) {
            long val;
            if (limit == 0) {
                val = 0;
            } else {
                long mask = makeMask(limit);
                // take care before code cleanup; it might break random sequence compatibility
                retry: while (true) {
                    val = 0;
                    for (int i = 1; 0 <= i; --i) {
                        if (((mask >>> (i * 32)) & 0xffffffffL) != 0) {
                            val |= (randomizer.genrandInt32() & 0xffffffffL) << (i * 32);
                            val &= mask;
                        }
                        if (limit < val) {
                            continue retry;
                        }
                    }
                    break;
                }
            }
            return val;
        }

        private static BigInteger randLimitedBignum(Randomizer randomizer, BigInteger limit) {
            byte[] buf = BigIntegerOps.toByteArray(limit);
            byte[] bytes = new byte[buf.length];
            int len = (buf.length + 3) / 4;
            // take care before code cleanup; it might break random sequence compatibility
            retry: while (true) {
                long mask = 0;
                boolean boundary = true;
                for (int idx = len - 1; 0 <= idx; --idx) {
                    long lim = getIntBigIntegerBuffer(buf, idx) & 0xffffffffL;
                    mask = (mask != 0) ? 0xffffffffL : makeMask(lim);
                    long rnd;
                    if (mask != 0) {
                        rnd = (randomizer.genrandInt32() & 0xffffffffL) & mask;
                        if (boundary) {
                            if (lim < rnd) {
                                continue retry;
                            }
                            if (rnd < lim) {
                                boundary = false;
                            }
                        }
                    } else {
                        rnd = 0;
                    }
                    setIntBigIntegerBuffer(bytes, idx, (int) rnd);
                }
                break;
            }
            return BigIntegerOps.create(bytes);
        }

        private static int getIntBigIntegerBuffer(byte[] src, int loc) {
            int v = 0;
            int idx = src.length - loc * 4 - 1;
            if (idx >= 0) {
                v |= (src[idx--] & 0xff);
                if (idx >= 0) {
                    v |= (src[idx--] & 0xff) << 8;
                    if (idx >= 0) {
                        v |= (src[idx--] & 0xff) << 16;
                        if (idx >= 0) {
                            v |= (src[idx--] & 0xff) << 24;
                        }
                    }
                }
            }
            return v;
        }

        private static void setIntBigIntegerBuffer(byte[] dest, int loc, int value) {
            int idx = dest.length - loc * 4 - 1;
            if (idx >= 0) {
                dest[idx--] = (byte) (value & 0xff);
                if (idx >= 0) {
                    dest[idx--] = (byte) ((value >> 8) & 0xff);
                    if (idx >= 0) {
                        dest[idx--] = (byte) ((value >> 16) & 0xff);
                        if (idx >= 0) {
                            dest[idx--] = (byte) ((value >> 24) & 0xff);
                        }
                    }
                }
            }
        }

        private static long makeMask(long x) {
            x = x | x >>> 1;
            x = x | x >>> 2;
            x = x | x >>> 4;
            x = x | x >>> 8;
            x = x | x >>> 16;
            x = x | x >>> 32;
            return x;
        }

    }

    @CoreMethod(names = "generate_seed", needsSelf = false)
    public static abstract class RandomizerGenSeedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyBignum generateSeed() {
            return randomSeedBignum(getContext());
        }

        private static final int DEFAULT_SEED_CNT = 4;

        @TruffleBoundary
        public static RubyBignum randomSeedBignum(RubyContext context) {
            byte[] seed = context.getRandomSeedBytes(DEFAULT_SEED_CNT * 4);
            final BigInteger bigInteger = new BigInteger(seed).abs();
            return BignumOperations.createBignum(bigInteger);
        }

    }

    @Primitive(name = "randomizer_bytes", lowerFixnum = 1)
    public static abstract class RandomizerBytesPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyString genRandBytes(RubyRandomizer randomizer, int length,
                @Cached MakeStringNode makeStringNode) {
            final Randomizer random = randomizer.randomizer;
            final byte[] bytes = new byte[length];
            int idx = 0;
            for (; length >= 4; length -= 4) {
                int r = random.genrandInt32();
                for (int i = 0; i < 4; ++i) {
                    bytes[idx++] = (byte) (r & 0xff);
                    r >>>= 8;
                }
            }
            if (length > 0) {
                int r = random.genrandInt32();
                for (int i = 0; i < length; ++i) {
                    bytes[idx++] = (byte) (r & 0xff);
                    r >>>= 8;
                }
            }

            return makeStringNode.executeMake(bytes, ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }
    }

    @TruffleBoundary
    private static int randomInt(Randomizer randomizer) {
        return randomizer.genrandInt32();
    }

}
