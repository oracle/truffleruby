# truffleruby_primitives: true

require Primitive.get_original_require(__FILE__)

# Redefine update --system to do nothing

class Gem::Commands::UpdateCommand

  alias_method :original_initialize, :initialize

  def initialize
    original_initialize

    add_option('--force-update', 'Update RubyGems system even though it is not supported.') do |value, options|
      value = true unless value
      options[:force_update] = value
    end
  end

  alias_method :original_update_rubygems, :update_rubygems

  def update_rubygems
    if options[:force_update]
      ui.alert 'TruffleRuby does not yet fully support RubyGems system update, it might break RubyGems'
      original_update_rubygems
    else
      # gem update --system is skipped on TruffleRuby as it might break RubyGems
      ui.alert_warning <<~EOS
        "gem update --system" does nothing on TruffleRuby, since it is not fully supported yet.
        Usually it is not needed to update RubyGems (e.g., for Bundler), since TruffleRuby already ships with a recent RubyGems.
        "gem update --system --force-update" can be used to force the update. However, it might break RubyGems.
      EOS
    end
  end

end unless Truffle::Boot.get_option 'testing-rubygems'
