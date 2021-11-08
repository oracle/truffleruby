# truffleruby_primitives: true

require Primitive.get_original_require(__FILE__)

module Bundler
  class SpecSet
    # Apply the fix from https://github.com/rubygems/rubygems/pull/4625
    def spec_for_dependency(dep, match_current_platform)
      specs_for_platforms = lookup[dep.name]
      if match_current_platform
        GemHelpers.select_best_platform_match(specs_for_platforms.select{|s| Gem::Platform.match_spec?(s) }, Bundler.local_platform)
      else
        GemHelpers.select_best_platform_match(specs_for_platforms, dep.__platform)
      end
    end
  end
end
