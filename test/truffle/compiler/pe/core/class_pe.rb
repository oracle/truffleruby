# Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module ClassPEFixtures
  A         = Class.new
  B         = Class.new A
  AInstance = ClassPEFixtures::A.new
end

example "ClassPEFixtures::B.superclass", ClassPEFixtures::A
example "ClassPEFixtures::A.new.class", ClassPEFixtures::A

# Reporting polymorphism for SingletonClassNode would cause a lot of splitting, and for most usages,
# getting the singleton class is a slow-path operation (e.g., to define class methods).
# Having an uncached version of SingletonClassNode might help for this.
# Always splitting Kernel#singleton_class would be an option, but it doesn't seem worth the cost.
tagged example "ClassPEFixtures::AInstance.singleton_class", ClassPEFixtures::AInstance.singleton_class
