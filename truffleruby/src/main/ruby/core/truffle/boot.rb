# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

TOPLEVEL_BINDING = binding

module Truffle
  module Boot

    def self.main_s
      $0 = find_s_file(original_input_file)
      load $0
      0
    end

    def self.check_syntax
      inner_check_syntax
      STDOUT.puts 'Syntax OK'
      true
    rescue SyntaxError => e
      STDERR.puts "SyntaxError in #{e.message}"
      false
    end

    def self.find_s_file(name)
      # nonstandard lookup
      # TODO (pitr-ch 25-Apr-2017): to be removed
      name_in_ruby_home_lib = "#{RbConfig::CONFIG['libdir']}/bin/#{name}"
      return name_in_ruby_home_lib if File.exist?(name_in_ruby_home_lib)

      # nonstandard lookup
      # TODO (pitr-ch 25-Apr-2017): remove when bin/executables are no longer using -S executable
      name_in_ruby_home_bin = "#{RbConfig::CONFIG['bindir']}/#{name}"
      return name_in_ruby_home_bin if File.exist?(name_in_ruby_home_bin)

      if ENV['RUBYPATH']
        path = find_in_environment_paths(name, ENV['RUBYPATH']) and return path
      end

      path = find_in_environment_paths(name, ENV['PATH']) and return path

      return name if File.exist?(name)

      raise LoadError.new("No such file or directory -- #{name}")
    end

    private_class_method :find_s_file

    def self.find_in_environment_paths(name, env_value)
      env_value.to_s.split(File::PATH_SEPARATOR).each do |path|
        name_in_path = "#{path}/#{name}"
        return name_in_path if File.exist?(name_in_path)
      end
      nil
    end

    private_class_method :find_in_environment_paths

  end
end

