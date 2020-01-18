# frozen_string_literal: true

# Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 2.0, or
# GNU General Public License version 2, or
# GNU Lesser General Public License version 2.1.

Truffle::Boot.delay do
  wd = Truffle::Boot.get_option('working-directory')
  Dir.chdir(wd) unless wd.empty?
end

if Truffle::Boot.ruby_home
  # Always provided features: ruby --disable-gems -e 'puts $"'
  begin
    require 'enumerator'
    require 'thread'
    require 'rational'
    require 'complex'
  rescue LoadError => e
    Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
  end

  if Truffle::Boot.get_option 'rubygems'
    Truffle::Boot.delay do
      if Truffle::Boot.resilient_gem_home?
        ENV.delete 'GEM_HOME'
        ENV.delete 'GEM_PATH'
        ENV.delete 'GEM_ROOT'
      end
    end

    begin
      Truffle::Boot.print_time_metric :'before-rubygems'
      begin
        if Truffle::Boot.get_option('rubygems-lazy')
          require 'truffle/lazy-rubygems'
        else
          Truffle::Boot.delay do
            require 'rubygems'
          end
        end
      ensure
        Truffle::Boot.print_time_metric :'after-rubygems'
      end
    rescue LoadError => e
      Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
    else
      if Truffle::Boot.get_option 'did-you-mean'
        # Load DidYouMean here manually, to avoid loading RubyGems eagerly
        Truffle::Boot.print_time_metric :'before-did-you-mean'
        begin
          $LOAD_PATH << "#{Truffle::Boot.ruby_home}/lib/gems/gems/did_you_mean-1.3.0/lib"
          require 'did_you_mean'
        rescue LoadError => e
          Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
        ensure
          Truffle::Boot.print_time_metric :'after-did-you-mean'
        end
      end
    end
  end
end

# Post-boot patching when using context pre-initialization
if Truffle::Boot.preinitializing?
  old_home = Truffle::Boot.ruby_home
  if old_home
    # We need to fix all paths which capture the image build-time home to point
    # to the runtime home.

    paths_starting_with_home = []
    [$LOAD_PATH, $LOADED_FEATURES].each do |array|
      array.each do |path|
        if path.start_with?(old_home)
          path.replace Truffle::Ropes.flatten_rope(path[old_home.size..-1])
          paths_starting_with_home << path
        else
          raise "Path #{path.inspect} in $LOAD_PATH or $LOADED_FEATURES was expected to start with #{old_home}"
        end
      end
    end
    old_home = nil

    Truffle::Boot.delay do
      new_home = Truffle::Boot.ruby_home
      paths_starting_with_home.each do |path|
        path.replace(new_home + path)
      end
    end
  end
end
