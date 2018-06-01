# Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

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
    Truffle::System.log :CONFIG,
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

    begin
      original = ORIGINALS.fetch(name).find do |original_path|
        path = "#{original_path}/#{require_path}"
        break path if File.file?(path)
      end
    rescue KeyError
      # Somehow we've encountered a patch on the $LOAD_PATH for a gem that hasn't been registered.
      # RSpec does this, for instance, by taking the $LOAD_PATH and passing it as an "-I" argument to a Ruby
      # subprocess. Since registered patches should always directly precede the library being patched on
      # the $LOAD_PATH, assume this invariant is held and construct the filename to load from the next
      # entry on the $LOAD_PATH. If this invariant isn't held, `require` will simply fail.
      pos = $LOAD_PATH.find_index("#{DIR}/#{name}")
      original_gem_path = $LOAD_PATH[pos + 1]
      ORIGINALS[name] = [original_gem_path]

      retry
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

  # Allows specifying patching outside the context of RubyGems by setting an environment variable value.
  # E.g., setting TRUFFLERUBY_CUSTOM_PATCH="launchy:$PWD/lib,$PWD/spec" will allow patching of files in the 'lib' and 'spec'
  # of a local checkout of the 'launchy' gem.
  def install_local_patches
    custom_patch = Truffle.invoke_primitive :java_get_env, 'TRUFFLERUBY_CUSTOM_PATCH' # Use the primitive here rather than ENV so it works if native is disabled.

    if custom_patch
      name, paths = custom_patch.split(':')
      begin
        Truffle::Patching.insert_patching_dir name, *paths.split(',')
      rescue # rubocop:disable Lint/HandleExceptions
        # We don't want to fail patching just because an environment variable is visible to a process. This might be
        # the case when running with rake where the rake process won't need the patches but the tests it spawns will.
      end
    end
  end

end
