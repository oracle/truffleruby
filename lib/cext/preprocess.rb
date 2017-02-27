# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

LOCAL = /\w+\s*(\[\s*\d+\s*\])?/
VALUE_LOCALS = /^(\s+)VALUE\s*(#{LOCAL}(\s*,\s*#{LOCAL})*);\s*$/

def preprocess(line)
  if line.include?('rb_scan_args')
    line.gsub(/\brb_scan_args\((\w+), (\w+), \"(.*)\", /) {
      # Translate
      #   rb_scan_args(argc, argv, "11", &v1, &v2)
      # into
      #   rb_jt_scan_args_11(argc, argv, "11", &v1, &v2)

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
          shim = 'rb_scan_args' # use the macro
      end

      "#{shim}(#{argc}, #{argv}, \"#{arity}\", "
    }
  elsif line =~ VALUE_LOCALS
    # Translate
    #   VALUE args[6], failed, a1, a2, a3, a4, a5, a6;
    #  into
    #   VALUE failed, a1, a2, a3, a4, a5, a6; VALUE *args = truffle_managed_malloc(6 * sizeof(VALUE));

    simple = []
    arrays = []

    line = $1

    $2.split(',').each do |local|
      local.strip!
      if local.end_with?(']')
        raise unless local =~ /(\w+)\s*\[\s*(\d+)\s*\]/
        arrays << [$1, $2.to_i]
      else
        simple << local
      end
    end

    unless simple.empty?
      line += "VALUE #{simple.join(', ')};"
    end

    arrays.each do |name, size|
      line += " VALUE *#{name} = truffle_managed_malloc(#{size} * sizeof(VALUE));"
    end

    line
  else
    line
  end
end

if __FILE__ == $0
  ARGF.each do |line|
    puts preprocess(line)
  end
end

