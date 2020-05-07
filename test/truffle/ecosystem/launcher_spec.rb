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

  def graalvm_home_check(path='')
    yield (RUBY_HOME + '/bin' + path)
    return if GRAALVM_HOME == nil # TODO test
    yield (GRAALVM_HOME + '/bin' + path)
    if Dir.exist?(GRAALVM_HOME + '/jre/bin')
      yield (GRAALVM_HOME + '/jre/bin' + path)
    end
  end

  versions = JSON.parse(File.read(File.expand_path('../../../../versions.json', __FILE__)))

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
