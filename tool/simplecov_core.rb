# Run with --coverage.global=true if you want coverage of the core library
# $ export TRUFFLERUBYOPT="--coverage.global=true --required_libraries=./tool/simplecov_core.rb"
# Then run a hello world, specs, etc

at_exit do
  require 'coverage'
  result = Coverage.result
  require 'rubygems' # specs run with --disable-gems
  require 'simplecov'
  SimpleCov::Result.new(SimpleCov.add_not_loaded_files(result)).format!
end
