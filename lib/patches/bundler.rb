require 'bundler'

major, minor, tiny = Bundler::VERSION.split('.').map(&:to_i)

unless (major > 1) || (major == 1 && minor > 16) || (major == 1 && minor == 16 && tiny >= 5)
  raise "unsupported bundler version #{Bundler::VERSION}, please use 1.16.5 or more recent"
end
