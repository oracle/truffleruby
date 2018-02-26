# Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

Truffle::Boot.delay do
  wd = Truffle::Boot.get_option('working_directory')
  Dir.chdir(wd) unless wd.empty?

  unless Truffle::Boot.ruby_home.nil?
    # Always provided features: ruby --disable-gems -e 'puts $"'
    begin
      require 'enumerator'
      require 'thread'
      require 'rational'
      require 'complex'
      require 'unicode_normalize'
      if Truffle::Boot.get_option('patching')
        Truffle::Boot.print_time_metric :'before-patching'
        require 'truffle/patching'
        Truffle::Patching.insert_patching_dir 'stdlib', "#{Truffle::Boot.ruby_home}/lib/mri"
        Truffle::Boot.print_time_metric :'after-patching'
      end
    rescue LoadError => e
      Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
    end

    if Truffle::Boot.get_option 'rubygems'
      if Truffle::Boot.resilient_gem_home?
        ENV.delete 'GEM_HOME'
        ENV.delete 'GEM_PATH'
        ENV.delete 'GEM_ROOT'
      end

      begin
        Truffle::Boot.print_time_metric :'before-rubygems'
        begin
          if Truffle::Boot.get_option('rubygems.lazy')
            require 'truffle/lazy-rubygems'
          else
            require 'rubygems'
          end
        ensure
          Truffle::Boot.print_time_metric :'after-rubygems'
        end
      rescue LoadError => e
        Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
      else
        # TODO (pitr-ch 17-Feb-2017): remove the warning when we can integrate with ruby managers
        if gem_home = ENV['GEM_HOME']
          bad_gem_home = false

          # rbenv does not set GEM_HOME
          # rbenv-gemset has to be installed which does set GEM_HOME, it's in the subdir of Truffle::Boot.ruby_home
          # rbenv/versions/<ruby>/gemsets
          bad_gem_home ||= gem_home.include?('rbenv/versions') && !gem_home.include?('rbenv/versions/truffleruby')

          # rvm stores gems at .rvm/gems/<ruby>@<gemset-name>
          bad_gem_home ||= gem_home.include?('rvm/gems') && !gem_home.include?('rvm/gems/truffleruby')

          # chruby stores gem in ~/.gem/<ruby>/<version>
          bad_gem_home ||= gem_home.include?('.gem') && !gem_home.include?('.gem/truffleruby')

          warn "[ruby] WARN A nonstandard GEM_HOME is set #{gem_home}" if $VERBOSE || bad_gem_home
          if bad_gem_home
            warn "[ruby] WARN The bad GEM_HOME may come from a ruby manager, make sure you've called one of: " +
                     '`rvm use system`, `rbenv system`, or `chruby system` to clear the environment.'
          end
        end

        if Truffle::Boot.get_option 'did_you_mean'
          Truffle::Boot.print_time_metric :'before-did-you-mean'
          begin
            $LOAD_PATH << "#{Truffle::Boot.ruby_home}/lib/ruby/gems/2.3.0/gems/did_you_mean-1.0.0/lib"
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
end
