require "bundler/gem_tasks"
require "rake/extensiontask"
require 'rspec/core/rake_task'

Rake::ExtensionTask.new('psd_native') do |ext|
  ext.lib_dir = File.join('lib', 'psd_native')
  ext.config_options = '--with-cflags="-std=c99"'
end

Rake::Task['spec'].prerequisites << :compile

RSpec::Core::RakeTask.new(:spec)
task :default => :spec