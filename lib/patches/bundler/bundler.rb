require 'openssl-stubs' if Truffle::Boot.patching_openssl_enabled?

Truffle::Patching.require_original __FILE__

raise 'unsupported bundler version please use 1.14.x' unless Bundler::VERSION =~ /^1\.14\./
