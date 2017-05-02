require "bundler/gem_tasks"
require "rspec/core/rake_task"
require "rake/extensiontask"

Dir['tasks/*.rake'].each { |file| load(file) }

Rake::ExtensionTask.new('oily_png') do |ext|
  ext.lib_dir = File.join('lib', 'oily_png')
  ext.config_options = '--with-cflags="-std=c99"'
end

RSpec::Core::RakeTask.new(:spec) do |task|
  task.pattern = "./spec/**/*_spec.rb"
  task.rspec_opts = ['--color']
end

Rake::Task['spec'].prerequisites << :compile

task :default => [:spec]
