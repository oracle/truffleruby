/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.language.objects.AllocationTracing;

public abstract class ArrayLiteralNode extends RubyContextSourceNode {

    public static ArrayLiteralNode create(RubyLanguage language, RubyNode[] values) {
        return new UninitialisedArrayLiteralNode(language, values);
    }

    @Children protected final RubyNode[] values;
    protected final RubyLanguage language;

    public ArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
        this.language = language;
        this.values = values;
    }

    protected RubyArray makeGeneric(VirtualFrame frame, Object[] alreadyExecuted) {
        final ArrayLiteralNode newNode = new ObjectArrayLiteralNode(language, values);
        newNode.unsafeSetSourceSection(getSourceIndexLength());
        replace(newNode);

        final Object[] executedValues = new Object[values.length];

        for (int n = 0; n < values.length; n++) {
            if (n < alreadyExecuted.length) {
                executedValues[n] = alreadyExecuted[n];
            } else {
                executedValues[n] = values[n].execute(frame);
            }
        }

        return cachedCreateArray(executedValues, executedValues.length);
    }

    protected RubyArray cachedCreateArray(Object store, int size) {
        final RubyArray array = createArray(store, size);
        AllocationTracing.trace(array, this);
        return array;
    }

    @Override
    public abstract RubyArray execute(VirtualFrame frame);

    @ExplodeLoop
    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        for (RubyNode value : values) {
            value.doExecuteVoid(frame);
        }
    }

    @ExplodeLoop
    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        for (RubyNode value : values) {
            if (value.isDefined(frame, language, context) == nil) {
                return nil;
            }
        }

        return super.isDefined(frame, language, context);
    }

    public int getSize() {
        return values.length;
    }

    public RubyNode stealNode(int index) {
        final RubyNode node = values[index];
        // Nullify it here so we make sure it's only referenced by the caller.
        values[index] = null;
        return node;
    }

    private static class EmptyArrayLiteralNode extends ArrayLiteralNode {

        public EmptyArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
            super(language, values);
        }

        @Override
        public RubyArray execute(VirtualFrame frame) {
            return cachedCreateArray(ArrayStoreLibrary.initialStorage(false), 0);
        }

    }

    private static class FloatArrayLiteralNode extends ArrayLiteralNode {

        public FloatArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
            super(language, values);
        }

        @ExplodeLoop
        @Override
        public RubyArray execute(VirtualFrame frame) {
            final double[] executedValues = new double[values.length];

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n].execute(frame);
                if (value instanceof Double) {
                    executedValues[n] = (double) value;
                } else {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    return makeGeneric(frame, executedValues, n, value);
                }
            }

            return cachedCreateArray(executedValues, values.length);
        }

        private RubyArray makeGeneric(VirtualFrame frame, final double[] executedValues, int n, Object value) {
            final Object[] executedObjects = new Object[n + 1];

            for (int i = 0; i < n; i++) {
                executedObjects[i] = executedValues[i];
            }
            executedObjects[n] = value;

            return makeGeneric(frame, executedObjects);
        }

    }

    private static class IntegerArrayLiteralNode extends ArrayLiteralNode {

        public IntegerArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
            super(language, values);
        }

        @ExplodeLoop
        @Override
        public RubyArray execute(VirtualFrame frame) {
            final int[] executedValues = new int[values.length];

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n].execute(frame);
                if (value instanceof Integer) {
                    executedValues[n] = (int) value;
                } else {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    return makeGeneric(frame, executedValues, n, value);
                }
            }

            return cachedCreateArray(executedValues, values.length);
        }

        private RubyArray makeGeneric(VirtualFrame frame, final int[] executedValues, int n, Object value) {
            final Object[] executedObjects = new Object[n + 1];

            for (int i = 0; i < n; i++) {
                executedObjects[i] = executedValues[i];
            }
            executedObjects[n] = value;

            return makeGeneric(frame, executedObjects);
        }

    }

    private static class LongArrayLiteralNode extends ArrayLiteralNode {

        public LongArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
            super(language, values);
        }

        @ExplodeLoop
        @Override
        public RubyArray execute(VirtualFrame frame) {
            final long[] executedValues = new long[values.length];

            for (int n = 0; n < values.length; n++) {
                final Object value = values[n].execute(frame);
                if (value instanceof Long) {
                    executedValues[n] = (long) value;
                } else {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    return makeGeneric(frame, executedValues, n, value);
                }
            }

            return cachedCreateArray(executedValues, values.length);
        }

        private RubyArray makeGeneric(VirtualFrame frame, final long[] executedValues, int n, Object value) {
            final Object[] executedObjects = new Object[n + 1];

            for (int i = 0; i < n; i++) {
                executedObjects[i] = executedValues[i];
            }
            executedObjects[n] = value;

            return makeGeneric(frame, executedObjects);
        }

    }

    private static class ObjectArrayLiteralNode extends ArrayLiteralNode {

        public ObjectArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
            super(language, values);
        }

        @ExplodeLoop
        @Override
        public RubyArray execute(VirtualFrame frame) {
            final Object[] executedValues = new Object[values.length];

            for (int n = 0; n < values.length; n++) {
                executedValues[n] = values[n].execute(frame);
            }

            return cachedCreateArray(executedValues, values.length);
        }

    }

    private static class UninitialisedArrayLiteralNode extends ArrayLiteralNode {

        public UninitialisedArrayLiteralNode(RubyLanguage language, RubyNode[] values) {
            super(language, values);
        }

        @Override
        public RubyArray execute(VirtualFrame frame) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            final Object[] executedValues = new Object[values.length];

            for (int n = 0; n < values.length; n++) {
                executedValues[n] = values[n].execute(frame);
            }

            final RubyArray array = cachedCreateArray(
                    storeSpecialisedFromObjects(executedValues),
                    executedValues.length);
            final Object store = array.getStore();

            final RubyNode newNode;

            if (store == ArrayStoreLibrary.initialStorage(false)) {
                newNode = new EmptyArrayLiteralNode(language, values);
            } else if (store instanceof int[]) {
                newNode = new IntegerArrayLiteralNode(language, values);
            } else if (store instanceof long[]) {
                newNode = new LongArrayLiteralNode(language, values);
            } else if (store instanceof double[]) {
                newNode = new FloatArrayLiteralNode(language, values);
            } else {
                newNode = new ObjectArrayLiteralNode(language, values);
            }

            newNode.unsafeSetSourceSection(getSourceIndexLength());
            replace(newNode);

            return array;
        }

        public Object storeSpecialisedFromObjects(Object... objects) {
            if (objects.length == 0) {
                return ArrayStoreLibrary.initialStorage(false);
            }

            boolean canUseInteger = true;
            boolean canUseLong = true;
            boolean canUseDouble = true;

            for (Object object : objects) {
                if (object instanceof Integer) {
                    canUseDouble = false;
                } else if (object instanceof Long) {
                    canUseInteger = canUseInteger && CoreLibrary.fitsIntoInteger((long) object);
                    canUseDouble = false;
                } else if (object instanceof Double) {
                    canUseInteger = false;
                    canUseLong = false;
                } else {
                    canUseInteger = false;
                    canUseLong = false;
                    canUseDouble = false;
                }
            }

            if (canUseInteger) {
                final int[] store = new int[objects.length];

                for (int n = 0; n < objects.length; n++) {
                    final Object object = objects[n];
                    if (object instanceof Integer) {
                        store[n] = (int) object;
                    } else if (object instanceof Long) {
                        store[n] = (int) (long) object;
                    } else {
                        throw CompilerDirectives.shouldNotReachHere();
                    }
                }

                return store;
            } else if (canUseLong) {
                final long[] store = new long[objects.length];

                for (int n = 0; n < objects.length; n++) {
                    final Object object = objects[n];
                    if (object instanceof Integer) {
                        store[n] = (int) object;
                    } else if (object instanceof Long) {
                        store[n] = (long) object;
                    } else {
                        throw CompilerDirectives.shouldNotReachHere();
                    }
                }

                return store;
            } else if (canUseDouble) {
                final double[] store = new double[objects.length];

                for (int n = 0; n < objects.length; n++) {
                    store[n] = CoreLibrary.toDouble(objects[n], nil);
                }

                return store;
            } else {
                return objects;
            }
        }

    }
}
