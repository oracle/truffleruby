/***** BEGIN LICENSE BLOCK *****
 * Version: EPL 2.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2002-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2004-2006 Charles O Nutter <headius@headius.com>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
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
 ***** END LICENSE BLOCK *****/
package org.truffleruby.collections;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.truffleruby.core.hash.ReHashable;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/**
 * A thread-safe cache removing entries when the value is no longer in use.
 *
 * Callers must hold to the returned value. The entry will stay in the map as long as the value is
 * referenced.
 */
public class WeakValueCache<Key, Value> implements ReHashable {

    private Map<Key, KeyedReference<Key, Value>> map = new ConcurrentHashMap<>();
    private final ReferenceQueue<Value> deadRefs = new ReferenceQueue<>();

    public Value get(Key key) {
        removeStaleEntries();
        final KeyedReference<Key, Value> reference = map.get(key);
        if (reference == null) {
            return null;
        }

        return reference.get();
    }

    /**
     * Returns the value in the cache (existing or added).
     * Similar to a putIfAbsent() but always return the value in the cache.
     */
    @TruffleBoundary
    public Value addInCacheIfAbsent(Key key, Value newValue) {
        removeStaleEntries();

        final KeyedReference<Key, Value> newRef = new KeyedReference<>(newValue, key, deadRefs);

        // Insert the newValue in the cache, only if there isn't an existing mapping for key with a non-stale value
        while (true) {
            final KeyedReference<Key, Value> oldRef = map.putIfAbsent(key, newRef);
            if (oldRef == null) {
                // We added an entry and there was none in the cache
                return newValue;
            } else {
                final Value oldValue = oldRef.get();
                if (oldValue != null) {
                    // A concurrent putIfAbsent() won the race, use the value in the cache
                    return oldValue;
                } else {
                    // A stale entry, replace it with a new entry
                    if (map.replace(key, oldRef, newRef)) {
                        return newValue;
                    } else {
                        // Some other thread replaced or removed the stale entry, try again from the start.
                        continue;
                    }
                }
            }
        }
    }

    public int size() {
        removeStaleEntries();
        return map.size();
    }

    public Collection<Value> values() {
        removeStaleEntries();
        final Collection<Value> values = new ArrayList<>(map.size());

        for (WeakReference<Value> reference : map.values()) {
            final Value value = reference.get();
            if (value != null) {
                values.add(value);
            }
        }

        return values;
    }

    public void rehash() {
        final Map<Key, KeyedReference<Key, Value>> oldMap = this.map;
        this.map = new ConcurrentHashMap<>(oldMap.size());
        for (Entry<Key, KeyedReference<Key, Value>> entry : oldMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
    }

    protected static class KeyedReference<Key, Value> extends WeakReference<Value> {

        private final Key key;

        public KeyedReference(Value object, Key key, ReferenceQueue<? super Value> queue) {
            super(object, queue);
            this.key = key;
        }

    }

    private void removeStaleEntries() {
        KeyedReference<?, ?> ref;
        while ((ref = (KeyedReference<?, ?>) deadRefs.poll()) != null) {
            // Remove key, but only if the entry is still referencing the GC'd value.
            // Another valid entry for key could have been added since the old value was GC'd.
            map.remove(ref.key, ref);
        }
    }
}
