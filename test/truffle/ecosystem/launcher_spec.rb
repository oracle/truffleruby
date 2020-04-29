require_relative '../../../spec/ruby/spec_helper'

require 'json'

describe "The launcher" do

  RUBY_HOME = File.expand_path(Truffle::Boot.ruby_home)
  GRAALVM_HOME = Truffle::System.get_java_property('org.graalvm.home')&.tap { |it| File.expand_path(it.to_s) }

  def kind(path)
    case path
    when RUBY_HOME + '/bin'; 'Ruby boot home'
    when GRAALVM_HOME + '/bin'; 'GraalVM home'
    when GRAALVM_HOME + '/jre/bin'; 'GraalVM home (jre/bin)'
    else 'invalid path: ' + path
    end
  end

  before :all do
    @old_cwd = Dir.pwd
    Dir.chdir RUBY_HOME
  end

  after :all do
    Dir.chdir @old_cwd
  end

  versions = JSON.parse(File.read(File.expand_path('../../../../versions.json', __FILE__)))

  LAUNCHERS = { bundle:      /^Bundler version #{versions['gems']['default']['bundler']}$/,
                bundler:     /^Bundler version #{versions['gems']['default']['bundler']}$/,
                gem:         /^#{versions['gems']['default']['gem']}$/,
                irb:         /^irb #{versions['gems']['default']['irb']}/,
                rake:        /^rake, version #{versions['gems']['default']['rake']}/,
                rdoc:        /^#{versions['gems']['default']['rdoc']}$/,
                ri:          /^ri #{versions['gems']['default']['rdoc']}$/,
                ruby:        /truffleruby .* like ruby #{versions['ruby']['version']}/,
                truffleruby: /truffleruby .* like ruby #{versions['ruby']['version']}/ }

  TRUFFLERUBY_S = './truffleruby -S '

  def check_launchers(prefix: '', dir: nil)
    LAUNCHERS.each do |launcher, test|
      next if prefix == TRUFFLERUBY_S && [:ruby, :truffleruby].include?(launcher)
      it "'#{prefix}#{launcher}' (#{kind dir}) works" do
        Dir.chdir(dir ? dir : Dir.pwd) do
          out = `#{prefix}#{launcher} --version`
          out.should =~ test
          $?.success?.should == true
        end
      end
    end
  end

  def check_launchers_in_dir(dir)
    check_launchers(prefix: './', dir: dir)
    check_launchers(prefix: TRUFFLERUBY_S, dir: dir)
  end

  def graalvm_home_check(path='')
    yield (RUBY_HOME + '/bin' + path)
    return if GRAALVM_HOME == nil # TODO test
    yield (GRAALVM_HOME + '/bin' + path)
    if Dir.exist?(GRAALVM_HOME + '/jre/bin')
      yield (GRAALVM_HOME + '/jre/bin' + path)
    end
  end

  graalvm_home_check { |path| check_launchers_in_dir path }

  it " for gem can install the hello-world gem" do
    Dir.chdir(__dir__ + '/hello-world') do
      `"#{RUBY_HOME}/bin/gem" build hello-world.gemspec`
      $?.success?.should == true
      `"#{RUBY_HOME}/bin/gem" install --local hello-world-0.0.1.gem`
      $?.success?.should == true
    end
  end

  graalvm_home_check '/hello-world.rb' do |path|
    it " for gem hello-world (#{kind path}) reports the correct ruby version" do
      version = `#{RUBY_HOME}/bin/ruby -v`
      out = `#{path}`
      out.should == "Hello world! from #{version}"
    end
  end

  it " for gem can uninstall the hello-world gem" do
    `#{RUBY_HOME}/bin/gem uninstall hello-world -x`
    $?.success?.should == true
    graalvm_home_check '/hello-world.rb' do |path|
      File.exist?(path).should == false
    end
  end

  # see doc/contributor/stdlib.md
  bundled_gems= [
      "did_you_mean #{versions['gems']['bundled']['did_you_mean']}",
      "minitest #{versions['gems']['bundled']['minitest']}",
      "net-telnet #{versions['gems']['bundled']['net-telnet']}",
      "power_assert #{versions['gems']['bundled']['power_assert']}",
      "rake #{versions['gems']['bundled']['rake']}",
      "test-unit #{versions['gems']['bundled']['test-unit']}",
      "xmlrpc #{versions['gems']['bundled']['xmlrpc']}",
  ]

  gem_list = `#{RUBY_HOME}/bin/gem list`

  bundled_gems.each do |gem|
    gem_name = gem.split.first
    it "for gem shows that #{gem_name} is installed" do
      gem.gsub! '.', '\\.'
      gem.sub! ' ', '.*'
      gem_list.should =~ Regexp.new(gem)
    end
  end
end
