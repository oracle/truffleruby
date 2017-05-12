require 'openssl-stubs' if Truffle::Boot.patching_openssl_enabled?
Truffle::Patching.require_original __FILE__
Truffle::Patching.install_gem_activation_hook if Truffle::Boot.patching_enabled?
