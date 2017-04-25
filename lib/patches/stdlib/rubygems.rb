require 'openssl-stubs'
Truffle::Patching.require_original __FILE__
Truffle::Patching.install_gem_activation_hook if Truffle::Boot.patching_enabled?
