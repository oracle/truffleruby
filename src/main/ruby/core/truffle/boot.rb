# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

TOPLEVEL_BINDING = binding

module Truffle::Boot

  PATCHING_OPENSSL = Truffle::Boot.get_option 'patching_openssl'
  PATCHING = Truffle::Boot.get_option 'patching'
  raise 'Cannot patch openssl without patching enabled' if PATCHING_OPENSSL and !PATCHING

  def self.patching_openssl_enabled?
    PATCHING_OPENSSL
  end

  def self.check_syntax(source_or_file)
    inner_check_syntax source_or_file
    STDOUT.puts 'Syntax OK'
    0
  rescue SyntaxError => e
    STDERR.puts "SyntaxError in #{e.message}"
    1
  end

  def self.find_s_file(name)
    # Nonstandard lookup

    # added to look up truffleruby own files first when it's not on PATH
    name_in_ruby_home_bin = "#{RbConfig::CONFIG['bindir']}/#{name}"
    return name_in_ruby_home_bin if File.exist?(name_in_ruby_home_bin)

    # Standard lookups

    if ENV['RUBYPATH']
      path = find_in_environment_paths(name, ENV['RUBYPATH']) and return path
    end
    path = find_in_environment_paths(name, ENV['PATH']) and return path
    return name if File.exist?(name)

    # Fail otherwise
    raise LoadError, "No such file or directory -- #{name}"
  end

  def self.find_in_environment_paths(name, env_value)
    env_value.to_s.split(File::PATH_SEPARATOR).each do |path|
      name_in_path = "#{path}/#{name}"
      return name_in_path if File.exist?(name_in_path)
    end
    nil
  end

  private_class_method :find_in_environment_paths

end

