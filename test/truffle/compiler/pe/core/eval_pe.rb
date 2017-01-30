# Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
# 
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

tagged example "eval('14')", 14

tagged example "eval('14 + 2')", 16

tagged example "eval('[1, 2, 3]')[1]", 2

tagged example "eval([1, 2, 3].inspect)[1]", 2

tagged counter example "eval(rand.to_s)"
