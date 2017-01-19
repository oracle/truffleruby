/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.queue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class UnsizedQueue {

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canTake = lock.newCondition();

    private Item addEnd;
    private Item takeEnd;
    private int size;

    public void add(Object item) {
        lock.lock();

        try {
            final Item newItem = new Item(item);
            if (addEnd != null) {
                addEnd.setNextToTake(newItem);
            }
            addEnd = newItem;
            if (takeEnd == null) {
                takeEnd = addEnd;
            }
            size++;
            canTake.signal();
        } finally {
            lock.unlock();
        }
    }

    public Object poll() {
        lock.lock();

        try {
            if (takeEnd == null) {
                return null;
            } else {
                return doTake();
            }
        } finally {
            lock.unlock();
        }
    }

    public Object take() throws InterruptedException {
        lock.lock();

        try {
            while (takeEnd == null) {
                canTake.await();
            }

            return doTake();
        } finally {
            lock.unlock();
        }
    }

    public Object poll(long timeoutMilliseconds) throws InterruptedException {
        lock.lock();

        try {
            if (takeEnd == null) {
                if (!canTake.await(timeoutMilliseconds, TimeUnit.MILLISECONDS)) {
                    return null;
                }
            }

            return doTake();
        } finally {
            lock.unlock();
        }
    }

    private Object doTake() {
        assert lock.isHeldByCurrentThread();
        final Object item = takeEnd.getItem();
        takeEnd = takeEnd.getNextToTake();
        if (takeEnd == null) {
            addEnd = null;
        }
        size--;
        return item;
    }

    public int size() {
        lock.lock();

        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public void clear() {
        lock.lock();

        try {
            addEnd = null;
            takeEnd = null;
            size = 0;
        } finally {
            lock.unlock();
        }
    }

    public int getNumberWaitingToTake() {
        lock.lock();

        try {
            return lock.getWaitQueueLength(canTake);
        } finally {
            lock.unlock();
        }
    }

    public Collection<Object> getContents() {
        lock.lock();

        final Collection<Object> objects = new ArrayList<>();

        try {
            Item iterator = takeEnd;

            while (iterator != null) {
                objects.add(iterator.getItem());
                iterator = iterator.getNextToTake();
            }
        } finally {
            lock.unlock();
        }

        return objects;
    }

    private class Item {

        private final Object item;
        private Item nextToTake;

        public Item(Object item) {
            this.item = item;
        }

        public Object getItem() {
            return item;
        }

        public void setNextToTake(Item nextToTake) {
            this.nextToTake = nextToTake;
        }

        public Item getNextToTake() {
            return nextToTake;
        }

    }

}
