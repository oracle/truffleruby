require 'openssl-stubs'

raise 'unsupported bundler version please use 1.14.x' unless Bundler::VERSION =~ /^1\.14\./
