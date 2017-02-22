# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

ARGF.each do |line|
  if line.include?('rb_scan_args')
    puts line.gsub(/rb_scan_args\((\w+), (\w+), \"(.*)\", /) {
      argc = $1
      argv = $2
      arity = $3

      case arity
        when '0:'
          shim = 'rb_jt_scan_args_0_hash'
        when '02'
          shim = 'rb_jt_scan_args_02'
        when '11'
          shim = 'rb_jt_scan_args_11'
        when '12'
          shim = 'rb_jt_scan_args_12'
        when '1*'
          shim = 'rb_jt_scan_args_1_star'
        else
          shim = 'rb_scan_args' # let it fail at runtime
      end

      "#{shim}(#{argc}, #{argv}, \"#{arity}\", "
    }
  else
    puts line
  end
end
