# Mark the gem extension as built
gem_name = ARGV.fetch(0)

require 'rubygems'
require 'fileutils'
spec = Gem::Specification.find_by_name(gem_name)
gem_build_complete_path = spec.gem_build_complete_path
FileUtils.mkdir_p(File.dirname(gem_build_complete_path))
FileUtils.touch(gem_build_complete_path)
