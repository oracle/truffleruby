require 'bundler'

unless /^(?<major>\d+)\.(?:(?<minor>\d+)\.(?<tiny>\d+))?/ =~ Bundler::VERSION
  raise "could not parse Bundler::VERSION #{Bundler::VERSION}"
end

major = major.to_i
minor = minor.to_i
tiny = tiny.to_i

unless (major > 1) || (major == 1 && minor > 16) || (major == 1 && minor == 16 && tiny >= 5)
  raise "unsupported bundler version #{Bundler::VERSION}, please use 1.16.5 or more recent"
end
