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
    'benchmark' => true,
    'bigdecimal' => true,
    'bundler' => true,
    'cgi' => true,
    'csv' => true,
    'date' => true,
    'dbm' => true,
    'delegate' => true,
    'did_you_mean' => true,
    'etc' => true,
    'fcntl' => true,
    'fiddle' => true,
    'fileutils' => true,
    'forwardable' => true,
    'gdbm' => true,
    'getoptlong' => true,
    'io' => true, # gem 'io-console', required as 'io/console'
    'ipaddr' => true,
    'irb' => true,
    'json' => true,
    'logger' => true,
    'matrix' => true,
    'mutex_m' => true,
    'net' => true, # gem 'net-pop', 'net-smtp', required as 'net/pop', 'net/smtp'
    'observer' => true,
    'open3' => true,
    'openssl' => true,
    'ostruct' => true,
    'prime' => true,
    'pstore' => true,
    'psych' => true,
    'racc' => true,
    'rdoc' => true,
    'readline' => true,
    'reline' => true,
    'rexml' => true,
    'rss' => true,
    'sdbm' => true,
    'singleton' => true,
    'stringio' => true,
    'strscan' => true,
    'timeout' => true,
    'tracer' => true,
    'uri' => true,
    'webrick' => true,
    'yaml' => true,
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
      warn "[ruby] WARNING gem paths: #{bad_dirs.join ', '} are not marked as installed by TruffleRuby. " +
               'They might belong to another Ruby implementation and break unexpectedly. ' +
               'Configure your Ruby manager to use TruffleRuby, or `unset GEM_HOME GEM_PATH`. ' +
               'See https://github.com/oracle/truffleruby/blob/master/doc/user/ruby-managers.md'
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
          Truffle::FileOperations.exist?("#{dir}/#{MARKER_NAME}")
    end
  end

  # Gem.path, without needing to load RubyGems
  def self.gem_paths
    @gem_paths ||= compute_gem_path
  end

  def self.compute_gem_path
    user_dir = "#{Dir.home}/.gem/truffleruby/#{abi_version}"
    default_dir = "#{Truffle::Boot.ruby_home}/lib/gems"
    home = ENV['GEM_HOME'] || default_dir
    # There is also vendor_dir, but it does not exist on TruffleRuby
    default_path = [user_dir, default_dir, home]

    if gem_path = ENV['GEM_PATH']
      paths = gem_path.split(File::PATH_SEPARATOR)
      if gem_path.end_with?(File::PATH_SEPARATOR)
        paths += default_path
      else
        paths << home
      end
    else
      paths = default_path
    end

    paths.map { |path| expand(path) }.uniq
  end

  def self.abi_version
    @abi_version ||= "#{RUBY_VERSION}.#{Truffle::Boot.basic_abi_version}".freeze
  end

  def self.expand(path)
    if File.directory?(path)
      File.realpath(path)
    else
      path
    end
  end
end
