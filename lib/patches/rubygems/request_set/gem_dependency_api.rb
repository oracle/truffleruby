require 'rubygems/request_set/gem_dependency_api'

class Gem::RequestSet::GemDependencyAPI
  engine_map = ENGINE_MAP.dup
  engine_map[:ruby] << 'truffleruby'
  engine_map[:ruby_18] << 'truffleruby'
  engine_map[:ruby_19] << 'truffleruby'
  engine_map[:ruby_20] << 'truffleruby'
  engine_map[:ruby_21] << 'truffleruby'
  engine_map[:truffleruby] = %w[truffleruby]
  remove_const :ENGINE_MAP
  ENGINE_MAP = engine_map.freeze

  platform_map = PLATFORM_MAP.dup
  platform_map[:truffleruby] = Gem::Platform::RUBY
  remove_const :PLATFORM_MAP
  PLATFORM_MAP = platform_map.freeze
end
