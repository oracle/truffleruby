# Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

errnos = File.readlines("#{__dir__}/known_errors.def").map(&:chomp)

c = <<EOC
#include <errno.h>
#include <string.h>
#include <stdio.h>

int main() {
EOC

errnos.each do |errno|
  c << <<-EOC
  #ifdef #{errno}
  printf("#{errno} = %s\\n", strerror(#{errno}));
  #else
  printf("#{errno} = TODO: Unknown description for #{errno}\\n");
  #endif
  EOC
end

c << <<EOC
  return 0;
}
EOC

file = 'strerror.c'
File.write file, c
system 'gcc', file
out = `./a.out`
out.lines.map do |line|
  errno, description = line.chomp.split(' = ')
  puts "map.put(#{errno.inspect}, #{description.inspect});"
end
