# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

class CommonPatches

  NO_ASSIGNMENT = /(?:[\),;]|==|!=)/

  def self.read_array(name)
    {
      match: /\b#{name}\[(\w+)\](\s*#{NO_ASSIGNMENT})/,
      replacement: "rb_tr_managed_from_handle(#{name}[\\1])\\2"
    }
  end

  def self.write_array(name, leaking)
    leaking_str = leaking ? '_leaking': ''
    {
      match: /#{name}\[(\w+)\](\s*=\s*)(\w.+);\s*$/,
      replacement: "#{name}[\\1]\\2rb_tr_handle_for_managed#{leaking_str}(\\3);"
    }
  end

  def self.read_write_array(name, leaking)
    [read_array(name),
     write_array(name, leaking)]
  end

  def self.read_field(struct_var_name, field_name)
    {
      match: /\b#{struct_var_name}(\.|->)#{field_name}(\s*#{NO_ASSIGNMENT})/,
      replacement: "rb_tr_managed_from_handle(#{struct_var_name}\\1#{field_name})\\2"
    }
  end

  def self.write_field(struct_var_name, field_name, leaking)
    leaking_str = leaking ? '_leaking': ''
    {
      match: /\b#{struct_var_name}(\.|->)#{field_name}(\s*=\s*)(\w.+);\s*(\\\s*)?$/ ,
      replacement: "#{struct_var_name}\\1#{field_name}\\2rb_tr_handle_for_managed#{leaking_str}(\\3);\\4"
    }
  end

  def self.read_write_field(struct_var_name, field_name, leaking)
    [read_field(struct_var_name, field_name),
     write_field(struct_var_name, field_name, leaking)]
  end

  def self.replace_reference_passing_with_array(var_name)
    [
      {
        match: /VALUE\s+#{var_name};/,
        replacement: "VALUE #{var_name}[1];"
      },
      {
        match: /([^&*])#{var_name}([);,])/,
        replacement: "\\1#{var_name}[0]\\2"
      },
      {
        match: "&#{var_name}",
        replacement: "#{var_name}"
      }
    ]
  end
end
