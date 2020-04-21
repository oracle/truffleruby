gem_original_require 'bundler/index'

module Bundler
  class Index
    remove_method :search_by_dependency

    # Use Gem::Platform.match_spec?(spec) instead of Gem::Platform.match(spec.platform)
    def search_by_dependency(dependency, base = nil)
      @cache[base || false] ||= {}
      @cache[base || false][dependency] ||= begin
        specs = specs_by_name(dependency.name)
        specs += base if base
        found = specs.select do |spec|
          next true if spec.source.is_a?(Source::Gemspec)
          if base # allow all platforms when searching from a lockfile
            dependency.matches_spec?(spec)
          else
            if Gem::Platform.respond_to? :match_spec?
              dependency.matches_spec?(spec) && Gem::Platform.match_spec?(spec)
            else
              dependency.matches_spec?(spec) && Gem::Platform.match(spec.platform)
            end
          end
        end

        found
      end
    end
  end
end
