require 'openssl-stubs' if Truffle::Boot.patching_openssl_enabled?

Truffle::Patching.require_original __FILE__
Truffle::Patching.install_gem_activation_hook

# Because did_you_mean was required directly without RubyGems
if Truffle::Boot.get_option 'did_you_mean'
  begin
    gem 'did_you_mean'
  rescue LoadError => e
    Truffle::Debug.log_warning "#{File.basename(__FILE__)}:#{__LINE__} #{e.message}"
  end
end
