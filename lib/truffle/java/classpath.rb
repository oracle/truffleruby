# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

class RubyClassLoader
  @@loader = java.lang.ClassLoader.get_system_class_loader


  def self.<<(a_string)
    urls = java.net.URL[1].new
    urls[0] = java.net.URL.new("file:" + a_string)
    @@loader = java.net.URLClassLoader.new(urls, @@loader)
    ::Truffle::Interop::Java.loader = JavaUtilities.unwrap_java_value(@@loader)
    self
  end
end

$CLASSPATH = RubyClassLoader
