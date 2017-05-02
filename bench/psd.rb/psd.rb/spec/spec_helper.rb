require './lib/psd'

class PSD
  attr_reader :file
end

RSpec.configure do |config|
  unless ENV['CIRCLECI']
    config.filter_run :focus => true
  end
  config.treat_symbols_as_metadata_keys_with_true_values = true
  config.run_all_when_everything_filtered = true
end
