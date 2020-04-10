require 'bundler/installer/standalone'

# Adds File.expand_path to generated setup.rb paths.
# Patch added to issue found while running rspec-core tests which use bundler standalone feature
# which adds relative paths and the FFI gem manipulates $LOAD_PATHS that don't match these relative paths.
# setup.rb before: $:.unshift "#{path}/../#{ruby_engine}/#{ruby_version}/gems/rake-13.0.1/lib"
# setup.rb after: $:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/rake-13.0.1/lib")
module Bundler
  class Standalone
    def generate
      SharedHelpers.filesystem_access(bundler_path) do |p|
        FileUtils.mkdir_p(p)
      end
      File.open File.join(bundler_path, "setup.rb"), "w" do |file|
        file.puts "require 'rbconfig'"
        file.puts "# ruby 1.8.7 doesn't define RUBY_ENGINE"
        file.puts "ruby_engine = defined?(RUBY_ENGINE) ? RUBY_ENGINE : 'ruby'"
        file.puts "ruby_version = RbConfig::CONFIG[\"ruby_version\"]"
        file.puts "path = File.expand_path('..', __FILE__)"
        paths.each do |path|
          file.puts %($:.unshift File.expand_path("\#{path}/#{path}"))
        end
      end
    end
  end
end
