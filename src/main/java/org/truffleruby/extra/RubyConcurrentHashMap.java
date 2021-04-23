/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.RubyContext;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;

import java.util.concurrent.ConcurrentHashMap;

public class RubyConcurrentHashMap extends RubyDynamicObject {

   public static class Key {

       public final Object key;

       public Key(Object key) {
           this.key = key;
       }

       @Override
       public int hashCode() {
           final Object code = RubyContext.send(key, "hash");
           if (code instanceof Integer) {
               return (int) code;
           } else if (code instanceof Long) {
               return (int) (long) code;
           } else {
               throw new UnsupportedOperationException(code.getClass().getName());
           }
       }

       @Override
       public boolean equals(Object otherKey) {
           assert otherKey instanceof Key;
           final Object code = RubyContext.send(key, "eql?", ((Key) otherKey).key);
           if (code instanceof Boolean) {
               return (boolean) code;
           } else {
               throw new UnsupportedOperationException(code.getClass().getName());
           }
       }
   }

  final RubyBasicObject options;
  public ConcurrentHashMap<Key, Object> concurrentHash = new ConcurrentHashMap<>();

  public RubyConcurrentHashMap(RubyClass rubyClass, Shape shape, RubyBasicObject options) {
      super(rubyClass, shape);
      this.options = options;
  }
}
