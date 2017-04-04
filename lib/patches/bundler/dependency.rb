class Bundler::Dependency

  # TruffleRuby: add record for truffleruby
  const_set :PLATFORM_MAP,
            PLATFORM_MAP.merge(truffleruby: Gem::Platform::RUBY)

  # TruffleRuby: recompute REVERSE_PLATFORM_MAP
  const_set(:REVERSE_PLATFORM_MAP, {}.tap do |reverse_platform_map|
    Bundler::Dependency::PLATFORM_MAP.each do |key, value|
      reverse_platform_map[value] ||= []
      reverse_platform_map[value] << key
    end

    reverse_platform_map.each { |_, platforms| platforms.freeze }
  end.freeze)

end

