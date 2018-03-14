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
      match: /\b#{struct_var_name}(\.|->)#{field_name}(\s*=\s*)(\w.+);\s*$/ ,
      replacement: "#{struct_var_name}\\1#{field_name}\\2rb_tr_handle_for_managed#{leaking_str}(\\3);"
    }
  end

  def self.read_write_field(struct_var_name, field_name, leaking)
    [read_field(struct_var_name, field_name),
     write_field(struct_var_name, field_name, leaking)]
  end
end
