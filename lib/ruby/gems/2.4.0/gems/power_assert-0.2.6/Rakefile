require "bundler/gem_tasks"

require "rake/testtask"
task :default => :test
Rake::TestTask.new do |t|
  # helper(simplecov) must be required before loading power_assert
  t.ruby_opts = ["-w", "-r./test/helper"]
  t.test_files = FileList["test/test_*.rb"]
end
