# frozen_string_literal: true

# Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

module Truffle::GemUtil
  DEFAULT_GEMS = {
    'bigdecimal' => true,
    'bundler' => true,
    'cmath' => true,
    'csv' => true,
    'date' => true,
    'dbm' => true,
    'e2mmap' => true,
    'etc' => true,
    'fcntl' => true,
    'fiddle' => true,
    'fileutils' => true,
    'forwardable' => true,
    'gdbm' => true,
    'ipaddr' => true,
    'io' => true, # gem 'io-console', required as 'io/console'
    'irb' => true,
    'json' => true,
    'logger' => true,
    'matrix' => true,
    'mutex_m' => true,
    'openssl' => true,
    'ostruct' => true,
    'prime' => true,
    'psych' => true,
    'rdoc' => true,
    'rss' => true,
    'rexml' => true,
    'scanf' => true,
    'sdbm' => true,
    'shell' => true,
    'stringio' => true,
    'strscan' => true,
    'sync' => true,
    'thwait' => true,
    'tracer' => true,
    'webrick' => true,
    'zlib' => true
  }

  MARKER_NAME = 'truffleruby_gem_dir_marker.txt'

  def self.upgraded_default_gem?(feature)
    if i = feature.index('/')
      first_component = feature[0...i]
    else
      first_component = feature
    end

    if DEFAULT_GEMS.include?(first_component)
      # No need to check for 'io/nonblock' and 'io/wait', just for 'io/console'
      return false if first_component == 'io' and !feature.start_with?('io/console')

      matcher = "#{first_component}-"
      gem_paths.each do |gem_dir|
        spec_dir = "#{gem_dir}/specifications"
        if File.directory?(spec_dir)
          Dir.each_child(spec_dir) do |spec|
            if spec.start_with?(matcher) and digit = spec[matcher.size] and '0' <= digit && digit <= '9'
              return true
            end
          end
        end
      end
    end

    false
  end

  def self.verify_gem_paths
    bad_dirs = bad_gem_dirs(gem_paths)
    unless bad_dirs.empty?
      warn "[ruby] WARNING gem paths: #{bad_dirs.join ', '} are not marked as installed by TruffleRuby " +
               '(they could belong to another Ruby implementation and break unexpectedly)'
    end
    bad_dirs
  end

  def self.bad_gem_dirs(dirs)
    dirs.reject do |dir|
      specifications = File.join(dir, 'specifications')

      # The path does not exist yet, nothing can be loaded, everything is fine
      !File.directory?(specifications) ||
          # The directory is empty, TruffleRuby could not have marked it, nothing can be loaded, everything is fine
          Dir.empty?(specifications) ||
          # The directory is marked as TruffleRuby's, everything is fine
          File.exist?("#{dir}/#{MARKER_NAME}")
    end
  end

  # Gem.path, without needing to load RubyGems
  def self.gem_paths
    @gem_paths ||= begin
      home = ENV['GEM_HOME'] || "#{Truffle::Boot.ruby_home}/lib/gems"
      paths = [home]

      if gem_path = ENV['GEM_PATH']
        paths.concat gem_path.split(File::PATH_SEPARATOR)
      else
        user_dir = "#{Dir.home}/.gem/truffleruby/#{RUBY_ENGINE_VERSION}"
        paths << user_dir
      end

      paths.map { |path| expand(path) }.uniq
    end
  end

  def self.expand(path)
    if File.directory?(path)
      File.realpath(path)
    else
      path
    end
  end
end
