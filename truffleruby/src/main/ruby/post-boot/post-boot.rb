# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
# code is released under a tri EPL/GPL/LGPL license. You can use it,
# redistribute it and/or modify it under the terms of the:
#
# Eclipse Public License version 1.0
# GNU General Public License version 2
# GNU Lesser General Public License version 2.1

# Always provided features: ruby --disable-gems -e 'puts $"'
begin
  require 'enumerator'
  require 'thread'
  require 'rational'
  require 'complex'
  require 'unicode_normalize'
rescue LoadError => e
  Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
end

if Truffle::Boot.rubygems_enabled?
  begin
    require 'rubygems'
  rescue LoadError => e
    Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
  else
    # TODO (pitr-ch 17-Feb-2017): remove the warning when we can integrate with ruby managers
    unless Gem.dir.include?(Truffle::Boot.ruby_home)
      bad_gem_home = false

      # rbenv does not set GEM_HOME
      # rbenv-gemset has to be installed which does set GEM_HOME, it's in the subdir of Truffle::Boot.ruby_home
      # rbenv/versions/<ruby>/gemsets
      bad_gem_home ||= Gem.dir.include?('rbenv/versions') && !Gem.dir.include?('rbenv/versions/truffleruby')

      # rvm stores gems at .rvm/gems/<ruby>@<gemset-name>
      bad_gem_home ||= Gem.dir.include?('rvm/gems') && !Gem.dir.include?('rvm/gems/truffleruby')

      # chruby stores gem in ~/.gem/<ruby>/<version>
      bad_gem_home ||= Gem.dir.include?('.gem') && !Gem.dir.include?('.gems/truffleruby')

      warn "[ruby] WARN A nonstandard GEM_HOME is set #{Gem.dir}" if $VERBOSE || bad_gem_home
      if bad_gem_home
        warn "[ruby] WARN The bad GEM_HOME may come from a ruby manager, make sure you've called one of: " +
                 '`rvm use system`, `rbenv system`, or `chruby system` to clear the environment.'
      end
    end

    require 'truffle/patching'

    if Truffle::Boot.did_you_mean_enabled?
      begin
        gem 'did_you_mean'
        require 'did_you_mean'
      rescue Gem::LoadError, LoadError => e # rubocop:disable Lint/ShadowedException
        Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
      end
    end
  end
end
