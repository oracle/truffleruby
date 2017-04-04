class Gem::RequestSet::GemDependencyAPI

  ENGINE_MAP[:ruby] << 'truffleruby'
  ENGINE_MAP[:ruby_18] << 'truffleruby'
  ENGINE_MAP[:ruby_19] << 'truffleruby'
  ENGINE_MAP[:ruby_20] << 'truffleruby'
  ENGINE_MAP[:ruby_21] << 'truffleruby'
  ENGINE_MAP[:truffleruby] = %w[truffleruby]

  PLATFORM_MAP[:truffleruby] = Gem::Platform::RUBY

end
