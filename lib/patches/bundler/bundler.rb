Truffle::Patching.require_original __FILE__

unless Bundler::VERSION =~ /^1\.16\./
  raise "unsupported bundler version #{Bundler::VERSION}, please use 1.16.x"
end
