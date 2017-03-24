# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module JavaUtilities

  HASH_MAP_CLASS = Java.java_class_by_name('java.util.concurrent.ConcurrentHashMap')
  
  CLASS_NEW_INSTANCE = Java.get_java_method(
    JAVA_CLASS_CLASS, 'newInstance', false, JAVA_OBJECT_CLASS)
  
  HASH_MAP_GET = Java.get_java_method(
    HASH_MAP_CLASS, 'get', false, JAVA_OBJECT_CLASS, JAVA_OBJECT_CLASS)
  
  HASH_MAP_PUT_IF_ABSENT = Java.get_java_method(
    HASH_MAP_CLASS, 'putIfAbsent', false, JAVA_OBJECT_CLASS, JAVA_OBJECT_CLASS, JAVA_OBJECT_CLASS)
  
  class ClassCache

    def initialize
      @cache = Java.invoke_java_method(
         CLASS_NEW_INSTANCE, HASH_MAP_CLASS)
    end

    def [](key)
      Java.invoke_java_method(HASH_MAP_GET, @cache, key)
    end

    def put_if_absent(key, value)
      Java.invoke_java_method(HASH_MAP_PUT_IF_ABSENT, @cache, key, value)
    end
      
  end

  private_constant :ClassCache

  PROXIES = ClassCache.new

  private_constant :PROXIES

end
