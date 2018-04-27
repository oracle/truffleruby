# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

module Truffle::Patching
  extend self

  ORIGINALS = {}

  DIR = "#{Truffle::Boot.ruby_home}/lib/patches"

  PATCHES = {
    'stdlib' => "#{DIR}/stdlib",
    'bundler' => "#{DIR}/bundler",
    'launchy' => "#{DIR}/launchy",
    'rake-compiler' => "#{DIR}/rake-compiler",
    'rspec-core' => "#{DIR}/rspec-core",
    'rspec-support' => "#{DIR}/rspec-support",
    'thread_safe' => "#{DIR}/thread_safe",
  }

  def self.paths_depending_on_home
    raise 'patching: should only have the stdlib path' unless ORIGINALS.size == 1
    [
      DIR,
      *PATCHES.values,
      *ORIGINALS.values[0]
    ]
  end

  def log(name, path)
    Truffle::System.log :PATCH,
                        "patching '#{name}' by inserting directory '#{path}' in LOAD_PATH before the original paths"
  end

  def insert_patching_dir(name, *paths)
    path = PATCHES[name]
    if path
      insertion_point = paths.
          map { |gem_require_path| $LOAD_PATH.index gem_require_path }.
          min
      raise "Could not find paths #{paths} in $LOAD_PATH (#{$LOAD_PATH})" unless insertion_point
      ORIGINALS[name] = paths
      Truffle::Patching.log(name, path)
      $LOAD_PATH.insert insertion_point, path if $LOAD_PATH[insertion_point-1] != path
      true
    else
      false
    end
  end

  def require_original(file)
    relative_path = file[DIR.length+1..-1]
    slash = relative_path.index '/'
    name = relative_path[0...slash]
    require_path = relative_path[slash+1..-1]

    original = ORIGINALS.fetch(name).find do |original_path|
      path = "#{original_path}/#{require_path}"
      break path if File.file?(path)
    end

    Kernel.require original
  end

  def install_gem_activation_hook
    Gem::Specification.class_eval do
      alias_method :activate_without_truffle_patching, :activate

      def activate
        result = activate_without_truffle_patching
        Truffle::Patching.insert_patching_dir name, *full_require_paths
        result
      end
    end
  end

end
