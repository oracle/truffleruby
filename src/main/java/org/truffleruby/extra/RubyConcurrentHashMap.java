package org.truffleruby.extra;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;

public class RubyConcurrentHashMap extends RubyDynamicObject {

  final RubyBasicObject options;
  public RubyHash hash;

  public RubyConcurrentHashMap(RubyClass rubyClass, Shape shape, RubyBasicObject options, RubyHash hash) {
      super(rubyClass, shape);
      this.options = options;
      this.hash = hash;
  }
}
