# truffleruby_primitives: true

require Primitive.get_original_require(__FILE__)

version = Bundler::VERSION.split('.').map(&:to_i)

unless (version <=> [1, 16, 5]) >= 0
  raise "unsupported bundler version #{Bundler::VERSION}, please use 1.16.5 or more recent"
end
