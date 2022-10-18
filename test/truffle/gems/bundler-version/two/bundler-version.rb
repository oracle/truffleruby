require 'bundler/setup'

require 'json'
versions = File.expand_path('../../../../../versions.json', __dir__)
expected = JSON.load(File.read(versions)).dig('gems', 'default', 'bundler')

p Bundler::VERSION == expected
