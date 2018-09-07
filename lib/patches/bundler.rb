require 'bundler'

unless Bundler::VERSION.start_with?("1.16.") || Bundler::VERSION.start_with?("2.")
  raise "unsupported bundler version #{Bundler::VERSION}, please use 1.16.x"
end
